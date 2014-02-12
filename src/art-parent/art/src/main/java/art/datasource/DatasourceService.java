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
package art.datasource;

import art.servlets.ArtConfig;
import art.utils.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class with methods for datasources
 *
 * @author Timothy Anyona
 */
@Service
public class DatasourceService {

	private static final Logger logger = LoggerFactory.getLogger(DatasourceService.class);
	final String SQL_SELECT_ALL_DATASOURCES = "SELECT DATABASE_ID, NAME, DRIVER,"
			+ " URL, USERNAME, PASSWORD, POOL_TIMEOUT, TEST_SQL, ACTIVE,"
			+ " CREATION_DATE, UPDATE_DATE"
			+ " FROM ART_DATABASES";

	/**
	 * Get all datasources
	 *
	 * @return list of all datasources, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public List<Datasource> getAllDatasources() throws SQLException {
		List<Datasource> datasources = new ArrayList<Datasource>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_SELECT_ALL_DATASOURCES;

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.executeQuery(conn, ps, sql);
			while (rs.next()) {
				Datasource datasource = new Datasource();
				populateDatasource(datasource, rs);
				datasources.add(datasource);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return datasources;
	}

	/**
	 * Get a datasource
	 *
	 * @param id
	 * @return populated object if datasource found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public Datasource getDatasource(int id) throws SQLException {
		Datasource datasource = null;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_SELECT_ALL_DATASOURCES + " WHERE DATABASE_ID = ? ";

		Object[] values = {
			id
		};

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.executeQuery(conn, ps, sql, values);
			if (rs.next()) {
				datasource = new Datasource();
				populateDatasource(datasource, rs);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return datasource;
	}

	/**
	 * Populate object with row from datasource
	 *
	 * @param group
	 * @param rs
	 * @throws SQLException
	 */
	private void populateDatasource(Datasource datasource, ResultSet rs) throws SQLException {
		datasource.setDatasourceId(rs.getInt("DATABASE_ID"));
		datasource.setName(rs.getString("NAME"));
		datasource.setDriver(rs.getString("DRIVER"));
		datasource.setUrl(rs.getString("URL"));
		datasource.setUsername(rs.getString("USERNAME"));
		datasource.setPassword(rs.getString("PASSWORD"));
		datasource.setConnectionPoolTimeout(rs.getInt("POOL_TIMEOUT"));
		datasource.setTestSql(rs.getString("TEST_SQL"));
		datasource.setActive(rs.getBoolean("ACTIVE"));
		datasource.setCreationDate(rs.getTimestamp("CREATION_DATE"));
		datasource.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
	}

	/**
	 * Delete a datasource
	 *
	 * @param id
	 * @throws SQLException
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public void deleteDatasource(int id) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();

			String sql = "DELETE FROM ART_DATABASES WHERE DATABASE_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) {
				logger.warn("Delete datasource failed. Datasource not found. Id={}", id);
			}

		} finally {
			DbUtils.close(rs, ps, conn);
		}
	}

	/**
	 * Add a new datasource to the database
	 *
	 * @param datasource
	 * @throws SQLException
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public void addDatasource(Datasource datasource) throws SQLException {
		int newId = allocateNewId();
		if (newId > 0) {
			datasource.setDatasourceId(newId);
			saveDatasource(datasource, true);
		} else {
			logger.warn("Datasource not added. Allocate new ID failed. Datasource='{}'", datasource.getName());
		}
	}

	/**
	 * Update an existing datasource
	 *
	 * @param datasource
	 * @throws SQLException
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public void updateDatasource(Datasource datasource) throws SQLException {
		saveDatasource(datasource, false);
	}

	/**
	 * Save a datasource
	 *
	 * @param datasource
	 * @param newRecord true if this is a new record, false otherwise
	 * @throws SQLException
	 */
	private void saveDatasource(Datasource datasource, boolean newRecord) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;

		String dateColumn;

		if (newRecord) {
			dateColumn = "CREATION_DATE";
		} else {
			dateColumn = "UPDATE_DATE";
		}

		String sql = "UPDATE ART_DATABASES SET NAME=?, DRIVER=?, URL=?,"
				+ " USERNAME=?, PASSWORD=?, POOL_TIMEOUT=?, TEST_SQL=?, ACTIVE=?"
				+ " ," + dateColumn + "=?"
				+ " WHERE DATABASE_ID=?";

		Object[] values = {
			datasource.getName(),
			datasource.getDriver(),
			datasource.getUrl(),
			datasource.getUsername(),
			datasource.getPassword(),
			datasource.getConnectionPoolTimeout(),
			datasource.getTestSql(),
			datasource.isActive(),
			DbUtils.getCurrentTimeStamp(),
			datasource.getDatasourceId()
		};

		try {
			conn = ArtConfig.getConnection();

			int affectedRows = DbUtils.executeUpdate(conn, ps, sql, values);
			if (affectedRows == 0) {
				logger.warn("Save datasource - no rows affected. Datasource='{}', newRecord={}", datasource.getName(), newRecord);
			}
		} finally {
			DbUtils.close(ps, conn);
		}
	}

	/**
	 * Generate an id and record for a new item
	 *
	 * @return new id generated, 0 otherwise
	 * @throws SQLException
	 */
	private synchronized int allocateNewId() throws SQLException {
		int newId = 0;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement psInsert = null;

		try {
			conn = ArtConfig.getConnection();
			//generate new id
			String sql = "SELECT MAX(DATABASE_ID) FROM ART_DATABASES";
			rs = DbUtils.executeQuery(conn, ps, sql);
			if (rs.next()) {
				newId = rs.getInt(1) + 1;

				//add dummy record with new id. fill all not null columns
				//name has unique constraint so use a random default value
				String allocatingName = "allocating-" + RandomStringUtils.randomAlphanumeric(3);
				sql = "INSERT INTO ART_DATABASES"
						+ " (DATABASE_ID,NAME,DRIVER,URL,USERNAME,PASSWORD)"
						+ " VALUES(?,?,'','','','')";

				Object[] values = {
					newId,
					allocatingName
				};

				int affectedRows = DbUtils.executeUpdate(conn, psInsert, sql, values);
				if (affectedRows == 0) {
					logger.warn("allocateNewId - no rows affected. id={}", newId);
				}
			} else {
				logger.warn("Could not get max id");
			}
		} finally {
			DbUtils.close(psInsert);
			DbUtils.close(rs, ps, conn);
		}

		return newId;
	}

}
