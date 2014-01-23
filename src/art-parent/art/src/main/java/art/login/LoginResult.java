package art.login;

/**
 * Class to hold results of a login attempt
 *
 * @author Timothy Anyona
 */
public class LoginResult {

	private boolean authenticated;
	private String message; //i18n message. for display
	private String error; //exception details
	private String details; //custom message. for logging

	/**
	 * Get the custom message for logging
	 *
	 * @return the custom message for logging
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * Set the custom message for logging
	 *
	 * @param details the custom message for logging
	 */
	public void setDetails(String details) {
		this.details = details;
	}

	/**
	 * Get the exception details
	 *
	 * @return the exception details
	 */
	public String getError() {
		return error;
	}

	/**
	 * Set the exception details
	 *
	 * @param error the exception details
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * Get the i18n message for display
	 *
	 * @return the i18n message for display
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the i18n message for display
	 *
	 * @param message the i18n message for display
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
