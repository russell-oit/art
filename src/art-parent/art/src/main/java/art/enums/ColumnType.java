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

import org.apache.commons.lang3.StringUtils;

/**
 * Represents database column types used in report output generation
 *
 * @author Timothy Anyona
 */
public enum ColumnType {

	Numeric, Date, Clob, String, Other, Binary, Time;

	/**
	 * Converts a value to an enum. If the conversion fails, String is returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value, String otherwise
	 */
	public static ColumnType toEnum(String value) {
		for (ColumnType v : values()) {
			if (v.toString().equalsIgnoreCase(value)) {
				return v;
			}
		}
		if (StringUtils.equalsIgnoreCase(value, "datetime")) {
			return Date;
		}
		return String;
	}
}
