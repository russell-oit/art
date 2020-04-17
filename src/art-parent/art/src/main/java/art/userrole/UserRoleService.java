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
package art.userrole;

import art.dbutils.DbService;
import art.role.Role;
import art.user.User;
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
 * Provides methods for retrieving, adding, updating and deleting user-role
 * records
 *
 * @author Timothy Anyona
 */
@Service
public class UserRoleService {

	private static final Logger logger = LoggerFactory.getLogger(UserRoleService.class);

	private final DbService dbService;

	@Autowired
	public UserRoleService(DbService dbService) {
		this.dbService = dbService;
	}

	public UserRoleService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL
			= "SELECT AU.USER_ID, AU.USERNAME, AR.ROLE_ID, AR.NAME AS ROLE_NAME"
			+ " FROM ART_USER_ROLE_MAP AURM"
			+ " INNER JOIN ART_USERS AU ON"
			+ " AURM.USER_ID=AU.USER_ID"
			+ " INNER JOIN ART_ROLES AR ON"
			+ " AURM.ROLE_ID=AR.ROLE_ID";

	/**
	 * Maps a resultset to an object
	 */
	private class UserRoleMapper extends BasicRowProcessor {

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
			UserRole userRole = new UserRole();

			User user = new User();
			user.setUserId(rs.getInt("USER_ID"));
			user.setUsername(rs.getString("USERNAME"));

			userRole.setUser(user);

			Role role = new Role();
			role.setRoleId(rs.getInt("ROLE_ID"));
			role.setName(rs.getString("ROLE_NAME"));

			userRole.setRole(role);

			return type.cast(userRole);
		}
	}

	/**
	 * Returns all user roles
	 *
	 * @return all user roles
	 * @throws SQLException
	 */
	public List<UserRole> getAllUserRoles() throws SQLException {
		logger.debug("Entering getAllUserRoles");

		ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class, new UserRoleMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns the user roles for a given user
	 *
	 * @param userId the id of the user
	 * @return user roles for a given user
	 * @throws SQLException
	 */
	public List<UserRole> getUserRolesForUser(int userId) throws SQLException {
		logger.debug("Entering getUserRolesForUser: userId={}", userId);

		String sql = SQL_SELECT_ALL + " WHERE AU.USER_ID=?";
		ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class, new UserRoleMapper());
		return dbService.query(sql, h, userId);
	}

	/**
	 * Returns the user roles for a given role
	 *
	 * @param roleId the id of the role
	 * @return user roles for a given role
	 * @throws SQLException
	 */
	public List<UserRole> getUserRolesForRole(int roleId) throws SQLException {
		logger.debug("Entering getUserRolesForRole: roleId={}", roleId);

		String sql = SQL_SELECT_ALL + " WHERE AR.ROLE_ID=?";
		ResultSetHandler<List<UserRole>> h = new BeanListHandler<>(UserRole.class, new UserRoleMapper());
		return dbService.query(sql, h, roleId);
	}

	/**
	 * Deletes a user role
	 *
	 * @param userId the user id
	 * @param roleId the role id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"roles", "users"}, allEntries = true)
	public void deleteUserRole(int userId, int roleId) throws SQLException {
		logger.debug("Entering deleteUserRole: userId={}, roleId={}", userId, roleId);

		String sql;

		sql = "DELETE FROM ART_USER_ROLE_MAP WHERE USER_ID=? AND ROLE_ID=?";
		dbService.update(sql, userId, roleId);
	}

	/**
	 * Recreates user-role records for a given user
	 *
	 * @param user the user
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "roles"}, allEntries = true)
	public void recreateUserRoles(User user) throws SQLException {
		Connection conn = null;
		recreateUserRoles(user, conn);
	}

	/**
	 * Recreates user-role records for a given user
	 *
	 * @param user the user
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "roles"}, allEntries = true)
	public void recreateUserRoles(User user, Connection conn) throws SQLException {
		logger.debug("Entering recreateUserRoles: user={}", user);

		int userId = user.getUserId();

		deleteAllRolesForUser(userId, conn);
		addUserRoles(userId, user.getRoles(), conn);
	}

	/**
	 * Delete all user-role records for the given user
	 *
	 * @param userId the user id
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	private void deleteAllRolesForUser(int userId, Connection conn) throws SQLException {
		logger.debug("Entering deleteAllRolesForUser: userId={}", userId);

		String sql = "DELETE FROM ART_USER_ROLE_MAP WHERE USER_ID=?";
		if (conn == null) {
			dbService.update(sql, userId);
		} else {
			dbService.update(conn, sql, userId);
		}
	}

	/**
	 * Adds user-role records for the given user
	 *
	 * @param userId the user id
	 * @param roles the roles
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	private void addUserRoles(int userId, List<Role> roles, Connection conn) throws SQLException {
		logger.debug("Entering addUserRoles: userId={}", userId);

		if (CollectionUtils.isEmpty(roles)) {
			return;
		}

		List<Integer> roleIds = new ArrayList<>();
		for (Role role : roles) {
			roleIds.add(role.getRoleId());
		}

		Integer[] users = {userId};
		String action = "add";
		updateUserRoles(action, users, roleIds.toArray(new Integer[0]), conn);
	}

	/**
	 * Adds or removes user-role records
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param users user ids
	 * @param roles role ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "roles"}, allEntries = true)
	public void updateUserRoles(String action, Integer[] users, Integer[] roles) throws SQLException {
		Connection conn = null;
		updateUserRoles(action, users, roles, conn);
	}

	/**
	 * Adds or removes user-role records
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param users user ids
	 * @param roles role ids
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "roles"}, allEntries = true)
	public void updateUserRoles(String action, Integer[] users, Integer[] roles,
			Connection conn) throws SQLException {

		logger.debug("Entering updateUserRoles: action='{}'", action);

		logger.debug("(users == null) = {}", users == null);
		logger.debug("(roles == null) = {}", roles == null);
		if (users == null || roles == null) {
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
			sql = "INSERT INTO ART_USER_ROLE_MAP (USER_ID, ROLE_ID) VALUES (?, ?)";
		} else {
			sql = "DELETE FROM ART_USER_ROLE_MAP WHERE USER_ID=? AND ROLE_ID=?";
		}

		String sqlTest = "UPDATE ART_USER_ROLE_MAP SET USER_ID=? WHERE USER_ID=? AND ROLE_ID=?";
		int affectedRows;
		boolean updateRecord;

		for (Integer userId : users) {
			for (Integer roleId : roles) {
				updateRecord = true;
				if (add) {
					//test if record exists. to avoid integrity constraint error
					if (conn == null) {
						affectedRows = dbService.update(sqlTest, userId, userId, roleId);
					} else {
						affectedRows = dbService.update(conn, sqlTest, userId, userId, roleId);
					}

					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRecord = false;
					}
				}
				if (updateRecord) {
					if (conn == null) {
						dbService.update(sql, userId, roleId);
					} else {
						dbService.update(conn, sql, userId, roleId);
					}
				}
			}
		}
	}

}
