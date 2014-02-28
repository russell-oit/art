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

import art.reportgroup.ReportGroupService;
import art.user.User;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

	@Autowired
	private ReportGroupService reportGroupService;

	@RequestMapping(value = "/app/reports", method = RequestMethod.GET)
	public String showReports(HttpSession session,
			@RequestParam(value = "reportId", required = false) Integer reportGroupId,
			HttpServletRequest request, Model model) {

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			List<AvailableReport> reports = reportService.getAvailableReports(sessionUser.getUsername());

			//allow to focus public_user in one report only. is this feature used? it's not documented
			if (reportGroupId != null) {
				List<AvailableReport> filteredReports = new ArrayList<AvailableReport>();
				for (AvailableReport report : reports) {
					if (report.getReportGroupId() == reportGroupId) {
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

	/**
	 * Return available reports using ajax
	 *
	 * @param session
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/app/getReports", method = RequestMethod.GET)
	public @ResponseBody
	List<AvailableReport> getReports(HttpSession session, HttpServletRequest request) {
		//object will be automatically converted to json because of @ResponseBody and presence of jackson libraries
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

	@RequestMapping(value = "/app/reportsConfig", method = RequestMethod.GET)
	public String showReportsConfig(Model model) {
		try {
			model.addAttribute("reports", reportService.getAllReports());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportsConfig";
	}

	@RequestMapping(value = "/app/deleteReport", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReport(@RequestParam("id") Integer id) {
		AjaxResponse response = new AjaxResponse();

		try {
			reportService.deleteReport(id);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addReport", method = RequestMethod.GET)
	public String addReport(Model model) {
		model.addAttribute("report", new Report());
		return showReport("add", model);
	}

	@RequestMapping(value = "/app/addReport", method = RequestMethod.POST)
	public String addReport(@ModelAttribute("report") @Valid Report report,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showReport("add", model);
		}

		try {
			reportService.addReport(report);
			redirectAttributes.addFlashAttribute("message", "page.message.recordAdded");
			return "redirect:/app/reportsConfig.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport("add", model);
	}

	@RequestMapping(value = "/app/editReport", method = RequestMethod.GET)
	public String editReport(@RequestParam("id") Integer id, Model model) {

		try {
			model.addAttribute("report", reportService.getReport(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport("edit", model);
	}

	@RequestMapping(value = "/app/editReport", method = RequestMethod.POST)
	public String editReport(@ModelAttribute("report") @Valid Report report,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showReport("edit", model);
		}

		try {
			reportService.updateReport(report);
			redirectAttributes.addFlashAttribute("message", "page.message.recordUpdated");
			return "redirect:/app/reportsConfig.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport("edit", model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @param session
	 * @return
	 */
	private String showReport(String action, Model model) {
		try {
			model.addAttribute("reportGroups", reportGroupService.getAllReportGroups());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);
		return "editReport";
	}

}
