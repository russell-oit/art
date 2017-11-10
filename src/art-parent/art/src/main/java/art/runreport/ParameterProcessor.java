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

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.parameter.Parameter;
import art.report.ChartOptions;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.reportparameter.ReportParameterService;
import art.user.User;
import art.utils.ArtUtils;
import art.utils.ExpressionHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes report parameters contained in http requests or maps and provides
 * their final values
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ParameterProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ParameterProcessor.class);

	private Locale locale;
	private boolean valuesAsIs;
	private User user;

	public ParameterProcessor() {
		locale = Locale.getDefault();
	}

	/**
	 * @return the valuesAsIs
	 */
	public boolean isValuesAsIs() {
		return valuesAsIs;
	}

	/**
	 * @param valuesAsIs the valuesAsIs to set
	 */
	public void setValuesAsIs(boolean valuesAsIs) {
		this.valuesAsIs = valuesAsIs;
	}

	/**
	 * Processes a http request for running a report and fills objects with
	 * parameter values to be used when running a report
	 *
	 * @param request the http request
	 * @param locale the locale being used
	 * @return final report parameters
	 * @throws java.sql.SQLException
	 * @throws java.text.ParseException
	 * @throws java.io.IOException
	 */
	public ParameterProcessorResult processHttpParameters(
			HttpServletRequest request, Locale locale)
			throws SQLException, ParseException, IOException {

		String reportIdString = request.getParameter("reportId");
		logger.debug("reportIdString='{}'", reportIdString);

		int reportId = Integer.parseInt(reportIdString);

		logger.debug("Entering processParameters: reportId={}", reportId);

		Map<String, String[]> passedValues = new HashMap<>();
		Map<String, String[]> requestParameters = request.getParameterMap();
		passedValues.putAll(requestParameters);

		HttpSession session = request.getSession();
		User sessionUser = (User) session.getAttribute("sessionUser");

		return process(passedValues, reportId, sessionUser, locale);
	}

	/**
	 * Processes parameter value strings and fills objects with parameter values
	 * to be used when running a report
	 *
	 * @param passedValuesMap the parameter values. key is html parameter name
	 * e.g. p-due_date, value is string array with values
	 * @param reportId the report id
	 * @param user the user under whose permission the report is being run
	 * @param locale the locale being used
	 * @return final report parameters
	 * @throws java.sql.SQLException
	 * @throws java.text.ParseException
	 * @throws java.io.IOException
	 */
	public ParameterProcessorResult process(Map<String, String[]> passedValuesMap,
			int reportId, User user, Locale locale) throws SQLException, ParseException, IOException {

		logger.debug("Entering processParameters: reportId={}", reportId);

		this.locale = locale;
		this.user = user;

		Map<String, ReportParameter> reportParamsMap = new HashMap<>();

		//get list of all defined report parameters
		ReportParameterService reportParameterService = new ReportParameterService();
		ReportService reportService = new ReportService();
		Report report = reportService.getReport(reportId);
		List<ReportParameter> reportParamsList;
		if (report.getReportType().isDashboard()) {
			List<Integer> reportIds = report.getDashboardReportIds();
			List<ReportParameter> tempReportParamsList = reportParameterService.getDashboardReportParameters(reportIds);
			//remove duplicates
			//https://stackoverflow.com/questions/203984/how-do-i-remove-repeated-elements-from-arraylist
			Map<String, ReportParameter> cleanMap = new LinkedHashMap<>();
			for (ReportParameter reportParam : tempReportParamsList) {
				cleanMap.put(reportParam.getParameter().getName(), reportParam);
			}
			reportParamsList = new ArrayList<>(cleanMap.values());
		} else {
			reportParamsList = reportParameterService.getEffectiveReportParameters(reportId);
		}

		for (ReportParameter reportParam : reportParamsList) {
			//build map for easier lookup
			reportParamsMap.put(reportParam.getParameter().getName(), reportParam);
		}

		setPassedParameterValues(passedValuesMap, reportParamsMap);

		//set actual values to be used when running the query
		setActualParameterValues(reportParamsList);

		handleAllValues(reportParamsMap);

		setLovValues(reportParamsMap);

		setDefaultValueLovValues(reportParamsMap);

		ParameterProcessorResult result = new ParameterProcessorResult();
		result.setReportParamsList(reportParamsList);
		result.setReportParamsMap(reportParamsMap);

		ReportOptions reportOptions = processReportOptions(passedValuesMap);
		result.setReportOptions(reportOptions);

		ChartOptions chartOptions = processChartOptions(passedValuesMap);
		result.setChartOptions(chartOptions);

		setIsChainedParent(reportParamsList);

		return result;
	}

	/**
	 * Populates lov values for all lov parameters
	 *
	 * @param reportParamsMap the report parameters
	 * @throws SQLException
	 */
	private void setLovValues(Map<String, ReportParameter> reportParamsMap) throws SQLException {
		ReportService reportService = new ReportService();

		for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
			ReportParameter reportParam = entry.getValue();
			Parameter param = reportParam.getParameter();
			if (param.isUseLov()) {
				//get applicable lov values.
				//don't run chained parameters. their values will be
				//loaded dynamically depending on parent and depends paremeter values
				if (!reportParam.isChained()) {
					ReportRunner lovReportRunner = new ReportRunner();
					try {
						int lovReportId = param.getLovReportId();
						Report lovReport = reportService.getReport(lovReportId);
						lovReportRunner.setUser(user);
						lovReportRunner.setReport(lovReport);
						lovReportRunner.setReportParamsMap(reportParamsMap);
						Map<Object, String> lovValues = lovReportRunner.getLovValuesAsObjects();
						reportParam.setLovValues(lovValues);
						Map<String, String> lovValuesAsString = reportParam.convertLovValuesFromObjectToString(lovValues);
						reportParam.setLovValuesAsString(lovValuesAsString);
					} finally {
						lovReportRunner.close();
					}
				}
			}
		}
	}

	/**
	 * Populates default values for parameters which use a default value report
	 *
	 * @param reportParamsMap the report parameters
	 * @param user the user under whose permission the report is being run
	 * @throws SQLException
	 */
	private void setDefaultValueLovValues(Map<String, ReportParameter> reportParamsMap)
			throws SQLException {

		for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
			ReportParameter reportParam = entry.getValue();
			Parameter param = reportParam.getParameter();
			Report defaultValueReport = param.getDefaultValueReport();

			if (defaultValueReport != null) {
				ReportRunner defaultValueLovReportRunner = new ReportRunner();
				try {
					defaultValueLovReportRunner.setUser(user);
					defaultValueLovReportRunner.setReport(defaultValueReport);
					defaultValueLovReportRunner.setReportParamsMap(reportParamsMap);

					Map<Object, String> lovValues = defaultValueLovReportRunner.getLovValuesAsObjects();
					if (reportParam.getPassedParameterValues() == null) {
						reportParam.getActualParameterValues().addAll(lovValues.keySet());
					}

					Map<String, String> lovValuesAsString = reportParam.convertLovValuesFromObjectToString(lovValues);
					reportParam.setDefaultValueLovValues(lovValuesAsString);
				} finally {
					defaultValueLovReportRunner.close();
				}
			}
		}
	}

	/**
	 * Populates the passed values property of the report parameters
	 *
	 * @param passedValuesMap the passed values
	 * @param reportParamsMap the report parameters
	 */
	private void setPassedParameterValues(Map<String, String[]> passedValuesMap,
			Map<String, ReportParameter> reportParamsMap) {

		logger.debug("Entering setPassedParameterValues");

		//process report parameters
		for (Entry<String, String[]> entry : passedValuesMap.entrySet()) {
			String htmlParamName = entry.getKey();
			logger.debug("htmlParamName='{}'", htmlParamName);

			if (StringUtils.startsWithIgnoreCase(htmlParamName, ArtUtils.PARAM_PREFIX)) { //use startswith instead of substring(0,2) because chrome passes a parameter "-" which causes StringIndexOutOfBoundsException. reported by yidong123
				//this is a report parameter. set it's value
				String[] paramValues = entry.getValue();

				String paramName = htmlParamName.substring(2);
				logger.debug("paramName='{}'", paramName);

				ReportParameter reportParam = reportParamsMap.get(paramName);
				logger.debug("reportParam={}", reportParam);

				if (reportParam == null) {
					//report parameter indicated in url but not configured for the report
					//e.g. with dashboard reports where report parameters are passed to all reports
					//do nothing
				} else {
					//check if this is a multi parameter that doesn't use an lov
					//multi param that doesn't use an lov contains values separated by newlines
					Parameter param = reportParam.getParameter();
					logger.debug("param={}", param);

					if (param.getParameterType() == ParameterType.MultiValue
							&& !param.isUseLov() && paramValues != null) {

						String firstValue = paramValues[0];
						logger.debug("firstValue='{}'", firstValue);

						String values[] = firstValue.split("\\r?\\n");
						reportParam.setPassedParameterValues(values);
					} else {
						reportParam.setPassedParameterValues(paramValues);
					}
				}
			}
		}
	}

	/**
	 * Processes final report parameters where "All" is selected for multi-value
	 * parameters
	 *
	 * @param reportParamsMap the report parameters
	 * @throws SQLException
	 * @throws ParseException
	 */
	private void handleAllValues(Map<String, ReportParameter> reportParamsMap)
			throws SQLException, ParseException {

		logger.debug("Entering handleAllValues");

		for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
			ReportParameter reportParam = entry.getValue();
			Parameter param = reportParam.getParameter();

			if (param.getParameterType() == ParameterType.MultiValue) {
				List<Object> actualValuesList = reportParam.getActualParameterValues();

				if (CollectionUtils.isEmpty(actualValuesList)) {
					//get all lov values that apply for the user
					List<Object> actualValues = new ArrayList<>(); //actual values list should not be null
					if (param.isUseLov()) {
						ReportRunner lovReportRunner = new ReportRunner();
						try {
							int lovReportId = param.getLovReportId();
							ReportService reportService = new ReportService();
							Report lovReport = reportService.getReport(lovReportId);
							lovReportRunner.setUser(user);
							lovReportRunner.setReport(lovReport);
							lovReportRunner.setReportParamsMap(reportParamsMap);
							Map<Object, String> lovValues = lovReportRunner.getLovValuesAsObjects();

							for (Entry<Object, String> entry2 : lovValues.entrySet()) {
								Object actualValue = entry2.getKey();
								actualValues.add(actualValue);
							}
						} finally {
							lovReportRunner.close();
						}
					}
					reportParam.setActualParameterValues(actualValues);
				}
			}
		}
	}

	/**
	 * Populates actual parameter values to be used for the report parameters
	 *
	 * @param reportParamsList the report parameters
	 * @throws NumberFormatException
	 * @throws ParseException
	 */
	private void setActualParameterValues(List<ReportParameter> reportParamsList)
			throws NumberFormatException, ParseException, IOException {

		logger.debug("Entering setActualParameterValues");

		for (ReportParameter reportParam : reportParamsList) {
			Parameter param = reportParam.getParameter();
			logger.debug("param={}", param);

			String[] passedValues = reportParam.getPassedParameterValues();

			List<Object> actualValues = new ArrayList<>(); //actual values list should not be null

			if (param.getParameterType() == ParameterType.SingleValue) {
				String actualValueString;
				if (passedValues == null) {
					//parameter value not specified. use default value
					actualValueString = param.getLocalizedDefaultValue(locale);
				} else {
					actualValueString = passedValues[0];
				}

				//convert string value to appropriate object
				Object actualValue = convertParameterStringValueToObject(actualValueString, param);
				actualValues.add(actualValue);
				reportParam.setActualParameterValues(actualValues);
			} else if (param.getParameterType() == ParameterType.MultiValue) {
				List<String> actualValueStrings = new ArrayList<>();
				if (passedValues == null) {
					//parameter value not specified. use default value
					String defaultValue = param.getLocalizedDefaultValue(locale);
					if (StringUtils.isNotEmpty(defaultValue)) {
						String defaultValues[] = defaultValue.split("\\r?\\n");
						actualValueStrings.addAll(Arrays.asList(defaultValues));
					}
				} else {
					actualValueStrings.addAll(Arrays.asList(passedValues));
				}

				if (actualValueStrings.isEmpty() || actualValueStrings.contains("ALL_ITEMS")) {
					//do nothing. all possible values will be added later
					//so that parameter substitution is available
				} else {
					for (String actualValueString : actualValueStrings) {
						//convert string value to appropriate object
						Object actualValue = convertParameterStringValueToObject(actualValueString, param);
						actualValues.add(actualValue);
					}
					reportParam.setActualParameterValues(actualValues);
				}

			}

		}
	}

	/**
	 * Converts a string parameter value to an object of the appropriate type,
	 * depending on the parameter data type
	 *
	 * @param value the string parameter value
	 * @param param the parameter object
	 * @return an object of the appropriate type
	 * @throws ParseException
	 */
	private Object convertParameterStringValueToObject(String value, Parameter param)
			throws ParseException {

		logger.debug("Entering convertParameterStringValueToObject: value='{}'", value);

		if (valuesAsIs) {
			return value;
		}

		ParameterDataType paramDataType = param.getDataType();

		String username = null;
		if (user != null) {
			username = user.getUsername();
		}
		
		ExpressionHelper expressionHelper = new ExpressionHelper();
		value = expressionHelper.processString(value, username);

		if (paramDataType.isNumeric()) {
			return convertParameterStringValueToNumber(value, param);
		} else if (paramDataType.isDate()) {
			return convertParameterStringValueToDate(value, param);
		} else {
			//parameter data types that are treated as strings
			return value;
		}
	}

	/**
	 * Converts a string parameter value to an appropriate numeric object,
	 * depending on the parameter data type
	 *
	 * @param value the string parameter value
	 * @param param the parameter object
	 * @return an appropriate numeric object e.g. Integer, Double
	 */
	private Object convertParameterStringValueToNumber(String value, Parameter param) {
		logger.debug("Entering convertParameterStringValueToNumber: value='{}'", value);

		String finalValue;
		if (StringUtils.isBlank(value)) {
			finalValue = "0";
		} else {
			finalValue = value;
		}

		ParameterDataType paramDataType = param.getDataType();

		switch (paramDataType) {
			case Integer:
				//use Double.valueOf() for cases where something like 15.0 is passed (e.g. with drilldown from chart)
				//Integer.valueOf() would throw an exception in such a case
				//intValue() merely returns the integer part; it does not do any rounding
				//https://stackoverflow.com/questions/9102318/cast-double-to-integer-in-java
				//return Double.valueOf(usedValue).intValue();

				//use BigDecimal to determine if the fraction part is non-zero and raise an exception
				//instead of silently using the integer part only
				//https://stackoverflow.com/questions/6063253/what-is-the-best-way-to-separate-double-into-two-parts-integer-fraction-in-j
				//https://stackoverflow.com/questions/10950914/how-to-check-if-bigdecimal-variable-0-in-java
				BigDecimal bd = new BigDecimal(finalValue);
				BigDecimal fractionPart = bd.subtract(new BigDecimal(bd.intValue()));
				if (fractionPart.compareTo(BigDecimal.ZERO) == 0) {
					return bd.intValue();
				} else {
					String paramName = param.getName();
					throw new IllegalArgumentException("Invalid integer value for parameter " + paramName + ": " + finalValue);
				}
			case Double:
				return Double.valueOf(finalValue);
			default:
				throw new IllegalArgumentException("Unknown numeric parameter data type: " + paramDataType);
		}
	}

	/**
	 * Converts a string parameter value to a date object
	 *
	 * @param value the string parameter value
	 * @param param the parameter object for the value
	 * @return a date object
	 * @throws ParseException
	 */
	private Date convertParameterStringValueToDate(String value, Parameter param) throws ParseException {
		return convertParameterStringValueToDate(value, param.getDateFormat());
	}

	/**
	 * Converts a string parameter value to a date object
	 *
	 * @param value the string parameter value
	 * @return a date object
	 * @throws ParseException
	 */
	public Date convertParameterStringValueToDate(String value) throws ParseException {
		String dateFormat = null;
		return convertParameterStringValueToDate(value, dateFormat);
	}

	/**
	 * Converts a string parameter value to a date object
	 *
	 * @param value the string parameter value
	 * @param dateFormat the date format that the value is in
	 * @return a date object
	 * @throws ParseException
	 */
	private Date convertParameterStringValueToDate(String value, String dateFormat) throws ParseException {
		logger.debug("Entering convertParameterStringValueToDate: value='{}',"
				+ " dateFormat='{}'", value, dateFormat);

		ExpressionHelper expressionHelper = new ExpressionHelper();
		Date dateValue = expressionHelper.convertStringToDate(value, dateFormat, locale);
		return dateValue;
	}

	/**
	 * Processes report options in a given set of parameters
	 *
	 * @param passedValuesMap the parameters that may contain some report
	 * options
	 * @return final report option values to use when running a report
	 */
	private ReportOptions processReportOptions(Map<String, String[]> passedValuesMap) {
		logger.debug("Entering processReportOptions");

		ReportOptions reportOptions = new ReportOptions();

		for (Entry<String, String[]> entry : passedValuesMap.entrySet()) {
			String htmlParamName = entry.getKey();
			String[] paramValues = entry.getValue();
			logger.debug("htmlParamName='{}'", htmlParamName);

			if (paramValues != null) {
				String paramValue = paramValues[0];

				if (StringUtils.equalsIgnoreCase(htmlParamName, "showSelectedParameters")) {
					reportOptions.setShowSelectedParameters(true);
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "splitColumn")) {
					reportOptions.setSplitColumn(Integer.parseInt(paramValue));
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "showSql")) {
					reportOptions.setShowSql(true);
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "prettyPrint")) {
					reportOptions.setPrettyPrint(true);
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "swapAxes")) {
					reportOptions.setSwapAxes(true);
				}

			}
		}

		return reportOptions;
	}

	/**
	 * Processes chart options in a given set of parameters
	 *
	 * @param passedValuesMap the parameters that may contain some chart options
	 * @return final chart options to use when running a report
	 */
	private ChartOptions processChartOptions(Map<String, String[]> passedValuesMap) {
		logger.debug("Entering processChartOptions");

		ChartOptions chartOptions = new ChartOptions();

		for (Entry<String, String[]> entry : passedValuesMap.entrySet()) {
			String htmlParamName = entry.getKey();
			String[] paramValues = entry.getValue();
			logger.debug("htmlParamName='{}'", htmlParamName);

			if (paramValues != null) {
				String paramValue = paramValues[0];

				if (StringUtils.equalsIgnoreCase(htmlParamName, "showLegend")) {
					chartOptions.setShowLegend(true);
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "showLabels")) {
					chartOptions.setShowLabels(true);
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "showData")) {
					chartOptions.setShowData(true);
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "showPoints")) {
					chartOptions.setShowPoints(true);
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "rotateAt")) {
					if (StringUtils.isNotBlank(paramValue)) {
						chartOptions.setRotateAt(Integer.parseInt(paramValue));
					}
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "removeAt")) {
					if (StringUtils.isNotBlank(paramValue)) {
						chartOptions.setRemoveAt(Integer.parseInt(paramValue));
					}
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "chartWidth")) {
					if (StringUtils.isNotBlank(paramValue)) {
						chartOptions.setWidth(Integer.valueOf(paramValue));
					}
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "chartHeight")) {
					if (StringUtils.isNotBlank(paramValue)) {
						chartOptions.setHeight(Integer.valueOf(paramValue));
					}
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "yAxisMin")) {
					if (StringUtils.isNotBlank(paramValue)) {
						chartOptions.setyAxisMin(Double.parseDouble(paramValue));
					}
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "yAxisMax")) {
					if (StringUtils.isNotBlank(paramValue)) {
						chartOptions.setyAxisMax(Double.parseDouble(paramValue));
					}
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "backgroundColor")) {
					chartOptions.setBackgroundColor(paramValue);
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "labelFormat")) {
					chartOptions.setBackgroundColor(paramValue);
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "_graphOptions")) {
					//handle legacy parameter name in art_jobs_parameters table
					chartOptions.setChartOptionsFromString(paramValue);
				}
			}
		}

		return chartOptions;
	}

	/**
	 * Populates the chained parent property for report parameters
	 *
	 * @param reportParamsList the report parameters
	 */
	private void setIsChainedParent(List<ReportParameter> reportParamsList) {
		StringBuilder allchainedParentsSb = new StringBuilder();

		for (ReportParameter reportParam : reportParamsList) {
			String chainedParents = reportParam.getChainedParents();
			if (StringUtils.isNotBlank(chainedParents)) {
				allchainedParentsSb.append(chainedParents);
			}
		}

		String allChainedParents = allchainedParentsSb.toString();
		for (ReportParameter reportParam : reportParamsList) {
			String paramName = reportParam.getParameter().getName();
			if (allChainedParents.contains(paramName)) {
				reportParam.setChainedParent(true);
			}
		}
	}
}
