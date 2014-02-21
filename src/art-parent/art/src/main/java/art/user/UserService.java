package art.user;

import art.enums.AccessLevel;
import art.servlets.ArtConfig;
import art.dbutils.DbUtils;
import art.usergroup.UserGroup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to users
 *
 * @author Timothy Anyona
 */
@Service
public class UserService {

	//for caching info, see
	//http://wangxiangblog.blogspot.com/2013/02/spring-cache.html
	//http://viralpatel.net/blogs/cache-support-spring-3-1-m1/
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	final String SQL_SELECT_ALL_USERS = "SELECT USERNAME, EMAIL, ACCESS_LEVEL, FULL_NAME, "
			+ " ACTIVE, PASSWORD, DEFAULT_QUERY_GROUP, PASSWORD_ALGORITHM, START_QUERY, "
			+ " USER_ID, CAN_CHANGE_PASSWORD, CREATION_DATE, UPDATE_DATE "
			+ " FROM ART_USERS ";

	/**
	 * Get a user object for the given username
	 *
	 * @param username
	 * @return populated user object if username exists, otherwise null
	 * @throws java.sql.SQLException
	 */
	@Cacheable("users")
	public User getUser(String username) throws SQLException {
		User user = null;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_SELECT_ALL_USERS + " WHERE USERNAME = ? ";

		Object[] values = {
			username
		};

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.query(conn, ps, sql, values);
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
	@Cacheable("users")
	public User getUser(int userId) throws SQLException {
		User user = null;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_SELECT_ALL_USERS + " WHERE USER_ID = ? ";

		Object[] values = {
			userId
		};

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.query(conn, ps, sql, values);
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
		user.setDefaultReportGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
		user.setPasswordAlgorithm(rs.getString("PASSWORD_ALGORITHM"));
		user.setStartReport(rs.getString("START_QUERY"));
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

		String sql = "SELECT AUG.USER_GROUP_ID, AUG.NAME, AUG.DESCRIPTION,"
				+ " AUG.DEFAULT_QUERY_GROUP, AUG.START_QUERY "
				+ " FROM ART_USER_GROUPS AUG"
				+ " INNER JOIN ART_USER_GROUP_ASSIGNMENT AUGA "
				+ " ON AUGA.USER_GROUP_ID=AUG.USER_GROUP_ID"
				+ " WHERE AUGA.USER_ID=?"
				+ " ORDER BY AUG.USER_GROUP_ID"; //have order by so that effective values are deterministic

		Object[] values = {
			user.getUserId()
		};

		try {
			int effectiveDefaultReportGroup = user.getDefaultReportGroup();
			String effectiveStartReport = user.getStartReport();

			List<UserGroup> groups = new ArrayList<UserGroup>();

			rs = DbUtils.query(conn, ps, sql, values);
			while (rs.next()) {
				UserGroup group = new UserGroup();

				group.setUserGroupId(rs.getInt("USER_GROUP_ID"));
				group.setName(rs.getString("NAME"));
				group.setDescription(rs.getString("DESCRIPTION"));
				group.setDefaultReportGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
				group.setStartReport(rs.getString("START_QUERY"));

				groups.add(group);

				if (effectiveDefaultReportGroup <= 0) {
					effectiveDefaultReportGroup = group.getDefaultReportGroup();
				}
				if (StringUtils.isBlank(effectiveStartReport)) {
					effectiveStartReport = group.getStartReport();
				}
			}

			user.setUserGroups(groups);

			user.setEffectiveDefaultReportGroup(effectiveDefaultReportGroup);
			user.setEffectiveStartReport(effectiveStartReport);
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
	@Cacheable(value = "users")
	public List<User> getAllUsers() throws SQLException {
		List<User> users = new ArrayList<User>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_SELECT_ALL_USERS;

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.query(conn, ps, sql);
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
	@CacheEvict(value = "users", allEntries = true)
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
	 * @param newPassword password hash
	 * @param passwordAlgorithm
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public void updatePassword(int userId, String newPassword, String passwordAlgorithm) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;

		String sql = "UPDATE ART_USERS SET PASSWORD=?, UPDATE_DATE=?,"
				+ " PASSWORD_ALGORITHM=?"
				+ " WHERE USER_ID = ?";

		Object[] values = {
			newPassword,
			DbUtils.getCurrentTimeStamp(),
			passwordAlgorithm,
			userId
		};

		try {
			conn = ArtConfig.getConnection();
			int affectedRows = DbUtils.update(conn, ps, sql, values);
			if (affectedRows == 0) {
				logger.warn("Update password - no rows affected. User ID={}", userId);
			}
		} finally {
			DbUtils.close(ps, conn);
		}
	}

	/**
	 * Add a new user to the database
	 *
	 * @param user
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public void addUser(User user) throws SQLException {
		int newId = allocateNewId();
		if (newId > 0) {
			user.setUserId(newId);
			saveUser(user, true);
		} else {
			logger.warn("User not added. Allocate new ID failed. Username='{}'", user.getUsername());
		}
	}

	/**
	 * Update an existing user record
	 *
	 * @param user
	 * @throws SQLException
	 */
	@CacheEvict(value = "users", allEntries = true)
	public void updateUser(User user) throws SQLException {
		saveUser(user, false);
	}

	/**
	 * Generate a user id and user record for a new user
	 *
	 * @return new user id generated, 0 otherwise
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
			String sql = "SELECT MAX(USER_ID) FROM ART_USERS";
			rs = DbUtils.query(conn, ps, sql);
			if (rs.next()) {
				newId = rs.getInt(1) + 1;

				//add dummy record with new id. fill all not null columns
				//username has unique constraint
				String allocatingUsername = "allocating-" + RandomStringUtils.randomAlphanumeric(3);
				sql = "INSERT INTO ART_USERS(USER_ID,USERNAME,PASSWORD)"
						+ " VALUES(?,?,'')";

				Object[] values = {
					newId,
					allocatingUsername
				};

				int affectedRows = DbUtils.update(conn, psInsert, sql, values);
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

	/**
	 * Save a user
	 *
	 * @param user
	 * @param newUser true if this is a new user, false if we are updating an
	 * existing user
	 * @throws SQLException
	 */
	private void saveUser(User user, boolean newUser) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;

		String dateColumn;

		if (newUser) {
			dateColumn = "CREATION_DATE";
		} else {
			dateColumn = "UPDATE_DATE";
		}

		final String SQL_UPDATE_USER = "UPDATE ART_USERS SET USERNAME=?, PASSWORD=?,"
				+ " PASSWORD_ALGORITHM=?, FULL_NAME=?, EMAIL=?,"
				+ " ACCESS_LEVEL=?, DEFAULT_QUERY_GROUP=?, START_QUERY=?,"
				+ " CAN_CHANGE_PASSWORD=?, ACTIVE=?";

		String sql = SQL_UPDATE_USER + "," + dateColumn + "=?"
				+ " WHERE USER_ID=?";

		try {
			conn = ArtConfig.getConnection();
			Integer accessLevel = null;
			if (user.getAccessLevel() != null) {
				accessLevel = Integer.valueOf(user.getAccessLevel().getValue());
			}

			Object[] values = {
				user.getUsername(),
				user.getPassword(),
				user.getPasswordAlgorithm(),
				user.getFullName(),
				user.getEmail(),
				accessLevel,
				user.getDefaultReportGroup(),
				user.getStartReport(),
				user.isCanChangePassword(),
				user.isActive(),
				DbUtils.getCurrentTimeStamp(),
				user.getUserId()
			};
			int affectedRows = DbUtils.update(conn, ps, sql, values);
			if (affectedRows == 0) {
				logger.warn("Save user - no rows affected. Username='{}'. newUser={}", user.getUsername(), newUser);
			}

			//save user groups. delete all existing records and recreate
			sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USER_ID=?";
			values = new Object[]{
				user.getUserId()
			};
			DbUtils.update(conn, ps, sql, values);

			//insert records afresh
			List<UserGroup> groups = user.getUserGroups();
			if (groups != null && !groups.isEmpty()) {
				List<Integer> userGroupIds = new ArrayList<Integer>();
				for (UserGroup group : user.getUserGroups()) {
					if (group != null && group.getUserGroupId() > 0) {
						userGroupIds.add(group.getUserGroupId());
					}
				}

				if (!userGroupIds.isEmpty()) {
					sql = "INSERT INTO ART_USER_GROUP_ASSIGNMENT (USER_ID,USERNAME,USER_GROUP_ID)"
							+ " VALUES(?,?,?)";
					ps = conn.prepareStatement(sql);

					List<Object> valuesList = new ArrayList<Object>();
					for (Integer id : userGroupIds) {
						valuesList.clear();
						valuesList.add(user.getUserId());
						valuesList.add(user.getUsername());
						valuesList.add(id);

						DbUtils.setValues(ps, valuesList.toArray());
						ps.addBatch();
					}

					ps.executeBatch();
				}
			}

		} finally {
			DbUtils.close(ps, conn);
		}
	}
}
