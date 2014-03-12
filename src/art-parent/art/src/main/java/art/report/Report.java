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

import art.datasource.Datasource;
import art.enums.ReportStatus;
import art.reportgroup.ReportGroup;
import java.io.Serializable;
import java.util.Date;

/**
 * Class to represent a report
 *
 * @author Timothy Anyona
 */
public class Report implements Serializable {

	private static final long serialVersionUID = 1L;
	private int reportId;
	private String name;
	private String shortDescription;
	private String description;
	private int reportType;
	private ReportGroup reportGroup;
	private Datasource datasource;
	private String contactPerson;
	private boolean useRules;
	private ReportStatus reportStatus;
	private boolean parametersInOutput;
	private String xAxisLabel;
	private String yAxisLabel;
	private String chartOptionsSetting;
	private String template;
	private int displayResultset;
	private String xmlaUrl;
	private String xmlaDatasource;
	private String xmlaCatalog;
	private String xmlaUsername;
	private String xmlaPassword;
	private Date creationDate;
	private Date updateDate;
	private String reportSource;
	private boolean useBlankXmlaPassword;
	private ChartOptions chartOptions;

	/**
	 * Get the value of chartOptions
	 *
	 * @return the value of chartOptions
	 */
	public ChartOptions getChartOptions() {
		return chartOptions;
	}

	/**
	 * Set the value of chartOptions
	 *
	 * @param chartOptions new value of chartOptions
	 */
	public void setChartOptions(ChartOptions chartOptions) {
		this.chartOptions = chartOptions;
	}

	/**
	 * Get the value of useBlankXmlaPassword
	 *
	 * @return the value of useBlankXmlaPassword
	 */
	public boolean isUseBlankXmlaPassword() {
		return useBlankXmlaPassword;
	}

	/**
	 * Set the value of useBlankXmlaPassword
	 *
	 * @param useBlankXmlaPassword new value of useBlankXmlaPassword
	 */
	public void setUseBlankXmlaPassword(boolean useBlankXmlaPassword) {
		this.useBlankXmlaPassword = useBlankXmlaPassword;
	}

	/**
	 * Get the value of reportSource
	 *
	 * @return the value of reportSource
	 */
	public String getReportSource() {
		return reportSource;
	}

	/**
	 * Set the value of reportSource
	 *
	 * @param reportSource new value of reportSource
	 */
	public void setReportSource(String reportSource) {
		this.reportSource = reportSource;
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
	 * @return the shortDescription
	 */
	public String getShortDescription() {
		return shortDescription;
	}

	/**
	 * @param shortDescription the shortDescription to set
	 */
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
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
	 * @return the reportType
	 */
	public int getReportType() {
		return reportType;
	}

	/**
	 * @param reportType the reportType to set
	 */
	public void setReportType(int reportType) {
		this.reportType = reportType;
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
	 * @return the useRules
	 */
	public boolean isUseRules() {
		return useRules;
	}

	/**
	 * @param useRules the useRules to set
	 */
	public void setUseRules(boolean useRules) {
		this.useRules = useRules;
	}

	/**
	 * @return the reportStatus
	 */
	public ReportStatus getReportStatus() {
		return reportStatus;
	}

	/**
	 * @param reportStatus the reportStatus to set
	 */
	public void setReportStatus(ReportStatus reportStatus) {
		this.reportStatus = reportStatus;
	}

	/**
	 * @return the parametersInOutput
	 */
	public boolean isParametersInOutput() {
		return parametersInOutput;
	}

	/**
	 * @param parametersInOutput the parametersInOutput to set
	 */
	public void setParametersInOutput(boolean parametersInOutput) {
		this.parametersInOutput = parametersInOutput;
	}

	/**
	 * @return the xAxisLabel
	 */
	public String getxAxisLabel() {
		return xAxisLabel;
	}

	/**
	 * @param xAxisLabel the xAxisLabel to set
	 */
	public void setxAxisLabel(String xAxisLabel) {
		this.xAxisLabel = xAxisLabel;
	}

	/**
	 * @return the yAxisLabel
	 */
	public String getyAxisLabel() {
		return yAxisLabel;
	}

	/**
	 * @param yAxisLabel the yAxisLabel to set
	 */
	public void setyAxisLabel(String yAxisLabel) {
		this.yAxisLabel = yAxisLabel;
	}

	/**
	 * @return the chartOptionsSetting
	 */
	public String getChartOptionsSetting() {
		return chartOptionsSetting;
	}

	/**
	 * @param chartOptionsSetting the chartOptionsSetting to set
	 */
	public void setChartOptionsSetting(String chartOptionsSetting) {
		this.chartOptionsSetting = chartOptionsSetting;
	}

	/**
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * @param template the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * @return the displayResultset
	 */
	public int getDisplayResultset() {
		return displayResultset;
	}

	/**
	 * @param displayResultset the displayResultset to set
	 */
	public void setDisplayResultset(int displayResultset) {
		this.displayResultset = displayResultset;
	}

	/**
	 * @return the xmlaUrl
	 */
	public String getXmlaUrl() {
		return xmlaUrl;
	}

	/**
	 * @param xmlaUrl the xmlaUrl to set
	 */
	public void setXmlaUrl(String xmlaUrl) {
		this.xmlaUrl = xmlaUrl;
	}

	/**
	 * @return the xmlaDatasource
	 */
	public String getXmlaDatasource() {
		return xmlaDatasource;
	}

	/**
	 * @param xmlaDatasource the xmlaDatasource to set
	 */
	public void setXmlaDatasource(String xmlaDatasource) {
		this.xmlaDatasource = xmlaDatasource;
	}

	/**
	 * @return the xmlaCatalog
	 */
	public String getXmlaCatalog() {
		return xmlaCatalog;
	}

	/**
	 * @param xmlaCatalog the xmlaCatalog to set
	 */
	public void setXmlaCatalog(String xmlaCatalog) {
		this.xmlaCatalog = xmlaCatalog;
	}

	/**
	 * @return the xmlaUsername
	 */
	public String getXmlaUsername() {
		return xmlaUsername;
	}

	/**
	 * @param xmlaUsername the xmlaUsername to set
	 */
	public void setXmlaUsername(String xmlaUsername) {
		this.xmlaUsername = xmlaUsername;
	}

	/**
	 * @return the xmlaPassword
	 */
	public String getXmlaPassword() {
		return xmlaPassword;
	}

	/**
	 * @param xmlaPassword the xmlaPassword to set
	 */
	public void setXmlaPassword(String xmlaPassword) {
		this.xmlaPassword = xmlaPassword;
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

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 73 * hash + this.reportId;
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
		final Report other = (Report) obj;
		if (this.reportId != other.reportId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Report [name=" + name + "]";
	}

}
