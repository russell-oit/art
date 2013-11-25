package art.settings;

import art.enums.ArtAuthenticationMethod;
import art.enums.ConnectionEncryptionMethod;
import art.enums.DisplayNull;
import art.enums.LdapAuthenticationMethod;
import art.enums.PdfPageSize;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Class to hold application settings
 *
 * @author Timothy Anyona
 */
public class Settings {

	private String administratorEmail;
	private String smtpServer;
	private String smtpUsername;
	private String smtpPassword;
	private ConnectionEncryptionMethod smtpConnectionEncryptionMethod = ConnectionEncryptionMethod.None;
	private int smtpPort = 25;
	private PdfPageSize pdfPageSize = PdfPageSize.A4Landscape;
	private String pdfFontName;
	private String pdfFontFile;
	private String pdfFontDirectory;
	private String pdfFontEncoding;
	private boolean pdfFontEmbedded;
	private int maxRowsDefault = 10000;
	private String maxRowsSpecific;
	private boolean showHeaderInPublicUserSession;
	private String rssLink;
	private int mondrianCacheExpiryPeriod;
	@NotBlank
	private String dateFormat = "dd-MMM-yyyy";
	@NotBlank
	private String timeFormat = "HH:mm:ss";
	private boolean schedulingEnabled = true;
	@NotBlank
	private String availableReportFormats = "htmlDataTable,htmlGrid,xls,xlsx,pdf,htmlPlain,html,xlsZip,slk,slkZip,tsv,tsvZip";
	private int maxRunningReports = 1000;
	private DisplayNull displayNull = DisplayNull.NoNumbersAsZero;
	private ArtAuthenticationMethod artAuthenticationMethod = ArtAuthenticationMethod.Internal;
	private String windowsDomainController;
	private String allowedWindowsDomains;
	private String databaseAuthenticationDriver;
	private String databaseAuthenticationUrl;
	private String ldapServer;
	private int ldapPort = 389;
	private ConnectionEncryptionMethod ldapConnectionEncryptionMethod = ConnectionEncryptionMethod.None;
	private String ldapUrl;
	private String ldapBaseDn;
	private String ldapBindDn;
	private String ldapBindPassword;
	private String ldapUserIdAttribute = "uid";
	private LdapAuthenticationMethod ldapAuthenticationMethod = LdapAuthenticationMethod.Simple;
	private String ldapRealm;

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
	public ConnectionEncryptionMethod getLdapConnectionEncryptionMethod() {
		return ldapConnectionEncryptionMethod;
	}

	/**
	 * Set the value of ldapConnectionEncryptionMethod
	 *
	 * @param ldapConnectionEncryptionMethod new value of
	 * ldapConnectionEncryptionMethod
	 */
	public void setLdapConnectionEncryptionMethod(ConnectionEncryptionMethod ldapConnectionEncryptionMethod) {
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
	 * @param databaseAuthenticationDriver new value of databaseAuthenticationDriver
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
	 * Get the value of displayNull
	 *
	 * @return the value of displayNull
	 */
	public DisplayNull getDisplayNull() {
		return displayNull;
	}

	/**
	 * Set the value of displayNull
	 *
	 * @param displayNull new value of displayNull
	 */
	public void setDisplayNull(DisplayNull displayNull) {
		this.displayNull = displayNull;
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
	 * Get the value of availableReportFormats
	 *
	 * @return the value of availableReportFormats
	 */
	public String getAvailableReportFormats() {
		return availableReportFormats;
	}

	/**
	 * Set the value of availableReportFormats
	 *
	 * @param availableReportFormats new value of availableReportFormats
	 */
	public void setAvailableReportFormats(String availableReportFormats) {
		this.availableReportFormats = availableReportFormats;
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
	 * Get the value of pdfPageSize
	 *
	 * @return the value of pdfPageSize
	 */
	public PdfPageSize getPdfPageSize() {
		return pdfPageSize;
	}

	/**
	 * Set the value of pdfPageSize
	 *
	 * @param pdfPageSize new value of pdfPageSize
	 */
	public void setPdfPageSize(PdfPageSize pdfPageSize) {
		this.pdfPageSize = pdfPageSize;
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
	 * Get the value of smtpConnectionEncryptionMethod
	 *
	 * @return the value of smtpConnectionEncryptionMethod
	 */
	public ConnectionEncryptionMethod getSmtpConnectionEncryptionMethod() {
		return smtpConnectionEncryptionMethod;
	}

	/**
	 * Set the value of smtpConnectionEncryptionMethod
	 *
	 * @param smtpConnectionEncryptionMethod new value of
	 * smtpConnectionEncryptionMethod
	 */
	public void setSmtpConnectionEncryptionMethod(ConnectionEncryptionMethod smtpConnectionEncryptionMethod) {
		this.smtpConnectionEncryptionMethod = smtpConnectionEncryptionMethod;
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
}
