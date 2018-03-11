/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.settings;

import art.servlets.Config;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * Provides utility methods related to application settings
 *
 * @author Timothy Anyona
 */
public class SettingsHelper {

	/**
	 * Refreshes some aspects of application settings
	 * 
	 * @param settings the updated settings
	 * @param session the http session
	 * @param servletContext the servlet context
	 */
	public void refreshSettings(Settings settings, HttpSession session,
			ServletContext servletContext) {

		if (session != null) {
			session.setAttribute("administratorEmail", settings.getAdministratorEmail());
			session.setAttribute("casLogoutUrl", settings.getCasLogoutUrl());
		}

		if (servletContext != null) {
			String dateDisplayPattern = settings.getDateFormat() + " " + settings.getTimeFormat();
			servletContext.setAttribute("dateDisplayPattern", dateDisplayPattern); //format of dates displayed in tables
		}

		Config.loadSettings();
	}

}
