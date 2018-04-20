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
package art.reportoptions;

import art.enums.ReportType;
import java.util.List;
import java.util.Map;

/**
 * Represents report options for mongodb reports
 * 
 * @author Timothy Anyona
 */
public class MongoDbOptions extends DataTablesOptions {
	
	private static final long serialVersionUID = 1L;
	private List<String> columns;
	private List<Map<String, String>> columnDataTypes;
	private ReportType reportType = ReportType.DataTables;

	/**
	 * @return the columns
	 */
	public List<String> getColumns() {
		return columns;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	/**
	 * @return the columnDataTypes
	 */
	public List<Map<String, String>> getColumnDataTypes() {
		return columnDataTypes;
	}

	/**
	 * @param columnDataTypes the columnDataTypes to set
	 */
	public void setColumnDataTypes(List<Map<String, String>> columnDataTypes) {
		this.columnDataTypes = columnDataTypes;
	}

	/**
	 * @return the reportType
	 */
	public ReportType getReportType() {
		return reportType;
	}

	/**
	 * @param reportType the reportType to set
	 */
	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}
}
