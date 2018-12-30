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

import art.encryption.AesEncryptor;
import art.encryption.DesEncryptor;
import art.enums.DatabaseProtocol;
import art.enums.DatabaseType;
import art.enums.DatasourceType;
import art.settings.EncryptionPassword;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.univocity.parsers.annotations.Parsed;
import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a datasource
 *
 * @author Timothy Anyona
 */
public class Datasource implements Serializable {

	private static final long serialVersionUID = 1L;
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
	private String passwordAlgorithm;
	@Parsed
	private DatasourceType datasourceType;
	@Parsed
	private String options;
	@Parsed
	private boolean active;
	private Date creationDate;
	private Date updateDate;
	@Parsed
	private String description;
	private String createdBy;
	private String updatedBy;
	@Parsed
	private boolean clearTextPassword;
	@Parsed
	private DatabaseProtocol databaseProtocol;
	@Parsed
	private DatabaseType databaseType;

	/**
	 * @return the databaseType
	 */
	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	/**
	 * @param databaseType the databaseType to set
	 */
	public void setDatabaseType(DatabaseType databaseType) {
		this.databaseType = databaseType;
	}

	/**
	 * @return the databaseProtocol
	 */
	public DatabaseProtocol getDatabaseProtocol() {
		return databaseProtocol;
	}

	/**
	 * @param databaseProtocol the databaseProtocol to set
	 */
	public void setDatabaseProtocol(DatabaseProtocol databaseProtocol) {
		this.databaseProtocol = databaseProtocol;
	}

	/**
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

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

	/**
	 * @return the clearTextPassword
	 */
	public boolean isClearTextPassword() {
		return clearTextPassword;
	}

	/**
	 * @param clearTextPassword the clearTextPassword to set
	 */
	public void setClearTextPassword(boolean clearTextPassword) {
		this.clearTextPassword = clearTextPassword;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
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

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 59 * hash + this.getDatasourceId();
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
		if (this.getDatasourceId() != other.getDatasourceId()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Datasource{" + "name=" + getName() + '}';
	}

	/**
	 * Decrypts the password field
	 *
	 * @throws java.lang.Exception
	 */
	public void decryptPassword() throws Exception {
		//use getPasswordAlgorithm() as it's overriden by the ArtDatabase class
		if (StringUtils.equalsIgnoreCase(getPasswordAlgorithm(), "art")) {
			if (StringUtils.startsWith(password, "o:")) {
				password = DesEncryptor.decrypt(password.substring(2));
			}
		} else if (StringUtils.equalsIgnoreCase(getPasswordAlgorithm(), "aes")) {
			password = AesEncryptor.decrypt(password);
		}
	}

	/**
	 * Encrypts the password field
	 *
	 * @throws java.lang.Exception
	 */
	public void encryptPassword() throws Exception {
		String key = null;
		EncryptionPassword encryptionPassword = null;
		encryptPassword(key, encryptionPassword);
	}

	/**
	 * Encrypts the password field
	 *
	 * @param key the key to use. If null, the current key will be used
	 * @param encryptionPassword the encryption password configuration. null if
	 * to use current.
	 * @throws java.lang.Exception
	 */
	public void encryptPassword(String key, EncryptionPassword encryptionPassword) throws Exception {
		password = AesEncryptor.encrypt(password, key, encryptionPassword);
		passwordAlgorithm = "AES";
	}

	/**
	 * Returns <code>true</code> if the password field is null
	 *
	 * @return <code>true</code> if the password field is null
	 */
	public boolean hasNullPassword() {
		if (password == null) {
			return true;
		} else {
			return false;
		}
	}
}
