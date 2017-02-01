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
package art.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reverse a resultset in a table. <br>
 * <pre>
 *  Usage
 *
 * CachedResult cr = new CachedResult();
 * cr.setTargetConnection(targetConnection);
 * cr.setResultset(rs);
 * cr.setCachedTableName(tableName);
 * cr.setMode(1|2); // 1 = append 2 = truncate/insert (3 = update (not implemented))
 * cr.cacheIt();
 *
 * List l = cr.getColumnsNames();
 *
 * Usage to delete an existing cached table
 * CachedResult cr = new CachedResult();
 * cr.setTargetConnection(targetConnection);
 * cr.setCachedTableName(tableName);
 * cr.drop();
 * </pre>
 *
 * @author Enrico Liboni
 */
public class CachedResult {

	private static final Logger logger = LoggerFactory.getLogger(CachedResult.class);
	private Connection conn; //target connection
	private ResultSet rs; // resultset to reverse in the target connection
	private String tableName;
	private int cacheMode; // 1 = append 2 = drop/insert
	private int rowsCount = 0;
	private final int BATCH_EXECUTE_SIZE = 500; // states after how many inserts the batch is executed
	private List<String> columnsNameList;

	/**
	 *
	 */
	public CachedResult() {
	}

	/* Setters
	 */
	/**
	 * Set where the new table will be created/data inserted (cached)
	 *
	 * @param c
	 */
	public void setTargetConnection(Connection c) {
		conn = c;
	}

	/**
	 * Set the name of the database table where data is cached in the target
	 * connection If the table does not exists it is recreated otherwise it will
	 * attempt to insert the values in the existing table. To avoid problems
	 * with special chars or case sensitive platforms the name is parsed and set
	 * to uppercase.
	 *
	 * @param s
	 */
	public void setCachedTableName(String s) {
		tableName = parseString(s).toUpperCase();
	}

	/**
	 * Set the resultset to cache
	 *
	 * @param r
	 */
	public void setResultSet(ResultSet r) {
		rs = r;
	}

	/**
	 * Set cache mode (1= append / 2= truncate&insert)
	 *
	 * @param i
	 */
	public void setCacheMode(int i) {
		cacheMode = i;
	}

	/* Getters
	 */
	/**
	 * Returns the cached table name
	 *
	 * @return the cached table name
	 */
	public String getCachedTableName() {
		return tableName;
	}

	/**
	 * Returns a List of cached table columns names
	 *
	 * @return List of cached table columns names
	 */
	public List<String> getCachedTableColumnList() {
		return columnsNameList;
	}

	/**
	 * Returns the cached table columns names in a string separated by a blank
	 *
	 * @return the cached table columns names in a string separated by a blank
	 */
	public String getCachedTableColumnsName() {
		StringBuilder sb = new StringBuilder(50);
		for (String column : columnsNameList) {
			sb.append(" ").append(column);
		}
		return sb.toString();
	}

	/**
	 * Returns the number of rows inserted in the cached table
	 *
	 * @return the number of rows inserted in the cached table
	 */
	public int getRowsCount() {
		return rowsCount;
	}

	/**
	 * Cache the result
	 *
	 * @throws SQLException
	 */
	public void cacheIt() throws SQLException {
		try {
			//check if the table exist in the target database (if yes assume it is correct)
			DatabaseMetaData dbmd = conn.getMetaData();
			ResultSet dbrs = dbmd.getTables(null, null, tableName, null); // note some rdbms might be case sensitive

			if (!dbrs.next()) {
				// table does not exist, let's create it
				createTable();
			}
			dbrs.close();
			if (cacheMode == 2) { // delete/insert:let's delete the content
				logger.debug("CACHED Table: {} deleting...", tableName);

				String sql = "DELETE FROM " + tableName; // truncate table is faster but might not work on all databases! Currently is not supported by hsqldb1.8
				Statement st = conn.createStatement();
				st.executeUpdate(sql);
				st.close();
			}
			// reverse the resultset into the table
			insertData();
		} catch (SQLException e) {
			// log and throw the exception
			logger.error("Error", e);
			throw e;
		}
	}

	/**
	 * Delete the cached table
	 */
	public void drop() {
		try {
			logger.debug("CACHED Table: {} dropping...", tableName);

			String sql = "drop table " + tableName;
			Statement st = conn.createStatement();
			st.executeUpdate(sql);
			st.close();
		} catch (SQLException e) {
			logger.warn("Table not dropped: {}", tableName, e); // it is not an error... 
		}
	}

	/**
	 * Create the cache table in the target database
	 */
	private void createTable() throws SQLException {
		StringBuilder createStatementSB = new StringBuilder(100);
		createStatementSB.append("CREATE TABLE ").append(tableName).append(" ( ");
		ResultSetMetaData rsmd = rs.getMetaData();
		int numberOfColumns = rsmd.getColumnCount();
		int j, p, s;
		for (j = 1; j <= numberOfColumns; j++) {
			String columnName = parseString(rsmd.getColumnLabel(j));
			int colType = rsmd.getColumnType(j);
			switch (colType) {
				case Types.CHAR:
				case Types.VARCHAR:
				case Types.LONGVARCHAR:
					createStatementSB.append(columnName).append(" VARCHAR(").append(rsmd.getPrecision(j)).append("), ");
					break;
				case Types.TINYINT:
				case Types.SMALLINT:
				case Types.INTEGER:
					createStatementSB.append(columnName).append(" INTEGER, ");
					break;
				case Types.FLOAT:
				case Types.REAL:
				case Types.DOUBLE:
					createStatementSB.append(columnName).append(" DOUBLE, "); // this might not work in some databses, i.e. oracle maps to numeric
					break;
				case Types.DECIMAL:
				case Types.NUMERIC:
					p = rsmd.getPrecision(j);
					s = rsmd.getScale(j);
					if (p < 1 || s < 0) { // handle buggy drivers
						createStatementSB.append(columnName).append(" NUMERIC, "); // use default
					} else {
						createStatementSB.append(columnName).append(" NUMERIC(").append(p).append(",").append(s).append("), ");
					}
					break;
				case Types.BIGINT:
					createStatementSB.append(columnName).append(" BIGINT, ");
					break;
				case Types.DATE:
					createStatementSB.append(columnName).append(" DATE, ");
					break;
				case Types.TIMESTAMP:
					createStatementSB.append(columnName).append(" TIMESTAMP, ");
					break;
				case Types.BOOLEAN:
				case Types.BIT:
					createStatementSB.append(columnName).append(" BOOLEAN, ");
					break;
				default:
					logger.debug("Unable to define type for column {} . Defaulting to varchar", rsmd.getColumnLabel(j));
					createStatementSB.append(columnName).append(" VARCHAR(1024), ");
			}
			logger.debug("Column: {} is type: {}", columnName, colType);
		}
		createStatementSB.append(" ART_FILTER VARCHAR(255) )"); // additional column for custom filtering...	
		logger.debug("CACHED Table: {} creation statement:\n {}", tableName, createStatementSB.toString());

		// Create the table
		Statement st = conn.createStatement();
		st.executeUpdate(createStatementSB.toString());
	}

	/**
	 * insert the data in rs
	 */
	private void insertData() throws SQLException {
		// Build the insert prepared statement

		columnsNameList = new ArrayList<String>();
		StringBuilder insertPreparedStatementSB = new StringBuilder(100);
		StringBuilder preparedStatementQuestionMarksSB = new StringBuilder();
		insertPreparedStatementSB.append("INSERT INTO ").append(tableName).append(" ( ");
		preparedStatementQuestionMarksSB.append("( ");
		ResultSetMetaData rsmd = rs.getMetaData();
		int numberOfColumns = rsmd.getColumnCount();
		int j;
		for (j = 1; j <= numberOfColumns; j++) {
			String columnName = parseString(rsmd.getColumnLabel(j));
			columnsNameList.add(columnName);
			insertPreparedStatementSB.append(j == 1 ? " " : ", ").append(columnName);
			preparedStatementQuestionMarksSB.append((j == 1 ? " ?" : ", ?"));
		}
		preparedStatementQuestionMarksSB.append(", '-' ) "); // default value for custom filter
		insertPreparedStatementSB.append(", ART_FILTER ) values ").append(preparedStatementQuestionMarksSB.toString());
		logger.debug("CACHED Table: {} insert prepared statement:\n {}", tableName, insertPreparedStatementSB.toString());

		// Feed the values in batches
		PreparedStatement ps = conn.prepareStatement(insertPreparedStatementSB.toString());
		while (rs.next()) {
			rowsCount++;
			for (j = 1; j <= numberOfColumns; j++) {
				// use same switch as before...
				Object o = rs.getObject(j);
				int colType = rsmd.getColumnType(j);
				switch (colType) {
					case Types.CHAR:
					case Types.VARCHAR:
					case Types.LONGVARCHAR:
						if (o == null) {
							ps.setNull(j, Types.VARCHAR);
						} else {
							ps.setString(j, rs.getString(j));
						}
						break;
					case Types.TINYINT:
					case Types.SMALLINT:
					case Types.INTEGER:
						if (o == null) {
							ps.setNull(j, Types.INTEGER);
						} else {
							ps.setInt(j, rs.getInt(j));
						}
						break;
					case Types.FLOAT: // maps to Java Double
					case Types.REAL:  // maps to Java Float
					case Types.DOUBLE: // maps to Java Double
						if (o == null) {
							ps.setNull(j, Types.DOUBLE);
						} else {
							ps.setDouble(j, rs.getDouble(j));
						}
						break;
					case Types.NUMERIC:
					case Types.DECIMAL:
						if (o == null) {
							ps.setNull(j, Types.NUMERIC);
						} else {
							ps.setBigDecimal(j, rs.getBigDecimal(j));
						}
						break;
					case Types.BIGINT:
						if (o == null) {
							ps.setNull(j, Types.BIGINT);
						} else {
							ps.setLong(j, rs.getLong(j));
						}
						break;
					case Types.DATE:
						if (o == null) {
							ps.setNull(j, Types.DATE);
						} else {
							ps.setDate(j, rs.getDate(j));
						}
						break;
					case Types.TIMESTAMP:
						if (o == null) {
							ps.setNull(j, Types.TIMESTAMP);
						} else {
							ps.setTimestamp(j, rs.getTimestamp(j));
						}
						break;
					case Types.BOOLEAN:
					case Types.BIT:
						if (o == null) {
							ps.setNull(j, Types.BOOLEAN);
						} else {
							ps.setBoolean(j, rs.getBoolean(j));
						}
						break;
					default:
						if (o == null) {
							ps.setNull(j, Types.VARCHAR);
						} else {
							String val = o.toString();
							if (val.length() > 1024) {
								val = val.substring(1, 1024);
							}
							ps.setString(j, val);
						}
				}
			}
			ps.addBatch();
			if (rowsCount % BATCH_EXECUTE_SIZE == 0) {
				ps.executeBatch();
			} // 
		}
		ps.executeBatch();
		ps.close();
	}

	/**
	 * Remove special chars and replace with _
	 */
	private String parseString(String s) {
		return s.replace(" ", "_").replace(",", "_").replace("(", "_").replace(")", "_")
				.replace("'", "_").replace("*", "_").replace("#", "_").replace("-", "_");
	}
}
