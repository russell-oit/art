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
package art.schedule;

import art.holiday.HolidayService;
import art.jobrunners.UpdateQuartzSchedulesJob;
import art.scheduleholiday.ScheduleHolidayService;
import art.user.User;
import art.utils.AjaxResponse;
import art.utils.ArtUtils;
import art.utils.SchedulerUtils;
import java.sql.SQLException;
import java.util.Date;
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
 * Controller for schedule configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class ScheduleController {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

	@Autowired
	private ScheduleService scheduleService;
	
	@Autowired
	private ScheduleHolidayService scheduleHolidayService;
	
	@Autowired
	private HolidayService holidayService;

	@RequestMapping(value = "/schedules", method = RequestMethod.GET)
	public String showSchedules(Model model) {
		logger.debug("Entering showSchedules");

		try {
			model.addAttribute("schedules", scheduleService.getAllSchedules());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "schedules";
	}

	@RequestMapping(value = "/deleteSchedule", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteSchedule(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteSchedule: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			scheduleService.deleteSchedule(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteSchedules", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteSchedules(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteSchedules: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			scheduleService.deleteSchedules(ids);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/getSchedule", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse getSchedule(@RequestParam("id") Integer id) {
		logger.debug("Entering getSchedule: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			Schedule schedule = scheduleService.getSchedule(id);
			response.setSuccess(true);
			response.setData(schedule);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addSchedule", method = RequestMethod.GET)
	public String addSchedule(Model model) {
		logger.debug("Entering addSchedule");

		model.addAttribute("schedule", new Schedule());

		return showEditSchedule("add", model);
	}

	@RequestMapping(value = "/editSchedule", method = RequestMethod.GET)
	public String editSchedule(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editSchedule: id={}", id);

		try {
			model.addAttribute("schedule", scheduleService.getSchedule(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSchedule("edit", model);
	}

	@RequestMapping(value = "/copySchedule", method = RequestMethod.GET)
	public String copySchedule(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering copySchedule: id={}", id);

		try {
			model.addAttribute("schedule", scheduleService.getSchedule(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSchedule("copy", model);
	}

	@RequestMapping(value = "/saveSchedule", method = RequestMethod.POST)
	public String saveSchedule(@ModelAttribute("schedule") @Valid Schedule schedule,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveSchedule: schedule={}, action='{}'", schedule, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditSchedule(action, model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equals(action, "add") || StringUtils.equals(action, "copy")) {
				scheduleService.addSchedule(schedule, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				scheduleService.updateSchedule(schedule, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			try {
				scheduleHolidayService.recreateScheduleHolidays(schedule);
				updateQuartzSchedules(schedule, sessionUser);
			} catch (SchedulerException | SQLException | RuntimeException ex) {
				logger.error("Error", ex);
				redirectAttributes.addFlashAttribute("error", ex);
			}

			String recordName = schedule.getName() + " (" + schedule.getScheduleId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/schedules";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSchedule(action, model);
	}

	/**
	 * Updates quartz schedules for any jobs that use this schedule as a fixed
	 * schedule
	 *
	 * @param schedule the schedule
	 * @param actionUser the user performing this action
	 * @throws SchedulerException
	 */
	private void updateQuartzSchedules(Schedule schedule, User actionUser) throws SchedulerException {
		int scheduleId = schedule.getScheduleId();
		String runId = scheduleId + "-" + ArtUtils.getUniqueId();

		JobDetail tempJob = newJob(UpdateQuartzSchedulesJob.class)
				.withIdentity(jobKey("updateSchedulesJob-" + runId, "updateSchedulesJobGroup"))
				.usingJobData("scheduleId", scheduleId)
				.usingJobData("userId", actionUser.getUserId())
				.build();

		// create SimpleTrigger that will fire once, immediately		        
		SimpleTrigger tempTrigger = (SimpleTrigger) newTrigger()
				.withIdentity(triggerKey("updateSchedulesTrigger-" + runId, "updateSchedulesTriggerGroup"))
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
	private String showEditSchedule(String action, Model model) {
		logger.debug("Entering showSchedule: action='{}'", action);
		
		try{
			model.addAttribute("holidays", holidayService.getAllHolidays());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);
		model.addAttribute("serverDateString", ArtUtils.isoDateTimeMillisecondsFormatter.format(new Date()));

		return "editSchedule";
	}
}
