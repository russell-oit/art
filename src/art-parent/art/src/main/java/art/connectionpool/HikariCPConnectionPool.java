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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a connection pool using the hikari cp library
 *
 * @author Timothy Anyona
 */
public class HikariCPConnectionPool extends ConnectionPool {

	private static final Logger logger = LoggerFactory.getLogger(HikariCPConnectionPool.class);

	private HikariDataSource hikariDataSource;

	@Override
	protected DataSource createPool(DatasourceInfo datasourceInfo, int maxPoolSize) {
		logger.debug("Entering createPool: maxPoolSize={}", maxPoolSize);

		HikariConfig config = new HikariConfig();

		config.setPoolName(datasourceInfo.getName());
		config.setUsername(datasourceInfo.getUsername());
		config.setPassword(datasourceInfo.getPassword());
		//explicitly set minimum idle connection count to a low value to avoid
		//"too many connection" errors where you have multiple report datasources using the same server
		config.setMinimumIdle(1);
		config.setMaximumPoolSize(maxPoolSize);
		config.setJdbcUrl(datasourceInfo.getUrl());
		config.setDriverClassName(datasourceInfo.getDriver()); //registers/loads the driver

		if (StringUtils.isBlank(datasourceInfo.getTestSql())
				|| StringUtils.equals(datasourceInfo.getTestSql(), "isValid")) {
			//do nothing
		} else {
			config.setConnectionTestQuery(datasourceInfo.getTestSql());
		}

		long timeoutMillis = TimeUnit.MINUTES.toMillis(datasourceInfo.getConnectionPoolTimeoutMins());
		config.setIdleTimeout(timeoutMillis);

		//set application name connection property
		config.setDataSourceProperties(getAppNameProperty(datasourceInfo.getUrl(), datasourceInfo.getName()));

		hikariDataSource = new HikariDataSource(config);
		
		return hikariDataSource;
	}

	@Override
	protected void closePool() {
		hikariDataSource.close();
	}
}
