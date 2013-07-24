/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * ArtDBCP.class
 *
 *
 * Purpose:	loaded at startup, it initializes an array of database connections
 * (pooled - art dbcp datasources) This array is stored in the context with name
 * "ArtDataSources" and used by all other ART classes
 *
 *
 * @version 1.1
 * @author Enrico Liboni
 * @mail enrico(at)computer.org Last changes: Logging
 */
package art.servlets;

import art.dbcp.DataSource;
import art.utils.ArtJob;
import art.utils.ArtSettings;
import art.utils.Encrypter;
import art.utils.QuartzProperties;
import art.utils.UserEntity;
import com.lowagie.text.FontFactory;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that initializes datasource connections and holds global variables.
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ArtDBCP extends HttpServlet {

	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(ArtDBCP.class);
	// Global variables
	private static String art_username, art_password, art_jdbc_driver, art_jdbc_url,
			exportPath, art_testsql, art_pooltimeout;
	private static int poolMaxConnections;
	private static LinkedHashMap<Integer, DataSource> dataSources; //use a LinkedHashMap that should store items sorted as per the order the items are inserted in the map...
	private static boolean artSettingsLoaded = false;
	private static ArtSettings as;
	private static ArrayList<String> userViewModes; //view modes shown to users
	private static boolean schedulingEnabled = true;
	private static String templatesPath; //full path to templates directory where formatted report templates and mondiran cube definitions are stored
	private static String relativeTemplatesPath; //relative path to templates directory. used by showAnalysis.jsp
	private static String appPath; //application path. to be used to get/build file paths in non-servlet classes
	private static String artVersion; //art version string
	private static boolean artFullVersion = true;
	private static org.quartz.Scheduler scheduler; //to allow access to scheduler from non-servlet classes
	private static ArrayList<String> allViewModes; //all view modes
	private static String passwordHashingAlgorithm = "bcrypt"; //use bcrypt for password hashing
	private static int defaultMaxRows;
	private static String artPropertiesFilePath; //full path to art.properties file
	private static boolean useCustomPdfFont = false; //to allow use of custom font for pdf output, enabling display of non-ascii characters
	private static boolean pdfFontEmbedded = false; //determines if custom font should be embedded in the generated pdf
	private static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy";
	private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
	private static String dateFormat = DEFAULT_DATE_FORMAT; //for date fields, format of date portion
	private static String timeFormat = DEFAULT_TIME_FORMAT; //for date fields, format of time portion
	public static final String RECIPIENT_ID = "recipient_id"; //column name in data query that contains recipient identifier column
	public static final String RECIPIENT_COLUMN = "recipient_column"; //column name in data query that contains recipient identifier
	public static final String RECIPIENT_ID_TYPE = "recipient_id_type"; //column name in data query to indicate if recipient id is a number or not
	private static String jobsPath;
	public static final String JOB_GROUP = "jobGroup"; //group name for quartz jobs
	public static final String TRIGGER_GROUP = "triggerGroup"; //group name for quartz triggers
	public static boolean showResultsInline = true;
	private static boolean nullValueEnabled = true; //to enable blank spaces instead of "null" for varchar fields on reports
	private static boolean customExportDirectory = false; //to enable custom export path
	private static boolean nullNumbersAsBlank=true; //whether null numbers are displayed as blank or a zero when nullValueEnabled is true

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

		ArtDBCPInit();
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

			//close connections
			if (dataSources != null) {
				for (Integer key : dataSources.keySet()) {
					DataSource ds = dataSources.get(key);
					ds.close();
				}
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}

		logger.info("ART Stopped.");
	}

	/**
	 * Initialize datasources, viewModes, quartz scheduler
	 */
	private void ArtDBCPInit() {

		logger.debug("Initializing variables");

		//Get some web.xml parameters    
		ServletContext ctx = getServletConfig().getServletContext();

		artVersion = ctx.getInitParameter("versionNumber");
		if (StringUtils.equals(ctx.getInitParameter("versionType"), "light")) {
			artFullVersion = false;
		}

		//set application path
		appPath = ctx.getRealPath("");

		//set templates path
		String sep = java.io.File.separator;
		templatesPath = appPath + sep + "WEB-INF" + sep + "templates" + sep;
		relativeTemplatesPath = "/WEB-INF/templates/";

		//set export path
		exportPath = appPath + sep + "export" + sep;
		try {
			Context ic = new InitialContext();
			String ex = (String) ic.lookup("java:comp/env/REPORT_EXPORT_DIRECTORY");
			if (ex != null) {
				//custom export path defined
				exportPath = ex + sep;
				customExportDirectory = true;

				logger.info("Using custom export path: {}", exportPath);
			}
		} catch (NamingException e) {
			logger.debug("Custom export directory not configured", e);
		}

		//set jobs path
		jobsPath = exportPath + "jobs" + sep;

		//set art.properties file path
		artPropertiesFilePath = appPath + sep + "WEB-INF" + sep + "art.properties";

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

			//start quartz scheduler
			try {
				//get quartz properties object to use to instantiate a scheduler
				QuartzProperties qp = new QuartzProperties();
				Properties props = qp.getProperties();

				if (props == null) {
					logger.warn("Quartz properties not set. Job scheduling will not be possible");
				} else {
					//start quartz scheduler
					SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);
					scheduler = schedulerFactory.getScheduler();

					if (schedulingEnabled) {
						scheduler.start();
					} else {
						scheduler.standby();
					}

					//migrate existing jobs to quartz, if any exist from previous art versions                
					ArtJob aj = new ArtJob();
					aj.migrateJobsToQuartz();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
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

		if (as.load()) {
			// settings defined
			art_username = as.getSetting("art_username");
			art_password = as.getSetting("art_password");
			// de-obfuscate the password
			art_password = Encrypter.decrypt(art_password);

			art_jdbc_url = as.getSetting("art_jdbc_url");
			if (StringUtils.isBlank(art_jdbc_url)) {
				art_jdbc_url = as.getSetting("art_url"); //for 2.2.1 to 2.3+ migration. property name changed from art_url to art_jdbc_url
			}
			art_jdbc_driver = as.getSetting("art_jdbc_driver");

			art_pooltimeout = as.getSetting("art_pooltimeout");
			art_testsql = as.getSetting("art_testsql");

			String pdfFontName = as.getSetting("pdf_font_name");
			if (StringUtils.isBlank(pdfFontName)) {
				useCustomPdfFont = false; //font name must be defined in order to use custom font
			} else {
				useCustomPdfFont = true;
			}
			String fontEmbedded = as.getSetting("pdf_font_embedded");
			if (StringUtils.equals(fontEmbedded, "no")) {
				pdfFontEmbedded = false;
			} else {
				pdfFontEmbedded = true;
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
			poolMaxConnections = 20; //set default;
			String poolMaxConnectionsString = "";
			try {
				poolMaxConnectionsString = getArtSetting("max_pool_connections");
				poolMaxConnections = Integer.parseInt(poolMaxConnectionsString);
			} catch (NumberFormatException e) {
				//invalid number
				logger.warn("Invalid number for max pool connections: {}", poolMaxConnectionsString, e);
			}

			//set scheduling enabled
			String scheduling = as.getSetting("scheduling_enabled");
			if (StringUtils.equals(scheduling, "no")) {
				schedulingEnabled = false;
			} else {
				schedulingEnabled = true;
			}

			//Get user view modes. if a view mode is not in the list, then it's hidden
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
			defaultMaxRows = 10000; //set default;
			String defaultMaxRowsString = "";
			try {
				defaultMaxRowsString = as.getSetting("default_max_rows");
				defaultMaxRows = Integer.parseInt(defaultMaxRowsString);
			} catch (NumberFormatException e) {
				//invalid number
				logger.warn("Invalid number for default max rows: {}", defaultMaxRowsString, e);
			}

			String resultsInline = as.getSetting("show_results_inline");
			if (StringUtils.equals(resultsInline, "no")) {
				showResultsInline = false;
			} else {
				showResultsInline = true;
			}

			String nullValue = as.getSetting("null_value_enabled");
			if (StringUtils.startsWith(nullValue, "no")) {
				nullValueEnabled = false;
				if (StringUtils.equals(nullValue, "no")) {
					nullNumbersAsBlank=true;
				} else {
					nullNumbersAsBlank=false; //null numbers as zero
				}
			} else {
				nullValueEnabled = true;
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

		boolean jndiDatasource = false;

		//initialize art repository datasource
		if (StringUtils.isBlank(art_jdbc_driver)) {
			jndiDatasource = true;
		}

		int artPoolTimeout = 15;
		if (StringUtils.isNotBlank(art_pooltimeout)) {
			artPoolTimeout = Integer.parseInt(art_pooltimeout);
		}
		DataSource artdb = new DataSource(artPoolTimeout * 60L, jndiDatasource);
		artdb.setName("ART_Repository");  //custom name
		artdb.setUrl(art_jdbc_url); //for jndi datasources, the url contains the jndi name/resource reference
		artdb.setUsername(art_username);
		artdb.setPassword(art_password);
		artdb.setLogToStandardOutput(true);
		artdb.setMaxConnections(poolMaxConnections);
		artdb.setDriver(art_jdbc_driver);
		if (StringUtils.length(art_testsql) > 3) {
			artdb.setTestSQL(art_testsql);
		}

		//Register jdbc driver for art repository
		if (!jndiDatasource) {
			try {
				Class.forName(art_jdbc_driver).newInstance();
			} catch (Exception e) {
				logger.error("Error while registering driver for ART repository: {}", art_jdbc_driver, e);
			}
		}

		//Initialize the datasources array
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;

		try {
			String sql;
			conn = artdb.getConnection();
			st = conn.createStatement();

			sql = "SELECT max(DATABASE_ID) FROM ART_DATABASES";
			rs = st.executeQuery(sql);

			if (rs.next()) {
				if (rs.getInt(1) > 0) { // datasources exist
					dataSources = new LinkedHashMap<Integer, DataSource>();
					rs.close();

					sql = "SELECT DATABASE_ID, NAME, DRIVER, URL, USERNAME, PASSWORD, POOL_TIMEOUT, TEST_SQL"
							+ " FROM ART_DATABASES"
							+ " WHERE DATABASE_ID > 0"
							+ " ORDER BY NAME"; // ordered by NAME to have them inserted in order in the LinkedHashMap dataSources (note: first item is always the ArtRepository)
					rs = st.executeQuery(sql);

					/**
					 * ******************************************
					 * ART database is the 0 one
					 */
					dataSources.put(Integer.valueOf(0), artdb);

					/**
					 * *****************************************
					 * Create other datasources 1-...
					 */
					while (rs.next()) {
						String driver = rs.getString("DRIVER");
						if (StringUtils.isBlank(driver)) {
							jndiDatasource = true;
						} else {
							jndiDatasource = false;
						}

						int thisPoolTimeoutSecs = 20 * 60; // set the default value to 20 mins
						String timeout = rs.getString("POOL_TIMEOUT");
						if (StringUtils.isNotBlank(timeout)) {
							thisPoolTimeoutSecs = Integer.parseInt(timeout) * 60;
						}

						DataSource ds = new DataSource(thisPoolTimeoutSecs, jndiDatasource);
						ds.setName(rs.getString("NAME"));
						ds.setUrl(rs.getString("URL"));
						ds.setUsername(rs.getString("USERNAME"));
						String pwd = rs.getString("PASSWORD");
						// decrypt password if stored encrypted
						if (pwd.startsWith("o:")) {
							pwd = Encrypter.decrypt(pwd.substring(2));
						}
						String testSQL = rs.getString("TEST_SQL");
						if (StringUtils.length(testSQL) > 3) {
							ds.setTestSQL(testSQL);
						}
						ds.setPassword(pwd);
						ds.setLogToStandardOutput(true);
						ds.setMaxConnections(poolMaxConnections);

						dataSources.put(Integer.valueOf(rs.getInt("DATABASE_ID")), ds);
					}
					rs.close();

					// Get jdbc classes to load
					rs = st.executeQuery("SELECT DISTINCT DRIVER FROM ART_DATABASES");
					while (rs.next()) {
						String dbDriver = rs.getString("DRIVER");
						if (StringUtils.isNotBlank(dbDriver) && !StringUtils.equals(dbDriver, art_jdbc_driver)) {
							// Register a database driver only if different from the ART one
							// (since ART db one has been already registered)
							try {
								Class.forName(dbDriver).newInstance();
								logger.info("Datasource JDBC Driver Registered: {}", dbDriver);
							} catch (Exception e) {
								logger.error("Error while registering Datasource Driver: {}", dbDriver, e);
							}
						}
					}
					rs.close();
				} else { // only art repository has been defined...
					dataSources = new LinkedHashMap<Integer, DataSource>();
					dataSources.put(Integer.valueOf(0), artdb);
				}
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (st != null) {
					st.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}
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
	 * @return full path to the export directory
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
		return pdfFontEmbedded;
	}

	/**
	 * Determine if query results should be shown on parameters page
	 *
	 * @return <code>true</code> if query results should be shown on parameters
	 * page
	 */
	public static boolean isShowResultsInline() {
		return showResultsInline;
	}

	/**
	 * Determine if displaying null value is enabled.
	 *
	 * @return <code>true</code> if displaying null value is enabled
	 */
	public static boolean isNullValueEnabled() {
		return nullValueEnabled;
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
	 * Determine if displaying null numbers as blank or as zero 
	 * when nullValueEnabled is true
	 * @return <code>true</code> if displaying null numbers as blank. Otherwise, display null numbers as zero
	 */
	public static boolean isNullNumbersAsBlank() {
		return nullNumbersAsBlank;
	}

	/**
	 * Log login attempts to the ART_LOGS table.
	 *
	 * @param user username
	 * @param type "login" if successful or "loginerr" if not
	 * @param ip ip address from which login was done or attempted
	 * @param message log message
	 */
	public static void log(String user, String type, String ip, String message) {

		Connection conn = null;
		PreparedStatement ps = null;

		if (StringUtils.length(message) > 4000) {
			message = message.substring(0, 4000);
		}

		try {
			java.sql.Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime());

			conn = getConnection();
			String sql = "INSERT INTO ART_LOGS"
					+ " (UPDATE_TIME, USERNAME, LOG_TYPE, IP, MESSAGE) "
					+ " VALUES (?,?,?,?,?) ";

			ps = conn.prepareStatement(sql);
			ps.setTimestamp(1, now);
			ps.setString(2, user);
			ps.setString(3, type);
			ps.setString(4, ip);
			ps.setString(5, message);

			ps.executeUpdate();

		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}
	}

	/**
	 * Log some action to the ART_LOGS table.
	 *
	 * @param user username of user who executed the query
	 * @param type type of event
	 * @param ip ip address from which query was run
	 * @param queryId id of the query that was run
	 * @param totalTime total time to execute the query and display the results
	 * @param fetchTime time to fetch the results from the database
	 * @param message log message
	 */
	public static void log(String user, String type, String ip, int queryId, long totalTime, long fetchTime, String message) {

		Connection conn = null;
		PreparedStatement ps = null;

		if (StringUtils.length(message) > 4000) {
			message = message.substring(0, 4000);
		}

		try {
			java.sql.Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime());
			conn = getConnection();
			String sql = "INSERT INTO ART_LOGS"
					+ " (UPDATE_TIME, USERNAME, LOG_TYPE, IP, QUERY_ID, TOTAL_TIME, FETCH_TIME, MESSAGE) "
					+ " VALUES (?,?,?,?,?,?,?,?) ";

			ps = conn.prepareStatement(sql);
			ps.setTimestamp(1, now);
			ps.setString(2, user);
			ps.setString(3, type);
			ps.setString(4, ip);
			ps.setInt(5, queryId);
			ps.setInt(6, (int) totalTime);
			ps.setInt(7, (int) fetchTime);
			ps.setString(8, message);

			ps.executeUpdate();

		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}
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
			if (artSettingsLoaded) {
				//settings have been defined
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
			if (artSettingsLoaded) {
				//settings have been defined
				if (dataSources != null) {
					for (Integer key : dataSources.keySet()) {
						DataSource ds = dataSources.get(key);
						if (ds != null) {
							if (StringUtils.equalsIgnoreCase(name, ds.getName())) {
								//this is the required datasource. get connection and exit loop
								conn = ds.getConnection();
								break;
							}
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
	public static Connection getAdminConnection() throws SQLException {
		logger.debug("Getting admin connection");
		// Create a connection to the ART repository for this admin and store it in the
		// admin session (we are not getting this from the pool since it should not be in Autocommit mode)
		Connection connArt;
		if (StringUtils.isNotBlank(art_jdbc_driver)) {
			connArt = DriverManager.getConnection(art_jdbc_url, art_username, art_password);
			connArt.setAutoCommit(false);
		} else {
			//using jndi datasource
			connArt = getJndiConnection(art_jdbc_url);
			connArt.setAutoCommit(false);
		}

		return connArt;
	}

	public static Connection getJndiConnection(String jndiUrl) throws SQLException {

		Connection conn = null;

		try {
			InitialContext ic = new InitialContext();
			javax.sql.DataSource ds = (javax.sql.DataSource) ic.lookup(getJndiDatasourceUrl(jndiUrl));
			conn = ds.getConnection();
		} catch (NamingException e) {
			logger.error("Error", e);
		}

		return conn;
	}

	/**
	 * Get the username used to connect to the ART repository.
	 *
	 *
	 * @return the username used to connect to the ART repository
	 */
	public static String getArtRepositoryUsername() {
		return art_username;

	}

	/**
	 * Get the password used to connect to the ART repository.
	 *
	 * @return the password used to connect to the ART repository
	 */
	public static String getArtRepositoryPassword() {
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
	 * Refresh all connections in the pool, attempting to properly close the
	 * connections before recreating them.
	 *
	 */
	public static void refreshConnections() {
		//properly close connections
		if (dataSources != null) {
			for (Integer key : dataSources.keySet()) {
				DataSource ds = dataSources.get(key);
				if (ds != null) {
					ds.close();
				}
			}
		}

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
		return schedulingEnabled;
	}

	/**
	 * Utility method to remove characters from query name that may result in an
	 * invalid output file name.
	 *
	 * @param fileName query name
	 * @return modified query name to be used in file names
	 */
	public static String cleanFileName(String fileName) {
		return fileName.replace('/', '_').replace('*', '_').replace('&', '_')
				.replace('?', '_').replace('!', '_').replace('\\', '_').replace('[', '_')
				.replace(']', '_').replace(':', '_').replace('|', '_')
				.replace('<', '_').replace('>', '_').replace('"', '_');
	}

	/**
	 * Determine if this is the full or light version.
	 *
	 * @return <code>true</code> if this is the full version
	 */
	public static boolean isArtFullVersion() {
		return artFullVersion;
	}

	/**
	 * Get the art version string. Displayed in art user pages.
	 *
	 * @return the art version string
	 */
	public static String getArtVersion() {
		String version = artVersion;

		if (!artFullVersion) {
			version = artVersion + " - light";
		}

		return version;
	}

	/**
	 * Get job files retention period in days
	 *
	 * @return job files retention period in days
	 */
	public static int getPublishedFilesRetentionPeriod() {
		int retentionPeriod = 0;
		String retentionPeriodString = "";

		try {
			retentionPeriodString = getArtSetting("published_files_retention_period");
			if (NumberUtils.isNumber(retentionPeriodString)) {
				retentionPeriod = Integer.parseInt(retentionPeriodString);
			}
		} catch (NumberFormatException e) {
			logger.warn("Invalid published filed retention period: {}", retentionPeriodString, e);
		}

		return retentionPeriod;
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
	 * Get the hash algorithm setting
	 *
	 * @return hash algorithm setting
	 */
	public static String getPasswordHashingAlgorithm() {
		return passwordHashingAlgorithm;
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

	/**
	 * Get random string to be appended to output filenames
	 *
	 * @return random string to be appended to output filenames
	 */
	public static String getRandomString() {
		return "-" + RandomStringUtils.randomAlphanumeric(10);
	}

	/**
	 * Get string to be displayed in query output for a date field
	 *
	 * @param dt
	 * @return
	 */
	public static String getDateDisplayString(java.util.Date dt) {
		String dateString;

		SimpleDateFormat zf = new SimpleDateFormat("HH:mm:ss.SSS"); //use to check if time component is 0
		SimpleDateFormat df = new SimpleDateFormat(dateFormat);
		SimpleDateFormat dtf = new SimpleDateFormat(dateFormat + " " + timeFormat);

		if (dt == null) {
			dateString = "";
		} else if (zf.format(dt).equals("00:00:00.000")) {
			//time component is 0. don't display time component
			dateString = df.format(dt);
		} else {
			//display both date and time
			dateString = dtf.format(dt);
		}

		return dateString;
	}

	/**
	 * Get a string to be used for correctly sorting dates irrespective of the
	 * date format used to display dates
	 *
	 * @param dt
	 * @return
	 */
	public static String getDateSortString(java.util.Date dt) {
		String sortKey;

		if (dt == null) {
			sortKey = "null";
		} else {
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
			sortKey = sf.format(dt);
		}

		return sortKey;
	}

	/**
	 * Authenticate the session.
	 *
	 * @param request
	 * @throws ArtException if couldn't authenticate
	 * @throws Exception
	 */
	public static String authenticateSession(HttpServletRequest request) throws Exception {

		/*
		 * Logic (an exception is generated if any action goes wrong)
		 *
		 * if username/password are provided -> check if they are for the ART
		 * superadmin -> validate the credentials else if username is in the
		 * session and the param 'external' is available -> this is an "external
		 * authenticated" session check if the username is an art user too end
		 *
		 * create a UserEntity UE and store it in the session store "username"
		 * attribute in the session as well if is an admin session initialize
		 * Admin attributes
		 */

		String msg = null; //error message if authentication failure

		HttpSession session = request.getSession();
		ResourceBundle messages = ResourceBundle.getBundle("art.i18n.ArtMessages", request.getLocale());

		int accessLevel = -1;
		boolean internalAuthentication = true;

		String username = request.getParameter("username");
		String password = request.getParameter("password");

		/*
		 * *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***
		 * ***
		 */
		if (username != null && password != null) {  // INTERNAL AUTHENTICATION ( a username and password is available)

			// check if the credentials match the ART Reposority username/password
			if (StringUtils.equals(username, ArtDBCP.getArtRepositoryUsername())
					&& StringUtils.equals(password, ArtDBCP.getArtRepositoryPassword()) && StringUtils.isNotBlank(username)) {
				// using repository username and password. Give user super admin privileges
				// no need to authenticate it
				accessLevel = 100;
				ArtDBCP.log(username, "login", request.getRemoteAddr(), "internal-superadmin, level: " + accessLevel);
			} else { // begin normal internal authentication
				/*
				 * Let's verify if username and password are valid
				 */
				Connection conn = null;
				PreparedStatement ps = null;
				try {
					//default to using bcrypt instead of md5 for password hashing
					//password = digestString(password, "MD5");

					String SqlQuery = "SELECT ACCESS_LEVEL, PASSWORD, HASHING_ALGORITHM FROM ART_USERS "
							+ "WHERE USERNAME = ? AND (ACTIVE_STATUS = 'A' OR ACTIVE_STATUS IS NULL)";
					conn = ArtDBCP.getConnection();
					if (conn == null) {
						// ART Repository Down !!!
						msg = messages.getString("invalidConnection");
					} else {
						ps = conn.prepareStatement(SqlQuery);
						ps.setString(1, username);
						ResultSet rs = ps.executeQuery();
						if (rs.next()) {
							//user exists. verify password
							if (Encrypter.VerifyPassword(password, rs.getString("PASSWORD"), rs.getString("HASHING_ALGORITHM"))) {
								// ----------------------------------------------------AUTHENTICATED!

								accessLevel = rs.getInt("ACCESS_LEVEL");
								session.setAttribute("username", username); // store username in the session
								ArtDBCP.log(username, "login", request.getRemoteAddr(), "internal, level: " + accessLevel);
							} else {
								//wrong password
								ArtDBCP.log(username, "loginerr", request.getRemoteAddr(), "internal, failed");
								msg = messages.getString("invalidAccount");
							}

						} else {
							//user doesn't exist
							ArtDBCP.log(username, "loginerr", request.getRemoteAddr(), "internal, failed");
							msg = messages.getString("invalidAccount");
						}
						rs.close();
					}
				} finally {
					try {
						if (ps != null) {
							ps.close();
						}
					} catch (Exception e) {
						logger.error("Error", e);
					}
					try {
						if (conn != null) {
							conn.close();
						}
					} catch (Exception e) {
						logger.error("Error", e);
					}
				}
			} // end internal authentication
			/*
			 * *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***
			 * *** ***
			 */
		} else if (session.getAttribute("username") != null) { // EXTERNAL AUTHENTICATION ( a username in session is available)
			/*
			 * a username is already in the session, but the ue object is not
			 * let's get other info and authenticate it
			 */
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				username = (String) session.getAttribute("username");
				String SqlQuery = ("SELECT ACCESS_LEVEL FROM ART_USERS "
						+ " WHERE USERNAME = ? AND (ACTIVE_STATUS = 'A' OR ACTIVE_STATUS IS NULL) ");
				conn = ArtDBCP.getConnection();
				if (conn == null) {
					// ART Repository Down !!!
					msg = messages.getString("invalidConnection");
				} else {
					ps = conn.prepareStatement(SqlQuery);
					ps.setString(1, username);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						// ----------------------------------------------------AUTHENTICATED!

						accessLevel = rs.getInt("ACCESS_LEVEL");
						internalAuthentication = false;

						ArtDBCP.log(username, "login", request.getRemoteAddr(), "external, level: " + accessLevel);
					} else {
						//external user not created in ART
						ArtDBCP.log(username, "loginerr", request.getRemoteAddr(), "external, failed");
						msg = messages.getString("invalidUser");
					}
					rs.close();
				}
			} finally {
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (Exception e) {
					logger.error("Error", e);
				}
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (Exception e) {
					logger.error("Error", e);
				}
			}
		} else {
			// if the request is for a public_user session
			// create it...
			if (request.getParameter("_public_user") != null) {
				username = "public_user";

				accessLevel = 0;
				internalAuthentication = true;
			} else {
				// ... otherwise this is a session expired / unauthorized access attempt...
				msg = messages.getString("sessionExpired");
			}
		}

		// if the session is authenticated, Create the UserEntity object and store it in the session
		if (msg == null) {
			//authentication successful
			UserEntity ue = new UserEntity(username);

			//override some properties
			ue.setAccessLevel(accessLevel);
			ue.setInternalAuth(internalAuthentication);

			session.setAttribute("ue", ue);
			session.setAttribute("username", username);

			// Set admin session
			if (accessLevel >= 10) {
				session.setAttribute("AdminSession", "Y");
				session.setAttribute("AdminLevel", Integer.valueOf(accessLevel));
				session.setAttribute("AdminUsername", username);
			}
		}

		return msg;
	}

	/**
	 * Generate a random number within a given range
	 *
	 * @param minimum
	 * @param maximum
	 * @return
	 */
	public static int getRandomNumber(int minimum, int maximum) {
		Random rn = new Random();
		int range = maximum - minimum + 1;
		int randomNum = rn.nextInt(range) + minimum;

		return randomNum;
	}

	/**
	 * Generate a random number within a given range
	 *
	 * @param minimum
	 * @param maximum
	 * @return
	 */
	public static long getRandomNumber(long minimum, long maximum) {
		Random rn = new Random();
		long randomNum = minimum + (long) (rn.nextDouble() * (maximum - minimum));

		return randomNum;
	}

	public static List<String> getFileDetailsFromResult(String result) {
		List<String> details = new ArrayList<String>();

		if (StringUtils.indexOf(result, "\n") > -1) {
			// publish jobs can have file link and message separated by newline(\n)
			String fileName = StringUtils.substringBefore(result, "\n"); //get file name
			fileName = StringUtils.replace(fileName, "\r", ""); //on windows pre-2.5, filenames had \\r\\n
			String resultMessage = StringUtils.substringAfter(result, "\n"); //message

			details.add(fileName);
			details.add(resultMessage);
		} else {
			details.add(result);
			details.add("");
		}

		return details;
	}

	public static String getJndiDatasourceUrl(String url) {
		String finalUrl = url;
		if (!StringUtils.startsWith(finalUrl, "java:")) {
			//use default jndi prefix
			finalUrl = "java:comp/env/" + finalUrl;
		}
		
		return finalUrl;
	}
}
