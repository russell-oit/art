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
package art.utils;

import art.dbutils.DbService;
import art.encryption.AesEncryptor;
import art.encryption.DesEncryptor;
import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.job.Job;
import art.job.JobService;
import art.user.User;
import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs upgrade steps on the art database. This is in addition to the manually
 * run upgrade scripts.
 *
 * @author Timothy Anyona
 */
public class UpgradeHelper {

	private static final Logger logger = LoggerFactory.getLogger(UpgradeHelper.class);

	private final DbService dbService;

	public UpgradeHelper() {
		dbService = new DbService();
	}

	/**
	 * Runs upgrade steps
	 *
	 * @param templatesPath the path to the templates directory
	 * @throws java.lang.Exception
	 */
	public void upgrade(String templatesPath) throws Exception {
		migrateJobsToQuartz();
		upgradeDatabase(templatesPath);
	}

	/**
	 * Migrates art jobs to quartz jobs
	 *
	 * @throws java.sql.SQLException
	 */
	private void migrateJobsToQuartz() throws SQLException {
		Scheduler scheduler = SchedulerUtils.getScheduler();

		if (scheduler == null) {
			logger.warn("Can't migrate jobs to Quartz. Scheduler not available");
			return;
		}

		String sql = "UPDATE ART_JOBS SET MIGRATED_TO_QUARTZ=NULL"
				+ " WHERE JOB_ID=?";

		User actionUser = new User();
		actionUser.setUsername("art migration");

		int nonQuartzJobCount = 0;
		int migratedCount = 0;

		JobService jobService = new JobService();

		List<Job> nonQuartzJobs = jobService.getNonQuartzJobs();
		for (Job job : nonQuartzJobs) {
			nonQuartzJobCount++;

			if (nonQuartzJobCount == 1) {
				logger.info("Migrating jobs to quartz...");
			}

			if (!job.isActive()) {
				continue;
			}

			Date now = new Date();
			Date endDate = job.getEndDate();
			if (endDate != null && endDate.before(now)) {
				continue;
			}

			int jobId = job.getJobId();

			try {
				jobService.processSchedules(job, actionUser);
				dbService.update(sql, jobId);
				migratedCount++;
			} catch (ParseException | SchedulerException | SQLException | RuntimeException ex) {
				logger.error("Error. Job Id {}", jobId, ex);
			}
		}

		if (nonQuartzJobCount > 0) {
			logger.info("Finished migrating jobs to quartz. Migrated {} out of {} jobs.", migratedCount, nonQuartzJobCount);
		}
	}

	/**
	 * Runs upgrade steps
	 *
	 * @param templatesPath the path to the templates directory
	 * @throws java.lang.Exception
	 */
	private void upgradeDatabase(String templatesPath) throws Exception {
		upgradeDatabaseTo30(templatesPath);
		upgradeDatabaseTo31();
		upgradeDatabaseTo38();
		upgradeDatabaseTo41();
	}

	/**
	 * Upgrades the database to 3.0
	 *
	 * @param templatesPath the path to the templates directory
	 * @throws java.lang.Exception
	 */
	private void upgradeDatabaseTo30(String templatesPath) throws Exception {
		logger.debug("Entering upgradeDatabaseTo30: templatesPath='{}'", templatesPath);

		String databaseVersionString = "3.0";
		String sql = "SELECT UPGRADED FROM ART_CUSTOM_UPGRADES WHERE DATABASE_VERSION=?";
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number upgradedValue = dbService.query(sql, h, databaseVersionString);
		if (upgradedValue == null || upgradedValue.intValue() == 1) {
			return;
		}

		logger.info("Performing 3.0 upgrade steps");

		addUserIds();
		addScheduleIds();
		addDrilldownIds();
		addRuleIds();
		addQueryRuleIds();
		addParameters();
		addUserRuleValueKeys();
		addUserGroupRuleValueKeys();
		addCachedDatasourceIds();
		updateDatasourcePasswords();

		sql = "UPDATE ART_CUSTOM_UPGRADES SET UPGRADED=1 WHERE DATABASE_VERSION=?";
		dbService.update(sql, databaseVersionString);

		logger.info("Done performing 3.0 upgrade steps");

		deleteDotJasperFiles(templatesPath);
	}

	/**
	 * Upgrades the database to 3.1
	 *
	 * @throws java.sql.SQLException
	 */
	private void upgradeDatabaseTo31() throws SQLException {
		logger.debug("Entering upgradeDatabaseTo31");

		String databaseVersionString = "3.1";
		String sql = "SELECT UPGRADED FROM ART_CUSTOM_UPGRADES WHERE DATABASE_VERSION=?";
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number upgradedValue = dbService.query(sql, h, databaseVersionString);
		if (upgradedValue == null || upgradedValue.intValue() == 1) {
			return;
		}

		logger.info("Performing 3.1 upgrade steps");

		populateReportSourceColumn();
		populateReportReportGroupsTable();
		populateDestinationsTable();

		sql = "UPDATE ART_CUSTOM_UPGRADES SET UPGRADED=1 WHERE DATABASE_VERSION=?";
		dbService.update(sql, databaseVersionString);

		logger.info("Done performing 3.1 upgrade steps");
	}

	/**
	 * Populates the destinations table.Table added in 3.1
	 *
	 * @throws java.sql.SQLException
	 */
	private void populateDestinationsTable() throws SQLException {
		logger.debug("Entering populateDestinationsTable");

		String sql;

		sql = "SELECT * FROM ART_FTP_SERVERS WHERE MIGRATED IS NULL";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> ftpServers = dbService.query(sql, h);

		logger.debug("ftpServers.isEmpty()={}", ftpServers.isEmpty());
		if (!ftpServers.isEmpty()) {
			logger.info("Moving ftp server records");

			//generate new destination id
			sql = "SELECT MAX(DESTINATION_ID) FROM ART_DESTINATIONS";
			int maxId = dbService.getMaxRecordId(sql);

			for (Map<String, Object> ftpServer : ftpServers) {
				maxId++;

				//create destination definition
				sql = "INSERT INTO ART_DESTINATIONS"
						+ " (DESTINATION_ID, NAME, DESCRIPTION, ACTIVE, DESTINATION_TYPE,"
						+ " SERVER, PORT, DESTINATION_USER, DESTINATION_PASSWORD,"
						+ " DESTINATION_PATH, CREATION_DATE, CREATED_BY,"
						+ " UPDATE_DATE, UPDATED_BY)"
						+ " VALUES(" + StringUtils.repeat("?", ",", 14) + ")";

				Number ftpServerId = (Number) ftpServer.get("FTP_SERVER_ID");

				Object[] values = {
					maxId,
					ftpServer.get("NAME"),
					ftpServer.get("DESCRIPTION"),
					ftpServer.get("ACTIVE"),
					ftpServer.get("CONNECTION_TYPE"),
					ftpServer.get("SERVER"),
					ftpServer.get("PORT"),
					ftpServer.get("FTP_USER"),
					ftpServer.get("PASSWORD"),
					ftpServer.get("REMOTE_DIRECTORY"),
					ftpServer.get("CREATION_DATE"),
					ftpServer.get("CREATED_BY"),
					ftpServer.get("UPDATE_DATE"),
					ftpServer.get("UPDATED_BY")
				};

				dbService.update(sql, values);

				//create job-destination record
				sql = "SELECT JOB_ID FROM ART_JOBS WHERE FTP_SERVER_ID=?";
				ResultSetHandler<List<Number>> h3 = new ColumnListHandler<>("JOB_ID");
				List<Number> jobIds = dbService.query(sql, h3, ftpServerId);

				logger.debug("jobIds.isEmpty()={}", jobIds.isEmpty());
				if (!jobIds.isEmpty()) {
					for (Number jobIdNumber : jobIds) {
						int jobIdInt;
						if (jobIdNumber == null) {
							jobIdInt = 0;
						} else {
							jobIdInt = jobIdNumber.intValue();
						}

						sql = "INSERT INTO ART_JOB_DESTINATION_MAP(JOB_ID, DESTINATION_ID) VALUES(?,?)";
						dbService.update(sql, jobIdInt, maxId);
					}
				}

				//update migrated status
				sql = "UPDATE ART_FTP_SERVERS SET MIGRATED=1"
						+ " WHERE FTP_SERVER_ID=?";
				dbService.update(sql, ftpServerId);
			}
		}
	}

	/**
	 * Populates report source column.Column added in 3.1
	 *
	 * @throws java.sql.SQLException
	 */
	private void populateReportSourceColumn() throws SQLException {
		logger.debug("Entering populateReportSourceColumn");

		String sql;

		sql = "SELECT QUERY_ID FROM ART_QUERIES WHERE REPORT_SOURCE IS NULL";
		ResultSetHandler<List<Number>> h2 = new ColumnListHandler<>("QUERY_ID");
		List<Number> reportIds = dbService.query(sql, h2);

		logger.debug("reportIds.isEmpty()={}", reportIds.isEmpty());
		if (!reportIds.isEmpty()) {
			logger.info("Moving report sources");

			for (Number reportIdNumber : reportIds) {
				int reportIdInt;
				if (reportIdNumber == null) {
					reportIdInt = 0;
				} else {
					reportIdInt = reportIdNumber.intValue();
				}

				sql = "SELECT SOURCE_INFO"
						+ " FROM ART_ALL_SOURCES "
						+ " WHERE OBJECT_ID=?"
						+ " ORDER BY LINE_NUMBER";

				ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
				List<Map<String, Object>> sourceLines = dbService.query(sql, h, reportIdInt);

				StringBuilder sb = new StringBuilder(1024);
				for (Map<String, Object> sourceLine : sourceLines) {
					//map list handler uses a case insensitive map, so case of column names doesn't matter
					String line = (String) sourceLine.get("SOURCE_INFO");
					sb.append(line);
				}

				String finalSource = sb.toString();

				sql = "UPDATE ART_QUERIES SET REPORT_SOURCE=? WHERE QUERY_ID=?";
				dbService.update(sql, finalSource, reportIdInt);
			}
		}
	}

	/**
	 * Populates the ART_REPORT_REPORT_GROUPS table. Table added in 3.1
	 *
	 * @throws SQLException
	 */
	private void populateReportReportGroupsTable() throws SQLException {
		String sql;

		sql = "SELECT QUERY_ID, QUERY_GROUP_ID"
				+ " FROM ART_QUERIES"
				+ " WHERE QUERY_GROUP_ID > 0";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> reports = dbService.query(sql, h2);

		logger.debug("reports.isEmpty()={}", reports.isEmpty());
		if (!reports.isEmpty()) {
			logger.info("Adding report - report group records");

			for (Map<String, Object> report : reports) {
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Number reportId = (Number) report.get("QUERY_ID");
				Number reportGroupId = (Number) report.get("QUERY_GROUP_ID");

				sql = "DELETE FROM ART_REPORT_REPORT_GROUPS WHERE REPORT_ID=?";
				dbService.update(sql, reportId);

				sql = "INSERT INTO ART_REPORT_REPORT_GROUPS(REPORT_ID, REPORT_GROUP_ID)"
						+ " VALUES(?,?)";
				dbService.update(sql, reportId, reportGroupId);

				sql = "UPDATE ART_QUERIES SET QUERY_GROUP_ID=0"
						+ " WHERE QUERY_ID=?";
				dbService.update(sql, reportId);
			}
		}
	}

	/**
	 * Populates user_id columns.Columns added in 3.0
	 *
	 * @throws java.sql.SQLException
	 */
	private void addUserIds() throws SQLException {
		logger.debug("Entering addUserIds");

		String sql;

		sql = "SELECT USERNAME FROM ART_USERS WHERE USER_ID IS NULL";
		ResultSetHandler<List<String>> h2 = new ColumnListHandler<>("USERNAME");
		List<String> users = dbService.query(sql, h2);

		logger.debug("users.isEmpty()={}", users.isEmpty());
		if (!users.isEmpty()) {
			logger.info("Adding user ids");

			//generate new id
			sql = "SELECT MAX(USER_ID) FROM ART_USERS";
			int maxId = dbService.getMaxRecordId(sql);

			for (String user : users) {
				maxId++;

				sql = "UPDATE ART_USERS SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_ADMIN_PRIVILEGES SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_USER_QUERIES SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_USER_QUERY_GROUPS SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_USER_RULES SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_JOBS SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_USER_JOBS SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_USER_GROUP_ASSIGNMENT SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_JOB_ARCHIVES SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);

				sql = "UPDATE ART_JOBS_AUDIT SET USER_ID=? WHERE USERNAME=?";
				dbService.update(sql, maxId, user);
			}
		}

	}

	/**
	 * Populates schedule_id column.Column added in 3.0
	 *
	 * @throws java.sql.SQLException
	 */
	private void addScheduleIds() throws SQLException {
		logger.debug("Entering addScheduleIds");

		String sql;

		sql = "SELECT SCHEDULE_NAME FROM ART_JOB_SCHEDULES WHERE SCHEDULE_ID IS NULL";
		ResultSetHandler<List<String>> h2 = new ColumnListHandler<>("SCHEDULE_NAME");
		List<String> schedules = dbService.query(sql, h2);

		logger.debug("schedules.isEmpty()={}", schedules.isEmpty());
		if (!schedules.isEmpty()) {
			logger.info("Adding schedule ids");

			//generate new id
			sql = "SELECT MAX(SCHEDULE_ID) FROM ART_JOB_SCHEDULES";
			int maxId = dbService.getMaxRecordId(sql);

			for (String schedule : schedules) {
				maxId++;
				sql = "UPDATE ART_JOB_SCHEDULES SET SCHEDULE_ID=? WHERE SCHEDULE_NAME=?";
				dbService.update(sql, maxId, schedule);
			}
		}
	}

	/**
	 * Populates drilldown_id column.Column added in 3.0
	 *
	 * @throws java.sql.SQLException
	 */
	private void addDrilldownIds() throws SQLException {
		logger.debug("Entering addDrilldownIds");

		String sql;

		sql = "SELECT QUERY_ID, DRILLDOWN_QUERY_POSITION"
				+ " FROM ART_DRILLDOWN_QUERIES"
				+ " WHERE DRILLDOWN_ID IS NULL";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> drilldowns = dbService.query(sql, h2);

		logger.debug("drilldowns.isEmpty()={}", drilldowns.isEmpty());
		if (!drilldowns.isEmpty()) {
			logger.info("Adding drilldown ids");

			//generate new id
			sql = "SELECT MAX(DRILLDOWN_ID) FROM ART_DRILLDOWN_QUERIES";
			int maxId = dbService.getMaxRecordId(sql);

			for (Map<String, Object> drilldown : drilldowns) {
				maxId++;
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Number parentReportId = (Number) drilldown.get("QUERY_ID");
				Number position = (Number) drilldown.get("DRILLDOWN_QUERY_POSITION");
				sql = "UPDATE ART_DRILLDOWN_QUERIES SET DRILLDOWN_ID=?"
						+ " WHERE QUERY_ID=? AND DRILLDOWN_QUERY_POSITION=?";
				dbService.update(sql, maxId, parentReportId, position);
			}
		}
	}

	/**
	 * Populates rule_id column.Column added in 3.0
	 *
	 * @throws java.sql.SQLException
	 */
	private void addRuleIds() throws SQLException {
		logger.debug("Entering addRuleIds");

		String sql;

		sql = "SELECT RULE_NAME FROM ART_RULES WHERE RULE_ID IS NULL";
		ResultSetHandler<List<String>> h2 = new ColumnListHandler<>(1);
		List<String> rules = dbService.query(sql, h2);

		logger.debug("rules.isEmpty()={}", rules.isEmpty());
		if (!rules.isEmpty()) {
			logger.info("Adding rule ids");

			//generate new id
			sql = "SELECT MAX(RULE_ID) FROM ART_RULES";
			int maxId = dbService.getMaxRecordId(sql);

			for (String rule : rules) {
				maxId++;

				sql = "UPDATE ART_RULES SET RULE_ID=? WHERE RULE_NAME=?";
				dbService.update(sql, maxId, rule);

				sql = "UPDATE ART_QUERY_RULES SET RULE_ID=? WHERE RULE_NAME=?";
				dbService.update(sql, maxId, rule);

				sql = "UPDATE ART_USER_RULES SET RULE_ID=? WHERE RULE_NAME=?";
				dbService.update(sql, maxId, rule);

				sql = "UPDATE ART_USER_GROUP_RULES SET RULE_ID=? WHERE RULE_NAME=?";
				dbService.update(sql, maxId, rule);
			}
		}
	}

	/**
	 * Populates query rule id column.Column added in 3.0
	 *
	 * @throws java.sql.SQLException
	 */
	private void addQueryRuleIds() throws SQLException {
		logger.debug("Entering addQueryRuleIds");

		String sql;

		sql = "SELECT QUERY_ID, RULE_NAME"
				+ " FROM ART_QUERY_RULES"
				+ " WHERE QUERY_RULE_ID IS NULL";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> reportRules = dbService.query(sql, h2);

		logger.debug("reportFilters.isEmpty()={}", reportRules.isEmpty());
		if (!reportRules.isEmpty()) {
			logger.info("Adding query rule ids");

			//generate new id
			sql = "SELECT MAX(QUERY_RULE_ID) FROM ART_QUERY_RULES";
			int maxId = dbService.getMaxRecordId(sql);

			for (Map<String, Object> reportRule : reportRules) {
				maxId++;
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Number reportId = (Number) reportRule.get("QUERY_ID");
				String ruleName = (String) reportRule.get("RULE_NAME");
				sql = "UPDATE ART_QUERY_RULES SET QUERY_RULE_ID=?"
						+ " WHERE QUERY_ID=? AND RULE_NAME=?";
				dbService.update(sql, maxId, reportId, ruleName);
			}
		}
	}

	/**
	 * Populates art_parameters table.Added in 3.0
	 *
	 * @throws java.sql.SQLException
	 */
	private void addParameters() throws SQLException {
		logger.debug("Entering addParameters");

		String sql;

		sql = "SELECT *"
				+ " FROM ART_QUERY_FIELDS"
				+ " WHERE MIGRATED IS NULL";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> parameters = dbService.query(sql, h2);

		logger.debug("parameters.isEmpty()={}", parameters.isEmpty());
		if (!parameters.isEmpty()) {
			logger.info("Adding parameters");

			//generate new parameter id
			sql = "SELECT MAX(PARAMETER_ID) FROM ART_PARAMETERS";
			int maxParameterId = dbService.getMaxRecordId(sql);

			//generate new report parameter id
			sql = "SELECT MAX(REPORT_PARAMETER_ID) FROM ART_REPORT_PARAMETERS";
			int maxReportParameterId = dbService.getMaxRecordId(sql);

			for (Map<String, Object> parameter : parameters) {
				maxParameterId++;

				//create parameter definition
				sql = "INSERT INTO ART_PARAMETERS"
						+ " (PARAMETER_ID, NAME, DESCRIPTION, PARAMETER_TYPE, PARAMETER_LABEL,"
						+ " HELP_TEXT, DATA_TYPE, DEFAULT_VALUE, HIDDEN, SHARED, USE_LOV,"
						+ " LOV_REPORT_ID, USE_RULES_IN_LOV,"
						+ " DRILLDOWN_COLUMN_INDEX, USE_DIRECT_SUBSTITUTION)"
						+ " VALUES(" + StringUtils.repeat("?", ",", 15) + ")";

				ParameterType parameterType;
				String parameterTypeString = (String) parameter.get("PARAM_TYPE");
				if (StringUtils.equals(parameterTypeString, "M")) {
					parameterType = ParameterType.MultiValue;
				} else {
					parameterType = ParameterType.SingleValue;
				}

				String dataTypeString = (String) parameter.get("PARAM_DATA_TYPE");
				ParameterDataType dataType = ParameterDataType.toEnum(dataTypeString);

				String useLovString = (String) parameter.get("USE_LOV");
				boolean useLov = BooleanUtils.toBoolean(useLovString);

				String useRulesInLovString = (String) parameter.get("APPLY_RULES_TO_LOV");
				boolean useRulesInLov = BooleanUtils.toBoolean(useRulesInLovString);

				String useDirectSubstitutionString = (String) parameter.get("DIRECT_SUBSTITUTION");
				boolean useDirectSubstitution = BooleanUtils.toBoolean(useDirectSubstitutionString);

				boolean hidden = false;
				boolean shared = false;
				Object[] values = {
					maxParameterId,
					parameter.get("PARAM_LABEL"), //name. meaning of name and label interchanged
					parameter.get("SHORT_DESCRIPTION"), //description
					parameterType.getValue(),
					parameter.get("NAME"), //label
					parameter.get("DESCRIPTION"), //help text
					dataType.getValue(),
					parameter.get("DEFAULT_VALUE"),
					BooleanUtils.toInteger(hidden),
					BooleanUtils.toInteger(shared),
					BooleanUtils.toInteger(useLov),
					parameter.get("LOV_QUERY_ID"),
					BooleanUtils.toInteger(useRulesInLov),
					parameter.get("DRILLDOWN_COLUMN"),
					BooleanUtils.toInteger(useDirectSubstitution)
				};

				dbService.update(sql, values);

				//create report parameter
				maxReportParameterId++;

				sql = "INSERT INTO ART_REPORT_PARAMETERS"
						+ " (REPORT_PARAMETER_ID, REPORT_ID, PARAMETER_ID, PARAMETER_POSITION)"
						+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

				Number reportId = (Number) parameter.get("QUERY_ID");
				Number position = (Number) parameter.get("FIELD_POSITION");

				dbService.update(sql, maxReportParameterId, reportId, maxParameterId, position);

				//update migrated status
				sql = "UPDATE ART_QUERY_FIELDS SET MIGRATED=1"
						+ " WHERE QUERY_ID=? AND FIELD_POSITION=?";
				dbService.update(sql, reportId, position);
			}
		}
	}

	/**
	 * Populates user rule value key column.Column added in 3.0
	 *
	 * @throws java.sql.SQLException
	 */
	private void addUserRuleValueKeys() throws SQLException {
		logger.debug("Entering addUserRuleValueKeys");

		String sql;

		sql = "SELECT *"
				+ " FROM ART_USER_RULES"
				+ " WHERE RULE_VALUE_KEY IS NULL AND RULE_TYPE='EXACT'";
		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> userRules = dbService.query(sql, h);

		logger.debug("userRules.isEmpty()={}", userRules.isEmpty());
		if (!userRules.isEmpty()) {
			logger.info("Adding user rule value keys");

			for (Map<String, Object> userRule : userRules) {
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				String username = (String) userRule.get("USERNAME");
				String ruleName = (String) userRule.get("RULE_NAME");
				String ruleValue = (String) userRule.get("RULE_VALUE");
				sql = "UPDATE ART_USER_RULES SET RULE_VALUE_KEY=?"
						+ " WHERE USERNAME=? AND RULE_NAME=? AND RULE_VALUE=?"
						+ " AND RULE_TYPE='EXACT'";
				dbService.update(sql, ArtUtils.getUniqueId(), username, ruleName, ruleValue);
			}
		}
	}

	/**
	 * Populates user group rule value key column.Column added in 3.0
	 *
	 * @throws java.sql.SQLException
	 */
	private void addUserGroupRuleValueKeys() throws SQLException {
		logger.debug("Entering addUserGroupRuleValueKeys");

		String sql;

		sql = "SELECT *"
				+ " FROM ART_USER_GROUP_RULES"
				+ " WHERE RULE_VALUE_KEY IS NULL AND RULE_TYPE='EXACT'";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> userGroupRules = dbService.query(sql, h2);

		logger.debug("userGroupRules.isEmpty()={}", userGroupRules.isEmpty());
		if (!userGroupRules.isEmpty()) {
			logger.info("Adding user group rule value keys");

			for (Map<String, Object> userGroupRule : userGroupRules) {
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Number userGroupId = (Number) userGroupRule.get("USER_GROUP_ID");
				String ruleName = (String) userGroupRule.get("RULE_NAME");
				String ruleValue = (String) userGroupRule.get("RULE_VALUE");
				sql = "UPDATE ART_USER_GROUP_RULES SET RULE_VALUE_KEY=?"
						+ " WHERE USER_GROUP_ID=? AND RULE_NAME=? AND RULE_VALUE=?"
						+ " AND RULE_TYPE='EXACT'";
				dbService.update(sql, ArtUtils.getUniqueId(), userGroupId, ruleName, ruleValue);
			}
		}
	}

	/**
	 * Populates cached_datasource_id column.Column added in 3.0
	 *
	 * @throws java.sql.SQLException
	 */
	private void addCachedDatasourceIds() throws SQLException {
		logger.debug("Entering addCachedDatasourceIds");

		String sql;

		sql = "SELECT OUTPUT_FORMAT, JOB_ID"
				+ " FROM ART_JOBS"
				+ " WHERE CACHED_DATASOURCE_ID IS NULL AND JOB_TYPE IN('CacheInsert','CacheAppend')";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> records = dbService.query(sql, h2);

		logger.debug("records.isEmpty()={}", records.isEmpty());
		if (!records.isEmpty()) {
			logger.info("Adding cached datasource ids");

			for (Map<String, Object> record : records) {
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Number jobId = (Number) record.get("JOB_ID");
				String cachedDatasourceIdString = (String) record.get("OUTPUT_FORMAT");
				Integer cachedDatasourceId = Integer.valueOf(cachedDatasourceIdString);
				sql = "UPDATE ART_JOBS SET CACHED_DATASOURCE_ID=?"
						+ " WHERE JOB_ID=?";
				dbService.update(sql, cachedDatasourceId, jobId);
			}
		}
	}

	/**
	 * Updates datasource passwords to use AES-128
	 *
	 * @throws Exception
	 */
	private void updateDatasourcePasswords() throws Exception {
		logger.debug("Entering updateDatasourcePasswords");

		String sql;

		sql = "SELECT PASSWORD, PASSWORD_ALGORITHM, DATABASE_ID"
				+ " FROM ART_DATABASES"
				+ " WHERE PASSWORD_ALGORITHM='ART'";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> records = dbService.query(sql, h2);

		logger.debug("records.isEmpty()={}", records.isEmpty());
		if (!records.isEmpty()) {
			logger.info("Updating datasource passwords");

			for (Map<String, Object> record : records) {
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				//oracle stores INTEGER data type as NUMBER(38,0) and jdbc driver returns a BigDecimal rather than an Integer
				Number datasourceId = (Number) record.get("DATABASE_ID");
				String oldPassword = (String) record.get("PASSWORD");

				if (oldPassword == null) {
					oldPassword = "";
				} else {
					if (oldPassword.startsWith("o:")) {
						//password is encrypted. decrypt
						oldPassword = DesEncryptor.decrypt(oldPassword.substring(2));
					}
				}

				String aesPassword = AesEncryptor.encrypt(oldPassword);

				sql = "UPDATE ART_DATABASES SET PASSWORD=?, PASSWORD_ALGORITHM='AES'"
						+ " WHERE DATABASE_ID=?";
				dbService.update(sql, aesPassword, datasourceId);
			}
		}
	}

	/**
	 * Deletes .jasper files in the given directory
	 *
	 * @param directoryPath the directory path
	 */
	private void deleteDotJasperFiles(String directoryPath) {
		File folder = new File(directoryPath);
		File fList[] = folder.listFiles();

		for (File f : fList) {
			if (f.getName().endsWith(".jasper")) {
				f.delete();
			}
		}
	}

	/**
	 * Upgrades the database to 3.8
	 *
	 * @throws java.sql.SQLException
	 */
	private void upgradeDatabaseTo38() throws SQLException {
		logger.debug("Entering upgradeDatabaseTo38");

		String databaseVersionString = "3.8";
		String sql = "SELECT UPGRADED FROM ART_CUSTOM_UPGRADES WHERE DATABASE_VERSION=?";
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number upgradedValue = dbService.query(sql, h, databaseVersionString);
		if (upgradedValue == null || upgradedValue.intValue() == 1) {
			return;
		}

		logger.info("Performing 3.8 upgrade steps");

		populateUserRolesTable();

		sql = "UPDATE ART_CUSTOM_UPGRADES SET UPGRADED=1 WHERE DATABASE_VERSION=?";
		dbService.update(sql, databaseVersionString);

		logger.info("Done performing 3.8 upgrade steps");
	}

	/**
	 * Populates the user roles table. Table added in 3.8
	 *
	 * @throws SQLException
	 */
	private void populateUserRolesTable() throws SQLException {
		logger.debug("Entering populateUserRolesTable");

		logger.info("Adding user - role records");

		String sql;

		//https://stackoverflow.com/questions/25969/insert-into-values-select-from
		sql = "INSERT INTO ART_USER_ROLE_MAP (USER_ID, ROLE_ID) SELECT USER_ID, ACCESS_LEVEL FROM ART_USERS";
		dbService.update(sql);

		sql = "UPDATE ART_USER_ROLE_MAP SET ROLE_ID=1 WHERE ROLE_ID=0";
		dbService.update(sql);

		sql = "UPDATE ART_USER_ROLE_MAP SET ROLE_ID=2 WHERE ROLE_ID=5";
		dbService.update(sql);

		sql = "UPDATE ART_USER_ROLE_MAP SET ROLE_ID=3 WHERE ROLE_ID=10";
		dbService.update(sql);

		sql = "UPDATE ART_USER_ROLE_MAP SET ROLE_ID=4 WHERE ROLE_ID=30";
		dbService.update(sql);

		sql = "UPDATE ART_USER_ROLE_MAP SET ROLE_ID=5 WHERE ROLE_ID=40";
		dbService.update(sql);

		sql = "UPDATE ART_USER_ROLE_MAP SET ROLE_ID=6 WHERE ROLE_ID=80";
		dbService.update(sql);

		sql = "UPDATE ART_USER_ROLE_MAP SET ROLE_ID=7 WHERE ROLE_ID=100";
		dbService.update(sql);
	}

	/**
	 * Upgrades the database to 4.1
	 *
	 * @throws java.sql.SQLException
	 */
	private void upgradeDatabaseTo41() throws SQLException {
		logger.debug("Entering upgradeDatabaseTo41");

		String databaseVersionString = "4.1";
		String sql = "SELECT UPGRADED FROM ART_CUSTOM_UPGRADES WHERE DATABASE_VERSION=?";
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number upgradedValue = dbService.query(sql, h, databaseVersionString);
		if (upgradedValue == null || upgradedValue.intValue() == 1) {
			return;
		}

		logger.info("Performing {} upgrade steps", databaseVersionString);

		removeUsernames41();

		sql = "UPDATE ART_CUSTOM_UPGRADES SET UPGRADED=1 WHERE DATABASE_VERSION=?";
		dbService.update(sql, databaseVersionString);

		logger.info("Done performing {} upgrade steps", databaseVersionString);
	}

	/**
	 * Removes some username fields used in foreign tables
	 *
	 * @throws SQLException
	 */
	private void removeUsernames41() throws SQLException {
		logger.debug("Entering removeUsernames41");

		recreateUserGroupAssignment();
		recreateUserQueries();
		recreateUserQueryGroups();
		recreateUserJobs();
	}

	/**
	 * Recreates the user - user group assignment table. To remove the username
	 * field. Some databases require to know the constraint name in order to
	 * drop a primary key so doing it from an upgrade script may be problematic.
	 *
	 * @throws SQLException
	 */
	private void recreateUserGroupAssignment() throws SQLException {
		logger.debug("Entering recreateUserGroupAssignment");

		String sql;

		sql = "SELECT USER_ID, USER_GROUP_ID"
				+ " FROM ART_USER_GROUP_ASSIGNMENT";
		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> memberships = dbService.query(sql, h);

		logger.debug("memberships.isEmpty()={}", memberships.isEmpty());
		if (!memberships.isEmpty()) {
			logger.info("Updating user - user group assignment");

			sql = "DELETE FROM ART_USER_USERGROUP_MAP";
			dbService.update(sql);

			sql = "INSERT INTO ART_USER_USERGROUP_MAP (USER_ID, USER_GROUP_ID)"
					+ " VALUES(?,?)";

			for (Map<String, Object> membership : memberships) {
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Number userId = (Number) membership.get("USER_ID");
				Number userGroupId = (Number) membership.get("USER_GROUP_ID");
				dbService.update(sql, userId, userGroupId);
			}
		}
	}

	/**
	 * Recreates the user queries table. To remove the username field.
	 *
	 * @throws SQLException
	 */
	private void recreateUserQueries() throws SQLException {
		logger.debug("Entering recreateUserQueries");

		String sql;

		sql = "SELECT USER_ID, QUERY_ID"
				+ " FROM ART_USER_QUERIES";
		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> memberships = dbService.query(sql, h);

		logger.debug("memberships.isEmpty()={}", memberships.isEmpty());
		if (!memberships.isEmpty()) {
			logger.info("Updating user - report assignment");

			sql = "DELETE FROM ART_USER_REPORT_MAP";
			dbService.update(sql);

			sql = "INSERT INTO ART_USER_REPORT_MAP (USER_ID, REPORT_ID)"
					+ " VALUES(?,?)";

			for (Map<String, Object> membership : memberships) {
				Number userId = (Number) membership.get("USER_ID");
				Number reportId = (Number) membership.get("QUERY_ID");
				dbService.update(sql, userId, reportId);
			}
		}
	}

	/**
	 * Recreates the user query groups table. To remove the username field.
	 *
	 * @throws SQLException
	 */
	private void recreateUserQueryGroups() throws SQLException {
		logger.debug("Entering recreateUserQueryGroups");

		String sql;

		sql = "SELECT USER_ID, QUERY_GROUP_ID"
				+ " FROM ART_USER_QUERY_GROUPS";
		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> memberships = dbService.query(sql, h);

		logger.debug("memberships.isEmpty()={}", memberships.isEmpty());
		if (!memberships.isEmpty()) {
			logger.info("Updating user - report group assignment");

			sql = "DELETE FROM ART_USER_REPORTGROUP_MAP";
			dbService.update(sql);

			sql = "INSERT INTO ART_USER_REPORTGROUP_MAP (USER_ID, REPORT_GROUP_ID)"
					+ " VALUES(?,?)";

			for (Map<String, Object> membership : memberships) {
				Number userId = (Number) membership.get("USER_ID");
				Number reportGroupId = (Number) membership.get("QUERY_GROUP_ID");
				dbService.update(sql, userId, reportGroupId);
			}
		}
	}

	/**
	 * Recreates the user jobs table. To remove the username field.
	 *
	 * @throws SQLException
	 */
	private void recreateUserJobs() throws SQLException {
		logger.debug("Entering recreateUserJobs");

		String sql;

		sql = "SELECT COUNT(*) FROM ART_USER_JOBS";
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number recordCount = dbService.query(sql, h);
		logger.debug("recordCount={}", recordCount);
		if (recordCount == null || recordCount.intValue() == 0) {
			return;
		}

		logger.info("Updating user - job assignment");

		sql = "DELETE FROM ART_USER_JOB_MAP";
		dbService.update(sql);

		String columns = "USER_ID, JOB_ID, USER_GROUP_ID, LAST_FILE_NAME,"
				+ " LAST_RUN_MESSAGE, LAST_RUN_DETAILS, LAST_START_DATE,"
				+ " LAST_END_DATE";

		sql = "INSERT INTO ART_USER_JOB_MAP (" + columns + ")"
				+ " SELECT " + columns + " FROM ART_USER_JOBS";

		dbService.update(sql);
	}

}
