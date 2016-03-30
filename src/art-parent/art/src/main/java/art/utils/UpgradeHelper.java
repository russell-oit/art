/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.connectionpool.DbConnections;
import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.job.Job;
import art.job.JobService;
import art.jobrunners.ReportJob;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import static org.quartz.JobKey.jobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
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

	public void upgrade(String artVersion, String upgradeFilePath) {
		migrateJobsToQuartz();
		upgradeDatabase(artVersion, upgradeFilePath);
	}

	/**
	 * Migrate existing jobs created in art versions before 1.11 to quartz jobs
	 *
	 */
	private void migrateJobsToQuartz() {

		Scheduler scheduler = SchedulerUtils.getScheduler();

		if (scheduler == null) {
			logger.info("Can't migrate jobs to Quartz. Scheduler not available");
			return;
		}

		JobService jobService = new JobService();

		Connection conn = null;
		PreparedStatement psUpdate = null;

		try {
			conn = DbConnections.getArtDbConnection();

			//prepare statement for updating migration status			
			String updateJobSqlString = "UPDATE ART_JOBS SET MIGRATED_TO_QUARTZ='Y',"
					+ " NEXT_RUN_DATE=?, JOB_MINUTE=?, JOB_HOUR=?, "
					+ " JOB_DAY=?, JOB_WEEKDAY=?, JOB_MONTH=? "
					+ " WHERE JOB_ID=?";
			psUpdate = conn.prepareStatement(updateJobSqlString);

			int totalRecordCount = 0; //total number of jobs to be migrated
			int migratedRecordCount = 0; //actual number of jobs migrated
			final int batchSize = 100; //max number of updates to batch together for executebatch

			List<Job> nonQuartzJobs = jobService.getNonQuartzJobs();
			for (Job job : nonQuartzJobs) {
				totalRecordCount += 1;

				//create quartz job
				String minute = job.getScheduleMinute();
				if (minute == null) {
					minute = "0"; //default to 0
				}
				String hour = job.getScheduleHour();
				if (hour == null) {
					hour = "3"; //default to 3am
				}
				String month = job.getScheduleMonth();
				if (month == null) {
					month = "*"; //default to every month
				}
				//set day and weekday
				String day = job.getScheduleDay();
				if (day == null) {
					day = "*";
				}
				String weekday = job.getScheduleWeekday();
				if (weekday == null) {
					weekday = "?";
				}

				//set default day of the month if weekday is defined
				if (day.length() == 0 && weekday.length() >= 1 && !weekday.equals("?")) {
					//weekday defined but day of the month is not. default day to ?
					day = "?";
				}

				if (day.length() == 0) {
					//no day of month defined. default to *
					day = "*";
				}
				if (weekday.length() == 0) {
					//no day of week defined. default to undefined
					weekday = "?";
				}
				if (day.equals("?") && weekday.equals("?")) {
					//unsupported. only one can be ?
					day = "*";
					weekday = "?";
				}
				if (day.equals("*") && weekday.equals("*")) {
					//unsupported. only one can be defined
					day = "*";
					weekday = "?";
				}

				String second = "0"; //seconds always 0
				String cronString = second + " " + minute + " " + hour + " " + day + " " + month + " " + weekday;
				if (CronExpression.isValidExpression(cronString)) {
					//ensure that trigger will fire at least once in the future
					CronTrigger tempTrigger = newTrigger().withSchedule(cronSchedule(cronString)).build();

					Date nextRunDate = tempTrigger.getFireTimeAfter(new Date());
					if (nextRunDate != null) {
						//create job
						migratedRecordCount += 1;
						if (migratedRecordCount == 1) {
							logger.info("Migrating jobs to quartz...");
						}

						int jobId = job.getJobId();
						String jobName = "job" + jobId;
						String triggerName = "trigger" + jobId;

						JobDetail quartzJob = newJob(ReportJob.class
						).withIdentity(jobName, ArtUtils.JOB_GROUP).usingJobData("jobId", jobId).build();

						//create trigger that defines the schedule for the job						
						CronTrigger trigger = newTrigger().withIdentity(triggerName, ArtUtils.TRIGGER_GROUP).withSchedule(cronSchedule(cronString)).build();

						//delete any existing jobs or triggers with the same id before adding them to the scheduler
						scheduler.deleteJob(jobKey(jobName, ArtUtils.JOB_GROUP)); //delete job records
						scheduler.unscheduleJob(triggerKey(triggerName, ArtUtils.TRIGGER_GROUP)); //delete any trigger records

						//add job and trigger to scheduler
						scheduler.scheduleJob(quartzJob, trigger);

						//update jobs table to indicate that the job has been migrated						
						psUpdate.setTimestamp(1, new java.sql.Timestamp(nextRunDate.getTime()));
						psUpdate.setString(2, minute);
						psUpdate.setString(3, hour);
						psUpdate.setString(4, day);
						psUpdate.setString(5, weekday);
						psUpdate.setString(6, month);
						psUpdate.setInt(7, jobId);

						psUpdate.addBatch();
						//run executebatch periodically to prevent out of memory errors
						if (migratedRecordCount % batchSize
								== 0) {
							psUpdate.executeBatch();
							psUpdate.clearBatch(); //not sure if this is necessary
						}
					}
				}
			}

			if (migratedRecordCount > 0) {
				psUpdate.executeBatch(); //run any remaining updates																
			}

			if (migratedRecordCount > 0) {
				//output the number of jobs migrated
				logger.info("Finished migrating jobs to quartz. Migrated {} out of {} jobs", migratedRecordCount, totalRecordCount);
			}
		} catch (SQLException | SchedulerException ex) {
			logger.error("Error", ex);
		} finally {
			DatabaseUtils.close(psUpdate, conn);
		}
	}

	/**
	 * run upgrade steps
	 */
	private void upgradeDatabase(String artVersion, String upgradeFilePath) {
		File upgradeFile = new File(upgradeFilePath);
		if (upgradeFile.exists()) {
			try {
				//don't consider alpha, beta, rc etc
				//also, pre-releases, i.e. alpha, beta, etc are considered less than final releases
				String version = StringUtils.substringBefore(artVersion, "-");
				logger.debug("version='{}'", version);

				//changes introduced in 3.0
				if (StringUtils.equals(version, "3.0")) {
					logger.info("Performing 3.0 upgrade steps");

					addUserIds();
					addScheduleIds();
					addDrilldownIds();
					addRuleIds();
					addQueryRuleIds();
					addParameters();
					addUserRuleValueKeys();
					addUserGroupRuleValueKeys();

					logger.info("Done performing 3.0 upgrade steps");
				}

				boolean deleted = upgradeFile.delete();
				if (!deleted) {
					logger.warn("Upgrade file not deleted: {}", upgradeFile);
				}
			} catch (SQLException ex) {
				logger.error("Error", ex);
			}
		}
	}

	/**
	 * Populate user_id columns. Columns added in 3.0
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
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxId = dbService.query(sql, h);
			logger.debug("maxId={}", maxId);

			if (maxId == null || maxId < 0) {
				maxId = 0;
			}

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
			}
		}

	}

	/**
	 * Populate schedule_id column. Column added in 3.0
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
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxId = dbService.query(sql, h);
			logger.debug("maxId={}", maxId);

			if (maxId == null || maxId < 0) {
				maxId = 0;
			}

			for (String schedule : schedules) {
				maxId++;
				sql = "UPDATE ART_JOB_SCHEDULES SET SCHEDULE_ID=? WHERE SCHEDULE_NAME=?";
				dbService.update(sql, maxId, schedule);
			}
		}
	}

	/**
	 * Populate drilldown_id column. Column added in 3.0
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
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxId = dbService.query(sql, h);
			logger.debug("maxId={}", maxId);

			if (maxId == null || maxId < 0) {
				maxId = 0;
			}

			for (Map<String, Object> drilldown : drilldowns) {
				maxId++;
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Integer parentReportId = (Integer) drilldown.get("QUERY_ID");
				Integer position = (Integer) drilldown.get("DRILLDOWN_QUERY_POSITION");
				sql = "UPDATE ART_DRILLDOWN_QUERIES SET DRILLDOWN_ID=?"
						+ " WHERE QUERY_ID=? AND DRILLDOWN_QUERY_POSITION=?";
				dbService.update(sql, maxId, parentReportId, position);
			}
		}
	}

	/**
	 * Populate rule_id column. Column added in 3.0
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
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxId = dbService.query(sql, h);
			logger.debug("maxId={}", maxId);

			if (maxId == null || maxId < 0) {
				maxId = 0;
			}

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
	 * Populate query rule id column. Column added in 3.0
	 */
	private void addQueryRuleIds() throws SQLException {
		logger.debug("Entering addQueryRuleIds");

		String sql;

		sql = "SELECT QUERY_ID, RULE_NAME"
				+ " FROM ART_QUERY_RULES"
				+ " WHERE QUERY_RULE_ID IS NULL";
		ResultSetHandler<List<Map<String, Object>>> h2 = new MapListHandler();
		List<Map<String, Object>> reportFilters = dbService.query(sql, h2);

		logger.debug("reportFilters.isEmpty()={}", reportFilters.isEmpty());
		if (!reportFilters.isEmpty()) {
			logger.info("Adding query rule ids");

			//generate new id
			sql = "SELECT MAX(QUERY_RULE_ID) FROM ART_QUERY_RULES";
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxId = dbService.query(sql, h);
			logger.debug("maxId={}", maxId);

			if (maxId == null || maxId < 0) {
				maxId = 0;
			}

			for (Map<String, Object> reportFilter : reportFilters) {
				maxId++;
				//map list handler uses a case insensitive map, so case of column names doesn't matter
				Integer reportId = (Integer) reportFilter.get("QUERY_ID");
				String ruleName = (String) reportFilter.get("RULE_NAME");
				sql = "UPDATE ART_QUERY_RULES SET QUERY_RULE_ID=?"
						+ " WHERE QUERY_ID=? AND RULE_NAME=?";
				dbService.update(sql, maxId, reportId, ruleName);
			}
		}
	}

	/**
	 * Populate art_parameters table. Added in 3.0
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
			ResultSetHandler<Integer> h = new ScalarHandler<>();
			Integer maxParameterId = dbService.query(sql, h);
			logger.debug("maxParameterId={}", maxParameterId);

			if (maxParameterId == null || maxParameterId < 0) {
				maxParameterId = 0;
			}

			//generate new report parameter id
			sql = "SELECT MAX(REPORT_PARAMETER_ID) FROM ART_REPORT_PARAMETERS";
			ResultSetHandler<Integer> h3 = new ScalarHandler<>();
			Integer maxReportParameterId = dbService.query(sql, h3);
			logger.debug("maxReportParameterId={}", maxReportParameterId);

			if (maxReportParameterId == null || maxReportParameterId < 0) {
				maxReportParameterId = 0;
			}

			for (Map<String, Object> parameter : parameters) {
				maxParameterId++;

				//create parameter definition
				sql = "INSERT INTO ART_PARAMETERS"
						+ " (PARAMETER_ID, NAME, DESCRIPTION, PARAMETER_TYPE, PARAMETER_LABEL,"
						+ " HELP_TEXT, DATA_TYPE, DEFAULT_VALUE, HIDDEN, USE_LOV,"
						+ " LOV_REPORT_ID, USE_FILTERS_IN_LOV, CHAINED_POSITION,"
						+ " CHAINED_VALUE_POSITION, DRILLDOWN_COLUMN_INDEX,"
						+ " USE_DIRECT_SUBSTITUTION)"
						+ " VALUES(" + StringUtils.repeat("?", ",", 16) + ")";

				ParameterType parameterType;
				String paramType = (String) parameter.get("PARAM_TYPE");
				if (StringUtils.equals(paramType, "M")) {
					parameterType = ParameterType.MultiValue;
				} else {
					parameterType = ParameterType.SingleValue;
				}

				ParameterDataType dataType;
				String dtType = (String) parameter.get("PARAM_DATA_TYPE");
				dataType = ParameterDataType.toEnum(dtType);

				String useLov = (String) parameter.get("USE_LOV");
				String useFiltersInLov = (String) parameter.get("APPLY_RULES_TO_LOV");
				String useDirectSubstitution = (String) parameter.get("DIRECT_SUBSTITUTION");

				Object[] values = {
					maxParameterId,
					(String) parameter.get("PARAM_LABEL"), //name. meaning of name and label interchanged
					(String) parameter.get("SHORT_DESCRIPTION"), //description
					parameterType.getValue(),
					(String) parameter.get("NAME"), //label
					(String) parameter.get("DESCRIPTION"), //help text
					dataType.getValue(),
					(String) parameter.get("DEFAULT_VALUE"),
					false,
					BooleanUtils.toBoolean(useLov),
					(Integer) parameter.get("LOV_QUERY_ID"),
					BooleanUtils.toBoolean(useFiltersInLov),
					(Integer) parameter.get("CHAINED_PARAM_POSITION"),
					(Integer) parameter.get("CHAINED_VALUE_POSITION"),
					(Integer) parameter.get("DRILLDOWN_COLUMN"),
					BooleanUtils.toBoolean(useDirectSubstitution)
				};

				dbService.update(sql, values);

				//create report parameter
				maxReportParameterId++;

				sql = "INSERT INTO ART_REPORT_PARAMETERS"
						+ " (REPORT_PARAMETER_ID, REPORT_ID, PARAMETER_ID, PARAMETER_POSITION)"
						+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

				Integer reportId = (Integer) parameter.get("QUERY_ID");
				Integer position = (Integer) parameter.get("FIELD_POSITION");

				dbService.update(sql, maxReportParameterId, reportId, maxParameterId, position);

				//update migrated status
				sql = "UPDATE ART_QUERY_FIELDS SET MIGRATED=1"
						+ " WHERE QUERY_ID=? AND FIELD_POSITION=?";
				dbService.update(sql, reportId, position);
			}
		}
	}

	/**
	 * Populate user rule value key column. Column added in 3.0
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
	 * Populate user group rule value key column. Column added in 3.0
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
				Integer userGroupId = (Integer) userGroupRule.get("USER_GROUP_ID");
				String ruleName = (String) userGroupRule.get("RULE_NAME");
				String ruleValue = (String) userGroupRule.get("RULE_VALUE");
				sql = "UPDATE ART_USER_GROUP_RULES SET RULE_VALUE_KEY=?"
						+ " WHERE USER_GROUP_ID=? AND RULE_NAME=? AND RULE_VALUE=?"
						+ " AND RULE_TYPE='EXACT'";
				dbService.update(sql, ArtUtils.getUniqueId(), userGroupId, ruleName, ruleValue);
			}
		}
	}
}
