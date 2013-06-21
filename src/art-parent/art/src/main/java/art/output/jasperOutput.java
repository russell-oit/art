package art.output;

import art.servlets.ArtDBCP;
import art.utils.ArtQuery;
import art.utils.ArtQueryParam;
import art.utils.PreparedQuery;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRAbstractLRUVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.util.JRVisitorSupport;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to generate formatted reports using the jasperreports library. Doesn't
 * implement ArtOutputInterface like other output classes
 *
 * @author Timothy Anyona
 */
public class jasperOutput {

	final static Logger logger = LoggerFactory.getLogger(jasperOutput.class);
	String fullFileName = "-No File";
	String queryName;
	String fileUserName;
	String outputFormat;
	String y_m_d;
	String h_m_s;
	String exportPath;
	String virtualizer = "swap";
	PrintWriter htmlout;
	private List<String> completedSubReports = new ArrayList<String>();

	/**
	 * Set html output object
	 *
	 * @param o html output object
	 */
	public void setWriter(PrintWriter o) {
		htmlout = o;
	}

	/**
	 * Set the virtualizer to use
	 *
	 * @param value the virtualizer to use
	 */
	public void setVirtualizer(String value) {
		virtualizer = value;
	}

	/**
	 * Set the directory where the file is to be saved
	 *
	 * @param s directory where the file is to be saved
	 */
	public void setExportPath(String s) {
		exportPath = s;
	}

	/**
	 * Get the full filename where the output has been saved
	 *
	 * @return full filename where the output has been saved
	 */
	public String getFileName() {
		return fullFileName;
	}

	/**
	 * Set the query name to be used in the file name
	 *
	 * @param s query name to be used in the file name
	 */
	public void setQueryName(String s) {
		queryName = s;
	}

	/**
	 * Set the username to be used in the file name
	 *
	 * @param s username to be used in the file name
	 */
	public void setFileUserName(String s) {
		fileUserName = s;
	}

	/**
	 * Set the output format
	 *
	 * @param s output format
	 */
	public void setOutputFormat(String s) {
		outputFormat = s;
	}

	/**
	 * Generate output and set final filename
	 *
	 * @param rs query resultset
	 * @param queryId query id
	 * @param inlineParams inline parameters
	 * @param multiParams multi parameters
	 */
	public void createFile(ResultSet rs, int queryId, Map<String, String> inlineParams, Map<String, String[]> multiParams, Map<String, ArtQueryParam> htmlParams) {

		Connection connQuery = null;
		Connection connArt = null;

		JRAbstractLRUVirtualizer tmpVirtualizer = null;

		try {
			String templateFileName;
			int datasourceId;
			String querySql;

			//get query datasource and template file name
			connArt = ArtDBCP.getConnection();
			ArtQuery aq = new ArtQuery();
			aq.create(connArt, queryId);
			templateFileName = aq.getTemplate();
			datasourceId = aq.getDatabaseId();
			querySql = aq.getText();

			String baseFileName = FilenameUtils.getBaseName(templateFileName);
			String templatesPath = ArtDBCP.getTemplatesPath();
			String jasperFilePath = templatesPath + baseFileName + ".jasper";
			String jrxmlFilePath = templatesPath + baseFileName + ".jrxml";

			File jasperFile = new File(jasperFilePath);
			File jrxmlFile = new File(jrxmlFilePath);

			String interactiveLink;

			//only proceed if template file available
			if (!jasperFile.exists() && !jrxmlFile.exists()) {
				//template file doesn't exist.
				logger.warn("Template file not found: {}.jrxml", templatesPath + baseFileName);

				fullFileName = "-Template file not found";

				//display error message instead of link when running query interactively
				interactiveLink = "Template file not found. Please contact the ART administrator.";
			} else {
				//compile report and subreports if necessary
				compileReport(baseFileName);

				//set report parameters
				Map<String, Object> params = new HashMap<String, Object>();

				//process inline parameters to obtain appropriate jasper data type objects and multi parameters to obtain parameter names instead of parameter identifiers
				PreparedQuery pq = new PreparedQuery();
				pq.setQueryId(queryId);
				pq.setInlineParams(inlineParams);
				pq.setMultiParams(multiParams);

				params.putAll(pq.getJasperInlineParams(querySql));
				params.putAll(pq.getJasperMultiParams(querySql));

				//set virtualizer properties, if virtualizer is to be used
				Properties props = new Properties();
				String sep = File.separator;
				String settingsFilePath = ArtDBCP.getAppPath() + sep + "WEB-INF" + sep + "classes" + sep + "jasper.properties";
				File settingsFile = new File(settingsFilePath);
				if (settingsFile.exists()) {
					FileInputStream o = new FileInputStream(settingsFilePath);
					try {
						props.load(o);
					} finally {
						o.close();
					}
				}

				//finalize properties object. use values from the properties file if they exist and set defaults for those that don't exist
				final String VIRTUALIZER = "virtualizer";
				final String SWAP_MAX_SIZE = "swap.maxSize";
				final String SWAP_BLOCK_SIZE = "swap.blockSize";
				final String SWAP_MIN_GROW_COUNT = "swap.minGrowCount";
				final String FILE_MAX_SIZE = "file.maxSize";
				final String GZIP_MAX_SIZE = "gzip.maxSize";

				if (props.getProperty(VIRTUALIZER) == null) {
					props.setProperty(VIRTUALIZER, "swap");
				}
				if (props.getProperty(SWAP_MAX_SIZE) == null) {
					props.setProperty(SWAP_MAX_SIZE, "300");
				}
				if (props.getProperty(SWAP_BLOCK_SIZE) == null) {
					props.setProperty(SWAP_BLOCK_SIZE, "4096");
				}
				if (props.getProperty(SWAP_MIN_GROW_COUNT) == null) {
					props.setProperty(SWAP_MIN_GROW_COUNT, "1024");
				}
				if (props.getProperty(FILE_MAX_SIZE) == null) {
					props.setProperty(FILE_MAX_SIZE, "300");
				}
				if (props.getProperty(GZIP_MAX_SIZE) == null) {
					props.setProperty(GZIP_MAX_SIZE, "300");
				}

				//use virtualizer if required
				if (!props.getProperty(VIRTUALIZER).equals("none")) {
					if (props.getProperty(VIRTUALIZER).equals("file")) {
						int maxSize = Integer.parseInt(props.getProperty(FILE_MAX_SIZE));
						tmpVirtualizer = new JRFileVirtualizer(maxSize, System.getProperty("java.io.tmpdir"));
						params.put(JRParameter.REPORT_VIRTUALIZER, tmpVirtualizer);
					} else if (props.getProperty(VIRTUALIZER).equals("gzip")) {
						int maxSize = Integer.parseInt(props.getProperty(GZIP_MAX_SIZE));
						tmpVirtualizer = new JRGzipVirtualizer(maxSize);
						params.put(JRParameter.REPORT_VIRTUALIZER, tmpVirtualizer);
					} else {
						//use swap virtualizer by default
						int maxSize = Integer.parseInt(props.getProperty(SWAP_MAX_SIZE));
						int blockSize = Integer.parseInt(props.getProperty(SWAP_BLOCK_SIZE));
						int minGrowCount = Integer.parseInt(props.getProperty(SWAP_MIN_GROW_COUNT));

						JRSwapFile swapFile = new JRSwapFile(System.getProperty("java.io.tmpdir"), blockSize, minGrowCount);
						tmpVirtualizer = new JRSwapFileVirtualizer(maxSize, swapFile);
						params.put(JRParameter.REPORT_VIRTUALIZER, tmpVirtualizer);
					}
				}

				//fill report with data
				JasperPrint jasperPrint;
				if (rs == null) {
					//use template query

					//use dynamic datasource if so configured
					boolean useDynamicDatasource = false;

					if (htmlParams != null) {
						for (Map.Entry<String, ArtQueryParam> entry : htmlParams.entrySet()) {
							ArtQueryParam param = entry.getValue();
							String paramDataType = param.getParamDataType();

							if (StringUtils.equalsIgnoreCase(paramDataType, "DATASOURCE")) {
								//get dynamic connection to use
								Object paramValueObject = param.getParamValue();
								if (paramValueObject != null) {
									String paramValue = (String) paramValueObject;
									if (StringUtils.isNotBlank(paramValue)) {
										useDynamicDatasource = true;
										if (NumberUtils.isNumber(paramValue)) {
											//use datasource id
											connQuery = ArtDBCP.getConnection(Integer.parseInt(paramValue));
										} else {
											//use datasource name
											connQuery = ArtDBCP.getConnection(paramValue);
										}
									}
								}
								break;
							}
						}
					}

					if (!useDynamicDatasource) {
						//not using dynamic datasource. use datasource defined on the query
						connQuery = ArtDBCP.getConnection(datasourceId);
					}
					jasperPrint = JasperFillManager.fillReport(jasperFilePath, params, connQuery);
				} else {
					//use recordset based on art query
					JRResultSetDataSource ds;
					ds = new JRResultSetDataSource(rs);
					jasperPrint = JasperFillManager.fillReport(jasperFilePath, params, ds);
				}

				//set virtualizer read only to optimize performance. must be set after print object has been generated
				if (tmpVirtualizer != null) {
					tmpVirtualizer.setReadOnly(true);
				}

				// Build output filename base
				java.util.Date today = new java.util.Date();

				String dateFormat = "yyyy_MM_dd";
				SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
				y_m_d = dateFormatter.format(today);

				String timeFormat = "HH_mm_ss";
				SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
				h_m_s = timeFormatter.format(today);

				String fileName = fileUserName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ArtDBCP.getRandomString();
				fileName = ArtDBCP.cleanFileName(fileName); //replace characters that would make an invalid filename
				String fullFileNameWithoutExt = exportPath + fileName;

				//export report
				if (StringUtils.equals(outputFormat, "pdf")) {
					fullFileName = fullFileNameWithoutExt + ".pdf";
					fileName = fileName + ".pdf";
					JasperExportManager.exportReportToPdfFile(jasperPrint, fullFileName);
				} else if (StringUtils.equals(outputFormat, "html")) {
					fullFileName = fullFileNameWithoutExt + ".html";
					fileName = fileName + ".html";

					JRXhtmlExporter exporter = new JRXhtmlExporter();

					exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
					exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, fullFileName);

					exporter.exportReport();
				} else if (StringUtils.equals(outputFormat, "xls")) {
					fullFileName = fullFileNameWithoutExt + ".xls";
					fileName = fileName + ".xls";

					JRXlsExporter exporter = new JRXlsExporter();

					exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
					exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, fullFileName);
					exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);

					exporter.exportReport();
				} else if (StringUtils.equals(outputFormat, "xlsx")) {
					fullFileName = fullFileNameWithoutExt + ".xlsx";
					fileName = fileName + ".xlsx";

					JRXlsxExporter exporter = new JRXlsxExporter();

					exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
					exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, fullFileName);
					exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
					exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);

					exporter.exportReport();
				}

				interactiveLink = "<a type=\"application/octet-stream\" href=\"../export/" + fileName + "\"> "
						+ fileName + "</a>";

			}

			//display link to access report if run interactively
			if (htmlout != null) {
				htmlout.println("<p><div align=\"center\"><table border=\"0\" width=\"90%\">");
				htmlout.println("<tr><td colspan=\"2\" class=\"data\" align=\"center\" >"
						+ interactiveLink
						+ "</td></tr>");
				htmlout.println("</table></div></p>");
			}
		} catch (Exception e) {
			logger.error("Error", e);
			if (htmlout != null) {
				//display error message on browser
				htmlout.println("<b>Error while generating report:</b> <p>" + e + "</p>");
			}
		} finally {
			try {
				if (connQuery != null) {
					connQuery.close();
				}
				if (connArt != null) {
					connArt.close();
				}
				if (tmpVirtualizer != null) {
					tmpVirtualizer.cleanup();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}
	}

	/**
	 * Recursively compile report and subreports
	 */
	public void compileReport(String baseFileName) {

		try {
			String templatesPath = ArtDBCP.getTemplatesPath();
			String jasperFilePath = templatesPath + baseFileName + ".jasper";
			String jrxmlFilePath = templatesPath + baseFileName + ".jrxml";

			File jasperFile = new File(jasperFilePath);
			File jrxmlFile = new File(jrxmlFilePath);

			//compile report if .jasper doesn't exist or is outdated
			if (!jasperFile.exists() || (jasperFile.lastModified() < jrxmlFile.lastModified())) {
				JasperCompileManager.compileReportToFile(jrxmlFilePath, jasperFilePath);
			}
			//load report object
			JasperReport jasperReport = (JasperReport) JRLoader.loadObjectFromFile(jasperFilePath);

			//Compile sub reports
			JRElementsVisitor.visitReport(jasperReport, new JRVisitorSupport() {
				@Override
				public void visitSubreport(JRSubreport subreport) {
					try {
						String subreportName = subreport.getExpression().getText().replace("\"", ""); //file name is quoted
						subreportName=StringUtils.substringBeforeLast(subreportName, ".");
						//Sometimes the same subreport can be used multiple times, but
						//there is no need to compile multiple times
						if (completedSubReports.contains(subreportName)) {
							return;
						}
						completedSubReports.add(subreportName);
						compileReport(subreportName);
					} catch (Exception e) {
						logger.error("Error", e);
					}
				}
			});

		} catch (Exception e) {
			logger.error("Error", e);
		}
	}
}