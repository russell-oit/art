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
 * Represents cache types
 *
 * @author Timothy Anyona
 */
public enum CacheType {

	JPivot("JPivot"), Saiku("Saiku"), Reports("Reports"), ReportGroups("ReportGroups"),
	Users("Users"), UserGroups("UserGroups"), Datasources("Datasources"),
	Schedules("Schedules"), Jobs("Jobs"), Rules("Rules"), Parameters("Parameters"),
	FtpServers("FtpServers");
	
	private final String value;

	private CacheType(String value) {
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
	public static List<CacheType> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<CacheType> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, null is returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value, null otherwise
	 */
	public static CacheType toEnum(String value) {
		return toEnum(value, null);
	}

	/**
	 * Convert a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
	 */
	public static CacheType toEnum(String value, CacheType defaultEnum) {
		for (CacheType v : values()) {
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
		return "cacheType.option." + value;
	}
}
