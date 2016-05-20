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
package art.adminright;

import art.datasource.Datasource;
import art.dbutils.DbService;
import art.reportgroup.ReportGroup;
import art.user.User;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding and deleting admin rights
 *
 * @author Timothy Anyona
 */
@Service
public class AdminRightService {

	private static final Logger logger = LoggerFactory.getLogger(AdminRightService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL_DATASOURCE_RIGHTS
			= "SELECT AU.USER_ID, AU.USERNAME, AD.DATABASE_ID, AD.NAME AS DATASOURCE_NAME"
			+ " FROM ART_ADMIN_PRIVILEGES AAP"
			+ " INNER JOIN ART_USERS AU ON"
			+ " AAP.USER_ID=AU.USER_ID"
			+ " INNER JOIN ART_DATABASES AD ON"
			+ " AAP.VALUE_ID=AD.DATABASE_ID"
			+ " WHERE AAP.PRIVILEGE='DB'";

	private final String SQL_SELECT_ALL_REPORT_GROUP_RIGHTS
			= "SELECT AU.USER_ID, AU.USERNAME, AQG.QUERY_GROUP_ID, AQG.NAME AS GROUP_NAME"
			+ " FROM ART_ADMIN_PRIVILEGES AAP"
			+ " INNER JOIN ART_USERS AU ON"
			+ " AAP.USER_ID=AU.USER_ID"
			+ " INNER JOIN ART_QUERY_GROUPS AQG ON"
			+ " AAP.VALUE_ID=AQG.QUERY_GROUP_ID"
			+ " WHERE AAP.PRIVILEGE='GRP'";

	/**
	 * Maps a resultset to an object
	 */
	private class AdminDatasourceRightMapper extends BasicRowProcessor {

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
			AdminDatasourceRight right = new AdminDatasourceRight();

			User user = new User();
			user.setUserId(rs.getInt("USER_ID"));
			user.setUsername(rs.getString("USERNAME"));

			right.setAdmin(user);

			Datasource datasource = new Datasource();
			datasource.setDatasourceId(rs.getInt("DATABASE_ID"));
			datasource.setName(rs.getString("DATASOURCE_NAME"));

			right.setDatasource(datasource);

			return type.cast(right);
		}
	}

	/**
	 * Maps a resultset to an object
	 */
	private class AdminReportGroupRightMapper extends BasicRowProcessor {

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
			AdminReportGroupRight right = new AdminReportGroupRight();

			User user = new User();
			user.setUserId(rs.getInt("USER_ID"));
			user.setUsername(rs.getString("USERNAME"));

			right.setAdmin(user);

			ReportGroup group = new ReportGroup();
			group.setReportGroupId(rs.getInt("QUERY_GROUP_ID"));
			group.setName(rs.getString("GROUP_NAME"));

			right.setReportGroup(group);

			return type.cast(right);
		}
	}

	/**
	 * Returns all admin-datasource rights
	 *
	 * @return all admin-datasource rights
	 * @throws SQLException
	 */
	public List<AdminDatasourceRight> getAllAdminDatasourceRights() throws SQLException {
		logger.debug("Entering getAllAdminDatasourceRights");

		ResultSetHandler<List<AdminDatasourceRight>> h = new BeanListHandler<>(AdminDatasourceRight.class, new AdminDatasourceRightMapper());
		return dbService.query(SQL_SELECT_ALL_DATASOURCE_RIGHTS, h);
	}

	/**
	 * Returns all admin-report group rights
	 *
	 * @return all admin-report group rights
	 * @throws SQLException
	 */
	public List<AdminReportGroupRight> getAllAdminReportGroupRights() throws SQLException {
		logger.debug("Entering getAllAdminReportGroupRights");

		ResultSetHandler<List<AdminReportGroupRight>> h = new BeanListHandler<>(AdminReportGroupRight.class, new AdminReportGroupRightMapper());
		return dbService.query(SQL_SELECT_ALL_REPORT_GROUP_RIGHTS, h);
	}

	/**
	 * Deletes an admin-datasource right
	 *
	 * @param userId the user id for the right
	 * @param datasourceId the datasource id for the right
	 * @throws SQLException
	 */
	public void deleteAdminDatasourceRight(int userId, int datasourceId) throws SQLException {
		logger.debug("Entering deleteAdminDatasourceRight: userId={}, datasourceId={}",
				userId, datasourceId);

		String sql;

		sql = "DELETE FROM ART_ADMIN_PRIVILEGES WHERE PRIVILEGE='DB'"
				+ " AND USER_ID=? AND VALUE_ID=?";
		dbService.update(sql, userId, datasourceId);
	}

	/**
	 * Deletes an admin-report group right
	 *
	 * @param userId the user id for the right
	 * @param reportGroupId the report group id for the right
	 * @throws SQLException
	 */
	public void deleteAdminReportGroupRight(int userId, int reportGroupId) throws SQLException {
		logger.debug("Entering deleteAdminReportGroupRight: userId={}, reportGroupId={}",
				userId, reportGroupId);

		String sql;

		sql = "DELETE FROM ART_ADMIN_PRIVILEGES WHERE PRIVILEGE='GRP'"
				+ " AND USER_ID=? AND VALUE_ID=?";
		dbService.update(sql, userId, reportGroupId);
	}

	/**
	 * Grants or revokes admin rights
	 *
	 * @param action "grant" or "revoke". anything else will be treated as
	 * revoke
	 * @param admins the relevant user identifiers in the format user
	 * id-username
	 * @param datasources the relevant datasource ids
	 * @param reportGroups the relevant report group ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"datasources", "reportGroups"}, allEntries = true) //clear caches so that admins can work with new values
	public void updateAdminRights(String action, String[] admins, Integer[] datasources,
			Integer[] reportGroups) throws SQLException {

		logger.debug("Entering updateAdminRights: action='{}'", action);

		logger.debug("(admins == null) = {}", admins == null);
		if (admins == null) {
			logger.warn("Update not performed. admins is null");
			return;
		}

		boolean grant;
		if (StringUtils.equalsIgnoreCase(action, "grant")) {
			grant = true;
		} else {
			grant = false;
		}

		String sqlReportGroup;
		String sqlDatasource;

		if (grant) {
			sqlDatasource = "INSERT INTO ART_ADMIN_PRIVILEGES (USER_ID, USERNAME, PRIVILEGE, VALUE_ID) values (? , ?, 'DB', ? ) ";
			sqlReportGroup = "INSERT INTO ART_ADMIN_PRIVILEGES (USER_ID, USERNAME, PRIVILEGE, VALUE_ID) values (? , ?, 'GRP', ? ) ";
		} else {
			sqlDatasource = "DELETE FROM ART_ADMIN_PRIVILEGES WHERE PRIVILEGE = 'DB' AND USER_ID = ? AND USERNAME=? AND VALUE_ID = ? ";
			sqlReportGroup = "DELETE FROM ART_ADMIN_PRIVILEGES WHERE PRIVILEGE = 'GRP' AND USER_ID = ? AND USERNAME=? AND VALUE_ID = ? ";
		}

		String sqlTestDatasource = "UPDATE ART_ADMIN_PRIVILEGES SET USER_ID=? WHERE PRIVILEGE='DB' AND USER_ID = ? AND USERNAME=? AND VALUE_ID = ?";
		String sqlTestReportGroup = "UPDATE ART_ADMIN_PRIVILEGES SET USER_ID=? WHERE PRIVILEGE='GRP' AND USER_ID = ? AND USERNAME=? AND VALUE_ID = ?";
		int affectedRows;
		boolean updateRight;

		for (String admin : admins) {
			Integer userId = Integer.valueOf(StringUtils.substringBefore(admin, "-"));
			//username won't be needed once user id columns completely replace username in foreign keys
			String username = StringUtils.substringAfter(admin, "-");

			//update datasource rights
			if (datasources != null) {
				for (Integer datasourceId : datasources) {
					//if you use a batch update, some drivers e.g. oracle will
					//stop after the first error. we should continue in the event of an integrity constraint error (access already granted)

					updateRight = true;
					if (grant) {
						//test if right exists. to avoid integrity constraint error
						affectedRows = dbService.update(sqlTestDatasource, userId, userId, username, datasourceId);
						if (affectedRows > 0) {
							//right exists. don't attempt a reinsert.
							updateRight = false;
						}
					}
					if (updateRight) {
						dbService.update(sqlDatasource, userId, username, datasourceId);
					}
				}
			}

			//update report group rights
			if (reportGroups != null) {
				for (Integer reportGroupId : reportGroups) {
					updateRight = true;
					if (grant) {
						//test if right exists. to avoid integrity constraint error
						affectedRows = dbService.update(sqlTestReportGroup, userId, userId, username, reportGroupId);
						if (affectedRows > 0) {
							//right exists. don't attempt a reinsert.
							updateRight = false;
						}
					}
					if (updateRight) {
						dbService.update(sqlReportGroup, userId, username, reportGroupId);
					}
				}
			}
		}
	}

}
