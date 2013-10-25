/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.servlets.ArtConfig;
import java.sql.*;
import java.text.Collator;
import java.util.*;
import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent art datasources and related utility methods
 *
 * @author Timothy Anyona
 */
public class Datasource {

	final static Logger logger = LoggerFactory.getLogger(Datasource.class);
	int datasourceId;
	String name = "";
	String driver = "";
	String url = "";
	String username = "";
	String password = "";
	int poolTimeout = 20;
	String testSql = "";
		private boolean active;

	/**
	 * Get the value of active
	 *
	 * @return the value of active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set the value of active
	 *
	 * @param active new value of active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}


	/**
	 *
	 */
	public Datasource() {
	}

	/**
	 *
	 * @return datasource id
	 */
	public int getDatasourceId() {
		return datasourceId;
	}

	/**
	 *
	 * @param value
	 */
	public void setDatasourceId(int value) {
		datasourceId = value;
	}

	/**
	 *
	 * @return datasource name
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 * @param value
	 */
	public void setName(String value) {
		name = value;
	}

	/**
	 *
	 * @return datasource driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 *
	 * @param value
	 */
	public void setDriver(String value) {
		driver = value;
	}

	/**
	 *
	 * @return datasource url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 *
	 * @param value
	 */
	public void setUrl(String value) {
		url = value;
	}

	/**
	 *
	 * @return datasource username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 *
	 * @param value
	 */
	public void setUsername(String value) {
		username = value;
	}

	/**
	 *
	 * @return datasource password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 *
	 * @param value
	 */
	public void setPassword(String value) {
		password = value;
	}

	/**
	 *
	 * @return connection pool timeout
	 */
	public int getPoolTimeout() {
		return poolTimeout;
	}

	/**
	 *
	 * @param value
	 */
	public void setPoolTimeout(int value) {
		poolTimeout = value;
	}

	/**
	 *
	 * @return test sql
	 */
	public String getTestSql() {
		return (testSql == null ? "" : testSql); // return empty string if null instead of "null", this might happen on datasources created with pre 2.0
	}

	/**
	 *
	 * @param value
	 */
	public void setTestSql(String value) {
		testSql = value;
	}

	/**
	 * Populate the object with details of the given datasource
	 *
	 * @param id
	 * @return <code>true</code> if successful
	 */
	public boolean load(int id) {
		boolean success = false;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql = "SELECT DATABASE_ID, NAME, DRIVER, URL, USERNAME, PASSWORD,"
					+ " POOL_TIMEOUT, TEST_SQL, ACTIVE_STATUS "
					+ " FROM ART_DATABASES "
					+ " WHERE DATABASE_ID = ?";

			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				datasourceId = rs.getInt("DATABASE_ID");
				name = rs.getString("NAME");
				driver = rs.getString("DRIVER");
				url = rs.getString("URL");
				username = rs.getString("USERNAME");
				password = rs.getString("PASSWORD");
				poolTimeout = rs.getInt("POOL_TIMEOUT");
				testSql = rs.getString("TEST_SQL");
				active=BooleanUtils.toBoolean(rs.getInt("ACTIVE"));
			}
			rs.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Insert new datasource
	 *
	 * @return <code>true</code> if successful
	 */
	public boolean insert() {
		boolean success = false;

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();

			java.util.Date now = new java.util.Date();
			java.sql.Date sysdate = new java.sql.Date(now.getTime());

			// get new datasource id
			String sql = "SELECT MAX(DATABASE_ID) FROM ART_DATABASES";
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			datasourceId = 1; // set to 1 if no datasources exist
			if (rs.next()) {
				datasourceId = rs.getInt(1) + 1;
			}
			rs.close();
			ps.close();

			//insert new datasource
			sql = "INSERT INTO ART_DATABASES (DATABASE_ID, NAME, DRIVER, URL,"
					+ " USERNAME, PASSWORD, POOL_TIMEOUT, TEST_SQL,"
					+ " UPDATE_DATE, ACTIVE) "
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			ps = conn.prepareStatement(sql);
			ps.setInt(1, datasourceId);
			ps.setString(2, name);
			ps.setString(3, driver);
			ps.setString(4, url);
			ps.setString(5, username);
			ps.setString(6, password);
			ps.setInt(7, poolTimeout);
			ps.setString(8, testSql);
			ps.setDate(9, sysdate);
			ps.setInt(10, BooleanUtils.toInteger(active));

			ps.executeUpdate();
			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Delete a datasource
	 *
	 * @param id
	 * @return <code>true</code> if successful
	 */
	public boolean delete(int id) {
		boolean success = false;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql = "DELETE FROM ART_DATABASES "
					+ " WHERE DATABASE_ID = ? ";

			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);

			ps.executeUpdate();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Update datasource
	 *
	 * @return <code>true</code> if successful
	 */
	public boolean update() {
		boolean success = false;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;

			java.util.Date now = new java.util.Date();
			java.sql.Date sysdate = new java.sql.Date(now.getTime());

			sql = "UPDATE ART_DATABASES SET NAME = ?, DRIVER = ?, URL = ?"
					+ " ,USERNAME = ?, PASSWORD = ?, POOL_TIMEOUT=?, TEST_SQL=?,"
					+ " UPDATE_DATE=?, ACTIVE=?"
					+ " WHERE DATABASE_ID = ? ";
			ps = conn.prepareStatement(sql);

			ps.setString(1, name);
			ps.setString(2, driver);
			ps.setString(3, url);
			ps.setString(4, username);
			ps.setString(5, password);
			ps.setInt(6, poolTimeout);
			ps.setString(7, testSql);
			ps.setDate(8, sysdate);
			ps.setInt(9, BooleanUtils.toInteger(active));
			
			ps.setInt(10, datasourceId);

			ps.executeUpdate();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Get id and name for all datasources
	 *
	 * @return id and name for all datasources
	 */
	public Map<String, Integer> getAllDatasourceNames() {
		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.TERTIARY); //order by case
		TreeMap<String, Integer> map = new TreeMap<String, Integer>(stringCollator);

		Connection conn = null;
		Statement st = null;

		try {
			conn = ArtConfig.getConnection();

			st = conn.createStatement();
			String sql = "SELECT NAME, DATABASE_ID FROM ART_DATABASES";

			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				map.put(rs.getString("NAME"), Integer.valueOf(rs.getInt("DATABASE_ID")));
			}
			rs.close();

		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}

	/**
	 * Get all queries that use a given datasource
	 *
	 * @param dsId
	 * @return all queries that use a given datasource
	 */
	public Map<Integer, ArtQuery> getLinkedQueries(int dsId) {
		TreeMap<Integer, ArtQuery> map = new TreeMap<Integer, ArtQuery>();

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			ResultSet rs;
			int queryId;

			sql = "SELECT QUERY_ID, QUERY_GROUP_ID, NAME"
					+ " FROM ART_QUERIES "
					+ " WHERE DATABASE_ID = ? ";

			ps = conn.prepareStatement(sql);

			ps.setInt(1, dsId);
			rs = ps.executeQuery();
			while (rs.next()) {
				ArtQuery aq = new ArtQuery();
				queryId = rs.getInt("QUERY_ID");
				aq.setQueryId(queryId);
				aq.setGroupId(rs.getInt("QUERY_GROUP_ID"));
				aq.setName(rs.getString("NAME"));

				map.put(queryId, aq);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}
}