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
import art.encryption.AesEncryptor;
import art.encryptor.Encryptor;
import art.encryptor.EncryptorService;
import art.enums.AccessLevel;
import art.enums.PageOrientation;
import art.enums.ParameterType;
import art.enums.ReportType;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.saiku.SaikuReport;
import art.user.User;
import art.utils.ActionResult;
import art.utils.ArtHelper;
import art.utils.ArtUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

	@Autowired
	public ReportService(DbService dbService, DatasourceService datasourceService,
			ReportGroupService reportGroupService, EncryptorService encryptorService) {

		this.dbService = dbService;
		this.datasourceService = datasourceService;
		this.reportGroupService = reportGroupService;
		this.encryptorService = encryptorService;
	}

	public ReportService() {
		dbService = new DbService();
		datasourceService = new DatasourceService();
		reportGroupService = new ReportGroupService();
		encryptorService = new EncryptorService();
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
			report.setReportTypeId(rs.getInt("QUERY_TYPE"));
			report.setReportType(ReportType.toEnum(rs.getInt("QUERY_TYPE")));
			report.setGroupColumn(rs.getInt("GROUP_COLUMN"));
			report.setContactPerson(rs.getString("CONTACT_PERSON"));
			report.setUsesRules(rs.getBoolean("USES_RULES"));
			report.setActive(rs.getBoolean("ACTIVE"));
			report.setHidden(rs.getBoolean("HIDDEN"));
			report.setParametersInOutput(rs.getBoolean("PARAMETERS_IN_OUTPUT"));
			report.setxAxisLabel(rs.getString("X_AXIS_LABEL"));
			report.setyAxisLabel(rs.getString("Y_AXIS_LABEL"));
			report.setChartOptionsSetting(rs.getString("GRAPH_OPTIONS"));
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
			report.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			report.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			report.setCreatedBy(rs.getString("CREATED_BY"));
			report.setUpdatedBy(rs.getString("UPDATED_BY"));

			ReportGroup reportGroup = reportGroupService.getReportGroup(rs.getInt("QUERY_GROUP_ID"));
			report.setReportGroup(reportGroup);

			Datasource datasource = datasourceService.getDatasource(rs.getInt("DATABASE_ID"));
			report.setDatasource(datasource);
			
			Encryptor encryptor = encryptorService.getEncryptor(rs.getInt("ENCRYPTOR_ID"));
			report.setEncryptor(encryptor);

			//decrypt open and modify passwords
			String clearTextOpenPassword = AesEncryptor.decrypt(report.getOpenPassword());
			report.setOpenPassword(clearTextOpenPassword);

			String clearTextModifypassword = AesEncryptor.decrypt(report.getModifyPassword());
			report.setModifyPassword(clearTextModifypassword);

			setChartOptions(report);

			try {
				report.loadGeneralOptions();
			} catch (IOException ex) {
				throw new SQLException(ex);
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
	 * @return accesible reports
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public List<Report> getAccessibleReports(int userId) throws SQLException {
		logger.debug("Entering getAccessibleReports: userId={}", userId);

		String sql = SQL_SELECT_ALL
				//only show active reports
				+ " WHERE AQ.ACTIVE=1"
				//don't show hidden reports
				+ " AND AQ.HIDDEN<>1"
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
				+ " FROM ART_USER_QUERY_GROUPS AUQG"
				+ " WHERE AUQG.QUERY_GROUP_ID=AQ.QUERY_GROUP_ID AND AUQG.USER_ID=?)"
				+ " OR"
				//user can run report if his user group has access to the report's group
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_GROUP_GROUPS AUGG"
				+ " INNER JOIN ART_USER_GROUP_ASSIGNMENT AUGA"
				+ " ON AUGG.USER_GROUP_ID=AUGA.USER_GROUP_ID"
				+ " WHERE AUGG.QUERY_GROUP_ID=AQ.QUERY_GROUP_ID AND AUGA.USER_ID=?)"
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

		List<Report> accessibleReports = getAccessibleReports(userId);
		List<Report> displayReports = new ArrayList<>();

		for (Report report : accessibleReports) {
			ReportType reportType = report.getReportType();
			switch (reportType) {
				case LovDynamic:
				case LovStatic:
				case JobRecipients:
				case SaikuConnection:
					//do nothing. don't add to new list
					break;
				default:
					displayReports.add(report);
			}
		}

		return displayReports;
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

		setReportSource(report);

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

		setReportSource(report);

		return report;
	}

	/**
	 * Returns jobs that use a given report
	 *
	 * @param reportId the report id
	 * @return linked job names
	 * @throws SQLException
	 */
	public List<String> getLinkedJobs(int reportId) throws SQLException {
		logger.debug("Entering getLinkedJobs: reportId={}", reportId);

		String sql = "SELECT JOB_NAME"
				+ " FROM ART_JOBS"
				+ " WHERE QUERY_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>(1);
		return dbService.query(sql, h, reportId);
	}

	/**
	 * Deletes a report
	 *
	 * @param id the report id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * jobs which prevented the report from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
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

		//delete report parameters
		sql = "DELETE FROM ART_REPORT_PARAMETERS WHERE REPORT_ID=?";
		dbService.update(sql, id);

		//delete query-rule relationships
		sql = "DELETE FROM ART_QUERY_RULES WHERE QUERY_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_QUERIES WHERE QUERY_ID=?";
		dbService.update(sql, id);

		//delete drilldown queries
		sql = "DELETE FROM ART_DRILLDOWN_QUERIES WHERE QUERY_ID=?";
		dbService.update(sql, id);

		//delete sql source
		sql = "DELETE FROM ART_ALL_SOURCES WHERE OBJECT_ID=?";
		dbService.update(sql, id);

		//lastly, delete query
		sql = "DELETE FROM ART_QUERIES WHERE QUERY_ID=?";
		int affectedRows = dbService.update(sql, id);
		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with delete. affectedRows={}, id={}", affectedRows, id);
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
	@CacheEvict(value = "reports", allEntries = true)
	public ActionResult deleteReports(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteReports: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<Integer> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteReport(id);
			if (!deleteResult.isSuccess()) {
				nonDeletedRecords.add(id);
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
		logger.debug("Entering addReport: report={}, actionUser={}", report, actionUser);

		//generate new id
		String sql = "SELECT MAX(QUERY_ID) FROM ART_QUERIES";
		ResultSetHandler<Integer> h = new ScalarHandler<>();
		Integer maxId = dbService.query(sql, h);
		logger.debug("maxId={}", maxId);

		int newId;
		if (maxId == null || maxId < 0) {
			//no records in the table, or only hardcoded records
			newId = 1;
		} else {
			newId = maxId + 1;
		}
		logger.debug("newId={}", newId);

		saveReport(report, newId, actionUser);

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
		logger.debug("Entering updateReport: report={}, actionUser={}", report, actionUser);

		Integer newRecordId = null;
		saveReport(report, newRecordId, actionUser);
	}

	/**
	 * Saves a report
	 *
	 * @param report the report to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	private void saveReport(Report report, Integer newRecordId, User actionUser) throws SQLException {
		logger.debug("Entering saveReport: report={}, newRecordId={}, actionUser={}",
				report, newRecordId, actionUser);

		//set values for possibly null property objects
		Integer reportGroupId; //database column doesn't allow null
		if (report.getReportGroup() == null) {
			reportGroupId = 0;
		} else {
			reportGroupId = report.getReportGroup().getReportGroupId();
		}

		Integer datasourceId; //database column doesn't allow null
		if (report.getDatasource() == null) {
			datasourceId = 0;
		} else {
			datasourceId = report.getDatasource().getDatasourceId();
		}
		
		Integer encryptorId;
		if (report.getEncryptor() == null) {
			encryptorId = 0;
		} else {
			encryptorId = report.getEncryptor().getEncryptorId();
		}

		Integer reportTypeId;
		if (report.getReportType() == null) {
			logger.warn("Report type not defined. Defaulting to 0");
			reportTypeId = 0;
		} else {
			reportTypeId = report.getReportType().getValue();
		}

		String shortDescription; //database column doesn't allow null
		if (report.getShortDescription() == null) {
			shortDescription = "";
		} else {
			shortDescription = report.getShortDescription();
		}

		String description; //database column doesn't allow null
		if (report.getDescription() == null) {
			description = "";
		} else {
			description = report.getDescription();
		}

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_QUERIES"
					+ " (QUERY_ID, NAME, SHORT_DESCRIPTION, DESCRIPTION, QUERY_TYPE,"
					+ " GROUP_COLUMN, QUERY_GROUP_ID, DATABASE_ID, CONTACT_PERSON, USES_RULES,"
					+ " ACTIVE, HIDDEN, PARAMETERS_IN_OUTPUT, X_AXIS_LABEL, Y_AXIS_LABEL,"
					+ " GRAPH_OPTIONS, SECONDARY_CHARTS, TEMPLATE, DISPLAY_RESULTSET,"
					+ " XMLA_DATASOURCE, XMLA_CATALOG, DEFAULT_REPORT_FORMAT,"
					+ " OMIT_TITLE_ROW, HIDDEN_COLUMNS, TOTAL_COLUMNS, DATE_COLUMN_FORMAT,"
					+ " NUMBER_COLUMN_FORMAT, COLUMN_FORMATS, LOCALE,"
					+ " NULL_NUMBER_DISPLAY, NULL_STRING_DISPLAY, FETCH_SIZE,"
					+ " REPORT_OPTIONS, PAGE_ORIENTATION, LOV_USE_DYNAMIC_DATASOURCE,"
					+ " OPEN_PASSWORD, MODIFY_PASSWORD, ENCRYPTOR_ID,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 40) + ")";

			Object[] values = {
				newRecordId,
				report.getName(),
				shortDescription,
				description,
				reportTypeId,
				report.getGroupColumn(),
				reportGroupId,
				encryptorId,
				report.getContactPerson(),
				BooleanUtils.toInteger(report.isUsesRules()),
				BooleanUtils.toInteger(report.isActive()),
				BooleanUtils.toInteger(report.isHidden()),
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
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_QUERIES SET NAME=?, SHORT_DESCRIPTION=?,"
					+ " DESCRIPTION=?, QUERY_TYPE=?, GROUP_COLUMN=?, QUERY_GROUP_ID=?,"
					+ " DATABASE_ID=?, CONTACT_PERSON=?, USES_RULES=?, ACTIVE=?,"
					+ " HIDDEN=?, PARAMETERS_IN_OUTPUT=?, X_AXIS_LABEL=?, Y_AXIS_LABEL=?,"
					+ " GRAPH_OPTIONS=?, SECONDARY_CHARTS=?, TEMPLATE=?, DISPLAY_RESULTSET=?,"
					+ " XMLA_DATASOURCE=?, XMLA_CATALOG=?, DEFAULT_REPORT_FORMAT=?,"
					+ " OMIT_TITLE_ROW=?, HIDDEN_COLUMNS=?, TOTAL_COLUMNS=?, DATE_COLUMN_FORMAT=?,"
					+ " NUMBER_COLUMN_FORMAT=?, COLUMN_FORMATS=?, LOCALE=?,"
					+ " NULL_NUMBER_DISPLAY=?, NULL_STRING_DISPLAY=?, FETCH_SIZE=?,"
					+ " REPORT_OPTIONS=?, PAGE_ORIENTATION=?, LOV_USE_DYNAMIC_DATASOURCE=?,"
					+ " OPEN_PASSWORD=?, MODIFY_PASSWORD=?, ENCRYPTOR_ID=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE QUERY_ID=?";

			Object[] values = {
				report.getName(),
				shortDescription,
				description,
				report.getReportTypeId(),
				report.getGroupColumn(),
				reportGroupId,
				encryptorId,
				report.getContactPerson(),
				BooleanUtils.toInteger(report.isUsesRules()),
				BooleanUtils.toInteger(report.isActive()),
				BooleanUtils.toInteger(report.isHidden()),
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
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				report.getReportId()
			};

			affectedRows = dbService.update(sql, values);
		}

		if (newRecordId != null) {
			report.setReportId(newRecordId);
		}

		updateReportSource(report.getReportId(), report.getReportSource());

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

		String[] ids = StringUtils.split(multipleReportEdit.getIds(), ",");
		if (!multipleReportEdit.isActiveUnchanged()) {
			sql = "UPDATE ART_QUERIES SET ACTIVE=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE QUERY_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleReportEdit.isActive()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(Arrays.asList(ids));

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

	/**
	 * Updates the report source for a given report
	 *
	 * @param reportId the report id
	 * @param reportSource the new report source
	 * @throws SQLException
	 */
	public void updateReportSource(int reportId, String reportSource) throws SQLException {
		logger.debug("Entering updateReportSource: reportId={}", reportId);

		// Delete Old Source
		String sql = "DELETE FROM ART_ALL_SOURCES WHERE OBJECT_ID=?";
		dbService.update(sql, reportId);

		// Write the source in small segments
		// This guarantees portability across databases with different max VARCHAR sizes
		sql = "INSERT INTO ART_ALL_SOURCES "
				+ " (OBJECT_ID, LINE_NUMBER, SOURCE_INFO)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 3) + ")";

		if (reportSource == null) {
			reportSource = "";
		}

		final int SOURCE_CHUNK_LENGTH = 4000; //length of column that holds report source

		List<Object[]> values = new ArrayList<>();

		int start = 0;
		int end = SOURCE_CHUNK_LENGTH;
		int lineNumber = 1;
		int textLength = reportSource.length();

		while (end < textLength) {
			values.add(new Object[]{
				reportId, lineNumber,
				reportSource.substring(start, end)
			});
			start = end;
			end = end + SOURCE_CHUNK_LENGTH;
			lineNumber++;
		}
		values.add(new Object[]{
			reportId, lineNumber,
			reportSource.substring(start)
		});

		dbService.batch(sql, values.toArray(new Object[0][]));
	}

	/**
	 * Populates the report source property for a report
	 *
	 * @param report the report to use
	 * @throws SQLException
	 */
	private void setReportSource(Report report) throws SQLException {
		logger.debug("Entering setReportSource: report={}", report);

		if (report == null) {
			return;
		}

		String sql = "SELECT SOURCE_INFO"
				+ " FROM ART_ALL_SOURCES "
				+ " WHERE OBJECT_ID=?"
				+ " ORDER BY LINE_NUMBER";

		int reportId = report.getReportId();

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> sourceLines = dbService.query(sql, h, reportId);

		StringBuilder sb = new StringBuilder(1024);
		for (Map<String, Object> sourceLine : sourceLines) {
			//map list handler uses a case insensitive map, so case of column names doesn't matter
			String line = (String) sourceLine.get("SOURCE_INFO");
			sb.append(line);
		}

		String finalSource = sb.toString();
		report.setReportSource(finalSource);
		//set html source for use with text reports
		ReportType reportType = report.getReportType();
		if (reportType == ReportType.Text) {
			report.setReportSourceHtml(report.getReportSource());
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

		//insert new report
		int newId = addReport(report, actionUser);

		try {
			//copy parameters
			copyTableRow("ART_REPORT_PARAMETERS", "REPORT_ID", originalReportId, newId, "REPORT_PARAMETER_ID");

			//copy rules
			copyTableRow("ART_QUERY_RULES", "QUERY_ID", originalReportId, newId, null);

			//copy drilldown reports
			copyTableRow("ART_DRILLDOWN_QUERIES", "QUERY_ID", originalReportId, newId, null);
		} catch (SQLException ex) {
			//if an error occurred when copying new report details, delete new report also
			deleteReport(newId);
			throw ex;
		}
	}

	/**
	 * Copies some aspect of a report
	 *
	 * @param tableName the name of the table to copy
	 * @param keyColumnName the name of the key column
	 * @param keyId the original value of the key column
	 * @param newKeyId the new value of the key column
	 * @param primaryKeyColumn the primary key of the table, that is to be
	 * incremented by 1. null if this is not required
	 * @return the number of records copied, 0 otherwise
	 * @throws SQLException
	 * @throws IllegalStateException if connection to the art database is not
	 * available
	 */
	private int copyTableRow(String tableName, String keyColumnName,
			int keyId, int newKeyId, String primaryKeyColumn) throws SQLException {

		logger.debug("Entering copyTableRow: tableName='{}', keyColumnName='{}',"
				+ " keyId={}, newKeyId={}", tableName, keyColumnName, keyId, newKeyId);

		int count = 0; //number of records copied

		Connection conn = DbConnections.getArtDbConnection();

		if (conn == null) {
			throw new IllegalStateException("Connection to the ART Database not available");
		}

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

			while (rs.next()) {
				//insert new record for each existing record
				List<Object> columnValues = new ArrayList<>();
				for (int i = 0; i < columnCount; i++) {
					if (StringUtils.equalsIgnoreCase(rsmd.getColumnName(i + 1), keyColumnName)) {
						columnValues.add(newKeyId);
					} else if (primaryKeyColumn != null && StringUtils.equalsIgnoreCase(rsmd.getColumnName(i + 1), primaryKeyColumn)) {
						//generate new id
						String sql2 = "SELECT MAX(" + primaryKeyColumn + ") FROM " + tableName;
						ResultSetHandler<Integer> h = new ScalarHandler<>();
						Integer maxId = dbService.query(sql2, h);
						logger.debug("maxId={}", maxId);

						int newId = maxId + 1;;
						columnValues.add(newId);
					} else {
						columnValues.add(rs.getObject(i + 1));
					}
				}

				dbService.update(sql, columnValues.toArray());
				count++;
			}
		} finally {
			DatabaseUtils.close(rs, ps, conn);
		}

		return count;
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
	@Cacheable(value = "reports")
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

		List<SaikuReport> saikuReports = new ArrayList<>();
		List<Report> availableReports = getAccessibleReports(userId);

		for (Report report : availableReports) {
			if (report.getReportType() == ReportType.SaikuReport) {
				SaikuReport saikuReport = new SaikuReport();
				saikuReport.setReportId(report.getReportId());
				saikuReport.setName(report.getLocalizedName(locale));
				saikuReport.setShortDescription(report.getLocalizedShortDescription(locale));
				saikuReport.setDescription(report.getLocalizedDescription(locale));
				saikuReports.add(saikuReport);
			}
		}

		return saikuReports;
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

		List<Report> saikuConnectionReports = new ArrayList<>();
		List<Report> availableReports = getAccessibleReports(userId);

		//http://zetcode.com/articles/javafilterlist/
		//http://guidogarcia.net/blog/2011/10/29/java-different-ways-filter-collection/
		for (Report report : availableReports) {
			if (report.getReportType() == ReportType.SaikuConnection) {
				saikuConnectionReports.add(report);
			}
		}

		return saikuConnectionReports;
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

		boolean canRunReport;

		String sql = "SELECT COUNT(*)"
				+ " FROM ART_QUERIES AQ"
				+ " WHERE QUERY_ID=?"
				+ " AND("
				//everyone can run lov report
				+ " QUERY_TYPE IN(?,?)"
				+ " OR"
				//everyone can run report if the public user has direct access to it
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_QUERIES AUQ"
				+ " INNER JOIN ART_USERS AU"
				+ " ON AUQ.USER_ID=AU.USER_ID"
				+ " WHERE AUQ.QUERY_ID=AQ.QUERY_ID AND AU.USERNAME=?)"
				+ " OR"
				//everyone can run report if the public user has access to the report's group
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_QUERY_GROUPS AUQG"
				+ " INNER JOIN ART_USERS AU"
				+ " ON AUQG.USER_ID=AU.USER_ID"
				+ " WHERE AUQG.QUERY_GROUP_ID=AQ.QUERY_GROUP_ID AND AU.USERNAME=?)"
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
				+ " WHERE AUQG.QUERY_GROUP_ID=AQ.QUERY_GROUP_ID AND AUQG.USER_ID=?)"
				+ " OR"
				//user can run report if his user group has access to the report's group
				+ " EXISTS (SELECT *"
				+ " FROM ART_USER_GROUP_GROUPS AUGG"
				+ " INNER JOIN ART_USER_GROUP_ASSIGNMENT AUGA"
				+ " ON AUGG.USER_GROUP_ID=AUGA.USER_GROUP_ID"
				+ " WHERE AUGG.QUERY_GROUP_ID=AQ.QUERY_GROUP_ID AND AUGA.USER_ID=?)"
				+ ")";

		Object[] values = {
			reportId,
			ReportType.LovDynamic.getValue(), //lov reports
			ReportType.LovStatic.getValue(),
			ArtUtils.PUBLIC_USER, //public user access to report
			ArtUtils.PUBLIC_USER, //public user access to report's group
			userId, //admin user
			AccessLevel.JuniorAdmin.getValue(),
			userId, //user access to report
			userId, //user group access to report
			userId, //user access to report group
			userId //user group access to report group
		};

		//some drivers return long, some integer
		//https://issues.apache.org/jira/browse/DBUTILS-27
		//https://issues.apache.org/jira/browse/DBUTILS-17
		//https://stackoverflow.com/questions/10240901/how-best-to-retrieve-result-of-select-count-from-sql-query-in-java-jdbc-lon
		//https://sourceforge.net/p/art/discussion/352129/thread/ee7c78d4/#3279
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number recordCountNumber = dbService.query(sql, h, values);
		if (recordCountNumber == null) {
			canRunReport = false;
		} else {
			long recordCountLong = recordCountNumber.longValue();
			if (recordCountLong == 0) {
				canRunReport = false;
			} else {
				canRunReport = true;
			}
		}

		return canRunReport;
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
	 * single user
	 *
	 * @param user the user
	 * @param reportId the id of the report
	 * @return <code>true</code> if user has exclusive access to the report
	 * @throws java.sql.SQLException
	 */
	public boolean hasExclusiveAccess(User user, int reportId) throws SQLException {
		logger.debug("Entering hasExclusiveAccess: user={}, reportId={}", user, reportId);

		boolean exclusive = false;

		String sql;
		int userAccessCount = 0;
		boolean userHasAccess = false;
		boolean assignedToGroup = false;

		sql = "SELECT USER_GROUP_ID FROM ART_USER_GROUP_QUERIES "
				+ " WHERE QUERY_ID = ?";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> userGroupsList = dbService.query(sql, h, reportId);

		if (!userGroupsList.isEmpty()) {
			//query granted to a group. user doesn't have exclusive access
			assignedToGroup = true;
		}

		if (!assignedToGroup) {
			sql = "SELECT USERNAME FROM ART_USER_QUERIES "
					+ " WHERE QUERY_ID = ?";

			ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
			List<Map<String, Object>> usersList = dbService.query(sql, h2, reportId);

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

		return exclusive;
	}
}
