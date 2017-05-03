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
package art.usergroup;

import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.user.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, deleting and updating user groups
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupService.class);

	private final DbService dbService;

	@Autowired
	public UserGroupService(DbService dbService) {
		this.dbService = dbService;
	}

	public UserGroupService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_USER_GROUPS AUG";

	/**
	 * Maps a resultset to an object
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
			group.setCreatedBy(rs.getString("CREATED_BY"));
			group.setUpdatedBy(rs.getString("UPDATED_BY"));

			return type.cast(group);
		}
	}

	/**
	 * Returns all user groups
	 *
	 * @return all user groups
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public List<UserGroup> getAllUserGroups() throws SQLException {
		logger.debug("Entering getAllUserGroups");

		ResultSetHandler<List<UserGroup>> h = new BeanListHandler<>(UserGroup.class, new UserGroupMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns the user group with the given id
	 *
	 * @param id the user group id
	 * @return the user group if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public UserGroup getUserGroup(int id) throws SQLException {
		logger.debug("Entering getUserGroup: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE USER_GROUP_ID=?";
		ResultSetHandler<UserGroup> h = new BeanHandler<>(UserGroup.class, new UserGroupMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns the user groups that the given user belongs to
	 *
	 * @param userId the user id
	 * @return the user's user groups
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public List<UserGroup> getUserGroupsForUser(int userId) throws SQLException {
		logger.debug("Entering getUserGroupsForUser: userId={}", userId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_USER_GROUP_ASSIGNMENT AUGA "
				+ " ON AUGA.USER_GROUP_ID=AUG.USER_GROUP_ID"
				+ " WHERE AUGA.USER_ID=?"
				+ " ORDER BY AUG.USER_GROUP_ID"; //have order by so that effective values are deterministic
		ResultSetHandler<List<UserGroup>> h = new BeanListHandler<>(UserGroup.class, new UserGroupMapper());
		return dbService.query(sql, h, userId);
	}

	/**
	 * Deletes the user group with the given id
	 *
	 * @param id the user group id
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void deleteUserGroup(int id) throws SQLException {
		logger.debug("Entering deleteUserGroup: id={}", id);

		String sql;

		//delete foreign key records
		sql = "DELETE FROM ART_USER_GROUP_RULES WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_JOBS WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_QUERIES WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_GROUPS WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_JOBS WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);

		//finally delete user group
		sql = "DELETE FROM ART_USER_GROUPS WHERE USER_GROUP_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes the user groups with the given ids
	 *
	 * @param ids the user group ids
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void deleteUserGroups(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteUserGroups: ids={}", (Object)ids);

		for (Integer id : ids) {
			deleteUserGroup(id);
		}
	}

	/**
	 * Adds a new user group
	 *
	 * @param group the user group
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public synchronized int addUserGroup(UserGroup group, User actionUser) throws SQLException {
		logger.debug("Entering addUserGroup: group={}, actionUser={}", group, actionUser);

		//generate new id
		String sql = "SELECT MAX(USER_GROUP_ID) FROM ART_USER_GROUPS";
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

		group.setUserGroupId(newId);

		boolean newRecord = true;
		saveUserGroup(group, newRecord, actionUser);

		return newId;
	}

	/**
	 * Updates a user group
	 *
	 * @param group the updated user group
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void updateUserGroup(UserGroup group, User actionUser) throws SQLException {
		logger.debug("Entering updateUserGroup: group={}, actionUser={}", group, actionUser);

		boolean newRecord = false;
		saveUserGroup(group, newRecord, actionUser);
	}

	/**
	 * Saves a user group
	 *
	 * @param group the user group
	 * @param newRecord whether this is a new record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	private void saveUserGroup(UserGroup group, boolean newRecord, User actionUser) throws SQLException {
		logger.debug("Entering saveUserGroup: group={}, newRecord={}, actionUser={}",
				group, newRecord, actionUser);

		int affectedRows;
		if (newRecord) {
			String sql = "INSERT INTO ART_USER_GROUPS"
					+ " (USER_GROUP_ID, NAME, DESCRIPTION, DEFAULT_QUERY_GROUP,"
					+ " START_QUERY, CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 7) + ")";

			Object[] values = {
				group.getUserGroupId(),
				group.getName(),
				group.getDescription(),
				group.getDefaultReportGroup(),
				group.getStartReport(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_USER_GROUPS SET NAME=?, DESCRIPTION=?,"
					+ " DEFAULT_QUERY_GROUP=?, START_QUERY=?, UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE USER_GROUP_ID=?";

			Object[] values = {
				group.getName(),
				group.getDescription(),
				group.getDefaultReportGroup(),
				group.getStartReport(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				group.getUserGroupId()
			};

			affectedRows = dbService.update(sql, values);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, group={}",
					affectedRows, newRecord, group);
		}
	}
}
