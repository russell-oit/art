/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
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

import art.dbutils.DbUtils;
import art.servlets.ArtConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;

/**
 * Class with methods to perform upgrade steps, additional to running of upgrade
 * scripts
 *
 * @author Timothy Anyona
 */
public class UpgradeHelper {

	final static org.slf4j.Logger logger = LoggerFactory.getLogger(UpgradeHelper.class);

	/**
	 * Perform upgrade steps
	 * @throws java.sql.SQLException
	 */
	public void upgrade() throws SQLException {
		logger.info("Performing additional upgrade steps...");
		addUserIds();
		logger.info("Done performing additional upgrade steps");
	}

	/**
	 * Populate user_id columns. Columns added in 3.0
	 */
	private void addUserIds() throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql;
		PreparedStatement psUpdate = null;

		try {
			conn = ArtConfig.getConnection();

			sql = "SELECT USER_ID, USERNAME FROM ART_USERS WHERE USER_ID IS NULL";
			ps = conn.prepareStatement(sql);

			rs = ps.executeQuery();
			int count = 0;
			while (rs.next()) {
				count++;
				String username = rs.getString("USERNAME");

				sql = "UPDATE ART_USERS SET USER_ID=? WHERE USERNAME=?";
				psUpdate = conn.prepareStatement(sql);
				psUpdate.setInt(1, count);
				psUpdate.setString(2, username);
				psUpdate.executeUpdate();

				sql = "UPDATE ART_ADMIN_PRIVILEGES SET USER_ID=? WHERE USERNAME=?";
				psUpdate = conn.prepareStatement(sql);
				psUpdate.setInt(1, count);
				psUpdate.setString(2, username);
				psUpdate.executeUpdate();

				sql = "UPDATE ART_USER_QUERIES SET USER_ID=? WHERE USERNAME=?";
				psUpdate = conn.prepareStatement(sql);
				psUpdate.setInt(1, count);
				psUpdate.setString(2, username);
				psUpdate.executeUpdate();

				sql = "UPDATE ART_USER_QUERY_GROUPS SET USER_ID=? WHERE USERNAME=?";
				psUpdate = conn.prepareStatement(sql);
				psUpdate.setInt(1, count);
				psUpdate.setString(2, username);
				psUpdate.executeUpdate();

				sql = "UPDATE ART_USER_RULES SET USER_ID=? WHERE USERNAME=?";
				psUpdate = conn.prepareStatement(sql);
				psUpdate.setInt(1, count);
				psUpdate.setString(2, username);
				psUpdate.executeUpdate();

				sql = "UPDATE ART_JOBS SET USER_ID=? WHERE USERNAME=?";
				psUpdate = conn.prepareStatement(sql);
				psUpdate.setInt(1, count);
				psUpdate.setString(2, username);
				psUpdate.executeUpdate();

				sql = "UPDATE ART_LOGS SET USER_ID=? WHERE USERNAME=?";
				psUpdate = conn.prepareStatement(sql);
				psUpdate.setInt(1, count);
				psUpdate.setString(2, username);
				psUpdate.executeUpdate();

				sql = "UPDATE ART_USER_JOBS SET USER_ID=? WHERE USERNAME=?";
				psUpdate = conn.prepareStatement(sql);
				psUpdate.setInt(1, count);
				psUpdate.setString(2, username);
				psUpdate.executeUpdate();

				sql = "UPDATE ART_USER_GROUP_ASSIGNMENT SET USER_ID=? WHERE USERNAME=?";
				psUpdate = conn.prepareStatement(sql);
				psUpdate.setInt(1, count);
				psUpdate.setString(2, username);
				psUpdate.executeUpdate();

				sql = "UPDATE ART_JOB_ARCHIVES SET USER_ID=? WHERE USERNAME=?";
				psUpdate = conn.prepareStatement(sql);
				psUpdate.setInt(1, count);
				psUpdate.setString(2, username);
				psUpdate.executeUpdate();
			}
		} finally {
			DbUtils.close(psUpdate);
			DbUtils.close(rs, ps, conn);
		}
	}

}
