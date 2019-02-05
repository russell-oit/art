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
import art.general.AjaxResponse;
import art.report.Report;
import art.report.ReportService;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import art.user.User;
import art.utils.ArtUtils;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
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
	public String showSelfServiceDashboards() {
		logger.debug("Entering showSelfServiceDashboards");

		return "selfServiceDashboards";
	}

	@GetMapping("/getDashboardCandidates")
	@ResponseBody
	public AjaxResponse getDashboardCandidates(HttpSession session, Locale locale) {
		logger.debug("Entering getDashboardCandidates");

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

	@GetMapping("/getEditDashboards")
	@ResponseBody
	public AjaxResponse getEditDashboards(HttpSession session, Locale locale) {
		logger.debug("Entering getEditDashboards");

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			List<Report> basicReports = new ArrayList<>();
			List<Report> reports = reportService.getAvailableGridstackDashboardReports(sessionUser.getUserId());

			List<Report> finalReports = new ArrayList<>();
			for (Report report : reports) {
				if (reportService.hasExclusiveOrOwnerAccess(sessionUser, report.getReportId())) {
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
	public String showSelfServiceReports(Model model, Locale locale) {
		logger.debug("Entering showSelfServiceReports");

		String languageTag = locale.toLanguageTag();

		String languageFileName = "query-builder." + languageTag + ".js";

		String languageFilePath = Config.getAppPath()
				+ "js" + File.separator
				+ "jQuery-QueryBuilder-2.5.2" + File.separator
				+ "i18n" + File.separator
				+ languageFileName;

		File languageFile = new File(languageFilePath);

		if (languageFile.exists()) {
			model.addAttribute("languageFileName", languageFileName);
		}

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
			List<Report> views = reportService.getAvailableViewReports(sessionUser.getUserId());
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
			String selfServiceOptionsString = report.getSelfServiceOptions();

			RunReportHelper runReportHelper = new RunReportHelper();
			List<SelfServiceColumn> columns = runReportHelper.getSelfServiceColumnsForView(report, sessionUser);

			Map<String, Object> result = new HashMap<>();
			result.put("allColumns", columns);

			if (StringUtils.isBlank(selfServiceOptionsString)) {
				result.put("fromColumns", columns);
			} else {
				SelfServiceOptions selfServiceOptions = ArtUtils.jsonToObjectIgnoreUnknown(selfServiceOptionsString, SelfServiceOptions.class);
				List<String> selfServiceColumns = selfServiceOptions.getColumns();
				//iterate based on the self service options to maintain saved columns order
				List<SelfServiceColumn> toColumns = new ArrayList<>();
				for (String selfServiceColumn : selfServiceColumns) {
					for (SelfServiceColumn column : columns) {
						if (StringUtils.equalsIgnoreCase(selfServiceColumn, column.getLabel())) {
							toColumns.add(column);
							break;
						}
					}
				}

				//https://www.mkyong.com/java8/java-8-streams-filter-examples/
				List<SelfServiceColumn> fromColumns = columns.stream()
						.filter(c -> !ArtUtils.containsIgnoreCase(selfServiceColumns, c.getLabel()))
						.collect(Collectors.toList());

				result.put("fromColumns", fromColumns);
				result.put("toColumns", toColumns);
			}
			result.put("options", selfServiceOptionsString);

			response.setData(result);
			response.setSuccess(true);
		} catch (Exception ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@GetMapping("/getEditSelfService")
	@ResponseBody
	public AjaxResponse getEditSelfService(HttpSession session, Locale locale) {
		logger.debug("Entering getEditSelfService");

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			List<Report> basicReports = new ArrayList<>();
			List<Report> reports = reportService.getAvailableSelfServiceReports(sessionUser.getUserId());

			List<Report> finalReports = new ArrayList<>();
			for (Report report : reports) {
				if (reportService.hasExclusiveOrOwnerAccess(sessionUser, report.getReportId())) {
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

}
