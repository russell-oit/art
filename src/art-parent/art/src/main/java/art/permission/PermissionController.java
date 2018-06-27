/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.permission;

import art.general.AjaxResponse;
import art.role.RoleService;
import art.report.ReportService;
import art.user.User;
import art.user.UserService;
import art.usergroup.UserGroupService;
import java.sql.SQLException;
import java.util.Locale;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for permission configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class PermissionController {

	private static final Logger logger = LoggerFactory.getLogger(PermissionController.class);

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private RoleService roleService;

	@RequestMapping(value = "/permissionsConfig", method = RequestMethod.GET)
	public String showPermissionsConfig(Model model) {
		logger.debug("Entering showPermissionsConfig");

		try {
			model.addAttribute("users", userService.getAllUsers());
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
			model.addAttribute("roles", roleService.getAllRoles());
			model.addAttribute("permissions", permissionService.getAllPermissions());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "permissionsConfig";
	}

	@RequestMapping(value = "/updatePermissions", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse updatePermissions(Model model, @RequestParam("action") String action,
			@RequestParam(value = "users[]", required = false) Integer[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups,
			@RequestParam(value = "roles[]", required = false) Integer[] roles,
			@RequestParam(value = "permissions[]", required = false) Integer[] permissions) {

		logger.debug("Entering updatePermissions: action='{}'", action);

		AjaxResponse response = new AjaxResponse();

		try {
			permissionService.updatePermissions(action, users, userGroups, roles, permissions);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

//	@GetMapping("/userPermissions")
//	public String showUserPermissions(Model model, @RequestParam("userId") Integer userId) {
//		logger.debug("Entering showUserPermissions: userId={}", userId);
//
//		try {
//			model.addAttribute("user", userService.getUser(userId));
//			model.addAttribute("userReportRights", permissionService.getUserReportRightsForUser(userId));
//			model.addAttribute("userPermissionRights", permissionService.getUserPermissionRightsForUser(userId));
//			model.addAttribute("userRoleRights", permissionService.getUserRoleRightsForUser(userId));
//		} catch (SQLException | RuntimeException ex) {
//			logger.error("Error", ex);
//			model.addAttribute("error", ex);
//		}
//
//		return "userPermissions";
//	}
//
//	@GetMapping("/userGroupPermissions")
//	public String showUserGroupPermissions(Model model,
//			@RequestParam("userGroupId") Integer userGroupId) {
//
//		logger.debug("Entering showUserGroupPermissions: userGroupId={}", userGroupId);
//
//		try {
//			model.addAttribute("userGroup", userGroupService.getUserGroup(userGroupId));
//			model.addAttribute("userGroupReportRights", permissionService.getUserGroupReportRightsForUserGroup(userGroupId));
//			model.addAttribute("userGroupPermissionRights", permissionService.getUserGroupPermissionRightsForUserGroup(userGroupId));
//			model.addAttribute("userGroupRoleRights", permissionService.getUserGroupRoleRightsForUserGroup(userGroupId));
//		} catch (SQLException | RuntimeException ex) {
//			logger.error("Error", ex);
//			model.addAttribute("error", ex);
//		}
//
//		return "userGroupPermissions";
//	}

}
