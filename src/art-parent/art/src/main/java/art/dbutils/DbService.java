/*
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

import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Provides methods for running sql queries against the art database
 *
 * @author Timothy Anyona
 */
@Component
public class DbService {

	/**
	 * Executes an INSERT, UPDATE or DELETE query against the art database
	 *
	 * @param sql
	 * @param params
	 * @return number of records affected
	 * @throws SQLException
	 */
	public int update(String sql, Object... params) throws SQLException {
		QueryRunner run = new QueryRunner(DbConnections.getArtDbConnectionPool());
		return run.update(sql, params);
	}

	/**
	 * Executes a SELECT query against the art database and returns an
	 * appropriate, populated object
	 *
	 * @param <T> the type of object that the handler returns
	 * @param sql
	 * @param rsh the handler that converts the results into an object
	 * @param params
	 * @return the object returned by the handler
	 * @throws SQLException
	 */
	public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		QueryRunner run = new QueryRunner(DbConnections.getArtDbConnectionPool());
		return run.query(sql, rsh, params);
	}

	/**
	 * Executes a batch of INSERT, UPDATE, or DELETE queries against the art
	 * database
	 *
	 * @param sql
	 * @param params the array of query replacement parameters. Each row in this
	 * array is one set of batch replacement values.
	 * @return the number of rows updated per statement
	 * @throws SQLException
	 */
	public int[] batch(String sql, Object[][] params) throws SQLException {
		QueryRunner run = new QueryRunner(DbConnections.getArtDbConnectionPool());
		return run.batch(sql, params);
	}
}
