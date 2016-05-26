/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.reportrule;

import art.rule.Rule;
import java.io.Serializable;

/**
 * Represents a report rule
 *
 * @author Timothy Anyona
 */
public class ReportRule implements Serializable {

	private static final long serialVersionUID = 1L;
	private int reportRuleId;
	private int reportId;
	private Rule rule;
	private String reportColumn;

	/**
	 * @return the reportRuleId
	 */
	public int getReportRuleId() {
		return reportRuleId;
	}

	/**
	 * @param reportRuleId the reportRuleId to set
	 */
	public void setReportRuleId(int reportRuleId) {
		this.reportRuleId = reportRuleId;
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
	 * @return the rule
	 */
	public Rule getRule() {
		return rule;
	}

	/**
	 * @param rule the rule to set
	 */
	public void setRule(Rule rule) {
		this.rule = rule;
	}

	/**
	 * @return the reportColumn
	 */
	public String getReportColumn() {
		return reportColumn;
	}

	/**
	 * @param reportColumn the reportColumn to set
	 */
	public void setReportColumn(String reportColumn) {
		this.reportColumn = reportColumn;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 71 * hash + this.reportRuleId;
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
		final ReportRule other = (ReportRule) obj;
		if (this.reportRuleId != other.reportRuleId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ReportRule{" + "reportRuleId=" + reportRuleId + '}';
	}
}
