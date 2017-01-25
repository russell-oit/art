/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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

/**
 * Represents a portlet that is contained within a dashboard
 * 
 * @author Timothy Anyona
 */
public class Portlet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String id;
	private String url;
	private String classNamePrefix;
	private String title;
	private boolean executeOnLoad;
	private int refreshPeriodSeconds;
	private int index;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the classNamePrefix
	 */
	public String getClassNamePrefix() {
		return classNamePrefix;
	}

	/**
	 * @param classNamePrefix the classNamePrefix to set
	 */
	public void setClassNamePrefix(String classNamePrefix) {
		this.classNamePrefix = classNamePrefix;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the executeOnLoad
	 */
	public boolean isExecuteOnLoad() {
		return executeOnLoad;
	}

	/**
	 * @param executeOnLoad the executeOnLoad to set
	 */
	public void setExecuteOnLoad(boolean executeOnLoad) {
		this.executeOnLoad = executeOnLoad;
	}

	/**
	 * @return the refreshPeriodSeconds
	 */
	public int getRefreshPeriodSeconds() {
		return refreshPeriodSeconds;
	}

	/**
	 * @param refreshPeriodSeconds the refreshPeriodSeconds to set
	 */
	public void setRefreshPeriodSeconds(int refreshPeriodSeconds) {
		this.refreshPeriodSeconds = refreshPeriodSeconds;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
}
