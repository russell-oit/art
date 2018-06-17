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

import art.general.AjaxResponse;
import art.report.Report;
import art.report.ReportService;
import art.user.User;
import java.io.IOException;
import java.sql.SQLException;
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

}
