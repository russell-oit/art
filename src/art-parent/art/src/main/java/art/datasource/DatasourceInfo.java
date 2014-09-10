/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
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

package art.datasource;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract representation of details common to report datasources and art database configuration
 * 
 * @author Timothy Anyona
 */
public abstract class DatasourceInfo {
	private int datasourceId;
	private String name;
	private String driver;
	private String url;
	private String username;
	private String password;
	@JsonIgnore
	private boolean useBlankPassword; //only used for user interface logic
	private String testSql;
	private int connectionPoolTimeout;
	private boolean jndi;

	/**
	 * @return the datasourceId
	 */
	public int getDatasourceId() {
		return datasourceId;
	}

	/**
	 * @param datasourceId the datasourceId to set
	 */
	public void setDatasourceId(int datasourceId) {
		this.datasourceId = datasourceId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the value of jndi
	 *
	 * @return the value of jndi
	 */
	public boolean isJndi() {
		return jndi;
	}

	/**
	 * Set the value of jndi
	 *
	 * @param jndi new value of jndi
	 */
	public void setJndi(boolean jndi) {
		this.jndi = jndi;
	}

	/**
	 * Get the value of useBlankPassword. only used for user interface logic
	 *
	 * @return the value of useBlankPassword
	 */
	public boolean isUseBlankPassword() {
		return useBlankPassword;
	}

	/**
	 * Set the value of useBlankPassword. only used for user interface logic
	 *
	 * @param useBlankPassword new value of useBlankPassword
	 */
	public void setUseBlankPassword(boolean useBlankPassword) {
		this.useBlankPassword = useBlankPassword;
	}

	/**
	 * Get the value of testSql
	 *
	 * @return the value of testSql
	 */
	public String getTestSql() {
		return testSql;
	}

	/**
	 * Set the value of testSql
	 *
	 * @param testSql new value of testSql
	 */
	public void setTestSql(String testSql) {
		this.testSql = testSql;
	}

	/**
	 * Get the value of connectionPoolTimeout
	 *
	 * @return the value of connectionPoolTimeout
	 */
	public int getConnectionPoolTimeout() {
		return connectionPoolTimeout;
	}

	/**
	 * Set the value of connectionPoolTimeout
	 *
	 * @param connectionPoolTimeout new value of connectionPoolTimeout
	 */
	public void setConnectionPoolTimeout(int connectionPoolTimeout) {
		this.connectionPoolTimeout = connectionPoolTimeout;
	}

	/**
	 * Get the value of password
	 *
	 * @return the value of password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the value of password
	 *
	 * @param password new value of password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Get the value of username
	 *
	 * @return the value of username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the value of username
	 *
	 * @param username new value of username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get the value of url
	 *
	 * @return the value of url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the value of url
	 *
	 * @param url new value of url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Get the value of driver
	 *
	 * @return the value of driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * Set the value of driver
	 *
	 * @param driver new value of driver
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}
}
