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

/**
 *
 * @author Timothy Anyona
 */
public class Portlet {
	private String source;
	private String baseUrl;
	private String classNamePrefix;
	private String title;
	private boolean executeOnLoad;
	private String refreshPeriod;

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
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
	 * @return the refreshPeriod
	 */
	public String getRefreshPeriod() {
		return refreshPeriod;
	}

	/**
	 * @param refreshPeriod the refreshPeriod to set
	 */
	public void setRefreshPeriod(String refreshPeriod) {
		this.refreshPeriod = refreshPeriod;
	}
	
}
