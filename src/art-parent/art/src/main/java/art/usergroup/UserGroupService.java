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
package art.usergroup;

import art.servlets.ArtConfig;
import art.utils.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to user groups
 *
 * @author Timothy Anyona
 */
@Service
public class UserGroupService {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupService.class);

	/**
	 * Get all user groups
	 *
	 * @return list of all user groups, empty list otherwise
	 * @throws SQLException
	 */
	public List<UserGroup> getAllUserGroups() throws SQLException {
		List<UserGroup> groups = new ArrayList<UserGroup>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "SELECT AUG.USER_GROUP_ID, AUG.NAME, AUG.DESCRIPTION,"
				+ " AUG.DEFAULT_QUERY_GROUP, AUG.START_QUERY "
				+ " FROM ART_USER_GROUPS AUG";

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.executeQuery(conn, ps, sql);
			while (rs.next()) {
				UserGroup group = new UserGroup();

				group.setUserGroupId(rs.getInt("USER_GROUP_ID"));
				group.setName(rs.getString("NAME"));
				group.setDescription(rs.getString("DESCRIPTION"));
				group.setDefaultReportGroup(rs.getInt("DEFAULT_QUERY_GROUP"));
				group.setStartReport(rs.getString("START_QUERY"));

				groups.add(group);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return groups;
	}

}
