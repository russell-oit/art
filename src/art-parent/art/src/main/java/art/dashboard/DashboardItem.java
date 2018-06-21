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

/**
 * Represents common properties of an item in a regular or gridstack dashboard
 *
 * @author Timothy Anyona
 */
public abstract class DashboardItem implements Serializable {

	//https://stackoverflow.com/questions/4452941/why-not-serialize-abstract-classes-in-java
	private static final long serialVersionUID = 1L;
	private String url;
	private String title;
	private boolean executeOnLoad;
	private int refreshPeriodSeconds;
	private int index;
	private String baseUrl;
	private String parametersJson;
	private String reportIdString;

	/**
	 * @return the reportIdString
	 */
	public String getReportIdString() {
		return reportIdString;
	}

	/**
	 * @param reportIdString the reportIdString to set
	 */
	public void setReportIdString(String reportIdString) {
		this.reportIdString = reportIdString;
	}

	/**
	 * @return the parametersJson
	 */
	public String getParametersJson() {
		return parametersJson;
	}

	/**
	 * @param parametersJson the parametersJson to set
	 */
	public void setParametersJson(String parametersJson) {
		this.parametersJson = parametersJson;
	}

	/**
	 * @return the baseUrl
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * @param baseUrl the baseUrl to set
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
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
