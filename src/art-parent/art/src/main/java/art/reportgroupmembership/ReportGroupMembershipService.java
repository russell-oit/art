/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
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
import art.report.ReportService;
import art.reportgroup.ReportGroupService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving report group memberships
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

}
