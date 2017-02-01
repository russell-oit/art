/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a dashboard, whose details are displayed in a dashboard report
 *
 * @author Timothy Anyona
 */
public class Dashboard extends AbstractDashboard {

	private static final long serialVersionUID = 1L;
	private List<List<Portlet>> columns;

	/**
	 * @return the columns
	 */
	public List<List<Portlet>> getColumns() {
		return columns;
	}

	/**
	 * @param columns the columns to set
	 */
	public void setColumns(List<List<Portlet>> columns) {
		this.columns = columns;
	}

	public List<Portlet> getAllPortlets() {
		List<Portlet> allPortlets = new ArrayList<>();

		for (List<Portlet> column : columns) {
			allPortlets.addAll(column);
		}

		return allPortlets;
	}
}
