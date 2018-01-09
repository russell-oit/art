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
package art.usergroup;

import art.reportgroup.ReportGroupService;
import art.user.User;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
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
 * Controller for user group configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class UserGroupController {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupController.class);

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private ReportGroupService reportGroupService;

	@RequestMapping(value = "/userGroups", method = RequestMethod.GET)
	public String showUserGroups(Model model) {
		logger.debug("Entering showUserGroups");

		try {
			model.addAttribute("groups", userGroupService.getAllUserGroups());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "userGroups";
	}

	@RequestMapping(value = "/deleteUserGroup", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteUserGroup(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteUserGroup: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = userGroupService.deleteUserGroup(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//user group not deleted because of linked users
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteUserGroups", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteUserGroups(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteUserGroups: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = userGroupService.deleteUserGroups(ids);

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

	@RequestMapping(value = "/addUserGroup", method = RequestMethod.GET)
	public String addUserGroup(Model model) {
		logger.debug("Entering addUserGroup");

		model.addAttribute("group", new UserGroup());

		return showEditUserGroup("add", model);
	}

	@RequestMapping(value = "/editUserGroup", method = RequestMethod.GET)
	public String editUserGroup(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editUserGroup: id={}", id);

		try {
			model.addAttribute("group", userGroupService.getUserGroup(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroup("edit", model);
	}

	@RequestMapping(value = "/copyUserGroup", method = RequestMethod.GET)
	public String copyUserGroup(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering copyUserGroup: id={}", id);

		try {
			model.addAttribute("group", userGroupService.getUserGroup(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroup("copy", model);
	}

	@RequestMapping(value = "/saveUserGroup", method = RequestMethod.POST)
	public String saveUserGroup(@ModelAttribute("group") @Valid UserGroup group,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveUserGroup: group={}, action='{}'", group, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUserGroup(action, model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equalsAny(action, "add", "copy")) {
				userGroupService.addUserGroup(group, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				userGroupService.updateUserGroup(group, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = group.getName() + " (" + group.getUserGroupId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/userGroups";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroup(action, model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action. "add" or "edit"
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditUserGroup(String action, Model model) {
		logger.debug("Entering showEditUserGroup: action='{}'", action);

		try {
			model.addAttribute("reportGroups", reportGroupService.getAllReportGroups());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);

		return "editUserGroup";
	}
}
