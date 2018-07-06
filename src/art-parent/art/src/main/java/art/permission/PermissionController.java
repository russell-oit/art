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
import art.rolepermission.RolePermissionService;
import art.user.UserService;
import art.usergroup.UserGroupService;
import art.usergrouppermission.UserGroupPermissionService;
import art.userpermission.UserPermissionService;
import java.sql.SQLException;
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
	
	@Autowired
	private UserPermissionService userPermissionService;
	
	@Autowired
	private UserGroupPermissionService userGroupPermissionService;
	
	@Autowired
	private RolePermissionService rolePermissionService;

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

	@RequestMapping(value = "/permissions", method = RequestMethod.GET)
	public String showPermissions(Model model) {
		logger.debug("Entering showPermissions");

		try {
			model.addAttribute("permissions", permissionService.getAllPermissions());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "permissions";
	}

	@RequestMapping(value = "/permissionUsage", method = RequestMethod.GET)
	public String showPermissionUsage(Model model,
			@RequestParam("permissionId") Integer permissionId) {

		logger.debug("Entering showPermissionUsage");

		try {
			model.addAttribute("permission", permissionService.getPermission(permissionId));
			model.addAttribute("userPermissions", userPermissionService.getUserPermissionsForPermission(permissionId));
			model.addAttribute("userGroupPermissions", userGroupPermissionService.getUserGroupPermissionsForPermission(permissionId));
			model.addAttribute("rolePermissions", rolePermissionService.getRolePermissionsForPermission(permissionId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "permissionUsage";
	}

}
