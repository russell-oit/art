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
import art.datasource.DatasourceInfo;
import art.datasource.DatasourceMapper;
import art.enums.ConnectionPoolLibrary;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

	private static Map<Integer, ConnectionPoolWrapper> connectionPoolMap;

	/**
	 * Create connections to all report databases. Connections are pooled
	 *
	 * @param artDbConfig
	 * @throws NamingException
	 * @throws SQLException
	 */
	public static void createConnectionPools(ArtDatabase artDbConfig) throws NamingException, SQLException {
		Objects.requireNonNull(artDbConfig, "artDbConfig must not be null");

		//reset pools map
		closeAllConnections();
		connectionPoolMap = new HashMap<>();

		ConnectionPoolLibrary connectionPoolLibrary = artDbConfig.getConnectionPoolLibrary();
		int maxPoolSize = artDbConfig.getMaxPoolConnections(); //will apply to all connection pools

		//create connection pool for the art database
		createConnectionPool(artDbConfig, maxPoolSize, connectionPoolLibrary);

		//create connection pools for report datasources
		//use QueryRunner directly instead of DbService to avoid circular references
		String sql = "SELECT *"
				+ " FROM ART_DATABASES"
				+ " WHERE ACTIVE=1";

		QueryRunner run = new QueryRunner(getArtDbConnectionPool());
		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		List<Datasource> datasources = run.query(sql, h);

		for (Datasource datasource : datasources) {
			//if creating connections for some datasource fails, continue creating for others
			try {
				createConnectionPool(datasource, maxPoolSize, connectionPoolLibrary);
			} catch (NamingException ex) {
				logger.error("Error creating connection pool. Datasource='{}'", ex, datasource.getName());
			}
		}
	}

	private static void createConnectionPool(DatasourceInfo datasourceInfo, int maxPoolSize,
			ConnectionPoolLibrary library) throws NamingException {

		ConnectionPoolWrapper wrapper = new ConnectionPoolWrapper(datasourceInfo, maxPoolSize, library);
		connectionPoolMap.put(wrapper.getPoolId(), wrapper);
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
	private static DataSource getConnectionPool(int datasourceId) throws SQLException {
		if (connectionPoolMap == null) {
			throw new IllegalStateException("connectionPoolMap is null");
		}

		ConnectionPoolWrapper wrapper = connectionPoolMap.get(Integer.valueOf(datasourceId));
		if (wrapper == null) {
			throw new SQLException("Connection pool doesn't exist for datasource id " + datasourceId);
		} else {
			return wrapper.getPool();
		}

	}

	/**
	 * Get a connection to ART database from the pool (same as getConnection(0))
	 *
	 * @return connection to the ART database or null if connection doesn't
	 * exist
	 * @throws java.sql.SQLException
	 */
	public static DataSource getArtDbConnectionPool() throws SQLException {
		return getConnectionPool(ArtDatabase.ART_DATABASE_DATASOURCE_ID);
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
		return getConnectionPool(datasourceId).getConnection();

	}

	/**
	 * Get a connection to ART database from the pool (same as getConnection(0))
	 *
	 * @return connection to the ART database or null if connection doesn't
	 * exist
	 * @throws java.sql.SQLException
	 */
	public static Connection getArtDbConnection() throws SQLException {
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
		Connection conn = null;

		for (Entry<Integer, ConnectionPoolWrapper> entry : connectionPoolMap.entrySet()) {
			ConnectionPoolWrapper wrapper = entry.getValue();
			if (StringUtils.equalsIgnoreCase(wrapper.getPoolName(), datasourceName)) {
				conn = wrapper.getPool().getConnection();
			}
		}

		return conn;
	}

	/**
	 * Close all connection pools (really close all connections in all the
	 * connection pools) and clear and nullify the connection pool map
	 */
	public static void closeAllConnections() {
		if (connectionPoolMap == null) {
			return;
		}

		for (Entry<Integer, ConnectionPoolWrapper> entry : connectionPoolMap.entrySet()) {
			ConnectionPoolWrapper wrapper = entry.getValue();
			wrapper.closePool();
		}

		connectionPoolMap.clear();
		connectionPoolMap = null;
	}

	/**
	 * Get connection located by the given jndi name
	 *
	 * @param jndiName
	 * @param useDefaultJndiNamespace
	 * @return
	 * @throws SQLException
	 * @throws javax.naming.NamingException
	 */
	public static Connection getJndiConnection(String jndiName, boolean useDefaultJndiNamespace) throws SQLException, NamingException {
		return ConnectionPoolWrapper.getJndiConnection(jndiName, useDefaultJndiNamespace);
	}

}
