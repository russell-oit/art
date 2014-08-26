/**
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
package art.utils;

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.parameter.Parameter;
import art.reportparameter.ReportParameter;
import art.reportparameter.ReportParameterService;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
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
	public Map<String, ReportParameter> processHttpParameters(
			HttpServletRequest request, int reportId) throws SQLException {

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
	public Map<String, ReportParameter> process(Map<String, String[]> passedValuesMap,
			int reportId) throws SQLException {

		logger.debug("Entering processParameters: reportId={}", reportId);

		Map<String, ReportParameter> reportParams = new HashMap<>();

		//get list of all defined report parameters
		ReportParameterService reportParameterService = new ReportParameterService();
		List<ReportParameter> reportParamsList = reportParameterService.getReportParameters(reportId);

		for (ReportParameter reportParam : reportParamsList) {
			//set default parameter values. so that they don't have to be specified on the url
			Parameter param = reportParam.getParameter();
			logger.debug("param={}", param);

			String defaultValue = param.getDefaultValue();
			logger.debug("defaultValue='{}'", defaultValue);

			if (defaultValue != null) {
				String defaultValues[] = defaultValue.split("\\r?\\n");
				reportParam.setPassedParameterValues(defaultValues);
			}

			//build map for easier lookup
			reportParams.put(param.getName(), reportParam);
		}

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

		//set actual values to be used when running the query
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

				//convert string value to appropriate object for use when running sql query
				ParameterDataType paramDataType = param.getDataType();
				if (paramDataType.isNumeric()) {
					if (StringUtils.isBlank(actualValueString)) {
						//use 0 as default value. so that explicit value doesn't have to be set
						//for default value field of parameter. explicitly state in application documentation
						actualValueString = "0";
					}

					switch (paramDataType) {
						case Integer:
							actualValues.add(Long.valueOf(actualValueString));
							break;
						case Number:
							actualValues.add(Double.valueOf(actualValueString));
							break;
						case Datasource:
							actualValues.add(Integer.valueOf(actualValueString));
							break;
						default:
							logger.warn("Unknown numeric parameter data type - {}. Defaulting to integer.", paramDataType);
							actualValues.add(Integer.valueOf(actualValueString));
					}

					reportParam.setActualParameterValues(actualValues);
				} else if (paramDataType.isDate()) {
					Date actualValueDate = processDateValue(actualValueString);

					//must convert java.util.date to appropriate java.sql types as required by jdbc
					//some drivers support using java.util.date in
					//preparedstatement setobject/setdate/settimestamp calls, but not all
					//see https://stackoverflow.com/questions/21162753/jdbc-resultset-i-need-a-getdatetime-but-there-is-only-getdate-and-gettimestamp
					//https://stackoverflow.com/questions/2305973/java-util-date-vs-java-sql-date
					switch (paramDataType) {
						case Date:
							//use java.sql.date which has time portion truncated
							actualValues.add(new java.sql.Date(actualValueDate.getTime()));
							break;
						case DateTime:
							actualValues.add(new java.sql.Timestamp(actualValueDate.getTime()));
							break;
						default:
							logger.warn("Unknown date parameter data type - {}. Defaulting to timestamp.", paramDataType);
							actualValues.add(new java.sql.Timestamp(actualValueDate.getTime()));
					}

					reportParam.setActualParameterValues(actualValues);
				} else {
					//parameter data types that are treated as strings
					actualValues = new ArrayList<>();
					actualValues.add(actualValueString);
					reportParam.setActualParameterValues(actualValues);
				}
			} else if (param.getParameterType() == ParameterType.MultiValue) {
				String actualValueString;
				if (passedValues == null) {
					//parameter value not specified. use default value
					actualValueString = param.getDefaultValue();
				} else {
					actualValueString = passedValues[0];
				}
			}

		}

		return reportParams;
	}

	private Object processDefaultValue(String defaultValue) {

	}

	private Date processDateValue(String defaultValue) {
		/*
		 * if default value has syntax "ADD DAYS|MONTHS|YEARS <integer>" or "Add
		 * day|MoN|Year <integer>" set default value as sysdate plus an offset
		 */

		if (defaultValue == null) {
			defaultValue = "";
		}

		if (defaultValue.toUpperCase().startsWith("ADD")) { // set an offset from today
			Calendar calendar = new GregorianCalendar();
			try {
				StringTokenizer st = new StringTokenizer(defaultValue.toUpperCase(), " ");
				if (st.hasMoreTokens()) {
					st.nextToken(); // skip 1st token
					String token = st.nextToken().trim(); // get 2nd token, i.e. one of DAYS, MONTHS or YEARS
					int field = (token.startsWith("YEAR") ? GregorianCalendar.YEAR : (token.startsWith("MON") ? GregorianCalendar.MONTH : GregorianCalendar.DAY_OF_MONTH));
					token = st.nextToken().trim(); // get last token, i.e. the offset (integer)
					int offset = Integer.parseInt(token);
					calendar.add(field, offset);
				}

				return calendar.getTime();

			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		//convert default date string as it is to a date
		String dateFormat;
		if (defaultValue.length() < 10) {
			dateFormat = "yyyy-M-d";
		} else if (defaultValue.length() == 10) {
			dateFormat = "yyyy-MM-dd";
		} else if (defaultValue.length() == 16) {
			dateFormat = "yyyy-MM-dd HH:mm";
		} else {
			dateFormat = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		dateFormatter.setLenient(false); //don't allow invalid date strings to be coerced into valid dates

		java.util.Date dateValue;
		try {
			dateValue = dateFormatter.parse(defaultValue);
		} catch (ParseException e) {
			logger.debug("Defaulting {} to now", defaultValue, e);
			//string could not be converted to a valid date. default to now
			dateValue = new java.util.Date();
		}

		//return date
		return dateValue;

	}

}
