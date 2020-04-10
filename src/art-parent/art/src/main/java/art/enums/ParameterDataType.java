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
package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents parameter data types
 *
 * @author Timothy Anyona
 */
public enum ParameterDataType {

	Varchar("Varchar"), Text("Text"), Integer("Integer"), Double("Double"),
	Date("Date"), DateTime("DateTime"), DateRange("DateRange"),
	Datasource("Datasource"), File("File");
	
	private final String value;

	private ParameterDataType(String value) {
		this.value = value;
	}

	/**
	 * Returns <code>true</code> if this data type represents numeric values
	 *
	 * @return <code>true</code> if this data type represents numeric values
	 */
	public boolean isNumeric() {
		switch(this){
			case Integer:
			case Double:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns <code>true</code> if this data type contains date values
	 *
	 * @return <code>true</code> if this data type contains date values
	 */
	public boolean isDate() {
		switch(this){
			case Date:
			case DateTime:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns this enum option's value
	 *
	 * @return this enum option's value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns all enum options
	 *
	 * @return all enum options
	 */
	public static List<ParameterDataType> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<ParameterDataType> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, Varchar is returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static ParameterDataType toEnum(String value) {
		return toEnum(value, Varchar);
	}

	/**
	 * Converts a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
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
	 * Returns this enum option's description
	 *
	 * @return this enum option's description
	 */
	public String getDescription() {
		return value;
	}
}
