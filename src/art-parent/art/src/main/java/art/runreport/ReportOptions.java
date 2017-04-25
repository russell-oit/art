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
package art.runreport;

import java.io.Serializable;

/**
 * Represents report options used when running a report
 *
 * @author Timothy Anyona
 */
public class ReportOptions implements Serializable {

	private static final long serialVersionUID = 1L;
	private boolean showSelectedParameters;
	private int splitColumn;
	private boolean showSql;
	private boolean prettyPrint;

	/**
	 * @return the prettyPrint
	 */
	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	/**
	 * @param prettyPrint the prettyPrint to set
	 */
	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	/**
	 * @return the showSql
	 */
	public boolean isShowSql() {
		return showSql;
	}

	/**
	 * @param showSql the showSql to set
	 */
	public void setShowSql(boolean showSql) {
		this.showSql = showSql;
	}

	/**
	 * @return the splitColumn
	 */
	public int getSplitColumn() {
		return splitColumn;
	}

	/**
	 * @param splitColumn the splitColumn to set
	 */
	public void setSplitColumn(int splitColumn) {
		this.splitColumn = splitColumn;
	}

	/**
	 * @return the showSelectedParameters
	 */
	public boolean isShowSelectedParameters() {
		return showSelectedParameters;
	}

	/**
	 * @param showSelectedParameters the showSelectedParameters to set
	 */
	public void setShowSelectedParameters(boolean showSelectedParameters) {
		this.showSelectedParameters = showSelectedParameters;
	}
}
