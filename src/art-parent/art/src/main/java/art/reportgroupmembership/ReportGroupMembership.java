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
package art.reportgroupmembership;

import art.report.Report;
import art.reportgroup.ReportGroup;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents report group memberships
 *
 * @author Timothy Anyona
 */
public class ReportGroupMembership implements Serializable {

	private static final long serialVersionUID = 1L;
	private Report report;
	private ReportGroup reportGroup;

	/**
	 * @return the report
	 */
	public Report getReport() {
		return report;
	}

	/**
	 * @param report the report to set
	 */
	public void setReport(Report report) {
		this.report = report;
	}

	/**
	 * @return the reportGroup
	 */
	public ReportGroup getReportGroup() {
		return reportGroup;
	}

	/**
	 * @param reportGroup the reportGroup to set
	 */
	public void setReportGroup(ReportGroup reportGroup) {
		this.reportGroup = reportGroup;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 13 * hash + Objects.hashCode(this.report);
		hash = 13 * hash + Objects.hashCode(this.reportGroup);
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
		final ReportGroupMembership other = (ReportGroupMembership) obj;
		if (!Objects.equals(this.report, other.report)) {
			return false;
		}
		if (!Objects.equals(this.reportGroup, other.reportGroup)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ReportGroupMembership{" + "report=" + report + ", reportGroup=" + reportGroup + '}';
	}
}
