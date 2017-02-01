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
package art.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents user access levels
 *
 * @author Timothy Anyona
 */
public enum AccessLevel {

	NormalUser(0), ScheduleUser(5), JuniorAdmin(10), MidAdmin(30),
	StandardAdmin(40), SeniorAdmin(80), SuperAdmin(100), RepositoryUser(-1);
	
	private final int value;

	private AccessLevel(int value) {
		this.value = value;
	}

	/**
	 * Returns this enum option's value
	 *
	 * @return this enum option's value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Returns all enum options
	 *
	 * @return all enum options
	 */
	public static List<AccessLevel> list() {
		//use a new list as Arrays.asList() returns a fixed-size list. can't add or remove from it
		List<AccessLevel> items = new ArrayList<>();
		items.addAll(Arrays.asList(values()));
		return items;
	}

	/**
	 * Converts a value to an enum. If the conversion fails, NormalUser is
	 * returned
	 *
	 * @param value the value to convert
	 * @return the enum option that corresponds to the value
	 */
	public static AccessLevel toEnum(int value) {
		return toEnum(value, NormalUser);
	}

	/**
	 * Converts a value to an enum. If the conversion fails, the specified
	 * default is returned
	 *
	 * @param value the value to convert
	 * @param defaultEnum the default enum option to use
	 * @return the enum option that corresponds to the value
	 */
	public static AccessLevel toEnum(int value, AccessLevel defaultEnum) {
		for (AccessLevel v : values()) {
			if (v.value == value) {
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
		switch (this) {
			case NormalUser:
				return "Normal User";
			case ScheduleUser:
				return "Schedule User";
			case JuniorAdmin:
				return "Junior Admin";
			case MidAdmin:
				return "Mid Admin";
			case StandardAdmin:
				return "Standard Admin";
			case SeniorAdmin:
				return "Senior Admin";
			case SuperAdmin:
				return "Super Admin";
			case RepositoryUser:
				return "Repository User";
			default:
				return this.name();
		}
	}
}
