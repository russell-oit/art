package art.reportgroup;

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
 * Class to provide methods related to report groups
 *
 * @author Timothy Anyona
 */
@Service
public class ReportGroupService {

	final static Logger logger = LoggerFactory.getLogger(ReportGroupService.class);

	/**
	 * Get report groups that are available for selection for a given user
	 *
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	public List<ReportGroup> getAvailableReportGroups(String username) throws SQLException {
		List<ReportGroup> groups = new ArrayList<ReportGroup>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;

			//union will return distinct results
			//get groups that user has explicit rights to see
			sql = "SELECT aqg.QUERY_GROUP_ID, aqg.NAME, aqg.DESCRIPTION "
					+ " FROM ART_USER_QUERY_GROUPS auqg , ART_QUERY_GROUPS aqg "
					+ " WHERE auqg.USERNAME = ? "
					+ " AND auqg.QUERY_GROUP_ID = aqg.QUERY_GROUP_ID "
					+ " UNION "
					//add groups to which the user has access through his user group 
					+ " SELECT AQG.QUERY_GROUP_ID, AQG.NAME, AQG.DESCRIPTION "
					+ " FROM ART_USER_GROUP_GROUPS AUGG, ART_QUERY_GROUPS AQG "
					+ " WHERE AUGG.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
					+ " AND EXISTS (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA "
					+ " WHERE AUGA.USERNAME = ? AND AUGA.USER_GROUP_ID = AUGG.USER_GROUP_ID)"
					+ " UNION "
					//add groups where user has right to query but not to group
					+ " SELECT AQG.QUERY_GROUP_ID, AQG.NAME, AQG.DESCRIPTION "
					+ " FROM ART_USER_QUERIES AUQ, ART_QUERIES AQ, ART_QUERY_GROUPS AQG "
					+ " WHERE AUQ.QUERY_ID=AQ.QUERY_ID AND AQ.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
					+ " AND AUQ.USERNAME = ? AND AQG.QUERY_GROUP_ID<>0"
					+ " AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120"
					+ " UNION "
					//add groups where user's group has rights to the query
					+ " SELECT AQG.QUERY_GROUP_ID, AQG.NAME, AQG.DESCRIPTION "
					+ " FROM ART_USER_GROUP_QUERIES AUGQ, ART_QUERIES AQ, ART_QUERY_GROUPS AQG "
					+ " WHERE AUGQ.QUERY_ID=AQ.QUERY_ID AND AQ.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID "
					+ " AND AQG.QUERY_GROUP_ID<>0 AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120 "
					+ " AND EXISTS (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA "
					+ " WHERE AUGA.USERNAME = ? AND AUGA.USER_GROUP_ID = AUGQ.USER_GROUP_ID)";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, username);
			ps.setString(3, username);
			ps.setString(4, username);

			rs = ps.executeQuery();
			while (rs.next()) {
				ReportGroup group = new ReportGroup();

				group.setReportGroupId(rs.getInt("QUERY_GROUP_ID"));
				group.setName(rs.getString("NAME"));
				group.setDescription(rs.getString("DESCRIPTION"));

				groups.add(group);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return groups;
	}

}
