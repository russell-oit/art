/*
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
package art.password;

import art.encryption.PasswordUtils;
import art.user.User;
import art.user.UserService;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring controller for the change password process
 *
 * @author Timothy Anyona
 */
@Controller
public class PasswordController {

	private static final Logger logger = LoggerFactory.getLogger(PasswordController.class);

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/app/password", method = RequestMethod.GET)
	public String showPassword(HttpSession session) {
		logger.debug("Entering showPassword");
		
		User sessionUser = (User) session.getAttribute("sessionUser");
		
		if (sessionUser.isCanChangePassword()) {
			return "password";
		} else {
			return "accessDenied";
		}
	}

	@RequestMapping(value = "/app/password", method = RequestMethod.POST)
	public String processPassword(HttpSession session,
			@RequestParam("newPassword1") String newPassword1,
			@RequestParam("newPassword2") String newPassword2,
			Model model, RedirectAttributes redirectAttributes) {
		
		logger.debug("Entering processPassword");

		if (!StringUtils.equals(newPassword1, newPassword2)) {
			model.addAttribute("message", "password.message.passwordsDontMatch");
		} else {
			//change password
			String passwordHash = PasswordUtils.HashPasswordBcrypt(newPassword1);
			String passwordAlgorithm = "bcrypt";

			User sessionUser = (User) session.getAttribute("sessionUser");
			try {
				userService.updatePassword(sessionUser.getUserId(), passwordHash, passwordAlgorithm, sessionUser);

				//update session user object
				sessionUser.setPassword(passwordHash);
				sessionUser.setPasswordAlgorithm(passwordAlgorithm);

				redirectAttributes.addFlashAttribute("message", "password.message.passwordUpdated");
				return "redirect:/app/success.do";
			} catch (SQLException ex) {
				logger.error("Error", ex);
				model.addAttribute("error", ex);
			}
		}

		return "password";
	}

}
