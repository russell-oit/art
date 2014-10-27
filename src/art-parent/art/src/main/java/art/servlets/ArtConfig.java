/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
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
package art.servlets;

import art.artdatabase.ArtDatabase;
import art.dbcp.ArtDBCPDataSource;
import art.dbutils.DbConnections;
import art.enums.ArtAuthenticationMethod;
import art.enums.ConnectionPoolLibrary;
import art.enums.DisplayNull;
import art.enums.LdapAuthenticationMethod;
import art.enums.LdapConnectionEncryptionMethod;
import art.enums.PdfPageSize;
import art.enums.ReportFormat;
import art.settings.CustomSettings;
import art.settings.Settings;
import art.utils.Encrypter;
import art.utils.SchedulerUtils;
import art.utils.UpgradeHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.FontFactory;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes and shuts down the application, including creating and shutting
 * down database connections and the quartz scheduler. Also provides methods to
 * retrieve application settings.
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ArtConfig extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ArtConfig.class);
	private static String exportPath;
	private static final ArrayList<String> reportFormats = new ArrayList<>(); //report formats available to users
	private static String appPath; //application path. to be used to get/build file paths in non-servlet classes
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat();
	private static final SimpleDateFormat timeFormatter = new SimpleDateFormat();
	private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat();
	private static String webinfPath;
	private static String artDatabaseFilePath;
	private static ArtDatabase artDbConfig;
	private static String settingsFilePath;
	private static Settings settings;
	private static final String sep = File.separator;
	private static CustomSettings customSettings;
	private static String workDirectoryPath;
	private static String artVersion;
	private static HashMap<String, Class<?>> directReportOutputClasses;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logger.info("ART is starting up");
		ArtConfigInit();
	}

	/**
	 * Stops the quartz scheduler, closes database connections and deregisters
	 * jdbc drivers
	 */
	@Override
	public void destroy() {
		logger.debug("Entering destroy");

		//shutdown quartz scheduler
		SchedulerUtils.shutdownScheduler();
		try {
			//have delay to avoid tomcat reporting that threads weren't stopped. 
			//(http://forums.terracotta.org/forums/posts/list/3479.page)
			//TODO retest
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException ex) {
			logger.error("Error", ex);
		}

		//close database connections
		DbConnections.closeAllConnections();

		//deregister jdbc drivers
		deregisterJdbcDrivers();

		logger.info("ART stopped");
	}

	private void deregisterJdbcDrivers() {
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				logger.debug("JDBC driver deregistered: {}", driver);
			} catch (SQLException ex) {
				logger.error("Error while deregistering JDBC driver: {}", driver, ex);
			}
		}
	}

	/**
	 * Initializes database connections, the quartz scheduler, application
	 * settings
	 */
	private void ArtConfigInit() {
		logger.debug("Entering ArtConfigInit");

		ServletContext ctx = getServletConfig().getServletContext();

		//save variables in application scope for access from jsp pages
		artVersion = ctx.getInitParameter("versionNumber");
		ctx.setAttribute("artVersion", artVersion);
		ctx.setAttribute("windowsDomainAuthentication", ArtAuthenticationMethod.WindowsDomain.getValue());
		ctx.setAttribute("internalAuthentication", ArtAuthenticationMethod.Internal.getValue());
		ctx.setAttribute("dateDisplayPattern", "dd-MMM-yyyy HH:mm:ss"); //format of dates displayed in tables

		//set application path
		appPath = ctx.getRealPath("");

		//set web-inf path
		webinfPath = appPath + sep + "WEB-INF" + sep;

		//load custom settings
		loadCustomSettings();

		//set show errors custom setting
		ctx.setAttribute("showErrors", customSettings.isShowErrors());

		//set work directory base path
		workDirectoryPath = webinfPath + sep + "work" + sep; //default work directory

		String customWorkDirectory = customSettings.getWorkDirectory();
		if (StringUtils.isNotBlank(customWorkDirectory)) {
			//custom work directory defined
			workDirectoryPath = customWorkDirectory;
			if (!StringUtils.right(workDirectoryPath, 1).equals(sep)) {
				workDirectoryPath = workDirectoryPath + sep;
			}

			logger.info("Using custom work directory: '{}'", workDirectoryPath);
		}

		//set export path
		exportPath = workDirectoryPath + "export" + sep; //default

		//set custom export path
		String customExportDirectory = customSettings.getExportDirectory();
		if (StringUtils.isNotBlank(customExportDirectory)) {
			//custom export directory defined
			exportPath = customExportDirectory;
			if (!StringUtils.right(exportPath, 1).equals(sep)) {
				exportPath = exportPath + sep;
			}

			logger.info("Using custom export directory: '{}'", exportPath);
		}

		//set art-database file path
		artDatabaseFilePath = workDirectoryPath + "art-database.json";

		//set settings file path
		settingsFilePath = workDirectoryPath + "art-settings.json";

		//ensure work directories exist
		createWorkDirectories();

		//TODO test load classes in @postconstruct in runreportcontroller
		//load classes for report output classes that do direct output
		//(they create output themselves and don't delegate to another entity like jasperreports)
		directReportOutputClasses = new HashMap<>();
		ClassLoader cl = this.getClass().getClassLoader();
		for (ReportFormat reportFormat : ReportFormat.list()) {
			String reportOutputClass = reportFormat.getDirectOutputClassName();
			if (reportOutputClass != null) {
				try {
					directReportOutputClasses.put(reportFormat.getValue(), cl.loadClass(reportOutputClass));
				} catch (ClassNotFoundException ex) {
					logger.error("Error while loading report output class: '{}'", reportOutputClass, ex);
				}
			}
		}

		//load settings and initialize variables
		loadSettings();

		//initialize datasources
		initializeArtDatabase();
	}

	/**
	 * Register custom fonts to be used in pdf output
	 */
	private static void registerPdfFonts(Settings pSettings) {
		//register pdf fonts. 
		//fresh registering of 661 fonts in c:\windows\fonts can take as little as 10 secs
		//re-registering already registered directory of 661 fonts takes as little as 1 sec

		if (pSettings == null) {
			return;
		}

		String pdfFontName = pSettings.getPdfFontName();
		if (StringUtils.isNotBlank(pdfFontName)) {
			//register pdf font if not already registered
			if (!FontFactory.isRegistered(pdfFontName)) {
				//font not registered. register any defined font files or directories
				String pdfFontDirectory = pSettings.getPdfFontDirectory();
				if (StringUtils.isNotBlank(pdfFontDirectory)) {
					logger.info("Registering fonts from directory: '{}'", pdfFontDirectory);
					int i = FontFactory.registerDirectory(pdfFontDirectory);
					logger.info("{} fonts registered", i);
				}

				String pdfFontFile = pSettings.getPdfFontFile();
				if (StringUtils.isNotBlank(pdfFontFile)) {
					logger.info("Registering font file: '{}'", pdfFontFile);
					FontFactory.register(pdfFontFile);
					logger.info("Font file {} registered", pdfFontFile);
				}

				//output registerd fonts
				StringBuilder sb = new StringBuilder(100);
				String newline = System.getProperty("line.separator");
				@SuppressWarnings("rawtypes")
				Set fonts = FontFactory.getRegisteredFonts();
				for (Object f : fonts) {
					sb.append(newline);
					sb.append(f);
				}
				logger.info("Registered fonts: {}", sb.toString());
			}
		}
	}

	/**
	 * Initializes the art database and report datasource connection pools, runs
	 * upgrade steps on the art database and starts the quartz scheduler
	 */
	public static void initializeArtDatabase() {

		//load art database settings
		loadArtDatabaseConfiguration();

		if (artDbConfig == null) {
			return;
		}

		try {
			//create connection pools
			DbConnections.createConnectionPools(artDbConfig);

			//upgrade art database
			String upgradeFilePath = ArtConfig.getArtTempPath() + "upgrade.txt";
			UpgradeHelper upgradeHelper = new UpgradeHelper();
			upgradeHelper.upgrade(artVersion, upgradeFilePath);
		} catch (NamingException | SQLException ex) {
			logger.error("Error", ex);
		}
		
		//create quartz scheduler
		createQuartzScheduler();

	}

	//TODO remove after refactoring
	/**
	 * Return a connection to the datasource with a given ID from the connection
	 * pool.
	 *
	 * @param i id of datasource. 0 = ART repository.
	 * @return connection to datasource or null if connection doesn't exist
	 */
	public static Connection getConnection(int i) {
		return null;
	}

	//TODO remove after refactoring
	/**
	 * Return a connection to ART repository from the pool (same as
	 * getConnection(0))
	 *
	 * @return connection to the ART repository or null if connection doesn't
	 * exist
	 */
	public static Connection getConnection() {
		return null;
	}

	/**
	 * Load art database configuration from art-database file
	 *
	 */
	private static void loadArtDatabaseConfiguration() {
		ArtDatabase artDatabase = null;

		try {
			File artDatabaseFile = new File(artDatabaseFilePath);
			if (artDatabaseFile.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				artDatabase
						= mapper.readValue(artDatabaseFile, ArtDatabase.class
						);

				//de-obfuscate password field
				artDatabase.setPassword(Encrypter.decrypt(artDatabase.getPassword()));
			} else {
				logger.info("ART Database configuration file not found");
			}
		} catch (IOException ex) {
			logger.error("Error", ex);
		}

		if (artDatabase != null) {
			artDbConfig = null;
			artDbConfig = artDatabase;

			//set defaults for invalid values
			setArtDatabaseDefaults(artDbConfig);
		}
	}

	/**
	 * Save art database configuration to file
	 *
	 * @param artDatabase
	 * @throws java.io.IOException
	 */
	public static void saveArtDatabaseConfiguration(ArtDatabase artDatabase)
			throws IOException {

		//obfuscate password field for storing
		artDatabase.setPassword(Encrypter.encrypt(artDatabase.getPassword()));

		File artDatabaseFile = new File(artDatabaseFilePath);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(artDatabaseFile, artDatabase);

		//refresh configuration and set defaults for invalid values
		loadArtDatabaseConfiguration();
	}

	/**
	 * Load application settings and set appropriate configurations
	 *
	 */
	private static void loadSettings() {
		Settings newSettings = null;

		try {
			File settingsFile = new File(settingsFilePath);
			if (settingsFile.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				newSettings = mapper.readValue(settingsFile, Settings.class);

				//de-obfuscate password fields
				newSettings.setSmtpPassword(Encrypter.decrypt(newSettings.getSmtpPassword()));
				newSettings.setLdapBindPassword(Encrypter.decrypt(newSettings.getLdapBindPassword()));
			}
		} catch (IOException ex) {
			logger.error("Error", ex);
		}

		//use default settings if error or none specified
		if (newSettings == null) {
			newSettings = new Settings();
		}

		settings = null;
		settings = newSettings;

		//set defaults for settings with invalid values
		setSettingsDefaults(settings);

		//set date formatters
		String dateFormat = settings.getDateFormat();
		timeFormatter.applyPattern("HH:mm:ss.SSS");
		dateFormatter.applyPattern(dateFormat);
		dateTimeFormatter.applyPattern(dateFormat + " " + settings.getTimeFormat());

		//set available report formats
		String reportFormatsString = settings.getReportFormats();
		String[] tmpReportFormatsArray = StringUtils.split(reportFormatsString, ",");
		String[] reportFormatsArray = StringUtils.stripAll(tmpReportFormatsArray);
		reportFormats.clear();
		if (reportFormatsArray != null) {
			reportFormats.addAll(Arrays.asList(reportFormatsArray));
		}

		//register pdf fonts 
		registerPdfFonts(settings);

		//register database authentication jdbc driver
		String driver = settings.getDatabaseAuthenticationDriver();
		if (StringUtils.isNotBlank(driver)) {
			try {
				Class.forName(driver).newInstance();
				logger.info("Database Authentication JDBC Driver Registered: {}", driver);
			} catch (Exception e) {
				logger.error("Error while registering Database Authentication JDBC Driver: {}", driver, e);
			}
		}

	}

	/**
	 * Save settings to file
	 *
	 * @param newSettings
	 * @throws IOException
	 */
	public static void saveSettings(Settings newSettings) throws IOException {
		//obfuscate password fields
		String clearTextSmtpPassword = newSettings.getSmtpPassword();
		String clearTextLdapBindPassword = newSettings.getLdapBindPassword();

		newSettings.setSmtpPassword(Encrypter.encrypt(clearTextSmtpPassword));
		newSettings.setLdapBindPassword(Encrypter.encrypt(clearTextLdapBindPassword));

		File settingsFile = new File(settingsFilePath);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(settingsFile, newSettings);

		//refresh settings and related configuration, and set defaults for invalid values
		loadSettings();
	}

	private static void createQuartzScheduler() {
		if (artDbConfig == null) {
			logger.warn("ART Database configuration not available");
			return;
		}

		String quartzFilePath = webinfPath + sep + "classes" + sep + "quartz.properties";
		SchedulerUtils.createScheduler(artDbConfig, quartzFilePath);
	}

	/**
	 * Set defaults for settings that have invalid values
	 */
	private static void setSettingsDefaults(Settings pSettings) {
		if (pSettings == null) {
			return;
		}

		if (pSettings.getSmtpPort() <= 0) {
			pSettings.setSmtpPort(25);
		}
		if (pSettings.getMaxRowsDefault() <= 0) {
			pSettings.setMaxRowsDefault(10000);
		}
		if (pSettings.getLdapPort() <= 0) {
			pSettings.setLdapPort(389);
		}
		if (StringUtils.isBlank(pSettings.getLdapUserIdAttribute())) {
			pSettings.setLdapUserIdAttribute("uid");
		}
		if (StringUtils.isBlank(pSettings.getDateFormat())) {
			pSettings.setDateFormat("dd-MMM-yyyy");
		}
		if (StringUtils.isBlank(pSettings.getTimeFormat())) {
			pSettings.setTimeFormat("HH:mm:ss");
		}
		if (StringUtils.isBlank(pSettings.getReportFormats())) {
			pSettings.setReportFormats("htmlDataTable,htmlGrid,xls,xlsx,pdf,htmlPlain,html,xlsZip,slk,slkZip,tsv,tsvZip");
		}
		if (pSettings.getMaxRunningReports() <= 0) {
			pSettings.setMaxRunningReports(1000);
		}
		if (pSettings.getArtAuthenticationMethod() == null) {
			pSettings.setArtAuthenticationMethod(ArtAuthenticationMethod.Internal);
		}
		if (pSettings.getLdapConnectionEncryptionMethod() == null) {
			pSettings.setLdapConnectionEncryptionMethod(LdapConnectionEncryptionMethod.None);
		}
		if (pSettings.getLdapAuthenticationMethod() == null) {
			pSettings.setLdapAuthenticationMethod(LdapAuthenticationMethod.Simple);
		}
		if (pSettings.getPdfPageSize() == null) {
			pSettings.setPdfPageSize(PdfPageSize.A4Landscape);
		}
		if (pSettings.getDisplayNull() == null) {
			pSettings.setDisplayNull(DisplayNull.NoNumbersAsZero);
		}
	}

	/**
	 * Set defaults for art database configuration items that have invalid
	 * values
	 *
	 * @param artDatabase
	 */
	public static void setArtDatabaseDefaults(ArtDatabase artDatabase) {
		if (artDatabase == null) {
			return;
		}

		if (artDatabase.getConnectionPoolTimeout() <= 0) {
			artDatabase.setConnectionPoolTimeout(20);
		}
		if (artDatabase.getMaxPoolConnections() <= 0) {
			artDatabase.setMaxPoolConnections(20);
		}
		if (artDatabase.getConnectionPoolLibrary() == null) {
			artDatabase.setConnectionPoolLibrary(ConnectionPoolLibrary.HikariCP);
		}

	}

	/**
	 * Set application name connection property to identify ART connections
	 *
	 * @param ds
	 */
	private static void setConnectionProperties(ArtDBCPDataSource ds) {
		String connectionName = "ART - " + ds.getPoolName();
		//ApplicationName property
		//see http://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html#setClientInfo%28java.lang.String,%20java.lang.String%29
		//has different name and maxlength for different drivers
		//maxlength mostly in the 254 range. Some exceptions include postgresql maxlength=64
		//some drivers don't seem to define it explicitly so may not support it and throw exception?
		//e.g. mysql, hsqldb

		String dbUrl = ds.getUrl();
		Properties properties = new Properties();
		if (StringUtils.startsWith(dbUrl, "jdbc:oracle")) {
			properties.put("v$session.program", connectionName);
		} else if (StringUtils.startsWith(dbUrl, "jdbc:sqlserver")) {
			properties.put("applicationName", connectionName);
		} else if (StringUtils.startsWith(dbUrl, "jdbc:jtds")) {
			properties.put("appName", connectionName);
		} else if (StringUtils.startsWith(dbUrl, "jdbc:db2") || StringUtils.startsWith(dbUrl, "jdbc:as400")) {
			//see http://publib.boulder.ibm.com/infocenter/db2luw/v9r5/index.jsp?topic=%2Fcom.ibm.db2.luw.apdv.java.doc%2Fsrc%2Ftpc%2Fimjcc_r0052001.html
			properties.put("ApplicationName", StringUtils.left(connectionName, 32));
		} else if (StringUtils.startsWith(dbUrl, "jdbc:ids") || StringUtils.startsWith(dbUrl, "jdbc:informix-sqli")) {
			//see http://publib.boulder.ibm.com/infocenter/db2luw/v9r5/index.jsp?topic=%2Fcom.ibm.db2.luw.apdv.java.doc%2Fsrc%2Ftpc%2Fimjcc_r0052001.html
			properties.put("ApplicationName", StringUtils.left(connectionName, 20));
		} else if (StringUtils.startsWith(dbUrl, "jdbc:postgresql")) {
			//see https://stackoverflow.com/questions/19224934/postgresql-how-to-set-application-name-from-jdbc-url
			properties.put("ApplicationName", connectionName);
		}

		//some drivers don't seem to define
		ds.setConnectionProperties(properties);
	}

	/**
	 * Load custom settings
	 */
	private static void loadCustomSettings() {
		CustomSettings newCustomSettings = null;

		try {
			String customSettingsFilePath = webinfPath + "art-config.json";
			File customSettingsFile = new File(customSettingsFilePath);
			if (customSettingsFile.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				newCustomSettings
						= mapper.readValue(customSettingsFile, CustomSettings.class
						);
			}
		} catch (IOException ex) {
			logger.error("Error", ex);
		}

		//use default settings if error or none specified
		if (newCustomSettings == null) {
			newCustomSettings = new CustomSettings();
		}

		customSettings = null;
		customSettings = newCustomSettings;
	}

	/**
	 * Get current custom settings
	 *
	 * @return
	 */
	public static CustomSettings getCustomSettings() {
		return customSettings;
	}

	/**
	 * Create art work directories
	 */
	private void createWorkDirectories() {
		makeDirectory(getTemplatesPath());
		makeDirectory(getArtTempPath());
		makeDirectory(getJobsExportPath());
		makeDirectory(getReportsExportPath());
	}

	/**
	 * Create a directory given a full path
	 *
	 * @param directoryPath
	 */
	private void makeDirectory(String directoryPath) {
		if (directoryPath == null) {
			return;
		}

		File directory = new File(directoryPath);

		if (!directory.exists()) {
			boolean created = directory.mkdirs();
			if (!created) {
				logger.warn("Directory not created: {}", directory);
			}
		}

	}

	public static Map<String, Class<?>> getDirectOutputReportClasses() {
		return directReportOutputClasses;
	}

	/**
	 * Determine if art database has been configured
	 *
	 * @return
	 */
	public static boolean isArtDatabaseConfigured() {
		if (artDbConfig == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get full path to the art-database file
	 *
	 * @return full path to the art-database file
	 */
	public static String getArtDatabaseFilePath() {
		return artDatabaseFilePath;
	}

	/**
	 * Get full path to the web-inf directory.
	 *
	 * @return full path to the web-inf directory
	 */
	public static String getWebinfPath() {
		return webinfPath;
	}

	/**
	 * Get full path to the hsqldb directory.
	 *
	 * @return full path to the hsqldb directory
	 */
	public static String getHsqldbPath() {
		return webinfPath + "hsqldb" + sep;
	}

	/**
	 * Get full path to the classes directory.
	 *
	 * @return full path to the classes directory
	 */
	public static String getClassesPath() {
		return webinfPath + "classes" + sep;
	}

	/**
	 * Get full path to the export directory.
	 *
	 * @return full path to the export directory
	 */
	public static String getExportPath() {
		return exportPath;
	}

	/**
	 * Get full path to the jobs export directory.
	 *
	 * @return full path to the jobs export directory
	 */
	public static String getJobsExportPath() {
		return exportPath + "jobs" + sep;
	}

	/**
	 * Get full path to the reports export directory.
	 *
	 * @return full path to the reports export directory
	 */
	public static String getReportsExportPath() {
		return exportPath + "reports" + sep;
	}

	/**
	 * Get full path to the templates directory.
	 *
	 * @return full path to the templates directory
	 */
	public static String getTemplatesPath() {
		return workDirectoryPath + "templates" + sep;
	}

	/**
	 * Get full path to the WEB-INF\tmp directory.
	 *
	 * @return full path to the templates directory
	 */
	public static String getArtTempPath() {
		return workDirectoryPath + "tmp" + sep;
	}

	/**
	 * Get the relative path to the templates directory. Used by
	 * showAnalysis.jsp
	 *
	 * @return relative path to the templates directory
	 */
	public static String getRelativeTemplatesPath() {
		return "/WEB-INF/work/templates/";
	}

	/**
	 * Get the full application path
	 *
	 * @return the full application path
	 */
	public static String getAppPath() {
		return appPath;
	}

	/**
	 * Get the art version
	 *
	 * @return the art version
	 */
	public static String getArtVersion() {
		return artVersion;
	}

	/**
	 * Determine whether a custom font should be used in pdf output
	 *
	 * @return <code>true</code> if a custom font should be used in pdf output
	 */
	public static boolean isUseCustomPdfFont() {
		if (StringUtils.isBlank(settings.getPdfFontName())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get user selectable report formats
	 *
	 * @return user selectable report formats
	 */
	public static List<String> getReportFormats() {
		return reportFormats;
	}

	/**
	 * Get the max rows for the given report format
	 *
	 * @param reportFormat
	 * @return the max rows for the given report format
	 */
	public static int getMaxRows(String reportFormat) {
		int max = settings.getMaxRowsDefault();

		String setting = settings.getMaxRowsSpecific();
		String[] maxRows = StringUtils.split(setting, ",");
		if (maxRows != null) {
			for (String maxSetting : maxRows) {
				if (StringUtils.containsIgnoreCase(maxSetting, reportFormat)) {
					String value = StringUtils.substringAfter(maxSetting, ":");
					max = NumberUtils.toInt(value, max);
					break;
				}
			}
		}

		return max;
	}

	/**
	 * Get string to be displayed in query output for a date field
	 *
	 * @param date
	 * @return
	 */
	public static String getDateDisplayString(Date date) {
		String dateString;

		if (date == null) {
			dateString = "";
		} else if (timeFormatter.format(date).equals("00:00:00.000")) {
			dateString = dateFormatter.format(date);
		} else {
			dateString = dateTimeFormatter.format(date);
		}
		return dateString;
	}

	/**
	 * Get art database configuration settings
	 *
	 * @return object with art database settings or null if art database not
	 * configured
	 */
	public static ArtDatabase getArtDbConfig() {
		return artDbConfig;
	}

	/**
	 * Get current settings
	 *
	 * @return
	 */
	public static Settings getSettings() {
		return settings;
	}

}
