/**
 * Copyright (C) 2013 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.report;

import art.servlets.ArtConfig;
import art.dbutils.DbUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to reports
 *
 * @author Timothy Anyona
 */
@Service
public class ReportService {

	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
	final String SQL_SELECT="SELECT AQ.QUERY_ID, AQ.NAME AS QUERY_NAME,"
			+ " AQ.SHORT_DESCRIPTION, AQ.DESCRIPTION, AQ.QUERY_TYPE, AQ.QUERY_GROUP_ID,"
			+ " AQG.NAME AS GROUP_NAME, AQ.DATABASE_ID, AD.NAME AS DATASOURCE_NAME,"
			+ " AQ.CONTACT_PERSON, AQ.USES_RULES, AQ.REPORT_STATUS, AQ.SHOW_PARAMETERS,"
			+ " AQ.X_AXIS_LABEL, AQ.Y_AXIS_LABEL, AQ.GRAPH_OPTIONS, AQ.TEMPLATE,"
			+ " AQ.DISPLAY_RESULTSET, AQ.XMLA_URL, AQ.XMLA_CATALOG,"
			+ " AQ.XMLA_USERNAME, AQ.XMLA_PASSWORD, AQ.CREATION_DATE, AQ.UPDATE_DATE"
			+ " FROM ART_QUERIES AQ"
			+ " LEFT JOIN ART_QUERY_GROUPS AQG ON"
			+ " AQ.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID"
			+ " LEFT JOIN ART_DATABASES AD ON" 
			+ " AQ.DATABASE_ID=AD.DATABASE_ID";

	/**
	 * Get the reports that a user can access from the reports page. Excludes
	 * disabled reports and some report types e.g. lovs
	 *
	 * @param username
	 * @return list of available reports, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public List<AvailableReport> getAvailableReports(String username) throws SQLException {
		List<AvailableReport> reports = new ArrayList<AvailableReport>();

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = ArtConfig.getConnection();

			String sql;

			//union will return distinct results
			//only show active reports
			//don't show dynamic lov(119), static lov(120), dynamic job recipient(121) reports
			sql = "SELECT AQ.QUERY_ID, AQ.NAME AS QUERY_NAME, AQ.DESCRIPTION,"
					+ " AQ.UPDATE_DATE, AQ.QUERY_GROUP_ID, AQG.NAME AS GROUP_NAME,"
					+ " AQ.CREATION_DATE"
					+ " FROM ART_QUERIES AQ "
					+ " INNER JOIN ART_QUERY_GROUPS AQG "
					+ " ON AQ.QUERY_GROUP_ID = AQG.QUERY_GROUP_ID "
					+ " WHERE AQ.REPORT_STATUS = 'Active' "
					+ " AND AQ.QUERY_TYPE<>119 AND AQ.QUERY_TYPE<>120 AND AQ.QUERY_TYPE<>121 "
					+ " AND AQ.QUERY_ID IN ("
					//get reports that user has explicit rights to see
					+ " SELECT AQ.QUERY_ID "
					+ " FROM ART_USER_QUERIES AUQ, ART_QUERIES AQ "
					+ " WHERE AUQ.QUERY_ID = AQ.QUERY_ID "
					+ " AND AUQ.USERNAME=? "
					+ " UNION ALL"
					//add reports to which the user has access through his user group 
					+ " SELECT AQ.QUERY_ID "
					+ " FROM ART_USER_GROUP_QUERIES AUGQ, ART_QUERIES AQ "
					+ " WHERE AUGQ.QUERY_ID = AQ.QUERY_ID "
					+ " AND EXISTS "
					+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ?"
					+ " AND AUGA.USER_GROUP_ID=AUGQ.USER_GROUP_ID)"
					+ " UNION ALL"
					// user can run all reports in the report groups he has direct access to
					+ " SELECT AQ.QUERY_ID "
					+ " FROM ART_USER_QUERY_GROUPS AUQG, ART_QUERIES AQ "
					+ " WHERE AUQG.QUERY_GROUP_ID = AQ.QUERY_GROUP_ID "
					+ " AND AUQG.USERNAME=? "
					+ " UNION ALL"
					//user can run all reports in the report groups that his user groups have access to
					+ " SELECT AQ.QUERY_ID "
					+ " FROM ART_USER_GROUP_GROUPS AUGG, ART_QUERIES AQ "
					+ " WHERE AUGG.QUERY_GROUP_ID = AQ.QUERY_GROUP_ID "
					+ " AND EXISTS "
					+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ?"
					+ " AND AUGA.USER_GROUP_ID=AUGG.USER_GROUP_ID)"
					+ " )";

			ps = conn.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, username);
			ps.setString(3, username);
			ps.setString(4, username);

			rs = ps.executeQuery();
			while (rs.next()) {
				AvailableReport report = new AvailableReport();

				report.setReportId(rs.getInt("QUERY_ID"));
				report.setName(rs.getString("QUERY_NAME"));
				report.setDescription(rs.getString("DESCRIPTION"));
				report.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
				report.setReportGroupId(rs.getInt("QUERY_GROUP_ID"));
				report.setReportGroupName(rs.getString("GROUP_NAME"));
				report.setCreationDate(rs.getTimestamp("CREATION_DATE"));

				reports.add(report);
			}
		} finally {
			DbUtils.close(rs, ps, conn);
		}

		return reports;
	}
	
	public List<Report> getAllReports() throws SQLException{
		List<Report> reports=new ArrayList<Report>();
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = SQL_SELECT;
		
		try{
			conn = ArtConfig.getConnection();
			rs = DbUtils.query(conn, ps, sql);
			while (rs.next()) {
				Report report=new Report();
				
				reports.add(report);
			}
		} finally{
			DbUtils.close(rs, ps, conn);
		}
		
		return reports;
	}

}
