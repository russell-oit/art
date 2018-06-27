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
 * Provides methods for retrieving, updating and deleting user group-role
 * records
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

	/**
	 * Recreates user group-role records for a given user group
	 *
	 * @param userGroup the user group
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "roles"}, allEntries = true)
	public void recreateUserGroupRoles(UserGroup userGroup) throws SQLException {
		logger.debug("Entering recreateUserGroupRoles: userGroup={}", userGroup);

		int userGroupId = userGroup.getUserGroupId();

		deleteAllRolesForUserGroup(userGroupId);
		addUserGroupRoles(userGroupId, userGroup.getRoles());
	}

	/**
	 * Delete all user group-role records for the given user group
	 *
	 * @param userGroupId the user group id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "roles"}, allEntries = true)
	public void deleteAllRolesForUserGroup(int userGroupId) throws SQLException {
		logger.debug("Entering deleteAllRolesForUserGroup: userGroupId={}", userGroupId);

		String sql = "DELETE FROM ART_USER_GROUP_ROLE_MAP WHERE USER_GROUP_ID=?";
		dbService.update(sql, userGroupId);
	}

	/**
	 * Adds user group-role records for the given user group
	 *
	 * @param userGroupId the user group id
	 * @param roles the roles
	 * @throws SQLException
	 */
	@CacheEvict(value = {"userGroups", "roles"}, allEntries = true)
	public void addUserGroupRoles(int userGroupId, List<Role> roles) throws SQLException {
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
	@CacheEvict(value = {"userGroups", "roles"}, allEntries = true)
	public void updateUserGroupRoles(String action, Integer[] userGroups, Integer[] roles) throws SQLException {
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
					affectedRows = dbService.update(sqlTest, userGroupId, userGroupId, roleId);
					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRecord = false;
					}
				}
				if (updateRecord) {
					dbService.update(sql, userGroupId, roleId);
				}
			}
		}
	}

}
