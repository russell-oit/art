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

import art.datasource.Datasource;
import art.datasource.DatasourceOptions;
import art.servlets.Config;
import art.utils.ArtUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
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
	protected DataSource createPool(Datasource datasource, int maxPoolSize) {
		logger.debug("Entering createPool: maxPoolSize={}", maxPoolSize);

		HikariConfig config = createHikariConfig(datasource);

		config.setPoolName(datasource.getName());
		config.setUsername(datasource.getUsername());
		config.setPassword(datasource.getPassword());
		//explicitly set minimum idle connection count to a low value to avoid
		//"too many connection" errors where you have multiple report datasources using the same server
		config.setMinimumIdle(1);
		config.setMaximumPoolSize(maxPoolSize);
		config.setJdbcUrl(datasource.getUrl());

		String driver = datasource.getDriver();
		if (StringUtils.isNotBlank(driver)) {
			config.setDriverClassName(driver); //registers/loads the driver
		}

		if (StringUtils.isBlank(datasource.getTestSql())
				|| StringUtils.equals(datasource.getTestSql(), "isValid")) {
			//do nothing
		} else {
			config.setConnectionTestQuery(datasource.getTestSql());
		}

		long timeoutMillis = TimeUnit.MINUTES.toMillis(datasource.getConnectionPoolTimeoutMins());
		config.setIdleTimeout(timeoutMillis);

		//set application name connection property
		config.setDataSourceProperties(getAppNameProperty(datasource.getUrl(), datasource.getName()));

		hikariDataSource = new HikariDataSource(config);

		return hikariDataSource;
	}

	/**
	 * Creates a HikariConfig object for a datasource, considering properties
	 * configured in the hikaricp.properties file and/or on the datasource
	 *
	 * @param datasource the datasource for which to create a config object
	 * @return new HikariConfig instance
	 */
	private HikariConfig createHikariConfig(Datasource datasource) {
		logger.debug("Entering createHikariConfig");

		try {
			//https://www.baeldung.com/java-merging-properties
			//https://stackoverflow.com/questions/2004833/how-to-merge-two-java-util-properties-objects
			Properties mergedProperties = new Properties();

			String propertiesFilePath = Config.getClassesPath() + "hikaricp.properties";
			Properties fileProperties = ArtUtils.loadPropertiesFromFile(propertiesFilePath);
			mergedProperties.putAll(fileProperties);

			String options = datasource.getOptions();
			if (StringUtils.isNotBlank(options)) {
				DatasourceOptions datasourceOptions = ArtUtils.jsonToObject(options, DatasourceOptions.class);
				Map<String, Object> datasourcePropertiesMap = datasourceOptions.getHikariCp();
				if (datasourcePropertiesMap != null) {
					Map<String, String> stringMap = new HashMap<>();
					for (Entry<String, Object> entry : datasourcePropertiesMap.entrySet()) {
						//https://stackoverflow.com/questions/2004833/how-to-merge-two-java-util-properties-objects
						String key = entry.getKey();
						Object value = entry.getValue();
						if (key != null && value != null) {
							stringMap.put(key, String.valueOf(value));
						}
					}
					mergedProperties.putAll(stringMap);
				}
			}

			HikariConfig config = new HikariConfig(mergedProperties);
			return config;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void closePool() {
		hikariDataSource.close();
	}
}
