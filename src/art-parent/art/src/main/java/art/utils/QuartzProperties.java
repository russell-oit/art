/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
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

import art.servlets.ArtDBCP;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to initialize a properties object to use to create a quartz scheduler
 * instance. <br> Instead of having all the configuration properties in a
 * properties file. To make it easier to change art database. in which case no
 * manual editing of the quartz properties file will be required also ensure the
 * database password is not available in plain text, as would happen if all the
 * details were held in a properties file some properties will remain in a
 * properties file e.g. thread count and can be manually changed from there only
 * retrieves properties. Doesn't save/update
 *
 * @author Timothy Anyona
 */
public class QuartzProperties {

	final static Logger logger = LoggerFactory.getLogger(QuartzProperties.class);
	String quartzPropsFilePath;

	/**
	 * Set path to quartz properties file
	 *
	 * @param value path to quartz properties file
	 */
	public void setQuartzPropsFilePath(String value) {
		quartzPropsFilePath = value;
	}

	/**
	 * Get populated properties object
	 *
	 * @return populated properties object or <code>null</code> if properties
	 * not loaded
	 */
	public Properties getProperties() {
		Properties props = null;

		try {
			String dbUrl;
			String dbDriver;
			String dbUsername;
			String dbPassword;
			final String DATASOURCE_NAME = "ArtDS";

			final String INSTANCE_NAME = "org.quartz.scheduler.instanceName";
			final String SKIP_UPDATE = "org.quartz.scheduler.skipUpdateCheck";
			final String MAKE_SCHEDULER_DAEMON = "org.quartz.scheduler.makeSchedulerThreadDaemon";
			final String THREAD_POOL_CLASS = "org.quartz.threadPool.class";
			final String THREAD_COUNT = "org.quartz.threadPool.threadCount";
			final String MAKE_THREADS_DAEMONS = "org.quartz.threadPool.makeThreadsDaemons";
			final String DRIVER_DELEGATE = "org.quartz.jobStore.driverDelegateClass";
			final String JOB_STORE_CLASS = "org.quartz.jobStore.class";
			final String DRIVER = "org.quartz.dataSource." + DATASOURCE_NAME + ".driver";
			final String URL = "org.quartz.dataSource." + DATASOURCE_NAME + ".URL";
			final String USER = "org.quartz.dataSource." + DATASOURCE_NAME + ".user";
			final String PASSWORD = "org.quartz.dataSource." + DATASOURCE_NAME + ".password";
			final String VALIDATION_QUERY = "org.quartz.dataSource." + DATASOURCE_NAME + ".validationQuery";
			final String JNDI_URL = "org.quartz.dataSource." + DATASOURCE_NAME + ".jndiURL";

			//check if art.properties file exists. this will supply data source details
			if (ArtDBCP.isArtSettingsLoaded()) {
				dbUrl = ArtDBCP.getArtSetting("art_jdbc_url");
				if (StringUtils.isBlank(dbUrl)) {
					dbUrl = ArtDBCP.getArtSetting("art_url"); //for 2.2.1 to 2.3+ migration. property name changed from art_url to art_jdbc_url
				}
				dbDriver = ArtDBCP.getArtSetting("art_jdbc_driver");
				dbUsername = ArtDBCP.getArtRepositoryUsername();
				dbPassword = ArtDBCP.getArtRepositoryPassword(); //has already been decrypted

				//load properties from quartz properties file if it exists
				props = new Properties();
				if (quartzPropsFilePath == null) {
					//use default path
					String sep = java.io.File.separator;
					quartzPropsFilePath = ArtDBCP.getAppPath() + sep + "WEB-INF" + sep + "classes" + sep + "art-quartz.properties";
				}
				File quartzFile = new File(quartzPropsFilePath);
				if (quartzFile.exists()) {
					FileInputStream o = new FileInputStream(quartzPropsFilePath);
					try {
						props.load(o);
					} finally {
						o.close();
					}
				}

				//finalize properties object. use values from the properties file if they exist and set defaults for those that don't exist
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
				props.setProperty("org.quartz.jobStore.dataSource", DATASOURCE_NAME);

				if (StringUtils.isNotBlank(dbDriver)) {
					//jdbc datasource
					if (props.getProperty(DRIVER) == null) {
						props.setProperty(DRIVER, dbDriver);
					}
					if (props.getProperty(URL) == null) {
						props.setProperty(URL, dbUrl);
					}
					if (props.getProperty(USER) == null) {
						props.setProperty(USER, dbUsername);
					}
					if (props.getProperty(PASSWORD) == null) {
						props.setProperty(PASSWORD, dbPassword);
					}

					int dbType = -1;
					final int ORACLE = 1;
					final int MYSQL = 2;
					final int HSQLDB = 3;
					final int POSTGRESQL = 4;
					final int SQLSERVER = 5;
					final int CUBRID = 6;

					if (dbDriver.indexOf("oracle") != -1) {
						dbType = ORACLE;
					} else if (dbDriver.indexOf("mysql") != -1) {
						dbType = MYSQL;
					} else if (dbDriver.indexOf("hsqldb") != -1) {
						dbType = HSQLDB;
					} else if (dbDriver.indexOf("postgresql") != -1) {
						dbType = POSTGRESQL;
					} else if (dbDriver.indexOf("jtds") != -1) {
						dbType = SQLSERVER;
					} else if (dbDriver.indexOf("sqlserver") != -1) {
						dbType = SQLSERVER;
					} else if (dbDriver.indexOf("cubrid") != -1) {
						dbType = CUBRID;
					}

					//set properties that depend on the database type
					switch (dbType) {
						case ORACLE:
							if (props.getProperty(DRIVER_DELEGATE) == null) {
								props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.oracle.OracleDelegate");
							}
							if (props.getProperty(VALIDATION_QUERY) == null) {
								props.setProperty(VALIDATION_QUERY, "select 1 from dual");
							}
							break;
						case MYSQL:
							if (props.getProperty(DRIVER_DELEGATE) == null) {
								props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
							}
							if (props.getProperty(VALIDATION_QUERY) == null) {
								props.setProperty(VALIDATION_QUERY, "select 1");
							}
							break;
						case HSQLDB:
							if (props.getProperty(DRIVER_DELEGATE) == null) {
								props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.HSQLDBDelegate");
							}
							if (props.getProperty(VALIDATION_QUERY) == null) {
								props.setProperty(VALIDATION_QUERY, "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
							}
							break;
						case POSTGRESQL:
							if (props.getProperty(DRIVER_DELEGATE) == null) {
								props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
							}
							if (props.getProperty(VALIDATION_QUERY) == null) {
								props.setProperty(VALIDATION_QUERY, "select 1");
							}
							break;
						case SQLSERVER:
							if (props.getProperty(DRIVER_DELEGATE) == null) {
								props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.MSSQLDelegate");
							}
							if (props.getProperty(VALIDATION_QUERY) == null) {
								props.setProperty(VALIDATION_QUERY, "select 1");
							}
							break;
						case CUBRID:
							if (props.getProperty(DRIVER_DELEGATE) == null) {
								props.setProperty(DRIVER_DELEGATE, "org.quartz.impl.jdbcjobstore.CUBRIDDelegate");
							}
							if (props.getProperty(VALIDATION_QUERY) == null) {
								props.setProperty(VALIDATION_QUERY, "select 1");
							}
							break;
						default:
							//use standard jdbc delegate if none is defined
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
						props.setProperty(JNDI_URL, ArtDBCP.getJndiDatasourceUrl(dbUrl));
					}
				}
			}
		} catch (Exception e) {
			props = null;
			logger.error("Error", e);
		}

		return props;
	}
}
