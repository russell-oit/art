/**
 * Copyright (C) 2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.report;

import art.user.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Spring controller for reports page
 *
 * @author Timothy Anyona
 */
@Controller
public class ReportController {

	private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

	@Autowired
	private ReportService reportService;

	@RequestMapping(value = "/app/reports", method = RequestMethod.GET)
	public String showReports(HttpSession session,
			@RequestParam(value = "groupId", required = false) Integer groupId,
			HttpServletRequest request, Model model) {
		
		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			List<AvailableReport> reports = reportService.getAvailableReports(sessionUser.getUsername());

			//allow to focus public_user in one group only. is this feature used? it's not documented
			if (groupId != null) {
				List<AvailableReport> filteredReports = new ArrayList<AvailableReport>();
				for (AvailableReport report : reports) {
					if (report.getReportGroupId() == groupId) {
						filteredReports.add(report);
					}
				}
				model.addAttribute("reports", filteredReports);
			} else {
				model.addAttribute("reports", reports);
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reports";
	}

	@RequestMapping(value = "/app/getReports", method = RequestMethod.GET)
	public @ResponseBody
	List<AvailableReport> getReports(HttpSession session, HttpServletRequest request) {
		//object will be automatically converted to json
		//see http://www.mkyong.com/spring-mvc/spring-3-mvc-and-json-example/
		User sessionUser = (User) session.getAttribute("sessionUser");

		List<AvailableReport> reports = null;
		try {
			reports = reportService.getAvailableReports(sessionUser.getUsername());
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}

		return reports;
	}

}
