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
package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents page orientation to use e.g with pdf output
 *
 * @author Timothy Anyona
 */
public enum PageOrientation {

	Portrait("Portrait"), Landscape("Landscape");

	private final String value;

	private PageOrientation(String value) {
		this.value = value;
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
	public static List<PageOrientation> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<PageOrientation> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, an exception is thrown
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static PageOrientation toEnum(String value) {
		for (PageOrientation v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		//https://dzone.com/articles/java-string-format-examples
		throw new IllegalArgumentException("Invalid page orientation: " + value);
	}

	/**
	 * Convert a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
	 */
	public static PageOrientation toEnum(String value, PageOrientation defaultEnum) {
		for (PageOrientation v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		return defaultEnum;
	}

	/**
	 * Returns this enum option's description
	 *
	 * @return enum option description
	 */
	public String getDescription() {
		return value;
	}

	/**
	 * Returns this enum option's i18n message string for use in the user
	 * interface
	 *
	 * @return this enum option's i18n message string
	 */
	public String getLocalizedDescription() {
		return "pageOrientation.option." + value;
	}
}
