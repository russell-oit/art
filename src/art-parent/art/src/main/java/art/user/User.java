package art.user;

import java.io.Serializable;
import org.apache.commons.lang.StringUtils;

/**
 * Class to represent a user. Data stored in the ART_USERS table
 *
 * @author Timothy Anyona
 */
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	private String username;
	private int accessLevel;
	private String email;
	private String activeStatus;
	private String fullName;
	private String password;
	private int defaultQueryGroup;
	private String canChangePassword;
	private String hashingAlgorithm;
	private String startQuery;

	/**
	 * Get the value of startQuery
	 *
	 * @return the value of startQuery
	 */
	public String getStartQuery() {
		return startQuery;
	}

	/**
	 * Set the value of startQuery
	 *
	 * @param startQuery new value of startQuery
	 */
	public void setStartQuery(String startQuery) {
		this.startQuery = startQuery;
	}

	/**
	 * Get the value of hashingAlgorithm
	 *
	 * @return the value of hashingAlgorithm
	 */
	public String getHashingAlgorithm() {
		return hashingAlgorithm;
	}

	/**
	 * Set the value of hashingAlgorithm
	 *
	 * @param hashingAlgorithm new value of hashingAlgorithm
	 */
	public void setHashingAlgorithm(String hashingAlgorithm) {
		this.hashingAlgorithm = hashingAlgorithm;
	}

	/**
	 * Get the value of canChangePassword
	 *
	 * @return the value of canChangePassword
	 */
	public String getCanChangePassword() {
		return canChangePassword;
	}

	/**
	 * Set the value of canChangePassword
	 *
	 * @param canChangePassword new value of canChangePassword
	 */
	public void setCanChangePassword(String canChangePassword) {
		this.canChangePassword = canChangePassword;
	}

	/**
	 * Get the value of defaultQueryGroup
	 *
	 * @return the value of defaultQueryGroup
	 */
	public int getDefaultQueryGroup() {
		return defaultQueryGroup;
	}

	/**
	 * Set the value of defaultQueryGroup
	 *
	 * @param defaultQueryGroup new value of defaultQueryGroup
	 */
	public void setDefaultQueryGroup(int defaultQueryGroup) {
		this.defaultQueryGroup = defaultQueryGroup;
	}

	/**
	 * Get the value of password
	 *
	 * @return the value of password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the value of password
	 *
	 * @param password new value of password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Get the value of fullName
	 *
	 * @return the value of fullName
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * Set the value of fullName
	 *
	 * @param fullName new value of fullName
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * Get the value of activeStatus
	 *
	 * @return the value of activeStatus
	 */
	public String getActiveStatus() {
		return activeStatus;
	}

	/**
	 * Set the value of activeStatus
	 *
	 * @param activeStatus new value of activeStatus
	 */
	public void setActiveStatus(String activeStatus) {
		this.activeStatus = activeStatus;
	}

	/**
	 * Get the value of email
	 *
	 * @return the value of email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Set the value of email
	 *
	 * @param email new value of email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

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

	/**
	 * Determine if the user is active (active status = 'Y')
	 *
	 * @return true if user is active, false otherwise
	 */
	public boolean isActive() {
		boolean active = false;

		if (StringUtils.equalsIgnoreCase(activeStatus, "A")) {
			active = true;
		}

		return active;
	}
}
