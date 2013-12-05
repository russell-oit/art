package art.login;

import art.servlets.ArtConfig;
import art.utils.DbUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to authenticate user using a database (using users that are allowed to
 * connect to the database)
 *
 * @author Timothy Anyona
 */
public class DbLogin {

	final static Logger logger = LoggerFactory.getLogger(DbLogin.class);

	public static LoginResult authenticate(String username, String password) {
		logger.debug("Entering authenticate: username='{}'", username);

		LoginResult result = new LoginResult();

		String url = ArtConfig.getSettings().getDatabaseAuthenticationUrl();

		logger.debug("Url='{}'", url);

		if (StringUtils.isBlank(url)) {
			logger.info("Database authentication not configured. username={}", username);

			result.setMessage("login.message.databaseAuthenticationNotConfigured");
			result.setDetails("database authentication not configured");

			logger.debug("Leaving authenticate: {}", result);
			return result;
		}
		
		logger.debug("Starting main block");
		
		try {
			Connection conn = DriverManager.getConnection(url, username, password);

			//if we are here, authentication is successful
			result.setAuthenticated(true);
			DbUtils.close(conn);
		} catch (SQLException ex) {
			logger.error("Error. username='{}'", username, ex);

			result.setMessage("login.message.invalidCredentials");
			result.setDetails(ex.getMessage());
			result.setError(ex.toString());
		}

		logger.debug("Leaving authenticate: {}", result);
		
		return result;
	}
}
