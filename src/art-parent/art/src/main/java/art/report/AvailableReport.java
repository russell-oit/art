/**
 * Copyright (C) 2013 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.report;

import java.util.Date;

/**
 * Class to hold available report data as displayed in the reports.jsp page
 *
 * @author Timothy Anyona
 */
public class AvailableReport {

	private int reportId;
	private String name;
	private String description;
	private Date updateDate;
	private String reportGroupName;
	private int reportGroupId;

	/**
	 * Get the value of reportGroupId
	 *
	 * @return the value of reportGroupId
	 */
	public int getReportGroupId() {
		return reportGroupId;
	}

	/**
	 * Set the value of reportGroupId
	 *
	 * @param reportGroupId new value of reportGroupId
	 */
	public void setReportGroupId(int reportGroupId) {
		this.reportGroupId = reportGroupId;
	}


	/**
	 * Get the value of reportGroupName
	 *
	 * @return the value of reportGroupName
	 */
	public String getReportGroupName() {
		return reportGroupName;
	}

	/**
	 * Set the value of reportGroupName
	 *
	 * @param reportGroupName new value of reportGroupName
	 */
	public void setReportGroupName(String reportGroupName) {
		this.reportGroupName = reportGroupName;
	}

	/**
	 * @return the reportId
	 */
	public int getReportId() {
		return reportId;
	}

	/**
	 * @param reportId the reportId to set
	 */
	public void setReportId(int reportId) {
		this.reportId = reportId;
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

}
