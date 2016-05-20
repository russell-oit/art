/*
 * Copyright (C) 2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.user;

import art.dbutils.DbService;
import art.enums.AccessLevel;
import art.dbutils.DatabaseUtils;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupService;
import art.utils.ActionResult;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to users
 *
 * @author Timothy Anyona
 */
@Service
public class UserService {

	//for caching info, see
	//http://wangxiangblog.blogspot.com/2013/02/spring-cache.html
	//http://viralpatel.net/blogs/cache-support-spring-3-1-m1/
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	private final DbService dbService;
	private final UserGroupService userGroupService;

	@Autowired
	public UserService(DbService dbService, UserGroupService userGroupService) {
		this.dbService = dbService;
		this.userGroupService = userGroupService;
	}

	public UserService() {
		dbService = new DbService();
		userGroupService = new UserGroupService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_USERS";

	/**
	 * Maps a resultset to an object
	 */
	private class UserMapper extends BasicRowProcessor {

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
			User user = new User();

			user.setUsername(rs.getString("USERNAME"));
			user.setEmail(rs.getString("EMAIL"));
			user.setAccessLevel(AccessLevel.toEnum(rs.getInt("ACCESS_LEVEL")));
			user.setFullName(rs.getString("FULL_NAME"));
			user.setActive(rs.getBoolean("ACTIVE"));
			user.setPassword(rs.getString("PASSWORD"));
			user.setDefaultReportGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
			user.setPasswordAlgorithm(rs.getString("PASSWORD_ALGORITHM"));
			user.setStartReport(rs.getString("START_QUERY"));
			user.setUserId(rs.getInt("USER_ID"));
			user.setCanChangePassword(rs.getBoolean("CAN_CHANGE_PASSWORD"));
			user.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			user.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			user.setCreatedBy(rs.getString("CREATED_BY"));
			user.setUpdatedBy(rs.getString("UPDATED_BY"));

			return type.cast(user);
		}
	}

	/**
	 * Get all users
	 *
	 * @return list of all users, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("users")
	public List<User> getAllUsers() throws SQLException {
		logger.debug("Entering getAllUsers");

		ResultSetHandler<List<User>> h = new BeanListHandler<>(User.class, new UserMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Get admin users (junior admin and above)
	 *
	 * @return list of admin users, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("users")
	public List<User> getAdminUsers() throws SQLException {
		logger.debug("Entering getAdminUsers");

		String sql = SQL_SELECT_ALL + " WHERE ACCESS_LEVEL>=" + AccessLevel.JuniorAdmin.getValue();
		ResultSetHandler<List<User>> h = new BeanListHandler<>(User.class, new UserMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Get a user
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("users")
	public User getUser(int id) throws SQLException {
		logger.debug("Entering getUser: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE USER_ID = ? ";
		ResultSetHandler<User> h = new BeanHandler<>(User.class, new UserMapper());
		User user = dbService.query(sql, h, id);
		populateUserGroups(user);
		return user;
	}

	/**
	 * Get a user
	 *
	 * @param username
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("users")
	public User getUser(String username) throws SQLException {
		logger.debug("Entering getUser: username='{}'", username);

		String sql = SQL_SELECT_ALL + " WHERE USERNAME = ? ";
		ResultSetHandler<User> h = new BeanHandler<>(User.class, new UserMapper());
		User user = dbService.query(sql, h, username);
		populateUserGroups(user);
		return user;
	}

	/**
	 * Populate a user's user groups and set properties whose values may come
	 * from user groups
	 *
	 * @param user
	 */
	private void populateUserGroups(User user) throws SQLException {
		if (user == null) {
			return;
		}

		int effectiveDefaultReportGroup = user.getDefaultReportGroup();
		String effectiveStartReport = user.getStartReport();

		List<UserGroup> groups = userGroupService.getUserGroupsForUser(user.getUserId());

		for (UserGroup group : groups) {
			if (effectiveDefaultReportGroup <= 0) {
				effectiveDefaultReportGroup = group.getDefaultReportGroup();
			}
			if (StringUtils.isBlank(effectiveStartReport)) {
				effectiveStartReport = group.getStartReport();
			}
		}

		user.setEffectiveDefaultReportGroup(effectiveDefaultReportGroup);
		user.setEffectiveStartReport(effectiveStartReport);

		user.setUserGroups(groups);
	}

	/**
	 * Delete a user and all related records
	 *
	 * @param id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * jobs which prevented the user from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public ActionResult deleteUser(int id) throws SQLException {
		logger.debug("Entering deleteUser: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedJobs = getLinkedJobs(id);
		if (!linkedJobs.isEmpty()) {
			result.setData(linkedJobs);
			return result;
		}

		String sql;

		//delete foreign key records
		sql = "DELETE FROM ART_ADMIN_PRIVILEGES WHERE USER_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_QUERIES WHERE USER_ID=?";
		dbService.update(sql, id);

		//delete user-report user relationships
		sql = "DELETE FROM ART_USER_QUERY_GROUPS WHERE USER_ID=?";
		dbService.update(sql, id);

		//delete user-rules relationships
		sql = "DELETE FROM ART_USER_RULES WHERE USER_ID=?";
		dbService.update(sql, id);

		//delete user-shared job relationships
		sql = "DELETE FROM ART_USER_JOBS WHERE USER_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_JOB_ARCHIVES WHERE USER_ID=?";
		dbService.update(sql, id);

		//lastly, delete user
		sql = "DELETE FROM ART_USERS WHERE USER_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);
		return result;
	}

	/**
	 * Delete a user and all related records
	 *
	 * @param ids
	 * @return ActionResult. if not successful, data contains a list of linked
	 * jobs which prevented the user from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public ActionResult deleteUsers(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteUsers: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<Integer> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteUser(id);
			if (!deleteResult.isSuccess()) {
				nonDeletedRecords.add(id);
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
	 * Update a user's password
	 *
	 * @param userId
	 * @param newPassword password hash
	 * @param passwordAlgorithm
	 * @param actionUser
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public void updatePassword(int userId, String newPassword, String passwordAlgorithm,
			User actionUser) throws SQLException {

		logger.debug("Entering updatePassword: userId={}, passwordAlgorithm='{}', actionUser={}",
				userId, passwordAlgorithm, actionUser);

		String sql = "UPDATE ART_USERS SET PASSWORD=?, PASSWORD_ALGORITHM=?,"
				+ " UPDATE_DATE=?, UPDATED_BY=?"
				+ " WHERE USER_ID=?";

		Object[] values = {
			newPassword,
			passwordAlgorithm,
			DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
			actionUser.getUsername(),
			userId
		};

		dbService.update(sql, values);
	}

	/**
	 * Add a new user to the database
	 *
	 * @param user
	 * @param actionUser
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public synchronized int addUser(User user, User actionUser) throws SQLException {
		logger.debug("Entering addUser: user={}, actionUser={}", user, actionUser);

		//generate new id
		String sql = "SELECT MAX(USER_ID) FROM ART_USERS";
		ResultSetHandler<Integer> h = new ScalarHandler<>();
		Integer maxId = dbService.query(sql, h);
		logger.debug("maxId={}", maxId);

		int newId;
		if (maxId == null || maxId < 0) {
			//no records in the table, or only hardcoded records
			newId = 1;
		} else {
			newId = maxId + 1;
		}
		logger.debug("newId={}", newId);

		user.setUserId(newId);

		saveUser(user, true, actionUser);

		return newId;
	}

	/**
	 * Update an existing user record
	 *
	 * @param user
	 * @param actionUser
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public void updateUser(User user, User actionUser) throws SQLException {
		logger.debug("Entering updateUser: user={}, actionUser={}", user, actionUser);

		saveUser(user, false, actionUser);
	}

	/**
	 * Update an existing user record
	 *
	 * @param multipleUserEdit
	 * @param actionUser
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public void updateUsers(MultipleUserEdit multipleUserEdit, User actionUser) throws SQLException {
		logger.debug("Entering updateUsers: multipleUserEdit={}, actionUser={}", multipleUserEdit, actionUser);

		String sql;

		String[] ids = StringUtils.split(multipleUserEdit.getIds(), ",");
		if (!multipleUserEdit.isActiveUnchanged()) {
			sql = "UPDATE ART_USERS SET ACTIVE=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE USER_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(multipleUserEdit.isActive());
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(Arrays.asList(ids));

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

	/**
	 * Save a user
	 *
	 * @param user
	 * @param newRecord
	 * @param actionUser
	 * @throws SQLException
	 */
	private void saveUser(User user, boolean newRecord, User actionUser) throws SQLException {
		logger.debug("Entering saveUser: user={}, newRecord={}, actionUser={}",
				user, newRecord, actionUser);

		//set values for possibly null property objects
		int accessLevel;
		if (user.getAccessLevel() == null) {
			logger.warn("Access level not defined. Defaulting to 0");
			accessLevel = 0;
		} else {
			accessLevel = user.getAccessLevel().getValue();
		}

		int affectedRows;
		if (newRecord) {
			String sql = "INSERT INTO ART_USERS"
					+ " (USER_ID, USERNAME, PASSWORD, PASSWORD_ALGORITHM,"
					+ " FULL_NAME, EMAIL, ACCESS_LEVEL, DEFAULT_QUERY_GROUP,"
					+ " START_QUERY, CAN_CHANGE_PASSWORD, ACTIVE, CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 13) + ")";

			Object[] values = {
				user.getUserId(),
				user.getUsername(),
				user.getPassword(),
				user.getPasswordAlgorithm(),
				user.getFullName(),
				user.getEmail(),
				accessLevel,
				user.getDefaultReportGroup(),
				user.getStartReport(),
				user.isCanChangePassword(),
				user.isActive(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_USERS SET USERNAME=?, PASSWORD=?,"
					+ " PASSWORD_ALGORITHM=?, FULL_NAME=?, EMAIL=?,"
					+ " ACCESS_LEVEL=?, DEFAULT_QUERY_GROUP=?, START_QUERY=?,"
					+ " CAN_CHANGE_PASSWORD=?, ACTIVE=?, UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE USER_ID=?";

			Object[] values = {
				user.getUsername(),
				user.getPassword(),
				user.getPasswordAlgorithm(),
				user.getFullName(),
				user.getEmail(),
				accessLevel,
				user.getDefaultReportGroup(),
				user.getStartReport(),
				user.isCanChangePassword(),
				user.isActive(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				user.getUserId()
			};

			affectedRows = dbService.update(sql, values);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, user={}",
					affectedRows, newRecord, user);
		}
	}

	/**
	 * Get jobs that are owned by a given user
	 *
	 * @param userId
	 * @return list with linked job names, empty list otherwise
	 * @throws SQLException
	 */
	public List<String> getLinkedJobs(int userId) throws SQLException {
		logger.debug("Entering getLinkedJobs: userId={}", userId);

		String sql = "SELECT JOB_NAME"
				+ " FROM ART_JOBS"
				+ " WHERE USER_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>("JOB_NAME");
		return dbService.query(sql, h, userId);
	}

}
