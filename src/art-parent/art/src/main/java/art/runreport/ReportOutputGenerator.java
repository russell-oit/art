/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.runreport;

import art.chart.Chart;
import art.chart.CategoryBasedChart;
import art.chart.ChartUtils;
import art.chart.PieChart;
import art.chart.SpeedometerChart;
import art.chart.TimeSeriesBasedChart;
import art.chart.XYChart;
import art.chart.XYZBasedChart;
import art.dbutils.DatabaseUtils;
import art.drilldown.Drilldown;
import art.drilldown.DrilldownService;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.enums.ZipType;
import art.output.CsvOutputArt;
import art.output.CsvOutputUnivocity;
import art.output.DocxOutput;
import art.output.FixedWidthOutput;
import art.output.FreeMarkerOutput;
import art.output.StandardOutput;
import art.output.GroupHtmlOutput;
import art.output.GroupOutput;
import art.output.GroupXlsxOutput;
import art.output.HtmlDataTableOutput;
import art.output.HtmlFancyOutput;
import art.output.HtmlGridOutput;
import art.output.HtmlPlainOutput;
import art.output.JasperReportsOutput;
import art.output.JsonOutput;
import art.output.JsonOutputResult;
import art.output.JxlsOutput;
import art.output.OdsOutput;
import art.output.OdtOutput;
import art.output.PdfOutput;
import art.output.ResultSetColumn;
import art.output.Rss20Output;
import art.output.SlkOutput;
import art.output.StandardOutputResult;
import art.output.ThymeleafOutput;
import art.output.TsvOutput;
import art.output.XDocReportOutput;
import art.output.XlsOutput;
import art.output.XlsxOutput;
import art.output.XmlOutput;
import art.report.ChartOptions;
import art.report.Report;
import art.report.ReportService;
import art.reportoptions.C3Options;
import art.reportoptions.ChartJsOptions;
import art.reportoptions.CsvOutputArtOptions;
import art.reportoptions.CsvServerOptions;
import art.reportoptions.DataTablesOptions;
import art.reportoptions.DatamapsOptions;
import art.reportoptions.FixedWidthOptions;
import art.reportoptions.LeafletOptions;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.cewolfart.ChartValidationException;
import net.sf.cewolfart.DatasetProduceException;
import net.sf.cewolfart.PostProcessingException;
import fr.opensagres.xdocreport.core.XDocReportException;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.util.CollectionUtils;

/**
 * Generates report output
 *
 * @author Timothy Anyona
 */
public class ReportOutputGenerator {

	private static final Logger logger = LoggerFactory.getLogger(ReportOutputGenerator.class);

	//optional variables for generateOutput() method
	private int jobId;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private ServletContext servletContext;
	private DrilldownService drilldownService;

	/**
	 * @return the drilldownService
	 */
	public DrilldownService getDrilldownService() {
		return drilldownService;
	}

	/**
	 * @param drilldownService the drilldownService to set
	 */
	public void setDrilldownService(DrilldownService drilldownService) {
		this.drilldownService = drilldownService;
	}

	/**
	 * @return the jobId
	 */
	public int getJobId() {
		return jobId;
	}

	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	/**
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * @return the response
	 */
	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	/**
	 * @return the servletContext
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * @param servletContext the servletContext to set
	 */
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	/**
	 * Generates report output
	 *
	 * @param report the report to use
	 * @param reportRunner the report runner to use
	 * @param reportFormat the report format
	 * @param locale the locale to use
	 * @param paramProcessorResult the parameter processor result
	 * @param writer the output writer to use
	 * @param fullOutputFilename the full path of the output file name
	 * @param user the user under who's permissions the report is being
	 * generated
	 * @param messageSource the messagesource to use
	 * @return the output result
	 * @throws IOException
	 * @throws SQLException
	 * @throws JRException
	 * @throws InvalidFormatException
	 * @throws DatasetProduceException
	 * @throws ChartValidationException
	 * @throws PostProcessingException
	 * @throws ServletException
	 * @throws freemarker.template.TemplateException
	 * @throws fr.opensagres.xdocreport.core.XDocReportException
	 */
	public ReportOutputGeneratorResult generateOutput(Report report, ReportRunner reportRunner,
			ReportFormat reportFormat, Locale locale,
			ParameterProcessorResult paramProcessorResult,
			PrintWriter writer, String fullOutputFilename, User user, MessageSource messageSource)
			throws IOException, SQLException, JRException,
			InvalidFormatException, DatasetProduceException, ChartValidationException,
			PostProcessingException, ServletException, TemplateException, XDocReportException {

		logger.debug("Entering generateOutput");

		ReportOutputGeneratorResult outputResult = new ReportOutputGeneratorResult();
		outputResult.setSuccess(true);

		ResultSet rs = null;
		Integer rowsRetrieved = null;

		boolean isJob = false;
		if (jobId > 0) {
			isJob = true;
		}

		if (!isJob) {
			Objects.requireNonNull(request, "request must not be null");
			Objects.requireNonNull(response, "response must not be null");
			Objects.requireNonNull(servletContext, "servletContext must not be null");
			Objects.requireNonNull(drilldownService, "drilldownService must not be null");
		}

		String fileName = FilenameUtils.getName(fullOutputFilename);

		try {
			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();
			ReportOptions reportOptions = paramProcessorResult.getReportOptions();
			ChartOptions parameterChartOptions = paramProcessorResult.getChartOptions();

			int reportId = report.getReportId();
			ReportType reportType = report.getReportType();

			//generate report output
			if (reportType.isJasperReports() || reportType.isJxls()) {
				if (reportType.isJasperReports()) {
					JasperReportsOutput jrOutput = new JasperReportsOutput();
					if (reportType == ReportType.JasperReportsArt) {
						rs = reportRunner.getResultSet();
						jrOutput.setResultSet(rs);
					}

					jrOutput.generateReport(report, reportParamsList, reportFormat, fullOutputFilename);
				} else if (reportType.isJxls()) {
					JxlsOutput jxlsOutput = new JxlsOutput();
					if (reportType == ReportType.JxlsArt) {
						rs = reportRunner.getResultSet();
						jxlsOutput.setResultSet(rs);
					}

					jxlsOutput.generateReport(report, reportParamsList, fullOutputFilename);
				}

				rowsRetrieved = getResultSetRowCount(rs);

				if (!isJob) {
					displayFileLink(fileName);
				}
			} else if (reportType == ReportType.Group) {
				rs = reportRunner.getResultSet();

				int splitColumnOption = reportOptions.getSplitColumn();
				int splitColumn;
				if (splitColumnOption > 0) {
					//option has been specified. override report setting
					splitColumn = splitColumnOption;
				} else {
					splitColumn = report.getGroupColumn();
				}

				String contextPath = null;
				if (request != null) {
					contextPath = request.getContextPath();
				}

				GroupOutput groupOutput;
				switch (reportFormat) {
					case html:
						groupOutput = new GroupHtmlOutput();
						groupOutput.setWriter(writer);
						groupOutput.setContextPath(contextPath);
						break;
					case xlsx:
						groupOutput = new GroupXlsxOutput();
						groupOutput.setReportName(report.getName());
						groupOutput.setFullOutputFileName(fullOutputFilename);
						break;
					default:
						throw new IllegalArgumentException("Unexpected group report format: " + reportFormat);
				}

				rowsRetrieved = groupOutput.generateGroupReport(rs, splitColumn);

				if (reportFormat == ReportFormat.xlsx) {
					displayFileLink(fileName);
				}
			} else if (reportType.isChart()) {
				rs = reportRunner.getResultSet();

				boolean swapAxes = false;
				if (request != null) {
					if (request.getParameter("swapAxes") != null) {
						swapAxes = true;
					}
				}

				ChartUtils.prepareTheme(Config.getSettings().getPdfFontName());

				Chart chart = prepareChart(report, reportFormat, locale, rs, parameterChartOptions, reportParamsMap, reportParamsList, swapAxes);

				//store data for potential use in html and pdf output
				RowSetDynaClass data = null;
				if (parameterChartOptions.isShowData()
						&& (reportFormat == ReportFormat.html || reportFormat == ReportFormat.pdf)) {
					int rsType = rs.getType();
					if (rsType == ResultSet.TYPE_SCROLL_INSENSITIVE || rsType == ResultSet.TYPE_SCROLL_SENSITIVE) {
						rs.beforeFirst();
						boolean lowercaseColumnNames = false;
						boolean useColumnAlias = true;
						data = new RowSetDynaClass(rs, lowercaseColumnNames, useColumnAlias);
					}

				}

				//add secondary charts
				String secondaryChartSetting = report.getSecondaryCharts();
				secondaryChartSetting = StringUtils.deleteWhitespace(secondaryChartSetting);
				String[] secondaryChartIds = StringUtils.split(secondaryChartSetting, ",");
				if (secondaryChartIds != null) {
					List<Chart> secondaryCharts = new ArrayList<>();
					ReportService reportService = new ReportService();
					for (String secondaryChartIdString : secondaryChartIds) {
						int secondaryChartId = Integer.parseInt(secondaryChartIdString);
						Report secondaryReport = reportService.getReport(secondaryChartId);
						ReportRunner secondaryReportRunner = new ReportRunner();
						secondaryReportRunner.setUser(user);
						secondaryReportRunner.setReport(secondaryReport);
						secondaryReportRunner.setReportParamsMap(reportParamsMap);
						ResultSet secondaryResultSet = null;
						try {
							secondaryReportRunner.execute();
							secondaryResultSet = secondaryReportRunner.getResultSet();
							swapAxes = false;
							Chart secondaryChart = prepareChart(secondaryReport, reportFormat, locale, secondaryResultSet, parameterChartOptions, reportParamsMap, reportParamsList, swapAxes);
							secondaryCharts.add(secondaryChart);
						} finally {
							DatabaseUtils.close(secondaryResultSet);
							secondaryReportRunner.close();
						}
					}
					chart.setSecondaryCharts(secondaryCharts);
				}

				if (isJob) {
					chart.generateFile(reportFormat, fullOutputFilename, data);
				} else {
					if (reportFormat == ReportFormat.html) {
						request.setAttribute("chart", chart);

						String htmlElementId = "chart-" + reportId;
						request.setAttribute("htmlElementId", htmlElementId);

						servletContext.getRequestDispatcher("/WEB-INF/jsp/showChart.jsp").include(request, response);

						if (data != null) {
							List<DynaBean> dataRows = data.getRows();
							DynaProperty[] columns = data.getDynaProperties();
							request.setAttribute("columns", columns);
							request.setAttribute("dataRows", dataRows);
							servletContext.getRequestDispatcher("/WEB-INF/jsp/showChartData.jsp").include(request, response);
						}
					} else {
						chart.generateFile(reportFormat, fullOutputFilename, data);
						displayFileLink(fileName);
					}
					rowsRetrieved = getResultSetRowCount(rs);
				}
			} else if (reportType.isStandardOutput() && reportFormat.isJson()) {
				rs = reportRunner.getResultSet();
				JsonOutput jsonOutput = new JsonOutput();
				jsonOutput.setPrettyPrint(reportOptions.isPrettyPrint());
				JsonOutputResult jsonOutputResult = jsonOutput.generateOutput(rs);
				String jsonString = jsonOutputResult.getJsonData();
				rowsRetrieved = jsonOutputResult.getRowCount();
				switch (reportFormat) {
					case jsonBrowser:
						//https://stackoverflow.com/questions/14533530/how-to-show-pretty-print-json-string-in-a-jsp-page
						writer.print("<pre>" + jsonString + "</pre>");
						break;
					default:
						writer.print(jsonString);
				}
				writer.flush();
			} else if (reportType.isStandardOutput()) {
				StandardOutput standardOutput = getStandardOutputInstance(reportFormat, isJob, report);

				standardOutput.setWriter(writer);
				standardOutput.setFullOutputFileName(fullOutputFilename);
				standardOutput.setReportParamsList(reportParamsList); //used to show selected parameters and drilldowns
				standardOutput.setShowSelectedParameters(reportOptions.isShowSelectedParameters());
				standardOutput.setLocale(locale);
				standardOutput.setReportName(report.getName());
				standardOutput.setMessageSource(messageSource);
				standardOutput.setIsJob(isJob);

				if (request != null) {
					String contextPath = request.getContextPath();
					standardOutput.setContextPath(contextPath);
				}

				//generate output
				rs = reportRunner.getResultSet();

				StandardOutputResult standardOutputResult;
				if (reportType.isCrosstab()) {
					standardOutputResult = standardOutput.generateCrosstabOutput(rs, reportFormat);
				} else {
					if (reportFormat.isHtml() && !isJob) {
						//only drill down for html output. drill down query launched from hyperlink                                            
						standardOutput.setDrilldowns(drilldownService.getDrilldowns(reportId));
					}

					//https://stackoverflow.com/questions/16675191/get-full-url-and-query-string-in-servlet-for-both-http-and-https-requests
					if (request != null) {
						String requestBaseUrl = request.getScheme() + "://"
								+ request.getServerName()
								+ ("http".equals(request.getScheme()) && request.getServerPort() == 80 || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":" + request.getServerPort())
								+ request.getContextPath();
						standardOutput.setRequestBaseUrl(requestBaseUrl);
					}

					standardOutputResult = standardOutput.generateTabularOutput(rs, reportFormat, report);
				}

				if (standardOutputResult.isSuccess()) {
					if (!reportFormat.isHtml() && standardOutput.outputHeaderAndFooter() && !isJob) {
						displayFileLink(fileName);
					}

					rowsRetrieved = standardOutputResult.getRowCount();
				} else {
					outputResult.setSuccess(false);
					outputResult.setMessage(standardOutputResult.getMessage());
				}
			} else if (reportType == ReportType.FreeMarker) {
				FreeMarkerOutput freemarkerOutput = new FreeMarkerOutput();
				rs = reportRunner.getResultSet();
				freemarkerOutput.generateReport(report, reportParamsList, rs, writer);
				rowsRetrieved = getResultSetRowCount(rs);
			} else if (reportType == ReportType.Thymeleaf) {
				ThymeleafOutput thymeleafOutput = new ThymeleafOutput();
				rs = reportRunner.getResultSet();
				thymeleafOutput.generateReport(report, reportParamsList, rs, writer);
				rowsRetrieved = getResultSetRowCount(rs);
			} else if (reportType.isXDocReport()) {
				XDocReportOutput xdocReportOutput = new XDocReportOutput();
				rs = reportRunner.getResultSet();
				xdocReportOutput.generateReport(report, reportParamsList, rs, reportFormat, fullOutputFilename);
				rowsRetrieved = getResultSetRowCount(rs);
				if (!isJob) {
					displayFileLink(fileName);
				}
			} else if (reportType == ReportType.ReactPivot) {
				if (isJob) {
					throw new IllegalStateException("ReactPivot report type not supported for jobs");
				}

				rs = reportRunner.getResultSet();
				JsonOutput jsonOutput = new JsonOutput();
				JsonOutputResult jsonOutputResult = jsonOutput.generateOutput(rs);
				String jsonData = jsonOutputResult.getJsonData();
				rowsRetrieved = jsonOutputResult.getRowCount();

				String templateFileName = report.getTemplate();
				String jsTemplatesPath = Config.getJsTemplatesPath();
				String fullTemplateFileName = jsTemplatesPath + templateFileName;

				logger.debug("templateFileName='{}'", templateFileName);

				//need to explicitly check if template file is empty string
				//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
				if (StringUtils.isBlank(templateFileName)) {
					throw new IllegalArgumentException("Template file not specified");
				}

				File templateFile = new File(fullTemplateFileName);
				if (!templateFile.exists()) {
					throw new IllegalStateException("Template file not found: " + templateFileName);
				}

				request.setAttribute("templateFileName", templateFileName);
				request.setAttribute("rows", jsonData);
				servletContext.getRequestDispatcher("/WEB-INF/jsp/showReactPivot.jsp").include(request, response);
			} else if (reportType.isPivotTableJs()) {
				if (isJob) {
					throw new IllegalStateException("PivotTable.js report types not supported for jobs");
				}

				request.setAttribute("reportType", reportType);

				if (reportType == ReportType.PivotTableJs) {
					rs = reportRunner.getResultSet();
					JsonOutput jsonOutput = new JsonOutput();
					JsonOutputResult jsonOutputResult = jsonOutput.generateOutput(rs);
					String jsonData = jsonOutputResult.getJsonData();
					rowsRetrieved = jsonOutputResult.getRowCount();
					request.setAttribute("input", jsonData);
				}

				String templateFileName = report.getTemplate();
				String jsTemplatesPath = Config.getJsTemplatesPath();
				String fullTemplateFileName = jsTemplatesPath + templateFileName;

				logger.debug("templateFileName='{}'", templateFileName);

				//template file not mandatory
				if (StringUtils.isNotBlank(templateFileName)) {
					File templateFile = new File(fullTemplateFileName);
					if (!templateFile.exists()) {
						throw new IllegalStateException("Template file not found: " + templateFileName);
					}
					request.setAttribute("templateFileName", templateFileName);
				}

				if (reportType == ReportType.PivotTableJsCsvServer) {
					String optionsString = report.getOptions();

					if (StringUtils.isBlank(optionsString)) {
						throw new IllegalArgumentException("Options not specified");
					}

					ObjectMapper mapper = new ObjectMapper();
					CsvServerOptions options = mapper.readValue(optionsString, CsvServerOptions.class);
					String dataFileName = options.getDataFile();

					logger.debug("dataFileName='{}'", dataFileName);

					//need to explicitly check if file name is empty string
					//otherwise file.exists() will return true because fullDataFileName will just have the directory name
					if (StringUtils.isBlank(dataFileName)) {
						throw new IllegalArgumentException("Data file not specified");
					}

					String fullDataFileName = jsTemplatesPath + dataFileName;

					File dataFile = new File(fullDataFileName);
					if (!dataFile.exists()) {
						throw new IllegalStateException("Data file not found: " + dataFileName);
					}

					request.setAttribute("dataFileName", dataFileName);
				}

				String localeString = locale.toString();

				String languageFileName = "pivot." + localeString + ".js";

				String languageFilePath = Config.getAppPath() + File.separator
						+ "js" + File.separator
						+ "pivottable-2.7.0" + File.separator
						+ languageFileName;

				File languageFile = new File(languageFilePath);

				if (languageFile.exists()) {
					request.setAttribute("locale", localeString);
				}

				servletContext.getRequestDispatcher("/WEB-INF/jsp/showPivotTableJs.jsp").include(request, response);
			} else if (reportType.isDygraphs()) {
				if (isJob) {
					throw new IllegalStateException("Dygraphs report types not supported for jobs");
				}

				request.setAttribute("reportType", reportType);

				if (reportType == ReportType.Dygraphs) {
					rs = reportRunner.getResultSet();
					CsvOutputUnivocity csvOutputUnivocity = new CsvOutputUnivocity();
					//use appropriate date formats to ensure correct interpretation by browsers
					//http://blog.dygraphs.com/2012/03/javascript-and-dates-what-mess.html
					//http://dygraphs.com/date-formats.html
					String dateFormat = "yyyy/MM/dd";
					String dateTimeFormat = "yyyy/MM/dd HH:mm";
					csvOutputUnivocity.setDateFormat(dateFormat);
					csvOutputUnivocity.setDateTimeFormat(dateTimeFormat);
					String csvString;
					try (StringWriter stringWriter = new StringWriter()) {
						csvOutputUnivocity.generateOutput(rs, stringWriter);
						csvString = stringWriter.toString();
					}
					rowsRetrieved = getResultSetRowCount(rs);
					//need to escape string for javascript, otherwise you get Unterminated string literal error
					//https://stackoverflow.com/questions/5016517/error-using-javascript-and-jsp-string-with-space-gives-unterminated-string-lit
					String escapedCsvString = Encode.forJavaScript(csvString);
					request.setAttribute("csvData", escapedCsvString);
				}

				String templateFileName = report.getTemplate();
				String jsTemplatesPath = Config.getJsTemplatesPath();
				String fullTemplateFileName = jsTemplatesPath + templateFileName;

				logger.debug("templateFileName='{}'", templateFileName);

				//template file not mandatory
				if (StringUtils.isNotBlank(templateFileName)) {
					File templateFile = new File(fullTemplateFileName);
					if (!templateFile.exists()) {
						throw new IllegalStateException("Template file not found: " + templateFileName);
					}
					request.setAttribute("templateFileName", templateFileName);
				}

				if (reportType == ReportType.DygraphsCsvServer) {
					String optionsString = report.getOptions();

					if (StringUtils.isBlank(optionsString)) {
						throw new IllegalArgumentException("Options not specified");
					}

					ObjectMapper mapper = new ObjectMapper();
					CsvServerOptions options = mapper.readValue(optionsString, CsvServerOptions.class);
					String dataFileName = options.getDataFile();

					logger.debug("dataFileName='{}'", dataFileName);

					//need to explicitly check if file name is empty string
					//otherwise file.exists() will return true because fullDataFileName will just have the directory name
					if (StringUtils.isBlank(dataFileName)) {
						throw new IllegalArgumentException("Data file not specified");
					}

					String fullDataFileName = jsTemplatesPath + dataFileName;

					File dataFile = new File(fullDataFileName);
					if (!dataFile.exists()) {
						throw new IllegalStateException("Data file not found: " + dataFileName);
					}

					request.setAttribute("dataFileName", dataFileName);
				}

				servletContext.getRequestDispatcher("/WEB-INF/jsp/showDygraphs.jsp").include(request, response);
			} else if (reportType.isDataTables()) {
				if (isJob) {
					throw new IllegalStateException("DataTables report types not supported for jobs");
				}

				request.setAttribute("reportType", reportType);

				if (reportType == ReportType.DataTables) {
					rs = reportRunner.getResultSet();
					JsonOutput jsonOutput = new JsonOutput();
					JsonOutputResult jsonOutputResult = jsonOutput.generateOutput(rs);
					String jsonData = jsonOutputResult.getJsonData();
					List<ResultSetColumn> columns = jsonOutputResult.getColumns();
					request.setAttribute("data", jsonData);
					request.setAttribute("columns", columns);
				}

				String templateFileName = report.getTemplate();
				String jsTemplatesPath = Config.getJsTemplatesPath();
				String fullTemplateFileName = jsTemplatesPath + templateFileName;

				logger.debug("templateFileName='{}'", templateFileName);

				//template file not mandatory
				if (StringUtils.isNotBlank(templateFileName)) {
					File templateFile = new File(fullTemplateFileName);
					if (!templateFile.exists()) {
						throw new IllegalStateException("Template file not found: " + templateFileName);
					}
					request.setAttribute("templateFileName", templateFileName);
				}

				String optionsString = report.getOptions();
				boolean showColumnFilters = true;
				if (StringUtils.isNotBlank(optionsString)) {
					ObjectMapper mapper = new ObjectMapper();
					DataTablesOptions options = mapper.readValue(optionsString, DataTablesOptions.class);
					showColumnFilters = options.isShowColumnFilters();
				}
				request.setAttribute("showColumnFilters", showColumnFilters);

				if (reportType == ReportType.DataTablesCsvServer) {
					if (StringUtils.isBlank(optionsString)) {
						throw new IllegalArgumentException("Options not specified");
					}

					ObjectMapper mapper = new ObjectMapper();
					DataTablesOptions options = mapper.readValue(optionsString, DataTablesOptions.class);
					String dataFileName = options.getDataFile();

					logger.debug("dataFileName='{}'", dataFileName);

					//need to explicitly check if file name is empty string
					//otherwise file.exists() will return true because fullDataFileName will just have the directory name
					if (StringUtils.isBlank(dataFileName)) {
						throw new IllegalArgumentException("Data file not specified");
					}

					String fullDataFileName = jsTemplatesPath + dataFileName;

					File dataFile = new File(fullDataFileName);
					if (!dataFile.exists()) {
						throw new IllegalStateException("Data file not found: " + dataFileName);
					}

					request.setAttribute("dataFileName", dataFileName);
				}

				String languageTag = locale.toLanguageTag();
				request.setAttribute("languageTag", languageTag);
				String localeString = locale.toString();
				request.setAttribute("locale", localeString);
				servletContext.getRequestDispatcher("/WEB-INF/jsp/showDataTables.jsp").include(request, response);
			} else if (reportType == ReportType.FixedWidth) {
				String optionsString = report.getOptions();
				if (StringUtils.isBlank(optionsString)) {
					throw new IllegalArgumentException("Options not specified");
				}

				ObjectMapper mapper = new ObjectMapper();
				FixedWidthOptions options = mapper.readValue(optionsString, FixedWidthOptions.class);
				FixedWidthOutput fixedWidthOutput = new FixedWidthOutput();
				rs = reportRunner.getResultSet();
				fixedWidthOutput.generateOutput(rs, writer, options);
				rowsRetrieved = getResultSetRowCount(rs);
			} else if (reportType == ReportType.C3) {
				if (isJob) {
					throw new IllegalStateException("C3.js report type not supported for jobs");
				}

				rs = reportRunner.getResultSet();
				JsonOutput jsonOutput = new JsonOutput();
				JsonOutputResult jsonOutputResult = jsonOutput.generateOutput(rs);
				String jsonData = jsonOutputResult.getJsonData();
				rowsRetrieved = jsonOutputResult.getRowCount();

				String templateFileName = report.getTemplate();
				String jsTemplatesPath = Config.getJsTemplatesPath();
				String fullTemplateFileName = jsTemplatesPath + templateFileName;

				logger.debug("templateFileName='{}'", templateFileName);

				//need to explicitly check if template file is empty string
				//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
				if (StringUtils.isBlank(templateFileName)) {
					throw new IllegalArgumentException("Template file not specified");
				}

				File templateFile = new File(fullTemplateFileName);
				if (!templateFile.exists()) {
					throw new IllegalStateException("Template file not found: " + templateFileName);
				}

				String optionsString = report.getOptions();
				if (StringUtils.isNotBlank(optionsString)) {
					ObjectMapper mapper = new ObjectMapper();
					C3Options options = mapper.readValue(optionsString, C3Options.class);
					String cssFileName = options.getCssFile();

					logger.debug("cssFileName='{}'", cssFileName);

					//need to explicitly check if file name is empty string
					//otherwise file.exists() will return true because fullDataFileName will just have the directory name
					if (StringUtils.isNotBlank(cssFileName)) {
						String fullCssFileName = jsTemplatesPath + cssFileName;

						File cssFile = new File(fullCssFileName);
						if (!cssFile.exists()) {
							throw new IllegalStateException("Css file not found: " + cssFileName);
						}

						request.setAttribute("cssFileName", cssFileName);
					}
				}

				request.setAttribute("templateFileName", templateFileName);
				request.setAttribute("data", jsonData);
				servletContext.getRequestDispatcher("/WEB-INF/jsp/showC3.jsp").include(request, response);
			} else if (reportType == ReportType.ChartJs) {
				if (isJob) {
					throw new IllegalStateException("Chart.js report type not supported for jobs");
				}

				rs = reportRunner.getResultSet();
				JsonOutput jsonOutput = new JsonOutput();
				JsonOutputResult jsonOutputResult = jsonOutput.generateOutput(rs);
				String jsonData = jsonOutputResult.getJsonData();
				rowsRetrieved = jsonOutputResult.getRowCount();

				String templateFileName = report.getTemplate();
				String jsTemplatesPath = Config.getJsTemplatesPath();
				String fullTemplateFileName = jsTemplatesPath + templateFileName;

				logger.debug("templateFileName='{}'", templateFileName);

				//need to explicitly check if template file is empty string
				//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
				if (StringUtils.isBlank(templateFileName)) {
					throw new IllegalArgumentException("Template file not specified");
				}

				File templateFile = new File(fullTemplateFileName);
				if (!templateFile.exists()) {
					throw new IllegalStateException("Template file not found: " + templateFileName);
				}

				ChartJsOptions options;
				String optionsString = report.getOptions();
				if (StringUtils.isBlank(optionsString)) {
					options = new ChartJsOptions();
				} else {
					ObjectMapper mapper = new ObjectMapper();
					options = mapper.readValue(optionsString, ChartJsOptions.class);
				}

				request.setAttribute("options", options);
				request.setAttribute("templateFileName", templateFileName);
				request.setAttribute("data", jsonData);
				servletContext.getRequestDispatcher("/WEB-INF/jsp/showChartJs.jsp").include(request, response);
			} else if (reportType.isDatamaps()) {
				if (isJob) {
					throw new IllegalStateException("Datamaps report types not supported for jobs");
				}

				request.setAttribute("reportType", reportType);

				if (reportType == ReportType.Datamaps) {
					rs = reportRunner.getResultSet();
					JsonOutput jsonOutput = new JsonOutput();
					JsonOutputResult jsonOutputResult = jsonOutput.generateOutput(rs);
					String jsonData = jsonOutputResult.getJsonData();
					rowsRetrieved = jsonOutputResult.getRowCount();
					request.setAttribute("data", jsonData);
				}

				String templateFileName = report.getTemplate();
				String jsTemplatesPath = Config.getJsTemplatesPath();
				String fullTemplateFileName = jsTemplatesPath + templateFileName;

				logger.debug("templateFileName='{}'", templateFileName);

				//need to explicitly check if template file is empty string
				//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
				if (StringUtils.isBlank(templateFileName)) {
					throw new IllegalArgumentException("Template file not specified");
				}

				File templateFile = new File(fullTemplateFileName);
				if (!templateFile.exists()) {
					throw new IllegalStateException("Template file not found: " + templateFileName);
				}

				DatamapsOptions options;
				String optionsString = report.getOptions();
				if (StringUtils.isBlank(optionsString)) {
					options = new DatamapsOptions();
				} else {
					ObjectMapper mapper = new ObjectMapper();
					options = mapper.readValue(optionsString, DatamapsOptions.class);
				}

				String datamapsJsFileName = options.getDatamapsJsFile();

				if (StringUtils.isBlank(datamapsJsFileName)) {
					throw new IllegalArgumentException("Datamaps js file not specified");
				}

				String fullDatamapsJsFileName = jsTemplatesPath + datamapsJsFileName;
				File datamapsJsFile = new File(fullDatamapsJsFileName);
				if (!datamapsJsFile.exists()) {
					throw new IllegalStateException("Datamaps js file not found: " + datamapsJsFileName);
				}

				String dataFileName = options.getDataFile();
				if (StringUtils.isNotBlank(dataFileName)) {
					String fullDataFileName = jsTemplatesPath + dataFileName;
					File dataFile = new File(fullDataFileName);
					if (!dataFile.exists()) {
						throw new IllegalStateException("Data file not found: " + dataFileName);
					}
				}

				String mapFileName = options.getMapFile();
				if (StringUtils.isNotBlank(mapFileName)) {
					String fullMapFileName = jsTemplatesPath + mapFileName;

					File mapFile = new File(fullMapFileName);
					if (!mapFile.exists()) {
						throw new IllegalStateException("Map file not found: " + mapFileName);
					}
				}

				String cssFileName = options.getCssFile();
				if (StringUtils.isNotBlank(cssFileName)) {
					String fullCssFileName = jsTemplatesPath + cssFileName;

					File cssFile = new File(fullCssFileName);
					if (!cssFile.exists()) {
						throw new IllegalStateException("Css file not found: " + cssFileName);
					}
				}

				request.setAttribute("options", options);
				request.setAttribute("templateFileName", templateFileName);
				servletContext.getRequestDispatcher("/WEB-INF/jsp/showDatamaps.jsp").include(request, response);
			} else if (reportType == ReportType.Leaflet) {
				if (isJob) {
					throw new IllegalStateException("Leaflet report type not supported for jobs");
				}

				rs = reportRunner.getResultSet();
				JsonOutput jsonOutput = new JsonOutput();
				JsonOutputResult jsonOutputResult = jsonOutput.generateOutput(rs);
				String jsonData = jsonOutputResult.getJsonData();
				rowsRetrieved = jsonOutputResult.getRowCount();

				String templateFileName = report.getTemplate();
				String jsTemplatesPath = Config.getJsTemplatesPath();
				String fullTemplateFileName = jsTemplatesPath + templateFileName;

				logger.debug("templateFileName='{}'", templateFileName);

				//need to explicitly check if template file is empty string
				//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
				if (StringUtils.isBlank(templateFileName)) {
					throw new IllegalArgumentException("Template file not specified");
				}

				File templateFile = new File(fullTemplateFileName);
				if (!templateFile.exists()) {
					throw new IllegalStateException("Template file not found: " + templateFileName);
				}

				LeafletOptions options;
				String optionsString = report.getOptions();
				if (StringUtils.isBlank(optionsString)) {
					options = new LeafletOptions();
				} else {
					ObjectMapper mapper = new ObjectMapper();
					options = mapper.readValue(optionsString, LeafletOptions.class);
				}

				String cssFileName = options.getCssFile();
				if (StringUtils.isNotBlank(cssFileName)) {
					String fullCssFileName = jsTemplatesPath + cssFileName;

					File cssFile = new File(fullCssFileName);
					if (!cssFile.exists()) {
						throw new IllegalStateException("Css file not found: " + cssFileName);
					}
				}

				String dataFileName = options.getDataFile();
				if (StringUtils.isNotBlank(dataFileName)) {
					String fullDataFileName = jsTemplatesPath + dataFileName;
					File dataFile = new File(fullDataFileName);
					if (!dataFile.exists()) {
						throw new IllegalStateException("Data file not found: " + dataFileName);
					}
				}

				List<String> jsFileNames = options.getJsFiles();
				if (!CollectionUtils.isEmpty(jsFileNames)) {
					for (String jsFileName : jsFileNames) {
						if (StringUtils.isNotBlank(jsFileName)) {
							String fullJsFileName = jsTemplatesPath + jsFileName;
							File jsFile = new File(fullJsFileName);
							if (!jsFile.exists()) {
								throw new IllegalStateException("Js file not found: " + jsFileName);
							}
						}
					}
				}

				request.setAttribute("options", options);
				request.setAttribute("data", jsonData);
				request.setAttribute("templateFileName", templateFileName);
				servletContext.getRequestDispatcher("/WEB-INF/jsp/showLeaflet.jsp").include(request, response);
			} else {
				throw new IllegalArgumentException("Unexpected report type: " + reportType);
			}
		} finally {
			DatabaseUtils.close(rs);
		}

		outputResult.setRowCount(rowsRetrieved);

		return outputResult;
	}

	/**
	 * Instantiates an appropriate chart object, sets some parameters and
	 * prepares the chart dataset
	 *
	 * @param report the chart's report
	 * @param reportFormat the report format to use
	 * @param locale the locale to use
	 * @param rs the resultset that has the chart data
	 * @param parameterChartOptions the parameter chart options
	 * @param reportParamsMap the report parameters map
	 * @param reportParamsList the report parameters list
	 * @param swapAxes whether to swap the values of the x and y axes
	 * @return the prepared chart
	 * @throws SQLException
	 */
	private Chart prepareChart(Report report, ReportFormat reportFormat, Locale locale,
			ResultSet rs, ChartOptions parameterChartOptions,
			Map<String, ReportParameter> reportParamsMap,
			List<ReportParameter> reportParamsList, boolean swapAxes) throws SQLException {

		ReportType reportType = report.getReportType();
		Chart chart = getChartInstance(reportType);

		ChartOptions effectiveChartOptions = getEffectiveChartOptions(report, parameterChartOptions, reportFormat);

		String shortDescription = report.getShortDescription();
		RunReportHelper runReportHelper = new RunReportHelper();
		shortDescription = runReportHelper.performDirectParameterSubstitution(shortDescription, reportParamsMap);

		chart.setReportType(reportType);
		chart.setLocale(locale);
		chart.setChartOptions(effectiveChartOptions);
		chart.setTitle(shortDescription);
		chart.setXAxisLabel(report.getxAxisLabel());
		chart.setYAxisLabel(report.getyAxisLabel());
		chart.setSwapAxes(swapAxes);

		Drilldown drilldown = null;
		if (reportFormat == ReportFormat.html) {
			int reportId = report.getReportId();
			List<Drilldown> drilldowns = drilldownService.getDrilldowns(reportId);
			if (!drilldowns.isEmpty()) {
				drilldown = drilldowns.get(0);
			}
		}

		chart.prepareDataset(rs, drilldown, reportParamsList);

		return chart;
	}

	/**
	 * Returns an appropriate instance of a chart object based on the given
	 * report type
	 *
	 * @param reportType the report type
	 * @return an appropriate instance of a chart object
	 * @throws IllegalArgumentException
	 */
	private Chart getChartInstance(ReportType reportType) throws IllegalArgumentException {
		logger.debug("Entering getChartInstance: reportType={}", reportType);

		Chart chart;
		switch (reportType) {
			case Pie2DChart:
			case Pie3DChart:
				chart = new PieChart(reportType);
				break;
			case SpeedometerChart:
				chart = new SpeedometerChart();
				break;
			case XYChart:
				chart = new XYChart();
				break;
			case TimeSeriesChart:
			case DateSeriesChart:
				chart = new TimeSeriesBasedChart(reportType);
				break;
			case LineChart:
			case HorizontalBar2DChart:
			case HorizontalBar3DChart:
			case VerticalBar2DChart:
			case VerticalBar3DChart:
			case StackedHorizontalBar2DChart:
			case StackedHorizontalBar3DChart:
			case StackedVerticalBar2DChart:
			case StackedVerticalBar3DChart:
				chart = new CategoryBasedChart(reportType);
				break;
			case BubbleChart:
			case HeatmapChart:
				chart = new XYZBasedChart(reportType);
				break;
			default:
				throw new IllegalArgumentException("Unexpected chart report type: " + reportType);
		}

		return chart;
	}

	/**
	 * Returns a standard output instance based on the given report format
	 *
	 * @param reportFormat the report format
	 * @param isJob whether this is a job or an interactive report
	 * @param report the report that is being run
	 * @return the standard output instance
	 * @throws IllegalArgumentException
	 * @throws java.io.IOException
	 */
	public StandardOutput getStandardOutputInstance(ReportFormat reportFormat, boolean isJob,
			Report report) throws IllegalArgumentException, IOException {

		logger.debug("Entering getStandardOutputInstance: reportFormat={}, isJob={}, report={}", reportFormat, isJob, report);

		StandardOutput standardOutput;

		String xlsDateFormat;
		String reportDateFormat = report.getDateFormat();
		if (StringUtils.isBlank(reportDateFormat)) {
			xlsDateFormat = null;
		} else {
			xlsDateFormat = reportDateFormat;
		}

		String xlsNumberFormat;
		String reportNumberFormat = report.getNumberFormat();
		if (StringUtils.isBlank(reportNumberFormat)) {
			xlsNumberFormat = null;
		} else {
			xlsNumberFormat = reportNumberFormat;
		}

		switch (reportFormat) {
			case htmlPlain:
				standardOutput = new HtmlPlainOutput(isJob);
				break;
			case htmlFancy:
				standardOutput = new HtmlFancyOutput();
				break;
			case htmlGrid:
				standardOutput = new HtmlGridOutput();
				break;
			case htmlDataTable:
				standardOutput = new HtmlDataTableOutput();
				break;
			case pdf:
				standardOutput = new PdfOutput();
				break;
			case xml:
				standardOutput = new XmlOutput();
				break;
			case rss20:
				standardOutput = new Rss20Output();
				break;
			case xls:
				standardOutput = new XlsOutput(xlsDateFormat, xlsNumberFormat);
				break;
			case xlsZip:
				standardOutput = new XlsOutput(ZipType.Zip, xlsDateFormat, xlsNumberFormat);
				break;
			case xlsx:
				standardOutput = new XlsxOutput(xlsDateFormat, xlsNumberFormat);
				break;
			case slk:
				standardOutput = new SlkOutput();
				break;
			case slkZip:
				standardOutput = new SlkOutput(ZipType.Zip);
				break;
			case tsv:
				standardOutput = new TsvOutput();
				break;
			case tsvZip:
				standardOutput = new TsvOutput(ZipType.Zip);
				break;
			case tsvGz:
				standardOutput = new TsvOutput(ZipType.Gzip);
				break;
			case docx:
				standardOutput = new DocxOutput();
				break;
			case odt:
				standardOutput = new OdtOutput();
				break;
			case ods:
				standardOutput = new OdsOutput();
				break;
			case csv:
				CsvOutputArtOptions options;
				String optionsString = report.getOptions();
				if (StringUtils.isBlank(optionsString)) {
					options = new CsvOutputArtOptions(); //has default values set
				} else {
					ObjectMapper mapper = new ObjectMapper();
					options = mapper.readValue(optionsString, CsvOutputArtOptions.class);
				}
				standardOutput = new CsvOutputArt(options);
				break;
			default:
				throw new IllegalArgumentException("Unexpected standard output report format: " + reportFormat);
		}

		return standardOutput;
	}

	/**
	 * Outputs a file link to the web browser
	 *
	 * @param fileName the file name
	 * @throws IOException
	 * @throws ServletException
	 */
	private void displayFileLink(String fileName) throws IOException, ServletException {
		if (request == null || servletContext == null) {
			return;
		}

		//display link to access report
		request.setAttribute("fileName", fileName);
		servletContext.getRequestDispatcher("/WEB-INF/jsp/showFileLink.jsp").include(request, response);
	}

	/**
	 * Returns the row count for a given resultset
	 *
	 * @param rs the resultset
	 * @return the row count
	 */
	private Integer getResultSetRowCount(ResultSet rs) {
		Integer rowCount = null;

		try {
			if (rs != null) {
				int rsType = rs.getType();
				if (rsType == ResultSet.TYPE_SCROLL_INSENSITIVE || rsType == ResultSet.TYPE_SCROLL_SENSITIVE) {
					//resultset is scrollable
					rs.last();
					rowCount = rs.getRow();
					rs.beforeFirst();
				}
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}

		return rowCount;
	}

	/**
	 * Returns the final chart options to use based on the given chart options,
	 * report and report format
	 *
	 * @param report the report
	 * @param parameterChartOptions the passed chart options
	 * @param reportFormat the report format
	 * @return the final chart options
	 */
	private ChartOptions getEffectiveChartOptions(Report report, ChartOptions parameterChartOptions,
			ReportFormat reportFormat) {

		ChartOptions reportChartOptions = report.getChartOptions();
		ChartOptions effectiveChartOptions = parameterChartOptions;

		Integer width = effectiveChartOptions.getWidth();
		if (width == null || width <= 0) {
			effectiveChartOptions.setWidth(reportChartOptions.getWidth());
		}
		width = effectiveChartOptions.getWidth();
		if (width == null || width <= 0) {
			final int DEFAULT_WIDTH = 500;
			effectiveChartOptions.setWidth(DEFAULT_WIDTH);
		}

		Integer height = effectiveChartOptions.getHeight();
		if (height == null || height <= 0) {
			effectiveChartOptions.setHeight(reportChartOptions.getHeight());
		}
		height = effectiveChartOptions.getHeight();
		if (height == null || height <= 0) {
			final int DEFAULT_HEIGHT = 300;
			effectiveChartOptions.setHeight(DEFAULT_HEIGHT);
		}

		//set default label format.
		//{2} for category based charts
		//{0} ({2}) for pie chart html output
		//{0} = {1} ({2}) for pie chart png and pdf output
		ReportType reportType = report.getReportType();
		String labelFormat = effectiveChartOptions.getLabelFormat();
		if (StringUtils.isBlank(labelFormat)) {
			effectiveChartOptions.setLabelFormat(reportChartOptions.getLabelFormat());
		}
		labelFormat = effectiveChartOptions.getLabelFormat();
		if (StringUtils.isBlank(labelFormat)) {
			if (reportType == ReportType.Pie2DChart || reportType == ReportType.Pie3DChart) {
				if (reportFormat == ReportFormat.html) {
					labelFormat = "{0} ({2})";
				} else {
					labelFormat = "{0} = {1} ({2})";
				}
			} else {
				labelFormat = "{2}";
			}
			effectiveChartOptions.setLabelFormat(labelFormat);
		}

		return effectiveChartOptions;
	}

}
