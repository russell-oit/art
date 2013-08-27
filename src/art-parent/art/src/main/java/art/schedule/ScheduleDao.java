/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.schedule;

import art.servlets.ArtConfig;
import art.utils.ArtUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to provide data access methods for a schedule, using plain jdbc
 *
 * @author Timothy Anyona
 */
public class ScheduleDao {

	final static Logger logger = LoggerFactory.getLogger(ScheduleDao.class);

	/**
	 * Check if a schedule exists
	 *
	 * @param scheduleName
	 * @return <code>true</code> if schedule exists
	 */
	public boolean scheduleExists(String scheduleName) {
		boolean scheduleExists = false;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();
			String sql = "SELECT AJS.JOB_MINUTE "
					+ " FROM ART_JOB_SCHEDULES AJS "
					+ " WHERE AJS.SCHEDULE_NAME = ?";

			ps = conn.prepareStatement(sql);
			ps.setString(1, scheduleName);

			rs = ps.executeQuery();
			if (rs.next()) {
				scheduleExists = true;
			}
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			ArtUtils.close(rs, ps, conn);
		}

		return scheduleExists;
	}

	/**
	 * Insert new schedule
	 *
	 */
	public void insertSchedule(Schedule schedule) {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();
			String sql = "INSERT INTO ART_JOB_SCHEDULES"
					+ "(SCHEDULE_NAME, JOB_MINUTE, JOB_HOUR, JOB_DAY, JOB_MONTH, JOB_WEEKDAY)"
					+ " VALUES (?, ?, ?, ?, ?, ?)";

			ps = conn.prepareStatement(sql);
			ps.setString(1, schedule.getScheduleName());
			ps.setString(2, schedule.getMinute());
			ps.setString(3, schedule.getHour());
			ps.setString(4, schedule.getDay());
			ps.setString(5, schedule.getMonth());
			ps.setString(6, schedule.getWeekday());

			ps.executeUpdate();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			ArtUtils.close(ps, conn);
		}
	}

	/**
	 * Update schedule
	 *
	 */
	public void updateSchedule(Schedule schedule) {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();
			String sql = "UPDATE ART_JOB_SCHEDULES SET JOB_MINUTE = ? , JOB_HOUR = ?"
					+ " , JOB_DAY = ? , JOB_MONTH = ? , JOB_WEEKDAY = ? "
					+ " WHERE SCHEDULE_NAME = ? ";

			ps = conn.prepareStatement(sql);
			ps.setString(1, schedule.getMinute());
			ps.setString(2, schedule.getHour());
			ps.setString(3, schedule.getDay());
			ps.setString(4, schedule.getMonth());
			ps.setString(5, schedule.getWeekday());
			ps.setString(6, schedule.getScheduleName());

			ps.executeUpdate();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			ArtUtils.close(ps, conn);
		}
	}

	/**
	 * Get all schedule names
	 *
	 * @return all schedule names
	 */
	public List<String> getAllScheduleNames() {
		List<String> names = new ArrayList<String>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();
			String sql = "SELECT SCHEDULE_NAME "
					+ " FROM ART_JOB_SCHEDULES";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				names.add(rs.getString("SCHEDULE_NAME"));
			}
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			ArtUtils.close(rs, ps, conn);
		}

		//sort names
		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.TERTIARY); //order by case
		Collections.sort(names, stringCollator);

		return names;
	}

	/**
	 * Delete a schedule
	 *
	 * @param scheduleName
	 */
	public void deleteSchedule(String scheduleName) {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();
			String sql = "DELETE FROM ART_JOB_SCHEDULES "
					+ " WHERE SCHEDULE_NAME = ?";

			ps = conn.prepareStatement(sql);
			ps.setString(1, scheduleName);

			ps.executeUpdate();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			ArtUtils.close(ps, conn);
		}
	}

	/**
	 * Get the schedule with the given name
	 *
	 * @param scheduleName
	 */
	public Schedule getSchedule(String scheduleName) {
		Schedule schedule = new Schedule();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();
			String sql = "SELECT SCHEDULE_NAME, JOB_MINUTE, JOB_HOUR, JOB_DAY,"
					+ " JOB_MONTH, JOB_WEEKDAY "
					+ " FROM ART_JOB_SCHEDULES "
					+ " WHERE SCHEDULE_NAME = ?";

			ps = conn.prepareStatement(sql);
			ps.setString(1, scheduleName);

			rs = ps.executeQuery();
			if (rs.next()) {
				schedule.setScheduleName(rs.getString("SCHEDULE_NAME"));
				schedule.setMinute(rs.getString("JOB_MINUTE"));
				schedule.setHour(rs.getString("JOB_HOUR"));
				schedule.setDay(rs.getString("JOB_DAY"));
				schedule.setMonth(rs.getString("JOB_MONTH"));
				schedule.setWeekday(rs.getString("JOB_WEEKDAY"));
			}
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			ArtUtils.close(rs, ps, conn);
		}

		return schedule;
	}

	/**
	 * Get all schedules
	 *
	 * @return
	 */
	public List<Schedule> getAllSchedules() {
		List<Schedule> schedules = new ArrayList<Schedule>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();
			String sql = "SELECT SCHEDULE_NAME, JOB_MINUTE, JOB_HOUR, JOB_DAY,"
					+ " JOB_MONTH, JOB_WEEKDAY "
					+ " FROM ART_JOB_SCHEDULES ";

			ps = conn.prepareStatement(sql);

			rs = ps.executeQuery();
			while (rs.next()) {
				Schedule schedule = new Schedule();
				schedule.setScheduleName(rs.getString("SCHEDULE_NAME"));
				schedule.setMinute(rs.getString("JOB_MINUTE"));
				schedule.setHour(rs.getString("JOB_HOUR"));
				schedule.setDay(rs.getString("JOB_DAY"));
				schedule.setMonth(rs.getString("JOB_MONTH"));
				schedule.setWeekday(rs.getString("JOB_WEEKDAY"));
				schedules.add(schedule);
			}
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			ArtUtils.close(rs, ps, conn);
		}

		return schedules;
	}

	
}
