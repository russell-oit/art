/*
 * ART. A Reporting Tool.
 * Copyright (C) 2020 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.startcondition;

import art.general.ActionResult;
import art.general.AjaxResponse;
import art.job.JobService;
import art.pipeline.PipelineService;
import art.user.User;
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
 * Controller for start condition configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class StartConditionController {

	private static final Logger logger = LoggerFactory.getLogger(StartConditionController.class);

	@Autowired
	private StartConditionService startConditionService;

	@Autowired
	private PipelineService pipelineService;
	
	@Autowired
	private JobService jobService;

	@RequestMapping(value = "/startConditions", method = RequestMethod.GET)
	public String showStartConditions(Model model) {
		logger.debug("Entering showStartConditions");

		try {
			model.addAttribute("startConditions", startConditionService.getAllStartConditions());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "startConditions";
	}

	@RequestMapping(value = "/deleteStartCondition", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteStartCondition(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteStartCondition: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = startConditionService.deleteStartCondition(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//start condition not deleted because of linked records
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteStartConditions", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteStartConditions(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteStartConditions: id={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = startConditionService.deleteStartConditions(ids);

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

	@RequestMapping(value = "/addStartCondition", method = RequestMethod.GET)
	public String addStartCondition(Model model) {
		logger.debug("Entering addStartCondition");

		model.addAttribute("startCondition", new StartCondition());

		return showEditStartCondition("add", model);
	}

	@RequestMapping(value = "/editStartCondition", method = RequestMethod.GET)
	public String editStartCondition(@RequestParam("id") Integer id, Model model) {

		logger.debug("Entering editStartCondition: id={}", id);

		try {
			model.addAttribute("startCondition", startConditionService.getStartCondition(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditStartCondition("edit", model);
	}

	@RequestMapping(value = "/copyStartCondition", method = RequestMethod.GET)
	public String copyStartCondition(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering copyStartCondition: id={}", id);

		try {
			model.addAttribute("startCondition", startConditionService.getStartCondition(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditStartCondition("copy", model);
	}

	@RequestMapping(value = "/saveStartCondition", method = RequestMethod.POST)
	public String saveStartCondition(@ModelAttribute("startCondition") @Valid StartCondition startCondition,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("action") String action,
			HttpSession session) {

		logger.debug("Entering saveStartCondition: startCondition={}, action='{}'",
				startCondition, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditStartCondition(action, model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equalsAny(action, "add", "copy")) {
				startConditionService.addStartCondition(startCondition, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				startConditionService.updateStartCondition(startCondition, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = startCondition.getName() + " (" + startCondition.getStartConditionId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);

			return "redirect:/startConditions";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditStartCondition(action, model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to take
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditStartCondition(String action, Model model) {
		logger.debug("Entering showEditStartCondition: action='{}'", action);

		model.addAttribute("action", action);

		return "editStartCondition";
	}

	@RequestMapping(value = "/startConditionUsage", method = RequestMethod.GET)
	public String showStartConditionUsage(Model model,
			@RequestParam("startConditionId") Integer startConditionId) {

		logger.debug("Entering showStartConditionUsage");

		try {
			model.addAttribute("startCondition", startConditionService.getStartCondition(startConditionId));
			model.addAttribute("pipelines", pipelineService.getPipelinesWithStartCondition(startConditionId));
			model.addAttribute("jobs", jobService.getJobsWithStartCondition(startConditionId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "startConditionUsage";
	}

}
