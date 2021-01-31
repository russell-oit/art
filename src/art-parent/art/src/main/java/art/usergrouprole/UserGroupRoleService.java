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
package art.usergrouprole;

import art.dbutils.DbService;
import art.role.Role;
import art.usergroup.UserGroup;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
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
 * Provides methods for retrieving, adding, updating and deleting user
 * group-role records
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupRoleService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupRoleService.class);

	private final DbService dbService;

	@Autowired
	public UserGroupRoleService(DbService dbService) {
		this.dbService = dbService;
	}

	public UserGroupRoleService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL
			= "SELECT AUG.USER_GROUP_ID, AUG.NAME AS USER_GROUP_NAME, AR.ROLE_ID, AR.NAME AS ROLE_NAME"
			+ " FROM ART_USER_GROUP_ROLE_MAP AUGRM"
			+ " INNER JOIN ART_USER_GROUPS AUG ON"
			+ " AUGRM.USER_GROUP_ID=AUG.USER_GROUP_ID"
			+ " INNER JOIN ART_ROLES AR ON"
			+ " AUGRM.ROLE_ID=AR.ROLE_ID";

	/**
	 * Maps a resultset to an object
	 */
	private class UserGroupRoleMapper extends BasicRowProcessor {

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
			UserGroupRole userGroupRole = new UserGroupRole();

			UserGroup userGroup = new UserGroup();
			userGroup.setUserGroupId(rs.getInt("USER_GROUP_ID"));
			userGroup.setName(rs.getString("USER_GROUP_NAME"));

			userGroupRole.setUserGroup(userGroup);

			Role role = new Role();
			role.setRoleId(rs.getInt("ROLE_ID"));
			role.setName(rs.getString("ROLE_NAME"));

			userGroupRole.setRole(role);

			return type.cast(userGroupRole);
		}
	}

	/**
	 * Returns all user group roles
	 *
	 * @return all user group roles
	 * @throws SQLException
	 */
	public List<UserGroupRole> getAllUserGroupRoles() throws SQLException {
		logger.debug("Entering getAllUserGroupRoles");

		ResultSetHandler<List<UserGroupRole>> h = new BeanListHandler<>(UserGroupRole.class, new UserGroupRoleMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns the user group roles for a given user group
	 *
	 * @param userGroupId the id of the user group
	 * @return user group roles for a given user group
	 * @throws SQLException
	 */
	public List<UserGroupRole> getUserGroupRolesForUserGroup(int userGroupId) throws SQLException {
		logger.debug("Entering getUserGroupRolesForUserGroup: userGroupId={}", userGroupId);

		String sql = SQL_SELECT_ALL + " WHERE AUG.USER_GROUP_ID=?";
		ResultSetHandler<List<UserGroupRole>> h = new BeanListHandler<>(UserGroupRole.class, new UserGroupRoleMapper());
		return dbService.query(sql, h, userGroupId);
	}

	/**
	 * Returns the user group roles for a given role
	 *
	 * @param roleId the id of the role
	 * @return user group roles for a given role
	 * @throws SQLException
	 */
	public List<UserGroupRole> getUserGroupRolesForRole(int roleId) throws SQLException {
		logger.debug("Entering getUserGroupRolesForRole: roleId={}", roleId);

		String sql = SQL_SELECT_ALL + " WHERE AR.ROLE_ID=?";
		ResultSetHandler<List<UserGroupRole>> h = new BeanListHandler<>(UserGroupRole.class, new UserGroupRoleMapper());
		return dbService.query(sql, h, roleId);
	}

	/**
	 * Deletes a user group role
	 *
	 * @param userGroupId the user group id
	 * @param roleId the role id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"roles", "userGroups", "users", "reports", "reportGroups", "datasources"}, allEntries = true)
	public void deleteUserGroupRole(int userGroupId, int roleId) throws SQLException {
		logger.debug("Entering deleteUserGroupRole: userGroupId={}, roleId={}",
				userGroupId, roleId);

		String sql;

		sql = "DELETE FROM ART_USER_GROUP_ROLE_MAP WHERE USER_GROUP_ID=? AND ROLE_ID=?";
		dbService.update(sql, userGroupId, roleId);
	}

	/**
	 * Recreates user group-role records for a given user group
	 *
	 * @param userGroup the user group
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "roles", "users", "reports", "reportGroups", "datasources"}, allEntries = true)
	public void recreateUserGroupRoles(UserGroup userGroup) throws SQLException {
		Connection conn = null;
		recreateUserGroupRoles(userGroup, conn);
	}

	/**
	 * Recreates user group-role records for a given user group
	 *
	 * @param userGroup the user group
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "roles", "users", "reports", "reportGroups", "datasources"}, allEntries = true)
	public void recreateUserGroupRoles(UserGroup userGroup, Connection conn) throws SQLException {
		logger.debug("Entering recreateUserGroupRoles: userGroup={}", userGroup);

		int userGroupId = userGroup.getUserGroupId();

		deleteAllRolesForUserGroup(userGroupId, conn);
		addUserGroupRoles(userGroupId, userGroup.getRoles(), conn);
	}

	/**
	 * Delete all user group-role records for the given user group
	 *
	 * @param userGroupId the user group id
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	private void deleteAllRolesForUserGroup(int userGroupId, Connection conn) throws SQLException {
		logger.debug("Entering deleteAllRolesForUserGroup: userGroupId={}", userGroupId);

		String sql = "DELETE FROM ART_USER_GROUP_ROLE_MAP WHERE USER_GROUP_ID=?";
		dbService.update(conn, sql, userGroupId);
	}

	/**
	 * Adds user group-role records for the given user group
	 *
	 * @param userGroupId the user group id
	 * @param roles the roles
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	public void addUserGroupRoles(int userGroupId, List<Role> roles,
			Connection conn) throws SQLException {

		logger.debug("Entering addUserGroupRoles: userGroupId={}", userGroupId);

		if (CollectionUtils.isEmpty(roles)) {
			return;
		}

		List<Integer> roleIds = new ArrayList<>();
		for (Role role : roles) {
			roleIds.add(role.getRoleId());
		}

		Integer[] userGroups = {userGroupId};
		String action = "add";
		updateUserGroupRoles(action, userGroups, roleIds.toArray(new Integer[0]));
	}

	/**
	 * Adds or removes user group-role records
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param userGroups user group ids
	 * @param roles role ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "roles", "users", "reports", "reportGroups", "datasources"}, allEntries = true)
	public void updateUserGroupRoles(String action, Integer[] userGroups,
			Integer[] roles) throws SQLException {

		Connection conn = null;
		updateUserGroupRoles(action, userGroups, roles, conn);
	}

	/**
	 * Adds or removes user group-role records
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param userGroups user group ids
	 * @param roles role ids
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "roles", "users", "reports", "reportGroups", "datasources"}, allEntries = true)
	public void updateUserGroupRoles(String action, Integer[] userGroups,
			Integer[] roles, Connection conn) throws SQLException {

		logger.debug("Entering updateUserGroupRoles: action='{}'", action);

		logger.debug("(userGroups == null) = {}", userGroups == null);
		logger.debug("(roles == null) = {}", roles == null);
		if (userGroups == null || roles == null) {
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
			sql = "INSERT INTO ART_USER_GROUP_ROLE_MAP (USER_GROUP_ID, ROLE_ID) VALUES (?, ?)";
		} else {
			sql = "DELETE FROM ART_USER_GROUP_ROLE_MAP WHERE USER_GROUP_ID=? AND ROLE_ID=?";
		}

		String sqlTest = "UPDATE ART_USER_GROUP_ROLE_MAP SET USER_GROUP_ID=? WHERE USER_GROUP_ID=? AND ROLE_ID=?";
		int affectedRows;
		boolean updateRecord;

		for (Integer userGroupId : userGroups) {
			for (Integer roleId : roles) {
				updateRecord = true;
				if (add) {
					//test if record exists. to avoid integrity constraint error
					affectedRows = dbService.update(conn, sqlTest, userGroupId, userGroupId, roleId);

					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRecord = false;
					}
				}
				if (updateRecord) {
					dbService.update(conn, sql, userGroupId, roleId);
				}
			}
		}
	}

}
