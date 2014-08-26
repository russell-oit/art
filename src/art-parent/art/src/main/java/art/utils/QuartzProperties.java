/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
/* 
 class to initialize a properties object to use to create a quartz scheduler instance
 instead of having all the configuration properties in a properties file.
 to make it easier to change art database. in which case no manual editing of the quartz properties file will be required
 also ensure the database password is not available in plain text, as would happen if all the details were held in a properties file
 some properties will remain in a properties file e.g. thread count and can be manually changed from there
 only retrieves properties. Doesn't save/update
 */
package art.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to initialize a properties object to use to create a quartz scheduler
 * instance.
 *
 * @author Timothy Anyona
 */
public class QuartzProperties {

	private static final Logger logger = LoggerFactory.getLogger(QuartzProperties.class);
	private String propertiesFilePath;
	private String dataSourceDriver;
	private String dataSourceUrl;
	private String dataSourceUsername;
	private String dataSourcePassword;

	/**
	 * @return the propertiesFilePath
	 */
	public String getPropertiesFilePath() {
		return propertiesFilePath;
	}

	/**
	 * @param propertiesFilePath the propertiesFilePath to set
	 */
	public void setPropertiesFilePath(String propertiesFilePath) {
		this.propertiesFilePath = propertiesFilePath;
	}

	/**
	 * @return the dataSourceDriver
	 */
	public String getDataSourceDriver() {
		return dataSourceDriver;
	}

	/**
	 * @param dataSourceDriver the dataSourceDriver to set
	 */
	public void setDataSourceDriver(String dataSourceDriver) {
		this.dataSourceDriver = dataSourceDriver;
	}

	/**
	 * @return the dataSourceUrl
	 */
	public String getDataSourceUrl() {
		return dataSourceUrl;
	}

	/**
	 * @param dataSourceUrl the dataSourceUrl to set
	 */
	public void setDataSourceUrl(String dataSourceUrl) {
		this.dataSourceUrl = dataSourceUrl;
	}

	/**
	 * @return the dataSourceUsername
	 */
	public String getDataSourceUsername() {
		return dataSourceUsername;
	}

	/**
	 * @param dataSourceUsername the dataSourceUsername to set
	 */
	public void setDataSourceUsername(String dataSourceUsername) {
		this.dataSourceUsername = dataSourceUsername;
	}

	/**
	 * @return the dataSourcePassword
	 */
	public String getDataSourcePassword() {
		return dataSourcePassword;
	}

	/**
	 * @param dataSourcePassword the dataSourcePassword to set
	 */
	public void setDataSourcePassword(String dataSourcePassword) {
		this.dataSourcePassword = dataSourcePassword;
	}

	/**
	 * Get populated properties object
	 *
	 * @return populated properties object
	 * @throws IOException
	 */
	public Properties getProperties() throws IOException {
		logger.debug("Entering getProperties");
		
		Properties properties = new Properties();

		//quartz property names
		final String INSTANCE_NAME = "org.quartz.scheduler.instanceName";
		final String SKIP_UPDATE = "org.quartz.scheduler.skipUpdateCheck";
		final String MAKE_SCHEDULER_DAEMON = "org.quartz.scheduler.makeSchedulerThreadDaemon";
		final String THREAD_POOL_CLASS = "org.quartz.threadPool.class";
		final String THREAD_COUNT = "org.quartz.threadPool.threadCount";
		final String MAKE_THREADS_DAEMONS = "org.quartz.threadPool.makeThreadsDaemons";
		final String DRIVER_DELEGATE = "org.quartz.jobStore.driverDelegateClass";
		final String JOB_STORE_CLASS = "org.quartz.jobStore.class";
		final String DATASOURCE_NAME = "org.quartz.jobStore.dataSource";

		//load properties from quartz properties file if it exists
		logger.debug("propertiesFilePath='{}", propertiesFilePath);
		if (propertiesFilePath != null) {
			File quartzFile = new File(propertiesFilePath);
			if (quartzFile.exists()) {
				try (FileInputStream o = new FileInputStream(propertiesFilePath)) {
					properties.load(o);
				}
			}
		}

		//finalize properties object. 
		//use values from the properties file if they exist and use defaults for those that don't exist
		//instance name
		if (properties.getProperty(INSTANCE_NAME) == null) {
			properties.setProperty(INSTANCE_NAME, "ArtScheduler");
		}
		//make scheduler thread daemon
		if (properties.getProperty(MAKE_SCHEDULER_DAEMON) == null) {
			properties.setProperty(MAKE_SCHEDULER_DAEMON, "true");
		}
		//skip update
		if (properties.getProperty(SKIP_UPDATE) == null) {
			properties.setProperty(SKIP_UPDATE, "true");
		}
		//threads
		if (properties.getProperty(THREAD_POOL_CLASS) == null) {
			properties.setProperty(THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
		}
		if (properties.getProperty(THREAD_COUNT) == null) {
			properties.setProperty(THREAD_COUNT, "5");
		}
		if (properties.getProperty(MAKE_THREADS_DAEMONS) == null) {
			properties.setProperty(MAKE_THREADS_DAEMONS, "true");
		}
		//job store class
		if (properties.getProperty(JOB_STORE_CLASS) == null) {
			properties.setProperty(JOB_STORE_CLASS, "org.quartz.impl.jdbcjobstore.JobStoreTX");
		}
		//data source
		if (properties.getProperty(DATASOURCE_NAME) == null) {
			properties.setProperty(DATASOURCE_NAME, "ArtDs");
		}
		
		//set datasource property names
		String datasourceName = properties.getProperty(DATASOURCE_NAME);
		
		final String DRIVER = "org.quartz.dataSource." + datasourceName + ".driver";
		final String URL = "org.quartz.dataSource." + datasourceName + ".URL";
		final String USER = "org.quartz.dataSource." + datasourceName + ".user";
		final String PASSWORD = "org.quartz.dataSource." + datasourceName + ".password";
		final String VALIDATION_QUERY = "org.quartz.dataSource." + datasourceName + ".validationQuery";
		final String JNDI_URL = "org.quartz.dataSource." + datasourceName + ".jndiURL";

		if (StringUtils.isNotBlank(dataSourceDriver)) {
			//jdbc datasource
			if (properties.getProperty(DRIVER) == null) {
				properties.setProperty(DRIVER, dataSourceDriver);
			}
			if (properties.getProperty(URL) == null) {
				properties.setProperty(URL, dataSourceUrl);
			}
			if (properties.getProperty(USER) == null) {
				properties.setProperty(USER, dataSourceUsername);
			}
			if (properties.getProperty(PASSWORD) == null) {
				properties.setProperty(PASSWORD, dataSourcePassword);
			}

			//set properties that depend on the database type
			if (StringUtils.startsWith(dataSourceUrl, "jdbc:oracle")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1 from dual");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:db2") || StringUtils.startsWith(dataSourceUrl, "jdbc:as400")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1 from sysibm.sysdummy1");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:hsqldb")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.HSQLDBDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:postgresql")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:cubrid")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.CUBRIDDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:sqlserver") || StringUtils.startsWith(dataSourceUrl, "jdbc:jtds")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.MSSQLDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:ids") || StringUtils.startsWith(dataSourceUrl, "jdbc:informix-sqli")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1 from systables where tabid = 1");
				}
			} else {
				//MySQL and any other databases that use the standard
				//jdbc delegate and have "select 1" as a valid query
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1");
				}
			}
		} else {
			//jndi datasource
			if (properties.getProperty(DRIVER_DELEGATE) == null) {
				properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
			}
			if (properties.getProperty(JNDI_URL) == null) {
				properties.setProperty(JNDI_URL, ArtUtils.getJndiDatasourceUrl(dataSourceUrl));
			}
		}

		return properties;
	}
}
