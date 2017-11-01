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
import art.enums.PageOrientation;
import art.enums.ReportType;
import art.reportgroup.ReportGroup;
import art.reportoptions.GeneralReportOptions;
import art.reportoptions.Reporti18nOptions;
import art.utils.ArtUtils;
import art.utils.XmlParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a report
 *
 * @author Timothy Anyona
 */
public class Report implements Serializable {

	private static final long serialVersionUID = 1L;
	private int reportId;
	private String name;
	private String shortDescription;
	private String description;
	private int reportTypeId;
	private ReportGroup reportGroup;
	private Datasource datasource;
	private String contactPerson;
	private boolean usesRules;
	private boolean parametersInOutput;
	private String xAxisLabel;
	private String yAxisLabel;
	private String chartOptionsSetting;
	private String template;
	private int displayResultset;
	private String xmlaDatasource;
	private String xmlaCatalog;
	private Date creationDate;
	private Date updateDate;
	private String reportSource;
	private boolean useBlankXmlaPassword;
	private ChartOptions chartOptions;
	private String reportSourceHtml; //used with text reports
	private String createdBy;
	private String updatedBy;
	private ReportType reportType;
	private int groupColumn;
	private boolean active = true;
	private boolean hidden;
	private String defaultReportFormat;
	private String secondaryCharts;
	private String hiddenColumns;
	private String totalColumns;
	private String dateFormat;
	private String numberFormat;
	private String columnFormats;
	private String locale;
	private String nullNumberDisplay;
	private String nullStringDisplay;
	private int fetchSize;
	private String options;
	private PageOrientation pageOrientation = PageOrientation.Portrait;
	private boolean omitTitleRow;
	private boolean lovUseDynamicDatasource;
	private GeneralReportOptions generalOptions;
	private String openPassword;
	private String modifyPassword;
	private boolean useNoneOpenPassword; //only for use with ui
	private boolean useNoneModifyPassword; //only for use with ui

	/**
	 * @return the useNoneOpenPassword
	 */
	public boolean isUseNoneOpenPassword() {
		return useNoneOpenPassword;
	}

	/**
	 * @param useNoneOpenPassword the useNoneOpenPassword to set
	 */
	public void setUseNoneOpenPassword(boolean useNoneOpenPassword) {
		this.useNoneOpenPassword = useNoneOpenPassword;
	}

	/**
	 * @return the useNoneModifyPassword
	 */
	public boolean isUseNoneModifyPassword() {
		return useNoneModifyPassword;
	}

	/**
	 * @param useNoneModifyPassword the useNoneModifyPassword to set
	 */
	public void setUseNoneModifyPassword(boolean useNoneModifyPassword) {
		this.useNoneModifyPassword = useNoneModifyPassword;
	}

	/**
	 * @return the openPassword
	 */
	public String getOpenPassword() {
		return openPassword;
	}

	/**
	 * @param openPassword the openPassword to set
	 */
	public void setOpenPassword(String openPassword) {
		this.openPassword = openPassword;
	}

	/**
	 * @return the modifyPassword
	 */
	public String getModifyPassword() {
		return modifyPassword;
	}

	/**
	 * @param modifyPassword the modifyPassword to set
	 */
	public void setModifyPassword(String modifyPassword) {
		this.modifyPassword = modifyPassword;
	}

	/**
	 * @return the generalOptions
	 */
	public GeneralReportOptions getGeneralOptions() {
		return generalOptions;
	}

	/**
	 * @param generalOptions the generalOptions to set
	 */
	public void setGeneralOptions(GeneralReportOptions generalOptions) {
		this.generalOptions = generalOptions;
	}

	/**
	 * @return the lovUseDynamicDatasource
	 */
	public boolean isLovUseDynamicDatasource() {
		return lovUseDynamicDatasource;
	}

	/**
	 * @param lovUseDynamicDatasource the lovUseDynamicDatasource to set
	 */
	public void setLovUseDynamicDatasource(boolean lovUseDynamicDatasource) {
		this.lovUseDynamicDatasource = lovUseDynamicDatasource;
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
	 * @return the pageOrientation
	 */
	public PageOrientation getPageOrientation() {
		return pageOrientation;
	}

	/**
	 * @param pageOrientation the pageOrientation to set
	 */
	public void setPageOrientation(PageOrientation pageOrientation) {
		this.pageOrientation = pageOrientation;
	}

	/**
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * @return the fetchSize
	 */
	public int getFetchSize() {
		return fetchSize;
	}

	/**
	 * @param fetchSize the fetchSize to set
	 */
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	/**
	 * @return the nullNumberDisplay
	 */
	public String getNullNumberDisplay() {
		return nullNumberDisplay;
	}

	/**
	 * @param nullNumberDisplay the nullNumberDisplay to set
	 */
	public void setNullNumberDisplay(String nullNumberDisplay) {
		this.nullNumberDisplay = nullNumberDisplay;
	}

	/**
	 * @return the nullStringDisplay
	 */
	public String getNullStringDisplay() {
		return nullStringDisplay;
	}

	/**
	 * @param nullStringDisplay the nullStringDisplay to set
	 */
	public void setNullStringDisplay(String nullStringDisplay) {
		this.nullStringDisplay = nullStringDisplay;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return the numberFormat
	 */
	public String getNumberFormat() {
		return numberFormat;
	}

	/**
	 * @param numberFormat the numberFormat to set
	 */
	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}

	/**
	 * @return the dateFormat
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @return the columnFormats
	 */
	public String getColumnFormats() {
		return columnFormats;
	}

	/**
	 * @param columnFormats the columnFormats to set
	 */
	public void setColumnFormats(String columnFormats) {
		this.columnFormats = columnFormats;
	}

	/**
	 * @return the totalColumns
	 */
	public String getTotalColumns() {
		return totalColumns;
	}

	/**
	 * @param totalColumns the totalColumns to set
	 */
	public void setTotalColumns(String totalColumns) {
		this.totalColumns = totalColumns;
	}

	/**
	 * @return the hiddenColumns
	 */
	public String getHiddenColumns() {
		return hiddenColumns;
	}

	/**
	 * @param hiddenColumns the hiddenColumns to set
	 */
	public void setHiddenColumns(String hiddenColumns) {
		this.hiddenColumns = hiddenColumns;
	}

	/**
	 * @return the secondaryCharts
	 */
	public String getSecondaryCharts() {
		return secondaryCharts;
	}

	/**
	 * @param secondaryCharts the secondaryCharts to set
	 */
	public void setSecondaryCharts(String secondaryCharts) {
		this.secondaryCharts = secondaryCharts;
	}

	/**
	 * @return the defaultReportFormat
	 */
	public String getDefaultReportFormat() {
		return defaultReportFormat;
	}

	/**
	 * @param defaultReportFormat the defaultReportFormat to set
	 */
	public void setDefaultReportFormat(String defaultReportFormat) {
		this.defaultReportFormat = defaultReportFormat;
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
	 * @return the groupColumn
	 */
	public int getGroupColumn() {
		return groupColumn;
	}

	/**
	 * @param groupColumn the groupColumn to set
	 */
	public void setGroupColumn(int groupColumn) {
		this.groupColumn = groupColumn;
	}

	/**
	 * @return the reportType
	 */
	public ReportType getReportType() {
		return reportType;
	}

	/**
	 * @param reportType the reportType to set
	 */
	public void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}

	/**
	 * Determine whether this is an lov report
	 *
	 * @return
	 */
	public boolean isLov() {
		ReportType reportTypeEnum = ReportType.toEnum(reportTypeId);

		if (reportTypeEnum == ReportType.LovDynamic || reportTypeEnum == ReportType.LovDynamic) {
			return true;
		} else {
			return false;
		}
	}

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
	 * Get the value of reportSourceHtml
	 *
	 * @return the value of reportSourceHtml
	 */
	public String getReportSourceHtml() {
		return reportSourceHtml;
	}

	/**
	 * Set the value of reportSourceHtml
	 *
	 * @param reportSourceHtml new value of reportSourceHtml
	 */
	public void setReportSourceHtml(String reportSourceHtml) {
		this.reportSourceHtml = reportSourceHtml;
	}

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
	 * @return the reportTypeId
	 */
	public int getReportTypeId() {
		return reportTypeId;
	}

	/**
	 * @param reportTypeId the reportTypeId to set
	 */
	public void setReportTypeId(int reportTypeId) {
		this.reportTypeId = reportTypeId;
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
	 * @return the usesRules
	 */
	public boolean isUsesRules() {
		return usesRules;
	}

	/**
	 * @param usesRules the usesRules to set
	 */
	public void setUsesRules(boolean usesRules) {
		this.usesRules = usesRules;
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
		return "Report{" + "name=" + name + "}";
	}

	/**
	 * Returns the report ids for reports defined within a dashboard report
	 *
	 * @return the report ids for reports defined within a dashboard report
	 */
	public List<Integer> getDashboardReportIds() {
		List<Integer> reportIds = new ArrayList<>();

		if (StringUtils.isBlank(reportSource)) {
			return Collections.emptyList();
		}

		List<String> reportIdStrings = XmlParser.getXmlElementValues(reportSource, "OBJECTID");
		reportIdStrings.addAll(XmlParser.getXmlElementValues(reportSource, "QUERYID"));
		reportIdStrings.addAll(XmlParser.getXmlElementValues(reportSource, "REPORTID"));

		//remove any parameter definitions, remaining only with the report id
		for (int i = 0; i < reportIdStrings.size(); i++) {
			String reportIdSetting = reportIdStrings.get(i);
			String reportId = StringUtils.substringBefore(reportIdSetting, "&");
			reportIdStrings.set(i, reportId);
		}

		for (String id : reportIdStrings) {
			reportIds.add(Integer.valueOf(id));
		}

		return reportIds;
	}

	/**
	 * Returns the name to use for this report, given a particular locale,
	 * taking into consideration the i18n options defined for the report
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized name
	 * @throws java.io.IOException
	 */
	public String getLocalizedName(Locale locale) throws IOException {
		String localizedName = null;

		if (generalOptions != null && locale != null) {
			Reporti18nOptions i18nOptions = generalOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nNameOptions = i18nOptions.getName();
				localizedName = ArtUtils.getLocalizedValue(locale, i18nNameOptions);
			}
		}

		if (localizedName == null) {
			localizedName = name;
		}

		return localizedName;
	}

	/**
	 * Returns the short description to use for this report, given a particular
	 * locale, taking into consideration the i18n options defined for the report
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized short description
	 * @throws java.io.IOException
	 */
	public String getLocalizedShortDescription(Locale locale) throws IOException {
		String localizedShortDescription = null;

		if (generalOptions != null && locale != null) {
			Reporti18nOptions i18nOptions = generalOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nShortDescriptionOptions = i18nOptions.getShortDescription();
				localizedShortDescription = ArtUtils.getLocalizedValue(locale, i18nShortDescriptionOptions);
			}
		}

		if (localizedShortDescription == null) {
			localizedShortDescription = shortDescription;
		}

		return localizedShortDescription;
	}

	/**
	 * Returns the description to use for this report, given a particular
	 * locale, taking into consideration the i18n options defined for the report
	 *
	 * @param locale the locale object for the relevant locale
	 * @return the localized description
	 * @throws java.io.IOException
	 */
	public String getLocalizedDescription(Locale locale) throws IOException {
		String localizedDescription = null;

		if (generalOptions != null && locale != null) {
			Reporti18nOptions i18nOptions = generalOptions.getI18n();
			if (i18nOptions != null) {
				List<Map<String, String>> i18nDescriptionOptions = i18nOptions.getDescription();
				localizedDescription = ArtUtils.getLocalizedValue(locale, i18nDescriptionOptions);
			}
		}

		if (localizedDescription == null) {
			localizedDescription = description;
		}

		return localizedDescription;
	}

	/**
	 * Loads the general report options object from the options string
	 *
	 * @throws java.io.IOException
	 */
	public void loadGeneralOptions() throws IOException {
		if (StringUtils.isNotBlank(options)) {
			ObjectMapper mapper = new ObjectMapper();
			generalOptions = mapper.readValue(options, GeneralReportOptions.class);
		}
	}

}
