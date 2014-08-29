/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.connection;

import art.dbcp.ArtDBCPDataSource;
import art.servlets.ArtConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for the connection pool status + refresh page
 *
 * @author Timothy Anyona
 */
@Controller
public class ConnectionController {

	@RequestMapping(value = "/app/connections", method = RequestMethod.GET)
	public String showConnections(Model model) {
		model.addAttribute("connectionPoolMap", ArtConfig.getDataSources());
		return "connections";
	}

	@RequestMapping(value = "/app/resetConnection", method = RequestMethod.POST)
	public @ResponseBody
	ResetConnectionResponse resetDatasource(@RequestParam("id") Integer id) {
		ResetConnectionResponse response = new ResetConnectionResponse();

		ArtDBCPDataSource ds = ArtConfig.getDataSource(id);
		if (ds != null) {
			ds.refreshConnections();
			response.setSuccess(true);
			response.setPoolSize(ds.getCurrentPoolSize());
			response.setInUseCount(ds.getTotalInUseCount());
		} else {
			response.setErrorMessage("Connection pool not found: " + id);
		}

		return response;
	}

	@RequestMapping(value = "/app/resetAllConnections", method = RequestMethod.POST)
	public String resetAllConnections(RedirectAttributes redirectAttributes) {
		ArtConfig.refreshConnections();
		redirectAttributes.addFlashAttribute("message", "connections.message.connectionsReset");
		return "redirect:/app/connections.do";
	}

}
