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
package art.fixedparamvalue;

import art.general.AjaxResponse;
import art.parameter.ParameterService;
import art.user.UserService;
import art.usergroup.UserGroupService;
import java.sql.SQLException;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for fixed parameter value configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class FixedParamValueController {

	private static final Logger logger = LoggerFactory.getLogger(FixedParamValueController.class);

	@Autowired
	private FixedParamValueService fixedParamValueService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private ParameterService parameterService;

	@GetMapping("/fixedParamValues")
	public String showFixedParamValues(Model model) {
		logger.debug("Entering showFixedParamValues");

		try {
			model.addAttribute("userFixedParamValues", fixedParamValueService.getAllUserFixedParamValues());
			model.addAttribute("userGroupFixedParamValues", fixedParamValueService.getAllUserGroupFixedParamValues());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "fixedParamValues";
	}

	@GetMapping("/fixedParamValuesConfig")
	public String showFixedParamValuesConfig(Model model) {
		logger.debug("Entering showFixedParamValuesConfig");

		try {
			model.addAttribute("users", userService.getAllUsers());
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
			model.addAttribute("parameters", parameterService.getFixedValueParameters());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "fixedParamValuesConfig";
	}

	@PostMapping("/deleteFixedParamValue")
	public @ResponseBody
	AjaxResponse deleteFixedParamValue(@RequestParam("id") String id) {
		logger.debug("Entering deleteFixedParamValue: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <fixed value type>~<fixed value key>. fixed value key contains a hyphen
		String[] values = StringUtils.split(id, "~");
		String fixedParamValueType = values[0];
		String fixedParamValueKey = values[1];

		try {
			if (StringUtils.equalsIgnoreCase(fixedParamValueType, "userFixedParamValue")) {
				fixedParamValueService.deleteUserFixedParamValue(fixedParamValueKey);
			} else if (StringUtils.equalsIgnoreCase(fixedParamValueType, "userGroupFixedParamValue")) {
				fixedParamValueService.deleteUserGroupFixedParamValue(fixedParamValueKey);
			}
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@PostMapping("/updateFixedParamValue")
	public @ResponseBody
	AjaxResponse updateFixedParamValue(Model model, @RequestParam("action") String action,
			@RequestParam(value = "users[]", required = false) Integer[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups,
			@RequestParam(value = "parameter") Integer parameter,
			@RequestParam(value = "value") String value) {

		logger.debug("Entering updateFixedParamValue: action='{}', parameter={}, value='{}'",
				action, parameter, value);

		AjaxResponse response = new AjaxResponse();

		try {
			if (StringUtils.equalsIgnoreCase(action, "add")) {
				fixedParamValueService.addFixedParamValue(users, userGroups, parameter, value);
			} else {
				fixedParamValueService.deleteFixedParamValues(users, userGroups, parameter);
			}
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/editUserFixedParamValue", method = RequestMethod.GET)
	public String editUserFixedParamValue(@RequestParam("id") String id, Model model,
			@RequestParam(value = "returnParameterId", required = false) Integer returnParameterId) {

		logger.debug("Entering editUserFixedParamValue: id='{}', returnParameterId", id, returnParameterId);

		try {
			model.addAttribute("fixedParamValue", fixedParamValueService.getUserFixedParamValue(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserFixedParamValue(model, returnParameterId);
	}

	@RequestMapping(value = "/saveUserFixedParamValue", method = RequestMethod.POST)
	public String saveUserFixedParamValue(@ModelAttribute("fixedParamValue") @Valid UserFixedParamValue fixedParamValue,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("returnParameterId") Integer returnParameterId) {

		logger.debug("Entering saveUserFixedParamValue: fixedParamValue={},", fixedParamValue);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUserFixedParamValue(model, returnParameterId);
		}

		try {
			fixedParamValueService.updateUserFixedParamValue(fixedParamValue);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");

			String recordName = fixedParamValue.getUser().getUsername() + " - "
					+ fixedParamValue.getParameter().getName()
					+ " - " + fixedParamValue.getValue();

			redirectAttributes.addFlashAttribute("recordName", recordName);

			if (returnParameterId == null || returnParameterId == 0) {
				return "redirect:/fixedParamValues";
			} else {
				return "redirect:/parameterFixedParamValues?parameterId=" + returnParameterId;
			}

		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserFixedParamValue(model, returnParameterId);
	}

	@RequestMapping(value = "/editUserGroupFixedParamValue", method = RequestMethod.GET)
	public String editUserGroupFixedParamValue(@RequestParam("id") String id, Model model,
			@RequestParam(value = "returnParameterId", required = false) Integer returnParameterId) {

		logger.debug("Entering editUserGroupFixedParamValue: id='{}',"
				+ " returnParameterId", id, returnParameterId);

		try {
			model.addAttribute("fixedParamValue", fixedParamValueService.getUserGroupFixedParamValue(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroupFixedParamValue(model, returnParameterId);
	}

	@RequestMapping(value = "/saveUserGroupFixedParamValue", method = RequestMethod.POST)
	public String saveUserGroupFixedParamValue(@ModelAttribute("fixedParamValue") @Valid UserGroupFixedParamValue fixedParamValue,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("returnParameterId") Integer returnParameterId) {

		logger.debug("Entering saveUserGroupFixedParamValue: fixedParamValue={},", fixedParamValue);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUserGroupFixedParamValue(model, returnParameterId);
		}

		try {
			fixedParamValueService.updateUserGroupFixedParamValue(fixedParamValue);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");

			String recordName = fixedParamValue.getUserGroup().getName() + " - "
					+ fixedParamValue.getParameter().getName()
					+ " - " + fixedParamValue.getValue();

			redirectAttributes.addFlashAttribute("recordName", recordName);

			if (returnParameterId == null || returnParameterId == 0) {
				return "redirect:/fixedParamValues";
			} else {
				return "redirect:/parameterFixedParamValues?parameterId=" + returnParameterId;
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroupFixedParamValue(model, returnParameterId);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param model the model to use
	 * @param returnParameterId the parameter id to display in the
	 * parameterFixedParamValues page after saving the user fixed parameter
	 * value, null if not applicable
	 * @return the jsp file to display
	 */
	private String showEditUserFixedParamValue(Model model, Integer returnParameterId) {
		logger.debug("Entering showEditUserFixedParamValue, returnParameterId={}", returnParameterId);

		model.addAttribute("returnParameterId", returnParameterId);

		return "editUserFixedParamValue";
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param model the model to use
	 * @param returnParameterId the parameter id to display in the
	 * parameterFixedParamValues page after saving the user group fixed
	 * parameter value, null if not applicable
	 * @return the jsp file to display
	 */
	private String showEditUserGroupFixedParamValue(Model model, Integer returnParameterId) {
		logger.debug("Entering showEditUserGroupFixedParamValue, returnParameterId={}", returnParameterId);

		model.addAttribute("returnParameterId", returnParameterId);

		return "editUserGroupFixedParamValue";
	}

	@GetMapping("/parameterFixedParamValues")
	public String showParameterFixedParamValues(Model model,
			@RequestParam("parameterId") Integer parameterId) {

		logger.debug("Entering showParameterFixedParamValues: parameterId={}", parameterId);

		try {
			model.addAttribute("parameter", parameterService.getParameter(parameterId));
			model.addAttribute("userFixedParamValues", fixedParamValueService.getUserFixedParamValues(parameterId));
			model.addAttribute("userGroupFixedParamValues", fixedParamValueService.getUserGroupFixedParamValues(parameterId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "parameterFixedParamValues";
	}

}
