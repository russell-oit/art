package art.login;

import art.servlets.ArtConfig;
import art.utils.DbUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to authenticate user via database
 *
 * @author Timothy Anyona
 */
public class DbLogin {

	final static Logger logger = LoggerFactory.getLogger(DbLogin.class);

	public static boolean authenticate(String username, String password) {
		boolean authenticated = false;


		String url = ArtConfig.getArtSetting("jdbc_auth_url");
		if (StringUtils.isBlank(url)) {
			logger.info("Database authentication url not set. Username={}", username);
		} else {
			try {
				Connection conn = DriverManager.getConnection(url, username, password);
				// If no exception has been raised at this point 
				// the authentication is successful
				authenticated = true;
				DbUtils.closeConnection(conn);
			} catch (SQLException ex) {
				logger.error("Error. Username={}", username, ex);
			}
		}

		return authenticated;
	}
}
