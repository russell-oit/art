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
package art.parameter;

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.report.ReportService;
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
 * Controller for parameter configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class ParameterController {

	private static final Logger logger = LoggerFactory.getLogger(ParameterController.class);

	@Autowired
	private ParameterService parameterService;

	@Autowired
	private ReportService reportService;

	@RequestMapping(value = "/parameters", method = RequestMethod.GET)
	public String showParameters(Model model) {
		logger.debug("Entering showParameters");

		try {
			model.addAttribute("parameters", parameterService.getAllParameters());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "parameters";
	}

	@RequestMapping(value = "/deleteParameter", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteParameter(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteParameter: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = parameterService.deleteParameter(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//parameter not deleted because of linked reports
				response.setData(deleteResult.getData());
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteParameters", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteParameters(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteParameters: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = parameterService.deleteParameters(ids);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				response.setData(deleteResult.getData());
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addParameter", method = RequestMethod.GET)
	public String addParameter(Model model) {
		logger.debug("Entering addParameter");

		Parameter param = new Parameter();
		param.setParameterType(ParameterType.SingleValue);

		model.addAttribute("parameter", param);

		return showEditParameter("add", model);
	}

	@RequestMapping(value = "/editParameter", method = RequestMethod.GET)
	public String editParameter(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editParameter: id={}", id);

		try {
			model.addAttribute("parameter", parameterService.getParameter(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditParameter("edit", model);
	}

	@RequestMapping(value = "/saveParameter", method = RequestMethod.POST)
	public String saveParameter(@ModelAttribute("parameter") @Valid Parameter parameter,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveParameter: parameter={}, action='{}'", parameter, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditParameter(action, model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equals(action, "add")) {
				parameterService.addParameter(parameter, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				parameterService.updateParameter(parameter, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = parameter.getName() + " (" + parameter.getParameterId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/parameters";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditParameter(action, model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to be performed
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditParameter(String action, Model model) {
		logger.debug("Entering showEditParameter: action='{}'", action);

		try {
			model.addAttribute("lovReports", reportService.getLovReports());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("parameterTypes", ParameterType.list());
		//form:options tag by default renders all enum values if no valid items are explicity passed/used
		//https://jira.spring.io/browse/SPR-3389
		//https://stackoverflow.com/questions/15073830/spring-formoptions-tag-with-enum
		model.addAttribute("dataTypes", ParameterDataType.list());

		model.addAttribute("action", action);

		return "editParameter";
	}
}
