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
package art.utils;

import art.datasource.Datasource;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides methods to construct a mongodb connection url, inserting username
 * and password into the url as appropriate
 *
 * @author Timothy Anyona
 */
public class MongoHelper {

	public String getUrlWithCredentials(Datasource datasource) {
		Objects.requireNonNull(datasource, "datasource must not be null");

		String url = datasource.getUrl();
		String username = datasource.getUsername();
		String password = datasource.getPassword();

		return MongoHelper.this.getUrlWithCredentials(url, username, password);
	}

	public String getUrlWithCredentials(String url, String username, String password) {
		if (StringUtils.isBlank(username)) {
			return url;
		}

		String beginning = "mongodb://";
		String end = StringUtils.substringAfter(url, beginning);
		String finalUrl=beginning + username + ":" + password + "@" + end;
		
		return finalUrl;
	}

}
