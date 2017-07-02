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

import art.datasource.DatasourceInfo;
import art.dbcp.ArtDBCPDataSource;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a connection pool using the art dbcp library
 *
 * @author Timothy Anyona
 */
public class ArtDBCPConnectionPool extends ConnectionPool {

	private static final Logger logger = LoggerFactory.getLogger(ArtDBCPConnectionPool.class);

	private ArtDBCPDataSource artDbcpDataSource;

	@Override
	protected DataSource createPool(DatasourceInfo datasourceInfo, int maxPoolSize) {
		logger.debug("Entering createPool: maxPoolSize={}", maxPoolSize);

		long timeoutSeconds = TimeUnit.MINUTES.toSeconds(datasourceInfo.getConnectionPoolTimeoutMins());
		artDbcpDataSource = new ArtDBCPDataSource(timeoutSeconds);

		artDbcpDataSource.setPoolName(datasourceInfo.getName()); //use the datasoure name as the connection pool name
		artDbcpDataSource.setUsername(datasourceInfo.getUsername());
		artDbcpDataSource.setPassword(datasourceInfo.getPassword());
		artDbcpDataSource.setMaxPoolSize(maxPoolSize);
		artDbcpDataSource.setUrl(datasourceInfo.getUrl());
		artDbcpDataSource.setDriverClassName(datasourceInfo.getDriver());
		artDbcpDataSource.setTestSql(datasourceInfo.getTestSql());

		//set application name connection property
		artDbcpDataSource.setConnectionProperties(getAppNameProperty(datasourceInfo.getUrl(), datasourceInfo.getName()));

		//register driver so that connections are immediately usable
		registerDriver(datasourceInfo.getDriver());

		return artDbcpDataSource;
	}

	private void registerDriver(String driver) {
		logger.debug("Entering registerDriver: driver='{}'", driver);

		try {
			//newInstance only needed for buggy drivers e.g. neo4j 3.1.0
			//https://stackoverflow.com/questions/2092659/what-is-difference-between-class-forname-and-class-forname-newinstance/2093236#2093236
			//for jdbc 4 drivers, you don't have to specify driver or use class.forName()
			//https://stackoverflow.com/questions/5484227/jdbc-class-forname-vs-drivermanager-registerdriver
			if (StringUtils.isNotBlank(driver)) {
				Class.forName(driver).newInstance();
				logger.debug("JDBC driver registered: '{}'", driver);
			}
			//Class.forName(driver);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
			logger.error("Error while registering JDBC driver: '{}'", driver, ex);
		}
	}

	@Override
	protected void closePool() {
		artDbcpDataSource.close();
	}

	@Override
	public void refresh() {
		artDbcpDataSource.refreshConnections();
	}

	@Override
	public Integer getCurrentSize() {
		return artDbcpDataSource.getCurrentPoolSize();
	}

	@Override
	public Integer getInUseCount() {
		return artDbcpDataSource.getInUseCount();
	}

	@Override
	public Integer getHighestReachedSize() {
		return artDbcpDataSource.getHighestReachedPoolSize();
	}

	@Override
	public Long getTotalConnectionRequests() {
		return artDbcpDataSource.getTotalConnectionRequests();
	}
}
