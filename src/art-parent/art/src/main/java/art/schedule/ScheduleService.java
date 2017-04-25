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
import art.user.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
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

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_JOB_SCHEDULES";

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
			schedule.setMinute(rs.getString("JOB_MINUTE"));
			schedule.setHour(rs.getString("JOB_HOUR"));
			schedule.setDay(rs.getString("JOB_DAY"));
			schedule.setMonth(rs.getString("JOB_MONTH"));
			schedule.setWeekday(rs.getString("JOB_WEEKDAY"));
			schedule.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			schedule.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			schedule.setCreatedBy(rs.getString("CREATED_BY"));
			schedule.setUpdatedBy(rs.getString("UPDATED_BY"));

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
	 * @throws SQLException
	 */
	@CacheEvict(value = "schedules", allEntries = true)
	public void deleteSchedule(int id) throws SQLException {
		logger.debug("Entering deleteSchedule: id={}", id);

		String sql;

		sql = "DELETE FROM ART_JOB_SCHEDULES WHERE SCHEDULE_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes multiple schedule
	 *
	 * @param ids the ids of schedules to delete
	 * @throws SQLException
	 */
	@CacheEvict(value = "schedules", allEntries = true)
	public void deleteSchedules(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteSchedules: ids={}", (Object) ids);

		String sql;

		sql = "DELETE FROM ART_JOB_SCHEDULES"
				+ " WHERE SCHEDULE_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";
		
		dbService.update(sql, (Object[]) ids);
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

		schedule.setScheduleId(newId);

		saveSchedule(schedule, true, actionUser);

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

		saveSchedule(schedule, false, actionUser);
	}

	/**
	 * Saves a schedule
	 *
	 * @param schedule the schedule to save
	 * @param newRecord whether this is a new record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	private void saveSchedule(Schedule schedule, boolean newRecord, User actionUser) throws SQLException {
		logger.debug("Entering saveSchedule: schedule={}, newRecord={}, actionUser={}",
				schedule, newRecord, actionUser);

		int affectedRows;
		if (newRecord) {
			String sql = "INSERT INTO ART_JOB_SCHEDULES"
					+ " (SCHEDULE_ID, SCHEDULE_NAME, DESCRIPTION, JOB_MINUTE,"
					+ " JOB_HOUR, JOB_DAY, JOB_MONTH, JOB_WEEKDAY, CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 10) + ")";

			Object[] values = {
				schedule.getScheduleId(),
				schedule.getName(),
				schedule.getDescription(),
				schedule.getMinute(),
				schedule.getHour(),
				schedule.getDay(),
				schedule.getMonth(),
				schedule.getWeekday(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_JOB_SCHEDULES SET SCHEDULE_NAME=?, DESCRIPTION=?,"
					+ " JOB_MINUTE=?, JOB_HOUR=?, JOB_DAY=?, JOB_MONTH=?,"
					+ " JOB_WEEKDAY=?, UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE SCHEDULE_ID=?";

			Object[] values = {
				schedule.getName(),
				schedule.getDescription(),
				schedule.getMinute(),
				schedule.getHour(),
				schedule.getDay(),
				schedule.getMonth(),
				schedule.getWeekday(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				schedule.getScheduleId()
			};

			affectedRows = dbService.update(sql, values);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, schedule={}",
					affectedRows, newRecord, schedule);
		}
	}
}
