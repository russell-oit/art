/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.login;

import art.encryption.PasswordUtils;
import art.user.User;
import art.user.UserService;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
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

				result.setMessage("login.message.invalidUser");
				result.setDetails("invalid user");
			} else {
				if (user.isActive()) {
					boolean passwordVerified = false;
					try {
						passwordVerified = PasswordUtils.VerifyPassword(password, user.getPassword(), user.getPasswordAlgorithm());
					} catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
						logger.error("Error. username='{}'", username, ex);
					}

					logger.debug("passwordVerified={}", passwordVerified);

					if (passwordVerified) {
						result.setAuthenticated(true);
					} else {
						//invalid password
						result.setMessage("login.message.invalidCredentials");
						result.setDetails("invalid password");
					}
				} else {
					//user disabled
					result.setMessage("login.message.userDisabled");
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
}
