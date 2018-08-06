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
import art.enums.ReportType;
import art.general.AjaxResponse;
import art.report.Report;
import art.report.ReportService;
import art.user.User;
import java.io.IOException;
import java.sql.SQLException;
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
import org.springframework.ui.Model;
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
	public String showSelfServiceDashboards(HttpSession session, Model model) {
		logger.debug("Entering showSelfServiceDashboards");

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			model.addAttribute("reports", reportService.getDashboardCandidateReports(sessionUser.getUserId()));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "selfServiceDashboards";
	}

	@GetMapping("/getDashboardCandidateReports")
	@ResponseBody
	public AjaxResponse getDashboardCandidateReports(HttpSession session, Locale locale) {
		logger.debug("Entering getDashboardCandidateReports");

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			List<Report> reports = reportService.getDashboardCandidateReports(sessionUser.getUserId());
			for (Report report : reports) {
				String name = report.getLocalizedName(locale);
				name = Encode.forHtmlContent(name);
				report.setName2(name);
			}
			response.setData(reports);
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
			List<Report> reports = reportService.getAccessibleReportsWithReportTypes(sessionUser.getUserId(), Arrays.asList(ReportType.GridstackDashboard));

			List<Report> finalReports = new ArrayList<>();
			for (Report report : reports) {
				if (reportService.hasExclusiveAccess(sessionUser, report.getReportId())) {
					finalReports.add(report);
				}
			}

			for (Report report : finalReports) {
				String name = report.getLocalizedName(locale);
				name = Encode.forHtmlContent(name);
				report.setName2(name);
			}
			response.setData(finalReports);
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
			List<Report> reports = reportService.getGridstackDashboardReports();
			for (Report report : reports) {
				String name = report.getLocalizedName(locale);
				name = Encode.forHtmlContent(name);
				report.setName2(name);
			}
			response.setData(reports);
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

}
