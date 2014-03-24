/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.datasource;

import java.io.Serializable;
import java.util.Date;

/**
 * Class to represent a datasource
 *
 * @author Timothy Anyona
 */
public class Datasource implements Serializable {

	private static final long serialVersionUID = 1L;
	private int datasourceId;
	private String name;
	private String driver;
	private String url;
	private String username;
	private String password;
	private boolean active;
	private Date creationDate;
	private Date updateDate;
	private String testSql;
	private int connectionPoolTimeout;
	private boolean useBlankPassword; //only used for user interface logic
	private String description;
	private boolean jndi;

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
	 * Get the value of description
	 *
	 * @return the value of description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the value of description
	 *
	 * @param description new value of description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the value of useBlankPassword
	 *
	 * @return the value of useBlankPassword
	 */
	public boolean isUseBlankPassword() {
		return useBlankPassword;
	}

	/**
	 * Set the value of useBlankPassword
	 *
	 * @param useBlankPassword new value of useBlankPassword
	 */
	public void setUseBlankPassword(boolean useBlankPassword) {
		this.useBlankPassword = useBlankPassword;
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
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @param driver the driver to set
	 */
	public void setDriver(String driver) {
		this.driver = driver;
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
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the updateDate
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate the updateDate to set
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	/**
	 * @return the testSql
	 */
	public String getTestSql() {
		return testSql;
	}

	/**
	 * @param testSql the testSql to set
	 */
	public void setTestSql(String testSql) {
		this.testSql = testSql;
	}

	/**
	 * @return the connectionPoolTimeout
	 */
	public int getConnectionPoolTimeout() {
		return connectionPoolTimeout;
	}

	/**
	 * @param connectionPoolTimeout the connectionPoolTimeout to set
	 */
	public void setConnectionPoolTimeout(int connectionPoolTimeout) {
		this.connectionPoolTimeout = connectionPoolTimeout;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 59 * hash + this.datasourceId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Datasource other = (Datasource) obj;
		if (this.datasourceId != other.datasourceId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Datasource{" + "name=" + name + '}';
	}

}
