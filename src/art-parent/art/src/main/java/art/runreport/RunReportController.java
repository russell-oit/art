/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.runreport;

import art.drilldown.DrilldownService;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.output.StandardOutput;
import art.report.Report;
import art.report.ReportService;
import art.reportoptions.TabularHeatmapOptions;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.user.User;
import art.utils.ArtLogsHelper;
import art.utils.ArtUtils;
import art.utils.FilenameHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.cloning.Cloner;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
	@RequestMapping(value = "/runReport", method = {RequestMethod.GET, RequestMethod.POST})
	public String runReport(@RequestParam("reportId") Integer reportId,
			@ModelAttribute("report") Report testReport,
			HttpServletRequest request, HttpServletResponse response,
			HttpSession session, Model model, Locale locale,
			RedirectAttributes redirectAttributes) throws IOException {

		Report report = null;
		User sessionUser = (User) session.getAttribute("sessionUser");
		ReportRunner reportRunner = null;

		runningReportsCount++;

		//check if output is being displayed within the show report page (inline) or in a new page
		boolean showInline = BooleanUtils.toBoolean(request.getParameter("showInline"));
		//check if the html code should be rendered as an html fragment (without <html> and </html> tags) e.g. in a dashboard section
		boolean isFragment = BooleanUtils.toBoolean(request.getParameter("isFragment"));

		//set appropriate error page to use
		String errorPage;
		if (showInline || isFragment) {
			errorPage = "reportErrorInline";
		} else {
			errorPage = "reportError";
		}

		String reportName = null;
		RunReportHelper runReportHelper = new RunReportHelper();

		try {
			if (testReport.getTestRun() == null) {
				report = reportService.getReport(reportId);
			} else {
				boolean selfServicePreview = BooleanUtils.toBoolean(request.getParameter("selfServicePreview"));
				if (selfServicePreview) {
					Report originalReport = reportService.getReport(reportId);
					if (originalReport == null) {
						throw new RuntimeException("Report not found: " + reportId);
					}
					Cloner cloner = new Cloner();
					Report originalReportCopy = cloner.deepClone(originalReport);
					testReport = originalReportCopy;

					String selfServiceOptions = request.getParameter("selfServiceOptions");
					String limitString = request.getParameter("limit");
					Integer limit;
					if (StringUtils.isBlank(limitString)) {
						limit = ReportRunner.RETURN_ALL_RECORDS;
					} else {
						limit = NumberUtils.toInt(limitString);
					}

					testReport.setSelfServiceOptions(selfServiceOptions);
					testReport.setLimit(limit);
					testReport.setSelfServicePreview(selfServicePreview);
				} else {
					boolean testData = BooleanUtils.toBoolean(request.getParameter("testData"));
					if (testData) {
						testReport.setReportType(ReportType.Tabular);
					} else {
						testReport.setReportType(ReportType.toEnum(testReport.getReportTypeId()));
						if (testReport.getReportType() == ReportType.Text) {
							testReport.setReportSource(testReport.getReportSourceHtml());
						}
					}

					testReport.loadGeneralOptions();
				}

				if (testReport.getReportType() != ReportType.View) {
					runReportHelper.applySelfServiceFields(testReport, sessionUser);
				}

				report = testReport;
			}

			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return errorPage;
			}

			request.setAttribute("locale", locale);

			reportName = report.getLocalizedName(locale);

			//check if user has permission to run report
			//admins can run all reports, even disabled ones. only check for non admin users
			sessionUser = (User) session.getAttribute("sessionUser");

			if (!sessionUser.hasConfigureReportsPermission()) {
				if (!report.isActive()) {
					model.addAttribute("message", "reports.message.reportDisabled");
					return errorPage;
				}

				if (!reportService.canUserRunReport(sessionUser, reportId)) {
					model.addAttribute("message", "reports.message.noPermission");
					return errorPage;
				}

				if (runningReportsCount > Config.getSettings().getMaxRunningReports()) {
					logger.warn("Report not run. Max running reports reached. user={}, report={}", sessionUser, report);
					model.addAttribute("message", "reports.message.maxRunningReportsReached");
					return errorPage;
				}
			}

			//make sure the browser does not cache the result using Ajax (this happens in IE)
//			if (isFragment) {
//				response.setHeader("Cache-control", "no-cache");
//			}
			response.setHeader("Cache-control", "no-cache");

			ReportType reportType = report.getReportType();

			if (reportType == ReportType.View) {
				runReportHelper.applySelfServiceFields(report, sessionUser);
			}

			ReportFormat reportFormat;
			String reportFormatString = request.getParameter("reportFormat");
			if (reportFormatString == null || StringUtils.equalsIgnoreCase(reportFormatString, "default")) {
				reportFormat = runReportHelper.getDefaultReportFormat(reportType);
			} else {
				reportFormat = ReportFormat.toEnum(reportFormatString);
			}

			if (reportType.isDashboard()) {
				//https://stackoverflow.com/questions/8585216/spring-forward-with-added-parameters
				request.setAttribute("suppliedReport", report);
				return "forward:/showDashboard";
			} else if (reportType.isJPivot()) {
				//setting model attributes won't include parameters in the redirect request because
				//we have setIgnoreDefaultModelOnRedirect in AppConfig.java
				//use redirect attributes instead
//				for (Entry<String, String[]> requestParam : request.getParameterMap().entrySet()) {
//					String paramName = requestParam.getKey();
//					String[] paramValue = requestParam.getValue();
//					model.addAttribute(paramName, paramValue);
//				}

				ParameterProcessor paramProcessor = new ParameterProcessor();
				ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request, locale);
				List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();

				ArtLogsHelper.logReportRun(sessionUser, request.getRemoteAddr(), reportId, reportFormat.getValue(), reportParamsList);

				//can't use addFlashAttribute() as flash attributes aren't included as part of request parameters
				redirectAttributes.addAllAttributes(request.getParameterMap());
				//using forward means adding runReport url-mapping to the jpivotcontroller filter-mapping in the web.xml file
				//doing this results in errors as a result of the runReport page being handled by jpivotError
				//so use redirect
				return "redirect:/showJPivot";
			} else if (reportType == ReportType.SaikuReport) {
				ParameterProcessor paramProcessor = new ParameterProcessor();
				ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request, locale);
				List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();

				ArtLogsHelper.logReportRun(sessionUser, request.getRemoteAddr(), reportId, reportFormat.getValue(), reportParamsList);

				List<String> parametersList = new ArrayList<>();
				Map<String, String[]> requestParameters = request.getParameterMap();
				for (Entry<String, String[]> entry : requestParameters.entrySet()) {
					String paramName = entry.getKey();
					String[] paramValues = entry.getValue();
					if (StringUtils.startsWithIgnoreCase(paramName, ArtUtils.PARAM_PREFIX)) {
						String encodedParamName = URLEncoder.encode(paramName, "UTF-8");
						for (String paramValue : paramValues) {
							String encodedParamValue = URLEncoder.encode(paramValue, "UTF-8");
							String saikuParam = "param"
									+ StringUtils.substring(encodedParamName, ArtUtils.PARAM_PREFIX.length())
									+ "=" + encodedParamValue;
							parametersList.add(saikuParam);
						}
					}
				}

				String parametersString = "";
				if (!parametersList.isEmpty()) {
					parametersString = "?" + StringUtils.join(parametersList, "&");
				}

				return "redirect:/saiku3/" + parametersString + "#query/open/" + reportId;
			}

			Integer totalTimeSeconds = null;
			Integer fetchTimeSeconds = null;

			Date overallStartTime = new Date();
			Instant overallStart = Instant.now();

			//this will be initialized according to the content type of the report output
			//setContentType() must be called before getWriter()
			PrintWriter writer;

			boolean showReportHeaderAndFooter = true;

			if (reportType.isStandardOutput() && reportFormat.hasStandardOutputInstance()) {
				ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();
				boolean isJob = false;
				StandardOutput standardOutput = reportOutputGenerator.getStandardOutputInstance(reportFormat, isJob, report);

				String contentType = standardOutput.getContentType();

				//set the content type according to the report output class
				response.setContentType(contentType);
				writer = response.getWriter();

				//the report output class determines if the report header and footer will be shown
				//if false the output class needs to take care of all the output
				showReportHeaderAndFooter = standardOutput.outputHeaderAndFooter();
				if (!showReportHeaderAndFooter) {
					showInline = true;
				}
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
			switch (reportFormat) {
				case json:
				case jsonBrowser:
					showInline = true;
					showReportHeaderAndFooter = false;
					break;
				default:
				//do nothing
			}

			//output page header. if showInline, page header and footer already exist. 
			if (!showInline) {
				request.setAttribute("title", reportName);
				request.setAttribute("reportFormat", reportFormat.getValue());

				boolean allowSelectParameters = BooleanUtils.toBoolean(request.getParameter("allowSelectParameters"));
				if (allowSelectParameters) {
					request.setAttribute("allowSelectParameters", allowSelectParameters);
					runReportHelper.setSelectReportParameterAttributes(report, request, session, locale);
				}

				request.setAttribute("reportType", reportType);

				servletContext.getRequestDispatcher("/WEB-INF/jsp/runReportPageHeader.jsp").include(request, response);
				writer.flush();
				request.setAttribute("pageHeaderLoaded", true);
			}

			List<ReportParameter> reportParamsList = null;

			if (reportType == ReportType.Text) {
				//http://jsoup.org/apidocs/org/jsoup/safety/Whitelist.html
				//https://stackoverflow.com/questions/9213189/jsoup-whitelist-relaxed-mode-too-strict-for-wysiwyg-editor
				String reportSource = report.getReportSource();
				//https://github.com/jhy/jsoup/issues/511#issuecomment-94978302
				Whitelist whitelist = Whitelist.relaxed()
						.addTags("hr")
						.addProtocols("img", "src", "data"); //for base64 encoded images
				String cleanSource = Jsoup.clean(reportSource, whitelist);
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

				reportRunner = new ReportRunner();
				reportRunner.setUser(sessionUser);

				reportRunner.setReport(report);

				boolean isDrilldown = BooleanUtils.toBoolean(request.getParameter("drilldown"));

				//prepare report parameters
				ParameterProcessor paramProcessor = new ParameterProcessor();
				paramProcessor.setSuppliedReport(report);
				paramProcessor.setIsFragment(isFragment);
				paramProcessor.setIsDrilldown(isDrilldown);
				ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request, locale);

				Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
				reportParamsList = paramProcessorResult.getReportParamsList();
				ReportOptions reportOptions = paramProcessorResult.getReportOptions();

				//get parameter display values if parameters need to be shown in html or file output
				//especially for lov values where display value may be different from data value
				//use treemap so that params can be displayed in param position order
				Map<Integer, ReportParameter> reportParamEntries = new TreeMap<>();

				boolean showParams = false;
				if (reportOptions.isShowSelectedParameters()) {
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

				int resultSetType = runReportHelper.getResultSetType(reportType);

				//run query
				Instant queryStart = Instant.now();

				reportRunner.execute(resultSetType);

				Instant queryEnd = Instant.now();

				// display status information, parameters and final sql
				if (showReportHeaderAndFooter) {
					String shortDescription = report.getLocalizedShortDescription(locale);
					shortDescription = runReportHelper.performDirectParameterSubstitution(shortDescription, reportParamsMap);

					String description = "";
					if (StringUtils.isNotBlank(shortDescription)) {
						description = " :: " + shortDescription;
					}

					DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
					String startTimeString = df.format(overallStartTime);

					String reportInfo = "<h4>" + Encode.forHtmlContent(reportName) + "<small>"
							+ Encode.forHtmlContent(description) + " :: "
							+ Encode.forHtmlContent(startTimeString) + "</small></h4>";

					displayReportInfo(writer, reportInfo);

					displayReportProgress(writer, messageSource.getMessage("reports.message.fetchingData", null, locale));

					//display parameters
					if (showParams) {
						request.setAttribute("reportParamEntries", reportParamEntries);
						servletContext.getRequestDispatcher("/WEB-INF/jsp/showSelectedParameters.jsp").include(request, response);
					}

					//display final sql. only admins can see sql
					if (reportOptions.isShowSql() && sessionUser.hasConfigureReportsPermission()) {
						//get estimate final sql with parameter placeholders replaced with parameter values
						String finalSql = reportRunner.getFinalSql();
						Object groovyData = reportRunner.getGroovyData();
						String codeClass;
						if (groovyData == null) {
							codeClass = "sql";
						} else {
							codeClass = "groovy";
						}
						request.setAttribute("finalSql", finalSql);
						request.setAttribute("codeClass", codeClass);
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
					String outputFileName = filenameHelper.getFullFilename(report, locale, reportFormat);

					ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();

					reportOutputGenerator.setDrilldownService(drilldownService);
					reportOutputGenerator.setRequest(request);
					reportOutputGenerator.setResponse(response);
					reportOutputGenerator.setServletContext(servletContext);

					if (reportType == ReportType.TabularHeatmap) {
						TabularHeatmapOptions options;

						String optionsString = report.getOptions();
						if (StringUtils.isBlank(optionsString)) {
							options = new TabularHeatmapOptions();
						} else {
							ObjectMapper mapper = new ObjectMapper();
							options = mapper.readValue(optionsString, TabularHeatmapOptions.class);
						}

						request.setAttribute("options", options);
						servletContext.getRequestDispatcher("/WEB-INF/jsp/activateTabularHeatmap.jsp").include(request, response);
					}

					ReportOutputGeneratorResult outputResult = reportOutputGenerator.generateOutput(report, reportRunner,
							reportFormat, locale, paramProcessorResult, writer, outputFileName, sessionUser, messageSource);

					if (outputResult.isSuccess()) {
						rowsRetrieved = outputResult.getRowCount();
					} else {
						model.addAttribute("message", outputResult.getMessage());
						return errorPage;
					}

					//encrypt file if applicable
					report.encryptFile(outputFileName);
				}

				// Print the "working" time elapsed
				// The time elapsed from a user perspective can be bigger because the servlet output
				// is "cached and transmitted" over the network by the servlet engine.
				//https://www.baeldung.com/java-measure-elapsed-time
				//http://tutorials.jenkov.com/java-date-time/duration.html
				Instant overallEnd = Instant.now();
				Duration overallDuration = Duration.between(overallStart, overallEnd);
				Duration queryDuration = Duration.between(queryStart, queryEnd);

				totalTimeSeconds = (int) overallDuration.getSeconds();
				fetchTimeSeconds = (int) queryDuration.getSeconds();

				double preciseTotalTimeSeconds = overallDuration.toMillis() / (double) 1000;
				DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(request.getLocale());
				df.applyPattern("#,##0.0##");

				if (showReportHeaderAndFooter) {
					Object[] value = {
						df.format(preciseTotalTimeSeconds)
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

			ArtLogsHelper.logReportRun(sessionUser, request.getRemoteAddr(), reportId, totalTimeSeconds, fetchTimeSeconds, reportFormat.getValue(), reportParamsList);
		} catch (Exception ex) {
			logger.error("Error. {}, {}", report, sessionUser, ex);
			if (reportName != null) {
				model.addAttribute("reportName", reportName);
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
