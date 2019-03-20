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
package art.reportoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Represents report options for reports which use groovy
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroovyOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<String> columns;
	private List<Map<String, String>> columnDataTypes;
	private List<Map<String, String>> columnLabels;

	/**
	 * @return the columnLabels
	 */
	public List<Map<String, String>> getColumnLabels() {
		return columnLabels;
	}

	/**
	 * @param columnLabels the columnLabels to set
	 */
	public void setColumnLabels(List<Map<String, String>> columnLabels) {
		this.columnLabels = columnLabels;
	}

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

}
