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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.report;

import art.general.ActionResult;
import art.general.ApiResponse;
import art.user.User;
import art.utils.ApiHelper;
import java.net.URI;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@GetMapping("/{id}")
	public ResponseEntity<?> getReportById(@PathVariable("id") Integer id) {
		logger.debug("Entering getReportById: id={}", id);

		try {
			Report report = reportService.getReport(id);
			if (report == null) {
				return ApiHelper.getNotFoundResponseEntity();
			} else {
				Report cleanReport = report.getCleanReport();
				return ApiHelper.getOkResponseEntity(cleanReport);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
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

}
