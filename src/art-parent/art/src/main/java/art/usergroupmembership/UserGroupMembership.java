/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.usergroupmembership;

import art.user.User;
import art.usergroup.UserGroup;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class to represent user group memberships
 *
 * @author Timothy Anyona
 */
public class UserGroupMembership implements Serializable {

	private static final long serialVersionUID = 1L;
	private User user;
	private UserGroup userGroup;

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the userGroup
	 */
	public UserGroup getUserGroup() {
		return userGroup;
	}

	/**
	 * @param userGroup the userGroup to set
	 */
	public void setUserGroup(UserGroup userGroup) {
		this.userGroup = userGroup;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 13 * hash + Objects.hashCode(this.user);
		hash = 13 * hash + Objects.hashCode(this.userGroup);
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
		final UserGroupMembership other = (UserGroupMembership) obj;
		if (!Objects.equals(this.user, other.user)) {
			return false;
		}
		if (!Objects.equals(this.userGroup, other.userGroup)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "UserGroupMembership{" + "user=" + user + ", userGroup=" + userGroup + '}';
	}
}
