/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.usergroupmembership;

import art.dbutils.DbService;
import art.user.User;
import art.user.UserService;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, updating and deleting user group memberships
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupMembershipService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupMembershipService.class);

	@Autowired
	private DbService dbService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_USER_GROUP_ASSIGNMENT";

	/**
	 * Maps a resultset to an object
	 */
	private class UserGroupMembershipMapper extends BasicRowProcessor {

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
			UserGroupMembership membership = new UserGroupMembership();

			membership.setUser(userService.getUser(rs.getInt("USER_ID")));
			membership.setUserGroup(userGroupService.getUserGroup(rs.getInt("USER_GROUP_ID")));

			return type.cast(membership);
		}
	}

	/**
	 * Returns all user group memberships
	 *
	 * @return all user group memberships
	 * @throws SQLException
	 */
	public List<UserGroupMembership> getAllUserGroupMemberships() throws SQLException {
		logger.debug("Entering getAllUserGroupMemberships");

		ResultSetHandler<List<UserGroupMembership>> h = new BeanListHandler<>(UserGroupMembership.class, new UserGroupMembershipMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Deletes a user group membership
	 *
	 * @param userId the user id
	 * @param userGroupId the user group id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "userGroups"}, allEntries = true)
	public void deleteUserGroupMembership(int userId, int userGroupId) throws SQLException {
		logger.debug("Entering deleteUserGroupMembership: userId={}, userGroupId={}",
				userId, userGroupId);

		String sql;

		sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_ID=? AND USER_GROUP_ID=?";
		dbService.update(sql, userId, userGroupId);
	}

	/**
	 * Delete all user group memberships for the given user
	 *
	 * @param userId the user id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "userGroups"}, allEntries = true)
	public void deleteAllUserGroupMembershipsForUser(int userId) throws SQLException {
		logger.debug("Entering deleteAllUserGroupMembershipsForUser: userId={}", userId);

		String sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_ID=?";
		dbService.update(sql, userId);
	}

	/**
	 * Adds user group memberships for the given user
	 * 
	 * @param user the user, not null
	 * @param userGroups the user groups
	 * @throws SQLException 
	 */
	@CacheEvict(value = {"users", "userGroups"}, allEntries = true)
	public void addUserGroupMemberships(User user, List<UserGroup> userGroups) throws SQLException {
		Objects.requireNonNull(user, "user must not be null");
		
		if (userGroups == null || userGroups.isEmpty()) {
			return;
		}

		List<Integer> userGroupIds = new ArrayList<>();
		for (UserGroup userGroup : userGroups) {
			userGroupIds.add(userGroup.getUserGroupId());
		}
		String[] users = {user.getUserId() + "-" + user.getUsername()};
		String action = "add";
		updateUserGroupMembership(action, users, userGroupIds.toArray(new Integer[0]));
	}

	/**
	 * Adds or removes user group memberships
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param users user identifiers in the format user id-username
	 * @param userGroups user group ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "userGroups"}, allEntries = true)
	public void updateUserGroupMembership(String action, String[] users, Integer[] userGroups) throws SQLException {
		logger.debug("Entering updateUserGroupMemberships: action='{}'", action);

		logger.debug("(users == null) = {}", users == null);
		logger.debug("(userGroups == null) = {}", userGroups == null);
		if (users == null || userGroups == null) {
			logger.warn("Update not performed. users or userGroups is null.");
			return;
		}

		boolean add;
		if (StringUtils.equalsIgnoreCase(action, "add")) {
			add = true;
		} else {
			add = false;
		}

		String sql;

		if (add) {
			sql = "INSERT INTO ART_USER_GROUP_ASSIGNMENT (USER_ID, USERNAME, USER_GROUP_ID) VALUES (?, ?, ?)";
		} else {
			sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_ID=? AND USERNAME=? AND USER_GROUP_ID=?";
		}

		String sqlTest = "UPDATE ART_USER_GROUP_ASSIGNMENT SET USER_ID=? WHERE USER_ID=? AND USERNAME=? AND USER_GROUP_ID=?";
		int affectedRows;
		boolean updateRight;

		for (String user : users) {
			Integer userId = Integer.valueOf(StringUtils.substringBefore(user, "-"));
			//username won't be needed once user id columns completely replace username in foreign keys
			String username = StringUtils.substringAfter(user, "-");

			for (Integer userGroupId : userGroups) {
				updateRight = true;
				if (add) {
					//test if record exists. to avoid integrity constraint error
					affectedRows = dbService.update(sqlTest, userId, userId, username, userGroupId);
					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRight = false;
					}
				}
				if (updateRight) {
					dbService.update(sql, userId, username, userGroupId);
				}
			}
		}
	}
}
