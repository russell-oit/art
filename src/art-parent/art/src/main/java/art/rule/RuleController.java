/*
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
package art.rule;

import art.enums.ParameterDataType;
import art.user.User;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
import java.sql.SQLException;
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

	@RequestMapping(value = "/app/rules", method = RequestMethod.GET)
	public String showRules(Model model) {
		logger.debug("Entering showRules");

		try {
			model.addAttribute("rules", ruleService.getAllRules());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "rules";
	}

	@RequestMapping(value = "/app/deleteRule", method = RequestMethod.POST)
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
				response.setData(deleteResult.getData());
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}
	
	@RequestMapping(value = "/app/deleteRules", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteRules(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteRules: id={}", (Object)ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = ruleService.deleteRules(ids);
			
			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				response.setData(deleteResult.getData());
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addRule", method = RequestMethod.GET)
	public String addRule(Model model) {
		logger.debug("Entering addRule");

		model.addAttribute("rule", new Rule());
		return showEditRule("add", model);
	}

	@RequestMapping(value = "/app/editRule", method = RequestMethod.GET)
	public String editRule(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editRule: id={}", id);

		try {
			model.addAttribute("rule", ruleService.getRule(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditRule("edit", model);
	}

	@RequestMapping(value = "/app/saveRule", method = RequestMethod.POST)
	public String saveRule(@ModelAttribute("rule") @Valid Rule rule,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveRule: rule={}, action='{}'", rule, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditRule(action, model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equals(action, "add")) {
				ruleService.addRule(rule, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				ruleService.updateRule(rule, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", rule.getName());
			return "redirect:/app/rules.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditRule(action, model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showEditRule(String action, Model model) {
		logger.debug("Entering showEditRule: action='{}'", action);

		model.addAttribute("dataTypes", ParameterDataType.list());
		model.addAttribute("action", action);
		return "editRule";
	}

}
