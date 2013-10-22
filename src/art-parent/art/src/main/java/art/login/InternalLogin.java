package art.login;

import art.servlets.ArtConfig;
import art.utils.DbUtils;
import art.utils.Encrypter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to authenticate users using ART's database
 *
 * @author Timothy Anyona
 */
public class InternalLogin {

	final static Logger logger = LoggerFactory.getLogger(InternalLogin.class);

	public static LoginResult authenticate(String username, String password) {
		LoginResult result = new LoginResult();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();

			String sql = "SELECT PASSWORD, HASHING_ALGORITHM, ACCESS_LEVEL, ACTIVE_STATUS "
					+ " FROM ART_USERS "
					+ " WHERE USERNAME = ?";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);

			rs = ps.executeQuery();
			if (rs.next()) {
				if (StringUtils.equalsIgnoreCase(rs.getString("ACTIVE_STATUS"), "A")) {
					if (Encrypter.VerifyPassword(password, rs.getString("PASSWORD"), rs.getString("HASHING_ALGORITHM"))) {
						result.setAuthenticated(true);
					} else {
						//invalid password
						result.setMessage("login.message.invalidPassword");
						result.setMessageDetails("invalid password");
					}
				} else {
					//user disabled
					result.setMessage("login.message.userDisabled");
					result.setMessageDetails("user disabled");
				}
			} else {
				//invalid username
				result.setMessage("login.message.invalidUsername");
				result.setMessageDetails("invalid username");
			}
		} catch (Exception e) {
			logger.error("Error. Username={}", username, e);
			
			result.setMessage("login.message.errorOccurred");
			result.setMessageDetails(e.getMessage());
			result.setError(e.toString());
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return result;
	}
}
