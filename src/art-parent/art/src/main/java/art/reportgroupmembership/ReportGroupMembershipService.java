/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@reports.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.reportgroupmembership;

import art.dbutils.DbService;
import art.report.Report;
import art.report.ReportService;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, updating and deleting report group
 * memberships
 *
 * @author Timothy Anyona
 */
@Service
public class ReportGroupMembershipService {

	private static final Logger logger = LoggerFactory.getLogger(ReportGroupMembershipService.class);

	private final DbService dbService;
	private final ReportService reportService;
	private final ReportGroupService reportGroupService;

	@Autowired
	public ReportGroupMembershipService(DbService dbService,
			ReportService reportService, ReportGroupService reportGroupService) {

		this.dbService = dbService;
		this.reportService = reportService;
		this.reportGroupService = reportGroupService;
	}

	public ReportGroupMembershipService() {
		dbService = new DbService();
		reportService = new ReportService();
		reportGroupService = new ReportGroupService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_REPORT_REPORT_GROUPS";

	/**
	 * Maps a resultset to an object
	 */
	private class ReportGroupMembershipMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			ReportGroupMembership membership = new ReportGroupMembership();

			membership.setReport(reportService.getReport(rs.getInt("REPORT_ID")));
			membership.setReportGroup(reportGroupService.getReportGroup(rs.getInt("REPORT_GROUP_ID")));

			return type.cast(membership);
		}
	}

	/**
	 * Returns all report group memberships
	 *
	 * @return all report group memberships
	 * @throws SQLException
	 */
	public List<ReportGroupMembership> getAllReportGroupMemberships() throws SQLException {
		logger.debug("Entering getAllReportGroupMemberships");

		ResultSetHandler<List<ReportGroupMembership>> h = new BeanListHandler<>(ReportGroupMembership.class, new ReportGroupMembershipMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Deletes a report group membership
	 *
	 * @param reportId the report id
	 * @param reportGroupId the report group id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"reports", "reportGroups"}, allEntries = true)
	public void deleteReportGroupMembership(int reportId, int reportGroupId) throws SQLException {
		logger.debug("Entering deleteReportGroupMembership: reportId={}, reportGroupId={}",
				reportId, reportGroupId);

		String sql;

		sql = "DELETE FROM ART_REPORT_REPORT_GROUPS WHERE REPORT_ID=? AND REPORT_GROUP_ID=?";
		dbService.update(sql, reportId, reportGroupId);
	}

	/**
	 * Delete all report group memberships for the given report
	 *
	 * @param reportId the report id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"reports", "reportGroups"}, allEntries = true)
	public void deleteAllReportGroupMembershipsForReport(int reportId) throws SQLException {
		logger.debug("Entering deleteAllReportGroupMembershipsForReport: reportId={}", reportId);

		String sql = "DELETE FROM ART_REPORT_REPORT_GROUPS WHERE REPORT_ID=?";
		dbService.update(sql, reportId);
	}

	/**
	 * Adds report group memberships for the given report
	 *
	 * @param report the report, not null
	 * @param reportGroups the report groups
	 * @throws SQLException
	 */
	@CacheEvict(value = {"reports", "reportGroups"}, allEntries = true)
	public void addReportGroupMemberships(Report report, List<ReportGroup> reportGroups) throws SQLException {
		logger.debug("Entering addReportGroupMemberships: report={}", report);

		Objects.requireNonNull(report, "report must not be null");

		if (CollectionUtils.isEmpty(reportGroups)) {
			return;
		}

		List<Integer> reportGroupIds = new ArrayList<>();
		for (ReportGroup reportGroup : reportGroups) {
			reportGroupIds.add(reportGroup.getReportGroupId());
		}
		Integer[] reports = {report.getReportId()};
		String action = "add";
		updateReportGroupMembership(action, reports, reportGroupIds.toArray(new Integer[0]));
	}

	/**
	 * Adds or removes report group memberships
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param reports report ids
	 * @param reportGroups report group ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"reports", "reportGroups"}, allEntries = true)
	public void updateReportGroupMembership(String action, Integer[] reports, Integer[] reportGroups) throws SQLException {
		logger.debug("Entering updateReportGroupMemberships: action='{}'", action);

		logger.debug("(reports == null) = {}", reports == null);
		logger.debug("(reportGroups == null) = {}", reportGroups == null);
		if (reports == null || reportGroups == null) {
			logger.warn("Update not performed. reports or reportGroups is null.");
			return;
		}

		boolean add;
		if (StringUtils.equalsIgnoreCase(action, "add")) {
			add = true;
		} else {
			add = false;
		}

		String sql;

		if (add) {
			sql = "INSERT INTO ART_REPORT_REPORT_GROUPS (REPORT_ID, REPORT_GROUP_ID) VALUES (?, ?)";
		} else {
			sql = "DELETE FROM ART_REPORT_REPORT_GROUPS WHERE REPORT_ID=? AND REPORT_GROUP_ID=?";
		}

		String sqlTest = "UPDATE ART_REPORT_REPORT_GROUPS SET REPORT_ID=? WHERE REPORT_ID=? AND REPORT_GROUP_ID=?";
		int affectedRows;
		boolean updateRecord;

		for (Integer reportId : reports) {
			for (Integer reportGroupId : reportGroups) {
				updateRecord = true;
				if (add) {
					//test if record exists. to avoid integrity constraint error
					affectedRows = dbService.update(sqlTest, reportId, reportId, reportGroupId);
					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRecord = false;
					}
				}
				if (updateRecord) {
					dbService.update(sql, reportId, reportGroupId);
				}
			}
		}
	}
}
