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
package art.dbutils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides jdbc helper methods
 *
 * @author Timothy Anyona
 */
public class DatabaseUtils {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseUtils.class);

	/**
	 * Closes a resultset
	 *
	 * @param rs the resultset to close
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
	 * Closes a statement
	 *
	 * @param st the statement to close
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
	 * Closes a connection
	 *
	 * @param conn the connection to close
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
	 * Closes a resultset, statement and connection
	 *
	 * @param rs the resultset to close
	 * @param st the statement to close
	 * @param conn the connection to close
	 */
	public static void close(ResultSet rs, Statement st, Connection conn) {
		close(rs);
		close(st);
		close(conn);
	}

	/**
	 * Closes a statement and connection
	 *
	 * @param st the statement to close
	 * @param conn the connection to close
	 */
	public static void close(Statement st, Connection conn) {
		close(null, st, conn);
	}

	/**
	 * Closes a resultset and connection
	 *
	 * @param rs the resultset to close
	 * @param conn the connection to close
	 */
	public static void close(ResultSet rs, Connection conn) {
		close(rs, null, conn);
	}

	/**
	 * Closes a resultset and statement
	 *
	 * @param rs the resultset to close
	 * @param st the statement to close
	 */
	public static void close(ResultSet rs, Statement st) {
		close(rs, st, null);
	}

	/**
	 * Sets the given parameter values in the given PreparedStatement.
	 *
	 * @param ps The PreparedStatement to set the given parameter values in.
	 * @param values The parameter values to be set in the created
	 * PreparedStatement.
	 * @throws SQLException If something fails during setting the
	 * PreparedStatement values.
	 */
	public static void setValues(PreparedStatement ps, Object... values) throws SQLException {
		//https://stackoverflow.com/questions/39589879/printing-an-array-with-slf4j-only-prints-the-first-element
		logger.debug("values = {}", (Object) values);

		boolean nullValueExists = false;
		for (int i = 0; i < values.length; i++) {
			if (values[i] == null) {
				nullValueExists = true;
			}
			ps.setObject(i + 1, values[i]);
		}

		if (nullValueExists) {
			logger.debug("non-typed null value passed. Driver may throw an exception.");
		}
	}

	/**
	 * Converts the given java.util.Date to a java.sql.Date
	 *
	 * @param date The java.util.Date to be converted, may be null
	 * @return The converted java.sql.Date. null if date passed was null
	 */
	public static java.sql.Date toSqlDate(java.util.Date date) {
		if (date == null) {
			return null;
		} else {
			return new java.sql.Date(date.getTime());
		}
	}

	/**
	 * Converts the given java.util.Date to a java.sql.Time
	 *
	 * @param time the java.util.Date to be converted, may be null
	 * @return the converted java.sql.Time. null if time passed was null
	 */
	public static Time toSqlTime(java.util.Date time) {
		if (time == null) {
			return null;
		} else {
			return new Time(time.getTime());
		}
	}

	/**
	 * Converts the given java.util.Date to a java.sql.Timestamp
	 *
	 * @param date The java.util.Date to be converted, may be null
	 * @return The converted java.sql.Timestamp. null if date passed was null
	 */
	public static java.sql.Timestamp toSqlTimestamp(java.util.Date date) {
		if (date == null) {
			return null;
		} else {
			return new java.sql.Timestamp(date.getTime());
		}
	}

	/**
	 * Gets the current time as a java.sql.Timestamp
	 *
	 * @return the current time as a java.sql.Timestamp
	 */
	public static java.sql.Timestamp getCurrentTimeAsSqlTimestamp() {
		return new java.sql.Timestamp(System.currentTimeMillis());
	}

	/**
	 * Executes an sql statement that returns a resultset
	 *
	 * @param conn the connection to use, not null
	 * @param ps the preparedstatement to use
	 * @param sql the sql to execute
	 * @param values the parameters to be used in the sql statement
	 * @return the resultset generated after executing the sql
	 * @throws SQLException
	 * @throws NullPointerException if conn is null
	 */
	public static ResultSet query(Connection conn, PreparedStatement ps, String sql, Object... values) throws SQLException {
		Objects.requireNonNull(conn, "conn must not be null");

		ps = conn.prepareStatement(sql);
		setValues(ps, values);

		return ps.executeQuery();
	}

	/**
	 * Executes an sql statement that doesn't return a resultset
	 *
	 * @param conn
	 * @param ps
	 * @param sql
	 * @param values
	 * @return
	 * @throws SQLException
	 * @throws NullPointerException if conn is null
	 */
//	public static int update(Connection conn, PreparedStatement ps, String sql, Object... values) throws SQLException {
//		Objects.requireNonNull(conn, "Connection must not be null");
//
//		ps = conn.prepareStatement(sql);
//		setValues(ps, values);
//
//		return ps.executeUpdate();
//	}
	/**
	 * Returns <code>true</code> if the given resultset contains a column with
	 * the given name
	 *
	 * @param rs the resultset to use
	 * @param columnName the column name to look for
	 * @return <code>true</code> if the given resultset contains a column with
	 * the given name
	 * @throws SQLException
	 */
	public static boolean ResultSetHasColumn(ResultSet rs, String columnName) throws SQLException {
		//https://stackoverflow.com/questions/3599861/how-can-i-determine-if-the-column-name-exist-in-the-resultset
		//http://pure-essence.net/2011/05/12/spring-check-if-a-column-exist-using-resultset/

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		for (int i = 1; i <= columnCount; i++) { //rsmd column indices start from 1, not 0
			//use getColumnLabel() to compare with column alias
			//use getColumnName() to compare with column name
			//don't use equalsIgnoreCase because rs.getXXX("columnName") methods
			//may be case sensitive depending on the database and server
			if (StringUtils.equals(rsmd.getColumnLabel(i), columnName)) {
				return true;
			}
		}
		return false;
	}
}
