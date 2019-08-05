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
import art.settings.EncryptionPassword;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents an smtp server configuration
 *
 * @author Timothy Anyona
 */
public class SmtpServer implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int smtpServerId;
	private String name;
	private String description;
	private boolean active = true;
	private String server;
	private int port = 25;
	private boolean useStartTls;
	private boolean useSmtpAuthentication;
	private String username;
	private String password;
	@JsonIgnore
	private boolean useBlankPassword; //only used for username interface logic
	private String from;
	private Date creationDate;
	private String createdBy;
	private Date updateDate;
	private String updatedBy;
	private boolean clearTextPassword;
	private boolean useOAuth2;
	private String oauthClientId;
	private String oauthClientSecret;
	private String oauthRefreshToken;
	private String oauthAccessToken;
	private Date oauthAccessTokenExpiry;

	/**
	 * @return the oauthAccessTokenExpiry
	 */
	public Date getOauthAccessTokenExpiry() {
		return oauthAccessTokenExpiry;
	}

	/**
	 * @param oauthAccessTokenExpiry the oauthAccessTokenExpiry to set
	 */
	public void setOauthAccessTokenExpiry(Date oauthAccessTokenExpiry) {
		this.oauthAccessTokenExpiry = oauthAccessTokenExpiry;
	}

	/**
	 * @return the useOAuth2
	 */
	public boolean isUseOAuth2() {
		return useOAuth2;
	}

	/**
	 * @param useOAuth2 the useOAuth2 to set
	 */
	public void setUseOAuth2(boolean useOAuth2) {
		this.useOAuth2 = useOAuth2;
	}

	/**
	 * @return the oauthClientId
	 */
	public String getOauthClientId() {
		return oauthClientId;
	}

	/**
	 * @param oauthClientId the oauthClientId to set
	 */
	public void setOauthClientId(String oauthClientId) {
		this.oauthClientId = oauthClientId;
	}

	/**
	 * @return the oauthClientSecret
	 */
	public String getOauthClientSecret() {
		return oauthClientSecret;
	}

	/**
	 * @param oauthClientSecret the oauthClientSecret to set
	 */
	public void setOauthClientSecret(String oauthClientSecret) {
		this.oauthClientSecret = oauthClientSecret;
	}

	/**
	 * @return the oauthRefreshToken
	 */
	public String getOauthRefreshToken() {
		return oauthRefreshToken;
	}

	/**
	 * @param oauthRefreshToken the oauthRefreshToken to set
	 */
	public void setOauthRefreshToken(String oauthRefreshToken) {
		this.oauthRefreshToken = oauthRefreshToken;
	}

	/**
	 * @return the oauthAccessToken
	 */
	public String getOauthAccessToken() {
		return oauthAccessToken;
	}

	/**
	 * @param oauthAccessToken the oauthAccessToken to set
	 */
	public void setOauthAccessToken(String oauthAccessToken) {
		this.oauthAccessToken = oauthAccessToken;
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
	 * Decrypts password fields
	 *
	 * @throws java.lang.Exception
	 */
	public void decryptPasswords() throws Exception {
		password = AesEncryptor.decrypt(password);
		oauthClientSecret = AesEncryptor.decrypt(oauthClientSecret);
		oauthRefreshToken = AesEncryptor.decrypt(oauthRefreshToken);
		oauthAccessToken = AesEncryptor.decrypt(oauthAccessToken);
	}

	/**
	 * Encrypts password fields
	 *
	 * @throws java.lang.Exception
	 */
	public void encryptPasswords() throws Exception {
		String key = null;
		EncryptionPassword encryptionPassword = null;
		encryptPasswords(key, encryptionPassword);
	}

	/**
	 * Encrypts password fields
	 *
	 * @param key the key to use. If null, the current key will be used
	 * @param encryptionPassword the encryption password configuration. null if
	 * to use current
	 * @throws java.lang.Exception
	 */
	public void encryptPasswords(String key, EncryptionPassword encryptionPassword) throws Exception {
		password = AesEncryptor.encrypt(password, key, encryptionPassword);
		oauthClientSecret = AesEncryptor.encrypt(oauthClientSecret, key, encryptionPassword);
		oauthRefreshToken = AesEncryptor.encrypt(oauthRefreshToken, key, encryptionPassword);
		oauthAccessToken = AesEncryptor.encrypt(oauthAccessToken, key, encryptionPassword);
	}

	/**
	 * Returns <code>true</code> if password fields are null
	 *
	 * @return <code>true</code> if password fields are null
	 */
	public boolean hasNullPasswords() {
		if (password == null && oauthClientSecret == null
				&& oauthRefreshToken == null && oauthAccessToken == null) {
			return true;
		} else {
			return false;
		}
	}

}
