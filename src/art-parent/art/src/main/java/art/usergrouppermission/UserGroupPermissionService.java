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
package art.usergrouppermission;

import art.dbutils.DbService;
import art.permission.Permission;
import art.usergroup.UserGroup;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving and deleting user group permissions
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupPermissionService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupPermissionService.class);

	private final DbService dbService;

	@Autowired
	public UserGroupPermissionService(DbService dbService) {
		this.dbService = dbService;
	}

	public UserGroupPermissionService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL
			= "SELECT AUG.USER_GROUP_ID, AUG.NAME AS USER_GROUP_NAME, AP.PERMISSION_ID, AP.NAME AS PERMISSION_NAME"
			+ " FROM ART_USER_GROUP_PERM_MAP AUGPM"
			+ " INNER JOIN ART_USER_GROUPS AUG ON"
			+ " AUGPM.USER_GROUP_ID=AUG.USER_GROUP_ID"
			+ " INNER JOIN ART_PERMISSIONS AP ON"
			+ " AUGPM.PERMISSION_ID=AP.PERMISSION_ID";

	/**
	 * Maps a resultset to an object
	 */
	private class UserGroupPermissionMapper extends BasicRowProcessor {

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
			UserGroupPermission userGroupPermission = new UserGroupPermission();

			UserGroup userGroup = new UserGroup();
			userGroup.setUserGroupId(rs.getInt("USER_GROUP_ID"));
			userGroup.setName(rs.getString("USER_GROUP_NAME"));

			userGroupPermission.setUserGroup(userGroup);

			Permission permission = new Permission();
			permission.setPermissionId(rs.getInt("PERMISSION_ID"));
			permission.setName(rs.getString("PERMISSION_NAME"));

			userGroupPermission.setPermission(permission);

			return type.cast(userGroupPermission);
		}
	}

	/**
	 * Returns all user group permissions
	 *
	 * @return all user group permissions
	 * @throws SQLException
	 */
	public List<UserGroupPermission> getAllUserGroupPermissions() throws SQLException {
		logger.debug("Entering getAllUserGroupPermissions");

		ResultSetHandler<List<UserGroupPermission>> h = new BeanListHandler<>(UserGroupPermission.class, new UserGroupPermissionMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns the user group permissions for a given user group
	 *
	 * @param userGroupId the id of the user group
	 * @return user group permissions for a given user group
	 * @throws SQLException
	 */
	public List<UserGroupPermission> getUserGroupPermissionsForUserGroup(int userGroupId) throws SQLException {
		logger.debug("Entering getUserGroupPermissionsForUserGroup: userGroupId={}", userGroupId);

		String sql = SQL_SELECT_ALL + " WHERE AUG.USER_GROUP_ID=?";
		ResultSetHandler<List<UserGroupPermission>> h = new BeanListHandler<>(UserGroupPermission.class, new UserGroupPermissionMapper());
		return dbService.query(sql, h, userGroupId);
	}

	/**
	 * Deletes a user group permission
	 *
	 * @param userGroupId the user group id
	 * @param permissionId the permission id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"permissions", "userGroups", "users"}, allEntries = true)
	public void deleteUserGroupPermission(int userGroupId, int permissionId) throws SQLException {
		logger.debug("Entering deleteUserGroupPermission: userGroupId={}, permissionId={}",
				userGroupId, permissionId);

		String sql;

		sql = "DELETE FROM ART_USER_GROUP_PERM_MAP WHERE USER_GROUP_ID=? AND PERMISSION_ID=?";
		dbService.update(sql, userGroupId, permissionId);
	}

}
