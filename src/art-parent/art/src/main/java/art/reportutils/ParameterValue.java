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

package art.reportutils;

import java.util.Objects;

/**
 * Class to represent a report parameter value
 * 
 * @author Timothy Anyona
 */
public class ParameterValue {
	private Object value;
	private JdbcValue jdbcValue;

	public ParameterValue(Object value, JdbcValue jdbcValue) {
		Objects.requireNonNull(jdbcValue, "jdbcValue must not be null");
		
		this.value = value;
		this.jdbcValue = jdbcValue;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the jdbcValue
	 */
	public JdbcValue getJdbcValue() {
		return jdbcValue;
	}
}
