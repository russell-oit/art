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
package art.password;

import art.encryption.PasswordUtils;
import art.servlets.Config;
import art.user.User;
import art.user.UserService;
import java.sql.SQLException;
import java.util.Locale;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for the change password process
 *
 * @author Timothy Anyona
 */
@Controller
public class PasswordController {

	private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private MessageSource messageSource;

	@RequestMapping(value = "/password", method = RequestMethod.GET)
	public String showPassword(HttpSession session) {
		logger.debug("Entering showPassword");

		User sessionUser = (User) session.getAttribute("sessionUser");

		if (sessionUser.isCanChangePassword()) {
			return "password";
		} else {
			return "accessDenied";
		}
	}

	@RequestMapping(value = "/password", method = RequestMethod.POST)
	public String processPassword(HttpSession session,
			@RequestParam("newPassword1") String newPassword1,
			@RequestParam("newPassword2") String newPassword2,
			Model model, RedirectAttributes redirectAttributes,
			Locale locale) {

		logger.debug("Entering processPassword");

		User sessionUser = (User) session.getAttribute("sessionUser");
		if (!sessionUser.isCanChangePassword()) {
			return "redirect:/accessDenied";
		}

		if (!StringUtils.equals(newPassword1, newPassword2)) {
			model.addAttribute("message", "password.message.passwordsDontMatch");
		} else if (!PasswordValidator.validateLength(newPassword1)) {
			int passwordMinLength = Config.getSettings().getPasswordMinLength();
			String message = messageSource.getMessage("settings.label.passwordMinLength", null, locale);
			message = message + ": " + passwordMinLength;
			model.addAttribute("message", message);
		} else if (!PasswordValidator.validateLowercase(newPassword1)) {
			int passwordMinLowercase = Config.getSettings().getPasswordMinLowercase();
			String message = messageSource.getMessage("settings.label.passwordMinLowercase", null, locale);
			message = message + ": " + passwordMinLowercase;
			model.addAttribute("message", message);
		} else if (!PasswordValidator.validateUppercase(newPassword1)) {
			int passwordMinUppercase = Config.getSettings().getPasswordMinUppercase();
			String message = messageSource.getMessage("settings.label.passwordMinUppercase", null, locale);
			message = message + ": " + passwordMinUppercase;
			model.addAttribute("message", message);
		} else if (!PasswordValidator.validateNumeric(newPassword1)) {
			int passwordMinNumeric = Config.getSettings().getPasswordMinNumeric();
			String message = messageSource.getMessage("settings.label.passwordMinNumeric", null, locale);
			message = message + ": " + passwordMinNumeric;
			model.addAttribute("message", message);
		} else if (!PasswordValidator.validateSpecial(newPassword1)) {
			int passwordMinSpecial = Config.getSettings().getPasswordMinSpecial();
			String message = messageSource.getMessage("settings.label.passwordMinSpecial", null, locale);
			message = message + ": " + passwordMinSpecial;
			model.addAttribute("message", message);
		} else {
			//change password
			String passwordHash = PasswordUtils.HashPasswordBcrypt(newPassword1);
			String passwordAlgorithm = "bcrypt";

			try {
				userService.updatePassword(sessionUser.getUserId(), passwordHash, passwordAlgorithm, sessionUser);

				//update session user object
				sessionUser.setPassword(passwordHash);
				sessionUser.setPasswordAlgorithm(passwordAlgorithm);

				redirectAttributes.addFlashAttribute("message", "password.message.passwordUpdated");
				return "redirect:/success";
			} catch (SQLException | RuntimeException ex) {
				logger.error("Error", ex);
				model.addAttribute("error", ex);
			}
		}

		return "password";
	}
}
