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
package art.settings;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.enums.ArtAuthenticationMethod;
import art.enums.LdapAuthenticationMethod;
import art.enums.LdapConnectionEncryptionMethod;
import art.enums.LoggerLevel;
import art.user.User;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving and updating system settings
 *
 * @author Timothy Anyona
 */
@Service
public class SettingsService {

	private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

	private final DbService dbService;

	@Autowired
	public SettingsService(DbService dbService) {
		this.dbService = dbService;
	}

	public SettingsService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_SETTINGS";

	/**
	 * Maps a resultset to an object
	 */
	private class SettingsMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			Settings settings = new Settings();

			settings.setSmtpServer(rs.getString("SMTP_SERVER"));
			settings.setSmtpPort(rs.getInt("SMTP_PORT"));
			settings.setSmtpUseStartTls(rs.getBoolean("SMTP_USE_STARTTLS"));
			settings.setUseSmtpAuthentication(rs.getBoolean("USE_SMTP_AUTHENTICATION"));
			settings.setSmtpUsername(rs.getString("SMTP_USERNAME"));
			settings.setSmtpPassword(rs.getString("SMTP_PASSWORD"));
			settings.setSmtpFrom(rs.getString("SMTP_FROM"));
			settings.setArtAuthenticationMethod(ArtAuthenticationMethod.toEnum(rs.getString("ART_AUTHENTICATION_METHOD")));
			settings.setWindowsDomainController(rs.getString("WINDOWS_DOMAIN_CONTROLLER"));
			settings.setAllowedWindowsDomains(rs.getString("ALLOWED_WINDOWS_DOMAINS"));
			settings.setDatabaseAuthenticationDriver(rs.getString("DB_AUTHENTICATION_DRIVER"));
			settings.setDatabaseAuthenticationUrl(rs.getString("DB_AUTHENTICATION_URL"));
			settings.setLdapServer(rs.getString("LDAP_SERVER"));
			settings.setLdapPort(rs.getInt("LDAP_PORT"));
			settings.setLdapConnectionEncryptionMethod(LdapConnectionEncryptionMethod.toEnum(rs.getString("LDAP_ENCRYPTION_METHOD")));
			settings.setLdapUrl(rs.getString("LDAP_URL"));
			settings.setLdapBaseDn(rs.getString("LDAP_BASE_DN"));
			settings.setUseLdapAnonymousBind(rs.getBoolean("USE_LDAP_ANONYMOUS_BIND"));
			settings.setLdapBindDn(rs.getString("LDAP_BIND_DN"));
			settings.setLdapBindPassword(rs.getString("LDAP_BIND_PASSWORD"));
			settings.setLdapUserIdAttribute(rs.getString("LDAP_USER_ID_ATTRIBUTE"));
			settings.setLdapAuthenticationMethod(LdapAuthenticationMethod.toEnum(rs.getString("LDAP_AUTHENTICATION_METHOD")));
			settings.setLdapRealm(rs.getString("LDAP_REALM"));
			settings.setCasLogoutUrl(rs.getString("CAS_LOGOUT_URL"));
			settings.setMaxRowsDefault(rs.getInt("MAX_ROWS_DEFAULT"));
			settings.setMaxRowsSpecific(rs.getString("MAX_ROWS_SPECIFIC"));
			settings.setPdfFontName(rs.getString("PDF_FONT_NAME"));
			settings.setPdfFontFile(rs.getString("PDF_FONT_FILE"));
			settings.setPdfFontDirectory(rs.getString("PDF_FONT_DIRECTORY"));
			settings.setPdfFontEncoding(rs.getString("PDF_FONT_ENCODING"));
			settings.setPdfFontEmbedded(rs.getBoolean("PDF_FONT_EMBEDDED"));
			settings.setAdministratorEmail(rs.getString("ADMIN_EMAIL"));
			settings.setDateFormat(rs.getString("APP_DATE_FORMAT"));
			settings.setTimeFormat(rs.getString("APP_TIME_FORMAT"));
			settings.setReportFormats(rs.getString("REPORT_FORMATS"));
			settings.setMaxRunningReports(rs.getInt("MAX_RUNNING_REPORTS"));
			settings.setShowHeaderInPublicUserSession(rs.getBoolean("HEADER_IN_PUBLIC_SESSION"));
			settings.setMondrianCacheExpiryPeriod(rs.getInt("MONDRIAN_CACHE_EXPIRY"));
			settings.setSchedulingEnabled(rs.getBoolean("SCHEDULING_ENABLED"));
			settings.setRssLink(rs.getString("RSS_LINK"));
			settings.setMaxFileUploadSizeMB(rs.getInt("MAX_FILE_UPLOAD_SIZE"));
			settings.setArtBaseUrl(rs.getString("ART_BASE_URL"));
			settings.setSystemLocale(rs.getString("SYSTEM_LOCALE"));
			settings.setLogsDatasourceId(rs.getInt("LOGS_DATASOURCE_ID"));
			settings.setErrorNotificationTo(rs.getString("ERROR_EMAIL_TO"));
			settings.setErrorNotificationFrom(rs.getString("ERROR_EMAIL_FROM"));
			settings.setErrorNotificationSubjectPattern(rs.getString("ERROR_EMAIL_SUBJECT_PATTERN"));
			settings.setErrorNotificatonLevel(LoggerLevel.toEnum(rs.getString("ERROR_EMAIL_LEVEL")));
			settings.setErrorNotificationLogger(rs.getString("ERROR_EMAIL_LOGGER"));
			settings.setErrorNotificationSuppressAfter(rs.getString("ERROR_EMAIL_SUPPRESS_AFTER"));
			settings.setErrorNotificationExpireAfter(rs.getString("ERROR_EMAIL_EXPIRE_AFTER"));
			settings.setErrorNotificationDigestFrequency(rs.getString("ERROR_EMAIL_DIGEST_FREQUENCY"));
			settings.setPasswordMinLength(rs.getInt("PASSWORD_MIN_LENGTH"));
			settings.setPasswordMinLowercase(rs.getInt("PASSWORD_MIN_LOWERCASE"));
			settings.setPasswordMinUppercase(rs.getInt("PASSWORD_MIN_UPPERCASE"));
			settings.setPasswordMinNumeric(rs.getInt("PASSWORD_MIN_NUMERIC"));
			settings.setPasswordMinSpecial(rs.getInt("PASSWORD_MIN_SPECIAL"));
			settings.setJwtTokenExpiryMins(rs.getInt("JWT_TOKEN_EXPIRY"));
			settings.setEnableDirectReportEmailing(rs.getBoolean("DIRECT_REPORT_EMAILING"));
			settings.setJsonOptions(rs.getString("JSON_OPTIONS"));
			settings.setDateTimeFormat(rs.getString("APP_DATETIME_FORMAT"));
			settings.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			settings.setUpdatedBy(rs.getString("UPDATED_BY"));

			try {
				settings.decryptPasswords();
			} catch (Exception ex) {
				logger.error("Error", ex);
			}

			return type.cast(settings);
		}
	}

	/**
	 * Returns system settings
	 *
	 * @return system settings
	 * @throws SQLException
	 */
	public Settings getSettings() throws SQLException {
		logger.debug("Entering getSettings");

		ResultSetHandler<Settings> h = new BeanHandler<>(Settings.class, new SettingsMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Updates system settings
	 *
	 * @param settings the updated settings
	 * @param actionUser the user performing the action
	 * @throws SQLException
	 */
	public void updateSettings(Settings settings, User actionUser) throws SQLException {
		Connection conn = null;
		updateSettings(settings, actionUser, conn);
	}

	/**
	 * Updates system settings
	 *
	 * @param settings the updated settings
	 * @param actionUser the user performing the action
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	public void updateSettings(Settings settings, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering updateSettings: actionUser={}", actionUser);

		saveSettings(settings, actionUser, conn);
	}

	/**
	 * Imports system settings
	 *
	 * @param settings the settings to import
	 * @param actionUser the user performing the action
	 * @param conn the connection to use. it will by closed by the method
	 * @throws SQLException
	 */
	public void importSettings(Settings settings, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering importSettings: actionUser={}", actionUser);

		boolean originalAutoCommit = true;

		try {
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			saveSettings(settings, actionUser, conn);
			conn.commit();
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	/**
	 * Saves system settings
	 *
	 * @param settings the updated settings
	 * @param actionUser the user performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	private void saveSettings(Settings settings, User actionUser, Connection conn)
			throws SQLException {

		logger.debug("Entering saveSettings: actionUser={}", actionUser);

		String sql;

		sql = "DELETE FROM ART_SETTINGS";
		dbService.update(conn, sql);

		setSettingsDefaults(settings);

		Integer logsDatasourceId = settings.getLogsDatasourceId();
		if (logsDatasourceId == 0) {
			logsDatasourceId = null;
		}

		sql = "INSERT INTO ART_SETTINGS"
				+ " (SMTP_SERVER, SMTP_PORT, SMTP_USE_STARTTLS,"
				+ " USE_SMTP_AUTHENTICATION, SMTP_USERNAME, SMTP_PASSWORD,"
				+ " SMTP_FROM, ART_AUTHENTICATION_METHOD, WINDOWS_DOMAIN_CONTROLLER,"
				+ " ALLOWED_WINDOWS_DOMAINS, DB_AUTHENTICATION_DRIVER,"
				+ " DB_AUTHENTICATION_URL, LDAP_SERVER, LDAP_PORT,"
				+ " LDAP_ENCRYPTION_METHOD, LDAP_URL, LDAP_BASE_DN,"
				+ " USE_LDAP_ANONYMOUS_BIND, LDAP_BIND_DN, LDAP_BIND_PASSWORD,"
				+ " LDAP_USER_ID_ATTRIBUTE, LDAP_AUTHENTICATION_METHOD,"
				+ " LDAP_REALM, CAS_LOGOUT_URL, MAX_ROWS_DEFAULT,"
				+ " MAX_ROWS_SPECIFIC, PDF_FONT_NAME, PDF_FONT_FILE,"
				+ " PDF_FONT_DIRECTORY, PDF_FONT_ENCODING, PDF_FONT_EMBEDDED,"
				+ " ADMIN_EMAIL, APP_DATE_FORMAT, APP_TIME_FORMAT, REPORT_FORMATS,"
				+ " MAX_RUNNING_REPORTS, HEADER_IN_PUBLIC_SESSION,"
				+ " MONDRIAN_CACHE_EXPIRY, SCHEDULING_ENABLED, RSS_LINK,"
				+ " MAX_FILE_UPLOAD_SIZE, ART_BASE_URL, SYSTEM_LOCALE,"
				+ " LOGS_DATASOURCE_ID, ERROR_EMAIL_TO, ERROR_EMAIL_FROM,"
				+ " ERROR_EMAIL_SUBJECT_PATTERN, ERROR_EMAIL_LEVEL,"
				+ " ERROR_EMAIL_LOGGER, ERROR_EMAIL_SUPPRESS_AFTER,"
				+ " ERROR_EMAIL_EXPIRE_AFTER, ERROR_EMAIL_DIGEST_FREQUENCY,"
				+ " PASSWORD_MIN_LENGTH, PASSWORD_MIN_LOWERCASE, PASSWORD_MIN_UPPERCASE,"
				+ " PASSWORD_MIN_NUMERIC, PASSWORD_MIN_SPECIAL, JWT_TOKEN_EXPIRY,"
				+ " DIRECT_REPORT_EMAILING, JSON_OPTIONS, APP_DATETIME_FORMAT,"
				+ " UPDATE_DATE, UPDATED_BY)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 63) + ")";

		Object[] values = {
			settings.getSmtpServer(),
			settings.getSmtpPort(),
			BooleanUtils.toInteger(settings.isSmtpUseStartTls()),
			BooleanUtils.toInteger(settings.isUseSmtpAuthentication()),
			settings.getSmtpUsername(),
			settings.getSmtpPassword(),
			settings.getSmtpFrom(),
			settings.getArtAuthenticationMethod().getValue(),
			settings.getWindowsDomainController(),
			settings.getAllowedWindowsDomains(),
			settings.getDatabaseAuthenticationDriver(),
			settings.getDatabaseAuthenticationUrl(),
			settings.getLdapServer(),
			settings.getLdapPort(),
			settings.getLdapConnectionEncryptionMethod().getValue(),
			settings.getLdapUrl(),
			settings.getLdapBaseDn(),
			BooleanUtils.toInteger(settings.isUseLdapAnonymousBind()),
			settings.getLdapBindDn(),
			settings.getLdapBindPassword(),
			settings.getLdapUserIdAttribute(),
			settings.getLdapAuthenticationMethod().getValue(),
			settings.getLdapRealm(),
			settings.getCasLogoutUrl(),
			settings.getMaxRowsDefault(),
			settings.getMaxRowsSpecific(),
			settings.getPdfFontName(),
			settings.getPdfFontFile(),
			settings.getPdfFontDirectory(),
			settings.getPdfFontEncoding(),
			BooleanUtils.toInteger(settings.isPdfFontEmbedded()),
			settings.getAdministratorEmail(),
			settings.getDateFormat(),
			settings.getTimeFormat(),
			settings.getReportFormats(),
			settings.getMaxRunningReports(),
			BooleanUtils.toInteger(settings.isShowHeaderInPublicUserSession()),
			settings.getMondrianCacheExpiryPeriod(),
			BooleanUtils.toInteger(settings.isSchedulingEnabled()),
			settings.getRssLink(),
			settings.getMaxFileUploadSizeMB(),
			settings.getArtBaseUrl(),
			settings.getSystemLocale(),
			logsDatasourceId,
			settings.getErrorNotificationTo(),
			settings.getErrorNotificationFrom(),
			settings.getErrorNotificationSubjectPattern(),
			settings.getErrorNotificatonLevel().getValue(),
			settings.getErrorNotificationLogger(),
			settings.getErrorNotificationSuppressAfter(),
			settings.getErrorNotificationExpireAfter(),
			settings.getErrorNotificationDigestFrequency(),
			settings.getPasswordMinLength(),
			settings.getPasswordMinLowercase(),
			settings.getPasswordMinUppercase(),
			settings.getPasswordMinNumeric(),
			settings.getPasswordMinSpecial(),
			settings.getJwtTokenExpiryMins(),
			BooleanUtils.toInteger(settings.isEnableDirectReportEmailing()),
			settings.getJsonOptions(),
			settings.getDateTimeFormat(),
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			actionUser.getUsername()
		};

		int affectedRows;
		affectedRows = dbService.update(conn, sql, values);

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}", affectedRows);
		}
	}

	/**
	 * Sets default values for some settings
	 *
	 * @param settings the settings object to set
	 */
	public void setSettingsDefaults(Settings settings) {
		if (settings == null) {
			return;
		}

		if (settings.getSmtpPort() <= 0) {
			settings.setSmtpPort(25);
		}
		if (settings.getMaxRowsDefault() <= 0) {
			settings.setMaxRowsDefault(10000);
		}
		if (settings.getLdapPort() <= 0) {
			settings.setLdapPort(389);
		}
		if (StringUtils.isBlank(settings.getLdapUserIdAttribute())) {
			settings.setLdapUserIdAttribute("uid");
		}
		if (StringUtils.isBlank(settings.getDateFormat())) {
			settings.setDateFormat("dd-MMM-yyyy");
		}
		if (StringUtils.isBlank(settings.getDateTimeFormat())) {
			settings.setDateTimeFormat("dd-MMM-yyyy HH:mm:ss");
		}
		if (StringUtils.isBlank(settings.getTimeFormat())) {
			settings.setTimeFormat("HH:mm:ss");
		}
		if (StringUtils.isBlank(settings.getReportFormats())) {
			settings.setReportFormats("htmlDataTable,htmlGrid,xlsx,pdf,docx,htmlPlain,pivotTableJs,c3,plotly");
		}
		if (settings.getMaxRunningReports() <= 0) {
			settings.setMaxRunningReports(1000);
		}
		if (settings.getArtAuthenticationMethod() == null) {
			settings.setArtAuthenticationMethod(ArtAuthenticationMethod.Internal);
		}
		if (settings.getLdapConnectionEncryptionMethod() == null) {
			settings.setLdapConnectionEncryptionMethod(LdapConnectionEncryptionMethod.None);
		}
		if (settings.getLdapAuthenticationMethod() == null) {
			settings.setLdapAuthenticationMethod(LdapAuthenticationMethod.Simple);
		}
		if (StringUtils.isBlank(settings.getErrorNotificationSubjectPattern())) {
			settings.setErrorNotificationSubjectPattern("ART [%level]: %logger - %m");
		}
		if (settings.getErrorNotificatonLevel() == null) {
			settings.setErrorNotificatonLevel(LoggerLevel.ERROR);
		}
		if (StringUtils.isBlank(settings.getErrorNotificationSuppressAfter())) {
			settings.setErrorNotificationSuppressAfter("3 in 5 minutes");
		}
		if (StringUtils.isBlank(settings.getErrorNotificationExpireAfter())) {
			settings.setErrorNotificationExpireAfter("5 minutes");
		}
		if (StringUtils.isBlank(settings.getErrorNotificationDigestFrequency())) {
			settings.setErrorNotificationDigestFrequency("30 minutes");
		}
	}

}
