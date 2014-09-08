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

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection that rather than closing the underlying connection, marks itself
 * as available when closed, so that it can be reused in a connection pool. A
 * realClose method is provided to really close the underlying connection.
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 * @since 3.0.0, rename of art.dbcp.EnhancedConnection
 */
public class PooledConnection implements Connection {
	/* 20050612  Statements are closed automatically when the EnanchedConnection is 
	 returned to the pool
	 20050612  test conection with a dummy sql, if it fails refresh the connection
	 */

	private static final Logger logger = LoggerFactory.getLogger(PooledConnection.class);
	private boolean inUse;
	private int inUseCount;
	private long lastOpenTime;
	private long lastCloseTime;
	private Connection conn; //actual underlying connection
	private List<Statement> openStatements;

	/**
	 * Creates a connection to the database with the given url using the given
	 * credentials
	 *
	 * @param jdbcUrl
	 * @param username
	 * @param password
	 * @throws SQLException
	 */
	public PooledConnection(String jdbcUrl, String username, String password) throws SQLException {
		this(jdbcUrl, username, password, null);
	}

	/**
	 * Creates a connection to the database with the given url using the given
	 * credentials and connection properties
	 *
	 * @param jdbcUrl
	 * @param username
	 * @param password
	 * @param properties any additional connection properties
	 * @throws SQLException
	 */
	public PooledConnection(String jdbcUrl, String username, String password, Properties properties) throws SQLException {
		logger.debug("Entering PooledConnection: jdbcUrl='{}', username='{}", jdbcUrl, username);

		Properties dbProperties = new Properties();
		dbProperties.put("user", username);
		dbProperties.put("password", password);
		//add custom connection properties e.g. ApplicationName
		if (properties != null && properties.size() > 0) {
			dbProperties.putAll(properties);
		}
		Driver driver = DriverManager.getDriver(jdbcUrl); // get the right driver for the given url
		conn = driver.connect(jdbcUrl, dbProperties); // get the connection
		openStatements = new ArrayList<>();
	}

	/**
	 * Marks the connection as in use (inUse=true). Always call open before
	 * providing a connection to users.
	 */
	public void open() {
		logger.debug("Entering open");

		inUse = true;
		inUseCount++;
		lastOpenTime = System.currentTimeMillis();

		logger.debug("inUseCount={}", inUseCount);
		logger.debug("lastOpenTime={}", lastOpenTime);
	}

	/**
	 * Marks the connection as available (inUse=false). The underlying database
	 * connection is not closed but any open statements are closed.
	 *
	 * @throws SQLException
	 */
	@Override
	public void close() throws SQLException {
		logger.debug("Entering close");

		inUseCount--;

		logger.debug("inUseCount={}", inUseCount);
		if (inUseCount == 0) {
			// close any open statement / prepared statement
			logger.debug("openStatements.size()={}", openStatements.size());
			for (int i = 0; i < openStatements.size(); i++) {
				Statement st = openStatements.get(i);
				if (st != null) {
					logger.debug("Closing statement {}", i);
					st.close();
				}
			}
			openStatements.clear();

			inUse = false;

			lastCloseTime = System.currentTimeMillis();
			logger.debug("lastCloseTime", lastCloseTime);
		} else {
			logger.info("More threads are using the same connection - statements not closed. inUseCount={}", inUseCount);
		}

	}

	/**
	 * Really close the underlying database connection
	 *
	 * @throws SQLException
	 */
	protected void realClose() throws SQLException {
		logger.debug("Entering realClose");

		try {
			//this.close(); // close all the open statements: not needed... since the driver should do this
			conn.close();    // note: the caller (ArtDBCPDataSource) must remove the object from the pool
			openStatements.clear();
		} catch (SQLException ex) {
			throw new SQLException("PooledConnection: error in realClose(): cause: " + ex.getMessage());
		}
	}

	/**
	 * Determine if the connection is valid by executing an sql statement
	 *
	 * @param testSql the sql statement to test the connection
	 * @return true if the connection is valid, false otherwise
	 */
	protected boolean isValid(String testSql) {
		logger.debug("Entering isValid: testSql='{}'", testSql);

		Objects.requireNonNull(testSql, "testSql must not be null");

		boolean valid = false;

		try {
			if (testSql.equals("isValid")) {
				//use jdbc 4 connection isValid method
				final int TIMEOUT_SECONDS = 10;
				valid = conn.isValid(TIMEOUT_SECONDS);
			} else {
				try (Statement st = conn.createStatement()) {
					st.executeQuery(testSql);
					//if no exception thrown, connection is valid
					valid = true;
				}
			}
		} catch (SQLException ex) {
			logger.error("Connection test failed", ex);
		}

		return valid;
	}

	/**
	 * Determine if the connection is currently in use
	 *
	 * @return true if the connection is in use, false otherwise
	 */
	public boolean isInUse() {
		return inUse;
	}

	/**
	 * Get the number of clients/threads who are using this connection
	 *
	 * @return the number of clients/threads who are using this connection
	 */
	public int getInUseCount() {
		return inUseCount;
	}

	/**
	 * The last time that the connection was opened (requested)
	 *
	 * @return last time that the connection was opened (requested) as
	 * milliseconds
	 */
	public long getLastOpenTime() {
		return lastOpenTime;
	}

	/**
	 * The last time that the connection was closed (returned to the pool)
	 *
	 * @return last time that the connection was closed (returned to the pool)
	 * as milliseconds
	 *
	 * @since 3.0.0
	 */
	public long getLastCloseTime() {
		return lastCloseTime;
	}

	/**
	 * Get the idle time for this connection
	 *
	 * @return time in milliseconds that this connection has been idle, or -1 if
	 * it is not idle i.e. it is currently in use
	 *
	 * @since 3.0.0
	 */
	public long getIdleTime() {
		if (!inUse) {
			return System.currentTimeMillis() - lastCloseTime;
		} else {
			return -1;
		}
	}

	/**
	 * Get the busy time for this connection
	 *
	 * @return time in milliseconds that this connection has been busy (in use),
	 * or -1 if it is not busy i.e. it is currently not in use
	 *
	 * @since 3.0.0
	 */
	public long getBusyTime() {
		if (inUse) {
			return System.currentTimeMillis() - lastOpenTime;
		} else {
			return -1;
		}
	}


	/*
	 * Here are all the standard JDBC Connection methods except for: close() it
	 * has been re-defined above for pool handling createStatement and
	 * prepareStatement where the "open" statements are maintained in a list in
	 * order to close it when the connection is returned to the pool.
	 */
	//--------------------------JDBC 1.0 (JDK 1.1)-----------------------------
	@Override
	public Statement createStatement() throws SQLException {
		Statement st = conn.createStatement();
		openStatements.add(st);
		return st;
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return conn.getMetaData();
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		CallableStatement cs = conn.prepareCall(sql);
		openStatements.add(cs);
		return cs;
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return conn.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		conn.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return conn.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		conn.commit();
	}

	@Override
	public void rollback() throws SQLException {
		conn.rollback();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return conn.isClosed();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		conn.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return conn.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		conn.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return conn.getCatalog();
	}

	@Override
	public java.sql.SQLWarning getWarnings() throws SQLException {
		return conn.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		conn.clearWarnings();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		conn.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return conn.getTransactionIsolation();
	}

	//--------------------------JDBC 2.0 (JDK 1.2, 1.3)-----------------------------
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {

		Statement st = conn.createStatement(resultSetType, resultSetConcurrency);
		openStatements.add(st);
		return st;
	}

	@Override
	public PreparedStatement prepareStatement(String Sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {

		PreparedStatement ps = conn.prepareStatement(Sql, resultSetType, resultSetConcurrency);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {

		CallableStatement cs = conn.prepareCall(sql, resultSetType, resultSetConcurrency);
		openStatements.add(cs);
		return cs;
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return conn.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		conn.setTypeMap(map);
	}

	//--------------------------JDBC 3.0 (JDK 1.4, 1.5)-----------------------------
	@Override
	public void setHoldability(int holdability) throws SQLException {
		conn.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return conn.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return conn.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return conn.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		conn.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		conn.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {

		Statement st = conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
		openStatements.add(st);
		return st;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {

		PreparedStatement ps = conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql, autoGeneratedKeys);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql, columnIndexes);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql, columnNames);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {

		CallableStatement cs = conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		openStatements.add(cs);
		return cs;
	}

	//--------------------------JDBC 4.0 (JDK 1.6)-----------------------------
	@Override
	public Clob createClob() throws SQLException {
		return conn.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return conn.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return conn.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return conn.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return conn.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		conn.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		conn.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return conn.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return conn.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return conn.createArrayOf(typeName, elements);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return conn.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return conn.isWrapperFor(iface);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return conn.createStruct(typeName, attributes);
	}

	//--------------------------JDBC 4.1 (JDK 1.7, 1.8)-----------------------------
	@Override
	public void abort(Executor executor) throws SQLException {
		conn.abort(executor);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return conn.getNetworkTimeout();
	}

	@Override
	public String getSchema() throws SQLException {
		return conn.getSchema();
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		conn.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		conn.setSchema(schema);
	}

}
