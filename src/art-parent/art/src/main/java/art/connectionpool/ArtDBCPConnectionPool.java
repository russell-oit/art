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
import art.dbcp.ArtDBCPDataSource;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class ArtDBCPConnectionPool extends ConnectionPool {
	
	private static final Logger logger = LoggerFactory.getLogger(ArtDBCPConnectionPool.class);
	
	private ArtDBCPDataSource artDbcpDataSource;

	@Override
	protected DataSource createPool(DatasourceInfo datasourceInfo, int maxPoolSize) {
		long timeoutSeconds = TimeUnit.MINUTES.toSeconds(datasourceInfo.getConnectionPoolTimeout());  //convert timeout mins to seconds
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
			//newInstance only needed for buggy drivers
			//https://stackoverflow.com/questions/2092659/what-is-difference-between-class-forname-and-class-forname-newinstance/2093236#2093236
//			Class.forName(driver).newInstance();
			Class.forName(driver);
			logger.debug("JDBC driver registered: {}", driver);
		} catch (ClassNotFoundException ex) {
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
