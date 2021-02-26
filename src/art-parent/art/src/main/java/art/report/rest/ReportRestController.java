/*
 * ART. A Reporting Tool.
 * Copyright (C) 2019 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.report.rest;

import art.cache.CacheHelper;
import art.drilldown.Drilldown;
import art.drilldown.DrilldownService;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.general.ActionResult;
import art.general.ApiResponse;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.reportparameter.ReportParameterService;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportOutputGenerator;
import art.runreport.ReportOutputGeneratorResult;
import art.runreport.ReportRunner;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import art.user.User;
import art.utils.ApiHelper;
import art.utils.ArtLogsHelper;
import art.utils.ArtUtils;
import art.utils.FilenameHelper;
import java.io.PrintWriter;
import java.net.URI;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Controller to provide rest services related to reports
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/api/reports")
public class ReportRestController {

	private static final Logger logger = LoggerFactory.getLogger(ReportRestController.class);

	@Autowired
	private ReportService reportService;

	@Autowired
	private DrilldownService drilldownService;

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private CacheHelper cacheHelper;

	@Autowired
	private ReportParameterService reportParameterService;

	@GetMapping("/{id}")
	public ResponseEntity<?> getReportById(@PathVariable("id") Integer id) {
		logger.debug("Entering getReportById: id={}", id);

		try {
			Report report = reportService.getReport(id);
			if (report == null) {
				return ApiHelper.getNotFoundResponseEntity();
			} else {
				prepareReportForOutput(report);
				return ApiHelper.getOkResponseEntity(report);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@GetMapping("/name/{name}")
	public ResponseEntity<?> getReportByName(@PathVariable("name") String name) {
		logger.debug("Entering getReportByName: name='{}'", name);

		try {
			Report report = reportService.getReport(name);
			if (report == null) {
				return ApiHelper.getNotFoundResponseEntity();
			} else {
				prepareReportForOutput(report);
				return ApiHelper.getOkResponseEntity(report);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	/**
	 * Adds report parameters and clears password fields
	 *
	 * @param report the report object
	 * @throws SQLException
	 */
	private void prepareReportForOutput(Report report) throws SQLException {
		int reportId = report.getReportId();
		List<ReportParameter> reportParams = reportParameterService.getReportParameters(reportId);
		report.setReportParams(reportParams);

		List<Drilldown> drilldowns = drilldownService.getDrilldowns(reportId);
		report.setDrilldowns(drilldowns);

		report.clearAllPasswords();

		//clear objects that could have had their passwords cleared, so as to have fresh start
		cacheHelper.clearReports();
		cacheHelper.clearDatasources();
		cacheHelper.clearEncryptors();
		cacheHelper.clearParameters();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteReport(@PathVariable("id") Integer id) {
		logger.debug("Entering deleteReport: id={}", id);

		try {
			ActionResult result = reportService.deleteReport(id);
			if (result.isSuccess()) {
				return ApiHelper.getOkResponseEntity();
			} else {
				String message = "Report not deleted because linked jobs exist";
				return ApiHelper.getLinkedRecordsExistResponseEntity(message, result.getData());
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PostMapping
	public ResponseEntity<ApiResponse> addReport(@RequestBody Report report,
			HttpSession session, UriComponentsBuilder b) {

		logger.debug("Entering addReport");

		try {
			String name = report.getName();
			if (StringUtils.isBlank(name)) {
				String message = "name field not provided or blank";
				return ApiHelper.getInvalidValueResponseEntity(message);
			}

			if (reportService.reportExists(name)) {
				String message = "A report with the given name already exists";
				return ApiHelper.getRecordExistsResponseEntity(message);
			}

			if (report.isClearTextPasswords()) {
				report.encryptPasswords();
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			int newId = reportService.addReport(report, sessionUser);

			UriComponents uriComponents = b.path("/api/reports/{id}").buildAndExpand(newId);
			URI uri = uriComponents.toUri();
			Report cleanReport = report.getCleanReport();
			return ApiHelper.getCreatedResponseEntity(uri, cleanReport);
		} catch (Exception ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateReport(@PathVariable("id") Integer id,
			@RequestBody Report report, HttpSession session) {

		logger.debug("Entering updateReport: id={}", id);

		try {
			report.setReportId(id);

			if (report.isClearTextPasswords()) {
				report.encryptPasswords();
			} else {
				Report currentReport = reportService.getReport(id);
				if (currentReport == null) {
					return ApiHelper.getNotFoundResponseEntity();
				} else {
					//use current passwords if nothing passed
					if (StringUtils.isEmpty(report.getOpenPassword())) {
						report.setOpenPassword(currentReport.getOpenPassword());
						report.encryptOpenPassword();
					}
					if (StringUtils.isEmpty(report.getModifyPassword())) {
						report.setModifyPassword(currentReport.getModifyPassword());
						report.encryptModifyPassword();
					}
				}
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			reportService.updateReport(report, sessionUser);

			Report cleanReport = report.getCleanReport();
			return ApiHelper.getOkResponseEntity(cleanReport);
		} catch (Exception ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PostMapping("/{id}/disable")
	public ResponseEntity<?> disableReport(@PathVariable("id") Integer id, HttpSession session) {
		logger.debug("Entering disableReport: id={}", id);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			reportService.disableReport(id, sessionUser);
			return ApiHelper.getOkResponseEntity();
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PostMapping("/{id}/enable")
	public ResponseEntity<?> enableReport(@PathVariable("id") Integer id, HttpSession session) {
		logger.debug("Entering enableReport: id={}", id);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			reportService.enableReport(id, sessionUser);
			return ApiHelper.getOkResponseEntity();
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PostMapping("/run")
	public void runReport(@RequestParam("reportId") Integer reportId,
			HttpSession session, HttpServletRequest request, Locale locale,
			HttpServletResponse response) {

		logger.debug("Entering runReport: reportId={}", reportId);

		ReportRunner reportRunner = null;

		try {
			Report report = reportService.getReport(reportId);
			if (report == null) {
				ApiHelper.outputNotFoundResponse(response);
				return;
			}

			User sessionUser = (User) session.getAttribute("sessionUser");

			if (!sessionUser.hasConfigureReportsPermission()) {
				if (!report.isActive()) {
					String message = "report disabled";
					ApiHelper.outputUnauthorizedResponse(response, message);
					return;
				}

				if (!reportService.canUserRunReport(sessionUser, reportId)) {
					String message = "no permission";
					ApiHelper.outputUnauthorizedResponse(response, message);
					return;
				}
			}

			ReportType reportType = report.getReportType();

			RunReportHelper runReportHelper = new RunReportHelper();

			ReportFormat reportFormat = runReportHelper.getReportFormat(request, report);

			if (reportType != ReportType.Update && reportFormat.isHtml()) {
				String message = "report format not allowed: " + reportFormat;
				ApiHelper.outputInvalidValueResponse(response, message);
				return;
			}

			Instant overallStart = Instant.now();

			PrintWriter writer = response.getWriter();

			ParameterProcessor paramProcessor = new ParameterProcessor();
			paramProcessor.setSuppliedReport(report);
			paramProcessor.setIsFragment(true);
			ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request, locale);

			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();

			reportRunner = new ReportRunner();
			reportRunner.setUser(sessionUser);
			reportRunner.setReport(report);
			reportRunner.setReportParamsMap(reportParamsMap);

			int resultSetType = runReportHelper.getResultSetType(reportType);

			//run query
			Instant queryStart = Instant.now();

			reportRunner.execute(resultSetType);

			Instant queryEnd = Instant.now();

			Integer rowsRetrieved = null;
			Integer rowsUpdated = null;
			String fileName = null;
			if (reportType == ReportType.Update) {
				reportRunner.getResultSet();
				rowsUpdated = reportRunner.getUpdateCount();
			} else {
				FilenameHelper filenameHelper = new FilenameHelper();
				fileName = filenameHelper.getFilename(report, locale, reportFormat, reportParamsMap);
				String exportPath = Config.getReportsExportPath();
				String outputFileName = exportPath + fileName;

				ReportOutputGenerator reportOutputGenerator = new ReportOutputGenerator();

				reportOutputGenerator.setDrilldownService(drilldownService);
				reportOutputGenerator.setRequest(request);
				reportOutputGenerator.setResponse(response);
				reportOutputGenerator.setServletContext(servletContext);
				reportOutputGenerator.setIsJob(true);

				ReportOutputGeneratorResult outputResult = reportOutputGenerator.generateOutput(report, reportRunner,
						reportFormat, locale, paramProcessorResult, writer, outputFileName, sessionUser, messageSource);

				if (outputResult.isSuccess()) {
					rowsRetrieved = outputResult.getRowCount();
				} else {
					ApiHelper.outputErrorResponse(response, outputResult.getMessage());
					return;
				}

				//encrypt file if applicable
				report.encryptFile(outputFileName);
			}

			Instant overallEnd = Instant.now();
			Duration overallDuration = Duration.between(overallStart, overallEnd);
			Duration queryDuration = Duration.between(queryStart, queryEnd);

			Integer totalTimeSeconds = (int) overallDuration.getSeconds();
			Integer fetchTimeSeconds = (int) queryDuration.getSeconds();

			ArtLogsHelper.logReportRun(sessionUser, request.getRemoteAddr(), reportId, totalTimeSeconds, fetchTimeSeconds, reportFormat.getValue(), reportParamsList);

			RunReportResponseObject responseObject = new RunReportResponseObject();
			responseObject.setRowsUpdated(rowsUpdated);
			responseObject.setRowsRetrieved(rowsRetrieved);
			if (fileName != null) {
				String urlFileName = Encode.forUriComponent(fileName);
				String url = ArtUtils.getBaseUrl(request) + "/export/reports/" + urlFileName;
				responseObject.setFileName(fileName);
				responseObject.setUrl(url);
			}
			ApiHelper.outputOkResponse(response, responseObject);
		} catch (Exception ex) {
			logger.error("Error", ex);
			ApiHelper.outputErrorResponse(response, ex);
		} finally {
			if (reportRunner != null) {
				reportRunner.close();
			}
		}
	}

}
