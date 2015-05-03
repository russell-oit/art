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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Timothy Anyona
 */
public class HikariCPConnectionPool extends ConnectionPool {

	private HikariDataSource hikariDataSource;

	@Override
	protected DataSource createPool(DatasourceInfo datasourceInfo, int maxPoolSize) {
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

		hikariDataSource = new HikariDataSource(config);
		return hikariDataSource;
	}

	@Override
	protected void closePool() {
		hikariDataSource.close();
	}

}
