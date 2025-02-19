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
package art.reportrule;

import art.report.Report;
import art.rule.RuleService;
import art.report.ReportService;
import art.general.AjaxResponse;
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
 * Controller for report rule configuration pages
 *
 * @author Timothy Anyona
 */
@Controller
public class ReportRuleController {

	private static final Logger logger = LoggerFactory.getLogger(ReportRuleController.class);

	@Autowired
	private ReportRuleService reportRuleService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private RuleService ruleService;

	@RequestMapping(value = "/reportRules", method = RequestMethod.GET)
	public String showReportRules(Model model, @RequestParam("reportId") Integer reportId) {
		logger.debug("Entering showReportRules");

		try {
			model.addAttribute("reportId", reportId);
			String reportName = "";
			Report report = reportService.getReport(reportId);
			if (report != null) {
				reportName = report.getName();
			}
			model.addAttribute("reportName", reportName);
			model.addAttribute("reportRules", reportRuleService.getReportRules(reportId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportRules";
	}

	@RequestMapping(value = "/deleteReportRule", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReportRule(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteReportRule: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			reportRuleService.deleteReportRule(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteReportRules", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReportRules(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteReportRules: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			reportRuleService.deleteReportRules(ids);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addReportRule", method = RequestMethod.GET)
	public String addReportRule(Model model, @RequestParam("reportId") Integer reportId) {
		logger.debug("Entering addReportRule: reportId={}", reportId);

		model.addAttribute("reportRule", new ReportRule());

		return showEditReportRule("add", model, reportId);
	}

	@RequestMapping(value = "/editReportRule", method = RequestMethod.GET)
	public String editReportRule(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editReportRule: id={}", id);

		int reportId = 0;

		try {
			ReportRule reportRule = reportRuleService.getReportRule(id);
			model.addAttribute("reportRule", reportRule);
			if (reportRule != null) {
				reportId = reportRule.getReportId();
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReportRule("edit", model, reportId);
	}

	@RequestMapping(value = "/saveReportRule", method = RequestMethod.POST)
	public String saveReportRule(@ModelAttribute("reportRule") @Valid ReportRule reportRule,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("action") String action,
			@RequestParam("reportId") Integer reportId) {

		logger.debug("Entering saveReportRule: reportRule={}, action='{}', reportId={}", reportRule, action, reportId);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditReportRule(action, model, reportId);
		}

		try {
			if (StringUtils.equals(action, "add")) {
				reportRule.setReportId(reportId);
				reportRuleService.addReportRule(reportRule);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				reportRuleService.updateReportRule(reportRule);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", ruleService.getRuleName(reportRule.getRule().getRuleId()));
			return "redirect:/reportRules?reportId=" + reportId;
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReportRule(action, model, reportId);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to take
	 * @param model the model to use
	 * @param reportId the report id
	 * @param locale the locale
	 * @return the jsp file to display
	 */
	private String showEditReportRule(String action, Model model, Integer reportId) {
		logger.debug("Entering showEditReportRule: action='{}', reportId={}", action, reportId);

		try {
			String reportName = "";
			Report report = reportService.getReport(reportId);
			if (report != null) {
				reportName = report.getName();
			}
			model.addAttribute("reportName", reportName);
			model.addAttribute("rules", ruleService.getAllRules());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("reportId", reportId);
		model.addAttribute("action", action);

		return "editReportRule";
	}
}
