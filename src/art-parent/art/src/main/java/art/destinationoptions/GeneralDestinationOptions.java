/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.destinationoptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents options applicable to a number of destination types
 *
 * @author Timothy Anyona
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneralDestinationOptions {

	private boolean createDirectories = true;

	/**
	 * @return the createDirectories
	 */
	public boolean isCreateDirectories() {
		return createDirectories;
	}

	/**
	 * @param createDirectories the createDirectories to set
	 */
	public void setCreateDirectories(boolean createDirectories) {
		this.createDirectories = createDirectories;
	}

}
