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

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import art.datasource.Datasource;
import art.datasource.DatasourceOptions;
import art.utils.ArtUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

    private Map<String, Object> toHikariProperties(String options) {
        Map<String, Object> result;
        if (StringUtils.isNotBlank(options)) {

        }
        return null;
    }

    @Override
    protected DataSource createPool(Datasource datasource, int maxPoolSize) {
        logger.debug("Entering createPool: maxPoolSize={}", maxPoolSize);

        HikariConfig config = initHikariConfig(datasource.getOptions());

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
     * Initializes data source properties.
     * If properties are specified in the configuration
     * then these properties will be transferred to the configuration
     *
     * @param options - data source settings in json format
     * @return new HikariConfig instance
     * @see DatasourceOptions
     */
    private HikariConfig initHikariConfig(String options) {
        try {
            DatasourceOptions datasourceOptions = ArtUtils.jsonToObject(options, DatasourceOptions.class);
            Map<String, Object> props;
            HikariConfig result;
            if (datasourceOptions != null && (props = datasourceOptions.getProperties()) != null) {
                Properties properties = new Properties();
                properties.putAll(props);
                result = new HikariConfig(properties);
            } else {
                result = new HikariConfig();
            }
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException("Illegal Datasource options value: " + options, e);
        }
    }

    @Override
    protected void closePool() {
        hikariDataSource.close();
    }
}
