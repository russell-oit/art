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
import art.utils.ActionResult;
import art.utils.ArtUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
			holiday.setDefinition(rs.getString("HOLIDAY_DEFINITION"));
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
	 * Returns holidays with given ids
	 *
	 * @param ids comma separated string of the holiday ids to retrieve
	 * @return holidays with given ids
	 * @throws SQLException
	 */
	public List<Holiday> getHolidays(String ids) throws SQLException {
		logger.debug("Entering getHolidays: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		String sql = SQL_SELECT_ALL
				+ " WHERE HOLIDAY_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<Holiday>> h = new BeanListHandler<>(Holiday.class, new HolidayMapper());
		return dbService.query(sql, h, idsArray);
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
	 * @return ActionResult. if not successful, data contains a list of linked
	 * schedules and jobs which prevented the holiday from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "holidays", allEntries = true)
	public ActionResult deleteHoliday(int id) throws SQLException {
		logger.debug("Entering deleteHoliday: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedRecords = getLinkedRecords(id);
		if (!linkedRecords.isEmpty()) {
			result.setData(linkedRecords);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_SCHEDULE_HOLIDAY_MAP WHERE HOLIDAY_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_JOB_HOLIDAY_MAP WHERE HOLIDAY_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_HOLIDAYS WHERE HOLIDAY_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes multiple holiday
	 *
	 * @param ids the ids of holidays to delete
	 * @return ActionResult. if not successful, data contains details of
	 * holidays which weren't deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "holidays", allEntries = true)
	public ActionResult deleteHolidays(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteHolidays: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteHoliday(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedRecords = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedRecords, ", ");
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
		int newId = dbService.getNewRecordId(sql);

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
	 * Imports holiday records
	 *
	 * @param holidays the list of holidays to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = "holidays", allEntries = true)
	public void importHolidays(List<Holiday> holidays, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering importHolidays: actionUser={}", actionUser);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(HOLIDAY_ID) FROM ART_HOLIDAYS";
			int id = dbService.getMaxRecordId(conn, sql);

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			for (Holiday holiday : holidays) {
				id++;
				saveHoliday(holiday, id, actionUser, conn);
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
	 * Saves a holiday
	 *
	 * @param holiday the holiday to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveHoliday(Holiday holiday, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveHoliday(holiday, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a holiday
	 *
	 * @param holiday the holiday to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. If null, the art database will be used
	 * @throws SQLException
	 */
	@CacheEvict(value = "holidays", allEntries = true)
	public void saveHoliday(Holiday holiday, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering saveHoliday: holiday={}, newRecordId={},"
				+ " actionUser={}", holiday, newRecordId, actionUser);

		int affectedRows;
		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_HOLIDAYS"
					+ " (HOLIDAY_ID, NAME, DESCRIPTION, HOLIDAY_DEFINITION,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 6) + ")";

			Object[] values = {
				newRecordId,
				holiday.getName(),
				holiday.getDescription(),
				holiday.getDefinition(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_HOLIDAYS SET NAME=?, DESCRIPTION=?,"
					+ " HOLIDAY_DEFINITION=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE HOLIDAY_ID=?";

			Object[] values = {
				holiday.getName(),
				holiday.getDescription(),
				holiday.getDefinition(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				holiday.getHolidayId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
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

	/**
	 * Returns the shared holidays used by a given job
	 *
	 * @param jobId the job id
	 * @return the shared holidays used by a given job
	 * @throws SQLException
	 */
	@Cacheable("holidays")
	public List<Holiday> getJobHolidays(int jobId) throws SQLException {
		logger.debug("Entering getJobHolidays: jobId={}", jobId);

		String sql = SQL_SELECT_ALL + " INNER JOIN ART_JOB_HOLIDAY_MAP AJHM"
				+ " ON AH.HOLIDAY_ID=AJHM.HOLIDAY_ID"
				+ " INNER JOIN ART_JOBS AJ"
				+ " ON AJHM.JOB_ID=AJ.JOB_ID"
				+ " WHERE AJ.JOB_ID=?";

		ResultSetHandler<List<Holiday>> h = new BeanListHandler<>(Holiday.class, new HolidayMapper());
		return dbService.query(sql, h, jobId);
	}

	/**
	 * Returns details of schedules and jobs that use a given holiday
	 *
	 * @param holidayId the holiday id
	 * @return linked schedule and job details
	 * @throws SQLException
	 */
	public List<String> getLinkedRecords(int holidayId) throws SQLException {
		logger.debug("Entering getLinkedRecords: holidayId={}", holidayId);

		//union removes duplicate records, union all does not
		//use union all in case a schedule and a job have the same name? or two jobs have the same name
		String sql = "SELECT AJS.SCHEDULE_ID AS RECORD_ID, AJS.SCHEDULE_NAME AS RECORD_NAME"
				+ " FROM ART_JOB_SCHEDULES AJS"
				+ " INNER JOIN ART_SCHEDULE_HOLIDAY_MAP ASHM"
				+ " ON AJS.SCHEDULE_ID=ASHM.SCHEDULE_ID"
				+ " WHERE ASHM.HOLIDAY_ID=?"
				+ " UNION ALL"
				+ " SELECT AJ.JOB_ID AS RECORD_ID, AJ.JOB_NAME AS RECORD_NAME"
				+ " FROM ART_JOBS AJ"
				+ " INNER JOIN ART_JOB_HOLIDAY_MAP AJHM"
				+ " ON AJ.JOB_ID=AJHM.JOB_ID"
				+ " WHERE AJHM.HOLIDAY_ID=?";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> recordDetails = dbService.query(sql, h, holidayId, holidayId);

		List<String> records = new ArrayList<>();
		for (Map<String, Object> recordDetail : recordDetails) {
			Integer recordId = (Integer) recordDetail.get("RECORD_ID");
			String recordName = (String) recordDetail.get("RECORD_NAME");
			records.add(recordName + " (" + recordId + ")");
		}

		return records;
	}
}
