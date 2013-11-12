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

import art.dbcp.DataSource;
import art.login.AuthenticationMethod;
import art.utils.ArtSettings;
import art.utils.ArtUtils;
import art.utils.DbUtils;
import art.utils.Encrypter;
import com.lowagie.text.FontFactory;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
	final static Logger logger = LoggerFactory.getLogger(ArtConfig.class);
	private static String art_password, art_jdbc_driver, art_jdbc_url;
	private static String exportPath;
	private static int poolMaxConnections;
	private static LinkedHashMap<Integer, DataSource> dataSources; //use a LinkedHashMap that should store items sorted as per the order the items are inserted in the map...
	private static boolean artSettingsLoaded = false;
	private static ArtSettings as;
	private static ArrayList<String> userViewModes; //view modes shown to users
	private static String templatesPath; //full path to templates directory where formatted report templates and mondiran cube definitions are stored
	private static String relativeTemplatesPath; //relative path to templates directory. used by showAnalysis.jsp
	private static String appPath; //application path. to be used to get/build file paths in non-servlet classes
	private static org.quartz.Scheduler scheduler; //to allow access to scheduler from non-servlet classes
	private static ArrayList<String> allViewModes; //all view modes
	private static int defaultMaxRows;
	private static String artPropertiesFilePath; //full path to art.properties file
	private static boolean useCustomPdfFont = false; //to allow use of custom font for pdf output, enabling display of non-ascii characters
	private static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy";
	private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
	private static String dateFormat = DEFAULT_DATE_FORMAT; //for date fields, format of date portion
	private static String timeFormat = DEFAULT_TIME_FORMAT; //for date fields, format of time portion
	private static String jobsPath;
	private static boolean customExportDirectory = false; //to enable custom export path
	private static String webinfPath;
	private static String artDatabaseFilePath; //full path to art-database.properties file
	private static String hsqldbPath;

	/**
	 * {@inheritDoc}
	 *
	 * @param config {@inheritDoc}
	 * @throws ServletException
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {

		super.init(config);

		logger.info("ART is starting up...");

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

			//close connections in the artdbcp connection pool
			clearConnections();
		} catch (Exception e) {
			logger.error("Error", e);
		}

		logger.info("ART Stopped.");
	}

	/**
	 * Initialize datasources, viewModes, quartz scheduler, application settings
	 */
	private void ArtConfigInit() {

		logger.debug("Initializing variables");

		//Get art version   
		ServletContext ctx = getServletConfig().getServletContext();
		String artVersion = ctx.getInitParameter("versionNumber");

		//save version in application scope for access from jsp pages
		ctx.setAttribute("artVersion", artVersion);

		//set application path
		appPath = ctx.getRealPath("");

		String sep = java.io.File.separator;

		//set web-inf path
		webinfPath = appPath + sep + "WEB-INF" + sep;

		//set hsqldb path
		hsqldbPath = webinfPath + sep + "hsqldb" + sep;

		//set templates path
		templatesPath = appPath + sep + "WEB-INF" + sep + "templates" + sep;
		relativeTemplatesPath = "/WEB-INF/templates/";

		//set export path
		exportPath = appPath + sep + "export" + sep;

		//set custom export directory
		try {
			Context ic = new InitialContext();
			String customExportPath = (String) ic.lookup("java:comp/env/REPORT_EXPORT_DIRECTORY");
			if (customExportPath != null) {
				//custom export path defined
				exportPath = customExportPath + sep;
				customExportDirectory = true;

				logger.info("Using custom export path: {}", exportPath);
			}
		} catch (NamingException e) {
			logger.debug("Custom export directory not configured", e);
		}

		//set jobs path
		jobsPath = exportPath + "jobs" + sep;

		//set art.properties file path
		artPropertiesFilePath = webinfPath + "art.properties";

		//set art-database.properties file path
		artDatabaseFilePath = webinfPath + "art-database.properties";

		//construct all view modes list
		allViewModes = new ArrayList<String>();

		//add all supported view modes
		allViewModes.add("tsvGz");
		allViewModes.add("xml");
		allViewModes.add("rss20");
		allViewModes.add("htmlGrid");
		allViewModes.add("html");
		allViewModes.add("xls");
		allViewModes.add("xlsx");
		allViewModes.add("pdf");
		allViewModes.add("htmlPlain");
		allViewModes.add("xlsZip");
		allViewModes.add("slk");
		allViewModes.add("slkZip");
		allViewModes.add("tsv");
		allViewModes.add("tsvZip");
		allViewModes.add("htmlDataTable");

		//load settings from art.properties file
		if (loadArtSettings()) {
			//initialize datasources
			initializeDatasources();

			//register pdf fonts
			registerPdfFonts();
		} else {
			//art.properties not available
			logger.warn("ART settings not available. Admin should define ART settings on first logon.");
		}
	}

	/**
	 * Load art.properties file and initialize variables
	 *
	 * @return <code>true</code> if file found. <code>false</code> otherwise.
	 */
	public static boolean loadArtSettings() {
		logger.debug("Loading art settings");

		as = new ArtSettings();

		if (as.load(artPropertiesFilePath)) {
			// settings defined

			art_password = as.getSetting("art_password");
			// de-obfuscate the password
			art_password = Encrypter.decrypt(art_password);

			art_jdbc_url = as.getSetting("art_jdbc_url");
			if (art_jdbc_url == null) {
				art_jdbc_url = as.getSetting("art_url"); //for 2.2.1 to 2.3+ migration. property name changed from art_url to art_jdbc_url
				as.setSetting("art_jdbc_url", art_jdbc_url);
			}
			art_jdbc_driver = as.getSetting("art_jdbc_driver");

			//register authentication jdbc driver
			if (StringUtils.isNotBlank(art_jdbc_driver)) {
				try {
					Class.forName(art_jdbc_driver).newInstance();
					logger.info("Authentication JDBC Driver Registered: {}", art_jdbc_driver);
				} catch (Exception e) {
					logger.error("Error while registering Authentication JDBC Driver: {}", art_jdbc_driver, e);
				}
			}

			String pdfFontName = as.getSetting("pdf_font_name");
			if (StringUtils.isBlank(pdfFontName)) {
				useCustomPdfFont = false; //font name must be defined in order to use custom font
			} else {
				useCustomPdfFont = true;
			}

			//set date format
			dateFormat = as.getSetting("date_format");
			if (StringUtils.isBlank(dateFormat)) {
				dateFormat = DEFAULT_DATE_FORMAT;
			}

			//set time format
			timeFormat = as.getSetting("time_format");
			if (StringUtils.isBlank(timeFormat)) {
				timeFormat = DEFAULT_TIME_FORMAT;
			}

			//set max connection pool connections
			final int DEFAULT_POOL_MAX_CONNECTIONS = 20;
			poolMaxConnections = NumberUtils.toInt(as.getSetting("max_pool_connections"), DEFAULT_POOL_MAX_CONNECTIONS);

			//set user view modes. if a view mode is not in the list, then it's hidden
			String modes = as.getSetting("view_modes");
			String[] viewModes = StringUtils.split(modes, ",");
			if (userViewModes == null) {
				userViewModes = new ArrayList<String>();
			} else {
				userViewModes.clear();
			}
			if (viewModes != null) {
				userViewModes.addAll(Arrays.asList(viewModes));
			}

			//set default max rows
			final int DEFAULT_MAX_ROWS = 1000; //default in case of incorrect setting
			defaultMaxRows = NumberUtils.toInt(as.getSetting("default_max_rows"), DEFAULT_MAX_ROWS);

			//cater for change of authentication method identifiers, from 2.5.2 - 3.0
			String loginMethod = as.getSetting("authentication_method");
			if (loginMethod == null) {
				loginMethod = as.getSetting("index_page_default");

				if (StringUtils.equalsIgnoreCase(loginMethod, "ldaplogin")) {
					loginMethod = AuthenticationMethod.Ldap.getValue();
				} else if (StringUtils.equalsIgnoreCase(loginMethod, "ntlogin")) {
					loginMethod = AuthenticationMethod.WindowsDomain.getValue();
				} else if (StringUtils.equalsIgnoreCase(loginMethod, "dblogin")) {
					loginMethod = AuthenticationMethod.Database.getValue();
				} else if (StringUtils.equalsIgnoreCase(loginMethod, "autologin")) {
					loginMethod = AuthenticationMethod.Auto.getValue();
				} else {
					loginMethod = AuthenticationMethod.Internal.getValue();
				}
				as.setSetting("authentication_method", loginMethod);
			}

			//change of admin email setting name
			String adminEmail = as.getSetting("administrator_email");
			if (adminEmail == null) {
				//use old setting
				adminEmail = as.getSetting("administrator");
				as.setSetting("administrator_email", adminEmail);
			}

			artSettingsLoaded = true;

		} else {
			artSettingsLoaded = false;
		}

		return artSettingsLoaded;

	}

	/**
	 * Register custom fonts to be used in pdf output
	 */
	public static void registerPdfFonts() {
		//register pdf fonts. 
		//fresh registering of 661 fonts in c:\windows\fonts can take as little as 10 secs
		//re-registering already registered directory of 661 fonts takes as little as 1 sec

		if (useCustomPdfFont) {
			//register pdf font if not already registered
			String pdfFontName = getArtSetting("pdf_font_name");
			if (!FontFactory.isRegistered(pdfFontName)) {
				//font not registered. register any defined font files or directories
				String pdfFontDirectory = as.getSetting("pdf_font_directory");
				if (StringUtils.isNotBlank(pdfFontDirectory)) {
					logger.info("Registering fonts from directory: {}", pdfFontDirectory);
					int i = FontFactory.registerDirectory(pdfFontDirectory);
					logger.info("{} fonts registered", i);
				}

				String pdfFontFile = as.getSetting("pdf_font_file");
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
		Properties p = loadArtDatabaseProperties();
		if (p != null) {
			//initialize art repository datasource
			art_jdbc_driver = p.getProperty("driver");
			art_jdbc_url = p.getProperty("url");
			String username = p.getProperty("username");
			art_password = p.getProperty("password");
			art_password = Encrypter.decrypt(art_password);
			int artPoolTimeout = NumberUtils.toInt(p.getProperty("connectionPoolTimeout"), ArtUtils.DEFAULT_CONNECTION_POOL_TIMEOUT);
			String art_testsql = p.getProperty("connectionTestSql");

			boolean jndiDatasource = false;


			if (StringUtils.isBlank(art_jdbc_driver)) {
				jndiDatasource = true;
			}
			DataSource artdb = new DataSource(artPoolTimeout * 60L, jndiDatasource);
			artdb.setName("ART Repository");  //custom name
			artdb.setUrl(art_jdbc_url); //for jndi datasources, the url contains the jndi name/resource reference
			artdb.setUsername(username);
			artdb.setPassword(art_password);
			artdb.setLogToStandardOutput(true);
			artdb.setMaxConnections(poolMaxConnections);
			artdb.setDriver(art_jdbc_driver);

			if (StringUtils.length(art_testsql) > 3) {
				artdb.setTestSQL(art_testsql);
			}

			//populate dataSources map
			dataSources = null;
			dataSources = new LinkedHashMap<Integer, DataSource>();

			//add art repository database to the dataSources map ("id" = 0). 
			//it's not explicitly defined from the admin console
			dataSources.put(Integer.valueOf(0), artdb);

			//add explicitly defined datasources
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;

			try {
				conn = artdb.getConnection();

				// ordered by NAME to have datasources inserted in order in the
				//LinkedHashMap dataSources (note: first item is always the ArtRepository)
				String sql = "SELECT DRIVER, POOL_TIMEOUT, NAME, URL, USERNAME,"
						+ " PASSWORD, TEST_SQL, DATABASE_ID"
						+ " FROM ART_DATABASES"
						+ " WHERE ACTIVE=1"
						+ " ORDER BY NAME";
				ps = conn.prepareStatement(sql);

				rs = ps.executeQuery();
				while (rs.next()) {
					String driver = rs.getString("DRIVER");
					if (StringUtils.isBlank(driver)) {
						jndiDatasource = true;
					} else {
						jndiDatasource = false;
					}

					int timeout = NumberUtils.toInt(rs.getString("POOL_TIMEOUT"), ArtUtils.DEFAULT_CONNECTION_POOL_TIMEOUT);

					DataSource ds = new DataSource(timeout, jndiDatasource);
					ds.setName(rs.getString("NAME"));
					ds.setUrl(rs.getString("URL"));
					ds.setUsername(rs.getString("USERNAME"));
					String password = rs.getString("PASSWORD");
					// decrypt password if stored encrypted
					if (password.startsWith("o:")) {
						password = Encrypter.decrypt(password.substring(2));
					}
					String testSQL = rs.getString("TEST_SQL");
					if (StringUtils.length(testSQL) > 3) {
						ds.setTestSQL(testSQL);
					}
					ds.setPassword(password);
					ds.setMaxConnections(poolMaxConnections);
					ds.setDriver(driver);

					dataSources.put(Integer.valueOf(rs.getInt("DATABASE_ID")), ds);
				}
			} catch (Exception e) {
				logger.error("Error", e);
			} finally {
				DbUtils.close(rs, ps, conn);
			}

			//register jdbc drivers for datasources in the map
			//only register a driver once. several datasources may use the same driver
			//use a set (doesn't add duplicate items)
			Set<String> drivers = new HashSet<String>();

			//get distinct drivers
			if (dataSources != null) {
				for (DataSource ds : dataSources.values()) {
					if (ds != null) {
						drivers.add(ds.getDriver());
					}
				}
			}

			//register drivers
			for (String driver : drivers) {
				if (StringUtils.isNotBlank(driver)) {
					try {
						Class.forName(driver).newInstance();
						logger.info("Datasource JDBC Driver Registered: {}", driver);
					} catch (Exception e) {
						logger.error("Error while registering Datasource JDBC Driver: {}", driver, e);
					}
				}
			}
		}
	}

	/**
	 * Get default authentication method configured for the application
	 *
	 * @return default authentication method configured for the application
	 */
	public static String getAuthenticationMethod() {
		String authenticationMethod = as.getSetting("authentication_method");
		if (StringUtils.isBlank(authenticationMethod)) {
			authenticationMethod = AuthenticationMethod.Internal.getValue();
		}

		return authenticationMethod;
	}

	/**
	 * Get bottom logo image path
	 *
	 * @return bottom logo image path
	 */
	public static String getBottomLogoPath() {
		String bottomLogoPath = as.getSetting("bottom_logo");

		if (StringUtils.isBlank(bottomLogoPath)) {
			bottomLogoPath = "/images/artminiicon.png";
		}

		return bottomLogoPath;
	}

	/**
	 * Determine if art database has been configured
	 *
	 * @return
	 */
	public static boolean isArtDatabaseConfigured() {
		boolean configured = false;

		Properties p = loadArtDatabaseProperties();
		if (p != null) {
			configured = true;
		}

		return configured;
	}

	/**
	 * Get full path to the art-database.properties file
	 *
	 * @return full path to the art-database.properties file
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
		return hsqldbPath;
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
	 * Get full path to the jobs directory.
	 *
	 * @return full path to the jobs directory
	 */
	public static String getJobsPath() {
		return jobsPath;
	}

	/**
	 * Get full path to the templates directory.
	 *
	 * @return full path to the templates directory
	 */
	public static String getTemplatesPath() {
		return templatesPath;
	}

	/**
	 * Get the relative path to the templates directory. Used by
	 * showAnalysis.jsp
	 *
	 * @return relative path to the templates directory
	 */
	public static String getRelativeTemplatesPath() {
		return relativeTemplatesPath;
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
	 * Get the full path to the art.properties file
	 *
	 * @return the full path to the art.properties file
	 */
	public static String getSettingsFilePath() {
		return artPropertiesFilePath;
	}

	/**
	 * Determine whether a custom font should be used in pdf output
	 *
	 * @return <code>true</code> if a custom font should be used in pdf output
	 */
	public static boolean isUseCustomPdfFont() {
		return useCustomPdfFont;
	}

	/**
	 * Determine if the custom pdf font should be embedded in the generated pdf
	 *
	 * @return <code>true</code> if the custom pdf font should be embedded
	 */
	public static boolean isPdfFontEmbedded() {
		return BooleanUtils.toBoolean(as.getSetting("pdf_font_embedded"));
	}

	/**
	 * Determine if query results should be shown on parameters page
	 *
	 * @return <code>true</code> if query results should be shown on parameters
	 * page
	 */
	public static boolean isShowResultsInline() {
		return BooleanUtils.toBoolean(as.getSetting("show_results_inline"));
	}

	/**
	 * Determine if displaying null value is enabled.
	 *
	 * @return <code>true</code> if displaying null value is enabled
	 */
	public static boolean isNullValueEnabled() {
		return BooleanUtils.toBoolean(as.getSetting("null_value_enabled"));
	}

	/**
	 * Determine if a custom export path is in use
	 *
	 * @return <code>true</code> if a custom export path is in use
	 */
	public static boolean isCustomExportDirectory() {
		return customExportDirectory;
	}

	/**
	 * Determine if displaying null numbers as blank or as zero when display
	 * null value is no
	 *
	 * @return <code>true</code> if displaying null numbers as blank. Otherwise,
	 * display null numbers as zero
	 */
	public static boolean isNullNumbersAsBlank() {
		boolean nullNumbersAsBlank = false;

		String nullValue = as.getSetting("null_value_enabled");

		//setting can be "yes", "no_numbers_as_blank" or "no_numbers_as_zero"
		if (StringUtils.equalsIgnoreCase(nullValue, "no_numbers_as_blank")) {
			nullNumbersAsBlank = true;
		}

		return nullNumbersAsBlank;
	}

	/**
	 * Determine if art.properties file is available and settings have been
	 * loaded.
	 *
	 * @return <code>true</code> if file is available and settings have been
	 * loaded correctly. <code>false</code> otherwise.
	 */
	public static boolean isArtSettingsLoaded() {
		return artSettingsLoaded; // is false if art.properties is not defined
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
				DataSource ds = dataSources.get(Integer.valueOf(i));
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
				for (DataSource ds : dataSources.values()) {
					if (ds != null) {
						if (StringUtils.equalsIgnoreCase(name, ds.getName())) {
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
		if (StringUtils.isNotBlank(art_jdbc_driver)) {
			connArt = DriverManager.getConnection(art_jdbc_url, as.getSetting("art_username"), art_password);
			connArt.setAutoCommit(false);
		} else {
			//using jndi datasource
			connArt = getJndiConnection(art_jdbc_url);
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
	 */
	public static Connection getJndiConnection(String jndiUrl) throws SQLException, NamingException {
		Connection conn;

		InitialContext ic = new InitialContext();
		javax.sql.DataSource ds = (javax.sql.DataSource) ic.lookup(ArtUtils.getJndiDatasourceUrl(jndiUrl));
		conn = ds.getConnection();

		return conn;
	}

	/**
	 * Get the username used to connect to the ART repository.
	 *
	 *
	 * @return the username used to connect to the ART repository
	 */
	public static String getRepositoryUsername() {
		return as.getSetting("art_username");

	}

	/**
	 * Get the password used to connect to the ART repository.
	 *
	 * @return the password used to connect to the ART repository
	 */
	public static String getRepositoryPassword() {
		return art_password;
	}

	/**
	 * Get an ART setting as defined in the art.properties file
	 *
	 * @param key setting name
	 * @return setting value
	 */
	public static String getArtSetting(String key) {
		return as.getSetting(key);
	}

	/**
	 * Get a DataSource object.
	 *
	 * @param i id of the datasource
	 * @return DataSource object
	 */
	public static DataSource getDataSource(int i) {
		return dataSources.get(Integer.valueOf(i));
	}

	/**
	 * Get all datasources
	 *
	 * @return all datasources
	 */
	public static Map<Integer, DataSource> getDataSources() {
		return dataSources;
	}

	/**
	 * Properly close connections in the dataSources connection pool
	 */
	private static void clearConnections() {
		if (dataSources != null) {
			for (Integer key : dataSources.keySet()) {
				DataSource ds = dataSources.get(key);
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

		logger.info("Datasources Refresh: Completed at {}", new java.util.Date().toString());
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

		logger.info("Datasources Force Refresh: Completed at {}", new java.util.Date().toString());
	}

	/**
	 * Get the list of available view modes. To allow users to select how to
	 * display the query result Note: not all the viewmodes are displayed here,
	 * see web.xml
	 *
	 * @return list of available view modes
	 */
	public static List<String> getUserViewModes() {
		return userViewModes;
	}

	/**
	 * Get the list of all valid view modes.
	 *
	 * @return list of all valid view modes
	 */
	public static List<String> getAllViewModes() {
		return allViewModes;
	}

	/**
	 * Determine if job scheduling is enabled.
	 *
	 * @return <code>true</code> if job scheduling is enabled
	 */
	public static boolean isSchedulingEnabled() {
		return BooleanUtils.toBoolean(as.getSetting("scheduling_enabled"));
	}

	/**
	 * Get mondrian cache expiry period in hours
	 *
	 * @return mondrian cache expiry period in hours
	 */
	public static int getMondrianCacheExpiry() {
		int cacheExpiry = 0;
		String cacheExpiryString = "";

		try {
			cacheExpiryString = getArtSetting("mondrian_cache_expiry");
			if (NumberUtils.isNumber(cacheExpiryString)) {
				cacheExpiry = Integer.parseInt(cacheExpiryString);
			}
		} catch (NumberFormatException e) {
			//invalid number set for cache expiry. default to 0 (no automatic clearing of cache)
			logger.warn("Invalid number for mondrian cache expiry: {}", cacheExpiryString, e);
		}

		return cacheExpiry;
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
	 * Get the max rows for the given view mode
	 *
	 * @param viewMode
	 * @return the max rows for the given view mode
	 */
	public static int getMaxRows(String viewMode) {
		int max = defaultMaxRows;

		String setting = as.getSetting("specific_max_rows");
		String[] maxRows = StringUtils.split(setting, ",");
		if (maxRows != null) {
			for (String maxSetting : maxRows) {
				if (maxSetting.indexOf(viewMode) != -1) {
					String value = StringUtils.substringAfter(maxSetting, ":");
					try {
						max = Integer.parseInt(value);
					} catch (NumberFormatException e) {
						//invalid number
						logger.warn("Invalid max rows value for setting: {}", maxSetting, e);
					}
					break;
				}
			}
		}

		return max;
	}

	/**
	 * Get the maximum number of running queries
	 *
	 * @return the maximum number of running queries
	 */
	public static int getMaxRunningQueries() {
		int maxQueries = 1000;
		String maxQueriesString = "";

		try {
			maxQueriesString = getArtSetting("max_running_queries");
			maxQueries = Integer.parseInt(maxQueriesString);
		} catch (NumberFormatException e) {
			//invalid number
			logger.warn("Invalid number for max running queries: {}", maxQueriesString, e);
		}

		return maxQueries;
	}

	public static List<String> getWindowsDomains() {
		List<String> domainsList = new ArrayList<String>();

		String[] domains = StringUtils.split(getArtSetting("mswin_domains"), ",");
		if (domains != null) {
			domainsList.addAll(Arrays.asList(domains));
		}

		return domainsList;
	}

	/**
	 * Get string to be displayed in query output for a date field
	 *
	 * @param dt
	 * @return
	 */
	public static String getDateDisplayString(java.util.Date dt) {
		String dateString;
		SimpleDateFormat zf = new SimpleDateFormat("HH:mm:ss.SSS");
		SimpleDateFormat df = new SimpleDateFormat(dateFormat);
		SimpleDateFormat dtf = new SimpleDateFormat(dateFormat + " " + timeFormat);
		if (dt == null) {
			dateString = "";
		} else if (zf.format(dt).equals("00:00:00.000")) {
			dateString = df.format(dt);
		} else {
			dateString = dtf.format(dt);
		}
		return dateString;
	}

	/**
	 * Load art database properties from art-database.properties file
	 *
	 * @return properties object with art database properties or null if file
	 * not found or error occurred
	 */
	public static Properties loadArtDatabaseProperties() {
		Properties p = null;

		try {
			File settingsFile = new File(artDatabaseFilePath);
			if (settingsFile.exists()) {
				FileInputStream o = new FileInputStream(artDatabaseFilePath);
				p = new Properties();
				try {
					p.load(o);
				} finally {
					o.close();
				}
			}
		} catch (Exception ex) {
			logger.error("Error", ex);

			p = null;
		}

		return p;
	}
}
