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
package art.user;

import art.enums.AccessLevel;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupService;
import art.utils.Encrypter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring controller for add and edit user processes. Use separate controller in
 * order to use modelattribute methods to conveniently add reference data used
 * in lists.
 *
 * @author Timothy Anyona
 */
@Controller
public class SaveUserController {

	private static final Logger logger = LoggerFactory.getLogger(SaveUserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private ReportGroupService reportGroupService;

	@ModelAttribute("userGroups")
	public List<UserGroup> addUserGroups(Model model) {
		try {
			return userGroupService.getAllUserGroups();
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return null;
	}

	@ModelAttribute("reportGroups")
	public List<ReportGroup> addReportGroups(Model model) {
		try {
			return reportGroupService.getAllReportGroups();
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return null;
	}

	@ModelAttribute("accessLevels")
	public List<AccessLevel> addAccessLevels(HttpSession session) {
		List<AccessLevel> levels = new ArrayList<AccessLevel>();

		//add only relevant levels according to the session user access level
		//to ensure admin can't give himself a higher level
		levels.add(AccessLevel.NormalUser);
		levels.add(AccessLevel.ScheduleUser);
		levels.add(AccessLevel.JuniorAdmin);
		levels.add(AccessLevel.MidAdmin);
		levels.add(AccessLevel.StandardAdmin);

		//only standard admins and above and the repository user can edit users
		User sessionUser = (User) session.getAttribute("sessionUser");
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

	@RequestMapping(value = "/app/addUser", method = RequestMethod.GET)
	public String showAddUser(Model model) {
		User user = new User();

		//set default properties for new users
		user.setActive(true);
		user.setCanChangePassword(true);

		model.addAttribute("user", user);
		model.addAttribute("action", "add");
		return "editUser";
	}

	@RequestMapping(value = "/app/addUser", method = RequestMethod.POST)
	public String processAddUser(@ModelAttribute("user") @Valid User user,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			model.addAttribute("action", "add");
			return "editUser";
		}

		//hash new password
		hashNewPassword(user, user.getPassword());

		try {
			userService.addUser(user);
			redirectAttributes.addFlashAttribute("message", "users.message.userAdded");
			return "redirect:/app/users.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", "add");
		return "editUser";
	}

	@RequestMapping(value = "/app/editUser", method = RequestMethod.GET)
	public String showEditUser(@RequestParam("userId") Integer userId, Model model,
			HttpSession session) {

		User user = null;

		try {
			user = userService.getUser(userId);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		//ensure an admin cannot edit admins of higher access level than himself
		User sessionUser = (User) session.getAttribute("sessionUser");
		if (!canEditUser(sessionUser, user)) {
			return "accessDenied";
		}

		model.addAttribute("user", user);
		model.addAttribute("action", "edit");
		return "editUser";
	}

	@RequestMapping(value = "/app/editUser", method = RequestMethod.POST)
	public String processEditUser(@ModelAttribute("user") @Valid User user,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		//ensure an admin cannot edit admins of higher access level than himself
		User sessionUser = (User) session.getAttribute("sessionUser");
		if (!canEditUser(sessionUser, user)) {
			return "accessDenied";
		}

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			model.addAttribute("action", "edit");
			return "editUser";
		}

		//set password as appropriate
		boolean useCurrentPassword = false;
		String newPassword = user.getPassword();
		if (user.isUseBlankPassword()) {
			newPassword = "";
		} else {
			if (StringUtils.isEmpty(newPassword)) {
				//password field blank. use current password
				useCurrentPassword = true;
			}
		}

		if (useCurrentPassword) {
			try {
				//password field blank. use current password
				User currentUser = userService.getUser(user.getUserId());
				user.setPassword(currentUser.getPassword());
				user.setPasswordAlgorithm(currentUser.getPasswordAlgorithm());
			} catch (SQLException ex) {
				logger.error("Error", ex);
			}
		} else {
			//hash new password
			hashNewPassword(user, newPassword);
		}

		try {
			userService.updateUser(user);
			redirectAttributes.addFlashAttribute("message", "users.message.userUpdated");
			return "redirect:/app/users.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", "edit");
		return "editUser";
	}

	/**
	 * Determine if the session user can edit a given user
	 *
	 * @param sessionUser
	 * @param editUser
	 * @return
	 */
	private boolean canEditUser(User sessionUser, User editUser) {
		if (sessionUser == null || editUser == null) {
			return false;
		}

		if (sessionUser.getAccessLevel().getValue() > editUser.getAccessLevel().getValue()
				|| sessionUser.getAccessLevel() == AccessLevel.SuperAdmin
				|| sessionUser.getAccessLevel() == AccessLevel.RepositoryUser) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Hash and set a new password for a user
	 * 
	 * @param user
	 * @param newPassword 
	 */
	private void hashNewPassword(User user, String newPassword) {
		user.setPassword(Encrypter.HashPasswordBcrypt(newPassword));
		user.setPasswordAlgorithm("bcrypt");
	}

}
