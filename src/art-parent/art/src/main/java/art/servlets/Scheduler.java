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

import art.cache.CacheHelper;
import art.utils.UpgradeHelper;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
	private static final Logger logger = LoggerFactory.getLogger(Scheduler.class);
	static long INTERVAL_MINUTES = 10; // run clean every x minutes
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
			if (ArtConfig.isArtDatabaseConfigured()) {
				upgrade();
			}

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

		// Delete old files in the reports export directory
		cleanDirectory(ArtConfig.getReportsExportPath());

		//clear mondrian cache
		clearMondrianCache();
	}

	/**
	 * Delete old files in a directory
	 *
	 * @param directoryPath
	 */
	private void cleanDirectory(String directoryPath) {
		File directory = new File(directoryPath);
		File[] files = directory.listFiles();
		long limit = System.currentTimeMillis() - INTERVAL_DELETE_FILES;

		//only delete expected file types
		List<String> validExtensions = new ArrayList<>();
		validExtensions.add("xml");
		validExtensions.add("pdf");
		validExtensions.add("xls");
		validExtensions.add("xlsx");
		validExtensions.add("html");
		validExtensions.add("zip");
		validExtensions.add("slk");
		validExtensions.add("gz");
		validExtensions.add("tsv");

		for (File file : files) {
			// Delete the file if it is older than INTERVAL_DELETE_FILES
			if (FileUtils.isFileOlder(file, limit)) {
				String extension = FilenameUtils.getExtension(file.getName()).toLowerCase();
				if (file.isDirectory() || validExtensions.contains(extension)) {
					boolean deleted = FileUtils.deleteQuietly(file);
					if (!deleted) {
						logger.warn("File not deleted: {}", file);
					}
				}
			}
		}
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
			String cacheFilePath = ArtConfig.getArtTempPath() + "mondrian-cache-cleared.txt";
			File cacheFile = new File(cacheFilePath);
			long limit = System.currentTimeMillis() - mondrianCacheExpiry;
			if (!cacheFile.exists() || FileUtils.isFileOlder(cacheFile, limit)) {
				clearCache = true;
			}

			if (clearCache) {
				logger.debug("Actually clearing mondrian cache");

				CacheHelper cacheHelper = new CacheHelper();
				cacheHelper.clearMondrian();

				try {
					//create/update file that indicates when the cache was last cleared
					FileUtils.writeStringToFile(cacheFile, new Date().toString());
				} catch (IOException e) {
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
