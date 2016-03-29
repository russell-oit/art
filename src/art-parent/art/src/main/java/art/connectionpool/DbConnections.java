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
package art.connectionpool;

import art.artdatabase.ArtDatabase;
import art.datasource.Datasource;
import art.datasource.DatasourceInfo;
import art.datasource.DatasourceMapper;
import art.enums.ConnectionPoolLibrary;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.naming.NamingException;
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

	/**
	 * Create connections to all report databases. Connections are pooled
	 *
	 * @param artDbConfig
	 * @throws NamingException
	 * @throws SQLException
	 */
	public static void createConnectionPools(ArtDatabase artDbConfig) throws SQLException {
		logger.debug("Entering createConnectionPools");
		
		Objects.requireNonNull(artDbConfig, "artDbConfig must not be null");

		//reset pools map
		closeAllConnections();
		connectionPoolMap = new HashMap<>();

		ConnectionPoolLibrary connectionPoolLibrary = artDbConfig.getConnectionPoolLibrary();
		int maxPoolSize = artDbConfig.getMaxPoolConnections(); //will apply to all connection pools
		
		logger.debug("connectionPoolLibrary={}", connectionPoolLibrary);
		logger.debug("maxPoolSize={}", maxPoolSize);

		//create connection pool for the art database
		createConnectionPool(artDbConfig, maxPoolSize, connectionPoolLibrary);

		//create connection pools for report datasources
		//use QueryRunner directly instead of DbService or DatasourceService to avoid circular references
		String sql = "SELECT *"
				+ " FROM ART_DATABASES"
				+ " WHERE ACTIVE=1";

		QueryRunner run = new QueryRunner(getArtDbConnectionPool());
		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		List<Datasource> datasources = run.query(sql, h);

		for (Datasource datasource : datasources) {
			createConnectionPool(datasource, maxPoolSize, connectionPoolLibrary);
		}
	}

	public static void createConnectionPool(DatasourceInfo datasourceInfo, int maxPoolSize,
			ConnectionPoolLibrary connectionPoolLibrary) {
		
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
			throw new IllegalArgumentException("Unexpected connection pool library " + connectionPoolLibrary);
		}

		pool.create(datasourceInfo, maxPoolSize);
		connectionPoolMap.put(pool.getPoolId(), pool);
	}

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

	private static DataSource getDataSource(int datasourceId) {
		logger.debug("Entering getDataSource: datasourceId={}", datasourceId);
		
		return getConnectionPool(datasourceId).getPool();
	}

	/**
	 * Get a connection to ART database from the pool (same as getConnection(0))
	 *
	 * @return connection to the ART database or null if connection doesn't
	 * exist
	 */
	public static DataSource getArtDbConnectionPool() {
		logger.debug("Entering getArtDbConnectionPool");
		
		return getDataSource(ArtDatabase.ART_DATABASE_DATASOURCE_ID);
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
		logger.debug("Entering getConnection: datasourceId={}", datasourceId);
		
		return getDataSource(datasourceId).getConnection();

	}

	/**
	 * Get a connection to ART database from the pool (same as getConnection(0))
	 *
	 * @return connection to the ART database or null if connection doesn't
	 * exist
	 * @throws java.sql.SQLException
	 */
	public static Connection getArtDbConnection() throws SQLException {
		logger.debug("Entering getArtDbConnection");
		
		return getConnection(ArtDatabase.ART_DATABASE_DATASOURCE_ID);
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
	 * Close all connection pools (really close all connections in all the
	 * connection pools) and clear and nullify the connection pool map
	 */
	public static void closeAllConnections() {
		logger.debug("Entering closeAllConnections");
		
		if (connectionPoolMap == null) {
			return;
		}

		for (ConnectionPool pool : connectionPoolMap.values()) {
			pool.close();
		}

		connectionPoolMap.clear();
		connectionPoolMap = null;
	}

	public static List<ConnectionPoolDetails> getAllConnectionPoolDetails() {
		logger.debug("Entering getAllConnectionPoolDetails");
		
		List<ConnectionPoolDetails> details = new ArrayList<>(connectionPoolMap.size());

		for (ConnectionPool pool : connectionPoolMap.values()) {
			details.add(pool.getPoolDetails());
		}

		return details;
	}

	public static ConnectionPoolDetails getConnectionPoolDetails(int datasourceId) {
		logger.debug("Entering getConnectionPoolDetails: datasourceId={}", datasourceId);
		
		return getConnectionPool(datasourceId).getPoolDetails();
	}

	public static void refreshConnectionPool(int datasourceId) {
		logger.debug("Entering refreshConnectionPool: datasourceId={}", datasourceId);
		
		getConnectionPool(datasourceId).refresh();
	}

	public static void removeConnectionPool(int datasourceId) {
		logger.debug("Entering removeConnectionPool: datasourceId={}", datasourceId);
		
		ConnectionPool pool = connectionPoolMap.get(datasourceId);
		if (pool != null) {
			pool.closePool();
			connectionPoolMap.remove(datasourceId);
		}
	}

}
