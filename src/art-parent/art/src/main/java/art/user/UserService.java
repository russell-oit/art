package art.user;

import art.enums.AccessLevel;
import art.servlets.ArtConfig;
import art.utils.ArtJob;
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
	final String ALL_USERS_SQL = "SELECT USERNAME, EMAIL, ACCESS_LEVEL, FULL_NAME, "
			+ " ACTIVE, PASSWORD, DEFAULT_QUERY_GROUP, "
			+ " HASHING_ALGORITHM, START_QUERY "
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

		try {
			conn = ArtConfig.getConnection();
			String sql;

			sql = ALL_USERS_SQL + " WHERE USERNAME = ? ";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);

			rs = ps.executeQuery();
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

		try {
			conn = ArtConfig.getConnection();
			String sql;

			sql = ALL_USERS_SQL;

			ps = conn.prepareStatement(sql);

			rs = ps.executeQuery();
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
	 * @param username
	 * @throws SQLException 
	 */
	public void deleteUser(String username) throws SQLException{
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			conn = ArtConfig.getConnection();
			String sql;
			
			//delete user-report relationships
			sql = "DELETE FROM ART_USER_QUERIES WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.executeUpdate();

			//delete user-report group relationships
			sql = "DELETE FROM ART_USER_QUERY_GROUPS WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.executeUpdate();

			//delete user-rules relationships
			sql = "DELETE FROM ART_USER_RULES WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.executeUpdate();

			//delete user-user group relationships
			sql = "DELETE FROM ART_USER_GROUP_ASSIGNMENT WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.executeUpdate();

			//delete user-shared job relationships
			sql = "DELETE FROM ART_USER_JOBS WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.executeUpdate();

			//delete user's jobs. this will delete all records related to the job e.g. quartz records, job parameters etc
			sql = "SELECT JOB_ID FROM ART_JOBS WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			
			rs = ps.executeQuery();
			while (rs.next()) {
				ArtJob aj = new ArtJob();
				aj.load(rs.getInt("JOB_ID"), username);
				aj.delete();
			}

			//lastly, delete user
			sql = "DELETE FROM ART_USERS WHERE USERNAME=?";
			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.executeUpdate();
			
		} finally {
			DbUtils.close(rs,ps, conn);
		}
	}
}
