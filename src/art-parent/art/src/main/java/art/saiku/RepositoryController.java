/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
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
package art.saiku;

import art.enums.ReportType;
import art.report.Report;
import art.report.ReportService;
import art.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/saiku2/api/repository")
public class RepositoryController {

	@Autowired
	private ReportService reportService;

	@GetMapping()
	public List<SaikuReport> getRepository(HttpSession session, Locale locale)
			throws SQLException, IOException {
		
		User sessionUser = (User) session.getAttribute("sessionUser");
		List<SaikuReport> reports = reportService.getAvailableSaikuReports(sessionUser.getUserId(), locale);
		return reports;
	}

	@PostMapping("/resource")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void saveResource(HttpSession session,
			@RequestParam("name") String name,
			@RequestParam("content") String content) throws SQLException, IOException {

		//https://stackoverflow.com/questions/14515994/convert-json-string-to-pretty-print-json-output-using-jackson
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		Object json = mapper.readValue(content, Object.class);
		String prettyContent = mapper.writeValueAsString(json);

		User sessionUser = (User) session.getAttribute("sessionUser");
		Report report = reportService.getReport(name);
		if (report == null) {
			//creating new report
			report = new Report();
			report.setName(name);
			report.setReportSource(prettyContent);
			report.setReportType(ReportType.SaikuReport);
			reportService.addReport(report, sessionUser);

			//give this user direct access to the report
			reportService.grantAccess(report, sessionUser);
		} else {
			//editing/overwriting existing report
			//check if this is the only user who has access. if so, he can overwrite the report
			int reportId = report.getReportId();
			boolean exclusiveAccess = reportService.hasOwnerAccess(sessionUser, reportId);
			boolean canOverwrite;

			if (exclusiveAccess || sessionUser.isAdminUser()) {
				canOverwrite = true;
			} else {
				canOverwrite = false;
			}

			if (canOverwrite) {
				report.setReportSource(prettyContent);
				reportService.updateReport(report, sessionUser);
			} else {
				throw new RuntimeException("Report not saved. You do not have access to overwrite the report.");
			}
		}

		//return a response entity instead of void to avoid firefox console error - XML Parsing Error: no root element found Location
		//returning void (with or without produces) doesn't set the Content-Type header in the response. Firefox assumes xhtml (xml) if no content-type given hence the error.
		//produces doesn't set response content type - https://stackoverflow.com/questions/30548822/spring-mvc-4-application-json-content-type-is-not-being-set-correctly
		//https://stackoverflow.com/questions/39788503/spring-restcontroller-produces-charset-utf-8
		//http://www.baeldung.com/spring-httpmessageconverter-rest
		//alternative is to have a response status of no_content on the method
		//https://stackoverflow.com/questions/26550124/spring-returning-empty-http-responses-with-responseentityvoid-doesnt-work
//		return ResponseEntity.ok("");
	}

	@DeleteMapping("/resource")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteResource(HttpSession session,
			@RequestParam("file") Integer reportId) throws SQLException {

		//https://stackoverflow.com/questions/32396884/return-http-204-on-null-with-spring-restcontroller
		//http://www.jcombat.com/spring/exception-handling-in-spring-restful-web-service
		//https://stackoverflow.com/questions/26550124/spring-returning-empty-http-responses-with-responseentityvoid-doesnt-work
		//https://blog.jayway.com/2012/09/16/improve-your-spring-rest-api-part-i/
		User sessionUser = (User) session.getAttribute("sessionUser");

		//check if this is the only user who has access. if so, he can delete the report
		boolean exclusiveAccess = reportService.hasOwnerAccess(sessionUser, reportId);
		boolean canDelete;

		if (exclusiveAccess || sessionUser.isAdminUser()) {
			canDelete = true;
		} else {
			canDelete = false;
		}

		if (canDelete) {
			reportService.deleteReport(reportId);
		} else {
			throw new RuntimeException("Report not deleted. You do not have access to delete the report.");
		}
	}

}
