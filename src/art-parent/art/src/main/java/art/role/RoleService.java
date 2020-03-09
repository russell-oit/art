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
package art.role;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.general.ActionResult;
import art.permission.Permission;
import art.permission.PermissionService;
import art.rolepermission.RolePermissionService;
import art.user.User;
import art.utils.ArtUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting roles
 *
 * @author Timothy Anyona
 */
@Service
public class RoleService {

	private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

	private final DbService dbService;
	private final PermissionService permissionService;
	private final RolePermissionService rolePermissionService;

	@Autowired
	public RoleService(DbService dbService, PermissionService permissionService,
			RolePermissionService rolePermissionService) {
		this.dbService = dbService;
		this.permissionService = permissionService;
		this.rolePermissionService = rolePermissionService;
	}

	public RoleService() {
		dbService = new DbService();
		permissionService = new PermissionService();
		rolePermissionService = new RolePermissionService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_ROLES AR";

	/**
	 * Maps a resultset to an object
	 */
	private class RoleMapper extends BasicRowProcessor {

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
			Role role = new Role();

			role.setRoleId(rs.getInt("ROLE_ID"));
			role.setName(rs.getString("NAME"));
			role.setDescription(rs.getString("DESCRIPTION"));
			role.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			role.setCreatedBy(rs.getString("CREATED_BY"));
			role.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			role.setUpdatedBy(rs.getString("UPDATED_BY"));

			List<Permission> permissions = permissionService.getRolePermissions(role.getRoleId());
			role.setPermissions(permissions);

			return type.cast(role);
		}
	}

	/**
	 * Returns all roles
	 *
	 * @return all roles
	 * @throws SQLException
	 */
	@Cacheable("roles")
	public List<Role> getAllRoles() throws SQLException {
		logger.debug("Entering getAllRoles");

		ResultSetHandler<List<Role>> h = new BeanListHandler<>(Role.class, new RoleMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns roles with given ids
	 *
	 * @param ids comma separated string of the role ids to retrieve
	 * @return roles with given ids
	 * @throws SQLException
	 */
	public List<Role> getRoles(String ids) throws SQLException {
		logger.debug("Entering getRoles: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		String sql = SQL_SELECT_ALL
				+ " WHERE ROLE_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<Role>> h = new BeanListHandler<>(Role.class, new RoleMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns a role with the given id
	 *
	 * @param id the role id
	 * @return role if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("roles")
	public Role getRole(int id) throws SQLException {
		logger.debug("Entering getRole: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE ROLE_ID=?";
		ResultSetHandler<Role> h = new BeanHandler<>(Role.class, new RoleMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns a role with the given name
	 *
	 * @param name the role name
	 * @return role if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("roles")
	public Role getRole(String name) throws SQLException {
		logger.debug("Entering getRole: name='{}'", name);

		String sql = SQL_SELECT_ALL + " WHERE NAME=?";
		ResultSetHandler<Role> h = new BeanHandler<>(Role.class, new RoleMapper());
		return dbService.query(sql, h, name);
	}

	/**
	 * Deletes a role
	 *
	 * @param id the role id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * users and user groups which prevented the role from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"roles", "users", "userGroups"}, allEntries = true)
	public ActionResult deleteRole(int id) throws SQLException {
		logger.debug("Entering deleteRole: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedRecords = getLinkedRecords(id);
		if (!linkedRecords.isEmpty()) {
			result.setData(linkedRecords);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_USER_ROLE_MAP WHERE ROLE_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_ROLE_MAP WHERE ROLE_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_ROLE_PERMISSION_MAP WHERE ROLE_ID=?";
		dbService.update(sql, id);

		//finally delete role
		sql = "DELETE FROM ART_ROLES WHERE ROLE_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);

		return result;
	}

	/**
	 * Deletes multiple roles
	 *
	 * @param ids the ids of the roles to delete
	 * @return ActionResult. if not successful, data contains details of roles
	 * which weren't deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = {"roles", "users", "userGroups"}, allEntries = true)
	public ActionResult deleteRoles(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteRoles: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteRole(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedRecords = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedRecords, ", ");
				nonDeletedRecords.add(value);
			}
		}

		if (nonDeletedRecords.isEmpty()) {
			result.setSuccess(true);
		} else {
			result.setData(nonDeletedRecords);
		}

		return result;
	}

	/**
	 * Adds a new role
	 *
	 * @param role the role to add
	 * @param actionUser the user who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "roles", allEntries = true)
	public synchronized int addRole(Role role, User actionUser) throws SQLException {
		logger.debug("Entering addRole: role={}, actionUser={}", role, actionUser);

		//generate new id
		String sql = "SELECT MAX(ROLE_ID) FROM ART_ROLES";
		int newId = dbService.getNewRecordId(sql);

		saveRole(role, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing role
	 *
	 * @param role the updated role
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = {"roles", "users", "userGroups"}, allEntries = true)
	public void updateRole(Role role, User actionUser) throws SQLException {
		logger.debug("Entering updateRole: role={}, actionUser={}", role, actionUser);

		Integer newRecordId = null;
		saveRole(role, newRecordId, actionUser);
	}

	/**
	 * Imports role records
	 *
	 * @param roles the list of roles to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param overwrite whether to overwrite existing records
	 * @throws SQLException
	 */
	@CacheEvict(value = "roles", allEntries = true)
	public void importRoles(List<Role> roles, User actionUser,
			Connection conn, boolean overwrite) throws SQLException {

		logger.debug("Entering importRoles: actionUser={}, overwrite={}",
				actionUser, overwrite);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(ROLE_ID) FROM ART_ROLES";
			int id = dbService.getMaxRecordId(conn, sql);

			List<Role> currentRoles = new ArrayList<>();
			if (overwrite) {
				currentRoles = getAllRoles();
			}

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			for (Role role : roles) {
				String roleName = role.getName();
				boolean update = false;
				if (overwrite) {
					Role existingRole = currentRoles.stream()
							.filter(d -> StringUtils.equals(roleName, d.getName()))
							.findFirst()
							.orElse(null);
					if (existingRole != null) {
						update = true;
						role.setRoleId(existingRole.getRoleId());
					}
				}

				Integer newRecordId;
				if (update) {
					newRecordId = null;
				} else {
					id++;
					newRecordId = id;
				}
				saveRole(role, newRecordId, actionUser, conn);
				rolePermissionService.recreateRolePermissions(role);
			}
			conn.commit();
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	/**
	 * Saves a role
	 *
	 * @param role the role to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveRole(Role role, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveRole(role, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a role
	 *
	 * @param role the role to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	@CacheEvict(value = "roles", allEntries = true)
	public void saveRole(Role role, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering saveRole: role={}, newRecordId={},actionUser={}",
				role, newRecordId, actionUser);

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_ROLES"
					+ " (ROLE_ID, NAME, DESCRIPTION,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 5) + ")";

			Object[] values = {
				newRecordId,
				role.getName(),
				role.getDescription(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_ROLES SET NAME=?, DESCRIPTION=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE ROLE_ID=?";

			Object[] values = {
				role.getName(),
				role.getDescription(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				role.getRoleId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		if (newRecordId != null) {
			role.setRoleId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, role={}",
					affectedRows, newRecord, role);
		}
	}

	/**
	 * Returns the roles attached to a given user
	 *
	 * @param userId the user id
	 * @return the user's roles
	 * @throws SQLException
	 */
	@Cacheable("roles")
	public List<Role> getRolesForUser(int userId) throws SQLException {
		logger.debug("Entering getRolesForUser: userId={}", userId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_USER_ROLE_MAP AURM"
				+ " ON AR.ROLE_ID=AURM.ROLE_ID"
				+ " WHERE AURM.USER_ID=?";
		ResultSetHandler<List<Role>> h = new BeanListHandler<>(Role.class, new RoleMapper());
		return dbService.query(sql, h, userId);
	}

	/**
	 * Returns the roles attached to a given user group
	 *
	 * @param userGroupId the user group id
	 * @return the user group's roles
	 * @throws SQLException
	 */
	@Cacheable("roles")
	public List<Role> getRolesForUserGroup(int userGroupId) throws SQLException {
		logger.debug("Entering getRolesForUserGroup: userGroupId={}", userGroupId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_USER_GROUP_ROLE_MAP AUGRM"
				+ " ON AR.ROLE_ID=AUGRM.ROLE_ID"
				+ " WHERE AUGRM.USER_GROUP_ID=?";
		ResultSetHandler<List<Role>> h = new BeanListHandler<>(Role.class, new RoleMapper());
		return dbService.query(sql, h, userGroupId);
	}

	/**
	 * Returns details of users and user groups that have a given role
	 *
	 * @param roleId the role id
	 * @return linked user and user group details
	 * @throws SQLException
	 */
	public List<String> getLinkedRecords(int roleId) throws SQLException {
		logger.debug("Entering getLinkedRecords: roleId={}", roleId);

		//union removes duplicate records, union all does not
		//use union all in case a user and a user group have the same name?
		String sql = "SELECT AU.USERNAME AS RECORD_NAME"
				+ " FROM ART_USERS AU"
				+ " INNER JOIN ART_USER_ROLE_MAP AURM"
				+ " ON AU.USER_ID=AURM.USER_ID"
				+ " WHERE AURM.ROLE_ID=?"
				+ " UNION ALL"
				+ " SELECT AUG.NAME AS RECORD_NAME"
				+ " FROM ART_USER_GROUPS AUG"
				+ " INNER JOIN ART_USER_GROUP_ROLE_MAP AUGRM"
				+ " ON AUG.USER_GROUP_ID=AUGRM.USER_GROUP_ID"
				+ " WHERE AUGRM.ROLE_ID=?";

		ResultSetHandler<List<Map<String, Object>>> h = new MapListHandler();
		List<Map<String, Object>> recordDetails = dbService.query(sql, h, roleId, roleId);

		List<String> records = new ArrayList<>();
		for (Map<String, Object> recordDetail : recordDetails) {
			String recordName = (String) recordDetail.get("RECORD_NAME");
			records.add(recordName);
		}

		return records;
	}

}
