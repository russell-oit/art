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
package art.fixedparamvalue;

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
 * Provides methods for retrieving, adding, updating and deleting fixed parameter values
 *
 * @author Timothy Anyona
 */
@Service
public class FixedParamValueService {

	private static final Logger logger = LoggerFactory.getLogger(FixedParamValueService.class);

	private final DbService dbService;
	private final UserService userService;
	private final ParameterService parameterService;
	private final UserGroupService userGroupService;

	@Autowired
	public FixedParamValueService(DbService dbService, UserService userService,
			ParameterService parameterService, UserGroupService userGroupService) {

		this.dbService = dbService;
		this.userService = userService;
		this.parameterService = parameterService;
		this.userGroupService = userGroupService;
	}

	public FixedParamValueService() {
		dbService = new DbService();
		userService = new UserService();
		parameterService = new ParameterService();
		userGroupService = new UserGroupService();
	}

	private final String SQL_SELECT_ALL_USER_FIXED_PARAM_VALUES = "SELECT * FROM ART_USER_FIXED_PARAM_VAL AUFPV";
	private final String SQL_SELECT_ALL_USER_GROUP_FIXED_PARAM_VALUES = "SELECT * FROM ART_USER_GROUP_FIXED_PARAM_VAL AUGFPV";

	/**
	 * Maps a resultset to an object
	 */
	private class UserFixedParamValueMapper extends BasicRowProcessor {

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
			UserFixedParamValue fixedParamValue = new UserFixedParamValue();

			fixedParamValue.setFixedParamValueKey(rs.getString("FIXED_VALUE_KEY"));
			fixedParamValue.setValue(rs.getString("PARAM_VALUE"));

			User user = userService.getUser(rs.getInt("USER_ID"));
			fixedParamValue.setUser(user);

			Parameter parameter = parameterService.getParameter(rs.getInt("PARAMETER_ID"));
			fixedParamValue.setParameter(parameter);

			return type.cast(fixedParamValue);
		}
	}

	/**
	 * Maps a resultset to an object
	 */
	private class UserGroupFixedParamValueMapper extends BasicRowProcessor {

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
			UserGroupFixedParamValue fixedParamValue = new UserGroupFixedParamValue();

			fixedParamValue.setFixedParamValueKey(rs.getString("FIXED_VALUE_KEY"));
			fixedParamValue.setValue(rs.getString("PARAM_VALUE"));

			UserGroup userGroup = userGroupService.getUserGroup(rs.getInt("USER_GROUP_ID"));
			fixedParamValue.setUserGroup(userGroup);

			Parameter parameter = parameterService.getParameter(rs.getInt("PARAMETER_ID"));
			fixedParamValue.setParameter(parameter);

			return type.cast(fixedParamValue);
		}
	}

	/**
	 * Returns all user fixed parameter values
	 *
	 * @return all user fixed parameter values
	 * @throws SQLException
	 */
	public List<UserFixedParamValue> getAllUserFixedParamValues() throws SQLException {
		logger.debug("Entering getAllUserFixedParamValues");

		ResultSetHandler<List<UserFixedParamValue>> h = new BeanListHandler<>(UserFixedParamValue.class, new UserFixedParamValueMapper());
		return dbService.query(SQL_SELECT_ALL_USER_FIXED_PARAM_VALUES, h);
	}

	/**
	 * Returns user fixed parameter values for a given user and parameter
	 *
	 * @param userId the user id
	 * @param parameterId the parameter id
	 * @return user fixed parameter values for the user and parameter
	 * @throws SQLException
	 */
	public List<UserFixedParamValue> getUserFixedParamValues(int userId, int parameterId)
			throws SQLException {

		logger.debug("Entering getUserFixedParamValues: userId={},"
				+ " parameterId={}", userId, parameterId);

		String sql = SQL_SELECT_ALL_USER_FIXED_PARAM_VALUES
				+ " WHERE USER_ID=? AND PARAMETER_ID=?";

		ResultSetHandler<List<UserFixedParamValue>> h = new BeanListHandler<>(UserFixedParamValue.class, new UserFixedParamValueMapper());
		return dbService.query(sql, h, userId, parameterId);
	}

	/**
	 * Returns the user fixed parameter values for a given parameter
	 *
	 * @param parameterId the id of the parameter
	 * @return user fixed parameter values for a given parameter
	 * @throws SQLException
	 */
	public List<UserFixedParamValue> getUserFixedParamValues(int parameterId) throws SQLException {
		logger.debug("Entering getUserFixedParamValues: parameterId={}", parameterId);

		String sql = SQL_SELECT_ALL_USER_FIXED_PARAM_VALUES + " WHERE PARAMETER_ID=?";
		ResultSetHandler<List<UserFixedParamValue>> h = new BeanListHandler<>(UserFixedParamValue.class, new UserFixedParamValueMapper());
		return dbService.query(sql, h, parameterId);
	}

	/**
	 * Returns a user fixed parameter value
	 *
	 * @param id the fixed parameter value key
	 * @return user fixed parameter value if found, null otherwise
	 * @throws SQLException
	 */
	public UserFixedParamValue getUserFixedParamValue(String id) throws SQLException {
		logger.debug("Entering getUserFixedParamValue: id='{}'", id);

		String sql = SQL_SELECT_ALL_USER_FIXED_PARAM_VALUES + " WHERE FIXED_VALUE_KEY=?";
		ResultSetHandler<UserFixedParamValue> h = new BeanHandler<>(UserFixedParamValue.class, new UserFixedParamValueMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns <code>true</code> if a user fixed parameter value exists
	 *
	 * @param userId the user id
	 * @param parameterId the parameter id
	 * @param value the value
	 * @return <code>true</code> if the user fixed parameter value exists
	 * @throws SQLException
	 */
	public boolean userFixedParamValueExists(int userId, int parameterId,
			String value) throws SQLException {

		logger.debug("Entering userFixedParamValueExists: userId={}, parameterId={},"
				+ " value='{}'", userId, parameterId, value);

		String sql = "SELECT COUNT(*) FROM ART_USER_FIXED_PARAM_VAL"
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
	 * Returns all user group fixed parameter values
	 *
	 * @return all user group fixed parameter values
	 * @throws SQLException
	 */
	public List<UserGroupFixedParamValue> getAllUserGroupFixedParamValues() throws SQLException {
		logger.debug("Entering getAllUserGroupFixedParamValues");

		ResultSetHandler<List<UserGroupFixedParamValue>> h = new BeanListHandler<>(UserGroupFixedParamValue.class, new UserGroupFixedParamValueMapper());
		return dbService.query(SQL_SELECT_ALL_USER_GROUP_FIXED_PARAM_VALUES, h);
	}

	/**
	 * Returns user group fixed parameter values for a given user group and
	 * parameter
	 *
	 * @param userGroupId the user group id
	 * @param parameterId the parameter id
	 * @return user fixed parameter values for the user group and parameter
	 * @throws SQLException
	 */
	public List<UserGroupFixedParamValue> getUserGroupFixedParamValues(int userGroupId,
			int parameterId) throws SQLException {

		logger.debug("Entering getUserGroupFixedParamValues: userGroupId={},"
				+ " parameterId={}", userGroupId, parameterId);

		String sql = SQL_SELECT_ALL_USER_GROUP_FIXED_PARAM_VALUES
				+ " WHERE USER_GROUP_ID=? AND PARAMETER_ID=?";

		ResultSetHandler<List<UserGroupFixedParamValue>> h = new BeanListHandler<>(UserGroupFixedParamValue.class, new UserGroupFixedParamValueMapper());
		return dbService.query(sql, h, userGroupId, parameterId);
	}

	/**
	 * Returns the user group fixed parameter values for a given parameter
	 *
	 * @param parameterId the id of the parameter
	 * @return user group fixed parameter values for a given parameter
	 * @throws SQLException
	 */
	public List<UserGroupFixedParamValue> getUserGroupFixedParamValues(int parameterId)
			throws SQLException {
		
		logger.debug("Entering getUserGroupFixedParamValues: parameterId={}", parameterId);

		String sql = SQL_SELECT_ALL_USER_GROUP_FIXED_PARAM_VALUES + " WHERE PARAMETER_ID=?";
		ResultSetHandler<List<UserGroupFixedParamValue>> h = new BeanListHandler<>(UserGroupFixedParamValue.class, new UserGroupFixedParamValueMapper());
		return dbService.query(sql, h, parameterId);
	}

	/**
	 * Returns a user group fixed parameter value
	 *
	 * @param id the fixed parameter value key
	 * @return user group fixed parameter value if found, null otherwise
	 * @throws SQLException
	 */
	public UserGroupFixedParamValue getUserGroupFixedParamValue(String id) throws SQLException {
		logger.debug("Entering getUserGroupFixedParamValue: id='{}'", id);

		String sql = SQL_SELECT_ALL_USER_GROUP_FIXED_PARAM_VALUES + " WHERE FIXED_VALUE_KEY=?";
		ResultSetHandler<UserGroupFixedParamValue> h = new BeanHandler<>(UserGroupFixedParamValue.class, new UserGroupFixedParamValueMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns <code>true</code> if a user group fixed parameter value exists
	 *
	 * @param userGroupId the user group id
	 * @param parameterId the parameter id
	 * @param value the value
	 * @return <code>true</code> if the user group fixed parameter value exists
	 * @throws SQLException
	 */
	public boolean userGroupFixedParamValueExists(int userGroupId, int parameterId,
			String value) throws SQLException {

		logger.debug("Entering userGroupFixedParamValueExists: userGroupId={},"
				+ " parameterId={}, value='{}'", userGroupId, parameterId, value);

		String sql = "SELECT COUNT(*) FROM ART_USER_GROUP_FIXED_PARAM_VAL"
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
	 * Deletes a user fixed parameter value
	 *
	 * @param id the fixed parameter value key
	 * @throws SQLException
	 */
	public void deleteUserFixedParamValue(String id) throws SQLException {
		logger.debug("Entering deleteUserFixedParamValue: id='{}'", id);

		String sql;

		sql = "DELETE FROM ART_USER_FIXED_PARAM_VAL WHERE FIXED_VALUE_KEY=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes a user group fixed parameter value
	 *
	 * @param id the fixed parameter value key
	 * @throws SQLException
	 */
	public void deleteUserGroupFixedParamValue(String id) throws SQLException {
		logger.debug("Entering deleteUserGroupFixedParamValue: id='{}'", id);

		String sql;

		sql = "DELETE FROM ART_USER_GROUP_FIXED_PARAM_VAL WHERE FIXED_VALUE_KEY=?";
		dbService.update(sql, id);
	}

	/**
	 * Deletes fixed parameter values for a given parameter
	 *
	 * @param users user ids
	 * @param userGroups user group ids
	 * @param parameterId the parameter id
	 * @throws SQLException
	 */
	public void deleteFixedParamValues(Integer[] users, Integer[] userGroups,
			int parameterId) throws SQLException {

		logger.debug("Entering deleteFixedParamValues: parameterId={}", parameterId);

		//use parameterized where in
		//https://stackoverflow.com/questions/2861230/what-is-the-best-approach-using-jdbc-for-parameterizing-an-in-clause
		//http://www.javaranch.com/journal/200510/Journal200510.jsp#a2
		//delete user values
		if (users != null) {
			String baseSql = "DELETE FROM ART_USER_FIXED_PARAM_VAL"
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
			String baseSql = "DELETE FROM ART_USER_GROUP_FIXED_PARAM_VAL"
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
	 * Adds a fixed parameter value for users or user groups
	 *
	 * @param users user ids
	 * @param userGroups user group ids
	 * @param parameterId the parameter id
	 * @param value the value
	 * @throws SQLException
	 */
	public void addFixedParamValue(Integer[] users, Integer[] userGroups,
			Integer parameterId, String value) throws SQLException {

		logger.debug("Entering addFixedParamValue: parameterId={}, value='{}'",
				parameterId, value);

		Objects.requireNonNull(parameterId, "parameterId must not be null");

		//add user fixed parameter values
		if (users != null) {
			String sql = "INSERT INTO ART_USER_FIXED_PARAM_VAL (FIXED_VALUE_KEY,"
					+ " USER_ID, PARAMETER_ID, PARAM_VALUE)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

			for (Integer userId : users) {
				if (!userFixedParamValueExists(userId, parameterId, value)) {
					dbService.update(sql, ArtUtils.getUniqueId(), userId,
							parameterId, value);
				}
			}
		}

		//add user group fixed parameter values
		if (userGroups != null) {
			String sql = "INSERT INTO ART_USER_GROUP_FIXED_PARAM_VAL (FIXED_VALUE_KEY,"
					+ " USER_GROUP_ID, PARAMETER_ID, PARAM_VALUE)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

			for (Integer userGroupId : userGroups) {
				if (!userGroupFixedParamValueExists(userGroupId, parameterId, value)) {
					dbService.update(sql, ArtUtils.getUniqueId(), userGroupId,
							parameterId, value);
				}
			}
		}
	}

	/**
	 * Updates a user fixed parameter value
	 *
	 * @param fixedParamValue the updated fixed parameter value
	 * @throws SQLException
	 */
	public void updateUserFixedParamValue(UserFixedParamValue fixedParamValue) throws SQLException {
		logger.debug("Entering updateUserFixedParamValue: fixedParamValue={}", fixedParamValue);

		String sql = "UPDATE ART_USER_FIXED_PARAM_VAL SET PARAM_VALUE=?"
				+ " WHERE FIXED_VALUE_KEY=?";

		Object[] values = {
			fixedParamValue.getValue(),
			fixedParamValue.getFixedParamValueKey()
		};

		dbService.update(sql, values);
	}

	/**
	 * Updates a user group fixed parameter value
	 *
	 * @param fixedParamValue the updated fixed parameter value
	 * @throws SQLException
	 */
	public void updateUserGroupFixedParamValue(UserGroupFixedParamValue fixedParamValue)
			throws SQLException {
		
		logger.debug("Entering updateUserGroupFixedParamValue: fixedParamValue={}", fixedParamValue);

		String sql = "UPDATE ART_USER_GROUP_FIXED_PARAM_VAL SET PARAM_VALUE=?"
				+ " WHERE FIXED_VALUE_KEY=?";

		Object[] values = {
			fixedParamValue.getValue(),
			fixedParamValue.getFixedParamValueKey()
		};

		dbService.update(sql, values);
	}

	/**
	 * Returns fixed parameter values for a given user and parameter
	 *
	 * @param user the user
	 * @param parameterId the parameter id
	 * @return fixed parameter values for a given user and parameter
	 * @throws SQLException
	 */
	public Map<String, String[]> getFixedParameterValues(User user, int parameterId)
			throws SQLException {

		logger.debug("Entering getFixedParameterValues: user={}, parameterId={}",
				user, parameterId);

		Objects.requireNonNull(user, "user must not be null");

		Map<String, List<String>> paramValues = new HashMap<>();

		int userId = user.getUserId();
		List<UserFixedParamValue> userFixedParamValues = getUserFixedParamValues(userId, parameterId);

		for (UserFixedParamValue userFixedParamValue : userFixedParamValues) {
			String paramName = userFixedParamValue.getParameter().getName();
			String htmlParamName = ArtUtils.PARAM_PREFIX + paramName;
			String value = userFixedParamValue.getValue();
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
				List<UserGroupFixedParamValue> userGroupFixedParamValues = getUserGroupFixedParamValues(userGroupId, parameterId);

				for (UserGroupFixedParamValue userGroupFixedParamValue : userGroupFixedParamValues) {
					String paramName = userGroupFixedParamValue.getParameter().getName();
					String htmlParamName = ArtUtils.PARAM_PREFIX + paramName;
					String value = userGroupFixedParamValue.getValue();
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

