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
package art.reportgroup;

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
 * Controller for report group configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class ReportGroupController {

	private static final Logger logger = LoggerFactory.getLogger(ReportGroupController.class);

	@Autowired
	private ReportGroupService reportGroupService;

	@RequestMapping(value = "/app/reportGroups", method = RequestMethod.GET)
	public String showReportGroups(Model model) {
		logger.debug("Entering showReportGroups");

		try {
			model.addAttribute("groups", reportGroupService.getAllReportGroups());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportGroups";
	}

	@RequestMapping(value = "/app/deleteReportGroup", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReportGroup(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteReportGroup: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = reportGroupService.deleteReportGroup(id);
			
			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//report group not deleted because of linked reports
				response.setData(deleteResult.getData());
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}
	
	@RequestMapping(value = "/app/deleteReportGroups", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReportGroups(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteReportGroups: ids={}", (Object)ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = reportGroupService.deleteReportGroups(ids);
			
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

	@RequestMapping(value = "/app/addReportGroup", method = RequestMethod.GET)
	public String addReportGroup(Model model) {
		logger.debug("Entering addReportGroup");

		model.addAttribute("group", new ReportGroup());
		return showEditReportGroup("add", model);
	}

	@RequestMapping(value = "/app/editReportGroup", method = RequestMethod.GET)
	public String editReportGroup(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editReportGroup: id={}", id);

		try {
			model.addAttribute("group", reportGroupService.getReportGroup(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReportGroup("edit", model);
	}

	@RequestMapping(value = "/app/saveReportGroup", method = RequestMethod.POST)
	public String saveReportGroup(@ModelAttribute("group") @Valid ReportGroup group,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveReportGroup: group={}, action='{}'", group, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditReportGroup(action, model);
		}

		User sessionUser = (User) session.getAttribute("sessionUser");

		try {
			if (StringUtils.equals(action, "add")) {
				reportGroupService.addReportGroup(group, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				reportGroupService.updateReportGroup(group, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", group.getName());
			return "redirect:/app/reportGroups.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReportGroup(action, model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @param session
	 * @return
	 */
	private String showEditReportGroup(String action, Model model) {
		logger.debug("Entering showEditReportGroup: action='{}'", action);

		model.addAttribute("action", action);
		return "editReportGroup";
	}

}
