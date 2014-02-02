package art.user;

import art.enums.AccessLevel;
import art.usergroup.UserGroup;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
	private int defaultReportGroup;
	private String passwordAlgorithm;
	private String startReport;
	private boolean active;
	private int userId;
	private boolean canChangePassword;
	private Date creationDate;
	private Date updateDate;
	private List<UserGroup> userGroups;
	private String effectiveStartReport;
	private int effectiveDefaultReportGroup;
	private boolean useBlankPassword; //only used for user interface logic

	/**
	 * Get the value of useBlankPassword. only used for user interface logic
	 *
	 * @return the value of useBlankPassword
	 */
	public boolean isUseBlankPassword() {
		return useBlankPassword;
	}

	/**
	 * Set the value of useBlankPassword. only used for user interface logic
	 *
	 * @param useBlankPassword new value of useBlankPassword
	 */
	public void setUseBlankPassword(boolean useBlankPassword) {
		this.useBlankPassword = useBlankPassword;
	}

	/**
	 * @return the effectiveStartReport
	 */
	public String getEffectiveStartReport() {
		return effectiveStartReport;
	}

	/**
	 * @param effectiveStartReport the effectiveStartReport to set
	 */
	public void setEffectiveStartReport(String effectiveStartReport) {
		this.effectiveStartReport = effectiveStartReport;
	}

	/**
	 * @return the effectiveDefaultReportGroup
	 */
	public int getEffectiveDefaultReportGroup() {
		return effectiveDefaultReportGroup;
	}

	/**
	 * @param effectiveDefaultReportGroup the effectiveDefaultReportGroup to set
	 */
	public void setEffectiveDefaultReportGroup(int effectiveDefaultReportGroup) {
		this.effectiveDefaultReportGroup = effectiveDefaultReportGroup;
	}

	/**
	 * Get the value of userGroups
	 *
	 * @return the value of userGroups
	 */
	public List<UserGroup> getUserGroups() {
		return userGroups;
	}

	/**
	 * Set the value of userGroups
	 *
	 * @param userGroups new value of userGroups
	 */
	public void setUserGroups(List<UserGroup> userGroups) {
		this.userGroups = userGroups;
	}

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
	public int getUserId() {
		return userId;
	}

	/**
	 * Set the value of userId
	 *
	 * @param userId new value of userId
	 */
	public void setUserId(int userId) {
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
	 * Get the value of startReport
	 *
	 * @return the value of startReport
	 */
	public String getStartReport() {
		return startReport;
	}

	/**
	 * Set the value of startReport
	 *
	 * @param startReport new value of startReport
	 */
	public void setStartReport(String startReport) {
		this.startReport = startReport;
	}

	/**
	 * Get the value of passwordAlgorithm
	 *
	 * @return the value of passwordAlgorithm
	 */
	public String getPasswordAlgorithm() {
		return passwordAlgorithm;
	}

	/**
	 * Set the value of passwordAlgorithm
	 *
	 * @param passwordAlgorithm new value of passwordAlgorithm
	 */
	public void setPasswordAlgorithm(String passwordAlgorithm) {
		this.passwordAlgorithm = passwordAlgorithm;
	}

	/**
	 * Get the value of defaultReportGroup
	 *
	 * @return the value of defaultReportGroup
	 */
	public int getDefaultReportGroup() {
		return defaultReportGroup;
	}

	/**
	 * Set the value of defaultReportGroup
	 *
	 * @param defaultReportGroup new value of defaultReportGroup
	 */
	public void setDefaultReportGroup(int defaultReportGroup) {
		this.defaultReportGroup = defaultReportGroup;
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
