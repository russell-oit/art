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

import java.util.List;

/**
 * Represents options for tabular heatmap reports
 * 
 * @author Timothy Anyona
 */
public class TabularHeatmapOptions extends GeneralReportOptions {
	
	private List<Integer> columns;
	private List<String> colors;
	private String nullColor;
	private boolean perColumn;

	/**
	 * @return the columns
	 */
	public List<Integer> getColumns() {
		return columns;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(List<Integer> columns) {
		this.columns = columns;
	}

	/**
	 * @return the colors
	 */
	public List<String> getColors() {
		return colors;
	}

	/**
	 * @param colors the colors to set
	 */
	public void setColors(List<String> colors) {
		this.colors = colors;
	}

	/**
	 * @return the nullColor
	 */
	public String getNullColor() {
		return nullColor;
	}

	/**
	 * @param nullColor the nullColor to set
	 */
	public void setNullColor(String nullColor) {
		this.nullColor = nullColor;
	}

	/**
	 * @return the perColumn
	 */
	public boolean isPerColumn() {
		return perColumn;
	}

	/**
	 * @param perColumn the perColumn to set
	 */
	public void setPerColumn(boolean perColumn) {
		this.perColumn = perColumn;
	}
	
}
