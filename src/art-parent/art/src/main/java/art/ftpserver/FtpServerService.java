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
package art.ftpserver;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.encryption.AesEncryptor;
import art.enums.FtpConnectionType;
import art.user.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
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
 * Provides methods for adding, deleting, retrieving and updating ftp server
 * configurations
 *
 * @author Timothy Anyona
 */
@Service
public class FtpServerService {

	private static final Logger logger = LoggerFactory.getLogger(FtpServerService.class);

	private final DbService dbService;

	@Autowired
	public FtpServerService(DbService dbService) {
		this.dbService = dbService;
	}

	public FtpServerService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_FTP_SERVERS";

	/**
	 * Maps a resultset to an object
	 */
	private class FtpServerMapper extends BasicRowProcessor {

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
			FtpServer ftpServer = new FtpServer();

			ftpServer.setFtpServerId(rs.getInt("FTP_SERVER_ID"));
			ftpServer.setName(rs.getString("NAME"));
			ftpServer.setDescription(rs.getString("DESCRIPTION"));
			ftpServer.setActive(rs.getBoolean("ACTIVE"));
			ftpServer.setConnectionType(FtpConnectionType.toEnum(rs.getString("CONNECTION_TYPE")));
			ftpServer.setServer(rs.getString("SERVER"));
			ftpServer.setPort(rs.getInt("PORT"));
			ftpServer.setUser(rs.getString("FTP_USER"));
			ftpServer.setPassword(rs.getString("PASSWORD"));
			ftpServer.setRemoteDirectory(rs.getString("REMOTE_DIRECTORY"));
			ftpServer.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			ftpServer.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			ftpServer.setCreatedBy(rs.getString("CREATED_BY"));
			ftpServer.setUpdatedBy(rs.getString("UPDATED_BY"));

			//decrypt password
			String password = ftpServer.getPassword();
			password = AesEncryptor.decrypt(password);
			ftpServer.setPassword(password);

			return type.cast(ftpServer);
		}
	}

	/**
	 * Returns all ftp servers
	 *
	 * @return all ftp servers
	 * @throws SQLException
	 */
	@Cacheable("ftpServers")
	public List<FtpServer> getAllFtpServers() throws SQLException {
		logger.debug("Entering getAllFtpServers");

		ResultSetHandler<List<FtpServer>> h = new BeanListHandler<>(FtpServer.class, new FtpServerMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns the ftp server with the given id
	 *
	 * @param id the ftp server id
	 * @return the ftp server if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("ftpServers")
	public FtpServer getFtpServer(int id) throws SQLException {
		logger.debug("Entering getFtpServer: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE FTP_SERVER_ID=?";
		ResultSetHandler<FtpServer> h = new BeanHandler<>(FtpServer.class, new FtpServerMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Deletes the ftp server with the given id
	 *
	 * @param id the ftp server id
	 * @throws SQLException
	 */
	@CacheEvict(value = "ftpServers", allEntries = true)
	public void deleteFtpServer(int id) throws SQLException {
		logger.debug("Entering deleteFtpServer: id={}", id);

		String sql;

		sql = "DELETE FROM ART_FTP_SERVERS WHERE FTP_SERVER_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes the ftp servers with the given ids
	 *
	 * @param ids the ftp server ids
	 * @throws SQLException
	 */
	@CacheEvict(value = "ftpServers", allEntries = true)
	public void deleteFtpServers(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteFtpServers: ids={}", (Object) ids);

		String sql;

		sql = "DELETE FROM ART_FTP_SERVERS"
				+ " WHERE FTP_SERVER_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";

		dbService.update(sql, (Object[]) ids);
	}

	/**
	 * Adds a new ftp server
	 *
	 * @param ftpServer the ftp server
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "ftpServers", allEntries = true)
	public synchronized int addFtpServer(FtpServer ftpServer, User actionUser) throws SQLException {
		logger.debug("Entering addFtpServer: ftpServer={}, actionUser={}", ftpServer, actionUser);

		//generate new id
		String sql = "SELECT MAX(FTP_SERVER_ID) FROM ART_FTP_SERVERS";
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

		saveFtpServer(ftpServer, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an ftp server
	 *
	 * @param ftpServer the updated ftp server
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "ftpServers", allEntries = true)
	public void updateFtpServer(FtpServer ftpServer, User actionUser) throws SQLException {
		logger.debug("Entering updateFtpServer: ftpServer={}, actionUser={}", ftpServer, actionUser);

		Integer newRecordId = null;
		saveFtpServer(ftpServer, newRecordId, actionUser);
	}

	/**
	 * Saves an ftp server
	 *
	 * @param ftpServer the ftp server
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	private void saveFtpServer(FtpServer ftpServer, Integer newRecordId, User actionUser) throws SQLException {
		logger.debug("Entering saveFtpServer: ftpServer={}, newRecordId={}, actionUser={}",
				ftpServer, newRecordId, actionUser);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_FTP_SERVERS"
					+ " (FTP_SERVER_ID, NAME, DESCRIPTION, ACTIVE, CONNECTION_TYPE,"
					+ " SERVER, PORT, FTP_USER, PASSWORD, REMOTE_DIRECTORY,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 12) + ")";

			Object[] values = {
				newRecordId,
				ftpServer.getName(),
				ftpServer.getDescription(),
				BooleanUtils.toInteger(ftpServer.isActive()),
				ftpServer.getConnectionType().getValue(),
				ftpServer.getServer(),
				ftpServer.getPort(),
				ftpServer.getUser(),
				ftpServer.getPassword(),
				ftpServer.getRemoteDirectory(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_FTP_SERVERS SET NAME=?, DESCRIPTION=?,"
					+ " ACTIVE=?, CONNECTION_TYPE=?, SERVER=?, PORT=?, FTP_USER=?,"
					+ " PASSWORD=?, REMOTE_DIRECTORY=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE FTP_SERVER_ID=?";

			Object[] values = {
				ftpServer.getName(),
				ftpServer.getDescription(),
				BooleanUtils.toInteger(ftpServer.isActive()),
				ftpServer.getConnectionType().getValue(),
				ftpServer.getServer(),
				ftpServer.getPort(),
				ftpServer.getUser(),
				ftpServer.getPassword(),
				ftpServer.getRemoteDirectory(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				ftpServer.getFtpServerId()
			};

			affectedRows = dbService.update(sql, values);
		}

		if (newRecordId != null) {
			ftpServer.setFtpServerId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, ftpServer={}",
					affectedRows, newRecord, ftpServer);
		}
	}

	/**
	 * Updates multiple ftp servers
	 *
	 * @param multipleFtpServerEdit the multiple ftp server edit object
	 * @param actionUser the user who is performing the edit
	 * @throws SQLException
	 */
	@CacheEvict(value = "ftpServers", allEntries = true)
	public void updateFtpServers(MultipleFtpServerEdit multipleFtpServerEdit, User actionUser)
			throws SQLException {

		logger.debug("Entering updateFtpServers: multipleFtpServerEdit={}, actionUser={}",
				multipleFtpServerEdit, actionUser);

		String sql;

		String[] ids = StringUtils.split(multipleFtpServerEdit.getIds(), ",");
		if (!multipleFtpServerEdit.isActiveUnchanged()) {
			sql = "UPDATE ART_FTP_SERVERS SET ACTIVE=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE FTP_SERVER_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleFtpServerEdit.isActive()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(Arrays.asList(ids));

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

}
