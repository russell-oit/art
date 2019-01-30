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
package art.utils;

import art.artdatabase.ArtDatabase;
import art.enums.DatabaseProtocol;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
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
	 * Creates and starts a new quartz scheduler instance, shutting down the
	 * existing one if any. The scheduler is stored as a static variable in this
	 * class.
	 *
	 * @param artDbConfig the art database configuration
	 * @param propertiesFilePath the quartz scheduler properties file path
	 * @return the created scheduler instance
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
			//use startDelayed() instead of start()
			//https://sourceforge.net/p/art/discussion/352129/thread/c3c7f2b2/
			int SCHEDULER_START_DELAY_SECONDS = 60;
			scheduler.startDelayed(SCHEDULER_START_DELAY_SECONDS);
		} catch (SchedulerException ex) {
			logger.error("Error", ex);
		}

		return scheduler;
	}

	/**
	 * Returns the quartz scheduler
	 *
	 * @return the quartz scheduler
	 */
	public static Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * Shuts down the quartz scheduler
	 */
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
	 * Returns quartz scheduler properties
	 *
	 * @param artDbConfig the art database configuration, not null
	 * @param propertiesFilePath the quartz scheduler properties file
	 * @return quartz scheduler properties
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
			if (properties.getProperty(JNDI_URL) == null) {
				properties.setProperty(JNDI_URL, url);
			}
		} else {
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
		}

		//set properties that depend on the database type
		DatabaseProtocol databaseProtocol = artDbConfig.getEffectiveDatabaseProtocol();
		if (properties.getProperty(DRIVER_DELEGATE) == null) {
			properties.setProperty(DRIVER_DELEGATE, databaseProtocol.quartzJobStoreDelegate());
		}
		if (properties.getProperty(VALIDATION_QUERY) == null) {
			properties.setProperty(VALIDATION_QUERY, databaseProtocol.testSql());
		}

		return properties;
	}
}
