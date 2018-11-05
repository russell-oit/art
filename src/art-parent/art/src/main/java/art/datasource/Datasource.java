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
import com.univocity.parsers.annotations.Parsed;
import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a datasource
 *
 * @author Timothy Anyona
 */
public class Datasource extends DatasourceInfo implements Serializable {

	private static final long serialVersionUID = 1L;
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
		if (StringUtils.equalsIgnoreCase(passwordAlgorithm, "art")) {
			if (StringUtils.startsWith(password, "o:")) {
				password = DesEncryptor.decrypt(password.substring(2));
			}
		} else if (StringUtils.equalsIgnoreCase(passwordAlgorithm, "aes")) {
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
		encryptPassword(key);
	}

	/**
	 * Encrypts the password field
	 *
	 * @param key the key to use
	 * @throws java.lang.Exception
	 */
	public void encryptPassword(String key) throws Exception {
		password = AesEncryptor.encrypt(password, key);
		passwordAlgorithm = "AES";
	}
}
