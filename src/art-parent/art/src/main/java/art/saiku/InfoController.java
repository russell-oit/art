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
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.saiku.service.util.dto.Plugin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/saiku2/info")
public class InfoController {

	@GetMapping()
	public List<Plugin> info(HttpServletRequest request) {
		ArrayList<Plugin> l = new ArrayList<>();
		String filePath = Config.getAppPath() + File.separator + "saiku" + File.separator + "js"
				+ File.separator + "saiku" + File.separator + "plugins" + File.separator;
		File f = new File(filePath);

		String[] directories = f.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		String contextPath = request.getContextPath();

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
							String path = contextPath + "/saiku/js/saiku/plugins/" + s.getParentFile().getName() + "/plugin.js";
							Plugin p = new Plugin(s.getParentFile().getName(), "", path);
							l.add(p);
						}
					}
				}
			}
		}
		return l;
	}

	@GetMapping("/ui-settings")
	public Map<String, Object> overrideUiSettings() {
		//not sure how this works with respect to overriding properties of the Settings object in /saiku/js/saiku/Settings.js, or other properties?
		//created new end point - /info/main-settings to specifically do override of Settings before anything else is done
		return Collections.emptyMap();
	}

	@GetMapping("/main-settings")
	public Map<String, Object> overrideMainSettings(Locale locale, HttpServletRequest request) {
		Map<String, Object> settings = new HashMap<>();
		settings.put("VERSION", "saiku-art");
		//locale can be changed either manually in Settings.js
		//or adding lang parameter when calling index.html
		//or passing "language" attribute when beginning the session
		//or through this newly added main-settings end point, updating the Settings object
		settings.put("I18N_LOCALE", locale.toString());
		settings.put("TOMCAT_WEBAPP", request.getContextPath());
		settings.put("REST_MOUNT_POINT", "/saiku2");
		String resourcesPath = request.getContextPath() + "/saiku/";
		settings.put("RESOURCES_PATH", resourcesPath);
		settings.put("SHOW_REFRESH_NONADMIN", true);
		settings.put("CONTEXT_PATH", request.getContextPath());
		String saikuHome = request.getContextPath() + "/saiku3";
		settings.put("SAIKU_HOME", saikuHome);
		return settings;
	}

}
