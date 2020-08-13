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
package art.report;

import art.datasource.Datasource;
import art.reportgroup.ReportGroup;
import java.io.Serializable;
import java.util.List;

/**
 * Represents multiple report edit details
 *
 * @author Timothy Anyona
 */
public class MultipleReportEdit implements Serializable {

	private static final long serialVersionUID = 1L;
	private String ids;
	private boolean active;
	private boolean activeUnchanged = true;
	private boolean hidden;
	private boolean hiddenUnchanged = true;
	private String contactPerson;
	private boolean contactPersonUnchanged = true;
	private List<ReportGroup> reportGroups;
	private boolean reportGroupsUnchanged = true;
	private boolean omitTitleRow;
	private boolean omitTitleRowUnchanged = true;
	private Datasource datasource;
	private boolean datasourceUnchanged = true;

	/**
	 * @return the datasource
	 */
	public Datasource getDatasource() {
		return datasource;
	}

	/**
	 * @param datasource the datasource to set
	 */
	public void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}

	/**
	 * @return the datasourceUnchanged
	 */
	public boolean isDatasourceUnchanged() {
		return datasourceUnchanged;
	}

	/**
	 * @param datasourceUnchanged the datasourceUnchanged to set
	 */
	public void setDatasourceUnchanged(boolean datasourceUnchanged) {
		this.datasourceUnchanged = datasourceUnchanged;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return the hiddenUnchanged
	 */
	public boolean isHiddenUnchanged() {
		return hiddenUnchanged;
	}

	/**
	 * @param hiddenUnchanged the hiddenUnchanged to set
	 */
	public void setHiddenUnchanged(boolean hiddenUnchanged) {
		this.hiddenUnchanged = hiddenUnchanged;
	}

	/**
	 * @return the contactPerson
	 */
	public String getContactPerson() {
		return contactPerson;
	}

	/**
	 * @param contactPerson the contactPerson to set
	 */
	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	/**
	 * @return the contactPersonUnchanged
	 */
	public boolean isContactPersonUnchanged() {
		return contactPersonUnchanged;
	}

	/**
	 * @param contactPersonUnchanged the contactPersonUnchanged to set
	 */
	public void setContactPersonUnchanged(boolean contactPersonUnchanged) {
		this.contactPersonUnchanged = contactPersonUnchanged;
	}

	/**
	 * @return the reportGroups
	 */
	public List<ReportGroup> getReportGroups() {
		return reportGroups;
	}

	/**
	 * @param reportGroups the reportGroups to set
	 */
	public void setReportGroups(List<ReportGroup> reportGroups) {
		this.reportGroups = reportGroups;
	}

	/**
	 * @return the reportGroupsUnchanged
	 */
	public boolean isReportGroupsUnchanged() {
		return reportGroupsUnchanged;
	}

	/**
	 * @param reportGroupsUnchanged the reportGroupsUnchanged to set
	 */
	public void setReportGroupsUnchanged(boolean reportGroupsUnchanged) {
		this.reportGroupsUnchanged = reportGroupsUnchanged;
	}

	/**
	 * @return the omitTitleRow
	 */
	public boolean isOmitTitleRow() {
		return omitTitleRow;
	}

	/**
	 * @param omitTitleRow the omitTitleRow to set
	 */
	public void setOmitTitleRow(boolean omitTitleRow) {
		this.omitTitleRow = omitTitleRow;
	}

	/**
	 * @return the omitTitleRowUnchanged
	 */
	public boolean isOmitTitleRowUnchanged() {
		return omitTitleRowUnchanged;
	}

	/**
	 * @param omitTitleRowUnchanged the omitTitleRowUnchanged to set
	 */
	public void setOmitTitleRowUnchanged(boolean omitTitleRowUnchanged) {
		this.omitTitleRowUnchanged = omitTitleRowUnchanged;
	}

	/**
	 * @return the ids
	 */
	public String getIds() {
		return ids;
	}

	/**
	 * @param ids the ids to set
	 */
	public void setIds(String ids) {
		this.ids = ids;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the activeUnchanged
	 */
	public boolean isActiveUnchanged() {
		return activeUnchanged;
	}

	/**
	 * @param activeUnchanged the activeUnchanged to set
	 */
	public void setActiveUnchanged(boolean activeUnchanged) {
		this.activeUnchanged = activeUnchanged;
	}

	@Override
	public String toString() {
		return "MultipleReportEdit{" + "ids=" + ids + '}';
	}

}
