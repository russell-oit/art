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
package art.smtpserver;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.user.User;
import art.utils.ActionResult;
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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for adding, deleting, retrieving and updating smtp server
 * configurations
 *
 * @author Timothy Anyona
 */
@Service
public class SmtpServerService {

	private static final Logger logger = LoggerFactory.getLogger(SmtpServerService.class);

	private final DbService dbService;

	@Autowired
	public SmtpServerService(DbService dbService) {
		this.dbService = dbService;
	}

	public SmtpServerService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_SMTP_SERVERS";

	/**
	 * Maps a resultset to an object
	 */
	private class SmtpServerMapper extends BasicRowProcessor {

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
			SmtpServer smtpServer = new SmtpServer();

			smtpServer.setSmtpServerId(rs.getInt("SMTP_SERVER_ID"));
			smtpServer.setName(rs.getString("NAME"));
			smtpServer.setDescription(rs.getString("DESCRIPTION"));
			smtpServer.setActive(rs.getBoolean("ACTIVE"));
			smtpServer.setServer(rs.getString("SERVER"));
			smtpServer.setPort(rs.getInt("PORT"));
			smtpServer.setUseStartTls(rs.getBoolean("USE_STARTTLS"));
			smtpServer.setUseSmtpAuthentication(rs.getBoolean("USE_SMTP_AUTHENTICATION"));
			smtpServer.setUsername(rs.getString("USERNAME"));
			smtpServer.setPassword(rs.getString("PASSWORD"));
			smtpServer.setFrom(rs.getString("SMTP_FROM"));
			smtpServer.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			smtpServer.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			smtpServer.setCreatedBy(rs.getString("CREATED_BY"));
			smtpServer.setUpdatedBy(rs.getString("UPDATED_BY"));

			//decrypt password
			smtpServer.decryptPassword();

			return type.cast(smtpServer);
		}
	}

	/**
	 * Returns all smtp servers
	 *
	 * @return all smtp servers
	 * @throws SQLException
	 */
	@Cacheable("smtpServers")
	public List<SmtpServer> getAllSmtpServers() throws SQLException {
		logger.debug("Entering getAllSmtpServers");

		ResultSetHandler<List<SmtpServer>> h = new BeanListHandler<>(SmtpServer.class, new SmtpServerMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns smtp servers with given ids
	 *
	 * @param ids comma separated string of the smtp server ids to retrieve
	 * @return smtp servers with given ids
	 * @throws SQLException
	 */
	public List<SmtpServer> getSmtpServers(String ids) throws SQLException {
		logger.debug("Entering getSmtpServers: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		String sql = SQL_SELECT_ALL
				+ " WHERE SMTP_SERVER_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<SmtpServer>> h = new BeanListHandler<>(SmtpServer.class, new SmtpServerMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns the smtp server with the given id
	 *
	 * @param id the smtp server id
	 * @return the smtp server if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("smtpServers")
	public SmtpServer getSmtpServer(int id) throws SQLException {
		logger.debug("Entering getSmtpServer: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE SMTP_SERVER_ID=?";
		ResultSetHandler<SmtpServer> h = new BeanHandler<>(SmtpServer.class, new SmtpServerMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Deletes an smtp server
	 *
	 * @param id the smtp server id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * jobs which prevented the smtp server from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "smtpServers", allEntries = true)
	public ActionResult deleteSmtpServer(int id) throws SQLException {
		logger.debug("Entering deleteSmtpServer: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedJobs = getLinkedJobs(id);
		if (!linkedJobs.isEmpty()) {
			result.setData(linkedJobs);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_SMTP_SERVERS WHERE SMTP_SERVER_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes multiple smtp servers
	 *
	 * @param ids the ids of smtp servers to delete
	 * @return ActionResult. if not successful, data contains details of smtp
	 * servers which weren't deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "smtpServers", allEntries = true)
	public ActionResult deleteSmtpServers(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteSmtpServers: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteSmtpServer(id);
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
	 * Adds a new smtp server
	 *
	 * @param smtpServer the smtp server
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "smtpServers", allEntries = true)
	public synchronized int addSmtpServer(SmtpServer smtpServer, User actionUser) throws SQLException {
		logger.debug("Entering addSmtpServer: smtpServer={}, actionUser={}", smtpServer, actionUser);

		//generate new id
		String sql = "SELECT MAX(SMTP_SERVER_ID) FROM ART_SMTP_SERVERS";
		int newId = dbService.getNewRecordId(sql);

		saveSmtpServer(smtpServer, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an smtp server
	 *
	 * @param smtpServer the updated smtp server
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = {"smtpServers", "jobs"}, allEntries = true)
	public void updateSmtpServer(SmtpServer smtpServer, User actionUser) throws SQLException {
		logger.debug("Entering updateSmtpServer: smtpServer={}, actionUser={}", smtpServer, actionUser);

		Integer newRecordId = null;
		saveSmtpServer(smtpServer, newRecordId, actionUser);
	}

	/**
	 * Imports smtp server records
	 *
	 * @param smtpServers the list of smtp servers to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = "smtpServers", allEntries = true)
	public void importSmtpServers(List<SmtpServer> smtpServers, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering importSmtpServers: actionUser={}", actionUser);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(SMTP_SERVER_ID) FROM ART_SMTP_SERVERS";
			int id = dbService.getMaxRecordId(conn, sql);

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			for (SmtpServer smtpServer : smtpServers) {
				id++;
				saveSmtpServer(smtpServer, id, actionUser, conn);
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
	 * Saves an smtp server
	 *
	 * @param smtpServer the smtp server to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveSmtpServer(SmtpServer smtpServer, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveSmtpServer(smtpServer, newRecordId, actionUser, conn);
	}

	/**
	 * Saves an smtp server
	 *
	 * @param smtpServer the smtp server
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	private void saveSmtpServer(SmtpServer smtpServer, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {
		logger.debug("Entering saveSmtpServer: smtpServer={}, newRecordId={},"
				+ " actionUser={}", smtpServer, newRecordId, actionUser);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_SMTP_SERVERS"
					+ " (SMTP_SERVER_ID, NAME, DESCRIPTION, ACTIVE,"
					+ " SERVER, PORT, USE_STARTTLS, USE_SMTP_AUTHENTICATION,"
					+ " USERNAME, PASSWORD, SMTP_FROM,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 13) + ")";

			Object[] values = {
				newRecordId,
				smtpServer.getName(),
				smtpServer.getDescription(),
				BooleanUtils.toInteger(smtpServer.isActive()),
				smtpServer.getServer(),
				smtpServer.getPort(),
				BooleanUtils.toInteger(smtpServer.isUseStartTls()),
				BooleanUtils.toInteger(smtpServer.isUseSmtpAuthentication()),
				smtpServer.getUsername(),
				smtpServer.getPassword(),
				smtpServer.getFrom(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_SMTP_SERVERS SET NAME=?, DESCRIPTION=?,"
					+ " ACTIVE=?, SERVER=?, PORT=?, USE_STARTTLS=?,"
					+ " USE_SMTP_AUTHENTICATION=?, USERNAME=?, PASSWORD=?,"
					+ " SMTP_FROM=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE SMTP_SERVER_ID=?";

			Object[] values = {
				smtpServer.getName(),
				smtpServer.getDescription(),
				BooleanUtils.toInteger(smtpServer.isActive()),
				smtpServer.getServer(),
				smtpServer.getPort(),
				BooleanUtils.toInteger(smtpServer.isUseStartTls()),
				BooleanUtils.toInteger(smtpServer.isUseSmtpAuthentication()),
				smtpServer.getUsername(),
				smtpServer.getPassword(),
				smtpServer.getFrom(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				smtpServer.getSmtpServerId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		if (newRecordId != null) {
			smtpServer.setSmtpServerId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, smtpServer={}",
					affectedRows, newRecord, smtpServer);
		}
	}

	/**
	 * Updates multiple smtp servers
	 *
	 * @param multipleSmtpServerEdit the multiple smtp server edit object
	 * @param actionUser the user who is performing the edit
	 * @throws SQLException
	 */
	@CacheEvict(value = {"smtpServers", "jobs"}, allEntries = true)
	public void updateSmtpServers(MultipleSmtpServerEdit multipleSmtpServerEdit, User actionUser)
			throws SQLException {

		logger.debug("Entering updateSmtpServers: multipleSmtpServerEdit={}, actionUser={}",
				multipleSmtpServerEdit, actionUser);

		String sql;

		List<Object> idsList = ArtUtils.idsToObjectList(multipleSmtpServerEdit.getIds());
		if (!multipleSmtpServerEdit.isActiveUnchanged()) {
			sql = "UPDATE ART_SMTP_SERVERS SET ACTIVE=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE SMTP_SERVER_ID IN(" + StringUtils.repeat("?", ",", idsList.size()) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleSmtpServerEdit.isActive()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(idsList);

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

	/**
	 * Returns details of jobs that use a given smtp server
	 *
	 * @param smtpServerId the smtp server id
	 * @return linked job details
	 * @throws SQLException
	 */
	public List<String> getLinkedJobs(int smtpServerId) throws SQLException {
		logger.debug("Entering getLinkedJobs: smtpServerId={}", smtpServerId);

		String sql = "SELECT JOB_ID, JOB_NAME"
				+ " FROM ART_JOBS"
				+ " WHERE SMTP_SERVER_ID=?";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> jobDetails = dbService.query(sql, h, smtpServerId);

		List<String> jobs = new ArrayList<>();
		for (Map<String, Object> jobDetail : jobDetails) {
			Integer jobId = (Integer) jobDetail.get("JOB_ID");
			String jobName = (String) jobDetail.get("JOB_NAME");
			jobs.add(jobName + " (" + jobId + ")");
		}

		return jobs;
	}

}
