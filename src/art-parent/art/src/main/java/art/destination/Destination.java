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
package art.destination;

import art.encryption.AesEncryptor;
import art.enums.DestinationType;
import com.univocity.parsers.annotations.Parsed;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents a destination
 *
 * @author Timothy Anyona
 */
public class Destination implements Serializable {

	private static final long serialVersionUID = 1L;
	@Parsed
	private int destinationId;
	@Parsed
	private String name;
	@Parsed
	private String description;
	@Parsed
	private boolean active = true;
	@Parsed
	private DestinationType destinationType;
	@Parsed
	private String server;
	@Parsed
	private int port;
	@Parsed
	private String user;
	@Parsed
	private String password;
	@Parsed
	private String path;
	@Parsed
	private String options;
	private Date creationDate;
	private String createdBy;
	private Date updateDate;
	private String updatedBy;
	private boolean useBlankPassword; //only used for user interface logic
	@Parsed
	private String domain;
	@Parsed
	private String subDirectory;
	@Parsed
	private boolean createDirectories;
	@Parsed
	private boolean clearTextPassword;
	@Parsed
	private String googleJsonKeyFile;

	/**
	 * @return the googleJsonKeyFile
	 */
	public String getGoogleJsonKeyFile() {
		return googleJsonKeyFile;
	}

	/**
	 * @param googleJsonKeyFile the googleJsonKeyFile to set
	 */
	public void setGoogleJsonKeyFile(String googleJsonKeyFile) {
		this.googleJsonKeyFile = googleJsonKeyFile;
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
	 * @return the createDirectories
	 */
	public boolean isCreateDirectories() {
		return createDirectories;
	}

	/**
	 * @param createDirectories the createDirectories to set
	 */
	public void setCreateDirectories(boolean createDirectories) {
		this.createDirectories = createDirectories;
	}

	/**
	 * @return the subDirectory
	 */
	public String getSubDirectory() {
		return subDirectory;
	}

	/**
	 * @param subDirectory the subDirectory to set
	 */
	public void setSubDirectory(String subDirectory) {
		this.subDirectory = subDirectory;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}

	/**
	 * @return the useBlankPassword
	 */
	public boolean isUseBlankPassword() {
		return useBlankPassword;
	}

	/**
	 * @param useBlankPassword the useBlankPassword to set
	 */
	public void setUseBlankPassword(boolean useBlankPassword) {
		this.useBlankPassword = useBlankPassword;
	}

	/**
	 * @return the destinationId
	 */
	public int getDestinationId() {
		return destinationId;
	}

	/**
	 * @param destinationId the destinationId to set
	 */
	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
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
	 * @return the destinationType
	 */
	public DestinationType getDestinationType() {
		return destinationType;
	}

	/**
	 * @param destinationType the destinationType to set
	 */
	public void setDestinationType(DestinationType destinationType) {
		this.destinationType = destinationType;
	}

	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
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
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
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
		int hash = 7;
		hash = 11 * hash + this.destinationId;
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
		final Destination other = (Destination) obj;
		if (this.destinationId != other.destinationId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Destination{" + "name=" + name + '}';
	}

	/**
	 * Decrypts the password field
	 */
	public void decryptPassword() {
		password = AesEncryptor.decrypt(password);
	}

	/**
	 * Encrypts the password field
	 */
	public void encryptPassword() {
		password = AesEncryptor.encrypt(password);
	}

}
