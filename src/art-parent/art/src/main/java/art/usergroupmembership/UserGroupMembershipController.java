/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.usergroupmembership;

import art.user.UserService;
import art.usergroup.UserGroupService;
import art.utils.AjaxResponse;
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
 * Controller for user group membership configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class UserGroupMembershipController {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupMembershipController.class);

	@Autowired
	private UserGroupMembershipService userGroupMembershipService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	@RequestMapping(value = "/app/userGroupMembership", method = RequestMethod.GET)
	public String showUserGroupMembership(Model model) {
		logger.debug("Entering showUserGroupMembership");

		try {
			model.addAttribute("memberships", userGroupMembershipService.getAllUserGroupMemberships());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "userGroupMembership";
	}

	@RequestMapping(value = "/app/userGroupMembershipConfig", method = RequestMethod.GET)
	public String showUserGroupMembershipConfig(Model model) {
		logger.debug("Entering showUserGroupMembershipConfig");

		try {
			model.addAttribute("users", userService.getAllUsers());
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "userGroupMembershipConfig";
	}

	@RequestMapping(value = "/app/deleteUserGroupMembership", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteUserGroupMembership(@RequestParam("id") String id) {

		logger.debug("Entering deleteUserGroupMembership: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <user id>-<user group id>
		String[] values = StringUtils.split(id, "-");
		int userId = NumberUtils.toInt(values[0]);
		int userGroupId = NumberUtils.toInt(values[1]);

		logger.debug("userId={}", userId);
		logger.debug("userGroupId={}", userGroupId);

		try {
			userGroupMembershipService.deleteUserGroupMembership(userId, userGroupId);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/updateUserGroupMembership", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse updateUserGroupMembership(Model model, @RequestParam("action") String action,
			@RequestParam("users[]") String[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups) {

		//jquery ajax post appends [] to parameter name where data is an array
		//https://stackoverflow.com/questions/17627056/how-to-pass-multiple-request-parameters-in-spring
		logger.debug("Entering updateUserGroupMembership: action='{}'", action);

		AjaxResponse response = new AjaxResponse();

		try {
			userGroupMembershipService.updateUserGroupMembership(action, users, userGroups);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}
}
