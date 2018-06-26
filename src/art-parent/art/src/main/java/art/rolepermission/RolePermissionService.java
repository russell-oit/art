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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
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
	@CacheEvict(value = {"roles", "permissions"}, allEntries = true)
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
	@CacheEvict(value = {"roles", "permissions"}, allEntries = true)
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
	@CacheEvict(value = {"roles", "permissions"}, allEntries = true)
	public void updateRolePermissions(String action, Integer[] roles, Integer[] permissions) throws SQLException {
		logger.debug("Entering updateRolePermissions: action='{}'", action);

		logger.debug("(roles == null) = {}", roles == null);
		logger.debug("(permissions == null) = {}", permissions == null);
		if (roles == null || permissions == null) {
			logger.warn("Update not performed. roles or permissions is null.");
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
