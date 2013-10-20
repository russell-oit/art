package art.user;

import art.servlets.ArtConfig;
import art.utils.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * Class for data access methods for users
 *
 * @author Timothy Anyona
 */
@Repository
public class UserRepository {

	final static Logger logger = LoggerFactory.getLogger(UserRepository.class);

	public User getUser(String username) {
		User user = null;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();
			String sql;

			sql = "SELECT EMAIL,ACCESS_LEVEL,FULL_NAME,ACTIVE_STATUS,"
					+ " PASSWORD,DEFAULT_QUERY_GROUP,CAN_CHANGE_PASSWORD, "
					+ " HASHING_ALGORITHM, START_QUERY "
					+ " FROM ART_USERS "
					+ " WHERE USERNAME = ? ";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);

			rs = ps.executeQuery();
			if (rs.next()) {
				user = new User();

				user.setUsername(username);
				user.setEmail(rs.getString("EMAIL"));
				user.setAccessLevel(rs.getInt("ACCESS_LEVEL"));
				user.setFullName(rs.getString("FULL_NAME"));
				user.setActiveStatus(rs.getString("ACTIVE_STATUS"));
				user.setPassword(rs.getString("PASSWORD"));
				user.setDefaultQueryGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
				user.setCanChangePassword(rs.getString("CAN_CHANGE_PASSWORD"));
				user.setHashingAlgorithm(rs.getString("HASHING_ALGORITHM"));
				user.setStartQuery(rs.getString("START_QUERY"));

				//set user properties whose values may come from user groups
				populateGroupValues(conn, user);
			}
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return user;
	}

	/**
	 * Set user properties whose values may come from user groups
	 *
	 * @param conn
	 * @param user
	 */
	private void populateGroupValues(Connection conn, User user) {
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
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			DbUtils.close(rs, ps);
		}
	}
}
