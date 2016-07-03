/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
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
package art.cache;

import art.servlets.Config;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import mondrian.olap.CacheControl;
import mondrian.rolap.RolapSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

/**
 * Provides methods for clearing ehcache caches
 *
 * @author Timothy Anyona
 */
@Component
public class CacheHelper {

	private static final Logger logger = LoggerFactory.getLogger(CacheHelper.class);

	/**
	 * Clears all mondrian caches
	 */
	public void clearMondrian() {
		logger.debug("Entering clearMondrian");

		List<RolapSchema> schemas = RolapSchema.getRolapSchemas();
		for (RolapSchema schema : schemas) {
			CacheControl cacheControl = schema.getInternalConnection().getCacheControl(null);
			cacheControl.flushSchemaCache();
		}

		BufferedWriter out = null;
		try {
			//create file that indicates when the cache was last cleared
			String cacheFilePath = Config.getArtTempPath() + "mondrian-cache-cleared.txt";
			out = new BufferedWriter(new FileWriter(cacheFilePath));
			Date now = new Date();
			out.write(now.toString());
		} catch (IOException ex) {
			logger.error("Error", ex);
		} finally {
			//Close the BufferedWriter
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException ex) {
				logger.error("Error while closing writer", ex);
			}
		}
	}

	/**
	 * Clears reports cache
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void clearReports() {
		logger.debug("Entering clearReports");
	}

	/**
	 * Clears report groups cache
	 */
	@CacheEvict(value = "reportGroups", allEntries = true)
	public void clearReportGroups() {
		logger.debug("Entering clearReportGroups");
	}

	/**
	 * Clears users cache
	 */
	@CacheEvict(value = "users", allEntries = true)
	public void clearUsers() {
		logger.debug("Entering clearUsers");
	}

	/**
	 * Clears user groups cache
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void clearUserGroups() {
		logger.debug("Entering clearUserGroups");
	}

	/**
	 * Clears datasources cache
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public void clearDatasources() {
		logger.debug("Entering clearDatasources");
	}

	/**
	 * Clears schedules cache
	 */
	@CacheEvict(value = "schedules", allEntries = true)
	public void clearSchedules() {
		logger.debug("Entering clearSchedules");
	}

	/**
	 * Clears jobs cache
	 */
	@CacheEvict(value = "jobs", allEntries = true)
	public void clearJobs() {
		logger.debug("Entering clearJobs");
	}

	/**
	 * Clears filters cache
	 */
	@CacheEvict(value = "filters", allEntries = true)
	public void clearFilters() {
		logger.debug("Entering clearFilters");
	}
	
	/**
	 * Clears rules cache
	 */
	@CacheEvict(value = "rules", allEntries = true)
	public void clearRules() {
		logger.debug("Entering clearRules");
	}

	/**
	 * Clears parameters cache
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public void clearParameters() {
		logger.debug("Entering clearParameters");
	}
	
	/**
	 * Clears the ftp servers cache
	 */
	@CacheEvict(value = "ftpServers", allEntries = true)
	public void clearFtpServers() {
		logger.debug("Entering clearFtpServers");
	}

	/**
	 * Clears all caches
	 */
	@CacheEvict(value = {"reports", "reportGroups", "users", "userGroups",
		"datasources", "schedules", "jobs", "rules", "parameters", "ftpServers"}, allEntries = true)
	public void clearAll() {
		logger.debug("Entering clearAll");
		
		clearMondrian();
	}

}
