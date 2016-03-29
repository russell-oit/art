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
package art.utils;

import art.connectionpool.DbConnections;
import art.dbutils.DatabaseUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for helper methods. Can have dependencies on other art classes
 *
 * @author Timothy Anyona
 */
public class ArtHelper {

	private static final Logger logger = LoggerFactory.getLogger(ArtHelper.class);
	private static final int MAX_LOG_MESSAGE_LENGTH = 500;
	
	/**
	 * Log some action to the ART_LOGS table.
	 *
	 * @param user username of user who executed the query
	 * @param type type of event
	 * @param ip ip address from which query was run
	 * @param queryId id of the query that was run
	 * @param totalTime total time to execute the query and display the results
	 * @param fetchTime time to fetch the results from the database
	 * @param message log message
	 */
	public static void log(String user, String type, String ip, int queryId, long totalTime, long fetchTime, String message) {
		if (StringUtils.length(message) > MAX_LOG_MESSAGE_LENGTH) {
			message = message.substring(0, MAX_LOG_MESSAGE_LENGTH);
		}

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DbConnections.getArtDbConnection();
			String sql = "INSERT INTO ART_LOGS"
					+ " (LOG_DATE, USERNAME, LOG_TYPE, IP, QUERY_ID,"
					+ " TOTAL_TIME, FETCH_TIME, MESSAGE) "
					+ " VALUES (?,?,?,?,?,?,?,?) ";

			ps = conn.prepareStatement(sql);

			Timestamp now = new Timestamp(new Date().getTime());

			ps.setTimestamp(1, now);
			ps.setString(2, user);
			ps.setString(3, type);
			ps.setString(4, ip);
			ps.setInt(5, queryId);
			ps.setInt(6, (int) totalTime);
			ps.setInt(7, (int) fetchTime);
			ps.setString(8, message);

			ps.executeUpdate();
		} catch (SQLException ex) {
			logger.error("Error", ex);
		} finally {
			DatabaseUtils.close(ps, conn);
		}
	}

	/**
	 * Log login attempts to the ART_LOGS table.
	 *
	 * @param user username
	 * @param type "login" if successful or "loginerr" if not
	 * @param ip ip address from which login was done or attempted
	 * @param message log message
	 */
	public static void log(String user, String type, String ip, String message) {
		if (StringUtils.length(message) > MAX_LOG_MESSAGE_LENGTH) {
			message = message.substring(0, MAX_LOG_MESSAGE_LENGTH);
		}

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			Timestamp now = new Timestamp(new Date().getTime());
			conn = DbConnections.getArtDbConnection();
			String sql = "INSERT INTO ART_LOGS"
					+ " (LOG_DATE, USERNAME, LOG_TYPE, IP, MESSAGE) "
					+ " VALUES (?,?,?,?,?) ";
			ps = conn.prepareStatement(sql);

			ps.setTimestamp(1, now);
			ps.setString(2, user);
			ps.setString(3, type);
			ps.setString(4, ip);
			ps.setString(5, message);

			ps.executeUpdate();
		} catch (SQLException ex) {
			logger.error("Error", ex);
		} finally {
			DatabaseUtils.close(ps, conn);
		}
	}

}
