/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.report;

import art.enums.ReportStatus;
import art.enums.ReportType;
import art.graph.ArtCategorySeries;
import art.graph.ArtDateSeries;
import art.graph.ArtGraph;
import art.graph.ArtPie;
import art.graph.ArtSpeedometer;
import art.graph.ArtTimeSeries;
import art.graph.ArtXY;
import art.graph.ArtXYZChart;
import art.output.ReportOutputHandler;
import art.output.ReportOutputInterface;
import art.output.htmlDataTableOutput;
import art.output.htmlPlainOutput;
import art.output.htmlReportOutWriter;
import art.output.JasperReportsOutput;
import art.output.jxlsOutput;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.reportparameter.ReportParameter;
import art.reportparameter.ReportParameterService;
import art.servlets.ArtConfig;
import art.user.User;
import art.utils.ActionResult;
import art.utils.ArtHelper;
import art.utils.ArtQuery;
import art.utils.ArtQueryParam;
import art.utils.ArtUtils;
import art.utils.DrilldownQuery;
import art.utils.ParameterProcessor;
import art.utils.UserEntity;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for run report process
 *
 * @author Timothy Anyona
 */
@Controller
public class RunReportController {

	private static final Logger logger = LoggerFactory.getLogger(RunReportController.class);

	private int runningReportsCount = 0;

	@Autowired
	private ReportService reportService;

	@Autowired
	private ServletContext ctx;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ReportParameterService reportParameterService;

	//use post to allow for large parameter input and get to allow for direct url execution
	@RequestMapping(value = "/app/runReport", method = {RequestMethod.GET, RequestMethod.POST})
	public String runReport(@RequestParam("reportId") Integer reportId,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, Model model, Locale locale) throws IOException {

		//check if output is being displayed within the show report page (inline) or in a new page
		boolean showInline = Boolean.valueOf(request.getParameter("showInline"));

		String errorPage;
		if (showInline) {
			errorPage = "reportErrorInline";
		} else {
			errorPage = "reportError";
		}

		Report report;

		try {
			report = reportService.getReport(reportId);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
			return errorPage;
		}

		User sessionUser = (User) session.getAttribute("sessionUser");
		String username = sessionUser.getUsername();

		//check if report can be run
		if (report == null) {
			model.addAttribute("message", "reports.message.reportNotFound");
			return errorPage;
		}

		model.addAttribute("reportName", report.getName());

		//admins can run all reports, even disabled ones. only check for non admin users
		if (!sessionUser.isAdminUser()) {
			if (report.getReportStatus() == ReportStatus.Disabled) {
				model.addAttribute("message", "reports.message.reportDisabled");
				return errorPage;
			}

			try {
				if (!reportService.canUserRunReport(sessionUser.getUserId(), reportId)) {
					model.addAttribute("message", "reports.message.noPermission");
					return errorPage;
				}
			} catch (SQLException ex) {
				logger.error("Error", ex);
				model.addAttribute("error", ex);
				return errorPage;
			}

			if (runningReportsCount > ArtConfig.getSettings().getMaxRunningReports()) {
				logger.warn("Report not run. Max running reports reached. user={}, report={}", sessionUser, report);
				model.addAttribute("message", "reports.message.maxRunningReportsReached");
				return errorPage;

			}
		}

		Map<String, Class<?>> directReportOutputClasses = ArtConfig.getDirectOutputReportClasses();

		// check if the html code should be rendered as an html fragmnet (without <html> and </html> tags)
		boolean isFragment = Boolean.valueOf(request.getParameter("isFragment"));

		// make sure the browser does not cache the result using Ajax (this happens in IE)
		if (isFragment) {
			response.setHeader("Cache-control", "no-cache");
		}

		String baseExportPath = ArtConfig.getExportPath();

		/*
		 * isFlushEnabled states if this servlet can produce html output.
		 * This is used to avoid to print on the page if the page control
		 * has to be redirected to another page (this is the case of graphs
		 * and output modes that do not generate html)
		 */
		boolean showHeaderAndFooter = true; // default generic HTML output
		ReportOutputInterface o = null;
		String str; //for error logging strings

		String reportName = report.getName();
		int reportTypeId = report.getReportTypeId();
		ReportType reportType = ReportType.toEnum(reportTypeId);
		String xAxisLabel = report.getxAxisLabel();
		String yAxisLabel = report.getyAxisLabel();
		String shortDescription = report.getShortDescription();
		String graphOptions = report.getChartOptionsSetting();

		request.setAttribute("reportName", reportName);

		String reportFormat = request.getParameter("reportFormat");
		if (reportFormat == null || StringUtils.equalsIgnoreCase(reportFormat, "default")) {
			if (reportType.isChart()) {
				reportFormat = "graph";
			} else if (reportType.isGroupReport()) {
				reportFormat = "htmlreport";
			} else if (reportType.isJasperReports()) {
				reportFormat = "pdf";
			} else {
				reportFormat = "html";
			}
		}

		//ensure html only reports only output as html
		if (reportType == ReportType.CrosstabHtml || reportType == ReportType.TabularHtml) {
			reportFormat = "html";
		}

		// this will be initialized according to the content type of the report output
		//setContentType must be called before getWriter
		PrintWriter out = null;

		/*
		 * Find if output allows this servlet to print the header&footer
		 * (flush active or not)
		 */
		if (StringUtils.equalsIgnoreCase(reportFormat, "SCHEDULE")) {
			// forward to the editJob page
//			ctx.getRequestDispatcher("/user/editJob.jsp").forward(request, response);
//			return; // a return is needed otherwise the flow would proceed!
			return "editJob";
		} else if (StringUtils.containsIgnoreCase(reportFormat, "graph")) {
			showHeaderAndFooter = false; // graphs are created in memory and displayed by showGraph.jsp page
		} else if (StringUtils.equalsIgnoreCase(reportFormat, "htmlreport")) {
			response.setContentType("text/html; charset=UTF-8");
			out = response.getWriter();
		} else if (reportTypeId == 115 || reportTypeId == 116 || reportTypeId == 117 || reportTypeId == 118) {
			//jasper report or jxls spreadsheet
			response.setContentType("text/html; charset=UTF-8");
			out = response.getWriter();
		} else if (reportType == ReportType.Update) {
			//update query
			response.setContentType("text/html; charset=UTF-8");
			out = response.getWriter();
		} else if (reportType == ReportType.Dashboard) {
			// forward to the showDashboard page
//			ctx.getRequestDispatcher("/user/showDashboard.jsp").forward(request, response);
//			return; // a return is needed otherwise the flow would proceed!
			return "showDashboard";
		} else if (reportType == ReportType.Text) {
			// forward to the showText page
//			ctx.getRequestDispatcher("/user/showText.jsp").forward(request, response);
//			return; // a return is needed otherwise the flow would proceed!

			//http://jsoup.org/apidocs/org/jsoup/safety/Whitelist.html
			//https://stackoverflow.com/questions/9213189/jsoup-whitelist-relaxed-mode-too-strict-for-wysiwyg-editor
			out = response.getWriter();
			String cleanSource = Jsoup.clean(report.getReportSource(), Whitelist.relaxed());
			String textOutput;
			if (showInline) {
				textOutput = cleanSource;
			} else {
				textOutput = "<div class=\"row\">"
						+ "<div class=\"col-md-10 col-md-offset-1\">"
						+ "<div id=\"reportOutput\">"
						+ cleanSource
						+ "</div></div></div>";
			}
			out.println(textOutput);
			return null;
		} else if (reportTypeId == 112 || reportTypeId == 113 || reportTypeId == 114) {
			// forward to the showAnalysis page
//			ctx.getRequestDispatcher("/user/showAnalysis.jsp").forward(request, response);
//			return; // a return is needed otherwise the flow would proceed!
			return "showAnalysis";
		} else {
			// This is not a request to schedule, produce a graph or an "htmlReport" or an update query
			// => Load the appropriate ReportOutputInterface for the view mode

			try {
				//@SuppressWarnings("rawtypes")
				Class<?> classx = directReportOutputClasses.get(reportFormat);
				o = (ReportOutputInterface) classx.newInstance();

				// Set the content type according to the object
				String contentType = o.getContentType();
				response.setContentType(contentType);
				// initialize the output stream - this is here
				// this need to appear after response.setContent!!!
				out = response.getWriter();

				// the view mode drives if this servlet uses the std header&footer,
				// if false the view mode needs to take care of all the output
				showHeaderAndFooter = o.isShowQueryHeaderAndFooter();

				if (isFragment) {
					showHeaderAndFooter = false; // disable in any case header&footer if isFragment is true
				}

			} catch (Exception e) {
				logger.error("Error while instantiating class: {}", reportFormat, e);
				request.setAttribute("errorMessage", "Error while initializing " + reportFormat + " view mode:" + e);
//				ctx.getRequestDispatcher("/user/error.jsp").forward(request, response);
//				return;
				return errorPage;
			}
		}

//		//should never be null here. explicit check for ide warnings
//		if (out == null) {
//			response.setContentType("text/html; charset=UTF-8");
//			out = response.getWriter();
//		}
		if (!showInline) {
			try {
				request.setAttribute("title", reportName);
				ctx.getRequestDispatcher("/WEB-INF/jsp/headerFragment.jsp").include(request, response);
			} catch (ServletException ex) {
				logger.error("Error", ex);
				return errorPage;
			}
			out.flush();
		}

		//output report header
		if (showHeaderAndFooter) {
			request.setAttribute("reportName", reportName);
			try {
				ctx.getRequestDispatcher("/WEB-INF/jsp/reportHeader.jsp").include(request, response);
			} catch (ServletException ex) {
				logger.error("Error", ex);
				return errorPage;
			}
			out.flush();

			//display initial report progress
			displayReportProgress(out, messageSource.getMessage("reports.message.configuring", null, locale));
		}

		//run query            
		int probe = 0; // used for debugging
		int rowsRetrieved = -1; //default to -1 in order to accomodate template reports for which you can't know the number of rows in the report

		ResultSet rs = null;
		ReportRunner reportRunner = null;

		try {
			long overallStartTime = System.currentTimeMillis(); //overall start time                    
			String startTimeString = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.MEDIUM, request.getLocale()).format(new java.util.Date(overallStartTime)); //for display in query output header

			/*
			 * Increment the currentNumberOfRunningQueries Note if an
			 * exception kills the current thread the value may be not
			 * decremented correctly This value is shown in the the
			 * "Show Connection Status" link from the Datasource page
			 * (ART Admin part)
			 */
			runningReportsCount++;

			reportRunner = new ReportRunner();
			reportRunner.setUsername(username);
			reportRunner.setReportId(reportId);
			reportRunner.setAdminSession(sessionUser.isAdminUser());

			//prepare report parameters
			ArtQuery aq = new ArtQuery();

			ParameterProcessor paramProcessor = new ParameterProcessor();
			Map<String, ReportParameter> reportParams = paramProcessor.processHttpParameters(request, reportId);

			//display parameters. contains param position and param object. use treemap so that params can be displayed in field position order
			Map<Integer, ArtQueryParam> displayParams = new TreeMap<>();

			//see if we should show parameter values in report output
			boolean showParams = Boolean.valueOf(request.getParameter("showParams"));
			if (report.isParametersInOutput()) {
				//always show params. especially for drill down reports
				showParams = true;
			}

			if (showParams) {
				//get display values for selections from lov parameters
				ParameterService parameterService = new ParameterService();

				for (Map.Entry<String, ReportParameter> entry : reportParams.entrySet()) {
					ReportParameter reportParam = entry.getValue();

					Parameter param = reportParam.getParameter();
					if (param.isUseLov()) {
						//get all possible lov values.							
						ReportRunner lovReportRunner = new ReportRunner();
						lovReportRunner.setReportId(param.getLovReportId());
						//for chained parameters, handle #filter# parameter
						int filterPosition = param.getEffectiveChainedValuePosition();
						if (filterPosition > 0) {
							//parameter chained on another parameter. get filter value
							Parameter filterParam = parameterService.getParameter(reportId, filterPosition);
							if (filterParam != null) {
								ReportParameter filterReportParam = reportParams.get(filterParam.getName());
								if (filterReportParam != null) {
									String[] filterValues = filterReportParam.getPassedParameterValues();
									lovReportRunner.setFilterValues(filterValues);
								}
							}
						}
						reportParam.setLovValues(lovReportRunner.getLovValues(false)); //false=don't apply rules
					}
				}
			}

			reportRunner.setReportParams(reportParams);

			//is scroll insensitive much slower than forward only?
			int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
//				int resultSetType;
//				if (queryType == 116 || queryType == 118) {
//					resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE; //need scrollable resultset in order to determine record count
//				} else if (StringUtils.equalsIgnoreCase(viewMode, "htmlreport")) {
//					resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
//				} else if (queryType < 0) {
//					resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE; //need scrollable resultset for graphs for show data option
//				} else {
//					resultSetType = ResultSet.TYPE_FORWARD_ONLY;
//				}

			// JavaScript code to write status
			if (showHeaderAndFooter) {
				displayReportProgress(out, messageSource.getMessage("reports.message.running", null, locale));
			}

			//run report
			long reportStartTime = System.currentTimeMillis();
			reportRunner.execute(resultSetType);
			long reportEndTime = System.currentTimeMillis();

			//get final sql with parameter placeholders replaced with parameter values
			String finalSQL = reportRunner.getFinalSQL();

			//determine if final sql should be shown. only admins can see sql
			boolean showSQL = false;
			int accessLevel = 0;
			UserEntity ue = (UserEntity) session.getAttribute("ue");
			if (ue != null) {
				accessLevel = ue.getAccessLevel();
			}
			if (request.getParameter("_showSQL") != null && accessLevel > 5) {
				showSQL = true;
			}

			//set output object properties if required
			if (o != null) {
				try {
					probe = 77;

					o.setMaxRows(ArtConfig.getMaxRows(reportFormat));
					o.setWriter(out);
					o.setQueryName(reportName);
					o.setFileUserName(username);
					o.setExportPath(baseExportPath);

					//don't set displayparams for html view modes. parameters will be displayed by this servlet
					if (!StringUtils.containsIgnoreCase(reportFormat, "html")) {
						o.setDisplayParameters(displayParams);
					}

					//ensure htmlplain output doesn't display parameters if inline
					if (o instanceof htmlPlainOutput) {
						htmlPlainOutput hpo = (htmlPlainOutput) o;
						hpo.setDisplayInline(showInline);

						//ensure parameters are displayed if not in inline mode
						hpo.setDisplayParameters(displayParams);
					}

					//enable localization for datatable output
					if (o instanceof htmlDataTableOutput) {
						htmlDataTableOutput dt = (htmlDataTableOutput) o;
						dt.setLocale(request.getLocale());
					}

				} catch (Exception e) {
					logger.error("Error setting properties for class: {}", reportFormat, e);
					request.setAttribute("errorMessage", "Error while initializing " + reportFormat + " view mode:" + e);
//						ctx.getRequestDispatcher("/user/error.jsp").forward(request, response);
//						return;
					return errorPage;
				}
			}

			// display status information, parameters and final sql
			if (showHeaderAndFooter) {
				String description = "";
				shortDescription = StringUtils.trim(shortDescription);
				if (StringUtils.length(shortDescription) > 0) {
					description = " :: " + shortDescription;
				}
				out.println("<script type=\"text/javascript\">displayReportInfo(\"<b>" + reportName + "</b>" + description + " :: " + startTimeString + "\");</script>");

				displayReportProgress(out, messageSource.getMessage("reports.message.fetchingData", null, locale));

				//display parameters
				if (showParams) {
					ReportOutputHandler.displayParameters(out, displayParams,
							messageSource.getMessage("reports.text.allItems", null, locale));
				}

				//display final sql
				if (showSQL) {
					ReportOutputHandler.displayFinalSQL(out, finalSQL);
				}

				out.flush();
			}
			probe = 90;

			//handle jasper reports output
			if (reportType == ReportType.JasperReportsArt || reportType == ReportType.JasperReportsTemplate) {
				JasperReportsOutput jrOutput = new JasperReportsOutput();
				jrOutput.setExportPath(baseExportPath);
				jrOutput.setReportFormat(reportFormat);
				jrOutput.setWriter(out);
				if (reportType == ReportType.JasperReportsTemplate) {
					//report will use query in the report template
					jrOutput.generateReport(report,reportParams);
				} else {
					//report will use data from art query
					rs = reportRunner.getResultSet();
					jrOutput.setResultSet(rs);
					jrOutput.generateReport(report,reportParams);
					rowsRetrieved = getNumberOfRows(rs);
				}
			} else if (reportTypeId == 117 || reportTypeId == 118) {
				//jxls spreadsheet
				probe = 92;
				jxlsOutput jxls = new jxlsOutput();
				jxls.setQueryName(reportName);
				jxls.setFileUserName(username);
				jxls.setExportPath(baseExportPath);
				jxls.setWriter(out);
				if (reportTypeId == 117) {
					//report will use query in the jxls template
					jxls.createFile(null, reportId, reportParams);
				} else {
					//report will use data from art query
					rs = reportRunner.getResultSet();
					jxls.createFile(rs, reportId, inlineParams, multiParams, reportParams);
					rowsRetrieved = getNumberOfRows(rs);
				}
			} else {
				//get query results
				rs = reportRunner.getResultSet();

				if (rs != null) {
					// it is a "select" query or a procedure ending with a select statement
					ResultSetMetaData rsmd = rs.getMetaData();

					try {
						if (StringUtils.equalsIgnoreCase(reportFormat, "htmlreport")) {
							/*
							 * HTML REPORT
							 */
							int splitCol;
							if (request.getParameter("SPLITCOL") == null) {
								splitCol = reportTypeId;
							} else {
								splitCol = Integer.parseInt(request.getParameter("SPLITCOL"));
							}
							rowsRetrieved = htmlReportOut(out, rs, rsmd, splitCol);
							probe = 100;
						} else if (StringUtils.containsIgnoreCase(reportFormat, "graph")) {
							/*
							 * GRAPH
							 */
							probe = 105;

							//do initial preparation of graph object
							ArtGraph ag = artGraphOut(rsmd, request, graphOptions, shortDescription, reportTypeId);

							//set some graph properties
							ag.setXAxisLabel(xAxisLabel);
							if (yAxisLabel == null) {
								yAxisLabel = rsmd.getColumnLabel(1);
							}
							ag.setYAxisLabel(yAxisLabel);

							if (showParams) {
								request.setAttribute("showParams", "true");
								ag.setDisplayParameters(displayParams);
							}

							//add drill down queries
							Map<Integer, DrilldownQuery> drilldownQueries = aq.getDrilldownQueries(reportId);

							//build graph dataset
							ag.prepareDataset(rs, drilldownQueries, inlineParams, multiParams);

							//set other properties relevant for the graph display
							if (showSQL) {
								request.setAttribute("showSQL", "true");
								request.setAttribute("finalSQL", finalSQL);
							}

							//enable file names to contain query name
							//set base file name
							java.util.Date today = new java.util.Date();
							SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
							String filename = username + "-" + reportName + "-" + dateFormatter.format(today) + ArtUtils.getRandomFileNameString();
							filename = ArtUtils.cleanFileName(filename);
							request.setAttribute("baseFileName", filename);

							//allow direct url not to need _query_id
							request.setAttribute("queryType", Integer.valueOf(reportTypeId));

							//allow use of both QUERY_ID and queryId in direct url
							request.setAttribute("queryId", Integer.valueOf(reportId));

							//pass graph object
							request.setAttribute("artGraph", ag);

							probe = 110;
						} else {
							ActionResult outputResult;

							if (reportTypeId == 101 || reportTypeId == 102) {
								/*
								 * CROSSTAB
								 */
								outputResult = ReportOutputHandler.flushXOutput(o, rs);
							} else {
								/*
								 * NORMAL TABULAR OUTPUT
								 */

								//add support for drill down queries
								Map<Integer, DrilldownQuery> drilldownQueries = null;
								if (StringUtils.containsIgnoreCase(reportFormat, "html")) {
									//only drill down for html output. drill down query launched from hyperlink                                            
									drilldownQueries = aq.getDrilldownQueries(reportId);
								}
								outputResult = ReportOutputHandler.flushOutput(o, rs, drilldownQueries, request.getContextPath(), inlineParams, multiParams);

							}

							if (outputResult.isSuccess()) {
								rowsRetrieved = (Integer) outputResult.getData();
							} else {
								model.addAttribute("message", outputResult.getMessage());
								return errorPage;
							}
						}

					} catch (Exception ex) {
						logger.error("Error. report={}, user={}", report, sessionUser, ex);
						model.addAttribute("error", ex);
						return errorPage;
					}

				} else {
					//this is an update query
					int rowsUpdated = reportRunner.getUpdateCount(); // will be -1 if query has multiple statements
					request.setAttribute("rowsUpdated", "" + rowsUpdated);
//						ctx.getRequestDispatcher("/user/updateExecuted.jsp").include(request, response);
				}
			}

			// Print the "working" time elapsed
			// The "working"  time elapsed is the time elapsed
			// from the when the query is created  (doPost()) to now (endTime).
			// The time elapsed from a user perspective can be bigger because the servlet output
			// is "cached and transmitted" over the network by the servlet engine.
			long endTime = new java.util.Date().getTime();

			long totalTime = (endTime - overallStartTime) / (1000);
			long fetchTime = (reportEndTime - reportStartTime) / (1000);
			double preciseTotalTime = (endTime - overallStartTime) / (double) 1000;
			NumberFormat nf = NumberFormat.getInstance(request.getLocale());
			DecimalFormat df = (DecimalFormat) nf;
			df.applyPattern("#,##0.0##");

			String msg;

			if (showHeaderAndFooter) {
				Object[] value = {
					df.format(preciseTotalTime)
				};
				msg = messageSource.getMessage("reports.text.timeTakenInSeconds", value, locale);
				request.setAttribute("timeTaken", msg);
				if (rowsRetrieved == -1) {
					msg = messageSource.getMessage("reports.text.unknown", null, locale);
					request.setAttribute("rowsRetrieved", msg);
				} else {
					df.applyPattern("#,##0");
					request.setAttribute("rowsRetrieved", df.format(rowsRetrieved));
				}

				try {
					ctx.getRequestDispatcher("/WEB-INF/jsp/reportFooter.jsp").include(request, response);
				} catch (ServletException ex) {
					logger.error("Error", ex);
					return errorPage;
				}

				//clear report progress
				displayReportProgress(out, "");
			}

			if (!showInline) {
				try {
					ctx.getRequestDispatcher("/WEB-INF/jsp/footerFragment.jsp").include(request, response);
				} catch (ServletException ex) {
					logger.error("Error", ex);
					return errorPage;
				}
			}

			ArtHelper.log(username, "query", request.getRemoteAddr(), reportId, totalTime, fetchTime, "query, " + reportFormat);
			probe = 200;

			if (StringUtils.containsIgnoreCase(reportFormat, "graph")) {
				//graph. set output format and forward to the rendering page
				if (StringUtils.equalsIgnoreCase(reportFormat, "graph")) {
					//only display graph on the browser
					request.setAttribute("outputToFile", "nofile");
				} else if (StringUtils.equalsIgnoreCase(reportFormat, "pdfgraph")) {
					//additionally generate graph in a pdf file
					request.setAttribute("outputToFile", "pdf");
				} else if (StringUtils.equalsIgnoreCase(reportFormat, "pnggraph")) {
					//additionally generate graph as a png file
					request.setAttribute("outputToFile", "png");
				}

//					ctx.getRequestDispatcher("/user/showGraph.jsp").forward(request, response);
				return "showGraph";
			}

		} catch (Exception e) { // we can't dispatch this error to a new page since we need to know if this servlet has already flushed something                    
			str = "Error while running query ID " + reportId + ", execution for user " + username + ", for session id " + session.getId() + ", at position: " + probe;
			logger.error("Error: {}", str, e);

//			String msg=messageSource.getMessage("page.message.errorOccurred", null, locale)
//					+ "<hr>" + messageSource.getMessage("step",null,locale) 
//					+ ":" + probe + "<br><code>" + e + "</code>";
//
//			request.setAttribute("message", msg );
			if (!showInline) { // we already flushed something: let's include the page
				request.setAttribute("headerOff", "true");
				request.setAttribute("error", e);
				try {
					ctx.getRequestDispatcher("/WEB-INF/jsp/reportErrorInline.jsp").include(request, response);
				} catch (ServletException ex) {
					logger.error("Error", ex);
					return errorPage;
				}

			} else { // let's forward to the error page
//					ctx.getRequestDispatcher("/user/error.jsp").forward(request, response);
				model.addAttribute("error", e);
				return errorPage;
			}
			// do not put a return here otherwise we risk the open connection to be maintained out of the pool

		} finally {
			// Decrement the currentNumberOfRunningQueries
			// close statements and return the connection to the pool
			try {
				if (rs != null) {
					rs.close();
				}
				if (reportRunner != null) {
					reportRunner.close();
				}
				runningReportsCount--;
			} catch (SQLException e) {
				runningReportsCount--;
				logger.error("Error while closing connections", e);
			}
		}

		return null;

	} // end doPost

	/**
	 * Generate a group report
	 *
	 * @param out
	 * @param rs
	 * @param rsmd
	 * @param splitCol
	 * @return number of rows output
	 * @throws SQLException
	 */
	private int htmlReportOut(PrintWriter out, ResultSet rs, ResultSetMetaData rsmd, int splitCol)
			throws SQLException {
		int col_count = rsmd.getColumnCount();
		int i;
		int counter = 0;
		htmlReportOutWriter o = new htmlReportOutWriter(out);
		String tmpstr;
		StringBuffer cmpStr; // temporary string used to compare values
		StringBuffer tmpCmpStr; // temporary string used to compare values

		// Report, is intended to be something like that:
		/*
		 * ------------------------------------- | Attr1 | Attr2 | Attr3 | //
		 * Main header ------------------------------------- | Value1 | Value2 |
		 * Value3 | // Main Data -------------------------------------
		 *
		 * -----------------------------... | SubAttr1 | Subattr2 |... // Sub
		 * Header -----------------------------... | SubValue1.1 | SubValue1.2
		 * |... // Sub Data -----------------------------... | SubValue2.1 |
		 * SubValue2.2 |... -----------------------------...
		 * ................................ ................................
		 * ................................
		 *
		 * etc...
		 */
		// Build main header HTML
		for (i = 0; i < (splitCol); i++) {
			tmpstr = rsmd.getColumnLabel(i + 1);
			o.addCellToMainHeader(tmpstr);
		}
		// Now the header is completed

		// Build the Sub Header
		for (; i < col_count; i++) {
			tmpstr = rsmd.getColumnLabel(i + 1);
			o.addCellToSubHeader(tmpstr);
		}

		int maxRows = ArtConfig.getMaxRows("htmlreport");

		while (rs.next() && counter < maxRows) {
			// Separators
			out.println("<br><hr style=\"width:90%;height:1px\"><br>");

			// Output Main Header and Main Data
			o.header(90);
			o.printMainHeader();
			o.beginLines();
			cmpStr = new StringBuffer();

			// Output Main Data (only one row, obviously)
			for (i = 0; i < splitCol; i++) {
				o.addCellToLine(rs.getString(i + 1));
				cmpStr.append(rs.getString(i + 1));
			}
			o.endLines();
			o.footer();

			// Output Sub Header and Sub Data
			o.header(80);
			o.printSubHeader();
			o.beginLines();

			// Output Sub Data (first line)
			for (; i < col_count; i++) {
				o.addCellToLine(rs.getString(i + 1));
			}

			boolean currentMain = true;
			while (currentMain && counter < maxRows) {  // next line
				// Get Main Data in order to compare it
				if (rs.next()) {
					counter++;
					tmpCmpStr = new StringBuffer();

					for (i = 0; i < splitCol; i++) {
						tmpCmpStr.append(rs.getString(i + 1));
					}

					if (tmpCmpStr.toString().equals(cmpStr.toString()) == true) { // same Main
						o.newLine();
						// Add data lines
						for (; i < col_count; i++) {
							o.addCellToLine(rs.getString(i + 1));
						}
					} else {
						o.endLines();
						o.footer();
						currentMain = false;
						rs.previous();
					}
				} else {
					currentMain = false;
					// The outer and inner while will exit
				}
			}
		}

		if (!(counter < maxRows)) {
			o.newLine();
			o.addCellToLine("<blink>Too many rows (>" + maxRows
					+ ")! Data not completed. Please narrow your search or use the xlsx, slk or tsv view modes</blink>", "qeattr", "left", col_count);
		}

		o.endLines();
		o.footer();

		return counter + 1; // number of rows
	}

	//prepare graph object for it's display using the showGraph.jsp page
	private ArtGraph artGraphOut(ResultSetMetaData rsmd, HttpServletRequest request,
			String graphOptions, String shortDescr, int queryType) throws SQLException {

		ArtGraph o;

		switch (queryType) {
			case -1:
				o = new ArtXY();
				break;
			case -2:
			case -13:
				o = new ArtPie();
				break;
			case -6:
				o = new ArtTimeSeries();
				break;
			case -7:
				o = new ArtDateSeries();
				break; //  this line was missing... added thanks to anonymous post in sf
			case -10:
				o = new ArtSpeedometer();
				break;
			case -11:
			case -12:
				o = new ArtXYZChart();
				o.setQueryType(queryType);
				break;
			default: //-3,-4,-5,-8,-9,-14,-15,-16,-17
				o = new ArtCategorySeries();
		}

		String link = rsmd.getColumnLabel(2);
		if (link.equals("LINK")) {
			o.setUseHyperLinks(true);
		} else {
			o.setUseHyperLinks(false);
		}

		int width;
		int height;
		String bgColor;

		// @200x100 -10:100 #FFFFFF nolegend nolabels
		// = make a chart 200px width 100px height focused
		//  on Y range from -10 to 100  with a white backround, without legend and labels
		String params = request.getParameter("_GRAPH_SIZE");
		boolean customParam = false;
		if (StringUtils.isNotBlank(params) && !params.equalsIgnoreCase("DEFAULT")) {
			customParam = true;
		}

		String tmp;
		String options = "";
		boolean usingShortDescription = true; //to accomodate graph options in short description string for art pre-2.0

		int indexOf = shortDescr.lastIndexOf("@");
		if (indexOf > -1) {
			options = shortDescr;
			shortDescr = shortDescr.substring(0, indexOf);
		}

		if (graphOptions != null) {
			options = graphOptions;
			usingShortDescription = false;
		}

		if (customParam) {
			tmp = params;
			usingShortDescription = false;
		} else {
			tmp = options;
		}

		//process graph options string
		ArtQuery aq = new ArtQuery();
		aq.setQueryType(queryType);
		aq.setGraphDisplayOptions(tmp, usingShortDescription);

		double yMin = aq.getGraphYMin();
		double yMax = aq.getGraphYMax();
		width = aq.getGraphWidth();
		height = aq.getGraphHeight();
		bgColor = aq.getGraphBgColor();
		boolean showLegend = aq.isShowLegend();
		boolean showLabels = aq.isShowLabels();
		boolean showPoints = aq.isShowPoints();
		boolean showGraphData = aq.isShowGraphData();
		int rotateAt = aq.getGraphRotateAt();
		int removeAt = aq.getGraphRemoveAt();

		request.setAttribute("_rotate_at", String.valueOf(rotateAt));
		request.setAttribute("_remove_at", String.valueOf(removeAt));

		if (yMin < yMax) {
			request.setAttribute("_from", String.valueOf(yMin)); //cewolf expects strings
			request.setAttribute("_to", String.valueOf(yMax));
		}

		if (!showLegend) {
			request.setAttribute("_nolegend", "true");
		}

		if (showLabels) {
			request.setAttribute("_showlabels", "true");
		} else {
			request.setAttribute("_nolabels", "true");
		}

		if (showPoints) {
			request.setAttribute("_showpoints", "true");
		}

		//override show legend, labels, data points with values selected using checkboxes.
		//only override if from showparams.jsp. if displayed from direct url or dashboard, no chance to change settings using check boxes
		if (request.getParameter("_GRAPH_SIZE") != null) {
			if (request.getParameter("_showLegend") == null) {
				request.setAttribute("_nolegend", "true");
			} else {
				request.removeAttribute("_nolegend");
			}
			if (request.getParameter("_showLabels") == null) {
				request.removeAttribute("_showlabels");
			} else {
				request.setAttribute("_showlabels", "true");
			}
			if (request.getParameter("_showDataPoints") == null) {
				request.removeAttribute("_showpoints");
			} else {
				request.setAttribute("_showpoints", "true");
			}
			if (request.getParameter("_showGraphData") == null) {
				showGraphData = false;
			} else {
				showGraphData = true;
			}
		}

		o.setTitle(shortDescr);
		o.setWidth(width);
		o.setHeight(height);
		o.setSeriesName(rsmd.getColumnLabel(2));
		o.setBgColor(bgColor);
		o.setShowGraphData(showGraphData);

		return o;
	}

	//get number of rows in a resultset.
	private int getNumberOfRows(ResultSet rs) {
		int rowsRetrieved = -1;

		try {
			if (rs != null) {
				int type = rs.getType();
				if (type == ResultSet.TYPE_SCROLL_INSENSITIVE || type == ResultSet.TYPE_SCROLL_SENSITIVE) {
					//resultset is scrollable
					rs.last();
					rowsRetrieved = rs.getRow();
					rs.beforeFirst();
				}
			}
		} catch (SQLException ex) {
			//not all drivers support this technique? If not supported, set number of rows to unknown (-1)
			rowsRetrieved = -1;
			logger.error("Error", ex);
		}

		return rowsRetrieved;
	}

	private void displayReportProgress(PrintWriter out, String message) {
		out.println("<script type='text/javascript'>displayReportProgress('"
				+ message
				+ "');</script>");
		out.flush();
	}

}
