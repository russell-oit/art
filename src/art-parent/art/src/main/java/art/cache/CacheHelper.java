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
import art.settings.SettingsHelper;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import net.sf.mondrianart.mondrian.olap.CacheControl;
import net.sf.mondrianart.mondrian.rolap.RolapSchema;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	public static final String JPIVOT_CACHE_FILE_NAME = "jpivot-cache-cleared.txt";
	public static final String SAIKU_CACHE_FILE_NAME = "saiku-cache-cleared.txt";

	@Autowired
	private ServletContext servletContext;

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

		updateCacheFile(JPIVOT_CACHE_FILE_NAME);
	}

	/**
	 * Clears mondrian caches used in saiku connections
	 */
	public void clearSaiku() {
		logger.debug("Entering clearSaiku");

		Config.refreshSaikuConnections();

		//https://sourceforge.net/p/art/discussion/352129/thread/4a74b4175f
		//https://forum.reportserver.net/viewtopic.php?id=376
		//https://forum.reportserver.net/viewtopic.php?id=1298
		//https://forum.reportserver.net/viewtopic.php?pid=4048
		//http://www2.datenwerke.net/files/forum/t376/flushMondrianCache.groovy
		List<mondrian.rolap.RolapSchema> schemas = mondrian.rolap.RolapSchema.getRolapSchemas();
		for (mondrian.rolap.RolapSchema schema : schemas) {
			mondrian.olap.CacheControl cacheControl = schema.getInternalConnection().getCacheControl(null);
			cacheControl.flushSchemaCache();
		}

		updateCacheFile(SAIKU_CACHE_FILE_NAME);
	}

	/**
	 * Updates a file to indicate that the mondrian cache has been cleared
	 *
	 * @param fileName the file name
	 */
	private void updateCacheFile(String fileName) {
		logger.debug("Entering updateCacheFile: fileName='{}'", fileName);

		String cacheFilePath = Config.getArtTempPath() + fileName;
		File cacheFile = new File(cacheFilePath);

		try {
			//create/update file that indicates when the cache was last cleared
			FileUtils.writeStringToFile(cacheFile, new Date().toString());
		} catch (IOException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Refreshes application settings
	 *
	 * @param session the http session
	 */
	public void clearSettings(HttpSession session) {
		logger.debug("Entering clearSettings");

		SettingsHelper settingsHelper = new SettingsHelper();
		settingsHelper.refreshSettings(session, servletContext);
	}

	/**
	 * Refreshes custom settings
	 */
	public void clearCustomSettings() {
		Config.initializeCustomSettings(servletContext);
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
	 * Clears the smtp servers cache
	 */
	@CacheEvict(value = "smtpServers", allEntries = true)
	public void clearSmtpServers() {
		logger.debug("Entering clearSmtpServers");
	}

	/**
	 * Clears the roles cache
	 */
	@CacheEvict(value = "roles", allEntries = true)
	public void clearRoles() {
		logger.debug("Entering clearRoles");
	}

	/**
	 * Clears the permissions cache
	 */
	@CacheEvict(value = "permissions", allEntries = true)
	public void clearPermissions() {
		logger.debug("Entering clearPermissions");
	}

	/**
	 * Clears the drilldowns cache
	 */
	@CacheEvict(value = "drilldowns", allEntries = true)
	public void clearDrilldowns() {
		logger.debug("Entering clearDrilldowns");
	}

	/**
	 * Clears the pipelines cache
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void clearPipelines() {
		logger.debug("Entering clearPipelines");
	}
	
	/**
	 * Clears the start conditions cache
	 */
	@CacheEvict(value = "startConditions", allEntries = true)
	public void clearStartConditions() {
		logger.debug("Entering clearStartConditions");
	}

	/**
	 * Clears all caches
	 *
	 * @param session the http session
	 */
	@CacheEvict(value = {"reports", "reportGroups", "users", "userGroups",
		"datasources", "schedules", "jobs", "rules", "parameters",
		"encryptors", "holidays", "destinations", "smtpServers", "roles",
		"permissions", "drilldowns", "pipelines", "startConditions"}, allEntries = true)
	public void clearAll(HttpSession session) {
		logger.debug("Entering clearAll");

		clearJPivot();
		clearSaiku();
		clearSettings(session);
		clearCustomSettings();
	}

}
