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
package art.parameter;

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.report.Report;
import art.utils.ArtUtils;
import art.utils.ExpressionHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Represent a parameter
 *
 * @author Timothy Anyona
 */
public class Parameter implements Serializable {

	private static final long serialVersionUID = 1L;
	private int parameterId;
	private String name;
	private String description;
	private ParameterType parameterType;
	private String label;
	private String helpText;
	private String placeholderText;
	private ParameterDataType dataType;
	private String defaultValue;
	private boolean hidden;
	private boolean fixedValue;
	private boolean useLov;
	@JsonIgnore
	private boolean useRulesInLov;
	private int drilldownColumnIndex;
	@JsonIgnore
	private boolean useDirectSubstitution;
	private Date creationDate;
	private Date updateDate;
	private String createdBy;
	private String updatedBy;
	private boolean shared;
	private String options;
	private String dateFormat;
	private boolean useDefaultValueInJobs;
	private boolean allowNull;
	private boolean multipleFiles;
	private String fileAccept;
	private boolean timeAsString;
	private String template;
	@JsonIgnore
	private ParameterOptions parameterOptions;
	private Report defaultValueReport;
	private Report lovReport;
	private String name2; //used for holding a processed name
	private String dtAction;
	@JsonIgnore
	private boolean overwriteFiles;

	/**
	 * @return the timeAsString
	 */
	public boolean isTimeAsString() {
		return timeAsString;
	}

	/**
	 * @param timeAsString the timeAsString to set
	 */
	public void setTimeAsString(boolean timeAsString) {
		this.timeAsString = timeAsString;
	}

	/**
	 * @return the fileAccept
	 */
	public String getFileAccept() {
		return fileAccept;
	}

	/**
	 * @param fileAccept the fileAccept to set
	 */
	public void setFileAccept(String fileAccept) {
		this.fileAccept = fileAccept;
	}

	/**
	 * @return the multipleFiles
	 */
	public boolean isMultipleFiles() {
		return multipleFiles;
	}

	/**
	 * @param multipleFiles the multipleFiles to set
	 */
	public void setMultipleFiles(boolean multipleFiles) {
		this.multipleFiles = multipleFiles;
	}

	/**
	 * @return the allowNull
	 */
	public boolean isAllowNull() {
		return allowNull;
	}

	/**
	 * @param allowNull the allowNull to set
	 */
	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
	}

	/**
	 * @return the name2
	 */
	public String getName2() {
		return name2;
	}

	/**
	 * @param name2 the name2 to set
	 */
	public void setName2(String name2) {
		this.name2 = name2;
	}

	/**
	 * @return the dtAction
	 */
	public String getDtAction() {
		return dtAction;
	}

	/**
	 * @param dtAction the dtAction to set
	 */
	public void setDtAction(String dtAction) {
		this.dtAction = dtAction;
	}

	/**
	 * @return the overwriteFiles
	 */
	public boolean isOverwriteFiles() {
		return overwriteFiles;
	}

	/**
	 * @param overwriteFiles the overwriteFiles to set
	 */
	public void setOverwriteFiles(boolean overwriteFiles) {
		this.overwriteFiles = overwriteFiles;
	}

	/**
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * @return the fixedValue
	 */
	public boolean isFixedValue() {
		return fixedValue;
	}

	/**
	 * @param fixedValue the fixedValue to set
	 */
	public void setFixedValue(boolean fixedValue) {
		this.fixedValue = fixedValue;
	}

	/**
	 * @return the lovReport
	 */
	public Report getLovReport() {
		return lovReport;
	}

	/**
	 * @param lovReport the lovReport to set
	 */
	public void setLovReport(Report lovReport) {
		this.lovReport = lovReport;
	}

	/**
	 * @return the useDefaultValueInJobs
	 */
	public boolean isUseDefaultValueInJobs() {
		return useDefaultValueInJobs;
	}

	/**
	 * @param useDefaultValueInJobs the useDefaultValueInJobs to set
	 */
	public void setUseDefaultValueInJobs(boolean useDefaultValueInJobs) {
		this.useDefaultValueInJobs = useDefaultValueInJobs;
	}

	/**
	 * @return the placeholderText
	 */
	public String getPlaceholderText() {
		return placeholderText;
	}

	/**
	 * @param placeholderText the placeholderText to set
	 */
	public void setPlaceholderText(String placeholderText) {
		this.placeholderText = placeholderText;
	}

	/**
	 * @return the parameterOptions
	 */
	public ParameterOptions getParameterOptions() {
		return parameterOptions;
	}

	/**
	 * @param parameterOptions the parameterOptions to set
	 */
	public void setParameterOptions(ParameterOptions parameterOptions) {
		this.parameterOptions = parameterOptions;
	}

	/**
	 * @return the dateFormat
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * @return the shared
	 */
	public boolean isShared() {
		return shared;
	}

	/**
	 * @param shared the shared to set
	 */
	public void setShared(boolean shared) {
		this.shared = shared;
	}

	/**
	 * @return the defaultValueReport
	 */
	public Report getDefaultValueReport() {
		return defaultValueReport;
	}

	/**
	 * @param defaultValueReport the defaultValueReport to set
	 */
	public void setDefaultValueReport(Report defaultValueReport) {
		this.defaultValueReport = defaultValueReport;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	/**
	 * Get the value of description
	 *
	 * @return the value of description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the value of description
	 *
	 * @param description new value of description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the parameterId
	 */
	public int getParameterId() {
		return parameterId;
	}

	/**
	 * @param parameterId the parameterId to set
	 */
	public void setParameterId(int parameterId) {
		this.parameterId = parameterId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the parameterType
	 */
	public ParameterType getParameterType() {
		return parameterType;
	}

	/**
	 * @param parameterType the parameterType to set
	 */
	public void setParameterType(ParameterType parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the helpText
	 */
	public String getHelpText() {
		return helpText;
	}

	/**
	 * @param helpText the helpText to set
	 */
	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	/**
	 * @return the dataType
	 */
	public ParameterDataType getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(ParameterDataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return the useLov
	 */
	public boolean isUseLov() {
		return useLov;
	}

	/**
	 * @param useLov the useLov to set
	 */
	public void setUseLov(boolean useLov) {
		this.useLov = useLov;
	}

	/**
	 * @return the useRulesInLov
	 */
	public boolean isUseRulesInLov() {
		return useRulesInLov;
	}

	/**
	 * @param useRulesInLov the useRulesInLov to set
	 */
	public void setUseRulesInLov(boolean useRulesInLov) {
		this.useRulesInLov = useRulesInLov;
	}

	/**
	 * @return the drilldownColumnIndex
	 */
	public int getDrilldownColumnIndex() {
		return drilldownColumnIndex;
	}

	/**
	 * @param drilldownColumnIndex the drilldownColumnIndex to set
	 */
	public void setDrilldownColumnIndex(int drilldownColumnIndex) {
		this.drilldownColumnIndex = drilldownColumnIndex;
	}

	/**
	 * @return the useDirectSubstitution
	 */
	public boolean isUseDirectSubstitution() {
		return useDirectSubstitution;
	}

	/**
	 * @param useDirectSubstitution the useDirectSubstitution to set
	 */
	public void setUseDirectSubstitution(boolean useDirectSubstitution) {
		this.useDirectSubstitution = useDirectSubstitution;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the updateDate
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate the updateDate to set
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 61 * hash + this.parameterId;
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
		final Parameter other = (Parameter) obj;
		if (this.parameterId != other.parameterId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Parameter{" + "parameterId=" + parameterId + '}';
	}

	/**
	 * Returns the html element name that should be used for this parameter
	 *
	 * @return the html element name that should be used for this parameter
	 */
	@JsonIgnore
	public String getHtmlElementName() {
		return ArtUtils.PARAM_PREFIX + name;
	}

	/**
	 * Returns the default value string to be used in html elements. Null is
	 * returned as an empty string.
	 *
	 * @param locale the locale to use
	 * @return the default value string to be used in html elements
	 */
	public String getHtmlDefaultValue(Locale locale) {
		String value = defaultValue;

		if (defaultValue == null) {
			value = "";
		}

		return getHtmlValue(value, locale);
	}

	/**
	 * Returns the string that should be used in html elements
	 *
	 * @param value the original value
	 * @param locale the locale to use
	 * @return the string that should be used in html elements
	 */
	public String getHtmlValue(Object value, Locale locale) {
		switch (dataType) {
			case Date:
			case DateTime:
				//convert date to string that will be recognised by parameter processor class
				return getDateString(value, locale);
			case Time:
				return getTimeString(value, locale);
			default:
				return String.valueOf(value);
		}
	}

	/**
	 * Returns a date string for a given date object, formatted according to the
	 * parameter's date format setting
	 *
	 * @param value the date value
	 * @param locale the locale to use
	 * @return the formatted date string
	 */
	public String getDateString(Object value, Locale locale) {
		if (value instanceof String) {
			//may be string when value obtained from job parameters for display purposes only in editJob.jsp
			return (String) value;
		}

		switch (dataType) {
			case Date:
			case DateTime:
				if (StringUtils.isBlank(dateFormat)) {
					if (dataType == ParameterDataType.Date) {
						return ArtUtils.isoDateFormatter.format(value);
					} else {
						return ArtUtils.isoDateTimeFormatter.format(value);
					}
				} else {
					SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat, locale);
					return dateFormatter.format(value);
				}
			default:
				throw new IllegalArgumentException("Unexpected date data type: " + dataType);
		}
	}

	/**
	 * Returns a time string for a given time object, formatted according to the
	 * parameter's date format setting
	 *
	 * @param value the time value
	 * @param locale the locale to use
	 * @return the formatted time string
	 */
	public String getTimeString(Object value, Locale locale) {
		if (value instanceof String) {
			//may be string when value obtained from job parameters for display purposes only in editJob.jsp
			return (String) value;
		}

		if (StringUtils.isBlank(dateFormat)) {
			return ((LocalTime) value).format(ArtUtils.isoTimeFormatter);
		} else {
			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(dateFormat, locale);
			return ((LocalTime) value).format(timeFormatter);
		}
	}

	/**
	 * Returns the label to use for this parameter, given a particular locale,
	 * taking into consideration the i18n options defined for the parameter
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized label
	 * @throws java.io.IOException
	 */
	public String getLocalizedLabel(Locale locale) throws IOException {
		//note that el can't reliably call overloaded methods, so if a method is to be called from el, don't overload it
		//https://stackoverflow.com/questions/9763619/does-el-support-overloaded-methods
		String localizedLabel = null;

		if (locale != null) {
			Parameteri18nOptions i18nOptions = parameterOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nLabelOptions = i18nOptions.getLabel();
				localizedLabel = ArtUtils.getLocalizedValue(locale, i18nLabelOptions);
			}
		}

		if (localizedLabel == null) {
			localizedLabel = label;
		}

		return localizedLabel;
	}

	/**
	 * Returns the help text to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized help text
	 * @throws java.io.IOException
	 */
	public String getLocalizedHelpText(Locale locale) throws IOException {
		String localizedHelpText = null;

		if (locale != null) {
			Parameteri18nOptions i18nOptions = parameterOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nHelpTextOptions = i18nOptions.getHelpText();
				localizedHelpText = ArtUtils.getLocalizedValue(locale, i18nHelpTextOptions);
			}
		}

		if (localizedHelpText == null) {
			localizedHelpText = helpText;
		}

		return localizedHelpText;
	}

	/**
	 * Returns the default value to use for this parameter, given a particular
	 * locale, taking into consideration the i18n options defined for the
	 * parameter
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized default value
	 * @throws java.io.IOException
	 */
	public String getLocalizedDefaultValue(Locale locale) throws IOException {
		String localizedDefaultValue = null;

		if (locale != null) {
			Parameteri18nOptions i18nOptions = parameterOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nDefaultValueOptions = i18nOptions.getDefaultValue();
				localizedDefaultValue = ArtUtils.getLocalizedValue(locale, i18nDefaultValueOptions);
			}
		}

		if (localizedDefaultValue == null) {
			localizedDefaultValue = defaultValue;
		}

		return localizedDefaultValue;
	}

	/**
	 * Returns the placeholder text to use for this parameter, given a
	 * particular locale, taking into consideration the i18n options defined for
	 * the parameter
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized placeholder text
	 * @throws java.io.IOException
	 */
	public String getLocalizedPlaceholderText(Locale locale) throws IOException {
		String localizedPlaceholderText = null;

		if (locale != null) {
			Parameteri18nOptions i18nOptions = parameterOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nPlaceholderTextOptions = i18nOptions.getPlaceholderText();
				localizedPlaceholderText = ArtUtils.getLocalizedValue(locale, i18nPlaceholderTextOptions);
			}
		}

		if (localizedPlaceholderText == null) {
			localizedPlaceholderText = placeholderText;
		}

		return localizedPlaceholderText;
	}

	/**
	 * Returns the html type attribute to use for text input
	 *
	 * @return the html type attribute to use for text input
	 */
	@JsonIgnore
	public String getHtmlTextInputType() {
		switch (dataType) {
			case Integer:
				return "number";
			default:
				return "text";
		}
	}

	/**
	 * Encrypts all passwords fields in the parameter members including e.g. for
	 * lov reports etc
	 *
	 * @throws java.lang.Exception
	 */
	public void encryptAllPasswords() throws Exception {
		if (defaultValueReport != null) {
			defaultValueReport.encryptAllPasswords();
		}
		if (lovReport != null) {
			lovReport.encryptAllPasswords();
		}
	}

	/**
	 * Sets all password fields in the parameter members to null
	 */
	public void clearAllPasswords() {
		if (defaultValueReport != null) {
			defaultValueReport.clearAllPasswords();
		}
		if (lovReport != null) {
			lovReport.clearAllPasswords();
		}
	}

	/**
	 * Returns <code>true</code> if this parameter is to be displayed in the
	 * select parameters page
	 *
	 * @return <code>true</code> if this parameter is to be displayed in the
	 * select parameters page
	 */
	@JsonIgnore
	public boolean isForDisplay() {
		if (hidden || fixedValue) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns <code>true</code> if the parameter has a mask1 (RobinHerbots)
	 * mask defined in the options field
	 *
	 * @return <code>true</code> if the parameter has a mask1 (RobinHerbots)
	 * mask defined in the options field
	 */
	public boolean hasRobinHerbotsMask() {
		return StringUtils.contains(options, "\"mask1\"");
	}

	/**
	 * Returns the string to use for date range parameter options, performing
	 * additional processing for startDate and endDate options
	 *
	 * @param locale the locale in use
	 * @return the string to use for date range parameter options
	 * @throws IOException
	 * @throws ParseException
	 */
	@JsonIgnore
	public String getDateRangeOptions(Locale locale) throws IOException, ParseException {
		if (StringUtils.isBlank(options) || !StringUtils.containsAny(options, "startDate", "endDate")) {
			return options;
		}

		//http://tutorials.jenkov.com/java-json/jackson-objectmapper.html#read-map-from-json-string
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.readValue(options, new TypeReference<Map<String, Object>>() {
		});

		@SuppressWarnings("unchecked")
		Map<String, Object> dateRange = (Map<String, Object>) map.get("dateRange");
		if (dateRange != null) {
			String format = (String) dateRange.get("format");
			if (StringUtils.isBlank(format)) {
				final String DEFAULT_FORMAT = "yyyy-MM-dd";
				format = DEFAULT_FORMAT;
			}

			boolean changesMade = false;
			ExpressionHelper expressionHelper = new ExpressionHelper();
			String startDate = (String) dateRange.get("startDate");
			if (StringUtils.startsWithIgnoreCase(startDate, "add")) {
				startDate = expressionHelper.processDateString(startDate, format, locale);
				dateRange.put("startDate", startDate);
				changesMade = true;
			}

			String endDate = (String) dateRange.get("endDate");
			if (StringUtils.startsWithIgnoreCase(endDate, "add")) {
				endDate = expressionHelper.processDateString(endDate, format, locale);
				dateRange.put("endDate", endDate);
				changesMade = true;
			}

			if (changesMade) {
				return ArtUtils.objectToJson(map);
			}
		}

		return options;
	}

	/**
	 * Returns a copy of this parameter with only some fields filled to avoid
	 * exposing passwords
	 *
	 * @return a copy of this parameter with only some fields filled
	 */
	@JsonIgnore
	public Parameter getBasicParameter() {
		Parameter parameter = new Parameter();

		parameter.setParameterId(parameterId);
		parameter.setName(name);
		parameter.setName2(name2);
		parameter.setDescription(description);
		parameter.setDtAction(dtAction);
		parameter.setShared(shared);

		return parameter;
	}

	/**
	 * Returns the id of the parameter for use with table actions
	 *
	 * @return the parameter id
	 */
	public int getDtId() {
		return parameterId;
	}

	/**
	 * Returns the name of the parameter for use with table actions
	 *
	 * @return the parameter name
	 */
	public String getDtName() {
		return name + " (" + parameterId + ")";
	}

}
