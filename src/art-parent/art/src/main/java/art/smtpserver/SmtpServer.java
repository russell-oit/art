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
package art.smtpserver;

import art.encryption.AesEncryptor;
import com.univocity.parsers.annotations.Parsed;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents an smtp server configuration
 *
 * @author Timothy Anyona
 */
public class SmtpServer implements Serializable {

	private static final long serialVersionUID = 1L;
	@Parsed
	private int smtpServerId;
	@Parsed
	private String name;
	@Parsed
	private String description;
	@Parsed
	private boolean active = true;
	@Parsed
	private String server;
	@Parsed
	private int port = 25;
	@Parsed
	private boolean useStartTls;
	@Parsed
	private boolean useSmtpAuthentication;
	@Parsed
	private String username;
	@Parsed
	private String password;
	private boolean useBlankPassword; //only used for username interface logic
	@Parsed
	private String from;
	private Date creationDate;
	private String createdBy;
	private Date updateDate;
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
	 * @return the smtpServerId
	 */
	public int getSmtpServerId() {
		return smtpServerId;
	}

	/**
	 * @param smtpServerId the smtpServerId to set
	 */
	public void setSmtpServerId(int smtpServerId) {
		this.smtpServerId = smtpServerId;
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
	 * @return the useStartTls
	 */
	public boolean isUseStartTls() {
		return useStartTls;
	}

	/**
	 * @param useStartTls the useStartTls to set
	 */
	public void setUseStartTls(boolean useStartTls) {
		this.useStartTls = useStartTls;
	}

	/**
	 * @return the useSmtpAuthentication
	 */
	public boolean isUseSmtpAuthentication() {
		return useSmtpAuthentication;
	}

	/**
	 * @param useSmtpAuthentication the useSmtpAuthentication to set
	 */
	public void setUseSmtpAuthentication(boolean useSmtpAuthentication) {
		this.useSmtpAuthentication = useSmtpAuthentication;
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
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
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
		int hash = 5;
		hash = 89 * hash + this.smtpServerId;
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
		final SmtpServer other = (SmtpServer) obj;
		if (this.smtpServerId != other.smtpServerId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "SmtpServer{" + "name=" + name + '}';
	}

	/**
	 * Decrypts the password field
	 * 
	 * @throws java.lang.Exception
	 */
	public void decryptPassword() throws Exception {
		password = AesEncryptor.decrypt(password);
	}
	
	/**
	 * Encrypts the password field
	 * 
	 * @throws java.lang.Exception
	 */
	public void encryptPassword() throws Exception {
		password = AesEncryptor.encrypt(password);
	}

}
