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
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.user.User;
import art.utils.ActionResult;
import art.utils.ArtUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
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
	private final ReportGroupService reportGroupService;

	@Autowired
	public UserGroupService(DbService dbService, ReportGroupService reportGroupService) {
		this.dbService = dbService;
		this.reportGroupService = reportGroupService;
	}

	public UserGroupService() {
		dbService = new DbService();
		reportGroupService = new ReportGroupService();
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
			group.setStartReport(rs.getString("START_QUERY"));
			group.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			group.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			group.setCreatedBy(rs.getString("CREATED_BY"));
			group.setUpdatedBy(rs.getString("UPDATED_BY"));

			ReportGroup defaultReportGroup = reportGroupService.getReportGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
			group.setDefaultReportGroup(defaultReportGroup);

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
	 * Returns user groups with given ids
	 *
	 * @param ids comma separated string of the user group ids to retrieve
	 * @return user groups with given ids
	 * @throws SQLException
	 */
	public List<UserGroup> getUserGroups(String ids) throws SQLException {
		logger.debug("Entering getUserGroups: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		String sql = SQL_SELECT_ALL
				+ " WHERE USER_GROUP_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<UserGroup>> h = new BeanListHandler<>(UserGroup.class, new UserGroupService.UserGroupMapper());
		return dbService.query(sql, h, idsArray);
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
	 * @return ActionResult. if not successful, data contains a list of linked
	 * users which prevented the user group from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "users"}, allEntries = true)
	public ActionResult deleteUserGroup(int id) throws SQLException {
		logger.debug("Entering deleteUserGroup: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedUsers = getLinkedUsers(id);
		if (!linkedUsers.isEmpty()) {
			result.setData(linkedUsers);
			return result;
		}

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

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes the user groups with the given ids
	 *
	 * @param ids the user group ids
	 * @return ActionResult. if not successful, data contains details of the
	 * user groups that were not deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "users"}, allEntries = true)
	public ActionResult deleteUserGroups(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteUserGroups: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteUserGroup(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedUsers = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedUsers, ", ");
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
		int newId = dbService.getNewRecordId(sql);

		saveUserGroup(group, newId, actionUser);

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

		Integer newRecordId = null;
		saveUserGroup(group, newRecordId, actionUser);
	}

	/**
	 * Imports user group records
	 *
	 * @param userGroups the list of user groups to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void importUserGroups(List<UserGroup> userGroups, User actionUser,
			Connection conn) throws SQLException {

		logger.debug("Entering importUserGroups: actionUser={}", actionUser);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(USER_GROUP_ID) FROM ART_USER_GROUPS";
			int userGroupId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(QUERY_GROUP_ID) FROM ART_QUERY_GROUPS";
			int reportGroupId = dbService.getMaxRecordId(conn, sql);

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			for (UserGroup userGroup : userGroups) {
				userGroupId++;
				ReportGroup defaultReportGroup = userGroup.getDefaultReportGroup();
				if (defaultReportGroup != null && StringUtils.isNotBlank(defaultReportGroup.getName())) {
					ReportGroup existingReportGroup = reportGroupService.getReportGroup(defaultReportGroup.getName());
					if (existingReportGroup == null) {
						reportGroupId++;
						reportGroupService.saveReportGroup(defaultReportGroup, reportGroupId, actionUser, conn);
					} else {
						userGroup.setDefaultReportGroup(existingReportGroup);
					}
				}
				saveUserGroup(userGroup, userGroupId, actionUser, conn);
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
	 * Saves a user group
	 *
	 * @param userGroup the user group to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveUserGroup(UserGroup userGroup, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveUserGroup(userGroup, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a user group
	 *
	 * @param group the user group
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	private void saveUserGroup(UserGroup group, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering saveUserGroup: group={}, newRecordId={},"
				+ " actionUser={}", group, newRecordId, actionUser);

		//set values for possibly null property objects
		Integer defaultReportGroupId = null;
		if (group.getDefaultReportGroup() != null) {
			defaultReportGroupId = group.getDefaultReportGroup().getReportGroupId();
			if (defaultReportGroupId == 0) {
				defaultReportGroupId = null;
			}
		}

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_USER_GROUPS"
					+ " (USER_GROUP_ID, NAME, DESCRIPTION, DEFAULT_QUERY_GROUP,"
					+ " START_QUERY, CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 7) + ")";

			Object[] values = {
				newRecordId,
				group.getName(),
				group.getDescription(),
				defaultReportGroupId,
				group.getStartReport(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_USER_GROUPS SET NAME=?, DESCRIPTION=?,"
					+ " DEFAULT_QUERY_GROUP=?, START_QUERY=?, UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE USER_GROUP_ID=?";

			Object[] values = {
				group.getName(),
				group.getDescription(),
				defaultReportGroupId,
				group.getStartReport(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				group.getUserGroupId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		if (newRecordId != null) {
			group.setUserGroupId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, group={}",
					affectedRows, newRecord, group);
		}
	}

	/**
	 * Returns users that are in a given user group
	 *
	 * @param userGroupId the user group id
	 * @return linked user names
	 * @throws SQLException
	 */
	public List<String> getLinkedUsers(int userGroupId) throws SQLException {
		logger.debug("Entering getLinkedUsers: userGroupId={}", userGroupId);

		String sql = "SELECT AU.USERNAME"
				+ " FROM ART_USERS AU"
				+ " INNER JOIN ART_USER_GROUP_ASSIGNMENT AUGA"
				+ " ON AU.USER_ID=AUGA.USER_ID"
				+ " WHERE AUGA.USER_GROUP_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>("USERNAME");
		return dbService.query(sql, h, userGroupId);
	}
}
