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
package art.job;

import art.user.User;
import java.io.Serializable;

/**
 * Represents details of a multiple job edit
 * 
 * @author Timothy Anyona
 */
public class MultipleJobEdit implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String ids;
	private boolean active;
	private boolean activeUnchanged = true;
	private User user;

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
		return "MultipleJobEdit{" + "ids=" + ids + '}';
	}
}
