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

import art.dbutils.DbService;
import art.report.AvailableReport;
import art.servlets.ArtConfig;
import art.dbutils.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT = "SELECT * FROM ART_DATABASES";

	/**
	 * Class to map resultset to an object
	 */
	private class DatasourceMapper extends BasicRowProcessor {

		@Override
		public List<Datasource> toBeanList(ResultSet rs, Class type) throws SQLException {
			List<Datasource> list = new ArrayList<Datasource>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public Datasource toBean(ResultSet rs, Class type) throws SQLException {
			Datasource datasource = new Datasource();

			datasource.setDatasourceId(rs.getInt("DATABASE_ID"));
			datasource.setName(rs.getString("NAME"));
			datasource.setDescription(rs.getString("DESCRIPTION"));
			datasource.setDriver(rs.getString("DRIVER"));
			datasource.setUrl(rs.getString("URL"));
			datasource.setUsername(rs.getString("USERNAME"));
			datasource.setPassword(rs.getString("PASSWORD"));
			datasource.setConnectionPoolTimeout(rs.getInt("POOL_TIMEOUT"));
			datasource.setTestSql(rs.getString("TEST_SQL"));
			datasource.setActive(rs.getBoolean("ACTIVE"));
			datasource.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			datasource.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));

			return datasource;
		}
	}

	/**
	 * Get all datasources
	 *
	 * @return list of all datasources, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public List<Datasource> getAllDatasources() throws SQLException {
		ResultSetHandler<List<Datasource>> h = new BeanListHandler<Datasource>(Datasource.class, new DatasourceMapper());
		return dbService.query(SQL_SELECT, h);
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
		String sql = SQL_SELECT + " WHERE DATABASE_ID = ? ";
		ResultSetHandler<Datasource> h = new BeanHandler<Datasource>(Datasource.class, new DatasourceMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Delete a datasource
	 *
	 * @param id
	 * @throws SQLException
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public void deleteDatasource(int id) throws SQLException {
		String sql = "DELETE FROM ART_DATABASES WHERE DATABASE_ID=?";
		int affectedRows = dbService.update(sql, id);
		if (affectedRows == 0) {
			logger.warn("Delete datasource failed. Datasource not found. Id={}", id);
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

		int affectedRows = dbService.update(sql, values);
		if (affectedRows == 0) {
			logger.warn("Save datasource - no rows affected. Datasource='{}', newRecord={}", datasource.getName(), newRecord);
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
			rs = DbUtils.query(conn, ps, sql);
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

				int affectedRows = DbUtils.update(conn, psInsert, sql, values);
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

	/**
	 * Get reports that use a given datasource
	 *
	 * @param datasourceId
	 * @return list with link reports, empty list otherwise
	 * @throws SQLException
	 */
	public List<AvailableReport> getLinkedReports(int datasourceId) throws SQLException {
		List<AvailableReport> reports = new ArrayList<AvailableReport>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "SELECT NAME"
				+ " FROM ART_QUERIES"
				+ " WHERE DATABASE_ID=?";

		Object[] values = {
			datasourceId
		};

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.query(conn, ps, sql, values);
			while (rs.next()) {
				AvailableReport report = new AvailableReport();
				report.setName(rs.getString("NAME"));
				reports.add(report);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return reports;

	}

}
