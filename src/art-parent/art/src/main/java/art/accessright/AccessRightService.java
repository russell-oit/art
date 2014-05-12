/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.accessright;

import art.dbutils.DbService;
import art.report.Report;
import art.reportgroup.ReportGroup;
import art.user.User;
import art.usergroup.UserGroup;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to access rights
 *
 * @author Timothy Anyona
 */
@Service
public class AccessRightService {

	private static final Logger logger = LoggerFactory.getLogger(AccessRightService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL_USER_REPORT_RIGHTS
			= "SELECT AU.USER_ID, AU.USERNAME, AQ.QUERY_ID, AQ.NAME AS REPORT_NAME"
			+ " FROM ART_USER_QUERIES AUQ"
			+ " INNER JOIN ART_USERS AU ON"
			+ " AUQ.USER_ID=AU.USER_ID"
			+ " INNER JOIN ART_QUERIES AQ ON"
			+ " AUQ.QUERY_ID=AQ.QUERY_ID";

	private final String SQL_SELECT_ALL_USER_REPORT_GROUP_RIGHTS
			= "SELECT AU.USER_ID, AU.USERNAME, AQG.QUERY_GROUP_ID, AQG.NAME AS GROUP_NAME"
			+ " FROM ART_USER_QUERY_GROUPS AUQG"
			+ " INNER JOIN ART_USERS AU ON"
			+ " AUQG.USER_ID=AU.USER_ID"
			+ " INNER JOIN ART_QUERY_GROUPS AQG ON"
			+ " AUQG.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID";

	private final String SQL_SELECT_ALL_USER_GROUP_REPORT_RIGHTS
			= "SELECT AUG.USER_GROUP_ID, AUG.NAME AS USER_GROUP_NAME, AQ.QUERY_ID, AQ.NAME AS REPORT_NAME"
			+ " FROM ART_USER_GROUP_QUERIES AUGQ"
			+ " INNER JOIN ART_USER_GROUPS AUG ON"
			+ " AUGQ.USER_GROUP_ID=AUG.USER_GROUP_ID"
			+ " INNER JOIN ART_QUERIES AQ ON"
			+ " AUGQ.QUERY_ID=AQ.QUERY_ID";

	private final String SQL_SELECT_ALL_USER_GROUP_REPORT_GROUP_RIGHTS
			= "SELECT AUG.USER_GROUP_ID, AUG.NAME AS USER_GROUP_NAME, AQG.QUERY_GROUP_ID,"
			+ " AQG.NAME AS REPORT_GROUP_NAME"
			+ " FROM ART_USER_GROUP_GROUPS AUGG"
			+ " INNER JOIN ART_USER_GROUPS AUG ON"
			+ " AUGG.USER_GROUP_ID=AUG.USER_GROUP_ID"
			+ " INNER JOIN ART_QUERY_GROUPS AQG ON"
			+ " AUGG.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID";

	/**
	 * Class to map resultset to an object
	 */
	private class UserReportRightMapper extends BasicRowProcessor {

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
			UserReportRight right = new UserReportRight();

			User user = new User();
			user.setUserId(rs.getInt("USER_ID"));
			user.setUsername(rs.getString("USERNAME"));

			right.setUser(user);

			Report report = new Report();
			report.setReportId(rs.getInt("QUERY_ID"));
			report.setName(rs.getString("REPORT_NAME"));

			right.setReport(report);

			return type.cast(right);
		}
	}

	/**
	 * Class to map resultset to an object
	 */
	private class UserReportGroupRightMapper extends BasicRowProcessor {

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
			UserReportGroupRight right = new UserReportGroupRight();

			User user = new User();
			user.setUserId(rs.getInt("USER_ID"));
			user.setUsername(rs.getString("USERNAME"));

			right.setUser(user);

			ReportGroup group = new ReportGroup();
			group.setReportGroupId(rs.getInt("QUERY_GROUP_ID"));
			group.setName(rs.getString("GROUP_NAME"));

			right.setReportGroup(group);

			return type.cast(right);
		}
	}

	/**
	 * Class to map resultset to an object
	 */
	private class UserGroupReportRightMapper extends BasicRowProcessor {

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
			UserGroupReportRight right = new UserGroupReportRight();

			UserGroup userGroup = new UserGroup();
			userGroup.setUserGroupId(rs.getInt("USER_GROUP_ID"));
			userGroup.setName(rs.getString("USER_GROUP_NAME"));

			right.setUserGroup(userGroup);

			Report report = new Report();
			report.setReportId(rs.getInt("QUERY_ID"));
			report.setName(rs.getString("REPORT_NAME"));

			right.setReport(report);

			return type.cast(right);
		}
	}

	/**
	 * Class to map resultset to an object
	 */
	private class UserGroupReportGroupRightMapper extends BasicRowProcessor {

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
			UserGroupReportGroupRight right = new UserGroupReportGroupRight();

			UserGroup userGroup = new UserGroup();
			userGroup.setUserGroupId(rs.getInt("USER_GROUP_ID"));
			userGroup.setName(rs.getString("USER_GROUP_NAME"));

			right.setUserGroup(userGroup);

			ReportGroup group = new ReportGroup();
			group.setReportGroupId(rs.getInt("QUERY_GROUP_ID"));
			group.setName(rs.getString("REPORT_GROUP_NAME"));

			right.setReportGroup(group);

			return type.cast(right);
		}
	}

	/**
	 * Get all user-report rights
	 *
	 * @return list of all user-report rights, empty list otherwise
	 * @throws SQLException
	 */
	public List<UserReportRight> getAllUserReportRights() throws SQLException {
		logger.debug("Entering getAllUserReportRights");

		ResultSetHandler<List<UserReportRight>> h = new BeanListHandler<>(UserReportRight.class, new UserReportRightMapper());
		return dbService.query(SQL_SELECT_ALL_USER_REPORT_RIGHTS, h);
	}

	/**
	 * Get all user-report group rights
	 *
	 * @return list of all user-report group rights, empty list otherwise
	 * @throws SQLException
	 */
	public List<UserReportGroupRight> getAllUserReportGroupRights() throws SQLException {
		logger.debug("Entering getAllUserReportGroupRights");

		ResultSetHandler<List<UserReportGroupRight>> h = new BeanListHandler<>(UserReportGroupRight.class, new UserReportGroupRightMapper());
		return dbService.query(SQL_SELECT_ALL_USER_REPORT_GROUP_RIGHTS, h);
	}

	/**
	 * Get all user group-report rights
	 *
	 * @return list of all user group-report rights, empty list otherwise
	 * @throws SQLException
	 */
	public List<UserGroupReportRight> getAllUserGroupReportRights() throws SQLException {
		logger.debug("Entering getAllUserGroupReportRights");

		ResultSetHandler<List<UserGroupReportRight>> h = new BeanListHandler<>(UserGroupReportRight.class, new UserGroupReportRightMapper());
		return dbService.query(SQL_SELECT_ALL_USER_GROUP_REPORT_RIGHTS, h);
	}

	/**
	 * Get all user group-report group rights
	 *
	 * @return list of all user group-report group rights, empty list otherwise
	 * @throws SQLException
	 */
	public List<UserGroupReportGroupRight> getAllUserGroupReportGroupRights() throws SQLException {
		logger.debug("Entering getAllUserGroupReportGroupRights");

		ResultSetHandler<List<UserGroupReportGroupRight>> h = new BeanListHandler<>(UserGroupReportGroupRight.class, new UserGroupReportGroupRightMapper());
		return dbService.query(SQL_SELECT_ALL_USER_GROUP_REPORT_GROUP_RIGHTS, h);
	}

	/**
	 * Delete a user-report right
	 *
	 * @param userId
	 * @param reportId
	 * @throws SQLException
	 */
	public void deleteUserReportRight(int userId, int reportId) throws SQLException {
		logger.debug("Entering deleteUserReportRight: userId={}, reportId={}", userId, reportId);

		String sql;

		sql = "DELETE FROM ART_USER_QUERIES WHERE USER_ID=? AND QUERY_ID=?";
		dbService.update(sql, userId, reportId);
	}

	/**
	 * Delete a user-report group right
	 *
	 * @param userId
	 * @param reportGroupId
	 * @throws SQLException
	 */
	public void deleteUserReportGroupRight(int userId, int reportGroupId) throws SQLException {
		logger.debug("Entering deleteUserReportGroupRight: userId={}, reportGroupId={}", userId, reportGroupId);

		String sql;

		sql = "DELETE FROM ART_USER_QUERY_GROUPS WHERE USER_ID=? AND QUERY_GROUP_ID=?";
		dbService.update(sql, userId, reportGroupId);
	}

	/**
	 * Delete a user group-report right
	 *
	 * @param userGroupId
	 * @param reportId
	 * @throws SQLException
	 */
	public void deleteUserGroupReportRight(int userGroupId, int reportId) throws SQLException {
		logger.debug("Entering deleteUserGroupReportRight: userGroupId={}, reportId={}", userGroupId, reportId);

		String sql;

		sql = "DELETE FROM ART_USER_GROUP_QUERIES WHERE USER_GROUP_ID=? AND QUERY_ID=?";
		dbService.update(sql, userGroupId, reportId);
	}

	/**
	 * Delete a user group-report group right
	 *
	 * @param userGroupId
	 * @param reportGroupId
	 * @throws SQLException
	 */
	public void deleteUserGroupReportGroupRight(int userGroupId, int reportGroupId) throws SQLException {
		logger.debug("Entering deleteUserGroupReportGroupRight: userGroupId={}, reportGroupId={}", userGroupId, reportGroupId);

		String sql;

		sql = "DELETE FROM ART_USER_GROUP_GROUPS WHERE USER_GROUP_ID=? AND QUERY_GROUP_ID=?";
		dbService.update(sql, userGroupId, reportGroupId);
	}

	/**
	 * Grant or revoke access rights
	 *
	 * @param action "grant" or "revoke". anything else will be treated as revoke
	 * @param users
	 * @param userGroups array of user group ids
	 * @param reports array of report ids
	 * @param reportGroups array of report group ids
	 * @throws SQLException
	 */
	public void updateAccessRights(String action, String[] users, Integer[] userGroups,
			Integer[] reports, Integer[] reportGroups) throws SQLException {

		logger.debug("Entering updateAccessRights: action='{}'", action);

		boolean grant;
		if (StringUtils.equalsIgnoreCase(action, "grant")) {
			grant = true;
		} else {
			grant = false;
		}

		//update user rights
		if (users != null) {
			String sqlUserReport;
			String sqlUserReportGroup;

			if (grant) {
				sqlUserReport = "INSERT INTO ART_USER_QUERIES (USER_ID, USERNAME, QUERY_ID) VALUES (?, ?, ?)";
				sqlUserReportGroup = "INSERT INTO ART_USER_QUERY_GROUPS (USER_ID, USERNAME, QUERY_GROUP_ID) VALUES (?, ?, ?)";
			} else {
				sqlUserReport = "DELETE FROM ART_USER_QUERIES WHERE USER_ID=? AND USERNAME=? AND QUERY_ID=?";
				sqlUserReportGroup = "DELETE FROM ART_USER_QUERY_GROUPS WHERE USER_ID=? AND USERNAME=? AND QUERY_GROUP_ID=?";
			}

			String sqlTestUserReport = "UPDATE ART_USER_QUERIES SET USER_ID=? WHERE USER_ID=? AND USERNAME=? AND QUERY_ID=?";
			String sqlTestUserReportGroup = "UPDATE ART_USER_QUERY_GROUPS SET USER_ID=? WHERE USER_ID=? AND USERNAME=? AND QUERY_GROUP_ID=?";
			int affectedRows;
			boolean updateRight;

			for (String user : users) {
				Integer userId = Integer.valueOf(StringUtils.substringBefore(user, "-"));
				//username won't be needed once user id columns completely replace username in foreign keys
				String username = StringUtils.substringAfter(user, "-");

				//update report privileges
				if (reports != null) {
					for (Integer reportId : reports) {
						//if you use a batch update, some drivers e.g. oracle will
						//stop after the first error. we should continue in the event of an integrity constraint error (access already granted)

						updateRight = true;
						if (grant) {
							//test if right exists. to avoid integrity constraint error
							affectedRows = dbService.update(sqlTestUserReport, userId, userId, username, reportId);
							if (affectedRows > 0) {
								//right exists. don't attempt a reinsert.
								updateRight = false;
							}
						}
						if (updateRight) {
							dbService.update(sqlUserReport, userId, username, reportId);
						}
					}
				}

				//update report group privileges
				if (reportGroups != null) {
					for (Integer reportGroupId : reportGroups) {
						updateRight = true;
						if (grant) {
							//test if right exists. to avoid integrity constraint error
							affectedRows = dbService.update(sqlTestUserReportGroup, userId, userId, username, reportGroupId);
							if (affectedRows > 0) {
								//right exists. don't attempt a reinsert.
								updateRight = false;
							}
						}
						if (updateRight) {
							dbService.update(sqlUserReportGroup, userId, username, reportGroupId);
						}
					}
				}

			}
		}

		//update user group rights
		if (userGroups != null) {
			String sqlUserGroupReport;
			String sqlUserGroupReportGroup;

			if (grant) {
				sqlUserGroupReport = "INSERT INTO ART_USER_GROUP_QUERIES (USER_GROUP_ID, QUERY_ID) VALUES (?, ?)";
				sqlUserGroupReportGroup = "INSERT INTO ART_USER_GROUP_GROUPS (USER_GROUP_ID, QUERY_GROUP_ID) VALUES (?, ?)";
			} else {
				sqlUserGroupReport = "DELETE FROM ART_USER_GROUP_QUERIES WHERE USER_GROUP_ID=? AND QUERY_ID=?";
				sqlUserGroupReportGroup = "DELETE FROM ART_USER_GROUP_GROUPS WHERE USER_GROUP_ID=? AND QUERY_GROUP_ID=?";
			}

			String sqlTestUserGroupReport = "UPDATE ART_USER_GROUP_QUERIES SET USER_GROUP_ID=? WHERE USER_GROUP_ID=? AND QUERY_ID=?";
			String sqlTestUserGroupReportGroup = "UPDATE ART_USER_GROUP_GROUPS SET USER_GROUP_ID=? WHERE USER_GROUP_ID=? AND QUERY_GROUP_ID=?";
			int affectedRows;
			boolean updateRight;

			for (Integer userGroupId : userGroups) {
				//update report privileges
				if (reports != null) {
					for (Integer reportId : reports) {
						//if you use a batch update, some drivers e.g. oracle will
						//stop after the first error. we should continue in the event of an integrity constraint error (access already granted)

						updateRight = true;
						if (grant) {
							//test if right exists. to avoid integrity constraint error
							affectedRows = dbService.update(sqlTestUserGroupReport, userGroupId, userGroupId, reportId);
							if (affectedRows > 0) {
								//right exists. don't attempt a reinsert.
								updateRight = false;
							}
						}
						if (updateRight) {
							dbService.update(sqlUserGroupReport, userGroupId, reportId);
						}
					}
				}

				//update report group privileges
				if (reportGroups != null) {
					for (Integer reportGroupId : reportGroups) {
						updateRight = true;
						if (grant) {
							//test if right exists. to avoid integrity constraint error
							affectedRows = dbService.update(sqlTestUserGroupReportGroup, userGroupId, userGroupId, reportGroupId);
							if (affectedRows > 0) {
								//right exists. don't attempt a reinsert.
								updateRight = false;
							}
						}
						if (updateRight) {
							dbService.update(sqlUserGroupReportGroup, userGroupId, reportGroupId);
						}
					}
				}

			}
		}
	}

}
