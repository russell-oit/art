/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
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
import art.output.JxlsOutput;
import art.output.PdfOutput;
import art.output.Rss20Output;
import art.output.SlkOutput;
import art.output.StandardOutputResult;
import art.output.TsvOutput;
import art.output.XDocReportOutput;
import art.output.XlsOutput;
import art.output.XlsxOutput;
import art.output.XmlOutput;
import art.report.ChartOptions;
import art.report.Report;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import de.laures.cewolf.ChartValidationException;
import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.PostProcessingException;
import fr.opensagres.xdocreport.core.XDocReportException;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	 */
	public ReportOutputGeneratorResult generateOutput(Report report, ReportRunner reportRunner,
			ReportFormat reportFormat, Locale locale,
			ParameterProcessorResult paramProcessorResult,
			PrintWriter writer, String fullOutputFilename)
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
				} else {
					//jxls output
					JxlsOutput jxlsOutput = new JxlsOutput();
					if (reportType == ReportType.JxlsArt) {
						rs = reportRunner.getResultSet();
						jxlsOutput.setResultSet(rs);
					}

					jxlsOutput.generateReport(report, reportParamsList, fullOutputFilename);
				}

				rowsRetrieved = getResultSetRowCount(rs);
				displayFileLink(fileName);
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

				//can have other group output formats depending on selected
				//report format e.g. xls group reports
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

				ChartOptions effectiveChartOptions = getEffectiveChartOptions(report, parameterChartOptions, reportFormat);

				String shortDescription = report.getShortDescription();
				RunReportHelper runReportHelper = new RunReportHelper();
				shortDescription = runReportHelper.performDirectParameterSubstitution(shortDescription, reportParamsMap);

				chart.setLocale(locale);
				chart.setChartOptions(effectiveChartOptions);
				chart.setTitle(shortDescription);

				Drilldown drilldown = null;
				if (reportFormat == ReportFormat.html) {
					List<Drilldown> drilldowns = drilldownService.getDrilldowns(reportId);
					if (!drilldowns.isEmpty()) {
						drilldown = drilldowns.get(0);
					}
				}

				chart.prepareDataset(rs, drilldown, reportParamsList);

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

				ChartUtils.prepareTheme(Config.getSettings().getPdfFontName());

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
							request.setAttribute("dataRows", dataRows);
							servletContext.getRequestDispatcher("/WEB-INF/jsp/showChartData.jsp").include(request, response);
						}
					} else {
						chart.generateFile(reportFormat, fullOutputFilename, data);
						displayFileLink(fileName);
					}
					rowsRetrieved = getResultSetRowCount(rs);
				}
			} else if (reportType.isStandardOutput()) {
				StandardOutput standardOutput = getStandardOutputInstance(reportFormat, isJob);

				standardOutput.setWriter(writer);
				standardOutput.setFullOutputFileName(fullOutputFilename);
				standardOutput.setReportParamsList(reportParamsList); //used to show selected parameters and drilldowns
				standardOutput.setShowSelectedParameters(reportOptions.isShowSelectedParameters());
				standardOutput.setLocale(locale);
				standardOutput.setReportName(report.getName());

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
					standardOutputResult = standardOutput.generateTabularOutput(rs, reportFormat);
				}

				if (standardOutputResult.isSuccess()) {
					if (!reportFormat.isHtml() && standardOutput.outputHeaderandFooter() && !isJob) {
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
			} else if (reportType.isXDocReport()) {
				XDocReportOutput xdocReportOutput = new XDocReportOutput();
				rs = reportRunner.getResultSet();
				xdocReportOutput.generateReport(report, reportParamsList, rs, reportFormat, fullOutputFilename);
				rowsRetrieved = getResultSetRowCount(rs);
				displayFileLink(fileName);
			}
		} finally {
			DatabaseUtils.close(rs);
		}

		outputResult.setRowCount(rowsRetrieved);

		return outputResult;
	}

	/**
	 * Returns a standard output instance based on the given report format
	 *
	 * @param reportFormat the report format
	 * @param isJob whether this is a job or an interactive report
	 * @return the standard output instance
	 * @throws IllegalArgumentException
	 */
	public StandardOutput getStandardOutputInstance(ReportFormat reportFormat, boolean isJob)
			throws IllegalArgumentException {

		logger.debug("Entering getStandardOutputInstance: reportFormat={}, isJob={}", reportFormat, isJob);

		StandardOutput standardOutput;

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
				standardOutput = new XlsOutput();
				break;
			case xlsZip:
				standardOutput = new XlsOutput(ZipType.Zip);
				break;
			case xlsx:
				standardOutput = new XlsxOutput();
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
