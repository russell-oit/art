/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.dbutils;

import art.datasource.Datasource;
import art.dbcp.ArtDBCPDataSource;
import art.enums.ConnectionPoolLibrary;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates different javax.sql.DataSource implementations in order to
 * provide a common interface for aspects which may be implemented differently.
 * An example is where some libraries don't have a pool name facility so this
 * class provides that, enabling searching for connection pools by name.
 *
 * @author Timothy Anyona
 */
public class ConnectionPoolWrapper {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolWrapper.class);

	private int poolId;
	private String poolName;
	private DataSource pool;

	public ConnectionPoolWrapper(Datasource ds, int maxPoolSize,
			ConnectionPoolLibrary library) throws NamingException {

		Objects.requireNonNull(ds, "ds must not be null");

		createConnectionPool(ds, maxPoolSize, library);
	}

	/**
	 * @return the poolId
	 */
	public int getPoolId() {
		return poolId;
	}

	/**
	 * @return the poolName
	 */
	public String getPoolName() {
		return poolName;
	}

	/**
	 * @return the pool
	 */
	public DataSource getPool() {
		return pool;
	}

	/**
	 * Closes the connection pool. This will close all the underlying database
	 * connections in the pool.
	 */
	public void closePool() {
		if (pool instanceof ArtDBCPDataSource) {
			ArtDBCPDataSource artDbcpDataSource = (ArtDBCPDataSource) pool;
			artDbcpDataSource.close();
		} else if (pool instanceof HikariDataSource) {
			HikariDataSource hikariDataSource = (HikariDataSource) pool;
			hikariDataSource.close();
		}
	}

	public Integer getCurrentPoolSize() {
		Integer currentPoolSize = null; //use object wrapper rather than primitive for "undefined" status, where library doesn't support the property

		if (pool instanceof ArtDBCPDataSource) {
			ArtDBCPDataSource artDbcpDataSource = (ArtDBCPDataSource) pool;
			currentPoolSize = artDbcpDataSource.getCurrentPoolSize();
		}

		return currentPoolSize;
	}

	public Integer getInUseCount() {
		Integer inUseCount = null;

		if (pool instanceof ArtDBCPDataSource) {
			ArtDBCPDataSource artDbcpDataSource = (ArtDBCPDataSource) pool;
			inUseCount = artDbcpDataSource.getInUseCount();
		}

		return inUseCount;
	}
	
	private void createConnectionPool(Datasource ds, int maxPoolSize,
			ConnectionPoolLibrary connectionPoolLibrary) throws NamingException {

		if (ds.isJndi()) {
			//for jndi datasources, the url contains the jndi name/resource reference
			pool = getJndiDataSource(ds.getUrl(),ds.isUseDefaultJndiNamespace());
		} else if (connectionPoolLibrary == ConnectionPoolLibrary.HikariCP) {
			pool = createHikariCPConnectionPool(ds, maxPoolSize);
		} else {
			pool = createArtDBCPConnectionPool(ds, maxPoolSize);
		}

		poolName = ds.getName();
		poolId = ds.getDatasourceId();
	}

	private DataSource createArtDBCPConnectionPool(Datasource ds, int maxPoolSize) {
		long timeoutSeconds = ds.getConnectionPoolTimeout() * 60L;  //convert timeout mins to seconds
		ArtDBCPDataSource newPool = new ArtDBCPDataSource(timeoutSeconds);

		newPool.setPoolName(ds.getName()); //use the datasoure name as the connection pool name
		newPool.setUsername(ds.getUsername());
		newPool.setPassword(ds.getPassword());
		newPool.setMaxPoolSize(maxPoolSize);
		newPool.setUrl(ds.getUrl());
		newPool.setDriverClassName(ds.getDriver());
		newPool.setTestSql(ds.getTestSql());

		//set application name connection property
		newPool.setConnectionProperties(getAppNameProperty(ds.getUrl(), ds.getName()));

		//register driver so that connections are immediately usable
		registerDriver(ds.getDriver());

		return newPool;
	}

	private DataSource createHikariCPConnectionPool(Datasource ds, int maxPoolSize) {
		HikariConfig config = new HikariConfig();

		config.setPoolName(ds.getName());
		config.setUsername(ds.getUsername());
		config.setPassword(ds.getPassword());
		config.setMaximumPoolSize(maxPoolSize);
		config.setJdbcUrl(ds.getUrl());
		config.setDriverClassName(ds.getDriver());
		if (StringUtils.equals(ds.getTestSql(), "isValid")) {
			config.setJdbc4ConnectionTest(true);
		} else {
			config.setJdbc4ConnectionTest(false);
			config.setConnectionTestQuery(ds.getTestSql());
		}

		long idleTimeoutMillis = ds.getConnectionPoolTimeout() * 60L * 1000L;  //convert timeout mins to milliseconds
		config.setIdleTimeout(idleTimeoutMillis);

		//set application name connection property
		config.setDataSourceProperties(getAppNameProperty(ds.getUrl(), ds.getName()));

		return new HikariDataSource(config);
	}

	private void registerDriver(String driver) {
		logger.debug("Entering registerDriver: driver='{}'", driver);

		try {
			Class.forName(driver).newInstance();
			logger.debug("JDBC driver registered: {}", driver);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			logger.error("Error while registering JDBC driver: '{}'", driver, ex);
		}
	}

	/**
	 * Get application name connection property to identify ART connections
	 *
	 * @param pool
	 */
	private Properties getAppNameProperty(String dbUrl, String poolName) {
		//ApplicationName property
		//see http://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html#setClientInfo%28java.lang.String,%20java.lang.String%29
		//has different name and maxlength for different drivers
		//maxlength mostly in the 254 range. Some exceptions include postgresql maxlength=64
		//some drivers don't seem to define it explicitly so may not support it and throw exception?
		//e.g. mysql, hsqldb

		Properties properties = new Properties();

		String connectionName = "ART - " + poolName;

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

		return properties;
	}

	private static DataSource getJndiDataSource(String jndiName, boolean useDefaultNamespace) throws NamingException {
		logger.debug("Entering getJndiDataSource: jndiName='{}'", jndiName);

		//throw exception if jndi url is null, rather than returning a null connection, which would be useless
		Objects.requireNonNull(jndiName, "jndiName must not be null");

		String finalJndiName;
		if (useDefaultNamespace) {
			finalJndiName = "java:comp/env/" + jndiName;
		} else {
			finalJndiName = jndiName;
		}
		InitialContext ic = new InitialContext();
		logger.debug("finalJndiName='{}'", finalJndiName);
		return (DataSource) ic.lookup(finalJndiName);
	}

	/**
	 * Get connection located by the given jndi name
	 *
	 * @param jndiName
	 * @param useDefaultNamespace
	 * @return
	 * @throws SQLException
	 * @throws javax.naming.NamingException
	 */
	public static Connection getJndiConnection(String jndiName, boolean useDefaultNamespace) throws SQLException, NamingException {
		return getJndiDataSource(jndiName, useDefaultNamespace).getConnection();
	}

}
