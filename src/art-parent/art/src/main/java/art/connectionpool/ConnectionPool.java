/*
 * Copyright (C) 2015 Enrico Liboni <eliboni@users.sourceforge.net>
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

import art.datasource.DatasourceInfo;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class that represents a connection pool
 *
 * @author Timothy Anyona
 */
public abstract class ConnectionPool {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

	private int poolId;
	private String name;
	protected DataSource pool;
	private int maxSize;

	/**
	 * @return the poolId
	 */
	public int getPoolId() {
		return poolId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the pool
	 */
	public DataSource getPool() {
		return pool;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void create(DatasourceInfo datasourceInfo, int maxSize) {
		Objects.requireNonNull(datasourceInfo, "datasourceInfo must not be null");

		pool = createPool(datasourceInfo, maxSize);
		name = datasourceInfo.getName();
		poolId = datasourceInfo.getDatasourceId();
		this.maxSize = maxSize;
	}

	protected abstract DataSource createPool(DatasourceInfo datasourceInfo, int maxPoolSize);

	public void close() {
		logger.debug("Entering close");
		
		closePool();
		pool = null;
	}

	protected void closePool() {

	}

	public void refresh() {

	}

	public Integer getCurrentSize() {
		return null; //use Integer rather than int for "undefined" status, where library doesn't support the property
	}

	public Integer getInUseCount() {
		return null;
	}

	public Integer getHighestReachedSize() {
		return null;
	}

	public Long getTotalConnectionRequests() {
		return null;
	}

	public Connection getConnection() throws SQLException {
		return pool.getConnection();
	}

	public ConnectionPoolDetails getPoolDetails() {
		logger.debug("Entering getPoolDetails");

		ConnectionPoolDetails details = new ConnectionPoolDetails();
		details.setPoolId(poolId);
		details.setName(name);
		details.setMaxPoolSize(maxSize);

		details.setHighestReachedPoolSize(getHighestReachedSize());
		details.setCurrentPoolSize(getCurrentSize());
		details.setInUseCount(getInUseCount());
		details.setTotalConnectionRequests(getTotalConnectionRequests());

		return details;
	}

	/**
	 * Get application name connection property to identify ART connections
	 *
	 * @param dbUrl
	 * @param poolName
	 *
	 * @param pool
	 */
	protected Properties getAppNameProperty(String dbUrl, String poolName) {
		logger.debug("Entering getAppNameProperty: dbUrl='{}', poolName='{}'", dbUrl, poolName);
		
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

}
