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
package art.rule;

import art.enums.ParameterDataType;
import art.reportrule.ReportRule;
import art.reportrule.ReportRuleService;
import art.user.User;
import art.general.ActionResult;
import art.general.AjaxResponse;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpSession;
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
 * Controller for rule configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class RuleController {

	private static final Logger logger = LoggerFactory.getLogger(RuleController.class);

	@Autowired
	private RuleService ruleService;

	@Autowired
	private ReportRuleService reportRuleService;

	@RequestMapping(value = "/rules", method = RequestMethod.GET)
	public String showRules(Model model) {
		logger.debug("Entering showRules");

		try {
			model.addAttribute("rules", ruleService.getAllRules());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "rules";
	}

	@RequestMapping(value = "/deleteRule", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteRule(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteRule: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = ruleService.deleteRule(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//rule not deleted because of linked reports
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteRules", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteRules(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteRules: id={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = ruleService.deleteRules(ids);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addRule", method = RequestMethod.GET)
	public String addRule(Model model,
			@RequestParam(value = "reportId", required = false) Integer reportId) {

		logger.debug("Entering addRule: reportId={}", reportId);

		model.addAttribute("rule", new Rule());

		Integer returnReportId = null;
		return showEditRule("add", model, reportId, returnReportId);
	}

	@RequestMapping(value = "/editRule", method = RequestMethod.GET)
	public String editRule(@RequestParam("id") Integer id, Model model,
			@RequestParam(value = "returnReportId", required = false) Integer returnReportId) {

		logger.debug("Entering editRule: id={}, returnReportId={}", id, returnReportId);

		try {
			model.addAttribute("rule", ruleService.getRule(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		Integer reportId = null;
		return showEditRule("edit", model, reportId, returnReportId);
	}

	@RequestMapping(value = "/copyRule", method = RequestMethod.GET)
	public String copyRule(@RequestParam("id") Integer id, Model model,
			@RequestParam(value = "reportId", required = false) Integer reportId,
			HttpSession session) {

		logger.debug("Entering copyRule: id={}, reportId={}", id, reportId);

		try {
			model.addAttribute("rule", ruleService.getRule(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		Integer returnReportId = null;
		return showEditRule("copy", model, reportId, returnReportId);
	}

	@RequestMapping(value = "/saveRule", method = RequestMethod.POST)
	public String saveRule(@ModelAttribute("rule") @Valid Rule rule,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("action") String action, @RequestParam("reportId") Integer reportId,
			@RequestParam("returnReportId") Integer returnReportId,
			HttpSession session) {

		logger.debug("Entering saveRule: rule={}, action='{}',"
				+ " reportId={}, returnReportId={}", rule, action, reportId, returnReportId);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditRule(action, model, reportId, returnReportId);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equalsAny(action, "add", "copy")) {
				ruleService.addRule(rule, sessionUser);
				if (reportId != null && reportId != 0) {
					ReportRule reportRule = new ReportRule();
					reportRule.setRule(rule);
					reportRule.setReportId(reportId);
					reportRuleService.addReportRule(reportRule);
				}
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				ruleService.updateRule(rule, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = rule.getName() + " (" + rule.getRuleId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);

			Integer reportRulesReportId = null;
			if (reportId != null && reportId != 0) {
				reportRulesReportId = reportId;
			} else if (returnReportId != null && returnReportId != 0) {
				reportRulesReportId = returnReportId;
			}

			if (reportRulesReportId == null) {
				return "redirect:/rules";
			} else {
				return "redirect:/reportRules?reportId=" + reportRulesReportId;
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditRule(action, model, reportId, returnReportId);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to take
	 * @param model the model to use
	 * @param reportId the report id of the report to add this rule to, null if
	 * not applicable
	 * @param returnReportId the report id to display in the reportRules page
	 * after saving the report, null if not applicable
	 * @return the jsp file to display
	 */
	private String showEditRule(String action, Model model, Integer reportId,
			Integer returnReportId) {

		logger.debug("Entering showEditRule: action='{}', reportId={},"
				+ " returnReportId={}", action, reportId, returnReportId);

		model.addAttribute("dataTypes", ParameterDataType.list());
		model.addAttribute("action", action);
		model.addAttribute("reportId", reportId);
		model.addAttribute("returnReportId", returnReportId);

		return "editRule";
	}
}
