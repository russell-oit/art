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
package art.adminright;

import art.datasource.DatasourceService;
import art.reportgroup.ReportGroupService;
import art.user.UserService;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
 * Controller for admin rights configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class AdminRightController {

	private static final Logger logger = LoggerFactory.getLogger(AdminRightController.class);

	@Autowired
	private AdminRightService adminRightService;

	@Autowired
	private UserService userService;

	@Autowired
	private DatasourceService datasourceService;

	@Autowired
	private ReportGroupService reportGroupService;

	@RequestMapping(value = "/app/adminRights", method = RequestMethod.GET)
	public String showAdminRights(Model model) {
		logger.debug("Entering showAdminRights");

		try {
			model.addAttribute("datasourceRights", adminRightService.getAllAdminDatasourceRights());
			model.addAttribute("reportGroupRights", adminRightService.getAllAdminReportGroupRights());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "adminRights";
	}

	@RequestMapping(value = "/app/adminRightsConfig", method = RequestMethod.GET)
	public String showAdminRightsConfig(Model model) {
		logger.debug("Entering showAdminRightsConfig");

		try {
			model.addAttribute("admins", userService.getAdminUsers());
			model.addAttribute("datasources", datasourceService.getAllDatasources());
			model.addAttribute("reportGroups", reportGroupService.getAllReportGroups());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "adminRightsConfig";
	}

	@RequestMapping(value = "/app/deleteAdminRight", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteAdminRight(@RequestParam("id") String id) {

		logger.debug("Entering deleteAdminRight: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <right type>-<admin user id>-<datasource or report group id>
		String[] values = StringUtils.split(id, "-");

		try {
			if (StringUtils.equalsIgnoreCase(values[0], "datasourceRight")) {
				adminRightService.deleteAdminDatasourceRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			} else if (StringUtils.equalsIgnoreCase(values[0], "reportGroupRight")) {
				adminRightService.deleteAdminReportGroupRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			}
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/updateAdminRight", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse updateAdminRight(Model model, @RequestParam("action") String action,
			@RequestParam("admins[]") String[] admins,
			@RequestParam(value = "datasources[]", required = false) Integer[] datasources,
			@RequestParam(value = "reportGroups[]", required = false) Integer[] reportGroups) {

		//jquery ajax post appends [] to parameter name where data is an array
		//https://stackoverflow.com/questions/17627056/how-to-pass-multiple-request-parameters-in-spring
		logger.debug("Entering updateAdminRight: action='{}'", action);

		AjaxResponse response = new AjaxResponse();

		try {
			adminRightService.updateAdminRights(action, admins, datasources, reportGroups);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

}
