/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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

import art.drilldown.DrilldownService;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.output.StandardOutput;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.user.User;
import art.utils.ArtHelper;
import art.utils.FilenameHelper;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.io.FilenameUtils;
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
	private ServletContext servletContext;

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
			report = reportService.getReport(reportId);

			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return errorPage;
			}

			//check if user has permission to run report
			//admins can run all reports, even disabled ones. only check for non admin users
			sessionUser = (User) session.getAttribute("sessionUser");

			if (!sessionUser.isAdminUser()) {
				if (!report.isActive()) {
					model.addAttribute("message", "reports.message.reportDisabled");
					return errorPage;
				}

				if (!reportService.canUserRunReport(sessionUser.getUserId(), reportId)) {
					model.addAttribute("message", "reports.message.noPermission");
					return errorPage;
				}

				if (runningReportsCount > Config.getSettings().getMaxRunningReports()) {
					logger.warn("Report not run. Max running reports reached. user={}, report={}", sessionUser, report);
					model.addAttribute("message", "reports.message.maxRunningReportsReached");
					return errorPage;
				}
			}

			String reportName = report.getName();
			ReportType reportType = report.getReportType();

			//check if the html code should be rendered as an html fragment (without <html> and </html> tags) e.g. in a dashboard section
			boolean isFragment = Boolean.valueOf(request.getParameter("isFragment"));

			//make sure the browser does not cache the result using Ajax (this happens in IE)
//			if (isFragment) {
//				response.setHeader("Cache-control", "no-cache");
//			}
			response.setHeader("Cache-control", "no-cache");

			if (reportType == ReportType.Dashboard) {
				return "forward:/app/showDashboard.do";
			} else if (reportType.isOlap()) {
				return "forward:/app/showAnalysis.do";
			}

			//get report format to use
			ReportFormat reportFormat;
			String reportFormatString = request.getParameter("reportFormat");
			if (reportFormatString == null || StringUtils.equalsIgnoreCase(reportFormatString, "default")) {
				if (reportType.isJasperReports()) {
					reportFormat = ReportFormat.pdf;
				} else if (reportType.isChart()) {
					reportFormat = ReportFormat.html;
				} else {
					reportFormat = ReportFormat.htmlFancy;
				}
			} else {
				reportFormat = ReportFormat.toEnum(reportFormatString);
			}

			//this will be initialized according to the content type of the report output
			//setContentType() must be called before getWriter()
			PrintWriter writer;

			boolean showReportHeaderAndFooter = true;

			if (reportType.isStandardOutput()) {
				ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();
				boolean isJob = false;
				StandardOutput standardOutput = reportOutputGenerator.getStandardOutputInstance(reportFormat, isJob, report);

				String contentType = standardOutput.getContentType();

				//set the content type according to the report output class
				response.setContentType(contentType);
				writer = response.getWriter();

				//the report output class determines if the report header and footer will be shown
				//if false the output class needs to take care of all the output
				showReportHeaderAndFooter = standardOutput.outputHeaderandFooter();
			} else {
				response.setContentType("text/html; charset=UTF-8");
				writer = response.getWriter();
			}

			if (isFragment) {
				//report header and footer not shown for fragments
				showReportHeaderAndFooter = false;
				showInline = true;
			}

			//handle output formats that require data only
			if (reportFormat == ReportFormat.xml || reportFormat == ReportFormat.rss20) {
				showInline = true;
			}

			//output page header. if showInline, page header and footer already exist. 
			if (!showInline) {
				request.setAttribute("title", reportName);
				request.setAttribute("reportFormat", reportFormat.getValue());

				boolean allowSelectParameters = Boolean.valueOf(request.getParameter("allowSelectParameters"));
				if (allowSelectParameters) {
					request.setAttribute("allowSelectParameters", allowSelectParameters);
					RunReportHelper runReportHelper = new RunReportHelper();
					runReportHelper.setSelectReportParameterAttributes(report, request, session, reportService);
				}

				servletContext.getRequestDispatcher("/WEB-INF/jsp/runReportPageHeader.jsp").include(request, response);
				writer.flush();
			}

			long totalTime = 0;
			long fetchTime = 0;

			if (reportType == ReportType.Text) {
				//http://jsoup.org/apidocs/org/jsoup/safety/Whitelist.html
				//https://stackoverflow.com/questions/9213189/jsoup-whitelist-relaxed-mode-too-strict-for-wysiwyg-editor
				String reportSource = report.getReportSource();
				String cleanSource = Jsoup.clean(reportSource, Whitelist.relaxed());
				request.setAttribute("reportSource", cleanSource);
				servletContext.getRequestDispatcher("/WEB-INF/jsp/showTextReport.jsp").include(request, response);
			} else {
				//output report header
				if (showReportHeaderAndFooter) {
					request.setAttribute("reportName", reportName);
					servletContext.getRequestDispatcher("/WEB-INF/jsp/runReportInfoHeader.jsp").include(request, response);
					writer.flush();

					//display initial report progress
					displayReportProgress(writer, messageSource.getMessage("reports.message.configuring", null, locale));
				}

				//run report
				Integer rowsRetrieved = null; //use Integer in order to have unknown status e.g. for template reports for which you can't know the number of rows in the report

				long overallStartTime = System.currentTimeMillis(); //overall start time

				reportRunner = new ReportRunner();
				reportRunner.setUser(sessionUser);
				reportRunner.setReport(report);

				//prepare report parameters
				ParameterProcessor paramProcessor = new ParameterProcessor();
				ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);

				Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
				List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();
				ReportOptions reportOptions = paramProcessorResult.getReportOptions();

				//get parameter display values if parameters need to be shown in html or file output
				//especially for lov values where display value may be different from data value
				//use treemap so that params can be displayed in param position order
				Map<Integer, ReportParameter> reportParamEntries = new TreeMap<>();

				boolean showParams = false;
				if (reportOptions.isShowSelectedParameters() || report.isParametersInOutput()) {
					showParams = true;

					for (ReportParameter reportParam : reportParamsList) {
						reportParamEntries.put(reportParam.getPosition(), reportParam);
					}
				}

				reportRunner.setReportParamsMap(reportParamsMap);

				// JavaScript code to write status
				if (showReportHeaderAndFooter) {
					displayReportProgress(writer, messageSource.getMessage("reports.message.running", null, locale));
				}

				//get resultset type to use
				int resultSetType = getResultSetType(reportType);

				//run query
				long queryStartTime = System.currentTimeMillis();
				reportRunner.execute(resultSetType);
				long queryEndTime = System.currentTimeMillis();

				// display status information, parameters and final sql
				if (showReportHeaderAndFooter) {
					String shortDescription = report.getShortDescription();
					RunReportHelper runReportHelper = new RunReportHelper();
					shortDescription = runReportHelper.performDirectParameterSubstitution(shortDescription, reportParamsMap);

					String description = "";
					shortDescription = StringUtils.trim(shortDescription);
					if (StringUtils.length(shortDescription) > 0) {
						description = " :: " + shortDescription;
					}

					DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
					String startTimeString = df.format(new Date(overallStartTime));

//					String reportInfo = "<b>" + reportName + "</b>"
//							+ description + " :: " + startTimeString;
					String reportInfo = "<h5>" + reportName + "<small>"
							+ description + " :: " + startTimeString + "</small></h5>";

					displayReportInfo(writer, reportInfo);

					displayReportProgress(writer, messageSource.getMessage("reports.message.fetchingData", null, locale));

					//display parameters
					if (showParams) {
						request.setAttribute("reportParamEntries", reportParamEntries);
						servletContext.getRequestDispatcher("/WEB-INF/jsp/showSelectedParameters.jsp").include(request, response);
					}

					//display final sql. only admins can see sql
					//determine if final sql should be shown. only admins can see sql
					if (reportOptions.isShowSql() && sessionUser.isAdminUser()) {
						//get estimate final sql with parameter placeholders replaced with parameter values
						String finalSql = reportRunner.getFinalSql();
						request.setAttribute("finalSql", finalSql);
						servletContext.getRequestDispatcher("/WEB-INF/jsp/showFinalSql.jsp").include(request, response);
					}

					writer.flush();
				}

				if (reportType == ReportType.Update) {
					reportRunner.getResultSet();
					Integer rowsUpdated = reportRunner.getUpdateCount(); // will be -1 if query has multiple statements
					request.setAttribute("rowsUpdated", rowsUpdated);
					servletContext.getRequestDispatcher("/WEB-INF/jsp/showUpdateReport.jsp").include(request, response);
				} else {
					//generate output
					//generate file name to use for report types and formats that generate files
					FilenameHelper filenameHelper = new FilenameHelper();
					String baseFileName = filenameHelper.getFileName(report);
					String exportPath = Config.getReportsExportPath();

					String extension;
					if (reportType.isJxls()) {
						String jxlsFilename = report.getTemplate();
						extension = FilenameUtils.getExtension(jxlsFilename);
					} else {
						extension = reportFormat.getFilenameExtension();
					}

					String fileName = baseFileName + "." + extension;
					String outputFileName = exportPath + fileName;

					ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();

					reportOutputGenerator.setDrilldownService(drilldownService);
					reportOutputGenerator.setRequest(request);
					reportOutputGenerator.setResponse(response);
					reportOutputGenerator.setServletContext(servletContext);

					ReportOutputGeneratorResult outputResult = reportOutputGenerator.generateOutput(report, reportRunner,
							reportFormat, locale, paramProcessorResult, writer, outputFileName, sessionUser);

					if (outputResult.isSuccess()) {
						rowsRetrieved = outputResult.getRowCount();
					} else {
						model.addAttribute("message", outputResult.getMessage());
						return errorPage;
					}
				}

				// Print the "working" time elapsed
				// The time elapsed from a user perspective can be bigger because the servlet output
				// is "cached and transmitted" over the network by the servlet engine.
				long overallEndTime = System.currentTimeMillis();

				totalTime = (overallEndTime - overallStartTime) / (1000);
				fetchTime = (queryEndTime - queryStartTime) / (1000);
				double preciseTotalTime = (overallEndTime - overallStartTime) / (double) 1000;
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

					servletContext.getRequestDispatcher("/WEB-INF/jsp/runReportInfoFooter.jsp").include(request, response);

					clearReportProgress(writer);
				}
			}

			if (!showInline) {
				request.setAttribute("reportFormat", reportFormat.getValue());
				servletContext.getRequestDispatcher("/WEB-INF/jsp/runReportPageFooter.jsp").include(request, response);
			}

			String username = sessionUser.getUsername();
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
			if (reportRunner != null) {
				reportRunner.close();
			}
		}

		return null;
	}

	/**
	 * Returns the resultset type to use for a given report type
	 *
	 * @param reportType the report type
	 * @return the resultset type to use
	 */
	private int getResultSetType(ReportType reportType) {
		//is scroll insensitive much slower than forward only?
		int resultSetType;
		if (reportType == ReportType.JasperReportsArt || reportType == ReportType.JxlsArt
				|| reportType == ReportType.FreeMarker || reportType.isXDocReport()
				|| reportType == ReportType.Group || reportType.isChart()) {
			//need scrollable resultset for jasper art report, jxls art report, freemarker, xdocreport in order to display record count
			//need scrollable resultset in order to generate group report
			//need scrollable resultset for charts for show data option
			resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
		} else {
			//report types will determine the record count e.g. for standard output reports
			//or no way to determine record count e.g. with jasper reports template report
			resultSetType = ResultSet.TYPE_FORWARD_ONLY;
		}

		return resultSetType;
	}

	/**
	 * Displays report progress
	 *
	 * @param out the writer to output to
	 * @param message the message to output
	 */
	private void displayReportProgress(PrintWriter out, String message) {
		displayInfo(out, message, "reportProgress");
		out.flush();
	}

	/**
	 * Displays report information
	 *
	 * @param out the writer to use
	 * @param message the message to output
	 */
	private void displayReportInfo(PrintWriter out, String message) {
		displayInfo(out, message, "reportInfo");
	}

	/**
	 * Displays information to the report output
	 *
	 * @param out the output writer to use
	 * @param message the message to output
	 * @param elementId the html element to output to
	 */
	private void displayInfo(PrintWriter out, String message, String elementId) {
		//can use jquery, e.g. $('reportProgress').html(), but need to ensure jquery library 
		//has been included in the page before calling this method
		out.println("<script type='text/javascript'>"
				+ "document.getElementById('" + elementId + "').innerHTML='" + message + "';"
				+ "</script>");
	}

	/**
	 * Clears the report progress indicator
	 *
	 * @param writer the output writer to use
	 */
	private void clearReportProgress(PrintWriter writer) {
		displayReportProgress(writer, "");
	}
}
