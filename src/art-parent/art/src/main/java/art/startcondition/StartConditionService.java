/*
 * ART. A Reporting Tool.
 * Copyright (C) 2020 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.startcondition;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.general.ActionResult;
import art.user.User;
import art.utils.ArtUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting start
 * conditions
 *
 * @author Timothy Anyona
 */
@Service
public class StartConditionService {

	private static final Logger logger = LoggerFactory.getLogger(StartConditionService.class);

	private final DbService dbService;

	@Autowired
	public StartConditionService(DbService dbService) {
		this.dbService = dbService;
	}

	public StartConditionService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_START_CONDITIONS ASCD";

	/**
	 * Maps a resultset to an object
	 */
	private class StartConditionMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			StartCondition startCondition = new StartCondition();

			startCondition.setStartConditionId(rs.getInt("START_CONDITION_ID"));
			startCondition.setName(rs.getString("NAME"));
			startCondition.setDescription(rs.getString("DESCRIPTION"));
			startCondition.setRetryDelayMins(rs.getInt("RETRY_DELAY_MINS"));
			startCondition.setRetryAttempts(rs.getInt("RETRY_ATTEMPTS"));
			startCondition.setCondition(rs.getString("START_CONDITION"));
			startCondition.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			startCondition.setCreatedBy(rs.getString("CREATED_BY"));
			startCondition.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			startCondition.setUpdatedBy(rs.getString("UPDATED_BY"));

			return type.cast(startCondition);
		}
	}

	/**
	 * Returns all start conditions
	 *
	 * @return all start conditions
	 * @throws SQLException
	 */
	@Cacheable("startConditions")
	public List<StartCondition> getAllStartConditions() throws SQLException {
		logger.debug("Entering getAllStartConditions");

		ResultSetHandler<List<StartCondition>> h = new BeanListHandler<>(StartCondition.class, new StartConditionMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns start conditions with given ids
	 *
	 * @param ids comma separated string of the start condition ids to retrieve
	 * @return start conditions with given ids
	 * @throws SQLException
	 */
	public List<StartCondition> getStartConditions(String ids) throws SQLException {
		logger.debug("Entering getStartConditions: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		if (idsArray.length == 0) {
			return new ArrayList<>();
		}

		String sql = SQL_SELECT_ALL
				+ " WHERE START_CONDITION_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<StartCondition>> h = new BeanListHandler<>(StartCondition.class, new StartConditionMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns a start condition with the given id
	 *
	 * @param id the start condition id
	 * @return start condition if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("startConditions")
	public StartCondition getStartCondition(int id) throws SQLException {
		logger.debug("Entering getStartCondition: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE START_CONDITION_ID=?";
		ResultSetHandler<StartCondition> h = new BeanHandler<>(StartCondition.class, new StartConditionMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns a start condition with the given name
	 *
	 * @param name the start condition name
	 * @return start condition if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("startConditions")
	public StartCondition getStartCondition(String name) throws SQLException {
		logger.debug("Entering getStartCondition: name='{}'", name);

		String sql = SQL_SELECT_ALL + " WHERE NAME=?";
		ResultSetHandler<StartCondition> h = new BeanHandler<>(StartCondition.class, new StartConditionMapper());
		return dbService.query(sql, h, name);
	}

	/**
	 * Deletes a start condition
	 *
	 * @param id the start condition id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * users and user groups which prevented the startCondition from being
	 * deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"startConditions"}, allEntries = true)
	public ActionResult deleteStartCondition(int id) throws SQLException {
		logger.debug("Entering deleteStartCondition: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedRecords = getLinkedRecords(id);
		if (!linkedRecords.isEmpty()) {
			result.setData(linkedRecords);
			return result;
		}

		String sql;

		//finally delete start condition
		sql = "DELETE FROM ART_START_CONDITIONS WHERE START_CONDITION_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes multiple start conditions
	 *
	 * @param ids the ids of the start conditions to delete
	 * @return ActionResult. if not successful, data contains details of start
	 * conditions which weren't deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"startConditions"}, allEntries = true)
	public ActionResult deleteStartConditions(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteStartConditions: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteStartCondition(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedRecords = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedRecords, ", ");
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
	 * Adds a new start condition
	 *
	 * @param startCondition the start condition to add
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "startConditions", allEntries = true)
	public synchronized int addStartCondition(StartCondition startCondition, User actionUser) throws SQLException {
		logger.debug("Entering addStartCondition: startCondition={}, actionUser={}", startCondition, actionUser);

		//generate new id
		String sql = "SELECT MAX(START_CONDITION_ID) FROM ART_START_CONDITIONS";
		int newId = dbService.getNewRecordId(sql);

		saveStartCondition(startCondition, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing start condition
	 *
	 * @param startCondition the updated start condition
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = {"startConditions", "jobs", "pipelines"}, allEntries = true)
	public void updateStartCondition(StartCondition startCondition, User actionUser) throws SQLException {
		Connection conn = null;
		updateStartCondition(startCondition, actionUser, conn);
	}

	/**
	 * Updates an existing start condition
	 *
	 * @param startCondition the updated start condition
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"startConditions", "jobs", "pipelines"}, allEntries = true)
	public void updateStartCondition(StartCondition startCondition, User actionUser, Connection conn) throws SQLException {
		logger.debug("Entering updateStartCondition: startCondition={}, actionUser={}", startCondition, actionUser);

		Integer newRecordId = null;
		saveStartCondition(startCondition, newRecordId, actionUser, conn);
	}

	/**
	 * Imports start condition records
	 *
	 * @param startConditions the list of start conditions to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param overwrite whether to overwrite existing records
	 * @throws SQLException
	 */
	@CacheEvict(value = "startConditions", allEntries = true)
	public void importStartConditions(List<StartCondition> startConditions, User actionUser,
			Connection conn, boolean overwrite) throws SQLException {

		logger.debug("Entering importStartConditions: actionUser={}, overwrite={}",
				actionUser, overwrite);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(START_CONDITION_ID) FROM ART_START_CONDITIONS";
			int id = dbService.getMaxRecordId(conn, sql);

			List<StartCondition> currentStartConditions = new ArrayList<>();
			if (overwrite) {
				currentStartConditions = getAllStartConditions();
			}

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			for (StartCondition startCondition : startConditions) {
				String startConditionName = startCondition.getName();
				boolean update = false;
				if (overwrite) {
					StartCondition existingStartCondition = currentStartConditions.stream()
							.filter(d -> StringUtils.equals(startConditionName, d.getName()))
							.findFirst()
							.orElse(null);
					if (existingStartCondition != null) {
						update = true;
						startCondition.setStartConditionId(existingStartCondition.getStartConditionId());
					}
				}

				Integer newRecordId;
				if (update) {
					newRecordId = null;
				} else {
					id++;
					newRecordId = id;
				}
				saveStartCondition(startCondition, newRecordId, actionUser, conn);
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
	 * Saves a start condition
	 *
	 * @param startCondition the start condition to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveStartCondition(StartCondition startCondition, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveStartCondition(startCondition, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a startCondition
	 *
	 * @param startCondition the start condition to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	@CacheEvict(value = "startConditions", allEntries = true)
	public void saveStartCondition(StartCondition startCondition, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering saveStartCondition: startCondition={}, newRecordId={},actionUser={}",
				startCondition, newRecordId, actionUser);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_START_CONDITIONS"
					+ " (START_CONDITION_ID, NAME, DESCRIPTION,"
					+ " RETRY_DELAY_MINS, RETRY_ATTEMPTS, START_CONDITION,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 8) + ")";

			Object[] values = {
				newRecordId,
				startCondition.getName(),
				startCondition.getDescription(),
				startCondition.getRetryDelayMins(),
				startCondition.getRetryAttempts(),
				startCondition.getCondition(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(conn, sql, values);
		} else {
			String sql = "UPDATE ART_START_CONDITIONS SET NAME=?, DESCRIPTION=?,"
					+ " RETRY_DELAY_MINS=?, RETRY_ATTEMPTS=?,"
					+ " START_CONDITION=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE START_CONDITION_ID=?";

			Object[] values = {
				startCondition.getName(),
				startCondition.getDescription(),
				startCondition.getRetryDelayMins(),
				startCondition.getRetryAttempts(),
				startCondition.getCondition(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				startCondition.getStartConditionId()
			};

			affectedRows = dbService.update(conn, sql, values);
		}

		if (newRecordId != null) {
			startCondition.setStartConditionId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, startCondition={}",
					affectedRows, newRecord, startCondition);
		}
	}

	/**
	 * Returns details of jobs and pipelines that have a given start condition
	 *
	 * @param startConditionId the start condition id
	 * @return linked job and pipeline details
	 * @throws SQLException
	 */
	public List<String> getLinkedRecords(int startConditionId) throws SQLException {
		logger.debug("Entering getLinkedRecords: startConditionId={}", startConditionId);

		//union removes duplicate records, union all does not
		//use union all in case a job and a pipeline have the same name?
		String sql = "SELECT JOB_NAME AS RECORD_NAME"
				+ " FROM ART_JOBS"
				+ " WHERE START_CONDITION_ID=?"
				+ " UNION ALL"
				+ " SELECT NAME AS RECORD_NAME"
				+ " FROM ART_PIPELINES"
				+ " WHERE START_CONDITION_ID=?";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> recordDetails = dbService.query(sql, h, startConditionId, startConditionId);

		List<String> records = new ArrayList<>();
		for (Map<String, Object> recordDetail : recordDetails) {
			String recordName = (String) recordDetail.get("RECORD_NAME");
			records.add(recordName);
		}

		return records;
	}

}
