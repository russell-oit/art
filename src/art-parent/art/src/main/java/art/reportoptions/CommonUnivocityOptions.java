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
package art.reportoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.univocity.parsers.common.processor.ObjectRowWriterProcessor;
import com.univocity.parsers.conversions.Conversions;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Common options for univocity output
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonUnivocityOptions {

	private String dateFormat;
	private String dateTimeFormat;
	private String numberFormat;
	private List<Map<String, List<String>>> fieldNumberFormats;
	private List<Map<String, List<String>>> fieldIntegerFormats;
	private List<Map<String, List<String>>> fieldLongFormats;
	private List<Map<String, List<String>>> fieldDoubleFormats;
	private List<Map<String, List<String>>> fieldDateFormats;
	private boolean includeHeaders = true;

	/**
	 * @return the includeHeaders
	 */
	public boolean isIncludeHeaders() {
		return includeHeaders;
	}

	/**
	 * @param includeHeaders the includeHeaders to set
	 */
	public void setIncludeHeaders(boolean includeHeaders) {
		this.includeHeaders = includeHeaders;
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
	 * @return the dateTimeFormat
	 */
	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	/**
	 * @param dateTimeFormat the dateTimeFormat to set
	 */
	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}

	/**
	 * @return the numberFormat
	 */
	public String getNumberFormat() {
		return numberFormat;
	}

	/**
	 * @param numberFormat the numberFormat to set
	 */
	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}

	/**
	 * @return the fieldNumberFormats
	 */
	public List<Map<String, List<String>>> getFieldNumberFormats() {
		return fieldNumberFormats;
	}

	/**
	 * @param fieldNumberFormats the fieldNumberFormats to set
	 */
	public void setFieldNumberFormats(List<Map<String, List<String>>> fieldNumberFormats) {
		this.fieldNumberFormats = fieldNumberFormats;
	}

	/**
	 * @return the fieldIntegerFormats
	 */
	public List<Map<String, List<String>>> getFieldIntegerFormats() {
		return fieldIntegerFormats;
	}

	/**
	 * @param fieldIntegerFormats the fieldIntegerFormats to set
	 */
	public void setFieldIntegerFormats(List<Map<String, List<String>>> fieldIntegerFormats) {
		this.fieldIntegerFormats = fieldIntegerFormats;
	}

	/**
	 * @return the fieldLongFormats
	 */
	public List<Map<String, List<String>>> getFieldLongFormats() {
		return fieldLongFormats;
	}

	/**
	 * @param fieldLongFormats the fieldLongFormats to set
	 */
	public void setFieldLongFormats(List<Map<String, List<String>>> fieldLongFormats) {
		this.fieldLongFormats = fieldLongFormats;
	}

	/**
	 * @return the fieldDoubleFormats
	 */
	public List<Map<String, List<String>>> getFieldDoubleFormats() {
		return fieldDoubleFormats;
	}

	/**
	 * @param fieldDoubleFormats the fieldDoubleFormats to set
	 */
	public void setFieldDoubleFormats(List<Map<String, List<String>>> fieldDoubleFormats) {
		this.fieldDoubleFormats = fieldDoubleFormats;
	}

	/**
	 * @return the fieldDateFormats
	 */
	public List<Map<String, List<String>>> getFieldDateFormats() {
		return fieldDateFormats;
	}

	/**
	 * @param fieldDateFormats the fieldDateFormats to set
	 */
	public void setFieldDateFormats(List<Map<String, List<String>>> fieldDateFormats) {
		this.fieldDateFormats = fieldDateFormats;
	}

	/**
	 * Sets field formats to use for different data types and fields
	 *
	 * @param processor the processor to set, not null
	 * @param locale the locale that determines date format output, not null
	 */
	public void initializeProcessor(ObjectRowWriterProcessor processor, Locale locale) {
		Objects.requireNonNull(processor, "processor must not be null");
		Objects.requireNonNull(locale, "locale must not be null");

		if (StringUtils.isNotBlank(dateFormat)) {
			processor.convertType(java.sql.Date.class, Conversions.toDate(locale, dateFormat));
		}

		if (StringUtils.isNotBlank(dateTimeFormat)) {
			processor.convertType(java.sql.Timestamp.class, Conversions.toDate(locale, dateTimeFormat));
		}

		if (StringUtils.isNotBlank(numberFormat)) {
			processor.convertType(java.lang.Integer.class, Conversions.formatToNumber(java.lang.Integer.class, numberFormat));
			processor.convertType(java.lang.Long.class, Conversions.formatToNumber(java.lang.Long.class, numberFormat));
			processor.convertType(java.lang.Double.class, Conversions.formatToNumber(java.lang.Double.class, numberFormat));
		}

		if (CollectionUtils.isNotEmpty(fieldNumberFormats)) {
			for (Map<String, List<String>> numberFormatDefinition : fieldNumberFormats) {
				Map.Entry<String, List<String>> entry = numberFormatDefinition.entrySet().iterator().next();
				String fieldNumberFormat = entry.getKey();
				List<String> fieldNames = entry.getValue();
				processor.convertFields(Conversions.formatToNumber(fieldNumberFormat)).set(fieldNames);
			}
		}

		if (CollectionUtils.isNotEmpty(fieldIntegerFormats)) {
			for (Map<String, List<String>> integerFormatDefinition : fieldIntegerFormats) {
				Map.Entry<String, List<String>> entry = integerFormatDefinition.entrySet().iterator().next();
				String fieldIntegerFormat = entry.getKey();
				List<String> fieldNames = entry.getValue();
				processor.convertFields(Conversions.formatToNumber(java.lang.Integer.class, fieldIntegerFormat)).set(fieldNames);
			}
		}

		if (CollectionUtils.isNotEmpty(fieldLongFormats)) {
			for (Map<String, List<String>> longFormatDefinition : fieldLongFormats) {
				Map.Entry<String, List<String>> entry = longFormatDefinition.entrySet().iterator().next();
				String fieldLongFormat = entry.getKey();
				List<String> fieldNames = entry.getValue();
				processor.convertFields(Conversions.formatToNumber(java.lang.Long.class, fieldLongFormat)).set(fieldNames);
			}
		}

		if (CollectionUtils.isNotEmpty(fieldDoubleFormats)) {
			for (Map<String, List<String>> doubleFormatDefinition : fieldDoubleFormats) {
				Map.Entry<String, List<String>> entry = doubleFormatDefinition.entrySet().iterator().next();
				String fieldDoubleFormat = entry.getKey();
				List<String> fieldNames = entry.getValue();
				processor.convertFields(Conversions.formatToNumber(java.lang.Double.class, fieldDoubleFormat)).set(fieldNames);
			}
		}

		if (CollectionUtils.isNotEmpty(fieldDateFormats)) {
			for (Map<String, List<String>> dateFormatDefinition : fieldDateFormats) {
				Map.Entry<String, List<String>> entry = dateFormatDefinition.entrySet().iterator().next();
				String fieldDateFormat = entry.getKey();
				List<String> fieldNames = entry.getValue();
				processor.convertFields(Conversions.toDate(locale, fieldDateFormat)).set(fieldNames);
			}
		}
	}

}
