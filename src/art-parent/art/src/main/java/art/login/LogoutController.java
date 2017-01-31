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
package art.login;

import art.enums.ArtAuthenticationMethod;
import art.user.User;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for the logout process
 *
 * @author Timothy Anyona
 */
@Controller
public class LogoutController {

	private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

	@Autowired
	private LoginService loginService;

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public String logout(HttpSession session, Model model) {
		logger.debug("Entering logout");

		String authenticationMethod = (String) session.getAttribute("authenticationMethod");
		logger.debug("authenticationMethod='{}'", authenticationMethod);

		User sessionUser = (User) session.getAttribute("sessionUser");
		try {
			loginService.removeLoggedInUser(sessionUser);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
		}

		String casLogoutUrl = (String) session.getAttribute("casLogoutUrl");

		session.invalidate();

		ArtAuthenticationMethod loginMethod = ArtAuthenticationMethod.toEnum(authenticationMethod);
		if (null == loginMethod) {
			return "redirect:/";
		} else switch (loginMethod) {
			case Auto:
				return "autoLogout";
			case CAS:
				if (casLogoutUrl != null) {
					model.addAttribute("casLogoutUrl", casLogoutUrl);
				}
				return "casLogout";
			default:
				return "redirect:/";
		}
	}
}
