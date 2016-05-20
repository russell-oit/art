/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the supported connection pool libraries
 *
 * @author Timothy Anyona
 */
public enum ConnectionPoolLibrary {

	ArtDBCP("ART-DBCP"), HikariCP("HikariCP");
	
	private final String value;

	private ConnectionPoolLibrary(String value) {
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
	public static List<ConnectionPoolLibrary> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<ConnectionPoolLibrary> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the value doesn't represent a known enum,
	 * an IllegalArgumentException is thrown
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static ConnectionPoolLibrary toEnum(String value) {
		for (ConnectionPoolLibrary v : values()) {
			if (v.value.equalsIgnoreCase(value)) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid enum value: " + value);
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
