/*
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
package art.runreport;

import art.chart.AbstractChart;
import art.chart.ChartUtils;
import art.chart.PieChart;
import art.chart.PostProcessorDefinition;
import art.chart.SpeedometerChart;
import art.chart.XYChart;
import art.dbutils.ArtDbUtils;
import art.drilldown.Drilldown;
import art.drilldown.DrilldownService;
import art.enums.ReportFormat;
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
import art.output.DirectReportOutput;
import art.output.DirectReportOutputHandler;
import art.output.HtmlDataTableOutput;
import art.output.HtmlPlainOutput;
import art.output.JasperReportsOutput;
import art.output.JxlsOutput;
import art.output.ReportOutputInterface;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.report.ChartOptions;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.servlets.ArtConfig;
import art.user.User;
import art.utils.ActionResult;
import art.utils.ArtHelper;
import art.utils.ArtQuery;
import art.utils.ArtQueryParam;
import art.utils.ArtUtils;
import art.utils.DrilldownQuery;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.RowSetDynaClass;
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
 * Controller for running reports interactively
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
	private DrilldownService drilldownService;

	//use post to allow for large parameter input and get to allow for direct url execution
	@RequestMapping(value = "/app/runReport", method = {RequestMethod.GET, RequestMethod.POST})
	public String runReport(@RequestParam("reportId") Integer reportId,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, Model model, Locale locale) throws IOException {

		Report report = null;
		User sessionUser = null;
		ResultSet rs = null;
		ReportRunner reportRunner = null;

		runningReportsCount++;

		//check if output is being displayed within the show report page (inline) or in a new page
		boolean showInline = Boolean.valueOf(request.getParameter("showInline"));

		//set appropriate error page to use
		String errorPage;
		if (showInline) {
			errorPage = "reportErrorInline";
		} else {
			errorPage = "reportError";
		}

		try {
			//get report
			report = reportService.getReport(reportId);

			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return errorPage;
			}

			//check if user has permission to run report
			//admins can run all reports, even disabled ones. only check for non admin users
			sessionUser = (User) session.getAttribute("sessionUser");
			String username = sessionUser.getUsername();

			if (!sessionUser.isAdminUser()) {
				if (report.getReportStatus() == ReportStatus.Disabled) {
					model.addAttribute("message", "reports.message.reportDisabled");
					return errorPage;
				}

				if (!reportService.canUserRunReport(sessionUser.getUserId(), reportId)) {
					model.addAttribute("message", "reports.message.noPermission");
					return errorPage;
				}

				if (runningReportsCount > ArtConfig.getSettings().getMaxRunningReports()) {
					logger.warn("Report not run. Max running reports reached. user={}, report={}", sessionUser, report);
					model.addAttribute("message", "reports.message.maxRunningReportsReached");
					return errorPage;

				}
			}

			String reportName = report.getName();
			int reportTypeId = report.getReportTypeId();
			ReportType reportType = ReportType.toEnum(reportTypeId);

			//check if the html code should be rendered as an html fragment (without <html> and </html> tags) e.g. in a dashboard section
			boolean isFragment = Boolean.valueOf(request.getParameter("isFragment"));

			//make sure the browser does not cache the result using Ajax (this happens in IE)
			if (isFragment) {
				response.setHeader("Cache-control", "no-cache");
			}

			//get report format to use
			String reportFormatString = request.getParameter("reportFormat");
			if (reportFormatString == null || StringUtils.equalsIgnoreCase(reportFormatString, "default")) {
				if (reportType.isJasperReports()) {
					reportFormatString = ReportFormat.pdf.getValue();
				} else if (reportType.isChart()) {
					reportFormatString = ReportFormat.html.getValue();
				} else {
					reportFormatString = ReportFormat.htmlDataTable.getValue();
				}
			}

			ReportFormat reportFormat = ReportFormat.toEnum(reportFormatString);

			//this will be initialized according to the content type of the report output
			//setContentType() must be called before getWriter()
			PrintWriter out = null;

			boolean showReportHeaderAndFooter = true;
			ReportOutputInterface o = null;

			if (reportType.isDirectOutput()) {
				//this is a direct output report
				Map<String, Class<?>> directReportOutputClasses = ArtConfig.getDirectOutputReportClasses();

				//@SuppressWarnings("rawtypes")
				Class<?> classx = directReportOutputClasses.get(reportFormat.getValue());
				if (classx == null) {
					throw new RuntimeException("Invalid report format: " + reportFormat.getValue());
				}

				o = (ReportOutputInterface) classx.newInstance();

				//set the content type according to the report output class
				response.setContentType(o.getContentType());
				out = response.getWriter();

				if (isFragment) {
					//report header and footer not shown for fragments
					showReportHeaderAndFooter = false;
				} else {
					//the report output class determines if the report header and footer will be shown
					//if false the output class needs to take care of all the output
					showReportHeaderAndFooter = o.isShowQueryHeaderAndFooter();
				}
			} else {
				response.setContentType("text/html; charset=UTF-8");
				out = response.getWriter();
			}

			//output page header. if showInline, page header and footer already exist. 
			if (!showInline) {
				request.setAttribute("title", reportName);
				request.setAttribute("reportFormat", reportFormat.getValue());
				ctx.getRequestDispatcher("/WEB-INF/jsp/runReportPageHeader.jsp").include(request, response);
				out.flush();
			}

			long totalTime = 0;
			long fetchTime = 0;

			if (reportType == ReportType.Text) {
				//http://jsoup.org/apidocs/org/jsoup/safety/Whitelist.html
				//https://stackoverflow.com/questions/9213189/jsoup-whitelist-relaxed-mode-too-strict-for-wysiwyg-editor
				String cleanSource = Jsoup.clean(report.getReportSource(), Whitelist.relaxed());
				request.setAttribute("reportSource", cleanSource);
				ctx.getRequestDispatcher("/WEB-INF/jsp/showText.jsp").include(request, response);
			} else if (reportType == ReportType.Dashboard) {
				// forward to the showDashboard page
//			ctx.getRequestDispatcher("/user/showDashboard.jsp").forward(request, response);
//			return; // a return is needed otherwise the flow would proceed!
				return "showDashboard";
			} else if (reportType.isOlap()) {
				// forward to the showAnalysis page
//			ctx.getRequestDispatcher("/user/showAnalysis.jsp").forward(request, response);
//			return; // a return is needed otherwise the flow would proceed!
				return "showAnalysis";
			} else {
				//output report header
				if (showReportHeaderAndFooter) {
					request.setAttribute("reportName", reportName);
					ctx.getRequestDispatcher("/WEB-INF/jsp/runReportInfoHeader.jsp").include(request, response);
					out.flush();

					//display initial report progress
					displayReportProgress(out, messageSource.getMessage("reports.message.configuring", null, locale));
				}

				//run query
				Integer rowsRetrieved = null; //use Integer in order to have unknown status e.g. for template reports for which you can't know the number of rows in the report

				long overallStartTime = System.currentTimeMillis(); //overall start time

				reportRunner = new ReportRunner();
				reportRunner.setUsername(username);
				reportRunner.setReport(report);
				reportRunner.setAdminSession(sessionUser.isAdminUser());

				//prepare report parameters
				ArtQuery aq = new ArtQuery();

				Map<String, String> inlineParams = new HashMap<>();
				Map<String, String[]> multiParams = new HashMap<>();

				ParameterProcessor paramProcessor = new ParameterProcessor();
				ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request, reportId);

				Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
				List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();
				ReportOptions reportOptions = paramProcessorResult.getReportOptions();
				ChartOptions chartOptions = paramProcessorResult.getChartOptions();

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

					for (Map.Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
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
									ReportParameter filterReportParam = reportParamsMap.get(filterParam.getName());
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

				reportRunner.setReportParamsMap(reportParamsMap);

				//is scroll insensitive much slower than forward only?
				int resultSetType;
				if (reportType == ReportType.JasperReportsArt || reportType == ReportType.JxlsArt
						|| reportType == ReportType.Group || reportType.isChart()) {
					//need scrollable resultset for jasper and jxls art report in order to display record count
					//need scrollable resultset in order to generate group report
					//need scrollable resultset for charts for show data option
					resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
				} else {
					//report types will determine the record count e.g. for direct output reports
					//or no way to determine record count e.g. with jasper reports template report
					resultSetType = ResultSet.TYPE_FORWARD_ONLY;
				}

				// JavaScript code to write status
				if (showReportHeaderAndFooter) {
					displayReportProgress(out, messageSource.getMessage("reports.message.running", null, locale));
				}

				//run query
				long queryStartTime = System.currentTimeMillis();
				reportRunner.execute(resultSetType);
				long queryEndTime = System.currentTimeMillis();

				//get final sql with parameter placeholders replaced with parameter values
				String finalSql = reportRunner.getFinalSql();

				//determine if final sql should be shown. only admins can see sql
				boolean showSql;
				if (Boolean.valueOf(request.getParameter("showSql")) && sessionUser.isAdminUser()) {
					showSql = true;
				} else {
					showSql = false;
				}

				// display status information, parameters and final sql
				if (showReportHeaderAndFooter) {
					String shortDescription = report.getShortDescription();

					String description = "";
					shortDescription = StringUtils.trim(shortDescription);
					if (StringUtils.length(shortDescription) > 0) {
						description = " :: " + shortDescription;
					}

					DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
					String startTimeString = df.format(new Date(overallStartTime));

					String reportInfo = "<b>" + reportName + "</b>" + description + " :: " + startTimeString;

					displayReportInfo(out, reportInfo);

					displayReportProgress(out, messageSource.getMessage("reports.message.fetchingData", null, locale));

					//display parameters
					if (showParams) {
						DirectReportOutputHandler.displayParameters(out, displayParams,
								messageSource.getMessage("reports.text.allItems", null, locale));
					}

					//display final sql
					if (showSql) {
						DirectReportOutputHandler.displayFinalSQL(out, finalSql);
					}

					out.flush();
				}

				//generate base components of file name to use for report types and formats that generate files
				String baseFileName = ArtUtils.getUniqueFileName(report.getReportId());
				String exportPath = ArtConfig.getReportsExportPath();

				//generate report output
				if (reportType.isJasperReports() || reportType.isJxls()) {
					String fileName = baseFileName + "." + reportFormat.getFilenameExtension();
					String outputFileName = exportPath + fileName;

					if (reportType.isJasperReports()) {
						JasperReportsOutput jrOutput = new JasperReportsOutput();
						if (reportType == ReportType.JasperReportsTemplate) {
							//report will use query in the report template
							jrOutput.generateReport(report, reportParamsList, reportType, reportFormat, outputFileName);
						} else {
							//report will use data from art query
							rs = reportRunner.getResultSet();
							jrOutput.setResultSet(rs);
							jrOutput.generateReport(report, reportParamsList, reportType, reportFormat, outputFileName);
							rowsRetrieved = getNumberOfRows(rs);
						}
					} else {
						//jxls output
						JxlsOutput jxlsOutput = new JxlsOutput();
						if (reportType == ReportType.JxlsTemplate) {
							//report will use query in the jxls template
							jxlsOutput.generateReport(report, reportParamsList, reportType, outputFileName);
						} else {
							//report will use data from art query
							rs = reportRunner.getResultSet();
							jxlsOutput.setResultSet(rs);
							jxlsOutput.generateReport(report, reportParamsList, reportType, outputFileName);
							rowsRetrieved = getNumberOfRows(rs);
						}
					}

					//display link to access report
					request.setAttribute("fileName", fileName);
					ctx.getRequestDispatcher("/WEB-INF/jsp/showFileLink.jsp").include(request, response);
				} else if (reportType == ReportType.Update) {
					int rowsUpdated = reportRunner.getUpdateCount(); // will be -1 if query has multiple statements
					request.setAttribute("rowsUpdated", "" + rowsUpdated);
//						ctx.getRequestDispatcher("/user/updateExecuted.jsp").include(request, response);
				} else if (reportType == ReportType.Group) {
					rs = reportRunner.getResultSet();

					String splitColParameter = request.getParameter("splitColumn");
					int splitCol;
					if (splitColParameter == null) {
						splitCol = reportTypeId;
					} else {
						splitCol = Integer.parseInt(splitColParameter);
					}

					rowsRetrieved = DirectReportOutputHandler.generateGroupReport(out, rs, splitCol);
				} else if (reportType.isChart()) {
					rs = reportRunner.getResultSet();

//					//do initial preparation of graph object
//					ArtGraph ag = prepareChartDetails(rs, request, report);
//
//					//set other properties relevant for the graph display
//					if (showSql) {
//						request.setAttribute("showSQL", "true");
//						request.setAttribute("finalSQL", finalSql);
//					}
//
//					if (showParams) {
//						request.setAttribute("showParams", "true");
//						ag.setDisplayParameters(displayParams);
//					}
//
//					//add drill down queries
//					Map<Integer, DrilldownQuery> drilldownQueries = aq.getDrilldownQueries(reportId);
//
//					//build graph dataset
//					ag.prepareDataset(rs, drilldownQueries, inlineParams, multiParams);
//
//					//enable file names to contain query name
//					//set base file name
//					Date today = new Date();
//					SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
//					String filename = username + "-" + reportName + "-" + dateFormatter.format(today) + ArtUtils.getRandomFileNameString();
//					filename = ArtUtils.cleanFileName(filename);
//					request.setAttribute("baseFileName", filename);
//
//					//allow direct url not to need _query_id
//					request.setAttribute("queryType", Integer.valueOf(reportTypeId));
//
//					//allow use of both QUERY_ID and queryId in direct url
//					request.setAttribute("queryId", Integer.valueOf(reportId));
//
//					//pass graph object
//					request.setAttribute("artGraph", o);
//
//					//graph. set output format and forward to the rendering page
//					if (StringUtils.equalsIgnoreCase(reportFormatString, "graph")) {
//						//only display graph on the browser
//						request.setAttribute("outputToFile", "nofile");
//					} else if (StringUtils.equalsIgnoreCase(reportFormatString, "pdfgraph")) {
//						//additionally generate graph in a pdf file
//						request.setAttribute("outputToFile", "pdf");
//					} else if (StringUtils.equalsIgnoreCase(reportFormatString, "pnggraph")) {
//						//additionally generate graph as a png file
//						request.setAttribute("outputToFile", "png");
//					}
					AbstractChart chart;
					switch (reportType) {
						case Pie2DChart:
						case Pie3DChart:
							chart = new PieChart(reportType);
							break;
						case SpeedometerChart:
							chart = new SpeedometerChart();
							break;
						case XYChart:
							chart=new XYChart();
							break;
						default:
							throw new IllegalArgumentException("Unexpected chart report type" + reportType);
					}

					chart.setLocale(request.getLocale());
					chart.setChartOptions(chartOptions);

					//TODO set effective chart options. default to report options but override with html parameters
					//TODO set default label format. {2} for category based charts
					//{0} ({2}) for pie chart html output
					//{0} = {1} ({2}) for pie chart png and pdf output
					List<Drilldown> drilldowns = drilldownService.getDrilldowns(reportId);
					Drilldown drilldown;
					if (drilldowns.isEmpty()) {
						drilldown = null;
					} else {
						drilldown = drilldowns.get(0);
					}
					chart.setDrilldown(drilldown);
					
					chart.prepareDataset(rs);

					List<PostProcessorDefinition> externalPostProcessors = new ArrayList<>();

					PostProcessorDefinition pp = new PostProcessorDefinition();
					pp.setId("chart");

					Map<String, String> internalPostProcessorParams = new HashMap<>();
					internalPostProcessorParams.put("showLegend", request.getParameter("showLegend"));

					pp.setParams(internalPostProcessorParams);

//					postProcessors.add(pp);
					chart.setInternalPostProcessorParams(internalPostProcessorParams);

					ChartUtils.prepareTheme(ArtConfig.getSettings().getPdfFontName());

					//store data for potential use in pdf output
					RowSetDynaClass data = null;
					if (chartOptions.isShowData()) {
						int rsType = rs.getType();
						if (rsType == ResultSet.TYPE_SCROLL_INSENSITIVE || rsType == ResultSet.TYPE_SCROLL_SENSITIVE) {
							rs.beforeFirst();
							boolean lowercaseColumnNames = false;
							boolean useColumnAlias = true;
							data = new RowSetDynaClass(rs, lowercaseColumnNames, useColumnAlias);
						}

					}

					if (reportFormat == ReportFormat.html) {
						request.setAttribute("chart", chart);

						String htmlElementId = "chart-" + reportId;
						request.setAttribute("htmlElementId", htmlElementId);

						request.setAttribute("externalPostProcessors", externalPostProcessors);

						ctx.getRequestDispatcher("/WEB-INF/jsp/showChart.jsp").include(request, response);
					} else {
						String fileName = baseFileName + "." + reportFormat.getFilenameExtension();
						String outputFileName = exportPath + fileName;

						chart.generateFile(reportFormat, outputFileName, data);
						//display link to access report
						request.setAttribute("fileName", fileName);
						ctx.getRequestDispatcher("/WEB-INF/jsp/showFileLink.jsp").include(request, response);
					}
					rowsRetrieved = getNumberOfRows(rs);
				} else {
					//direct output report. "standard/tabular" or crosstab reports
					//get query results
					// it is a "select" query or a procedure ending with a select statement
					rs = reportRunner.getResultSet();

					ActionResult outputResult;

					//set output object properties if required
					if (o != null) {
						o.setMaxRows(ArtConfig.getMaxRows(reportFormatString));
						o.setWriter(out);
						o.setQueryName(reportName);
						o.setFileUserName(username);
//						o.setExportPath(exportPath);

						//don't set displayparams for html view modes. parameters will be displayed by this servlet
						if (!StringUtils.containsIgnoreCase(reportFormatString, "html")) {
							o.setDisplayParameters(displayParams);
						}

						//ensure htmlplain output doesn't display parameters if inline
						if (o instanceof HtmlPlainOutput) {
							HtmlPlainOutput hpo = (HtmlPlainOutput) o;
							hpo.setDisplayInline(showInline);

							//ensure parameters are displayed if not in inline mode
							hpo.setDisplayParameters(displayParams);
						}

						//enable localization for datatable output
						if (o instanceof HtmlDataTableOutput) {
							HtmlDataTableOutput dt = (HtmlDataTableOutput) o;
							dt.setLocale(request.getLocale());
						}

						if (o instanceof DirectReportOutput) {
							DirectReportOutput dro = (DirectReportOutput) o;
							dro.setContextPath(request.getContextPath());
						}

						if (reportType.isCrosstab()) {
							outputResult = DirectReportOutputHandler.flushXOutput(o, rs);
						} else {

							//add support for drill down queries
							Map<Integer, DrilldownQuery> drilldownQueries = null;
							if (reportFormat.isHtml()) {
								//only drill down for html output. drill down query launched from hyperlink                                            
								drilldownQueries = aq.getDrilldownQueries(reportId);
							}
							outputResult = DirectReportOutputHandler.flushOutput(o, rs, drilldownQueries, request.getContextPath(), inlineParams, multiParams);

						}

						if (outputResult.isSuccess()) {
							rowsRetrieved = (Integer) outputResult.getData();
						} else {
							model.addAttribute("message", outputResult.getMessage());
							return errorPage;
						}
					}

				}

				// Print the "working" time elapsed
				// The time elapsed from a user perspective can be bigger because the servlet output
				// is "cached and transmitted" over the network by the servlet engine.
				long endTime = System.currentTimeMillis();

				totalTime = (endTime - overallStartTime) / (1000);
				fetchTime = (queryEndTime - queryStartTime) / (1000);
				double preciseTotalTime = (endTime - overallStartTime) / (double) 1000;
				NumberFormat nf = NumberFormat.getInstance(request.getLocale());
				DecimalFormat df = (DecimalFormat) nf;
				df.applyPattern("#,##0.0##");

				if (showReportHeaderAndFooter) {
					Object[] value = {
						df.format(preciseTotalTime)
					};

					String msg = messageSource.getMessage("reports.text.timeTakenInSeconds", value, locale);
					request.setAttribute("timeTaken", msg);
					if (rowsRetrieved == null) {
						msg = messageSource.getMessage("reports.text.unknown", null, locale);
						request.setAttribute("rowsRetrieved", msg);
					} else {
						df.applyPattern("#,##0");
						request.setAttribute("rowsRetrieved", df.format(rowsRetrieved));
					}

					ctx.getRequestDispatcher("/WEB-INF/jsp/runReportInfoFooter.jsp").include(request, response);

					//clear report progress
					displayReportProgress(out, "");
				}
			}

			if (!showInline) {
				request.setAttribute("reportFormat", reportFormat.getValue());
				ctx.getRequestDispatcher("/WEB-INF/jsp/runReportPageFooter.jsp").include(request, response);
			}

			ArtHelper.log(username, "query", request.getRemoteAddr(), reportId, totalTime, fetchTime, "query, " + reportFormatString);

		} catch (Exception ex) {
			logger.error("Error. {}, {}", report, sessionUser, ex);
			if (report != null) {
				model.addAttribute("reportName", report.getName());
			}
			model.addAttribute("error", ex);
			return errorPage;
		} finally {
			runningReportsCount--;
			ArtDbUtils.close(rs);
			if (reportRunner != null) {
				reportRunner.close();
			}
		}

		return null;
	}

	//prepare chart object and options for generation and display
	private ArtGraph prepareChartDetails(ResultSet rs, HttpServletRequest request,
			Report report) throws SQLException {

		ResultSetMetaData rsmd = rs.getMetaData();

		String shortDescription = report.getShortDescription();
		String xAxisLabel = report.getxAxisLabel();
		String yAxisLabel = report.getyAxisLabel();
		String graphOptions = report.getChartOptionsSetting();

		ReportType chartType = ReportType.toEnum(report.getReportTypeId());

		int queryType = report.getReportTypeId();

		ArtGraph o;

		switch (chartType) {
			case XYChart:
				o = new ArtXY();
				break;
			case Pie2DChart:
			case Pie3DChart:
				o = new ArtPie();
				break;
			case TimeSeriesChart:
				o = new ArtTimeSeries();
				break;
			case DateSeriesChart:
				o = new ArtDateSeries();
				break; //  this line was missing... added thanks to anonymous post in sf
			case SpeedometerChart:
				o = new ArtSpeedometer();
				break;
			case BubbleChart:
			case HeatmapChart:
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

		int indexOf = shortDescription.lastIndexOf("@");
		if (indexOf > -1) {
			options = shortDescription;
			shortDescription = shortDescription.substring(0, indexOf);
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

		o.setTitle(shortDescription);
		o.setWidth(width);
		o.setHeight(height);
		o.setSeriesName(rsmd.getColumnLabel(2));
		o.setBgColor(bgColor);
		o.setShowGraphData(showGraphData);

		//set some graph properties
		o.setXAxisLabel(xAxisLabel);
		if (yAxisLabel == null) {
			yAxisLabel = rsmd.getColumnLabel(1);
		}
		o.setYAxisLabel(yAxisLabel);

		return o;

	}

	//get number of rows in a resultset.
	private Integer getNumberOfRows(ResultSet rs) {
		Integer rowsRetrieved = null;

		try {
			if (rs != null) {
				int rsType = rs.getType();
				if (rsType == ResultSet.TYPE_SCROLL_INSENSITIVE || rsType == ResultSet.TYPE_SCROLL_SENSITIVE) {
					//resultset is scrollable
					rs.last();
					rowsRetrieved = rs.getRow();
					rs.beforeFirst();
				}
			}
		} catch (SQLException ex) {
			//not all drivers support this technique? If not supported, set number of rows to unknown
			rowsRetrieved = null;
			logger.error("Error", ex);
		}

		return rowsRetrieved;
	}

	private void displayReportProgress(PrintWriter out, String message) {
		out.println("<script type='text/javascript'>$('reportProgress').html('"
				+ message + "');</script>");
		out.flush();
	}

	private void displayReportInfo(PrintWriter out, String message) {
		out.println("<script type='text/javascript'>$('reportInfo').html('"
				+ message + "');</script>");
	}

}
