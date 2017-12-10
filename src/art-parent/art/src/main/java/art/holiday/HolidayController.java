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
package art.holiday;

import art.job.JobService;
import art.jobrunners.UpdateQuartzSchedulesJob;
import art.schedule.ScheduleService;
import art.user.User;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
import art.utils.ArtUtils;
import art.utils.SchedulerUtils;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import static org.quartz.JobKey.jobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
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
 * Controller for holiday configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class HolidayController {

	private static final Logger logger = LoggerFactory.getLogger(HolidayController.class);

	@Autowired
	private HolidayService holidayService;
	
	@Autowired
	private JobService jobService;
	
	@Autowired
	private ScheduleService scheduleService;

	@RequestMapping(value = "/holidays", method = RequestMethod.GET)
	public String showHolidays(Model model) {
		logger.debug("Entering showHolidays");

		try {
			model.addAttribute("holidays", holidayService.getAllHolidays());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "holidays";
	}

	@RequestMapping(value = "/deleteHoliday", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteHoliday(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteHoliday: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = holidayService.deleteHoliday(id);
			
			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//holiday not deleted because of linked schedules or jobs
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteHolidays", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteHolidays(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteHolidays: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = holidayService.deleteHolidays(ids);
			
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

	@RequestMapping(value = "/addHoliday", method = RequestMethod.GET)
	public String addHoliday(Model model) {
		logger.debug("Entering addHoliday");

		model.addAttribute("holiday", new Holiday());

		return showEditHoliday("add", model);
	}

	@RequestMapping(value = "/editHoliday", method = RequestMethod.GET)
	public String editHoliday(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editHoliday: id={}", id);

		try {
			model.addAttribute("holiday", holidayService.getHoliday(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditHoliday("edit", model);
	}

	@RequestMapping(value = "/copyHoliday", method = RequestMethod.GET)
	public String copyHoliday(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering copyHoliday: id={}", id);

		try {
			model.addAttribute("holiday", holidayService.getHoliday(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditHoliday("copy", model);
	}

	@RequestMapping(value = "/saveHoliday", method = RequestMethod.POST)
	public String saveHoliday(@ModelAttribute("holiday") @Valid Holiday holiday,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveHoliday: holiday={}, action='{}'", holiday, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditHoliday(action, model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equals(action, "add") || StringUtils.equals(action, "copy")) {
				holidayService.addHoliday(holiday, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				holidayService.updateHoliday(holiday, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			
			String recordName = holiday.getName() + " (" + holiday.getHolidayId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);

			try {
				updateQuartzSchedules(holiday, sessionUser);
			} catch (SchedulerException ex) {
				logger.error("Error", ex);
				redirectAttributes.addFlashAttribute("error", ex);
			}

			return "redirect:/holidays";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditHoliday(action, model);
	}

	/**
	 * Updates quartz schedules for any jobs that use this holiday as a shared
	 * holiday
	 *
	 * @param holiday the holiday
	 * @param actionUser the user performing this action
	 * @throws SchedulerException
	 */
	private void updateQuartzSchedules(Holiday holiday, User actionUser) throws SchedulerException {
		int holidayId = holiday.getHolidayId();
		String runId = holidayId + "-" + ArtUtils.getUniqueId();

		JobDetail tempJob = newJob(UpdateQuartzSchedulesJob.class)
				.withIdentity(jobKey("updateHolidaysJob-" + runId, "updateHolidaysJobGroup"))
				.usingJobData("holidayId", holidayId)
				.usingJobData("userId", actionUser.getUserId())
				.build();

		// create SimpleTrigger that will fire once, immediately		        
		SimpleTrigger tempTrigger = (SimpleTrigger) newTrigger()
				.withIdentity(triggerKey("updateHolidaysTrigger-" + runId, "updateHolidaysTriggerGroup"))
				.startNow()
				.build();

		Scheduler scheduler = SchedulerUtils.getScheduler();
		scheduler.scheduleJob(tempJob, tempTrigger);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to use
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditHoliday(String action, Model model) {
		logger.debug("Entering showHoliday: action='{}'", action);

		model.addAttribute("action", action);

		return "editHoliday";
	}
	
	@RequestMapping(value = "/recordsWithHoliday", method = RequestMethod.GET)
	public String showRecordsWithHoliday(@RequestParam("holidayId") Integer holidayId, Model model) {
		logger.debug("Entering showRecordsWithHoliday: holidayId={}", holidayId);

		try {
			model.addAttribute("schedules", scheduleService.getSchedulesWithHoliday(holidayId));
			model.addAttribute("jobs", jobService.getJobsWithHoliday(holidayId));
			model.addAttribute("holiday", holidayService.getHoliday(holidayId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "recordsWithHoliday";
	}
}
