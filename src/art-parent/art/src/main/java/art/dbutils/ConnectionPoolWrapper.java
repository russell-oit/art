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

import art.datasource.DatasourceInfo;
import art.dbcp.ArtDBCPDataSource;
import art.enums.ConnectionPoolLibrary;
import art.utils.ArtUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
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

	public ConnectionPoolWrapper(DatasourceInfo datasourceInfo, int maxPoolSize,
			ConnectionPoolLibrary library) throws NamingException {

		Objects.requireNonNull(datasourceInfo, "datasourceInfo must not be null");

		createConnectionPool(datasourceInfo, maxPoolSize, library);
	}

	private void createConnectionPool(DatasourceInfo datasourceInfo, int maxPoolSize,
			ConnectionPoolLibrary connectionPoolLibrary) throws NamingException {

		if (datasourceInfo.isJndi()) {
			//for jndi datasources, the url contains the jndi name/resource reference
			pool = ArtUtils.getJndiDataSource(datasourceInfo.getUrl());
		} else if (connectionPoolLibrary == ConnectionPoolLibrary.HikariCP) {
			pool = createHikariCPConnectionPool(datasourceInfo, maxPoolSize);
		} else {
			pool = createArtDBCPConnectionPool(datasourceInfo, maxPoolSize);
		}

		poolName = datasourceInfo.getName();
		poolId = datasourceInfo.getDatasourceId();
	}

	private DataSource createArtDBCPConnectionPool(DatasourceInfo datasourceInfo, int maxPoolSize) {
		long timeoutSeconds = TimeUnit.MINUTES.toSeconds(datasourceInfo.getConnectionPoolTimeout());  //convert timeout mins to seconds
		ArtDBCPDataSource newPool = new ArtDBCPDataSource(timeoutSeconds);

		newPool.setPoolName(datasourceInfo.getName()); //use the datasoure name as the connection pool name
		newPool.setUsername(datasourceInfo.getUsername());
		newPool.setPassword(datasourceInfo.getPassword());
		newPool.setMaxPoolSize(maxPoolSize);
		newPool.setUrl(datasourceInfo.getUrl());
		newPool.setDriverClassName(datasourceInfo.getDriver());
		newPool.setTestSql(datasourceInfo.getTestSql());

		//set application name connection property
		newPool.setConnectionProperties(getAppNameProperty(datasourceInfo.getUrl(), datasourceInfo.getName()));

		//register driver so that connections are immediately usable
		registerDriver(datasourceInfo.getDriver());
		
		return newPool;
	}

	private DataSource createHikariCPConnectionPool(DatasourceInfo datasourceInfo, int maxPoolSize) {
		HikariConfig config = new HikariConfig();

		config.setPoolName(datasourceInfo.getName());
		config.setUsername(datasourceInfo.getUsername());
		config.setPassword(datasourceInfo.getPassword());
		//explicitly set minimum idle connection count to a low value to avoid
		//too many connection errors where you have multiple report datasources using the same server
		config.setMinimumIdle(1);
		config.setMaximumPoolSize(maxPoolSize);
		config.setJdbcUrl(datasourceInfo.getUrl());
		config.setDriverClassName(datasourceInfo.getDriver()); //registers/loads the driver

		//Either jdbc4ConnectionTest must be enabled or a connectionTestQuery must be specified
		//othwerise error will be thrown when valid connection check is done
		//(hikaricp does this check every time a connection is requested)
		if (StringUtils.isBlank(datasourceInfo.getTestSql())
				|| StringUtils.equals(datasourceInfo.getTestSql(), "isValid")) {
			config.setJdbc4ConnectionTest(true);
		} else {
			config.setJdbc4ConnectionTest(false);
			config.setConnectionTestQuery(datasourceInfo.getTestSql());
		}

		long timeoutMillis = TimeUnit.MINUTES.toMillis(datasourceInfo.getConnectionPoolTimeout());
		config.setIdleTimeout(timeoutMillis);

		//set application name connection property
		config.setDataSourceProperties(getAppNameProperty(datasourceInfo.getUrl(), datasourceInfo.getName()));

		return new HikariDataSource(config);
	}

	private void registerDriver(String driver) {
		logger.debug("Entering registerDriver: driver='{}'", driver);

		try {
			//newInstance only needed for buggy drivers
			//https://stackoverflow.com/questions/2092659/what-is-difference-between-class-forname-and-class-forname-newinstance/2093236#2093236
//			Class.forName(driver).newInstance();
			Class.forName(driver);
			logger.debug("JDBC driver registered: {}", driver);
		} catch (ClassNotFoundException ex) {
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

	public Integer getCurrentPoolSize() {
		Integer currentPoolSize = null; //use Integer rather than int for "undefined" status, where library doesn't support the property

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

	public Connection getConnection() throws SQLException {
		return pool.getConnection();
	}
}
