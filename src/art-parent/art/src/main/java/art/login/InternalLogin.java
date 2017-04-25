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

import art.encryption.PasswordUtils;
import art.user.User;
import art.user.UserService;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticates users using ART's database
 *
 * @author Timothy Anyona
 */
public class InternalLogin {

	private static final Logger logger = LoggerFactory.getLogger(InternalLogin.class);

	/**
	 * Authenticates a user using art internal credentials
	 *
	 * @param username the username to use
	 * @param password the password to use
	 * @return the result of the authentication process
	 */
	public static LoginResult authenticate(String username, String password) {
		logger.debug("Entering authenticate: username='{}'", username);

		LoginResult result = new LoginResult();

		UserService userService = new UserService();

		try {
			User user = userService.getUser(username);

			if (user == null) {
				//user doesn't exist
				logger.debug("No records returned");

				result.setMessage("login.message.artUserInvalid");
				result.setDetails("invalid user");
			} else {
				if (user.isActive()) {
					boolean passwordVerified = false;
					String passwordAlgorithm = user.getPasswordAlgorithm();
					try {
						passwordVerified = PasswordUtils.VerifyPassword(password, user.getPassword(), passwordAlgorithm);
					} catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
						logger.error("Error. username='{}'", username, ex);
					}

					logger.debug("passwordVerified={}", passwordVerified);

					if (passwordVerified) {
						result.setAuthenticated(true);

						//replace md5 password hashes with bcrypt hashes
						if (StringUtils.equalsIgnoreCase(passwordAlgorithm, "md5")) {
							createBcryptPassword(password, user, userService);
						}
					} else {
						//invalid password
						result.setMessage("login.message.invalidPassword");
						result.setDetails("invalid password");
					}
				} else {
					//user disabled
					result.setMessage("login.message.artUserDisabled");
					result.setDetails("user disabled");
				}
			}
		} catch (SQLException ex) {
			logger.error("Error. username='{}'", username, ex);

			result.setMessage("page.message.errorOccurred");
			result.setDetails(ex.getMessage());
			result.setError(ex.toString());
		}

		return result;
	}

	/**
	 * Creates a bcrypt password for the given user
	 * 
	 * @param password the clear text password
	 * @param user the user
	 * @param userService the user service
	 * @throws SQLException 
	 */
	private static void createBcryptPassword(String password, User user, UserService userService)
			throws SQLException {
		
		String bcryptPassword = PasswordUtils.HashPasswordBcrypt(password);
		user.setPassword(bcryptPassword);
		String bcryptAlgorithm = "bcrypt";
		user.setPasswordAlgorithm(bcryptAlgorithm);
		userService.updatePassword(user.getUserId(), bcryptPassword, bcryptAlgorithm, user);
	}
}
