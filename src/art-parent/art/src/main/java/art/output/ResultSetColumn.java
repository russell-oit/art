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
package art.output;

import art.enums.SqlColumnType;
import java.io.Serializable;

/**
 * Provides details of a resultset column
 *
 * @author Timothy Anyona
 */
public class ResultSetColumn implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private SqlColumnType type;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public SqlColumnType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(SqlColumnType type) {
		this.type = type;
	}
}
