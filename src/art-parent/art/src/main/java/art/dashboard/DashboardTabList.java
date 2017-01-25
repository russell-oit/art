/*
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.dashboard;

import java.io.Serializable;
import java.util.List;

/**
 * Represents the list of tabs that should be used to display items in a tabbed
 * dashboard
 *
 * @author Timothy Anyona
 */
public class DashboardTabList implements Serializable {

	private static final long serialVersionUID = 1L;
	private int defaultTab = 1;
	private List<DashboardTab> tabs;

	/**
	 * @return the defaultTab
	 */
	public int getDefaultTab() {
		return defaultTab;
	}

	/**
	 * @param defaultTab the defaultTab to set
	 */
	public void setDefaultTab(int defaultTab) {
		this.defaultTab = defaultTab;
	}

	/**
	 * @return the tabs
	 */
	public List<DashboardTab> getTabs() {
		return tabs;
	}

	/**
	 * @param tabs the tabs to set
	 */
	public void setTabs(List<DashboardTab> tabs) {
		this.tabs = tabs;
	}
}
