package art.login;

import art.user.User;
import art.user.UserService;
import art.utils.Encrypter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to authenticate users using ART's database
 *
 * @author Timothy Anyona
 */
public class InternalLogin {

	private static final Logger logger = LoggerFactory.getLogger(InternalLogin.class);

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
						passwordVerified = Encrypter.VerifyPassword(password, user.getPassword(), user.getPasswordAlgorithm());
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
