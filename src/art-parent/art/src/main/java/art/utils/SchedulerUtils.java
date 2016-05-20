/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.utils;

import art.artdatabase.ArtDatabase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for creating, retrieving and shutting down the quartz
 * scheduler instance used within the application
 *
 * @author Timothy Anyona
 */
public class SchedulerUtils {

	private static final Logger logger = LoggerFactory.getLogger(SchedulerUtils.class);

	private static Scheduler scheduler;

	/**
	 * Creates and starts a new scheduler instance, shutting down the existing
	 * one if any. The scheduler is stored as a static variable in this class.
	 *
	 * @param artDbConfig
	 * @param propertiesFilePath
	 * @return
	 */
	public static Scheduler createScheduler(ArtDatabase artDbConfig, String propertiesFilePath) {
		try {
			//shutdown existing scheduler instance. if shutdown throws an exception, don't create a new scheduler
			if (scheduler != null) {
				scheduler.shutdown();
				scheduler = null;
			}

			//create new scheduler instance
			Properties schedulerProperties = getSchedulerProperties(artDbConfig, propertiesFilePath);
			SchedulerFactory schedulerFactory = new StdSchedulerFactory(schedulerProperties);
			scheduler = schedulerFactory.getScheduler();
			scheduler.start();
		} catch (SchedulerException ex) {
			logger.error("Error", ex);
		}

		return scheduler;
	}

	public static Scheduler getScheduler() {
		return scheduler;
	}

	public static void shutdownScheduler() {
		if (scheduler != null) {
			try {
				scheduler.shutdown();
				scheduler = null;
			} catch (SchedulerException ex) {
				logger.error("Error", ex);
			}
		}
	}

	/**
	 * Get populated properties object
	 *
	 * @param artDbConfig
	 * @param propertiesFilePath
	 * @return populated properties object
	 */
	private static Properties getSchedulerProperties(ArtDatabase artDbConfig, String propertiesFilePath) {
		logger.debug("Entering getSchedulerProperties: propertiesFilePath='{}'", propertiesFilePath);

		Objects.requireNonNull(artDbConfig, "artDbConfig must not be null");

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
		if (propertiesFilePath != null) {
			File propertiesFile = new File(propertiesFilePath);
			if (propertiesFile.exists()) {
				try (FileInputStream o = new FileInputStream(propertiesFilePath)) {
					properties.load(o);
				} catch (IOException ex) {
					logger.error("Error", ex);
				}
			}
		}

		//finalize properties object
		//use values from the properties file or defaults for those that don't exist
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

		String url = artDbConfig.getUrl();

		if (artDbConfig.isJndi()) {
			//jndi datasource
			if (properties.getProperty(DRIVER_DELEGATE) == null) {
				properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
			}
			if (properties.getProperty(JNDI_URL) == null) {
				properties.setProperty(JNDI_URL, url);
			}
		} else {
			//jdbc datasource
			if (properties.getProperty(DRIVER) == null) {
				properties.setProperty(DRIVER, artDbConfig.getDriver());
			}
			if (properties.getProperty(URL) == null) {
				properties.setProperty(URL, url);
			}
			if (properties.getProperty(USER) == null) {
				properties.setProperty(USER, artDbConfig.getUsername());
			}
			if (properties.getProperty(PASSWORD) == null) {
				properties.setProperty(PASSWORD, artDbConfig.getPassword());
			}

			//set properties that depend on the database type
			if (StringUtils.startsWith(url, "jdbc:oracle")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1 from dual");
				}
			} else if (StringUtils.startsWith(url, "jdbc:db2") || StringUtils.startsWith(url, "jdbc:as400")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1 from sysibm.sysdummy1");
				}
			} else if (StringUtils.startsWith(url, "jdbc:hsqldb")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.HSQLDBDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "values 1");
				}
			} else if (StringUtils.startsWith(url, "jdbc:postgresql")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1");
				}
			} else if (StringUtils.startsWith(url, "jdbc:cubrid")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.CUBRIDDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1");
				}
			} else if (StringUtils.startsWith(url, "jdbc:sqlserver") || StringUtils.startsWith(url, "jdbc:jtds")) {
				if (properties.getProperty(DRIVER_DELEGATE) == null) {
					properties.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.MSSQLDelegate");
				}
				if (properties.getProperty(VALIDATION_QUERY) == null) {
					properties.setProperty(VALIDATION_QUERY, "select 1");
				}
			} else if (StringUtils.startsWith(url, "jdbc:ids") || StringUtils.startsWith(url, "jdbc:informix-sqli")) {
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
		}

		return properties;
	}
}
