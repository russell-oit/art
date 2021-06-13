/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.user;

import art.encryption.PasswordUtils;
import art.mail.Mailer;
import art.reportgroup.ReportGroupService;
import art.servlets.Config;
import art.usergroup.UserGroupService;
import art.usergroupmembership.UserGroupMembershipService2;
import art.general.ActionResult;
import art.general.AjaxResponse;
import art.role.RoleService;
import art.utils.AjaxTableHelper;
import art.utils.MailService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.mail.MessagingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;

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
	private UserGroupMembershipService2 userGroupMembershipService2;

	@Autowired
	private TemplateEngine defaultTemplateEngine;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private RoleService roleService;

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private MailService mailService;

	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public String showUsers(Model model) {
		logger.debug("Entering showUsers");

		return "users";
	}

	@GetMapping("/getUsers")
	public @ResponseBody
	AjaxResponse getUsers(Locale locale, HttpServletRequest request,
			HttpServletResponse httpResponse, HttpSession session) {

		logger.debug("Entering getUsers");

		AjaxResponse ajaxResponse = new AjaxResponse();

		try {
			List<User> users = userService.getAllUsersBasic();

			WebContext ctx = new WebContext(request, httpResponse, servletContext, locale);
			AjaxTableHelper ajaxTableHelper = new AjaxTableHelper(messageSource, locale);

			List<User> basicUsers = new ArrayList<>();

			for (User user : users) {
				String encodedUsername = ajaxTableHelper.processName(user.getUsername(), user.getCreationDate(), user.getUpdateDate());
				user.setUsername2(encodedUsername);

				User sessionUser = (User) session.getAttribute("sessionUser");

				ctx.setVariable("user", user);
				ctx.setVariable("sessionUser", sessionUser);
				String templateName = "usersAction";
				String dtAction = defaultTemplateEngine.process(templateName, ctx);
				user.setDtAction(dtAction);

				basicUsers.add(user.getBasicUser());
			}

			ajaxResponse.setData(basicUsers);
			ajaxResponse.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			ajaxResponse.setErrorMessage(ex.toString());
		}

		return ajaxResponse;
	}

	@RequestMapping(value = "/deleteUser", method = RequestMethod.POST)
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
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteUsers", method = RequestMethod.POST)
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
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addUser", method = RequestMethod.GET)
	public String addUser(Model model) {
		logger.debug("Entering addUser");

		User user = new User();

		model.addAttribute("user", user);

		return showEditUser("add", model);
	}

	@RequestMapping(value = "/editUser", method = RequestMethod.GET)
	public String editUser(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editUser: id={}", id);

		User user = null;

		try {
			user = userService.getUser(id);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("user", user);

		return showEditUser("edit", model);
	}

	@RequestMapping(value = "/copyUser", method = RequestMethod.GET)
	public String copyUser(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering copyUser: id={}", id);

		User user = null;

		try {
			user = userService.getUser(id);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("user", user);

		return showEditUser("copy", model);
	}

	@RequestMapping(value = "/editUsers", method = RequestMethod.GET)
	public String editUsers(@RequestParam("ids") String ids, Model model) {
		logger.debug("Entering editUsers: ids={}", ids);

		MultipleUserEdit multipleUserEdit = new MultipleUserEdit();
		multipleUserEdit.setIds(ids);

		model.addAttribute("multipleUserEdit", multipleUserEdit);

		return showEditUsers(model);
	}

	@RequestMapping(value = "/saveUser", method = RequestMethod.POST)
	public String saveUser(@ModelAttribute("user") @Valid User user,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("action") String action,
			HttpSession session, Locale locale) {

		logger.debug("Entering saveUser: user={}, action='{}'", user, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUser(action, model);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPassword(user, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showEditUser(action, model);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");

			if (user.isGenerateAndSend()) {
				String message = null;
				if (!Config.getCustomSettings().isEnableEmailing()) {
					message = "jobs.message.emailingDisabled";
				} else if (!Config.isEmailServerConfigured()) {
					message = "jobs.message.emailServerNotConfigured";
				} else if (StringUtils.isBlank(user.getEmail())) {
					message = "users.message.userEmailNotSet";
				} else if (StringUtils.isBlank(getFromAddress(sessionUser))) {
					message = "users.message.fromAddressNotAvailable";
				}

				if (message != null) {
					model.addAttribute("message", message);
					return showEditUser(action, model);
				}
			}

			String autogeneratedPassword = null;
			if (user.isGenerateAndSend()) {
				final int AUTOGENERATED_PASSWORD_LENGTH = 10;
				// Pick from some letters that won't be easily mistaken for each other.
				// So, for example, omit o O and 0, 1 l and L.
				//http://www.java2s.com/Code/Java/Security/GeneratearandomStringsuitableforuseasatemporarypassword.htm
				//https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/RandomStringUtils.html#random-int-java.lang.String-
				//http://theopentutorials.com/tutorials/java/util/generating-a-random-password-with-restriction-in-java/
				final String PASSWORD_LETTERS = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";
				autogeneratedPassword = RandomStringUtils.random(AUTOGENERATED_PASSWORD_LENGTH, PASSWORD_LETTERS);
				//hash password
				user.setPassword(PasswordUtils.HashPasswordBcrypt(autogeneratedPassword));
				user.setPasswordAlgorithm("bcrypt");
			}

			if (StringUtils.equalsAny(action, "add", "copy")) {
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

			String recordName = user.getUsername() + " (" + user.getUserId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);

			try {
				if (user.isGenerateAndSend()) {
					boolean newAccount;
					if (StringUtils.equalsAny(action, "add", "copy")) {
						newAccount = true;
					} else {
						newAccount = false;
					}

					sendCredentialsEmail(newAccount, user, autogeneratedPassword, locale, sessionUser);
				}
			} catch (Exception ex) {
				logger.error("Error", ex);
				redirectAttributes.addFlashAttribute("error", ex);
			}

			return "redirect:/users";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUser(action, model);
	}

	@RequestMapping(value = "/saveUsers", method = RequestMethod.POST)
	public String saveUsers(@ModelAttribute("multipleUserEdit") @Valid MultipleUserEdit multipleUserEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveUsers: multipleUserEdit={}", multipleUserEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUsers(model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			userService.updateUsers(multipleUserEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleUserEdit.getIds());

			if (!multipleUserEdit.isUserGroupsUnchanged()) {
				try {
					String[] ids = StringUtils.split(multipleUserEdit.getIds(), ",");
					for (String idString : ids) {
						int id = Integer.parseInt(idString);
						User user = userService.getUser(id);
						user.setUserGroups(multipleUserEdit.getUserGroups());
						userGroupMembershipService2.recreateUserGroupMemberships(user);
					}
				} catch (SQLException | RuntimeException ex) {
					logger.error("Error", ex);
					redirectAttributes.addFlashAttribute("error", ex);
				}
			}

			return "redirect:/users";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUsers(model);
	}

	/**
	 * Prepares model data for edit user page and returns the jsp file to
	 * display
	 *
	 * @param action the action. "add" or "edit"
	 * @param model the model to use
	 * @return returns the jsp file to display
	 */
	private String showEditUser(String action, Model model) {
		logger.debug("Entering showEditUser: action='{}'", action);

		try {
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
			model.addAttribute("reportGroups", reportGroupService.getAllReportGroups());
			model.addAttribute("roles", roleService.getAllRoles());
		} catch (SQLException | RuntimeException ex) {
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
	private String showEditUsers(Model model) {
		logger.debug("Entering showEditUsers");

		try {
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "editUsers";
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
			if (StringUtils.isEmpty(newPassword)
					&& StringUtils.equalsAny(action, "edit", "copy")) {
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

	/**
	 * Sends an email to a user to indicate their ART login details
	 *
	 * @param newAccount whether this is a new user account
	 * @param user the target user
	 * @param password the user's password
	 * @param locale the locale to be used for the email text
	 * @param sessionUser the session user
	 * @throws Exception
	 */
	private void sendCredentialsEmail(boolean newAccount, User user,
			String password, Locale locale, User sessionUser) throws Exception {

		logger.debug("Entering sendCredentialsEmail: newAccount={}, user={},"
				+ " locale={}, sessionUser={}", newAccount, user, locale, sessionUser);

		String artLink = Config.getSettings().getArtBaseUrl();
		String username = user.getUsername();

		logger.debug("artLink='{}'", artLink);
		logger.debug("username='{}'", username);

		Context ctx = new Context(locale);
		ctx.setVariable("newAccount", newAccount);
		ctx.setVariable("username", username);
		ctx.setVariable("password", password);
		ctx.setVariable("artLink", artLink);

		String templateName = "credentialsEmail";
		String finalMessage = defaultTemplateEngine.process(templateName, ctx);

		String subjectI18nText;
		if (newAccount) {
			subjectI18nText = "email.subject.accountCreated";
		} else {
			subjectI18nText = "email.subject.passwordReset";
		}
		String subjectText = messageSource.getMessage(subjectI18nText, null, locale);
		String subject = "ART - " + subjectText;

		String from = getFromAddress(sessionUser);

		String to = user.getEmail();
		logger.debug("to='{}'", to);

		Mailer mailer = mailService.getMailer();

		mailer.setFrom(from);
		mailer.setSubject(subject);
		mailer.setTo(to);
		mailer.setMessage(finalMessage);

		mailer.send();
	}

	/**
	 * Returns the from email address to use with generate and send
	 *
	 * @param sessionUser the session user
	 * @return the from email address to use with generate and send
	 */
	private String getFromAddress(User sessionUser) {
		logger.debug("Entering getFromAddress: sessionUser={}", sessionUser);

		String from;
		String smtpFrom = Config.getSettings().getSmtpFrom();
		logger.debug("smtpFrom='{}'", smtpFrom);

		if (StringUtils.isBlank(smtpFrom)) {
			logger.debug("sessionUser.getEmail()='{}'", sessionUser.getEmail());
			from = sessionUser.getEmail();
		} else {
			from = smtpFrom;
		}

		return from;
	}

}
