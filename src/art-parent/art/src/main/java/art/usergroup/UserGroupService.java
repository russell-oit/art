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
package art.usergroup;

import art.servlets.ArtConfig;
import art.utils.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to user groups
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupService.class);
	final String SQL_SELECT_ALL_USER_GROUPS = "SELECT USER_GROUP_ID, NAME, DESCRIPTION,"
			+ " DEFAULT_QUERY_GROUP, START_QUERY, CREATION_DATE, UPDATE_DATE "
			+ " FROM ART_USER_GROUPS";

	/**
	 * Get all user groups
	 *
	 * @return list of all user groups, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public List<UserGroup> getAllUserGroups() throws SQLException {
		List<UserGroup> groups = new ArrayList<UserGroup>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_SELECT_ALL_USER_GROUPS;

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.executeQuery(conn, ps, sql);
			while (rs.next()) {
				UserGroup group = new UserGroup();
				populateUserGroup(group, rs);
				groups.add(group);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return groups;
	}

	/**
	 * Get a user object
	 *
	 * @param id
	 * @return user object if user found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("userGroups")
	public UserGroup getUserGroup(int id) throws SQLException {
		UserGroup group = null;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_SELECT_ALL_USER_GROUPS + " WHERE USER_GROUP_ID = ? ";

		Object[] values = {
			id
		};

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.executeQuery(conn, ps, sql, values);
			if (rs.next()) {
				group = new UserGroup();
				populateUserGroup(group, rs);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return group;
	}

	/**
	 * Populate object with row from table
	 *
	 * @param group
	 * @param rs
	 * @throws SQLException
	 */
	private void populateUserGroup(UserGroup group, ResultSet rs) throws SQLException {
		group.setUserGroupId(rs.getInt("USER_GROUP_ID"));
		group.setName(rs.getString("NAME"));
		group.setDescription(rs.getString("DESCRIPTION"));
		group.setDefaultReportGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
		group.setStartReport(rs.getString("START_QUERY"));
		group.setCreationDate(rs.getTimestamp("CREATION_DATE"));
		group.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
	}

	/**
	 * Delete a user group
	 *
	 * @param id
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void deleteUserGroup(int id) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();

			String sql = "DELETE FROM ART_USER_GROUPS WHERE USER_GROUP_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			int affectedRows = ps.executeUpdate();
			if (affectedRows == 0) {
				logger.warn("Delete user group failed. Group not found. User Group Id={}", id);
			}

		} finally {
			DbUtils.close(rs, ps, conn);
		}
	}

	/**
	 * Add a new user group to the database
	 *
	 * @param group
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void addUserGroup(UserGroup group) throws SQLException {
		int newId = allocateNewId();
		if (newId > 0) {
			group.setUserGroupId(newId);
			saveUserGroup(group, true);
		} else {
			logger.warn("User group not added. Allocate new ID failed. User Group='{}'", group.getName());
		}
	}

	/**
	 * Update an existing user group
	 *
	 * @param group
	 * @throws SQLException
	 */
	@CacheEvict(value = "userGroups", allEntries = true)
	public void updateUserGroup(UserGroup group) throws SQLException {
		saveUserGroup(group, false);
	}

	/**
	 * Save a user group
	 *
	 * @param group
	 * @param newRecord true if this is a new record, false otherwise
	 * @throws SQLException
	 */
	private void saveUserGroup(UserGroup group, boolean newRecord) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;

		String dateColumn;

		if (newRecord) {
			dateColumn = "CREATION_DATE";
		} else {
			dateColumn = "UPDATE_DATE";
		}

		String sqlUpdate = "UPDATE ART_USER_GROUPS SET NAME=?, DESCRIPTION=?,"
				+ " DEFAULT_QUERY_GROUP=?, START_QUERY=?";

		String sql = sqlUpdate + "," + dateColumn + "=?"
				+ " WHERE USER_GROUP_ID=?";

		Object[] values = {
			group.getName(),
			group.getDescription(),
			group.getDefaultReportGroup(),
			group.getStartReport(),
			DbUtils.getCurrentTimeStamp(),
			group.getUserGroupId()
		};

		try {
			conn = ArtConfig.getConnection();

			int affectedRows = DbUtils.executeUpdate(conn, ps, sql, values);
			if (affectedRows == 0) {
				logger.warn("Save user group - no rows affected. User Group='{}', newRecord={}", group.getName(), newRecord);
			}
		} finally {
			DbUtils.close(ps, conn);
		}
	}

	/**
	 * Generate an id and record for a new item
	 *
	 * @return new id generated, 0 otherwise
	 * @throws SQLException
	 */
	private synchronized int allocateNewId() throws SQLException {
		int newId = 0;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement psInsert = null;

		try {
			conn = ArtConfig.getConnection();
			//generate new id
			String sql = "SELECT MAX(USER_GROUP_ID) FROM ART_USER_GROUPS";
			rs = DbUtils.executeQuery(conn, ps, sql);
			if (rs.next()) {
				newId = rs.getInt(1) + 1;

				//add dummy record with new id. fill all not null columns
				//name has unique constraint so use a random default value
				String allocatingName = "allocating-" + RandomStringUtils.randomAlphanumeric(3);
				sql = "INSERT INTO ART_USER_GROUPS(USER_GROUP_ID,NAME)"
						+ " VALUES(?,?)";

				Object[] values = {
					newId,
					allocatingName
				};

				int affectedRows = DbUtils.executeUpdate(conn, psInsert, sql, values);
				if (affectedRows == 0) {
					logger.warn("allocateNewId - no rows affected. id={}", newId);
				}
			} else {
				logger.warn("Could not get max id");
			}
		} finally {
			DbUtils.close(psInsert);
			DbUtils.close(rs, ps, conn);
		}

		return newId;
	}

}
