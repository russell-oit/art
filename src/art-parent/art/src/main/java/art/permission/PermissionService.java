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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving permissions
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

		ResultSetHandler<List<Permission>> h = new BeanListHandler<>(Permission.class, new PermissionMapper());
		return dbService.query(SQL_SELECT_ALL, h);
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

}
