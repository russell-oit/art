package art.login;

/**
 * Class to hold results of a login attempt
 *
 * @author Timothy Anyona
 */
public class LoginResult {

	private boolean authenticated;
	private String message; //i18n message. only for use in the login page
	private String error; //full exception details
	private String details; //exception message or custom message. for display or logging

	/**
	 * Get the exception message or custom message
	 *
	 * @return the exception message or custom message
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * Set the exception message or custom message
	 *
	 * @param details the exception message or custom message
	 */
	public void setDetails(String details) {
		this.details = details;
	}

	/**
	 * Get the full exception details
	 *
	 * @return the full exception details
	 */
	public String getError() {
		return error;
	}

	/**
	 * Set the full exception details
	 *
	 * @param error the full exception details
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * Get the i18n message. only for use in the login page
	 *
	 * @return the i18n message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the i18n message. only for use in the login page
	 *
	 * @param message the i18n message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Get the value of authenticated
	 *
	 * @return the value of authenticated
	 */
	public boolean isAuthenticated() {
		return authenticated;
	}

	/**
	 * Set the value of authenticated
	 *
	 * @param authenticated new value of authenticated
	 */
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	@Override
	public String toString() {
		return "LoginResult{" + "authenticated=" + authenticated + ", message=" + message + ", error=" + error + ", details=" + details + '}';
	}
	
}
