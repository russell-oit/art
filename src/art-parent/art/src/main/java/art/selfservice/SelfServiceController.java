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
import art.general.AjaxResponse;
import art.report.Report;
import art.report.ReportService;
import art.reportoptions.GeneralReportOptions;
import art.reportoptions.ViewOptions;
import art.runreport.ReportRunner;
import art.servlets.Config;
import art.user.User;
import art.utils.ArtUtils;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

	@Autowired
	private MessageSource messageSource;

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

	@GetMapping("/getEditAllDashboards")
	@ResponseBody
	public AjaxResponse getEditAllDashboards(Locale locale) {
		logger.debug("Entering getEditAllDashboards");

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
			@RequestParam(value = "viewReportId", defaultValue = "0") Integer viewReportId,
			HttpSession session) {

		logger.debug("Entering getViewDetails: reportId={}", reportId);

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			Report report;
			String selfServiceOptionsString = null;
			if (viewReportId > 0) {
				report = reportService.getReport(viewReportId);
				if (report == null) {
					throw new RuntimeException("View report not available");
				}
				Report selfServiceReport = reportService.getReport(reportId);
				selfServiceOptionsString = selfServiceReport.getSelfServiceOptions();
			} else {
				report = reportService.getReport(reportId);
			}

			GeneralReportOptions generalOptions = report.getGeneralOptions();
			ViewOptions viewOptions = generalOptions.getView();

			List<String> omitColumns = null;
			List<Map<String, String>> columnLabels = null;
			List<Map<String, String>> columnDescriptions = null;

			if (viewOptions != null) {
				omitColumns = viewOptions.getOmitColumns();
				columnLabels = viewOptions.getColumnLabels();
				columnDescriptions = viewOptions.getColumnDescriptions();
			}

			List<SelfServiceColumn> columns = new ArrayList<>();

			ReportRunner reportRunner = new ReportRunner();
			ResultSet rs = null;
			try {
				reportRunner.setUser(sessionUser);
				reportRunner.setReport(report);
				rs = reportRunner.executeQuery();

				if (rs == null) {
					throw new RuntimeException("ResultSet is null");
				}

				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();

				for (int i = 1; i <= columnCount; i++) {
					SelfServiceColumn column = new SelfServiceColumn();

					column.setName(rsmd.getColumnName(i));
					column.setLabel(rsmd.getColumnLabel(i));

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

				if (omitColumns != null) {
					for (String columnName : omitColumns) {
						//https://stackoverflow.com/questions/10431981/remove-elements-from-collection-while-iterating
						columns.removeIf(column -> StringUtils.equalsIgnoreCase(columnName, column.getLabel()));
					}
				}

				for (SelfServiceColumn column : columns) {
					String label = column.getLabel();
					String userLabel = null;
					if (columnLabels != null) {
						for (Map<String, String> labelDefinition : columnLabels) {
							Map<String, String> caseInsensitiveMap = new CaseInsensitiveMap<>(labelDefinition);
							userLabel = caseInsensitiveMap.get(label);
							if (userLabel != null) {
								break;
							}
						}
					}
					if (userLabel == null) {
						userLabel = label;
					}
					column.setUserLabel(userLabel);

					String description = null;
					if (columnDescriptions != null) {
						for (Map<String, String> descriptionDefinition : columnDescriptions) {
							Map<String, String> caseInsensitiveMap = new CaseInsensitiveMap<>(descriptionDefinition);
							description = caseInsensitiveMap.get(label);
							if (description != null) {
								break;
							}
						}
					}
					if (description == null) {
						description = "";
					}
					column.setDescription(description);

					column.setLabel(Encode.forHtmlAttribute(column.getLabel()));
					column.setUserLabel(Encode.forHtmlContent(column.getUserLabel()));
					column.setDescription(Encode.forHtmlAttribute(column.getDescription()));
				}
			} finally {
				DatabaseUtils.close(rs);
				reportRunner.close();
			}

			Map<String, Object> result = new HashMap<>();
			result.put("allColumns", columns);
			if (StringUtils.isBlank(selfServiceOptionsString)) {
				result.put("fromColumns", columns);
				//result.put("toColumns", null);
			} else {
				SelfServiceOptions selfServiceOptions = ArtUtils.jsonToObjectIgnoreUnknown(selfServiceOptionsString, SelfServiceOptions.class);
				List<String> selfServiceColumns = selfServiceOptions.getColumns();
				//https://stackoverflow.com/questions/46958023/java-stream-divide-into-two-lists-by-boolean-predicate
				Map<Boolean, List<SelfServiceColumn>> partitioned
						= columns.stream().collect(
								Collectors.partitioningBy(c -> ArtUtils.containsIgnoreCase(selfServiceColumns, c.getLabel())));
				List<SelfServiceColumn> toColumns = partitioned.get(true);
				List<SelfServiceColumn> fromColumns = partitioned.get(false);

				result.put("fromColumns", fromColumns);
				result.put("toColumns", toColumns);
			}

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
			List<Report> reports = reportService.getSelfServiceReports();

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
