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
package art.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the list of tabs that should be used to display items in a tabbed
 * dashboard
 *
 * @author Timothy Anyona
 */
public class DashboardTabList implements Serializable {

	private static final long serialVersionUID = 1L;
	private int defaultTab;
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

	public List<DashboardItem> getAllItems() {
		List<DashboardItem> allItems = new ArrayList<>();
		
		for (DashboardTab tab : tabs) {
			allItems.addAll(tab.getItems());
		}

		return allItems;
	}
}
