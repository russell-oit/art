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
package art.usergroup;

import art.reportgroup.ReportGroupService;
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

	@RequestMapping(value = "/app/userGroups", method = RequestMethod.GET)
	public String showUserGroups(Model model) {
		logger.debug("Entering showUserGroups");

		try {
			model.addAttribute("groups", userGroupService.getAllUserGroups());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "userGroups";
	}

	@RequestMapping(value = "/app/deleteUserGroup", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteUserGroup(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteUserGroup: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			userGroupService.deleteUserGroup(id);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addUserGroup", method = RequestMethod.GET)
	public String addUserGroup(Model model) {
		logger.debug("Entering addUserGroup");

		model.addAttribute("group", new UserGroup());
		return showUserGroup("add", model);
	}

	@RequestMapping(value = "/app/saveUserGroup", method = RequestMethod.POST)
	public String saveUserGroup(@ModelAttribute("group") @Valid UserGroup group,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveUserGroup: group={}, action='{}'", group, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showUserGroup(action, model);
		}

		try {
			if (StringUtils.equals(action, "add")) {
				userGroupService.addUserGroup(group);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				userGroupService.updateUserGroup(group);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", group.getName());
			return "redirect:/app/userGroups.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showUserGroup(action, model);
	}

	@RequestMapping(value = "/app/editUserGroup", method = RequestMethod.GET)
	public String editUserGroup(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editUserGroup: id={}", id);

		try {
			model.addAttribute("group", userGroupService.getUserGroup(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showUserGroup("edit", model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showUserGroup(String action, Model model) {
		logger.debug("Entering showUserGroup: action='{}'", action);

		try {
			model.addAttribute("reportGroups", reportGroupService.getAllReportGroups());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);
		return "editUserGroup";
	}

}
