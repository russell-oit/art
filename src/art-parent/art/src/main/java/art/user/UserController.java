/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.user;

import art.encryption.PasswordUtils;
import art.enums.AccessLevel;
import art.reportgroup.ReportGroupService;
import art.usergroup.UserGroupService;
import art.usergroupmembership.UserGroupMembershipService;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for the user configuration process
 *
 * @author Timothy Anyona
 */
@Controller
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private ReportGroupService reportGroupService;

	@Autowired
	private UserGroupMembershipService userGroupMembershipService;

	@RequestMapping(value = "/app/users", method = RequestMethod.GET)
	public String showUsers(Model model) {
		logger.debug("Entering showUsers");

		try {
			model.addAttribute("users", userService.getAllUsers());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "users";
	}

	@RequestMapping(value = "/app/deleteUser", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteUser(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteUser: id={}", id);

		//object will be automatically converted to json
		//see http://www.mkyong.com/spring-mvc/spring-3-mvc-and-json-example/
		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = userService.deleteUser(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//user not deleted because of linked jobs
				response.setData(deleteResult.getData());
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/deleteUsers", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteUsers(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteUsers: ids={}", (Object) ids);

		//object will be automatically converted to json
		//see http://www.mkyong.com/spring-mvc/spring-3-mvc-and-json-example/
		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = userService.deleteUsers(ids);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				response.setData(deleteResult.getData());
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addUser", method = RequestMethod.GET)
	public String addUser(Model model, HttpSession session) {
		logger.debug("Entering addUser");

		User user = new User();

		//set default properties for new users
		user.setActive(true);
		user.setCanChangePassword(true);

		model.addAttribute("user", user);
		
		return showEditUser("add", model, session);
	}

	@RequestMapping(value = "/app/editUser", method = RequestMethod.GET)
	public String editUser(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering editUser: id={}", id);

		User user = null;

		try {
			user = userService.getUser(id);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		//ensure an admin cannot edit admins of higher access level than himself
		if (!canEditUser(session, user)) {
			return "accessDenied";
		}

		model.addAttribute("user", user);
		return showEditUser("edit", model, session);
	}

	@RequestMapping(value = "/app/editUsers", method = RequestMethod.GET)
	public String editUsers(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editUsers: ids={}", ids);

		String[] singleIds = StringUtils.split(ids, ",");

		for (String singleId : singleIds) {
			try {
				int id = Integer.parseInt(singleId);
				User user = userService.getUser(id);
				//ensure an admin cannot edit admins of higher access level than himself
				if (!canEditUser(session, user)) {
					return "accessDenied";
				}
			} catch (SQLException ex) {
				logger.error("Error", ex);
				model.addAttribute("error", ex);
			}
		}

		MultipleUserEdit multipleUserEdit = new MultipleUserEdit();
		multipleUserEdit.setIds(ids);

		model.addAttribute("multipleUserEdit", multipleUserEdit);
		return "editUsers";
	}

	@RequestMapping(value = "/app/saveUser", method = RequestMethod.POST)
	public String saveUser(@ModelAttribute("user") @Valid User user,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveUser: user={}, action='{}'", user, action);

		if (StringUtils.equals(action, "edit")) {
			//ensure an admin cannot edit admins of higher access level than himself
			if (!canEditUser(session, user)) {
				return "accessDenied";
			}
		}

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUser(action, model, session);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPassword(user, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showEditUser(action, model, session);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");

			if (StringUtils.equals(action, "add")) {
				userService.addUser(user, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				userService.updateUser(user, sessionUser);
				//update session user if appropriate
				logger.debug("user.getUserId()={}", user.getUserId());
				logger.debug("sessionUser.getUserId()={}", sessionUser.getUserId());
				if (user.getUserId() == sessionUser.getUserId()) {
					session.removeAttribute("sessionUser");
					session.setAttribute("sessionUser", userService.getUser(user.getUserId()));
				}
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", user.getUsername());

			saveUserGroups(user);
			
			return "redirect:/app/users.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUser(action, model, session);
	}

	/**
	 * Save user groups for the given user
	 * 
	 * @param user the user
	 * @throws SQLException 
	 */
	private void saveUserGroups(User user) throws SQLException {
		userGroupMembershipService.deleteAllUserGroupMembershipsForUser(user.getUserId());
		userGroupMembershipService.addUserGroupMemberships(user, user.getUserGroups());
	}

	@RequestMapping(value = "/app/saveUsers", method = RequestMethod.POST)
	public String saveUsers(@ModelAttribute("multipleUserEdit") @Valid MultipleUserEdit multipleUserEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveUsers: multipleUserEdit={}", multipleUserEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUsers();
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			userService.updateUsers(multipleUserEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleUserEdit.getIds());
			return "redirect:/app/users.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUsers();
	}

	/**
	 * Prepares model data for edit user page and returns the jsp file to
	 * display
	 *
	 * @param action the action. "add" or "edit"
	 * @param model the model to use
	 * @param session the http session
	 * @return returns the jsp file to display
	 */
	private String showEditUser(String action, Model model, HttpSession session) {
		logger.debug("Entering showEditUser: action='{}'", action);

		try {
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
			model.addAttribute("reportGroups", reportGroupService.getAllReportGroups());
			model.addAttribute("accessLevels", getAccessLevels(session));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);
		return "editUser";
	}

	/**
	 * Prepares model data for edit users page and returns the jsp file to
	 * display
	 *
	 * @return returns the jsp file to display
	 */
	private String showEditUsers() {
		logger.debug("Entering showEditUsers");

		return "editUsers";
	}

	/**
	 * Returns the relevant access levels to be used in the edit user page
	 *
	 * @param session the http session
	 * @return the access levels to be used
	 */
	private List<AccessLevel> getAccessLevels(HttpSession session) {
		logger.debug("Entering getAccessLevels");

		List<AccessLevel> levels = new ArrayList<>();

		//add only relevant levels according to the session user access level
		//to ensure admin can't give himself a higher level
		levels.add(AccessLevel.NormalUser);
		levels.add(AccessLevel.ScheduleUser);
		levels.add(AccessLevel.JuniorAdmin);
		levels.add(AccessLevel.MidAdmin);
		levels.add(AccessLevel.StandardAdmin);

		//only standard admins and above and the repository user can edit users
		User sessionUser = (User) session.getAttribute("sessionUser");
		logger.debug("sessionUser.getAccessLevel().getValue()={}", sessionUser.getAccessLevel().getValue());
		if (sessionUser.getAccessLevel().getValue() >= AccessLevel.SeniorAdmin.getValue()
				|| sessionUser.getAccessLevel() == AccessLevel.RepositoryUser) {
			levels.add(AccessLevel.SeniorAdmin);
		}
		if (sessionUser.getAccessLevel().getValue() >= AccessLevel.SuperAdmin.getValue()
				|| sessionUser.getAccessLevel() == AccessLevel.RepositoryUser) {
			levels.add(AccessLevel.SuperAdmin);
		}

		return levels;
	}

	/**
	 * Returns <code>true</code> if the session user can edit a given user
	 *
	 * @param sessionUser the session user
	 * @param editUser the user to edit
	 * @return <code>true</code> if the session user can edit the given user
	 */
	private boolean canEditUser(HttpSession session, User editUser) {
		logger.debug("Entering canEditUser");

		User sessionUser = (User) session.getAttribute("sessionUser");

		logger.debug("sessionUser={}", sessionUser);
		logger.debug("editUser={}", editUser);
		if (sessionUser == null || editUser == null) {
			return false;
		}

		logger.debug("sessionUser.getAccessLevel().getValue()={}", sessionUser.getAccessLevel().getValue());
		logger.debug("editUser.getAccessLevel().getValue()={}", editUser.getAccessLevel().getValue());
		if (sessionUser.getAccessLevel().getValue() > editUser.getAccessLevel().getValue()
				|| sessionUser.getAccessLevel() == AccessLevel.SuperAdmin
				|| sessionUser.getAccessLevel() == AccessLevel.RepositoryUser) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Encrypts the password for the given user
	 *
	 * @param user the user, containing the new password to be used
	 * @param action the action being taken. "add" or "edit"
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setPassword(User user, String action) throws SQLException {
		logger.debug("Entering setPassword: user={}, action='{}'", user, action);

		boolean useCurrentPassword = false;
		String newPassword = user.getPassword();

		logger.debug("user.isUseBlankPassword()={}", user.isUseBlankPassword());
		if (user.isUseBlankPassword()) {
			newPassword = "";
		} else {
			logger.debug("StringUtils.isEmpty(newPassword)={}", StringUtils.isEmpty(newPassword));
			if (StringUtils.isEmpty(newPassword) && StringUtils.equals(action, "edit")) {
				//password field blank. use current password
				useCurrentPassword = true;
			}
		}

		logger.debug("useCurrentPassword={}", useCurrentPassword);
		if (useCurrentPassword) {
			//password field blank. use current password
			User currentUser = userService.getUser(user.getUserId());
			logger.debug("currentUser={}", currentUser);
			if (currentUser == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				user.setPassword(currentUser.getPassword());
				user.setPasswordAlgorithm(currentUser.getPasswordAlgorithm());
			}
		} else {
			//hash new password
			user.setPassword(PasswordUtils.HashPasswordBcrypt(newPassword));
			user.setPasswordAlgorithm("bcrypt");
		}

		return null;
	}
}
