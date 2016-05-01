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
package art.jobrunners;

import art.cache.CacheHelper;
import art.servlets.Config;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class CleanJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(CleanJob.class);

	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
		logger.debug("Entering execute");

		// Delete old files in the reports export directory
		cleanDirectory(Config.getReportsExportPath());

		//clear mondrian cache
		clearMondrianCache();
	}

	/**
	 * Delete old files in a directory
	 *
	 * @param directoryPath
	 */
	private void cleanDirectory(String directoryPath) {
		logger.debug("Entering cleanDirectory: directoryPath='{}'", directoryPath);
		
		File directory = new File(directoryPath);
		File[] files = directory.listFiles();
		final long DELETE_FILES_MINUTES = 45; // Delete exported files older than x minutes
		final long DELETE_FILES_MILLIS = TimeUnit.MINUTES.toMillis(DELETE_FILES_MINUTES);
		long limit = System.currentTimeMillis() - DELETE_FILES_MILLIS;

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
			// Delete the file if it is older than DELETE_FILES_MINUTES
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

		int mondrianCacheExpiryHours = Config.getSettings().getMondrianCacheExpiryPeriod();
		long mondrianCacheExpiryMillis = TimeUnit.HOURS.toMillis(mondrianCacheExpiryHours);

		if (mondrianCacheExpiryMillis > 0) {
			boolean clearCache = false;
			String cacheFilePath = Config.getArtTempPath() + "mondrian-cache-cleared.txt";
			File cacheFile = new File(cacheFilePath);
			long limit = System.currentTimeMillis() - mondrianCacheExpiryMillis;
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
				} catch (IOException ex) {
					logger.error("Error", ex);
				}
			}
		}
	}

}
