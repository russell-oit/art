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
package art.paramdefault;

import art.dbutils.DbService;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.user.User;
import art.user.UserService;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupService;
import art.utils.ArtUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting param defaults
 *
 * @author Timothy Anyona
 */
@Service
public class ParamDefaultService {

	private static final Logger logger = LoggerFactory.getLogger(ParamDefaultService.class);

	private final DbService dbService;
	private final UserService userService;
	private final ParameterService parameterService;
	private final UserGroupService userGroupService;

	@Autowired
	public ParamDefaultService(DbService dbService, UserService userService,
			ParameterService parameterService, UserGroupService userGroupService) {

		this.dbService = dbService;
		this.userService = userService;
		this.parameterService = parameterService;
		this.userGroupService = userGroupService;
	}

	public ParamDefaultService() {
		dbService = new DbService();
		userService = new UserService();
		parameterService = new ParameterService();
		userGroupService = new UserGroupService();
	}

	private final String SQL_SELECT_ALL_USER_PARAM_DEFAULTS = "SELECT * FROM ART_USER_PARAM_DEFAULTS AUPD";
	private final String SQL_SELECT_ALL_USER_GROUP_PARAM_DEFAULTS = "SELECT * FROM ART_USER_GROUP_PARAM_DEFAULTS AUGPD";

	/**
	 * Maps a resultset to an object
	 */
	private class UserParamDefaultMapper extends BasicRowProcessor {

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
			UserParamDefault paramDefault = new UserParamDefault();

			paramDefault.setParamDefaultKey(rs.getString("PARAM_DEFAULT_KEY"));
			paramDefault.setValue(rs.getString("PARAM_VALUE"));

			User user = userService.getUser(rs.getInt("USER_ID"));
			paramDefault.setUser(user);

			Parameter parameter = parameterService.getParameter(rs.getInt("PARAMETER_ID"));
			paramDefault.setParameter(parameter);

			return type.cast(paramDefault);
		}
	}

	/**
	 * Maps a resultset to an object
	 */
	private class UserGroupParamDefaultMapper extends BasicRowProcessor {

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
			UserGroupParamDefault paramDefault = new UserGroupParamDefault();

			paramDefault.setParamDefaultKey(rs.getString("PARAM_DEFAULT_KEY"));
			paramDefault.setValue(rs.getString("PARAM_VALUE"));

			UserGroup userGroup = userGroupService.getUserGroup(rs.getInt("USER_GROUP_ID"));
			paramDefault.setUserGroup(userGroup);

			Parameter parameter = parameterService.getParameter(rs.getInt("PARAMETER_ID"));
			paramDefault.setParameter(parameter);

			return type.cast(paramDefault);
		}
	}

	/**
	 * Returns all user parameter defaults
	 *
	 * @return all user parameter defaults
	 * @throws SQLException
	 */
	public List<UserParamDefault> getAllUserParamDefaults() throws SQLException {
		logger.debug("Entering getAllUserParamDefaults");

		ResultSetHandler<List<UserParamDefault>> h = new BeanListHandler<>(UserParamDefault.class, new UserParamDefaultMapper());
		return dbService.query(SQL_SELECT_ALL_USER_PARAM_DEFAULTS, h);
	}

	/**
	 * Returns user parameter default values for a given user and parameter
	 *
	 * @param userId the user id
	 * @param parameterId the parameter id
	 * @return user parameter default values for the user and parameter
	 * @throws SQLException
	 */
	public List<UserParamDefault> getUserParamDefaults(int userId, int parameterId)
			throws SQLException {

		logger.debug("Entering getUserParamDefaults: userId={},"
				+ " parameterId={}", userId, parameterId);

		String sql = SQL_SELECT_ALL_USER_PARAM_DEFAULTS
				+ " WHERE USER_ID=? AND PARAMETER_ID=?";

		ResultSetHandler<List<UserParamDefault>> h = new BeanListHandler<>(UserParamDefault.class, new UserParamDefaultMapper());
		return dbService.query(sql, h, userId, parameterId);
	}

	/**
	 * Returns the user parameter default values for a given parameter
	 *
	 * @param parameterId the id of the parameter
	 * @return user parameter default values for a given parameter
	 * @throws SQLException
	 */
	public List<UserParamDefault> getUserParamDefaults(int parameterId) throws SQLException {
		logger.debug("Entering getUserParamDefaults: parameterId={}", parameterId);

		String sql = SQL_SELECT_ALL_USER_PARAM_DEFAULTS + " WHERE PARAMETER_ID=?";
		ResultSetHandler<List<UserParamDefault>> h = new BeanListHandler<>(UserParamDefault.class, new UserParamDefaultMapper());
		return dbService.query(sql, h, parameterId);
	}

	/**
	 * Returns a user parameter default value
	 *
	 * @param id the parameter default key
	 * @return user parameter default value if found, null otherwise
	 * @throws SQLException
	 */
	public UserParamDefault getUserParamDefault(String id) throws SQLException {
		logger.debug("Entering getUserParamDefault: id='{}'", id);

		String sql = SQL_SELECT_ALL_USER_PARAM_DEFAULTS + " WHERE PARAM_DEFAULT_KEY=?";
		ResultSetHandler<UserParamDefault> h = new BeanHandler<>(UserParamDefault.class, new UserParamDefaultMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns <code>true</code> if a user parameter default exists
	 *
	 * @param userId the user id
	 * @param parameterId the parameter id
	 * @param value the default value
	 * @return <code>true</code> if the user parameter default exists
	 * @throws SQLException
	 */
	public boolean userParamDefaultExists(int userId, int parameterId,
			String value) throws SQLException {

		logger.debug("Entering userParamDefaultExists: userId={}, parameterId={},"
				+ " value='{}'", userId, parameterId, value);

		String sql = "SELECT COUNT(*) FROM ART_USER_PARAM_DEFAULTS"
				+ " WHERE USER_ID=? AND PARAMETER_ID=? AND PARAM_VALUE=?";

		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number recordCount = dbService.query(sql, h, userId, parameterId, value);

		if (recordCount == null || recordCount.longValue() == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns all user group parameter defaults
	 *
	 * @return all user group parameter defaults
	 * @throws SQLException
	 */
	public List<UserGroupParamDefault> getAllUserGroupParamDefaults() throws SQLException {
		logger.debug("Entering getAllUserGroupParamDefaults");

		ResultSetHandler<List<UserGroupParamDefault>> h = new BeanListHandler<>(UserGroupParamDefault.class, new UserGroupParamDefaultMapper());
		return dbService.query(SQL_SELECT_ALL_USER_GROUP_PARAM_DEFAULTS, h);
	}

	/**
	 * Returns user group parameter default values for a given user group and
	 * parameter
	 *
	 * @param userGroupId the user group id
	 * @param parameterId the parameter id
	 * @return user parameter default values for the user group and parameter
	 * @throws SQLException
	 */
	public List<UserGroupParamDefault> getUserGroupParamDefaults(int userGroupId,
			int parameterId) throws SQLException {

		logger.debug("Entering getUserGroupParamDefaults: userGroupId={},"
				+ " parameterId={}", userGroupId, parameterId);

		String sql = SQL_SELECT_ALL_USER_GROUP_PARAM_DEFAULTS
				+ " WHERE USER_GROUP_ID=? AND PARAMETER_ID=?";

		ResultSetHandler<List<UserGroupParamDefault>> h = new BeanListHandler<>(UserGroupParamDefault.class, new UserGroupParamDefaultMapper());
		return dbService.query(sql, h, userGroupId, parameterId);
	}

	/**
	 * Returns the user group parameter default values for a given parameter
	 *
	 * @param parameterId the id of the parameter
	 * @return user group parameter default values for a given parameter
	 * @throws SQLException
	 */
	public List<UserGroupParamDefault> getUserGroupParamDefaults(int parameterId) throws SQLException {
		logger.debug("Entering getUserGroupParamDefaults: parameterId={}", parameterId);

		String sql = SQL_SELECT_ALL_USER_GROUP_PARAM_DEFAULTS + " WHERE PARAMETER_ID=?";
		ResultSetHandler<List<UserGroupParamDefault>> h = new BeanListHandler<>(UserGroupParamDefault.class, new UserGroupParamDefaultMapper());
		return dbService.query(sql, h, parameterId);
	}

	/**
	 * Returns a user group parameter default value
	 *
	 * @param id the parameter default key
	 * @return user group parameter default value if found, null otherwise
	 * @throws SQLException
	 */
	public UserGroupParamDefault getUserGroupParamDefault(String id) throws SQLException {
		logger.debug("Entering getUserGroupParamDefault: id='{}'", id);

		String sql = SQL_SELECT_ALL_USER_GROUP_PARAM_DEFAULTS + " WHERE PARAM_DEFAULT_KEY=?";
		ResultSetHandler<UserGroupParamDefault> h = new BeanHandler<>(UserGroupParamDefault.class, new UserGroupParamDefaultMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns <code>true</code> if a user group parameter default exists
	 *
	 * @param userGroupId the user group id
	 * @param parameterId the parameter id
	 * @param value the default value
	 * @return <code>true</code> if the user group parameter default exists
	 * @throws SQLException
	 */
	public boolean userGroupParamDefaultExists(int userGroupId, int parameterId,
			String value) throws SQLException {

		logger.debug("Entering userGroupParamDefaultExists: userGroupId={},"
				+ " parameterId={}, value='{}'", userGroupId, parameterId, value);

		String sql = "SELECT COUNT(*) FROM ART_USER_GROUP_PARAM_DEFAULTS"
				+ " WHERE USER_GROUP_ID=? AND PARAMETER_ID=? AND PARAM_VALUE=?";

		ResultSetHandler<Number> h = new ScalarHandler<>();
		Number recordCount = dbService.query(sql, h, userGroupId, parameterId, value);

		if (recordCount == null || recordCount.longValue() == 0) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Deletes a user parameter default
	 *
	 * @param id the parameter default key
	 * @throws SQLException
	 */
	public void deleteUserParamDefault(String id) throws SQLException {
		logger.debug("Entering deleteUserParamDefault: id='{}'", id);

		String sql;

		sql = "DELETE FROM ART_USER_PARAM_DEFAULTS WHERE PARAM_DEFAULT_KEY=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes a user group parameter default
	 *
	 * @param id the parameter default key
	 * @throws SQLException
	 */
	public void deleteUserGroupParamDefault(String id) throws SQLException {
		logger.debug("Entering deleteUserGroupParamDefault: id='{}'", id);

		String sql;

		sql = "DELETE FROM ART_USER_GROUP_PARAM_DEFAULTS WHERE PARAM_DEFAULT_KEY=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes parameter defaults for a given parameter
	 *
	 * @param users user ids
	 * @param userGroups user group ids
	 * @param parameterId the parameter id
	 * @throws SQLException
	 */
	public void deleteParamDefaults(Integer[] users, Integer[] userGroups,
			int parameterId) throws SQLException {

		logger.debug("Entering deleteParamDefaults: parameterId={}", parameterId);

		//use parameterized where in
		//https://stackoverflow.com/questions/2861230/what-is-the-best-approach-using-jdbc-for-parameterizing-an-in-clause
		//http://www.javaranch.com/journal/200510/Journal200510.jsp#a2
		//delete user values
		if (users != null) {
			String baseSql = "DELETE FROM ART_USER_PARAM_DEFAULTS"
					+ " WHERE USER_ID IN(%s) AND PARAMETER_ID=?";

			//prepare parameter placeholders for user ids
			String userIdPlaceHolders = StringUtils.repeat("?", ",", users.length);

			//set final sql string to be used
			String finalSql = String.format(baseSql, userIdPlaceHolders);

			//prepare final parameter values, in the correct order to match the placeholders
			List<Object> finalValues = new ArrayList<>();
			finalValues.addAll(Arrays.asList(users));
			finalValues.add(parameterId);

			dbService.update(finalSql, finalValues.toArray());
		}

		//delete user group values
		if (userGroups != null) {
			String baseSql = "DELETE FROM ART_USER_GROUP_PARAM_DEFAULTS"
					+ " WHERE USER_GROUP_ID IN(%s) AND PARAMETER_ID=?";

			//prepare parameter placeholders for user group ids
			String userGroupIdPlaceHolders = StringUtils.repeat("?", ",", userGroups.length);

			//set final sql string to be used
			String finalSql = String.format(baseSql, userGroupIdPlaceHolders);

			//prepare final parameter values, in the correct order to match the placeholders
			List<Object> finalValues = new ArrayList<>();
			finalValues.addAll(Arrays.asList(userGroups));
			finalValues.add(parameterId);

			dbService.update(finalSql, finalValues.toArray());
		}
	}

	/**
	 * Adds a parameter default for users or user groups
	 *
	 * @param users user ids
	 * @param userGroups user group ids
	 * @param parameterId the parameter id
	 * @param value the default value
	 * @throws SQLException
	 */
	public void addParamDefault(Integer[] users, Integer[] userGroups,
			Integer parameterId, String value) throws SQLException {

		logger.debug("Entering addParamDefault: parameterId={}, value='{}'",
				parameterId, value);

		Objects.requireNonNull(parameterId, "parameterId must not be null");

		//add user parameter defaults
		if (users != null) {
			String sql = "INSERT INTO ART_USER_PARAM_DEFAULTS (PARAM_DEFAULT_KEY,"
					+ " USER_ID, PARAMETER_ID, PARAM_VALUE)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

			for (Integer userId : users) {
				if (!userParamDefaultExists(userId, parameterId, value)) {
					dbService.update(sql, ArtUtils.getDatabaseUniqueId(), userId,
							parameterId, value);
				}
			}
		}

		//add user group parameter defaults
		if (userGroups != null) {
			String sql = "INSERT INTO ART_USER_GROUP_PARAM_DEFAULTS (PARAM_DEFAULT_KEY,"
					+ " USER_GROUP_ID, PARAMETER_ID, PARAM_VALUE)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

			for (Integer userGroupId : userGroups) {
				if (!userGroupParamDefaultExists(userGroupId, parameterId, value)) {
					dbService.update(sql, ArtUtils.getDatabaseUniqueId(), userGroupId,
							parameterId, value);
				}
			}
		}
	}

	/**
	 * Updates a user parameter default value
	 *
	 * @param paramDefault the updated parameter default value
	 * @throws SQLException
	 */
	public void updateUserParamDefault(UserParamDefault paramDefault) throws SQLException {
		logger.debug("Entering updateUserParamDefault: paramDefault={}", paramDefault);

		String sql = "UPDATE ART_USER_PARAM_DEFAULTS SET PARAM_VALUE=?"
				+ " WHERE PARAM_DEFAULT_KEY=?";

		Object[] values = {
			paramDefault.getValue(),
			paramDefault.getParamDefaultKey()
		};

		dbService.update(sql, values);
	}

	/**
	 * Updates a user group parameter default value
	 *
	 * @param paramDefault the updated parameter default value
	 * @throws SQLException
	 */
	public void updateUserGroupParamDefault(UserGroupParamDefault paramDefault) throws SQLException {
		logger.debug("Entering updateUserGroupParamDefault: paramDefault={}", paramDefault);

		String sql = "UPDATE ART_USER_GROUP_PARAM_DEFAULTS SET PARAM_VALUE=?"
				+ " WHERE PARAM_DEFAULT_KEY=?";

		Object[] values = {
			paramDefault.getValue(),
			paramDefault.getParamDefaultKey()
		};

		dbService.update(sql, values);
	}

	/**
	 * Returns parameter default values for a given user and parameter
	 *
	 * @param user the user
	 * @param parameterId the parameter id
	 * @return parameter default values for a given user and parameter
	 * @throws SQLException
	 */
	public Map<String, String[]> getParameterDefaultValues(User user, int parameterId)
			throws SQLException {

		logger.debug("Entering getParameterDefaultValues: user={}, parameterId={}",
				user, parameterId);

		Objects.requireNonNull(user, "user must not be null");

		Map<String, List<String>> paramValues = new HashMap<>();

		int userId = user.getUserId();
		List<UserParamDefault> userParamDefaults = getUserParamDefaults(userId, parameterId);

		for (UserParamDefault userParamDefault : userParamDefaults) {
			String paramName = userParamDefault.getParameter().getName();
			String htmlParamName = ArtUtils.PARAM_PREFIX + paramName;
			String value = userParamDefault.getValue();
			List<String> values = paramValues.get(htmlParamName);
			if (values == null) {
				values = new ArrayList<>();
				paramValues.put(htmlParamName, values);
			}
			values.add(value);
		}

		List<UserGroup> userGroups = user.getUserGroups();
		if (CollectionUtils.isNotEmpty(userGroups)) {
			for (UserGroup userGroup : userGroups) {
				int userGroupId = userGroup.getUserGroupId();
				List<UserGroupParamDefault> userGroupParamDefaults = getUserGroupParamDefaults(userGroupId, parameterId);

				for (UserGroupParamDefault userGroupParamDefault : userGroupParamDefaults) {
					String paramName = userGroupParamDefault.getParameter().getName();
					String htmlParamName = ArtUtils.PARAM_PREFIX + paramName;
					String value = userGroupParamDefault.getValue();
					List<String> values = paramValues.get(htmlParamName);
					if (values == null) {
						values = new ArrayList<>();
						paramValues.put(htmlParamName, values);
					}
					values.add(value);
				}
			}
		}

		Map<String, String[]> finalValues = new HashMap<>();

		for (Map.Entry<String, List<String>> entry : paramValues.entrySet()) {
			String name = entry.getKey();
			List<String> values = entry.getValue();
			String[] valuesArray = values.toArray(new String[0]);
			finalValues.put(name, valuesArray);
		}

		return finalValues;
	}

}
