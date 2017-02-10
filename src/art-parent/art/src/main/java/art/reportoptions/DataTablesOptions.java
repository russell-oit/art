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

/**
 * Represents report options for datatables reports
 *
 * @author Timothy Anyona
 */
public class DataTablesOptions {

	private boolean showColumnFilters = true;

	/**
	 * @return the showColumnFilters
	 */
	public boolean isShowColumnFilters() {
		return showColumnFilters;
	}

	/**
	 * @param showColumnFilters the showColumnFilters to set
	 */
	public void setShowColumnFilters(boolean showColumnFilters) {
		this.showColumnFilters = showColumnFilters;
	}
}
