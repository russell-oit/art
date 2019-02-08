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
package art.usergroupmembership;

import art.dbutils.DbService;
import art.user.UserService;
import art.usergroup.UserGroupService;
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
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving user group memberships
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupMembershipService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupMembershipService.class);

	private final DbService dbService;
	private final UserService userService;
	private final UserGroupService userGroupService;

	@Autowired
	public UserGroupMembershipService(DbService dbService, UserService userService,
			UserGroupService userGroupService) {

		this.dbService = dbService;
		this.userService = userService;
		this.userGroupService = userGroupService;
	}

	public UserGroupMembershipService() {
		dbService = new DbService();
		userService = new UserService();
		userGroupService = new UserGroupService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_USER_USERGROUP_MAP";

	/**
	 * Maps a resultset to an object
	 */
	private class UserGroupMembershipMapper extends BasicRowProcessor {

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
			UserGroupMembership membership = new UserGroupMembership();

			membership.setUser(userService.getUser(rs.getInt("USER_ID")));
			membership.setUserGroup(userGroupService.getUserGroup(rs.getInt("USER_GROUP_ID")));

			return type.cast(membership);
		}
	}

	/**
	 * Returns all user group memberships
	 *
	 * @return all user group memberships
	 * @throws SQLException
	 */
	public List<UserGroupMembership> getAllUserGroupMemberships() throws SQLException {
		logger.debug("Entering getAllUserGroupMemberships");

		ResultSetHandler<List<UserGroupMembership>> h = new BeanListHandler<>(UserGroupMembership.class, new UserGroupMembershipMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns user group memberships for a particular user group
	 *
	 * @param userGroupId the user group id
	 * @return user group memberships for a particular user group
	 * @throws SQLException
	 */
	public List<UserGroupMembership> getUserGroupMembershipsForUserGroup(int userGroupId)
			throws SQLException {

		logger.debug("Entering getUserGroupMembershipsForUserGroup: userGroupId={}", userGroupId);

		String sql = SQL_SELECT_ALL + " WHERE USER_GROUP_ID=?";
		ResultSetHandler<List<UserGroupMembership>> h = new BeanListHandler<>(UserGroupMembership.class, new UserGroupMembershipMapper());
		return dbService.query(sql, h, userGroupId);
	}

}
