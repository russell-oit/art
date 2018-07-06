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
package art.rolepermission;

import art.dbutils.DbService;
import art.permission.Permission;
import art.role.Role;
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
 * Provides methods for retrieving, updating and deleting role-permission
 * records
 *
 * @author Timothy Anyona
 */
@Service
public class RolePermissionService {

	private static final Logger logger = LoggerFactory.getLogger(RolePermissionService.class);

	private final DbService dbService;

	@Autowired
	public RolePermissionService(DbService dbService) {
		this.dbService = dbService;
	}

	public RolePermissionService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL
			= "SELECT AR.ROLE_ID, AR.NAME AS ROLE_NAME, AP.PERMISSION_ID, AP.NAME AS PERMISSION_NAME"
			+ " FROM ART_ROLE_PERMISSION_MAP ARPM"
			+ " INNER JOIN ART_ROLES AR ON"
			+ " ARPM.ROLE_ID=AR.ROLE_ID"
			+ " INNER JOIN ART_PERMISSIONS AP ON"
			+ " ARPM.PERMISSION_ID=AP.PERMISSION_ID";

	/**
	 * Maps a resultset to an object
	 */
	private class RolePermissionMapper extends BasicRowProcessor {

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
			RolePermission rolePermission = new RolePermission();

			Role role = new Role();
			role.setRoleId(rs.getInt("ROLE_ID"));
			role.setName(rs.getString("ROLE_NAME"));

			rolePermission.setRole(role);

			Permission permission = new Permission();
			permission.setPermissionId(rs.getInt("PERMISSION_ID"));
			permission.setName(rs.getString("PERMISSION_NAME"));

			rolePermission.setPermission(permission);

			return type.cast(rolePermission);
		}
	}

	/**
	 * Returns all role permissions
	 *
	 * @return all role permissions
	 * @throws SQLException
	 */
	public List<RolePermission> getAllRolePermissions() throws SQLException {
		logger.debug("Entering getAllRolePermissions");

		ResultSetHandler<List<RolePermission>> h = new BeanListHandler<>(RolePermission.class, new RolePermissionMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns the role permissions for a given role
	 *
	 * @param roleId the id of the role
	 * @return role permissions for a given role
	 * @throws SQLException
	 */
	public List<RolePermission> getRolePermissionsForRole(int roleId) throws SQLException {
		logger.debug("Entering getRolePermissionsForRole: roleId={}", roleId);

		String sql = SQL_SELECT_ALL + " WHERE AR.ROLE_ID=?";
		ResultSetHandler<List<RolePermission>> h = new BeanListHandler<>(RolePermission.class, new RolePermissionMapper());
		return dbService.query(sql, h, roleId);
	}

	/**
	 * Returns the role permissions for a given permission
	 *
	 * @param permissionId the id of the permission
	 * @return role permissions for a given permission
	 * @throws SQLException
	 */
	public List<RolePermission> getRolePermissionsForPermission(int permissionId) throws SQLException {
		logger.debug("Entering getRolePermissionsForPermission: permissionId={}", permissionId);

		String sql = SQL_SELECT_ALL + " WHERE AP.PERMISSION_ID=?";
		ResultSetHandler<List<RolePermission>> h = new BeanListHandler<>(RolePermission.class, new RolePermissionMapper());
		return dbService.query(sql, h, permissionId);
	}

	/**
	 * Deletes a role permission
	 *
	 * @param roleId the role id
	 * @param permissionId the permission id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"permissions", "roles", "users", "userGroups"}, allEntries = true)
	public void deleteRolePermission(int roleId, int permissionId) throws SQLException {
		logger.debug("Entering deleteRolePermission: roleId={}, permissionId={}", roleId, permissionId);

		String sql;

		sql = "DELETE FROM ART_ROLE_PERMISSION_MAP WHERE ROLE_ID=? AND PERMISSION_ID=?";
		dbService.update(sql, roleId, permissionId);
	}

	/**
	 * Recreates role-permission records for a given role
	 *
	 * @param role the role
	 * @throws SQLException
	 */
	@CacheEvict(value = {"roles", "permissions"}, allEntries = true)
	public void recreateRolePermissions(Role role) throws SQLException {
		logger.debug("Entering recreateRolePermissions: role={}", role);

		int roleId = role.getRoleId();

		deleteAllPermissionsForRole(roleId);
		addRolePermissions(roleId, role.getPermissions());
	}

	/**
	 * Delete all role-permission records for the given role
	 *
	 * @param roleId the role id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"roles", "permissions", "users", "userGroups"}, allEntries = true)
	public void deleteAllPermissionsForRole(int roleId) throws SQLException {
		logger.debug("Entering deleteAllPermissionsForRole: roleId={}", roleId);

		String sql = "DELETE FROM ART_ROLE_PERMISSION_MAP WHERE ROLE_ID=?";
		dbService.update(sql, roleId);
	}

	/**
	 * Adds role-permission records for the given role
	 *
	 * @param roleId the role id
	 * @param permissions the permissions
	 * @throws SQLException
	 */
	@CacheEvict(value = {"roles", "permissions", "users", "userGroups"}, allEntries = true)
	public void addRolePermissions(int roleId, List<Permission> permissions) throws SQLException {
		logger.debug("Entering addRolePermissions: roleId={}", roleId);

		if (CollectionUtils.isEmpty(permissions)) {
			return;
		}

		List<Integer> permissionIds = new ArrayList<>();
		for (Permission permission : permissions) {
			permissionIds.add(permission.getPermissionId());
		}

		Integer[] roles = {roleId};
		String action = "add";
		updateRolePermissions(action, roles, permissionIds.toArray(new Integer[0]));
	}

	/**
	 * Adds or removes role-permission records
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param roles role ids
	 * @param permissions permission ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"roles", "permissions", "users", "userGroups"}, allEntries = true)
	public void updateRolePermissions(String action, Integer[] roles, Integer[] permissions) throws SQLException {
		logger.debug("Entering updateRolePermissions: action='{}'", action);

		logger.debug("(roles == null) = {}", roles == null);
		logger.debug("(permissions == null) = {}", permissions == null);
		if (roles == null || permissions == null) {
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
			sql = "INSERT INTO ART_ROLE_PERMISSION_MAP (ROLE_ID, PERMISSION_ID) VALUES (?, ?)";
		} else {
			sql = "DELETE FROM ART_ROLE_PERMISSION_MAP WHERE ROLE_ID=? AND PERMISSION_ID=?";
		}

		String sqlTest = "UPDATE ART_ROLE_PERMISSION_MAP SET ROLE_ID=? WHERE ROLE_ID=? AND PERMISSION_ID=?";
		int affectedRows;
		boolean updateRecord;

		for (Integer roleId : roles) {
			for (Integer permissionId : permissions) {
				updateRecord = true;
				if (add) {
					//test if record exists. to avoid integrity constraint error
					affectedRows = dbService.update(sqlTest, roleId, roleId, permissionId);
					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRecord = false;
					}
				}
				if (updateRecord) {
					dbService.update(sql, roleId, permissionId);
				}
			}
		}
	}

}
