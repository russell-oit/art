/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This class is stored in the user Session and provide entities associated with
 * the user (groups, queries. jobs etc) It also includes some general purpose
 * methods
 */
package art.utils;

import art.servlets.ArtConfig;
import java.io.Serializable;
import java.sql.*;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent a user and some general purpose methods
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class UserEntity implements Serializable {

	private static final long serialVersionUID = 1L; //implement serializable to enable ue object in session to be serialized
	final static Logger logger = LoggerFactory.getLogger(UserEntity.class);
	String username = "";
	String email = "";
	boolean internalAuth;
	int accessLevel;
	java.util.Date loginDate;
	String fullName = "";
	String activeStatus;
	int defaultQueryGroup = -1;
	String canChangePasswordString;
	boolean canChangePassword = true;
	String password;
	String hashingAlgorithm = "bcrypt";
	private String startQuery;

	/**
	 *
	 */
	public UserEntity() {
		loginDate = new java.util.Date();
	}

	/**
	 * @return the startQuery
	 */
	public String getStartQuery() {
		if (startQuery == null) {
			startQuery = ""; //set to empty string for display in editUser.jsp
		}
		return startQuery;
	}

	/**
	 * @param startQuery the startQuery to set
	 */
	public void setStartQuery(String startQuery) {
		this.startQuery = startQuery;
	}

	/**
	 *
	 * @param user
	 */
	public UserEntity(String user) {
		loginDate = new java.util.Date();
		privateLoad(user);
	}

	/**
	 * Set algorithm used to hash the password
	 *
	 * @param value algorithm used to hash the password. One of bcrypt, MD5,
	 * SHA-1
	 */
	public void setHashingAlgorithm(String value) {
		hashingAlgorithm = value;
	}

	/**
	 * Get algorithm used to hash the password
	 *
	 * @return algorithm used to hash the password
	 */
	public String getHashingAlgorithm() {
		return hashingAlgorithm;
	}

	/**
	 *
	 * @param value
	 */
	public void setPassword(String value) {
		password = value;
	}

	/**
	 *
	 * @return user's password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 *
	 * @param value
	 */
	public void setCanChangePasswordString(String value) {
		canChangePasswordString = value;
	}

	/**
	 *
	 * @return whether the user can change their password
	 */
	public String getCanChangePasswordString() {
		return canChangePasswordString;
	}

	/**
	 *
	 * @return default query group
	 */
	public int getDefaultQueryGroup() {
		return defaultQueryGroup;
	}

	/**
	 *
	 * @param value
	 */
	public void setDefaultQueryGroup(int value) {
		defaultQueryGroup = value;
	}

	/**
	 *
	 * @return <code>true</code> if user can change their password
	 */
	public boolean isCanChangePassword() {
		if (canChangePasswordString == null || canChangePasswordString.equals("Y")) {
			canChangePassword = true;
		} else {
			canChangePassword = false;
		}

		return canChangePassword;
	}

	/**
	 *
	 * @param value
	 */
	public void setCanChangePassword(boolean value) {
		canChangePassword = value;
		if (canChangePassword) {
			canChangePasswordString = "Y";
		} else {
			canChangePasswordString = "N";
		}
	}

	/**
	 *
	 * @return full name
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 *
	 * @param value
	 */
	public void setFullName(String value) {
		fullName = value;
	}

	/**
	 *
	 * @return active status
	 */
	public String getActiveStatus() {
		return activeStatus;
	}

	/**
	 *
	 * @param value
	 */
	public void setActiveStatus(String value) {
		activeStatus = value;
	}

	/**
	 *
	 * @param s
	 */
	public void setUsername(String s) {
		username = s;
	}

	/**
	 *
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 *
	 * @param s
	 */
	public void setEmail(String s) {
		email = s;
	}

	/**
	 *
	 * @return user's email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 *
	 * @param b
	 */
	public void setInternalAuth(boolean b) {
		internalAuth = b;
	}

	/**
	 *
	 * @return <code>true</code> if internal authentication used
	 */
	public boolean isInternalAuth() {
		return internalAuth;
	}

	/**
	 *
	 * @param i
	 */
	public void setAccessLevel(int i) {
		accessLevel = i;
	}

	/**
	 *
	 * @return access level
	 */
	public int getAccessLevel() {
		return accessLevel;
	}

	/**
	 *
	 * @return login date
	 */
	public java.util.Date getLoginDate() {
		return loginDate;
	}

	/**
	 * Populate user object
	 *
	 * @param user
	 */
	public void load(String user) {
		username = user;

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();

			String SqlQuery = "SELECT EMAIL,ACCESS_LEVEL,FULL_NAME,ACTIVE_STATUS, PASSWORD "
					+ " ,DEFAULT_QUERY_GROUP,CAN_CHANGE_PASSWORD, HASHING_ALGORITHM, START_QUERY "
					+ " FROM ART_USERS "
					+ " WHERE USERNAME = ? ";

			PreparedStatement ps = conn.prepareStatement(SqlQuery);
			ps.setString(1, user);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				setAccessLevel(rs.getInt("ACCESS_LEVEL"));
				setEmail(rs.getString("EMAIL"));
				setFullName(rs.getString("FULL_NAME"));
				setActiveStatus(rs.getString("ACTIVE_STATUS"));
				setDefaultQueryGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
				canChangePasswordString = rs.getString("CAN_CHANGE_PASSWORD");
				password = rs.getString("PASSWORD");
				hashingAlgorithm = rs.getString("HASHING_ALGORITHM");
				startQuery = rs.getString("START_QUERY");
			}
			rs.close();
			ps.close();

			if (defaultQueryGroup <= 0) {
				//no default query group at user level. Use default for first user group, if exists
				SqlQuery = "SELECT AUG.DEFAULT_QUERY_GROUP "
						+ " FROM ART_USER_GROUP_ASSIGNMENT AUGA, ART_USER_GROUPS AUG "
						+ " WHERE AUGA.USER_GROUP_ID=AUG.USER_GROUP_ID "
						+ " AND AUGA.USERNAME=? "
						+ " ORDER BY AUG.NAME";

				ps = conn.prepareStatement(SqlQuery);
				ps.setString(1, user);
				rs = ps.executeQuery();
				while (rs.next()) {
					setDefaultQueryGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
					if (defaultQueryGroup > 0) {
						//first default found. use this
						break;
					}
				}
				rs.close();
				ps.close();
			}

			if (StringUtils.isBlank(startQuery)) {
				//no start query at user level. Use start query for first user group, if exists
				SqlQuery = "SELECT AUG.START_QUERY "
						+ " FROM ART_USER_GROUP_ASSIGNMENT AUGA, ART_USER_GROUPS AUG "
						+ " WHERE AUGA.USER_GROUP_ID=AUG.USER_GROUP_ID "
						+ " AND AUGA.USERNAME=? "
						+ " ORDER BY AUG.NAME";

				ps = conn.prepareStatement(SqlQuery);
				ps.setString(1, user);
				rs = ps.executeQuery();
				while (rs.next()) {
					startQuery = rs.getString("START_QUERY");
					if (StringUtils.isNotBlank(startQuery)) {
						//first start query found. use this
						break;
					}
				}
				rs.close();
				ps.close();
			}
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}
	}

	//private method for use by constructor
	private void privateLoad(String user) {
		username = user;
		load(user);
	}

	/**
	 * Allow reloading of properties for the current username
	 */
	public void reload() {
		load(username);
	}

	/**
	 * Insert new user
	 *
	 * @return <code>true</code> if successful
	 */
	public boolean insert() {
		boolean success = false;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			java.util.Date now = new java.util.Date();
			java.sql.Date sysdate = new java.sql.Date(now.getTime());

			String sql = "INSERT INTO ART_USERS"
					+ " (USERNAME,PASSWORD,EMAIL,FULL_NAME,ACTIVE_STATUS,ACCESS_LEVEL "
					+ " ,UPDATE_DATE, DEFAULT_QUERY_GROUP, CAN_CHANGE_PASSWORD"
					+ " , HASHING_ALGORITHM, START_QUERY) "
					+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);
			ps.setString(3, email);
			ps.setString(4, fullName);
			ps.setString(5, activeStatus);
			ps.setInt(6, accessLevel);
			ps.setDate(7, sysdate);
			ps.setInt(8, defaultQueryGroup);
			ps.setString(9, canChangePasswordString);
			ps.setString(10, hashingAlgorithm);
			ps.setString(11, startQuery);

			ps.executeUpdate();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Update user
	 *
	 * @return <code>true</code> if successful
	 */
	public boolean update() {
		boolean success = false;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;

			java.util.Date now = new java.util.Date();
			java.sql.Date sysdate = new java.sql.Date(now.getTime());

			if (password.equals("")) {
				sql = "UPDATE ART_USERS SET EMAIL = ? , FULL_NAME = ? , ACTIVE_STATUS = ?, ACCESS_LEVEL = ? , UPDATE_DATE = ? "
						+ ",DEFAULT_QUERY_GROUP = ?, CAN_CHANGE_PASSWORD = ?, START_QUERY=? "
						+ " WHERE USERNAME = ?";

				ps = conn.prepareStatement(sql);

				ps.setString(1, email);
				ps.setString(2, fullName);
				ps.setString(3, activeStatus);
				ps.setInt(4, accessLevel);
				ps.setDate(5, sysdate);
				ps.setInt(6, defaultQueryGroup);
				ps.setString(7, canChangePasswordString);
				ps.setString(8, startQuery);
				ps.setString(9, username);
			} else {
				sql = "UPDATE ART_USERS SET PASSWORD = ?, EMAIL = ? , FULL_NAME = ?"
						+ " ,ACTIVE_STATUS = ?, ACCESS_LEVEL = ? , UPDATE_DATE = ? "
						+ " ,DEFAULT_QUERY_GROUP = ?, CAN_CHANGE_PASSWORD = ?, HASHING_ALGORITHM=?, START_QUERY=? "
						+ " WHERE USERNAME = ? ";

				ps = conn.prepareStatement(sql);

				ps.setString(1, password);
				ps.setString(2, email);
				ps.setString(3, fullName);
				ps.setString(4, activeStatus);
				ps.setInt(5, accessLevel);
				ps.setDate(6, sysdate);
				ps.setInt(7, defaultQueryGroup);
				ps.setString(8, canChangePasswordString);
				ps.setString(9, hashingAlgorithm);
				ps.setString(10, startQuery);
				ps.setString(11, username);
			}

			ps.executeUpdate();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Delete a user
	 *
	 * @param uName
	 * @return <code>true</code> if successful
	 */
	public boolean delete(String uName) {
		boolean success = false;

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			PreparedStatement ps;
			ResultSet rs;

			//delete user-query relationships
			sql = "DELETE FROM ART_USER_QUERIES WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, uName);
			ps.executeUpdate();

			//delete user-query group relationships
			sql = "DELETE FROM ART_USER_QUERY_GROUPS WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, uName);
			ps.executeUpdate();

			//delete user-rules relationships
			sql = "DELETE FROM ART_USER_RULES WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, uName);
			ps.executeUpdate();

			//delete user-user group relationships
			sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, uName);
			ps.executeUpdate();

			//delete user-shared job relationships
			sql = "DELETE FROM ART_USER_JOBS WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, uName);
			ps.executeUpdate();

			//delete user's jobs. this will delete all records related to the job e.g. quartz records, job parameters etc
			sql = "SELECT JOB_ID FROM ART_JOBS WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, uName);
			rs = ps.executeQuery();
			while (rs.next()) {
				ArtJob aj = new ArtJob();
				aj.load(rs.getInt("JOB_ID"), uName);
				aj.delete();
			}
			rs.close();


			//lastly, delete user
			sql = "DELETE FROM ART_USERS WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, uName);
			ps.executeUpdate();

			ps.close();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Add or remove user group membership
	 *
	 * @param action
	 * @param userGroups
	 */
	public void updateUserGroupMembership(String action, String[] userGroups) {

		if (action == null || userGroups == null) {
			return;
		}

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			PreparedStatement ps;

			if (action.equals("ADD")) {
				sql = "INSERT INTO ART_USER_GROUP_ASSIGNMENT (USERNAME, USER_GROUP_ID) values (? ,?)";
			} else {
				sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USERNAME = ? AND USER_GROUP_ID = ?";
			}
			ps = conn.prepareStatement(sql);

			//allow for deleting of split jobs where access was granted via group membership
			String sqlSplitJobs = "DELETE FROM ART_USER_JOBS WHERE USERNAME = ? AND USER_GROUP_ID = ?";
			PreparedStatement psSplitJobs = conn.prepareStatement(sqlSplitJobs);

			for (int i = 0; i < userGroups.length; i++) {
				ps.setString(1, username);
				ps.setInt(2, Integer.parseInt(userGroups[i]));
				try {
					ps.executeUpdate();
				} catch (SQLIntegrityConstraintViolationException e) {
					//logger.info("User {} already a member of User Group ID {}", username, userGroups[i]);
				} catch (SQLException e) {
					logger.error("Error updating user group membership. User={}, User Group ID={}", new Object[]{username, userGroups[i], e});
				}

				//delete split jobs where access was granted via group membership
				if (action.equals("REMOVE")) {
					try {
						psSplitJobs.setString(1, username);
						psSplitJobs.setInt(2, Integer.parseInt(userGroups[i]));
						psSplitJobs.executeUpdate();
					} catch (SQLException e) {
						logger.error("Error while removing split jobs. " + e);
					}
				}
			}
			ps.close();
			psSplitJobs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}
	}

	/**
	 * Get all query groups the user has access to
	 *
	 * @return all query groups the user has access to
	 */
	public Map<String, Integer> getQueryGroups() {
		return getQueryGroup(-1);
	}

	/**
	 * get groups that user has explicity rights to see
	 *
	 * @param groupId
	 * @return all query groups that user has explicity rights to see
	 */
	public Map<String, Integer> getQueryGroup(int groupId) {
		// if groupId = -1 returns all groups the user has access to

		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.TERTIARY); //order by case
		TreeMap<String, Integer> map = new TreeMap<String, Integer>(stringCollator);

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();

			PreparedStatement ps;
			ResultSet rs;

			String SqlQuery = "SELECT aqg.QUERY_GROUP_ID , aqg.NAME "
					+ " FROM ART_USER_QUERY_GROUPS auqg , ART_QUERY_GROUPS aqg "
					+ " WHERE auqg.USERNAME = ? "
					+ " AND auqg.QUERY_GROUP_ID = aqg.QUERY_GROUP_ID "
					+ (groupId == -1 ? "" : ("AND aqg.QUERY_GROUP_ID =" + groupId))
					+ " ORDER BY aqg.NAME ";

			ps = conn.prepareStatement(SqlQuery);
			ps.setString(1, username);

			rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getString("NAME"), new Integer(rs.getInt("QUERY_GROUP_ID")));
			}
			rs.close();
			ps.close();

			//add query groups to which the user has access through his user group membership
			SqlQuery = "SELECT DISTINCT AQG.QUERY_GROUP_ID, AQG.NAME "
					+ " FROM ART_USER_GROUP_GROUPS AUGG, ART_QUERY_GROUPS AQG "
					+ " WHERE AUGG.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
					+ (groupId == -1 ? "" : ("AND AQG.QUERY_GROUP_ID =" + groupId))
					+ " AND EXISTS (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA "
					+ " WHERE AUGA.USERNAME = ? AND AUGA.USER_GROUP_ID = AUGG.USER_GROUP_ID)"; //no need for order by. treemap automatically orders items when they are retrieved

			ps = conn.prepareStatement(SqlQuery); //use preparedstatement so that possible ' in username is properly handled
			ps.setString(1, username);
			rs = ps.executeQuery();

			//add groups to the map. treemap doesn't retain duplicate keys so there will be no duplicates in the final result
			while (rs.next()) {
				map.put(rs.getString("NAME"), new Integer(rs.getInt("QUERY_GROUP_ID")));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}

	/**
	 * Get all query groups the user should be able to see. may have more items
	 * that he has direct access to
	 *
	 * @return query groups that should be displayed on the start page
	 */
	public Map<String, Integer> getAvailableQueryGroups() {
		return getAvailableQueryGroup(-1);
	}

	/**
	 *
	 * @param groupId
	 * @return query groups that should be displayed on the start page
	 */
	public Map<String, Integer> getAvailableQueryGroup(int groupId) {
		Map<String, Integer> map;

		//get groups that user has explicity rights to see
		map = getQueryGroup(groupId);

		//add groups where user has right to query but not to group
		Connection conn = null;
		try {
			conn = ArtConfig.getConnection();

			PreparedStatement ps;
			ResultSet rs;

			String SqlQuery = "SELECT DISTINCT AQG.QUERY_GROUP_ID, AQG.NAME, AQ.QUERY_TYPE "
					+ " FROM ART_USER_QUERIES AUQ, ART_QUERIES AQ, ART_QUERY_GROUPS AQG "
					+ " WHERE AUQ.QUERY_ID=AQ.QUERY_ID AND AQ.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
					+ " AND AUQ.USERNAME=? AND AQG.QUERY_GROUP_ID<>0"
					+ " AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120";

			ps = conn.prepareStatement(SqlQuery);
			ps.setString(1, username);

			rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getString("NAME"), Integer.valueOf(rs.getInt("QUERY_GROUP_ID")));
			}
			rs.close();
			ps.close();

			//add groups where user's group has rights to the query
			SqlQuery = "SELECT DISTINCT AQG.QUERY_GROUP_ID, AQG.NAME "
					+ " FROM ART_USER_GROUP_QUERIES AUGQ, ART_QUERIES AQ, ART_QUERY_GROUPS AQG "
					+ " WHERE AUGQ.QUERY_ID=AQ.QUERY_ID AND AQ.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
					+ " AND AQG.QUERY_GROUP_ID<>0 AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120 "
					+ " AND EXISTS (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA "
					+ " WHERE AUGA.USERNAME = ? AND AUGA.USER_GROUP_ID = AUGQ.USER_GROUP_ID)";

			ps = conn.prepareStatement(SqlQuery);
			ps.setString(1, username);

			rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getString("NAME"), new Integer(rs.getInt("QUERY_GROUP_ID")));
			}
			rs.close();
			ps.close();

		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}


		return map;
	}

	/**
	 * Returns the queries the user can see in the given group
	 *
	 * @param groupId
	 * @return the queries the user can see in the given group
	 */
	public Map<String, Integer> getAvailableQueries(int groupId) {
		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.TERTIARY); //order by case
		TreeMap<String, Integer> map = new TreeMap<String, Integer>(stringCollator);

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();
			String sqlQuery;
			PreparedStatement ps;
			ResultSet rs;

			// User can run queries directly granted to him. don't show static lov, dynamic lov or dynamic job recipient queries
			sqlQuery = "SELECT AQ.QUERY_ID, AQ.NAME "
					+ " FROM ART_USER_QUERIES AUQ, ART_QUERIES AQ "
					+ " WHERE AUQ.QUERY_ID=AQ.QUERY_ID "
					+ " AND AUQ.USERNAME=? AND AQ.QUERY_GROUP_ID=? "
					+ " AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120 AND AQ.QUERY_TYPE<>121 "
					+ " AND AQ.ACTIVE_STATUS = 'A' "; // show only active queries


			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, username);
			ps.setInt(2, groupId);
			rs = ps.executeQuery();

			while (rs.next()) {
				map.put(rs.getString("NAME"), Integer.valueOf(rs.getInt("QUERY_ID")));
			}
			rs.close();
			ps.close();

			//add queries to which user has access through user group membership
			sqlQuery = "SELECT DISTINCT AQ.QUERY_ID, AQ.NAME "
					+ " FROM ART_USER_GROUP_QUERIES AUGQ, ART_QUERIES AQ "
					+ " WHERE AUGQ.QUERY_ID = AQ.QUERY_ID "
					+ " AND AQ.ACTIVE_STATUS = 'A' AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120 AND AQ.QUERY_TYPE<>121 "
					+ " AND AQ.QUERY_GROUP_ID = ? AND EXISTS "
					+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ?"
					+ " AND AUGA.USER_GROUP_ID=AUGQ.USER_GROUP_ID)";

			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, groupId);
			ps.setString(2, username);

			rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getString("NAME"), new Integer(rs.getInt("QUERY_ID")));
			}
			rs.close();
			ps.close();

			// user can run all queries in the query groups he has access to
			sqlQuery = "SELECT AQ.QUERY_ID, AQ.NAME "
					+ " FROM ART_USER_QUERY_GROUPS AUQG, ART_QUERIES AQ "
					+ " WHERE AUQG.QUERY_GROUP_ID = AQ.QUERY_GROUP_ID "
					+ " AND AUQG.USERNAME=? AND AQ.QUERY_GROUP_ID = ? "
					+ " AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120 AND AQ.QUERY_TYPE<>121 "
					+ " AND AQ.ACTIVE_STATUS = 'A' "; // show only active queries


			ps = conn.prepareStatement(sqlQuery);
			ps.setString(1, username);
			ps.setInt(2, groupId);
			rs = ps.executeQuery();

			while (rs.next()) {
				map.put(rs.getString(2), new Integer(rs.getInt(1)));
			}
			rs.close();
			ps.close();

			//user can run all queries in the query groups his user groups have access to
			sqlQuery = "SELECT DISTINCT AQ.QUERY_ID, AQ.NAME "
					+ " FROM ART_USER_GROUP_GROUPS AUGG, ART_QUERIES AQ "
					+ " WHERE AUGG.QUERY_GROUP_ID = AQ.QUERY_GROUP_ID "
					+ " AND AQ.ACTIVE_STATUS = 'A' AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120 AND AQ.QUERY_TYPE<>121 "
					+ " AND AQ.QUERY_GROUP_ID = ? AND EXISTS "
					+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ?"
					+ " AND AUGA.USER_GROUP_ID=AUGG.USER_GROUP_ID)";

			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, groupId);
			ps.setString(2, username);

			rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getString("NAME"), new Integer(rs.getInt("QUERY_ID")));
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}

	/**
	 * Get the shared jobs the user has access to
	 *
	 * @return the shared jobs the user has access to
	 */
	public Map<Integer, ArtJob> getSharedJobs() {
		Map<Integer, ArtJob> jobs = new TreeMap<Integer, ArtJob>();

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();
			String sql;
			PreparedStatement ps;
			ResultSet rs;

			//get shared jobs user has access to via group membership. non-spit jobs. no entries for them in the art_user_jobs table
			sql = "SELECT aq.NAME AS QUERY_NAME, aj.JOB_NAME, aj.JOB_ID, aj.JOB_TYPE, aj.SUBJECT, aq.USES_RULES, aj.ALLOW_SPLITTING "
					+ " , aj.LAST_START_DATE , aj.LAST_FILE_NAME , aj.NEXT_RUN_DATE, aj.CACHED_TABLE_NAME "
					+ " ,aj.LAST_END_DATE, aj.OUTPUT_FORMAT, aj.MAIL_TOS, aj.SUBJECT, aj.MESSAGE "
					+ " ,aj.JOB_MINUTE, aj.JOB_HOUR, aj.JOB_DAY, aj.JOB_WEEKDAY, aj.JOB_MONTH "
					+ " FROM ART_JOBS aj, ART_QUERIES aq, ART_USER_GROUP_JOBS AUGJ "
					+ " WHERE aq.QUERY_ID = aj.QUERY_ID AND aj.JOB_ID = AUGJ.JOB_ID "
					+ " AND aj.USERNAME <> ? AND EXISTS "
					+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ? "
					+ " AND AUGA.USER_GROUP_ID=AUGJ.USER_GROUP_ID)";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, username);
			rs = ps.executeQuery();

			while (rs.next()) {
				ArtJob aj = new ArtJob();
				aj.setQueryName(rs.getString("QUERY_NAME"));
				aj.setJobName(rs.getString("JOB_NAME"));
				aj.setJobIdOnly(rs.getInt("JOB_ID"));
				aj.setJobType(rs.getInt("JOB_TYPE"));
				aj.setQueryRulesFlag(rs.getString("USES_RULES"));
				aj.setAllowSplitting(rs.getString("ALLOW_SPLITTING"));
				aj.setLastStartDate(rs.getTimestamp("LAST_START_DATE"));
				aj.setFileName(rs.getString("LAST_FILE_NAME"));
				aj.setNextRunDate(rs.getTimestamp("NEXT_RUN_DATE"));
				aj.setCachedTableName(rs.getString("CACHED_TABLE_NAME"));
				aj.setLastEndDate(rs.getTimestamp("LAST_END_DATE"));
				aj.setOutputFormat(rs.getString("OUTPUT_FORMAT"));
				aj.setTos(rs.getString("MAIL_TOS"));
				aj.setSubject(rs.getString("SUBJECT"));
				aj.setMessage(rs.getString("MESSAGE"));
				aj.setMinute(rs.getString("JOB_MINUTE"));
				aj.setHour(rs.getString("JOB_HOUR"));
				aj.setDay(rs.getString("JOB_DAY"));
				aj.setWeekday(rs.getString("JOB_WEEKDAY"));
				aj.setMonth(rs.getString("JOB_MONTH"));

				aj.buildParametersDisplayString();

				jobs.put(Integer.valueOf(rs.getInt("JOB_ID")), aj);
			}
			rs.close();
			ps.close();


			//get jobs user has direct access to. both split and non-split jobs
			sql = "SELECT aq.NAME AS QUERY_NAME, aj.JOB_NAME, aj.JOB_ID, aj.JOB_TYPE, aj.SUBJECT, aq.USES_RULES, aj.ALLOW_SPLITTING "
					+ " , aj.LAST_START_DATE , aj.LAST_FILE_NAME , aj.NEXT_RUN_DATE, aj.CACHED_TABLE_NAME "
					+ " ,auj.LAST_FILE_NAME AS SHARED_FILE_NAME, auj.LAST_START_DATE AS SHARED_START_DATE "
					+ " ,aj.LAST_END_DATE, aj.OUTPUT_FORMAT, aj.MAIL_TOS, aj.SUBJECT, aj.MESSAGE "
					+ " ,aj.JOB_MINUTE, aj.JOB_HOUR, aj.JOB_DAY, aj.JOB_WEEKDAY, aj.JOB_MONTH, auj.LAST_END_DATE AS SHARED_END_DATE "
					+ " FROM ART_JOBS aj, ART_QUERIES aq, ART_USER_JOBS auj "
					+ " WHERE aq.QUERY_ID = aj.QUERY_ID AND aj.JOB_ID=auj.JOB_ID "
					+ " AND auj.USERNAME = ? AND aj.USERNAME <> ?";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, username);
			rs = ps.executeQuery();

			while (rs.next()) {
				ArtJob aj = new ArtJob();
				aj.setQueryName(rs.getString("QUERY_NAME"));
				aj.setJobName(rs.getString("JOB_NAME"));
				aj.setJobIdOnly(rs.getInt("JOB_ID"));
				aj.setJobType(rs.getInt("JOB_TYPE"));
				aj.setQueryRulesFlag(rs.getString("USES_RULES"));
				aj.setAllowSplitting(rs.getString("ALLOW_SPLITTING"));
				aj.setLastStartDate(rs.getTimestamp("LAST_START_DATE"));
				aj.setFileName(rs.getString("LAST_FILE_NAME"));
				aj.setNextRunDate(rs.getTimestamp("NEXT_RUN_DATE"));
				aj.setCachedTableName(rs.getString("CACHED_TABLE_NAME"));
				aj.setSharedFileName(rs.getString("SHARED_FILE_NAME"));
				aj.setSharedLastStartDate(rs.getTimestamp("SHARED_START_DATE"));
				aj.setLastEndDate(rs.getTimestamp("LAST_END_DATE"));
				aj.setSharedLastEndDate(rs.getTimestamp("SHARED_END_DATE"));
				aj.setOutputFormat(rs.getString("OUTPUT_FORMAT"));
				aj.setTos(rs.getString("MAIL_TOS"));
				aj.setSubject(rs.getString("SUBJECT"));
				aj.setMessage(rs.getString("MESSAGE"));
				aj.setMinute(rs.getString("JOB_MINUTE"));
				aj.setHour(rs.getString("JOB_HOUR"));
				aj.setDay(rs.getString("JOB_DAY"));
				aj.setWeekday(rs.getString("JOB_WEEKDAY"));
				aj.setMonth(rs.getString("JOB_MONTH"));

				aj.buildParametersDisplayString();

				jobs.put(new Integer(rs.getInt("JOB_ID")), aj);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return jobs;
	}

	/**
	 * Get the job archives the user has access to
	 *
	 * @return the job archives the user has access to
	 */
	public Map<String, ArtJob> getJobArchives() {
		Map<String, ArtJob> jobs = new TreeMap<String, ArtJob>();

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();
			String sql;
			ResultSet rs;

			//get job archives that user has access to
			sql = "SELECT AJ.JOB_ID, AJ.JOB_NAME, AJA.ARCHIVE_ID, AJA.ARCHIVE_FILE_NAME, AJA.END_DATE "
					+ " FROM ART_JOB_ARCHIVES AJA, ART_JOBS AJ"
					+ " WHERE AJA.JOB_ID=AJ.JOB_ID"
					+ " AND AJA.USERNAME=?" //user owns job or individualized output
					+ " UNION"
					+ " SELECT AJ.JOB_ID, AJ.JOB_NAME, AJA.ARCHIVE_ID, AJA.ARCHIVE_FILE_NAME, AJA.END_DATE"
					+ " FROM ART_JOB_ARCHIVES AJA, ART_JOBS AJ, ART_USER_JOBS AUJ"
					+ " WHERE AJA.JOB_ID=AJ.JOB_ID AND AJ.JOB_ID=AUJ.JOB_ID"
					+ " AND AJA.USERNAME<>? AND AJA.JOB_SHARED='Y' AND AUJ.USERNAME=?" //job shared with user
					+ " UNION"
					+ " SELECT AJ.JOB_ID, AJ.JOB_NAME, AJA.ARCHIVE_ID, AJA.ARCHIVE_FILE_NAME, AJA.END_DATE"
					+ " FROM ART_JOB_ARCHIVES AJA, ART_JOBS AJ, ART_USER_GROUP_JOBS AUGJ, ART_USER_GROUP_ASSIGNMENT AUGA"
					+ " WHERE AJA.JOB_ID=AJ.JOB_ID AND AJ.JOB_ID=AUGJ.JOB_ID"
					+ " AND AUGJ.USER_GROUP_ID=AUGA.USER_GROUP_ID AND AUGA.USERNAME=?"
					+ " AND AJA.USERNAME<>? AND AJA.JOB_SHARED='Y'"; //job shared with user group

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, username);
			ps.setString(3, username);
			ps.setString(4, username);
			ps.setString(5, username);

			rs = ps.executeQuery();

			while (rs.next()) {
				ArtJob aj = new ArtJob();
				int jobId = rs.getInt("JOB_ID");

				aj.setJobIdOnly(jobId);
				aj.setJobName(rs.getString("JOB_NAME"));
				aj.setFileName(rs.getString("ARCHIVE_FILE_NAME"));

				java.util.Date endDate = rs.getTimestamp("END_DATE");
				aj.setLastEndDate(endDate);

				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");
				DecimalFormat nf = new DecimalFormat("0");
				nf.setMinimumIntegerDigits(10); //ensure all numbers are pre-padded with zeros so that sorting works correctly
				nf.setMaximumFractionDigits(20);
				
				String datePart="";
				if(endDate!=null){
					datePart=df.format(endDate);
				}

				String key = nf.format(jobId) + datePart;

				jobs.put(key, aj);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return jobs;
	}

	/**
	 * Get the jobs the user owns
	 *
	 * @return the jobs the user owns
	 */
	public Map<Integer, ArtJob> getJobs() {
		Map<Integer, ArtJob> jobs = new TreeMap<Integer, ArtJob>();

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();
			String sql;
			ResultSet rs;

			sql = "SELECT aq.NAME AS QUERY_NAME, aj.JOB_NAME, aj.USERNAME"
					+ " ,aj.OUTPUT_FORMAT, aj.JOB_TYPE "
					+ " , aj.MAIL_TOS, aj.MESSAGE , aj.SUBJECT "
					+ " , aj.JOB_MINUTE, aj.JOB_HOUR, aj.JOB_DAY, aj.JOB_WEEKDAY, aj.JOB_MONTH "
					+ " , aj.JOB_ID, aj.CACHED_TABLE_NAME "
					+ " , aj.LAST_START_DATE ,  aj.LAST_END_DATE , aj.LAST_FILE_NAME"
					+ " , aq.QUERY_TYPE, aj.NEXT_RUN_DATE "
					+ " FROM ART_JOBS aj , ART_QUERIES aq "
					+ " WHERE aq.QUERY_ID = aj.QUERY_ID "
					+ " AND aj.USERNAME = ? ";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			rs = ps.executeQuery();

			while (rs.next()) {
				ArtJob aj = new ArtJob();
				aj.setQueryName(rs.getString("QUERY_NAME"));
				aj.setJobName(rs.getString("JOB_NAME"));
				aj.setUsername(rs.getString("USERNAME"));
				aj.setJobIdOnly(rs.getInt("JOB_ID"));
				aj.setJobType(rs.getInt("JOB_TYPE"));
				aj.setQueryType(rs.getInt("QUERY_TYPE"));
				aj.setLastStartDate(rs.getTimestamp("LAST_START_DATE"));
				aj.setFileName(rs.getString("LAST_FILE_NAME"));
				aj.setNextRunDate(rs.getTimestamp("NEXT_RUN_DATE"));
				aj.setCachedTableName(rs.getString("CACHED_TABLE_NAME"));
				aj.setLastEndDate(rs.getTimestamp("LAST_END_DATE"));
				aj.setOutputFormat(rs.getString("OUTPUT_FORMAT"));
				aj.setTos(rs.getString("MAIL_TOS"));
				aj.setSubject(rs.getString("SUBJECT"));
				aj.setMessage(rs.getString("MESSAGE"));
				aj.setMinute(rs.getString("JOB_MINUTE"));
				aj.setHour(rs.getString("JOB_HOUR"));
				aj.setDay(rs.getString("JOB_DAY"));
				aj.setWeekday(rs.getString("JOB_WEEKDAY"));
				aj.setMonth(rs.getString("JOB_MONTH"));

				aj.buildParametersDisplayString();

				jobs.put(Integer.valueOf(rs.getInt("JOB_ID")), aj);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return jobs;
	}

	/**
	 * Get the jobs the user doesn't own
	 *
	 * @return the jobs the user doesn't own
	 */
	public Map<String, ArtJob> getOtherJobs() {
		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.TERTIARY); //order by case
		Map<String, ArtJob> jobs = new TreeMap<String, ArtJob>(stringCollator);

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			ResultSet rs;
			String uname;
			int jobId;

			sql = "SELECT aq.NAME AS QUERY_NAME, aj.JOB_NAME, aj.USERNAME, aj.OUTPUT_FORMAT, aj.JOB_TYPE "
					+ " , aj.MAIL_TOS, aj.MESSAGE , aj.SUBJECT "
					+ " , aj.JOB_MINUTE, aj.JOB_HOUR, aj.JOB_DAY, aj.JOB_WEEKDAY, aj.JOB_MONTH "
					+ " , aj.JOB_ID, aj.CACHED_TABLE_NAME "
					+ " , aj.LAST_START_DATE ,  aj.LAST_END_DATE , aj.LAST_FILE_NAME , aq.QUERY_TYPE, aj.NEXT_RUN_DATE "
					+ " FROM ART_JOBS aj , ART_QUERIES aq "
					+ " WHERE aq.QUERY_ID = aj.QUERY_ID "
					+ " AND aj.USERNAME <> ? ";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			rs = ps.executeQuery();

			while (rs.next()) {
				ArtJob aj = new ArtJob();
				aj.setQueryName(rs.getString("QUERY_NAME"));
				aj.setJobName(rs.getString("JOB_NAME"));
				aj.setUsername(rs.getString("USERNAME"));
				aj.setJobIdOnly(rs.getInt("JOB_ID"));
				aj.setJobType(rs.getInt("JOB_TYPE"));
				aj.setQueryType(rs.getInt("QUERY_TYPE"));
				aj.setLastStartDate(rs.getTimestamp("LAST_START_DATE"));
				aj.setFileName(rs.getString("LAST_FILE_NAME"));
				aj.setNextRunDate(rs.getTimestamp("NEXT_RUN_DATE"));
				aj.setCachedTableName(rs.getString("CACHED_TABLE_NAME"));
				aj.setLastEndDate(rs.getTimestamp("LAST_END_DATE"));
				aj.setOutputFormat(rs.getString("OUTPUT_FORMAT"));
				aj.setTos(rs.getString("MAIL_TOS"));
				aj.setSubject(rs.getString("SUBJECT"));
				aj.setMessage(rs.getString("MESSAGE"));
				aj.setMinute(rs.getString("JOB_MINUTE"));
				aj.setHour(rs.getString("JOB_HOUR"));
				aj.setDay(rs.getString("JOB_DAY"));
				aj.setWeekday(rs.getString("JOB_WEEKDAY"));
				aj.setMonth(rs.getString("JOB_MONTH"));

				aj.buildParametersDisplayString();

				uname = rs.getString("USERNAME");
				jobId = rs.getInt("JOB_ID");

				jobs.put(uname + jobId, aj);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return jobs;
	}

	/**
	 * Get all usernames
	 *
	 * @return all usernames
	 */
	public List<String> getAllUsernames() {
		List<String> usernames = new ArrayList<String>();

		Connection conn = null;
		Statement st = null;

		try {
			conn = ArtConfig.getConnection();
			st = conn.createStatement();
			String SqlQuery = "SELECT USERNAME FROM ART_USERS "
					+ //" WHERE USERNAME <> 'public_user' "+  // 20110503 - eliboni - commented out otherwise it is not possible to assign privs to public_user....
					" ORDER BY USERNAME";

			ResultSet rs = st.executeQuery(SqlQuery);
			while (rs.next()) {
				usernames.add(rs.getString("USERNAME"));
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		//sort usernames
		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.TERTIARY); //order by case
		Collections.sort(usernames, stringCollator);

		return usernames;
	}

	/**
	 * Get usernames for junior and mid level admins
	 *
	 * @return usernames for junior and mid level admins
	 */
	public List<String> getJuniorAdminUsernames() {
		List<String> usernames = new ArrayList<String>();

		Connection conn = null;
		Statement st = null;

		try {
			conn = ArtConfig.getConnection();
			st = conn.createStatement();
			String SqlQuery = "SELECT USERNAME FROM ART_USERS "
					+ " WHERE ACCESS_LEVEL BETWEEN 10 AND 30";

			ResultSet rs = st.executeQuery(SqlQuery);
			while (rs.next()) {
				usernames.add(rs.getString("USERNAME"));
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		//sort usernames
		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.TERTIARY); //order by case
		Collections.sort(usernames, stringCollator);

		return usernames;
	}

	/**
	 * Update this user's password
	 *
	 * @param newPassword
	 * @param algorithm
	 * @return <code>true</code> if successful
	 */
	public boolean updatePassword(String newPassword, String algorithm) {
		boolean success = false;

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;

			sql = "UPDATE ART_USERS SET PASSWORD = ?, UPDATE_DATE = ?, HASHING_ALGORITHM=? "
					+ " WHERE USERNAME = ?";

			ps = conn.prepareStatement(sql);

			ps.setString(1, newPassword);
			ps.setDate(2, new java.sql.Date(System.currentTimeMillis()));
			ps.setString(3, algorithm);
			ps.setString(4, username);

			ps.executeUpdate();
			success = true;
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return success;
	}

	/**
	 * Get all shared jobs
	 *
	 * @return all shared jobs
	 */
	public Map<String, ArtJob> getAllSharedJobs() {
		Collator stringCollator = Collator.getInstance();
		stringCollator.setStrength(Collator.TERTIARY); //order by case
		TreeMap<String, ArtJob> jobs = new TreeMap<String, ArtJob>(stringCollator);

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			ResultSet rs;
			String uname;
			int jobId;

			sql = "SELECT AJ.JOB_ID, AJ.JOB_NAME, AJ.USERNAME"
					+ " FROM ART_JOBS AJ, ART_QUERIES AQ"
					+ " WHERE AJ.QUERY_ID=AQ.QUERY_ID "
					+ " AND AJ.ALLOW_SHARING='Y'";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				ArtJob aj = new ArtJob();
				aj.setJobName(rs.getString("JOB_NAME"));
				aj.setUsername(rs.getString("USERNAME"));
				aj.setJobIdOnly(rs.getInt("JOB_ID"));

				uname = rs.getString("USERNAME");
				jobId = rs.getInt("JOB_ID");

				jobs.put(uname + jobId, aj);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return jobs;
	}

	/**
	 * Grant or revoke access to query groups and datasources for junior and mid
	 * admins
	 *
	 * @param action
	 * @param admins
	 * @param queryGroups
	 * @param datasources
	 */
	public void updateJuniorAdminPrivileges(String action, String[] admins, String[] queryGroups, String[] datasources) {

		if (action == null || admins == null) {
			return;
		}

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();

			String sqlGroup;
			String sqlDatasource;
			PreparedStatement psGroup;
			PreparedStatement psDatasource;

			if (action.equals("GRANT")) {
				sqlGroup = "INSERT INTO ART_ADMIN_PRIVILEGES (USERNAME, PRIVILEGE, VALUE_ID) values (? , 'GRP', ? ) ";
				sqlDatasource = "INSERT INTO ART_ADMIN_PRIVILEGES (USERNAME, PRIVILEGE, VALUE_ID) values (? , 'DB', ? ) ";
			} else {
				sqlGroup = "DELETE FROM ART_ADMIN_PRIVILEGES WHERE PRIVILEGE = 'GRP' AND USERNAME = ? AND VALUE_ID = ? ";
				sqlDatasource = "DELETE FROM ART_ADMIN_PRIVILEGES WHERE PRIVILEGE = 'DB' AND USERNAME = ? AND VALUE_ID = ? ";
			}
			psGroup = conn.prepareStatement(sqlGroup);
			psDatasource = conn.prepareStatement(sqlDatasource);

			for (int i = 0; i < admins.length; i++) {
				//update query group privileges
				if (queryGroups != null) {
					for (int j = 0; j < queryGroups.length; j++) {
						psGroup.setString(1, admins[i]);
						psGroup.setInt(2, Integer.parseInt(queryGroups[j]));
						try {
							psGroup.executeUpdate();
						} catch (SQLIntegrityConstraintViolationException e) {
							logger.info("Admin {} already has access to Query Group ID {}", admins[i], queryGroups[j]);
						} catch (SQLException e) {
							logger.error("Error updating admin privileges. Query Group ID={}, Admin={}", new Object[]{queryGroups[j], admins[i], e});
						}
					}
				}

				//update datasource privileges
				if (datasources != null) {
					for (int j = 0; j < datasources.length; j++) {
						psDatasource.setString(1, admins[i]);
						psDatasource.setInt(2, Integer.parseInt(datasources[j]));
						try {
							psDatasource.executeUpdate();
						} catch (SQLIntegrityConstraintViolationException e) {
							logger.info("Admin {} already has access to Datasource ID {}", admins[i], datasources[j]);
						} catch (SQLException e) {
							logger.error("Error updating admin privileges. Datasource ID={}, Admin={}", new Object[]{datasources[j], admins[i], e});
						}
					}
				}
			}
			psGroup.close();
			psDatasource.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}
	}

	/**
	 * Get an indicator of which junior and mid admins have access to which
	 * query groups
	 *
	 * @return an indicator of which junior and mid admins have access to which
	 * query groups
	 */
	public Map<Integer, String> getJuniorAdminGroupAssignment() {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			ResultSet rs;
			String tmp;
			Integer count = 0;

			sql = "SELECT AAP.USERNAME, AQG.NAME "
					+ " FROM ART_ADMIN_PRIVILEGES AAP,ART_QUERY_GROUPS AQG "
					+ " WHERE AAP.VALUE_ID=AQG.QUERY_GROUP_ID "
					+ " AND AAP.PRIVILEGE='GRP' "
					+ " ORDER BY AAP.USERNAME";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				count++;
				tmp = rs.getString("USERNAME") + " - " + rs.getString("NAME");
				map.put(count, tmp);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}

	/**
	 * Get an indicator of which junior and mid admins have access to which
	 * datasources
	 *
	 * @return an indicator of which junior and mid admins have access to which
	 * datasources
	 */
	public Map<Integer, String> getJuniorAdminDatasourceAssignment() {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			ResultSet rs;
			String tmp;
			Integer count = 0;

			sql = "SELECT AAP.USERNAME, AD.NAME "
					+ " FROM ART_ADMIN_PRIVILEGES AAP,ART_DATABASES AD "
					+ " WHERE AAP.VALUE_ID=AD.DATABASE_ID "
					+ " AND AAP.PRIVILEGE='DB' "
					+ " ORDER BY AAP.USERNAME";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				count++;
				tmp = rs.getString("USERNAME") + " - " + rs.getString("NAME");
				map.put(count, tmp);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}

	/**
	 * Get lookup rules that reference a given user
	 *
	 * @param uName
	 * @return lookup rules that reference a given user
	 */
	public Map<Integer, Rule> getLinkedLookupRules(String uName) {
		TreeMap<Integer, Rule> map = new TreeMap<Integer, Rule>();

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			ResultSet rs;
			Integer count = 0;

			sql = "SELECT USERNAME, RULE_NAME "
					+ " FROM ART_USER_RULES "
					+ " WHERE RULE_TYPE='LOOKUP' AND RULE_VALUE=? "
					+ " ORDER BY USERNAME,RULE_NAME";

			ps = conn.prepareStatement(sql);

			ps.setString(1, uName);
			rs = ps.executeQuery();
			while (rs.next()) {
				count++;

				Rule rule = new Rule();
				rule.setUsername(rs.getString("USERNAME"));
				rule.setRuleName(rs.getString("RULE_NAME"));

				map.put(count, rule);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}

	/**
	 * Get all queries
	 *
	 * @param level
	 * @param uname
	 * @return all queries
	 */
	public Map<Integer, ArtQuery> getAdminQueries(int level, String uname) {
		TreeMap<Integer, ArtQuery> map = new TreeMap<Integer, ArtQuery>();

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			ResultSet rs;
			Integer count = 0;

			if (level > 30) {
				//get all queries
				sql = "SELECT AG.NAME AS GROUP_NAME, AQ.NAME, AQ.QUERY_ID "
						+ " FROM ART_QUERIES AQ, ART_QUERY_GROUPS AG "
						+ " WHERE AG.QUERY_GROUP_ID = AQ.QUERY_GROUP_ID "
						+ " ORDER BY AG.NAME , AQ.NAME ";
				ps = conn.prepareStatement(sql);
			} else {
				// get only queries matching the "junior" admin priviledges
				sql = "SELECT AG.NAME AS GROUP_NAME, AQ.NAME, AQ.QUERY_ID "
						+ " FROM ART_QUERIES AQ, ART_QUERY_GROUPS AG, ART_ADMIN_PRIVILEGES AP "
						+ " WHERE AG.QUERY_GROUP_ID = AQ.QUERY_GROUP_ID "
						+ " AND AG.QUERY_GROUP_ID = AP.VALUE_ID "
						+ " AND AP.PRIVILEGE = 'GRP' "
						+ " AND AP.USERNAME = ? "
						+ " ORDER BY AG.NAME , AQ.NAME ";
				ps = conn.prepareStatement(sql);
				ps.setString(1, uname);
			}
			rs = ps.executeQuery();

			while (rs.next()) {
				count++;
				ArtQuery aq = new ArtQuery();
				aq.setGroupName(rs.getString("GROUP_NAME"));
				aq.setName(rs.getString("NAME"));
				aq.setQueryId(rs.getInt("QUERY_ID"));

				map.put(count, aq);
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}

	/**
	 * Grant or revoke access to queries and query groups for given users
	 *
	 * @param action
	 * @param users
	 * @param queries
	 * @param queryGroups
	 */
	public void updateUserPrivileges(String action, String[] users, String[] queries, String[] queryGroups) {

		if (action == null || users == null) {
			return;
		}

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();

			String sqlGroup;
			String sqlObject;
			PreparedStatement psGroup;
			PreparedStatement psObject;

			if (action.equals("GRANT")) {
				sqlGroup = "INSERT INTO ART_USER_QUERY_GROUPS (USERNAME, QUERY_GROUP_ID) values (? , ? ) ";
				sqlObject = "INSERT INTO ART_USER_QUERIES (USERNAME, QUERY_ID) values (? , ?)";
			} else {
				sqlGroup = "DELETE FROM ART_USER_QUERY_GROUPS WHERE USERNAME = ? AND QUERY_GROUP_ID = ? ";
				sqlObject = "DELETE FROM ART_USER_QUERIES WHERE USERNAME = ? AND QUERY_ID = ? ";
			}
			psGroup = conn.prepareStatement(sqlGroup);
			psObject = conn.prepareStatement(sqlObject);

			for (int i = 0; i < users.length; i++) {
				//update query group privileges
				if (queryGroups != null) {
					for (int j = 0; j < queryGroups.length; j++) {
						psGroup.setString(1, users[i]);
						psGroup.setInt(2, Integer.parseInt(queryGroups[j]));
						try {
							psGroup.executeUpdate();
						} catch (SQLIntegrityConstraintViolationException e) {
							logger.info("Access to Query Group ID {} already granted to User {}", queryGroups[j], users[i]);
						} catch (SQLException e) {
							logger.error("Error updating user privileges. Query Group ID={}, User={}", new Object[]{queryGroups[j], users[i], e});
						}
					}
				}

				//update object privileges
				if (queries != null) {
					for (int j = 0; j < queries.length; j++) {
						psObject.setString(1, users[i]);
						psObject.setInt(2, Integer.parseInt(queries[j]));
						try {
							psObject.executeUpdate();
						} catch (SQLIntegrityConstraintViolationException e) {
							logger.info("Access to Query ID {} already granted to User {}", queries[j], users[i]);
						} catch (SQLException e) {
							logger.error("Error updating user privileges. Query ID={}, User={}", new Object[]{queries[j], users[i], e});
						}
					}
				}
			}
			psGroup.close();
			psObject.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}
	}

	/**
	 * Grant or revoke access to queries and query groups for given user groups
	 *
	 * @param action
	 * @param userGroups
	 * @param queries
	 * @param queryGroups
	 */
	public void updateUserGroupPrivileges(String action, String[] userGroups, String[] queries, String[] queryGroups) {

		if (action == null || userGroups == null) {
			return;
		}

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();

			String sqlGroup;
			String sqlObject;
			PreparedStatement psGroup;
			PreparedStatement psObject;

			if (action.equals("GRANT")) {
				sqlGroup = "INSERT INTO ART_USER_GROUP_GROUPS (USER_GROUP_ID, QUERY_GROUP_ID) values (? , ? ) ";
				sqlObject = "INSERT INTO ART_USER_GROUP_QUERIES (USER_GROUP_ID, QUERY_ID) values (? , ?)";
			} else {
				sqlGroup = "DELETE FROM ART_USER_GROUP_GROUPS WHERE USER_GROUP_ID = ? AND QUERY_GROUP_ID = ? ";
				sqlObject = "DELETE FROM ART_USER_GROUP_QUERIES WHERE USER_GROUP_ID = ? AND QUERY_ID = ? ";
			}
			psGroup = conn.prepareStatement(sqlGroup);
			psObject = conn.prepareStatement(sqlObject);

			for (int i = 0; i < userGroups.length; i++) {
				//update query group privileges
				if (queryGroups != null) {
					for (int j = 0; j < queryGroups.length; j++) {
						psGroup.setInt(1, Integer.parseInt(userGroups[i]));
						psGroup.setInt(2, Integer.parseInt(queryGroups[j]));
						try {
							psGroup.executeUpdate();
						} catch (SQLIntegrityConstraintViolationException e) {
							logger.info("Access to Query Group ID {} already granted to User Group ID {}", queryGroups[j], userGroups[i]);
						} catch (SQLException e) {
							logger.error("Error updating user privileges. Query Group ID={}, User Group ID={}", new Object[]{queryGroups[j], userGroups[i], e});
						}
					}
				}

				//update object privileges
				if (queries != null) {
					for (int j = 0; j < queries.length; j++) {
						psObject.setInt(1, Integer.parseInt(userGroups[i]));
						psObject.setInt(2, Integer.parseInt(queries[j]));
						try {
							psObject.executeUpdate();
						} catch (SQLIntegrityConstraintViolationException e) {
							logger.info("Access to Query ID {} already granted to User Group ID {}", queries[j], userGroups[i]);
						} catch (SQLException e) {
							logger.error("Error updating user privileges. Query ID={}, User Group ID={}", new Object[]{queries[j], userGroups[i], e});
						}
					}
				}
			}
			psGroup.close();
			psObject.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}
	}

	/**
	 * Get an indicator of which users and user groups have access to which
	 * query groups
	 *
	 * @return an indicator of which users and user groups have access to which
	 * query groups
	 */
	public Map<Integer, String> getQueryGroupAssignment() {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			PreparedStatement ps;
			ResultSet rs;
			String tmp;
			Integer count = 0;

			//add user group assignments
			sql = "SELECT AUG.NAME AS USER_GROUP,AQG.NAME AS QUERY_GROUP "
					+ " FROM ART_USER_GROUP_GROUPS AUGG, ART_USER_GROUPS AUG, ART_QUERY_GROUPS AQG "
					+ " WHERE AUGG.USER_GROUP_ID=AUG.USER_GROUP_ID "
					+ " AND AUGG.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
					+ " ORDER BY AUG.NAME";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				count++;
				tmp = "[" + rs.getString("USER_GROUP") + "] - " + rs.getString("QUERY_GROUP");
				map.put(count, tmp);
			}
			rs.close();
			ps.close();

			//add user assignments
			sql = "SELECT AUQG.USERNAME, AQG.NAME AS QUERY_GROUP "
					+ " FROM ART_USER_QUERY_GROUPS AUQG, ART_QUERY_GROUPS AQG "
					+ " WHERE AUQG.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
					+ " ORDER BY AUQG.USERNAME";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				count++;
				tmp = rs.getString("USERNAME") + " - " + rs.getString("QUERY_GROUP");
				map.put(count, tmp);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}

	/**
	 * Get an indicator of which users and user groups have access to which
	 * queries
	 *
	 * @return an indicator of which users and user groups have access to which
	 * queries
	 */
	public Map<Integer, String> getQueryAssignment() {
		TreeMap<Integer, String> map = new TreeMap<Integer, String>();

		Connection conn = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;
			PreparedStatement ps;
			ResultSet rs;
			String tmp;
			Integer count = 0;

			//add user group assignments
			sql = "SELECT AUG.NAME AS USER_GROUP,AQ.NAME AS QUERY_NAME "
					+ " FROM ART_USER_GROUP_QUERIES AUGQ, ART_USER_GROUPS AUG, ART_QUERIES AQ "
					+ " WHERE AUGQ.USER_GROUP_ID=AUG.USER_GROUP_ID "
					+ " AND AUGQ.QUERY_ID=AQ.QUERY_ID "
					+ " ORDER BY AUG.NAME";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				count++;
				tmp = "[" + rs.getString("USER_GROUP") + "] - " + rs.getString("QUERY_NAME");
				map.put(count, tmp);
			}
			rs.close();
			ps.close();

			//add user assignments
			sql = "SELECT AUQ.USERNAME, AQ.NAME AS QUERY_NAME "
					+ " FROM ART_USER_QUERIES AUQ, ART_QUERIES AQ "
					+ " WHERE AUQ.QUERY_ID=AQ.QUERY_ID "
					+ " ORDER BY AUQ.USERNAME";

			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				count++;
				tmp = rs.getString("USERNAME") + " - " + rs.getString("QUERY_NAME");
				map.put(count, tmp);
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return map;
	}
}
