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
import art.dbcp.ArtDBCPDataSource;
import art.enums.ConnectionPoolLibrary;
import art.settings.Settings;
import art.utils.Encrypter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class DbConnections {

	private static final Logger logger = LoggerFactory.getLogger(DbConnections.class);

	private static Map<Integer, DataSource> connectionPoolMap; //use a LinkedHashMap that should store items sorted as per the order the items are inserted in the map...

	private static void createConnectionPools(Settings artSettings, ArtDatabase artDbConfig) {
		Objects.requireNonNull(artSettings, "artSettings must not be null");

		ConnectionPoolLibrary connectionPoolLibrary = artSettings.getConnectionPoolLibrary();

		if (connectionPoolLibrary == ConnectionPoolLibrary.HikariCP) {

		} else {
			createArtDBCPConnectionPools(artDbConfig);
		}
	}

	private static void createHikariCPConnectionPools() {
		HikariConfig config = new HikariConfig();
		config.setMaximumPoolSize(100);
		config.setJdbcUrl(null);
		config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
		config.addDataSourceProperty("serverName", "localhost");
		config.addDataSourceProperty("port", "3306");
		config.addDataSourceProperty("databaseName", "mydb");
		config.addDataSourceProperty("user", "bart");
		config.addDataSourceProperty("password", "51mp50n");

		HikariDataSource ds = new HikariDataSource(config);
	}

	private static void createArtDBCPConnectionPools(ArtDatabase artDbConfig) {

		if (artDbConfig == null) {
			return;
		}

		//create art database connection pool
		String artDbDriver = artDbConfig.getDriver();
		int artDbPoolTimeoutInMins = artDbConfig.getConnectionPoolTimeout();
		long artDbPoolTimeoutInSeconds = artDbPoolTimeoutInMins * 60L;

		int maxPoolSize = artDbConfig.getMaxPoolConnections(); //will apply to all connection pools

		ArtDBCPDataSource artDbPool = new ArtDBCPDataSource(artDbPoolTimeoutInSeconds);
		artDbPool.setPoolName("ART Database");
		artDbPool.setJndi(artDbConfig.isJndi());
		artDbPool.setUrl(artDbConfig.getUrl()); //for jndi datasources, the url contains the jndi name/resource reference
		artDbPool.setUsername(artDbConfig.getUsername());
		artDbPool.setPassword(artDbConfig.getPassword());
		artDbPool.setMaxPoolSize(maxPoolSize);
		artDbPool.setDriverClassName(artDbDriver);
		artDbPool.setTestSql(artDbConfig.getTestSql());

		//set application name connection property
		setConnectionProperties(artDbPool);

		//populate pools map
		connectionPoolMap = null;
		connectionPoolMap = new HashMap<>();

		//add art repository database to the connection pool map. 
		//"id" = 0. it's not explicitly defined in the admin console
		connectionPoolMap.put(0, artDbPool);

		//register art database driver. must do this before getting details of other datasources
		if (StringUtils.isNotBlank(artDbDriver)) {
			try {
				Class.forName(artDbDriver).newInstance();
				logger.info("ART Database JDBC driver registered: {}", artDbDriver);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
				logger.error("Error while registering ART Database JDBC driver: {}", artDbDriver, ex);
			}
		}

		//create connection pools for report datasources
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		//save driver names for later registration
		//only register a driver once. several datasources may use the same driver
		//use a set (doesn't add duplicate items)
		Set<String> datasourceDrivers = new HashSet<>();

		try {
			conn = artDbPool.getConnection();

			String sql = "SELECT *"
					+ " FROM ART_DATABASES"
					+ " WHERE ACTIVE=1";
			ps = conn.prepareStatement(sql);

			rs = ps.executeQuery();
			while (rs.next()) {
				int timeoutInMins = rs.getInt("POOL_TIMEOUT");
				long timeoutInSeconds = timeoutInMins * 60L;

				ArtDBCPDataSource pool = new ArtDBCPDataSource(timeoutInSeconds);
				pool.setPoolName(rs.getString("NAME")); //use the datasoure name as the connection pool name
				pool.setJndi(rs.getBoolean("JNDI"));
				pool.setUrl(rs.getString("URL"));
				pool.setUsername(rs.getString("USERNAME"));
				String password = rs.getString("PASSWORD");
				// decrypt password if stored encrypted
				if (password.startsWith("o:")) {
					password = Encrypter.decrypt(password.substring(2));
				}
				pool.setPassword(password);
				pool.setTestSql(rs.getString("TEST_SQL"));
				pool.setMaxPoolSize(maxPoolSize);
				String driver = rs.getString("DRIVER");
				pool.setDriverClassName(driver);

				//set application name connection property
				setConnectionProperties(pool);

				connectionPoolMap.put(rs.getInt("DATABASE_ID"), pool);

				//save driver name for later registration
				if (!StringUtils.equals(driver, artDbDriver)) {
					//art database driver is already registered
					datasourceDrivers.add(driver);
				}
			}
		} catch (SQLException e) {
			logger.error("Error", e);
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		registerDrivers(datasourceDrivers);
	}

	private static void registerDrivers(Set<String> drivers) {
		//register jdbc drivers for datasources in the map
		//only register a driver once. several datasources may use the same driver
		//use a set (doesn't add duplicate items)

		Objects.requireNonNull(drivers, "drivers must not be null");

		for (String driver : drivers) {
			if (StringUtils.isNotBlank(driver)) {
				//blank is valid. for jndi datasources
				try {
					Class.forName(driver).newInstance();
					logger.info("Datasource JDBC driver registered: {}", driver);
				} catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
					logger.error("Error while registering Datasource JDBC driver: {}", driver, ex);
				}
			}
		}
	}

	/**
	 * Set application name connection property to identify ART connections
	 *
	 * @param pool
	 */
	private static void setConnectionProperties(ArtDBCPDataSource pool) {
		//ApplicationName property
		//see http://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html#setClientInfo%28java.lang.String,%20java.lang.String%29
		//has different name and maxlength for different drivers
		//maxlength mostly in the 254 range. Some exceptions include postgresql maxlength=64
		//some drivers don't seem to define it explicitly so may not support it and throw exception?
		//e.g. mysql, hsqldb

		Properties properties = new Properties();

		String connectionName = "ART - " + pool.getPoolName();
		String dbUrl = pool.getUrl();

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

		//some drivers don't seem to define
		pool.setConnectionProperties(properties);
	}

	/**
	 * Get a database connection from the connection pool for the datasource
	 * with the given ID from the connection
	 *
	 * @param datasourceId datasource id. 0 = ART database.
	 * @return connection to datasource
	 * @throws java.sql.SQLException if connection doesn't exist or there was a
	 * database error
	 */
	public static Connection getConnection(int datasourceId) throws SQLException {
		if (connectionPoolMap == null) {
			throw new IllegalStateException("connectionPoolMap is null");
		}

		DataSource pool = connectionPoolMap.get(Integer.valueOf(datasourceId));
		if (pool == null) {
			throw new SQLException("Connection pool doesn't exist for datasource id " + datasourceId);
		} else {
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
		return getConnection(0); // i=0 => ART Repository
	}

	/**
	 * Get a datasource connection based on the datasource name.
	 *
	 * @param name datasource name
	 * @return connection to the datasource or null if connection doesn't exist
	 * @throws java.sql.SQLException
	 */
	public static Connection getConnection(String name) throws SQLException {
		if (connectionPoolMap == null) {
			throw new IllegalStateException("connectionPoolMap is null");
		}

		Connection conn = null;
		for (DataSource pool : connectionPoolMap.values()) {
			if (pool != null) {
				if (pool instanceof ArtDBCPDataSource) {
					ArtDBCPDataSource artDbcpPool = (ArtDBCPDataSource) pool;
					if (StringUtils.equalsIgnoreCase(name, artDbcpPool.getPoolName())) {
						//this is the required datasource. get connection and exit loop
						conn = pool.getConnection();
						break;
					}
				} else if (pool instanceof HikariDataSource) {
					HikariDataSource hikariPool = (HikariDataSource) pool;
					if (StringUtils.equalsIgnoreCase(name, hikariPool.getPoolName())) {
						//this is the required datasource. get connection and exit loop
						conn = pool.getConnection();
						break;
					}
				}
			}
		}

		return conn;
	}

}
