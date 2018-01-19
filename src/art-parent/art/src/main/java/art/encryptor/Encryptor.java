/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.encryptor;

import art.enums.EncryptorType;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents an encryptor
 *
 * @author Timothy Anyona
 */
public class Encryptor implements Serializable {

	private static final long serialVersionUID = 1L;
	private int encryptorId;
	private String name;
	private String description;
	private boolean active = true;
	private EncryptorType encryptorType = EncryptorType.AESCrypt;
	private String aesCryptPassword;
	private Date creationDate;
	private String createdBy;
	private Date updateDate;
	private String updatedBy;
	private String openPgpPublicKeyFile;
	private String openPgpPublicKeyString;
	private String openPgpSigningKeyFile;
	private String openPgpSigningKeyPassphrase;
	private String openPassword;
	private String modifyPassword;
	private boolean useNoneOpenPassword; //only for use with ui
	private boolean useNoneModifyPassword; //only for use with ui

	/**
	 * @return the openPassword
	 */
	public String getOpenPassword() {
		return openPassword;
	}

	/**
	 * @param openPassword the openPassword to set
	 */
	public void setOpenPassword(String openPassword) {
		this.openPassword = openPassword;
	}

	/**
	 * @return the modifyPassword
	 */
	public String getModifyPassword() {
		return modifyPassword;
	}

	/**
	 * @param modifyPassword the modifyPassword to set
	 */
	public void setModifyPassword(String modifyPassword) {
		this.modifyPassword = modifyPassword;
	}

	/**
	 * @return the useNoneOpenPassword
	 */
	public boolean isUseNoneOpenPassword() {
		return useNoneOpenPassword;
	}

	/**
	 * @param useNoneOpenPassword the useNoneOpenPassword to set
	 */
	public void setUseNoneOpenPassword(boolean useNoneOpenPassword) {
		this.useNoneOpenPassword = useNoneOpenPassword;
	}

	/**
	 * @return the useNoneModifyPassword
	 */
	public boolean isUseNoneModifyPassword() {
		return useNoneModifyPassword;
	}

	/**
	 * @param useNoneModifyPassword the useNoneModifyPassword to set
	 */
	public void setUseNoneModifyPassword(boolean useNoneModifyPassword) {
		this.useNoneModifyPassword = useNoneModifyPassword;
	}

	/**
	 * @return the openPgpPublicKeyFile
	 */
	public String getOpenPgpPublicKeyFile() {
		return openPgpPublicKeyFile;
	}

	/**
	 * @param openPgpPublicKeyFile the openPgpPublicKeyFile to set
	 */
	public void setOpenPgpPublicKeyFile(String openPgpPublicKeyFile) {
		this.openPgpPublicKeyFile = openPgpPublicKeyFile;
	}

	/**
	 * @return the openPgpPublicKeyString
	 */
	public String getOpenPgpPublicKeyString() {
		return openPgpPublicKeyString;
	}

	/**
	 * @param openPgpPublicKeyString the openPgpPublicKeyString to set
	 */
	public void setOpenPgpPublicKeyString(String openPgpPublicKeyString) {
		this.openPgpPublicKeyString = openPgpPublicKeyString;
	}

	/**
	 * @return the openPgpSigningKeyFile
	 */
	public String getOpenPgpSigningKeyFile() {
		return openPgpSigningKeyFile;
	}

	/**
	 * @param openPgpSigningKeyFile the openPgpSigningKeyFile to set
	 */
	public void setOpenPgpSigningKeyFile(String openPgpSigningKeyFile) {
		this.openPgpSigningKeyFile = openPgpSigningKeyFile;
	}

	/**
	 * @return the openPgpSigningKeyPassphrase
	 */
	public String getOpenPgpSigningKeyPassphrase() {
		return openPgpSigningKeyPassphrase;
	}

	/**
	 * @param openPgpSigningKeyPassphrase the openPgpSigningKeyPassphrase to set
	 */
	public void setOpenPgpSigningKeyPassphrase(String openPgpSigningKeyPassphrase) {
		this.openPgpSigningKeyPassphrase = openPgpSigningKeyPassphrase;
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
	 * @return the encryptorId
	 */
	public int getEncryptorId() {
		return encryptorId;
	}

	/**
	 * @param encryptorId the encryptorId to set
	 */
	public void setEncryptorId(int encryptorId) {
		this.encryptorId = encryptorId;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the encryptorType
	 */
	public EncryptorType getEncryptorType() {
		return encryptorType;
	}

	/**
	 * @param encryptorType the encryptorType to set
	 */
	public void setEncryptorType(EncryptorType encryptorType) {
		this.encryptorType = encryptorType;
	}

	/**
	 * @return the aesCryptPassword
	 */
	public String getAesCryptPassword() {
		return aesCryptPassword;
	}

	/**
	 * @param aesCryptPassword the aesCryptPassword to set
	 */
	public void setAesCryptPassword(String aesCryptPassword) {
		this.aesCryptPassword = aesCryptPassword;
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

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + this.encryptorId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Encryptor other = (Encryptor) obj;
		if (this.encryptorId != other.encryptorId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Encryptor{" + "name=" + name + '}';
	}

}
