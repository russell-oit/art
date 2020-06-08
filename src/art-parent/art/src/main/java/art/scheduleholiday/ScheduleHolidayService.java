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
package art.scheduleholiday;

import art.dbutils.DbService;
import art.holiday.Holiday;
import art.schedule.Schedule;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, updating and deleting schedule-holiday
 * records
 *
 * @author Timothy Anyona
 */
@Service
public class ScheduleHolidayService {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleHolidayService.class);

	private final DbService dbService;

	@Autowired
	public ScheduleHolidayService(DbService dbService) {
		this.dbService = dbService;
	}

	public ScheduleHolidayService() {
		dbService = new DbService();
	}

	/**
	 * Recreates schedule-holiday records for a given schedule
	 *
	 * @param schedule the schedule
	 * @throws SQLException
	 */
	@CacheEvict(value = {"schedules", "holidays"}, allEntries = true)
	public void recreateScheduleHolidays(Schedule schedule) throws SQLException {
		Connection conn = null;
		recreateScheduleHolidays(schedule, conn);
	}

	/**
	 * Recreates schedule-holiday records for a given schedule
	 *
	 * @param schedule the schedule
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"schedules", "holidays"}, allEntries = true)
	public void recreateScheduleHolidays(Schedule schedule, Connection conn) throws SQLException {
		logger.debug("Entering recreateScheduleHolidays: schedule={}", schedule);

		int scheduleId = schedule.getScheduleId();
		deleteAllScheduleHolidaysForSchedule(scheduleId, conn);
		addScheduleHolidays(scheduleId, schedule.getSharedHolidays(), conn);
	}

	/**
	 * Delete all schedule-holiday records for the given schedule
	 *
	 * @param scheduleId the schedule id
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	private void deleteAllScheduleHolidaysForSchedule(int scheduleId, Connection conn) throws SQLException {
		logger.debug("Entering deleteAllScheduleHolidaysForSchedule: scheduleId={}", scheduleId);

		String sql = "DELETE FROM ART_SCHEDULE_HOLIDAY_MAP WHERE SCHEDULE_ID=?";
		dbService.update(conn, sql, scheduleId);
	}

	/**
	 * Adds schedule-holiday records for the given schedule
	 *
	 * @param scheduleId the schedule id
	 * @param holidays the holidays
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	private void addScheduleHolidays(int scheduleId, List<Holiday> holidays,
			Connection conn) throws SQLException {

		logger.debug("Entering addScheduleHolidays: scheduleId={}", scheduleId);

		if (CollectionUtils.isEmpty(holidays)) {
			return;
		}

		List<Integer> holidayIds = new ArrayList<>();
		for (Holiday holiday : holidays) {
			holidayIds.add(holiday.getHolidayId());
		}

		Integer[] schedules = {scheduleId};
		String action = "add";
		updateScheduleHolidays(action, schedules, holidayIds.toArray(new Integer[0]), conn);
	}

	/**
	 * Adds or removes schedule-holiday records
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param schedules schedule ids
	 * @param holidays holiday ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"schedules", "holidays"}, allEntries = true)
	public void updateScheduleHolidays(String action, Integer[] schedules,
			Integer[] holidays) throws SQLException {

		Connection conn = null;
		updateScheduleHolidays(action, schedules, holidays, conn);
	}

	/**
	 * Adds or removes schedule-holiday records
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param schedules schedule ids
	 * @param holidays holiday ids
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"schedules", "holidays"}, allEntries = true)
	public void updateScheduleHolidays(String action, Integer[] schedules,
			Integer[] holidays, Connection conn) throws SQLException {

		logger.debug("Entering updateScheduleHolidays: action='{}'", action);

		logger.debug("(schedules == null) = {}", schedules == null);
		logger.debug("(holidays == null) = {}", holidays == null);
		if (schedules == null || holidays == null) {
			return;
		}

		boolean add;
		if (StringUtils.equalsIgnoreCase(action, "add")) {
			add = true;
		} else {
			add = false;
		}

		String sql;

		if (add) {
			sql = "INSERT INTO ART_SCHEDULE_HOLIDAY_MAP (SCHEDULE_ID, HOLIDAY_ID) VALUES (?, ?)";
		} else {
			sql = "DELETE FROM ART_SCHEDULE_HOLIDAY_MAP WHERE SCHEDULE_ID=? AND HOLIDAY_ID=?";
		}

		String sqlTest = "UPDATE ART_SCHEDULE_HOLIDAY_MAP SET SCHEDULE_ID=? WHERE SCHEDULE_ID=? AND HOLIDAY_ID=?";
		int affectedRows;
		boolean updateRecord;

		for (Integer scheduleId : schedules) {
			for (Integer holidayId : holidays) {
				updateRecord = true;
				if (add) {
					//test if record exists. to avoid integrity constraint error
					affectedRows = dbService.update(conn, sqlTest, scheduleId, scheduleId, holidayId);

					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRecord = false;
					}
				}
				if (updateRecord) {
					dbService.update(conn, sql, scheduleId, holidayId);
				}
			}
		}
	}

}
