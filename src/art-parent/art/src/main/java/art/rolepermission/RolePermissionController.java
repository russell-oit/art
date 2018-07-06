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
package art.rolepermission;

import art.general.AjaxResponse;
import art.permission.PermissionService;
import art.role.RoleService;
import java.sql.SQLException;
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
 * Controller for role permission configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class RolePermissionController {

	private static final Logger logger = LoggerFactory.getLogger(RolePermissionController.class);

	@Autowired
	private RoleService roleService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RolePermissionService rolePermissionService;

	@RequestMapping(value = "/rolePermissionsConfig", method = RequestMethod.GET)
	public String showRolePermissionsConfig(Model model) {
		logger.debug("Entering showRolePermissionsConfig");

		try {
			model.addAttribute("roles", roleService.getAllRoles());
			model.addAttribute("permissions", permissionService.getAllPermissions());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "rolePermissionsConfig";
	}

	@RequestMapping(value = "/updateRolePermissions", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse updateRolePermissions(Model model, @RequestParam("action") String action,
			@RequestParam("roles[]") Integer[] roles,
			@RequestParam("permissions[]") Integer[] permissions) {

		logger.debug("Entering updateRolePermissions: action='{}'", action);

		AjaxResponse response = new AjaxResponse();

		try {
			rolePermissionService.updateRolePermissions(action, roles, permissions);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}
	
	@RequestMapping(value = "/deleteRolePermission", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteRolePermission(@RequestParam("id") String id) {
		logger.debug("Entering deleteRolePermission: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <role id>-<permission id>
		String[] values = StringUtils.split(id, "-");

		try {
			rolePermissionService.deleteRolePermission(NumberUtils.toInt(values[0]), NumberUtils.toInt(values[1]));
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

}
