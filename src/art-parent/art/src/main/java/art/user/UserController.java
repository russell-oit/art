package art.user;

import art.enums.AccessLevel;
import art.reportgroup.ReportGroupService;
import art.usergroup.UserGroupService;
import art.utils.AjaxResponse;
import art.utils.Encrypter;
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
 * Spring controller for the user configuration process
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

	@RequestMapping(value = "/app/users", method = RequestMethod.GET)
	public String showUsers(Model model) {
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
	AjaxResponse deleteUser(@RequestParam("id") Integer userId) {
		//object will be automatically converted to json
		//see http://www.mkyong.com/spring-mvc/spring-3-mvc-and-json-example/
		
		AjaxResponse response = new AjaxResponse();

		try {
			userService.deleteUser(userId);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addUser", method = RequestMethod.GET)
	public String showAddUser(Model model, HttpSession session) {
		User user = new User();

		//set default properties for new users
		user.setActive(true);
		user.setCanChangePassword(true);

		model.addAttribute("user", user);
		return prepareEditUser("add", model, session);
	}

	@RequestMapping(value = "/app/addUser", method = RequestMethod.POST)
	public String processAddUser(@ModelAttribute("user") @Valid User user,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return prepareEditUser("add", model, session);
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

		return prepareEditUser("add", model, session);
	}

	@RequestMapping(value = "/app/editUser", method = RequestMethod.GET)
	public String showEditUser(@RequestParam("id") Integer userId, Model model,
			HttpSession session) {

		User user = null;

		try {
			user = userService.getUser(userId);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		//ensure an admin cannot edit admins of higher access level than himself
		if (!canEditUser(session, user)) {
			return "accessDenied";
		}

		model.addAttribute("user", user);
		return prepareEditUser("edit", model, session);
	}

	@RequestMapping(value = "/app/editUser", method = RequestMethod.POST)
	public String processEditUser(@ModelAttribute("user") @Valid User user,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		//ensure an admin cannot edit admins of higher access level than himself
		if (!canEditUser(session, user)) {
			return "accessDenied";
		}

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return prepareEditUser("edit", model, session);
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
				model.addAttribute("error", ex);
				return prepareEditUser("edit", model, session);
			}
		} else {
			//hash new password
			hashNewPassword(user, newPassword);
		}

		try {
			userService.updateUser(user);
			//update session user if appropriate
			User sessionUser = (User) session.getAttribute("sessionUser");
			if(user.equals(sessionUser)){
				session.removeAttribute("sessionUser");
				session.setAttribute("sessionUser", userService.getUser(user.getUserId()));
			}
			redirectAttributes.addFlashAttribute("message", "users.message.userUpdated");
			return "redirect:/app/users.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return prepareEditUser("edit", model, session);
	}

	/**
	 * Prepare model data for edit user page and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @param session
	 * @return
	 */
	private String prepareEditUser(String action, Model model, HttpSession session) {
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
	 * Get relevant access levels to be used in the edit user page
	 *
	 * @param session
	 * @return
	 */
	private List<AccessLevel> getAccessLevels(HttpSession session) {
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

	/**
	 * Determine if the session user can edit a given user
	 *
	 * @param sessionUser
	 * @param editUser
	 * @return
	 */
	private boolean canEditUser(HttpSession session, User editUser) {
		User sessionUser = (User) session.getAttribute("sessionUser");

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
