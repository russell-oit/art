/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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

/**
 * Options for blob store destinations i.e. s3, azure
 *
 * @author Timothy Anyona
 */
public class BlobStorageOptions {

	private boolean createContainer;
	private String containerLocation;

	/**
	 * @return the createContainer
	 */
	public boolean isCreateContainer() {
		return createContainer;
	}

	/**
	 * @param createContainer the createContainer to set
	 */
	public void setCreateContainer(boolean createContainer) {
		this.createContainer = createContainer;
	}

	/**
	 * @return the containerLocation
	 */
	public String getContainerLocation() {
		return containerLocation;
	}

	/**
	 * @param containerLocation the containerLocation to set
	 */
	public void setContainerLocation(String containerLocation) {
		this.containerLocation = containerLocation;
	}

}
