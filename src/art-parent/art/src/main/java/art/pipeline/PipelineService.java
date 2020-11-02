/*
 * ART. A Reporting Tool.
 * Copyright (C) 2020 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.pipeline;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.jobrunners.PipelineJob;
import art.pipelinerunningjob.PipelineRunningJobService;
import art.schedule.Schedule;
import art.schedule.ScheduleService;
import art.user.User;
import art.utils.ArtUtils;
import art.utils.QuartzScheduleHelper;
import art.utils.SchedulerUtils;
import art.utils.TriggersResult;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting pipelines
 *
 * @author Timothy Anyona
 */
@Service
public class PipelineService {

	private static final Logger logger = LoggerFactory.getLogger(PipelineService.class);

	private final DbService dbService;
	private final PipelineRunningJobService pipelineRunningJobService;
	private final ScheduleService scheduleService;

	@Autowired
	public PipelineService(DbService dbService,
			PipelineRunningJobService pipelineRunningJobService,
			ScheduleService scheduleService) {

		this.dbService = dbService;
		this.pipelineRunningJobService = pipelineRunningJobService;
		this.scheduleService = scheduleService;
	}

	public PipelineService() {
		dbService = new DbService();
		pipelineRunningJobService = new PipelineRunningJobService();
		scheduleService = new ScheduleService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_PIPELINES AP";

	/**
	 * Maps a resultset to an object
	 */
	private class PipelineMapper extends BasicRowProcessor {

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
			Pipeline pipeline = new Pipeline();

			pipeline.setPipelineId(rs.getInt("PIPELINE_ID"));
			pipeline.setName(rs.getString("NAME"));
			pipeline.setDescription(rs.getString("DESCRIPTION"));
			pipeline.setSerial(rs.getString("SERIAL"));
			pipeline.setContinueOnError(rs.getBoolean("CONTINUE_ON_ERROR"));
			pipeline.setQuartzCalendarNames(rs.getString("QUARTZ_CALENDAR_NAMES"));
			pipeline.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			pipeline.setCreatedBy(rs.getString("CREATED_BY"));
			pipeline.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			pipeline.setUpdatedBy(rs.getString("UPDATED_BY"));

			List<Integer> runningJobs = pipelineRunningJobService.getPipelineRunningJobs(pipeline.getPipelineId());
			pipeline.setRunningJobs(runningJobs);

			Schedule schedule = scheduleService.getSchedule(rs.getInt("SCHEDULE_ID"));
			pipeline.setSchedule(schedule);

			return type.cast(pipeline);
		}
	}

	/**
	 * Returns all pipelines
	 *
	 * @return all pipelines
	 * @throws SQLException
	 */
	@Cacheable("pipelines")
	public List<Pipeline> getAllPipelines() throws SQLException {
		logger.debug("Entering getAllPipelines");

		ResultSetHandler<List<Pipeline>> h = new BeanListHandler<>(Pipeline.class, new PipelineMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns pipelines with given ids
	 *
	 * @param ids comma separated string of the pipeline ids to retrieve
	 * @return pipelines with given ids
	 * @throws SQLException
	 */
	public List<Pipeline> getPipelines(String ids) throws SQLException {
		logger.debug("Entering getPipelines: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		if (idsArray.length == 0) {
			return new ArrayList<>();
		}

		String sql = SQL_SELECT_ALL
				+ " WHERE PIPELINE_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<Pipeline>> h = new BeanListHandler<>(Pipeline.class, new PipelineMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns a pipeline with the given id
	 *
	 * @param id the pipeline id
	 * @return pipeline if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("pipelines")
	public Pipeline getPipeline(int id) throws SQLException {
		logger.debug("Entering getPipeline: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE PIPELINE_ID=?";
		ResultSetHandler<Pipeline> h = new BeanHandler<>(Pipeline.class, new PipelineMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns a pipeline with the given name
	 *
	 * @param name the pipeline name
	 * @return pipeline if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("pipelines")
	public Pipeline getPipeline(String name) throws SQLException {
		logger.debug("Entering getPipeline: name='{}'", name);

		String sql = SQL_SELECT_ALL + " WHERE NAME=?";
		ResultSetHandler<Pipeline> h = new BeanHandler<>(Pipeline.class, new PipelineMapper());
		return dbService.query(sql, h, name);
	}

	/**
	 * Deletes a pipeline
	 *
	 * @param id the pipeline id
	 * @throws SQLException
	 * @throws org.quartz.SchedulerException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void deletePipeline(int id) throws SQLException, SchedulerException {
		logger.debug("Entering deletePipeline: id={}", id);

		Pipeline pipeline = getPipeline(id);

		deleteQuartzJob(pipeline);

		String sql = "DELETE FROM ART_PIPELINES WHERE PIPELINE_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes multiple pipelines
	 *
	 * @param ids the ids of the pipelines to delete
	 * @throws SQLException
	 * @throws org.quartz.SchedulerException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void deletePipelines(Integer[] ids) throws SQLException, SchedulerException {
		logger.debug("Entering deletePipelines: ids={}", (Object) ids);

		for (Integer id : ids) {
			deletePipeline(id);
		}
	}

	/**
	 * Adds a new pipeline
	 *
	 * @param pipeline the pipeline to add
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public synchronized int addPipeline(Pipeline pipeline, User actionUser) throws SQLException {
		logger.debug("Entering addPipeline: pipeline={}, actionUser={}", pipeline, actionUser);

		//generate new id
		String sql = "SELECT MAX(PIPELINE_ID) FROM ART_PIPELINES";
		int newId = dbService.getNewRecordId(sql);

		savePipeline(pipeline, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing pipeline
	 *
	 * @param pipeline the updated pipeline
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void updatePipeline(Pipeline pipeline, User actionUser) throws SQLException {
		Connection conn = null;
		updatePipeline(pipeline, actionUser, conn);
	}

	/**
	 * Updates an existing pipeline
	 *
	 * @param pipeline the updated pipeline
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void updatePipeline(Pipeline pipeline, User actionUser, Connection conn) throws SQLException {
		logger.debug("Entering updatePipeline: pipeline={}, actionUser={}", pipeline, actionUser);

		Integer newRecordId = null;
		savePipeline(pipeline, newRecordId, actionUser, conn);
	}

	/**
	 * Imports pipeline records
	 *
	 * @param pipelines the list of pipelines to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param overwrite whether to overwrite existing records
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void importPipelines(List<Pipeline> pipelines, User actionUser,
			Connection conn, boolean overwrite) throws SQLException {

		logger.debug("Entering importPipelines: actionUser={}, overwrite={}",
				actionUser, overwrite);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(PIPELINE_ID) FROM ART_PIPELINES";
			int id = dbService.getMaxRecordId(conn, sql);

			List<Pipeline> currentPipelines = new ArrayList<>();
			if (overwrite) {
				currentPipelines = getAllPipelines();
			}

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			for (Pipeline pipeline : pipelines) {
				String pipelineName = pipeline.getName();
				boolean update = false;
				if (overwrite) {
					Pipeline existingPipeline = currentPipelines.stream()
							.filter(d -> StringUtils.equals(pipelineName, d.getName()))
							.findFirst()
							.orElse(null);
					if (existingPipeline != null) {
						update = true;
						pipeline.setPipelineId(existingPipeline.getPipelineId());
					}
				}

				Integer newRecordId;
				if (update) {
					newRecordId = null;
				} else {
					id++;
					newRecordId = id;
				}
				savePipeline(pipeline, newRecordId, actionUser, conn);
			}
			conn.commit();
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	/**
	 * Saves a pipeline
	 *
	 * @param pipeline the pipeline to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void savePipeline(Pipeline pipeline, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		savePipeline(pipeline, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a pipeline
	 *
	 * @param pipeline the pipeline to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void savePipeline(Pipeline pipeline, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering savePipeline: pipeline={}, newRecordId={},actionUser={}",
				pipeline, newRecordId, actionUser);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		Integer scheduleId = null;
		if (pipeline.getSchedule() != null) {
			scheduleId = pipeline.getSchedule().getScheduleId();
			if (scheduleId == 0) {
				scheduleId = null;
			}
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_PIPELINES"
					+ " (PIPELINE_ID, NAME, DESCRIPTION, SERIAL,"
					+ " CONTINUE_ON_ERROR, SCHEDULE_ID, QUARTZ_CALENDAR_NAMES,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 9) + ")";

			Object[] values = {
				newRecordId,
				pipeline.getName(),
				pipeline.getDescription(),
				pipeline.getSerial(),
				BooleanUtils.toInteger(pipeline.isContinueOnError()),
				scheduleId,
				pipeline.getQuartzCalendarNames(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(conn, sql, values);
		} else {
			String sql = "UPDATE ART_PIPELINES SET NAME=?, DESCRIPTION=?,"
					+ "	SERIAL=?, CONTINUE_ON_ERROR=?, SCHEDULE_ID=?,"
					+ " QUARTZ_CALENDAR_NAMES=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE PIPELINE_ID=?";

			Object[] values = {
				pipeline.getName(),
				pipeline.getDescription(),
				pipeline.getSerial(),
				BooleanUtils.toInteger(pipeline.isContinueOnError()),
				scheduleId,
				pipeline.getQuartzCalendarNames(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				pipeline.getPipelineId()
			};

			affectedRows = dbService.update(conn, sql, values);
		}

		if (newRecordId != null) {
			pipeline.setPipelineId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, pipeline={}",
					affectedRows, newRecord, pipeline);
		}
	}

	/**
	 * Marks a pipeline as cancelled
	 *
	 * @param pipelineId the pipeline id
	 * @throws SQLException
	 */
	public void cancelPipeline(int pipelineId) throws SQLException {
		logger.debug("Entering cancelPipeline");

		String sql = "UPDATE ART_PIPELINES SET CANCELLED=1 WHERE PIPELINE_ID=?";
		dbService.update(sql, pipelineId);
	}

	/**
	 * Marks a pipeline as uncancelled
	 *
	 * @param pipelineId the pipeline id
	 * @throws SQLException
	 */
	public void uncancelPipeline(int pipelineId) throws SQLException {
		logger.debug("Entering uncancelPipeline");

		String sql = "UPDATE ART_PIPELINES SET CANCELLED=0 WHERE PIPELINE_ID=?";
		dbService.update(sql, pipelineId);
	}

	/**
	 * Returns <code>true</code> if the pipeline has been cancelled
	 *
	 * @param pipelineId the pipeline id
	 * @return <code>true</code> if the pipeline has been cancelled
	 * @throws SQLException
	 */
	public boolean isPipelineCancelled(int pipelineId) throws SQLException {
		logger.debug("Entering isPipelineCancelled");

		String sql = "SELECT CANCELLED"
				+ " FROM ART_PIPELINES"
				+ " WHERE PIPELINE_ID=?";

		ResultSetHandler<Number> h = new ScalarHandler<>("CANCELLED");
		Number value = dbService.query(sql, h, pipelineId);
		if (value == null) {
			return false;
		} else {
			return BooleanUtils.toBoolean(value.intValue());
		}
	}

	/**
	 * Deletes the quartz job associated with the given pipeline, also deleting
	 * any associated triggers
	 *
	 * @param pipeline the pipeline object
	 * @param scheduler the quartz scheduler
	 * @throws org.quartz.SchedulerException
	 */
	private void deleteQuartzJob(Pipeline pipeline) throws SchedulerException {
		int pipelineId = pipeline.getPipelineId();
		String jobName = "pipeline" + pipelineId;
		String quartzCalendarNames = pipeline.getQuartzCalendarNames();

		SchedulerUtils.deleteQuartzJob(jobName, quartzCalendarNames);
	}

	/**
	 * Processes schedule and creates quartz schedules for the job
	 *
	 * @param pipeline the pipeline object
	 * @param actionUser the user who initiated the action
	 * @throws java.text.ParseException
	 * @throws org.quartz.SchedulerException
	 * @throws java.sql.SQLException
	 */
	public void processSchedules(Pipeline pipeline, User actionUser)
			throws ParseException, SchedulerException, SQLException {

		Scheduler scheduler = SchedulerUtils.getScheduler();
		if (scheduler == null) {
			logger.warn("Scheduler not available");
			return;
		}

		//delete job while it has old calendar names, before updating the calendar names field
		deleteQuartzJob(pipeline);

		Schedule schedule = pipeline.getSchedule();
		if (schedule == null) {
			return;
		}

		//job must have been saved in order to use job id for job, trigger and calendar names
		int pipelineId = pipeline.getPipelineId();

		String timeZoneId = schedule.getTimeZone();

		TimeZone timeZone;
		if (StringUtils.isBlank(timeZoneId)) {
			timeZone = TimeZone.getDefault();
		} else {
			timeZone = TimeZone.getTimeZone(timeZoneId);
		}

		logger.debug("timeZoneId='{}'", timeZoneId);
		logger.debug("timeZone={}", timeZone);

		QuartzScheduleHelper quartzScheduleHelper = new QuartzScheduleHelper();

		TriggersResult triggersResult = quartzScheduleHelper.processTriggers(pipeline, timeZone, scheduler);
		Set<Trigger> triggers = triggersResult.getTriggers();
		List<String> calendarNames = triggersResult.getCalendarNames();

		String quartzCalendarNames = StringUtils.join(calendarNames, ",");
		pipeline.setQuartzCalendarNames(quartzCalendarNames);

		//update calendar names fields
		updatePipeline(pipeline, actionUser);

		String jobName = "pipeline" + pipelineId;

		JobDetail quartzJob = JobBuilder.newJob(PipelineJob.class)
				.withIdentity(jobName, ArtUtils.JOB_GROUP)
				.usingJobData("pipelineId", pipelineId)
				.build();

		//add job and triggers to scheduler
		boolean replace = true;
		scheduler.scheduleJob(quartzJob, triggers, replace);
	}

	/**
	 * Returns pipelines that use a given schedule
	 *
	 * @param scheduleId the schedule id
	 * @return pipelines that use the schedule
	 * @throws SQLException
	 */
	@Cacheable("pipelines")
	public List<Pipeline> getPipelinesWithSchedule(int scheduleId) throws SQLException {
		logger.debug("Entering getPipelinesWithSchedule: scheduleId={}", scheduleId);

		String sql = SQL_SELECT_ALL
				+ " WHERE SCHEDULE_ID=?";

		ResultSetHandler<List<Pipeline>> h = new BeanListHandler<>(Pipeline.class, new PipelineMapper());
		return dbService.query(sql, h, scheduleId);
	}

	/**
	 * Returns pipelines that use a given holiday as part of the schedule
	 *
	 * @param holidayId the holiday id
	 * @return pipelines that use the holiday
	 * @throws SQLException
	 */
	public List<Pipeline> getHolidayPipelines(int holidayId) throws SQLException {
		logger.debug("Entering getHolidayPipelines");

		String sql = SQL_SELECT_ALL
				//where holidays are part of the pipeline schedule
				+ " WHERE EXISTS (SELECT *"
				+ " FROM ART_JOB_SCHEDULES AJS"
				+ " INNER JOIN ART_SCHEDULE_HOLIDAY_MAP ASHM"
				+ " ON AJS.SCHEDULE_ID=ASHM.SCHEDULE_ID"
				+ " WHERE AJS.SCHEDULE_ID=AP.SCHEDULE_ID AND ASHM.HOLIDAY_ID=?)";

		ResultSetHandler<List<Pipeline>> h = new BeanListHandler<>(Pipeline.class, new PipelineService.PipelineMapper());
		return dbService.query(sql, h, holidayId, holidayId);
	}

	/**
	 * Returns pipelines that use a given start condition
	 *
	 * @param startConditionId the start condition id
	 * @return pipelines that use the start condition
	 * @throws SQLException
	 */
	@Cacheable("pipelines")
	public List<Pipeline> getPipelinesWithStartCondition(int startConditionId) throws SQLException {
		logger.debug("Entering getPipelinesWithStartCondition: startConditionId={}", startConditionId);

		String sql = SQL_SELECT_ALL
				+ " WHERE START_CONDITION_ID=?";

		ResultSetHandler<List<Pipeline>> h = new BeanListHandler<>(Pipeline.class, new PipelineMapper());
		return dbService.query(sql, h, startConditionId);
	}

}
