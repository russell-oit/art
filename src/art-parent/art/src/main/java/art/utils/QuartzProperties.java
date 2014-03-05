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
		Properties props = new Properties();

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
		if (propertiesFilePath != null) {
			File quartzFile = new File(propertiesFilePath);
			if (quartzFile.exists()) {
				try (FileInputStream o = new FileInputStream(propertiesFilePath)) {
					props.load(o);
				}
			}
		}

		//finalize properties object. 
		//use values from the properties file if they exist and use defaults for those that don't exist
		//instance name
		if (props.getProperty(INSTANCE_NAME) == null) {
			props.setProperty(INSTANCE_NAME, "ArtScheduler");
		}
		//make scheduler thread daemon
		if (props.getProperty(MAKE_SCHEDULER_DAEMON) == null) {
			props.setProperty(MAKE_SCHEDULER_DAEMON, "true");
		}
		//skip update
		if (props.getProperty(SKIP_UPDATE) == null) {
			props.setProperty(SKIP_UPDATE, "true");
		}
		//threads
		if (props.getProperty(THREAD_POOL_CLASS) == null) {
			props.setProperty(THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
		}
		if (props.getProperty(THREAD_COUNT) == null) {
			props.setProperty(THREAD_COUNT, "5");
		}
		if (props.getProperty(MAKE_THREADS_DAEMONS) == null) {
			props.setProperty(MAKE_THREADS_DAEMONS, "true");
		}
		//job store class
		if (props.getProperty(JOB_STORE_CLASS) == null) {
			props.setProperty(JOB_STORE_CLASS, "org.quartz.impl.jdbcjobstore.JobStoreTX");
		}
		//data source
		if (props.getProperty(DATASOURCE_NAME) == null) {
			props.setProperty(DATASOURCE_NAME, "ArtDs");
		}
		String dataSource = props.getProperty(DATASOURCE_NAME);

		final String DRIVER = "org.quartz.dataSource." + dataSource + ".driver";
		final String URL = "org.quartz.dataSource." + dataSource + ".URL";
		final String USER = "org.quartz.dataSource." + dataSource + ".user";
		final String PASSWORD = "org.quartz.dataSource." + dataSource + ".password";
		final String VALIDATION_QUERY = "org.quartz.dataSource." + dataSource + ".validationQuery";
		final String JNDI_URL = "org.quartz.dataSource." + dataSource + ".jndiURL";

		if (StringUtils.isNotBlank(dataSourceDriver)) {
			//jdbc datasource
			if (props.getProperty(DRIVER) == null) {
				props.setProperty(DRIVER, dataSourceDriver);
			}
			if (props.getProperty(URL) == null) {
				props.setProperty(URL, dataSourceUrl);
			}
			if (props.getProperty(USER) == null) {
				props.setProperty(USER, dataSourceUsername);
			}
			if (props.getProperty(PASSWORD) == null) {
				props.setProperty(PASSWORD, dataSourcePassword);
			}

			//set properties that depend on the database type
			if (StringUtils.startsWith(dataSourceUrl, "jdbc:oracle")) {
				if (props.getProperty(DRIVER_DELEGATE) == null) {
					props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate");
				}
				if (props.getProperty(VALIDATION_QUERY) == null) {
					props.setProperty(VALIDATION_QUERY, "select 1 from dual");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:db2") || StringUtils.startsWith(dataSourceUrl, "jdbc:as400")) {
				if (props.getProperty(DRIVER_DELEGATE) == null) {
					props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
				}
				if (props.getProperty(VALIDATION_QUERY) == null) {
					props.setProperty(VALIDATION_QUERY, "select 1 from sysibm.sysdummy1");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:hsqldb")) {
				if (props.getProperty(DRIVER_DELEGATE) == null) {
					props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.HSQLDBDelegate");
				}
				if (props.getProperty(VALIDATION_QUERY) == null) {
					props.setProperty(VALIDATION_QUERY, "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:postgresql")) {
				if (props.getProperty(DRIVER_DELEGATE) == null) {
					props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
				}
				if (props.getProperty(VALIDATION_QUERY) == null) {
					props.setProperty(VALIDATION_QUERY, "select 1");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:cubrid")) {
				if (props.getProperty(DRIVER_DELEGATE) == null) {
					props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.CUBRIDDelegate");
				}
				if (props.getProperty(VALIDATION_QUERY) == null) {
					props.setProperty(VALIDATION_QUERY, "select 1");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:sqlserver") || StringUtils.startsWith(dataSourceUrl, "jdbc:jtds")) {
				if (props.getProperty(DRIVER_DELEGATE) == null) {
					props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.MSSQLDelegate");
				}
				if (props.getProperty(VALIDATION_QUERY) == null) {
					props.setProperty(VALIDATION_QUERY, "select 1");
				}
			} else if (StringUtils.startsWith(dataSourceUrl, "jdbc:ids") || StringUtils.startsWith(dataSourceUrl, "jdbc:informix-sqli")) {
				if (props.getProperty(DRIVER_DELEGATE) == null) {
					props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
				}
				if (props.getProperty(VALIDATION_QUERY) == null) {
					props.setProperty(VALIDATION_QUERY, "select 1 from systables where tabid = 1");
				}
			} else {
				//MySQL and any other databases that use the standard
				//jdbc delegate and have "select 1" as a valid query
				if (props.getProperty(DRIVER_DELEGATE) == null) {
					props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
				}
				if (props.getProperty(VALIDATION_QUERY) == null) {
					props.setProperty(VALIDATION_QUERY, "select 1");
				}
			}
		} else {
			//jndi datasource
			if (props.getProperty(DRIVER_DELEGATE) == null) {
				props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
			}
			if (props.getProperty(JNDI_URL) == null) {
				props.setProperty(JNDI_URL, ArtUtils.getJndiDatasourceUrl(dataSourceUrl));
			}
		}

		return props;
	}
}
