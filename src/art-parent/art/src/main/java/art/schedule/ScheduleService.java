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
package art.schedule;

import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.holiday.Holiday;
import art.holiday.HolidayService;
import art.scheduleholiday.ScheduleHolidayService;
import art.user.User;
import art.utils.ActionResult;
import art.utils.ArtUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting schedules
 *
 * @author Timothy Anyona
 */
@Service
public class ScheduleService {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

	private final DbService dbService;
	private final HolidayService holidayService;
	private final ScheduleHolidayService scheduleHolidayService;

	@Autowired
	public ScheduleService(DbService dbService, HolidayService holidayService,
			ScheduleHolidayService scheduleHolidayService) {

		this.dbService = dbService;
		this.holidayService = holidayService;
		this.scheduleHolidayService = scheduleHolidayService;
	}

	public ScheduleService() {
		dbService = new DbService();
		holidayService = new HolidayService();
		scheduleHolidayService = new ScheduleHolidayService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_JOB_SCHEDULES AJS";

	/**
	 * Maps a resultset to an object
	 */
	private class ScheduleMapper extends BasicRowProcessor {

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
			Schedule schedule = new Schedule();

			schedule.setScheduleId(rs.getInt("SCHEDULE_ID"));
			schedule.setName(rs.getString("SCHEDULE_NAME"));
			schedule.setDescription(rs.getString("DESCRIPTION"));
			schedule.setSecond(rs.getString("JOB_SECOND"));
			schedule.setMinute(rs.getString("JOB_MINUTE"));
			schedule.setHour(rs.getString("JOB_HOUR"));
			schedule.setDay(rs.getString("JOB_DAY"));
			schedule.setMonth(rs.getString("JOB_MONTH"));
			schedule.setWeekday(rs.getString("JOB_WEEKDAY"));
			schedule.setYear(rs.getString("JOB_YEAR"));
			schedule.setTimeZone(rs.getString("TIME_ZONE"));
			schedule.setExtraSchedules(rs.getString("EXTRA_SCHEDULES"));
			schedule.setHolidays(rs.getString("HOLIDAYS"));
			schedule.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			schedule.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			schedule.setCreatedBy(rs.getString("CREATED_BY"));
			schedule.setUpdatedBy(rs.getString("UPDATED_BY"));

			List<Holiday> sharedHolidays = holidayService.getScheduleHolidays(schedule.getScheduleId());
			schedule.setSharedHolidays(sharedHolidays);

			return type.cast(schedule);
		}
	}

	/**
	 * Returns all schedules
	 *
	 * @return all schedules
	 * @throws SQLException
	 */
	@Cacheable("schedules")
	public List<Schedule> getAllSchedules() throws SQLException {
		logger.debug("Entering getAllSchedules");

		ResultSetHandler<List<Schedule>> h = new BeanListHandler<>(Schedule.class, new ScheduleMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns schedules with given ids
	 *
	 * @param ids comma separated string of the schedule ids to retrieve
	 * @return schedules with given ids
	 * @throws SQLException
	 */
	public List<Schedule> getSchedules(String ids) throws SQLException {
		logger.debug("Entering getSchedules: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		String sql = SQL_SELECT_ALL
				+ " WHERE SCHEDULE_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<Schedule>> h = new BeanListHandler<>(Schedule.class, new ScheduleMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns a schedule
	 *
	 * @param id the schedule id
	 * @return schedule if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("schedules")
	public Schedule getSchedule(int id) throws SQLException {
		logger.debug("Entering getSchedule: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE SCHEDULE_ID=?";
		ResultSetHandler<Schedule> h = new BeanHandler<>(Schedule.class, new ScheduleMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Deletes a schedule
	 *
	 * @param id the schedule id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * jobs which prevented the schedule from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "schedules", allEntries = true)
	public ActionResult deleteSchedule(int id) throws SQLException {
		logger.debug("Entering deleteSchedule: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedJobs = getLinkedJobs(id);
		if (!linkedJobs.isEmpty()) {
			result.setData(linkedJobs);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_JOB_SCHEDULES WHERE SCHEDULE_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes multiple schedules
	 *
	 * @param ids the ids of schedules to delete
	 * @return ActionResult. if not successful, data contains details of
	 * schedules which weren't deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "schedules", allEntries = true)
	public ActionResult deleteSchedules(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteSchedules: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteSchedule(id);
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
	 * Adds a new schedule
	 *
	 * @param schedule the schedule to add
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "schedules", allEntries = true)
	public synchronized int addSchedule(Schedule schedule, User actionUser) throws SQLException {
		logger.debug("Entering addSchedule: schedule={}, actionUser={}", schedule, actionUser);

		//generate new id
		String sql = "SELECT MAX(SCHEDULE_ID) FROM ART_JOB_SCHEDULES";
		int newId = dbService.getNewRecordId(sql);

		saveSchedule(schedule, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing schedule
	 *
	 * @param schedule the updated schedule
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "schedules", allEntries = true)
	public void updateSchedule(Schedule schedule, User actionUser) throws SQLException {
		logger.debug("Entering updateSchedule: schedule={}, actionUser={}", schedule, actionUser);

		Integer newRecordId = null;
		saveSchedule(schedule, newRecordId, actionUser);
	}

	/**
	 * Imports schedule records
	 *
	 * @param schedules the list of schedules to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = "schedules", allEntries = true)
	public void importSchedules(List<Schedule> schedules, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering importSchedules: actionUser={}", actionUser);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(SCHEDULE_ID) FROM ART_JOB_SCHEDULES";
			int scheduleId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(HOLIDAY_ID) FROM ART_HOLIDAYS";
			int holidayId = dbService.getMaxRecordId(sql);

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			Map<String, Holiday> addedHolidays = new HashMap<>();
			for (Schedule schedule : schedules) {
				scheduleId++;
				List<Holiday> sharedHolidays = schedule.getSharedHolidays();
				if (CollectionUtils.isNotEmpty(sharedHolidays)) {
					List<Holiday> newSharedHolidays = new ArrayList<>();
					for (Holiday holiday : sharedHolidays) {
						String holidayName = holiday.getName();
						Holiday existingHoliday = holidayService.getHoliday(holidayName);
						if (existingHoliday == null) {
							Holiday addedHoliday = addedHolidays.get(holidayName);
							if (addedHoliday == null) {
								holidayId++;
								holidayService.saveHoliday(holiday, holidayId, actionUser, conn);
								addedHolidays.put(holidayName, holiday);
								newSharedHolidays.add(holiday);
							} else {
								newSharedHolidays.add(addedHoliday);
							}
						} else {
							newSharedHolidays.add(existingHoliday);
						}
					}
					schedule.setSharedHolidays(newSharedHolidays);
				}
				saveSchedule(schedule, scheduleId, actionUser, conn);
				scheduleHolidayService.recreateScheduleHolidays(schedule);
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
	 * Saves a schedule
	 *
	 * @param schedule the schedule to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveSchedule(Schedule schedule, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveSchedule(schedule, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a schedule
	 *
	 * @param schedule the schedule to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	private void saveSchedule(Schedule schedule, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering saveSchedule: schedule={}, newRecordId={},"
				+ " actionUser={}", schedule, newRecordId, actionUser);

		int affectedRows;
		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_JOB_SCHEDULES"
					+ " (SCHEDULE_ID, SCHEDULE_NAME, DESCRIPTION,"
					+ " JOB_SECOND, JOB_MINUTE,"
					+ " JOB_HOUR, JOB_DAY, JOB_MONTH, JOB_WEEKDAY, JOB_YEAR,"
					+ " TIME_ZONE, EXTRA_SCHEDULES, HOLIDAYS,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 15) + ")";

			Object[] values = {
				newRecordId,
				schedule.getName(),
				schedule.getDescription(),
				schedule.getSecond(),
				schedule.getMinute(),
				schedule.getHour(),
				schedule.getDay(),
				schedule.getMonth(),
				schedule.getWeekday(),
				schedule.getYear(),
				schedule.getTimeZone(),
				schedule.getExtraSchedules(),
				schedule.getHolidays(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_JOB_SCHEDULES SET SCHEDULE_NAME=?, DESCRIPTION=?,"
					+ " JOB_SECOND=?, JOB_MINUTE=?, JOB_HOUR=?, JOB_DAY=?, JOB_MONTH=?,"
					+ " JOB_WEEKDAY=?, JOB_YEAR=?, TIME_ZONE=?,"
					+ " EXTRA_SCHEDULES=?, HOLIDAYS=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE SCHEDULE_ID=?";

			Object[] values = {
				schedule.getName(),
				schedule.getDescription(),
				schedule.getSecond(),
				schedule.getMinute(),
				schedule.getHour(),
				schedule.getDay(),
				schedule.getMonth(),
				schedule.getWeekday(),
				schedule.getYear(),
				schedule.getTimeZone(),
				schedule.getExtraSchedules(),
				schedule.getHolidays(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				schedule.getScheduleId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		if (newRecordId != null) {
			schedule.setScheduleId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, schedule={}",
					affectedRows, newRecord, schedule);
		}
	}

	/**
	 * Returns details of jobs that use a given schedule as a fixed schedule
	 *
	 * @param scheduleId the schedule id
	 * @return linked job details
	 * @throws SQLException
	 */
	public List<String> getLinkedJobs(int scheduleId) throws SQLException {
		logger.debug("Entering getLinkedJobs: scheduleId={}", scheduleId);

		String sql = "SELECT JOB_ID, JOB_NAME"
				+ " FROM ART_JOBS"
				+ " WHERE SCHEDULE_ID=?";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> jobDetails = dbService.query(sql, h, scheduleId);

		List<String> jobs = new ArrayList<>();
		for (Map<String, Object> jobDetail : jobDetails) {
			Integer jobId = (Integer) jobDetail.get("JOB_ID");
			String jobName = (String) jobDetail.get("JOB_NAME");
			jobs.add(jobName + " (" + jobId + ")");
		}

		return jobs;
	}

	/**
	 * Returns schedules that use a given holiday
	 *
	 * @param holidayId the holiday id
	 * @return schedules that use the holiday
	 * @throws SQLException
	 */
	public List<Schedule> getSchedulesWithHoliday(int holidayId) throws SQLException {
		logger.debug("Entering getSchedulesWithHoliday: holidayId={}", holidayId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_SCHEDULE_HOLIDAY_MAP ASHM"
				+ " ON AJS.SCHEDULE_ID=ASHM.SCHEDULE_ID"
				+ " WHERE ASHM.HOLIDAY_ID=?";

		ResultSetHandler<List<Schedule>> h = new BeanListHandler<>(Schedule.class, new ScheduleMapper());
		return dbService.query(sql, h, holidayId);
	}
}
