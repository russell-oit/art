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

import art.datasource.Datasource;
import art.dbutils.DbService;
import art.servlets.ArtConfig;
import art.dbutils.DbUtils;
import art.enums.ReportStatus;
import art.reportgroup.ReportGroup;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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

	@Autowired
	private DbService dbService;

	private final int SOURCE_CHUNK_LENGTH = 4000; //length of column that holds report source

	private final String SQL_SELECT_ALL = "SELECT AQ.*, "
			+ " AQG.NAME AS GROUP_NAME, AD.DATABASE_NAME AS DATASOURCE_NAME"
			+ " FROM ART_QUERIES AQ"
			+ " LEFT JOIN ART_QUERY_GROUPS AQG ON" //use left join so that all reports are returned
			+ " AQ.QUERY_GROUP_ID=AQG.QUERY_GROUP_ID"
			+ " LEFT JOIN ART_DATABASES AD ON"
			+ " AQ.DATABASE_ID=AD.DATABASE_ID";

	/**
	 * Class to map resultset to an object
	 */
	private class ReportMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<T>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			Report report = new Report();

			report.setReportId(rs.getInt("QUERY_ID"));
			report.setName(rs.getString("NAME"));
			report.setShortDescription(rs.getString("SHORT_DESCRIPTION"));
			report.setDescription(rs.getString("DESCRIPTION"));
			report.setReportType(rs.getInt("QUERY_TYPE"));

			ReportGroup reportGroup = new ReportGroup();
			reportGroup.setReportGroupId(rs.getInt("QUERY_GROUP_ID"));
			reportGroup.setName(rs.getString("GROUP_NAME"));
			report.setReportGroup(reportGroup);

			Datasource datasource = new Datasource();
			datasource.setDatasourceId(rs.getInt("DATABASE_ID"));
			datasource.setName(rs.getString("DATASOURCE_NAME"));
			report.setDatasource(datasource);

			report.setContactPerson(rs.getString("CONTACT_PERSON"));
			report.setUsesRules(rs.getBoolean("USES_RULES"));
			report.setReportStatus(ReportStatus.toEnum(rs.getString("REPORT_STATUS")));
			report.setShowParameters(rs.getBoolean("SHOW_PARAMETERS"));
			report.setxAxisLabel(rs.getString("X_AXIS_LABEL"));
			report.setyAxisLabel(rs.getString("Y_AXIS_LABEL"));
			report.setGraphOptions(rs.getString("GRAPH_OPTIONS"));
			report.setTemplate(rs.getString("TEMPLATE"));
			report.setDisplayResultset(rs.getInt("DISPLAY_RESULTSET"));
			report.setXmlaUrl(rs.getString("XMLA_URL"));
			report.setXmlaDatasource(rs.getString("XMLA_DATASOURCE"));
			report.setXmlaCatalog(rs.getString("XMLA_CATALOG"));
			report.setXmlaUsername(rs.getString("XMLA_USERNAME"));
			report.setXmlaPassword(rs.getString("XMLA_PASSWORD"));

			report.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			report.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));

			return type.cast(report);
		}
	}

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
					+ " LEFT JOIN ART_QUERY_GROUPS AQG "
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
					//add reports to which the user has access through his report 
					+ " SELECT AQ.QUERY_ID "
					+ " FROM ART_USER_GROUP_QUERIES AUGQ, ART_QUERIES AQ "
					+ " WHERE AUGQ.QUERY_ID = AQ.QUERY_ID "
					+ " AND EXISTS "
					+ " (SELECT * FROM ART_USER_GROUP_ASSIGNMENT AUGA WHERE AUGA.USERNAME = ?"
					+ " AND AUGA.USER_GROUP_ID=AUGQ.USER_GROUP_ID)"
					+ " UNION ALL"
					// user can run all reports in the report reports he has direct access to
					+ " SELECT AQ.QUERY_ID "
					+ " FROM ART_USER_QUERY_GROUPS AUQG, ART_QUERIES AQ "
					+ " WHERE AUQG.QUERY_GROUP_ID = AQ.QUERY_GROUP_ID "
					+ " AND AUQG.USERNAME=? "
					+ " UNION ALL"
					//user can run all reports in the report reports that his reports have access to
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

	/**
	 * Get all reports
	 *
	 * @return list of all reports, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public List<Report> getAllReports() throws SQLException {
		logger.debug("Entering getAllReports");

		ResultSetHandler<List<Report>> h = new BeanListHandler<Report>(Report.class, new ReportMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Get a report
	 *
	 * @param id
	 * @return populated object if report found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("reports")
	public Report getReport(int id) throws SQLException {
		logger.debug("Entering getReport: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE QUERY_ID = ? ";
		ResultSetHandler<Report> h = new BeanHandler<Report>(Report.class, new ReportMapper());
		Report report = dbService.query(sql, h, id);
		populateReportSource(report);
		return report;
	}

	/**
	 * Delete a report
	 *
	 * @param id
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void deleteReport(int id) throws SQLException {
		logger.debug("Entering deleteReport: id={}", id);

		Connection conn = null;
		PreparedStatement ps = null;
		String sql;

		try {
			conn = ArtConfig.getConnection();

			//delete query-user relationships
			sql = "DELETE FROM ART_USER_QUERIES WHERE QUERY_ID = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ps.executeUpdate();

			//delete query parameters
			sql = "DELETE FROM ART_QUERY_FIELDS WHERE QUERY_ID = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ps.executeUpdate();

			//delete sql source
			sql = "DELETE FROM ART_ALL_SOURCES WHERE OBJECT_ID = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ps.executeUpdate();

			//delete query-rule relationships
			sql = "DELETE FROM ART_QUERY_RULES WHERE QUERY_ID = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ps.executeUpdate();

			//delete drilldown queries
			sql = "DELETE FROM ART_DRILLDOWN_QUERIES WHERE QUERY_ID = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ps.executeUpdate();

			//lastly, delete query
			sql = "DELETE FROM ART_QUERIES WHERE QUERY_ID = ?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			ps.executeUpdate();
		} finally {
			DbUtils.close(ps, conn);
		}
	}

	/**
	 * Add a new report to the database
	 *
	 * @param report
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void addReport(Report report) throws SQLException {
		logger.debug("Entering addReport: report={}", report);

		int newId = allocateNewId();
		logger.debug("newId={}", newId);

		if (newId > 0) {
			report.setReportId(newId);
			saveReport(report, true);
		} else {
			logger.warn("Add failed. Allocate new ID failed. report={}", report);
		}
	}

	/**
	 * Update an existing report
	 *
	 * @param report
	 * @throws SQLException
	 */
	@CacheEvict(value = "reports", allEntries = true)
	public void updateReport(Report report) throws SQLException {
		logger.debug("Entering updateReport: report={}", report);

		saveReport(report, false);
	}

	/**
	 * Save a report
	 *
	 * @param report
	 * @param newRecord true if this is a new record, false otherwise
	 * @throws SQLException
	 */
	private void saveReport(Report report, boolean newRecord) throws SQLException {
		logger.debug("Entering saveReport: report={}, newRecord={}", report, newRecord);

		String dateColumn;

		if (newRecord) {
			dateColumn = "CREATION_DATE=?";
		} else {
			dateColumn = "UPDATE_DATE=?";
		}

		String sql = "UPDATE ART_QUERIES SET NAME=?, SHORT_DESCRIPTION=?,"
				+ " DESCRIPTION=?, QUERY_TYPE=?, QUERY_GROUP_ID=?,"
				+ " DATABASE_ID=?, CONTACT_PERSON=?, USES_RULES=?, "
				+ " REPORT_STATUS=?, SHOW_PARAMETERS=?, X_AXIS_LABEL=?, Y_AXIS_LABEL=?,"
				+ " GRAPH_OPTIONS=?, TEMPLATE=?, DISPLAY_RESULTSET=?, XMLA_URL=?,"
				+ " XMLA_DATASOURCE=?, XMLA_CATALOG=?,"
				+ " XMLA_USERNAME=?, XMLA_PASSWORD=?,"
				+ dateColumn
				+ " WHERE QUERY_ID=?";

		//set values for possibly null property objects
		Integer reportGroupId = 0; //database column doesn't allow null
		if (report.getReportGroup() != null) {
			reportGroupId = report.getReportGroup().getReportGroupId();
		}

		Integer datasourceId = 0; //database column doesn't allow null
		if (report.getDatasource() != null) {
			datasourceId = report.getDatasource().getDatasourceId();
		}

		String reportStatus = null;
		if (report.getReportStatus() != null) {
			reportStatus = report.getReportStatus().getValue();
		}

		Object[] values = {
			report.getName(),
			report.getShortDescription(),
			report.getDescription(),
			report.getReportType(),
			reportGroupId,
			datasourceId,
			report.getContactPerson(),
			report.isUsesRules(),
			reportStatus,
			report.isShowParameters(),
			report.getxAxisLabel(),
			report.getyAxisLabel(),
			report.getGraphOptions(),
			report.getTemplate(),
			report.getDisplayResultset(),
			report.getXmlaUrl(),
			report.getXmlaDatasource(),
			report.getXmlaCatalog(),
			report.getXmlaUsername(),
			report.getXmlaPassword(),
			DbUtils.getCurrentTimeStamp(),
			report.getReportId()
		};

		int affectedRows = dbService.update(sql, values);
		logger.debug("affectedRows={}", affectedRows);

		//update report source stored in different table
		updateReportSource(report.getReportId(), report.getReportSource());
	}

	/**
	 * Update the report source for a given report
	 *
	 * @param reportId
	 * @param reportSource
	 * @throws SQLException
	 */
	public void updateReportSource(int reportId, String reportSource) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ArtConfig.getConnection();
			// Delete Old SQL Source
			String sql = "DELETE FROM ART_ALL_SOURCES WHERE OBJECT_ID=?";
			ps = conn.prepareStatement(sql);
			ps.setInt(1, reportId);
			ps.executeUpdate();

			// Write the query in small segments
			// This guarantees portability across databases with different max VARCHAR sizes
			sql = "INSERT INTO ART_ALL_SOURCES "
					+ " (OBJECT_ID, LINE_NUMBER, SOURCE_INFO)"
					+ " VALUES(?, ?, ?)";
			ps = conn.prepareStatement(sql);

			if (reportSource == null) {
				reportSource = "";
			}

			int start = 0;
			int end = SOURCE_CHUNK_LENGTH;
			int step = 1;
			int textLength = reportSource.length();

			ps.setInt(1, reportId);

			while (end < textLength) {
				ps.setInt(2, step++);
				ps.setString(3, reportSource.substring(start, end));

				ps.addBatch();
				start = end;
				end = end + SOURCE_CHUNK_LENGTH;
			}
			ps.setInt(2, step);
			ps.setString(3, reportSource.substring(start));

			ps.addBatch();
			ps.executeBatch();
		} finally {
			DbUtils.close(ps, conn);
		}
	}

	/**
	 * Get and populate the report source for a report
	 *
	 * @param report
	 * @throws SQLException
	 */
	private void populateReportSource(Report report) throws SQLException {
		if (report == null) {
			return;
		}

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String sql = "SELECT SOURCE_INFO"
				+ " FROM ART_ALL_SOURCES "
				+ " WHERE OBJECT_ID=?"
				+ " ORDER BY LINE_NUMBER";

		try {
			conn = ArtConfig.getConnection();
			rs = DbUtils.query(conn, ps, sql, report.getReportId());
			StringBuilder sb = new StringBuilder(1024);
			while (rs.next()) {
				sb.append(rs.getString("SOURCE_INFO"));
			}
			report.setReportSource(sb.toString());
		} finally {
			DbUtils.close(rs, ps, conn);
		}
	}

	/**
	 * Generate an id and record for a new item
	 *
	 * @return new id generated, 0 otherwise
	 * @throws SQLException
	 */
	private synchronized int allocateNewId() throws SQLException {
		logger.debug("Entering allocateNewId");

		int newId = 0;

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		PreparedStatement psInsert = null;

		try {
			conn = ArtConfig.getConnection();
			//generate new id
			String sql = "SELECT MAX(QUERY_ID) FROM ART_QUERIES";
			rs = DbUtils.query(conn, ps, sql);
			if (rs.next()) {
				newId = rs.getInt(1) + 1;
				logger.debug("newId={}", newId);

				//add dummy record with new id. fill all not null columns
				//name has unique constraint so use a random default value
				String allocatingName = "allocating-" + RandomStringUtils.randomAlphanumeric(3);
				sql = "INSERT INTO ART_QUERIES"
						+ " (QUERY_ID, NAME, SHORT_DESCRIPTION, DESCRIPTION,"
						+ " QUERY_GROUP_ID, DATABASE_ID)"
						+ " VALUES (?,?,'','',0,0)";

				Object[] values = {
					newId,
					allocatingName
				};

				int affectedRows = DbUtils.update(conn, psInsert, sql, values);
				logger.debug("affectedRows={}", affectedRows);

				if (affectedRows == 0) {
					logger.warn("allocateNewId - no rows affected. newId={}", newId);
				}
			} else {
				logger.warn("Could not get max id");
			}
		} finally {
			DbUtils.close(psInsert);
			DbUtils.close(rs, ps, conn);
		}

		return newId;
	}

}
