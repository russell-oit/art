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
package art.output;

import art.connectionpool.DbConnections;
import art.dbutils.DatabaseUtils;
import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jxls.common.JxlsException;
import org.jxls.jdbc.CaseInsensitiveHashMap;

/**
 * Jxls jdbc helper that allows specifying of a datasource for a query
 *
 * @author Timothy Anyona
 */
public class ArtJxlsJdbcHelper {

	//code from jxls jdbc helper
	//https://bitbucket.org/leonate/jxls/src
	private Connection conn;

	public ArtJxlsJdbcHelper(Connection conn) {
		this.conn = conn;
	}

	public ArtJxlsJdbcHelper() {

	}

	public List<Map<String, Object>> query(String sql, Object... params) {
		List<Map<String, Object>> result;
		if (conn == null) {
			throw new JxlsException("Null jdbc connection");
		}

		if (sql == null) {
			throw new JxlsException("Null SQL statement");
		}

		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			fillStatement(stmt, params);
			try (ResultSet rs = stmt.executeQuery()) {
				result = handle(rs);
			}
		} catch (Exception e) {
			throw new JxlsException("Failed to execute sql", e);
		}

		return result;
	}

	/*
     * The implementation is a slightly modified version of a similar method of AbstractQueryRunner in Apache DBUtils
	 */
	private void fillStatement(PreparedStatement stmt, Object[] params) throws SQLException {
		// nothing to do here
		if (params == null) {
			return;
		}

		// check the parameter count, if we can
		ParameterMetaData pmd = null;
		boolean pmdKnownBroken = false;

		int stmtCount = 0;
		int paramsCount = 0;
		try {
			pmd = stmt.getParameterMetaData();
			stmtCount = pmd.getParameterCount();
			paramsCount = params.length;
		} catch (Exception e) {
			pmdKnownBroken = true;
		}

		if (stmtCount != paramsCount) {
			throw new SQLException("Wrong number of parameters: expected "
					+ stmtCount + ", was given " + paramsCount);
		}

		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {
				stmt.setObject(i + 1, params[i]);
			} else {
				// VARCHAR works with many drivers regardless
				// of the actual column type. Oddly, NULL and
				// OTHER don't work with Oracle's drivers.
				int sqlType = Types.VARCHAR;
				if (!pmdKnownBroken) {
					try {
						/*
                         * It's not possible for pmdKnownBroken to change from
                         * true to false, (once true, always true) so pmd cannot
                         * be null here.
						 */
						sqlType = pmd.getParameterType(i + 1);
					} catch (SQLException e) {
						pmdKnownBroken = true;
					}
				}
				stmt.setNull(i + 1, sqlType);
			}
		}
	}

	public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
		List<Map<String, Object>> rows = new ArrayList<>();
		while (rs.next()) {
			rows.add(handleRow(rs));
		}
		return rows;
	}

	private Map<String, Object> handleRow(ResultSet rs) throws SQLException {
		Map<String, Object> result = new CaseInsensitiveHashMap();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();
		for (int i = 1; i <= cols; i++) {
			String columnName = rsmd.getColumnLabel(i);
			if (null == columnName || 0 == columnName.length()) {
				columnName = rsmd.getColumnName(i);
			}
			result.put(columnName, rs.getObject(i));
		}
		return result;
	}

	public List<Map<String, Object>> query2(int datasourceId, String sql, Object... params) {
		List<Map<String, Object>> result;

		//save existing connection so that it can be restored after exec is finished
		Connection connOriginal = conn;
		Connection connQuery = null;

		try {
			//get connection and use it to execute the given sql
			connQuery = DbConnections.getConnection(datasourceId);
			conn = connQuery;
			result = query(sql, params);
		} catch (SQLException ex) {
			throw new JxlsException(ex);
		} finally {
			//restore original connnection. may be used by other calls to query that don't specify the datasource
			conn = connOriginal;
			DatabaseUtils.close(connQuery);
		}

		return result;
	}
	
	public List<Map<String, Object>> query2(String datasourceName, String sql, Object... params) {
		//use different method name to avoid errors with wrong method being called with query(String, Object...)
		List<Map<String, Object>> result;

		//save existing connection so that it can be restored after exec is finished
		Connection connOriginal = conn;
		Connection connQuery = null;

		try {
			//get connection and use it to execute the given sql
			connQuery = DbConnections.getConnection(datasourceName);
			conn = connQuery;
			result = query(sql, params);
		} catch (SQLException ex) {
			throw new JxlsException(ex);
		} finally {
			//restore original connnection. may be used by other calls to query that don't specify the datasource
			conn = connOriginal;
			DatabaseUtils.close(connQuery);
		}

		return result;
	}

}
