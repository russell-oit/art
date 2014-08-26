/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
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
package art.enums;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;

/**
 * Enum to represent parameter data types
 *
 * @author Timothy Anyona
 */
public enum ParameterDataType {

	Varchar("Varchar"), Text("Text"), Integer("Integer"), Number("Number"),
	Date("Date"), DateTime("DateTime"), Datasource("Datasource");
	private String value;

	private ParameterDataType(String value) {
		this.value = value;
	}

	/**
	 * Determine if this data type contains numeric values
	 *
	 * @return
	 */
	public boolean isNumeric() {
		if (this == Integer || this == Number || this == Datasource) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determine if this data type contains date values
	 *
	 * @return
	 */
	public boolean isDate() {
		if (this == Date || this == DateTime) {
			return true;
		} else {
			return false;
		}
	}

	public String processDefaultValue(String value) {
		String processedValue;

		switch (this) {
			case Integer:
			case Number:
			case Datasource:

				break;
			case Date:
			case DateTime:
				processedValue = processDefaultDateValue(value);
				break;
			case Varchar:
			case Text:
				processedValue = value;
		}

		return processedValue;
	}

	/**
	 * Get enum value
	 *
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get a list of all enum values
	 *
	 * @return
	 */
	public static List<ParameterDataType> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<ParameterDataType> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Convert a value to an enum. If the conversion fails, Varchar is returned
	 *
	 * @param value
	 * @return
	 */
	public static ParameterDataType toEnum(String value) {
		return toEnum(value, Varchar);
	}

	/**
	 * Convert a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value
	 * @param defaultEnum
	 * @return
	 */
	public static ParameterDataType toEnum(String value, ParameterDataType defaultEnum) {
		for (ParameterDataType v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return defaultEnum;
	}

	/**
	 * Get enum description. In case description needs to be different from
	 * internal value
	 *
	 * @return
	 */
	public String getDescription() {
		return value;
	}

	private String processDefaultNumericValue(String value) {
		if (value == null) {
			//return null to allow use of null values for report queries
			
			//null doesn't really make sense for select queries.
			//col = null always returns false so select must have explicit is null statement
			//query could be for an update report, with an insert that can potentially take nulls?
			//or query could have multiple resultsets, with some being inserts that can take nulls?
			//null can be used with dynamic sql tags? isnull operator?
			return null;
		} else if (StringUtils.isBlank(value)) {
			return "0";
		} else {
			return value;
		}
	}

	private String processDefaultStringValue(String value) {
		return value;
	}

	private String processDefaultDateValue(String value) {
		/*
		 * if default value has syntax "ADD DAYS|MONTHS|YEARS <integer>" or "Add
		 * day|MoN|Year <integer>" set default value as sysdate plus an offset
		 */

		if (value == null) {
			return null;
		}

		if (value.toUpperCase().startsWith("ADD")) { // set an offset from today
			Calendar calendar = new GregorianCalendar();
			try {
				StringTokenizer st = new StringTokenizer(value.toUpperCase(), " ");
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
		if (value.length() < 10) {
			dateFormat = "yyyy-M-d";
		} else if (value.length() == 10) {
			dateFormat = "yyyy-MM-dd";
		} else if (value.length() == 16) {
			dateFormat = "yyyy-MM-dd HH:mm";
		} else {
			dateFormat = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		dateFormatter.setLenient(false); //don't allow invalid date strings to be coerced into valid dates

		java.util.Date dateValue;
		try {
			dateValue = dateFormatter.parse(value);
		} catch (ParseException e) {
			logger.debug("Defaulting {} to now", value, e);
			//string could not be converted to a valid date. default to now
			dateValue = new java.util.Date();
		}

		//return date
		return dateValue;

	}

}
