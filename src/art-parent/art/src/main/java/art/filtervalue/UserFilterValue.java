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
import art.user.User;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class to represent user filter values
 *
 * @author Timothy Anyona
 */
public class UserFilterValue implements Serializable {

	private static final long serialVersionUID = 1L;
	private User user;
	private Filter filter;
	private String filterValue;
	private String filterValueKey;

	/**
	 * @return the filterValueKey
	 */
	public String getFilterValueKey() {
		return filterValueKey;
	}

	/**
	 * @param filterValueKey the filterValueKey to set
	 */
	public void setFilterValueKey(String filterValueKey) {
		this.filterValueKey = filterValueKey;
	}

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
		int hash = 7;
		hash = 79 * hash + Objects.hashCode(this.filterValueKey);
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
		final UserFilterValue other = (UserFilterValue) obj;
		if (!Objects.equals(this.filterValueKey, other.filterValueKey)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "UserFilterValue{" + "filterValueKey=" + filterValueKey + '}';
	}
}
