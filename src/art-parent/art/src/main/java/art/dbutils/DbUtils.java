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
package art.dbutils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for utility methods related to databases
 *
 * @author Timothy Anyona
 */
public class DbUtils {

	private static final Logger logger = LoggerFactory.getLogger(DbUtils.class);

	/**
	 * Close resultset
	 *
	 * @param rs
	 */
	public static void close(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				logger.error("Error", ex);
			}
		}
	}

	/**
	 * Close statement
	 *
	 * @param st
	 */
	public static void close(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException ex) {
				logger.error("Error", ex);
			}
		}
	}

	/**
	 * Close connection
	 *
	 * @param conn
	 */
	public static void close(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException ex) {
				logger.error("Error", ex);
			}
		}
	}

	/**
	 * Close resultset, statement and connection
	 *
	 * @param rs
	 * @param st
	 * @param conn
	 */
	public static void close(ResultSet rs, Statement st, Connection conn) {
		close(rs);
		close(st);
		close(conn);
	}

	/**
	 * Close statement and connection
	 *
	 * @param st
	 * @param conn
	 */
	public static void close(Statement st, Connection conn) {
		close(null, st, conn);
	}

	/**
	 * Close resultset and statement
	 *
	 * @param rs
	 * @param st
	 */
	public static void close(ResultSet rs, Statement st) {
		close(rs, st, null);
	}

	/**
	 * Set the given parameter values in the given PreparedStatement.
	 *
	 * @param ps The PreparedStatement to set the given parameter values in.
	 * @param values The parameter values to be set in the created
	 * PreparedStatement.
	 * @throws SQLException If something fails during setting the
	 * PreparedStatement values.
	 */
	public static void setValues(PreparedStatement ps, Object... values) throws SQLException {
		for (int i = 0; i < values.length; i++) {
			ps.setObject(i + 1, values[i]);
		}
	}

	/**
	 * Converts the given java.util.Date to java.sql.Date
	 *
	 * @param date The java.util.Date to be converted to java.sql.Date.
	 * @return The converted java.sql.Date.
	 */
	public static java.sql.Date toSqlDate(java.util.Date date) {
		if (date == null) {
			return null;
		}

		return new java.sql.Date(date.getTime());
	}

	/**
	 * Converts the given java.util.Date to java.sql.Timestamp
	 *
	 * @param date The java.util.Date to be converted to java.sql.Date.
	 * @return The converted java.sql.Timestamp
	 */
	public static java.sql.Timestamp toSqlTimestamp(java.util.Date date) {
		if (date == null) {
			return null;
		}

		return new java.sql.Timestamp(date.getTime());
	}

	/**
	 * Gets the current time as a java.sql.Timestamp
	 *
	 * @return
	 */
	public static java.sql.Timestamp getCurrentTimeStamp() {
		return new java.sql.Timestamp(System.currentTimeMillis());
	}

	/**
	 * Execute an sql statement that returns a resultset
	 *
	 * @param conn
	 * @param ps
	 * @param sql
	 * @param values
	 * @return
	 * @throws SQLException
	 */
	public static ResultSet query(Connection conn, PreparedStatement ps, String sql, Object... values) throws SQLException {
		if (conn == null) {
			logger.warn("Connection not available");
			return null;
		}

		ps = conn.prepareStatement(sql);
		setValues(ps, values);

		return ps.executeQuery();
	}

	/**
	 * Execute an sql statement that doesn't return a resultset
	 *
	 * @param conn
	 * @param ps
	 * @param sql
	 * @param values
	 * @return
	 * @throws SQLException
	 */
	public static int update(Connection conn, PreparedStatement ps, String sql, Object... values) throws SQLException {
		if (conn == null) {
			logger.warn("Connection not available");
			return 0;
		}

		ps = conn.prepareStatement(sql);
		setValues(ps, values);

		return ps.executeUpdate();
	}

}
