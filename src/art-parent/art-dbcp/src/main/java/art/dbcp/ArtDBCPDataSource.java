/*
 * Copyright 2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of art-dbcp.
 *
 * art-dbcp is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 2.1 of the License.
 *
 * art-dbcp is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with art-dbcp. If not, see <http://www.gnu.org/licenses/>.
 */
package art.dbcp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a database connection pool. Features: <ul> <li> The
 * pool dynamically increases/decreases. An unused connection is removed from
 * the pool after {@code timeout} seconds. </li> <li> The underlying work is
 * transparent from the developer's point of view. A connection is obtained from
 * the pool using the getConnection() method. A connection is returned to the
 * pool by simply closing the connection (any open statement is automatically
 * closed thus you might not need to close statements and resultsets). </li>
 * <li>
 * Connections that are open for too long are removed automatically to prevent
 * your application from hanging because of buggy drivers or network problems
 * </li> </ul>
 * Usage Example:
 * <pre>
 * // load drivers
 * ArtDBCPDataSource db = new ArtDBCPDataSource();
 * db.setName(NAME); // custom name
 * db.setUrl(DB_URL);
 * db.setUsername(USERNAME);
 * db.setPassword(PASSWORD);
 * // (optional)
 * db.setTestSQL("SELECT 'OK' FROM DUAL"); // oracle
 *
 * Connection c = db.getConnection();
 * ...
 * // use c as a normal Connection object
 * ...
 * c.close(); //This does not actually close the connection, it only decreases
 * // a counter and closes any open statements
 * //The internal timer will really close an idle connection after
 * // a timeout (default 30 min)
 * //
 * db.close(); // really close all connections in the pool and stop the internal timer.
 * </pre>
 *
 * Known issue: a connection may appear not in use if it has been returned by
 * getOldestConnection
 *
 * @author Enrico Liboni
 */
public class ArtDBCPDataSource implements TimerListener {

	private static final Logger logger = LoggerFactory.getLogger(ArtDBCPDataSource.class);
	private String name; //a name for this connection pool
	private String url;
	private String username;
	private String password;
	private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000; //30 minutes
	private long timeout = DEFAULT_TIMEOUT; //timeout after which idle connections are closed
	private static final long DEFAULT_MAX_QUERY_RUNNING_TIME = 20 * 60 * 1000; // 20 minutes
	private long maxQueryRunningTime; // max running time for a query, before its connection is forcibly removed from the pool
	private long totalConnectionRequests;
	private int biggestPoolSizeReached;
	private int maxConnections = 10; //max number of underlying connections that can be created
	private String testSql;
	LocalTimer t;
	private final List<EnhancedConnection> pool = new ArrayList<>(maxConnections); //stores active connections
	private long thisObjectTicket; // for debugging
	private String driver; //added to support olap
	private boolean jndi; //if this is a jndi datasource
	private DataSource jndiDataSource; //hold the jndi datasource object
	private Properties connectionProperties;

	/**
	 * Create a connection pool object with the connection timeout set to
	 * <code>timeout</code> (in seconds, the default is 30 minutes) and the
	 * maximum query duration set to <code>maxQueryRunningTime</code> (in
	 * seconds, the default is 20 minutes).<br> The connection pool is checked
	 * every <code>timeout</code> seconds: <ul> <li>if a connection was inactive
	 * for more than <code>timeout</code> seconds it is closed and removed from
	 * the pool to free resources. </li> <li> if a connection is being used for
	 * more than <code>maxQueryRunningTime </code> seconds (i.e. whoever
	 * required it did not release it after <code>maxQueryRunningTime </code>
	 * seconds) the connection is removed from the pool, without closing it.
	 * </li>
	 * </ul> The latter is useful when a connection hangs because of bugs in the
	 * driver or network problems. Usually a connection.close() statement waits
	 * until the connection finishes to execute a query: if the connection
	 * hangs, the risk is to have the connection.close() waiting forever.
	 * Removing the connection from the pool does not kill current query
	 * executions (if a query takes more than 20 minutes to run it will finish
	 * correctly) but leave to the garbage collector the task of closing/killing
	 * it. On JSPs - where queries are usually quick ones and 20 minutes means a
	 * connection problem - this will allow your application to run smoothly.
	 *
	 * @param timeout
	 * @param maxQueryRunningTime
	 */
	public ArtDBCPDataSource(long timeout, long maxQueryRunningTime) {
		this(timeout, maxQueryRunningTime, false);
	}

	/**
	 * Create a connection pool with the connection timeout set to
	 * <code>timeout</code> (in sec)
	 *
	 * @param timeout
	 */
	public ArtDBCPDataSource(long timeout) {
		this(timeout, DEFAULT_MAX_QUERY_RUNNING_TIME, false);
	}

	/**
	 * Create a connection pool
	 *
	 * @param timeout
	 * @param jndi
	 */
	public ArtDBCPDataSource(long timeout, boolean jndi) {
		this(timeout, DEFAULT_MAX_QUERY_RUNNING_TIME, jndi);
	}

	/**
	 * Create a connection pool. The default connection timeout is
	 * <code>30</code> minutes
	 *
	 */
	public ArtDBCPDataSource() {
		this(DEFAULT_TIMEOUT, DEFAULT_MAX_QUERY_RUNNING_TIME, false);
	}

	/**
	 * Create a connection pool
	 *
	 * @param timeout
	 * @param maxQueryRunningTime
	 * @param jndi
	 */
	public ArtDBCPDataSource(long timeout, long maxQueryRunningTime, boolean jndi) {
		if (timeout < 0) {
			throw new IllegalArgumentException("Invalid timeout - " + timeout
					+ ". timeout cannot be < 0");
		}
		if (maxQueryRunningTime < 0) {
			throw new IllegalArgumentException("Invalid maxQueryRunningTime - "
					+ maxQueryRunningTime + ". maxQueryRunningTime cannot be < 0");
		}

		this.timeout = timeout * 1000;
		this.maxQueryRunningTime = maxQueryRunningTime * 1000;
		this.jndi = jndi;
		startTimer();
	}

	private void startTimer() {
		logger.debug("Entering startTimer");

		logger.debug("jndi={}", jndi);
		if (!jndi) {
			logger.debug("timeout={}", timeout);

			// Start timer for connection timeout
			t = new LocalTimer((TimerListener) this, timeout);
			t.start();

			thisObjectTicket = System.currentTimeMillis();
			logger.debug("thisObjectTicket={}", thisObjectTicket);

			try {
				//set maximum time to wait for a connection to be made.
				//some drivers may not honour this, instead using a url parameter e.g. mysql connector/j
				//see https://stackoverflow.com/questions/1683949/connection-timeout-for-drivermanager-getconnection
				DriverManager.setLoginTimeout(10);
			} catch (Exception ex) {
				logger.error("Driver not able to set login timeout", ex);
			}
		}

	}

	/**
	 * @return the connectionProperties
	 */
	public Properties getConnectionProperties() {
		return connectionProperties;
	}

	/**
	 * @param connectionProperties the connectionProperties to set
	 */
	public void setConnectionProperties(Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	/**
	 * Set driver class name
	 *
	 * @param value
	 */
	public void setDriver(String value) {
		driver = value;
	}

	/**
	 * Get driver class name
	 *
	 * @return
	 */
	public String getDriver() {
		return driver;
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
	 * Set the name of the connection pool
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
	 * Set the test SQL thereby activating connection testing
	 *
	 * Every <i>timeout</i> seconds a connection is tested by executing the
	 * given SQL query. If the query execution raises an exception the
	 * connection is removed from the pool.
	 *
	 * @param s
	 */
	public void setTestSql(String s) {
		testSql = s;
	}

	/**
	 * Get the connection pool name
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the database JDBC URL
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
	 * Get a connection from the pool
	 *
	 * @return
	 * @throws SQLException
	 * @throws javax.naming.NamingException
	 */
	public Connection getConnection() throws SQLException, NamingException {
		logger.debug("{} - Entering getConnection: jndi={}", thisObjectTicket, jndi);

		Connection conn;

		if (jndi) {
			conn = getConnectionFromJndi();
		} else {
			logger.debug("(t == null) = {}", t == null);
			if (t == null) {
				startTimer();
			}
			conn = getConnectionFromPool();
		}

		return conn;
	}

	private Connection getConnectionFromJndi() throws NamingException, SQLException {
		logger.debug("{} - Entering getConnectionFromJndi", thisObjectTicket);

		//throw exception if jndi url is null, rather than returning a null connection, which would be useless
		Objects.requireNonNull(url, "url must not be null. Connection pool=" + name);

		logger.debug("(jndiDatasource == null) = {}", jndiDataSource == null);
		if (jndiDataSource == null) {
			//first time we are getting a connection from jndi. get the jndi datasource object
			InitialContext ic = new InitialContext();
			logger.debug("url='{}'", url);
			String finalUrl;
			if (url.startsWith("java:")) {
				//full jndi url provided. use as is
				finalUrl = url;
			} else {
				//relative url provided. use default jndi prefix
				finalUrl = "java:comp/env/" + url;
			}
			logger.debug("finalUrl='{}'", finalUrl);
			jndiDataSource = (DataSource) ic.lookup(finalUrl);
		}

		return jndiDataSource.getConnection();
	}

	/**
	 * Close all active connections and stop the internal timer. This really
	 * closes the underlying database connections.
	 */
	public void close() {
		logger.debug("{} - Entering close. jndi={}", thisObjectTicket, jndi);

		if (jndi) {
			//do nothing
			//jndi datasource is managed by the container. 
			//there is no way to close it or all it's connections from the application
		} else {
			try {
				logger.debug("pool.size()={}", pool.size());
				for (int i = (pool.size() - 1); i >= 0; i--) {
					EnhancedConnection conn = pool.get(i);
					conn.realClose();
					pool.remove(i);
				}
			} catch (SQLException ex) {
				logger.error("Error. Connection pool='{}'", name, ex);
			}

			t.interrupt();
			t = null;
		}
	}

	/**
	 * Remove all existing connections from the pool, closing them properly.
	 * This really closes the underlying database connections.
	 */
	public void refreshConnections() {
		logger.debug("{} - Entering refreshConnections. Connection pool='{}'. jndi={}",
				thisObjectTicket, name, jndi);

		if (jndi) {
			//do nothing
			//jndi datasource is managed by the container. 
			//there is no way to close it or all it's connections from the application
		} else {
			//really close all connections, and remove them from the pool
			logger.debug("pool.size()={}", pool.size());
			for (int i = 0; i < pool.size(); i++) {
				EnhancedConnection conn = pool.get(i);
				try {
					conn.realClose();
				} catch (SQLException ex) {
					logger.error("Error. Connection pool='{}'", name, ex);
				}
				pool.remove(i);
			}
		}
	}

	/**
	 * Remove all existing connection from the pool without closing them. Useful
	 * if the connection.close() method hangs because of buggy drivers or if
	 * connections are null. In general you should not use this method as it
	 * means something is going wrong with your database.
	 */
	public void forceRefreshConnections() {
		logger.debug("{} - Entering forceRefreshConnections. Connection pool='{}'. jndi={}",
				thisObjectTicket, name, jndi);

		if (jndi) {
			//do nothing
			//jndi datasource is managed by the container. 
			//there is no way to close it or all it's connections from the application
		} else {
			//remove all connections from the pool without closing them first
			pool.clear();
		}

	}

	private synchronized Connection getConnectionFromPool() throws SQLException {
		logger.debug("{} - Entering getConnectionFromPool", thisObjectTicket);

		totalConnectionRequests++;

		try {
			logger.debug("{} - pool.size()={}", thisObjectTicket, pool.size());
			logger.debug("{} - maxConnections={}", thisObjectTicket, maxConnections);
			if (pool.isEmpty()) {
				logger.debug("{} - getNew (first)", thisObjectTicket);
				return getNewConnection();
			} else if (isThereAFreeConnection()) {
				logger.debug("{} - getFree", thisObjectTicket);
				return getFreeConnection();
			} else if (pool.size() < maxConnections) {
				logger.debug("{} - getNew", thisObjectTicket);
				return getNewConnection();
			} else {
				//return the "oldest" used connection. the one that was used last.
				//the one that likely will become free first
				logger.debug("{} - getOldest", thisObjectTicket);
				return getOldestConnection();
			}
		} catch (SQLException ex) {
			logger.error("Error. Connection pool='{}'", name, ex);
			throw new SQLException("getConnectionFromPool exception: " + ex.getMessage());
		}
	}

	private boolean isThereAFreeConnection() {
		boolean connectionAvailable = false;

		for (EnhancedConnection conn : pool) {
			if (!conn.isInUse()) {
				connectionAvailable = true;
				break;
			}
		}

		return connectionAvailable;
	}

	private EnhancedConnection getFreeConnection() {
		for (EnhancedConnection conn : pool) {
			if (!conn.isInUse()) {
				conn.open();
				return conn;
			}
		}

		return null; //there was no free connection
	}

	private EnhancedConnection getOldestConnection() {
		long oldestConnectionUsedTime = -1;
		int oldestConnectionIndex = -1;

		for (int i = 0; i < pool.size(); i++) {
			EnhancedConnection conn = pool.get(i);
			if (conn.getLastOpenTime() > oldestConnectionUsedTime) {
				oldestConnectionUsedTime = conn.getLastOpenTime();
				oldestConnectionIndex = i;
			}
		}

		EnhancedConnection oldestConnection = pool.get(oldestConnectionIndex);
		oldestConnection.open(); // this is useless as it is already opened - just set a new last used date
		return oldestConnection;
	}

	private EnhancedConnection getNewConnection() throws SQLException {
		EnhancedConnection conn = new EnhancedConnection(url, username, password, connectionProperties);
		pool.add(conn);
		if (pool.size() > biggestPoolSizeReached) {
			biggestPoolSizeReached = pool.size();
		}
		conn.open();
		return conn;
	}

	/**
	 * Get the current pool size.
	 *
	 * @return
	 */
	public int getCurrentPoolSize() {
		return pool.size();
	}

	/**
	 * Get the biggest pool size ever reached.
	 *
	 * @return
	 */
	public int getBiggestPoolSizeReached() {
		return biggestPoolSizeReached;
	}

	/**
	 * Get the total number of times a connection was requested from the pool.
	 *
	 * @return
	 */
	public long getTotalConnectionRequests() {
		return totalConnectionRequests;
	}

	/**
	 * This method is called automatically on timeouts to check if a connection
	 * needs to be closed. A connection will be closed if: <ul> <li> it was
	 * unused for more than the TIMEOUT period </li> </ul> A connection will be
	 * removed from the pool (without closing it) if it is in use from more than
	 * the MAX_QUERY_RUNNING_TIME period (probably the connection hangs),
	 * leaving to the garbage collector the work to destroy it. <br> If testing
	 * is enabled (see testSql()) the connection is tested after the timeout
	 * period.
	 */
	@Override
	public void timeElapsed() {
		logger.debug("{} - Entering timeElapsed. jndi={}", thisObjectTicket, jndi);

		if (!jndi) {
			try {
				for (int i = (pool.size() - 1); i >= 0; i--) {
					// if      the connection is free and was created before TIMEOUT millis
					//   or
					// if      the connection is not free but it was busy from more than MAX_QUERY_RUNNING_TIME millis
					// then  
					//         destroy the connection trying to close it only if it is not in use
					// else if test is enabled (and connection is to be kept)
					// then 
					//         perform a connection test

					//20140829 Timothy Anyona. Rearranged the code to make logic easier to understand
					EnhancedConnection conn = pool.get(i);
					boolean removeConnection = false;

					if (conn.getIdleTime() > timeout) {
						removeConnection = true;
					} else if (conn.getBusyTime() > maxQueryRunningTime) {
						removeConnection = true;
						logger.warn("Connection {i} of Connection pool '{}'"
								+ " was in use for too much time and has been"
								+ " removed from the pool", i, name);
					} else if (testSql != null && testSql.length() > 0) {
						logger.debug("{} - testSql='{}'", thisObjectTicket, testSql);
						try {
							conn.test(testSql);
						} catch (SQLException ex) {
							logger.error("Connection test failed. Connection pool='{}',"
									+ " Connection {}, testSql='{}'", name, i, testSql, ex);

							pool.remove(i);
							//conn.realClose();
						}
					}

					if (removeConnection) {
						//don't close busy connection. just remove from the pool.
						pool.remove(i);
						if (!conn.isInUse()) {
							conn.realClose();
						}
					}

				}
			} catch (SQLException ex) {
				logger.error("Error. Connection pool='{}'", name, ex);
			}
		}

	}

	/**
	 * Get the whole connection pool
	 *
	 * @return
	 */
	public List<EnhancedConnection> getPool() {
		return pool;
	}

	/**
	 * Get the number of connections that are currently in use in the whole pool
	 *
	 * @return
	 */
	public int getTotalInUseCount() {
		int count = 0;
		for (EnhancedConnection ec : pool) {
			if (ec.isInUse()) {
				count++;
			}
		}

		return count;
	}
}
