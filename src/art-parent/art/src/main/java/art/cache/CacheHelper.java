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
package art.cache;

import art.servlets.Config;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import net.sf.mondrianart.mondrian.olap.CacheControl;
import net.sf.mondrianart.mondrian.rolap.RolapSchema;
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
	 * Clears mondrian caches used by jpivot
	 */
	public void clearJPivot() {
		logger.debug("Entering clearJPivot");

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
	 * Clears mondrian caches used in saiku connections
	 */
	public void clearSaiku(){
		logger.debug("Entering clearSaiku");
		Config.refreshSaikuConnections();
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
	 * Clears the encryptors cache
	 */
	@CacheEvict(value = "encryptors", allEntries = true)
	public void clearEncryptors() {
		logger.debug("Entering clearEncryptors");
	}
	
	/**
	 * Clears the holidays cache
	 */
	@CacheEvict(value = "holidays", allEntries = true)
	public void clearHolidays() {
		logger.debug("Entering clearHolidays");
	}
	
	/**
	 * Clears the destinations cache
	 */
	@CacheEvict(value = "destinations", allEntries = true)
	public void clearDestinations() {
		logger.debug("Entering clearDestinations");
	}
	
	/**
	 * Clears all caches
	 */
	@CacheEvict(value = {"reports", "reportGroups", "users", "userGroups",
		"datasources", "schedules", "jobs", "rules", "parameters",
		"encryptors", "holidays", "destinations"}, allEntries = true)
	public void clearAll() {
		logger.debug("Entering clearAll");
		
		clearJPivot();
		clearSaiku();
	}

}
