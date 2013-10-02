package art.user;

import java.io.Serializable;

/**
 * Class to represent a user. Data stored in the ART_USERS table
 *
 * @author Timothy Anyona
 */
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	private String username;
	private int accessLevel;

	/**
	 * Get the value of accessLevel
	 *
	 * @return the value of accessLevel
	 */
	public int getAccessLevel() {
		return accessLevel;
	}

	/**
	 * Set the value of accessLevel
	 *
	 * @param accessLevel new value of accessLevel
	 */
	public void setAccessLevel(int accessLevel) {
		this.accessLevel = accessLevel;
	}

	/**
	 * Get the value of username
	 *
	 * @return the value of username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the value of username
	 *
	 * @param username new value of username
	 */
	public void setUsername(String username) {
		this.username = username;
	}
}
