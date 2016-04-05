/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.reportfilter;

import art.filter.FilterService;
import art.report.ReportService;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
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
 * Controller for report filter configuration pages
 *
 * @author Timothy Anyona
 */
@Controller
public class ReportFilterController {

	private static final Logger logger = LoggerFactory.getLogger(ReportFilterController.class);

	@Autowired
	private ReportFilterService reportFilterService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private FilterService filterService;

	@RequestMapping(value = "/app/reportFilters", method = RequestMethod.GET)
	public String showReportFilters(Model model, @RequestParam("reportId") Integer reportId) {

		logger.debug("Entering showReportFilters");

		try {
			model.addAttribute("reportId", reportId);
			model.addAttribute("reportName", reportService.getReportName(reportId));
			model.addAttribute("reportFilters", reportFilterService.getReportFilters(reportId));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportFilters";
	}

	@RequestMapping(value = "/app/deleteReportFilter", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReportFilter(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteReportFilter: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			reportFilterService.deleteReportFilter(id);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addReportFilter", method = RequestMethod.GET)
	public String addReportFilter(Model model, @RequestParam("reportId") Integer reportId) {
		logger.debug("Entering addReportFilter");

		logger.debug("Entering addReportFilter: reportId={}", reportId);

		model.addAttribute("reportFilter", new ReportFilter());
		return showEditReportFilter("add", model, reportId);
	}
	
	@RequestMapping(value = "/app/editReportFilter", method = RequestMethod.GET)
	public String editReportFilter(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editReportFilter: id={}", id);

		int reportId = 0;

		try {
			ReportFilter reportFilter = reportFilterService.getReportFilter(id);
			model.addAttribute("reportFilter", reportFilter);
			if (reportFilter != null) {
				reportId = reportFilter.getReportId();
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReportFilter("edit", model, reportId);
	}

	@RequestMapping(value = "/app/saveReportFilter", method = RequestMethod.POST)
	public String saveReportFilter(@ModelAttribute("reportFilter") @Valid ReportFilter reportFilter,
			@RequestParam("action") String action,
			@RequestParam("reportId") Integer reportId,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveReportFilter: reportFilter={}, action='{}', reportId={}", reportFilter, action, reportId);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditReportFilter(action, model, reportId);
		}

		try {
			if (StringUtils.equals(action, "add")) {
				reportFilterService.addReportFilter(reportFilter, reportId);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				reportFilterService.updateReportFilter(reportFilter);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", filterService.getFilterName(reportFilter.getFilter().getFilterId()));
			return "redirect:/app/reportFilters.do?reportId=" + reportId;
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReportFilter(action, model, reportId);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showEditReportFilter(String action, Model model, Integer reportId) {
		logger.debug("Entering showEditReportFilter: action='{}', reportId={}", action, reportId);

		try {
			model.addAttribute("reportName", reportService.getReportName(reportId));
			model.addAttribute("filters", filterService.getAllFilters());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("reportId", reportId);
		model.addAttribute("action", action);
		
		return "editReportFilter";
	}

}
