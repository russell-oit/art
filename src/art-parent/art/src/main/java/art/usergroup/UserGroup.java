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
package art.usergroup;

import art.migration.PrefixTransformer;
import art.reportgroup.ReportGroup;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents a user group
 *
 * @author Timothy Anyona
 */
public class UserGroup implements Serializable {

	// Constants ---------------------------------------------------------------
	private static final long serialVersionUID = 1L;

	// Properties --------------------------------------------------------------
	@Parsed
	private int parentId; //used for import/export of linked records e.g. users
	@Parsed
	private int userGroupId;
	@Parsed
	private String name;
	@Parsed
	private String description;
	@Parsed
	private String startReport;
	private Date creationDate;
	private Date updateDate;
	private String createdBy;
	private String updatedBy;
	@Nested(headerTransformer = PrefixTransformer.class, args = "drg")
	private ReportGroup defaultReportGroup;

	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	// Getters/setters ---------------------------------------------------------
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
	public ReportGroup getDefaultReportGroup() {
		return defaultReportGroup;
	}

	/**
	 * @param defaultReportGroup the defaultReportGroup to set
	 */
	public void setDefaultReportGroup(ReportGroup defaultReportGroup) {
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

	// Object overrides --------------------------------------------------------
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
