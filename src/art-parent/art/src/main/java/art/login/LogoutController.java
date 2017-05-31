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
package art.login;

import art.enums.ArtAuthenticationMethod;
import art.saiku.SaikuConnectionProvider;
import art.servlets.Config;
import art.user.User;
import java.sql.SQLException;
import java.util.Map;
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
		
		int userId = sessionUser.getUserId();
		Config.closeSaikuConnections(userId);

		String casLogoutUrl = (String) session.getAttribute("casLogoutUrl");

		session.invalidate();

		ArtAuthenticationMethod loginMethod = ArtAuthenticationMethod.toEnum(authenticationMethod);
		if (null == loginMethod) {
			return "redirect:/login";
		} else switch (loginMethod) {
			case Auto:
				return "autoLogout";
			case CAS:
				if (casLogoutUrl != null) {
					model.addAttribute("casLogoutUrl", casLogoutUrl);
				}
				return "casLogout";
			default:
				return "redirect:/login";
		}
	}
}
