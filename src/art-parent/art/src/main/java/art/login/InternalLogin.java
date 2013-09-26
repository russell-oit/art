/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author Timothy Anyona
 */
public class InternalLogin {

	final static Logger logger = LoggerFactory.getLogger(InternalLogin.class);

	public static boolean authenticate(String username, String password) {
		boolean authenticated = false;
		if (StringUtils.equals(username, ArtConfig.getArtRepositoryUsername()) && StringUtils.equals(password, ArtConfig.getArtRepositoryPassword()) && StringUtils.isNotBlank(username)) {
			//repository user
			authenticated = true;
		} else {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				String SqlQuery = "SELECT PASSWORD, HASHING_ALGORITHM, ACCESS_LEVEL"
						+ " FROM ART_USERS "
						+ " WHERE USERNAME = ? AND ACTIVE_STATUS = 'A'";
				conn = ArtConfig.getConnection();
				if (conn == null) {
					logger.warn("Connection to the ART Repository is not available.");
				} else {
					ps = conn.prepareStatement(SqlQuery);
					ps.setString(1, username);
					rs = ps.executeQuery();
					if (rs.next()) {
						if (Encrypter.VerifyPassword(password, rs.getString("PASSWORD"), rs.getString("HASHING_ALGORITHM"))) {
							authenticated = true;
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error. Username={}", username, e);
			} finally {
				DbUtils.close(rs, ps, conn);
			}
		}
		
		return authenticated;
	}
}
