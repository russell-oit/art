/**
 * Copyright 2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of artdbcp.
 *
 * artdbcp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 2.1 of the License.
 *
 * artdbcp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with artdbcp.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 Known issue: a connection may appear not in use if it has been
 returned by getOlderConnection

 */
package art.dbcp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * This class implements a database connections pool. Features: <ul> <li> The
 * pool dynamically increase/decrease. An unused connection is removed from the
 * pool after
 * <code>timeout</code> seconds </li> <li> The underlying work is transparent
 * from the developer point of view. A connection is obtained from the pool
 * using the getConnection() method. A connection is returned to the pool by
 * simply closing the connection (any open statement is automatically closed
 * thus you might avoid to close statements and resultsets). </li> <li> Too long
 * opened connection are garbaged automatically to prevent your application to
 * hang because of buggy drivers or network problems </li> <li> A logging
 * facility is provided to monitor the database pool, plus a method exists to
 * get the pool detail status with some statistics. </li> </ul> Usage Example:
 * <pre>
 * // load drivers
 * DataSource db = new DataSource();
 * db.setName(NAME); // custom name
 * db.setUrl(DB_URL);
 * db.setUsername(USERNAME);
 * db.setPassword(PASSWORD);
 * // (optional)
 * db.setLogPath(LOG_PATH);
 * db.setLogFileName(LOG_FILE_NAME);
 * // (optional)
 * db.setTestSQL("SELECT 'OK' FROM DUAL"); // oracle
 *
 * Connection c = db.getConnection();
 * ...
 * // use c as a normal Connection object
 * ...
 * c.close(); //This does not actually close the connection, it only decreases
 *            // a counter and closes any open statement
 *            //The internal timer will really close an idle connection after
 *            // a timeout (default 30 min)
 * //
 * db.close(); // really close all connections in the pool and stop internal timer.
 * </pre> <br> Changes on version 2.1: <ul> <li> open statements are closed
 * automatically when a connection is returned to the pool <li> getConnection()
 * now throws a SQLException instead of returning a null object (if an issue
 * prevents the method to return a new working connection). <li> open() has been
 * deprecated (use getConnection()) instead <li> status is available in xml
 * format getXmlStatus() <li> writeLog is synchronized thus more pools can use
 * the same log files </ul>
 *
 * @author Enrico Liboni
 * @version 2.0.0 ******************************
 */
public class DataSource implements TimerListener {

	private String name = ""; //datasource name
	private String url = "";
	private String username = "";
	private String password = "";
	private String logPath = "";
	private String logFileName = "";
	final String sNAME = "artdbcp - DataSource";
	private long TIMEOUT = 30 * 60 * 1000; //30 mins     
	private long MAX_QUERY_RUNNING_TIME = 20 * 60 * 1000; // 20 minutes max running time for a query, before forcibly removing its connection from the pool
	private long totalConnections;
	private int maxReachedPoolSize;
	final boolean DEBUG = false;
	private int maxConnections = 10;
	private boolean isLogFileEnabled = false;
	private String testSQL;
	private boolean enableTest = false;
	LocalTimer t;
	private List<EnhancedConnection> connectionPool = new ArrayList<EnhancedConnection>(maxConnections); // this vector stores active connections
	private long thisObjectTicket; // for debug
	private String driver = ""; //added to support olap
	private boolean logToStandardOutput = false; //to support logging to standard output
	private boolean jndi = false; //determine if this is a jndi datasource
	private javax.sql.DataSource jndiDatasource = null; //hold jndi datasource object

	/**
	 * create a connection pool object with the connection timeout set to
	 * <code>timeout</code> (in seconds, the default is 30 minutes) and the
	 * maximum query duration set to
	 * <code>maxQueryRunningTime</code> (in seconds, the default is 20
	 * minutes).<br> The connection pool is checked every
	 * <code>timeout</code> seconds: <ul> <li>if a connection was inactive for
	 * more than
	 * <code>timeout</code> seconds it is closed and removed from the pool to
	 * free resources. </li> <li> if a connection is being used for more than
	 * <code>maxQueryRunningTime </code> seconds (i.e. who required it did not
	 * release it after
	 * <code>maxQueryRunningTime </code> seconds) the connection is removed from
	 * the pool, without closing it. </li> </li> </ul> The latter is useful when
	 * a connection hangs because of bugs in the driver or network problems.
	 * Usually a connection.close() statement waits until the connection
	 * finishes to execute a query: if the connection hangs, the risk is to have
	 * the connection.close() waiting forever. Removing the connection from the
	 * pool does not kill current query excutions (if a query is lasting more
	 * tha 20 minutes it will finish correclty) but leave to the garbage
	 * collector the task to close/kill it. On JSPs - where queries are usually
	 * quick ones and 20 minutes means a connection problem - this will allow
	 * your application to run smoothly.
	 *
	 * @param timeout
	 * @param maxQueryRunningTime
	 */
	public DataSource(long timeout, long maxQueryRunningTime) {
		MAX_QUERY_RUNNING_TIME = maxQueryRunningTime * 1000;
		TIMEOUT = timeout * 1000;
		startTimer();
	}

	/**
	 * create a DataSource object with the connection timeout set to
	 * <code>timeout</code> (in sec)
	 *
	 * @param timeout
	 */
	public DataSource(long timeout) {
		TIMEOUT = timeout * 1000;
		startTimer();
	}

	public DataSource(long timeout, boolean isJndi) {
		jndi = isJndi;
		TIMEOUT = timeout * 1000;
		startTimer();
	}

	/**
	 * create a DataSource object. The default connection timeout is
	 * <code>30</code> minutes
	 *
	 */
	public DataSource() {
		startTimer();
	}

	private void startTimer() {
		if (!jndi) {
			// Start timer for connection time out
			t = new LocalTimer((TimerListener) this, TIMEOUT);
			t.start();
			thisObjectTicket = new java.util.Date().getTime();
			try {
				DriverManager.setLoginTimeout(10);
			} catch (Exception e) {
				System.err.println(sNAME + " Driver not able to set Timeout.  " + e);
			}
			if (DEBUG) {
				System.err.println("DataSource:   constructor");
			}
		}
	}

	/**
	 * Set driver name
	 *
	 * @param value
	 */
	public void setDriver(String value) {
		driver = value;
	}

	/**
	 * Get driver name
	 *
	 * @return
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * Determine if log should be written to standard output
	 *
	 * @param value
	 */
	public void setLogToStandardOutput(boolean value) {
		logToStandardOutput = value;
	}

	/**
	 * Determine if log should be written to standard output
	 *
	 * @return
	 */
	public boolean isLogToStandardOutput() {
		return logToStandardOutput;
	}

	/**
	 * Set max number of concurrent active connections. Default is 10.
	 *
	 * @param s
	 */
	public void setMaxConnections(int s) {
		maxConnections = s;
	}

	/**
	 * Set the name of the database (it's a label)
	 *
	 * @param s
	 */
	public void setName(String s) {
		name = s;
	}

	/**
	 * Set the database JDBC url
	 *
	 * @param s
	 */
	public void setUrl(String s) {
		url = s;
	}

	/**
	 * Set the database username
	 *
	 * @param s
	 */
	public void setUsername(String s) {
		username = s;
	}

	/**
	 * Set the database password
	 *
	 * @param s
	 */
	public void setPassword(String s) {
		password = s;
	}

	/**
	 * Set the database log file path
	 *
	 * @param s
	 */
	public void setLogPath(String s) {
		logPath = s;
	}

	/**
	 * Set the database log file name and enable logging. If you don't call this
	 * method, logging is disabled
	 *
	 * @param s
	 */
	public void setLogFileName(String s) {
		isLogFileEnabled = true;
		logFileName = s;
	}

	/**
	 * Set the test SQL and activate connection test
	 *
	 * Every <i>timeout</i> seconds a connection is tested by executing the
	 * given SQL query. If the query execution raises an exception the
	 * connection is removed from the pool.
	 *
	 * @param s
	 */
	public void setTestSQL(String s) {
		enableTest = true;
		testSQL = s;
	}

	/**
	 * Get the database name
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the database URL
	 *
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Get the database username
	 *
	 * @return
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get the database password
	 *
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Get the database log file path
	 *
	 * @return
	 */
	public String getLogPath() {
		return logPath;
	}

	/**
	 * Get the database log file name
	 *
	 * @return
	 */
	public String getLogFileName() {
		return logFileName;
	}

	/**
	 * Get a connection from the pool
	 *
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		Connection conn = null;

		if (!jndi) {
			if (t == null) {
				startTimer();
			}

			if (DEBUG) {
				System.err.println(thisObjectTicket + "DataSource:   open()");
			}
			conn = getConnectionFromPool(); // is an EnanchedConnection object
		} else {
			try {
				if (url != null) {
					if (jndiDatasource == null) {
						InitialContext ic = new InitialContext();
						String finalUrl = url;
						if (!finalUrl.startsWith("java:")) {
							//use default jndi prefix
							finalUrl = "java:comp/env/" + finalUrl;
						}
						jndiDatasource = (javax.sql.DataSource) ic.lookup(finalUrl);
					}
					conn = jndiDatasource.getConnection();
				} else {
					writeLog("Can't get connection. JNDI url is null");
				}
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}

		return conn;
	}

	/**
	 * Close all active connections (the internal timer is stopped too)
	 */
	public void close() { // close all active connection and stops timer
		if (!jndi) {
			int i;
			EnhancedConnection e;
			try {
				for (i = (connectionPool.size() - 1); i >= 0; i--) {
					e = connectionPool.get(i);
					e.realClose();  // close the connection   
					connectionPool.remove(i);
				}
			} catch (Exception ex) {
				writeLog("DataSource: " + name + " - close(): Exception: " + ex + "\n");
			}
			t.interrupt();
			t = null;
		} else {
			jndiDatasource = null;
		}
	}

	/**
	 * Remove all existing connection from the pool closing them properly.
	 *
	 * @return
	 */
	public void refreshConnection() {
		if (!jndi) {
			// Close all connections
			writeLog("DataSource: " + name + " - refreshConnection() - removing from pool connections");
			for (int i = 0; i < connectionPool.size(); i++) {
				EnhancedConnection s = connectionPool.get(i);
				try {
					s.realClose();
				} catch (Exception e) {
					writeLog("DataSource: " + name + " - refreshConnection() - exception while closing a connection" + e);
				}
				connectionPool.remove(i);
			}
			if (DEBUG) {
				System.err.println(thisObjectTicket + "DataSource:   refresh() ... done");
			}
		} else {
			jndiDatasource = null;
		}
	}

	/**
	 * Remove all existing connection from the pool without closing them. Useful
	 * if the connection.close() method hangs because of buggy drivers or if
	 * connections are null. In general you should not use this method as it
	 * means something is going wrong with your database.
	 */
	public void forceRefreshConnection() {
		if (!jndi) {
			// Close all connections
			if (DEBUG) {
				System.err.println(thisObjectTicket + "DataSource:   forceRefresh()");
			}
			writeLog("DataSource: " + name + " - forceRefreshConnection() - removing from pool without closing");
			for (int i = 0; i < connectionPool.size(); i++) {
				connectionPool.remove(i);
			}
			if (DEBUG) {
				System.err.println(thisObjectTicket + "DataSource:   forceRefresh() ... done");
			}
			writeLog("DataSource: " + name + " - forceRefreshConnection()... done");
		} else {
			jndiDatasource = null;
		}
	}

	private synchronized void writeLog(String msg) {

		msg = new java.util.Date().toString() + " " + msg;

		if (isLogFileEnabled) {
			try {
				String logfilename = logPath + logFileName;
				FileOutputStream logfile = new FileOutputStream(logfilename, true);
				byte[] buf;
				buf = msg.getBytes();
				logfile.write(buf);
				logfile.close();
			} catch (IOException e) {
				e.printStackTrace(System.out);
			} catch (Exception ex) {
				ex.printStackTrace(System.out);
			}
		}

		//log to standard output if enabled
		if (logToStandardOutput) {
			System.out.println(msg);
		}
	}

	private synchronized Connection getConnectionFromPool() throws SQLException {
		totalConnections++;
		if (DEBUG) {
			System.err.println(thisObjectTicket + " DataSource:   getConnectionFromPool()");
		}
		try {
			if (connectionPool.isEmpty()) {
				if (DEBUG) {
					System.err.println(thisObjectTicket + " DataSource:   getConnectionFromPool() - getNew (first)");
				}
				return getNewDatabaseConnection();
			} else if (isThereAFreeConnection()) {
				if (DEBUG) {
					System.err.println(thisObjectTicket + " DataSource:   getConnectionFromPool() - getFree");
				}
				return getFreeConnection();        /*
				 * return an existing free connection
				 */
			} else if (connectionPool.size() < maxConnections) {
				if (DEBUG) {
					System.err.println(thisObjectTicket + " DataSource:   getConnectionFromPool() - getNew");
				}
				return getNewDatabaseConnection(); /*
				 * create a new connection
				 */
			} else {
				if (DEBUG) {
					System.err.println(thisObjectTicket + " DataSource:   getConnectionFromPool() - getOlder");
				}
				return getOlderConnection();       /*
				 * return the "older" used connection, i.e. the one that liley
				 * will become free first
				 */
			}
		} catch (SQLException e) {
			writeLog("DataSource :" + name + " - getConnectionFromPool(): " + e + "\n");
			throw new SQLException("getConnectionFromPool exception: " + e.getMessage());
		}
	}

	private boolean isThereAFreeConnection() {
		int i;
		EnhancedConnection s;
		for (i = 0; i < connectionPool.size(); i++) {
			s = connectionPool.get(i);
			if (!s.getInUse()) {
				return true;
			}
		}
		return false;
	}

	private EnhancedConnection getFreeConnection() {
		int i;
		EnhancedConnection s;
		for (i = 0; i < connectionPool.size(); i++) {
			s = connectionPool.get(i);
			if (!s.getInUse()) {
				s.open();
				return s;
			}
		}
		return null;
	}

	private EnhancedConnection getOlderConnection() {
		int i;
		long last = -1;
		int index = -1;
		EnhancedConnection s;
		for (i = 0; i < connectionPool.size(); i++) {
			s = connectionPool.get(i);
			if (s.getLastUsedTime() > last) {
				last = s.getLastUsedTime();
				index = i;
			}
		}
		s = connectionPool.get(index); // this is the older one
		s.open(); // this is useless  as it is already opened - just set a new last used date
		return s;
	}

	private EnhancedConnection getNewDatabaseConnection() throws SQLException {
		EnhancedConnection c = new EnhancedConnection(url, username, password);
		connectionPool.add(c);
		if (connectionPool.size() > maxReachedPoolSize) {
			maxReachedPoolSize = connectionPool.size();
		}
		c.open();
		return c;
	}

	/**
	 * Get the current pool size.
	 *
	 * @return
	 */
	public int getPoolSize() {
		return connectionPool.size();
	}

	/**
	 * Get the maximum pool size reached.
	 *
	 * @return
	 */
	public int getMaxReachedPoolSize() {
		return maxReachedPoolSize;
	}

	/**
	 * Get the total number of times a connection was requested from the pool.
	 *
	 * @return
	 */
	public long getTotalConnections() {
		return totalConnections;
	}

	/**
	 * This method is called automatically on timeouts to check if a connection
	 * needs to be closed. A connection will be closed if: <ul> <li> it was
	 * unused for more than TIMEOUT millis </li> </ul> A connection will be
	 * removed from the pool (without closing it) if it is in use from more than
	 * MAX_QUERY_RUNNING_TIME millis (probably the connection hangs), leaving to
	 * the garbage collector the work to destroy it. <br> If test is enable (see
	 * testSQL()) the connection is tested.
	 */
	@Override
	public void timeElapsed() {
		if (!jndi) {
			if (DEBUG) {
				System.err.println(thisObjectTicket + "DataSource:   timeElapsed()");
			}
			int i;
			long currentTime = new java.util.Date().getTime();
			EnhancedConnection e;
			boolean b;
			try {
				for (i = (connectionPool.size() - 1); i >= 0; i--) {
					b = false;
					e = connectionPool.get(i);
					// if      the connection is free and was created before TIMEOUT millis
					//   or
					// if      the connection is not free but it was busy from more than MAX_QUERY_RUNNING_TIME millis
					// then  
					//         destroy the connection trying to close it only if it is not in use
					// else if test is enabled (and connection is to be kept)
					// then 
					//         perform a connection test
					if ((!e.getInUse() && (currentTime - e.getLastUsedTime()) > TIMEOUT)
							|| (b = e.getInUse() && (currentTime - e.getLastUsedTime()) > MAX_QUERY_RUNNING_TIME)) {
						if (DEBUG) {
							System.err.println(thisObjectTicket + "DataSource:   timeElapsed() - closing");
						}
						// 20090909 the two lines below have been swapped: 
						// connection is now removed from the pool before trying to close it
						// since if the connection is broken, and exception would be raised on .realClose()
						connectionPool.remove(i);
						if (!b) {
							e.realClose();    // close it only if not used - this sometime hangs in buggy drivers
						}
						e = null; // just to be sure the GC does its work...
						if (b) {
							writeLog("DataSource: " + name + " - timeElapsed(): The connection [" + i + "] was in use for too much time, removing it from the pool\n");
						}

					} else if (enableTest) {
						try {
							e.test(testSQL);
						} catch (Exception testex) {
							writeLog("DataSource " + name + " - timeElapsed(): Connection Test Failed, removed  " + i + " Cause: " + testex + "\n");
							connectionPool.remove(i);
							//e.realClose();
						}
					}

				}
			} catch (Exception ex) {
				writeLog("DataSource: " + name + " - timeElapsed(): Exception on time elapsed " + ex + "\n");
			}
		}

	}

	/**
	 * return connection pool
	 *
	 * @return
	 */
	public List<EnhancedConnection> getConnectionPool() {
		return connectionPool;
	}
}
