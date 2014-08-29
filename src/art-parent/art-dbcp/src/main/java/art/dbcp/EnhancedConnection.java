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
 */
public class EnhancedConnection implements Connection {
	/* 20050612  Statements are closed automatically when the EnanchedConnection is 
	 returned to the pool
	 20050612  test conection with a dummy sql, if it fails refresh the connection
	 */

	private static final Logger logger = LoggerFactory.getLogger(EnhancedConnection.class);
	private boolean inUse;
	private int inUseCount;
	private long lastOpenTime;
	private long lastCloseTime;
	private Connection conn; //actual underlying connection
	private List<Statement> openStatements;

	/**
	 * Constructor. Creates a connection to the given database using the given
	 * credentials
	 *
	 * @param url jdbc url
	 * @param username username to use in making the connection
	 * @param password password to use in making the connection
	 * @throws SQLException
	 */
	public EnhancedConnection(String url, String username, String password) throws SQLException {
		this(url, username, password, null);
	}

	/**
	 * Constructor. Creates a connection to the given database using the given
	 * credentials and properties
	 *
	 * @param url jdbc url
	 * @param username username to use in making the connection
	 * @param password password to use in making the connection
	 * @param properties any additional connection properties
	 * @throws SQLException
	 */
	public EnhancedConnection(String url, String username, String password, Properties properties) throws SQLException {
		logger.debug("Entering constructor: url='{}', username='{}", url, username);

		Properties dbProperties = new Properties();
		dbProperties.put("user", username);
		dbProperties.put("password", password);
		//add custom connection properties e.g. ApplicationName
		if (properties != null && properties.size() > 0) {
			dbProperties.putAll(properties);
		}
		Driver driver = DriverManager.getDriver(url); // get the right driver for the given url
		conn = driver.connect(url, dbProperties); // get the connection
		openStatements = new ArrayList<>();
	}

	/**
	 * Mark the connection as in use and update usage statistics. Always call
	 * open before providing a connection to users
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
	 * Close any open statements and mark the connection as available
	 * (inUse=false). The underlying database connection is not closed.
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
			//20140829 Timothy Anyona. reinstate this.close(). to at least clear the openStatements list?
			this.close();
			//this.close(); // close all the open statements: not needed... since the driver should do this
			conn.close();    // note: the caller (ArtDBCPDataSource) must remove the object from the pool
		} catch (SQLException ex) {
			logger.error("Error", ex);
			throw new SQLException("EnhancedConnection: error in realClose(): cause: " + ex.getMessage());
		}

	}

	/**
	 * Execute an sql statement on the connection, in order to test if the
	 * connection is still valid
	 *
	 * @param testSql
	 * @throws SQLException
	 */
	protected void test(String testSql) throws SQLException {
		try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(testSql)) {
			while (rs.next()) {
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean isInUse() {
		return inUse;
	}

	/**
	 *
	 * @return
	 */
	public int getInUseCount() {
		return inUseCount;
	}

	/**
	 * The last time that the connection was requested
	 *
	 * @return
	 */
	public long getLastOpenTime() {
		return lastOpenTime;
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
		//20140829 Timothy Anyona. Added method to ease logic in ArtDBCPDataSource
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
		//20140829 Timothy Anyona. Added method to ease logic in ArtDBCPDataSource
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
