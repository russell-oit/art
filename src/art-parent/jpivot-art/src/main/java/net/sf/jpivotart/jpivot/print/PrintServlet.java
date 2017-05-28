/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * Copyright (C) 2003-2004 TONBELLER AG.
 * All Rights Reserved.
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package net.sf.jpivotart.jpivot.print;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import net.sf.jpivotart.jpivot.chart.ChartComponent;
import net.sf.jpivotart.jpivot.table.TableComponent;
import net.sf.wcfart.wcf.component.RendererParameters;
import net.sf.wcfart.wcf.controller.RequestContext;
import net.sf.wcfart.wcf.controller.RequestContextFactoryFinder;
import net.sf.wcfart.wcf.utils.XmlUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.xml.sax.SAXException;

/**
 * Expected HTTP GET Parameters: - cube - the jpivot cube id, used to lookup
 * table, chart, and print references - type - the output type, 0 for xls, 1 for
 * pdf - filenamePre - (optional) - defaults to xls_export, specifies the
 * filename the browser will use to name the output.
 *
 * @author arosselet
 */
public class PrintServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(PrintServlet.class);
	private static final int XML = 0;
	private static final int PDF = 1;
	String basePath;
	private FopFactory fopFactory;
	private TransformerFactory tFactory = TransformerFactory.newInstance();
	String foXslFilename = "fo_mdxtable.xsl";
	String foXslFilePath;

	/**
	 * Initializes the servlet.
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			foXslFilePath = config.getServletContext().getRealPath("/WEB-INF/jpivot/table/" + foXslFilename);
			//https://xmlgraphics.apache.org/fop/2.2/embedding.html
			// get the physical path for the config file
			String fopConfigPath = config.getServletContext().getRealPath("/WEB-INF/jpivot/print/userconfig.xml");
			fopFactory = FopFactory.newInstance(new File(fopConfigPath));
		} catch (SAXException | IOException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 *
	 * @param context
	 */
	protected void processRequest(RequestContext context) throws ServletException, IOException {
		HttpServletRequest request = context.getRequest();
		HttpServletResponse response = context.getResponse();
		if (request.getParameter("cube") != null && request.getParameter("type") != null) {
			try {
				String xslUri = null;
				String filename = null;
				int type = Integer.parseInt(request.getParameter("type"));
				String filenamePre;
				if (request.getParameter("filenamePre") != null) {
					filenamePre = request.getParameter("filenamePre");
				} else if (type == PDF) {
					filenamePre = "pdf_export";
				} else {
					filenamePre = "xls_export";
				}

				switch (type) {
					case XML:
						xslUri = "/WEB-INF/jpivot/table/xls_mdxtable.xsl";
						RendererParameters.setParameter(context.getRequest(), "mode", "excel", "request");
						response.setContentType("application/vnd.ms-excel");
						filename = filenamePre + ".xls";
						break;
					case PDF:
						xslUri = "/WEB-INF/jpivot/table/" + foXslFilename;
						RendererParameters.setParameter(context.getRequest(), "mode", "print", "request");
						response.setContentType("application/pdf");
						filename = filenamePre + ".pdf";
						break;
				}
				if (xslUri != null) {
					boolean xslCache = true;
					// get references to needed elements
					String tableRef = "table" + request.getParameter("cube");
					String chartRef = "chart" + request.getParameter("cube");
					String printRef = "print" + request.getParameter("cube");

					Map<String, Object> parameters = new HashMap<>();

					OutputStream outStream = response.getOutputStream();
					PrintWriter out = new PrintWriter(outStream);
					HttpSession session = request.getSession();
					// set up filename for download.
					response.setHeader("Content-Disposition", "attachment; filename=" + filename);

					// get TableComponent
					TableComponent table = (TableComponent) context.getModelReference(tableRef);
					// only proceed if table component exists
					if (table != null) {
						// add parameters from printConfig
						PrintComponent printConfig = (PrintComponent) context.getModelReference(printRef);
						if (printConfig != null) {
							if (printConfig.isSetTableWidth()) {
								parameters.put(printConfig.PRINT_TABLE_WIDTH, new Double(printConfig.getTableWidth()));
							}
							if (printConfig.getReportTitle().trim().length() != 0) {
								parameters.put(printConfig.PRINT_TITLE, printConfig.getReportTitle().trim());
							}
							parameters.put(printConfig.PRINT_PAGE_ORIENTATION, printConfig.getPageOrientation());
							parameters.put(printConfig.PRINT_PAPER_TYPE, printConfig.getPaperType());
							if (printConfig.getPaperType().equals("custom")) {
								parameters.put(printConfig.PRINT_PAGE_WIDTH, new Double(printConfig.getPageWidth()));
								parameters.put(printConfig.PRINT_PAGE_HEIGHT, new Double(printConfig.getPageHeight()));
							}
							parameters.put(printConfig.PRINT_CHART_PAGEBREAK, new Boolean(printConfig.isChartPageBreak()));

						}

						// add parameters and image from chart if visible
						ChartComponent chart = (ChartComponent) request.getSession().getAttribute(chartRef);
						if (chart != null && chart.isVisible()) {

							String host = request.getServerName();
							int port = request.getServerPort();
							String location = request.getContextPath();
							String scheme = request.getScheme();
							if (type == PDF) {
								String chartFilename = chart.getFilename();
								if (chartFilename.indexOf("..") >= 0) {
									throw new ServletException("File '" + chartFilename + "' does not exist within temp directory.");
								}
								File file = new File(System.getProperty("java.io.tmpdir"), chartFilename);
								if (!file.exists()) {
									throw new ServletException("File '" + file.getAbsolutePath() + "' does not exist.");
								}
								parameters.put("chartimage", "file:" + file.getCanonicalPath());
							} else {
								String chartServlet = scheme + "://" + host + ":" + port + location + "/GetChart";
								parameters.put("chartimage", chartServlet + "?filename=" + chart.getFilename());
							}
							parameters.put("chartheight", new Integer(chart.getChartHeight()));
							parameters.put("chartwidth", new Integer(chart.getChartWidth()));
						}

						//parameters.put("message",table.getReportTitle());
						// add "context" and "renderId" to parameter map
						//parameters.put("renderId", renderId);
						parameters.put("context", context.getRequest().getContextPath());

						// Some FOP-PDF versions require a complete URL, not a path
						//parameters.put("contextUrl", createContextURLValue(context));
						table.setDirty(true);
						Document document = table.render(context);
						table.setDirty(true);

						DOMSource source = new DOMSource(document);
						// set up xml transformation
						Transformer transformer = XmlUtils.getTransformer(session, xslUri, xslCache);
						for (Iterator<String> it = parameters.keySet().iterator(); it.hasNext();) {
							String name = it.next();
							Object value = parameters.get(name);
							transformer.setParameter(name, value);
						}
						StringWriter sw = new StringWriter();
						StreamResult result = new StreamResult(sw);
						//do transform
						transformer.transform(source, result);
						sw.flush();

						// if this is XML, then we are done, so output xml file.
						if (type == XML) {
							response.setContentLength(sw.toString().length());
							out.write(sw.toString());
							RendererParameters.removeParameter(context.getRequest(), "mode", "excel", "request");
						} else if (type == PDF) {
							// need to generate PDF from the FO xml
							try {
								//https://xmlgraphics.apache.org/fop/2.2/servlets.html
								ByteArrayInputStream bain = new ByteArrayInputStream(sw.toString().getBytes("UTF-8"));
								ByteArrayOutputStream baout = new ByteArrayOutputStream();

								Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, baout);

								//Setup Transformer
								Source xsltSrc = new StreamSource(new File(foXslFilePath));
								Transformer transformer2 = tFactory.newTransformer(xsltSrc);

								//Make sure the XSL transformation's result is piped through to FOP
								Result res = new SAXResult(fop.getDefaultHandler());

								//Setup input
								Source src = new StreamSource(bain);

								//Start the transformation and rendering process
								transformer2.transform(src, res);

								final byte[] content = baout.toByteArray();
								response.setContentLength(content.length);
								outStream.write(content);
								RendererParameters.removeParameter(context.getRequest(), "mode", "print", "request");
							} catch (Exception e) {
								logger.error("Error", e);
							}
						}
						//close output streams
						out.flush();
						out.close();
						outStream.flush();
					}
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}
	}

	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestContext context = RequestContextFactoryFinder.createContext(request, response, true);
		try {
			processRequest(context);
		} finally {
			context.invalidate();
		}
	}

	/**
	 * Returns a short description of the servlet.
	 */
	public String getServletInfo() {
		return "Export OLAP table";
	}

}
