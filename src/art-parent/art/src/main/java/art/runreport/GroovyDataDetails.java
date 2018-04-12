/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.runreport;

import art.output.ColumnTypeDefinition;
import art.output.ResultSetColumn;
import java.util.List;
import java.util.Map;

/**
 * Represents attributes of data used for report generation, when using groovy
 * data and not a resultset
 *
 * @author Timothy Anyona
 */
public class GroovyDataDetails {

	private int rowCount;
	private int colCount;
	private List<String> columnNames;
	private List<? extends Object> dataList;
	private Map<Integer, ColumnTypeDefinition> columnTypes;
	private List<ResultSetColumn> resultSetColumns;
	
	/**
	 * @return the rowCount
	 */
	public int getRowCount() {
		return rowCount;
	}

	/**
	 * @param rowCount the rowCount to set
	 */
	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	/**
	 * @return the colCount
	 */
	public int getColCount() {
		return colCount;
	}

	/**
	 * @param colCount the colCount to set
	 */
	public void setColCount(int colCount) {
		this.colCount = colCount;
	}

	/**
	 * @return the columnNames
	 */
	public List<String> getColumnNames() {
		return columnNames;
	}

	/**
	 * @param columnNames the columnNames to set
	 */
	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	/**
	 * @return the dataList
	 */
	public List<? extends Object> getDataList() {
		return dataList;
	}

	/**
	 * @param dataList the dataList to set
	 */
	public void setDataList(List<? extends Object> dataList) {
		this.dataList = dataList;
	}

	/**
	 * @return the columnTypes
	 */
	public Map<Integer, ColumnTypeDefinition> getColumnTypes() {
		return columnTypes;
	}

	/**
	 * @param columnTypes the columnTypes to set
	 */
	public void setColumnTypes(Map<Integer, ColumnTypeDefinition> columnTypes) {
		this.columnTypes = columnTypes;
	}

	/**
	 * @return the resultSetColumns
	 */
	public List<ResultSetColumn> getResultSetColumns() {
		return resultSetColumns;
	}

	/**
	 * @param resultSetColumns the resultSetColumns to set
	 */
	public void setResultSetColumns(List<ResultSetColumn> resultSetColumns) {
		this.resultSetColumns = resultSetColumns;
	}

}
