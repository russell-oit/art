/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.servlets.ArtConfig;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for helper methods. Can have dependencies on other art classes
 *
 * @author Timothy Anyona
 */
public class ArtHelper {

	final static Logger logger = LoggerFactory.getLogger(ArtHelper.class);

	/**
	 * Authenticate the session.
	 *
	 * @param request
	 * @throws ArtException if couldn't authenticate
	 * @throws Exception
	 */
	public static String authenticateSession(HttpServletRequest request) throws Exception {
		String msg = null;
		HttpSession session = request.getSession();
		ResourceBundle messages = ResourceBundle.getBundle("art.i18n.ArtMessages", request.getLocale());
		int accessLevel = -1;
		boolean internalAuthentication = true;
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (username != null && password != null) {
			if (StringUtils.equals(username, ArtConfig.getArtRepositoryUsername()) && StringUtils.equals(password, ArtConfig.getArtRepositoryPassword()) && StringUtils.isNotBlank(username)) {
				accessLevel = 100;
				log(username, "login", request.getRemoteAddr(), "internal-superadmin, level: " + accessLevel);
			} else {
				Connection conn = null;
				PreparedStatement ps = null;
				ResultSet rs = null;
				try {
					String SqlQuery = "SELECT ACCESS_LEVEL, PASSWORD, HASHING_ALGORITHM FROM ART_USERS " + "WHERE USERNAME = ? AND (ACTIVE_STATUS = 'A' OR ACTIVE_STATUS IS NULL)";
					conn = ArtConfig.getConnection();
					if (conn == null) {
						msg = messages.getString("invalidConnection");
					} else {
						ps = conn.prepareStatement(SqlQuery);
						ps.setString(1, username);
						rs = ps.executeQuery();
						if (rs.next()) {
							if (Encrypter.VerifyPassword(password, rs.getString("PASSWORD"), rs.getString("HASHING_ALGORITHM"))) {
								accessLevel = rs.getInt("ACCESS_LEVEL");
								session.setAttribute("username", username);
								log(username, "login", request.getRemoteAddr(), "internal, level: " + accessLevel);
							} else {
								log(username, "loginerr", request.getRemoteAddr(), "internal, failed");
								msg = messages.getString("invalidAccount");
							}
						} else {
							log(username, "loginerr", request.getRemoteAddr(), "internal, failed");
							msg = messages.getString("invalidAccount");
						}
					}
				} finally {
					DbUtils.close(rs, ps, conn);
				}
			}
		} else if (session.getAttribute("username") != null) {
			Connection conn = null;
			PreparedStatement ps = null;
			ResultSet rs = null;
			try {
				username = (String) session.getAttribute("username");
				String SqlQuery = "SELECT ACCESS_LEVEL FROM ART_USERS " + " WHERE USERNAME = ? AND (ACTIVE_STATUS = 'A' OR ACTIVE_STATUS IS NULL) ";
				conn = ArtConfig.getConnection();
				if (conn == null) {
					msg = messages.getString("invalidConnection");
				} else {
					ps = conn.prepareStatement(SqlQuery);
					ps.setString(1, username);
					rs = ps.executeQuery();
					if (rs.next()) {
						accessLevel = rs.getInt("ACCESS_LEVEL");
						internalAuthentication = false;
						log(username, "login", request.getRemoteAddr(), "external, level: " + accessLevel);
					} else {
						log(username, "loginerr", request.getRemoteAddr(), "external, failed");
						msg = messages.getString("invalidUser");
					}
				}
			} finally {
				DbUtils.close(rs, ps, conn);
			}
		} else {
			if (request.getParameter("_public_user") != null) {
				username = "public_user";
				accessLevel = 0;
				internalAuthentication = true;
			} else {
				msg = messages.getString("sessionExpired");
			}
		}
		if (msg == null) {
			UserEntity ue = new UserEntity(username);
			ue.setAccessLevel(accessLevel);
			ue.setInternalAuth(internalAuthentication);
			session.setAttribute("ue", ue);
			session.setAttribute("username", username);
			if (accessLevel >= 10) {
				session.setAttribute("AdminSession", "Y");
				session.setAttribute("AdminLevel", Integer.valueOf(accessLevel));
				session.setAttribute("AdminUsername", username);
			}
		}
		return msg;
	}

	/**
	 * Get language file to use for datatables, depending on the locale
	 *
	 * @param request
	 * @return
	 */
	public static String getDataTablesLanguageUrl(HttpServletRequest request) {
		String url = "";
		String languageFileName = "dataTables." + request.getLocale().toString() + ".txt";
		String sep = File.separator;
		String languageFilePath = ArtConfig.getAppPath() + sep + "js" + sep + languageFileName;
		File languageFile = new File(languageFilePath);
		if (languageFile.exists()) {
			url = request.getContextPath() + "/js/" + languageFileName;
		}
		return url;
	}

	/**
	 * Log some action to the ART_LOGS table.
	 *
	 * @param user username of user who executed the query
	 * @param type type of event
	 * @param ip ip address from which query was run
	 * @param queryId id of the query that was run
	 * @param totalTime total time to execute the query and display the results
	 * @param fetchTime time to fetch the results from the database
	 * @param message log message
	 */
	public static void log(String user, String type, String ip, int queryId, long totalTime, long fetchTime, String message) {
		if (StringUtils.length(message) > 4000) {
			message = message.substring(0, 4000);
		}

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			Timestamp now = new Timestamp(new Date().getTime());
			conn = ArtConfig.getConnection();
			String sql = "INSERT INTO ART_LOGS"
					+ " (UPDATE_TIME, USERNAME, LOG_TYPE, IP, QUERY_ID,"
					+ " TOTAL_TIME, FETCH_TIME, MESSAGE) "
					+ " VALUES (?,?,?,?,?,?,?,?) ";

			ps = conn.prepareStatement(sql);
			ps.setTimestamp(1, now);
			ps.setString(2, user);
			ps.setString(3, type);
			ps.setString(4, ip);
			ps.setInt(5, queryId);
			ps.setInt(6, (int) totalTime);
			ps.setInt(7, (int) fetchTime);
			ps.setString(8, message);

			ps.executeUpdate();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			DbUtils.close(ps, conn);
		}
	}

	/**
	 * Log login attempts to the ART_LOGS table.
	 *
	 * @param user username
	 * @param type "login" if successful or "loginerr" if not
	 * @param ip ip address from which login was done or attempted
	 * @param message log message
	 */
	public static void log(String user, String type, String ip, String message) {
		if (StringUtils.length(message) > 4000) {
			message = message.substring(0, 4000);
		}

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			Timestamp now = new Timestamp(new Date().getTime());
			conn = ArtConfig.getConnection();
			String sql = "INSERT INTO ART_LOGS"
					+ " (UPDATE_TIME, USERNAME, LOG_TYPE, IP, MESSAGE) "
					+ " VALUES (?,?,?,?,?) ";
			ps = conn.prepareStatement(sql);

			ps.setTimestamp(1, now);
			ps.setString(2, user);
			ps.setString(3, type);
			ps.setString(4, ip);
			ps.setString(5, message);

			ps.executeUpdate();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			DbUtils.close(ps, conn);
		}
	}
}
