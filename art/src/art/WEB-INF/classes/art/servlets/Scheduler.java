/*
 * Copyright (C) 2001/2004  Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   (version 2) along with this program (see documentation directory);
 *   otherwise, have a look at http://www.gnu.org or write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
/**
 *
 * Description: Delete export and job files older than XX minutes
 *
 *
 * TODO:	Find a better way to avoid user to read all available export files. The
 * workaround is to have a dummy index.html file so that the web server does not
 * display all the content Anyway, this may not work on all servlet engine
 *
 * @author Enrico Liboni
 * @mail enrico(at)computer.org
 */
package art.servlets;

import art.utils.ArtJob;
import art.utils.ArtSettings;
import art.utils.Encrypter;
import art.utils.QuartzProperties;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.lang.StringUtils;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to delete export and job files older than x minutes. Also clears the
 * mondrian cache every x hours.
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class Scheduler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(Scheduler.class);
	static long INTERVAL_MINUTES = 5; // run clean every x minutes
	static long DELETE_FILES_MINUTES = 45; // Delete exported files older than x minutes
	long INTERVAL = (1000 * 60 * INTERVAL_MINUTES); // INTERVAL_MINUTES in milliseconds
	long INTERVAL_DELETE_FILES = (1000 * 60 * DELETE_FILES_MINUTES); // DELETE_FILES_MINUTES in milliseconds
	String exportPath;
	final String MONDRIAN_CACHE_CLEARED_FILE_NAME = "mondrian-cache-cleared.txt"; //file to indicate when the mondrian cache was last cleared
	Timer t;

	/**
	 * Initialize quartz scheduler and clean files timer.
	 *
	 * @param config servlet config
	 * @throws ServletException
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {

		try {
			super.init(config);

			logger.debug("ART Scheduler starting up");

			exportPath = ArtDBCP.getExportPath();

			if (ArtDBCP.isArtSettingsLoaded()) {
				//get quartz properties object to use to instantiate a scheduler
				QuartzProperties qp = new QuartzProperties();
				Properties props = qp.getProperties();

				if (props == null) {
					logger.warn("Quartz properties not set. Job scheduling will not be possible");
				} else {
					//start quartz scheduler
					SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);
					org.quartz.Scheduler scheduler = schedulerFactory.getScheduler();

					if (ArtDBCP.isSchedulingEnabled()) {
						scheduler.start();
					} else {
						scheduler.standby();
					}

					//save scheduler, to make it accessible throughout the application
					ArtDBCP.setScheduler(scheduler);

					//migrate existing jobs to quartz, if any exist from previous art versions                
					ArtJob aj = new ArtJob();
					aj.migrateJobsToQuartz();
				}
			}

			t = new Timer(this);
			t.start();

			logger.debug("ART clean thread running");
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	/**
	 * Stop quartz scheduler and clean files timer.
	 */
	@Override
	public void destroy() {

		try {
			if (t != null) {
				t.interrupt();

				logger.info("ART clean thread stopped");
			}

			//shutdown quartz scheduler
			org.quartz.Scheduler scheduler = ArtDBCP.getScheduler();
			if (scheduler != null) {
				scheduler.shutdown();
				Thread.sleep(1000); //allow delay to avoid tomcat reporting that threads weren't stopped. (http://forums.terracotta.org/forums/posts/list/3479.page)
			}

		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	/**
	 * Delete old export or job files.
	 */
	public void clean() {

		logger.debug("Running clean");

		if (ArtDBCP.isArtSettingsLoaded()) {
			try {
				// Delete old files in the export directory
				File exportFiles = new File(exportPath);
				File[] fileNames = exportFiles.listFiles();
				long lastModified;
				long actualTime = new java.util.Date().getTime();
				String fileName;
				for (int i = 0; i < fileNames.length; i++) {
					lastModified = fileNames[i].lastModified();
					fileName = fileNames[i].getName();
					// Delete the file if it is older than INTERVAL_DELETE_FILES
					// and the name is not "index.html"
					// This is a workaround in order to avoid a user to
					// view all the export files though the browser...
					if ((actualTime - lastModified) > INTERVAL_DELETE_FILES) {
						//delete directories that may be created by jasper report html output
						if (fileNames[i].isDirectory()) {
							if (!fileName.equals("jobs")) {
								deleteDirectory(fileNames[i]);
							}
						} else if (!fileName.equals("index.html") && !fileName.equals(MONDRIAN_CACHE_CLEARED_FILE_NAME)) {
							fileNames[i].delete();
						}
					}
				}

				// Delete old files in the export/jobs directory
				long jobFilesRetentionPeriod = (long) ArtDBCP.getPublishedFilesRetentionPeriod(); //configured file retention period in days
				jobFilesRetentionPeriod = jobFilesRetentionPeriod * 24 * 60 * 60 * 1000; //convert period defined in days to milliseconds

				if (jobFilesRetentionPeriod > 0) {
					exportFiles = new File(exportPath + "jobs/");
					fileNames = exportFiles.listFiles();
					for (int i = 0; i < fileNames.length; i++) {
						lastModified = fileNames[i].lastModified();
						if ((actualTime - lastModified) > jobFilesRetentionPeriod) {
							//delete directories that may be created by jasper report html output
							if (fileNames[i].isDirectory()) {
								deleteDirectory(fileNames[i]);
							} else if (!fileNames[i].getName().equals("index.html")) {
								fileNames[i].delete();
							}
						}
					}
				}

				//clear mondrian cache as per configuration
				if (ArtDBCP.isArtFullVersion()) {
					clearMondrianCache();
				}

			} catch (Exception e) {
				logger.error("Error", e);
			}
		} else {
			logger.debug("ART settings not defined");
		}

	}

	/**
	 * Delete directory, including all files and subdirectories under it.
	 *
	 * @param path directory to delete
	 * @return <code>true</code> if directory deleted. <code>false</code>
	 * otherwise.
	 */
	private boolean deleteDirectory(File path) {
		logger.debug("Deleting directory: {}", path);

		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	/**
	 * clear mondrian cache according to configured cache expiry
	 */
	private void clearMondrianCache() {
		logger.debug("Entering clear mondrian cache");

		long mondrianCacheExpiry; //cache expiry duration in milliseconds

		mondrianCacheExpiry = (long) ArtDBCP.getMondrianCacheExpiry();
		mondrianCacheExpiry = mondrianCacheExpiry * 60 * 60 * 1000; //convert period defined in hours to milliseconds

		if (mondrianCacheExpiry > 0) {
			boolean clearCache = false;
			long actualTime = new java.util.Date().getTime();
			File cacheFile = new File(exportPath + MONDRIAN_CACHE_CLEARED_FILE_NAME);
			if (cacheFile.exists()) {
				//check last modified date
				long lastModified = cacheFile.lastModified();
				if ((actualTime - lastModified) > mondrianCacheExpiry) {
					clearCache = true;
					cacheFile.delete();
				}
			} else {
				clearCache = true;
			}

			if (clearCache) {
				logger.debug("Actually clearing mondrian cache");

				//clear all mondrian caches
				java.util.Iterator<mondrian.rolap.RolapSchema> schemaIterator = mondrian.rolap.RolapSchema.getRolapSchemas();
				while (schemaIterator.hasNext()) {
					mondrian.rolap.RolapSchema schema = schemaIterator.next();
					mondrian.olap.CacheControl cacheControl = schema.getInternalConnection().getCacheControl(null);

					cacheControl.flushSchemaCache();
				}

				BufferedWriter out;
				try {
					//create file that indicates when the cache was last cleared
					out = new BufferedWriter(new FileWriter(exportPath + MONDRIAN_CACHE_CLEARED_FILE_NAME));
					java.util.Date now = new java.util.Date();
					out.write(now.toString());
					out.close();
				} catch (Exception e) {
					logger.error("Error", e);
				}
			}

		}
	}

	/**
	 * Get the interval between file cleaning runs.
	 *
	 * @return the interval between file cleaning runs.
	 */
	public long getInterval() {
		return INTERVAL;




	}
}

//thread that runs scheduler clean method to delete old export and jobs files
class Timer extends Thread {

	art.servlets.Scheduler scheduler;
	long interval;

	public Timer(art.servlets.Scheduler s) {
		scheduler = s;
		interval = s.getInterval();
	}

	@Override
	public void run() {
		try {
			while (true) {
				// clean old files in export path
				scheduler.clean();

				sleep(interval);
			}
		} catch (InterruptedException e) {
		}
	}
}
