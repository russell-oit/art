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
	
	private int parentId; //used for import/export of linked records e.g. reports
	private int reportRuleId;
	private int reportId;
	private String reportColumn;
	private Rule rule;

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
