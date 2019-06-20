/*
 * ART. A Reporting Tool.
 * Copyright (C) 2019 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.report.rest;

/**
 * Response object for rest run report
 * 
 * @author Timothy Anyona
 */
public class RunReportResponseObject {
	
	private String fileName;
	private String url;
	private Integer rowsRetrieved;
	private Integer rowsUpdated;

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
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
	 * @return the rowsUpdated
	 */
	public Integer getRowsUpdated() {
		return rowsUpdated;
	}

	/**
	 * @param rowsUpdated the rowsUpdated to set
	 */
	public void setRowsUpdated(Integer rowsUpdated) {
		this.rowsUpdated = rowsUpdated;
	}

	/**
	 * @return the rowsRetrieved
	 */
	public Integer getRowsRetrieved() {
		return rowsRetrieved;
	}

	/**
	 * @param rowsRetrieved the rowsRetrieved to set
	 */
	public void setRowsRetrieved(Integer rowsRetrieved) {
		this.rowsRetrieved = rowsRetrieved;
	}
	
}
