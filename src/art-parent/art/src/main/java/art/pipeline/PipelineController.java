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
package art.pipeline;

import art.general.AjaxResponse;
import art.jobrunners.PipelineJob;
import art.user.User;
import art.user.UserService;
import art.utils.ArtUtils;
import art.utils.SchedulerUtils;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for pipeline configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class PipelineController {

	private static final Logger logger = LoggerFactory.getLogger(PipelineController.class);

	@Autowired
	private PipelineService pipelineService;

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/pipelines", method = RequestMethod.GET)
	public String showPipelines(Model model) {
		logger.debug("Entering showPipelines");

		try {
			model.addAttribute("pipelines", pipelineService.getAllPipelines());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "pipelines";
	}

	@RequestMapping(value = "/deletePipeline", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deletePipeline(@RequestParam("id") Integer id) {
		logger.debug("Entering deletePipeline: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			pipelineService.deletePipeline(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deletePipelines", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deletePipelines(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deletePipelines: id={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			pipelineService.deletePipelines(ids);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addPipeline", method = RequestMethod.GET)
	public String addPipeline(Model model) {
		logger.debug("Entering addPipeline");

		model.addAttribute("pipeline", new Pipeline());

		return showEditPipeline("add", model);
	}

	@RequestMapping(value = "/editPipeline", method = RequestMethod.GET)
	public String editPipeline(@RequestParam("id") Integer id, Model model) {

		logger.debug("Entering editPipeline: id={}", id);

		try {
			model.addAttribute("pipeline", pipelineService.getPipeline(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditPipeline("edit", model);
	}

	@RequestMapping(value = "/copyPipeline", method = RequestMethod.GET)
	public String copyPipeline(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering copyPipeline: id={}", id);

		try {
			model.addAttribute("pipeline", pipelineService.getPipeline(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditPipeline("copy", model);
	}

	@RequestMapping(value = "/savePipeline", method = RequestMethod.POST)
	public String savePipeline(@ModelAttribute("pipeline") @Valid Pipeline pipeline,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("action") String action,
			HttpSession session) {

		logger.debug("Entering savePipeline: pipeline={}, action='{}'", pipeline, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditPipeline(action, model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equalsAny(action, "add", "copy")) {
				pipelineService.addPipeline(pipeline, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				pipelineService.updatePipeline(pipeline, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = pipeline.getName() + " (" + pipeline.getPipelineId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);

			return "redirect:/pipelines";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditPipeline(action, model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to take
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditPipeline(String action, Model model) {
		logger.debug("Entering showEditPipeline: action='{}'", action);

		model.addAttribute("action", action);

		return "editPipeline";
	}

	@RequestMapping(value = "/runPipeline", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse runPipeline(@RequestParam("id") Integer id, HttpSession session) {
		logger.debug("Entering runPipeline: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			scheduleTempPipeline(id, sessionUser);
			response.setSuccess(true);
		} catch (SchedulerException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	/**
	 * Schedules a pipeline
	 *
	 * @param pipelineId the pipeline id
	 * @param sessionUser the session user
	 * @throws SchedulerException
	 */
	private void scheduleTempPipeline(Integer pipelineId, User sessionUser)
			throws SchedulerException {

		logger.debug("Entering scheduleTempPipeline: pipelineId={},"
				+ " sessionUser={}", pipelineId, sessionUser);

		String runId = pipelineId + "-" + ArtUtils.getUniqueId();

		String username = sessionUser.getUsername();

		JobDetail tempPipeline = JobBuilder.newJob(PipelineJob.class)
				.withIdentity("tempPipeline-" + runId, "tempPipelineGroup")
				.usingJobData("pipelineId", pipelineId)
				.usingJobData("username", username)
				.usingJobData("tempPipeline", Boolean.TRUE)
				.build();

		Trigger tempTrigger = TriggerBuilder.newTrigger()
				.withIdentity("tempTrigger-" + runId, "tempTriggerGroup")
				.startNow()
				.build();

		Scheduler scheduler = SchedulerUtils.getScheduler();
		scheduler.scheduleJob(tempPipeline, tempTrigger);
	}

	@PostMapping("cancelPipeline")
	public @ResponseBody
	AjaxResponse cancelPipeline(@RequestParam("id") Integer id) {
		logger.debug("Entering cancelPipeline: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			pipelineService.cancelPipeline(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@PostMapping("refreshPipeline")
	public @ResponseBody
	AjaxResponse refreshPipeline(@RequestParam("id") Integer id) {
		logger.debug("Entering refreshPipeline: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			Pipeline pipeline = pipelineService.getPipeline(id);

			response.setData(pipeline);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

}
