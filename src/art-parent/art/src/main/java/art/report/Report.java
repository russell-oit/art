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
import art.parameter.Parameter;
import art.reportoptions.C3Options;
import art.reportoptions.CloneOptions;
import art.reportoptions.PlotlyOptions;
import art.reportparameter.ReportParameter;
import art.reportrule.ReportRule;
import art.ruleValue.UserGroupRuleValue;
import art.ruleValue.UserRuleValue;
import art.servlets.Config;
import art.settings.EncryptionPassword;
import art.utils.ArtUtils;
import art.utils.XmlParser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.cloning.Cloner;
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
import org.owasp.encoder.Encode;
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
	
	private int reportId;
	private String name;
	private String shortDescription;
	private String description;
	@JsonIgnore
	private int reportTypeId;
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
	private String createdBy;
	private int createdById;
	private String updatedBy;
	private String reportSource;
	@JsonIgnore
	private boolean useBlankXmlaPassword;
	@JsonIgnore
	private ChartOptions chartOptions;
	@JsonIgnore
	private String reportSourceHtml; //used with text reports
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
	@JsonIgnore
	private GeneralReportOptions generalOptions;
	private String openPassword;
	private String modifyPassword;
	@JsonIgnore
	private boolean useNoneOpenPassword; //only for use with ui
	@JsonIgnore
	private boolean useNoneModifyPassword; //only for use with ui
	@JsonIgnore
	private Report sourceReport;
	private int sourceReportId;
	@JsonIgnore
	private CloneOptions cloneOptions;
	private List<ReportGroup> reportGroups;
	private boolean clearTextPasswords;
	@JsonIgnore
	private Boolean testRun; //used for the test report functionality
	private boolean useGroovy;
	private String pivotTableJsSavedOptions;
	private String gridstackSavedOptions;
	private String name2; //used for holding a processed report name e.g. in self service dashboard reports list
	private String comment;
	private int viewReportId;
	private String selfServiceOptions;
	private Datasource datasource;
	private Encryptor encryptor;
	private List<ReportParameter> reportParams; //used in import/export
	private List<UserRuleValue> userRuleValues; //used in import/export
	private List<UserGroupRuleValue> userGroupRuleValues; //used in import/export
	private List<ReportRule> reportRules; //used in import/export
	private List<UserReportRight> userReportRights; //used in import/export
	private List<UserGroupReportRight> userGroupReportRights; //used in import/export
	private List<Drilldown> drilldowns; //used in import/export
	private String description2; //used for holding a processed description
	private String dtActiveStatus;
	private String dtAction;
	private String reportGroupNames; //used to prevent Unrecognized field error with json import. alternative is to use jsonignoreproperties on the class
	private String reportGroupNamesHtml;
	@JsonIgnore
	private boolean overwriteFiles;
	@JsonIgnore
	private Integer limit;
	@JsonIgnore
	private boolean selfServicePreview;

	/**
	 * @return the selfServicePreview
	 */
	public boolean isSelfServicePreview() {
		return selfServicePreview;
	}

	/**
	 * @param selfServicePreview the selfServicePreview to set
	 */
	public void setSelfServicePreview(boolean selfServicePreview) {
		this.selfServicePreview = selfServicePreview;
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
	 * @return the createdById
	 */
	public int getCreatedById() {
		return createdById;
	}

	/**
	 * @param createdById the createdById to set
	 */
	public void setCreatedById(int createdById) {
		this.createdById = createdById;
	}

	/**
	 * @return the limit
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	/**
	 * @return the viewReportId
	 */
	public int getViewReportId() {
		return viewReportId;
	}

	/**
	 * @param viewReportId the viewReportId to set
	 */
	public void setViewReportId(int viewReportId) {
		this.viewReportId = viewReportId;
	}

	/**
	 * @return the selfServiceOptions
	 */
	public String getSelfServiceOptions() {
		return selfServiceOptions;
	}

	/**
	 * @param selfServiceOptions the selfServiceOptions to set
	 */
	public void setSelfServiceOptions(String selfServiceOptions) {
		this.selfServiceOptions = selfServiceOptions;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the overwriteFiles
	 */
	public boolean isOverwriteFiles() {
		return overwriteFiles;
	}

	/**
	 * @param overwriteFiles the overwriteFiles to set
	 */
	public void setOverwriteFiles(boolean overwriteFiles) {
		this.overwriteFiles = overwriteFiles;
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
	 * @return the testRun
	 */
	public Boolean getTestRun() {
		return testRun;
	}

	/**
	 * @param testRun the testRun to set
	 */
	public void setTestRun(Boolean testRun) {
		this.testRun = testRun;
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
				throw new RuntimeException("Public key file not found: " + publicKeyFilePath);
			}

			publicKey = new Key(publicKeyFile);
		}

		Key signingKey = null;
		String signingKeyFileName = encryptor.getOpenPgpSigningKeyFile();
		if (StringUtils.isNotBlank(signingKeyFileName)) {
			String signingKeyFilePath = templatesPath + signingKeyFileName;
			File signingKeyFile = new File(signingKeyFilePath);
			if (!signingKeyFile.exists()) {
				throw new RuntimeException("Signing key file not found: " + signingKeyFilePath);
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
	 * @return the names of the report groups that this report belongs to in a
	 * comma separated string
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
	 * Returns report group names, html encoded
	 *
	 * @return report group names, html encoded
	 */
	public String getReportGroupNamesHtml() {
		reportGroupNamesHtml = getReportGroupNames();
		if (StringUtils.isNotBlank(reportGroupNamesHtml)) {
			reportGroupNamesHtml = Encode.forHtml(reportGroupNamesHtml);
		}

		return reportGroupNamesHtml;
	}

	/**
	 * Decrypts password fields
	 *
	 * @throws java.lang.Exception
	 */
	public void decryptPasswords() throws Exception {
		openPassword = AesEncryptor.decrypt(openPassword);
		modifyPassword = AesEncryptor.decrypt(modifyPassword);
	}

	/**
	 * Encrypts password fields
	 *
	 * @throws java.lang.Exception
	 */
	public void encryptPasswords() throws Exception {
		String key = null;
		EncryptionPassword encryptionPassword = null;
		encryptPasswords(key, encryptionPassword);
	}

	/**
	 * Encrypts the open password field
	 *
	 * @throws Exception
	 */
	public void encryptOpenPassword() throws Exception {
		openPassword = AesEncryptor.encrypt(openPassword);
	}

	/**
	 * Encrypts the modify password field
	 *
	 * @throws Exception
	 */
	public void encryptModifyPassword() throws Exception {
		modifyPassword = AesEncryptor.encrypt(modifyPassword);
	}

	/**
	 * Encrypts password fields
	 *
	 * @param key the key to use. If null, the current key will be used
	 * @param encryptionPassword the encryption configuration to use. null if to
	 * use current.
	 * @throws java.lang.Exception
	 */
	public void encryptPasswords(String key, EncryptionPassword encryptionPassword) throws Exception {
		openPassword = AesEncryptor.encrypt(openPassword, key, encryptionPassword);
		modifyPassword = AesEncryptor.encrypt(modifyPassword, key, encryptionPassword);
	}

	/**
	 * Returns <code>true</code> if all password fields are null
	 *
	 * @return <code>true</code> if all password fields are null
	 */
	public boolean hasNullPasswords() {
		if (openPassword == null && modifyPassword == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Clears password fields
	 */
	public void clearPasswords() {
		openPassword = null;
		modifyPassword = null;
	}

	/**
	 * Encrypts all passwords fields in the report including for datasources etc
	 *
	 * @throws java.lang.Exception
	 */
	public void encryptAllPasswords() throws Exception {
		encryptPasswords();

		if (datasource != null) {
			datasource.encryptPassword();
		}

		if (encryptor != null) {
			encryptor.encryptPasswords();
		}

		if (reportParams != null) {
			for (ReportParameter reportParam : reportParams) {
				Parameter reportParamParameter = reportParam.getParameter();
				reportParamParameter.encryptAllPasswords();
			}
		}
	}

	/**
	 * Encrypts all passwords fields in the report including for datasources etc
	 * where the respective clearTextPassword field is true
	 *
	 * @throws java.lang.Exception
	 */
	public void encryptAllClearTextPasswords() throws Exception {
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
	 * Returns a copy of this report with only some fields filled to avoid
	 * exposing passwords
	 *
	 * @return a copy of this report with only some fields filled
	 */
	@JsonIgnore
	public Report getBasicReport() {
		Report basic = new Report();

		basic.setReportId(reportId);
		basic.setName(name);
		basic.setName2(name2);
		basic.setDescription(description);
		basic.setDescription2(description2);
		basic.setDtActiveStatus(dtActiveStatus);
		basic.setDtAction(dtAction);
		basic.setReportGroups(reportGroups);
		basic.setViewReportId(viewReportId);

		return basic;
	}

	/**
	 * Returns a copy of this report with password fields set to null
	 *
	 * @return a copy of this report with password fields set to null
	 */
	@JsonIgnore
	public Report getCleanReport() {
		//https://stackoverflow.com/questions/45834393/hiding-sensitive-information-in-response
		//https://www.baeldung.com/entity-to-and-from-dto-for-a-java-spring-application
		Cloner cloner = new Cloner();
		Report copy = cloner.deepClone(this);

		copy.clearPasswords();
		copy.datasource = null;
		copy.encryptor = null;
		copy.reportParams = null;

		return copy;
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
	 * Returns <code>true</code> if this is a self service report
	 *
	 * @return <code>true</code> if this is a self service report
	 */
	@JsonIgnore
	public boolean isSelfService() {
		if (viewReportId > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns <code>true</code> if this is a view or self service report
	 *
	 * @return <code>true</code> if this is a view or self service report
	 */
	@JsonIgnore
	public boolean isViewOrSelfService() {
		if (reportType != null && reportType == ReportType.View) {
			return true;
		} else if (isSelfService()) {
			return true;
		} else {
			return false;
		}
	}

}
