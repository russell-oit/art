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
package art.usergroup;

import java.io.Serializable;
import java.util.Date;

/**
 * Class to represent a user group
 *
 * @author Timothy Anyona
 */
public class UserGroup implements Serializable {

	private static final long serialVersionUID = 1L;
	private int userGroupId;
	private String name;
	private String description;
	private int defaultReportGroup;
	private String startReport;
	private Date creationDate;
	private Date updateDate;
	private String createdBy;
	private String updatedBy;

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
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the updateDate
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate the updateDate to set
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	/**
	 * @return the userGroupId
	 */
	public int getUserGroupId() {
		return userGroupId;
	}

	/**
	 * @param userGroupId the userGroupId to set
	 */
	public void setUserGroupId(int userGroupId) {
		this.userGroupId = userGroupId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the defaultReportGroup
	 */
	public int getDefaultReportGroup() {
		return defaultReportGroup;
	}

	/**
	 * @param defaultReportGroup the defaultReportGroup to set
	 */
	public void setDefaultReportGroup(int defaultReportGroup) {
		this.defaultReportGroup = defaultReportGroup;
	}

	/**
	 * @return the startReport
	 */
	public String getStartReport() {
		return startReport;
	}

	/**
	 * @param startReport the startReport to set
	 */
	public void setStartReport(String startReport) {
		this.startReport = startReport;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 29 * hash + this.userGroupId;
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
		final UserGroup other = (UserGroup) obj;
		if (this.userGroupId != other.userGroupId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "UserGroup{" + "name=" + name + '}';
	}

}
