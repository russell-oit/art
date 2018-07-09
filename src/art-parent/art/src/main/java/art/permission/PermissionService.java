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
package art.permission;

import art.dbutils.DbService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving and updating permissions
 *
 * @author Timothy Anyona
 */
@Service
public class PermissionService {

	private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

	private final DbService dbService;

	@Autowired
	public PermissionService(DbService dbService) {
		this.dbService = dbService;
	}

	public PermissionService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_PERMISSIONS AP";

	/**
	 * Maps a resultset to an object
	 */
	private class PermissionMapper extends BasicRowProcessor {

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
			Permission permission = new Permission();

			permission.setPermissionId(rs.getInt("PERMISSION_ID"));
			permission.setName(rs.getString("NAME"));

			return type.cast(permission);
		}
	}

	/**
	 * Returns all permissions
	 *
	 * @return all permissions
	 * @throws SQLException
	 */
	@Cacheable("permissions")
	public List<Permission> getAllPermissions() throws SQLException {
		logger.debug("Entering getAllPermissions");

		String sql = SQL_SELECT_ALL + " ORDER BY NAME";
		ResultSetHandler<List<Permission>> h = new BeanListHandler<>(Permission.class, new PermissionMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Returns a permission with the given id
	 *
	 * @param id the permission id
	 * @return permission if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("permissions")
	public Permission getPermission(int id) throws SQLException {
		logger.debug("Entering getPermission: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE PERMISSION_ID=?";
		ResultSetHandler<Permission> h = new BeanHandler<>(Permission.class, new PermissionMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns a permission with the given name
	 *
	 * @param name the permission name
	 * @return permission if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("permissions")
	public Permission getPermission(String name) throws SQLException {
		logger.debug("Entering getPermission: name='{}'", name);

		String sql = SQL_SELECT_ALL + " WHERE NAME=?";
		ResultSetHandler<Permission> h = new BeanHandler<>(Permission.class, new PermissionMapper());
		return dbService.query(sql, h, name);
	}

	/**
	 * Returns the permissions in a given role
	 *
	 * @param roleId the role id
	 * @return the permissions in a given role
	 * @throws SQLException
	 */
	@Cacheable("permissions")
	public List<Permission> getRolePermissions(int roleId) throws SQLException {
		logger.debug("Entering getRolePermissions: roleId={}", roleId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_ROLE_PERMISSION_MAP ARPM"
				+ " ON AP.PERMISSION_ID=ARPM.PERMISSION_ID"
				+ " WHERE ARPM.ROLE_ID=?";
		ResultSetHandler<List<Permission>> h = new BeanListHandler<>(Permission.class, new PermissionMapper());
		return dbService.query(sql, h, roleId);
	}

	/**
	 * Adds or removes permissions
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param users the relevant user ids
	 * @param userGroups the relevant user group ids
	 * @param roles the relevant role ids
	 * @param permissions the relevant permission ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"roles", "permissions", "users", "userGroups"}, allEntries = true)
	public void updatePermissions(String action, Integer[] users, Integer[] userGroups,
			Integer[] roles, Integer[] permissions) throws SQLException {

		logger.debug("Entering updatePermissions: action='{}'", action);

		boolean add;
		if (StringUtils.equalsIgnoreCase(action, "add")) {
			add = true;
		} else {
			add = false;
		}

		//update user permissions
		if (users != null) {
			String sqlUserRole;
			String sqlUserPermission;

			if (add) {
				sqlUserRole = "INSERT INTO ART_USER_ROLE_MAP (USER_ID, ROLE_ID) VALUES (?, ?)";
				sqlUserPermission = "INSERT INTO ART_USER_PERMISSION_MAP (USER_ID, PERMISSION_ID) VALUES (?, ?)";
			} else {
				sqlUserRole = "DELETE FROM ART_USER_ROLE_MAP WHERE USER_ID=? AND ROLE_ID=?";
				sqlUserPermission = "DELETE FROM ART_USER_PERMISSION_MAP WHERE USER_ID=? AND PERMISSION_ID=?";
			}

			String sqlTestUserRole = "UPDATE ART_USER_ROLE_MAP SET USER_ID=? WHERE USER_ID=? AND ROLE_ID=?";
			String sqlTestUserPermission = "UPDATE ART_USER_PERMISSION_MAP SET USER_ID=? WHERE USER_ID=? AND PERMISSION_ID=?";

			int affectedRows;
			boolean updateRight;

			for (Integer userId : users) {
				//update roles
				if (roles != null) {
					for (Integer roleId : roles) {
						//if you use a batch update, some drivers e.g. oracle will
						//stop after the first error. we should continue in the event of an integrity constraint error (access already added)

						updateRight = true;
						if (add) {
							//test if role exists. to avoid integrity constraint error
							affectedRows = dbService.update(sqlTestUserRole, userId, userId, roleId);
							if (affectedRows > 0) {
								//role exists. don't attempt a reinsert.
								updateRight = false;
							}
						}
						if (updateRight) {
							dbService.update(sqlUserRole, userId, roleId);
						}
					}
				}

				//update permissions
				if (permissions != null) {
					for (Integer permissionId : permissions) {
						updateRight = true;
						if (add) {
							//test if permission exists. to avoid integrity constraint error
							affectedRows = dbService.update(sqlTestUserPermission, userId, userId, permissionId);
							if (affectedRows > 0) {
								//permission exists. don't attempt a reinsert.
								updateRight = false;
							}
						}
						if (updateRight) {
							dbService.update(sqlUserPermission, userId, permissionId);
						}
					}
				}
			}
		}

		//update user group permissions
		if (userGroups != null) {
			String sqlUserGroupRole;
			String sqlUserGroupPermission;

			if (add) {
				sqlUserGroupRole = "INSERT INTO ART_USER_GROUP_ROLE_MAP (USER_GROUP_ID, ROLE_ID) VALUES (?, ?)";
				sqlUserGroupPermission = "INSERT INTO ART_USER_GROUP_PERM_MAP (USER_GROUP_ID, PERMISSION_ID) VALUES (?, ?)";
			} else {
				sqlUserGroupRole = "DELETE FROM ART_USER_GROUP_ROLE_MAP WHERE USER_GROUP_ID=? AND ROLE_ID=?";
				sqlUserGroupPermission = "DELETE FROM ART_USER_GROUP_PERM_MAP WHERE USER_GROUP_ID=? AND PERMISSION_ID=?";
			}

			String sqlTestUserGroupRole = "UPDATE ART_USER_GROUP_ROLE_MAP SET USER_GROUP_ID=? WHERE USER_GROUP_ID=? AND ROLE_ID=?";
			String sqlTestUserGroupPermission = "UPDATE ART_USER_GROUP_PERM_MAP SET USER_GROUP_ID=? WHERE USER_GROUP_ID=? AND PERMISSION_ID=?";

			int affectedRows;
			boolean updateRight;

			for (Integer userGroupId : userGroups) {
				//update roles
				if (roles != null) {
					for (Integer roleId : roles) {
						//if you use a batch update, some drivers e.g. oracle will
						//stop after the first error. we should continue in the event of an integrity constraint error (access already added)

						updateRight = true;
						if (add) {
							//test if role exists. to avoid integrity constraint error
							affectedRows = dbService.update(sqlTestUserGroupRole, userGroupId, userGroupId, roleId);
							if (affectedRows > 0) {
								//role exists. don't attempt a reinsert.
								updateRight = false;
							}
						}
						if (updateRight) {
							dbService.update(sqlUserGroupRole, userGroupId, roleId);
						}
					}
				}

				//update permissions
				if (permissions != null) {
					for (Integer permissionId : permissions) {
						updateRight = true;
						if (add) {
							//test if permission exists. to avoid integrity constraint error
							affectedRows = dbService.update(sqlTestUserGroupPermission, userGroupId, userGroupId, permissionId);
							if (affectedRows > 0) {
								//permission exists. don't attempt a reinsert.
								updateRight = false;
							}
						}
						if (updateRight) {
							dbService.update(sqlUserGroupPermission, userGroupId, permissionId);
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the permissions attached to a given user
	 *
	 * @param userId the user id
	 * @return the user's permissions
	 * @throws SQLException
	 */
	@Cacheable("permissions")
	public List<Permission> getPermissionsForUser(int userId) throws SQLException {
		logger.debug("Entering getPermissionsForUser: userId={}", userId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_USER_PERMISSION_MAP AUPM"
				+ " ON AP.PERMISSION_ID=AUPM.PERMISSION_ID"
				+ " WHERE AUPM.USER_ID=?";
		ResultSetHandler<List<Permission>> h = new BeanListHandler<>(Permission.class, new PermissionMapper());
		return dbService.query(sql, h, userId);
	}

	/**
	 * Returns the permissions attached to a given user group
	 *
	 * @param userGroupId the user group id
	 * @return the user group's permissions
	 * @throws SQLException
	 */
	@Cacheable("permissions")
	public List<Permission> getPermissionsForUserGroup(int userGroupId) throws SQLException {
		logger.debug("Entering getPermissionsForUserGroup: userGroupId={}", userGroupId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_USER_GROUP_PERM_MAP AUGPM"
				+ " ON AP.PERMISSION_ID=AUGPM.PERMISSION_ID"
				+ " WHERE AUGPM.USER_GROUP_ID=?";
		ResultSetHandler<List<Permission>> h = new BeanListHandler<>(Permission.class, new PermissionMapper());
		return dbService.query(sql, h, userGroupId);
	}
	
}
