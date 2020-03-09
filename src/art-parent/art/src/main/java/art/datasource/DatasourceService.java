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
package art.datasource;

import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.enums.DatasourceType;
import art.user.User;
import art.general.ActionResult;
import art.utils.ArtUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting datasources
 *
 * @author Timothy Anyona
 */
@Service
public class DatasourceService {

	private static final Logger logger = LoggerFactory.getLogger(DatasourceService.class);

	private final DbService dbService;

	@Autowired
	public DatasourceService(DbService dbService) {
		this.dbService = dbService;
	}

	public DatasourceService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_DATABASES";

	/**
	 * Returns all datasources
	 *
	 * @return all datasources
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public List<Datasource> getAllDatasources() throws SQLException {
		logger.debug("Entering getAllDatasources");

		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns datasources with given ids
	 *
	 * @param ids comma separated string of the datasource ids to retrieve
	 * @return datasources with given ids
	 * @throws SQLException
	 */
	public List<Datasource> getDatasources(String ids) throws SQLException {
		logger.debug("Entering getDatasources: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		String sql = SQL_SELECT_ALL
				+ " WHERE DATABASE_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns active jdbc datasources
	 *
	 * @return active jdbc datasources
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public List<Datasource> getActiveJdbcDatasources() throws SQLException {
		logger.debug("Entering getActiveJdbcDatasources");

		String sql = SQL_SELECT_ALL + " WHERE ACTIVE=1 AND DATASOURCE_TYPE=?";

		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		return dbService.query(sql, h, DatasourceType.JDBC.getValue());
	}

	/**
	 * Returns olap4j datasources
	 *
	 * @return olap4j datasources
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public List<Datasource> getOlap4jDatasources() throws SQLException {
		logger.debug("Entering getOlap4jDatasources");

		String sql = SQL_SELECT_ALL + " WHERE DRIVER IN('mondrian.olap4j.MondrianOlap4jDriver',"
				+ "'org.olap4j.driver.xmla.XmlaOlap4jDriver')";

		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Returns a datasource with the given id
	 *
	 * @param id the datasource id
	 * @return the datasource found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public Datasource getDatasource(int id) throws SQLException {
		logger.debug("Entering getDatasource: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE DATABASE_ID=?";
		ResultSetHandler<Datasource> h = new BeanHandler<>(Datasource.class, new DatasourceMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns a datasource with the given name
	 *
	 * @param name the datasource name
	 * @return the datasource found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public Datasource getDatasource(String name) throws SQLException {
		logger.debug("Entering getDatasource: name='{}'", name);

		String sql = SQL_SELECT_ALL + " WHERE NAME=?";
		ResultSetHandler<Datasource> h = new BeanHandler<>(Datasource.class, new DatasourceMapper());
		return dbService.query(sql, h, name);
	}

	/**
	 * Deletes a datasource
	 *
	 * @param id the datasource id
	 * @return ActionResult. if ActionResult.success is false, ActionResult.data
	 * contains a list of linked reports which prevented the datasource from
	 * being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"datasources", "reports"}, allEntries = true)
	public ActionResult deleteDatasource(int id) throws SQLException {
		logger.debug("Entering deleteDatasource: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedReports = getLinkedReports(id);
		if (!linkedReports.isEmpty()) {
			result.setData(linkedReports);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_DATABASES WHERE DATABASE_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes multiple datasources
	 *
	 * @param ids the ids of the datasources to delete
	 * @return ActionResult. if ActionResult.success is false, ActionResult.data
	 * contains a list of datasource ids that were not deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"datasources", "reports"}, allEntries = true)
	public ActionResult deleteDatasources(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteDatasource: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteDatasource(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedReports = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedReports, ", ");
				nonDeletedRecords.add(value);
			}
		}

		if (nonDeletedRecords.isEmpty()) {
			result.setSuccess(true);
		} else {
			result.setData(nonDeletedRecords);
		}

		return result;
	}

	/**
	 * Adds a new datasource to the database
	 *
	 * @param datasource the datasource to add
	 * @param actionUser the user who is performing the add
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public synchronized int addDatasource(Datasource datasource, User actionUser) throws SQLException {
		logger.debug("Entering addDatasource: datasource={}, actionUser={}", datasource, actionUser);

		//generate new id
		String sql = "SELECT MAX(DATABASE_ID) FROM ART_DATABASES";
		int newId = dbService.getNewRecordId(sql);

		//only set new id after successful save. in case copy is attempted and there is an error
		//new id will be set in save, if no exception is thrown
//		datasource.setDatasourceId(newId);
		saveDatasource(datasource, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing datasource
	 *
	 * @param datasource the updated datasource
	 * @param actionUser the user who is performing the update
	 * @throws SQLException
	 */
	@CacheEvict(value = {"datasources", "reports"}, allEntries = true)
	public void updateDatasource(Datasource datasource, User actionUser) throws SQLException {
		Connection conn = null;
		updateDatasource(datasource, actionUser, conn);
	}

	/**
	 * Updates an existing datasource
	 *
	 * @param datasource the updated datasource
	 * @param actionUser the user who is performing the update
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"datasources", "reports"}, allEntries = true)
	public void updateDatasource(Datasource datasource, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering updateDatasource: datasource={}, actionUser={}",
				datasource, actionUser);

		Integer newRecordId = null;
		saveDatasource(datasource, newRecordId, actionUser, conn);
	}

	/**
	 * Imports datasource records
	 *
	 * @param datasources the list of datasources to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param overwrite whether to overwrite existing records
	 * @throws SQLException
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public void importDatasources(List<Datasource> datasources, User actionUser,
			Connection conn, boolean overwrite) throws SQLException {

		logger.debug("Entering importDatasources: actionUser={}, overwrite={}",
				actionUser, overwrite);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(DATABASE_ID) FROM ART_DATABASES";
			int id = dbService.getMaxRecordId(conn, sql);

			List<Datasource> currentDatasources = new ArrayList<>();;
			if (overwrite) {
				currentDatasources = getAllDatasources();
			}

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			for (Datasource datasource : datasources) {
				String datasourceName = datasource.getName();
				boolean update = false;
				if (overwrite) {
					Datasource existingDatasource = currentDatasources.stream()
							.filter(d -> StringUtils.equals(datasourceName, d.getName()))
							.findFirst()
							.orElse(null);
					if (existingDatasource != null) {
						update = true;
						datasource.setDatasourceId(existingDatasource.getDatasourceId());
					}
				}

				Integer newRecordId;
				if (update) {
					newRecordId = null;
				} else {
					id++;
					newRecordId = id;
				}
				saveDatasource(datasource, newRecordId, actionUser, conn);
			}
			conn.commit();
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	/**
	 * Saves a datasource
	 *
	 * @param datasource the datasource to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveDatasource(Datasource datasource, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveDatasource(datasource, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a datasource
	 *
	 * @param datasource the datasource to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @param conn the connection to use. If null, the art database will be used
	 * @throws SQLException
	 */
	@CacheEvict(value = "datasources", allEntries = true)
	public void saveDatasource(Datasource datasource, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering saveDatasource: datasource={}, "
				+ " actionUser={}, newRecordId={}", datasource, actionUser, newRecordId);

		int affectedRows;
		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		String databaseProtocol;
		if (datasource.getDatabaseProtocol() == null) {
			databaseProtocol = null;
		} else {
			databaseProtocol = datasource.getDatabaseProtocol().getValue();
		}

		String databaseType;
		if (datasource.getDatabaseType() == null) {
			databaseType = null;
		} else {
			databaseType = datasource.getDatabaseType().getValue();
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_DATABASES"
					+ " (DATABASE_ID, NAME, DESCRIPTION, DATASOURCE_TYPE,"
					+ " JNDI, DATABASE_TYPE, DATABASE_PROTOCOL, DRIVER, URL,"
					+ " USERNAME, PASSWORD, PASSWORD_ALGORITHM, POOL_TIMEOUT,"
					+ " TEST_SQL, ACTIVE, DATASOURCE_OPTIONS,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 18) + ")";

			Object[] values = {
				newRecordId,
				datasource.getName(),
				datasource.getDescription(),
				datasource.getDatasourceType().getValue(),
				//postgresql requires explicit cast from boolean to integer
				BooleanUtils.toInteger(datasource.isJndi()),
				databaseType,
				databaseProtocol,
				datasource.getDriver(),
				datasource.getUrl(),
				datasource.getUsername(),
				datasource.getPassword(),
				datasource.getPasswordAlgorithm(),
				datasource.getConnectionPoolTimeoutMins(),
				datasource.getTestSql(),
				BooleanUtils.toInteger(datasource.isActive()),
				datasource.getOptions(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_DATABASES SET NAME=?, DESCRIPTION=?,"
					+ " DATASOURCE_TYPE=?, JNDI=?, DATABASE_TYPE=?,"
					+ " DATABASE_PROTOCOL=?, DRIVER=?, URL=?, USERNAME=?,"
					+ " PASSWORD=?, PASSWORD_ALGORITHM=?, POOL_TIMEOUT=?,"
					+ " TEST_SQL=?, ACTIVE=?, DATASOURCE_OPTIONS=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE DATABASE_ID=?";

			Object[] values = {
				datasource.getName(),
				datasource.getDescription(),
				datasource.getDatasourceType().getValue(),
				BooleanUtils.toInteger(datasource.isJndi()),
				databaseType,
				databaseProtocol,
				datasource.getDriver(),
				datasource.getUrl(),
				datasource.getUsername(),
				datasource.getPassword(),
				datasource.getPasswordAlgorithm(),
				datasource.getConnectionPoolTimeoutMins(),
				datasource.getTestSql(),
				BooleanUtils.toInteger(datasource.isActive()),
				datasource.getOptions(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				datasource.getDatasourceId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		//if no exception, update datasource id in case of new or copy
		if (newRecordId != null) {
			datasource.setDatasourceId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);
		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, datasource={}",
					affectedRows, newRecord, datasource);
		}
	}

	/**
	 * Returns names of reports that use a given datasource
	 *
	 * @param datasourceId the datasource id
	 * @return linked report names
	 * @throws SQLException
	 */
	public List<String> getLinkedReports(int datasourceId) throws SQLException {
		logger.debug("Entering getLinkedReports: datasourceId={}", datasourceId);

		String sql = "SELECT NAME"
				+ " FROM ART_QUERIES"
				+ " WHERE DATASOURCE_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>("NAME");
		return dbService.query(sql, h, datasourceId);
	}

	/**
	 * Returns datasources that an admin can use, according to his access level
	 *
	 * @param user the admin user
	 * @return available datasources
	 * @throws SQLException
	 */
	@Cacheable("datasources")
	public List<Datasource> getAdminDatasources(User user) throws SQLException {
		logger.debug("Entering getAdminDatasources: user={}", user);

		if (user == null || user.getAccessLevel() == null) {
			return Collections.emptyList();
		}

		logger.debug("user.getAccessLevel()={}", user.getAccessLevel());

		ResultSetHandler<List<Datasource>> h = new BeanListHandler<>(Datasource.class, new DatasourceMapper());
		if (user.hasStandardAdminAndAboveAccessLevel()) {
			//standard admins and above can work with everything
			return dbService.query(SQL_SELECT_ALL, h);
		} else {
			String sql = "SELECT AD.*"
					+ " FROM ART_DATABASES AD, ART_ADMIN_PRIVILEGES AAP"
					+ " WHERE AD.DATABASE_ID = AAP.VALUE_ID"
					+ " AND AAP.PRIVILEGE = 'DB'"
					+ " AND AAP.USER_ID = ?";
			return dbService.query(sql, h, user.getUserId());
		}
	}

	/**
	 * Updates multiple datasources
	 *
	 * @param multipleDatasourceEdit the multiple datasource edit object
	 * @param actionUser the user who is performing the edit
	 * @throws SQLException
	 */
	@CacheEvict(value = {"datasources", "reports"}, allEntries = true)
	public void updateDatasources(MultipleDatasourceEdit multipleDatasourceEdit, User actionUser)
			throws SQLException {

		logger.debug("Entering updateDatasources: multipleDatasourceEdit={}, actionUser={}",
				multipleDatasourceEdit, actionUser);

		String sql;

		List<Object> idsList = ArtUtils.idsToObjectList(multipleDatasourceEdit.getIds());
		if (!multipleDatasourceEdit.isActiveUnchanged()) {
			sql = "UPDATE ART_DATABASES SET ACTIVE=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE DATABASE_ID IN(" + StringUtils.repeat("?", ",", idsList.size()) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleDatasourceEdit.isActive()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(idsList);

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

}
