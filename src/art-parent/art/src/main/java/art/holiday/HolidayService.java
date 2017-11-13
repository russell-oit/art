/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.holiday;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
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
 * Provides methods for adding, deleting, retrieving and updating holiday
 * configurations
 *
 * @author Timothy Anyona
 */
@Service
public class HolidayService {

	private static final Logger logger = LoggerFactory.getLogger(HolidayService.class);

	private final DbService dbService;

	@Autowired
	public HolidayService(DbService dbService) {
		this.dbService = dbService;
	}

	public HolidayService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_HOLIDAYS AH";

	/**
	 * Maps a resultset to an object
	 */
	private class HolidayMapper extends BasicRowProcessor {

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
			Holiday holiday = new Holiday();

			holiday.setHolidayId(rs.getInt("HOLIDAY_ID"));
			holiday.setName(rs.getString("NAME"));
			holiday.setDescription(rs.getString("DESCRIPTION"));
			holiday.setDetails(rs.getString("DETAILS"));
			holiday.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			holiday.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			holiday.setCreatedBy(rs.getString("CREATED_BY"));
			holiday.setUpdatedBy(rs.getString("UPDATED_BY"));

			return type.cast(holiday);
		}
	}

	/**
	 * Returns all holidays
	 *
	 * @return all holidays
	 * @throws SQLException
	 */
	@Cacheable("holidays")
	public List<Holiday> getAllHolidays() throws SQLException {
		logger.debug("Entering getAllHolidays");

		ResultSetHandler<List<Holiday>> h = new BeanListHandler<>(Holiday.class, new HolidayMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns a holiday
	 *
	 * @param id the holiday id
	 * @return holiday if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("holidays")
	public Holiday getHoliday(int id) throws SQLException {
		logger.debug("Entering getHoliday: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE HOLIDAY_ID=?";
		ResultSetHandler<Holiday> h = new BeanHandler<>(Holiday.class, new HolidayMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Deletes a holiday
	 *
	 * @param id the holiday id
	 * @throws SQLException
	 */
	@CacheEvict(value = "holidays", allEntries = true)
	public void deleteHoliday(int id) throws SQLException {
		logger.debug("Entering deleteHoliday: id={}", id);

		String sql;

		sql = "DELETE FROM ART_HOLIDAYS WHERE HOLIDAY_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes multiple holiday
	 *
	 * @param ids the ids of holidays to delete
	 * @throws SQLException
	 */
	@CacheEvict(value = "holidays", allEntries = true)
	public void deleteHolidays(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteHolidays: ids={}", (Object) ids);

		String sql;

		sql = "DELETE FROM ART_HOLIDAYS"
				+ " WHERE HOLIDAY_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";

		dbService.update(sql, (Object[]) ids);
	}

	/**
	 * Adds a new holiday
	 *
	 * @param holiday the holiday to add
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "holidays", allEntries = true)
	public synchronized int addHoliday(Holiday holiday, User actionUser) throws SQLException {
		logger.debug("Entering addHoliday: holiday={}, actionUser={}", holiday, actionUser);

		//generate new id
		String sql = "SELECT MAX(HOLIDAY_ID) FROM ART_HOLIDAYS";
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

		saveHoliday(holiday, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing holiday
	 *
	 * @param holiday the updated holiday
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "holidays", allEntries = true)
	public void updateHoliday(Holiday holiday, User actionUser) throws SQLException {
		logger.debug("Entering updateHoliday: holiday={}, actionUser={}", holiday, actionUser);

		Integer newRecordId = null;
		saveHoliday(holiday, newRecordId, actionUser);
	}

	/**
	 * Saves a holiday
	 *
	 * @param holiday the holiday to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	private void saveHoliday(Holiday holiday, Integer newRecordId, User actionUser) throws SQLException {
		logger.debug("Entering saveHoliday: holiday={}, newRecordId={}, actionUser={}",
				holiday, newRecordId, actionUser);

		int affectedRows;
		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_HOLIDAYS"
					+ " (HOLIDAY_ID, NAME, DESCRIPTION, DETAILS,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 6) + ")";

			Object[] values = {
				newRecordId,
				holiday.getName(),
				holiday.getDescription(),
				holiday.getDetails(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_HOLIDAYS SET NAME=?, DESCRIPTION=?,"
					+ " DETAILS=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE HOLIDAY_ID=?";

			Object[] values = {
				holiday.getName(),
				holiday.getDescription(),
				holiday.getDetails(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				holiday.getHolidayId()
			};

			affectedRows = dbService.update(sql, values);
		}

		if (newRecordId != null) {
			holiday.setHolidayId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, holiday={}",
					affectedRows, newRecord, holiday);
		}
	}

	/**
	 * Returns the shared holidays used by a given schedule
	 * 
	 * @param scheduleId the schedule id
	 * @return the shared holidays used by a given schedule
	 * @throws SQLException 
	 */
	@Cacheable("holidays")
	public List<Holiday> getScheduleHolidays(int scheduleId) throws SQLException {
		logger.debug("Entering getScheduleHolidays: scheduleId={}", scheduleId);

		String sql = SQL_SELECT_ALL + " INNER JOIN ART_SCHEDULE_HOLIDAY_MAP ASHM"
				+ " ON AH.HOLIDAY_ID=ASHM.HOLIDAY_ID"
				+ " INNER JOIN ART_JOB_SCHEDULES AJS"
				+ " ON ASHM.SCHEDULE_ID=AJS.SCHEDULE_ID"
				+ " WHERE AJS.SCHEDULE_ID=?";

		ResultSetHandler<List<Holiday>> h = new BeanListHandler<>(Holiday.class, new HolidayMapper());
		return dbService.query(sql, h, scheduleId);
	}
}
