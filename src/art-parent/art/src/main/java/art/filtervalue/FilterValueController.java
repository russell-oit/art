/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.filtervalue;

import art.filter.FilterService;
import art.user.UserService;
import art.usergroup.UserGroupService;
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
 * Controller for filter value configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class FilterValueController {

	private static final Logger logger = LoggerFactory.getLogger(FilterValueController.class);

	@Autowired
	private FilterValueService filterValueService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private FilterService filterService;

	@RequestMapping(value = "/app/filterValues", method = RequestMethod.GET)
	public String showFilterValues(Model model) {
		logger.debug("Entering showFilterValues");

		try {
			model.addAttribute("userFilterValues", filterValueService.getAllUserFilterValues());
			model.addAttribute("userGroupFilterValues", filterValueService.getAllUserGroupFilterValues());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "filterValues";
	}

	@RequestMapping(value = "/app/filterValuesConfig", method = RequestMethod.GET)
	public String showFilterValuesConfig(Model model, HttpSession session) {
		logger.debug("Entering showFilterValuesConfig");

		try {
			model.addAttribute("users", userService.getAllUsers());
			model.addAttribute("userGroups", userGroupService.getAllUserGroups());
			model.addAttribute("filters", filterService.getAllFilters());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "filterValuesConfig";
	}

	@RequestMapping(value = "/app/deleteFilterValue", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteFilterValue(@RequestParam("id") String id) {

		logger.debug("Entering deleteFilterValue: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <value type>~<filter value key>. filter value key contains a hyphen
		String[] values = StringUtils.split(id, "~");

		try {
			if (StringUtils.equalsIgnoreCase(values[0], "userFilterValue")) {
				filterValueService.deleteUserFilterValue(values[1]);
			} else if (StringUtils.equalsIgnoreCase(values[0], "userGroupFilterValue")) {
				filterValueService.deleteUserGroupFilterValue(values[1]);
			}
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/deleteAllFilterValues", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteAllFilterValues(
			@RequestParam(value = "users[]", required = false) String[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups,
			@RequestParam(value = "filterId") Integer filterId) {

		logger.debug("Entering deleteAllFilterValues: filterId={}", filterId);

		AjaxResponse response = new AjaxResponse();

		try {
			filterValueService.deleteAllFilterValues(users, userGroups, filterId);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/updateFilterValue", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse updateFilterValue(Model model, @RequestParam("action") String action,
			@RequestParam(value = "users[]", required = false) String[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups,
			@RequestParam(value = "filter") String filter,
			@RequestParam(value = "filterValue") String filterValue) {

		logger.debug("Entering updateFilterValue: action='{}', filter='{}', filterValue='{}'",
				action, filter, filterValue);

		AjaxResponse response = new AjaxResponse();

		try {
			if (StringUtils.equalsIgnoreCase(action, "add")) {
				filterValueService.addFilterValue(users, userGroups, filter, filterValue);
			} else {
				Integer filterId = Integer.valueOf(StringUtils.substringBefore(filter, "-"));
				filterValueService.deleteAllFilterValues(users, userGroups, filterId);
			}
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/editUserFilterValue", method = RequestMethod.GET)
	public String editUserFilterValue(@RequestParam("id") String id, Model model) {
		logger.debug("Entering editUserFilterValue: id='{}'", id);

		try {
			model.addAttribute("value", filterValueService.getUserFilterValue(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserFilterValue();
	}

	@RequestMapping(value = "/app/saveUserFilterValue", method = RequestMethod.POST)
	public String saveUserFilterValue(@ModelAttribute("value") @Valid UserFilterValue value,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveUserFilterValue: value={},", value);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUserFilterValue();
		}

		try {
			filterValueService.updateUserFilterValue(value);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");

			String recordName = value.getUser().getUsername() + " - "
					+ value.getFilter().getName() + " - " + value.getFilterValue();

			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/app/filterValues.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserFilterValue();
	}

	@RequestMapping(value = "/app/editUserGroupFilterValue", method = RequestMethod.GET)
	public String editUserGroupFilterValue(@RequestParam("id") String id, Model model) {
		logger.debug("Entering editUserGroupFilterValue: id='{}'", id);

		try {
			model.addAttribute("value", filterValueService.getUserGroupFilterValue(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroupFilterValue();
	}

	@RequestMapping(value = "/app/saveUserGroupFilterValue", method = RequestMethod.POST)
	public String saveUserGroupFilterValue(@ModelAttribute("value") @Valid UserGroupFilterValue value,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveUserGroupFilterValue: value={},", value);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditUserGroupFilterValue();
		}

		try {
			filterValueService.updateUserGroupFilterValue(value);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");

			String recordName = value.getUserGroup().getName() + " - "
					+ value.getFilter().getName() + " - " + value.getFilterValue();

			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/app/filterValues.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditUserGroupFilterValue();
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @return
	 */
	private String showEditUserFilterValue() {
		logger.debug("Entering showEditUserFilterValue");

		return "editUserFilterValue";
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @return
	 */
	private String showEditUserGroupFilterValue() {
		logger.debug("Entering showEditUserGroupFilterValue");

		return "editUserGroupFilterValue";
	}

}
