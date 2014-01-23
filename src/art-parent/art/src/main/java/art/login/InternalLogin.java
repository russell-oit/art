package art.login;

import art.servlets.ArtConfig;
import art.utils.DbUtils;
import art.utils.Encrypter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		logger.debug("Entering authenticate: username='{}'", username);
		
		LoginResult result = new LoginResult();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();
			
			String sql = "SELECT PASSWORD, PASSWORD_ALGORITHM, ACCESS_LEVEL, ACTIVE "
					+ " FROM ART_USERS "
					+ " WHERE USERNAME = ?";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);

			rs = ps.executeQuery();
			if (rs.next()) {
				boolean active=rs.getBoolean("ACTIVE");
				
				logger.debug("active={}", active);
				
				if (active) {
					boolean passwordVerified = false;
					try {
						passwordVerified = Encrypter.VerifyPassword(password, rs.getString("PASSWORD"), rs.getString("PASSWORD_ALGORITHM"));
					} catch (Exception ex) {
						logger.error("Error. username='{}'", username, ex);
					}
					
					logger.debug("passwordVerified={}", passwordVerified);
					
					if (passwordVerified) {
						result.setAuthenticated(true);
					} else {
						//invalid password
						result.setMessage("login.message.invalidCredentials");
						result.setDetails("invalid password");
					}
				} else {
					//user disabled
					result.setMessage("login.message.userDisabled");
					result.setDetails("user disabled");
				}
			} else {
				logger.debug("No records returned");
				
				//user doesn't exist
				result.setMessage("login.message.invalidUser");
				result.setDetails("invalid user");
			}
		} catch (SQLException ex) {
			logger.error("Error. username='{}'", username, ex);

			result.setMessage("page.message.errorOccurred");
			result.setDetails(ex.getMessage());
			result.setError(ex.toString());
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		logger.debug("Leaving authenticate: {}", result);
		
		return result;
	}
}
