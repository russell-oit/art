/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.usergroupmembership;

import art.dbutils.DbService;
import art.user.User;
import art.usergroup.UserGroup;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Provides methods for updating and deleting user group memberships
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupMembershipService2 {
	//use second class to avoid circular dependency with UserService

	private static final Logger logger = LoggerFactory.getLogger(UserGroupMembershipService2.class);

	private final DbService dbService;

	@Autowired
	public UserGroupMembershipService2(DbService dbService) {
		this.dbService = dbService;
	}

	public UserGroupMembershipService2() {
		dbService = new DbService();
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
		logger.debug("Entering deleteUserGroupMembership: userId={},"
				+ " userGroupId={}", userId, userGroupId);

		String sql;

		sql = "DELETE FROM ART_USER_USERGROUP_MAP WHERE USER_ID=? AND USER_GROUP_ID=?";
		dbService.update(sql, userId, userGroupId);
	}

	/**
	 * Recreates user group membership records for a given user
	 *
	 * @param user the user
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "userGroups"}, allEntries = true)
	public void recreateUserGroupMemberships(User user) throws SQLException {
		Connection conn = null;
		recreateUserGroupMemberships(user, conn);
	}

	/**
	 * Recreates user group membership records for a given user
	 *
	 * @param user the user
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "userGroups"}, allEntries = true)
	public void recreateUserGroupMemberships(User user, Connection conn) throws SQLException {
		logger.debug("Entering recreateUserGroupMemberships: user={}", user);

		deleteAllUserGroupMembershipsForUser(user.getUserId(), conn);
		addUserGroupMemberships(user, user.getUserGroups(), conn);
	}

	/**
	 * Delete all user group memberships for the given user
	 *
	 * @param userId the user id
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	private void deleteAllUserGroupMembershipsForUser(int userId, Connection conn) throws SQLException {
		logger.debug("Entering deleteAllUserGroupMembershipsForUser: userId={}", userId);

		String sql = "DELETE FROM ART_USER_USERGROUP_MAP WHERE USER_ID=?";
		if (conn == null) {
			dbService.update(sql, userId);
		} else {
			dbService.update(conn, sql, userId);
		}
	}

	/**
	 * Adds user group memberships for the given user
	 *
	 * @param user the user, not null
	 * @param userGroups the user groups
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	private void addUserGroupMemberships(User user, List<UserGroup> userGroups,
			Connection conn) throws SQLException {

		Objects.requireNonNull(user, "user must not be null");

		if (CollectionUtils.isEmpty(userGroups)) {
			return;
		}

		List<Integer> userGroupIds = new ArrayList<>();
		for (UserGroup userGroup : userGroups) {
			userGroupIds.add(userGroup.getUserGroupId());
		}
		Integer[] users = {user.getUserId()};
		String action = "add";
		updateUserGroupMembership(action, users, userGroupIds.toArray(new Integer[0]));
	}

	/**
	 * Adds or removes user group memberships
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param users user ids
	 * @param userGroups user group ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "userGroups", "reports"}, allEntries = true)
	public void updateUserGroupMembership(String action, Integer[] users,
			Integer[] userGroups) throws SQLException {

		Connection conn = null;
		updateUserGroupMembership(action, users, userGroups, conn);
	}

	/**
	 * Adds or removes user group memberships
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param users user ids
	 * @param userGroups user group ids
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "userGroups", "reports"}, allEntries = true)
	public void updateUserGroupMembership(String action, Integer[] users,
			Integer[] userGroups, Connection conn) throws SQLException {

		logger.debug("Entering updateUserGroupMemberships: action='{}'", action);

		logger.debug("(users == null) = {}", users == null);
		logger.debug("(userGroups == null) = {}", userGroups == null);
		if (users == null || userGroups == null) {
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
			sql = "INSERT INTO ART_USER_USERGROUP_MAP (USER_ID, USER_GROUP_ID) VALUES (?,?)";
		} else {
			sql = "DELETE FROM ART_USER_USERGROUP_MAP WHERE USER_ID=? AND USER_GROUP_ID=?";
		}

		String sqlTest = "UPDATE ART_USER_USERGROUP_MAP SET USER_ID=? WHERE USER_ID=? AND USER_GROUP_ID=?";

		for (Integer userId : users) {
			for (Integer userGroupId : userGroups) {
				boolean updateRight = true;
				int affectedRows;
				if (add) {
					//test if record exists. to avoid integrity constraint error
					if (conn == null) {
						affectedRows = dbService.update(sqlTest, userId, userId, userGroupId);
					} else {
						affectedRows = dbService.update(conn, sqlTest, userId, userId, userGroupId);
					}

					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRight = false;
					}
				}
				if (updateRight) {
					if (conn == null) {
						dbService.update(sql, userId, userGroupId);
					} else {
						dbService.update(conn, sql, userId, userGroupId);
					}
				}
			}
		}
	}

}
