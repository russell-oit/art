/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.login;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.user.User;
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
				+ " (USER_ID, USERNAME, LOGIN_DATE, IP_ADDRESS)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

		Object[] values = {
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
