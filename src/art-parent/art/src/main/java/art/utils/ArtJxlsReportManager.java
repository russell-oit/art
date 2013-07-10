package art.utils;

import art.servlets.ArtDBCP;
import java.sql.*;
import java.util.*;
import net.sf.jxls.report.ReportManager;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that implements the jxls ReportManager interface. Based on the code in
 * the jxls ReportManagerImpl class
 *
 * @author Timothy Anyona
 */
public class ArtJxlsReportManager implements ReportManager {

	final static Logger logger = LoggerFactory.getLogger(ArtJxlsReportManager.class);
	Connection conn;

	/**
	 *
	 * @param c
	 */
	public ArtJxlsReportManager(Connection c) {
		conn = c;
	}

	/**
	 *
	 * @param sql
	 * @return rows that have been returned by the query
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List exec(String sql) throws SQLException {
		List rows = null;

		if (conn == null) {
			logger.warn("No database connection");
		} else {
			Statement stmt = conn.createStatement();
			sql = sql.replaceAll("&apos;", "'");
			try {
				ResultSet rs = stmt.executeQuery(sql);
				RowSetDynaClass rsdc = new RowSetDynaClass(rs, false, true); //use lowercase properties = false, use column labels =true
				rs.close();
				rows = rsdc.getRows();
			} finally {
				stmt.close();
			}
		}

		return rows;
	}

	/**
	 * Overload that takes a connection
	 *
	 * @param c connection to target datasource
	 * @param sql sql to execute
	 * @return rows that have been returned by the query
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	public List exec(Connection c, String sql) throws SQLException {
		conn = c;
		return exec(sql);
	}

	/**
	 * Allow exec to take datasource id
	 *
	 * @param datasourceId datasource id
	 * @param sql
	 * @return rows that have been returned by the query
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	public List exec(int datasourceId, String sql) throws SQLException {
		List rows = null;

		//save existing connection so that it can be restored after exec is finished
		Connection connOriginal = conn;
		Connection connQuery = null;

		try {
			//get connection and use it to execute the given sql
			connQuery = ArtDBCP.getConnection(datasourceId);
			if (connQuery == null) {
				logger.warn("Could not get database connection to datasource {}", datasourceId);
			} else {
				rows = exec(connQuery, sql);
			}
		} finally {
			try {
				//restore original connnection. may be used by other calls to rm.exec that don't specify the datasource
				conn = connOriginal;

				if (connQuery != null) {
					connQuery.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return rows;
	}

	/**
	 * Allow exec to take datasource name
	 *
	 * @param datasourceName datasource name
	 * @param sql
	 * @return rows that have been returned by the query
	 * @throws SQLException
	 */
	@SuppressWarnings("rawtypes")
	public List exec(String datasourceName, String sql) throws SQLException {
		List rows = null;

		//save existing connection so that it can be restored after exec is finished
		Connection connOriginal = conn;
		Connection connQuery = null;

		try {
			//get connection and use it to execute the given sql
			connQuery = ArtDBCP.getConnection(datasourceName);
			if (connQuery == null) {
				logger.warn("Could not get database connection to datasource {}", datasourceName);
			} else {
				rows = exec(connQuery, sql);
			}
		} finally {
			try {
				//restore original connnection. may be used by other calls to rm.exec that don't specify the datasource
				conn = connOriginal;

				if (connQuery != null) {
					connQuery.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return rows;
	}
}