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
 * Represents report options for view reports
 * 
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViewOptions implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private List<String> omitColumns;
	private List<Map<String, String>> columnLabels;
	private List<Map<String, String>> columnDescriptions;

	/**
	 * @return the columnDescriptions
	 */
	public List<Map<String, String>> getColumnDescriptions() {
		return columnDescriptions;
	}

	/**
	 * @param columnDescriptions the columnDescriptions to set
	 */
	public void setColumnDescriptions(List<Map<String, String>> columnDescriptions) {
		this.columnDescriptions = columnDescriptions;
	}

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
	 * @return the omitColumns
	 */
	public List<String> getOmitColumns() {
		return omitColumns;
	}

	/**
	 * @param omitColumns the omitColumns to set
	 */
	public void setOmitColumns(List<String> omitColumns) {
		this.omitColumns = omitColumns;
	}
	
}
