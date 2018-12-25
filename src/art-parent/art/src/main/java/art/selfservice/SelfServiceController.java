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
package art.selfservice;

import art.dashboard.DashboardHelper;
import art.dashboard.GridstackDashboard;
import art.dbutils.DatabaseUtils;
import art.enums.ReportType;
import art.general.AjaxResponse;
import art.report.Report;
import art.report.ReportService;
import art.runreport.GroovyDataDetails;
import art.runreport.ReportRunner;
import art.runreport.RunReportHelper;
import art.user.User;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpSession;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for self service reports
 *
 * @author Timothy Anyona
 */
@Controller
public class SelfServiceController {

	private static final Logger logger = LoggerFactory.getLogger(SelfServiceController.class);

	@Autowired
	private ReportService reportService;

	@GetMapping("/selfServiceDashboards")
	public String showSelfServiceDashboards() {
		logger.debug("Entering showSelfServiceDashboards");

		return "selfServiceDashboards";
	}

	@GetMapping("/getDashboardCandidateReports")
	@ResponseBody
	public AjaxResponse getDashboardCandidateReports(HttpSession session, Locale locale) {
		logger.debug("Entering getDashboardCandidateReports");

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			List<Report> basicReports = new ArrayList<>();
			List<Report> reports = reportService.getDashboardCandidateReports(sessionUser.getUserId());
			for (Report report : reports) {
				String name = report.getLocalizedName(locale);
				String encodedName = Encode.forHtmlContent(name);
				report.setName2(encodedName);
				basicReports.add(report.getBasicReport());
			}
			response.setData(basicReports);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@GetMapping("/getEditDashboardReports")
	@ResponseBody
	public AjaxResponse getEditDashboardReports(HttpSession session, Locale locale) {
		logger.debug("Entering getEditDashboardReports");

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			List<Report> basicReports = new ArrayList<>();
			List<Report> reports = reportService.getAccessibleReportsWithReportTypes(sessionUser.getUserId(), Arrays.asList(ReportType.GridstackDashboard));

			List<Report> finalReports = new ArrayList<>();
			for (Report report : reports) {
				if (reportService.hasExclusiveAccess(sessionUser, report.getReportId())) {
					finalReports.add(report);
				}
			}

			for (Report report : finalReports) {
				String name = report.getLocalizedName(locale);
				String encodedName = Encode.forHtmlContent(name);
				report.setName2(encodedName);
				basicReports.add(report.getBasicReport());
			}
			response.setData(basicReports);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@GetMapping("/getEditAllDashboardReports")
	@ResponseBody
	public AjaxResponse getEditAllDashboardReports(Locale locale) {
		logger.debug("Entering getEditAllDashboardReports");

		AjaxResponse response = new AjaxResponse();

		try {
			List<Report> basicReports = new ArrayList<>();
			List<Report> reports = reportService.getGridstackDashboardReports();
			for (Report report : reports) {
				String name = report.getLocalizedName(locale);
				String encodedName = Encode.forHtmlContent(name);
				report.setName2(encodedName);
				basicReports.add(report.getBasicReport());
			}
			response.setData(basicReports);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@GetMapping("/getDashboardDetails")
	@ResponseBody
	public AjaxResponse getDashboardDetails(@RequestParam("reportId") Integer reportId) {
		logger.debug("Entering getDashboardDetails: reportId={}", reportId);

		AjaxResponse response = new AjaxResponse();

		try {
			Report report = reportService.getReport(reportId);
			DashboardHelper dashboardHelper = new DashboardHelper();
			GridstackDashboard dashboard = dashboardHelper.buildBasicGridstackDashboardObject(report);
			response.setData(dashboard);
			response.setSuccess(true);
		} catch (Exception ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@GetMapping("/selfServiceReports")
	public String showSelfServiceReports() {
		logger.debug("Entering showSelfServiceReports");

		return "selfServiceReports";
	}

	@GetMapping("/getViews")
	@ResponseBody
	public AjaxResponse getViews(HttpSession session, Locale locale) {
		logger.debug("Entering getViews");

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			List<Report> basicReports = new ArrayList<>();
			List<ReportType> includedReportTypes = Arrays.asList(ReportType.View);
			List<Report> views = reportService.getAccessibleReportsWithReportTypes(sessionUser.getUserId(), includedReportTypes);
			for (Report report : views) {
				String name = report.getLocalizedName(locale);
				String encodedName = Encode.forHtmlContent(name);
				report.setName2(encodedName);
				basicReports.add(report.getBasicReport());
			}
			response.setData(basicReports);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@GetMapping("/getViewDetails")
	@ResponseBody
	public AjaxResponse getViewDetails(@RequestParam("reportId") Integer reportId,
			HttpSession session) {

		logger.debug("Entering getViewDetails: reportId={}", reportId);

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			Report report = reportService.getReport(reportId);
			ReportRunner reportRunner = new ReportRunner();
			ResultSet rs = null;
			try {
				reportRunner.setUser(sessionUser);
				reportRunner.setReport(report);
				rs = reportRunner.executeQuery();
				Object groovyData = reportRunner.getGroovyData();
				if (groovyData == null) {
					List<SelfServiceColumn> columns = new ArrayList<>();

					ResultSetMetaData rsmd = rs.getMetaData();
					int columnCount = rsmd.getColumnCount();

					for (int i = 1; i <= columnCount; i++) {
						SelfServiceColumn column = new SelfServiceColumn();

						String name = rsmd.getColumnName(i);
						String encodedName = Encode.forHtmlAttribute(name);
						column.setName(encodedName);
						
						String label = rsmd.getColumnLabel(i);
						String encodedLabel = Encode.forHtmlContent(label);
						column.setLabel(encodedLabel);

						int sqlType = rsmd.getColumnType(i);

						String type;

						switch (sqlType) {
							case Types.INTEGER:
							case Types.TINYINT:
							case Types.SMALLINT:
							case Types.BIGINT:
								type = "integer";
								break;
							case Types.NUMERIC:
							case Types.DECIMAL:
							case Types.FLOAT:
							case Types.REAL:
							case Types.DOUBLE:
								type = "double";
								break;
							case Types.DATE:
								type = "date";
								break;
							case Types.TIME:
								type = "time";
								break;
							case Types.TIMESTAMP:
								type = "datetime";
								break;
							default:
								type = "string";
						}

						column.setType(type);

						columns.add(column);
					}
					response.setData(columns);
				} else {
					GroovyDataDetails dataDetails = RunReportHelper.getGroovyDataDetails(groovyData, report);
				}
			} finally {
				DatabaseUtils.close(rs);
				reportRunner.close();
			}
			response.setSuccess(true);
		} catch (Exception ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

}
