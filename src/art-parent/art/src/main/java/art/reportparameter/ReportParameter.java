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
package art.reportparameter;

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.parameter.Parameter;
import art.report.Report;
import art.utils.ArtUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a report parameter
 *
 * @author Timothy Anyona
 */
public class ReportParameter implements Serializable {

	private static final long serialVersionUID = 1L;

	private int reportParameterId;
	private Report report;
	private Parameter parameter;
	private int position;
	private String[] passedParameterValues; //used for run report logic
	private Map<Object, String> lovValues; //store value and label for lov parameters
	private List<Object> actualParameterValues;
	private String chainedParents;
	private String chainedDepends;
	private boolean chainedParent;
	private Map<String, String> lovValuesAsString;

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
	 * Returns actual parameter values in a formatted manner
	 *
	 * @return actual parameter values in a formatted manner
	 */
	public String getDisplayValues() {
		List<String> paramDisplayStrings = new ArrayList<>();

		for (Object paramValue : actualParameterValues) {
			String paramValueString;
			ParameterDataType parameterDataType = parameter.getDataType();
			switch (parameterDataType) {
				case Date:
					paramValueString = ArtUtils.isoDateFormatter.format((Date) paramValue);
					break;
				case DateTime:
					paramValueString = ArtUtils.isoDateTimeFormatter.format((Date) paramValue);
					break;
				default:
					paramValueString = String.valueOf(paramValue);
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
				paramDisplayString = displayValue + " (" + paramValueString + ")";
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
	public String getNameAndDisplayValues() {
		return parameter.getLabel() + ": " + getDisplayValues();
	}

	/**
	 * Returns the effective actual parameter value. An appropriate object for
	 * single-value parameters or a list for multi-value parameters.
	 *
	 * @return the effective actual parameter value
	 */
	public Object getEffectiveActualParameterValue() {
		if (parameter.getParameterType() == ParameterType.SingleValue) {
			return actualParameterValues.get(0);
		} else {
			return actualParameterValues;
		}
	}

	/**
	 * Returns the html element name to be used for this parameter
	 *
	 * @return the html element name to be used for this parameter
	 */
	public String getHtmlElementName() {
		return "p-" + parameter.getName();
	}

	/**
	 * Returns the html element value to be used for this parameter
	 *
	 * @return the html element value to be used for this parameter
	 */
	public String getHtmlValue() {
		Object value = getEffectiveActualParameterValue();

		if (value == null) {
			return "";
		} 

		ParameterDataType parameterDataType = parameter.getDataType();
		switch (parameterDataType) {
			case Date:
				return ArtUtils.isoDateFormatter.format(value);
			case DateTime:
				return ArtUtils.isoDateTimeFormatter.format(value);
			default:
				if (value instanceof List) {
					List<Object> values = new ArrayList<>();
					List valueList = (List) value;
					for (int i = 0; i < valueList.size(); i++) {
						values.add(String.valueOf(valueList.get(i)));
					}
					return StringUtils.join(values, "\\r\\n");
				} else {
					return String.valueOf(value);
				}
		}
	}

	/**
	 * Returns the html element ids of chained parents elements
	 *
	 * @return the html element ids of chained parents elements
	 */
	public String getChainedParentsHtmlIds() {
		return getHtmlIds(chainedParents);
	}

	/**
	 * Returns the html element ids of chained depends elements
	 *
	 * @return the html element ids of chained depends elements
	 */
	public String getChainedDependsHtmlIds() {
		return getHtmlIds(chainedDepends);
	}

	/**
	 * Returns html element ids to be used with the given strings
	 *
	 * @param ids the strings to use
	 * @return html element ids to be used with the given strings
	 */
	private String getHtmlIds(String ids) {
		if (StringUtils.isBlank(ids)) {
			return "";
		}

		String[] idsArray = StringUtils.split(ids, ",");
		List<String> idsList = new ArrayList<>();
		for (String id : idsArray) {
			String finalId = "#p-" + id;
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
	public Object getValue() {
		//for use with jxls
		return getEffectiveActualParameterValue();
	}

	/**
	 * Returns the effective actual parameter value for use within an sql query.
	 * Values are escaped.
	 *
	 * @return effective actual parameter value for use within an sql query
	 */
	public Object getSqlValue() {
		//for use with jxls
		ParameterType parameterType = parameter.getParameterType();

		Object value = getEffectiveActualParameterValue();
		if (parameterType == null) {
			throw new IllegalStateException("Unexpected parameter type: " + parameterType);
		} else {
			switch (parameterType) {
				case SingleValue:
					String finalValue;
					switch (parameter.getDataType()) {
						case Integer:
						case Datasource:
						case Number:
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
						case Datasource:
						case Number:
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
					throw new IllegalStateException("Unexpected parameter type: " + parameterType);
			}
		}
	}
}
