/*
 * Copyright (C) 2015 Enrico Liboni <eliboni@users.sourceforge.net>
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

import art.chart.AbstractChart;
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
import art.output.StandardOutput;
import art.output.DirectReportOutputHandler;
import art.output.HtmlDataTableOutput;
import art.output.HtmlFancyOutput;
import art.output.HtmlGridOutput;
import art.output.HtmlPlainOutput;
import art.output.JasperReportsOutput;
import art.output.JxlsOutput;
import art.output.PdfOutput;
import art.output.StandardOutputResult;
import art.report.ChartOptions;
import art.report.Report;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import de.laures.cewolf.ChartValidationException;
import de.laures.cewolf.DatasetProduceException;
import de.laures.cewolf.PostProcessingException;
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
import net.sf.jxls.exception.ParsePropertyException;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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

	public void generateOutput(Report report, ReportRunner reportRunner,
			ReportType reportType, ReportFormat reportFormat, Locale locale,
			ParameterProcessorResult paramProcessorResult,
			PrintWriter writer, String fileName, String fullOutputFilename)
			throws IOException, SQLException, JRException, ParsePropertyException,
			InvalidFormatException, DatasetProduceException, ChartValidationException,
			PostProcessingException, ServletException {

		ResultSet rs = null;
		Integer rowsRetrieved = null;

		int reportId = report.getReportId();

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

		try {

			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();
			ReportOptions reportOptions = paramProcessorResult.getReportOptions();
			ChartOptions parameterChartOptions = paramProcessorResult.getChartOptions();

			//generate report output
			if (reportType.isJasperReports() || reportType.isJxls()) {
				if (reportType.isJasperReports()) {
					JasperReportsOutput jrOutput = new JasperReportsOutput();
					if (reportType == ReportType.JasperReportsArt) {
						rs = reportRunner.getResultSet();
						jrOutput.setResultSet(rs);
					}

					jrOutput.generateReport(report, reportParamsList, reportType, reportFormat, fullOutputFilename);
				} else {
					//jxls output
					JxlsOutput jxlsOutput = new JxlsOutput();
					if (reportType == ReportType.JxlsArt) {
						rs = reportRunner.getResultSet();
						jxlsOutput.setResultSet(rs);
					}

					jxlsOutput.generateReport(report, reportParamsList, reportType, fullOutputFilename);
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
					splitColumn = report.getReportTypeId();
				}

				rowsRetrieved = DirectReportOutputHandler.generateGroupReport(writer, rs, splitColumn);
			} else if (reportType.isChart()) {
				rs = reportRunner.getResultSet();

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

				//TODO set effective chart options. default to report options but override with html parameters
				//using object wrappers will require extra null checks e.g. if boolean property is true
				//alternatively use helper libraries e.g. commons-lang, BooleanUtils.isTrue( bool )
//					ChartOptions reportChartOptions = report.getChartOptions();
//					ChartOptions effectiveChartOptions = reportChartOptions;
//					BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
//					notNull.copyProperties(effectiveChartOptions, parameterChartOptions);
				//TODO set default label format. {2} for category based charts
				//{0} ({2}) for pie chart html output
				//{0} = {1} ({2}) for pie chart png and pdf output
				chart.setLocale(locale);
				chart.setChartOptions(parameterChartOptions);

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

						//TODO show data
					} else {
						chart.generateFile(reportFormat, fullOutputFilename, data);
						displayFileLink(fileName);
					}
					rowsRetrieved = getResultSetRowCount(rs);
				}
			} else if (reportType.isStandardOutput()) {
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
					case xls:
					case xlsZip:
						return PACKAGE_NAME + "XlsOutput";
					case xlsx:
						return PACKAGE_NAME + "XlsxOutput";
					case pdf:
						standardOutput=new PdfOutput();
						break;
					case slk:
					case slkZip:
						return PACKAGE_NAME + "SlkOutput";
					case tsv:
					case tsvZip:
					case tsvGz:
						return PACKAGE_NAME + "TsvOutput";
					case xml:
						return PACKAGE_NAME + "XmlOutput";
					case rss20:
						return PACKAGE_NAME + "Rss20Output";
					default:
						throw new IllegalArgumentException("Unexpected standard output report format: " + reportFormat);
				}

				standardOutput.setWriter(writer);
				standardOutput.setFullOutputFileName(fullOutputFilename);
				standardOutput.setReportParamsList(reportParamsList); //used to show selected parameters and drilldowns
				standardOutput.setShowSelectedParameters(reportOptions.isShowSelectedParameters());

				if (request != null) {
					String contextPath = request.getContextPath();
					standardOutput.setContextPath(contextPath);
				}

				//generate output
				rs = reportRunner.getResultSet();
				StandardOutputResult outputResult;

				if (reportType.isCrosstab()) {
					outputResult = DirectReportOutputHandler.flushXOutput(standardOutput, rs);
				} else {
					if (reportFormat.isHtml()) {
						//only drill down for html output. drill down query launched from hyperlink                                            
						standardOutput.setDrilldowns(drilldownService.getDrilldowns(reportId));
					}
					outputResult = standardOutput.generateTabularOutput(rs, reportFormat);
				}

				if (outputResult.isSuccess()) {
					rowsRetrieved = outputResult.getRowCount();
				} else {
					model.addAttribute("message", outputResult.getMessage());
					return errorPage;
				}
			}

		} finally {
			DatabaseUtils.close(rs);
		}

	}

	private void displayFileLink(String fileName) throws IOException, ServletException {
		//display link to access report
		request.setAttribute("fileName", fileName);
		servletContext.getRequestDispatcher("/WEB-INF/jsp/showFileLink.jsp").include(request, response);
	}

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

}
