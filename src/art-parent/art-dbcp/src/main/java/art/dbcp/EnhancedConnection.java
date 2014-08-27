/*
 * Copyright 2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of artdbcp.
 *
 * artdbcp is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 2.1 of the License.
 *
 * artdbcp is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with artdbcp. If not, see <http://www.gnu.org/licenses/>.
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
 *
 * @author Enrico Liboni
 */
public class EnhancedConnection implements Connection {
	/* 20050612  Statements are closed automatically when the EnanchedConnection is 
	 returned to the pool
	 20050612  test conection with a dummy sql, if it fails refresh the connection
	 */

	private static final Logger logger = LoggerFactory.getLogger(EnhancedConnection.class);
	private boolean inUse = false;
	private int inUseCount = 0;
	private long lastUsedTime = 0;
	private Connection c;
	private List<Statement> openStatements;

	public EnhancedConnection(String url, String username, String password) throws SQLException {
		this(url, username, password, null);
	}

	public EnhancedConnection(String url, String username, String password, Properties properties) throws SQLException {
		logger.debug("Entering constructor");

		Properties dbProperties = new Properties();
		dbProperties.put("user", username);
		dbProperties.put("password", password);
		//add custom connection properties e.g. ApplicationName
		if (properties != null && properties.size() > 0) {
			dbProperties.putAll(properties);
		}
		Driver d = DriverManager.getDriver(url); // get the right driver for the given url
		c = d.connect(url, dbProperties); // get the connection
		openStatements = new ArrayList<>();
	}

	/**
	 * Open connection
	 */
	public void open() {
		inUse = true;
		inUseCount++;
		lastUsedTime = new java.util.Date().getTime();
	}

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
		} else {
			logger.info("More threads are using the same connection - statements not closed. inUseCount={}", inUseCount);
		}

	}

	/**
	 *
	 * @throws SQLException
	 */
	protected void realClose() throws SQLException {
		logger.debug("Entering realClose");

		try {
			//this.close(); // close all the open statements: not needed... since the driver should do this
			c.close();    // note: the caller (Datasource)  must remove the object from the pool
		} catch (SQLException ex) {
			logger.error("Error", ex);
			//why catch and throw?
			throw new SQLException("EnanchedConnection: error in realClose(): cause: " + ex.getMessage());
		}

	}

	/**
	 *
	 * @param testSQL
	 * @throws SQLException
	 */
	protected void test(String testSQL) throws SQLException {
		try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(testSQL)) {
			while (rs.next()) {
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean getInUse() {
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
	 *
	 * @return
	 */
	public long getLastUsedTime() {
		return lastUsedTime;
	}


	/*
	 * Here are all the standard JDBC Connection methods except for: close() it
	 * has been re-defined above for pool handling createStatement and
	 * prepareStatement where the "open" statements are maintained in a list in
	 * order to close it when the connection is returned to the pool.
	 */
	@Override
	public Statement createStatement() throws SQLException {
		Statement st = c.createStatement();
		openStatements.add(st);
		return st;
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement ps = c.prepareStatement(sql);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return c.getMetaData();
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		CallableStatement cs = c.prepareCall(sql);
		openStatements.add(cs);
		return cs;
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		return c.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		c.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return c.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		c.commit();
	}

	@Override
	public void rollback() throws SQLException {
		c.rollback();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return c.isClosed();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		c.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return c.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		c.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		return c.getCatalog();
	}

	@Override
	public java.sql.SQLWarning getWarnings() throws SQLException {
		return c.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		c.clearWarnings();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		c.setTransactionIsolation(level);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return c.getTransactionIsolation();
	}

	//--------------------------JDBC 2.0 (JDK 1.2)-----------------------------
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {

		Statement st = c.createStatement(resultSetType, resultSetConcurrency);
		openStatements.add(st);
		return st;
	}

	@Override
	public PreparedStatement prepareStatement(String Sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {

		PreparedStatement ps = c.prepareStatement(Sql, resultSetType, resultSetConcurrency);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {

		CallableStatement cs = c.prepareCall(sql, resultSetType, resultSetConcurrency);
		openStatements.add(cs);
		return cs;
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return c.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		c.setTypeMap(map);
	}

	//--------------------------JDBC 3.0 (JDK 1.4/1.5)-----------------------------
	@Override
	public void setHoldability(int holdability) throws SQLException {
		c.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		return c.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		return c.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		return c.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		c.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		c.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {

		Statement st = c.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
		openStatements.add(st);
		return st;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {

		PreparedStatement ps = c.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		PreparedStatement ps = c.prepareStatement(sql, autoGeneratedKeys);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException {
		PreparedStatement ps = c.prepareStatement(sql, columnIndexes);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException {
		PreparedStatement ps = c.prepareStatement(sql, columnNames);
		openStatements.add(ps);
		return ps;
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {

		CallableStatement cs = c.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		openStatements.add(cs);
		return cs;
	}

	//--------------------------JDBC 4.0 (JDK 1.6)-----------------------------
	@Override
	public Clob createClob() throws SQLException {
		return c.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return c.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return c.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return c.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return c.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		c.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		c.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return c.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return c.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return c.createArrayOf(typeName, elements);
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return c.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return c.isWrapperFor(iface);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return c.createStruct(typeName, attributes);
	}

	//--------------------------JDBC 4.1 (JDK 1.7)-----------------------------
	@Override
	public void abort(Executor executor) throws SQLException {
		c.abort(executor);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return c.getNetworkTimeout();
	}

	@Override
	public String getSchema() throws SQLException {
		return c.getSchema();
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		c.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		c.setSchema(schema);
	}

}
