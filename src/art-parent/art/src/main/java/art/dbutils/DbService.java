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

import art.datasource.Datasource;
import art.datasource.DatasourceMapper;
import art.servlets.ArtConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
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
	 * @param values
	 * @return
	 * @throws SQLException
	 */
	public int update(String sql, Object... values) throws SQLException {
		int affectedRows = 0;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();
			affectedRows = DbUtils.update(conn, ps, sql, values);
		} finally {
			DbUtils.close(ps, conn);
		}

		return affectedRows;
	}

	public <T> T query(String sql, ResultSetHandler<T> rsh) throws SQLException {
		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();
			QueryRunner run = new QueryRunner();

			return run.query(conn, sql, rsh);
		} finally {
			DbUtils.close(conn);
		}
	}

}
