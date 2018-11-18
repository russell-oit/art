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
package art.settings;

import art.encryption.AesEncryptor;
import art.enums.ArtAuthenticationMethod;
import art.enums.LdapAuthenticationMethod;
import art.enums.LdapConnectionEncryptionMethod;
import art.enums.LoggerLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents application settings
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings implements Serializable {

	private static final long serialVersionUID = 1L;
	private String smtpServer;
	private int smtpPort;
	private boolean smtpUseStartTls;
	private boolean useSmtpAuthentication;
	private String smtpUsername;
	private String smtpPassword;
	@JsonIgnore
	private boolean useBlankSmtpPassword; //ignore when saving object
	private int maxRowsDefault;
	private String maxRowsSpecific;
	private ArtAuthenticationMethod artAuthenticationMethod;
	private String windowsDomainController;
	private String allowedWindowsDomains;
	private String databaseAuthenticationDriver;
	private String databaseAuthenticationUrl;
	private String ldapServer;
	private int ldapPort;
	private LdapConnectionEncryptionMethod ldapConnectionEncryptionMethod;
	private String ldapUrl;
	private String ldapBaseDn;
	private boolean useLdapAnonymousBind;
	private String ldapBindDn;
	private String ldapBindPassword;
	@JsonIgnore
	private boolean useBlankLdapBindPassword; //only used for user interface logic
	private String ldapUserIdAttribute;
	private LdapAuthenticationMethod ldapAuthenticationMethod;
	private String ldapRealm;
	private String pdfFontName;
	private String pdfFontFile;
	private String pdfFontDirectory;
	private String pdfFontEncoding;
	private boolean pdfFontEmbedded;
	private String administratorEmail;
	private String dateFormat;
	private String timeFormat;
	private String reportFormats;
	private int maxRunningReports;
	private boolean showHeaderInPublicUserSession;
	private int mondrianCacheExpiryPeriod;
	private boolean schedulingEnabled = true;
	private String rssLink;
	private int maxFileUploadSizeMB = 5;
	private String artBaseUrl;
	private String casLogoutUrl;
	private String smtpFrom;
	private String systemLocale;
	private int logsDatasourceId;
	private Date updateDate;
	private String updatedBy;
	private String errorNotificationTo;
	private String errorNotificationFrom;
	private String errorNotificationSubjectPattern;
	private LoggerLevel errorNotificatonLevel;
	private String errorNotificationLogger;
	private String errorNotificationSuppressAfter;
	private String errorNotificationExpireAfter;
	private String errorNotificationDigestFrequency;
	private int passwordMinLength;
	private int passwordMinLowercase;
	private int passwordMinUppercase;
	private int passwordMinNumeric;
	private int passwordMinSpecial;
	private boolean clearTextPasswords; //used to enable import with passwords specified in clear text
	private int jwtTokenExpiryMins = 60;
	private boolean enableDirectReportEmailing;

	/**
	 * @return the enableDirectReportEmailing
	 */
	public boolean isEnableDirectReportEmailing() {
		return enableDirectReportEmailing;
	}

	/**
	 * @param enableDirectReportEmailing the enableDirectReportEmailing to set
	 */
	public void setEnableDirectReportEmailing(boolean enableDirectReportEmailing) {
		this.enableDirectReportEmailing = enableDirectReportEmailing;
	}

	/**
	 * @return the jwtTokenExpiryMins
	 */
	public int getJwtTokenExpiryMins() {
		return jwtTokenExpiryMins;
	}

	/**
	 * @param jwtTokenExpiryMins the jwtTokenExpiryMins to set
	 */
	public void setJwtTokenExpiryMins(int jwtTokenExpiryMins) {
		this.jwtTokenExpiryMins = jwtTokenExpiryMins;
	}

	/**
	 * @return the clearTextPasswords
	 */
	public boolean isClearTextPasswords() {
		return clearTextPasswords;
	}

	/**
	 * @param clearTextPasswords the clearTextPasswords to set
	 */
	public void setClearTextPasswords(boolean clearTextPasswords) {
		this.clearTextPasswords = clearTextPasswords;
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

	/**
	 * @return the logsDatasourceId
	 */
	public int getLogsDatasourceId() {
		return logsDatasourceId;
	}

	/**
	 * @param logsDatasourceId the logsDatasourceId to set
	 */
	public void setLogsDatasourceId(int logsDatasourceId) {
		this.logsDatasourceId = logsDatasourceId;
	}

	/**
	 * @return the systemLocale
	 */
	public String getSystemLocale() {
		return systemLocale;
	}

	/**
	 * @param systemLocale the systemLocale to set
	 */
	public void setSystemLocale(String systemLocale) {
		this.systemLocale = systemLocale;
	}

	/**
	 * @return the smtpFrom
	 */
	public String getSmtpFrom() {
		return smtpFrom;
	}

	/**
	 * @param smtpFrom the smtpFrom to set
	 */
	public void setSmtpFrom(String smtpFrom) {
		this.smtpFrom = smtpFrom;
	}

	/**
	 * @return the casLogoutUrl
	 */
	public String getCasLogoutUrl() {
		return casLogoutUrl;
	}

	/**
	 * @param casLogoutUrl the casLogoutUrl to set
	 */
	public void setCasLogoutUrl(String casLogoutUrl) {
		this.casLogoutUrl = casLogoutUrl;
	}

	/**
	 * @return the artBaseUrl
	 */
	public String getArtBaseUrl() {
		return artBaseUrl;
	}

	/**
	 * @param artBaseUrl the artBaseUrl to set
	 */
	public void setArtBaseUrl(String artBaseUrl) {
		this.artBaseUrl = artBaseUrl;
	}

	/**
	 * Get the value of maxFileUploadSizeMB
	 *
	 * @return the value of maxFileUploadSizeMB
	 */
	public int getMaxFileUploadSizeMB() {
		return maxFileUploadSizeMB;
	}

	/**
	 * Set the value of maxFileUploadSizeMB
	 *
	 * @param maxFileUploadSizeMB new value of maxFileUploadSizeMB
	 */
	public void setMaxFileUploadSizeMB(int maxFileUploadSizeMB) {
		this.maxFileUploadSizeMB = maxFileUploadSizeMB;
	}

	/**
	 * Get the value of useLdapAnonymousBind
	 *
	 * @return the value of useLdapAnonymousBind
	 */
	public boolean isUseLdapAnonymousBind() {
		return useLdapAnonymousBind;
	}

	/**
	 * Set the value of useLdapAnonymousBind
	 *
	 * @param useLdapAnonymousBind new value of useLdapAnonymousBind
	 */
	public void setUseLdapAnonymousBind(boolean useLdapAnonymousBind) {
		this.useLdapAnonymousBind = useLdapAnonymousBind;
	}

	/**
	 * Get the value of useSmtpAuthentication
	 *
	 * @return the value of useSmtpAuthentication
	 */
	public boolean isUseSmtpAuthentication() {
		return useSmtpAuthentication;
	}

	/**
	 * Set the value of useSmtpAuthentication
	 *
	 * @param useSmtpAuthentication new value of useSmtpAuthentication
	 */
	public void setUseSmtpAuthentication(boolean useSmtpAuthentication) {
		this.useSmtpAuthentication = useSmtpAuthentication;
	}

	/**
	 * @return the smtpUseStartTls
	 */
	public boolean isSmtpUseStartTls() {
		return smtpUseStartTls;
	}

	/**
	 * @param smtpUseStartTls the smtpUseStartTls to set
	 */
	public void setSmtpUseStartTls(boolean smtpUseStartTls) {
		this.smtpUseStartTls = smtpUseStartTls;
	}

	/**
	 * Get the value of useBlankLdapBindPassword. only used for user interface
	 * logic
	 *
	 * @return the value of useBlankLdapBindPassword
	 */
	public boolean isUseBlankLdapBindPassword() {
		return useBlankLdapBindPassword;
	}

	/**
	 * Set the value of useBlankLdapBindPassword. only used for user interface
	 * logic
	 *
	 * @param useBlankLdapBindPassword new value of useBlankLdapBindPassword
	 */
	public void setUseBlankLdapBindPassword(boolean useBlankLdapBindPassword) {
		this.useBlankLdapBindPassword = useBlankLdapBindPassword;
	}

	/**
	 * Get the value of useBlankSmtpPassword. only used for user interface logic
	 *
	 * @return the value of useBlankSmtpPassword
	 */
	public boolean isUseBlankSmtpPassword() {
		return useBlankSmtpPassword;
	}

	/**
	 * Set the value of useBlankSmtpPassword. only used for user interface logic
	 *
	 * @param useBlankSmtpPassword new value of useBlankSmtpPassword
	 */
	public void setUseBlankSmtpPassword(boolean useBlankSmtpPassword) {
		this.useBlankSmtpPassword = useBlankSmtpPassword;
	}

	/**
	 * Get the value of ldapRealm
	 *
	 * @return the value of ldapRealm
	 */
	public String getLdapRealm() {
		return ldapRealm;
	}

	/**
	 * Set the value of ldapRealm
	 *
	 * @param ldapRealm new value of ldapRealm
	 */
	public void setLdapRealm(String ldapRealm) {
		this.ldapRealm = ldapRealm;
	}

	/**
	 * Get the value of ldapAuthenticationMethod
	 *
	 * @return the value of ldapAuthenticationMethod
	 */
	public LdapAuthenticationMethod getLdapAuthenticationMethod() {
		return ldapAuthenticationMethod;
	}

	/**
	 * Set the value of ldapAuthenticationMethod
	 *
	 * @param ldapAuthenticationMethod new value of ldapAuthenticationMethod
	 */
	public void setLdapAuthenticationMethod(LdapAuthenticationMethod ldapAuthenticationMethod) {
		this.ldapAuthenticationMethod = ldapAuthenticationMethod;
	}

	/**
	 * Get the value of ldapUserIdAttribute
	 *
	 * @return the value of ldapUserIdAttribute
	 */
	public String getLdapUserIdAttribute() {
		return ldapUserIdAttribute;
	}

	/**
	 * Set the value of ldapUserIdAttribute
	 *
	 * @param ldapUserIdAttribute new value of ldapUserIdAttribute
	 */
	public void setLdapUserIdAttribute(String ldapUserIdAttribute) {
		this.ldapUserIdAttribute = ldapUserIdAttribute;
	}

	/**
	 * Get the value of ldapBindPassword
	 *
	 * @return the value of ldapBindPassword
	 */
	public String getLdapBindPassword() {
		return ldapBindPassword;
	}

	/**
	 * Set the value of ldapBindPassword
	 *
	 * @param ldapBindPassword new value of ldapBindPassword
	 */
	public void setLdapBindPassword(String ldapBindPassword) {
		this.ldapBindPassword = ldapBindPassword;
	}

	/**
	 * Get the value of ldapBindDn
	 *
	 * @return the value of ldapBindDn
	 */
	public String getLdapBindDn() {
		return ldapBindDn;
	}

	/**
	 * Set the value of ldapBindDn
	 *
	 * @param ldapBindDn new value of ldapBindDn
	 */
	public void setLdapBindDn(String ldapBindDn) {
		this.ldapBindDn = ldapBindDn;
	}

	/**
	 * Get the value of ldapBaseDn
	 *
	 * @return the value of ldapBaseDn
	 */
	public String getLdapBaseDn() {
		return ldapBaseDn;
	}

	/**
	 * Set the value of ldapBaseDn
	 *
	 * @param ldapBaseDn new value of ldapBaseDn
	 */
	public void setLdapBaseDn(String ldapBaseDn) {
		this.ldapBaseDn = ldapBaseDn;
	}

	/**
	 * Get the value of ldapUrl
	 *
	 * @return the value of ldapUrl
	 */
	public String getLdapUrl() {
		return ldapUrl;
	}

	/**
	 * Set the value of ldapUrl
	 *
	 * @param ldapUrl new value of ldapUrl
	 */
	public void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}

	/**
	 * Get the value of ldapConnectionEncryptionMethod
	 *
	 * @return the value of ldapConnectionEncryptionMethod
	 */
	public LdapConnectionEncryptionMethod getLdapConnectionEncryptionMethod() {
		return ldapConnectionEncryptionMethod;
	}

	/**
	 * Set the value of ldapConnectionEncryptionMethod
	 *
	 * @param ldapConnectionEncryptionMethod new value of
	 * ldapConnectionEncryptionMethod
	 */
	public void setLdapConnectionEncryptionMethod(LdapConnectionEncryptionMethod ldapConnectionEncryptionMethod) {
		this.ldapConnectionEncryptionMethod = ldapConnectionEncryptionMethod;
	}

	/**
	 * Get the value of ldapPort
	 *
	 * @return the value of ldapPort
	 */
	public int getLdapPort() {
		return ldapPort;
	}

	/**
	 * Set the value of ldapPort
	 *
	 * @param ldapPort new value of ldapPort
	 */
	public void setLdapPort(int ldapPort) {
		this.ldapPort = ldapPort;
	}

	/**
	 * Get the value of ldapServer
	 *
	 * @return the value of ldapServer
	 */
	public String getLdapServer() {
		return ldapServer;
	}

	/**
	 * Set the value of ldapServer
	 *
	 * @param ldapServer new value of ldapServer
	 */
	public void setLdapServer(String ldapServer) {
		this.ldapServer = ldapServer;
	}

	/**
	 * Get the value of databaseAuthenticationUrl
	 *
	 * @return the value of databaseAuthenticationUrl
	 */
	public String getDatabaseAuthenticationUrl() {
		return databaseAuthenticationUrl;
	}

	/**
	 * Set the value of databaseAuthenticationUrl
	 *
	 * @param databaseAuthenticationUrl new value of databaseAuthenticationUrl
	 */
	public void setDatabaseAuthenticationUrl(String databaseAuthenticationUrl) {
		this.databaseAuthenticationUrl = databaseAuthenticationUrl;
	}

	/**
	 * Get the value of databaseAuthenticationDriver
	 *
	 * @return the value of databaseAuthenticationDriver
	 */
	public String getDatabaseAuthenticationDriver() {
		return databaseAuthenticationDriver;
	}

	/**
	 * Set the value of databaseAuthenticationDriver
	 *
	 * @param databaseAuthenticationDriver new value of
	 * databaseAuthenticationDriver
	 */
	public void setDatabaseAuthenticationDriver(String databaseAuthenticationDriver) {
		this.databaseAuthenticationDriver = databaseAuthenticationDriver;
	}

	/**
	 * Get the value of allowedWindowsDomains
	 *
	 * @return the value of allowedWindowsDomains
	 */
	public String getAllowedWindowsDomains() {
		return allowedWindowsDomains;
	}

	/**
	 * Set the value of allowedWindowsDomains
	 *
	 * @param allowedWindowsDomains new value of allowedWindowsDomains
	 */
	public void setAllowedWindowsDomains(String allowedWindowsDomains) {
		this.allowedWindowsDomains = allowedWindowsDomains;
	}

	/**
	 * Get the value of windowsDomainController
	 *
	 * @return the value of windowsDomainController
	 */
	public String getWindowsDomainController() {
		return windowsDomainController;
	}

	/**
	 * Set the value of windowsDomainController
	 *
	 * @param windowsDomainController new value of windowsDomainController
	 */
	public void setWindowsDomainController(String windowsDomainController) {
		this.windowsDomainController = windowsDomainController;
	}

	/**
	 * Get the value of artAuthenticationMethod
	 *
	 * @return the value of artAuthenticationMethod
	 */
	public ArtAuthenticationMethod getArtAuthenticationMethod() {
		return artAuthenticationMethod;
	}

	/**
	 * Set the value of artAuthenticationMethod
	 *
	 * @param artAuthenticationMethod new value of artAuthenticationMethod
	 */
	public void setArtAuthenticationMethod(ArtAuthenticationMethod artAuthenticationMethod) {
		this.artAuthenticationMethod = artAuthenticationMethod;
	}

	/**
	 * Get the value of maxRunningReports
	 *
	 * @return the value of maxRunningReports
	 */
	public int getMaxRunningReports() {
		return maxRunningReports;
	}

	/**
	 * Set the value of maxRunningReports
	 *
	 * @param maxRunningReports new value of maxRunningReports
	 */
	public void setMaxRunningReports(int maxRunningReports) {
		this.maxRunningReports = maxRunningReports;
	}

	/**
	 * Get the value of reportFormats
	 *
	 * @return the value of reportFormats
	 */
	public String getReportFormats() {
		return reportFormats;
	}

	/**
	 * Set the value of reportFormats
	 *
	 * @param reportFormats new value of reportFormats
	 */
	public void setReportFormats(String reportFormats) {
		this.reportFormats = reportFormats;
	}

	/**
	 * Get the value of schedulingEnabled
	 *
	 * @return the value of schedulingEnabled
	 */
	public boolean isSchedulingEnabled() {
		return schedulingEnabled;
	}

	/**
	 * Set the value of schedulingEnabled
	 *
	 * @param schedulingEnabled new value of schedulingEnabled
	 */
	public void setSchedulingEnabled(boolean schedulingEnabled) {
		this.schedulingEnabled = schedulingEnabled;
	}

	/**
	 * Get the value of timeFormat
	 *
	 * @return the value of timeFormat
	 */
	public String getTimeFormat() {
		return timeFormat;
	}

	/**
	 * Set the value of timeFormat
	 *
	 * @param timeFormat new value of timeFormat
	 */
	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	/**
	 * Get the value of dateFormat
	 *
	 * @return the value of dateFormat
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * Set the value of dateFormat
	 *
	 * @param dateFormat new value of dateFormat
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * Get the value of mondrianCacheExpiryPeriod
	 *
	 * @return the value of mondrianCacheExpiryPeriod
	 */
	public int getMondrianCacheExpiryPeriod() {
		return mondrianCacheExpiryPeriod;
	}

	/**
	 * Set the value of mondrianCacheExpiryPeriod
	 *
	 * @param mondrianCacheExpiryPeriod new value of mondrianCacheExpiryPeriod
	 */
	public void setMondrianCacheExpiryPeriod(int mondrianCacheExpiryPeriod) {
		this.mondrianCacheExpiryPeriod = mondrianCacheExpiryPeriod;
	}

	/**
	 * Get the value of rssLink
	 *
	 * @return the value of rssLink
	 */
	public String getRssLink() {
		return rssLink;
	}

	/**
	 * Set the value of rssLink
	 *
	 * @param rssLink new value of rssLink
	 */
	public void setRssLink(String rssLink) {
		this.rssLink = rssLink;
	}

	/**
	 * Get the value of showHeaderInPublicUserSession
	 *
	 * @return the value of showHeaderInPublicUserSession
	 */
	public boolean isShowHeaderInPublicUserSession() {
		return showHeaderInPublicUserSession;
	}

	/**
	 * Set the value of showHeaderInPublicUserSession
	 *
	 * @param showHeaderInPublicUserSession new value of
	 * showHeaderInPublicUserSession
	 */
	public void setShowHeaderInPublicUserSession(boolean showHeaderInPublicUserSession) {
		this.showHeaderInPublicUserSession = showHeaderInPublicUserSession;
	}

	/**
	 * Get the value of maxRowsSpecific
	 *
	 * @return the value of maxRowsSpecific
	 */
	public String getMaxRowsSpecific() {
		return maxRowsSpecific;
	}

	/**
	 * Set the value of maxRowsSpecific
	 *
	 * @param maxRowsSpecific new value of maxRowsSpecific
	 */
	public void setMaxRowsSpecific(String maxRowsSpecific) {
		this.maxRowsSpecific = maxRowsSpecific;
	}

	/**
	 * Get the value of maxRowsDefault
	 *
	 * @return the value of maxRowsDefault
	 */
	public int getMaxRowsDefault() {
		return maxRowsDefault;
	}

	/**
	 * Set the value of maxRowsDefault
	 *
	 * @param maxRowsDefault new value of maxRowsDefault
	 */
	public void setMaxRowsDefault(int maxRowsDefault) {
		this.maxRowsDefault = maxRowsDefault;
	}

	/**
	 * Get the value of pdfFontEmbedded
	 *
	 * @return the value of pdfFontEmbedded
	 */
	public boolean isPdfFontEmbedded() {
		return pdfFontEmbedded;
	}

	/**
	 * Set the value of pdfFontEmbedded
	 *
	 * @param pdfFontEmbedded new value of pdfFontEmbedded
	 */
	public void setPdfFontEmbedded(boolean pdfFontEmbedded) {
		this.pdfFontEmbedded = pdfFontEmbedded;
	}

	/**
	 * Get the value of pdfFontEncoding
	 *
	 * @return the value of pdfFontEncoding
	 */
	public String getPdfFontEncoding() {
		return pdfFontEncoding;
	}

	/**
	 * Set the value of pdfFontEncoding
	 *
	 * @param pdfFontEncoding new value of pdfFontEncoding
	 */
	public void setPdfFontEncoding(String pdfFontEncoding) {
		this.pdfFontEncoding = pdfFontEncoding;
	}

	/**
	 * Get the value of pdfFontDirectory
	 *
	 * @return the value of pdfFontDirectory
	 */
	public String getPdfFontDirectory() {
		return pdfFontDirectory;
	}

	/**
	 * Set the value of pdfFontDirectory
	 *
	 * @param pdfFontDirectory new value of pdfFontDirectory
	 */
	public void setPdfFontDirectory(String pdfFontDirectory) {
		this.pdfFontDirectory = pdfFontDirectory;
	}

	/**
	 * Get the value of pdfFontFile
	 *
	 * @return the value of pdfFontFile
	 */
	public String getPdfFontFile() {
		return pdfFontFile;
	}

	/**
	 * Set the value of pdfFontFile
	 *
	 * @param pdfFontFile new value of pdfFontFile
	 */
	public void setPdfFontFile(String pdfFontFile) {
		this.pdfFontFile = pdfFontFile;
	}

	/**
	 * Get the value of pdfFontName
	 *
	 * @return the value of pdfFontName
	 */
	public String getPdfFontName() {
		return pdfFontName;
	}

	/**
	 * Set the value of pdfFontName
	 *
	 * @param pdfFontName new value of pdfFontName
	 */
	public void setPdfFontName(String pdfFontName) {
		this.pdfFontName = pdfFontName;
	}

	/**
	 * Get the value of smtpPort
	 *
	 * @return the value of smtpPort
	 */
	public int getSmtpPort() {
		return smtpPort;
	}

	/**
	 * Set the value of smtpPort
	 *
	 * @param smtpPort new value of smtpPort
	 */
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	/**
	 * Get the value of smtpPassword
	 *
	 * @return the value of smtpPassword
	 */
	public String getSmtpPassword() {
		return smtpPassword;
	}

	/**
	 * Set the value of smtpPassword
	 *
	 * @param smtpPassword new value of smtpPassword
	 */
	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}

	/**
	 * Get the value of smtpUsername
	 *
	 * @return the value of smtpUsername
	 */
	public String getSmtpUsername() {
		return smtpUsername;
	}

	/**
	 * Set the value of smtpUsername
	 *
	 * @param smtpUsername new value of smtpUsername
	 */
	public void setSmtpUsername(String smtpUsername) {
		this.smtpUsername = smtpUsername;
	}

	/**
	 * Get the value of smtpServer
	 *
	 * @return the value of smtpServer
	 */
	public String getSmtpServer() {
		return smtpServer;
	}

	/**
	 * Set the value of smtpServer
	 *
	 * @param smtpServer new value of smtpServer
	 */
	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	/**
	 * Get the value of administratorEmail
	 *
	 * @return the value of administratorEmail
	 */
	public String getAdministratorEmail() {
		return administratorEmail;
	}

	/**
	 * Set the value of administratorEmail
	 *
	 * @param administratorEmail new value of administratorEmail
	 */
	public void setAdministratorEmail(String administratorEmail) {
		this.administratorEmail = administratorEmail;
	}

	/**
	 * @return the errorNotificationTo
	 */
	public String getErrorNotificationTo() {
		return errorNotificationTo;
	}

	/**
	 * @param errorNotificationTo the errorNotificationTo to set
	 */
	public void setErrorNotificationTo(String errorNotificationTo) {
		this.errorNotificationTo = errorNotificationTo;
	}

	/**
	 * @return the errorNotificationFrom
	 */
	public String getErrorNotificationFrom() {
		return errorNotificationFrom;
	}

	/**
	 * @param errorNotificationFrom the errorNotificationFrom to set
	 */
	public void setErrorNotificationFrom(String errorNotificationFrom) {
		this.errorNotificationFrom = errorNotificationFrom;
	}

	/**
	 * @return the errorNotificationSubjectPattern
	 */
	public String getErrorNotificationSubjectPattern() {
		return errorNotificationSubjectPattern;
	}

	/**
	 * @param errorNotificationSubjectPattern the
	 * errorNotificationSubjectPattern to set
	 */
	public void setErrorNotificationSubjectPattern(String errorNotificationSubjectPattern) {
		this.errorNotificationSubjectPattern = errorNotificationSubjectPattern;
	}

	/**
	 * @return the errorNotificatonLevel
	 */
	public LoggerLevel getErrorNotificatonLevel() {
		return errorNotificatonLevel;
	}

	/**
	 * @param errorNotificatonLevel the errorNotificatonLevel to set
	 */
	public void setErrorNotificatonLevel(LoggerLevel errorNotificatonLevel) {
		this.errorNotificatonLevel = errorNotificatonLevel;
	}

	/**
	 * @return the errorNotificationLogger
	 */
	public String getErrorNotificationLogger() {
		return errorNotificationLogger;
	}

	/**
	 * @param errorNotificationLogger the errorNotificationLogger to set
	 */
	public void setErrorNotificationLogger(String errorNotificationLogger) {
		this.errorNotificationLogger = errorNotificationLogger;
	}

	/**
	 * @return the errorNotificationSuppressAfter
	 */
	public String getErrorNotificationSuppressAfter() {
		return errorNotificationSuppressAfter;
	}

	/**
	 * @param errorNotificationSuppressAfter the errorNotificationSuppressAfter
	 * to set
	 */
	public void setErrorNotificationSuppressAfter(String errorNotificationSuppressAfter) {
		this.errorNotificationSuppressAfter = errorNotificationSuppressAfter;
	}

	/**
	 * @return the errorNotificationExpireAfter
	 */
	public String getErrorNotificationExpireAfter() {
		return errorNotificationExpireAfter;
	}

	/**
	 * @param errorNotificationExpireAfter the errorNotificationExpireAfter to
	 * set
	 */
	public void setErrorNotificationExpireAfter(String errorNotificationExpireAfter) {
		this.errorNotificationExpireAfter = errorNotificationExpireAfter;
	}

	/**
	 * @return the errorNotificationDigestFrequency
	 */
	public String getErrorNotificationDigestFrequency() {
		return errorNotificationDigestFrequency;
	}

	/**
	 * @param errorNotificationDigestFrequency the
	 * errorNotificationDigestFrequency to set
	 */
	public void setErrorNotificationDigestFrequency(String errorNotificationDigestFrequency) {
		this.errorNotificationDigestFrequency = errorNotificationDigestFrequency;
	}

	/**
	 * @return the passwordMinLength
	 */
	public int getPasswordMinLength() {
		return passwordMinLength;
	}

	/**
	 * @param passwordMinLength the passwordMinLength to set
	 */
	public void setPasswordMinLength(int passwordMinLength) {
		this.passwordMinLength = passwordMinLength;
	}

	/**
	 * @return the passwordMinLowercase
	 */
	public int getPasswordMinLowercase() {
		return passwordMinLowercase;
	}

	/**
	 * @param passwordMinLowercase the passwordMinLowercase to set
	 */
	public void setPasswordMinLowercase(int passwordMinLowercase) {
		this.passwordMinLowercase = passwordMinLowercase;
	}

	/**
	 * @return the passwordMinUppercase
	 */
	public int getPasswordMinUppercase() {
		return passwordMinUppercase;
	}

	/**
	 * @param passwordMinUppercase the passwordMinUppercase to set
	 */
	public void setPasswordMinUppercase(int passwordMinUppercase) {
		this.passwordMinUppercase = passwordMinUppercase;
	}

	/**
	 * @return the passwordMinNumeric
	 */
	public int getPasswordMinNumeric() {
		return passwordMinNumeric;
	}

	/**
	 * @param passwordMinNumeric the passwordMinNumeric to set
	 */
	public void setPasswordMinNumeric(int passwordMinNumeric) {
		this.passwordMinNumeric = passwordMinNumeric;
	}

	/**
	 * @return the passwordMinSpecial
	 */
	public int getPasswordMinSpecial() {
		return passwordMinSpecial;
	}

	/**
	 * @param passwordMinSpecial the passwordMinSpecial to set
	 */
	public void setPasswordMinSpecial(int passwordMinSpecial) {
		this.passwordMinSpecial = passwordMinSpecial;
	}

	/**
	 * Decrypt password fields
	 *
	 * @throws java.lang.Exception
	 */
	public void decryptPasswords() throws Exception {
		smtpPassword = AesEncryptor.decrypt(smtpPassword);
		ldapBindPassword = AesEncryptor.decrypt(ldapBindPassword);
	}

	/**
	 * Encrypt password fields
	 *
	 * @throws java.lang.Exception
	 */
	public void encryptPasswords() throws Exception {
		String key = null;
		EncryptionPassword encryptionPassword = null;
		encryptPasswords(key, encryptionPassword);
	}

	/**
	 * Encrypt password fields
	 *
	 * @param key the key to use. If null, the current key will be used
	 * @param encryptionPassword the encryption password configuration. null if
	 * to use current.
	 * @throws java.lang.Exception
	 */
	public void encryptPasswords(String key, EncryptionPassword encryptionPassword) throws Exception {
		smtpPassword = AesEncryptor.encrypt(smtpPassword, key, encryptionPassword);
		ldapBindPassword = AesEncryptor.encrypt(ldapBindPassword, key, encryptionPassword);
	}
}
