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
import art.dbutils.DatabaseUtils;
import art.encryptor.Encryptor;
import art.enums.ColumnType;
import art.enums.EncryptorType;
import art.enums.ParameterDataType;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.enums.SqlColumnType;
import art.output.ColumnTypeDefinition;
import art.output.ResultSetColumn;
import art.parameter.Parameter;
import art.report.ChartOptions;
import art.report.Report;
import art.reportoptions.GeneralReportOptions;
import art.reportoptions.GroovyOptions;
import art.reportoptions.TemplateResultOptions;
import art.reportoptions.ViewOptions;
import art.reportparameter.ReportParameter;
import art.selfservice.SelfServiceColumn;
import art.selfservice.SelfServiceOptions;
import art.servlets.Config;
import art.user.User;
import art.utils.ArtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itfsw.query.builder.support.model.JsonRule;
import groovy.sql.GroovyRowResult;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
	 * @param reportParamsMap the report parameters
	 * @return the connection to use for running the report
	 * @throws SQLException
	 */
	public Connection getEffectiveReportConnection(Report report,
			Map<String, ReportParameter> reportParamsMap) throws SQLException {

		Collection<ReportParameter> reportParams = null;
		if (reportParamsMap != null) {
			reportParams = reportParamsMap.values();
		}

		return getEffectiveReportConnection(report, reportParams);
	}

	/**
	 * Returns the connection to use for running the given report
	 *
	 * @param report the report
	 * @param reportParams the report parameters
	 * @return the connection to use for running the report
	 * @throws SQLException
	 */
	public Connection getEffectiveReportConnection(Report report,
			Collection<ReportParameter> reportParams) throws SQLException {

		logger.debug("Entering getEffectiveReportConnection: report={}", report);

		Connection conn = null;

		String dynamicDatasourceIdString = null;
		if (reportParams != null) {
			for (ReportParameter reportParam : reportParams) {
				if (reportParam.getParameter().getDataType() == ParameterDataType.Datasource) {
					String[] passedValues = reportParam.getPassedParameterValues();
					if (passedValues != null && StringUtils.isNotBlank(passedValues[0])) {
						dynamicDatasourceIdString = (String) reportParam.getEffectiveActualParameterValue();
						break;
					}
				}
			}
		}

		logger.debug("dynamicDatasourceIdString='{}'", dynamicDatasourceIdString);

		if (dynamicDatasourceIdString == null) {
			//use datasource defined on the report
			Datasource reportDatasource = report.getDatasource();
			if (reportDatasource != null) {
				conn = DbConnections.getConnection(reportDatasource.getDatasourceId());
			}
		} else {
			//use datasource indicated in parameter
			if (NumberUtils.isCreatable(dynamicDatasourceIdString)) {
				//search with datasource id
				int dynamicDatasourceIdInt = Integer.parseInt(dynamicDatasourceIdString);
				conn = DbConnections.getConnection(dynamicDatasourceIdInt);
			} else {
				//search with datasource name
				conn = DbConnections.getConnection(dynamicDatasourceIdString);
			}
		}

		return conn;
	}

	/**
	 * Sets request attributes relevant for the select parameters portion of the
	 * run report page
	 *
	 * @param report the report that is being run
	 * @param request the http request
	 * @param session the http session
	 * @param locale the current locale
	 * @throws ParseException
	 * @throws SQLException
	 * @throws java.io.IOException
	 */
	public void setSelectReportParameterAttributes(Report report,
			HttpServletRequest request, HttpSession session, Locale locale)
			throws ParseException, SQLException, IOException {

		ParameterProcessorResult paramProcessorResult = null;
		setSelectReportParameterAttributes(report, request, session, locale, paramProcessorResult);
	}

	/**
	 * Sets request attributes relevant for the select parameters portion of the
	 * run report page
	 *
	 * @param report the report that is being run
	 * @param request the http request
	 * @param session the http session
	 * @param locale the current locale
	 * @param paramProcessorResult the param processor result
	 * @throws ParseException
	 * @throws SQLException
	 * @throws java.io.IOException
	 */
	public void setSelectReportParameterAttributes(Report report,
			HttpServletRequest request, HttpSession session, Locale locale,
			ParameterProcessorResult paramProcessorResult)
			throws ParseException, SQLException, IOException {

		logger.debug("Entering setSelectReportParameterAttributes: report={}", report);

		boolean startSelectParametersHidden = BooleanUtils.toBoolean(request.getParameter("startSelectParametersHidden"));
		request.setAttribute("startSelectParametersHidden", startSelectParametersHidden);

		request.setAttribute("report", report);
		request.setAttribute("locale", locale);

		setRefreshPeriodAttribute(report, request);

		User sessionUser = (User) session.getAttribute("sessionUser");

		//prepare report parameters
		if (paramProcessorResult == null) {
			ParameterProcessor paramProcessor = new ParameterProcessor();
			paramProcessor.setUseSavedValues(true);
			paramProcessor.setParameterSelection(true);
			paramProcessorResult = paramProcessor.processHttpParameters(request, locale);
		}
		List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();
		ReportOptions reportOptions = paramProcessorResult.getReportOptions();

		request.setAttribute("reportOptions", reportOptions);

		ChartOptions parameterChartOptions = paramProcessorResult.getChartOptions();
		ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();
		ChartOptions effectiveChartOptions = reportOutputGenerator.getEffectiveChartOptions(report, parameterChartOptions);
		request.setAttribute("chartOptions", effectiveChartOptions);

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

		boolean showSaveParameterSelection = false;
		if (reportType.isChart()) {
			showSaveParameterSelection = true;
		} else {
			for (ReportParameter reportParam : reportParamsList) {
				Parameter param = reportParam.getParameter();
				if (param.isForDisplay()) {
					showSaveParameterSelection = true;
					break;
				}
			}
		}
		request.setAttribute("showSaveParameterSelection", showSaveParameterSelection);

		boolean hasDateParam = false;
		for (ReportParameter reportParam : reportParamsList) {
			Parameter param = reportParam.getParameter();
			if (param.isForDisplay() && param.getDataType().isDate()) {
				hasDateParam = true;
				break;
			}
		}
		request.setAttribute("hasDateParam", hasDateParam);

		boolean hasDateRangeParam = false;
		for (ReportParameter reportParam : reportParamsList) {
			Parameter param = reportParam.getParameter();
			if (param.isForDisplay() && param.getDataType() == ParameterDataType.DateRange) {
				hasDateRangeParam = true;
				break;
			}
		}
		request.setAttribute("hasDateRangeParam", hasDateRangeParam);

		boolean hasLovParam = false;
		for (ReportParameter reportParam : reportParamsList) {
			Parameter param = reportParam.getParameter();
			if (param.isForDisplay() && param.isUseLov()) {
				hasLovParam = true;
				break;
			}
		}
		request.setAttribute("hasLovParam", hasLovParam);

		boolean hasChainedParam = false;
		for (ReportParameter reportParam : reportParamsList) {
			Parameter param = reportParam.getParameter();
			if (param.isForDisplay() && reportParam.isChained()) {
				hasChainedParam = true;
				break;
			}
		}
		request.setAttribute("hasChainedParam", hasChainedParam);

		boolean hasRobinHerbotsMask = false;
		for (ReportParameter reportParam : reportParamsList) {
			Parameter param = reportParam.getParameter();
			if (param.isForDisplay() && param.hasRobinHerbotsMask()) {
				hasRobinHerbotsMask = true;
				break;
			}
		}
		request.setAttribute("hasRobinHerbotsMask", hasRobinHerbotsMask);

		request.setAttribute("runId", ArtUtils.getUniqueId(report.getReportId()));

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
			case C3:
			case ChartJs:
			case Datamaps:
			case DatamapsFile:
			case Leaflet:
			case OpenLayers:
			case SaikuReport:
			case SaikuConnection:
			case MongoDB:
			case OrgChartDatabase:
			case OrgChartJson:
			case OrgChartList:
			case OrgChartAjax:
			case Plotly:
			case XDocReportFreeMarkerPptx:
			case XDocReportVelocityPptx:
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
			reportFormat = request.getParameter("reportFormat");
			if (reportFormat == null) {
				reportFormat = report.getDefaultReportFormat();
			}
		}
		request.setAttribute("reportFormat", reportFormat);

		boolean enableSchedule;
		if (sessionUser.hasPermission("schedule_jobs")
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
			case OrgChartJson:
			case OrgChartList:
			case OrgChartAjax:
			case ReportEngineFile:
				enableShowSql = false;
				enableShowSelectedParameters = false;
				break;
			default:
				if (sessionUser.hasConfigureReportsPermission()) {
					enableShowSql = true;
				} else {
					enableShowSql = false;
				}

				if (reportType.isDashboard() || reportType == ReportType.JxlsTemplate) {
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
			case ReactPivot:
			case GridstackDashboard:
			case ChartJs:
			case C3:
			case TabularHeatmap:
			case OrgChartDatabase:
			case OrgChartJson:
			case OrgChartList:
			case OrgChartAjax:
			case Plotly:
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
			case Dygraphs:
			case DygraphsCsvLocal:
			case DygraphsCsvServer:
			case DataTables:
			case DataTablesCsvLocal:
			case DataTablesCsvServer:
			case Datamaps:
			case DatamapsFile:
			case Leaflet:
			case OpenLayers:
			case MongoDB:
			case Velocity:
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
			case C3:
			case ChartJs:
			case Datamaps:
			case DatamapsFile:
			case Leaflet:
			case OpenLayers:
			case TabularHeatmap:
			case SaikuReport:
			case MongoDB:
			case Velocity:
			case OrgChartDatabase:
			case OrgChartJson:
			case OrgChartList:
			case OrgChartAjax:
			case Plotly:
				enableEmail = false;
				break;
			default:
				enableEmail = true;
		}

		if (!Config.getSettings().isEnableDirectReportEmailing()
				|| !Config.isEmailServerConfigured()
				|| StringUtils.isBlank(sessionUser.getEmail())) {
			enableEmail = false;
		}

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
				case FixedWidth:
					formats.add("html");
					formats.add("txt");
					formats.add("txtZip");
					break;
				case CSV:
					formats.add("html");
					formats.add("csv");
					formats.add("csvZip");
					break;
				case File:
					formats.add("html");
					formats.add("file");
					formats.add("fileZip");
					break;
				case FreeMarker:
				case Velocity:
				case Thymeleaf:
					formats.add("html");
					formats.add("htmlFancy");
					formats.add("file");
					formats.add("fileZip");
					break;
				default:
					//tabular, crosstab, lov dynamic, etc
					formats = Config.getReportFormats();
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

		if (MapUtils.isNotEmpty(reportParamsMap)) {
			for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
				String paramName = entry.getKey();
				ReportParameter reportParam = entry.getValue();

				List<Object> actualParameterValues = reportParam.getActualParameterValues();

				if (CollectionUtils.isEmpty(actualParameterValues)) {
					continue;
				}

				String searchString = placeholderPrefix + "#" + paramName + "#";

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

				String replaceString = StringUtils.join(paramValues, ",");
				outputString = StringUtils.replaceIgnoreCase(outputString, searchString, replaceString);
			}
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
		if (reportType.requiresScrollableResultSet() || reportType.isXDocReport()
				|| reportType == ReportType.JasperReportsArt
				|| reportType == ReportType.JxlsArt
				|| reportType == ReportType.FreeMarker
				|| reportType == ReportType.Thymeleaf
				|| reportType == ReportType.Velocity
				|| reportType == ReportType.Dygraphs
				|| reportType == ReportType.CSV
				|| reportType == ReportType.FixedWidth) {
			//need scrollable resultset in order to run the report or in order to display record count
			resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
		} else {
			//report types will determine the record count e.g. for standard output reports
			//or no way to determine record count e.g. with jasper reports template report
			resultSetType = ResultSet.TYPE_FORWARD_ONLY;
		}

		return resultSetType;
	}

	/**
	 * Returns the open password to use for a report's output file
	 *
	 * @param report the report
	 * @param dynamicOpenPassword the dynamic open password
	 * @return the open password to use for a report's output file
	 */
	public String getEffectiveOpenPassword(Report report, String dynamicOpenPassword) {
		Objects.requireNonNull(report, "report must not be null");

		String openPassword = null;
		String reportOpenPassword = report.getOpenPassword();
		String encryptorOpenPassword = null;
		Encryptor encryptor = report.getEncryptor();
		if (encryptor != null && encryptor.isActive()
				&& encryptor.getEncryptorType() == EncryptorType.Password) {
			encryptorOpenPassword = encryptor.getOpenPassword();
		}

		if (StringUtils.isNotEmpty(reportOpenPassword)) {
			openPassword = reportOpenPassword;
		}
		if (StringUtils.isNotEmpty(encryptorOpenPassword)) {
			openPassword = encryptorOpenPassword;
		}
		if (StringUtils.isNotEmpty(dynamicOpenPassword)) {
			openPassword = dynamicOpenPassword;
		}

		return openPassword;
	}

	/**
	 * Returns the modify password to use for a report's output file
	 *
	 * @param report the report
	 * @param dynamicModifyPassword the dynamic modify password
	 * @return the modify password to use for a report's output file
	 */
	public String getEffectiveModifyPassword(Report report, String dynamicModifyPassword) {
		Objects.requireNonNull(report, "report must not be null");

		String modifyPassword = null;
		String reportModifyPassword = report.getModifyPassword();
		String encryptorModifyPassword = null;
		Encryptor encryptor = report.getEncryptor();
		if (encryptor != null && encryptor.isActive()
				&& encryptor.getEncryptorType() == EncryptorType.Password) {
			encryptorModifyPassword = encryptor.getModifyPassword();
		}

		if (StringUtils.isNotEmpty(reportModifyPassword)) {
			modifyPassword = reportModifyPassword;
		}
		if (StringUtils.isNotEmpty(encryptorModifyPassword)) {
			modifyPassword = encryptorModifyPassword;
		}
		if (StringUtils.isNotEmpty(dynamicModifyPassword)) {
			modifyPassword = dynamicModifyPassword;
		}

		return modifyPassword;
	}

	/**
	 * Returns attributes of data used in report generation
	 *
	 * @param data the data
	 * @return data attributes
	 * @throws java.lang.Exception
	 */
	public static GroovyDataDetails getGroovyDataDetails(Object data) throws Exception {
		Report report = null;
		return getGroovyDataDetails(data, report);
	}

	/**
	 * Returns attributes of data used in report generation
	 *
	 * @param data the data
	 * @param report the report
	 * @return data attributes
	 * @throws java.lang.Exception
	 */
	@SuppressWarnings("rawtypes")
	public static GroovyDataDetails getGroovyDataDetails(Object data, Report report) throws Exception {
		Objects.requireNonNull(data, "data must not be null");

		@SuppressWarnings("unchecked")
		List<? extends Object> dataList = (List<? extends Object>) data;
		int rowCount = dataList.size();

		List<String> optionsColumnNames = null;
		List<Map<String, String>> columnDataTypes = null;
		List<Map<String, String>> optionsColumnLabels = null;
		if (report != null) {
			String options = report.getOptions();
			if (StringUtils.isNotBlank(options)) {
				ObjectMapper mapper = new ObjectMapper();
				GroovyOptions groovyOptions = mapper.readValue(options, GroovyOptions.class);
				optionsColumnNames = groovyOptions.getColumns();
				columnDataTypes = groovyOptions.getColumnDataTypes();
				optionsColumnLabels = groovyOptions.getColumnLabels();
			}
		}

		int colCount = 0;
		List<String> dataColumnNames = new ArrayList<>();
		Map<Integer, ColumnTypeDefinition> dataColumnTypes = new HashMap<>();
		if (CollectionUtils.isNotEmpty(dataList)) {
			Object sample = dataList.get(0);
			if (sample instanceof GroovyRowResult) {
				GroovyRowResult sampleResult = (GroovyRowResult) sample;

				for (Entry entry : sampleResult.entrySet()) {
					colCount++;
					String columnName = String.valueOf(entry.getKey());
					dataColumnNames.add(columnName);
					Object columnValue = entry.getValue();
					dataColumnTypes.put(colCount, getColumnTypeDefinition(columnValue));
				}
			} else if (sample instanceof DynaBean) {
				DynaBean sampleBean = (DynaBean) sample;
				DynaProperty[] columns = sampleBean.getDynaClass().getDynaProperties();

				for (DynaProperty column : columns) {
					colCount++;
					String columnName = column.getName();
					dataColumnNames.add(columnName);
					Object columnValue = sampleBean.get(columnName);
					dataColumnTypes.put(colCount, getColumnTypeDefinition(columnValue));
				}
			} else if (sample instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, ? extends Object> sampleRow = (Map<String, ? extends Object>) sample;

				for (Entry<String, ? extends Object> entry : sampleRow.entrySet()) {
					colCount++;
					String columnName = entry.getKey();
					dataColumnNames.add(columnName);
					Object columnValue = entry.getValue();
					dataColumnTypes.put(colCount, getColumnTypeDefinition(columnValue));
				}
			} else {
				//https://stackoverflow.com/questions/3333974/how-to-loop-over-a-class-attributes-in-java
				//https://stackoverflow.com/questions/2638590/best-way-of-invoking-getter-by-reflection
				Map<String, Object> sampleRow = ArtUtils.objectToMap(sample);

				for (Entry<String, Object> entry : sampleRow.entrySet()) {
					colCount++;
					String columnName = entry.getKey();
					dataColumnNames.add(columnName);
					Object columnValue = entry.getValue();
					dataColumnTypes.put(colCount, getColumnTypeDefinition(columnValue));
				}
			}
		}

		List<String> columnNames = new ArrayList<>();
		Map<Integer, ColumnTypeDefinition> columnTypes = new HashMap<>();
		if (CollectionUtils.isEmpty(optionsColumnNames)) {
			columnNames.addAll(dataColumnNames);
			columnTypes.putAll(dataColumnTypes);
		} else {
			columnNames.addAll(optionsColumnNames);
		}

		if (CollectionUtils.isNotEmpty(columnDataTypes)) {
			for (int i = 1; i <= columnNames.size(); i++) {
				String dataColumnName = columnNames.get(i - 1);
				for (Map<String, String> columnDataTypeDefinition : columnDataTypes) {
					Entry<String, String> entry = columnDataTypeDefinition.entrySet().iterator().next();
					String columnName = entry.getKey();
					String dataType = entry.getValue();
					if (StringUtils.equalsIgnoreCase(columnName, dataColumnName)
							|| StringUtils.equals(columnName, String.valueOf(i))) {
						ColumnTypeDefinition columnTypeDefinition = new ColumnTypeDefinition();
						columnTypeDefinition.setColumnType(ColumnType.toEnum(dataType));
						columnTypes.put(i, columnTypeDefinition);
						break;
					}
				}
			}
		}

		for (int i = 1; i <= columnNames.size(); i++) {
			ColumnTypeDefinition columnTypeDefinition = columnTypes.get(i);
			if (columnTypeDefinition == null) {
				columnTypeDefinition = new ColumnTypeDefinition();
				columnTypeDefinition.setColumnType(ColumnType.String);
				columnTypes.put(i, columnTypeDefinition);
			}
		}

		List<ResultSetColumn> resultSetColumns = new ArrayList<>();
		List<String> columnLabels = new ArrayList<>();
		for (int i = 1; i <= columnNames.size(); i++) {
			String columnName = columnNames.get(i - 1);
			ColumnTypeDefinition columnTypeDefinition = columnTypes.get(i);
			ColumnType columnType = columnTypeDefinition.getColumnType();

			SqlColumnType resultSetColumnType;
			switch (columnType) {
				case Numeric:
					resultSetColumnType = SqlColumnType.Numeric;
					break;
				case Date:
					resultSetColumnType = SqlColumnType.DateTime;
					break;
				default:
					resultSetColumnType = SqlColumnType.String;
			}

			ResultSetColumn resultSetColumn = new ResultSetColumn();
			resultSetColumn.setName(columnName);
			resultSetColumn.setType(resultSetColumnType);

			String columnLabel = null;
			if (optionsColumnLabels != null) {
				for (Map<String, String> labelDefinition : optionsColumnLabels) {
					Map<String, String> caseInsensitiveMap = new CaseInsensitiveMap<>(labelDefinition);
					columnLabel = caseInsensitiveMap.get(columnName);
					if (columnLabel != null) {
						break;
					}
				}
			}
			if (columnLabel == null) {
				columnLabel = columnName;
			}
			resultSetColumn.setLabel(columnLabel);
			columnLabels.add(columnLabel);

			resultSetColumns.add(resultSetColumn);
		}

		GroovyDataDetails details = new GroovyDataDetails();

		details.setRowCount(rowCount);
		details.setColCount(columnNames.size());
		details.setColumnNames(columnNames);
		details.setDataList(dataList);
		details.setColumnTypes(columnTypes);
		details.setResultSetColumns(resultSetColumns);
		details.setColumnLabels(columnLabels);

		return details;
	}

	/**
	 * Returns the column type definition to use for a column with a certain
	 * column value
	 *
	 * @param columnValue the column value
	 * @return the column type definition to use
	 */
	private static ColumnTypeDefinition getColumnTypeDefinition(Object columnValue) {
		ColumnTypeDefinition columnTypeDefinition = new ColumnTypeDefinition();

		if (columnValue instanceof Number) {
			columnTypeDefinition.setColumnType(ColumnType.Numeric);
		} else if (columnValue instanceof Date) {
			columnTypeDefinition.setColumnType(ColumnType.Date);
		} else {
			columnTypeDefinition.setColumnType(ColumnType.String);
		}

		return columnTypeDefinition;
	}

	/**
	 * Returns data used in report generation as a list of maps
	 *
	 * @param data the data
	 * @return the data as a list of maps
	 * @throws java.lang.Exception
	 */
	public static List<Map<String, ?>> getMapListData(Object data) throws Exception {
		List<Map<String, ?>> finalData = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<? extends Object> dataList = (List<? extends Object>) data;
		if (CollectionUtils.isNotEmpty(dataList)) {
			Object sample = dataList.get(0);
			if (sample instanceof GroovyRowResult) {
				for (Object row : dataList) {
					//https://6by9.wordpress.com/2012/10/13/groovyrowresult-as-a-hashmap/
					GroovyRowResult rowResult = (GroovyRowResult) row;
					Map<String, Object> rowMap = new LinkedHashMap<>();
					for (Object columnName : rowResult.keySet()) {
						rowMap.put(String.valueOf(columnName), rowResult.get(columnName));
					}
					finalData.add(rowMap);
				}
			} else if (sample instanceof DynaBean) {
				for (Object row : dataList) {
					DynaBean rowBean = (DynaBean) row;
					DynaProperty[] columns = rowBean.getDynaClass().getDynaProperties();
					Map<String, Object> rowMap = new LinkedHashMap<>();
					for (DynaProperty column : columns) {
						String columnName = column.getName();
						rowMap.put(columnName, rowBean.get(columnName));
					}
					finalData.add(rowMap);
				}
			} else if (sample instanceof Map) {
				for (Object row : dataList) {
					@SuppressWarnings("unchecked")
					Map<String, ? extends Object> rowMap = (Map<String, ? extends Object>) row;
					finalData.add(rowMap);
				}
			} else {
				for (Object row : dataList) {
					Map<String, Object> rowMap = ArtUtils.objectToMap(row);
					finalData.add(rowMap);
				}
			}
		}

		return finalData;
	}

	/**
	 * Returns only the data component of data used for report generation
	 *
	 * @param data the data used for report generation
	 * @return the data component of the data used for report generation
	 * @throws java.lang.Exception
	 */
	public static List<List<Object>> getListData(Object data) throws Exception {
		List<List<Object>> listData = new ArrayList<>();
		List<Map<String, ?>> mapListData = getMapListData(data);
		for (Map<String, ?> row : mapListData) {
			List<Object> rowData = new ArrayList<>();
			for (Object value : row.values()) {
				rowData.add(value);
			}
			listData.add(rowData);
		}

		return listData;
	}

	/**
	 * Returns the date time value for a given data index
	 *
	 * @param row the object representing a row of data
	 * @param index the one-based index
	 * @param columnNames the column names
	 * @return the date time value for a given data index
	 */
	public static Date getDateTimeRowValue(Object row, int index, List<String> columnNames) {
		Object columnValue = getRowValue(row, index, columnNames);
		Date dateValue = (Date) columnValue;
		return dateValue;
	}

	/**
	 * Returns the date time value for a given data index
	 *
	 * @param row the object representing a row of data
	 * @param index the one-based index
	 * @return the date time value for a given data index
	 */
	public static Date getDateTimeRowValue(Map<Integer, Object> row, int index) {
		Object columnValue = getRowValue(row, index);
		Date dateValue = (Date) columnValue;
		return dateValue;
	}

	/**
	 * Returns the date time value for a given data index
	 *
	 * @param row the object representing a row of data. May be null if indexRow
	 * is used.
	 * @param indexRow the row of data with the column index as the key. May be
	 * null if row is used. If not null, will be used even if row is supplied.
	 * @param index the one-based index
	 * @param columnNames the column names
	 * @return the date time value for a given data index
	 */
	public static Date getDateTimeRowValue(Object row, Map<Integer, Object> indexRow,
			int index, List<String> columnNames) {

		if (indexRow == null) {
			return getDateTimeRowValue(row, index, columnNames);
		} else {
			return getDateTimeRowValue(indexRow, index);
		}
	}

	/**
	 * Returns the date value for a given data index
	 *
	 * @param row the object representing a row of data
	 * @param index the one-based index
	 * @param columnNames the column names
	 * @return the date value for a given data index
	 */
	public static Date getDateRowValue(Object row, int index, List<String> columnNames) {
		Object columnValue = getRowValue(row, index, columnNames);
		Date dateValue = (Date) columnValue;
		Date zeroTimeDate = ArtUtils.zeroTime(dateValue);
		return zeroTimeDate;
	}

	/**
	 * Returns the date value for a given data index
	 *
	 * @param row the object representing a row of data
	 * @param index the one-based index
	 * @return the date value for a given data index
	 */
	public static Date getDateRowValue(Map<Integer, Object> row, int index) {
		Object columnValue = getRowValue(row, index);
		Date dateValue = (Date) columnValue;
		Date zeroTimeDate = ArtUtils.zeroTime(dateValue);
		return zeroTimeDate;
	}

	/**
	 * Returns the date value for a given data index
	 *
	 * @param row the object representing a row of data. May be null if indexRow
	 * is used.
	 * @param indexRow the row of data with the column index as the key. May be
	 * null if row is used. If not null, will be used even if row is supplied.
	 * @param index the one-based index
	 * @param columnNames the column names
	 * @return the date value for a given data index
	 */
	public static Date getDateRowValue(Object row, Map<Integer, Object> indexRow,
			int index, List<String> columnNames) {

		if (indexRow == null) {
			return getDateRowValue(row, index, columnNames);
		} else {
			return getDateRowValue(indexRow, index);
		}
	}

	/**
	 * Returns the string value for a given data index
	 *
	 * @param row the object representing a row of data
	 * @param index the one-based index
	 * @param columnNames the column names
	 * @return the string value for a given data index
	 */
	public static String getStringRowValue(Object row, int index, List<String> columnNames) {
		Object columnValue = getRowValue(row, index, columnNames);
		return String.valueOf(columnValue);
	}

	/**
	 * Returns the string value for a given data index
	 *
	 * @param row the object representing a row of data
	 * @param index the one-based index
	 * @return the string value for a given data index
	 */
	public static String getStringRowValue(Map<Integer, Object> row, int index) {
		Object columnValue = getRowValue(row, index);
		return String.valueOf(columnValue);
	}

	/**
	 * Returns the string value for a given data index
	 *
	 * @param row the object representing a row of data. May be null if indexRow
	 * is used.
	 * @param indexRow the row of data with the column index as the key. May be
	 * null if row is used. If not null, will be used even if row is supplied.
	 * @param index the one-based index
	 * @param columnNames the column names
	 * @return the string value for a given data index
	 */
	public static String getStringRowValue(Object row, Map<Integer, Object> indexRow,
			int index, List<String> columnNames) {

		if (indexRow == null) {
			return getStringRowValue(row, index, columnNames);
		} else {
			return getStringRowValue(indexRow, index);
		}
	}

	/**
	 * Returns the string value for a given data index
	 *
	 * @param row the object representing a row of data
	 * @param columnName the column name
	 * @return the string value for a given data index
	 */
	public static String getStringRowValue(Object row, String columnName) {
		Object columnValue = getRowValue(row, columnName);
		return String.valueOf(columnValue);
	}

	/**
	 * Returns the double value for a given data index
	 *
	 * @param row the object representing a row of data
	 * @param index the one-based index
	 * @param columnNames the column names
	 * @return the double value for a given data index
	 */
	public static double getDoubleRowValue(Object row, int index, List<String> columnNames) {
		Object columnValue = getRowValue(row, index, columnNames);
		double doubleValue;
		if (columnValue == null) {
			doubleValue = 0D;
		} else {
			doubleValue = ((Number) columnValue).doubleValue();
		}

		return doubleValue;
	}

	/**
	 * Returns the double value for a given data index
	 *
	 * @param row the object representing a row of data
	 * @param index the one-based index
	 * @return the double value for a given data index
	 */
	public static double getDoubleRowValue(Map<Integer, Object> row, int index) {
		Object columnValue = getRowValue(row, index);
		double doubleValue;
		if (columnValue == null) {
			doubleValue = 0D;
		} else {
			doubleValue = ((Number) columnValue).doubleValue();
		}

		return doubleValue;
	}

	/**
	 * Returns the double value for a given data index
	 *
	 * @param row the object representing a row of data. May be null if indexRow
	 * is used.
	 * @param indexRow the row of data with the column index as the key. May be
	 * null if row is used. If not null, will be used even if row is supplied.
	 * @param index the one-based index
	 * @param columnNames the column names
	 * @return the double value for a given data index
	 */
	public static double getDoubleRowValue(Object row, Map<Integer, Object> indexRow,
			int index, List<String> columnNames) {

		if (indexRow == null) {
			return getDoubleRowValue(row, index, columnNames);
		} else {
			return getDoubleRowValue(indexRow, index);
		}
	}

	/**
	 * Returns the value for a given data index
	 *
	 * @param row the object representing a row of data
	 * @param index the one-based index
	 * @param columnNames the column names
	 * @return the value for a given data index
	 */
	public static Object getRowValue(Object row, int index, List<String> columnNames) {
		String columnName = columnNames.get(index - 1);
		return getRowValue(row, columnName);
	}

	/**
	 * Returns the value for a given data column
	 *
	 * @param row the object representing a row of data
	 * @param columnName the column name
	 * @return the value for a given data column
	 */
	public static Object getRowValue(Object row, String columnName) {
		Object columnValue;
		if (row instanceof DynaBean) {
			DynaBean rowBean = (DynaBean) row;
			columnValue = rowBean.get(columnName);
		} else if (row instanceof Map) {
			@SuppressWarnings("rawtypes")
			Map rowMap = (Map) row;
			columnValue = rowMap.get(columnName);
		} else {
			throw new IllegalArgumentException("Unexpected row object: " + row.getClass().getCanonicalName());
		}

		return columnValue;
	}

	/**
	 * Returns the value for a given data column
	 *
	 * @param row the object representing a row of data
	 * @param columnIndex the column index
	 * @return the value for a given data column
	 */
	public static Object getRowValue(Map<Integer, Object> row, Integer columnIndex) {
		return row.get(columnIndex);
	}

	/**
	 * Returns form parameters contained in the request as a string that can go
	 * into a url
	 *
	 * @param request the http request
	 * @return request parameters as a string
	 * @throws UnsupportedEncodingException
	 */
	public String getRequestParametersString(HttpServletRequest request)
			throws UnsupportedEncodingException {

		List<String> parametersList = new ArrayList<>();
		Map<String, String[]> requestParameters = request.getParameterMap();
		for (Entry<String, String[]> entry : requestParameters.entrySet()) {
			String paramName = entry.getKey();
			String[] paramValues = entry.getValue();
			String encodedParamName = URLEncoder.encode(paramName, "UTF-8");
			for (String paramValue : paramValues) {
				String encodedParamValue = URLEncoder.encode(paramValue, "UTF-8");
				String paramString = encodedParamName + "=" + encodedParamValue;
				parametersList.add(paramString);
			}
		}

		String parametersString = StringUtils.join(parametersList, "&");

		return parametersString;
	}

	/**
	 * Returns the default report format to use for a given report type
	 *
	 * @param reportType the report type
	 * @return the default report format to use
	 */
	public ReportFormat getDefaultReportFormat(ReportType reportType) {
		Objects.requireNonNull(reportType, "reportType must not be null");

		ReportFormat reportFormat;

		if (reportType.isJasperReports()) {
			reportFormat = ReportFormat.pdf;
		} else if (reportType.isStandardOutput() || reportType.isReportEngine()) {
			reportFormat = ReportFormat.htmlFancy;
		} else if (reportType.isXDocReportPptx()) {
			reportFormat = ReportFormat.pptx;
		} else {
			reportFormat = ReportFormat.html;
		}

		return reportFormat;
	}

	/**
	 * Returns basic self service column information for a report
	 *
	 * @param report the report
	 * @param user the user
	 * @param runId the run id of the report
	 * @return self service columns
	 * @throws SQLException
	 */
	private List<SelfServiceColumn> getSelfServiceColumns(Report report,
			User user, String runId) throws SQLException {

		boolean nameAsLabel = false;
		boolean setType = false;
		return getSelfServiceColumns(report, user, nameAsLabel, setType, runId);
	}

	/**
	 * Returns self service columns for use within the self service reports user
	 * interface
	 *
	 * @param report the report
	 * @param user the current user
	 * @return self service columns
	 * @throws SQLException
	 */
	public List<SelfServiceColumn> getSelfServiceColumnsForView(Report report,
			User user) throws SQLException {

		boolean nameAsLabel = true;
		boolean setType = true;
		return getSelfServiceColumns(report, user, nameAsLabel, setType);
	}

	/**
	 * Returns self service columns based on the report configuration
	 *
	 * @param report the report
	 * @param user the current user
	 * @param nameAsLabel whether name should be used for the label for self
	 * service reports
	 * @param setType whether the type field should be set
	 * @return self service columns
	 * @throws SQLException
	 */
	private List<SelfServiceColumn> getSelfServiceColumns(Report report,
			User user, boolean nameAsLabel, boolean setType) throws SQLException {

		String runId = null;
		return getSelfServiceColumns(report, user, nameAsLabel, setType, runId);
	}

	/**
	 * Returns self service columns based on the report configuration
	 *
	 * @param report the report
	 * @param user the current user
	 * @param nameAsLabel whether name should be used for the label for self
	 * service reports
	 * @param setType whether the type field should be set
	 * @param runId the run id for the report
	 * @return self service columns
	 * @throws SQLException
	 */
	private List<SelfServiceColumn> getSelfServiceColumns(Report report,
			User user, boolean nameAsLabel, boolean setType, String runId) throws SQLException {

		Objects.requireNonNull(report, "report must not be null");

		List<SelfServiceColumn> columns = new ArrayList<>();

		GeneralReportOptions generalOptions = report.getGeneralOptions();
		ViewOptions viewOptions = generalOptions.getView();

		List<String> omitColumns = null;
		List<Map<String, String>> columnLabels = null;
		List<Map<String, String>> columnDescriptions = null;

		if (viewOptions != null) {
			omitColumns = viewOptions.getOmitColumns();
			columnLabels = viewOptions.getColumnLabels();
			columnDescriptions = viewOptions.getColumnDescriptions();
		}

		ReportRunner reportRunner = new ReportRunner();
		ResultSet rs = null;
		try {
			reportRunner.setLimit(ReportRunner.RETURN_ZERO_RECORDS);
			reportRunner.setUseViewColumns(true);
			reportRunner.setUser(user);
			reportRunner.setReport(report);
			reportRunner.setRunId(runId);

			rs = reportRunner.executeQuery();

			if (rs == null) {
				throw new RuntimeException("ResultSet is null");
			}

			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			for (int i = 1; i <= columnCount; i++) {
				SelfServiceColumn column = new SelfServiceColumn();

				column.setName(rsmd.getColumnName(i));

				if (report.isSelfService() && nameAsLabel) {
					column.setLabel(rsmd.getColumnName(i));
				} else {
					column.setLabel(rsmd.getColumnLabel(i));
				}

				if (setType) {
					int sqlType = rsmd.getColumnType(i);

					String type;

					switch (sqlType) {
						case Types.INTEGER:
						case Types.TINYINT:
						case Types.SMALLINT:
						case Types.BIGINT:
							type = "integer";
							break;
						case Types.NUMERIC:
						case Types.DECIMAL:
						case Types.FLOAT:
						case Types.REAL:
						case Types.DOUBLE:
							type = "double";
							break;
						case Types.DATE:
							type = "date";
							break;
						case Types.TIME:
							type = "time";
							break;
						case Types.TIMESTAMP:
							type = "datetime";
							break;
						default:
							type = "string";
					}

					column.setType(type);
				}

				columns.add(column);
			}

			if (omitColumns != null) {
				for (String columnName : omitColumns) {
					//https://stackoverflow.com/questions/10431981/remove-elements-from-collection-while-iterating
					columns.removeIf(column -> StringUtils.equalsIgnoreCase(columnName, column.getLabel()));
				}
			}

			for (SelfServiceColumn column : columns) {
				String label = column.getLabel();
				String userLabel = null;
				if (columnLabels != null) {
					for (Map<String, String> labelDefinition : columnLabels) {
						Map<String, String> caseInsensitiveMap = new CaseInsensitiveMap<>(labelDefinition);
						userLabel = caseInsensitiveMap.get(label);
						if (userLabel != null) {
							break;
						}
					}
				}
				if (userLabel == null) {
					userLabel = label;
				}
				column.setUserLabel(userLabel);

				String description = null;
				if (columnDescriptions != null) {
					for (Map<String, String> descriptionDefinition : columnDescriptions) {
						Map<String, String> caseInsensitiveMap = new CaseInsensitiveMap<>(descriptionDefinition);
						description = caseInsensitiveMap.get(label);
						if (description != null) {
							break;
						}
					}
				}
				if (description == null) {
					description = "";
				}
				column.setDescription(description);
			}
		} finally {
			DatabaseUtils.close(rs);
			reportRunner.close();
		}

		return columns;
	}

	/**
	 * Verifies the selected self service columns and sets the final columns
	 * string to use
	 *
	 * @param report the report
	 * @param user the current user
	 * @throws IOException
	 * @throws SQLException
	 */
	public void applySelfServiceFields(Report report, User user) throws IOException, SQLException {
		String runId = null;
		applySelfServiceFields(report, user, runId);
	}

	/**
	 * Verifies the selected self service columns and sets the final columns
	 * string to use
	 *
	 * @param report the report
	 * @param user the current user
	 * @param runId the run id for the report
	 * @throws IOException
	 * @throws SQLException
	 */
	public void applySelfServiceFields(Report report, User user, String runId)
			throws IOException, SQLException {

		Objects.requireNonNull(report, "report must not be null");

		if (!report.isViewOrSelfService()) {
			return;
		}

		List<SelfServiceColumn> selfServiceColumns = getSelfServiceColumns(report, user, runId);

		SelfServiceOptions selfServiceOptions;
		String selfServiceOptionsString = report.getSelfServiceOptions();
		if (StringUtils.isBlank(selfServiceOptionsString)) {
			selfServiceOptions = new SelfServiceOptions();
		} else {
			selfServiceOptions = ArtUtils.jsonToObjectIgnoreUnknown(selfServiceOptionsString, SelfServiceOptions.class);
		}

		JsonRule javaRule = selfServiceOptions.getJavaRule();
		if (javaRule != null) {
			for (JsonRule rule : javaRule.getRules()) {
				String field = rule.getField();
				boolean found = false;
				for (SelfServiceColumn selfServiceColumn : selfServiceColumns) {
					if (StringUtils.equalsIgnoreCase(field, selfServiceColumn.getLabel())) {
						found = true;
						break;
					}
				}
				if (!found) {
					throw new RuntimeException("Field not found: " + field);
				}
			}
		}

		List<String> chosenColumns = new ArrayList<>();
		List<String> columns = selfServiceOptions.getColumns();
		if (CollectionUtils.isEmpty(columns)) {
			for (SelfServiceColumn referenceColumn : selfServiceColumns) {
				String columnSpecification = getColumnSpecification(referenceColumn);
				chosenColumns.add(columnSpecification);
			}
		} else {
			for (String column : columns) {
				SelfServiceColumn referenceColumn = selfServiceColumns.stream()
						.filter(c -> c.getLabel().equals(column))
						.findAny()
						.orElseThrow(() -> new RuntimeException("Invalid column: " + column));
				String columnSpecification = getColumnSpecification(referenceColumn);
				chosenColumns.add(columnSpecification);
			}
		}

		String columnsString = StringUtils.join(chosenColumns, ", ");

		selfServiceOptions.setColumnsString(columnsString);
		selfServiceOptionsString = ArtUtils.objectToJson(selfServiceOptions);

		report.setSelfServiceOptions(selfServiceOptionsString);
	}

	/**
	 * Returns the column specification to use for a self service column i.e.
	 * column name alone or column name and alias
	 *
	 * @param column the self service column
	 * @return the column specification to use
	 */
	private String getColumnSpecification(SelfServiceColumn column) {
		String columnSpecification;
		if (StringUtils.equals(column.getLabel(), column.getUserLabel())) {
			columnSpecification = column.getLabel();
		} else {
			//https://stackoverflow.com/questions/19657101/what-is-the-difference-between-square-brackets-and-single-quotes-for-aliasing-in
			String cleanUserLabel = StringUtils.remove(column.getUserLabel(), "\"");
			columnSpecification = column.getLabel() + " as \"" + cleanUserLabel + "\"";
		}

		return columnSpecification;
	}

	/**
	 * Sets attributes required for report auto refresh
	 *
	 * @param report the report
	 * @param request the http request
	 */
	public void setRefreshPeriodAttribute(Report report, HttpServletRequest request) {
		request.setAttribute("httpMethod", request.getMethod());

		String refreshPeriodParameter = request.getParameter("refreshPeriodSeconds");
		int refreshPeriod;
		if (refreshPeriodParameter == null) {
			refreshPeriod = report.getGeneralOptions().getRefreshPeriodSeconds();
		} else {
			refreshPeriod = NumberUtils.toInt(refreshPeriodParameter);
		}
		request.setAttribute("refreshPeriodSeconds", refreshPeriod);
	}

	/**
	 * Returns the column names in a given resultset
	 *
	 * @param rs the resultset
	 * @return column names in a given resultset
	 * @throws SQLException
	 */
	public List<String> getColumnNames(ResultSet rs) throws SQLException {
		TemplateResultOptions templateResultOptions = null;
		return getColumnNames(rs, templateResultOptions);

	}

	/**
	 * Returns the column names in a given resultset
	 *
	 * @param rs the resultset
	 * @param templateResultOptions template result options
	 * @return column names in a given resultset
	 * @throws SQLException
	 */
	public List<String> getColumnNames(ResultSet rs,
			TemplateResultOptions templateResultOptions) throws SQLException {

		Objects.requireNonNull(rs, "rs must not be null");

		if (templateResultOptions == null) {
			templateResultOptions = new TemplateResultOptions();
		}

		List<String> columnNames = new ArrayList<>();

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			String columnLabel = rsmd.getColumnLabel(i);
			String columnName = rsmd.getColumnName(i);
			String finalColumnName;
			if (templateResultOptions.isUseColumnLabels()
					&& StringUtils.isNotBlank(columnLabel)) {
				finalColumnName = columnLabel;
			} else {
				finalColumnName = columnName;
			}

			if (templateResultOptions.isUseLowerCaseProperties()) {
				finalColumnName = StringUtils.lowerCase(finalColumnName, Locale.ENGLISH);
			}

			columnNames.add(finalColumnName);
		}

		return columnNames;
	}

}
