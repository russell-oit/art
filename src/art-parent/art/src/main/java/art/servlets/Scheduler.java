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
/**
 *
 * Description: Delete export and job files older than XX minutes
 *
 *
 * Note:	Find a better way to avoid user to read all available export files. The
 * workaround is to have a dummy index.html file so that the web server does not
 * display all the content Anyway, this may not work on all servlet engines. Use
 * ExportPathFilter?
 *
 * @author Enrico Liboni
 * @mail enrico(at)computer.org
 */
package art.servlets;

import art.utils.UpgradeHelper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
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
	Timer t;

	/**
	 * Start clean files thread
	 *
	 * @param config servlet config
	 * @throws ServletException
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {

		try {
			super.init(config);

			logger.debug("ART Scheduler starting up");

			//run upgrade steps
			upgrade();

			//start clean thread timer
			t = new Timer(this);
			t.start();

			logger.debug("ART scheduler running");
		} catch (ServletException e) {
			logger.error("Error", e);
		}
	}

	/**
	 * run upgrade steps
	 */
	private void upgrade() {
		File upgradeFile = new File(ArtConfig.getArtTempPath() + "upgrade.txt");
		if (upgradeFile.exists()) {
			try {
				UpgradeHelper upgradeHelper = new UpgradeHelper();
				upgradeHelper.upgrade();
				boolean deleted = upgradeFile.delete();
				if (!deleted) {
					logger.warn("Upgrade file not deleted: {}", upgradeFile);
				}
			} catch (SQLException ex) {
				logger.error("Error", ex);
			}
		}
	}

	/**
	 * Stop clean files thread
	 */
	@Override
	public void destroy() {

		try {
			//stop clean thread
			if (t != null) {
				t.interrupt();
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	/**
	 * Delete old export files
	 */
	public void clean() {

		logger.debug("Running clean");

		if (ArtConfig.isArtSettingsLoaded()) {
			try {
				// Delete old files in the export directory
				File exportFiles = new File(ArtConfig.getExportPath());
				File[] fileNames = exportFiles.listFiles();
				long lastModified;
				long actualTime = new Date().getTime();
				String fileName;
				for (File file : fileNames) {
					lastModified = file.lastModified();
					fileName = file.getName();
					// Delete the file if it is older than INTERVAL_DELETE_FILES
					// and the name is not "index.html"
					// This is a workaround in order to avoid a user to
					// view all the export files though the browser...
					if ((actualTime - lastModified) > INTERVAL_DELETE_FILES) {
						//delete directories that may be created by jasper report html output
						if (file.isDirectory()) {
							if (!fileName.equals("jobs")) {
								deleteDirectory(file);
							}
						} else if (!fileName.equals("index.html")) {
							boolean deleted = file.delete();
							if (!deleted) {
								logger.warn("File not deleted: {}", file);
							}
						}
					}
				}

				//clear mondrian cache
				clearMondrianCache();

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
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					boolean deleted = file.delete();
					if (!deleted) {
						logger.warn("File not deleted: {}", file);
					}
				}
			}
		}
		return (path.delete());
	}

	/**
	 * clear mondrian cache according to configured cache expiry
	 */
	private void clearMondrianCache() {
		logger.debug("Entering clearMondrianCache");

		long mondrianCacheExpiry; //cache expiry duration in milliseconds

		mondrianCacheExpiry = (long) ArtConfig.getSettings().getMondrianCacheExpiryPeriod();
		mondrianCacheExpiry = mondrianCacheExpiry * 60 * 60 * 1000; //convert period defined in hours to milliseconds

		if (mondrianCacheExpiry > 0) {
			boolean clearCache = false;
			long nowTimestamp = new Date().getTime();
			String cacheFilePath = ArtConfig.getArtTempPath() + "mondrian-cache-cleared.txt";
			File cacheFile = new File(cacheFilePath);
			if (cacheFile.exists()) {
				//check last modified date
				long lastModified = cacheFile.lastModified();
				if ((nowTimestamp - lastModified) > mondrianCacheExpiry) {
					clearCache = true;
					boolean deleted = cacheFile.delete();
					if (!deleted) {
						logger.warn("File not deleted: {}", cacheFile);
					}
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

				BufferedWriter out = null;
				try {
					//create file that indicates when the cache was last cleared
					out = new BufferedWriter(new FileWriter(cacheFilePath));
					Date now = new Date();
					out.write(now.toString());
				} catch (IOException e) {
					logger.error("Error", e);
				} finally {
					//Close the BufferedWriter
					try {
						if (out != null) {
							out.flush();
							out.close();
						}
					} catch (IOException e) {
						logger.error("Error while closing writer", e);
					}
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

//thread that runs scheduler clean method to delete old export files
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
