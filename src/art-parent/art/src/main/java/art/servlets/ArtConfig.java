/**
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
import art.dbutils.DbService;
import art.dbutils.DbUtils;
import art.enums.ArtAuthenticationMethod;
import art.enums.DisplayNull;
import art.enums.LdapAuthenticationMethod;
import art.enums.LdapConnectionEncryptionMethod;
import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.enums.PdfPageSize;
import art.settings.CustomSettings;
import art.settings.Settings;
import art.utils.ArtJob;
import art.utils.ArtUtils;
import art.utils.Encrypter;
import art.utils.QuartzProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.FontFactory;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.CronExpression;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import static org.quartz.JobKey.jobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that initializes datasource connections and application settings.
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ArtConfig extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ArtConfig.class);
	private static String exportPath;
	private static LinkedHashMap<Integer, ArtDBCPDataSource> dataSources; //use a LinkedHashMap that should store items sorted as per the order the items are inserted in the map...
	private static final ArrayList<String> reportFormats = new ArrayList<>(); //report formats available to users
	private static String appPath; //application path. to be used to get/build file paths in non-servlet classes
	private static org.quartz.Scheduler scheduler; //to allow access to scheduler from non-servlet classes
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat();
	private static final SimpleDateFormat timeFormatter = new SimpleDateFormat();
	private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat();
	private static String webinfPath;
	private static String artDatabaseFilePath;
	private static ArtDatabase artDbConfig;
	private static String settingsFilePath;
	private static Settings settings;
	private static final String sep = java.io.File.separator;
	private static CustomSettings customSettings;
	private static String workDirectoryPath;
	private static String artVersion;
	private static HashMap<String, Class<?>> directOutputReportClasses;

	/**
	 * {@inheritDoc}
	 *
	 * @param config {@inheritDoc}
	 * @throws ServletException
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		logger.info("ART is starting up");

		ArtConfigInit();
	}

	/**
	 * Stop quartz scheduler and close datasource connections
	 */
	@Override
	public void destroy() {

		try {
			//shutdown quartz scheduler
			if (scheduler != null) {
				scheduler.shutdown();
				Thread.sleep(1000); //allow delay to avoid tomcat reporting that threads weren't stopped. (http://forums.terracotta.org/forums/posts/list/3479.page)
			}

			//close database connections
			DbConnections.closeAllConnections();

			//deregister jdbc drivers
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
		} catch (SchedulerException | InterruptedException ex) {
			logger.error("Error", ex);
		}

		logger.info("ART Stopped.");
	}

	/**
	 * Initialize datasources, viewModes, quartz scheduler, application settings
	 */
	private void ArtConfigInit() {

		logger.debug("Initializing variables");

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

		//list report output classes that do direct output (they create output themselves and don't delegate to another entity e.g. jasperreports)
		ArrayList<String> directOutputReportClassNames = new ArrayList<>();
		directOutputReportClassNames.add("htmlDataTable");
		directOutputReportClassNames.add("htmlGrid");
		directOutputReportClassNames.add("xls");
		directOutputReportClassNames.add("xlsx");
		directOutputReportClassNames.add("pdf");
		directOutputReportClassNames.add("htmlPlain");
		directOutputReportClassNames.add("html");
		directOutputReportClassNames.add("xlsZip");
		directOutputReportClassNames.add("slk");
		directOutputReportClassNames.add("slkZip");
		directOutputReportClassNames.add("tsv");
		directOutputReportClassNames.add("tsvZip");
		directOutputReportClassNames.add("tsvGz");
		directOutputReportClassNames.add("xml");
		directOutputReportClassNames.add("rss20");

		//load classes for all report formats
		directOutputReportClasses = new HashMap<>(directOutputReportClassNames.size());
		ClassLoader cl = this.getClass().getClassLoader();
		for (String reportOutputClass : directOutputReportClassNames) {
			try {
				directOutputReportClasses.put(reportOutputClass, cl.loadClass("art.output." + reportOutputClass + "Output"));
			} catch (ClassNotFoundException ex) {
				logger.error("Error while loading report output class: {}", reportOutputClass, ex);
			}
		}

		//load settings and initialize variables
		loadSettings();

		//initialize datasources
		initializeDatasources();
	}

	public static Map<String, Class<?>> getDirectOutputReportClasses() {
		return directOutputReportClasses;
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
					logger.info("Registering fonts from directory: {}", pdfFontDirectory);
					int i = FontFactory.registerDirectory(pdfFontDirectory);
					logger.info("{} fonts registered", i);
				}

				String pdfFontFile = pSettings.getPdfFontFile();
				if (StringUtils.isNotBlank(pdfFontFile)) {
					logger.info("Registering font file: {}", pdfFontFile);
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
	 * Initialize art repository datasource, and other defined datasources
	 */
	private static void initializeDatasources() {

		//load art database settings
		loadArtDatabaseConfiguration();

		if (artDbConfig == null) {
			return;
		}

		try {
			//create connection pools
			DbConnections.createConnectionPools(artDbConfig);

			//create quartz scheduler
			createQuartzScheduler();

			//migrate existing jobs to quartz, if any exist from previous art versions
			//quartz scheduler needs to be available
			migrateJobsToQuartz();

			//run additional steps
			upgrade();
		} catch (NamingException | SQLException ex) {
			logger.error("Error", ex);
		}

	}

	//TODO remove after refactoring
	public static String getArtSetting(String value) {
		return value;
	}

	//TODO remove after refactoring
	public static boolean isArtSettingsLoaded() {
		return false;
	}

	//TODO remove after refactoring
	public static String getSettingsFilePath() {
		return "";
	}

	//TODO remove after refactoring
	public static boolean loadArtSettings() {
		return false;
	}

	//TODO remove after refactoring
	public static boolean isShowResultsInline() {
		return false;
	}

	/**
	 * Determine if art database has been configured
	 *
	 * @return
	 */
	public static boolean isArtDatabaseConfigured() {
		if (artDbConfig != null) {
			return true;
		} else {
			return false;
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
	 * Return a connection to the datasource with a given ID from the connection
	 * pool.
	 *
	 * @param i id of datasource. 0 = ART repository.
	 * @return connection to datasource or null if connection doesn't exist
	 */
	public static Connection getConnection(int i) {
		Connection conn = null;

		try {
			if (dataSources != null) {
				ArtDBCPDataSource ds = dataSources.get(Integer.valueOf(i));
				conn = ds.getConnection(); // i=0 => ART Repository
			}
		} catch (Exception e) {
			logger.error("Error while getting connection for datasource: {}", i, e);
		}

		return conn;
	}

	/**
	 * Return a connection to ART repository from the pool (same as
	 * getConnection(0))
	 *
	 * @return connection to the ART repository or null if connection doesn't
	 * exist
	 */
	public static Connection getConnection() {
		return getConnection(0); // i=0 => ART Repository
	}

	/**
	 * Get a datasource connection based on the datasource name.
	 *
	 * @param name datasource name
	 * @return connection to the datasource or null if connection doesn't exist
	 */
	public static Connection getConnection(String name) {
		Connection conn = null;

		try {
			if (dataSources != null) {
				for (ArtDBCPDataSource ds : dataSources.values()) {
					if (ds != null) {
						if (StringUtils.equalsIgnoreCase(name, ds.getPoolName())) {
							//this is the required datasource. get connection and exit loop
							conn = ds.getConnection();
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while getting connection for datasource: {}", name, e);
		}

		return conn;
	}

	/**
	 * Return a normal JDBC connection to the ART repository with autocommit
	 * disabled (used for Admins)
	 *
	 * @return connection to the ART repository with autocommit disabled
	 * @throws java.sql.SQLException
	 */
	public static Connection getAdminConnection() throws SQLException, NamingException {
		logger.debug("Getting admin connection");

		// Create a connection to the ART repository for this admin and store it in the
		// admin session (we are not getting this from the pool since it should not be in Autocommit mode)
		Connection connArt;

		String url = artDbConfig.getUrl();
		String username = artDbConfig.getUsername();
		String password = artDbConfig.getPassword();

		if (StringUtils.isNotBlank(artDbConfig.getDriver())) {
			connArt = DriverManager.getConnection(url, username, password);
			connArt.setAutoCommit(false);
		} else {
			//using jndi datasource
			connArt = getJndiConnection(url);
			connArt.setAutoCommit(false);
		}

		return connArt;
	}

	/**
	 * Get connection located by the given jndi url
	 *
	 * @param jndiUrl
	 * @return
	 * @throws SQLException
	 * @throws javax.naming.NamingException
	 */
	public static Connection getJndiConnection(String jndiUrl) throws SQLException, NamingException {
		Connection conn;

		javax.sql.DataSource ds = ArtUtils.getJndiDataSource(jndiUrl);
		conn = ds.getConnection();

		return conn;
	}

	/**
	 * Get a ArtDBCPDataSource object.
	 *
	 * @param i id of the datasource
	 * @return ArtDBCPDataSource object
	 */
	public static ArtDBCPDataSource getDataSource(int i) {
		return dataSources.get(Integer.valueOf(i));
	}

	/**
	 * Get all datasources
	 *
	 * @return all datasources
	 */
	public static Map<Integer, ArtDBCPDataSource> getDataSources() {
		return dataSources;
	}

	/**
	 * Properly close connections in the dataSources connection pool and clear
	 * the datasources list
	 */
	private static void clearConnections() {
		if (dataSources != null) {
			for (Integer key : dataSources.keySet()) {
				ArtDBCPDataSource ds = dataSources.get(key);
				if (ds != null) {
					ds.close();
				}
			}
			dataSources.clear();
			dataSources = null;
		}
	}

	/**
	 * Refresh all connections in the pool, attempting to properly close the
	 * connections before recreating them.
	 *
	 */
	public static void refreshConnections() {
		//properly close connections
		clearConnections();

		//reset datasources array
		initializeDatasources();

		logger.info("Datasources Refresh: Completed at {}", new Date().toString());
	}

	/**
	 * Refresh all connections in the pool, without attempting to close any
	 * existing connections.
	 *
	 * This is intended to be used on buggy jdbc drivers where for some reasons
	 * the connection.close() method hangs. This may produce a memory leak since
	 * connections are not closed, just removed from the pool: let's hope the
	 * garbage collector decide to remove them sooner or later...
	 */
	public static void forceRefreshConnections() {
		//no attempt to close connections
		dataSources = null;

		//reset datasources array
		initializeDatasources();

		logger.info("Datasources Force Refresh: Completed at {}", new Date().toString());
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
	 * Store the quartz scheduler object
	 *
	 * @param s quartz scheduler object
	 */
	public static void setScheduler(org.quartz.Scheduler s) {
		scheduler = s;
	}

	/**
	 * Get the quartz scheduler object
	 *
	 * @return quartz scheduler object
	 */
	public static org.quartz.Scheduler getScheduler() {
		return scheduler;
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
				if (maxSetting.indexOf(reportFormat) != -1) {
					String value = StringUtils.substringAfter(maxSetting, ":");
					max = NumberUtils.toInt(value);
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
	 * Load art database configuration from art-database file
	 *
	 */
	private static void loadArtDatabaseConfiguration() {
		ArtDatabase artDatabase = null;

		try {
			File artDatabaseFile = new File(artDatabaseFilePath);
			if (artDatabaseFile.exists()) {
				ObjectMapper mapper = new ObjectMapper();
				artDatabase = mapper.readValue(artDatabaseFile, ArtDatabase.class);

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

		//update scheduler
		if (scheduler != null) {
			try {
				if (settings.isSchedulingEnabled()) {
					//start scheduler if it was in stand by. otherwise, it's already started
					if (scheduler.isInStandbyMode()) {
						scheduler.start();
					}
				} else {
					//put scheduler in stand by mode if it was running
					if (!scheduler.isInStandbyMode()) {
						scheduler.standby();
					}
				}
			} catch (SchedulerException ex) {
				logger.error("error", ex);
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

	/**
	 * Get current settings
	 *
	 * @return
	 */
	public static Settings getSettings() {
		return settings;
	}

	private static void createQuartzScheduler() {
		if (artDbConfig == null) {
			logger.warn("ART Database configuration not available");
			return;
		}

		//prepare quartz scheduler properties
		QuartzProperties qp = new QuartzProperties();

		qp.setPropertiesFilePath(webinfPath + sep + "classes" + sep + "quartz.properties");
		qp.setDataSourceDriver(artDbConfig.getDriver());
		qp.setJndiDataSource(artDbConfig.isJndi());
		qp.setDataSourceUrl(artDbConfig.getUrl());
		qp.setDataSourceUsername(artDbConfig.getUsername());
		qp.setDataSourcePassword(artDbConfig.getPassword());

		try {
			Properties props = qp.getProperties();

			//shutdown existing scheduler instance
			if (scheduler != null) {
				scheduler.shutdown();
				scheduler = null;
			}

			//create new scheduler instance
			SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);
			scheduler = schedulerFactory.getScheduler();

			if (settings.isSchedulingEnabled()) {
				scheduler.start();
			} else {
				scheduler.standby();
			}

		} catch (IOException | SchedulerException ex) {
			logger.error("Error", ex);
		}
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

	/**
	 * Migrate existing jobs created in art versions before 1.11 to quartz jobs
	 *
	 */
	private static void migrateJobsToQuartz() throws SQLException {

		Connection conn = DbConnections.getArtDbConnection();

		if (conn == null) {
			logger.info("Can't migrate jobs to Quartz. Connection to ART repository not available");
		} else if (scheduler == null) {
			logger.info("Can't migrate jobs to Quartz. Scheduler not available");
		}

		if (scheduler == null || conn == null) {
			return;
		}

		PreparedStatement psUpdate = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String oldJobsSqlString;
			String updateJobSqlString;

			//prepare statement for updating migration status			
			updateJobSqlString = "UPDATE ART_JOBS SET MIGRATED_TO_QUARTZ='Y',"
					+ " NEXT_RUN_DATE=?, JOB_MINUTE=?, JOB_HOUR=?, "
					+ " JOB_DAY=?, JOB_WEEKDAY=?, JOB_MONTH=? "
					+ " WHERE JOB_ID=?";
			psUpdate = conn.prepareStatement(updateJobSqlString);

			//determine the jobs to migrate
			oldJobsSqlString = "SELECT JOB_MINUTE, JOB_HOUR, JOB_MONTH, JOB_DAY,"
					+ " JOB_WEEKDAY, JOB_ID "
					+ " FROM ART_JOBS"
					+ " WHERE MIGRATED_TO_QUARTZ='N'";

			ps = conn.prepareStatement(oldJobsSqlString);
			rs = ps.executeQuery();

			String minute;
			String hour;
			String day;
			String weekday;
			String month;
			String second = "0"; //seconds always 0
			String cronString;
			Date nextRunDate;

			int jobId;
			String jobName;
			String triggerName;

			int totalRecordCount = 0; //total number of jobs to be migrated
			int migratedRecordCount = 0; //actual number of jobs migrated
			final int batchSize = 100; //max number of updates to batch together for executebatch

			while (rs.next()) {
				totalRecordCount += 1;

				//create quartz job
				minute = rs.getString("JOB_MINUTE");
				if (minute == null) {
					minute = "0"; //default to 0
				}
				hour = rs.getString("JOB_HOUR");
				if (hour == null) {
					hour = "3"; //default to 3am
				}
				month = rs.getString("JOB_MONTH");
				if (month == null) {
					month = "*"; //default to every month
				}
				//set day and weekday
				day = rs.getString("JOB_DAY");
				if (day == null) {
					day = "*";
				}
				weekday = rs.getString("JOB_WEEKDAY");
				if (weekday == null) {
					weekday = "?";
				}

				//set default day of the month if weekday is defined
				if (day.length() == 0 && weekday.length() >= 1 && !weekday.equals("?")) {
					//weekday defined but day of the month is not. default day to ?
					day = "?";
				}

				if (day.length() == 0) {
					//no day of month defined. default to *
					day = "*";
				}
				if (weekday.length() == 0) {
					//no day of week defined. default to undefined
					weekday = "?";
				}
				if (day.equals("?") && weekday.equals("?")) {
					//unsupported. only one can be ?
					day = "*";
					weekday = "?";
				}
				if (day.equals("*") && weekday.equals("*")) {
					//unsupported. only one can be defined
					day = "*";
					weekday = "?";
				}

				cronString = second + " " + minute + " " + hour + " " + day + " " + month + " " + weekday;
				if (CronExpression.isValidExpression(cronString)) {
					//ensure that trigger will fire at least once in the future
					CronTrigger tempTrigger = newTrigger().withSchedule(cronSchedule(cronString)).build();

					nextRunDate = tempTrigger.getFireTimeAfter(new Date());
					if (nextRunDate != null) {
						//create job
						migratedRecordCount += 1;
						if (migratedRecordCount == 1) {
							logger.info("Migrating jobs to quartz...");
						}

						jobId = rs.getInt("JOB_ID");
						jobName = "job" + jobId;
						triggerName = "trigger" + jobId;

						JobDetail quartzJob = newJob(ArtJob.class).withIdentity(jobName, ArtUtils.JOB_GROUP).usingJobData("jobid", jobId).build();

						//create trigger that defines the schedule for the job						
						CronTrigger trigger = newTrigger().withIdentity(triggerName, ArtUtils.TRIGGER_GROUP).withSchedule(cronSchedule(cronString)).build();

						//delete any existing jobs or triggers with the same id before adding them to the scheduler
						scheduler.deleteJob(jobKey(jobName, ArtUtils.JOB_GROUP)); //delete job records
						scheduler.unscheduleJob(triggerKey(triggerName, ArtUtils.TRIGGER_GROUP)); //delete any trigger records

						//add job and trigger to scheduler
						scheduler.scheduleJob(quartzJob, trigger);

						//update jobs table to indicate that the job has been migrated						
						psUpdate.setTimestamp(1, new java.sql.Timestamp(nextRunDate.getTime()));
						psUpdate.setString(2, minute);
						psUpdate.setString(3, hour);
						psUpdate.setString(4, day);
						psUpdate.setString(5, weekday);
						psUpdate.setString(6, month);
						psUpdate.setInt(7, jobId);

						psUpdate.addBatch();
						//run executebatch periodically to prevent out of memory errors
						if (migratedRecordCount % batchSize == 0) {
							ps.executeBatch();
							ps.clearBatch(); //not sure if this is necessary
						}
					}
				}
			}

			if (migratedRecordCount > 0) {
				psUpdate.executeBatch(); //run any remaining updates																
			}

			if (migratedRecordCount > 0) {
				//output the number of jobs migrated
				logger.info("Finished migrating jobs to quartz. Migrated {} out of {} jobs", migratedRecordCount, totalRecordCount);
			}
		} catch (SQLException | SchedulerException ex) {
			logger.error("Error", ex);
		} finally {
			DbUtils.close(psUpdate);
			DbUtils.close(rs, ps, conn);
		}
	}

	/**
	 * run upgrade steps
	 */
	private static void upgrade() {
		File upgradeFile = new File(ArtConfig.getArtTempPath() + "upgrade.txt");
		if (upgradeFile.exists()) {
			try {
				//don't consider alpha, beta, rc etc
				//also, pre-releases, i.e. alpha, beta, etc are considered less than final releases
				String version = StringUtils.substringBefore(artVersion, "-");
				logger.debug("version='{}'", version);

				//changes introduced in 3.0
				if (StringUtils.equals(version, "3.0")) {
					logger.info("Performing 3.0 upgrade steps");

					addUserIds();
					addScheduleIds();
					addDrilldownIds();
					addRuleIds();
					addQueryRuleIds();
					addParameters();
					addUserRuleValueKeys();
					addUserGroupRuleValueKeys();

					logger.info("Done performing 3.0 upgrade steps");
				}

				boolean deleted = upgradeFile.delete();
				if (!deleted) {
					logger.warn("Upgrade file not deleted: {}", upgradeFile);
				}
			} catch (SQLException ex) {
				logger.error("Error", ex);
			}
		}
	}

	/**
	 * Populate user_id columns. Columns added in 3.0
	 */
	private static void addUserIds() throws SQLException {
		logger.debug("Entering addUserIds");

		String sql;

		DbService dbService = new DbService();

		sql = "SELECT USERNAME FROM ART_USERS WHERE USER_ID IS NULL";
		ResultSetHandler<List<String>> h2 = new ColumnListHandler<>("USERNAME");
		List<String> users = dbService.query(sql, h2);

		logger.debug("users.isEmpty()={}", users.isEmpty());
		if (!users.isEmpty()) {
			logger.info("Adding user ids");

			//generate new id
			sql = "SELECT MAX(USER_ID) FROM ART_USERS";
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxId = dbService.query(sql, h);
			logger.debug("maxId={}", maxId);

			if (maxId == null || maxId < 0) {
				maxId = 0;
			}

			for (String user : users) {
				maxId++;

				sql = "UPDATE ART_USERS SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_ADMIN_PRIVILEGES SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_USER_QUERIES SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_USER_QUERY_GROUPS SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_USER_RULES SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_JOBS SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_USER_JOBS SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_USER_GROUP_ASSIGNMENT SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_JOB_ARCHIVES SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);
			}
		}

	}

	/**
	 * Populate schedule_id column. Column added in 3.0
	 */
	private static void addScheduleIds() throws SQLException {
		logger.debug("Entering addScheduleIds");

		String sql;

		DbService dbService = new DbService();

		sql = "SELECT SCHEDULE_NAME FROM ART_JOB_SCHEDULES WHERE SCHEDULE_ID IS NULL";
		ResultSetHandler<List<String>> h2 = new ColumnListHandler<>("SCHEDULE_NAME");
		List<String> schedules = dbService.query(sql, h2);

		logger.debug("schedules.isEmpty()={}", schedules.isEmpty());
		if (!schedules.isEmpty()) {
			logger.info("Adding schedule ids");

			//generate new id
			sql = "SELECT MAX(SCHEDULE_ID) FROM ART_JOB_SCHEDULES";
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxId = dbService.query(sql, h);
			logger.debug("maxId={}", maxId);

			if (maxId == null || maxId < 0) {
				maxId = 0;
			}

			for (String schedule : schedules) {
				maxId++;
				sql = "UPDATE ART_JOB_SCHEDULES SET SCHEDULE_ID=? WHERE SCHEDULE_NAME=?";
				dbService.update(sql, maxId, schedule);
			}
		}
	}

	/**
	 * Populate drilldown_id column. Column added in 3.0
	 */
	private static void addDrilldownIds() throws SQLException {
		logger.debug("Entering addDrilldownIds");

		String sql;

		DbService dbService = new DbService();

		sql = "SELECT QUERY_ID, DRILLDOWN_QUERY_POSITION"
				+ " FROM ART_DRILLDOWN_QUERIES"
				+ " WHERE DRILLDOWN_ID IS NULL";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> drilldowns = dbService.query(sql, h2);

		logger.debug("drilldowns.isEmpty()={}", drilldowns.isEmpty());
		if (!drilldowns.isEmpty()) {
			logger.info("Adding drilldown ids");

			//generate new id
			sql = "SELECT MAX(DRILLDOWN_ID) FROM ART_DRILLDOWN_QUERIES";
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxId = dbService.query(sql, h);
			logger.debug("maxId={}", maxId);

			if (maxId == null || maxId < 0) {
				maxId = 0;
			}

			for (Map<String, Object> drilldown : drilldowns) {
				maxId++;
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Integer parentReportId = (Integer) drilldown.get("QUERY_ID");
				Integer position = (Integer) drilldown.get("DRILLDOWN_QUERY_POSITION");
				sql = "UPDATE ART_DRILLDOWN_QUERIES SET DRILLDOWN_ID=?"
						+ " WHERE QUERY_ID=? AND DRILLDOWN_QUERY_POSITION=?";
				dbService.update(sql, maxId, parentReportId, position);
			}
		}
	}

	/**
	 * Populate rule_id column. Column added in 3.0
	 */
	private static void addRuleIds() throws SQLException {
		logger.debug("Entering addRuleIds");

		String sql;

		DbService dbService = new DbService();

		sql = "SELECT RULE_NAME FROM ART_RULES WHERE RULE_ID IS NULL";
		ResultSetHandler<List<String>> h2 = new ColumnListHandler<>(1);
		List<String> rules = dbService.query(sql, h2);

		logger.debug("rules.isEmpty()={}", rules.isEmpty());
		if (!rules.isEmpty()) {
			logger.info("Adding rule ids");

			//generate new id
			sql = "SELECT MAX(RULE_ID) FROM ART_RULES";
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxId = dbService.query(sql, h);
			logger.debug("maxId={}", maxId);

			if (maxId == null || maxId < 0) {
				maxId = 0;
			}

			for (String rule : rules) {
				maxId++;

				sql = "UPDATE ART_RULES SET RULE_ID=? WHERE RULE_NAME=?";
				dbService.update(sql, maxId, rule);

				sql = "UPDATE ART_QUERY_RULES SET RULE_ID=? WHERE RULE_NAME=?";
				dbService.update(sql, maxId, rule);

				sql = "UPDATE ART_USER_RULES SET RULE_ID=? WHERE RULE_NAME=?";
				dbService.update(sql, maxId, rule);

				sql = "UPDATE ART_USER_GROUP_RULES SET RULE_ID=? WHERE RULE_NAME=?";
				dbService.update(sql, maxId, rule);
			}
		}
	}

	/**
	 * Populate query rule id column. Column added in 3.0
	 */
	private static void addQueryRuleIds() throws SQLException {
		logger.debug("Entering addQueryRuleIds");

		String sql;

		DbService dbService = new DbService();

		sql = "SELECT QUERY_ID, RULE_NAME"
				+ " FROM ART_QUERY_RULES"
				+ " WHERE QUERY_RULE_ID IS NULL";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> reportFilters = dbService.query(sql, h2);

		logger.debug("reportFilters.isEmpty()={}", reportFilters.isEmpty());
		if (!reportFilters.isEmpty()) {
			logger.info("Adding query rule ids");

			//generate new id
			sql = "SELECT MAX(QUERY_RULE_ID) FROM ART_QUERY_RULES";
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxId = dbService.query(sql, h);
			logger.debug("maxId={}", maxId);

			if (maxId == null || maxId < 0) {
				maxId = 0;
			}

			for (Map<String, Object> reportFilter : reportFilters) {
				maxId++;
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Integer reportId = (Integer) reportFilter.get("QUERY_ID");
				String ruleName = (String) reportFilter.get("RULE_NAME");
				sql = "UPDATE ART_QUERY_RULES SET QUERY_RULE_ID=?"
						+ " WHERE QUERY_ID=? AND RULE_NAME=?";
				dbService.update(sql, maxId, reportId, ruleName);
			}
		}
	}

	/**
	 * Populate art_parameters table. Added in 3.0
	 */
	private static void addParameters() throws SQLException {
		logger.debug("Entering addParameters");

		String sql;

		DbService dbService = new DbService();

		sql = "SELECT *"
				+ " FROM ART_QUERY_FIELDS"
				+ " WHERE MIGRATED IS NULL";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> parameters = dbService.query(sql, h2);

		logger.debug("parameters.isEmpty()={}", parameters.isEmpty());
		if (!parameters.isEmpty()) {
			logger.info("Adding parameters");

			//generate new parameter id
			sql = "SELECT MAX(PARAMETER_ID) FROM ART_PARAMETERS";
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxParameterId = dbService.query(sql, h);
			logger.debug("maxParameterId={}", maxParameterId);

			if (maxParameterId == null || maxParameterId < 0) {
				maxParameterId = 0;
			}

			//generate new report parameter id
			sql = "SELECT MAX(REPORT_PARAMETER_ID) FROM ART_REPORT_PARAMETERS";
			ResultSetHandler<Integer> h3 = new ScalarHandler<>();
			Integer maxReportParameterId = dbService.query(sql, h3);
			logger.debug("maxReportParameterId={}", maxReportParameterId);

			if (maxReportParameterId == null || maxReportParameterId < 0) {
				maxReportParameterId = 0;
			}

			for (Map<String, Object> parameter : parameters) {
				maxParameterId++;

				//create parameter definition
				sql = "INSERT INTO ART_PARAMETERS"
						+ " (PARAMETER_ID, NAME, DESCRIPTION, PARAMETER_TYPE, PARAMETER_LABEL,"
						+ " HELP_TEXT, DATA_TYPE, DEFAULT_VALUE, HIDDEN, USE_LOV,"
						+ " LOV_REPORT_ID, USE_FILTERS_IN_LOV, CHAINED_POSITION,"
						+ " CHAINED_VALUE_POSITION, DRILLDOWN_COLUMN_INDEX,"
						+ " USE_DIRECT_SUBSTITUTION)"
						+ " VALUES(" + StringUtils.repeat("?", ",", 16) + ")";

				ParameterType parameterType;
				String paramType = (String) parameter.get("PARAM_TYPE");
				if (StringUtils.equals(paramType, "M")) {
					parameterType = ParameterType.MultiValue;
				} else {
					parameterType = ParameterType.SingleValue;
				}

				ParameterDataType dataType;
				String dtType = (String) parameter.get("PARAM_DATA_TYPE");
				dataType = ParameterDataType.toEnum(dtType);

				String useLov = (String) parameter.get("USE_LOV");
				String useFiltersInLov = (String) parameter.get("APPLY_RULES_TO_LOV");
				String useDirectSubstitution = (String) parameter.get("DIRECT_SUBSTITUTION");

				Object[] values = {
					maxParameterId,
					(String) parameter.get("PARAM_LABEL"), //name. meaning of name and label interchanged
					(String) parameter.get("SHORT_DESCRIPTION"), //description
					parameterType.getValue(),
					(String) parameter.get("NAME"), //label
					(String) parameter.get("DESCRIPTION"), //help text
					dataType.getValue(),
					(String) parameter.get("DEFAULT_VALUE"),
					false,
					BooleanUtils.toBoolean(useLov),
					(Integer) parameter.get("LOV_QUERY_ID"),
					BooleanUtils.toBoolean(useFiltersInLov),
					(Integer) parameter.get("CHAINED_PARAM_POSITION"),
					(Integer) parameter.get("CHAINED_VALUE_POSITION"),
					(Integer) parameter.get("DRILLDOWN_COLUMN"),
					BooleanUtils.toBoolean(useDirectSubstitution)
				};

				dbService.update(sql, values);

				//create report parameter
				maxReportParameterId++;

				sql = "INSERT INTO ART_REPORT_PARAMETERS"
						+ " (REPORT_PARAMETER_ID, REPORT_ID, PARAMETER_ID, PARAMETER_POSITION)"
						+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

				Integer reportId = (Integer) parameter.get("QUERY_ID");
				Integer position = (Integer) parameter.get("FIELD_POSITION");

				dbService.update(sql, maxReportParameterId, reportId, maxParameterId, position);

				//update migrated status
				sql = "UPDATE ART_QUERY_FIELDS SET MIGRATED=1"
						+ " WHERE QUERY_ID=? AND FIELD_POSITION=?";
				dbService.update(sql, reportId, position);
			}
		}
	}

	/**
	 * Populate user rule value key column. Column added in 3.0
	 */
	private static void addUserRuleValueKeys() throws SQLException {
		logger.debug("Entering addUserRuleValueKeys");

		String sql;

		DbService dbService = new DbService();

		sql = "SELECT *"
				+ " FROM ART_USER_RULES"
				+ " WHERE RULE_VALUE_KEY IS NULL AND RULE_TYPE='EXACT'";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> userRules = dbService.query(sql, h2);

		logger.debug("userRules.isEmpty()={}", userRules.isEmpty());
		if (!userRules.isEmpty()) {
			logger.info("Adding user rule value keys");

			for (Map<String, Object> userRule : userRules) {
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				String username = (String) userRule.get("USERNAME");
				String ruleName = (String) userRule.get("RULE_NAME");
				String ruleValue = (String) userRule.get("RULE_VALUE");
				sql = "UPDATE ART_USER_RULES SET RULE_VALUE_KEY=?"
						+ " WHERE USERNAME=? AND RULE_NAME=? AND RULE_VALUE=?"
						+ " AND RULE_TYPE='EXACT'";
				dbService.update(sql, ArtUtils.getUniqueId(), username, ruleName, ruleValue);
			}
		}
	}

	/**
	 * Populate user group rule value key column. Column added in 3.0
	 */
	private static void addUserGroupRuleValueKeys() throws SQLException {
		logger.debug("Entering addUserGroupRuleValueKeys");

		String sql;

		DbService dbService = new DbService();

		sql = "SELECT *"
				+ " FROM ART_USER_GROUP_RULES"
				+ " WHERE RULE_VALUE_KEY IS NULL AND RULE_TYPE='EXACT'";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> userGroupRules = dbService.query(sql, h2);

		logger.debug("userGroupRules.isEmpty()={}", userGroupRules.isEmpty());
		if (!userGroupRules.isEmpty()) {
			logger.info("Adding user group rule value keys");

			for (Map<String, Object> userGroupRule : userGroupRules) {
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Integer userGroupId = (Integer) userGroupRule.get("USER_GROUP_ID");
				String ruleName = (String) userGroupRule.get("RULE_NAME");
				String ruleValue = (String) userGroupRule.get("RULE_VALUE");
				sql = "UPDATE ART_USER_GROUP_RULES SET RULE_VALUE_KEY=?"
						+ " WHERE USER_GROUP_ID=? AND RULE_NAME=? AND RULE_VALUE=?"
						+ " AND RULE_TYPE='EXACT'";
				dbService.update(sql, ArtUtils.getUniqueId(), userGroupId, ruleName, ruleValue);
			}
		}
	}

}
