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
package art.filter;

import art.enums.ParameterDataType;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
 * Controller for filter configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class FilterController {

	private static final Logger logger = LoggerFactory.getLogger(FilterController.class);

	@Autowired
	private FilterService filterService;

	@RequestMapping(value = "/app/filters", method = RequestMethod.GET)
	public String showFilters(Model model) {
		logger.debug("Entering showFilters");

		try {
			model.addAttribute("filters", filterService.getAllFilters());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "filters";
	}

	@RequestMapping(value = "/app/deleteFilter", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteFilter(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteFilter: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			List<String> linkedReports = new ArrayList<>();
			int count = filterService.deleteFilter(id, linkedReports);
			logger.debug("count={}", count);
			if (count == -1) {
				//filter not deleted because of linked records
				response.setData(linkedReports);
			} else {
				response.setSuccess(true);
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addFilter", method = RequestMethod.GET)
	public String addFilter(Model model) {
		logger.debug("Entering addFilter");

		model.addAttribute("filter", new Filter());
		return showFilter("add", model);
	}

	@RequestMapping(value = "/app/saveFilter", method = RequestMethod.POST)
	public String saveFilter(@ModelAttribute("filter") @Valid Filter filter,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveFilter: filter={}, action='{}'", filter, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showFilter(action, model);
		}

		try {
			if (StringUtils.equals(action, "add")) {
				filterService.addFilter(filter);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				filterService.updateFilter(filter);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", filter.getName());
			return "redirect:/app/filters.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showFilter(action, model);
	}

	@RequestMapping(value = "/app/editFilter", method = RequestMethod.GET)
	public String editFilter(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editFilter: id={}", id);

		try {
			model.addAttribute("filter", filterService.getFilter(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showFilter("edit", model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showFilter(String action, Model model) {
		logger.debug("Entering showFilter: action='{}'", action);

		model.addAttribute("dataTypes", ParameterDataType.list());
		model.addAttribute("action", action);
		return "editFilter";
	}

}
