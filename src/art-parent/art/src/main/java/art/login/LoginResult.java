package art.login;

/**
 * Class to hold results of a login attempt
 *
 * @author Timothy Anyona
 */
public class LoginResult {

	private boolean authenticated;
	private String message; //i18n message. only used for display in the ui
	private String error; //full exception details
	private String messageDetails; //exception message or custom message

	/**
	 * Get the exception message or custom message
	 *
	 * @return the exception message or custom message
	 */
	public String getMessageDetails() {
		return messageDetails;
	}

	/**
	 * Set the exception message or custom message
	 *
	 * @param messageDetails the exception message or custom message
	 */
	public void setMessageDetails(String messageDetails) {
		this.messageDetails = messageDetails;
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
	 * Get the i18n message. only used for display in the ui
	 *
	 * @return the i18n message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the i18n message. only used for display in the ui
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
}
