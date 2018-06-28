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
package art.userpermission;

import art.general.AjaxResponse;
import art.user.UserService;
import java.sql.SQLException;
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
 * Controller for user permission display
 *
 * @author Timothy Anyona
 */
@Controller
public class UserPermissionController {

	private static final Logger logger = LoggerFactory.getLogger(UserPermissionController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private UserPermissionService userPermissionService;

	@GetMapping("/userPermissions")
	public String showUserPermissions(Model model, @RequestParam("userId") Integer userId) {
		logger.debug("Entering showUserPermissions: userId={}", userId);

		try {
			model.addAttribute("user", userService.getUser(userId));
			model.addAttribute("userPermissions", userPermissionService.getUserPermissionsForUser(userId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "userPermissions";
	}

	@RequestMapping(value = "/deleteUserPermission", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteUserPermission(@RequestParam("id") String id) {
		logger.debug("Entering deleteUserPermission: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <user id>-<permission id>
		String[] values = StringUtils.split(id, "-");

		try {
			userPermissionService.deleteUserPermission(NumberUtils.toInt(values[0]), NumberUtils.toInt(values[1]));
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

}
