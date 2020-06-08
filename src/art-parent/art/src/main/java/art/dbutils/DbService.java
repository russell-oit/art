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

import art.connectionpool.DbConnections;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
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

	private static final Logger logger = LoggerFactory.getLogger(DbService.class);

	/**
	 * Executes an INSERT, UPDATE or DELETE query against the art database
	 *
	 * @param sql the sql to execute
	 * @param params the parameters to use
	 * @return number of records affected
	 * @throws SQLException
	 */
	public int update(String sql, Object... params) throws SQLException {
		Connection conn = null;
		return update(conn, sql, params);
	}

	/**
	 * Executes an INSERT, UPDATE or DELETE query against a database
	 *
	 * @param conn the connection to the database. if null, the art database
	 * will be used
	 * @param sql the sql to execute
	 * @param params the parameters to use
	 * @return number of records affected
	 * @throws SQLException
	 */
	public int update(Connection conn, String sql, Object... params) throws SQLException {
		if (conn == null) {
			QueryRunner run = new QueryRunner(DbConnections.getArtDbDataSource());
			return run.update(sql, params);
		} else {
			QueryRunner run = new QueryRunner();
			return run.update(conn, sql, params);
		}
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
		Connection conn = null;
		return query(conn, sql, rsh, params);
	}

	/**
	 * Executes a SELECT query against a database and returns an appropriate,
	 * populated object
	 *
	 * @param <T> the type of object that the handler returns
	 * @param conn the connection to the database. if null, the art database
	 * will be used.
	 * @param sql the sql to execute
	 * @param rsh the handler that converts the results into an object
	 * @param params the parameters to use
	 * @return the object returned by the handler
	 * @throws SQLException
	 */
	public <T> T query(Connection conn, String sql, ResultSetHandler<T> rsh,
			Object... params) throws SQLException {

		if (conn == null) {
			QueryRunner run = new QueryRunner(DbConnections.getArtDbDataSource());
			return run.query(sql, rsh, params);
		} else {
			QueryRunner run = new QueryRunner();
			return run.query(conn, sql, rsh, params);
		}
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

	/**
	 * Returns the max record id for a table in the art database
	 *
	 * @param sql the sql to get the current max id for the table
	 * @param params query parameters
	 * @return the max record id
	 * @throws SQLException
	 */
	public int getMaxRecordId(String sql, Object... params) throws SQLException {
		Connection conn = null;
		return getMaxRecordId(conn, sql, params);
	}

	/**
	 * Returns the max record id for a table in a given database
	 *
	 * @param conn the connection to the database. if null the art database is
	 * used
	 * @param sql the sql to get the current max id for the table
	 * @param params query parameters
	 * @return the max record id
	 * @throws SQLException
	 */
	public int getMaxRecordId(Connection conn, String sql, Object... params) throws SQLException {
		//use Number rather than Integer because oracle returns a java.math.BigDecimal object
		//oracle doesn't have an "INTEGER" data type. When INTEGER is specified, it's stored as NUMBER(38,0)
		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number maxIdNumber = query(conn, sql, h, params);
		logger.debug("maxIdNumber={}", maxIdNumber);

		int maxIdInt;
		if (maxIdNumber == null) {
			//no records in the table
			maxIdInt = 0;
		} else {
			maxIdInt = maxIdNumber.intValue();
		}

		if (maxIdInt < 0) {
			//only hardcoded records present in the table
			maxIdInt = 0;
		}

		return maxIdInt;
	}

	/**
	 * Returns a new record id for a table in the art database
	 *
	 * @param sql the sql query to get the current max id for the table
	 * @param params parameters to be used in the query
	 * @return the new record id
	 * @throws SQLException
	 */
	public int getNewRecordId(String sql, Object... params) throws SQLException {
		int maxId = getMaxRecordId(sql, params);
		int newId = maxId + 1;
		return newId;
	}

}
