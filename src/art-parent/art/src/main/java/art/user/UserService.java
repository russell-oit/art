/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.user;

import art.dbutils.DbService;
import art.enums.AccessLevel;
import art.dbutils.DatabaseUtils;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
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
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting users
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
	private final ReportGroupService reportGroupService;

	@Autowired
	public UserService(DbService dbService, UserGroupService userGroupService,
			ReportGroupService reportGroupService) {

		this.dbService = dbService;
		this.userGroupService = userGroupService;
		this.reportGroupService = reportGroupService;
	}

	public UserService() {
		dbService = new DbService();
		userGroupService = new UserGroupService();
		reportGroupService = new ReportGroupService();
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
			user.setPasswordAlgorithm(rs.getString("PASSWORD_ALGORITHM"));
			user.setStartReport(rs.getString("START_QUERY"));
			user.setUserId(rs.getInt("USER_ID"));
			user.setCanChangePassword(rs.getBoolean("CAN_CHANGE_PASSWORD"));
			user.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			user.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			user.setCreatedBy(rs.getString("CREATED_BY"));
			user.setUpdatedBy(rs.getString("UPDATED_BY"));

			ReportGroup defaultReportGroup = reportGroupService.getReportGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
			user.setDefaultReportGroup(defaultReportGroup);

			return type.cast(user);
		}
	}

	/**
	 * Returns all users
	 *
	 * @return all users
	 * @throws SQLException
	 */
	@Cacheable("users")
	public List<User> getAllUsers() throws SQLException {
		logger.debug("Entering getAllUsers");

		ResultSetHandler<List<User>> h = new BeanListHandler<>(User.class, new UserMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns admin users (junior admin and above)
	 *
	 * @return admin users
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
	 * Returns a user
	 *
	 * @param id the user id
	 * @return user if found, null otherwise
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
	 * Returns a user
	 *
	 * @param username the username
	 * @return user if found, null otherwise
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
	 * Populates a user's user groups and sets properties whose values may come
	 * from user groups
	 *
	 * @param user the user
	 */
	private void populateUserGroups(User user) throws SQLException {
		if (user == null) {
			return;
		}

		ReportGroup effectiveDefaultReportGroup = user.getDefaultReportGroup();
		String effectiveStartReport = user.getStartReport();

		List<UserGroup> groups = userGroupService.getUserGroupsForUser(user.getUserId());

		for (UserGroup group : groups) {
			if (effectiveDefaultReportGroup == null) {
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
	 * Deletes a user and all related records
	 *
	 * @param id the user id
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
	 * Deletes multiple users
	 *
	 * @param ids the ids for users to delete
	 * @return ActionResult. if not successful, data contains details of users
	 * who weren't deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public ActionResult deleteUsers(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteUsers: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteUser(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedJobs = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedJobs, ", ");
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
	 * Updates a user's password
	 *
	 * @param userId the user id
	 * @param newPassword the new password hash
	 * @param passwordAlgorithm the password algorithm
	 * @param actionUser the user who is performing the action
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
	 * Adds a new user
	 *
	 * @param user the user to add
	 * @param actionUser the user who is performing the action
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

		saveUser(user, newId, actionUser);

		return newId;
	}

	/**
	 * Updates a user record
	 *
	 * @param user the updated user record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public void updateUser(User user, User actionUser) throws SQLException {
		logger.debug("Entering updateUser: user={}, actionUser={}", user, actionUser);

		Integer newRecordId = null;
		saveUser(user, newRecordId, actionUser);
	}

	/**
	 * Updates multiple users
	 *
	 * @param multipleUserEdit the multiple user edit details
	 * @param actionUser the user who is performing the action
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
			valuesList.add(BooleanUtils.toInteger(multipleUserEdit.isActive()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(Arrays.asList(ids));

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
		if (!multipleUserEdit.isCanChangePasswordUnchanged()) {
			sql = "UPDATE ART_USERS SET CAN_CHANGE_PASSWORD=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE USER_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(BooleanUtils.toInteger(multipleUserEdit.isCanChangePassword()));
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(Arrays.asList(ids));

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
		if (!multipleUserEdit.isAccessLevelUnchanged()) {
			sql = "UPDATE ART_USERS SET ACCESS_LEVEL=?, UPDATED_BY=?, UPDATE_DATE=?"
					+ " WHERE USER_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";

			List<Object> valuesList = new ArrayList<>();
			valuesList.add(multipleUserEdit.getAccessLevel().getValue());
			valuesList.add(actionUser.getUsername());
			valuesList.add(DatabaseUtils.getCurrentTimeAsSqlTimestamp());
			valuesList.addAll(Arrays.asList(ids));

			Object[] valuesArray = valuesList.toArray(new Object[valuesList.size()]);

			dbService.update(sql, valuesArray);
		}
	}

	/**
	 * Saves a user
	 *
	 * @param user the user to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the action
	 * @throws SQLException
	 */
	private void saveUser(User user, Integer newRecordId, User actionUser) throws SQLException {
		logger.debug("Entering saveUser: user={}, newRecordId={}, actionUser={}",
				user, newRecordId, actionUser);

		//set values for possibly null property objects
		int accessLevel;
		if (user.getAccessLevel() == null) {
			logger.warn("Access level not defined. Defaulting to 0");
			accessLevel = 0;
		} else {
			accessLevel = user.getAccessLevel().getValue();
		}

		int defaultReportGroupId;
		if (user.getDefaultReportGroup() == null) {
			defaultReportGroupId = 0;
		} else {
			defaultReportGroupId = user.getDefaultReportGroup().getReportGroupId();
		}

		int affectedRows;

		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_USERS"
					+ " (USER_ID, USERNAME, PASSWORD, PASSWORD_ALGORITHM,"
					+ " FULL_NAME, EMAIL, ACCESS_LEVEL, DEFAULT_QUERY_GROUP,"
					+ " START_QUERY, CAN_CHANGE_PASSWORD, ACTIVE, CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 13) + ")";

			Object[] values = {
				newRecordId,
				user.getUsername(),
				user.getPassword(),
				user.getPasswordAlgorithm(),
				user.getFullName(),
				user.getEmail(),
				accessLevel,
				defaultReportGroupId,
				user.getStartReport(),
				BooleanUtils.toInteger(user.isCanChangePassword()),
				BooleanUtils.toInteger(user.isActive()),
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
				defaultReportGroupId,
				user.getStartReport(),
				BooleanUtils.toInteger(user.isCanChangePassword()),
				BooleanUtils.toInteger(user.isActive()),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				user.getUserId()
			};

			affectedRows = dbService.update(sql, values);
		}

		if (newRecordId != null) {
			user.setUserId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, user={}",
					affectedRows, newRecord, user);
		}
	}

	/**
	 * Returns jobs that are owned by a given user
	 *
	 * @param userId the user id
	 * @return linked job names
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
