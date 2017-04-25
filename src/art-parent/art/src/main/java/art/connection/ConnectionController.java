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
package art.connection;

import art.connectionpool.DbConnections;
import art.enums.ConnectionPoolLibrary;
import art.servlets.Config;
import art.utils.AjaxResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for the connection pool status page
 *
 * @author Timothy Anyona
 */
@Controller
public class ConnectionController {

	private static final Logger logger = LoggerFactory.getLogger(ConnectionController.class);

	@RequestMapping(value = "/connections", method = RequestMethod.GET)
	public String showConnections(Model model) {
		logger.debug("Entering showConnections");

		model.addAttribute("connectionPoolDetails", DbConnections.getAllConnectionPoolDetails());
		ConnectionPoolLibrary connectionPoolLibrary = Config.getArtDbConfig().getConnectionPoolLibrary();
		if (connectionPoolLibrary == ConnectionPoolLibrary.ArtDBCP) {
			model.addAttribute("usingArtDBCPConnectionPoolLibrary", true);
		}
		return "connections";
	}

	@RequestMapping(value = "/refreshConnectionPool", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse refreshConnectionPool(@RequestParam("id") Integer datasourceId) {
		logger.debug("Entering refreshConnectionPool: id={}", datasourceId);

		AjaxResponse response = new AjaxResponse();

		DbConnections.refreshConnectionPool(datasourceId);
		response.setData(DbConnections.getConnectionPoolDetails(datasourceId));

		response.setSuccess(true);
		
		return response;
	}
}
