/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.destination;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.encryption.AesEncryptor;
import art.enums.DestinationType;
import art.user.User;
import art.utils.ActionResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for adding, deleting, retrieving and updating destination
 * configurations
 *
 * @author Timothy Anyona
 */
@Service
public class DestinationService {

	private static final Logger logger = LoggerFactory.getLogger(DestinationService.class);

	private final DbService dbService;

	@Autowired
	public DestinationService(DbService dbService) {
		this.dbService = dbService;
	}

	public DestinationService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_DESTINATIONS AD";

	/**
	 * Maps a resultset to an object
	 */
	private class DestinationMapper extends BasicRowProcessor {

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
			Destination destination = new Destination();

			destination.setDestinationId(rs.getInt("DESTINATION_ID"));
			destination.setName(rs.getString("NAME"));
			destination.setDescription(rs.getString("DESCRIPTION"));
			destination.setActive(rs.getBoolean("ACTIVE"));
			destination.setDestinationType(DestinationType.toEnum(rs.getString("DESTINATION_TYPE")));
			destination.setServer(rs.getString("SERVER"));
			destination.setPort(rs.getInt("PORT"));
			destination.setUser(rs.getString("DESTINATION_USER"));
			destination.setPassword(rs.getString("DESTINATION_PASSWORD"));
			destination.setDomain(rs.getString("USER_DOMAIN"));
			destination.setPath(rs.getString("DESTINATION_PATH"));
			destination.setSubDirectory(rs.getString("SUB_DIRECTORY"));
			destination.setCreateDirectories(rs.getBoolean("CREATE_DIRECTORIES"));
			destination.setOptions(rs.getString("DESTINATION_OPTIONS"));
			destination.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			destination.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			destination.setCreatedBy(rs.getString("CREATED_BY"));
			destination.setUpdatedBy(rs.getString("UPDATED_BY"));

			//decrypt password
			String password = destination.getPassword();
			password = AesEncryptor.decrypt(password);
			destination.setPassword(password);

			return type.cast(destination);
		}
	}

	/**
	 * Returns all destinations
	 *
	 * @return all destinations
	 * @throws SQLException
	 */
	@Cacheable("destinations")
	public List<Destination> getAllDestinations() throws SQLException {
		logger.debug("Entering getAllDestinations");

		ResultSetHandler<List<Destination>> h = new BeanListHandler<>(Destination.class, new DestinationMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns a destination
	 *
	 * @param id the destination id
	 * @return destination if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("destinations")
	public Destination getDestination(int id) throws SQLException {
		logger.debug("Entering getDestination: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE DESTINATION_ID=?";
		ResultSetHandler<Destination> h = new BeanHandler<>(Destination.class, new DestinationMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Deletes a destination
	 *
	 * @param id the destination id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * jobs which prevented the destination from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "destinations", allEntries = true)
	public ActionResult deleteDestination(int id) throws SQLException {
		logger.debug("Entering deleteDestination: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedJobs = getLinkedJobs(id);
		if (!linkedJobs.isEmpty()) {
			result.setData(linkedJobs);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_JOB_DESTINATION_MAP WHERE DESTINATION_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_DESTINATIONS WHERE DESTINATION_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes multiple destinations
	 *
	 * @param ids the ids of destinations to delete
	 * @return ActionResult. if not successful, data contains details of
	 * destinations which weren't deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "destinations", allEntries = true)
	public ActionResult deleteDestinations(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteDestinations: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteDestination(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedJobs = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedJobs, ", ");
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
	 * Adds a new destination
	 *
	 * @param destination the destination to add
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "destinations", allEntries = true)
	public synchronized int addDestination(Destination destination, User actionUser) throws SQLException {
		logger.debug("Entering addDestination: destination={}, actionUser={}", destination, actionUser);

		//generate new id
		String sql = "SELECT MAX(DESTINATION_ID) FROM ART_DESTINATIONS";
		ResultSetHandler<Integer> h = new ScalarHandler<>();
		Integer maxId = dbService.query(sql, h);
		logger.debug("maxId={}", maxId);

		int newId;
		if (maxId == null || maxId < 0) {
			//no records in the table, or only hardcoded records
			newId = 1;
		} else {
			newId = maxId + 1;
		}
		logger.debug("newId={}", newId);

		saveDestination(destination, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing destination
	 *
	 * @param destination the updated destination
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = {"destinations", "jobs"}, allEntries = true)
	public void updateDestination(Destination destination, User actionUser) throws SQLException {
		logger.debug("Entering updateDestination: destination={}, actionUser={}", destination, actionUser);

		Integer newRecordId = null;
		saveDestination(destination, newRecordId, actionUser);
	}

	/**
	 * Saves a destination
	 *
	 * @param destination the destination to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	private void saveDestination(Destination destination, Integer newRecordId, User actionUser) throws SQLException {
		logger.debug("Entering saveDestination: destination={}, newRecordId={}, actionUser={}",
				destination, newRecordId, actionUser);

		int affectedRows;
		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_DESTINATIONS"
					+ " (DESTINATION_ID, NAME, DESCRIPTION, ACTIVE,"
					+ " DESTINATION_TYPE, SERVER, PORT, DESTINATION_USER,"
					+ " DESTINATION_PASSWORD, USER_DOMAIN,"
					+ " DESTINATION_PATH, SUB_DIRECTORY,"
					+ " CREATE_DIRECTORIES, DESTINATION_OPTIONS,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 16) + ")";

			Object[] values = {
				newRecordId,
				destination.getName(),
				destination.getDescription(),
				BooleanUtils.toInteger(destination.isActive()),
				destination.getDestinationType().getValue(),
				destination.getServer(),
				destination.getPort(),
				destination.getUser(),
				destination.getPassword(),
				destination.getDomain(),
				destination.getPath(),
				destination.getSubDirectory(),
				BooleanUtils.toInteger(destination.isCreateDirectories()),
				destination.getOptions(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_DESTINATIONS SET NAME=?, DESCRIPTION=?,"
					+ " ACTIVE=?, DESTINATION_TYPE=?, SERVER=?, PORT=?,"
					+ " DESTINATION_USER=?, DESTINATION_PASSWORD=?,"
					+ " USER_DOMAIN=?, DESTINATION_PATH=?,"
					+ " SUB_DIRECTORY=?, CREATE_DIRECTORIES=?, DESTINATION_OPTIONS=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE DESTINATION_ID=?";

			Object[] values = {
				destination.getName(),
				destination.getDescription(),
				BooleanUtils.toInteger(destination.isActive()),
				destination.getDestinationType().getValue(),
				destination.getServer(),
				destination.getPort(),
				destination.getUser(),
				destination.getPassword(),
				destination.getDomain(),
				destination.getPath(),
				destination.getSubDirectory(),
				BooleanUtils.toInteger(destination.isCreateDirectories()),
				destination.getOptions(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				destination.getDestinationId()
			};

			affectedRows = dbService.update(sql, values);
		}

		if (newRecordId != null) {
			destination.setDestinationId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, destination={}",
					affectedRows, newRecord, destination);
		}
	}

	/**
	 * Returns the destinations used by a given job
	 *
	 * @param jobId the job id
	 * @return the destinations used by a given job
	 * @throws SQLException
	 */
	@Cacheable("destinations")
	public List<Destination> getJobDestinations(int jobId) throws SQLException {
		logger.debug("Entering getJobDestinations: jobId={}", jobId);

		String sql = SQL_SELECT_ALL + " INNER JOIN ART_JOB_DESTINATION_MAP AJDM"
				+ " ON AD.DESTINATION_ID=AJDM.DESTINATION_ID"
				+ " INNER JOIN ART_JOBS AJ"
				+ " ON AJDM.JOB_ID=AJ.JOB_ID"
				+ " WHERE AJ.JOB_ID=?";

		ResultSetHandler<List<Destination>> h = new BeanListHandler<>(Destination.class, new DestinationMapper());
		return dbService.query(sql, h, jobId);
	}

	/**
	 * Updates multiple destinations
	 *
	 * @param multipleDestinationEdit the multiple destination edit object
	 * @param actionUser the user who is performing the edit
	 * @throws SQLException
	 */
	@CacheEvict(value = "destinations", allEntries = true)
	public void updateDestinations(MultipleDestinationEdit multipleDestinationEdit, User actionUser)
			throws SQLException {

		logger.debug("Entering updateDestinations: multipleDestinationEdit={}, actionUser={}",
				multipleDestinationEdit, actionUser);

		String sql;

		String[] ids = StringUtils.split(multipleDestinationEdit.getIds(), ",");
		if (!multipleDestinationEdit.isActiveUnchanged()) {
			sql = "UPDATE ART_DESTINATIONS SET ACTIVE=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE DESTINATION_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleDestinationEdit.isActive()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(Arrays.asList(ids));

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

	/**
	 * Returns details of jobs that use a given destination
	 *
	 * @param destinationId the destination id
	 * @return linked job details
	 * @throws SQLException
	 */
	public List<String> getLinkedJobs(int destinationId) throws SQLException {
		logger.debug("Entering getLinkedJobs: destinationId={}", destinationId);

		String sql = "SELECT AJ.JOB_ID, AJ.JOB_NAME"
				+ " FROM ART_JOBS AJ"
				+ " INNER JOIN ART_JOB_DESTINATION_MAP AJDM"
				+ " ON AJ.JOB_ID=AJDM.JOB_ID"
				+ " WHERE AJDM.DESTINATION_ID=?";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> jobDetails = dbService.query(sql, h, destinationId);

		List<String> jobs = new ArrayList<>();
		for (Map<String, Object> jobDetail : jobDetails) {
			Integer jobId = (Integer) jobDetail.get("JOB_ID");
			String jobName = (String) jobDetail.get("JOB_NAME");
			jobs.add(jobName + " (" + jobId + ")");
		}

		return jobs;
	}
}
