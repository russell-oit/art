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

import art.enums.AccessLevel;
import art.usergroup.UserGroup;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a multiple user edit
 *
 * @author Timothy Anyona
 */
public class MultipleUserEdit implements Serializable {

	private static final long serialVersionUID = 1L;
	private String ids;
	private boolean active;
	private boolean activeUnchanged = true;
	private boolean canChangePassword;
	private boolean canChangePasswordUnchanged = true;
	private AccessLevel accessLevel;
	private boolean accessLevelUnchanged = true;
	private List<UserGroup> userGroups;
	private boolean userGroupsUnchanged = true;

	/**
	 * @return the canChangePassword
	 */
	public boolean isCanChangePassword() {
		return canChangePassword;
	}

	/**
	 * @param canChangePassword the canChangePassword to set
	 */
	public void setCanChangePassword(boolean canChangePassword) {
		this.canChangePassword = canChangePassword;
	}

	/**
	 * @return the canChangePasswordUnchanged
	 */
	public boolean isCanChangePasswordUnchanged() {
		return canChangePasswordUnchanged;
	}

	/**
	 * @param canChangePasswordUnchanged the canChangePasswordUnchanged to set
	 */
	public void setCanChangePasswordUnchanged(boolean canChangePasswordUnchanged) {
		this.canChangePasswordUnchanged = canChangePasswordUnchanged;
	}

	/**
	 * @return the accessLevel
	 */
	public AccessLevel getAccessLevel() {
		return accessLevel;
	}

	/**
	 * @param accessLevel the accessLevel to set
	 */
	public void setAccessLevel(AccessLevel accessLevel) {
		this.accessLevel = accessLevel;
	}

	/**
	 * @return the accessLevelUnchanged
	 */
	public boolean isAccessLevelUnchanged() {
		return accessLevelUnchanged;
	}

	/**
	 * @param accessLevelUnchanged the accessLevelUnchanged to set
	 */
	public void setAccessLevelUnchanged(boolean accessLevelUnchanged) {
		this.accessLevelUnchanged = accessLevelUnchanged;
	}

	/**
	 * @return the userGroups
	 */
	public List<UserGroup> getUserGroups() {
		return userGroups;
	}

	/**
	 * @param userGroups the userGroups to set
	 */
	public void setUserGroups(List<UserGroup> userGroups) {
		this.userGroups = userGroups;
	}

	/**
	 * @return the userGroupsUnchanged
	 */
	public boolean isUserGroupsUnchanged() {
		return userGroupsUnchanged;
	}

	/**
	 * @param userGroupsUnchanged the userGroupsUnchanged to set
	 */
	public void setUserGroupsUnchanged(boolean userGroupsUnchanged) {
		this.userGroupsUnchanged = userGroupsUnchanged;
	}

	/**
	 * @return the ids
	 */
	public String getIds() {
		return ids;
	}

	/**
	 * @param ids the ids to set
	 */
	public void setIds(String ids) {
		this.ids = ids;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the activeUnchanged
	 */
	public boolean isActiveUnchanged() {
		return activeUnchanged;
	}

	/**
	 * @param activeUnchanged the activeUnchanged to set
	 */
	public void setActiveUnchanged(boolean activeUnchanged) {
		this.activeUnchanged = activeUnchanged;
	}

	@Override
	public String toString() {
		return "MultipleUserEdit{" + "ids=" + ids + '}';
	}
}
