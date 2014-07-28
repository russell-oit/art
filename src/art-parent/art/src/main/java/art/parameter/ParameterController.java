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
package art.parameter;

import art.enums.ParameterType;
import art.report.ReportService;
import art.user.User;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import java.util.ArrayList;
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

	@RequestMapping(value = "/app/parameters", method = RequestMethod.GET)
	public String showParameters(Model model) {
		logger.debug("Entering showParameters");

		try {
			model.addAttribute("parameters", parameterService.getAllParameters());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "parameters";
	}

	@RequestMapping(value = "/app/deleteParameter", method = RequestMethod.POST)
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
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addParameter", method = RequestMethod.GET)
	public String addParameter(Model model) {
		logger.debug("Entering addParameter");

		model.addAttribute("parameter", new Parameter());
		return showParameter("add", model);
	}

	@RequestMapping(value = "/app/editParameter", method = RequestMethod.GET)
	public String editParameter(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editParameter: id={}", id);

		try {
			model.addAttribute("parameter", parameterService.getParameter(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showParameter("edit", model);
	}

	@RequestMapping(value = "/app/saveParameter", method = RequestMethod.POST)
	public String saveParameter(@ModelAttribute("parameter") @Valid Parameter parameter,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveParameter: parameter={}, action='{}'", parameter, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showParameter(action, model);
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
			redirectAttributes.addFlashAttribute("recordName", parameter.getName());
			return "redirect:/app/parameters.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showParameter(action, model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showParameter(String action, Model model) {
		logger.debug("Entering showParameter: action='{}'", action);

		try {
			model.addAttribute("lovReports", reportService.getLovReports());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("parameterTypes", ParameterType.list());
		//model.addAttribute("dataTypes", ParameterDataType.list()); //datatypes loaded automatically?

		model.addAttribute("action", action);
		return "editParameter";
	}

}
