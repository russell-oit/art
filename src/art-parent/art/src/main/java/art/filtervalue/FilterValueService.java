/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.filtervalue;

import art.dbutils.DbService;
import art.enums.ParameterDataType;
import art.filter.Filter;
import art.user.User;
import art.usergroup.UserGroup;
import art.utils.ArtUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to filter values
 *
 * @author Timothy Anyona
 */
@Service
public class FilterValueService {

	private static final Logger logger = LoggerFactory.getLogger(FilterValueService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL_USER_FILTER_VALUES
			= "SELECT AUR.RULE_VALUE_KEY, AUR.RULE_VALUE, AU.USER_ID, AU.USERNAME,"
			+ " AR.RULE_ID, AR.RULE_NAME, AR.DATA_TYPE"
			+ " FROM ART_USER_RULES AUR"
			+ " INNER JOIN ART_USERS AU ON"
			+ " AUR.USER_ID=AU.USER_ID"
			+ " INNER JOIN ART_RULES AR ON"
			+ " AUR.RULE_ID=AR.RULE_ID";

	private final String SQL_SELECT_ALL_USER_GROUP_FILTER_VALUES
			= "SELECT AUGR.RULE_VALUE_KEY, AUGR.RULE_VALUE, AUG.USER_GROUP_ID, AUG.NAME AS USER_GROUP_NAME,"
			+ " AR.RULE_ID, AR.RULE_NAME, AR.DATA_TYPE"
			+ " FROM ART_USER_GROUP_RULES AUGR"
			+ " INNER JOIN ART_USER_GROUPS AUG ON"
			+ " AUGR.USER_GROUP_ID=AUG.USER_GROUP_ID"
			+ " INNER JOIN ART_RULES AR ON"
			+ " AUGR.RULE_ID=AR.RULE_ID";

	/**
	 * Maps a resultset to an object
	 */
	private class UserFilterValueMapper extends BasicRowProcessor {

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
			UserFilterValue value = new UserFilterValue();

			value.setFilterValue(rs.getString("RULE_VALUE"));
			value.setFilterValueKey(rs.getString("RULE_VALUE_KEY"));

			User user = new User();
			user.setUserId(rs.getInt("USER_ID"));
			user.setUsername(rs.getString("USERNAME"));

			value.setUser(user);

			Filter filter = new Filter();
			filter.setFilterId(rs.getInt("RULE_ID"));
			filter.setName(rs.getString("RULE_NAME"));
			filter.setDataType(ParameterDataType.toEnum(rs.getString("DATA_TYPE")));

			value.setFilter(filter);

			return type.cast(value);
		}
	}

	/**
	 * Maps a resultset to an object
	 */
	private class UserGroupFilterValueMapper extends BasicRowProcessor {

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
			UserGroupFilterValue value = new UserGroupFilterValue();

			value.setFilterValue(rs.getString("RULE_VALUE"));
			value.setFilterValueKey(rs.getString("RULE_VALUE_KEY"));

			UserGroup userGroup = new UserGroup();
			userGroup.setUserGroupId(rs.getInt("USER_GROUP_ID"));
			userGroup.setName(rs.getString("USER_GROUP_NAME"));

			value.setUserGroup(userGroup);

			Filter filter = new Filter();
			filter.setFilterId(rs.getInt("RULE_ID"));
			filter.setName(rs.getString("RULE_NAME"));
			filter.setDataType(ParameterDataType.toEnum(rs.getString("DATA_TYPE")));

			value.setFilter(filter);

			return type.cast(value);
		}
	}

	/**
	 * Get all user filter values
	 *
	 * @return list of all user filter values, empty list otherwise
	 * @throws SQLException
	 */
	public List<UserFilterValue> getAllUserFilterValues() throws SQLException {
		logger.debug("Entering getAllUserFilterValues");

		ResultSetHandler<List<UserFilterValue>> h = new BeanListHandler<>(UserFilterValue.class, new UserFilterValueMapper());
		return dbService.query(SQL_SELECT_ALL_USER_FILTER_VALUES, h);
	}

	/**
	 * Get a user filter value
	 *
	 * @param id rule value key
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	public UserFilterValue getUserFilterValue(String id) throws SQLException {
		logger.debug("Entering getUserFilterValue");

		String sql = SQL_SELECT_ALL_USER_FILTER_VALUES + " WHERE RULE_VALUE_KEY=?";
		ResultSetHandler<UserFilterValue> h = new BeanHandler<>(UserFilterValue.class, new UserFilterValueMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Get all user group filter values
	 *
	 * @return list of all user group filter values, empty list otherwise
	 * @throws SQLException
	 */
	public List<UserGroupFilterValue> getAllUserGroupFilterValues() throws SQLException {
		logger.debug("Entering getAllUserGroupFilterValues");

		ResultSetHandler<List<UserGroupFilterValue>> h = new BeanListHandler<>(UserGroupFilterValue.class, new UserGroupFilterValueMapper());
		return dbService.query(SQL_SELECT_ALL_USER_GROUP_FILTER_VALUES, h);
	}

	/**
	 * Get a user group filter value
	 *
	 * @param id rule value key
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	public UserGroupFilterValue getUserGroupFilterValue(String id) throws SQLException {
		logger.debug("Entering getUserGroupFilterValue");

		String sql = SQL_SELECT_ALL_USER_GROUP_FILTER_VALUES + " WHERE RULE_VALUE_KEY=?";
		ResultSetHandler<UserGroupFilterValue> h = new BeanHandler<>(UserGroupFilterValue.class, new UserGroupFilterValueMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Delete a user filter value
	 *
	 * @param id filter value key
	 * @throws SQLException
	 */
	public void deleteUserFilterValue(String id) throws SQLException {
		logger.debug("Entering deleteUserFilterValue: id='{}'", id);

		String sql;

		sql = "DELETE FROM ART_USER_RULES WHERE RULE_VALUE_KEY=?";
		dbService.update(sql, id);
	}

	/**
	 * Delete a user group filter value
	 *
	 * @param id filter value key
	 * @throws SQLException
	 */
	public void deleteUserGroupFilterValue(String id) throws SQLException {
		logger.debug("Entering deleteUserGroupFilterValue: id='{}'", id);

		String sql;

		sql = "DELETE FROM ART_USER_GROUP_RULES WHERE RULE_VALUE_KEY=?";
		dbService.update(sql, id);
	}

	/**
	 * Delete all user filter values
	 *
	 * @param users
	 * @param userGroups array of user group ids
	 * @param filterId
	 * @throws SQLException
	 */
	public void deleteAllFilterValues(String[] users, Integer[] userGroups,
			int filterId) throws SQLException {

		logger.debug("Entering deleteAllFilterValues: filterId={}", filterId);

		//use parameterized where in
		//https://stackoverflow.com/questions/2861230/what-is-the-best-approach-using-jdbc-for-parameterizing-an-in-clause
		//http://www.javaranch.com/journal/200510/Journal200510.jsp#a2
		//delete user values
		if (users != null) {
			String baseSql = "DELETE FROM ART_USER_RULES WHERE USER_ID IN(%s) AND RULE_ID=?";

			//get user ids
			List<Integer> userIds = new ArrayList<>();
			for (String user : users) {
				Integer userId = Integer.valueOf(StringUtils.substringBefore(user, "-"));
				//username won't be needed once user id columns completely replace username in foreign keys
				userIds.add(userId);
			}

			//prepare parameter placeholders for user ids
			String userIdPlaceHolders = StringUtils.repeat("?", ",", userIds.size());

			//set final sql string to be used
			String finalSql = String.format(baseSql, userIdPlaceHolders);

			//prepare final parameter values, in the correct order to match the placeholders
			List<Object> finalValues = new ArrayList<>();
			finalValues.addAll(userIds);
			finalValues.add(filterId);

			dbService.update(finalSql, finalValues.toArray());
		}

		//delete user group values
		if (userGroups != null) {
			String baseSql = "DELETE FROM ART_USER_GROUP_RULES"
					+ " WHERE USER_GROUP_ID IN(%s) AND RULE_ID=?";

			//prepare parameter placeholders for user group ids
			String userGroupIdPlaceHolders = StringUtils.repeat("?", ",", userGroups.length);

			//set final sql string to be used
			String finalSql = String.format(baseSql, userGroupIdPlaceHolders);

			//prepare final parameter values, in the correct order to match the placeholders
			List<Object> finalValues = new ArrayList<>();
			finalValues.addAll(Arrays.asList(userGroups));
			finalValues.add(filterId);

			dbService.update(finalSql, finalValues.toArray());
		}

	}

	/**
	 * Add filter value for users and/or user groups
	 *
	 * @param users
	 * @param userGroups array of user group ids
	 * @param filterKey
	 * @param filterValue
	 * @throws SQLException
	 */
	public void addFilterValue(String[] users, Integer[] userGroups,
			String filterKey, String filterValue) throws SQLException {

		logger.debug("Entering addFilterValue: filterId='{}', filterValue='{}'",
				filterKey, filterValue);

		if (filterKey == null) {
			logger.warn("Cannot add filter value. Filter not specified");
			return;
		}

		Integer filterId = Integer.valueOf(StringUtils.substringBefore(filterKey, "-"));
		//filter name won't be needed once rule id columns completely replace rule name in foreign keys
		String filterName = StringUtils.substringAfter(filterKey, "-");

		//add user filter values
		if (users != null) {
			String sql = "INSERT INTO ART_USER_RULES (RULE_VALUE_KEY, USER_ID, USERNAME, RULE_ID,"
					+ " RULE_NAME, RULE_VALUE)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 6) + ")";

			for (String user : users) {
				Integer userId = Integer.valueOf(StringUtils.substringBefore(user, "-"));
				//username won't be needed once user id columns completely replace username in foreign keys
				String username = StringUtils.substringAfter(user, "-");

				dbService.update(sql, ArtUtils.getUniqueId(), userId, username,
						filterId, filterName, filterValue);
			}
		}

		//add user group filter values
		if (userGroups != null) {
			String sql = "INSERT INTO ART_USER_GROUP_RULES (RULE_VALUE_KEY,"
					+ " USER_GROUP_ID, RULE_ID, RULE_NAME, RULE_VALUE)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 5) + ")";

			for (Integer userGroupId : userGroups) {
				dbService.update(sql, ArtUtils.getUniqueId(), userGroupId,
						filterId, filterName, filterValue);
			}
		}
	}

	/**
	 * Update a user filter value record
	 *
	 * @param value
	 * @throws SQLException
	 */
	public void updateUserFilterValue(UserFilterValue value) throws SQLException {
		logger.debug("Entering updateUserFilterValue: value={}", value);

		String sql = "UPDATE ART_USER_RULES SET RULE_VALUE=?"
				+ " WHERE RULE_VALUE_KEY=?";

		Object[] values = {
			value.getFilterValue(),
			value.getFilterValueKey()
		};

		dbService.update(sql, values);
	}

	/**
	 * Update a user group filter value record
	 *
	 * @param value
	 * @throws SQLException
	 */
	public void updateUserGroupFilterValue(UserGroupFilterValue value) throws SQLException {
		logger.debug("Entering updateUserGroupFilterValue: value={}", value);

		String sql = "UPDATE ART_USER_GROUP_RULES SET RULE_VALUE=?"
				+ " WHERE RULE_VALUE_KEY=?";
		
		Object[] values = {
			value.getFilterValue(),
			value.getFilterValueKey()
		};

		dbService.update(sql, values);
	}

}
