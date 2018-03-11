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
package art.datasource;

import art.enums.DatasourceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.univocity.parsers.annotations.Parsed;

/**
 * Represents details common to report datasources and art database
 * configuration
 *
 * @author Timothy Anyona
 */
public abstract class DatasourceInfo {

	@Parsed
	private int datasourceId;
	@Parsed
	private String name;
	@Parsed
	private String driver;
	@Parsed
	private String url;
	@Parsed
	private String username;
	@Parsed
	protected String password;
	@JsonIgnore
	private boolean useBlankPassword; //only used for user interface logic
	@Parsed
	private String testSql;
	@Parsed
	private int connectionPoolTimeoutMins;
	@Parsed
	private boolean jndi;
	@Parsed
	protected String passwordAlgorithm;
	@Parsed
	private DatasourceType datasourceType;

	/**
	 * @return the datasourceType
	 */
	public DatasourceType getDatasourceType() {
		return datasourceType;
	}

	/**
	 * @param datasourceType the datasourceType to set
	 */
	public void setDatasourceType(DatasourceType datasourceType) {
		this.datasourceType = datasourceType;
	}

	/**
	 * @return the passwordAlgorithm
	 */
	public String getPasswordAlgorithm() {
		return passwordAlgorithm;
	}

	/**
	 * @param passwordAlgorithm the passwordAlgorithm to set
	 */
	public void setPasswordAlgorithm(String passwordAlgorithm) {
		this.passwordAlgorithm = passwordAlgorithm;
	}

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
	 * Get the value of connectionPoolTimeoutMins
	 *
	 * @return the value of connectionPoolTimeoutMins
	 */
	public int getConnectionPoolTimeoutMins() {
		return connectionPoolTimeoutMins;
	}

	/**
	 * Set the value of connectionPoolTimeoutMins
	 *
	 * @param connectionPoolTimeoutMins new value of connectionPoolTimeoutMins
	 */
	public void setConnectionPoolTimeoutMins(int connectionPoolTimeoutMins) {
		this.connectionPoolTimeoutMins = connectionPoolTimeoutMins;
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
