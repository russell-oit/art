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
import art.job.JobService;
import art.jobrunners.UpdateQuartzSchedulesJob;
import art.scheduleholiday.ScheduleHolidayService;
import art.servlets.Config;
import art.user.User;
import art.general.ActionResult;
import art.general.AjaxResponse;
import art.utils.ArtUtils;
import art.utils.CronStringHelper;
import art.utils.SchedulerUtils;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
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

	@Autowired
	private JobService jobService;

	@Autowired
	private ServletContext servletContext;

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
			ActionResult deleteResult = scheduleService.deleteSchedule(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//schedule not deleted because of linked jobs
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
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
			ActionResult deleteResult = scheduleService.deleteSchedules(ids);

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

	@RequestMapping(value = "/getSchedule", method = RequestMethod.GET)
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
	public String addSchedule(Model model, Locale locale) {
		logger.debug("Entering addSchedule");

		Schedule schedule = new Schedule();
		model.addAttribute("schedule", schedule);

		return showEditSchedule("add", model, schedule, locale);
	}

	@RequestMapping(value = "/editSchedule", method = RequestMethod.GET)
	public String editSchedule(@RequestParam("id") Integer id, Model model,
			Locale locale) {

		logger.debug("Entering editSchedule: id={}", id);

		Schedule schedule = null;
		try {
			schedule = scheduleService.getSchedule(id);
			model.addAttribute("schedule", schedule);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSchedule("edit", model, schedule, locale);
	}

	@RequestMapping(value = "/copySchedule", method = RequestMethod.GET)
	public String copySchedule(@RequestParam("id") Integer id, Model model,
			Locale locale) {

		logger.debug("Entering copySchedule: id={}", id);

		Schedule schedule = null;
		try {
			schedule = scheduleService.getSchedule(id);
			model.addAttribute("schedule", schedule);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSchedule("copy", model, schedule, locale);
	}

	@RequestMapping(value = "/saveSchedule", method = RequestMethod.POST)
	public String saveSchedule(@ModelAttribute("schedule") @Valid Schedule schedule,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("action") String action,
			HttpSession session, Locale locale) {

		logger.debug("Entering saveSchedule: schedule={}, action='{}'", schedule, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditSchedule(action, model, schedule, locale);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equalsAny(action, "add", "copy")) {
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

		return showEditSchedule(action, model, schedule, locale);
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

		JobDetail tempJob = JobBuilder.newJob(UpdateQuartzSchedulesJob.class)
				.withIdentity("updateSchedulesJob-" + runId, "updateSchedulesJobGroup")
				.usingJobData("scheduleId", scheduleId)
				.usingJobData("userId", actionUser.getUserId())
				.build();

		// create SimpleTrigger that will fire once, immediately		        
		SimpleTrigger tempTrigger = (SimpleTrigger) TriggerBuilder.newTrigger()
				.withIdentity("updateSchedulesTrigger-" + runId, "updateSchedulesTriggerGroup")
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
	 * @param schedule the schedule
	 * @param locale the locale
	 * @return the jsp file to display
	 */
	private String showEditSchedule(String action, Model model,
			Schedule schedule, Locale locale) {

		logger.debug("Entering showSchedule: action='{}', schedule={}", action, schedule);

		try {
			model.addAttribute("holidays", holidayService.getAllHolidays());

			if (schedule != null && !StringUtils.equals(action, "add")) {
				String cronString = CronStringHelper.getCronString(schedule);
				String mainScheduleDescription = CronStringHelper.getCronScheduleDescription(cronString, locale);
				model.addAttribute("mainScheduleDescription", mainScheduleDescription);
				Date nextRunDate = CronStringHelper.getNextRunDate(cronString);
				model.addAttribute("nextRunDate", nextRunDate);
			}
		} catch (SQLException | RuntimeException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);
		model.addAttribute("serverDateString", ArtUtils.isoDateTimeMillisecondsFormatter.format(new Date()));
		model.addAttribute("serverTimeZoneDescription", Config.getServerTimeZoneDescription());
		model.addAttribute("serverTimeZone", TimeZone.getDefault().getID());
		model.addAttribute("timeZones", Config.getTimeZones());

		return "editSchedule";
	}

	@PostMapping("/describeSchedule")
	public @ResponseBody
	AjaxResponse describeSchedule(@RequestParam("second") String second,
			@RequestParam("minute") String minute,
			@RequestParam("hour") String hour,
			@RequestParam("day") String day,
			@RequestParam("month") String month,
			@RequestParam("weekday") String weekday,
			@RequestParam("year") String year, Locale locale) {

		logger.debug("Entering describeSchedule: second='{}', minute='{}',"
				+ " hour='{}', day='{}', month='{}', weekday='{}', year='{}'",
				second, minute, hour, day, month, weekday, year);

		AjaxResponse response = new AjaxResponse();

		try {
			String cronString = CronStringHelper.getCronString(second, minute, hour, day, month, weekday, year);
			String description = CronStringHelper.getCronScheduleDescription(cronString, locale);
			Date nextRunDate = CronStringHelper.getNextRunDate(cronString);

			String dateDisplayPattern = (String) servletContext.getAttribute("dateDisplayPattern");
			SimpleDateFormat dateFormatter = new SimpleDateFormat(dateDisplayPattern, locale);
			String nextRunDateString = "";
			if (nextRunDate != null) {
				//may be null if the schedule will never run in the future
				nextRunDateString = dateFormatter.format(nextRunDate);
			}

			ScheduleDescription scheduleDescription = new ScheduleDescription();
			scheduleDescription.setDescription(description);
			scheduleDescription.setNextRunDate(nextRunDate);
			scheduleDescription.setNextRunDateString(nextRunDateString);

			response.setData(scheduleDescription);
			response.setSuccess(true);
		} catch (ParseException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/jobsWithSchedule", method = RequestMethod.GET)
	public String showJobsWithSchedule(@RequestParam("scheduleId") Integer scheduleId, Model model) {
		logger.debug("Entering showJobsWithSchedule: scheduleId={}", scheduleId);

		try {
			model.addAttribute("jobs", jobService.getJobsWithSchedule(scheduleId));
			model.addAttribute("schedule", scheduleService.getSchedule(scheduleId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "jobsWithSchedule";
	}
}
