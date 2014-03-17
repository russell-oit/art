/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
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

import art.servlets.ArtConfig;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods for running queries against the art database
 *
 * @author Timothy Anyona
 */
@Service
public class DbService {

	private static final Logger logger = LoggerFactory.getLogger(DbService.class);

	/**
	 * Execute an sql statement that doesn't return a resultset
	 *
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int update(String sql, Object... params) throws SQLException {
		Connection conn = ArtConfig.getConnection();
		if (conn == null) {
			logger.warn("Connection to the ART Database not available");
			return 0;
		}

		try {
			QueryRunner run = new QueryRunner();
			return run.update(conn, sql, params);
		} finally {
			DbUtils.close(conn);
		}
	}

	/**
	 * Execute a select statement with parameters and fill an appropriate object
	 * or object list
	 *
	 * @param <T>
	 * @param sql
	 * @param rsh
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		Connection conn = ArtConfig.getConnection();
		if (conn == null) {
			logger.warn("Connection to the ART Database not available");
			return null;
		}

		try {
			QueryRunner run = new QueryRunner();
			return run.query(conn, sql, rsh, params);
		} finally {
			DbUtils.close(conn);
		}
	}

}
