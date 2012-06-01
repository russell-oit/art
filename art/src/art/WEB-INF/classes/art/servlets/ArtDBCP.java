/*
 * Copyright (C) 2001/2003  Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   (version 2) along with this program (see documentation directory);
 *   otherwise, have a look at http://www.gnu.org or write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
/** ArtDBCP.class
 *
 *
 * Purpose:	loaded at startup, it initializes an array of
 *              database connections (pooled - art dbcp datasources)
 *              This array is stored in the context
 *              with name "ArtDataSources" and used by all other ART classes
 *
 *
 * @version 1.1
 * @author Enrico Liboni
 * @mail   enrico(at)computer.org
 * Last changes:
 *    Logging
 */
package art.servlets;

import art.dbcp.DataSource;
import art.utils.*;

import java.io.*;
import java.util.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that initializes target database connections and holds global variables.
 * 
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class ArtDBCP extends HttpServlet {

    private static final long serialVersionUID = 1L;
    final static Logger logger = LoggerFactory.getLogger(ArtDBCP.class);
    // Global variables
    private static String art_username, art_password, art_jdbc_driver, art_url,
            exportPath, art_testsql, art_pooltimeout;
    private static int poolMaxConnections;
    private static ArtDBCP thisArtDBCP;
    private static LinkedHashMap<Integer, DataSource> dataSources; //use a LinkedHashMap that should store items sorted as per the order the items are inserted in the map...
    private static boolean artPropsStatus = false;
    private static ArtProps ap;
    private static ArrayList<String> userViewModes; //view modes shown to users
    private static boolean isSchedulingEnabled = true;
    private static String templatesPath; //full path to templates directory where formatted report templates and mondiran cube definitions are stored
    private static String relativeTemplatesPath; //relative path to templates directory. used by showAnalysis.jsp
    private static String appPath; //application path. to be used to get/build file paths in non-servlet classes
    private static String artVersion; //art version string
    private static boolean artFullVersion = true;
    private static org.quartz.Scheduler scheduler; //to allow access to scheduler from non-servlet classes
    private static ArrayList<String> allViewModes; //all view modes
    private static String passwordHashingAlgorithm = "bcrypt"; //use bcrypt for password hashing
    private static int defaultMaxRows;
    private static ServletContext ctx;
    private static int maxRunningQueries;

    /**
     * {@inheritDoc}
     * @param config {@inheritDoc}
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        logger.info("ART is starting up...");

        ctx = getServletConfig().getServletContext();

        thisArtDBCP = this;
        ArtDBCPInit();

    }

    /**
     * Close the connection pools
     */
    @Override
    public void destroy() {       
        
        if (dataSources != null) {
            for (Integer key : dataSources.keySet()) {                
                DataSource ds = dataSources.get(key);
                ds.close();
            }
        }

        logger.info("ART Stopped.");
    }

    /**
     *  Load art.props file and initialize variables
     *
     * @return <code>true</code> if file found. <code>false</code> otherwise.
     */
    private boolean initArtProperties() {
        logger.debug("Loading art.props file");

        String sep = File.separator;
        String propsFile = ctx.getRealPath("") + sep + "WEB-INF" + sep + "art.props";

        ap = new ArtProps();

        if (ap.load(propsFile)) { // file already exist  
            logger.debug("art.props found");

            art_username = ap.getProp("art_username");
            art_password = ap.getProp("art_password");
            // de-obfuscate the password
            art_password = Encrypter.decrypt(art_password);

            art_url = ap.getProp("art_url");
            art_jdbc_driver = ap.getProp("art_jdbc_driver");

            art_pooltimeout = ap.getProp("art_pooltimeout");
            art_testsql = ap.getProp("art_testsql");

            artPropsStatus = true;

        } else {
            artPropsStatus = false;
        }

        return artPropsStatus;

    }

    /** 
     * Initialize datasources, viewModes and variables
     */
    private void ArtDBCPInit() {

        logger.debug("Initializing variables");

        //Get some web.xml parameters                        
        poolMaxConnections = Integer.parseInt(ctx.getInitParameter("poolMaxConnections"));
        artVersion = ctx.getInitParameter("versionNumber");
        defaultMaxRows = Integer.parseInt(ctx.getInitParameter("defaultMaxRows"));
        maxRunningQueries=Integer.parseInt(ctx.getInitParameter("maxNumberOfRunningQueries"));

        if ("light".equals(ctx.getInitParameter("versionType"))) {
            artFullVersion = false;
        }

        if ("false".equals(ctx.getInitParameter("enableJobScheduling"))) {
            isSchedulingEnabled = false;
        }

        //set application path
        appPath = ctx.getRealPath("");

        //set templates path
        String sep = java.io.File.separator;
        templatesPath = appPath + sep + "WEB-INF" + sep + "templates" + sep;
        relativeTemplatesPath = "/WEB-INF/templates/";

        //set export path
        exportPath = appPath + sep + "export" + sep;


        //Get user view modes from web.xml file. if a view mode is not in the user list, then it's hidden
        StringTokenizer stCode = new StringTokenizer(ctx.getInitParameter("userViewModesList"), ",");
        String token;
        userViewModes = new ArrayList<String>();
        try {
            while (stCode.hasMoreTokens()) {
                token = stCode.nextToken();
                userViewModes.add(token);
            }
        } catch (Exception e) {
            logger.error("Error while initializing user view modes", e);
        }

        //remove any duplicates in user view modes
        Collection<String> noDup;
        noDup = new LinkedHashSet<String>(userViewModes);
        userViewModes.clear();
        userViewModes.addAll(noDup);

        //construct all view modes list
        allViewModes = new ArrayList<String>(userViewModes);

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

        //remove any duplicates that may exist in all view modes
        noDup = new LinkedHashSet<String>(allViewModes);
        allViewModes.clear();
        allViewModes.addAll(noDup);


        //load properties from art.props file
        if (!initArtProperties()) {
            //art.props not available. don't continue as required configuration settings will be missing
            logger.warn("Not able to get ART properties file (WEB-INF/art.props). Admin should define ART properties at first logon");
            return;
        }

        //Register jdbc driver for art repository
        try {
            Class.forName(art_jdbc_driver).newInstance();
        } catch (Exception e) {
            logger.error("Error wihle loading driver for ART repository: {}", art_jdbc_driver, e);
        }

        //initialize art repository datasource
        int artPoolTimeout = 15;
        if (art_pooltimeout != null) {
            artPoolTimeout = Integer.parseInt(art_pooltimeout);
        }
        DataSource artdb = new DataSource(artPoolTimeout * 60);
        artdb.setName("ART_Repository"); // custom name
        artdb.setUrl(art_url);
        artdb.setUsername(art_username);
        artdb.setPassword(art_password);
        artdb.setLogToStandardOutput(true);
        artdb.setMaxConnections(poolMaxConnections);
        artdb.setDriver(art_jdbc_driver);
        if (art_testsql != null && art_testsql.length() > 3) {
            artdb.setTestSQL(art_testsql);
        }


        //Initialize the target databases array
        try {
            Connection conn = artdb.getConnection();
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT max(DATABASE_ID) FROM ART_DATABASES");

            if (rs.next()) {
                if (rs.getInt(1) > 0) { // datasources exist
                    dataSources = new LinkedHashMap<Integer, DataSource>();
                    rs.close();
                    rs = st.executeQuery("SELECT DATABASE_ID, NAME, URL, USERNAME, PASSWORD, POOL_TIMEOUT, TEST_SQL FROM ART_DATABASES WHERE DATABASE_ID > 0 ORDER BY NAME");// ordered by NAME to have them inserted in order in the LinkedHashMap dataSources (note: first item is always the ArtRepository)
                    int i;

                    /********************************************
                     * ART database is the 0 one
                     */
                    dataSources.put(new Integer(0), artdb);

                    /*******************************************
                     * Create other datasources 1-...
                     */
                    while (rs.next()) {
                        i = rs.getInt("DATABASE_ID");
                        int thisPoolTimeoutSecs = 20 * 60; // set the default value to 20 mins
                        if (rs.getString("POOL_TIMEOUT") != null) {
                            thisPoolTimeoutSecs = Integer.parseInt(rs.getString("POOL_TIMEOUT")) * 60;
                        }

                        DataSource ds = new DataSource(thisPoolTimeoutSecs);
                        ds.setName(rs.getString("NAME"));
                        ds.setUrl(rs.getString("URL"));
                        ds.setUsername(rs.getString("USERNAME"));
                        String pwd = rs.getString("PASSWORD");
                        // decrypt password if stored encrypted
                        if (pwd.startsWith("o:")) {
                            pwd = Encrypter.decrypt(pwd.substring(2));
                        }
                        String testSQL = rs.getString("TEST_SQL");
                        if (testSQL != null && testSQL.length() > 3) {
                            ds.setTestSQL(testSQL);
                        }
                        ds.setPassword(pwd);
                        ds.setLogToStandardOutput(true);
                        ds.setMaxConnections(poolMaxConnections);

                        dataSources.put(new Integer(i), ds);
                    }
                    rs.close();

                    // Get jdbc classes to load
                    rs = st.executeQuery("SELECT DISTINCT DRIVER FROM ART_DATABASES");
                    while (rs.next()) {
                        String dbDriver = rs.getString("DRIVER");
                        if (!dbDriver.equals(art_jdbc_driver)) {
                            // Register a query database driver only if different from the ART one
                            // (since ART db one has been already registered by the JVM)
                            try {
                                Class.forName(dbDriver).newInstance();
                                logger.info("Target Database JDBC Driver Registered: {}", dbDriver);
                            } catch (Exception e) {
                                logger.error("Error while registering Target Database Driver: {}", dbDriver, e);
                            }
                        }
                    }
                    st.close();
                    conn.close();
                } else { // only art repository has been defined...
                    dataSources = new LinkedHashMap<Integer, DataSource>();
                    dataSources.put(new Integer(0), artdb);
                }
            }

        } catch (SQLException e) {
            logger.error("Error while initializing target databases array", e);
        } catch (Exception e) {
            logger.error("Error while initializing target databases array", e);
            artPropsStatus = false;
        }
    }

    /**
     * Get full path to the export directory.
     * @return full path to the export directory
     */
    public static String getExportPath() {
        return exportPath;
    }

    /**
     * Get full path to the templates directory.
     * @return full path to the templates directory
     */
    public static String getTemplatesPath() {
        return templatesPath;
    }

    /**
     *  Get the relative path to the templates directory. Used by showAnalysis.jsp
     * @return relative path to the templates directory
     */
    public static String getRelativeTemplatesPath() {
        return relativeTemplatesPath;
    }

    /**
     * Get the full application path
     * @return the full application path
     */
    public static String getAppPath() {
        return appPath;
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
        java.sql.Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime());
        Connection logConn = null;

        if (message != null && message.length() > 4000) {
            message = message.substring(0, 4000);
        }

        try {
            logConn = getConnection();
            String SQLUpdate = "INSERT INTO ART_LOGS"
                    + " (UPDATE_TIME, USERNAME, LOG_TYPE, IP, MESSAGE) "
                    + " values (?,?,?,?,?) ";

            PreparedStatement psUpdate = logConn.prepareStatement(SQLUpdate);
            psUpdate.setTimestamp(1, now);
            psUpdate.setString(2, user);
            psUpdate.setString(3, type);
            psUpdate.setString(4, ip);
            psUpdate.setString(5, message);

            psUpdate.executeUpdate();

            psUpdate.close();

        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                logConn.close();
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }
    }

    /**
     * Log object execution to the ART_LOGS table.
     * 
     * @param user username of user who executed the query
     * @param type "object"
     * @param ip ip address from which query was run
     * @param objectId id of the query that was run
     * @param totalTime total time to execute the query and display the results
     * @param fetchTime time to fetch the results from the database
     * @param message log message
     */
    public static void log(String user, String type, String ip, int objectId, long totalTime, long fetchTime, String message) {
        java.sql.Timestamp now = new java.sql.Timestamp(new java.util.Date().getTime());
        Connection logConn = null;

        if (message != null && message.length() > 4000) {
            message = message.substring(0, 4000);
        }

        try {
            logConn = getConnection();
            String SQLUpdate = "INSERT INTO ART_LOGS"
                    + " (UPDATE_TIME, USERNAME, LOG_TYPE, IP, OBJECT_ID, TOTAL_TIME, FETCH_TIME, MESSAGE) "
                    + " values (?,?,?,?,?,?,?,?) ";

            PreparedStatement psUpdate = logConn.prepareStatement(SQLUpdate);
            psUpdate.setTimestamp(1, now);
            psUpdate.setString(2, user);
            psUpdate.setString(3, type);
            psUpdate.setString(4, ip);
            psUpdate.setInt(5, objectId);
            psUpdate.setInt(6, (int) totalTime);
            psUpdate.setInt(7, (int) fetchTime);
            psUpdate.setString(8, message);

            psUpdate.executeUpdate();

            psUpdate.close();

        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            try {
                logConn.close();
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }
    }

    /**
     * Determine if art.props file is available and properties have been loaded.
     * @return <code>true</code> if file is available. <code>false</code> otherwise.
     */
    public static boolean getArtPropsStatus() {
        return artPropsStatus; // is false if art.props is not defined
    }

    /** 
     * Return a connection to the target database with a given ID from the connection pool.
     * 
     * @param i id of target database. 0 = ART repository.
     * @return connection to target database or null if connection doesn't exist
     */
    public static Connection getConnection(int i) {
        Connection conn = null;

        try {
            if (artPropsStatus) {
                //artprops has been defined
                DataSource ds = dataSources.get(new Integer(i));
                conn = ds.getConnection(); // i=0 => ART Repository
            }
        } catch (Exception e) {
            logger.error("Error while getting connection for datasource: {}", i, e);
        }

        return conn;
    }

    /** 
     * Return a connection to ART repository from the pool (same as getConnection(0))
     * 
     * @return connection to the ART repository or null if connection doesn't exist
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
            if (artPropsStatus) {
                //artprops has been defined
                if (dataSources != null) {
                    for (Integer key : dataSources.keySet()) {
                        DataSource ds = dataSources.get(key);
                        if (ds != null) {
                            if (name != null && name.equalsIgnoreCase(ds.getName())) {
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
     * Return a normal JDBC connection to the ART repository
     * with autocommit disabled (used for Admins)
     * 
     * @return connection to the ART repository with autocommit disabled
     * @throws java.sql.SQLException 
     */
    public static Connection getAdminConnection() throws java.sql.SQLException {
        logger.debug("Getting admin connection");
        // Create a connection to the ART repository for this admin and store it in the
        // admin session (we are not getting this from the pool sice it is not in Autocommit mode)
        Connection connArt = DriverManager.getConnection(art_url, art_username, art_password);
        connArt.setAutoCommit(false);
        return connArt;
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
    
     * @return the password used to connect to the ART repository
     */
    public static String getArtRepositoryPassword() {
        return art_password;
    }

    /** 
     * Get an ART property as defined in the art.props file
     * 
     * @param key property name
     * @return property value
     */
    public static String getArtProps(String key) {
        return ap.getProp(key);
    }

    /**
     * Get a DataSource object.
     * 
     * @param i id of the datasource
     * @return DataSource object
     */
    public static DataSource getDataSource(int i) {
        return dataSources.get(new Integer(i));
    }

    /**
     * Get all datasources
     * @return all datasources
     */
    public static HashMap getDataSources() {
        return dataSources;
    }

    /**
     * Refresh all connections in the pool, 
     * attempting to properly close the connections before recreating them.
     * 
     */
    public static void refreshConnections() {
        if (dataSources != null) {
            for (Integer key : dataSources.keySet()) {
                DataSource ds = dataSources.get(key);
                if (ds != null) {
                    ds.close();
                }
            }
        }
        thisArtDBCP.ArtDBCPInit();

        logger.info("Datasources Refresh: Completed at {}", new java.util.Date().toString());
    }

    /** 
     * Refresh all connections in the pool, without attempting to close any existing connections.
     *
     * This is intended to be used on buggy jdbc drivers where for some
     * reasons the connection.close() method hangs. This may produce a memory leak
     * since connections are not closed, just removed from the pool: let's hope
     * the garbage collector decide to remove them sooner or later...
     */
    public static void forceRefreshConnections() {
        dataSources = null;
        thisArtDBCP.ArtDBCPInit();

        logger.info("Datasources Force Refresh: Completed at {}", new java.util.Date().toString());
    }

    /** 
     * Get the list of available view modes.
     *To allow users to select how to display the query result
     * Note: not all the viewmodes are displayed here, see web.xml
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
        return isSchedulingEnabled;
    }

    /**
     * Utility method to remove characters from query name
     * that may result in an invalid output file name.
     * 
     * @param fileName query name
     * @return modified query name to be used in file names
     */
    public static String cleanFileName(String fileName) {
        return fileName.replace('/', '_').replace('*', '_').replace('&', '_').replace('?', '_').replace('!', '_').replace('\\', '_').replace('[', '_').replace(']', '_').replace(':', '_');
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
        int retentionPeriod;
        String retentionPeriodString = "";

        try {
            retentionPeriodString = getArtProps("published_files_retention_period");
            retentionPeriod = Integer.parseInt(retentionPeriodString);
        } catch (NumberFormatException e) {
            logger.warn("Invalid number for published files retention period: {}", retentionPeriodString, e);
            retentionPeriod = 1;
        }

        return retentionPeriod;
    }

    /**
     * Get mondrian cache expiry period in hours
     * 
     * @return mondrian cache expiry period in hours
     */
    public static int getMondrianCacheExpiry() {
        int cacheExpiry;
        String cacheExpiryString = "";

        try {
            cacheExpiryString = getArtProps("mondrian_cache_expiry");
            cacheExpiry = Integer.parseInt(cacheExpiryString);
        } catch (NumberFormatException e) {
            //invalid number set for cache expiry. default to 0 (no automatic clearing of cache)
            logger.warn("Invalid number for mondrian cache expiry: {}", cacheExpiryString, e);
            cacheExpiry = 0;
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
     * Get the default max rows
     * @return the default max rows
     */
    public static int getDefaultMaxRows() {
        return defaultMaxRows;
    }

    /**
     * Get the max rows for the given view mode
     * @param viewMode
     * @return the max rows for the given view mode
     */
    public static int getMaxRows(String viewMode) {
        int max;

        String sMax = ctx.getInitParameter(viewMode + "OutputMaxRows");
        if (sMax == null) {
            max = defaultMaxRows;
        } else {
            max = Integer.parseInt(sMax);
        }

        return max;
    }
    
    /**
     * Get the maximum number of running queries
     * @return the maximum number of running queries
     */
    public static int getMaxRunningQueries() {
        return maxRunningQueries;
    }
}
