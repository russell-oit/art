package art.login;

import art.utils.ArtHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with utility methods related to the login process
 *
 * @author Timothy Anyona
 */
public class LoginHelper {

	final static Logger logger = LoggerFactory.getLogger(LoginHelper.class);

	/**
	 * Log login attempts
	 *
	 * @param loginMethod
	 * @param result
	 * @param username
	 * @param ip ip address from which login was done or attempted
	 */
	public void logLoginAttempt(AuthenticationMethod loginMethod, LoginResult result,
			String username, String ip) {

		String message = loginMethod.getValue();
		if (!result.isAuthenticated()) {
			//add failure message
			message += ", " + result.getMessageDetails();
		}
		logLoginAttempt(result.isAuthenticated(), username, ip, message);
	}

	/**
	 * Log login attempts
	 *
	 * @param success
	 * @param username
	 * @param ip
	 * @param message
	 */
	public void logLoginAttempt(boolean success, String username, String ip, String message) {
		String loginStatus;
		if (success) {
			loginStatus = "login";
		} else {
			loginStatus = "loginerr";
		}

		ArtHelper.log(username, loginStatus, ip, message);
	}
}
