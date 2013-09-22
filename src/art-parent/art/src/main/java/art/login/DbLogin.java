package art.login;

import art.utils.DbUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class DbLogin {

	final static Logger logger = LoggerFactory.getLogger(DbLogin.class);

	public static boolean authenticate(String url, String username, String password) {
		boolean success = false;
		
		try {
			Connection conn = DriverManager.getConnection(url, username, password);
			// If no exception has been raised at this point 
			// the authentication is successful
			success = true;
			DbUtils.closeConnection(conn);
		} catch (SQLException ex) {
			logger.error("DB Login Error: username={}", username, ex);
		}

		return success;
	}
}
