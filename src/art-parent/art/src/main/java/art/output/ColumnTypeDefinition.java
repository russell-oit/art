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
package art.output;

import art.enums.ColumnType;

/**
 * Represents a description of the column type of a resultset column
 * 
 * @author Timothy Anyona
 */
public class ColumnTypeDefinition {
	
	private ColumnType columnType;
	private int sqlType;

	/**
	 * @return the columnType
	 */
	public ColumnType getColumnType() {
		return columnType;
	}

	/**
	 * @param columnType the columnType to set
	 */
	public void setColumnType(ColumnType columnType) {
		this.columnType = columnType;
	}

	/**
	 * @return the sqlType
	 */
	public int getSqlType() {
		return sqlType;
	}

	/**
	 * @param sqlType the sqlType to set
	 */
	public void setSqlType(int sqlType) {
		this.sqlType = sqlType;
	}
}
