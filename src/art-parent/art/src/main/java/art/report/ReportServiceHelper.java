/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.report;

import art.reportparameter.ReportParameterService;
import art.user.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides method to import report records
 *
 * @author Timothy Anyona
 */
public class ReportServiceHelper {
	//use separate class to avoid circular with ParameterService and ReportParameterService

	private static final Logger logger = LoggerFactory.getLogger(ReportServiceHelper.class);

	/**
	 * Imports report records
	 *
	 * @param reports the list of reports to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param reportService the ReportService object to use
	 * @param reportParameterService the ReportParameterService object to use
	 * @throws SQLException
	 */
	public void importReports(List<Report> reports, User actionUser,
			Connection conn, ReportService reportService,
			ReportParameterService reportParameterService) throws SQLException {

		logger.debug("Entering importReports: actionUser={}", actionUser);

		boolean originalAutoCommit = true;

		try {
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			reportService.importReports(reports, actionUser, conn);
			reportParameterService.importReportParameters(reports, actionUser, conn);

			conn.commit();
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}
}
