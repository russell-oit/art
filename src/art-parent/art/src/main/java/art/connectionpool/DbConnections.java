/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.connectionpool;

import art.artdatabase.ArtDatabase;
import art.datasource.Datasource;
import art.datasource.DatasourceInfo;
import art.datasource.DatasourceMapper;
import art.enums.ConnectionPoolLibrary;
import art.enums.DatasourceType;
import art.utils.MongoHelper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides connections to databases. All connections are held and managed in
 * connection pools.
 *
 * @author Timothy Anyona
 */
public class DbConnections {

	private static final Logger logger = LoggerFactory.getLogger(DbConnections.class);

	private static Map<Integer, ConnectionPool> connectionPoolMap;
	private static Map<Integer, MongoClient> mongodbConnections;
	private static final int ART_DATABASE2_DATASOURCE_ID = -2;

	/**
	 * Creates connections to all report databases. Connections are pooled.
	 *
	 * @param artDbConfig
	 * @throws SQLException
	 */
	public static void createConnectionPools(ArtDatabase artDbConfig) throws SQLException {
		logger.debug("Entering createConnectionPools");

		Objects.requireNonNull(artDbConfig, "artDbConfig must not be null");

		//reset pools map
		closeAllConnections();
		connectionPoolMap = new HashMap<>();
		mongodbConnections = new HashMap<>();

		ConnectionPoolLibrary connectionPoolLibrary = artDbConfig.getConnectionPoolLibrary();
		int maxPoolSize = artDbConfig.getMaxPoolConnections(); //will apply to all connection pools

		logger.debug("connectionPoolLibrary={}", connectionPoolLibrary);
		logger.debug("maxPoolSize={}", maxPoolSize);

		//create connection pool for the art database
		createConnectionPool(artDbConfig, maxPoolSize, connectionPoolLibrary);

		//create second art database connection pool - for use with autoCommit(false) connections
		artDbConfig.setName("ART Database 2");
		artDbConfig.setDatasourceId(ART_DATABASE2_DATASOURCE_ID);
		createConnectionPool(artDbConfig, maxPoolSize, connectionPoolLibrary);

		//create connection pools for report databases
		createJdbcDatasourceConnectionPools(maxPoolSize, connectionPoolLibrary);
		createMongodbDatasourceConnectionPools();
	}

	/**
	 * Create connection pools for jdbc datasources
	 *
	 * @param maxPoolSize max connection pool size
	 * @param connectionPoolLibrary connection pool library to use
	 * @throws SQLException
	 */
	private static void createJdbcDatasourceConnectionPools(int maxPoolSize,
			ConnectionPoolLibrary connectionPoolLibrary) throws SQLException {

		logger.debug("Entering createJdbcDatasourceConnectionPools");

		//create connection pools for report datasources
		//use QueryRunner directly instead of DbService or DatasourceService to avoid circular references
		String sql = "SELECT *"
				+ " FROM ART_DATABASES"
				+ " WHERE ACTIVE=1 AND DATASOURCE_TYPE=?";

		QueryRunner run = new QueryRunner(getArtDbDataSource());
		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		List<Datasource> datasources = run.query(sql, h, DatasourceType.JDBC.getValue());

		for (Datasource datasource : datasources) {
			try {
				createConnectionPool(datasource, maxPoolSize, connectionPoolLibrary);
			} catch (RuntimeException ex) {
				//include runtime exception in case of PoolInitializationException when using hikaricp
				logger.error("Error", ex);
			}
		}
	}

	/**
	 * Creates connection pools for mongodb datasources
	 *
	 * @throws SQLException
	 */
	private static void createMongodbDatasourceConnectionPools() throws SQLException {
		logger.debug("Entering createMongodbDatasourceConnectionPools");

		//create connection pools for report datasources
		//use QueryRunner directly instead of DbService or DatasourceService to avoid circular references
		String sql = "SELECT *"
				+ " FROM ART_DATABASES"
				+ " WHERE ACTIVE=1 AND DATASOURCE_TYPE=?";

		QueryRunner run = new QueryRunner(getArtDbDataSource());
		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		List<Datasource> datasources = run.query(sql, h, DatasourceType.MongoDB.getValue());

		for (Datasource datasource : datasources) {
			createMongodbConnectionPool(datasource);
		}
	}

	/**
	 * Creates a connection pool for the given jdbc datasource
	 *
	 * @param datasourceInfo the datasource details
	 * @param maxPoolSize the maximum pool size
	 * @param connectionPoolLibrary the connection pool library
	 * @param autoCommit the auto commit setting for the connection
	 */
	public static void createConnectionPool(DatasourceInfo datasourceInfo,
			int maxPoolSize, ConnectionPoolLibrary connectionPoolLibrary) {

		logger.debug("Entering createConnectionPool");

		//remove any existing connection pool for this datasource
		removeConnectionPool(datasourceInfo.getDatasourceId());

		logger.debug("datasourceInfo.isJndi()={}", datasourceInfo.isJndi());

		ConnectionPool pool;
		if (datasourceInfo.isJndi()) {
			//for jndi datasources, the url contains the jndi name/resource reference
			pool = new JndiConnectionPool();
		} else if (connectionPoolLibrary == ConnectionPoolLibrary.HikariCP) {
			pool = new HikariCPConnectionPool();
		} else if (connectionPoolLibrary == ConnectionPoolLibrary.ArtDBCP) {
			pool = new ArtDBCPConnectionPool();
		} else {
			throw new IllegalArgumentException("Unexpected connection pool library: " + connectionPoolLibrary);
		}

		pool.create(datasourceInfo, maxPoolSize);
		DataSource dataSource = pool.getPool();
		if (dataSource != null) {
			//may be null if jndi connection and there was an error
			connectionPoolMap.put(pool.getPoolId(), pool);
		}
	}

	/**
	 * Creates a connection pool for a mongodb datasource
	 *
	 * @param datasource the mongodb datasource
	 */
	public static void createMongodbConnectionPool(Datasource datasource) {
		logger.debug("Entering createMongodbConnectionPool");

		removeConnectionPool(datasource.getDatasourceId());

		MongoHelper mongoHelper = new MongoHelper();
		String mongoUrl = mongoHelper.getUrlWithCredentials(datasource);
		MongoClientURI uri = new MongoClientURI(mongoUrl);
		MongoClient mongoClient = new MongoClient(uri);

		mongodbConnections.put(datasource.getDatasourceId(), mongoClient);
	}

	/**
	 * Returns a connection pool for the given datasource
	 *
	 * @param datasourceId the datasource id
	 * @return a connection pool for the given datasource
	 */
	private static ConnectionPool getConnectionPool(int datasourceId) {
		logger.debug("Entering getConnectionPool: datasourceId={}", datasourceId);

		if (connectionPoolMap == null) {
			throw new IllegalStateException("connectionPoolMap is null");
		}

		ConnectionPool pool = connectionPoolMap.get(datasourceId);
		if (pool == null) {
			throw new RuntimeException("Connection pool doesn't exist for datasource id " + datasourceId);
		} else {
			return pool;
		}
	}

	/**
	 * Returns a javax.sql.DataSource for the given art datasource
	 *
	 * @param datasourceId the art datasource id
	 * @return a javax.sql.DataSource for the given art datasource
	 */
	public static DataSource getDataSource(int datasourceId) {
		logger.debug("Entering getDataSource: datasourceId={}", datasourceId);

		return getConnectionPool(datasourceId).getPool();
	}

	/**
	 * Returns a javax.sql.DataSource for the ART database
	 *
	 * @return a javax.sql.DataSource for the ART database
	 */
	public static DataSource getArtDbDataSource() {
		logger.debug("Entering getArtDbDataSource");

		return getDataSource(ArtDatabase.ART_DATABASE_DATASOURCE_ID);
	}

	/**
	 * Returns a database connection from the connection pool for the datasource
	 * with the given id
	 *
	 * @param datasourceId the datasource id
	 * @return a database connection for the given datasource
	 * @throws java.sql.SQLException if there was a database error
	 */
	public static Connection getConnection(int datasourceId) throws SQLException {
		logger.debug("Entering getConnection: datasourceId={}", datasourceId);

		return getDataSource(datasourceId).getConnection();
	}

	/**
	 * Returns a connection to ART database
	 *
	 * @return a connection to the ART database
	 * @throws java.sql.SQLException
	 */
	public static Connection getArtDbConnection() throws SQLException {
		logger.debug("Entering getArtDbConnection");

		return getConnection(ArtDatabase.ART_DATABASE_DATASOURCE_ID);
	}

	/**
	 * Returns the second connection to ART database, with auto commit off
	 *
	 * @return the second connection to the ART database, with auto commit off
	 * @throws java.sql.SQLException
	 */
	public static Connection getArtDb2Connection() throws SQLException {
		logger.debug("Entering getArtDb2Connection");

		Connection conn = getConnection(ART_DATABASE2_DATASOURCE_ID);
		conn.setAutoCommit(false);
		return conn;
	}

	/**
	 * Returns a database connection from the connection pool for the datasource
	 * with the given name
	 *
	 * @param datasourceName the datasource name
	 * @return a database connection for the given datasource
	 * @throws java.sql.SQLException if there was a database error
	 */
	public static Connection getConnection(String datasourceName) throws SQLException {
		logger.debug("Entering getConnection: datasourceName='{}'", datasourceName);

		Connection conn = null;

		for (ConnectionPool pool : connectionPoolMap.values()) {
			if (StringUtils.equalsIgnoreCase(pool.getName(), datasourceName)) {
				conn = pool.getConnection();
			}
		}

		return conn;
	}

	/**
	 * Returns a mongodb connection for a datasource with the given id
	 *
	 * @param datasourceId the datasource id
	 * @return a mongodb connection for a datasource with the given id
	 */
	public static MongoClient getMongodbConnection(int datasourceId) {
		logger.debug("Entering getMongodbConnection: datasourceId={}", datasourceId);
		return mongodbConnections.get(datasourceId);
	}

	/**
	 * Closes all connection pools (really closes all connections in all the
	 * connection pools) and clears and nullifies the connection pool map
	 */
	public static void closeAllConnections() {
		logger.debug("Entering closeAllConnections");

		if (connectionPoolMap != null) {
			for (ConnectionPool pool : connectionPoolMap.values()) {
				pool.close();
			}
			connectionPoolMap.clear();
			connectionPoolMap = null;
		}

		if (mongodbConnections != null) {
			for (MongoClient mongoClient : mongodbConnections.values()) {
				mongoClient.close();
			}
			mongodbConnections.clear();
			mongodbConnections = null;
		}

	}

	/**
	 * Returns connection pool details for all datasources
	 *
	 * @return connection pool details for all datasources
	 */
	public static List<ConnectionPoolDetails> getAllConnectionPoolDetails() {
		logger.debug("Entering getAllConnectionPoolDetails");

		List<ConnectionPoolDetails> details = new ArrayList<>(connectionPoolMap.size());

		for (ConnectionPool pool : connectionPoolMap.values()) {
			details.add(pool.getPoolDetails());
		}

		return details;
	}

	/**
	 * Returns connection pool details for the given datasource
	 *
	 * @param datasourceId the datasource id
	 * @return connection pool details for the given datasource
	 */
	public static ConnectionPoolDetails getConnectionPoolDetails(int datasourceId) {
		logger.debug("Entering getConnectionPoolDetails: datasourceId={}", datasourceId);

		return getConnectionPool(datasourceId).getPoolDetails();
	}

	/**
	 * Refreshes the connection pool for the given datasource
	 *
	 * @param datasourceId the datasource id
	 */
	public static void refreshConnectionPool(int datasourceId) {
		logger.debug("Entering refreshConnectionPool: datasourceId={}", datasourceId);

		getConnectionPool(datasourceId).refresh();
	}

	/**
	 * Removes connections for the given datasource from the connection pool map
	 *
	 * @param datasourceId the datasource id
	 */
	public static void removeConnectionPool(int datasourceId) {
		logger.debug("Entering removeConnectionPool: datasourceId={}", datasourceId);

		ConnectionPool pool = connectionPoolMap.get(datasourceId);
		if (pool != null) {
			pool.closePool();
			connectionPoolMap.remove(datasourceId);
			return;
		}

		//datasource not among jdbc connection pool. try mongodb connection pool
		MongoClient mongoClient = mongodbConnections.get(datasourceId);
		if (mongoClient != null) {
			mongoClient.close();
			mongodbConnections.remove(datasourceId);
		}
	}
}
