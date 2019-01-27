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
import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.connectionpool.DbConnections;
import art.datasource.DatasourceService;
import art.encryptor.Encryptor;
import art.encryptor.EncryptorService;
import art.enums.AccessLevel;
import art.enums.PageOrientation;
import art.enums.ParameterType;
import art.enums.ReportType;
import art.general.ActionResult;
import art.parameter.ParameterService;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.reportgroupmembership.ReportGroupMembershipService2;
import art.reportoptions.CloneOptions;
import art.saiku.SaikuReport;
import art.user.User;
import art.utils.ArtHelper;
import art.utils.ArtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting reports
 *
 * @author Timothy Anyona
 */
@Service
public class ReportService {

	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

	private final DbService dbService;
	private final DatasourceService datasourceService;
	private final ReportGroupService reportGroupService;
	private final EncryptorService encryptorService;
	private final ReportGroupMembershipService2 reportGroupMembershipService2;

	@Autowired
	public ReportService(DbService dbService, DatasourceService datasourceService,
			ReportGroupService reportGroupService, EncryptorService encryptorService,
			ReportGroupMembershipService2 reportGroupMembershipService2) {

		this.dbService = dbService;
		this.datasourceService = datasourceService;
		this.reportGroupService = reportGroupService;
		this.encryptorService = encryptorService;
		this.reportGroupMembershipService2 = reportGroupMembershipService2;
	}

	public ReportService() {
		dbService = new DbService();
		datasourceService = new DatasourceService();
		reportGroupService = new ReportGroupService();
		encryptorService = new EncryptorService();
		reportGroupMembershipService2 = new ReportGroupMembershipService2();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_QUERIES AQ";

	/**
	 * Maps a resultset to an object
	 */
	private class ReportMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			Report report = new Report();

			report.setReportId(rs.getInt("QUERY_ID"));
			report.setName(rs.getString("NAME"));
			report.setShortDescription(rs.getString("SHORT_DESCRIPTION"));
			report.setDescription(rs.getString("DESCRIPTION"));
			report.setComment(rs.getString("DEVELOPER_COMMENT"));
			report.setReportTypeId(rs.getInt("QUERY_TYPE"));
			report.setReportType(ReportType.toEnum(rs.getInt("QUERY_TYPE")));
			report.setGroupColumn(rs.getInt("GROUP_COLUMN"));
			report.setContactPerson(rs.getString("CONTACT_PERSON"));
			report.setUsesRules(rs.getBoolean("USES_RULES"));
			report.setActive(rs.getBoolean("ACTIVE"));
			report.setHidden(rs.getBoolean("HIDDEN"));
			report.setReportSource(rs.getString("REPORT_SOURCE"));
			report.setParametersInOutput(rs.getBoolean("PARAMETERS_IN_OUTPUT"));
			report.setxAxisLabel(rs.getString("X_AXIS_LABEL"));
			report.setyAxisLabel(rs.getString("Y_AXIS_LABEL"));
			report.setChartOptionsSetting(rs.getString("GRAPH_OPTIONS"));
			report.setSecondaryCharts(rs.getString("SECONDARY_CHARTS"));
			report.setTemplate(rs.getString("TEMPLATE"));
			report.setDisplayResultset(rs.getInt("DISPLAY_RESULTSET"));
			report.setXmlaDatasource(rs.getString("XMLA_DATASOURCE"));
			report.setXmlaCatalog(rs.getString("XMLA_CATALOG"));
			report.setDefaultReportFormat(rs.getString("DEFAULT_REPORT_FORMAT"));
			report.setOmitTitleRow(rs.getBoolean("OMIT_TITLE_ROW"));
			report.setHiddenColumns(rs.getString("HIDDEN_COLUMNS"));
			report.setTotalColumns(rs.getString("TOTAL_COLUMNS"));
			report.setDateFormat(rs.getString("DATE_COLUMN_FORMAT"));
			report.setNumberFormat(rs.getString("NUMBER_COLUMN_FORMAT"));
			report.setColumnFormats(rs.getString("COLUMN_FORMATS"));
			report.setLocale(rs.getString("LOCALE"));
			report.setNullNumberDisplay(rs.getString("NULL_NUMBER_DISPLAY"));
			report.setNullStringDisplay(rs.getString("NULL_STRING_DISPLAY"));
			report.setFetchSize(rs.getInt("FETCH_SIZE"));
			report.setOptions(rs.getString("REPORT_OPTIONS"));
			report.setPageOrientation(PageOrientation.toEnum(rs.getString("PAGE_ORIENTATION"), PageOrientation.Portrait));
			report.setLovUseDynamicDatasource(rs.getBoolean("LOV_USE_DYNAMIC_DATASOURCE"));
			report.setOpenPassword(rs.getString("OPEN_PASSWORD"));
			report.setModifyPassword(rs.getString("MODIFY_PASSWORD"));
			report.setSourceReportId(rs.getInt("SOURCE_REPORT_ID"));
			report.setUseGroovy(rs.getBoolean("USE_GROOVY"));
			report.setPivotTableJsSavedOptions(rs.getString("PIVOTTABLEJS_SAVED_OPTIONS"));
			report.setGridstackSavedOptions(rs.getString("GRIDSTACK_SAVED_OPTIONS"));
			report.setViewReportId(rs.getInt("VIEW_REPORT_ID"));
			report.setSelfServiceOptions(rs.getString("SELF_SERVICE_OPTIONS"));
			report.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			report.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			report.setCreatedBy(rs.getString("CREATED_BY"));
			report.setUpdatedBy(rs.getString("UPDATED_BY"));

			ReportType reportType = report.getReportType();
			if (reportType == ReportType.Text) {
				report.setReportSourceHtml(report.getReportSource());
			}

			Datasource datasource = datasourceService.getDatasource(rs.getInt("DATASOURCE_ID"));
			report.setDatasource(datasource);

			Encryptor encryptor = encryptorService.getEncryptor(rs.getInt("ENCRYPTOR_ID"));
			report.setEncryptor(encryptor);

			List<ReportGroup> reportGroups = reportGroupService.getReportGroupsForReport(report.getReportId());
			report.setReportGroups(reportGroups);

			//decrypt open and modify passwords
			try {
				report.decryptPasswords();
			} catch (Exception ex) {
				logger.error("Error. {}", report, ex);
			}

			setChartOptions(report);

			String options = report.getOptions();
			if (StringUtils.isNotBlank(options)) {
				try {
					ObjectMapper mapper = new ObjectMapper();
					CloneOptions cloneOptions = mapper.readValue(options, CloneOptions.class);
					report.setCloneOptions(cloneOptions);
				} catch (IOException ex) {
					logger.error("Error. {}", report, ex);
				}
			}

			try {
				report.loadGeneralOptions();
			} catch (IOException ex) {
				logger.error("Error. {}", report, ex);
			}

			return type.cast(report);
		}

		/**
		 * Sets display options for charts
		 *
		 * @param report the report to set
		 */
		private void setChartOptions(Report report) {

			ChartOptions chartOptions = new ChartOptions();
			chartOptions.initializeBooleansToFalse();

			String optionsString;
			boolean usingShortDescription;
			if (report.getChartOptionsSetting() == null) {
				//to support legacy configurations where options were saved in short description field
				usingShortDescription = true;
				optionsString = report.getShortDescription();
			} else {
				usingShortDescription = false;
				optionsString = report.getChartOptionsSetting();
			}

			if (optionsString == null) {
				report.setChartOptions(new ChartOptions());
				return;
			}

			int index;
			index = optionsString.lastIndexOf("@");

			if (usingShortDescription || index > -1) {
				ArtHelper artHelper = new ArtHelper();

				//set default for showlegend. false for heat maps. true for all other graphs
				ReportType reportType = report.getReportType();
				boolean defaultShowLegendOption = artHelper.getDefaultShowLegendOption(reportType);
				chartOptions.setShowLegend(defaultShowLegendOption);

				//set default for showlabels. true for pie charts. false for all other graphs
				boolean defaultShowLabelsOption = artHelper.getDefaultShowLabelsOption(reportType);
				chartOptions.setShowLabels(defaultShowLabelsOption);
			}

			String options;
			if (index > -1) {
				//options specified as part of short description. for backward compatibility with pre-2.0
				options = optionsString.substring(index + 1); //+1 so that the @ is not included in the options string
			} else {
				if (usingShortDescription) {
					//no @ symbol so graph options not specified in short description
					options = "";
				} else {
					options = optionsString;
				}
			}

			chartOptions.setChartOptionsFromString(options);

			report.setChartOptions(chartOptions);
		}
	}

	/**
	 * Returns the reports that a user can access. Excludes disabled reports.
	 *
	 * @param userId the user id
	 * @param includedReportTypes report types that should be included
	 * @return accessible reports
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getAccessibleReportsWithReportTypes(int userId,
			List<ReportType> includedReportTypes) throws SQLException {

		List<ReportType> excludedReportTypes = null;
		return getAccessibleReports(userId, includedReportTypes, excludedReportTypes);
	}

	/**
	 * Returns the reports that a user can access. Excludes disabled reports.
	 *
	 * @param userId the user id
	 * @param excludedReportTypes report types that should be excluded
	 * @return accessible reports
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getAccessibleReportsWithoutReportTypes(int userId,
			List<ReportType> excludedReportTypes) throws SQLException {

		List<ReportType> includedReportTypes = null;
		return getAccessibleReports(userId, includedReportTypes, excludedReportTypes);
	}

	/**
	 * Returns the reports that a user can access. Excludes disabled reports.
	 *
	 * @param userId the user id
	 * @param includedReportTypes report types that should be included
	 * @param excludedReportTypes report types that should be excluded
	 * @return accessible reports
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getAccessibleReports(int userId,
			List<ReportType> includedReportTypes,
			List<ReportType> excludedReportTypes) throws SQLException {

		String extraContidion = null;
		return getAccessibleReports(userId, includedReportTypes, excludedReportTypes, extraContidion);
	}

	/**
	 * Returns the reports that a user can access. Excludes disabled reports.
	 *
	 * @param userId the user id
	 * @param includedReportTypes report types that should be included
	 * @param excludedReportTypes report types that should be excluded
	 * @param extraCondition an extra condition to be applied
	 * @return accessible reports
	 * @throws SQLException
	 */
	private List<Report> getAccessibleReports(int userId,
			List<ReportType> includedReportTypes, List<ReportType> excludedReportTypes,
			String extraCondition) throws SQLException {

		logger.debug("Entering getAccessibleReports: userId={}", userId);

		String includedReportTypesCondition = "";
		if (CollectionUtils.isNotEmpty(includedReportTypes)) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < includedReportTypes.size(); i++) {
				if (i == 0) {
					sb.append(includedReportTypes.get(i).getValue());
				} else {
					sb.append(",").append(includedReportTypes.get(i).getValue());
				}
			}
			includedReportTypesCondition = " AND AQ.QUERY_TYPE IN(" + sb.toString() + ") ";
		}

		String excludedReportTypesCondition = "";
		if (CollectionUtils.isNotEmpty(excludedReportTypes)) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < excludedReportTypes.size(); i++) {
				if (i == 0) {
					sb.append(excludedReportTypes.get(i).getValue());
				} else {
					sb.append(",").append(excludedReportTypes.get(i).getValue());
				}
			}
			excludedReportTypesCondition = " AND AQ.QUERY_TYPE NOT IN(" + sb.toString() + ") ";
		}

		if (extraCondition == null) {
			extraCondition = "";
		} else {
			extraCondition = " " + extraCondition;
		}

		String sql = SQL_SELECT_ALL
				//only show active reports
				+ " WHERE AQ.ACTIVE=1"
				//don't show hidden reports
				+ " AND AQ.HIDDEN<>1"
				+ extraCondition
				+ includedReportTypesCondition
				+ excludedReportTypesCondition
				+ " AND("
				//user can run report if he has direct access to it
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_QUERIES AUQ"
				+ " WHERE AUQ.QUERY_ID=AQ.QUERY_ID AND AUQ.USER_ID=?)"
				+ " OR"
				//user can run report if he belongs to a user group which has direct access to the report
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_GROUP_QUERIES AUGQ"
				+ " INNER JOIN ART_USER_GROUP_ASSIGNMENT AUGA"
				+ " ON AUGQ.USER_GROUP_ID=AUGA.USER_GROUP_ID"
				+ " WHERE AUGQ.QUERY_ID=AQ.QUERY_ID AND AUGA.USER_ID=?)"
				+ " OR"
				//user can run report if he has access to the report's group
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_QUERY_GROUPS AUQG, ART_REPORT_REPORT_GROUPS ARRG"
				+ " WHERE AUQG.QUERY_GROUP_ID=ARRG.REPORT_GROUP_ID"
				+ " AND ARRG.REPORT_ID=AQ.QUERY_ID AND AUQG.USER_ID=?)"
				+ " OR"
				//user can run report if his user group has access to the report's group
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_GROUP_GROUPS AUGG"
				+ " INNER JOIN ART_USER_GROUP_ASSIGNMENT AUGA"
				+ " ON AUGG.USER_GROUP_ID=AUGA.USER_GROUP_ID"
				+ " INNER JOIN ART_REPORT_REPORT_GROUPS ARRG"
				+ " ON AUGG.QUERY_GROUP_ID=ARRG.REPORT_GROUP_ID"
				+ " WHERE ARRG.REPORT_ID=AQ.QUERY_ID AND AUGA.USER_ID=?)"
				+ ")";

		Object[] values = {
			userId, //user access to report
			userId, //user group access to report
			userId, //user access to report group
			userId //user group access to report group
		};

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h, values);
	}

	/**
	 * Returns the reports that a user can access from the reports page.
	 * Excludes disabled reports and some report types e.g. lovs
	 *
	 * @param userId the user id
	 * @return available reports
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public List<Report> getDisplayReports(int userId) throws SQLException {
		logger.debug("Entering getDisplayReports: userId={}", userId);

		List<ReportType> excludedReportTypes = Arrays.asList(ReportType.LovStatic,
				ReportType.LovDynamic, ReportType.JobRecipients,
				ReportType.SaikuConnection, ReportType.View);

		return getAccessibleReportsWithoutReportTypes(userId, excludedReportTypes);
	}

	/**
	 * Returns the reports that a user can add to a self service dashboard
	 *
	 * @param userId the user id
	 * @return dashboard candidate reports
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public List<Report> getDashboardCandidateReports(int userId) throws SQLException {
		logger.debug("Entering getDashboardCandidateReports: userId={}", userId);

		List<ReportType> excludedReportTypes = Arrays.asList(ReportType.LovStatic,
				ReportType.LovDynamic, ReportType.JobRecipients, ReportType.SaikuConnection,
				ReportType.Dashboard, ReportType.GridstackDashboard, ReportType.Update,
				ReportType.JPivotMondrian, ReportType.JPivotMondrianXmla,
				ReportType.JPivotSqlServerXmla, ReportType.SaikuReport,
				ReportType.View);

		return getAccessibleReportsWithoutReportTypes(userId, excludedReportTypes);
	}

	/**
	 * Returns all reports
	 *
	 * @return all reports
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getAllReports() throws SQLException {
		logger.debug("Entering getAllReports");

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns reports with given ids
	 *
	 * @param ids comma separated string of the report ids to retrieve
	 * @return reports with given ids
	 * @throws SQLException
	 */
	public List<Report> getReports(String ids) throws SQLException {
		logger.debug("Entering getReports: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		String sql = SQL_SELECT_ALL
				+ " WHERE QUERY_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns all active saiku connection reports
	 *
	 * @return all active saiku connection reports
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getAllActiveSaikuConnectionReports() throws SQLException {
		logger.debug("Entering getAllActiveSaikuConnectionReports");

		String sql = SQL_SELECT_ALL + " WHERE QUERY_TYPE=150 AND ACTIVE=1";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Returns a report
	 *
	 * @param id the report id
	 * @return report if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public Report getReport(int id) throws SQLException {
		logger.debug("Entering getReport: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE QUERY_ID=?";
		ResultSetHandler<Report> h = new BeanHandler<>(Report.class, new ReportMapper());
		Report report = dbService.query(sql, h, id);

		setEffectiveReportSource(report);

		return report;
	}

	/**
	 * Sets the effective report source for a report, in case it is a clone
	 * report
	 *
	 * @param report the report object to set
	 * @throws SQLException
	 */
	private void setEffectiveReportSource(Report report) throws SQLException {
		if (report == null) {
			return;
		}

		int sourceReportId = report.getSourceReportId();
		if (sourceReportId > 0) {
			String newSource = getReportSource(sourceReportId);
			if (StringUtils.isNotBlank(newSource)) {
				report.setReportSource(newSource);
			}
//			Report sourceReport = getSourceReport(sourceReportId);
//			if (sourceReport != null) {
//				report.setReportSource(sourceReport.getReportSource());
//				report.setSourceReport(sourceReport);
//			}
		}
	}

//	/**
//	 * Returns a source report
//	 *
//	 * @param id the source report id
//	 * @return report if found, null otherwise
//	 * @throws SQLException
//	 */
//	private Report getSourceReport(int id) throws SQLException {
//		logger.debug("Entering getSourceReport: id={}", id);
//
//		//use separate method to avoid recursion issues
//		String sql = SQL_SELECT_ALL + " WHERE QUERY_ID=?";
//		ResultSetHandler<Report> h = new BeanHandler<>(Report.class, new ReportMapper());
//		Report report = dbService.query(sql, h, id);
//
//		return report;
//	}
	/**
	 * Returns the report source for a given report
	 *
	 * @param reportId the report id
	 * @return the report source
	 * @throws SQLException
	 */
	private String getReportSource(int reportId) throws SQLException {
		String sql = "SELECT REPORT_SOURCE FROM ART_QUERIES WHERE QUERY_ID=?";
		ResultSetHandler<String> h = new ScalarHandler<>(1);
		return dbService.query(sql, h, reportId);
	}

	/**
	 * Returns a report, with it's own source, not that of a parent (for clone
	 * reports)
	 *
	 * @param id the report id
	 * @return report if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public Report getReportWithOwnSource(int id) throws SQLException {
		logger.debug("Entering getReportWithOwnSource: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE QUERY_ID=?";
		ResultSetHandler<Report> h = new BeanHandler<>(Report.class, new ReportMapper());
		Report report = dbService.query(sql, h, id);

		return report;
	}

	/**
	 * Returns a report
	 *
	 * @param reportName the report name
	 * @return report if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public Report getReport(String reportName) throws SQLException {
		logger.debug("Entering getReport: reportName={}", reportName);

		String sql = SQL_SELECT_ALL + " WHERE NAME=?";
		ResultSetHandler<Report> h = new BeanHandler<>(Report.class, new ReportMapper());
		Report report = dbService.query(sql, h, reportName);

		return report;
	}

	/**
	 * Returns details of jobs that use a given report
	 *
	 * @param reportId the report id
	 * @return linked job details
	 * @throws SQLException
	 */
	public List<String> getLinkedJobs(int reportId) throws SQLException {
		logger.debug("Entering getLinkedJobs: reportId={}", reportId);

		String sql = "SELECT JOB_ID, JOB_NAME"
				+ " FROM ART_JOBS"
				+ " WHERE QUERY_ID=?";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> jobDetails = dbService.query(sql, h, reportId);

		List<String> jobs = new ArrayList<>();
		for (Map<String, Object> jobDetail : jobDetails) {
			Integer jobId = (Integer) jobDetail.get("JOB_ID");
			String jobName = (String) jobDetail.get("JOB_NAME");
			jobs.add(jobName + " (" + jobId + ")");
		}

		return jobs;
	}

	/**
	 * Deletes a report
	 *
	 * @param id the report id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * jobs which prevented the report from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"reports", "parameters"}, allEntries = true)
	public ActionResult deleteReport(int id) throws SQLException {
		logger.debug("Entering deleteReport: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedJobs = getLinkedJobs(id);
		if (!linkedJobs.isEmpty()) {
			result.setData(linkedJobs);
			return result;
		}

		String sql;

		//delete query-user relationships
		sql = "DELETE FROM ART_USER_QUERIES WHERE QUERY_ID=?";
		dbService.update(sql, id);

		//delete query-rule relationships
		sql = "DELETE FROM ART_QUERY_RULES WHERE QUERY_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_QUERIES WHERE QUERY_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_DRILLDOWN_QUERIES WHERE QUERY_ID=? OR DRILLDOWN_QUERY_ID=?";
		dbService.update(sql, id, id);

		sql = "DELETE FROM ART_REPORT_REPORT_GROUPS WHERE REPORT_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_SAVED_PARAMETERS WHERE REPORT_ID=?";
		dbService.update(sql, id);

		//delete report parameters
		//get non-shared parameters for later deleting
		sql = "SELECT ARP.PARAMETER_ID"
				+ " FROM ART_REPORT_PARAMETERS ARP"
				+ " INNER JOIN ART_PARAMETERS AP"
				+ " ON ARP.PARAMETER_ID=AP.PARAMETER_ID"
				+ " WHERE ARP.REPORT_ID=?"
				+ " AND AP.SHARED=0"
				+ " GROUP BY ARP.PARAMETER_ID"
				+ " HAVING COUNT(*)=1";
		ResultSetHandler<List<Number>> h = new ColumnListHandler<>("PARAMETER_ID");
		List<Number> nonSharedParameterIds = dbService.query(sql, h, id);

		sql = "DELETE FROM ART_REPORT_PARAMETERS WHERE REPORT_ID=?";
		dbService.update(sql, id);

		//almost lastly, delete report
		sql = "DELETE FROM ART_QUERIES WHERE QUERY_ID=?";
		int affectedRows = dbService.update(sql, id);

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with delete. affectedRows={}, id={}", affectedRows, id);
		}

		//delete non-shared parameters that the report used
		if (CollectionUtils.isNotEmpty(nonSharedParameterIds)) {
			List<Integer> deleteParameterIds = new ArrayList<>();
			for (Number parameterIdNumber : nonSharedParameterIds) {
				int parameterIdInt = parameterIdNumber.intValue();
				deleteParameterIds.add(parameterIdInt);
			}
			ParameterService parameterService = new ParameterService();
			parameterService.deleteParameters(deleteParameterIds.toArray(new Integer[0]));
		}

		result.setSuccess(true);
		return result;
	}

	/**
	 * Deletes multiple reports
	 *
	 * @param ids the ids of the reports to delete
	 * @return ActionResult. if not successful, data contains ids of reports
	 * that were not deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"reports", "parameters"}, allEntries = true)
	public ActionResult deleteReports(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteReports: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteReport(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedJobs = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedJobs, ", ");
				nonDeletedRecords.add(value);
			}
		}

		if (nonDeletedRecords.isEmpty()) {
			result.setSuccess(true);
		} else {
			result.setData(nonDeletedRecords);
		}
		return result;

	}

	/**
	 * Adds a new report to the database
	 *
	 * @param report the report to add
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public synchronized int addReport(Report report, User actionUser) throws SQLException {
		Connection conn = null;
		return addReport(report, actionUser, conn);
	}

	/**
	 * Adds a new report to the database
	 *
	 * @param report the report to add
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public synchronized int addReport(Report report, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering addReport: report={}, actionUser={}", report, actionUser);

		//generate new id
		String sql = "SELECT MAX(QUERY_ID) FROM ART_QUERIES";
		int newId = dbService.getNewRecordId(sql);

		saveReport(report, newId, actionUser, conn);

		return newId;
	}

	/**
	 * Updates an existing report
	 *
	 * @param report the updated report
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void updateReport(Report report, User actionUser) throws SQLException {
		Connection conn = null;
		updateReport(report, actionUser, conn);
	}

	/**
	 * Updates an existing report
	 *
	 * @param report the updated report
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void updateReport(Report report, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering updateReport: report={}, actionUser={}",
				report, actionUser);

		Integer newRecordId = null;
		saveReport(report, newRecordId, actionUser, conn);
	}

	/**
	 * Imports report records
	 *
	 * @param reports the list of reports to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use. if autocommit is false, no commit is
	 * performed
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void importReports(List<Report> reports, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering importReports: actionUser={}", actionUser);

		String sql = "SELECT MAX(QUERY_ID) FROM ART_QUERIES";
		int reportId = dbService.getMaxRecordId(conn, sql);

		sql = "SELECT MAX(DATABASE_ID) FROM ART_DATABASES";
		int datasourceId = dbService.getMaxRecordId(conn, sql);

		sql = "SELECT MAX(ENCRYPTOR_ID) FROM ART_ENCRYPTORS";
		int encryptorId = dbService.getMaxRecordId(conn, sql);

		sql = "SELECT MAX(QUERY_GROUP_ID) FROM ART_QUERY_GROUPS";
		int reportGroupId = dbService.getMaxRecordId(conn, sql);

		Map<String, Datasource> addedDatasources = new HashMap<>();
		Map<String, Encryptor> addedEncryptors = new HashMap<>();
		Map<String, ReportGroup> addedReportGroups = new HashMap<>();
		for (Report report : reports) {
			reportId++;

			Datasource datasource = report.getDatasource();
			if (datasource != null) {
				String datasourceName = datasource.getName();
				if (StringUtils.isBlank(datasourceName)) {
					report.setDatasource(null);
				} else {
					Datasource existingDatasource = datasourceService.getDatasource(datasourceName);
					if (existingDatasource == null) {
						Datasource addedDatasource = addedDatasources.get(datasourceName);
						if (addedDatasource == null) {
							datasourceId++;
							datasourceService.saveDatasource(datasource, datasourceId, actionUser, conn);
							addedDatasources.put(datasourceName, datasource);
						} else {
							report.setDatasource(addedDatasource);
						}
					} else {
						report.setDatasource(existingDatasource);
					}
				}
			}

			Encryptor encryptor = report.getEncryptor();
			if (encryptor != null) {
				String encryptorName = encryptor.getName();
				if (StringUtils.isBlank(encryptorName)) {
					report.setEncryptor(null);
				} else {
					Encryptor existingEncryptor = encryptorService.getEncryptor(encryptorName);
					if (existingEncryptor == null) {
						Encryptor addedEncryptor = addedEncryptors.get(encryptorName);
						if (addedEncryptor == null) {
							encryptorId++;
							encryptorService.saveEncryptor(encryptor, encryptorId, actionUser, conn);
							addedEncryptors.put(encryptorName, encryptor);
						} else {
							report.setEncryptor(addedEncryptor);
						}
					} else {
						report.setEncryptor(existingEncryptor);
					}
				}
			}

			List<ReportGroup> reportGroups = report.getReportGroups();
			if (CollectionUtils.isNotEmpty(reportGroups)) {
				List<ReportGroup> newReportGroups = new ArrayList<>();
				for (ReportGroup reportGroup : reportGroups) {
					String reportGroupName = reportGroup.getName();
					ReportGroup existingReportGroup = reportGroupService.getReportGroup(reportGroupName);
					if (existingReportGroup == null) {
						ReportGroup addedReportGroup = addedReportGroups.get(reportGroupName);
						if (addedReportGroup == null) {
							reportGroupId++;
							reportGroupService.saveReportGroup(reportGroup, reportGroupId, actionUser, conn);
							addedReportGroups.put(reportGroupName, reportGroup);
							newReportGroups.add(reportGroup);
						} else {
							newReportGroups.add(addedReportGroup);
						}
					} else {
						newReportGroups.add(existingReportGroup);
					}
				}
				report.setReportGroups(newReportGroups);
			}

			saveReport(report, reportId, actionUser, conn);
			reportGroupMembershipService2.recreateReportGroupMemberships(report);
		}
	}

	/**
	 * Saves a report
	 *
	 * @param report the report to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void saveReport(Report report, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering saveReport: report={}, newRecordId={},"
				+ " actionUser={}", report, newRecordId, actionUser);

		Integer reportGroupId = 0; //field no longer used but can't be null

		Integer datasourceId = null;
		if (report.getDatasource() != null) {
			datasourceId = report.getDatasource().getDatasourceId();
			if (datasourceId == 0) {
				datasourceId = null;
			}
		}

		Integer encryptorId = null;
		if (report.getEncryptor() != null) {
			encryptorId = report.getEncryptor().getEncryptorId();
			if (encryptorId == 0) {
				encryptorId = null;
			}
		}

		Integer reportTypeId;
		if (report.getReportType() == null) {
			reportTypeId = 0;
		} else {
			reportTypeId = report.getReportType().getValue();
		}

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_QUERIES"
					+ " (QUERY_ID, NAME, SHORT_DESCRIPTION, DESCRIPTION,"
					+ " DEVELOPER_COMMENT, QUERY_TYPE,"
					+ " GROUP_COLUMN, QUERY_GROUP_ID, DATASOURCE_ID,"
					+ " CONTACT_PERSON, USES_RULES,"
					+ " ACTIVE, HIDDEN, REPORT_SOURCE, PARAMETERS_IN_OUTPUT,"
					+ " X_AXIS_LABEL, Y_AXIS_LABEL,"
					+ " GRAPH_OPTIONS, SECONDARY_CHARTS, TEMPLATE, DISPLAY_RESULTSET,"
					+ " XMLA_DATASOURCE, XMLA_CATALOG, DEFAULT_REPORT_FORMAT,"
					+ " OMIT_TITLE_ROW, HIDDEN_COLUMNS, TOTAL_COLUMNS, DATE_COLUMN_FORMAT,"
					+ " NUMBER_COLUMN_FORMAT, COLUMN_FORMATS, LOCALE,"
					+ " NULL_NUMBER_DISPLAY, NULL_STRING_DISPLAY, FETCH_SIZE,"
					+ " REPORT_OPTIONS, PAGE_ORIENTATION, LOV_USE_DYNAMIC_DATASOURCE,"
					+ " OPEN_PASSWORD, MODIFY_PASSWORD, ENCRYPTOR_ID, SOURCE_REPORT_ID,"
					+ " USE_GROOVY, PIVOTTABLEJS_SAVED_OPTIONS, GRIDSTACK_SAVED_OPTIONS,"
					+ " VIEW_REPORT_ID, SELF_SERVICE_OPTIONS,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 48) + ")";

			Object[] values = {
				newRecordId,
				report.getName(),
				report.getShortDescription(),
				report.getDescription(),
				report.getComment(),
				reportTypeId,
				report.getGroupColumn(),
				reportGroupId,
				datasourceId,
				report.getContactPerson(),
				BooleanUtils.toInteger(report.isUsesRules()),
				BooleanUtils.toInteger(report.isActive()),
				BooleanUtils.toInteger(report.isHidden()),
				report.getReportSource(),
				BooleanUtils.toInteger(report.isParametersInOutput()),
				report.getxAxisLabel(),
				report.getyAxisLabel(),
				report.getChartOptionsSetting(),
				report.getSecondaryCharts(),
				report.getTemplate(),
				report.getDisplayResultset(),
				report.getXmlaDatasource(),
				report.getXmlaCatalog(),
				report.getDefaultReportFormat(),
				BooleanUtils.toInteger(report.isOmitTitleRow()),
				report.getHiddenColumns(),
				report.getTotalColumns(),
				report.getDateFormat(),
				report.getNumberFormat(),
				report.getColumnFormats(),
				report.getLocale(),
				report.getNullNumberDisplay(),
				report.getNullStringDisplay(),
				report.getFetchSize(),
				report.getOptions(),
				report.getPageOrientation().getValue(),
				BooleanUtils.toInteger(report.isLovUseDynamicDatasource()),
				report.getOpenPassword(),
				report.getModifyPassword(),
				encryptorId,
				report.getSourceReportId(),
				BooleanUtils.toInteger(report.isUseGroovy()),
				report.getPivotTableJsSavedOptions(),
				report.getGridstackSavedOptions(),
				report.getViewReportId(),
				report.getSelfServiceOptions(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_QUERIES SET NAME=?, SHORT_DESCRIPTION=?,"
					+ " DESCRIPTION=?, DEVELOPER_COMMENT=?, QUERY_TYPE=?,"
					+ " GROUP_COLUMN=?, QUERY_GROUP_ID=?,"
					+ " DATASOURCE_ID=?, CONTACT_PERSON=?, USES_RULES=?, ACTIVE=?,"
					+ " HIDDEN=?, REPORT_SOURCE=?, PARAMETERS_IN_OUTPUT=?,"
					+ " X_AXIS_LABEL=?, Y_AXIS_LABEL=?,"
					+ " GRAPH_OPTIONS=?, SECONDARY_CHARTS=?, TEMPLATE=?, DISPLAY_RESULTSET=?,"
					+ " XMLA_DATASOURCE=?, XMLA_CATALOG=?, DEFAULT_REPORT_FORMAT=?,"
					+ " OMIT_TITLE_ROW=?, HIDDEN_COLUMNS=?, TOTAL_COLUMNS=?, DATE_COLUMN_FORMAT=?,"
					+ " NUMBER_COLUMN_FORMAT=?, COLUMN_FORMATS=?, LOCALE=?,"
					+ " NULL_NUMBER_DISPLAY=?, NULL_STRING_DISPLAY=?, FETCH_SIZE=?,"
					+ " REPORT_OPTIONS=?, PAGE_ORIENTATION=?, LOV_USE_DYNAMIC_DATASOURCE=?,"
					+ " OPEN_PASSWORD=?, MODIFY_PASSWORD=?, ENCRYPTOR_ID=?,"
					+ " SOURCE_REPORT_ID=?, USE_GROOVY=?, PIVOTTABLEJS_SAVED_OPTIONS=?,"
					+ " GRIDSTACK_SAVED_OPTIONS=?, VIEW_REPORT_ID=?,"
					+ " SELF_SERVICE_OPTIONS=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE QUERY_ID=?";

			Object[] values = {
				report.getName(),
				report.getShortDescription(),
				report.getDescription(),
				report.getComment(),
				report.getReportTypeId(),
				report.getGroupColumn(),
				reportGroupId,
				datasourceId,
				report.getContactPerson(),
				BooleanUtils.toInteger(report.isUsesRules()),
				BooleanUtils.toInteger(report.isActive()),
				BooleanUtils.toInteger(report.isHidden()),
				report.getReportSource(),
				BooleanUtils.toInteger(report.isParametersInOutput()),
				report.getxAxisLabel(),
				report.getyAxisLabel(),
				report.getChartOptionsSetting(),
				report.getSecondaryCharts(),
				report.getTemplate(),
				report.getDisplayResultset(),
				report.getXmlaDatasource(),
				report.getXmlaCatalog(),
				report.getDefaultReportFormat(),
				BooleanUtils.toInteger(report.isOmitTitleRow()),
				report.getHiddenColumns(),
				report.getTotalColumns(),
				report.getDateFormat(),
				report.getNumberFormat(),
				report.getColumnFormats(),
				report.getLocale(),
				report.getNullNumberDisplay(),
				report.getNullStringDisplay(),
				report.getFetchSize(),
				report.getOptions(),
				report.getPageOrientation().getValue(),
				BooleanUtils.toInteger(report.isLovUseDynamicDatasource()),
				report.getOpenPassword(),
				report.getModifyPassword(),
				encryptorId,
				report.getSourceReportId(),
				BooleanUtils.toInteger(report.isUseGroovy()),
				report.getPivotTableJsSavedOptions(),
				report.getGridstackSavedOptions(),
				report.getViewReportId(),
				report.getSelfServiceOptions(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				report.getReportId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		if (newRecordId != null) {
			report.setReportId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, report={}",
					affectedRows, newRecord, report);
		}
	}

	/**
	 * Updates multiple reports
	 *
	 * @param multipleReportEdit the multiple report edit details
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void updateReports(MultipleReportEdit multipleReportEdit, User actionUser) throws SQLException {
		logger.debug("Entering updateReports: multipleReportEdit={}, actionUser={}", multipleReportEdit, actionUser);

		String sql;

		List<Object> idsList = ArtUtils.idsToObjectList(multipleReportEdit.getIds());
		if (!multipleReportEdit.isActiveUnchanged()) {
			sql = "UPDATE ART_QUERIES SET ACTIVE=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE QUERY_ID IN(" + StringUtils.repeat("?", ",", idsList.size()) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleReportEdit.isActive()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(idsList);

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
		if (!multipleReportEdit.isHiddenUnchanged()) {
			sql = "UPDATE ART_QUERIES SET HIDDEN=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE QUERY_ID IN(" + StringUtils.repeat("?", ",", idsList.size()) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleReportEdit.isHidden()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(idsList);

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
		if (!multipleReportEdit.isContactPersonUnchanged()) {
			sql = "UPDATE ART_QUERIES SET CONTACT_PERSON=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE QUERY_ID IN(" + StringUtils.repeat("?", ",", idsList.size()) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(multipleReportEdit.getContactPerson());
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(idsList);

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
		if (!multipleReportEdit.isOmitTitleRowUnchanged()) {
			sql = "UPDATE ART_QUERIES SET OMIT_TITLE_ROW=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE QUERY_ID IN(" + StringUtils.repeat("?", ",", idsList.size()) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleReportEdit.isOmitTitleRow()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(idsList);

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

	/**
	 * Copies a report
	 *
	 * @param report the new report
	 * @param originalReportId the original report id
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public synchronized void copyReport(Report report, int originalReportId, User actionUser) throws SQLException {
		logger.debug("Entering copyReport: report={}, originalReportId={}, actionUser={}",
				report, originalReportId, actionUser);

		Connection conn = DbConnections.getArtDbConnection();
		boolean originalAutoCommit = true;

		try {
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			//insert new report
			int newId = addReport(report, actionUser, conn);

			//copy parameters
			copyTableRow(conn, "ART_REPORT_PARAMETERS", "REPORT_ID", originalReportId, newId, "REPORT_PARAMETER_ID");

			//copy rules
			copyTableRow(conn, "ART_QUERY_RULES", "QUERY_ID", originalReportId, newId, null);

			//copy drilldown reports
			copyTableRow(conn, "ART_DRILLDOWN_QUERIES", "QUERY_ID", originalReportId, newId, null);

			conn.commit();
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
			DatabaseUtils.close(conn);
		}
	}

	/**
	 * Copies some aspect of a report
	 *
	 * @param conn the connection to use
	 * @param tableName the name of the table to copy
	 * @param keyColumnName the name of the key column
	 * @param keyId the original value of the key column
	 * @param newKeyId the new value of the key column
	 * @param primaryKeyColumn the primary key of the table, that is to be
	 * incremented by 1. null if this is not required
	 * @return the number of records copied
	 * @throws SQLException
	 * @throws IllegalStateException if connection to the art database is not
	 * available
	 */
	private int copyTableRow(Connection conn, String tableName, String keyColumnName,
			int keyId, int newKeyId, String primaryKeyColumn) throws SQLException {

		logger.debug("Entering copyTableRow: tableName='{}', keyColumnName='{}',"
				+ " keyId={}, newKeyId={}", tableName, keyColumnName, keyId, newKeyId);

		int recordsCopied = 0;

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT * FROM " + tableName
					+ " WHERE " + keyColumnName + " = ?";

			rs = DatabaseUtils.query(conn, ps, sql, keyId);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			sql = "INSERT INTO " + tableName + " VALUES ("
					+ StringUtils.repeat("?", ",", columnCount) + ")";

			String sql2 = "SELECT MAX(" + primaryKeyColumn + ") FROM " + tableName;
			int id = dbService.getMaxRecordId(conn, sql2);

			while (rs.next()) {
				//insert new record for each existing record
				List<Object> columnValues = new ArrayList<>();
				for (int i = 0; i < columnCount; i++) {
					if (StringUtils.equalsIgnoreCase(rsmd.getColumnName(i + 1), keyColumnName)) {
						columnValues.add(newKeyId);
					} else if (primaryKeyColumn != null && StringUtils.equalsIgnoreCase(rsmd.getColumnName(i + 1), primaryKeyColumn)) {
						//generate new id
						id++;
						columnValues.add(id);
					} else {
						columnValues.add(rs.getObject(i + 1));
					}
				}

				dbService.update(conn, sql, columnValues.toArray());
				recordsCopied++;
			}
		} finally {
			DatabaseUtils.close(rs, ps);
		}

		return recordsCopied;
	}

	/**
	 * Returns the name of a given report
	 *
	 * @param reportId the report id
	 * @return the report name
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public String getReportName(int reportId) throws SQLException {
		logger.debug("Entering getReportName: reportId={}", reportId);

		String sql = "SELECT NAME FROM ART_QUERIES WHERE QUERY_ID=?";
		ResultSetHandler<String> h = new ScalarHandler<>(1);
		return dbService.query(sql, h, reportId);
	}

	/**
	 * Returns drilldown reports
	 *
	 * @return drilldown reports
	 * @throws SQLException
	 */
	//@Cacheable(value = "reports") //if you make this cacheable, you'll need to cacheevict "reports" in ParameterService
	public List<Report> getDrilldownReports() throws SQLException {
		logger.debug("Entering getDrilldownReports");

		//candidate drilldown report is a report with at least one inline parameter where drill down column > 0
		String sql = SQL_SELECT_ALL
				+ " WHERE EXISTS "
				+ " (SELECT * FROM ART_REPORT_PARAMETERS ARP, ART_PARAMETERS AP"
				+ " WHERE AQ.QUERY_ID = ARP.REPORT_ID AND ARP.PARAMETER_ID = AP.PARAMETER_ID"
				+ " AND AP.PARAMETER_TYPE = ? AND AP.DRILLDOWN_COLUMN_INDEX > 0)";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h, ParameterType.SingleValue.getValue());
	}

	/**
	 * Returns lov reports
	 *
	 * @return lov reports
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getLovReports() throws SQLException {
		logger.debug("Entering getLovReports");

		String sql = SQL_SELECT_ALL
				+ " WHERE AQ.QUERY_GROUP_ID=-1 OR QUERY_TYPE=119 OR QUERY_TYPE=120";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Returns dashboard reports
	 *
	 * @return dashboard reports
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getDashboardReports() throws SQLException {
		logger.debug("Entering getDashboardReports");

		String sql = SQL_SELECT_ALL
				+ " WHERE QUERY_TYPE=110 OR QUERY_TYPE=129";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Returns gridstack dashboard reports
	 *
	 * @return gridstack dashboard reports
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getGridstackDashboardReports() throws SQLException {
		logger.debug("Entering getGridstackDashboardReports");

		String sql = SQL_SELECT_ALL + " WHERE QUERY_TYPE=129";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h);
	}
	
	/**
	 * Returns self service reports
	 *
	 * @return self service reports
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getSelfServiceReports() throws SQLException {
		logger.debug("Entering getSelfServiceReports");

		String sql = SQL_SELECT_ALL + " WHERE VIEW_REPORT_ID>0";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Returns saiku reports that a given user can access
	 *
	 * @param userId the id of the user
	 * @param locale the locale to determine the report name, description, etc
	 * @return saiku reports that a given user can access
	 * @throws SQLException
	 * @throws java.io.IOException
	 */
	@Cacheable(value = "reports")
	public List<SaikuReport> getAvailableSaikuReports(int userId, Locale locale)
			throws SQLException, IOException {

		logger.debug("Entering getAvailableSaikuReports: userId={}", userId);

		List<SaikuReport> finalSaikuReports = new ArrayList<>();
		List<Report> availableSaikuReports = getAccessibleReportsWithReportTypes(userId, Arrays.asList(ReportType.SaikuReport));

		for (Report report : availableSaikuReports) {
			SaikuReport saikuReport = new SaikuReport();
			saikuReport.setReportId(report.getReportId());
			saikuReport.setName(report.getLocalizedName(locale));
			saikuReport.setShortDescription(report.getLocalizedShortDescription(locale));
			saikuReport.setDescription(report.getLocalizedDescription(locale));
			finalSaikuReports.add(saikuReport);
		}

		return finalSaikuReports;
	}

	/**
	 * Returns saiku connection reports that a given user can access
	 *
	 * @param userId the id of the user
	 * @return saiku connection reports that a given user can access
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getAvailableSaikuConnectionReports(int userId) throws SQLException {
		logger.debug("Entering getAvailableSaikuConnectionReports: userId={}", userId);

		return getAccessibleReportsWithReportTypes(userId, Arrays.asList(ReportType.SaikuConnection));
	}

	/**
	 * Returns view reports that a given user can access
	 *
	 * @param userId the id of the user
	 * @return view reports that a given user can access
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getAvailableViewReports(int userId) throws SQLException {
		logger.debug("Entering getAvailableViewReports: userId={}", userId);

		return getAccessibleReportsWithReportTypes(userId, Arrays.asList(ReportType.View));
	}

	/**
	 * Returns gridstack dashboard reports that a given user can access
	 *
	 * @param userId the id of the user
	 * @return gridstack dashboard reports that a given user can access
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getAvailableGridstackDashboardReports(int userId) throws SQLException {
		logger.debug("Entering getAvailableGridstackDashboardReports: userId={}", userId);

		return getAccessibleReportsWithReportTypes(userId, Arrays.asList(ReportType.GridstackDashboard));
	}

	/**
	 * Returns self service reports that a given user can access
	 *
	 * @param userId the id of the user
	 * @return self service reports that a given user can access
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getAvailableSelfServiceReports(int userId) throws SQLException {
		logger.debug("Entering getAvailableSelfServiceReports: userId={}", userId);

		List<ReportType> includedReportTypes = Arrays.asList(ReportType.Tabular);
		List<ReportType> excludedReportTypes = null;
		String extraCondition = "AND AQ.VIEW_REPORT_ID>0";

		return getAccessibleReports(userId, includedReportTypes, excludedReportTypes, extraCondition);
	}

	/**
	 * Returns dynamic recipient reports
	 *
	 * @return dynamic recipient reports
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public List<Report> getDynamicRecipientReports() throws SQLException {
		logger.debug("Entering getDynamicRecipientReports");

		String sql = SQL_SELECT_ALL
				+ " WHERE QUERY_TYPE=121";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Returns <code>true</code> if a user can run a report
	 *
	 * @param userId the user id
	 * @param reportId the report id
	 * @return <code>true</code> if a user can run a report
	 * @throws SQLException
	 */
//	@Cacheable(value = "reports") 
	public boolean canUserRunReport(int userId, int reportId) throws SQLException {
		logger.debug("Entering canUserRunReport: userId={}, reportId={}", userId, reportId);

		String sql = "SELECT COUNT(*)"
				+ " FROM ART_QUERIES AQ"
				+ " WHERE QUERY_ID=?"
				+ " AND("
				//everyone can run lov report
				+ " QUERY_TYPE IN(?,?)"
				+ " OR"
				//admins can run all reports
				+ " EXISTS (SELECT *"
				+ " FROM ART_USERS"
				+ " WHERE USER_ID=? AND ACCESS_LEVEL>=?)"
				+ " OR"
				//user can run report if he has direct access to it
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_QUERIES AUQ"
				+ " WHERE AUQ.QUERY_ID=AQ.QUERY_ID AND AUQ.USER_ID=?)"
				+ " OR"
				//user can run report if he belongs to a user group which has direct access to the report
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_GROUP_QUERIES AUGQ"
				+ " INNER JOIN ART_USER_GROUP_ASSIGNMENT AUGA"
				+ " ON AUGQ.USER_GROUP_ID=AUGA.USER_GROUP_ID"
				+ " WHERE AUGQ.QUERY_ID=AQ.QUERY_ID AND AUGA.USER_ID=?)"
				+ " OR"
				//user can run report if he has access to the report's group
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_QUERY_GROUPS AUQG"
				+ " INNER JOIN ART_REPORT_REPORT_GROUPS ARRG"
				+ " ON AUQG.QUERY_GROUP_ID=ARRG.REPORT_GROUP_ID"
				+ " WHERE ARRG.REPORT_ID=AQ.QUERY_ID AND AUQG.USER_ID=?)"
				+ " OR"
				//user can run report if his user group has access to the report's group
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_GROUP_GROUPS AUGG"
				+ " INNER JOIN ART_USER_GROUP_ASSIGNMENT AUGA"
				+ " ON AUGG.USER_GROUP_ID=AUGA.USER_GROUP_ID"
				+ " INNER JOIN ART_REPORT_REPORT_GROUPS ARRG"
				+ " ON AUGG.QUERY_GROUP_ID=ARRG.REPORT_GROUP_ID"
				+ " WHERE ARRG.REPORT_ID=AQ.QUERY_ID AND AUGA.USER_ID=?)"
				+ ")";

		Object[] values = {
			reportId,
			ReportType.LovDynamic.getValue(), //lov reports
			ReportType.LovStatic.getValue(),
			userId, //admin user
			AccessLevel.JuniorAdmin.getValue(),
			userId, //user access to report
			userId, //user group access to report
			userId, //user access to report group
			userId //user group access to report group
		};

		//some drivers return long for select count(*), some return integer
		//https://issues.apache.org/jira/browse/DBUTILS-27
		//https://issues.apache.org/jira/browse/DBUTILS-17
		//https://stackoverflow.com/questions/10240901/how-best-to-retrieve-result-of-select-count-from-sql-query-in-java-jdbc-lon
		//https://sourceforge.net/p/art/discussion/352129/thread/ee7c78d4/#3279
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number recordCount = dbService.query(sql, h, values);

		if (recordCount == null || recordCount.longValue() == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Grants access for a given report to a user
	 *
	 * @param report the report
	 * @param user the user
	 * @throws SQLException
	 */
	public void grantAccess(Report report, User user) throws SQLException {
		logger.debug("Entering grantAccess: report={}, user={}", report, user);

		String sql = "INSERT INTO ART_USER_QUERIES (USERNAME, USER_ID, QUERY_ID)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 3) + ")";

		Object[] values = {
			user.getUsername(),
			user.getUserId(),
			report.getReportId()
		};

		dbService.update(sql, values);
	}

	/**
	 * Returns <code>true</code> if a report is only directly allocated to a
	 * single user or if the user is the owner of the report
	 *
	 * @param user the user
	 * @param reportId the id of the report
	 * @return <code>true</code> if user has exclusive access to the report
	 * @throws java.sql.SQLException
	 */
	public boolean hasExclusiveOrOwnerAccess(User user, int reportId) throws SQLException {
		logger.debug("Entering hasExclusiveOrOwnerAccess: user={}, reportId={}", user, reportId);

		boolean owner;
		boolean exclusive = false;

		//check if user is the owner of the report
		String sql = "SELECT COUNT(*) FROM ART_QUERIES"
				+ " WHERE QUERY_ID=? AND CREATED_BY=?";
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number recordCount = dbService.query(sql, h, reportId, user.getUsername());

		if (recordCount == null || recordCount.longValue() == 0) {
			owner = false;
		} else {
			owner = true;
		}

		if (!owner) {
			//check if user has exclusive access
			int userAccessCount = 0;
			boolean userHasAccess = false;
			boolean assignedToGroup = false;

			sql = "SELECT USER_GROUP_ID FROM ART_USER_GROUP_QUERIES "
					+ " WHERE QUERY_ID=?";

			ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
			List<Map<String, Object>> userGroupsList = dbService.query(sql, h2, reportId);

			if (!userGroupsList.isEmpty()) {
				//report granted to a group. user doesn't have exclusive access
				assignedToGroup = true;
			}

			if (!assignedToGroup) {
				sql = "SELECT USERNAME FROM ART_USER_QUERIES "
						+ " WHERE QUERY_ID=?";

				ResultSetHandler<List<Map<String, Object>>> h3 = new MapListHandler();
				List<Map<String, Object>> usersList = dbService.query(sql, h3, reportId);

				String username = user.getUsername();
				for (Map<String, Object> userRecord : usersList) {
					userAccessCount++;
					if (userAccessCount >= 2) {
						//more than one user has access
						break;
					}
					//map list handler uses a case insensitive map, so case of column names doesn't matter
					String usernameValue = (String) userRecord.get("USERNAME");
					if (StringUtils.equals(username, usernameValue)) {
						userHasAccess = true;
					}
				}
			}

			if (!assignedToGroup && userHasAccess && userAccessCount == 1) {
				//only one user has explicit access
				exclusive = true;
			}
		}

		if (owner || exclusive) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns reports that use a given parameter
	 *
	 * @param parameterId the parameter id
	 * @return reports that use the parameter
	 * @throws SQLException
	 */
	public List<Report> getReportsForParameter(int parameterId) throws SQLException {
		logger.debug("Entering getReportsForParameter: parameterId={}", parameterId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_REPORT_PARAMETERS ARP"
				+ " ON ARP.REPORT_ID=AQ.QUERY_ID"
				+ " WHERE ARP.PARAMETER_ID=?";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h, parameterId);
	}

	/**
	 * Returns reports that use a given datasource
	 *
	 * @param datasourceId the datasource id
	 * @return reports that use the datasource
	 * @throws SQLException
	 */
	public List<Report> getReportsWithDatasource(int datasourceId) throws SQLException {
		logger.debug("Entering getReportsWithDatasource: datasourceId={}", datasourceId);

		String sql = SQL_SELECT_ALL
				+ " WHERE DATASOURCE_ID=?";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h, datasourceId);
	}

	/**
	 * Returns reports that use a given encryptor
	 *
	 * @param encryptorId the encryptor id
	 * @return reports that use the encryptor
	 * @throws SQLException
	 */
	public List<Report> getReportsWithEncryptor(int encryptorId) throws SQLException {
		logger.debug("Entering getReportsWithEncryptor: encryptorId={}", encryptorId);

		String sql = SQL_SELECT_ALL
				+ " WHERE ENCRYPTOR_ID=?";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h, encryptorId);
	}

	/**
	 * Returns reports are in a given report group
	 *
	 * @param reportGroupId the report group id
	 * @return reports that are in the report group
	 * @throws SQLException
	 */
	public List<Report> getReportsInReportGroup(int reportGroupId) throws SQLException {
		logger.debug("Entering getReportsInReportGroup: reportGroupId={}", reportGroupId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_REPORT_REPORT_GROUPS ARRG"
				+ " ON ARRG.REPORT_ID=AQ.QUERY_ID"
				+ " WHERE ARRG.REPORT_GROUP_ID=?";

		ResultSetHandler<List<Report>> h = new BeanListHandler<>(Report.class, new ReportMapper());
		return dbService.query(sql, h, reportGroupId);
	}

	/**
	 * Returns <code>true</code> if a report name exists
	 *
	 * @param reportName the user id
	 * @return <code>true</code> if a report name exists
	 * @throws SQLException
	 */
	@Cacheable(value = "reports")
	public boolean reportNameExists(String reportName) throws SQLException {
		logger.debug("Entering reportNameExists: reportName='{}'", reportName);

		String sql = "SELECT COUNT(*) FROM ART_QUERIES"
				+ " WHERE NAME=?";
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number recordCount = dbService.query(sql, h, reportName);

		if (recordCount == null || recordCount.longValue() == 0) {
			return false;
		} else {
			return true;
		}
	}

}
