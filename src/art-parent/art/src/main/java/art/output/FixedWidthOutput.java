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
package art.output;

import art.reportoptions.FixedWidthOptions;
import com.univocity.parsers.common.processor.ObjectRowWriterProcessor;
import com.univocity.parsers.conversions.Conversions;
import com.univocity.parsers.fixed.FieldAlignment;
import com.univocity.parsers.fixed.FixedWidthFields;
import com.univocity.parsers.fixed.FixedWidthRoutines;
import com.univocity.parsers.fixed.FixedWidthWriterSettings;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * Generates fixed width output
 *
 * @author Timothy Anyona
 */
public class FixedWidthOutput {

	//https://stackoverflow.com/questions/8669967/java-fixed-width-file-format-read-write-library
	//https://github.com/uniVocity/univocity-parsers/blob/master/src/test/java/com/univocity/parsers/examples/FixedWidthWriterExamples.java
	private static final Logger logger = LoggerFactory.getLogger(FixedWidthOutput.class);

	/**
	 * Generates fixed width output for data in the given resultset. The
	 * resultset and writer get closed by the method.
	 *
	 * @param rs the resultset that contains the data to output
	 * @param outputWriter the writer to output to
	 * @param options options that determine the format of the output
	 */
	public void generateOutput(ResultSet rs, Writer outputWriter, FixedWidthOptions options) throws SQLException {
		logger.debug("Entering generateOutput");

		ObjectRowWriterProcessor processor = new ObjectRowWriterProcessor();

		String dateFormat = options.getDateFormat();
		String dateTimeFormat = options.getDateTimeFormat();
		String numberFormat = options.getNumberFormat();

		logger.debug("dateFormat='{}'", dateFormat);
		logger.debug("dateTimeFormat='{}'", dateTimeFormat);

		if (StringUtils.isNotBlank(dateFormat)) {
			processor.convertType(java.sql.Date.class, Conversions.toDate(dateFormat));
		}
		
		if (StringUtils.isNotBlank(dateTimeFormat)) {
			processor.convertType(java.sql.Timestamp.class, Conversions.toDate(dateTimeFormat));
		}
		
		if (StringUtils.isNotBlank(numberFormat)) {
			processor.convertType(java.lang.Integer.class, Conversions.formatToNumber(java.lang.Integer.class, numberFormat));
			processor.convertType(java.lang.Long.class, Conversions.formatToNumber(java.lang.Long.class, numberFormat));
			processor.convertType(java.lang.Double.class, Conversions.formatToNumber(java.lang.Double.class, numberFormat));
		}
		
		List<Map<String,List<String>>> fieldNumberFormats=options.getFieldNumberFormats();
		if(!CollectionUtils.isEmpty(fieldNumberFormats)){
			for(Map<String,List<String>> numberFormatDefinition: fieldNumberFormats){
				Entry<String, List<String>> entry = numberFormatDefinition.entrySet().iterator().next();
				String fieldNumberFormat = entry.getKey();
				List<String> fieldNames = entry.getValue();
				processor.convertFields(Conversions.formatToNumber(fieldNumberFormat)).set(fieldNames);
			}
		}
		
		List<Map<String,List<String>>> fieldIntegerFormats=options.getFieldIntegerFormats();
		if(!CollectionUtils.isEmpty(fieldIntegerFormats)){
			for(Map<String,List<String>> integerFormatDefinition: fieldIntegerFormats){
				Entry<String, List<String>> entry = integerFormatDefinition.entrySet().iterator().next();
				String fieldIntegerFormat = entry.getKey();
				List<String> fieldNames = entry.getValue();
				processor.convertFields(Conversions.formatToNumber(java.lang.Integer.class, fieldIntegerFormat)).set(fieldNames);
			}
		}
		
		List<Map<String,List<String>>> fieldLongFormats=options.getFieldLongFormats();
		if(!CollectionUtils.isEmpty(fieldLongFormats)){
			for(Map<String,List<String>> longFormatDefinition: fieldLongFormats){
				Entry<String, List<String>> entry = longFormatDefinition.entrySet().iterator().next();
				String fieldLongFormat = entry.getKey();
				List<String> fieldNames = entry.getValue();
				processor.convertFields(Conversions.formatToNumber(java.lang.Long.class, fieldLongFormat)).set(fieldNames);
			}
		}
		
		List<Map<String,List<String>>> fieldDoubleFormats=options.getFieldDoubleFormats();
		if(!CollectionUtils.isEmpty(fieldDoubleFormats)){
			for(Map<String,List<String>> doubleFormatDefinition: fieldDoubleFormats){
				Entry<String, List<String>> entry = doubleFormatDefinition.entrySet().iterator().next();
				String fieldDoubleFormat = entry.getKey();
				List<String> fieldNames = entry.getValue();
				processor.convertFields(Conversions.formatToNumber(java.lang.Double.class, fieldDoubleFormat)).set(fieldNames);
			}
		}
		
		List<Map<String,List<String>>> fieldDateFormats=options.getFieldDateFormats();
		if(!CollectionUtils.isEmpty(fieldDateFormats)){
			for(Map<String,List<String>> dateFormatDefinition: fieldDateFormats){
				Entry<String, List<String>> entry = dateFormatDefinition.entrySet().iterator().next();
				String fieldDateFormat = entry.getKey();
				List<String> fieldNames = entry.getValue();
				processor.convertFields(Conversions.toDate(fieldDateFormat)).set(fieldNames);
			}
		}

		FixedWidthFields fields;

		List<Integer> fieldLengths = options.getFieldLengths();
		List<Map<String, Integer>> fieldLengthsByName = options.getFieldLengthsByName();

		ResultSetMetaData rsmd = rs.getMetaData();

		if (!CollectionUtils.isEmpty(fieldLengths)) {
			//https://stackoverflow.com/questions/718554/how-to-convert-an-arraylist-containing-integers-to-primitive-int-array
			int[] fieldLengthsArray = ArrayUtils.toPrimitive(options.getFieldLengths().toArray(new Integer[0]));

			List<String> columnNames = new ArrayList<>();
			for (int i = 1; i <= fieldLengthsArray.length; i++) {
				columnNames.add(rsmd.getColumnLabel(i));
			}

			String[] headers = columnNames.toArray(new String[0]);

			fields = new FixedWidthFields(headers, fieldLengthsArray);
		} else if (!CollectionUtils.isEmpty(fieldLengthsByName)) {
			fields = new FixedWidthFields();
			//addField() will just add fields in the order added, which may not be the order in the resultset
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String columnName = rsmd.getColumnLabel(i);
				for (Map<String, Integer> fieldLengthDefinition : fieldLengthsByName) {
					//https://stackoverflow.com/questions/1509391/how-to-get-the-one-entry-from-hashmap-without-iterating
					// Get the first entry that the iterator returns
					Entry<String, Integer> entry = fieldLengthDefinition.entrySet().iterator().next();
					String fieldName = entry.getKey();
					Integer fieldLength = entry.getValue();
					if (StringUtils.equalsIgnoreCase(columnName, fieldName)) {
						fields.addField(fieldName, fieldLength);
						break;
					}
				}
			}
		} else {
			throw new IllegalStateException("fieldLengths or fieldLengthsByName not defined");
		}

		List<Map<String, List<String>>> fieldAlignmentByName = options.getFieldAlignmentByName();
		if (!CollectionUtils.isEmpty(fieldAlignmentByName)) {
			for (Map<String, List<String>> alignmentDefinition : fieldAlignmentByName) {
				// Get the first entry that the iterator returns
				Entry<String, List<String>> entry = alignmentDefinition.entrySet().iterator().next();
				String alignment = entry.getKey();
				List<String> fieldNames = entry.getValue();
				String[] fieldNamesArray = fieldNames.toArray(new String[0]);
				if (StringUtils.equalsIgnoreCase(alignment, "left")) {
					fields.setAlignment(FieldAlignment.LEFT, fieldNamesArray);
				} else if (StringUtils.equalsIgnoreCase(alignment, "right")) {
					fields.setAlignment(FieldAlignment.RIGHT, fieldNamesArray);
				} else if (StringUtils.equalsIgnoreCase(alignment, "center")) {
					fields.setAlignment(FieldAlignment.CENTER, fieldNamesArray);
				} else {
					throw new IllegalArgumentException("Invalid field alignment: " + alignment);
				}
			}
		}

		List<Map<String, List<Integer>>> fieldAlignmentByPosition = options.getFieldAlignmentByPosition();
		if (!CollectionUtils.isEmpty(fieldAlignmentByPosition)) {
			for (Map<String, List<Integer>> alignmentDefinition : fieldAlignmentByPosition) {
				// Get the first entry that the iterator returns
				Entry<String, List<Integer>> entry = alignmentDefinition.entrySet().iterator().next();
				String alignment = entry.getKey();
				List<Integer> fieldPositions = entry.getValue();
				int[] fieldPositionsArray = ArrayUtils.toPrimitive(fieldPositions.toArray(new Integer[0]));
				if (StringUtils.equalsIgnoreCase(alignment, "left")) {
					fields.setAlignment(FieldAlignment.LEFT, fieldPositionsArray);
				} else if (StringUtils.equalsIgnoreCase(alignment, "right")) {
					fields.setAlignment(FieldAlignment.RIGHT, fieldPositionsArray);
				} else if (StringUtils.equalsIgnoreCase(alignment, "center")) {
					fields.setAlignment(FieldAlignment.CENTER, fieldPositionsArray);
				} else {
					throw new IllegalArgumentException("Invalid field alignment: " + alignment);
				}
			}
		}

		List<Map<Character, List<String>>> fieldPaddingByName = options.getFieldPaddingByName();
		if (!CollectionUtils.isEmpty(fieldPaddingByName)) {
			for (Map<Character, List<String>> paddingDefinition : fieldPaddingByName) {
				// Get the first entry that the iterator returns
				Entry<Character, List<String>> entry = paddingDefinition.entrySet().iterator().next();
				Character padding = entry.getKey();
				List<String> fieldNames = entry.getValue();
				String[] fieldNamesArray = fieldNames.toArray(new String[0]);
				fields.setPadding(padding, fieldNamesArray);
			}
		}

		List<Map<Character, List<Integer>>> fieldPaddingByPosition = options.getFieldPaddingByPosition();
		if (!CollectionUtils.isEmpty(fieldPaddingByPosition)) {
			for (Map<Character, List<Integer>> paddingDefinition : fieldPaddingByPosition) {
				// Get the first entry that the iterator returns
				Entry<Character, List<Integer>> entry = paddingDefinition.entrySet().iterator().next();
				Character padding = entry.getKey();
				List<Integer> fieldPositions = entry.getValue();
				int[] fieldPositionsArray = ArrayUtils.toPrimitive(fieldPositions.toArray(new Integer[0]));
				fields.setPadding(padding, fieldPositionsArray);
			}
		}

		FixedWidthWriterSettings writerSettings = new FixedWidthWriterSettings(fields);

		writerSettings.setRowWriterProcessor(processor);
		writerSettings.setHeaderWritingEnabled(options.isIncludeHeaders());
		writerSettings.getFormat().setPadding(options.getPadding());
		writerSettings.setUseDefaultPaddingForHeaders(options.isUseDefaultPaddingForHeaders());

		String defaultAlignmentForHeaders = options.getDefaultAlignmentForHeaders();
		if (StringUtils.isNotBlank(defaultAlignmentForHeaders)) {
			if (StringUtils.equalsIgnoreCase(defaultAlignmentForHeaders, "left")) {
				writerSettings.setDefaultAlignmentForHeaders(FieldAlignment.LEFT);
			} else if (StringUtils.equalsIgnoreCase(defaultAlignmentForHeaders, "right")) {
				writerSettings.setDefaultAlignmentForHeaders(FieldAlignment.RIGHT);
			} else if (StringUtils.equalsIgnoreCase(defaultAlignmentForHeaders, "center")) {
				writerSettings.setDefaultAlignmentForHeaders(FieldAlignment.CENTER);
			} else {
				throw new IllegalArgumentException("Invalid defaultAlignmentForHeaders: " + defaultAlignmentForHeaders);
			}
		}

		FixedWidthRoutines routines = new FixedWidthRoutines(writerSettings);
		routines.write(rs, outputWriter);
	}

}
