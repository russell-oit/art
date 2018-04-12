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
package art.paramdefault;

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
 * Controller for parameter default configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class ParamDefaultController {

	private static final Logger logger = LoggerFactory.getLogger(ParamDefaultController.class);

	@Autowired
	private ParamDefaultService paramDefaultService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private ParameterService parameterService;

	@GetMapping("/paramDefaults")
	public String showParamDefaults(Model model) {
		logger.debug("Entering showParamDefaults");

		try {
			model.addAttribute("userParamDefaults", paramDefaultService.getAllUserParamDefaults());
			model.addAttribute("userGroupParamDefaults", paramDefaultService.getAllUserGroupParamDefaults());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "paramDefaults";
	}

	@GetMapping("/paramDefaultsConfig")
	public String showParamDefaultsConfig(Model model) {
		logger.debug("Entering showParamDefaultsConfig");

		try {
			model.addAttribute("users", userService.getAllUsers());
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
			model.addAttribute("parameters", parameterService.getAllParameters());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "paramDefaultsConfig";
	}

	@PostMapping("/deleteParamDefault")
	public @ResponseBody
	AjaxResponse deleteParamDefault(@RequestParam("id") String id) {
		logger.debug("Entering deleteParamDefault: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <param default type>~<param default key>. param default key contains a hyphen
		String[] values = StringUtils.split(id, "~");
		String paramDefaultType = values[0];
		String paramDefaultKey = values[1];

		try {
			if (StringUtils.equalsIgnoreCase(paramDefaultType, "userParamDefault")) {
				paramDefaultService.deleteUserParamDefault(paramDefaultKey);
			} else if (StringUtils.equalsIgnoreCase(paramDefaultType, "userGroupParamDefault")) {
				paramDefaultService.deleteUserGroupParamDefault(paramDefaultKey);
			}
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@PostMapping("/updateParamDefault")
	public @ResponseBody
	AjaxResponse updateParamDefault(Model model, @RequestParam("action") String action,
			@RequestParam(value = "users[]", required = false) Integer[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups,
			@RequestParam(value = "parameter") Integer parameter,
			@RequestParam(value = "value") String value) {

		logger.debug("Entering updateParamDefault: action='{}', parameter={}, value='{}'",
				action, parameter, value);

		AjaxResponse response = new AjaxResponse();

		try {
			if (StringUtils.equalsIgnoreCase(action, "add")) {
				paramDefaultService.addParamDefault(users, userGroups, parameter, value);
			} else {
				paramDefaultService.deleteParamDefaults(users, userGroups, parameter);
			}
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/editUserParamDefault", method = RequestMethod.GET)
	public String editUserParamDefault(@RequestParam("id") String id, Model model,
			@RequestParam(value = "returnParameterId", required = false) Integer returnParameterId) {

		logger.debug("Entering editUserParamDefault: id='{}', returnParameterId", id, returnParameterId);

		try {
			model.addAttribute("paramDefault", paramDefaultService.getUserParamDefault(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserParamDefault(model, returnParameterId);
	}

	@RequestMapping(value = "/saveUserParamDefault", method = RequestMethod.POST)
	public String saveUserParamDefault(@ModelAttribute("paramDefault") @Valid UserParamDefault paramDefault,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("returnParameterId") Integer returnParameterId) {

		logger.debug("Entering saveUserParamDefault: paramDefault={},", paramDefault);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUserParamDefault(model, returnParameterId);
		}

		try {
			paramDefaultService.updateUserParamDefault(paramDefault);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");

			String recordName = paramDefault.getUser().getUsername() + " - "
					+ paramDefault.getParameter().getName()
					+ " - " + paramDefault.getValue();

			redirectAttributes.addFlashAttribute("recordName", recordName);

			if (returnParameterId == null || returnParameterId == 0) {
				return "redirect:/paramDefaults";
			} else {
				return "redirect:/parameterParamDefaults?parameterId=" + returnParameterId;
			}

		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserParamDefault(model, returnParameterId);
	}

	@RequestMapping(value = "/editUserGroupParamDefault", method = RequestMethod.GET)
	public String editUserGroupParamDefault(@RequestParam("id") String id, Model model,
			@RequestParam(value = "returnParameterId", required = false) Integer returnParameterId) {

		logger.debug("Entering editUserGroupParamDefault: id='{}', returnParameterId", id, returnParameterId);

		try {
			model.addAttribute("paramDefault", paramDefaultService.getUserGroupParamDefault(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroupParamDefault(model, returnParameterId);
	}

	@RequestMapping(value = "/saveUserGroupParamDefault", method = RequestMethod.POST)
	public String saveUserGroupParamDefault(@ModelAttribute("paramDefault") @Valid UserGroupParamDefault paramDefault,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("returnParameterId") Integer returnParameterId) {

		logger.debug("Entering saveUserGroupParamDefault: paramDefault={},", paramDefault);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUserGroupParamDefault(model, returnParameterId);
		}

		try {
			paramDefaultService.updateUserGroupParamDefault(paramDefault);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");

			String recordName = paramDefault.getUserGroup().getName() + " - "
					+ paramDefault.getParameter().getName()
					+ " - " + paramDefault.getValue();

			redirectAttributes.addFlashAttribute("recordName", recordName);
			
			if (returnParameterId == null || returnParameterId == 0) {
				return "redirect:/paramDefaults";
			} else {
				return "redirect:/parameterParamDefaults?parameterId=" + returnParameterId;
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroupParamDefault(model, returnParameterId);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param model the model to use
	 * @param returnParameterId the parameter id to display in the
	 * parameterParamDefaults page after saving the user parameter default
	 * value, null if not applicable
	 * @return the jsp file to display
	 */
	private String showEditUserParamDefault(Model model, Integer returnParameterId) {
		logger.debug("Entering showEditUserParamDefault, returnParameterId={}", returnParameterId);

		model.addAttribute("returnParameterId", returnParameterId);

		return "editUserParamDefault";
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param model the model to use
	 * @param returnParameterId the parameter id to display in the
	 * parameterParamDefaults page after saving the user group parameter default
	 * value, null if not applicable
	 * @return the jsp file to display
	 */
	private String showEditUserGroupParamDefault(Model model, Integer returnParameterId) {
		logger.debug("Entering showEditUserGroupParamDefault, returnParameterId={}", returnParameterId);

		model.addAttribute("returnParameterId", returnParameterId);

		return "editUserGroupParamDefault";
	}

	@GetMapping("/parameterParamDefaults")
	public String showParameterParamDefaults(Model model,
			@RequestParam("parameterId") Integer parameterId) {

		logger.debug("Entering showParameterParamDefaults: parameterId={}", parameterId);

		try {
			model.addAttribute("parameter", parameterService.getParameter(parameterId));
			model.addAttribute("userParamDefaults", paramDefaultService.getUserParamDefaults(parameterId));
			model.addAttribute("userGroupParamDefaults", paramDefaultService.getUserGroupParamDefaults(parameterId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "parameterParamDefaults";
	}

}
