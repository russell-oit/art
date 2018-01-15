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
import java.util.List;

/**
 * Options for reportengine reports
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportEngineOptions {

	private boolean sortValues;
	private String url;
	private String separator = ",";
	private List<ReportEngineGroupColumn> groupColumns;
	private Boolean showTotals;
	private Boolean showGrandTotal;
	private List<ReportEngineDataColumn> dataColumns;
	private boolean pivot;
	private List<String> pivotHeaderRows;
	private ReportEngineDataColumn pivotData;
	private boolean firstLineIsHeader = true;
	private int columnCount;

	/**
	 * @return the sortValues
	 */
	public boolean isSortValues() {
		return sortValues;
	}

	/**
	 * @param sortValues the sortValues to set
	 */
	public void setSortValues(boolean sortValues) {
		this.sortValues = sortValues;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the separator
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * @param separator the separator to set
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * @return the groupColumns
	 */
	public List<ReportEngineGroupColumn> getGroupColumns() {
		return groupColumns;
	}

	/**
	 * @param groupColumns the groupColumns to set
	 */
	public void setGroupColumns(List<ReportEngineGroupColumn> groupColumns) {
		this.groupColumns = groupColumns;
	}

	/**
	 * @return the showTotals
	 */
	public Boolean getShowTotals() {
		return showTotals;
	}

	/**
	 * @param showTotals the showTotals to set
	 */
	public void setShowTotals(Boolean showTotals) {
		this.showTotals = showTotals;
	}

	/**
	 * @return the showGrandTotal
	 */
	public Boolean getShowGrandTotal() {
		return showGrandTotal;
	}

	/**
	 * @param showGrandTotal the showGrandTotal to set
	 */
	public void setShowGrandTotal(Boolean showGrandTotal) {
		this.showGrandTotal = showGrandTotal;
	}

	/**
	 * @return the dataColumns
	 */
	public List<ReportEngineDataColumn> getDataColumns() {
		return dataColumns;
	}

	/**
	 * @param dataColumns the dataColumns to set
	 */
	public void setDataColumns(List<ReportEngineDataColumn> dataColumns) {
		this.dataColumns = dataColumns;
	}

	/**
	 * @return the pivot
	 */
	public boolean isPivot() {
		return pivot;
	}

	/**
	 * @param pivot the pivot to set
	 */
	public void setPivot(boolean pivot) {
		this.pivot = pivot;
	}

	/**
	 * @return the pivotHeaderRows
	 */
	public List<String> getPivotHeaderRows() {
		return pivotHeaderRows;
	}

	/**
	 * @param pivotHeaderRows the pivotHeaderRows to set
	 */
	public void setPivotHeaderRows(List<String> pivotHeaderRows) {
		this.pivotHeaderRows = pivotHeaderRows;
	}

	/**
	 * @return the pivotData
	 */
	public ReportEngineDataColumn getPivotData() {
		return pivotData;
	}

	/**
	 * @param pivotData the pivotData to set
	 */
	public void setPivotData(ReportEngineDataColumn pivotData) {
		this.pivotData = pivotData;
	}

	/**
	 * @return the firstLineIsHeader
	 */
	public boolean isFirstLineIsHeader() {
		return firstLineIsHeader;
	}

	/**
	 * @param firstLineIsHeader the firstLineIsHeader to set
	 */
	public void setFirstLineIsHeader(boolean firstLineIsHeader) {
		this.firstLineIsHeader = firstLineIsHeader;
	}

	/**
	 * @return the columnCount
	 */
	public int getColumnCount() {
		return columnCount;
	}

	/**
	 * @param columnCount the columnCount to set
	 */
	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

}
