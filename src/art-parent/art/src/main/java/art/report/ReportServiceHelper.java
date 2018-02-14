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

import art.datasource.Datasource;
import art.datasource.DatasourceService;
import art.dbutils.DbService;
import art.encryptor.Encryptor;
import art.encryptor.EncryptorService;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.reportgroupmembership.ReportGroupMembershipService2;
import art.reportparameter.ReportParameterService;
import art.user.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
	 * @throws SQLException
	 */
	public void importReports(List<Report> reports, User actionUser,
			Connection conn) throws SQLException {

		boolean commit = true;
		importReports(reports, actionUser, conn, commit);
	}

	/**
	 * Imports report records
	 *
	 * @param reports the list of reports to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @throws SQLException
	 */
	public void importReports(List<Report> reports, User actionUser,
			Connection conn, boolean commit) throws SQLException {

		logger.debug("Entering importReports: actionUser={}", actionUser);

		boolean originalAutoCommit = true;

		try {
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			DbService dbService = new DbService();
			ReportService reportService = new ReportService();
			ReportParameterService reportParameterService = new ReportParameterService();
			DatasourceService datasourceService = new DatasourceService();
			EncryptorService encryptorService = new EncryptorService();
			ReportGroupService reportGroupService = new ReportGroupService();
			ReportGroupMembershipService2 reportGroupMembershipService2 = new ReportGroupMembershipService2();

			String sql = "SELECT MAX(QUERY_ID) FROM ART_QUERIES";
			int reportId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(DATABASE_ID) FROM ART_DATABASES";
			int datasourceId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(ENCRYPTOR_ID) FROM ART_ENCRYPTORS";
			int encryptorId = dbService.getMaxRecordId(conn, sql);

			sql = "SELECT MAX(QUERY_GROUP_ID) FROM ART_QUERY_GROUPS";
			int reportGroupId = dbService.getMaxRecordId(conn, sql);

			Map<String, Datasource> addedDatasources = new HashMap<>();
			Map<String, Encryptor> addedEncryptors = new HashMap<>();
			Map<String, ReportGroup> addedReportGroups = new HashMap<>();
			Map<String, Report> addedReports = new HashMap<>();
			for (Report report : reports) {
				String reportName = report.getName();
				if (StringUtils.isNotBlank(reportName)) {
					Report existingReport = reportService.getReport(reportName);
					if (existingReport == null) {
						Report addedReport = addedReports.get(reportName);
						if (addedReport == null) {
							reportId++;

							Datasource datasource = report.getDatasource();
							if (datasource != null) {
								String datasourceName = datasource.getName();
								if (StringUtils.isBlank(datasourceName)) {
									report.setDatasource(null);
								} else {
									Datasource existingDatasource = datasourceService.getDatasource(datasourceName);
									if (existingDatasource == null) {
										Datasource addedDatasource = addedDatasources.get(datasourceName);
										if (addedDatasource == null) {
											datasourceId++;
											datasourceService.saveDatasource(datasource, datasourceId, actionUser, conn);
											addedDatasources.put(datasourceName, datasource);
										} else {
											report.setDatasource(addedDatasource);
										}
									} else {
										report.setDatasource(existingDatasource);
									}
								}
							}

							Encryptor encryptor = report.getEncryptor();
							if (encryptor != null) {
								String encryptorName = encryptor.getName();
								if (StringUtils.isBlank(encryptorName)) {
									report.setEncryptor(null);
								} else {
									Encryptor existingEncryptor = encryptorService.getEncryptor(encryptorName);
									if (existingEncryptor == null) {
										Encryptor addedEncryptor = addedEncryptors.get(encryptorName);
										if (addedEncryptor == null) {
											encryptorId++;
											encryptorService.saveEncryptor(encryptor, encryptorId, actionUser, conn);
											addedEncryptors.put(encryptorName, encryptor);
										} else {
											report.setEncryptor(addedEncryptor);
										}
									} else {
										report.setEncryptor(existingEncryptor);
									}
								}
							}

							List<ReportGroup> reportGroups = report.getReportGroups();
							if (CollectionUtils.isNotEmpty(reportGroups)) {
								List<ReportGroup> newReportGroups = new ArrayList<>();
								for (ReportGroup reportGroup : reportGroups) {
									String reportGroupName = reportGroup.getName();
									ReportGroup existingReportGroup = reportGroupService.getReportGroup(reportGroupName);
									if (existingReportGroup == null) {
										ReportGroup addedReportGroup = addedReportGroups.get(reportGroupName);
										if (addedReportGroup == null) {
											reportGroupId++;
											reportGroupService.saveReportGroup(reportGroup, reportGroupId, actionUser, conn);
											addedReportGroups.put(reportGroupName, reportGroup);
											newReportGroups.add(reportGroup);
										} else {
											newReportGroups.add(addedReportGroup);
										}
									} else {
										newReportGroups.add(existingReportGroup);
									}
								}
								report.setReportGroups(newReportGroups);
							}

							reportService.saveReport(report, reportId, actionUser, conn);
							addedReports.put(reportName, report);
							reportGroupMembershipService2.recreateReportGroupMemberships(report);
						} else {
							report.setReportId(addedReport.getReportId());
						}
					} else {
						report.setReportId(existingReport.getReportId());
					}
				}
			}

			reportParameterService.importReportParameters(reports, actionUser, conn);

			if (commit) {
				conn.commit();
			}
		} catch (SQLException ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}
}
