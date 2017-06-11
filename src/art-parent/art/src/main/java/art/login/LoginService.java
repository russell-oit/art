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

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.user.User;
import art.utils.ArtUtils;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods for adding or deleting currently logged in users
 *
 * @author Timothy Anyona
 */
@Service
public class LoginService {

	private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

	private final DbService dbService;

	@Autowired
	public LoginService(DbService dbService) {
		this.dbService = dbService;
	}

	public LoginService() {
		dbService = new DbService();
	}

	/**
	 * Adds a record to the logged_in_users table for the given user
	 *
	 * @param user the user
	 * @param ipAddress the ip address from which they are logging in
	 * @throws SQLException
	 */
	public void addLoggedInUser(User user, String ipAddress) throws SQLException {
		logger.debug("Entering addLoggedInUser: user={}, ipAddress='{}'", user, ipAddress);

		String sql = "INSERT INTO ART_LOGGED_IN_USERS"
				+ " (LOGGED_IN_USERS_ID, USER_ID, USERNAME, LOGIN_DATE, IP_ADDRESS)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 5) + ")";

		String id = ArtUtils.getUniqueId() + "-" + user.getUserId();

		Object[] values = {
			id,
			user.getUserId(),
			user.getUsername(),
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			ipAddress
		};

		dbService.update(sql, values);
	}

	/**
	 * Removes a user's record from the logged_in_users table
	 *
	 * @param user the user
	 * @throws SQLException
	 */
	public void removeLoggedInUser(User user) throws SQLException {
		logger.debug("Entering removeLoggedInUser: user={}", user);

		String sql = "DELETE FROM ART_LOGGED_IN_USERS"
				+ " WHERE USER_ID=?";

		dbService.update(sql, user.getUserId());
	}
}
