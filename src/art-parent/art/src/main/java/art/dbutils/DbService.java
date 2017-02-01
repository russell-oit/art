/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.dbutils;

import art.connectionpool.DbConnections;
import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
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
	 * @param sql the sql to execute
	 * @param params the parameters to use
	 * @return number of records affected
	 * @throws SQLException
	 */
	public int update(String sql, Object... params) throws SQLException {
		QueryRunner run = new QueryRunner(DbConnections.getArtDbDataSource());
		return run.update(sql, params);
	}

	/**
	 * Executes a SELECT query against the art database and returns an
	 * appropriate, populated object
	 *
	 * @param <T> the type of object that the handler returns
	 * @param sql the sql to execute
	 * @param rsh the handler that converts the results into an object
	 * @param params the parameters to use
	 * @return the object returned by the handler
	 * @throws SQLException
	 */
	public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
		QueryRunner run = new QueryRunner(DbConnections.getArtDbDataSource());
		return run.query(sql, rsh, params);
	}

	/**
	 * Executes a batch of INSERT, UPDATE, or DELETE queries against the art
	 * database
	 *
	 * @param sql the sql to execute
	 * @param params the array of query replacement parameters. Each row in this
	 * array is one set of batch replacement values.
	 * @return the number of rows updated per statement
	 * @throws SQLException
	 */
	public int[] batch(String sql, Object[][] params) throws SQLException {
		QueryRunner run = new QueryRunner(DbConnections.getArtDbDataSource());
		return run.batch(sql, params);
	}
}
