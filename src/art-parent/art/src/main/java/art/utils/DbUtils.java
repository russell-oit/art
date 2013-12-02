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

import java.sql.Connection;
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
	final static Logger logger = LoggerFactory.getLogger(DbUtils.class);

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
				ArtUtils.logger.error("Error", ex);
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
				ArtUtils.logger.error("Error", ex);
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
				ArtUtils.logger.error("Error", ex);
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
	
}
