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
import javax.servlet.http.HttpSession;
import org.saiku.service.olap.OlapDiscoverService;
import org.saiku.service.olap.ThinQueryService;
import org.springframework.stereotype.Component;

/**
 * Provides methods to get the appropriate discover service and thin query
 * service for the current user
 *
 * @author Timothy Anyona
 */
@Component
public class DiscoverHelper {

	public OlapDiscoverService getDiscoverService(HttpSession session) {
		User sessionUser = (User) session.getAttribute("sessionUser");
		int userId = sessionUser.getUserId();
		return Config.getOlapDiscoverService(userId);
	}

	public ThinQueryService getThinQueryService(HttpSession session) {
		User sessionUser = (User) session.getAttribute("sessionUser");
		int userId = sessionUser.getUserId();
		return Config.getThinQueryService(userId);
	}

}
