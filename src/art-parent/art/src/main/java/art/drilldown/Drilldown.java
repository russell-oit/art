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
package art.drilldown;

import art.report.Report;
import java.io.Serializable;

/**
 * Represents a drilldown
 *
 * @author Timothy Anyona
 */
public class Drilldown implements Serializable {

	private static final long serialVersionUID = 1L;
	private int position;
	private String headerText;
	private String linkText;
	private String reportFormat;
	private boolean openInNewWindow;
	private int drilldownId;
	private Report drilldownReport;
	private int parentReportId;

	/**
	 * Get the value of parentReportId
	 *
	 * @return the value of parentReportId
	 */
	public int getParentReportId() {
		return parentReportId;
	}

	/**
	 * Set the value of parentReportId
	 *
	 * @param parentReportId new value of parentReportId
	 */
	public void setParentReportId(int parentReportId) {
		this.parentReportId = parentReportId;
	}

	/**
	 * @return the drilldownReport
	 */
	public Report getDrilldownReport() {
		return drilldownReport;
	}

	/**
	 * @param drilldownReport the drilldownReport to set
	 */
	public void setDrilldownReport(Report drilldownReport) {
		this.drilldownReport = drilldownReport;
	}

	/**
	 * Get the value of drilldownId
	 *
	 * @return the value of drilldownId
	 */
	public int getDrilldownId() {
		return drilldownId;
	}

	/**
	 * Set the value of drilldownId
	 *
	 * @param drilldownId new value of drilldownId
	 */
	public void setDrilldownId(int drilldownId) {
		this.drilldownId = drilldownId;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	/**
	 * @return the headerText
	 */
	public String getHeaderText() {
		return headerText;
	}

	/**
	 * @param headerText the headerText to set
	 */
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}

	/**
	 * @return the linkText
	 */
	public String getLinkText() {
		return linkText;
	}

	/**
	 * @param linkText the linkText to set
	 */
	public void setLinkText(String linkText) {
		this.linkText = linkText;
	}

	/**
	 * @return the reportFormat
	 */
	public String getReportFormat() {
		return reportFormat;
	}

	/**
	 * @param reportFormat the reportFormat to set
	 */
	public void setReportFormat(String reportFormat) {
		this.reportFormat = reportFormat;
	}

	/**
	 * @return the openInNewWindow
	 */
	public boolean isOpenInNewWindow() {
		return openInNewWindow;
	}

	/**
	 * @param openInNewWindow the openInNewWindow to set
	 */
	public void setOpenInNewWindow(boolean openInNewWindow) {
		this.openInNewWindow = openInNewWindow;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + this.drilldownId;
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
		final Drilldown other = (Drilldown) obj;
		if (this.drilldownId != other.drilldownId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Drilldown{" + "drilldownId=" + drilldownId + '}';
	}

}
