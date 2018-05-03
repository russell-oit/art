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

import art.accessright.UserGroupReportRight;
import art.accessright.UserReportRight;
import art.datasource.Datasource;
import art.drilldown.Drilldown;
import art.encryptor.Encryptor;
import art.enums.EncryptorType;
import art.enums.PageOrientation;
import art.enums.ReportType;
import art.reportgroup.ReportGroup;
import art.reportoptions.GeneralReportOptions;
import art.reportoptions.Reporti18nOptions;
import art.encryption.AESCrypt;
import art.encryption.AesEncryptor;
import art.migration.PrefixTransformer;
import art.reportoptions.C3Options;
import art.reportoptions.CloneOptions;
import art.reportoptions.PlotlyOptions;
import art.reportparameter.ReportParameter;
import art.reportrule.ReportRule;
import art.ruleValue.UserGroupRuleValue;
import art.ruleValue.UserRuleValue;
import art.servlets.Config;
import art.utils.ArtUtils;
import art.utils.XmlParser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.annotations.Nested;
import com.univocity.parsers.annotations.Parsed;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openpgp.PGPException;
import org.c02e.jpgpj.HashingAlgorithm;
import org.c02e.jpgpj.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a report
 *
 * @author Timothy Anyona
 */
public class Report implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(Report.class);

	private static final long serialVersionUID = 1L;
	@Parsed
	private int reportId;
	@Parsed
	private String name;
	@Parsed
	private String shortDescription;
	@Parsed
	private String description;
	private int reportTypeId;
	@Parsed
	private String contactPerson;
	@Parsed
	private boolean usesRules;
	@Parsed
	private boolean parametersInOutput;
	@Parsed
	private String xAxisLabel;
	@Parsed
	private String yAxisLabel;
	@Parsed
	private String chartOptionsSetting;
	@Parsed
	private String template;
	@Parsed
	private int displayResultset;
	@Parsed
	private String xmlaDatasource;
	@Parsed
	private String xmlaCatalog;
	private Date creationDate;
	private Date updateDate;
	@Parsed
	private String reportSource;
	private boolean useBlankXmlaPassword;
	@JsonIgnore
	private ChartOptions chartOptions;
	@JsonIgnore
	private String reportSourceHtml; //used with text reports
	private String createdBy;
	private String updatedBy;
	@Parsed
	private ReportType reportType;
	@Parsed
	private int groupColumn;
	@Parsed
	private boolean active = true;
	@Parsed
	private boolean hidden;
	@Parsed
	private String defaultReportFormat;
	@Parsed
	private String secondaryCharts;
	@Parsed
	private String hiddenColumns;
	@Parsed
	private String totalColumns;
	@Parsed
	private String dateFormat;
	@Parsed
	private String numberFormat;
	@Parsed
	private String columnFormats;
	@Parsed
	private String locale;
	@Parsed
	private String nullNumberDisplay;
	@Parsed
	private String nullStringDisplay;
	@Parsed
	private int fetchSize;
	@Parsed
	private String options;
	@Parsed
	private PageOrientation pageOrientation = PageOrientation.Portrait;
	@Parsed
	private boolean omitTitleRow;
	@Parsed
	private boolean lovUseDynamicDatasource;
	@JsonIgnore
	private GeneralReportOptions generalOptions;
	@Parsed
	private String openPassword;
	@Parsed
	private String modifyPassword;
	private boolean useNoneOpenPassword; //only for use with ui
	private boolean useNoneModifyPassword; //only for use with ui
	private Report sourceReport;
	@Parsed
	private int sourceReportId;
	@JsonIgnore
	private CloneOptions cloneOptions;
	private List<ReportGroup> reportGroups;
	@Parsed
	private boolean clearTextPasswords;
	private Boolean dummyBoolean; //used for the test report functionality
	@Parsed
	private boolean useGroovy;
	@Parsed
	private String pivotTableJsSavedOptions;
	@Parsed
	private String gridstackSavedOptions;
	@Nested(headerTransformer = PrefixTransformer.class, args = "datasource")
	private Datasource datasource;
	@Nested(headerTransformer = PrefixTransformer.class, args = "encryptor")
	private Encryptor encryptor;
	private List<ReportParameter> reportParams; //used in import/export
	private List<UserRuleValue> userRuleValues; //used in import/export
	private List<UserGroupRuleValue> userGroupRuleValues; //used in import/export
	private List<ReportRule> reportRules; //used in import/export
	private List<UserReportRight> userReportRights; //used in import/export
	private List<UserGroupReportRight> userGroupReportRights; //used in import/export
	private List<Drilldown> drilldowns; //used in import/export
	private String dtName;
	private String dtActiveStatus;
	private String dtAction;
	private String dtRowId; //used to prevent Unrecognized field error with json import. alternative is to use jsonignoreproperties on the class
	private String reportGroupNames; //used to prevent Unrecognized field error with json import. alternative is to use jsonignoreproperties on the class

	/**
	 * @return the dtName
	 */
	public String getDtName() {
		return dtName;
	}

	/**
	 * @param dtName the dtName to set
	 */
	public void setDtName(String dtName) {
		this.dtName = dtName;
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
	 * @return the gridstackSavedOptions
	 */
	public String getGridstackSavedOptions() {
		return gridstackSavedOptions;
	}

	/**
	 * @param gridstackSavedOptions the gridstackSavedOptions to set
	 */
	public void setGridstackSavedOptions(String gridstackSavedOptions) {
		this.gridstackSavedOptions = gridstackSavedOptions;
	}

	/**
	 * @return the pivotTableJsSavedOptions
	 */
	public String getPivotTableJsSavedOptions() {
		return pivotTableJsSavedOptions;
	}

	/**
	 * @param pivotTableJsSavedOptions the pivotTableJsSavedOptions to set
	 */
	public void setPivotTableJsSavedOptions(String pivotTableJsSavedOptions) {
		this.pivotTableJsSavedOptions = pivotTableJsSavedOptions;
	}

	/**
	 * @return the useGroovy
	 */
	public boolean isUseGroovy() {
		return useGroovy;
	}

	/**
	 * @param useGroovy the useGroovy to set
	 */
	public void setUseGroovy(boolean useGroovy) {
		this.useGroovy = useGroovy;
	}

	/**
	 * @return the dummyBoolean
	 */
	public Boolean getDummyBoolean() {
		return dummyBoolean;
	}

	/**
	 * @param dummyBoolean the dummyBoolean to set
	 */
	public void setDummyBoolean(Boolean dummyBoolean) {
		this.dummyBoolean = dummyBoolean;
	}

	/**
	 * @return the drilldowns
	 */
	public List<Drilldown> getDrilldowns() {
		return drilldowns;
	}

	/**
	 * @param drilldowns the drilldowns to set
	 */
	public void setDrilldowns(List<Drilldown> drilldowns) {
		this.drilldowns = drilldowns;
	}

	/**
	 * @return the userReportRights
	 */
	public List<UserReportRight> getUserReportRights() {
		return userReportRights;
	}

	/**
	 * @param userReportRights the userReportRights to set
	 */
	public void setUserReportRights(List<UserReportRight> userReportRights) {
		this.userReportRights = userReportRights;
	}

	/**
	 * @return the userGroupReportRights
	 */
	public List<UserGroupReportRight> getUserGroupReportRights() {
		return userGroupReportRights;
	}

	/**
	 * @param userGroupReportRights the userGroupReportRights to set
	 */
	public void setUserGroupReportRights(List<UserGroupReportRight> userGroupReportRights) {
		this.userGroupReportRights = userGroupReportRights;
	}

	/**
	 * @return the reportRules
	 */
	public List<ReportRule> getReportRules() {
		return reportRules;
	}

	/**
	 * @param reportRules the reportRules to set
	 */
	public void setReportRules(List<ReportRule> reportRules) {
		this.reportRules = reportRules;
	}

	/**
	 * @return the userRuleValues
	 */
	public List<UserRuleValue> getUserRuleValues() {
		return userRuleValues;
	}

	/**
	 * @param userRuleValues the userRuleValues to set
	 */
	public void setUserRuleValues(List<UserRuleValue> userRuleValues) {
		this.userRuleValues = userRuleValues;
	}

	/**
	 * @return the userGroupRuleValues
	 */
	public List<UserGroupRuleValue> getUserGroupRuleValues() {
		return userGroupRuleValues;
	}

	/**
	 * @param userGroupRuleValues the userGroupRuleValues to set
	 */
	public void setUserGroupRuleValues(List<UserGroupRuleValue> userGroupRuleValues) {
		this.userGroupRuleValues = userGroupRuleValues;
	}

	/**
	 * @return the reportParams
	 */
	public List<ReportParameter> getReportParams() {
		return reportParams;
	}

	/**
	 * @param reportParams the reportParams to set
	 */
	public void setReportParams(List<ReportParameter> reportParams) {
		this.reportParams = reportParams;
	}

	/**
	 * @return the clearTextPasswords
	 */
	public boolean isClearTextPasswords() {
		return clearTextPasswords;
	}

	/**
	 * @param clearTextPasswords the clearTextPasswords to set
	 */
	public void setClearTextPasswords(boolean clearTextPasswords) {
		this.clearTextPasswords = clearTextPasswords;
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
	 * @return the cloneOptions
	 */
	public CloneOptions getCloneOptions() {
		return cloneOptions;
	}

	/**
	 * @param cloneOptions the cloneOptions to set
	 */
	public void setCloneOptions(CloneOptions cloneOptions) {
		this.cloneOptions = cloneOptions;
	}

	/**
	 * @return the sourceReportId
	 */
	public int getSourceReportId() {
		return sourceReportId;
	}

	/**
	 * @param sourceReportId the sourceReportId to set
	 */
	public void setSourceReportId(int sourceReportId) {
		this.sourceReportId = sourceReportId;
	}

	/**
	 * @return the sourceReport
	 */
	public Report getSourceReport() {
		return sourceReport;
	}

	/**
	 * @param sourceReport the sourceReport to set
	 */
	public void setSourceReport(Report sourceReport) {
		this.sourceReport = sourceReport;
	}

	/**
	 * @return the encryptor
	 */
	public Encryptor getEncryptor() {
		return encryptor;
	}

	/**
	 * @param encryptor the encryptor to set
	 */
	public void setEncryptor(Encryptor encryptor) {
		this.encryptor = encryptor;
	}

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
	 * Returns <code>true</code> if this is an lov report
	 *
	 * @return <code>true</code> if this is an lov report
	 */
	@JsonIgnore
	public boolean isLov() {
		ReportType reportTypeEnum = ReportType.toEnum(reportTypeId);

		switch (reportTypeEnum) {
			case LovDynamic:
			case LovStatic:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Returns the report ids for reports defined within a dashboard report
	 *
	 * @return the report ids for reports defined within a dashboard report
	 */
	@JsonIgnore
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
			String reportIdString = StringUtils.substringBefore(reportIdSetting, "&");
			reportIdStrings.set(i, reportIdString);
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
		C3Options defaultC3Options = new C3Options();
		List<String> chartTypes = new ArrayList<>(Arrays.asList("all"));
		defaultC3Options.setChartTypes(chartTypes);

		PlotlyOptions defaultPlotlyOptions = new PlotlyOptions();
		String defaultPlotlyMode = "lines+markers";
		defaultPlotlyOptions.setType("scatter");
		defaultPlotlyOptions.setMode(defaultPlotlyMode);
		defaultPlotlyOptions.setChartTypes(chartTypes);

		if (StringUtils.isBlank(options)) {
			generalOptions = new GeneralReportOptions();
			generalOptions.setC3(defaultC3Options);
			generalOptions.setPlotly(defaultPlotlyOptions);
		} else {
			ObjectMapper mapper = new ObjectMapper();
			generalOptions = mapper.readValue(options, GeneralReportOptions.class);
			C3Options c3Options = generalOptions.getC3();
			if (c3Options == null) {
				generalOptions.setC3(defaultC3Options);
			}

			PlotlyOptions plotlyOptions = generalOptions.getPlotly();
			if (plotlyOptions == null) {
				generalOptions.setPlotly(defaultPlotlyOptions);
			} else if (plotlyOptions.getMode() == null) {
				plotlyOptions.setMode(defaultPlotlyMode);
			}
		}
	}

	/**
	 * Encrypts a file using the encryptor defined on the report
	 *
	 * @param finalFileName the full path of the final file name of the file
	 * e.g. c:\test\file.xls.aes
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @throws org.bouncycastle.openpgp.PGPException
	 */
	public void encryptFile(String finalFileName) throws IOException,
			GeneralSecurityException, PGPException {

		logger.debug("Entering encrypt: finalFileName='{}'", finalFileName);

		File file = new File(finalFileName);
		if (!file.exists()) {
			return;
		}

		if (encryptor == null || !encryptor.isActive()) {
			return;
		}

		EncryptorType encryptorType = encryptor.getEncryptorType();
		logger.debug("encryptorType={}", encryptorType);
		switch (encryptorType) {
			case AESCrypt:
				encryptFileAesCrypt(finalFileName);
				break;
			case OpenPGP:
				encryptFileOpenPgp(finalFileName);
				break;
			default:
				break;
		}
	}

	/**
	 * Encrypts a file using the aes crypt encryptor defined on the report
	 *
	 * @param finalFileName the full path of the final file name of the file
	 * e.g. c:\test\file.xls.aes
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private void encryptFileAesCrypt(String finalFileName) throws IOException, GeneralSecurityException {
		AESCrypt aes = new AESCrypt(encryptor.getAesCryptPassword());
		//http://www.baeldung.com/java-how-to-rename-or-move-a-file
		String tempFileName = FilenameUtils.removeExtension(finalFileName);
		File tempFile = new File(tempFileName);
		File finalFile = new File(finalFileName);
		FileUtils.moveFile(finalFile, tempFile);
		aes.encrypt(tempFileName, finalFileName);
		tempFile.delete();
	}

	/**
	 * Encrypts a file using the openpgp encryptor defined on the report
	 *
	 * @param finalFileName the full path of the final file name of the file
	 * e.g. c:\test\file.xls.aes
	 * @throws IOException
	 * @throws PGPException
	 */
	private void encryptFileOpenPgp(String finalFileName) throws IOException, PGPException {
		//http://blog.swwomm.com/2016/07/jpgpj-new-java-gpg-library.html

		String templatesPath = Config.getTemplatesPath();

		Key publicKey;
		String openPgpPublicKeyString = encryptor.getOpenPgpPublicKeyString();
		if (StringUtils.isNotBlank(openPgpPublicKeyString)) {
			publicKey = new Key(openPgpPublicKeyString);
		} else {
			String publicKeyFileName = encryptor.getOpenPgpPublicKeyFile();
			if (StringUtils.isBlank(publicKeyFileName)) {
				throw new IllegalArgumentException("Public key not specified");
			}

			String publicKeyFilePath = templatesPath + publicKeyFileName;
			File publicKeyFile = new File(publicKeyFilePath);
			if (!publicKeyFile.exists()) {
				throw new IllegalStateException("Public key file not found: " + publicKeyFilePath);
			}

			publicKey = new Key(publicKeyFile);
		}

		Key signingKey = null;
		String signingKeyFileName = encryptor.getOpenPgpSigningKeyFile();
		if (StringUtils.isNotBlank(signingKeyFileName)) {
			String signingKeyFilePath = templatesPath + signingKeyFileName;
			File signingKeyFile = new File(signingKeyFilePath);
			if (!signingKeyFile.exists()) {
				throw new IllegalStateException("Signing key file not found: " + signingKeyFilePath);
			}

			signingKey = new Key(signingKeyFile, encryptor.getOpenPgpSigningKeyPassphrase());
		}

		org.c02e.jpgpj.Encryptor pgpEncryptor;

		if (signingKey == null) {
			pgpEncryptor = new org.c02e.jpgpj.Encryptor(publicKey);
			pgpEncryptor.setSigningAlgorithm(HashingAlgorithm.Unsigned);
		} else {
			pgpEncryptor = new org.c02e.jpgpj.Encryptor(publicKey, signingKey);
		}

		String tempFileName = FilenameUtils.removeExtension(finalFileName);
		File tempFile = new File(tempFileName);
		File finalFile = new File(finalFileName);
		FileUtils.moveFile(finalFile, tempFile);
		pgpEncryptor.encrypt(tempFile, finalFile);
		tempFile.delete();
	}

	/**
	 * Returns the names of the report groups that this report belongs to in a
	 * comma separated string
	 *
	 * @return the names of the report groups that this report belongs to
	 */
	public String getReportGroupNames() {
		reportGroupNames = "";
		if (CollectionUtils.isNotEmpty(reportGroups)) {
			List<String> names = new ArrayList<>();
			for (ReportGroup reportGroup : reportGroups) {
				names.add(reportGroup.getName());
			}
			reportGroupNames = StringUtils.join(names, ", ");
		}

		return reportGroupNames;
	}

	/**
	 * Decrypts password fields
	 */
	public void decryptPasswords() {
		openPassword = AesEncryptor.decrypt(openPassword);
		modifyPassword = AesEncryptor.decrypt(modifyPassword);
	}

	/**
	 * Encrypts password fields
	 */
	public void encryptPasswords() {
		openPassword = AesEncryptor.encrypt(openPassword);
		modifyPassword = AesEncryptor.encrypt(modifyPassword);
	}

	/**
	 * Encrypts all passwords fields in the report including for datasources etc
	 */
	public void encryptAllPasswords() {
		encryptPasswords();
		
		if (datasource != null) {
			datasource.encryptPassword();
		}
		
		if (encryptor != null) {
			encryptor.encryptPasswords();
		}
	}

	/**
	 * Encrypts all passwords fields in the report including for datasources etc
	 * where the respective clearTextPassword field is true
	 */
	public void encryptAllClearTextPasswords() {
		if (clearTextPasswords) {
			encryptPasswords();
		}
		
		if (datasource != null && datasource.isClearTextPassword()) {
			datasource.encryptPassword();
		}
		
		if (encryptor != null && encryptor.isClearTextPasswords()) {
			encryptor.encryptPasswords();
		}
	}

	/**
	 * Returns a copy of this report with only some fields filled
	 *
	 * @return a copy of this report with only some fields filled
	 */
	@JsonIgnore
	public Report getBasicReport() {
		Report basic = new Report();

		basic.setReportId(reportId);
		basic.setName(name);
		basic.setDescription(description);
		basic.setUseGroovy(useGroovy);
		basic.setReportSource(reportSource);
		basic.setReportType(reportType);
		basic.setReportTypeId(reportTypeId);
		basic.setDtName(dtName);
		basic.setDtActiveStatus(dtActiveStatus);
		basic.setDtAction(dtAction);
		basic.setCreatedBy(createdBy);
		basic.setUpdatedBy(updatedBy);
		basic.setReportGroups(reportGroups);
		basic.setUseGroovy(useGroovy);
		basic.setPivotTableJsSavedOptions(pivotTableJsSavedOptions);
		basic.setGridstackSavedOptions(gridstackSavedOptions);
		basic.setOptions(options);
		basic.setReportSource(reportSource);

		if (reportType == ReportType.Text) {
			basic.setReportSourceHtml(reportSource);
		}

		if (datasource != null) {
			datasource.setPassword("");
			datasource.setUsername("");
			datasource.setUrl("");
			datasource.setDriver("");
			datasource.setTestSql("");
			basic.setDatasource(datasource);
		}

		return basic;
	}

	/**
	 * Returns the string to use as the record's datatable rowid
	 *
	 * @return the string to use as the record's datatable rowid
	 */
	public String getDtRowId() {
		dtRowId = "row-" + reportId;
		return dtRowId;
	}

}
