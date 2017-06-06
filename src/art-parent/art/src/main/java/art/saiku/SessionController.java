/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.saiku;

import art.servlets.Config;
import art.user.User;
import art.utils.ArtUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.saiku.olap.util.exception.SaikuOlapException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

/**
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/saiku2/session")
public class SessionController {

	@PostMapping()
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void login(HttpSession session, Locale locale) {
		//do nothing. user already authenticated by art
	}

	@GetMapping()
	public Map<String, Object> getSessionDetails(HttpSession session, Locale locale) throws SaikuOlapException {
		Map<String, Object> saikuSessionDetails = createSessionDetails(session, locale);
		return saikuSessionDetails;
	}

	private Map<String, Object> createSessionDetails(HttpSession session, Locale locale)
			throws IllegalStateException {

		Map<String, Object> sessionDetails = new HashMap<>();
		User sessionUser = (User) session.getAttribute("sessionUser");
		sessionDetails.put("username", sessionUser.getUsername());
		sessionDetails.put("sessionid", ArtUtils.getUniqueId());
		sessionDetails.put("authid", RequestContextHolder.currentRequestAttributes().getSessionId());
		List<String> roles = new ArrayList<>();
		sessionDetails.put("roles", roles);
		sessionDetails.put("language", locale.toString());
		return sessionDetails;
	}

	@DeleteMapping()
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpSession session) throws IOException {
		User sessionUser = (User) session.getAttribute("sessionUser");
		int userId = sessionUser.getUserId();
		Config.closeSaikuConnections(userId);
		
		//https://stackoverflow.com/questions/29085295/spring-mvc-restcontroller-and-redirect
		//http://forum.spring.io/forum/spring-projects/web/747839-how-to-redirect-properly-in-restful-spring-mvc-controller
	}

}
