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
package art.usergroup;

import art.dbutils.DbService;
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
 * Class to provide methods related to user groups
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_USER_GROUPS";

	/**
	 * Class to map resultset to an object
	 */
	private class UserGroupMapper extends BasicRowProcessor {

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
			UserGroup group = new UserGroup();

			group.setUserGroupId(rs.getInt("USER_GROUP_ID"));
			group.setName(rs.getString("NAME"));
			group.setDescription(rs.getString("DESCRIPTION"));
			group.setDefaultReportGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
			group.setStartReport(rs.getString("START_QUERY"));
			group.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			group.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));

			return type.cast(group);
		}
	}

	/**
	 * Get all user groups
	 *
	 * @return list of all user groups, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public List<UserGroup> getAllUserGroups() throws SQLException {
		logger.debug("Entering getAllUserGroups");

		ResultSetHandler<List<UserGroup>> h = new BeanListHandler<>(UserGroup.class, new UserGroupMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Get a user group
	 *
	 * @param id
	 * @return populated object if user group found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public UserGroup getUserGroup(int id) throws SQLException {
		logger.debug("Entering getUserGroup: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE USER_GROUP_ID = ? ";
		ResultSetHandler<UserGroup> h = new BeanHandler<>(UserGroup.class, new UserGroupMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Delete a user group
	 *
	 * @param id
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void deleteUserGroup(int id) throws SQLException {
		logger.debug("Entering deleteUserGroup: id={}", id);

		String sql = "DELETE FROM ART_USER_GROUPS WHERE USER_GROUP_ID=?";
		int affectedRows = dbService.update(sql, id);
		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with delete. affectedRows={}, id={}", affectedRows, id);
		}
	}

	/**
	 * Add a new user group to the database
	 *
	 * @param group
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void addUserGroup(UserGroup group) throws SQLException {
		logger.debug("Entering addUserGroup: group={}", group);

		int newId = allocateNewId();
		logger.debug("newId={}", newId);

		if (newId > 0) {
			group.setUserGroupId(newId);
			try {
				saveUserGroup(group, true);
			} catch (SQLException ex) {
				//delete placeholder for new record
				logger.debug("Deleting placeholder for new record");
				deleteUserGroup(newId);

				//rethrow exception
				throw ex;
			}
		} else {
			logger.warn("Add failed. Allocate new ID failed. group={}", group);
		}
	}

	/**
	 * Update an existing user group
	 *
	 * @param group
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void updateUserGroup(UserGroup group) throws SQLException {
		logger.debug("Entering updateUserGroup: group={}", group);

		saveUserGroup(group, false);
	}

	/**
	 * Save a user group
	 *
	 * @param group
	 * @param newRecord true if this is a new record, false otherwise
	 * @throws SQLException
	 */
	private void saveUserGroup(UserGroup group, boolean newRecord) throws SQLException {
		logger.debug("Entering saveUserGroup: group={}, newRecord={}", group, newRecord);

		String dateColumn;

		if (newRecord) {
			dateColumn = "CREATION_DATE=?";
		} else {
			dateColumn = "UPDATE_DATE=?";
		}

		String sql = "UPDATE ART_USER_GROUPS SET NAME=?, DESCRIPTION=?,"
				+ " DEFAULT_QUERY_GROUP=?, START_QUERY=?,"
				+ dateColumn
				+ " WHERE USER_GROUP_ID=?";

		Object[] values = {
			group.getName(),
			group.getDescription(),
			group.getDefaultReportGroup(),
			group.getStartReport(),
			DbUtils.getCurrentTimeStamp(),
			group.getUserGroupId()
		};

		int affectedRows = dbService.update(sql, values);
		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, group={}, newRecord={}", group, newRecord);
		}
	}

	/**
	 * Generate an id and record for a new item
	 *
	 * @return new id generated, 0 otherwise
	 * @throws SQLException
	 */
	private synchronized int allocateNewId() throws SQLException {
		logger.debug("Entering allocateNewId");

		int newId = 0;

		Connection conn = ArtConfig.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement psInsert = null;

		if (conn == null) {
			logger.warn("Connection to the ART Database not available");
			return 0;
		}

		try {
			//generate new id
			String sql = "SELECT MAX(USER_GROUP_ID) FROM ART_USER_GROUPS";
			rs = DbUtils.query(conn, ps, sql);
			if (rs.next()) {
				newId = rs.getInt(1) + 1;
				logger.debug("newId={}", newId);

				//add dummy record with new id. fill all not null columns
				//name has unique constraint so use a random default value
				String allocatingName = "allocating-" + RandomStringUtils.randomAlphanumeric(3);
				sql = "INSERT INTO ART_USER_GROUPS"
						+ " (USER_GROUP_ID,NAME)"
						+ " VALUES(?,?)";

				Object[] values = {
					newId,
					allocatingName
				};

				int affectedRows = DbUtils.update(conn, psInsert, sql, values);
				logger.debug("affectedRows={}", affectedRows);

				if (affectedRows != 1) {
					logger.warn("Problem with allocateNewId. affectedRows={}, newId={}", affectedRows, newId);
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
