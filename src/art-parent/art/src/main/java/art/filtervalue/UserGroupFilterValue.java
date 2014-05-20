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
package art.filtervalue;

import art.filter.Filter;
import art.usergroup.UserGroup;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class to represent user group filter values
 *
 * @author Timothy Anyona
 */
public class UserGroupFilterValue implements Serializable {

	private static final long serialVersionUID = 1L;
	private UserGroup userGroup;
	private Filter filter;
	private String filterValue;
	private String filterValueKey;

	/**
	 * Get the value of filterValueKey
	 *
	 * @return the value of filterValueKey
	 */
	public String getFilterValueKey() {
		return filterValueKey;
	}

	/**
	 * Set the value of filterValueKey
	 *
	 * @param filterValueKey new value of filterValueKey
	 */
	public void setFilterValueKey(String filterValueKey) {
		this.filterValueKey = filterValueKey;
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

	/**
	 * @return the filter
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * @return the filterValue
	 */
	public String getFilterValue() {
		return filterValue;
	}

	/**
	 * @param filterValue the filterValue to set
	 */
	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 67 * hash + Objects.hashCode(this.filterValueKey);
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
		final UserGroupFilterValue other = (UserGroupFilterValue) obj;
		if (!Objects.equals(this.filterValueKey, other.filterValueKey)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "UserGroupFilterValue{" + "filterValueKey=" + filterValueKey + '}';
	}
}
