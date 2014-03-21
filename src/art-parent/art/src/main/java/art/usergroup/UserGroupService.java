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
	 * @return new record id if operation is successful, 0 otherwise
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public synchronized int addUserGroup(UserGroup group) throws SQLException {
		logger.debug("Entering addUserGroup: group={}", group);

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
				+ " START_QUERY, CREATION_DATE)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 6) + ")";

		Object[] values = {
			newId,
			group.getName(),
			group.getDescription(),
			group.getDefaultReportGroup(),
			group.getStartReport(),
			DbUtils.getCurrentTimeStamp()
		};

		int affectedRows = dbService.update(sql, values);
		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with add. affectedRows={}, group={}", affectedRows, group);
		}
		
		group.setUserGroupId(newId);
		return newId;
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

		String sql = "UPDATE ART_USER_GROUPS SET NAME=?, DESCRIPTION=?,"
				+ " DEFAULT_QUERY_GROUP=?, START_QUERY=?, UPDATE_DATE=?"
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
			logger.warn("Problem with update. affectedRows={}, group={}", affectedRows, group);
		}
	}
}
