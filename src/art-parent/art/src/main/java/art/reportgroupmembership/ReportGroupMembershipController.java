/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.reportgroupmembership;

import art.report.ReportService;
import art.reportgroup.ReportGroupService;
import art.general.AjaxResponse;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
 * Controller for report group membership configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class ReportGroupMembershipController {

	private static final Logger logger = LoggerFactory.getLogger(ReportGroupMembershipController.class);
	
	@Autowired
	private ReportGroupMembershipService reportGroupMembershipService;

	@Autowired
	private ReportGroupMembershipService2 reportGroupMembershipService2;

	@Autowired
	private ReportService reportService;

	@Autowired
	private ReportGroupService reportGroupService;

	@RequestMapping(value = "/reportGroupMembership", method = RequestMethod.GET)
	public String showReportGroupMembership(Model model) {
		logger.debug("Entering showReportGroupMembership");

		try {
			model.addAttribute("memberships", reportGroupMembershipService.getAllReportGroupMemberships());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportGroupMembership";
	}

	@RequestMapping(value = "/reportGroupMembershipConfig", method = RequestMethod.GET)
	public String showReportGroupMembershipConfig(Model model) {
		logger.debug("Entering showReportGroupMembershipConfig");

		try {
			model.addAttribute("reports", reportService.getAllReports());
			model.addAttribute("reportGroups", reportGroupService.getAllReportGroups());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportGroupMembershipConfig";
	}

	@RequestMapping(value = "/deleteReportGroupMembership", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReportGroupMembership(@RequestParam("id") String id) {

		logger.debug("Entering deleteReportGroupMembership: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <report id>-<report group id>
		String[] values = StringUtils.split(id, "-");
		int reportId = NumberUtils.toInt(values[0]);
		int reportGroupId = NumberUtils.toInt(values[1]);

		logger.debug("reportId={}", reportId);
		logger.debug("reportGroupId={}", reportGroupId);

		try {
			reportGroupMembershipService2.deleteReportGroupMembership(reportId, reportGroupId);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/updateReportGroupMembership", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse updateReportGroupMembership(Model model, @RequestParam("action") String action,
			@RequestParam("reports[]") Integer[] reports,
			@RequestParam(value = "reportGroups[]", required = false) Integer[] reportGroups) {

		//jquery ajax post appends [] to parameter name where data is an array
		//https://stackoverflow.com/questions/17627056/how-to-pass-multiple-request-parameters-in-spring
		logger.debug("Entering updateReportGroupMembership: action='{}'", action);

		AjaxResponse response = new AjaxResponse();

		try {
			reportGroupMembershipService2.updateReportGroupMembership(action, reports, reportGroups);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}
}
