/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.servlets.ArtConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to use to save job schedule details for later reuse
 *
 * @author Timothy Anyona
 */
public class JobSchedule {

	final static Logger logger = LoggerFactory.getLogger(JobSchedule.class);
	String minute = "";
	String hour = "";
	String day = "";
	String month = "";
	String weekday = "";
	String scheduleName = "";

	/**
	 *
	 */
	public JobSchedule() {
	}

	/**
	 *
	 * @param value
	 */
	public void setScheduleName(String value) {
		scheduleName = value;
	}

	/**
	 *
	 * @return schedule name
	 */
	public String getScheduleName() {
		return scheduleName;
	}

	/**
	 *
	 * @param value
	 */
	public void setMinute(String value) {
		minute = value;
	}

	/**
	 *
	 * @param value
	 */
	public void setHour(String value) {
		hour = value;
	}

	/**
	 *
	 * @param value
	 */
	public void setDay(String value) {
		day = value;
	}

	/**
	 *
	 * @param value
	 */
	public void setWeekday(String value) {
		weekday = value;
	}

	/**
	 *
	 * @param value
	 */
	public void setMonth(String value) {
		month = value;
	}

	/**
	 *
	 * @return minute that job should run
	 */
	public String getMinute() {
		return minute;
	}

	/**
	 *
	 * @return hour that job should run
	 */
	public String getHour() {
		return hour;
	}

	/**
	 *
	 * @return day of the month that job should run
	 */
	public String getDay() {
		return day;
	}

	/**
	 *
	 * @return weekday that job should run
	 */
	public String getWeekday() {
		return weekday;
	}

	/**
	 *
	 * @return month that job should run
	 */
	public String getMonth() {
		return month;
	}

	/**
	 * Check if a schedule exists
	 *
	 * @param name
	 * @return <code>true</code> if schedule exists
	 */
	public boolean exists(String name) {
		boolean scheduleExists = false;
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();
			String sql = "SELECT AJS.JOB_MINUTE "
					+ " FROM ART_JOB_SCHEDULES AJS "
					+ " WHERE AJS.SCHEDULE_NAME = ?";

			ps = conn.prepareStatement(sql);
			ps.setString(1, name);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				scheduleExists = true;
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return scheduleExists;
	}

	/**
	 * Insert new schedule
	 *
	 * @return <code>true</code> if successful
	 */
	public boolean insert() {
		boolean success = false;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();
			String sql = "INSERT INTO ART_JOB_SCHEDULES"
					+ "(SCHEDULE_NAME, JOB_MINUTE, JOB_HOUR, JOB_DAY, JOB_MONTH, JOB_WEEKDAY)"
					+ " VALUES (?, ?, ?, ?, ?, ?)";

			ps = conn.prepareStatement(sql);
			ps.setString(1, scheduleName);
			ps.setString(2, minute);
			ps.setString(3, hour);
			ps.setString(4, day);
			ps.setString(5, month);
			ps.setString(6, weekday);

			ps.executeUpdate();

			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Update schedule
	 *
	 * @return <code>true</code> if successful
	 */
	public boolean update() {
		boolean success = false;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();
			String sql = "UPDATE ART_JOB_SCHEDULES SET JOB_MINUTE = ? , JOB_HOUR = ?"
					+ " , JOB_DAY = ? , JOB_MONTH = ? , JOB_WEEKDAY = ? "
					+ " WHERE SCHEDULE_NAME = ? ";

			ps = conn.prepareStatement(sql);
			ps.setString(1, minute);
			ps.setString(2, hour);
			ps.setString(3, day);
			ps.setString(4, month);
			ps.setString(5, weekday);
			ps.setString(6, scheduleName);

			ps.executeUpdate();

			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
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

		try {
			conn = ArtConfig.getConnection();

			String sql = "SELECT SCHEDULE_NAME "
					+ " FROM ART_JOB_SCHEDULES";

			ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				names.add(rs.getString("SCHEDULE_NAME"));
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
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
	 * @param name
	 * @return <code>true</code> if successful
	 */
	public boolean delete(String name) {
		boolean deleted = false;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql = "DELETE FROM ART_JOB_SCHEDULES "
					+ " WHERE SCHEDULE_NAME = ?";
			ps = conn.prepareStatement(sql);

			ps.setString(1, name);
			ps.executeUpdate();
			deleted = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return deleted;
	}

	/**
	 * Populate the schedule object with details of the given schedule name
	 *
	 * @param name
	 * @return <code>true</code> if successful
	 */
	public boolean load(String name) {
		boolean success = false;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql = "SELECT SCHEDULE_NAME, JOB_MINUTE, JOB_HOUR, JOB_DAY,"
					+ " JOB_MONTH, JOB_WEEKDAY "
					+ " FROM ART_JOB_SCHEDULES "
					+ " WHERE SCHEDULE_NAME = ?";
			ps = conn.prepareStatement(sql);

			ps.setString(1, name);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				scheduleName = rs.getString("SCHEDULE_NAME");
				minute = rs.getString("JOB_MINUTE");
				hour = rs.getString("JOB_HOUR");
				day = rs.getString("JOB_DAY");
				month = rs.getString("JOB_MONTH");
				weekday = rs.getString("JOB_WEEKDAY");
			}
			rs.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}
}
