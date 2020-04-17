/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.enums.DatasourceType;
import art.enums.DateFieldType;
import art.enums.ParameterDataType;
import art.report.Report;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import com.mongodb.MongoClient;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.groovy.sandbox.SandboxTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

/**
 * Processes strings that may contain parameter, field or groovy expressions
 *
 * @author Timothy Anyona
 */
public class ExpressionHelper {

	private static final Logger logger = LoggerFactory.getLogger(ExpressionHelper.class);

	public static final String GROOVY_START_STRING = "g[";
	public final String GROOVY_END_STRING = "]g";
	public final String FIELD_START_STRING = "f[";
	public final String FIELD_END_STRING = "]f";

	/**
	 * Processes a string that may have parameter or field expressions and
	 * returns the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	public String processString(String string) throws ParseException {
		String username = null;
		return processString(string, username);
	}

	/**
	 * Processes a string that may have parameter or field expressions and
	 * returns the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param username the username to replace
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	public String processString(String string, String username) throws ParseException {
		Map<String, ReportParameter> reportParamsMap = null;
		return processString(string, reportParamsMap, username);
	}

	/**
	 * Processes a string that may have parameter or field expressions and
	 * returns the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param reportParamsMap a map containing report parameters
	 * @param username the username to replace
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	public String processString(String string, Map<String, ReportParameter> reportParamsMap,
			String username) throws ParseException {

		Map<String, String> recipientColumns = null;
		return processString(string, reportParamsMap, username, recipientColumns);
	}

	/**
	 * Processes a string that may have parameter or field expressions and
	 * returns the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param reportParamsMap a map containing report parameters
	 * @param username the username to replace
	 * @param recipientColumns dynamic recipient details
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	public String processString(String string, Map<String, ReportParameter> reportParamsMap,
			String username, Map<String, String> recipientColumns) throws ParseException {

		String finalString = string;
		finalString = processParameters(finalString, reportParamsMap);
		finalString = processDynamicRecipients(finalString, recipientColumns);
		finalString = processFields(finalString, username);
		finalString = processGroovy(finalString, reportParamsMap);
		return finalString;
	}

	/**
	 * Processes a string that may have parameter expressions and returns the
	 * processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param reportParamsMap a map containing report parameters
	 * @return the processed value with these items replaced
	 */
	public String processParameters(String string, Map<String, ReportParameter> reportParamsMap) {
		String finalString = string;

		if (StringUtils.isNotBlank(string) && MapUtils.isNotEmpty(reportParamsMap)) {
			for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
				String paramName = entry.getKey();
				ReportParameter reportParam = entry.getValue();

				List<Object> actualParameterValues = reportParam.getActualParameterValues();

				String replaceString;
				if (CollectionUtils.isEmpty(actualParameterValues)) {
					replaceString = "";
				} else {
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

					replaceString = StringUtils.join(paramValues, ",");
				}

				String paramIdentifier = "#" + paramName + "#";
				finalString = StringUtils.replaceIgnoreCase(finalString, paramIdentifier, replaceString);
			}
		}

		return finalString;
	}

	/**
	 * Processes a string that may have dynamic recipient column expressions and
	 * returns the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param recipientColumns the recipient details
	 * @return the processed value with these items replaced
	 */
	public String processDynamicRecipients(String string, Map<String, String> recipientColumns) {
		String finalString = string;

		if (StringUtils.isNotBlank(string) && MapUtils.isNotEmpty(recipientColumns)) {
			for (Entry<String, String> entry : recipientColumns.entrySet()) {
				String columnName = entry.getKey();
				String columnValue = entry.getValue();

				String columnIdentifier = "#" + columnName + "#";
				finalString = StringUtils.replaceIgnoreCase(finalString, columnIdentifier, columnValue);
			}
		}

		return finalString;
	}

	/**
	 * Processes a string that may have field expressions and returns the
	 * processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param username the username to replace
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	public String processFields(String string, String username) throws ParseException {
		String finalString = string;
		finalString = processUsername(finalString, username);
		finalString = processDates(finalString);
		return finalString;
	}

	/**
	 * Processes a string that may have username field expressions and returns
	 * the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param username the username to replace
	 * @return the processed value with these items replaced
	 */
	public String processUsername(String string, String username) {
		String replaceString;
		if (StringUtils.isBlank(username)) {
			replaceString = "";
		} else {
			replaceString = username;
		}

		String fieldName = FIELD_START_STRING + "username" + FIELD_END_STRING;
		String finalString = StringUtils.replace(string, fieldName, replaceString);
		return finalString;
	}

	/**
	 * Processes a string that may have date field expressions and returns the
	 * processed value with these items replaced
	 *
	 * @param string the string to process
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	public String processDates(String string) throws ParseException {
		String finalString = string;
		//process datetime field before date field
		finalString = processDate(finalString, DateFieldType.DateTime);
		finalString = processDate(finalString, DateFieldType.Date);
		return finalString;
	}

	/**
	 * Processes a string that may have date field expressions and returns the
	 * processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param dateFieldType the type of date field to process
	 * @return the processed value with these items replaced
	 * @throws ParseException
	 */
	private String processDate(String string, DateFieldType dateFieldType) throws ParseException {
		String finalString = string;

		String dateFieldStartString;
		switch (dateFieldType) {
			case Date:
				dateFieldStartString = FIELD_START_STRING + "date";
				break;
			case DateTime:
				dateFieldStartString = FIELD_START_STRING + "datetime";
				break;
			default:
				throw new IllegalArgumentException("Unexpected date field type: " + dateFieldType);
		}

		String[] dateFields = StringUtils.substringsBetween(string, dateFieldStartString, FIELD_END_STRING);
		if (dateFields != null) {
			Map<String, String> dateFieldValues = new HashMap<>();
			for (String dateField : dateFields) {
				String dateValue = processDateFieldContents(dateField, dateFieldType);
				String dateSpecification = dateFieldStartString + dateField + FIELD_END_STRING;
				dateFieldValues.put(dateSpecification, dateValue);
			}

			for (Entry<String, String> entry : dateFieldValues.entrySet()) {
				String searchString = entry.getKey();
				String replaceString = entry.getValue();
				finalString = StringUtils.replace(finalString, searchString, replaceString);
			}
		}

		return finalString;
	}

	/**
	 * Processes the contents of a date field and returns the processed value
	 * which will be used to replace the date field definition
	 *
	 * @param dateField the contents of the date field definition
	 * @param dateFieldType the type of date field being processed
	 * @return returns the processed value
	 * @throws ParseException
	 */
	private String processDateFieldContents(String dateField, DateFieldType dateFieldType) throws ParseException {
		String result = dateField;

		if (StringUtils.isBlank(result)) {
			switch (dateFieldType) {
				case Date:
					result = ArtUtils.isoDateFormatter.format(new Date());
					break;
				case DateTime:
					result = ArtUtils.isoDateTimeMillisecondsFormatter.format(new Date());
					break;
				default:
					throw new IllegalArgumentException("Unexpected date field type: " + dateFieldType);
			}
		} else {
			String separator = result.substring(0, 1);
			String expression = result.substring(1);
			String[] components = StringUtils.splitPreserveAllTokens(expression, separator);
			String dateString = components[0].trim();
			String outputFormat;
			switch (dateFieldType) {
				case Date:
					outputFormat = ArtUtils.ISO_DATE_FORMAT;
					break;
				case DateTime:
					outputFormat = ArtUtils.ISO_DATE_TIME_MILLISECONDS_FORMAT;
					break;
				default:
					throw new IllegalArgumentException("Unexpected date field type: " + dateFieldType);
			}
			if (components.length > 1) {
				outputFormat = components[1];
			}
			String outputLocaleString = null;
			if (components.length > 2) {
				outputLocaleString = components[2].trim();
			}
			Locale outputLocale = ArtUtils.getLocaleFromString(outputLocaleString);
			String inputFormat = null;
			if (components.length > 3) {
				inputFormat = components[3].trim();
			}
			String inputLocaleString = null;
			if (components.length > 4) {
				inputLocaleString = components[4].trim();
			}
			Locale inputLocale = ArtUtils.getLocaleFromString(inputLocaleString);

			Date dateValue = convertStringToDate(dateString, inputFormat, inputLocale);
			SimpleDateFormat dateFormatter = new SimpleDateFormat(outputFormat, outputLocale);
			result = dateFormatter.format(dateValue);
		}

		return result;
	}

	/**
	 * Converts a string representation of a date to a date object
	 *
	 * @param string the string
	 * @return the date object of the string representation
	 * @throws ParseException
	 */
	public Date convertStringToDate(String string) throws ParseException {
		String dateFormat = null;
		Locale locale = Locale.ENGLISH;
		return convertStringToDate(string, dateFormat, locale);
	}

	/**
	 * Converts a string representation of a date to a date object
	 *
	 * @param string the string
	 * @param dateFormat the date format that the string is in
	 * @param locale the locale to use for the date
	 * @return the date object of the string representation
	 * @throws ParseException
	 */
	public Date convertStringToDate(String string, String dateFormat,
			Locale locale) throws ParseException {

		logger.debug("Entering convertStringToDate: string='{}',"
				+ " dateFormat='{}', locale={}", string, dateFormat, locale);

		if (string == null) {
			return null;
		}

		Date dateValue;
		Date now = new Date();

		String trimString = StringUtils.trimToEmpty(string);

		if (StringUtils.equalsIgnoreCase(trimString, "now")
				|| StringUtils.isBlank(string)) {
			dateValue = now;
		} else if (StringUtils.equalsIgnoreCase(trimString, "today")) {
			dateValue = ArtUtils.zeroTime(now);
		} else if (StringUtils.startsWithIgnoreCase(trimString, "add")) {
			//e.g. add days 1
			String[] tokens = StringUtils.split(trimString); //splits by space
			if (tokens.length != 3) {
				throw new IllegalArgumentException("Invalid interval: " + trimString);
			}

			String period = tokens[1];
			int offset = Integer.parseInt(tokens[2]);

			if (StringUtils.startsWithIgnoreCase(period, "day")) {
				dateValue = DateUtils.addDays(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "week")) {
				dateValue = DateUtils.addWeeks(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "month")) {
				dateValue = DateUtils.addMonths(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "year")) {
				dateValue = DateUtils.addYears(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "hour")) {
				dateValue = DateUtils.addHours(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "min")) {
				dateValue = DateUtils.addMinutes(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "sec")) {
				dateValue = DateUtils.addSeconds(now, offset);
			} else if (StringUtils.startsWithIgnoreCase(period, "milli")) {
				dateValue = DateUtils.addMilliseconds(now, offset);
			} else {
				throw new IllegalArgumentException("Invalid period: " + period);
			}
		} else {
			//convert date string as it is to a date
			if (StringUtils.isBlank(dateFormat)) {
				if (string.length() == ArtUtils.ISO_DATE_FORMAT.length()) {
					dateFormat = ArtUtils.ISO_DATE_FORMAT;
				} else if (string.length() == ArtUtils.ISO_DATE_TIME_FORMAT.length()) {
					dateFormat = ArtUtils.ISO_DATE_TIME_FORMAT;
				} else if (string.length() == ArtUtils.ISO_DATE_TIME_SECONDS_FORMAT.length()) {
					dateFormat = ArtUtils.ISO_DATE_TIME_SECONDS_FORMAT;
				} else if (string.length() == ArtUtils.ISO_DATE_TIME_MILLISECONDS_FORMAT.length()) {
					dateFormat = ArtUtils.ISO_DATE_TIME_MILLISECONDS_FORMAT;
				} else {
					throw new IllegalArgumentException("Unexpected date format: " + string);
				}
			}

			if (locale == null) {
				locale = Locale.getDefault();
			}

			//not all locales work with simpledateformat
			//with lenient set to false, parsing may throw an error if the locale is not available
			if (logger.isDebugEnabled()) {
				Locale[] locales = SimpleDateFormat.getAvailableLocales();
				if (!Arrays.asList(locales).contains(locale)) {
					logger.debug("Locale '{}' not available for date parameter parsing", locale);
				}
			}

			SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat, locale);
			dateFormatter.setLenient(false); //don't allow invalid date strings to be coerced into valid dates
			dateValue = dateFormatter.parse(string);
		}

		return dateValue;
	}

	/**
	 * Converts a date representation that includes simple arithmetic like "add
	 * days 1" to a proper date string
	 *
	 * @param dateString the date representation
	 * @param format the format of the output date string
	 * @param locale the locale in use
	 * @return the proper, formatted date string
	 * @throws ParseException
	 */
	public String processDateString(String dateString, String format,
			Locale locale) throws ParseException {

		logger.debug("Entering processDateString: dateString='{}',"
				+ " format='{}', locale={}", dateString, format, locale);

		Objects.requireNonNull(format, "format must not be null");

		if (locale == null) {
			locale = Locale.getDefault();
		}

		Date date = convertStringToDate(dateString, format, locale);
		SimpleDateFormat dateFormatter = new SimpleDateFormat(format, locale);
		String finalString = dateFormatter.format(date);

		return finalString;
	}

	/**
	 * Processes the contents of a groovy expression specification and returns
	 * the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @return the processed value
	 */
	public String processGroovy(String string) {
		Map<String, ReportParameter> reportParamsMap = null;
		return processGroovy(string, reportParamsMap);
	}

	/**
	 * Processes the contents of a groovy expression specification and returns
	 * the processed value with these items replaced
	 *
	 * @param string the string to process
	 * @param reportParamsMap a map with report parameters
	 * @return the processed value
	 */
	public String processGroovy(String string, Map<String, ReportParameter> reportParamsMap) {
		String finalString = string;

		String[] groovyExpressions = StringUtils.substringsBetween(string, GROOVY_START_STRING, GROOVY_END_STRING);
		if (groovyExpressions != null) {
			CompilerConfiguration cc = new CompilerConfiguration();
			cc.addCompilationCustomizers(new SandboxTransformer());

			Map<String, Object> variables = new HashMap<>();
			if (reportParamsMap != null) {
				variables.putAll(reportParamsMap);
			}

			Binding binding = new Binding(variables);

			GroovyShell shell = new GroovyShell(binding, cc);

			GroovySandbox sandbox = null;
			if (Config.getCustomSettings().isEnableGroovySandbox()) {
				sandbox = new GroovySandbox();
				sandbox.register();
			}

			try {
				Map<String, String> groovyExpressionValues = new HashMap<>();
				for (String groovyExpression : groovyExpressions) {
					Object resultObject = shell.evaluate(groovyExpression);
					String resultString = String.valueOf(resultObject);
					String groovySpecification = GROOVY_START_STRING + groovyExpression + GROOVY_END_STRING;
					groovyExpressionValues.put(groovySpecification, resultString);
				}

				for (Entry<String, String> entry : groovyExpressionValues.entrySet()) {
					String searchString = entry.getKey();
					String replaceString = entry.getValue();
					finalString = StringUtils.replace(finalString, searchString, replaceString);
				}
			} finally {
				if (sandbox != null) {
					sandbox.unregister();
				}
			}
		}

		return finalString;
	}

	/**
	 * Runs a groovy expression and returns the result
	 *
	 * @param string the string containing the groovy script
	 * @return the object returned by the groovy script
	 */
	public Object runGroovyExpression(String string) {
		Map<String, Object> variables = null;
		return runGroovyExpression(string, variables);
	}

	/**
	 * Runs a groovy expression and returns the result
	 *
	 * @param string the string containing the groovy script
	 * @param report the report being run
	 * @param reportParamsMap report parameters
	 * @param multiFileMap files to be included as variables
	 * @return the object returned by the groovy script
	 */
	public Object runGroovyExpression(String string, Report report,
			Map<String, ReportParameter> reportParamsMap,
			MultiValueMap<String, MultipartFile> multiFileMap) {

		Map<String, Object> variables = new HashMap<>();

		if (reportParamsMap != null) {
			for (Entry<String, ReportParameter> entry : reportParamsMap.entrySet()) {
				String paramName = entry.getKey();
				ReportParameter reportParam = entry.getValue();
				if (reportParam.getParameter().getDataType() == ParameterDataType.File) {
					variables.put(paramName, null);
				} else {
					variables.put(paramName, reportParam);
				}
			}
		}

		if (multiFileMap != null) {
			variables.putAll(multiFileMap);
		}

		MongoClient mongoClient = null;
		Datasource datasource = report.getDatasource();
		if (datasource != null && datasource.getDatasourceType() == DatasourceType.MongoDB) {
			mongoClient = DbConnections.getMongodbConnection(datasource.getDatasourceId());
		}
		variables.put("mongoClient", mongoClient);

		return runGroovyExpression(string, variables);
	}

	/**
	 * Runs a groovy expression and returns the result
	 *
	 * @param string the string containing the groovy script
	 * @param variables the variables to pass
	 * @return the object returned by the groovy script
	 */
	public Object runGroovyExpression(String string, Map<String, Object> variables) {
		CompilerConfiguration cc = new CompilerConfiguration();
		cc.addCompilationCustomizers(new SandboxTransformer());

		Binding binding;
		if (variables == null) {
			binding = new Binding();
		} else {
			binding = new Binding(variables);
		}

		GroovyShell shell = new GroovyShell(binding, cc);

		GroovySandbox sandbox = null;
		if (Config.getCustomSettings().isEnableGroovySandbox()) {
			sandbox = new GroovySandbox();
			sandbox.register();
		}

		if (StringUtils.startsWith(string, GROOVY_START_STRING)) {
			string = StringUtils.substringBetween(string, GROOVY_START_STRING, GROOVY_END_STRING);
		}

		Object result = null;
		try {
			result = shell.evaluate(string);
		} finally {
			if (sandbox != null) {
				sandbox.unregister();
			}
		}

		return result;
	}

}
