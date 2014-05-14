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
import art.dbutils.DbUtils;
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
 * Class to provide methods related to user groups
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_USER_GROUPS AUG";

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
			group.setCreatedBy(rs.getString("CREATED_BY"));
			group.setUpdatedBy(rs.getString("UPDATED_BY"));

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
	 * @return populated object if found, null otherwise
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
	 * Get user groups that a given user belongs to
	 *
	 * @param userId
	 * @return list of the user's user groups, empty list otherwise
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
	 * Delete a user group
	 *
	 * @param id
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
	 * Add a new user group to the database
	 *
	 * @param group
	 * @param actionUser
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

		sql = "INSERT INTO ART_USER_GROUPS"
				+ " (USER_GROUP_ID, NAME, DESCRIPTION, DEFAULT_QUERY_GROUP,"
				+ " START_QUERY, CREATION_DATE, CREATED_BY)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 7) + ")";

		Object[] values = {
			newId,
			group.getName(),
			group.getDescription(),
			group.getDefaultReportGroup(),
			group.getStartReport(),
			DbUtils.getCurrentTimeStamp(),
			actionUser.getUsername()
		};

		dbService.update(sql, values);

		return newId;
	}

	/**
	 * Update an existing user group
	 *
	 * @param group
	 * @param actionUser
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void updateUserGroup(UserGroup group, User actionUser) throws SQLException {
		logger.debug("Entering updateUserGroup: group={}, actionUser={}", group, actionUser);

		String sql = "UPDATE ART_USER_GROUPS SET NAME=?, DESCRIPTION=?,"
				+ " DEFAULT_QUERY_GROUP=?, START_QUERY=?, UPDATE_DATE=?, UPDATED_BY=?"
				+ " WHERE USER_GROUP_ID=?";

		Object[] values = {
			group.getName(),
			group.getDescription(),
			group.getDefaultReportGroup(),
			group.getStartReport(),
			DbUtils.getCurrentTimeStamp(),
			actionUser.getUsername(),
			group.getUserGroupId()
		};

		dbService.update(sql, values);
	}
}
