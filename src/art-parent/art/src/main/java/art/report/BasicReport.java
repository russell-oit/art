/*
 * ART. A Reporting Tool.
 * Copyright (C) 2019 Enrico Liboni <eliboni@users.sf.net>
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
package art.report;

import art.reportgroup.ReportGroup;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;

/**
 * Holds a few report properties
 *
 * @author Timothy Anyona
 */
public class BasicReport implements Serializable {

	private static final long serialVersionUID = 1L;

	private int reportId;
	private String name;
	private String name2;
	private String description2;
	private String dtActiveStatus;
	private String dtAction;
	private String reportGroupNames2;
	private String reportGroupNamesFilter;
	private String userReportGroupNames2;
	private String userReportGroupNamesFilter;

	public BasicReport(Report report) {
		if (report == null) {
			return;
		}

		reportId = report.getReportId();
		name = report.getName();

		initializeReportGroupNames(report.getReportGroups());
	}

	/**
	 * @return the userReportGroupNamesFilter
	 */
	public String getUserReportGroupNamesFilter() {
		return userReportGroupNamesFilter;
	}

	/**
	 * @return the userReportGroupNames2
	 */
	public String getUserReportGroupNames2() {
		return userReportGroupNames2;
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
	 * @return the name2
	 */
	public String getName2() {
		return name2;
	}

	/**
	 * @param name2 the name2 to set
	 */
	public void setName2(String name2) {
		this.name2 = name2;
	}

	/**
	 * @return the description2
	 */
	public String getDescription2() {
		return description2;
	}

	/**
	 * @param description2 the description2 to set
	 */
	public void setDescription2(String description2) {
		this.description2 = description2;
	}

	/**
	 * @return the dtActiveStatus
	 */
	public String getDtActiveStatus() {
		return dtActiveStatus;
	}

	/**
	 * @param dtActiveStatus the dtActiveStatus to set
	 */
	public void setDtActiveStatus(String dtActiveStatus) {
		this.dtActiveStatus = dtActiveStatus;
	}

	/**
	 * @return the dtAction
	 */
	public String getDtAction() {
		return dtAction;
	}

	/**
	 * @param dtAction the dtAction to set
	 */
	public void setDtAction(String dtAction) {
		this.dtAction = dtAction;
	}

	/**
	 * @return the reportGroupNames2
	 */
	public String getReportGroupNames2() {
		return reportGroupNames2;
	}

	/**
	 * @param reportGroupNames2 the reportGroupNames2 to set
	 */
	public void setReportGroupNames2(String reportGroupNames2) {
		this.reportGroupNames2 = reportGroupNames2;
	}

	/**
	 * Returns the id of the report for use with table actions
	 *
	 * @return the report id
	 */
	public int getDtId() {
		return reportId;
	}

	/**
	 * Returns the name of the report for use with table actions
	 *
	 * @return the report name
	 */
	public String getDtName() {
		return name;
	}

	/**
	 * Set flattened report group names
	 *
	 * @param reportGroups the report groups list
	 */
	private void initializeReportGroupNames(List<ReportGroup> reportGroups) {
		reportGroupNames2 = "";
		reportGroupNamesFilter = "~";
		userReportGroupNames2 = "";
		userReportGroupNamesFilter = "~";

		if (CollectionUtils.isNotEmpty(reportGroups)) {
			List<String> allGroupNames = new ArrayList<>();
			List<String> visibleGroupNames = new ArrayList<>();
			for (ReportGroup reportGroup : reportGroups) {
				String groupName = reportGroup.getName();
				allGroupNames.add(groupName);
				if (!reportGroup.isHidden()) {
					visibleGroupNames.add(groupName);
				}
			}

			final String DATA_SEPARATOR = ", ";
			String allReportGroupNames = StringUtils.join(allGroupNames, DATA_SEPARATOR);
			reportGroupNames2 = Encode.forHtml(allReportGroupNames);
			reportGroupNamesFilter = reportGroupNames2;

			if (!visibleGroupNames.isEmpty()) {
				String visibleReportGroupNames = StringUtils.join(visibleGroupNames, DATA_SEPARATOR);
				userReportGroupNames2 = Encode.forHtml(visibleReportGroupNames);
				userReportGroupNamesFilter = userReportGroupNames2;
			}
		}
	}

	/**
	 * Returns the string to be used for filtering report groups
	 *
	 * @return the string to be used for filtering report groups
	 */
	public String getReportGroupNamesFilter() {
		return reportGroupNamesFilter;
	}
}
