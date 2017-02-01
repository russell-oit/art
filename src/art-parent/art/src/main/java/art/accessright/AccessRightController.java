/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.accessright;

import art.job.JobService;
import art.report.ReportService;
import art.reportgroup.ReportGroupService;
import art.user.User;
import art.user.UserService;
import art.usergroup.UserGroupService;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
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

	@Autowired
	private JobService jobService;

	@RequestMapping(value = "/accessRights", method = RequestMethod.GET)
	public String showAccessRights(Model model) {
		logger.debug("Entering showAccessRights");

		try {
			model.addAttribute("userReportRights", accessRightService.getAllUserReportRights());
			model.addAttribute("userReportGroupRights", accessRightService.getAllUserReportGroupRights());
			model.addAttribute("userJobRights", accessRightService.getAllUserJobRights());
			model.addAttribute("userGroupReportRights", accessRightService.getAllUserGroupReportRights());
			model.addAttribute("userGroupReportGroupRights", accessRightService.getAllUserGroupReportGroupRights());
			model.addAttribute("userGroupJobRights", accessRightService.getAllUserGroupJobRights());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "accessRights";
	}

	@RequestMapping(value = "/accessRightsConfig", method = RequestMethod.GET)
	public String showAccessRightsConfig(Model model, HttpSession session) {
		logger.debug("Entering showAccessRightsConfig");

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			model.addAttribute("users", userService.getAllUsers());
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
			model.addAttribute("reports", reportService.getAllReports());
			model.addAttribute("reportGroups", reportGroupService.getAdminReportGroups(sessionUser));
			model.addAttribute("jobs", jobService.getAllJobs());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "accessRightsConfig";
	}

	@RequestMapping(value = "/deleteAccessRight", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteAccessRight(@RequestParam("id") String id) {
		logger.debug("Entering deleteAccessRight: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <right type>-<user or user group id>-<report, report group or job id>
		String[] values = StringUtils.split(id, "-");
		String rightType = values[0];

		try {
			if (StringUtils.equalsIgnoreCase(rightType, "userReportRight")) {
				accessRightService.deleteUserReportRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			} else if (StringUtils.equalsIgnoreCase(rightType, "userReportGroupRight")) {
				accessRightService.deleteUserReportGroupRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			} else if (StringUtils.equalsIgnoreCase(rightType, "userJobRight")) {
				accessRightService.deleteUserJobRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			} else if (StringUtils.equalsIgnoreCase(rightType, "userGroupReportRight")) {
				accessRightService.deleteUserGroupReportRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			} else if (StringUtils.equalsIgnoreCase(rightType, "userGroupReportGroupRight")) {
				accessRightService.deleteUserGroupReportGroupRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			} else if (StringUtils.equalsIgnoreCase(rightType, "userGroupJobRight")) {
				accessRightService.deleteUserGroupJobRight(NumberUtils.toInt(values[1]), NumberUtils.toInt(values[2]));
			}
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/updateAccessRight", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse updateAccessRight(Model model, @RequestParam("action") String action,
			@RequestParam(value = "users[]", required = false) String[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups,
			@RequestParam(value = "reports[]", required = false) Integer[] reports,
			@RequestParam(value = "reportGroups[]", required = false) Integer[] reportGroups,
			@RequestParam(value = "jobs[]", required = false) Integer[] jobs) {

		//jquery ajax post appends [] to parameter name where data is an array
		//https://stackoverflow.com/questions/17627056/how-to-pass-multiple-request-parameters-in-spring
		logger.debug("Entering updateAccessRight: action='{}'", action);

		AjaxResponse response = new AjaxResponse();

		try {
			accessRightService.updateAccessRights(action, users, userGroups, reports, reportGroups, jobs);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

}
