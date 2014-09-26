/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.runreport;

/**
 * Class to represent a value to be used when calling preparedstatement
 * setObject method. Includes type information to properly handle a null values.
 * Some drivers throw an exception if a null is passed without type information.
 *
 * @author Timothy Anyona
 */
public class JdbcValue {

	private final Object value;
	private final int sqlType; //one of java.sql.Types constants

	public JdbcValue(Object value, int sqlType) {
		this.value = value;
		this.sqlType = sqlType;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the sqlType
	 */
	public int getSqlType() {
		return sqlType;
	}
}
