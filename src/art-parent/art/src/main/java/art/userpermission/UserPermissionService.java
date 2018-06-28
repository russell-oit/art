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
package art.userpermission;

import art.dbutils.DbService;
import art.permission.Permission;
import art.user.User;
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
 * Provides methods for retrieving and deleting user permissions
 *
 * @author Timothy Anyona
 */
@Service
public class UserPermissionService {

	private static final Logger logger = LoggerFactory.getLogger(UserPermissionService.class);

	private final DbService dbService;

	@Autowired
	public UserPermissionService(DbService dbService) {
		this.dbService = dbService;
	}

	public UserPermissionService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL
			= "SELECT AU.USER_ID, AU.USERNAME, AP.PERMISSION_ID, AP.NAME AS PERMISSION_NAME"
			+ " FROM ART_USER_PERMISSION_MAP AUPM"
			+ " INNER JOIN ART_USERS AU ON"
			+ " AUPM.USER_ID=AU.USER_ID"
			+ " INNER JOIN ART_PERMISSIONS AP ON"
			+ " AUPM.PERMISSION_ID=AP.PERMISSION_ID";

	/**
	 * Maps a resultset to an object
	 */
	private class UserPermissionMapper extends BasicRowProcessor {

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
			UserPermission userPermission = new UserPermission();

			User user = new User();
			user.setUserId(rs.getInt("USER_ID"));
			user.setUsername(rs.getString("USERNAME"));

			userPermission.setUser(user);

			Permission permission = new Permission();
			permission.setPermissionId(rs.getInt("PERMISSION_ID"));
			permission.setName(rs.getString("PERMISSION_NAME"));

			userPermission.setPermission(permission);

			return type.cast(userPermission);
		}
	}

	/**
	 * Returns all user permissions
	 *
	 * @return all user permissions
	 * @throws SQLException
	 */
	public List<UserPermission> getAllUserPermissions() throws SQLException {
		logger.debug("Entering getAllUserPermissions");

		ResultSetHandler<List<UserPermission>> h = new BeanListHandler<>(UserPermission.class, new UserPermissionMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns the user permissions for a given user
	 *
	 * @param userId the id of the user
	 * @return user permissions for a given user
	 * @throws SQLException
	 */
	public List<UserPermission> getUserPermissionsForUser(int userId) throws SQLException {
		logger.debug("Entering getUserPermissionsForUser: userId={}", userId);

		String sql = SQL_SELECT_ALL + " WHERE AU.USER_ID=?";
		ResultSetHandler<List<UserPermission>> h = new BeanListHandler<>(UserPermission.class, new UserPermissionMapper());
		return dbService.query(sql, h, userId);
	}

	/**
	 * Deletes a user permission
	 *
	 * @param userId the user id
	 * @param permissionId the permission id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"permissions", "users"}, allEntries = true)
	public void deleteUserPermission(int userId, int permissionId) throws SQLException {
		logger.debug("Entering deleteUserPermission: userId={}, permissionId={}", userId, permissionId);

		String sql;

		sql = "DELETE FROM ART_USER_PERMISSION_MAP WHERE USER_ID=? AND PERMISSION_ID=?";
		dbService.update(sql, userId, permissionId);
	}

}
