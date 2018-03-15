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

import art.enums.CacheType;
import art.utils.AjaxResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for cache configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class CacheController {

	private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

	@Autowired
	private CacheHelper cacheHelper;

	@RequestMapping(value = "/caches", method = RequestMethod.GET)
	public String showCaches(Model model) {
		logger.debug("Entering showCaches");

		model.addAttribute("caches", CacheType.list());

		return "caches";
	}

	@RequestMapping(value = "/clearAllCaches", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse clearAllCaches(HttpSession session) {
		logger.debug("Entering clearAllCaches");

		AjaxResponse response = new AjaxResponse();

		cacheHelper.clearAll(session);

		response.setSuccess(true);

		return response;
	}

	@RequestMapping(value = "/clearCache", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse clearCache(@RequestParam("id") String id, HttpSession session) {
		logger.debug("Entering clearCache: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		String message = null;
		CacheType cacheType = CacheType.toEnum(id);

		if (cacheType == null) {
			message = "Unknown cache: " + id;
		} else {
			switch (cacheType) {
				case JPivot:
					cacheHelper.clearJPivot();
					break;
				case Saiku:
					cacheHelper.clearSaiku();
					break;
				case Reports:
					cacheHelper.clearReports();
					break;
				case ReportGroups:
					cacheHelper.clearReportGroups();
					break;
				case Users:
					cacheHelper.clearUsers();
					break;
				case UserGroups:
					cacheHelper.clearUserGroups();
					break;
				case Datasources:
					cacheHelper.clearDatasources();
					break;
				case Schedules:
					cacheHelper.clearSchedules();
					break;
				case Jobs:
					cacheHelper.clearJobs();
					break;
				case Rules:
					cacheHelper.clearRules();
					break;
				case Parameters:
					cacheHelper.clearParameters();
					break;
				case Encryptors:
					cacheHelper.clearEncryptors();
					break;
				case Holidays:
					cacheHelper.clearHolidays();
					break;
				case Destinations:
					cacheHelper.clearDestinations();
					break;
				case SmtpServers:
					cacheHelper.clearSmtpServers();
					break;
				case Settings:
					cacheHelper.clearSettings(session);
					break;
				case CustomSettings:
					cacheHelper.clearCustomSettings();
					break;
				default:
					message = "Clear cache not available: " + id;
			}
		}

		if (message == null) {
			response.setSuccess(true);
		} else {
			logger.info(message);
			response.setErrorMessage(message);
		}

		return response;
	}

}
