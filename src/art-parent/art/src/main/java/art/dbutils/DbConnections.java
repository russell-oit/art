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

import art.artdatabase.ArtDatabase;
import art.datasource.Datasource;
import art.datasource.DatasourceMapper;
import art.dbcp.ArtDBCPDataSource;
import art.enums.ConnectionPoolLibrary;
import art.settings.Settings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class DbConnections {

	private static final Logger logger = LoggerFactory.getLogger(DbConnections.class);

	private static Map<Integer, ConnectionPoolWrapper> connectionPoolMap;

	public static void createConnectionPools(Settings artSettings, ArtDatabase artDbConfig) throws NamingException, SQLException {
		Objects.requireNonNull(artSettings, "artSettings must not be null");
		Objects.requireNonNull(artDbConfig, "artDbConfig must not be null");

		ConnectionPoolLibrary connectionPoolLibrary = artSettings.getConnectionPoolLibrary();

		//reset pools map
		closeAllConnections();
		connectionPoolMap = new HashMap<>();

		int maxPoolSize = artDbConfig.getMaxPoolConnections(); //will apply to all connection pools

		//create art database connection pool
		Datasource ds = new Datasource();
		ds.setName("ART Database");
		ds.setDatasourceId(0); //custom id for the art database
		ds.setJndi(artDbConfig.isJndi());
		ds.setUrl(artDbConfig.getUrl());
		ds.setDriver(artDbConfig.getDriver());
		ds.setConnectionPoolTimeout(artDbConfig.getConnectionPoolTimeout());
		ds.setUsername(artDbConfig.getUsername());
		ds.setPassword(artDbConfig.getPassword());
		ds.setTestSql(artDbConfig.getTestSql());

		createConnectionPool(ds, maxPoolSize, connectionPoolLibrary);

		//create connection pools for report datasources
		Connection conn = null;

		try {
			//don't use DbService to avoid circular references
			conn = getArtDbConnection();

			String sql = "SELECT *"
					+ " FROM ART_DATABASES"
					+ " WHERE ACTIVE=1";

			ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());

			QueryRunner run = new QueryRunner();
			List<Datasource> datasources = run.query(conn, sql, h);
			for (Datasource datasource : datasources) {
				createConnectionPool(datasource, maxPoolSize, connectionPoolLibrary);
			}
		} finally {
			DbUtils.close(conn);
		}
	}

	private static DataSource createArtDBCPConnectionPool(Datasource ds, int maxPoolSize) {
		long timeoutSeconds = ds.getConnectionPoolTimeout() * 60L;  //convert timeout mins to seconds
		ArtDBCPDataSource pool = new ArtDBCPDataSource(timeoutSeconds);

		pool.setPoolName(ds.getName()); //use the datasoure name as the connection pool name
		pool.setUsername(ds.getUsername());
		pool.setPassword(ds.getPassword());
		pool.setMaxPoolSize(maxPoolSize);
		pool.setUrl(ds.getUrl());
		pool.setDriverClassName(ds.getDriver());
		pool.setTestSql(ds.getTestSql());

		//set application name connection property
		pool.setConnectionProperties(getAppNameProperty(ds.getUrl(), ds.getName()));

		return pool;
	}

	private static DataSource createHikariCPConnectionPool(Datasource ds, int maxPoolSize) {
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

	private static void createConnectionPool(Datasource ds, int maxPoolSize,
			ConnectionPoolLibrary connectionPoolLibrary) throws NamingException {

		DataSource pool;

		if (ds.isJndi()) {
			//for jndi datasources, the url contains the jndi name/resource reference
			pool = getJndiDataSource(ds.getUrl());
		} else if (connectionPoolLibrary == ConnectionPoolLibrary.HikariCP) {
			pool = createHikariCPConnectionPool(ds, maxPoolSize);
			//hikaricp registers drivers when setdriverclassname is called
		} else {
			//use art-dbcp
			pool = createArtDBCPConnectionPool(ds, maxPoolSize);
			registerDriver(ds.getDriver());
		}

		ConnectionPoolWrapper wrapper = new ConnectionPoolWrapper(pool);
		wrapper.setPoolName(ds.getName());
		wrapper.setPoolId(ds.getDatasourceId());

		//add art database to the connection pool map. 
		connectionPoolMap.put(wrapper.getPoolId(), wrapper);
	}

	private static void registerDriver(String driver) {
		try {
			Class.forName(driver).newInstance();
			logger.info("JDBC driver registered: {}", driver);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			logger.error("Error while registering JDBC driver: '{}'", driver, ex);
		}
	}

	/**
	 * Get application name connection property to identify ART connections
	 *
	 * @param pool
	 */
	private static Properties getAppNameProperty(String dbUrl, String poolName) {
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
	 * Get a database connection from the connection pool for the datasource
	 * with the given ID
	 *
	 * @param datasourceId datasource id. 0 = ART database.
	 * @return database connection for the given datasource
	 * @throws java.sql.SQLException if connection doesn't exist or there was a
	 * database error
	 */
	public static Connection getConnection(int datasourceId) throws SQLException {
		//some connection pool libraries don't provide a pool name property
		//so for maximum flexibility don't provide access using datasource name
		//or implement another map with n

		if (connectionPoolMap == null) {
			throw new IllegalStateException("connectionPoolMap is null");
		}

		ConnectionPoolWrapper wrapper = connectionPoolMap.get(Integer.valueOf(datasourceId));
		if (wrapper == null) {
			throw new SQLException("Connection pool doesn't exist for datasource id " + datasourceId);
		} else {
			DataSource pool = wrapper.getPool();
			return pool.getConnection();
		}

	}

	/**
	 * Get a connection to ART database from the pool (same as getConnection(0))
	 *
	 * @return connection to the ART database or null if connection doesn't
	 * exist
	 * @throws java.sql.SQLException
	 */
	public static Connection getArtDbConnection() throws SQLException {
		return getConnection(0); // i=0 => ART database
	}

	/**
	 * Get a database connection from the connection pool for the datasource
	 * with the given nme
	 *
	 * @param datasourceName datasource name
	 * @return database connection for the given datasource
	 * @throws java.sql.SQLException if connection doesn't exist or there was a
	 * database error
	 */
	public static Connection getConnection(String datasourceName) throws SQLException {
		Connection conn = null;

		for (Entry<Integer, ConnectionPoolWrapper> entry : connectionPoolMap.entrySet()) {
			ConnectionPoolWrapper wrapper = entry.getValue();
			if (StringUtils.equalsIgnoreCase(wrapper.getPoolName(), datasourceName)) {
				conn = wrapper.getPool().getConnection();
			}
		}

		return conn;
	}

	private static DataSource getJndiDataSource(String jndiName) throws NamingException {
		logger.debug("Entering getConnectionFromJndi");

		//throw exception if jndi url is null, rather than returning a null connection, which would be useless
		Objects.requireNonNull(jndiName, "jndiName must not be null");

		//first time we are getting a connection from jndi. get the jndi datasource object
		InitialContext ic = new InitialContext();
		logger.debug("jndiName='{}'", jndiName);
		String finalJndiName;
		if (jndiName.startsWith("java:")) {
			//full jndi name provided. use as is
			finalJndiName = jndiName;
		} else {
			//relative name provided. add default jndi prefix
			finalJndiName = "java:comp/env/" + jndiName;
		}
		logger.debug("finalJndiName='{}'", finalJndiName);
		return (DataSource) ic.lookup(finalJndiName);
	}

	/**
	 * Close all connection pools (really close all connections in all the
	 * connection pools) and clear and nullify the connection pool map
	 */
	public static void closeAllConnections() {
		if (connectionPoolMap != null) {
			for (Entry<Integer, ConnectionPoolWrapper> entry : connectionPoolMap.entrySet()) {
				ConnectionPoolWrapper wrapper = entry.getValue();
				DataSource ds = wrapper.getPool();
				if (ds instanceof ArtDBCPDataSource) {
					ArtDBCPDataSource pool = (ArtDBCPDataSource) ds;
					pool.close();
				} else if (ds instanceof HikariDataSource) {
					HikariDataSource pool = (HikariDataSource) ds;
					pool.close();
				}
			}
			connectionPoolMap.clear();
			connectionPoolMap = null;
		}
	}

}
