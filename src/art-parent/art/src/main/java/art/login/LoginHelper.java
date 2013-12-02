package art.login;

import art.enums.ArtAuthenticationMethod;
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
	public void log(ArtAuthenticationMethod loginMethod, LoginResult result,
			String username, String ip) {

		log(loginMethod, result.isAuthenticated(), username, ip, result.getDetails());
	}

	/**
	 * Log login attempts
	 *
	 * @param success
	 * @param username
	 * @param ip
	 * @param failureMessage
	 */
	private void log(ArtAuthenticationMethod loginMethod, boolean success,
			String username, String ip, String failureMessage) {

		String loginStatus;
		String logMessage;
		if (success) {
			loginStatus = "login";
			logMessage = loginMethod.getValue();
		} else {
			loginStatus = "loginerr";
			logMessage = loginMethod.getValue() + ", " + failureMessage;
		}

		ArtHelper.log(username, loginStatus, ip, logMessage);
		
		//also log to file
		logger.info("{}. username={}, message={}", loginStatus, username, logMessage);
	}

	public void logSuccess(ArtAuthenticationMethod loginMethod,
			String username, String ip) {

		log(loginMethod, true, username, ip, "");
	}

	public void logFailure(ArtAuthenticationMethod loginMethod,
			String username, String ip, String message) {

		log(loginMethod, false, username, ip, message);

	}
}
