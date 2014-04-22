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
package art.accessright;

import art.report.ReportService;
import art.reportgroup.ReportGroupService;
import art.user.User;
import art.user.UserService;
import art.usergroup.UserGroupService;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
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
 * Controller for access right configuration and display
 *
 * @author Timothy Anyona
 */
@Controller
public class AccessRightController {

	private static final Logger logger = LoggerFactory.getLogger(AccessRightController.class);

	@Autowired
	private AccessRightService accessRightService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private ReportGroupService reportGroupService;

	@Autowired
	private ReportService reportService;

	@RequestMapping(value = "/app/accessRights", method = RequestMethod.GET)
	public String showAccessRights(Model model) {
		logger.debug("Entering showAccessRights");

		try {
			model.addAttribute("userReportRights", accessRightService.getAllUserReportRights());
			model.addAttribute("userReportGroupRights", accessRightService.getAllUserReportGroupRights());
			model.addAttribute("userGroupReportRights", accessRightService.getAllUserGroupReportRights());
			model.addAttribute("userGroupReportGroupRights", accessRightService.getAllUserGroupReportGroupRights());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "accessRights";
	}

	@RequestMapping(value = "/app/accessRightsConfig", method = RequestMethod.GET)
	public String showAccessRightsConfig(Model model, HttpSession session) {
		logger.debug("Entering showAccessRightsConfig");

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			model.addAttribute("users", userService.getAllUsers());
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
			model.addAttribute("reports", reportService.getAllReports());
			model.addAttribute("reportGroups", reportGroupService.getAdminReportGroups(sessionUser));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "accessRightsConfig";
	}

	@RequestMapping(value = "/app/deleteAccessRight", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteAccessRight(@RequestParam("id") String id) {

		logger.debug("Entering deleteAccessRight: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <right type>-<user or user group id>-<report or report group id>
		String[] values = StringUtils.split(id, "-");

		try {
			if (StringUtils.equalsIgnoreCase(values[0], "userReportRight")) {
				accessRightService.deleteUserReportRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			} else if (StringUtils.equalsIgnoreCase(values[0], "userReportGroupRight")) {
				accessRightService.deleteUserReportGroupRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			} else if (StringUtils.equalsIgnoreCase(values[0], "userGroupReportRight")) {
				accessRightService.deleteUserGroupReportRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			} else if (StringUtils.equalsIgnoreCase(values[0], "userGroupReportGroupRight")) {
				accessRightService.deleteUserGroupReportGroupRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			}
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/updateAccessRight", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse updateAccessRight(Model model, @RequestParam("action") String action,
			@RequestParam(value = "users[]", required = false) String[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups,
			@RequestParam(value = "reports[]", required = false) Integer[] reports,
			@RequestParam(value = "reportGroups[]", required = false) Integer[] reportGroups) {

		//jquery ajax post appends [] to parameter name where data is an array
		//https://stackoverflow.com/questions/17627056/how-to-pass-multiple-request-parameters-in-spring
		logger.debug("Entering updateAccessRight: action='{}'", action);

		AjaxResponse response = new AjaxResponse();

		try {
			accessRightService.updateAccessRights(action, users, userGroups, reports, reportGroups);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

}
