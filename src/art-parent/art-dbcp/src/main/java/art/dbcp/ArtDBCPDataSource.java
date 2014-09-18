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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a database connection pool. Features:
 *
 * <ul>
 * <li> The pool dynamically increases/decreases. An unused connection is
 * removed from the pool after <code>timeout</code> seconds.
 * </li>
 * <li> The underlying work is transparent from the developer's point of view. A
 * connection is obtained from the pool using the getConnection() method. A
 * connection is returned to the pool by simply closing the connection (any open
 * statement is automatically closed thus you might not need to close statements
 * and resultsets).
 * </li>
 * <li>
 * Connections that are open for too long are removed automatically to prevent
 * your application from hanging because of buggy drivers or network problems
 * </li>
 * </ul>
 *
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
 * @author Timothy Anyona
 * @since 3.0.0, rename of art.dbcp.DataSource
 */
public class ArtDBCPDataSource implements TimerListener, DataSource {

	private static final Logger logger = LoggerFactory.getLogger(ArtDBCPDataSource.class);
	private String poolName; //a name for this connection pool
	private String url;
	private String username;
	private String password;
	private static final long DEFAULT_TIMEOUT_SECONDS = TimeUnit.MINUTES.toSeconds(30); //30 minutes
	private long timeoutMillis; //timeout after which idle connections are closed
	private static final long DEFAULT_MAX_QUERY_RUNNING_TIME_SECONDS = TimeUnit.MINUTES.toSeconds(20); // 20 minutes
	private long maxQueryRunningTimeMillis; // max running time for a query, before its connection is forcibly removed from the pool
	private long totalConnectionRequests;
	private int biggestPoolSizeReached;
	private int maxPoolSize = 10; //max number of underlying connections that can be created
	private String testSql;
	LocalTimer t;
	private final List<PooledConnection> pool = new ArrayList<>(maxPoolSize); //stores active connections
	private long thisObjectTicket; // for debugging
	private String driverClassName; //The fully qualified Java class name of the JDBC driver to be used
	private Properties connectionProperties; //any additional connection properties, apart from the "user" and "password" properties

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
	 * driverClassName or network problems. Usually a connection.close()
	 * statement waits until the connection finishes to execute a query: if the
	 * connection hangs, the risk is to have the connection.close() waiting
	 * forever. Removing the connection from the pool does not kill current
	 * query executions (if a query takes more than 20 minutes to run it will
	 * finish correctly) but leave to the garbage collector the task of
	 * closing/killing it. On JSPs - where queries are usually quick ones and 20
	 * minutes means a connection problem - this will allow your application to
	 * run smoothly.
	 *
	 * @param timeoutSeconds
	 * @param maxQueryRunningTimeSeconds
	 */
	public ArtDBCPDataSource(long timeoutSeconds, long maxQueryRunningTimeSeconds) {
		logger.debug("Entering ArtDBCPDataSource: timeoutSeconds={}, maxQueryRunningTimeSeconds={}",
				timeoutSeconds, maxQueryRunningTimeSeconds);

		if (timeoutSeconds < 0) {
			throw new IllegalArgumentException("Invalid timeoutSeconds: " + timeoutSeconds
					+ ". timeoutSeconds cannot be < 0");
		}
		if (maxQueryRunningTimeSeconds < 0) {
			throw new IllegalArgumentException("Invalid maxQueryRunningTimeSeconds: "
					+ maxQueryRunningTimeSeconds + ". maxQueryRunningTimeSeconds cannot be < 0");
		}

		this.timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds);
		this.maxQueryRunningTimeMillis = TimeUnit.SECONDS.toMillis(maxQueryRunningTimeSeconds);
		startTimer();
	}

	/**
	 * Create a connection pool with the connection timeout set to
	 * <code>timeout</code> (in sec)
	 *
	 * @param timeout
	 */
	public ArtDBCPDataSource(long timeout) {
		this(timeout, DEFAULT_MAX_QUERY_RUNNING_TIME_SECONDS);
	}

	/**
	 * Create a connection pool. The default connection timeout is
	 * <code>30</code> minutes
	 *
	 */
	public ArtDBCPDataSource() {
		this(DEFAULT_TIMEOUT_SECONDS, DEFAULT_MAX_QUERY_RUNNING_TIME_SECONDS);
	}

	private void startTimer() {
		logger.debug("Entering startTimer");

		logger.debug("timeoutMillis={}", timeoutMillis);

		// Start timer for connection timeout
		t = new LocalTimer((TimerListener) this, timeoutMillis);
		t.start();

		thisObjectTicket = System.currentTimeMillis();
		logger.debug("thisObjectTicket={}", thisObjectTicket);

		try {
			//set maximum time to wait for a connection to be made.
			//some drivers may not honour this, using a url parameter instead e.g. mysql connector/j
			//see https://stackoverflow.com/questions/1683949/connection-timeout-for-drivermanager-getconnection
			final int TIMEOUT_SECONDS = 10;
			DriverManager.setLoginTimeout(TIMEOUT_SECONDS);
		} catch (Exception ex) {
			logger.error("Driver not able to set login timeout", ex);
		}
	}

	/**
	 * @return the connectionProperties
	 */
	public Properties getConnectionProperties() {
		return connectionProperties;
	}

	/**
	 * Set additional properties to be passed to the jdbc driver when making the
	 * connection, apart from the "user" and "password" properties
	 *
	 * @param connectionProperties the connectionProperties to set
	 */
	public void setConnectionProperties(Properties connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	/**
	 * Set the fully qualified Java class name of the JDBC driver to be used
	 *
	 * @param value
	 */
	public void setDriverClassName(String value) {
		driverClassName = value;
	}

	/**
	 * Get the driver class name
	 *
	 * @return the driver class name
	 */
	public String getDriverClassName() {
		return driverClassName;
	}

	/**
	 * Set the maximum number of concurrent active connections. Default is 10.
	 *
	 * @param maxPoolSize
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	/**
	 * Set the name of the connection pool
	 *
	 * @param poolName
	 */
	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	/**
	 * Set the database JDBC url
	 *
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Set the database username to use for making the connection
	 *
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Set the database password to use for making the connection
	 *
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Set the sql query to use to test the validity of connections. If not set,
	 * connection tests will not be done
	 *
	 * Every <i>timeout</i> seconds a connection is tested by executing the
	 * given sql query. If the query execution raises an exception the
	 * connection is removed from the pool.
	 *
	 * @param testSql
	 */
	public void setTestSql(String testSql) {
		this.testSql = testSql;
	}

	/**
	 * Get the connection pool name
	 *
	 * @return the connection pool name
	 */
	public String getPoolName() {
		return poolName;
	}

	/**
	 * Get the database JDBC URL
	 *
	 * @return the database JDBC URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Get the database username
	 *
	 * @return the database username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get the database password
	 *
	 * @return the database password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Get the current pool size
	 *
	 * @return the current pool size
	 */
	public int getCurrentPoolSize() {
		return pool.size();
	}

	/**
	 * Get the biggest pool size ever reached
	 *
	 * @return the biggest pool size ever reached
	 */
	public int getBiggestPoolSizeReached() {
		return biggestPoolSizeReached;
	}

	/**
	 * Get the total number of times a connection was requested from the pool
	 *
	 * @return the total number of times a connection was requested from the
	 * pool
	 */
	public long getTotalConnectionRequests() {
		return totalConnectionRequests;
	}

	/**
	 * Get a connection from the pool
	 *
	 * @return a useable database connection
	 * @throws SQLException
	 */
	@Override
	public Connection getConnection() throws SQLException {
		logger.debug("{} - Entering getConnection", thisObjectTicket);

		logger.debug("(t == null) = {}", t == null);
		if (t == null) {
			startTimer();
		}

		return getConnectionFromPool();
	}

	/**
	 * Really close all underlying database connections, clear the pool and stop
	 * the timeout timer
	 */
	public void close() {
		logger.debug("{} - Entering close", thisObjectTicket);

		for (PooledConnection conn : pool) {
			try {
				conn.realClose();
			} catch (SQLException ex) {
				logger.error("Error. Connection pool='{}'", poolName, ex);
			}
		}

		pool.clear();

		t.interrupt();
		t = null;
	}

	/**
	 * Remove all existing connections from the pool, closing them properly.
	 * This really closes the underlying database connections.
	 */
	public void refreshConnections() {
		logger.debug("{} - Entering refreshConnections. Connection pool='{}'",
				thisObjectTicket, poolName);

		//really close all connections, and remove them from the pool
		logger.debug("pool.size()={}", pool.size());
		for (int i = 0; i < pool.size(); i++) {
			PooledConnection conn = pool.get(i);
			try {
				conn.realClose();
			} catch (SQLException ex) {
				logger.error("Error. Connection pool='{}'", poolName, ex);
			}
			pool.remove(i);
		}
	}

	/**
	 * Remove all existing connection from the pool without closing them. Useful
	 * if the connection.close() method hangs because of buggy drivers or if
	 * connections are null. In general you should not use this method as it
	 * means something is going wrong with your database.
	 */
	public void forceRefreshConnections() {
		logger.debug("{} - Entering forceRefreshConnections. Connection pool='{}'",
				thisObjectTicket, poolName);

		//remove all connections from the pool without closing them first
		pool.clear();
	}

	private synchronized Connection getConnectionFromPool() throws SQLException {
		logger.debug("{} - Entering getConnectionFromPool", thisObjectTicket);

		totalConnectionRequests++;
		logger.debug("totalConnectionRequests={}", totalConnectionRequests);

		try {
			logger.debug("{} - pool.size()={}", thisObjectTicket, pool.size());
			logger.debug("{} - maxPoolSize={}", thisObjectTicket, maxPoolSize);
			if (pool.isEmpty()) {
				logger.debug("{} - getNew (first)", thisObjectTicket);
				return getNewConnection();
			} else if (isThereAFreeConnection()) {
				logger.debug("{} - getFree", thisObjectTicket);
				return getFreeConnection();
			} else if (pool.size() < maxPoolSize) {
				logger.debug("{} - getNew", thisObjectTicket);
				return getNewConnection();
			} else {
				//return the "oldest" used connection. the one that was used last.
				//the one that likely will become free first
				logger.debug("{} - getOldest", thisObjectTicket);
				return getOldestConnection();
			}
		} catch (SQLException ex) {
			logger.error("Error. Connection pool='{}'", poolName, ex);
			throw new SQLException("getConnectionFromPool exception: " + ex.getMessage());
		}
	}

	private boolean isThereAFreeConnection() {
		boolean connectionAvailable = false;

		for (PooledConnection conn : pool) {
			if (!conn.isInUse()) {
				connectionAvailable = true;
				break;
			}
		}

		return connectionAvailable;
	}

	private PooledConnection getFreeConnection() {
		for (PooledConnection conn : pool) {
			if (!conn.isInUse()) {
				conn.open();
				return conn;
			}
		}

		return null; //there was no free connection
	}

	private PooledConnection getOldestConnection() {
		long oldestConnectionUsedTime = -1;
		int oldestConnectionIndex = -1;

		for (int i = 0; i < pool.size(); i++) {
			PooledConnection conn = pool.get(i);
			if (conn.getLastOpenTime() > oldestConnectionUsedTime) {
				oldestConnectionUsedTime = conn.getLastOpenTime();
				oldestConnectionIndex = i;
			}
		}

		PooledConnection oldestConnection = pool.get(oldestConnectionIndex);
		oldestConnection.open(); // this is useless as it is already opened - just set a new last used date
		return oldestConnection;
	}

	private PooledConnection getNewConnection() throws SQLException {
		PooledConnection conn = new PooledConnection(url, username, password, connectionProperties);
		pool.add(conn);
		if (pool.size() > biggestPoolSizeReached) {
			biggestPoolSizeReached = pool.size();
		}
		conn.open();
		return conn;
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
		logger.debug("{} - Entering timeElapsed", thisObjectTicket);

		try {
			logger.debug("pool.size()={}", pool.size());
			for (int i = (pool.size() - 1); i >= 0; i--) {
				// if      the connection is free and was created before TIMEOUT millis
				//   or
				// if      the connection is not free but it was busy from more than MAX_QUERY_RUNNING_TIME millis
				// then  
				//         destroy the connection trying to close it only if it is not in use
				// else if isValid is enabled (and connection is to be kept)
				// then 
				//         perform a connection isValid

				PooledConnection conn = pool.get(i);
				boolean removeConnection = false;

				long idleTime = conn.getIdleTime();
				long busyTime = conn.getBusyTime();

				logger.debug("idleTime={}", idleTime);
				logger.debug("busyTime={}", busyTime);
				logger.debug("timeoutMillis={}", timeoutMillis);
				logger.debug("maxQueryRunningTimeMillis={}", maxQueryRunningTimeMillis);
				logger.debug("testSql='{}'", testSql);

				if (idleTime > timeoutMillis) {
					removeConnection = true;
				} else if (busyTime > maxQueryRunningTimeMillis) {
					removeConnection = true;
					logger.warn("Connection {i} of Connection pool '{}'"
							+ " was in use for too much time and has been"
							+ " removed from the pool", i, poolName);
				} else if (testSql != null && testSql.length() > 0) {
					if (!conn.isValid(testSql)) {
						logger.error("Connection test failed. Connection pool='{}',"
								+ " Connection {}, testSql='{}'", poolName, i, testSql);

						//20140902 Timothy Anyona. remove without realClose means connection will never be closed?
						pool.remove(i);
						//conn.realClose();
					}
				}

				if (removeConnection) {
					//don't close busy connection. just remove from the pool.
					//20140902 Timothy Anyona. remove without realClose means connection will never be closed?
					pool.remove(i);
					if (!conn.isInUse()) {
						conn.realClose();
					}
				}
			}
		} catch (SQLException ex) {
			logger.error("Error. Connection pool='{}'", poolName, ex);
		}
	}

	/**
	 * Get the number of connections that are currently in use in the whole pool
	 *
	 * @return the number of connections that are currently in use in the whole
	 * pool
	 */
	public int getInUseCount() {
		int count = 0;
		for (PooledConnection conn : pool) {
			if (conn.isInUse()) {
				count++;
			}
		}

		return count;
	}

	//-----java.sql.DataSource interface methods, apart from getConnection()-------------
	/**
	 * Not supported. Always throws UnsupportedOperationException
	 *
	 * @param username
	 * @param password
	 * @return Always throws UnsupportedOperationException
	 * @throws SQLException
	 */
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Not supported. Always throws UnsupportedOperationException
	 *
	 * @return Always throws UnsupportedOperationException
	 * @throws SQLException
	 */
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Not supported. Always throws UnsupportedOperationException
	 *
	 * @param out
	 * @throws SQLException
	 */
	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Not supported. Always throws UnsupportedOperationException
	 *
	 * @param seconds
	 * @throws SQLException
	 */
	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Not supported. Always throws UnsupportedOperationException
	 *
	 * @return Always throws UnsupportedOperationException
	 * @throws SQLException
	 */
	@Override
	public int getLoginTimeout() throws SQLException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Not supported. Always throws UnsupportedOperationException
	 *
	 * @return Always throws UnsupportedOperationException
	 * @throws SQLFeatureNotSupportedException
	 */
	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Not supported. Always throws UnsupportedOperationException
	 *
	 * @param <T>
	 * @param iface
	 * @return Always throws UnsupportedOperationException
	 * @throws SQLException
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/**
	 * Not supported. Always throws UnsupportedOperationException
	 *
	 * @param iface
	 * @return Always throws UnsupportedOperationException
	 * @throws SQLException
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("Not supported.");
	}
}
