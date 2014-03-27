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
			userService.deleteUser(id);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addUser", method = RequestMethod.GET)
	public String addUserGet(Model model, HttpSession session) {
		logger.debug("Entering addUserGet");

		User user = new User();

		//set default properties for new users
		user.setActive(true);
		user.setCanChangePassword(true);

		model.addAttribute("user", user);
		return showUser("add", model, session);
	}

	@RequestMapping(value = "/app/addUser", method = RequestMethod.POST)
	public String addUserPost(@ModelAttribute("user") @Valid User user,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering addUserPost: user={}", user);

		String action = "add";

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showUser(action, model, session);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPassword(user, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showUser("add", model, session);
			}

			userService.addUser(user);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			redirectAttributes.addFlashAttribute("recordName", user.getUsername());
			return "redirect:/app/users.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showUser("add", model, session);
	}

	@RequestMapping(value = "/app/editUser", method = RequestMethod.GET)
	public String editUserGet(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering editUserGet: id={}", id);

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
		return showUser("edit", model, session);
	}

	@RequestMapping(value = "/app/editUser", method = RequestMethod.POST)
	public String editUserPost(@ModelAttribute("user") @Valid User user,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering editUserPost: user={}", user);

		//ensure an admin cannot edit admins of higher access level than himself
		if (!canEditUser(session, user)) {
			return "accessDenied";
		}

		String action = "edit";

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showUser(action, model, session);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPassword(user, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showUser("edit", model, session);
			}

			userService.updateUser(user);
			//update session user if appropriate
			User sessionUser = (User) session.getAttribute("sessionUser");
			logger.debug("user.getUserId()={}", user.getUserId());
			logger.debug("sessionUser.getUserId()={}", sessionUser.getUserId());
			if (user.getUserId() == sessionUser.getUserId()) {
				session.removeAttribute("sessionUser");
				session.setAttribute("sessionUser", userService.getUser(user.getUserId()));
			}
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			redirectAttributes.addFlashAttribute("recordName", user.getUsername());
			return "redirect:/app/users.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showUser(action, model, session);
	}

	/**
	 * Prepare model data for edit user page and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @param session
	 * @return
	 */
	private String showUser(String action, Model model, HttpSession session) {
		logger.debug("Entering showUser: action='{}'", action);

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
	 * Determine if the session user can edit a given user
	 *
	 * @param sessionUser
	 * @param editUser
	 * @return
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
	 * Set password
	 *
	 * @param user
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
			user.setPassword(Encrypter.HashPasswordBcrypt(newPassword));
			user.setPasswordAlgorithm("bcrypt");
		}

		return null;
	}
}
