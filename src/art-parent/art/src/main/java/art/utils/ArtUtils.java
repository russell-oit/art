/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package art.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for utility methods
 * 
 * @author Timothy Anyona
 */
public class ArtUtils {
	final static Logger logger = LoggerFactory.getLogger(ArtUtils.class);
	
	/**
	 * Close resultset
	 *
	 * @param rs
	 */
	public static void closeResultSet(ResultSet rs) {
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
	public static void closeStatement(Statement st) {
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
	public static void closeConnection(Connection conn) {
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
		closeResultSet(rs);
		closeStatement(st);
		closeConnection(conn);
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
}
