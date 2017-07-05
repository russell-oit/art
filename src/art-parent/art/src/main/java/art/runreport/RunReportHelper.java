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

import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.enums.AccessLevel;
import art.enums.ParameterDataType;
import art.enums.ReportType;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.user.User;
import art.utils.ArtUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility functions used to aid running of reports
 *
 * @author Timothy Anyona
 */
public class RunReportHelper {

	private static final Logger logger = LoggerFactory.getLogger(RunReportHelper.class);

	/**
	 * Returns the connection to use for running the given report
	 *
	 * @param report the report
	 * @param reportParams the report parameters
	 * @return the connection to use for running the report
	 * @throws SQLException
	 */
	public Connection getEffectiveReportDatasource(Report report,
			Collection<ReportParameter> reportParams) throws SQLException {

		logger.debug("Entering getEffectiveReportDatasource: report={}", report);

		Connection conn;

		Integer dynamicDatasourceId = null;
		if (reportParams != null) {
			for (ReportParameter reportParam : reportParams) {
				if (reportParam.getParameter().getDataType() == ParameterDataType.Datasource) {
					String[] passedValues = reportParam.getPassedParameterValues();
					if (passedValues != null && StringUtils.isNotBlank(passedValues[0])) {
						dynamicDatasourceId = (Integer) reportParam.getEffectiveActualParameterValue();
						break;
					}
				}
			}
		}

		if (dynamicDatasourceId == null) {
			//use datasource defined on the report
			Datasource reportDatasource = report.getDatasource();
			conn = DbConnections.getConnection(reportDatasource.getDatasourceId());
		} else {
			//use datasource indicated in parameter
			conn = DbConnections.getConnection(dynamicDatasourceId);
		}

		return conn;
	}

	/**
	 * Returns the connection to use for running the given report
	 *
	 * @param report the report
	 * @param reportParamsMap the report parameters
	 * @return the connection to use for running the report
	 * @throws SQLException
	 */
	public Connection getEffectiveReportDatasource(Report report,
			Map<String, ReportParameter> reportParamsMap) throws SQLException {

		Collection<ReportParameter> reportParams = null;
		if (reportParamsMap != null) {
			reportParams = reportParamsMap.values();
		}

		return getEffectiveReportDatasource(report, reportParams);
	}

	/**
	 * Sets request attributes relevant for the select parameters portion of the
	 * run report page
	 *
	 * @param report the report that is being run
	 * @param request the http request
	 * @param session the http session
	 * @param reportService the report service to use
	 * @throws ParseException
	 * @throws SQLException
	 * @throws java.io.IOException
	 */
	public void setSelectReportParameterAttributes(Report report,
			HttpServletRequest request, HttpSession session,
			ReportService reportService, Locale locale)
			throws ParseException, SQLException, IOException {

		logger.debug("Entering setSelectReportParameterAttributes: report={}", report);

		boolean startSelectParametersHidden = Boolean.parseBoolean(request.getParameter("startSelectParametersHidden"));
		request.setAttribute("startSelectParametersHidden", startSelectParametersHidden);

		request.setAttribute("report", report);

		User sessionUser = (User) session.getAttribute("sessionUser");

		//prepare report parameters
		ParameterProcessor paramProcessor = new ParameterProcessor();
		paramProcessor.setLocale(locale);
		ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);
		List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();

		ReportType reportType = report.getReportType();

		//create map in order to display parameters by position
		Map<Integer, ReportParameter> reportParams = new TreeMap<>();
		//for dashboard different report parameters for different reports may have
		//same position so just display all in the order of the list (an arbitrary order)
		if (reportType.isDashboard()) {
			Integer count = 0;
			for (ReportParameter reportParam : reportParamsList) {
				count++;
				reportParams.put(count, reportParam);
			}
		} else {
			for (ReportParameter reportParam : reportParamsList) {
				reportParams.put(reportParam.getPosition(), reportParam);
			}
		}

		request.setAttribute("reportParams", reportParams);

		boolean enableReportFormats;
		switch (reportType) {
			case JPivotMondrian:
			case JPivotMondrianXmla:
			case JPivotSqlServerXmla:
			case Update:
			case Text:
			case TabularHtml:
			case CrosstabHtml:
			case JxlsArt:
			case JxlsTemplate:
			case FreeMarker:
			case Thymeleaf:
			case ReactPivot:
			case PivotTableJs:
			case PivotTableJsCsvLocal:
			case PivotTableJsCsvServer:
			case Dygraphs:
			case DygraphsCsvLocal:
			case DygraphsCsvServer:
			case DataTables:
			case DataTablesCsvLocal:
			case DataTablesCsvServer:
			case FixedWidth:
			case C3:
			case ChartJs:
			case Datamaps:
			case DatamapsFile:
			case Leaflet:
			case OpenLayers:
			case SaikuReport:
			case SaikuConnection:
			case MongoDB:
				enableReportFormats = false;
				break;
			default:
				enableReportFormats = true;
				List<String> reportFormats = getAvailableReportFormats(reportType);
				request.setAttribute("reportFormats", reportFormats);
		}
		request.setAttribute("enableReportFormats", enableReportFormats);
		String reportFormat = (String) request.getAttribute("reportFormat");
		if (reportFormat == null) {
			reportFormat = report.getDefaultReportFormat();
		}
		request.setAttribute("reportFormat", reportFormat);

		int accessLevel = sessionUser.getAccessLevel().getValue();

		boolean enableSchedule;
		if (accessLevel >= AccessLevel.ScheduleUser.getValue()
				&& Config.getSettings().isSchedulingEnabled()) {
			enableSchedule = reportType.canSchedule();
		} else {
			enableSchedule = false;
		}
		request.setAttribute("enableSchedule", enableSchedule);

		boolean enableShowSql;
		boolean enableShowSelectedParameters;
		switch (reportType) {
			case JPivotMondrian:
			case JPivotMondrianXmla:
			case JPivotSqlServerXmla:
			case JasperReportsTemplate:
			case JxlsTemplate:
			case Text:
			case PivotTableJsCsvLocal:
			case PivotTableJsCsvServer:
			case DygraphsCsvLocal:
			case DygraphsCsvServer:
			case DataTablesCsvLocal:
			case DataTablesCsvServer:
			case DatamapsFile:
			case SaikuReport:
			case SaikuConnection:
			case MongoDB:
				enableShowSql = false;
				enableShowSelectedParameters = false;
				break;
			default:
				if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
					enableShowSql = true;
				} else {
					enableShowSql = false;
				}

				if (reportType.isDashboard()) {
					enableShowSql = false;
				}

				if (reportParamsList.isEmpty()) {
					enableShowSelectedParameters = false;
				} else {
					enableShowSelectedParameters = true;
				}
		}
		request.setAttribute("enableShowSql", enableShowSql);
		request.setAttribute("enableShowSelectedParameters", enableShowSelectedParameters);

		request.setAttribute("isChart", reportType.isChart());

		boolean enableRunInline;
		switch (reportType) {
			case JPivotMondrian:
			case JPivotMondrianXmla:
			case JPivotSqlServerXmla:
			case SaikuReport:
				enableRunInline = false;
				break;
			default:
				enableRunInline = true;
		}
		request.setAttribute("enableRunInline", enableRunInline);

		boolean enablePrint;
		switch (reportType) {
			case JPivotMondrian:
			case JPivotMondrianXmla:
			case JPivotSqlServerXmla:
			case JxlsArt:
			case JxlsTemplate:
			case JasperReportsArt:
			case JasperReportsTemplate:
			case XDocReportFreeMarkerDocx:
			case XDocReportFreeMarkerOdt:
			case XDocReportFreeMarkerPptx:
			case XDocReportVelocityDocx:
			case XDocReportVelocityOdt:
			case XDocReportVelocityPptx:
			case Update:
			case PivotTableJs:
			case PivotTableJsCsvLocal:
			case PivotTableJsCsvServer:
			case SaikuReport:
				enablePrint = false;
				break;
			default:
				enablePrint = true;
		}
		request.setAttribute("enablePrint", enablePrint);

		boolean enablePrintAlways;
		switch (reportType) {
			case TabularHtml:
			case CrosstabHtml:
			case FreeMarker:
			case Thymeleaf:
			case ReactPivot:
			case Dygraphs:
			case DygraphsCsvLocal:
			case DygraphsCsvServer:
			case DataTables:
			case DataTablesCsvLocal:
			case DataTablesCsvServer:
			case FixedWidth:
			case C3:
			case ChartJs:
			case Datamaps:
			case DatamapsFile:
			case Leaflet:
			case OpenLayers:
			case TabularHeatmap:
			case MongoDB:
				enablePrintAlways = true;
				break;
			default:
				enablePrintAlways = false;
		}
		request.setAttribute("enablePrintAlways", enablePrintAlways);

		boolean enableEmail;
		switch (reportType) {
			case Update:
			case CrosstabHtml:
			case TabularHtml:
			case Text:
			case JPivotMondrian:
			case JPivotMondrianXmla:
			case JPivotSqlServerXmla:
			case FreeMarker:
			case Thymeleaf:
			case ReactPivot:
			case PivotTableJs:
			case PivotTableJsCsvLocal:
			case PivotTableJsCsvServer:
			case Dygraphs:
			case DygraphsCsvLocal:
			case DygraphsCsvServer:
			case DataTables:
			case DataTablesCsvLocal:
			case DataTablesCsvServer:
			case FixedWidth:
			case C3:
			case ChartJs:
			case Datamaps:
			case DatamapsFile:
			case Leaflet:
			case OpenLayers:
			case TabularHeatmap:
			case SaikuReport:
			case MongoDB:
				enableEmail = false;
				break;
			default:
				enableEmail = true;
		}

		if (!Config.isEmailServerConfigured() || StringUtils.isBlank(sessionUser.getEmail())) {
			enableEmail = false;
		}

//		enableEmail = false; //disable email for now. feature may be abused by users to send spam?
		request.setAttribute("enableEmail", enableEmail);

		boolean enableSwapAxes;
		switch (reportType) {
			case XYChart:
			case LineChart:
			case HorizontalBar2DChart:
			case HorizontalBar3DChart:
			case VerticalBar2DChart:
			case VerticalBar3DChart:
			case StackedHorizontalBar2DChart:
			case StackedHorizontalBar3DChart:
			case StackedVerticalBar2DChart:
			case StackedVerticalBar3DChart:
				enableSwapAxes = true;
				break;
			default:
				enableSwapAxes = false;
		}
		request.setAttribute("enableSwapAxes", enableSwapAxes);
	}

	/**
	 * Returns the available report formats for the given report type
	 *
	 * @param reportType the report type
	 * @return the available report formats
	 */
	private List<String> getAvailableReportFormats(ReportType reportType) {
		logger.debug("Entering getAvailableReportFormats: reportType={}", reportType);

		List<String> formats = new ArrayList<>();

		if (reportType.isChart()) {
			formats.add("html");
			formats.add("pdf");
			formats.add("png");
		} else {
			switch (reportType) {
				case Tabular:
				case Crosstab:
				case LovDynamic:
					String formatsString = Config.getSettings().getReportFormats();
					String[] formatsArray = StringUtils.split(formatsString, ",");
					formats = Arrays.asList(formatsArray);
					break;
				case JasperReportsArt:
				case JasperReportsTemplate:
					formats.add("pdf");
					formats.add("docx");
					formats.add("odt");
					formats.add("xlsx");
					formats.add("ods");
					formats.add("html");
					break;
				case Group:
					formats.add("html");
					formats.add("xlsx");
					break;
				case XDocReportFreeMarkerDocx:
				case XDocReportVelocityDocx:
					formats.add("docx");
					formats.add("pdf");
					formats.add("html");
					break;
				case XDocReportFreeMarkerOdt:
				case XDocReportVelocityOdt:
					formats.add("odt");
					formats.add("pdf");
					formats.add("html");
					break;
				case XDocReportFreeMarkerPptx:
				case XDocReportVelocityPptx:
					formats.add("pptx");
					break;
				case Dashboard:
				case GridstackDashboard:
					formats.add("html");
					formats.add("pdf");
					break;
				case TabularHeatmap:
					formats.add("htmlDataTable");
					formats.add("htmlFancy");
					formats.add("htmlPlain");
					break;
				default:
					throw new IllegalArgumentException("Unexpected report type: " + reportType);
			}
		}

		return formats;
	}

	/**
	 * Replaces direct parameter substitution placeholders with parameter values
	 *
	 * @param sourceString the string the contains the parameter placeholders
	 * @param reportParamsMap the parameter values
	 * @return a string with parameter placeholders replaced with their values
	 */
	public String performDirectParameterSubstitution(String sourceString,
			Map<String, ReportParameter> reportParamsMap) {

		String placeholderPrefix = ""; //default to no prefix
		return performDirectParameterSubstitution(sourceString, placeholderPrefix, reportParamsMap);
	}

	/**
	 * Replaces direct parameter substitution placeholders with parameter values
	 *
	 * @param sourceString the string the contains the parameter placeholders
	 * @param placeholderPrefix a prefix to be added to the parameter name
	 * @param reportParamsMap the parameter values
	 * @return a string with parameter placeholders replaced with their values
	 */
	public String performDirectParameterSubstitution(String sourceString, String placeholderPrefix,
			Map<String, ReportParameter> reportParamsMap) {

		logger.debug("Entering performDirectParameterSubstitution");

		String outputString = sourceString;

		for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
			String paramName = entry.getKey();
			ReportParameter reportParam = entry.getValue();

			List<Object> actualParameterValues = reportParam.getActualParameterValues();

			if (actualParameterValues == null || actualParameterValues.isEmpty()) {
				continue;
			}

			String paramIdentifier = placeholderPrefix + "#" + paramName + "#";
			String searchString = Pattern.quote(paramIdentifier); //quote in case it contains special regex characters

			List<String> paramValues = new ArrayList<>();
			for (Object value : actualParameterValues) {
				String paramValue;
				if (value instanceof Date) {
					Date dateValue = (Date) value;
					paramValue = ArtUtils.isoDateTimeMillisecondsFormatter.format(dateValue);
				} else {
					paramValue = String.valueOf(value);
				}
				paramValues.add(paramValue);
			}

			String paramValuesString = StringUtils.join(paramValues, ",");
			String replaceString = Matcher.quoteReplacement(paramValuesString); //quote in case it contains special regex characters
			outputString = outputString.replaceAll("(?iu)" + searchString, replaceString); //(?iu) makes replace case insensitive across unicode characters
		}

		return outputString;
	}

	/**
	 * Returns the resultset type to use for a given report type
	 *
	 * @param reportType the report type
	 * @return the resultset type to use
	 */
	public int getResultSetType(ReportType reportType) {
		//is scroll insensitive much slower than forward only?
		int resultSetType;
		if (reportType == ReportType.JasperReportsArt || reportType == ReportType.JxlsArt
				|| reportType == ReportType.FreeMarker || reportType.isXDocReport()
				|| reportType == ReportType.Group || reportType.isChart()
				|| reportType == ReportType.Thymeleaf
				|| reportType == ReportType.Dygraphs) {
			//need scrollable resultset for jasper art report, jxls art report,
			//freemarker, xdocreport, thymeleaf, dygraphs in order to display record count
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
}
