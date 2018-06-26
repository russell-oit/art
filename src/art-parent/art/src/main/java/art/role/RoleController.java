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
package art.role;

import art.general.AjaxResponse;
import art.permission.PermissionService;
import art.user.User;
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
 * Controller for role configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class RoleController {

	private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

	@Autowired
	private RoleService roleService;

	@Autowired
	private PermissionService permissionService;

	@RequestMapping(value = "/roles", method = RequestMethod.GET)
	public String showRoles(Model model) {
		logger.debug("Entering showRoles");

		try {
			model.addAttribute("roles", roleService.getAllRoles());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "roles";
	}

	@RequestMapping(value = "/deleteRole", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteRole(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteRole: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			roleService.deleteRole(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteRoles", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteRoles(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteRoles: id={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			roleService.deleteRoles(ids);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addRole", method = RequestMethod.GET)
	public String addRole(Model model) {
		logger.debug("Entering addRole");

		model.addAttribute("role", new Role());

		return showEditRole("add", model);
	}

	@RequestMapping(value = "/editRole", method = RequestMethod.GET)
	public String editRole(@RequestParam("id") Integer id, Model model) {

		logger.debug("Entering editRole: id={}", id);

		try {
			model.addAttribute("role", roleService.getRole(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditRole("edit", model);
	}

	@RequestMapping(value = "/copyRole", method = RequestMethod.GET)
	public String copyRole(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering copyRole: id={}", id);

		try {
			model.addAttribute("role", roleService.getRole(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditRole("copy", model);
	}

	@RequestMapping(value = "/saveRole", method = RequestMethod.POST)
	public String saveRole(@ModelAttribute("role") @Valid Role role,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveRole: role={}, action='{}'", role, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditRole(action, model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equalsAny(action, "add", "copy")) {
				roleService.addRole(role, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				roleService.updateRole(role, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = role.getName() + " (" + role.getRoleId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/roles";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditRole(action, model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to take
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditRole(String action, Model model) {
		logger.debug("Entering showEditRole: action='{}'", action);

		try {
			model.addAttribute("permissions", permissionService.getAllPermissions());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);

		return "editRole";
	}
}
