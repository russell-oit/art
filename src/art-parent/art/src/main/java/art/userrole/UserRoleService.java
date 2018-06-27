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
 * Provides methods for retrieving, updating and deleting user-role records
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

	/**
	 * Recreates user-role records for a given user
	 *
	 * @param user the user
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "roles"}, allEntries = true)
	public void recreateUserRoles(User user) throws SQLException {
		logger.debug("Entering recreateUserRoles: user={}", user);

		int userId = user.getUserId();

		deleteAllRolesForUser(userId);
		addUserRoles(userId, user.getRoles());
	}

	/**
	 * Delete all user-role records for the given user
	 *
	 * @param userId the user id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "roles"}, allEntries = true)
	public void deleteAllRolesForUser(int userId) throws SQLException {
		logger.debug("Entering deleteAllRolesForUser: userId={}", userId);

		String sql = "DELETE FROM ART_USER_ROLE_MAP WHERE USER_ID=?";
		dbService.update(sql, userId);
	}

	/**
	 * Adds user-role records for the given user
	 *
	 * @param userId the user id
	 * @param roles the roles
	 * @throws SQLException
	 */
	@CacheEvict(value = {"users", "roles"}, allEntries = true)
	public void addUserRoles(int userId, List<Role> roles) throws SQLException {
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
		updateUserRoles(action, users, roleIds.toArray(new Integer[0]));
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
					affectedRows = dbService.update(sqlTest, userId, userId, roleId);
					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRecord = false;
					}
				}
				if (updateRecord) {
					dbService.update(sql, userId, roleId);
				}
			}
		}
	}

}
