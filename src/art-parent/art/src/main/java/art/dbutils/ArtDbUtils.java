/*
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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides jdbc helper methods
 *
 * @author Timothy Anyona
 */
public class ArtDbUtils {

	private static final Logger logger = LoggerFactory.getLogger(ArtDbUtils.class);

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
	 * Close resultset and connection
	 *
	 * @param rs
	 * @param conn
	 */
	public static void close(ResultSet rs, Connection conn) {
		close(rs, null, conn);
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
			if (values[i] == null) {
				logger.warn("non-typed null value passed. Driver may throw an exception");
			}
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
		} else {
			return new java.sql.Date(date.getTime());
		}
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
		} else {
			return new java.sql.Timestamp(date.getTime());
		}
	}

	/**
	 * Gets the current time as a java.sql.Timestamp
	 *
	 * @return
	 */
	public static java.sql.Timestamp getCurrentTimeAsSqlTimestamp() {
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
	 * @throws NullPointerException if conn is null
	 */
	public static ResultSet query(Connection conn, PreparedStatement ps, String sql, Object... values) throws SQLException {
		Objects.requireNonNull(conn, "Connection must not be null");

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
	 * 
	 * @param rs
	 * @param columnName
	 * @return
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
