/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.job;

import java.io.Serializable;

/**
 *
 * @author Timothy Anyona
 */
public class MultipleJobEdit implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String ids;
	private boolean active;
	private boolean activeUnchanged = true;

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
