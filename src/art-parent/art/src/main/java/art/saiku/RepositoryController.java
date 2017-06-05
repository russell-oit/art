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
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/saiku2/rest/saiku/api/repository")
public class RepositoryController {

	@Autowired
	private ReportService reportService;

	@GetMapping()
	public List<SaikuReport> getRepository(HttpSession session) throws SQLException {
		User sessionUser = (User) session.getAttribute("sessionUser");
		List<SaikuReport> reports = reportService.getAvailableSaikuReports(sessionUser.getUserId());
		return reports;
	}

	@PostMapping("/resource")
	public ResponseEntity saveResource(HttpSession session,
			@RequestParam("name") String name,
			@RequestParam("content") String content) throws SQLException {

		User sessionUser = (User) session.getAttribute("sessionUser");
		Report report = reportService.getReport(name);
		if (report == null) {
			report = new Report();
			report.setName(name);
			report.setReportSource(content);
			report.setReportType(ReportType.SaikuMondrian);
			reportService.addReport(report, sessionUser);
		} else {
			report.setReportSource(content);
			reportService.updateReport(report, sessionUser);
		}

		//return a response entity instead of void to avoid firefox console error: 
		//XML Parsing Error: no root element found Location
		return ResponseEntity.ok("");
	}

}
