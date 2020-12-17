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
package art.reportparameter;

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.parameter.Parameter;
import art.report.Report;
import art.utils.ArtUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a report parameter
 *
 * @author Timothy Anyona
 */
public class ReportParameter implements Serializable {

	private static final long serialVersionUID = 1L;

	private int parentId; //used for import/export of linked records e.g. reports
	private int reportParameterId;
	@JsonIgnore
	private Report report;
	private int position;
	@JsonIgnore
	private String[] passedParameterValues; //used for run report logic
	@JsonIgnore
	private Map<Object, String> lovValues; //store value and label for lov parameters
	@JsonIgnore
	private List<Object> actualParameterValues = new ArrayList<>();
	private String chainedParents;
	private String chainedDepends;
	@JsonIgnore
	private boolean chainedParent;
	@JsonIgnore
	private Map<String, String> lovValuesAsString;
	@JsonIgnore
	private Map<String, String> defaultValueLovValues;
	private Parameter parameter;

	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return the defaultValueLovValues
	 */
	public Map<String, String> getDefaultValueLovValues() {
		return defaultValueLovValues;
	}

	/**
	 * @param defaultValueLovValues the defaultValueLovValues to set
	 */
	public void setDefaultValueLovValues(Map<String, String> defaultValueLovValues) {
		this.defaultValueLovValues = defaultValueLovValues;
	}

	/**
	 * @return the lovValuesAsString
	 */
	public Map<String, String> getLovValuesAsString() {
		return lovValuesAsString;
	}

	/**
	 * @param lovValuesAsString the lovValuesAsString to set
	 */
	public void setLovValuesAsString(Map<String, String> lovValuesAsString) {
		this.lovValuesAsString = lovValuesAsString;
	}

	/**
	 * @return the chainedParent
	 */
	public boolean isChainedParent() {
		return chainedParent;
	}

	/**
	 * @param chainedParent the chainedParent to set
	 */
	public void setChainedParent(boolean chainedParent) {
		this.chainedParent = chainedParent;
	}

	/**
	 * @return the chainedParents
	 */
	public String getChainedParents() {
		return chainedParents;
	}

	/**
	 * @param chainedParents the chainedParents to set
	 */
	public void setChainedParents(String chainedParents) {
		this.chainedParents = chainedParents;
	}

	/**
	 * @return the chainedDepends
	 */
	public String getChainedDepends() {
		return chainedDepends;
	}

	/**
	 * @param chainedDepends the chainedDepends to set
	 */
	public void setChainedDepends(String chainedDepends) {
		this.chainedDepends = chainedDepends;
	}

	/**
	 * Get the value of actualParameterValues
	 *
	 * @return the value of actualParameterValues
	 */
	public List<Object> getActualParameterValues() {
		return actualParameterValues;
	}

	/**
	 * Set the value of actualParameterValues
	 *
	 * @param actualParameterValues new value of actualParameterValues
	 */
	public void setActualParameterValues(List<Object> actualParameterValues) {
		this.actualParameterValues = actualParameterValues;
	}

	/**
	 * Get the value of lovValues
	 *
	 * @return the value of lovValues
	 */
	public Map<Object, String> getLovValues() {
		return lovValues;
	}

	/**
	 * Set the value of lovValues
	 *
	 * @param lovValues new value of lovValues
	 */
	public void setLovValues(Map<Object, String> lovValues) {
		this.lovValues = lovValues;
	}

	/**
	 * @return the reportParameterId
	 */
	public int getReportParameterId() {
		return reportParameterId;
	}

	/**
	 * @param reportParameterId the reportParameterId to set
	 */
	public void setReportParameterId(int reportParameterId) {
		this.reportParameterId = reportParameterId;
	}

	/**
	 * @return the report
	 */
	public Report getReport() {
		return report;
	}

	/**
	 * @param report the report to set
	 */
	public void setReport(Report report) {
		this.report = report;
	}

	/**
	 * @return the parameter
	 */
	public Parameter getParameter() {
		return parameter;
	}

	/**
	 * @param parameter the parameter to set
	 */
	public void setParameter(Parameter parameter) {
		this.parameter = parameter;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the passedParameterValues
	 */
	public String[] getPassedParameterValues() {
		return passedParameterValues;
	}

	/**
	 * @param passedParameterValues the passedParameterValues to set
	 */
	public void setPassedParameterValues(String[] passedParameterValues) {
		this.passedParameterValues = passedParameterValues;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 23 * hash + this.reportParameterId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ReportParameter other = (ReportParameter) obj;
		if (this.reportParameterId != other.reportParameterId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ReportParameter{" + "reportParameterId=" + reportParameterId + '}';
	}

	/**
	 * Returns actual parameter values in a formatted manner. For lov
	 * parameters, parameter display value is included together with bracketed
	 * internal value.
	 *
	 * @return actual parameter values in a formatted manner
	 */
	@JsonIgnore
	public String getDisplayValues() {
		boolean textOnly = false;
		return getDisplayValuesWithTextOption(textOnly);
	}

	/**
	 * Returns actual parameter values in a formatted manner. For lov
	 * parameters, only parameter display name is included.
	 *
	 * @return actual parameter values in a formatted manner
	 */
	@JsonIgnore
	public String getDisplayValuesTextOnly() {
		boolean textOnly = true;
		return getDisplayValuesWithTextOption(textOnly);
	}

	/**
	 * Returns actual parameter values in a formatted manner
	 *
	 * @param textOnly whether to have text only without bracketed value for lov
	 * parameters
	 * @return actual parameter values in a formatted manner
	 */
	@JsonIgnore
	private String getDisplayValuesWithTextOption(boolean textOnly) {
		List<String> paramDisplayStrings = new ArrayList<>();

		for (Object paramValue : actualParameterValues) {
			String paramValueString;
			if (paramValue == null) {
				paramValueString = "null";
			} else {
				ParameterDataType parameterDataType = parameter.getDataType();
				switch (parameterDataType) {
					case Date:
						paramValueString = ArtUtils.isoDateFormatter.format((Date) paramValue);
						break;
					case DateTime:
						paramValueString = ArtUtils.isoDateTimeSecondsFormatter.format((Date) paramValue);
						break;
					default:
						paramValueString = String.valueOf(paramValue);
				}
			}

			String displayValue = null;
			if (parameter.isUseLov() && lovValues != null) {
				//for lov parameters, show both parameter value and display string if any
				displayValue = lovValues.get(paramValue);
			}

			String paramDisplayString;
			if (displayValue == null) {
				paramDisplayString = paramValueString;
			} else {
				paramDisplayString = displayValue;
				if (!textOnly) {
					paramDisplayString += " (" + paramValueString + ")";
				}
			}

			paramDisplayStrings.add(paramDisplayString);
		}

		return StringUtils.join(paramDisplayStrings, ", ");
	}

	/**
	 * Returns the parameter name and actual parameter values in a formatted
	 * manner
	 *
	 * @return the parameter name and actual parameter values in a formatted
	 * manner
	 */
	@JsonIgnore
	public String getNameAndDisplayValues() {
		return parameter.getName() + ": " + getDisplayValues();
	}

	/**
	 * Returns the parameter label and actual parameter values in a formatted
	 * manner
	 *
	 * @return the parameter label and actual parameter values in a formatted
	 * manner
	 */
	@JsonIgnore
	public String getLabelAndDisplayValues() {
		return parameter.getLabel() + ": " + getDisplayValues();
	}

	/**
	 * Returns the parameter label and actual parameter values in a formatted
	 * manner, parameter name being localized according to the given locale
	 *
	 * @param locale the locale object for the appropriate locale
	 * @return the parameter label and actual parameter values in a formatted
	 * manner, parameter name being localized according to the given locale
	 * @throws java.io.IOException
	 */
	@JsonIgnore
	public String getLocalizedLabelAndDisplayValues(Locale locale) throws IOException {
		return parameter.getLocalizedLabel(locale) + ": " + getDisplayValues();
	}

	/**
	 * Returns the effective actual parameter value. An appropriate object for
	 * single-value parameters or a list for multi-value parameters.
	 *
	 * @return the effective actual parameter value
	 */
	@JsonIgnore
	public Object getEffectiveActualParameterValue() {
		if (parameter.getParameterType() == ParameterType.SingleValue) {
			if (actualParameterValues.contains(null) || actualParameterValues.isEmpty()) {
				return null;
			} else {
				return actualParameterValues.get(0);
			}
		} else {
			return actualParameterValues;
		}
	}

	/**
	 * Returns the html element name to be used for this parameter
	 *
	 * @return the html element name to be used for this parameter
	 */
	@JsonIgnore
	public String getHtmlElementName() {
		if (parameter.getDataType() == ParameterDataType.File) {
			return parameter.getName();
		} else {
			return ArtUtils.PARAM_PREFIX + parameter.getName();
		}
	}

	/**
	 * Returns the html element name to be used with a hidden input for this
	 * parameter
	 *
	 * @return the html element name to be used with a hidden input for this
	 * parameter
	 */
	@JsonIgnore
	public String getHiddenHtmlElementName() {
		return getHtmlElementName() + "-hidden";
	}

	/**
	 * Returns the html element name to be used with a null check box for this
	 * parameter
	 *
	 * @return the html element name to be used with a null check box for this
	 * parameter
	 */
	@JsonIgnore
	public String getNullHtmlElementName() {
		return getHtmlElementName() + "-null";
	}

	/**
	 * Returns the html element value to be used for this parameter
	 *
	 * @return the html element value to be used for this parameter
	 * @throws java.io.IOException
	 */
	@JsonIgnore
	public String getHtmlValue() throws IOException {
		Locale locale = null;
		return getHtmlValueWithLocale(locale);
	}

	/**
	 * Returns the html element value to be used for this parameter
	 *
	 * @param locale the locale being used
	 * @return the html element value to be used for this parameter
	 * @throws java.io.IOException
	 */
	@JsonIgnore
	public String getHtmlValueWithLocale(Locale locale) throws IOException {
		//note that el can't reliably call overloaded methods, so if a method is to be called from el, don't overload it
		//https://stackoverflow.com/questions/9763619/does-el-support-overloaded-methods
		Object value = getEffectiveActualParameterValue();

		if (value == null) {
			return "";
		}

		String returnValue;

		if (value instanceof List) {
			List<String> values = new ArrayList<>();
			@SuppressWarnings("unchecked")
			List<Object> valueList = (List<Object>) value;
			for (int i = 0; i < valueList.size(); i++) {
				String htmlValue = String.valueOf(valueList.get(i));
				values.add(htmlValue);
			}
			//https://stackoverflow.com/questions/8627902/new-line-in-text-area
			//https://stackoverflow.com/questions/7693994/how-to-convert-ascii-code-0-255-to-a-string-of-the-associated-character
			int NEWLINE_CHAR_ASCII = 10;
			returnValue = StringUtils.join(values, String.valueOf(Character.toChars(NEWLINE_CHAR_ASCII)));
		} else {
			ParameterDataType parameterDataType = parameter.getDataType();
			if (parameterDataType.isDate()) {
				returnValue = parameter.getDateString(value, locale);
			} else if (parameterDataType.isNumeric()) {
				String[] passedValues = passedParameterValues;
				String defaultValue = parameter.getLocalizedDefaultValue(locale);
				Report defaultValueReport = parameter.getDefaultValueReport();

				if (passedValues == null && StringUtils.isBlank(defaultValue)
						&& defaultValueReport == null) {
					returnValue = ""; //return blank instead of "0" for integers or "0.0" for doubles
				} else {
					returnValue = String.valueOf(value);
				}
			} else {
				returnValue = String.valueOf(value);
			}
		}

		return returnValue;
	}

	/**
	 * Returns the html element ids of chained parents elements
	 *
	 * @return the html element ids of chained parents elements
	 */
	@JsonIgnore
	public String getChainedParentsHtmlIds() {
		return getHtmlIds(chainedParents);
	}

	/**
	 * Returns the html element ids of chained depends elements
	 *
	 * @return the html element ids of chained depends elements
	 */
	@JsonIgnore
	public String getChainedDependsHtmlIds() {
		return getHtmlIds(chainedDepends);
	}

	/**
	 * Returns html element ids to be used with the given strings
	 *
	 * @param ids the strings to use
	 * @return html element ids to be used with the given strings
	 */
	@JsonIgnore
	private String getHtmlIds(String ids) {
		if (StringUtils.isBlank(ids)) {
			return "";
		}

		String[] idsArray = StringUtils.split(ids, ",");
		List<String> idsList = new ArrayList<>();
		for (String id : idsArray) {
			String finalId = "#" + ArtUtils.PARAM_PREFIX + id;
			idsList.add(finalId);
		}

		String finalIds = StringUtils.join(idsList, ",");
		return finalIds;
	}

	/**
	 * Returns <code>true</code> if this parameter is chained
	 *
	 * @return <code>true</code> if this parameter is chained
	 */
	@JsonIgnore
	public boolean isChained() {
		if (StringUtils.isBlank(chainedParents)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Converts lov values to string
	 *
	 * @param lovValuesAsObjects lov values using objects
	 * @return lov values using strings
	 */
	public Map<String, String> convertLovValuesFromObjectToString(Map<Object, String> lovValuesAsObjects) {
		Map<String, String> stringLovValues = new LinkedHashMap<>();

		for (Entry<Object, String> entry : lovValuesAsObjects.entrySet()) {
			Object dataValue = entry.getKey();
			String displayValue = entry.getValue();

			String stringValue;
			switch (parameter.getDataType()) {
				case Date:
					stringValue = ArtUtils.isoDateFormatter.format(dataValue);
					break;
				case DateTime:
					stringValue = ArtUtils.isoDateTimeFormatter.format(dataValue);
					break;
				default:
					stringValue = String.valueOf(dataValue);
			}

			stringLovValues.put(stringValue, displayValue);
		}

		return stringLovValues;
	}

	/**
	 * Returns the effective actual parameter value. Returns the same result as
	 * getEffectiveActualParameterValue
	 *
	 * @return effective actual parameter value
	 */
	@JsonIgnore
	public Object getValue() {
		//for use with jxls
		//https://sourceforge.net/p/art/discussion/352129/thread/f8dc311dbe/?limit=25
		//https://mostafa-asg.github.io/post/customize-json-xml-spring-mvc-output/
		//https://stackoverflow.com/questions/6541757/when-using-spring-mvc-for-rest-how-do-you-enable-jackson-to-pretty-print-render
		//https://stackoverflow.com/questions/37844101/can-you-configure-spring-controller-specific-jackson-deserialization
		return getEffectiveActualParameterValue();
	}

	/**
	 * Returns the effective actual parameter value for use within an sql query.
	 * Values are escaped.
	 *
	 * @return effective actual parameter value for use within an sql query
	 */
	@JsonIgnore
	public Object getSqlValue() {
		//for use with jxls
		ParameterType parameterType = parameter.getParameterType();

		Object value = getEffectiveActualParameterValue();
		if (parameterType == null) {
			throw new RuntimeException("Unexpected parameter type: " + parameterType);
		} else {
			switch (parameterType) {
				case SingleValue:
					String finalValue;
					switch (parameter.getDataType()) {
						case Integer:
						case Double:
							finalValue = String.valueOf(value);
							break;
						case Date:
							finalValue = "'" + ArtUtils.isoDateFormatter.format(value) + "'";
							break;
						case DateTime:
							finalValue = "'" + ArtUtils.isoDateTimeMillisecondsFormatter.format(value) + "'";
							break;
						default:
							finalValue = String.valueOf(value);
							StringUtils.replace(finalValue, "'", "''");
							finalValue = "'" + finalValue + "'";
					}
					return finalValue;
				case MultiValue:
					@SuppressWarnings("unchecked") List<Object> values = (List<Object>) value;
					String finalValues;
					List<String> stringValues = new ArrayList<>();
					switch (parameter.getDataType()) {
						case Integer:
						case Double:
							for (Object listValue : values) {
								stringValues.add(String.valueOf(listValue));
							}
							break;
						case Date:
							for (Object listValue : values) {
								stringValues.add("'" + ArtUtils.isoDateFormatter.format(listValue) + "'");
							}
							break;
						case DateTime:
							for (Object listValue : values) {
								stringValues.add("'" + ArtUtils.isoDateTimeMillisecondsFormatter.format(listValue) + "'");
							}
							break;
						default:
							for (Object listValue : values) {
								String stringValue = String.valueOf(listValue);
								StringUtils.replace(stringValue, "'", "''");
								stringValue = "'" + stringValue + "'";
								stringValues.add(stringValue);
							}
					}
					finalValues = StringUtils.join(stringValues, ",");

					return finalValues;
				default:
					throw new RuntimeException("Unexpected parameter type: " + parameterType);
			}
		}
	}

	/**
	 * Returns the passed parameter value. Single string value or null for
	 * single value parameters or a string array or null for multi value
	 * parameters
	 *
	 * @return the passed parameter value
	 */
	@JsonIgnore
	public Object getPassedValue() {
		if (parameter.getParameterType() == ParameterType.SingleValue) {
			if (passedParameterValues == null || passedParameterValues.length == 0) {
				return null;
			} else {
				return passedParameterValues[0];
			}
		} else {
			return passedParameterValues;
		}
	}

	/**
	 * Returns <code>true</code> if the given lov value should be selected in
	 * the parameter dropdown list
	 *
	 * @param lovValue the lov value
	 * @return <code>true</code> if the given lov value should be selected in
	 * the parameter dropdown list
	 * @throws java.io.IOException
	 */
	public boolean selectLovValue(String lovValue) throws IOException {
		ParameterType parameterType = parameter.getParameterType();

		switch (parameterType) {
			case SingleValue:
				//compare lov value to actual parameter value - default value or passed value
				if (passedParameterValues == null) {
					String defaultValue = parameter.getDefaultValue();
					if (StringUtils.equalsIgnoreCase(defaultValue, lovValue)) {
						return true;
					}

					if (MapUtils.isNotEmpty(defaultValueLovValues)) {
						for (String value : defaultValueLovValues.keySet()) {
							if (StringUtils.equalsIgnoreCase(value, lovValue)) {
								return true;
							}
						}
					}
				} else {
					String htmlValue = getHtmlValue();
					if (StringUtils.equalsIgnoreCase(htmlValue, lovValue)) {
						return true;
					}
				}

				return false;
			case MultiValue:
				//compare lov value to default values or passed values
				if (passedParameterValues == null) {
					String defaultValueSetting = parameter.getDefaultValue();
					if (StringUtils.isNotEmpty(defaultValueSetting)) {
						String defaultValues[] = defaultValueSetting.split("\\r?\\n");
						for (String defaultValue : defaultValues) {
							if (StringUtils.equalsIgnoreCase(defaultValue, lovValue)) {
								return true;
							}
						}
					}

					if (MapUtils.isNotEmpty(defaultValueLovValues)) {
						for (String value : defaultValueLovValues.keySet()) {
							if (StringUtils.equalsIgnoreCase(value, lovValue)) {
								return true;
							}
						}
					}
				} else {
					for (String passedValue : passedParameterValues) {
						if (StringUtils.equalsIgnoreCase(passedValue, lovValue)) {
							return true;
						}
					}
				}

				return false;
			default:
				return false;
		}
	}

	/**
	 * Returns all the possible default values
	 *
	 * @return all the possible default values
	 * @throws IOException
	 */
	@JsonIgnore
	public List<String> getDefaultValues() throws IOException {
		List<String> defaultValues = new ArrayList<>();

		ParameterType parameterType = parameter.getParameterType();

		switch (parameterType) {
			case SingleValue:
				//compare lov value to actual parameter value - default value or passed value
				if (passedParameterValues == null) {
					String defaultValue = parameter.getDefaultValue();
					if (StringUtils.isNotEmpty(defaultValue)) {
						defaultValues.add(defaultValue);
					}

					if (MapUtils.isNotEmpty(defaultValueLovValues)) {
						for (String value : defaultValueLovValues.keySet()) {
							if (StringUtils.isNotEmpty(value)) {
								defaultValues.add(value);
							}
						}
					}
				} else {
					String htmlValue = getHtmlValue();
					if (StringUtils.isNotEmpty(htmlValue)) {
						defaultValues.add(htmlValue);
					}
				}

				break;
			case MultiValue:
				//compare lov value to default values or passed values
				if (passedParameterValues == null) {
					String defaultValueSetting = parameter.getDefaultValue();
					if (StringUtils.isNotEmpty(defaultValueSetting)) {
						String defaultValuesArray[] = defaultValueSetting.split("\\r?\\n");
						for (String defaultValue : defaultValuesArray) {
							if (StringUtils.isNotEmpty(defaultValue)) {
								defaultValues.add(defaultValue);
							}
						}
					}

					if (MapUtils.isNotEmpty(defaultValueLovValues)) {
						for (String value : defaultValueLovValues.keySet()) {
							if (StringUtils.isNotEmpty(value)) {
								defaultValues.add(value);
							}
						}
					}
				} else {
					for (String passedValue : passedParameterValues) {
						if (StringUtils.isNotEmpty(passedValue)) {
							defaultValues.add(passedValue);
						}
					}
				}

				break;
			default:
				break;
		}

		return defaultValues;
	}

}
