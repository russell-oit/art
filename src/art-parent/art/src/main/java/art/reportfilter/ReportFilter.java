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
package art.reportfilter;

import art.filter.Filter;
import java.io.Serializable;

/**
 * Class to represent a reportId filter
 *
 * @author Timothy Anyona
 */
public class ReportFilter implements Serializable {

	private static final long serialVersionUID = 1L;
	private int reportFilterId;
	private int reportId;
	private Filter filter;
	private String reportColumn;
	private String filterKey; //<rule id>-<rule name>. remove when rule_name column is removed

	/**
	 * Get the value of filterKey
	 *
	 * @return the value of filterKey
	 */
	public String getFilterKey() {
		return filterKey;
	}

	/**
	 * Set the value of filterKey
	 *
	 * @param filterKey new value of filterKey
	 */
	public void setFilterKey(String filterKey) {
		this.filterKey = filterKey;
	}

	/**
	 * @return the reportIdFilterId
	 */
	public int getReportFilterId() {
		return reportFilterId;
	}

	/**
	 * @param reportFilterId the reportIdFilterId to set
	 */
	public void setReportFilterId(int reportFilterId) {
		this.reportFilterId = reportFilterId;
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
	 * @return the filter
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * @return the reportIdColumn
	 */
	public String getReportColumn() {
		return reportColumn;
	}

	/**
	 * @param reportColumn the reportIdColumn to set
	 */
	public void setReportColumn(String reportColumn) {
		this.reportColumn = reportColumn;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 71 * hash + this.reportFilterId;
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
		final ReportFilter other = (ReportFilter) obj;
		if (this.reportFilterId != other.reportFilterId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ReportFilter{" + "reportFilterId=" + reportFilterId + '}';
	}

}
