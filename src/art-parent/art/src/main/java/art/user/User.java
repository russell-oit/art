/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.user;

import art.encryption.PasswordUtils;
import art.enums.AccessLevel;
import art.migration.PrefixTransformer;
import art.permission.Permission;
import art.reportgroup.ReportGroup;
import art.role.Role;
import art.usergroup.UserGroup;
import art.utils.ArtUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a user
 *
 * @author Timothy Anyona
 */
public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	@Parsed
	private int userId;
	@Parsed
	private String username;
	@Parsed
	private AccessLevel accessLevel;
	@Parsed
	private String email;
	@Parsed
	private String fullName;
	@Parsed
	private String password;
	@Parsed
	private String passwordAlgorithm;
	@Parsed
	private String startReport; //can contain only report id or report id with parameters e.g. 1?p-param1=value
	@Parsed
	private boolean active = true;
	@Parsed
	private boolean canChangePassword = true;
	private Date creationDate;
	private Date updateDate;
	private List<UserGroup> userGroups;
	private String effectiveStartReport;
	@JsonIgnore
	private ReportGroup effectiveDefaultReportGroup;
	private boolean useBlankPassword; //only used for user interface logic
	private String createdBy;
	private String updatedBy;
	private boolean generateAndSend; //only used for user interface logic
	@Parsed
	private boolean clearTextPassword; //used to allow import with clear text passwords
	@Nested(headerTransformer = PrefixTransformer.class, args = "defaultReportGroup")
	private ReportGroup defaultReportGroup;
	private List<Role> roles;
	private List<Permission> permissions;
	@JsonIgnore
	private List<String> flatPermissions = new ArrayList<>();

	/**
	 * @return the permissions
	 */
	public List<Permission> getPermissions() {
		return permissions;
	}

	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}

	/**
	 * @return the flatPermissions
	 */
	public List<String> getFlatPermissions() {
		return flatPermissions;
	}

	/**
	 * @param flatPermissions the flatPermissions to set
	 */
	public void setFlatPermissions(List<String> flatPermissions) {
		this.flatPermissions = flatPermissions;
	}

	/**
	 * @return the roles
	 */
	public List<Role> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	/**
	 * @return the clearTextPassword
	 */
	public boolean isClearTextPassword() {
		return clearTextPassword;
	}

	/**
	 * @param clearTextPassword the clearTextPassword to set
	 */
	public void setClearTextPassword(boolean clearTextPassword) {
		this.clearTextPassword = clearTextPassword;
	}

	/**
	 * @return the generateAndSend
	 */
	public boolean isGenerateAndSend() {
		return generateAndSend;
	}

	/**
	 * @param generateAndSend the generateAndSend to set
	 */
	public void setGenerateAndSend(boolean generateAndSend) {
		this.generateAndSend = generateAndSend;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

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
	public ReportGroup getEffectiveDefaultReportGroup() {
		return effectiveDefaultReportGroup;
	}

	/**
	 * @param effectiveDefaultReportGroup the effectiveDefaultReportGroup to set
	 */
	public void setEffectiveDefaultReportGroup(ReportGroup effectiveDefaultReportGroup) {
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
		//public user can't change password
		if (isPublicUser()) {
			return false;
		} else {
			return canChangePassword;
		}
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
	public ReportGroup getDefaultReportGroup() {
		return defaultReportGroup;
	}

	/**
	 * Set the value of defaultReportGroup
	 *
	 * @param defaultReportGroup new value of defaultReportGroup
	 */
	public void setDefaultReportGroup(ReportGroup defaultReportGroup) {
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
		//ensure public user always has normal user access level
		if (isPublicUser()) {
			return AccessLevel.NormalUser;
		} else {
			return accessLevel;
		}
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
		int hash = 7;
		hash = 59 * hash + this.userId;
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
		if (this.userId != other.userId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "User{" + "username=" + username + '}';
	}

	/**
	 * Returns <code>true</code> if this is an admin user
	 *
	 * @return <code>true</code> if this is an admin user
	 */
	@JsonIgnore
	public boolean isAdminUser() {
		if (accessLevel == null || accessLevel.getValue() < AccessLevel.JuniorAdmin.getValue()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns <code>true</code> if this is the public user
	 *
	 * @return <code>true</code> if this is the public user
	 */
	@JsonIgnore
	public boolean isPublicUser() {
		if (StringUtils.equals(username, ArtUtils.PUBLIC_USER)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Encrypts the password field
	 */
	public void encryptPassword() {
		password = PasswordUtils.HashPasswordBcrypt(password);
		passwordAlgorithm = "bcrypt";
	}

	/**
	 * Creates a list of all permissions that this user has
	 */
	public void prepareFlatPermissions() {
		flatPermissions.clear();

		if (CollectionUtils.isNotEmpty(permissions)) {
			for (Permission permission : permissions) {
				flatPermissions.add(permission.getName());
			}
		}

		if (CollectionUtils.isNotEmpty(roles)) {
			for (Role role : roles) {
				List<Permission> rolePermissions = role.getPermissions();
				if (CollectionUtils.isNotEmpty(rolePermissions)) {
					for (Permission permission : rolePermissions) {
						flatPermissions.add(permission.getName());
					}
				}
			}
		}

		if (CollectionUtils.isNotEmpty(userGroups)) {
			for (UserGroup userGroup : userGroups) {
				List<Permission> userGroupPermissions = userGroup.getPermissions();
				if (CollectionUtils.isNotEmpty(userGroupPermissions)) {
					for (Permission permission : userGroupPermissions) {
						flatPermissions.add(permission.getName());
					}
				}

				List<Role> userGroupRoles = userGroup.getRoles();
				if (CollectionUtils.isNotEmpty(userGroupRoles)) {
					for (Role role : userGroupRoles) {
						List<Permission> rolePermissions = role.getPermissions();
						if (CollectionUtils.isNotEmpty(rolePermissions)) {
							for (Permission permission : rolePermissions) {
								flatPermissions.add(permission.getName());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Returns <code>true</code> if the user has the given permission
	 *
	 * @param permission the permission name
	 * @return <code>true</code> if the user has the given permission
	 */
	public boolean hasPermission(String permission) {
		boolean hasPermission = false;

		if (CollectionUtils.isNotEmpty(flatPermissions)) {
			hasPermission = flatPermissions.contains(permission);
		}

		return hasPermission;
	}

	/**
	 * Returns <code>true</code> if the user has any of the given permissions
	 *
	 * @param permissions the permissions
	 * @return <code>true</code> if the user has any of the given permissions
	 */
	public boolean hasAnyPermission(String... permissions) {
		boolean hasPermission = false;

		if (CollectionUtils.isNotEmpty(flatPermissions) && permissions != null) {
			List<String> permissionsList = Arrays.asList(permissions);
			hasPermission = CollectionUtils.containsAny(flatPermissions, permissionsList);
		}

		return hasPermission;
	}

	/**
	 * Returns <code>true</code> if the user has any of the given permissions
	 *
	 * @param permissionString the permissions, in a comma separated string
	 * @return <code>true</code> if the user has any of the given permissions
	 */
	public boolean hasAnyPermissionCommaSeparated(String permissionString) {
		//need a separate method because you can't use a method with a varargs parameter in el/jsp
		//https://stackoverflow.com/questions/15560508/invoke-method-with-varargs-in-el-throws-java-lang-illegalargumentexception-wron
		//https://stackoverflow.com/questions/46955785/illegalargumentexception-on-expression-language-with-varargs-parameter-can-not-b
		//https://sourceforge.net/p/art/discussion/352129/thread/c1a607e9c1/?limit=25#92c5
		String[] tempPermissions=StringUtils.split(permissionString, ",");
		String[] strippedPermissions = StringUtils.stripAll(tempPermissions);
		return hasAnyPermission(strippedPermissions);
	}

	/**
	 * Returns <code>true</code> if the user has any configure permission
	 *
	 * @return <code>true</code> if the user has any configure permission
	 */
	public boolean hasConfigurePermission() {
		boolean hasPermission = false;

		if (CollectionUtils.isNotEmpty(flatPermissions)) {
			for (String permission : flatPermissions) {
				if (StringUtils.startsWith(permission, "configure")) {
					hasPermission = true;
					break;
				}
			}
		}

		return hasPermission;
	}

	/**
	 * Sets permissions for the initial setup or repository user
	 */
	public void buildSetupUserPermissions() {
		flatPermissions.clear();

		flatPermissions.addAll(Arrays.asList("configure_users",
				"configure_art_database", "configure_roles"));
	}

}
