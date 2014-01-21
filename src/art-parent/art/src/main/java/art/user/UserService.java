package art.user;

import art.enums.AccessLevel;
import art.servlets.ArtConfig;
import art.utils.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to users
 *
 * @author Timothy Anyona
 */
@Service
public class UserService {

	final static Logger logger = LoggerFactory.getLogger(UserService.class);
	final String SQL_GET_ALL_USERS = "SELECT USERNAME, EMAIL, ACCESS_LEVEL, FULL_NAME, "
			+ " ACTIVE, PASSWORD, DEFAULT_QUERY_GROUP, HASHING_ALGORITHM, START_QUERY, "
			+ " USER_ID, CAN_CHANGE_PASSWORD, CREATION_DATE, UPDATE_DATE "
			+ " FROM ART_USERS ";

	/**
	 * Get a user object for the given username
	 *
	 * @param username
	 * @return populated user object if username exists, otherwise null
	 * @throws java.sql.SQLException
	 */
	public User getUser(String username) throws SQLException {
		User user = null;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_GET_ALL_USERS + " WHERE USERNAME = ? ";

		Object[] values = {
			username
		};

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.executeQuery(conn, ps, sql, values);
			if (rs.next()) {
				user = new User();
				populateUser(user, rs);
				//set user properties whose values may come from user groups
				populateGroupValues(conn, user);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return user;
	}

	/**
	 * Get a user object
	 *
	 * @param userId
	 * @return user object if user found, null otherwise
	 * @throws SQLException
	 */
	public User getUser(int userId) throws SQLException {
		User user = null;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_GET_ALL_USERS + " WHERE USER_ID = ? ";

		Object[] values = {
			userId
		};

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.executeQuery(conn, ps, sql, values);
			if (rs.next()) {
				user = new User();
				populateUser(user, rs);
				//set user properties whose values may come from user groups
				populateGroupValues(conn, user);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return user;
	}

	/**
	 * Populate user object with row from users table
	 *
	 * @param user
	 * @param rs
	 * @throws SQLException
	 */
	private void populateUser(User user, ResultSet rs) throws SQLException {
		user.setUsername(rs.getString("USERNAME"));
		user.setEmail(rs.getString("EMAIL"));
		user.setAccessLevel(AccessLevel.getEnum(rs.getInt("ACCESS_LEVEL")));
		user.setFullName(rs.getString("FULL_NAME"));
		user.setActive(rs.getBoolean("ACTIVE"));
		user.setPassword(rs.getString("PASSWORD"));
		user.setDefaultQueryGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
		user.setHashingAlgorithm(rs.getString("HASHING_ALGORITHM"));
		user.setStartQuery(rs.getString("START_QUERY"));
		user.setUserId(rs.getInt("USER_ID"));
		user.setCanChangePassword(rs.getBoolean("CAN_CHANGE_PASSWORD"));
		user.setCreationDate(rs.getTimestamp("CREATION_DATE"));
		user.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
	}

	/**
	 * Set user properties whose values may come from user groups
	 *
	 * @param conn
	 * @param user
	 */
	private void populateGroupValues(Connection conn, User user) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT AUG.DEFAULT_QUERY_GROUP, AUG.START_QUERY "
					+ " FROM ART_USER_GROUP_ASSIGNMENT AUGA, ART_USER_GROUPS AUG "
					+ " WHERE AUGA.USER_GROUP_ID=AUG.USER_GROUP_ID "
					+ " AND AUGA.USERNAME=? "
					+ " ORDER BY AUG.NAME";

			ps = conn.prepareStatement(sql);
			ps.setString(1, user.getUsername());

			int defaultQueryGroup = user.getDefaultQueryGroup();
			String startQuery = user.getStartQuery();

			rs = ps.executeQuery();
			while (rs.next()) {
				if (defaultQueryGroup <= 0) {
					defaultQueryGroup = rs.getInt("DEFAULT_QUERY_GROUP");
				}
				if (StringUtils.isBlank(startQuery)) {
					startQuery = rs.getString("START_QUERY");
				}
			}

			user.setDefaultQueryGroup(defaultQueryGroup);
			user.setStartQuery(startQuery);
		} finally {
			DbUtils.close(rs, ps);
		}
	}

	/**
	 * Get all users
	 *
	 * @return all users
	 * @throws java.sql.SQLException
	 */
	public List<User> getAllUsers() throws SQLException {
		List<User> users = new ArrayList<User>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_GET_ALL_USERS;

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.executeQuery(conn, ps, sql);
			while (rs.next()) {
				User user = new User();
				populateUser(user, rs);
				users.add(user);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return users;
	}

	/**
	 * Delete a user and all related records
	 *
	 * @param userId
	 * @throws SQLException
	 */
	public void deleteUser(int userId) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();
			String sql;
			int affectedRows;

			//delete user-report relationships
			sql = "DELETE FROM ART_USER_QUERIES WHERE USER_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();

			//delete user-report group relationships
			sql = "DELETE FROM ART_USER_QUERY_GROUPS WHERE USER_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();

			//delete user-rules relationships
			sql = "DELETE FROM ART_USER_RULES WHERE USER_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();

			//delete user-user group relationships
			sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();

			//delete user-shared job relationships
			sql = "DELETE FROM ART_USER_JOBS WHERE USER_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			ps.executeUpdate();

			//delete user's jobs. this will delete all records related to the job e.g. quartz records, job parameters etc
			sql = "SELECT JOB_ID FROM ART_JOBS WHERE USER_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);

			rs = ps.executeQuery();
			while (rs.next()) {
				//TODO delete jor using user id
//				ArtJob aj = new ArtJob();
//				aj.load(rs.getInt("JOB_ID"), userId);
//				aj.delete();
			}

			//lastly, delete user
			sql = "DELETE FROM ART_USERS WHERE USER_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, userId);
			affectedRows = ps.executeUpdate();
			if (affectedRows == 0) {
				logger.warn("Delete user failed. User not found. User Id={}", userId);
			}

		} finally {
			DbUtils.close(rs, ps, conn);
		}
	}

	/**
	 * Update a user's password
	 *
	 * @param userId
	 * @param newPassword bcrypt hash
	 * @throws SQLException
	 */
	public void updatePassword(int userId, String newPassword) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;

		String sql = "UPDATE ART_USERS SET PASSWORD = ?, UPDATE_DATE = ?,"
				+ " HASHING_ALGORITHM='bcrypt'"
				+ " WHERE USER_ID = ?";

		Object[] values = {
			newPassword,
			DbUtils.getCurrentTimeStamp(),
			userId
		};

		try {
			conn = ArtConfig.getConnection();
			int affectedRows = DbUtils.executeUpdate(conn, ps, sql, values);
			if (affectedRows == 0) {
				logger.warn("Update password failed. User not found. User ID={}", userId);
			}
		} finally {
			DbUtils.close(ps, conn);
		}
	}
}
