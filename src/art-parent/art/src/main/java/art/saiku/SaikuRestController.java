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
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.saiku.olap.util.exception.SaikuOlapException;
import org.saiku.service.util.dto.Plugin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Controller for saiku rest endpoints
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/saiku2/rest/saiku")
public class SaikuRestController {

	@PostMapping("/session")
	public void login(HttpSession session, Locale locale) {
		//do nothing. user already authenticated by art
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

	@GetMapping("/session")
	public Map<String, Object> getSessionDetails(HttpSession session, Locale locale) throws SaikuOlapException {
		Map<String, Object> saikuSessionDetails = createSessionDetails(session, locale);
		return saikuSessionDetails;
	}

	@GetMapping("/info")
	public List<Plugin> info() {
		ArrayList<Plugin> l = new ArrayList<>();
		String filePath = Config.getAppPath() + File.separator + "saiku" + File.separator + "js"
				+ File.separator + "saiku" + File.separator + "plugins" + File.separator;
		File f = new File(filePath);

		String[] directories = f.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		if (directories != null && directories.length > 0) {
			for (String d : directories) {
				File subdir = new File(filePath + "/" + d);
				File[] subfiles = subdir.listFiles();

				/**
				 * use a metadata.js file for alternative details.
				 */
				if (subfiles != null) {
					for (File s : subfiles) {
						if (s.getName().equals("plugin.js")) {
							Plugin p = new Plugin(s.getParentFile().getName(), "", "js/saiku/plugins/" + s.getParentFile().getName() + "/plugin.js");
							l.add(p);
						}
					}
				}
			}
		}
		return l;
	}

	@GetMapping("/info/ui-settings")
	public Map<String, Object> getUiSettings(Locale locale, HttpServletRequest request) {
		//optionally override properties of the Settings object in /saiku/js/saiku/Settings.js
		Map<String, Object> uiSettings = new HashMap<>();
		uiSettings.put("VERSION", "saiku-art");
		//locale can only be changed either manually in Settings.js
		//or adding lang parameter to saiku index.html call
		//or passing "language" attribute when beginning the session
		//uiSettings.put("I18N_LOCALE", locale.toString());
		String restUrlRoot = request.getContextPath() + "/saiku2";
		uiSettings.put("TOMCAT_WEBAPP", restUrlRoot);
		return uiSettings;
	}

}
