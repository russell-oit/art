package art.user;

import art.enums.AccessLevel;
import java.io.Serializable;
import java.util.Date;

/**
 * Class to represent a user. Data stored in the ART_USERS table
 *
 * @author Timothy Anyona
 */
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	private String username;
	private AccessLevel accessLevel;
	private String email;
	private String fullName;
	private String password;
	private int defaultQueryGroup;
	private String hashingAlgorithm;
	private String startQuery;
	private boolean active;
	private Integer userId;
	private boolean canChangePassword;
	private Date creationDate;
	private Date updateDate;

	/**
	 * Get the value of updateDate
	 *
	 * @return the value of updateDate
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * Set the value of updateDate
	 *
	 * @param updateDate new value of updateDate
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}


	/**
	 * Get the value of creationDate
	 *
	 * @return the value of creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Set the value of creationDate
	 *
	 * @param creationDate new value of creationDate
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Get the value of canChangePassword
	 *
	 * @return the value of canChangePassword
	 */
	public boolean isCanChangePassword() {
		return canChangePassword;
	}

	/**
	 * Set the value of canChangePassword
	 *
	 * @param canChangePassword new value of canChangePassword
	 */
	public void setCanChangePassword(boolean canChangePassword) {
		this.canChangePassword = canChangePassword;
	}

	/**
	 * Get the value of userId
	 *
	 * @return the value of userId
	 */
	public Integer getUserId() {
		return userId;
	}

	/**
	 * Set the value of userId
	 *
	 * @param userId new value of userId
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
	 * Get the value of active
	 *
	 * @return the value of active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set the value of active
	 *
	 * @param active new value of active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

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
	public AccessLevel getAccessLevel() {
		return accessLevel;
	}

	/**
	 * Set the value of accessLevel
	 *
	 * @param accessLevel new value of accessLevel
	 */
	public void setAccessLevel(AccessLevel accessLevel) {
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

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + (this.username != null ? this.username.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final User other = (User) obj;
		if ((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "User{" + "username=" + username + '}';
	}
	
}
