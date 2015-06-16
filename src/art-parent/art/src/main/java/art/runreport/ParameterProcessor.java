/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
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

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.parameter.Parameter;
import art.report.ChartOptions;
import art.reportparameter.ReportParameter;
import art.reportparameter.ReportParameterService;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes report parameters contained in a http request and populates maps with
 * their values
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ParameterProcessor {

	private static final Logger logger = LoggerFactory.getLogger(ParameterProcessor.class);

	/**
	 * Process a http request for running a report and fill objects with
	 * parameter values to be used when running a report
	 *
	 * @param request http request
	 * @param reportId report id
	 * @return map with report parameters. key is parameter name, value is
	 * populated report parameter object
	 * @throws java.sql.SQLException
	 */
	public ParameterProcessorResult processHttpParameters(
			HttpServletRequest request, int reportId) throws SQLException, ParseException {

		logger.debug("Entering processParameters: reportId={}", reportId);

		Map<String, String[]> passedValues = new HashMap<>();

		Enumeration<String> htmlParamNames = request.getParameterNames();
		while (htmlParamNames.hasMoreElements()) {
			String htmlParamName = htmlParamNames.nextElement();
			logger.debug("htmlParamName='{}'", htmlParamName);

			passedValues.put(htmlParamName, request.getParameterValues(htmlParamName));
		}

		return process(passedValues, reportId);
	}

	/**
	 * Process parameter value strings and fill objects with parameter values to
	 * be used when running a report
	 *
	 * @param passedValuesMap map passed parameter values. key is html parameter
	 * name e.g. p-due_date, value is string array with values
	 * @param reportId report id
	 * @return map with report parameters. key is parameter name e.g. due_date,
	 * value is populated report parameter object
	 * @throws java.sql.SQLException
	 */
	public ParameterProcessorResult process(Map<String, String[]> passedValuesMap,
			int reportId) throws SQLException, ParseException {

		logger.debug("Entering processParameters: reportId={}", reportId);

		Map<String, ReportParameter> reportParamsMap = new HashMap<>();

		//get list of all defined report parameters
		ReportParameterService reportParameterService = new ReportParameterService();
		List<ReportParameter> reportParamsList = reportParameterService.getReportParameters(reportId);

		for (ReportParameter reportParam : reportParamsList) {
			//build map for easier lookup
			reportParamsMap.put(reportParam.getParameter().getName(), reportParam);
		}

		setPassedParameterValues(passedValuesMap, reportParamsMap);

		//set actual values to be used when running the query
		setActualParameterValues(reportParamsList);

		ParameterProcessorResult result = new ParameterProcessorResult();

		result.setReportParamsList(reportParamsList);
		result.setReportParamsMap(reportParamsMap);

		ReportOptions reportOptions = processReportOptions(passedValuesMap);
		result.setReportOptions(reportOptions);

		ChartOptions chartOptions = processChartOptions(passedValuesMap);
		result.setChartOptions(chartOptions);

		return result;
	}

	private void setPassedParameterValues(Map<String, String[]> passedValuesMap, Map<String, ReportParameter> reportParams) {
		//process report parameters
		for (Entry<String, String[]> entry : passedValuesMap.entrySet()) {
			String htmlParamName = entry.getKey();
			logger.debug("htmlParamName='{}'", htmlParamName);

			if (htmlParamName.startsWith("p-")) { //use startswith instead of substring(0,2) because chrome passes a parameter "-" which causes StringIndexOutOfBoundsException. reported by yidong123
				//this is a report parameter. set it's value
				String[] paramValues = entry.getValue();

				String paramName = htmlParamName.substring(2);
				logger.debug("paramName='{}'", paramName);

				ReportParameter reportParam = reportParams.get(paramName);
				logger.debug("reportParam={}", reportParam);

				if (reportParam != null) {
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

	private void setActualParameterValues(List<ReportParameter> reportParamsList) throws NumberFormatException, ParseException {
		for (ReportParameter reportParam : reportParamsList) {
			Parameter param = reportParam.getParameter();
			logger.debug("param={}", param);

			String[] passedValues = reportParam.getPassedParameterValues();

			List<Object> actualValues = new ArrayList<>(); //actual values list should not be null

			if (param.getParameterType() == ParameterType.SingleValue) {
				String actualValueString;
				if (passedValues == null) {
					//parameter value not specified. use default value
					actualValueString = param.getDefaultValue();
				} else {
					actualValueString = passedValues[0];
				}

				//convert string value to appropriate object
				Object actualValue = convertParameterValue(actualValueString, param.getDataType());
				actualValues.add(actualValue);
				reportParam.setActualParameterValues(actualValues);
			} else if (param.getParameterType() == ParameterType.MultiValue) {
				List<String> actualValueStrings = new ArrayList<>();
				if (passedValues == null) {
					//parameter value not specified. use default value
					String defaultValue = param.getDefaultValue();
					if (defaultValue != null) {
						String defaultValues[] = defaultValue.split("\\r?\\n");
						actualValueStrings.addAll(Arrays.asList(defaultValues));
					}
				} else {
					actualValueStrings.addAll(Arrays.asList(passedValues));
				}

				if (actualValueStrings.isEmpty() || actualValueStrings.contains("ALL_ITEMS")) {
					//TODO get all values
				} else {
					for (String actualValueString : actualValueStrings) {
						//convert string value to appropriate object
						Object actualValue = convertParameterValue(actualValueString, param.getDataType());
						actualValues.add(actualValue);
					}
					reportParam.setActualParameterValues(actualValues);
				}

			}

		}
	}

	public Object convertParameterValue(String value, ParameterDataType paramDataType) throws ParseException {
		if (paramDataType.isNumeric()) {
			return convertParameterValueToNumber(value, paramDataType);
		} else if (paramDataType.isDate()) {
			return convertParameterValueToDate(value);
		} else {
			//parameter data types that are treated as strings
			return value;
		}
	}

	private Object convertParameterValueToNumber(String value, ParameterDataType paramDataType) {
		String usedValue;
		if (StringUtils.isBlank(value)) {
			usedValue = "0";
		} else {
			usedValue = value;
		}

		switch (paramDataType) {
			case Integer:
			case Datasource:
				//use Double.valueOf() for cases where something like 15.0 is passed (e.g. with drilldown from chart)
				//Integer.valueOf() would throw an exception is such a case
				//intValue() merely returns the integer part; it does not do any rounding
				//https://stackoverflow.com/questions/9102318/cast-double-to-integer-in-java
				return Double.valueOf(usedValue).intValue();
			case Number:
				return Double.valueOf(usedValue);
			default:
				throw new IllegalArgumentException("Unknown numeric parameter data type: " + paramDataType);
		}
	}

	private Date convertParameterValueToDate(String value) throws ParseException {
		Date dateValue;

		if (StringUtils.startsWithIgnoreCase(value, "add")) {
			//e.g. add days 1
			String[] tokens = StringUtils.split(value);
			if (tokens.length != 3) {
				throw new IllegalArgumentException("Invalid interval: " + value);
			}

			String period = tokens[1];
			int offset = Integer.parseInt(tokens[2]);
			Date now = new Date();

			if (StringUtils.startsWithIgnoreCase(period, "day")) {
				dateValue = DateUtils.addDays(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "month")) {
				dateValue = DateUtils.addMonths(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "year")) {
				dateValue = DateUtils.addYears(now, offset);
			} else {
				throw new IllegalArgumentException("Invalid period: " + period);
			}
		} else if (StringUtils.equalsIgnoreCase(value, "now")) {
			dateValue = new Date();
		} else {
			//convert date string as it is to a date
			String DATE_STRING = "yyyy-MM-dd";
			String DATE_TIME_STRING = "yyyy-MM-dd HH:mm";
			String DATE_TIME_SECONDS_STRING = "yyyy-MM-dd HH:mm";
			String dateFormat;
			if (value.length() == DATE_STRING.length()) {
				dateFormat = DATE_STRING;
			} else if (value.length() == DATE_TIME_STRING.length()) {
				dateFormat = DATE_TIME_STRING;
			} else if (value.length() == DATE_TIME_SECONDS_STRING.length()) {
				dateFormat = DATE_TIME_SECONDS_STRING;
			} else {
				throw new IllegalArgumentException("Invalid date format: " + value);
			}

			SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
			dateFormatter.setLenient(false); //don't allow invalid date strings to be coerced into valid dates
			dateValue = dateFormatter.parse(value);
		}

		return dateValue;
	}

	private ReportOptions processReportOptions(Map<String, String[]> passedValuesMap) {
		ReportOptions reportOptions = new ReportOptions();

		for (Entry<String, String[]> entry : passedValuesMap.entrySet()) {
			String htmlParamName = entry.getKey();
			String[] paramValues = entry.getValue();
			logger.debug("htmlParamName='{}'", htmlParamName);

			if (paramValues != null) {
				String paramValue = paramValues[0];

				if (StringUtils.equalsIgnoreCase(htmlParamName, "showSelectedParameters")) {
					reportOptions.setShowSelectedParameters(Boolean.valueOf(paramValue));
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "splitColumn")) {
					reportOptions.setSplitColumn(Integer.parseInt(paramValue));
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "showSql")) {
					reportOptions.setShowSql(Boolean.valueOf(paramValue));
				}

				//TODO process other params
			}
		}

		return reportOptions;
	}

	private ChartOptions processChartOptions(Map<String, String[]> passedValuesMap) {
		ChartOptions chartOptions = new ChartOptions();

		for (Entry<String, String[]> entry : passedValuesMap.entrySet()) {
			String htmlParamName = entry.getKey();
			String[] paramValues = entry.getValue();
			logger.debug("htmlParamName='{}'", htmlParamName);

			if (paramValues != null) {
				String paramValue = paramValues[0];

				if (StringUtils.equalsIgnoreCase(htmlParamName, "showLegend")) {
					chartOptions.setShowLegend(Boolean.valueOf(paramValue));
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "showData")) {
					chartOptions.setShowData(Boolean.valueOf(paramValue));
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "showPoints")) {
					chartOptions.setShowPoints(Boolean.valueOf(paramValue));
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "rotateAt")) {
					chartOptions.setRotateAt(Integer.parseInt(paramValue));
				} else if (StringUtils.equalsIgnoreCase(htmlParamName, "removeAt")) {
					chartOptions.setRemoveAt(Integer.parseInt(paramValue));
				}

				//TODO process other params
			}
		}

		return chartOptions;
	}

}
