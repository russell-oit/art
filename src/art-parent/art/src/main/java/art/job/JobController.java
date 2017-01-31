/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.job;

import art.datasource.DatasourceService;
import art.enums.JobType;
import art.ftpserver.FtpServerService;
import art.jobparameter.JobParameter;
import art.jobparameter.JobParameterService;
import art.jobrunners.ReportJob;
import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportOptions;
import art.schedule.ScheduleService;
import art.servlets.Config;
import art.user.User;
import art.utils.AjaxResponse;
import art.utils.ArtUtils;
import art.utils.SchedulerUtils;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
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
import org.springframework.context.MessageSource;
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
 * Controller for jobs and jobs configuration pages
 *
 * @author Timothy Anyona
 */
@Controller
public class JobController {

	private static final Logger logger = LoggerFactory.getLogger(JobController.class);

	@Autowired
	private JobService jobService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private ScheduleService scheduleService;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private DatasourceService datasourceService;

	@Autowired
	private JobParameterService jobParameterService;

	@Autowired
	private FtpServerService ftpServerService;

	@RequestMapping(value = "/jobs", method = RequestMethod.GET)
	public String showJobs(Model model, HttpSession session) {
		logger.debug("Entering showJobs");

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			List<Job> jobs = jobService.getJobs(sessionUser.getUserId());
			model.addAttribute("jobs", jobs);
			model.addAttribute("nextPage", "jobs");
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", "jobs");

		return "jobs";
	}

	@RequestMapping(value = "/jobsConfig", method = RequestMethod.GET)
	public String showJobsConfig(Model model) {
		logger.debug("Entering showJobsConfig");

		try {
			model.addAttribute("jobs", jobService.getAllJobs());
			model.addAttribute("nextPage", "jobsConfig");
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", "config");

		return "jobs";
	}

	@RequestMapping(value = "/deleteJob", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteJob(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteJob: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			jobService.deleteJob(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException | SchedulerException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteJobs", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteJobs(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteJobs: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			jobService.deleteJobs(ids);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException | SchedulerException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/refreshJob", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse refreshJob(@RequestParam("id") Integer id, Locale locale) {
		logger.debug("Entering refreshJob: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			Job job = jobService.getJob(id);

			String lastRunMessage = job.getLastRunMessage();
			if (StringUtils.isNotBlank(lastRunMessage)) {
				lastRunMessage = messageSource.getMessage(lastRunMessage, null, locale);
				job.setLastRunMessage(lastRunMessage);
			}
			String lastEndDateString = Config.getDateDisplayString(job.getLastEndDate());
			job.setLastEndDateString(lastEndDateString);
			String nextRunDateString = Config.getDateDisplayString(job.getNextRunDate());
			job.setNextRunDateString(nextRunDateString);

			response.setData(job);

			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/runJob", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse runJob(@RequestParam("id") Integer id, HttpServletRequest request) {
		logger.debug("Entering runJob: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			String runId = id + "-" + ArtUtils.getUniqueId();

			JobDetail tempJob = newJob(ReportJob.class)
					.withIdentity(jobKey("tempJob-" + runId, "tempJobGroup"))
					.usingJobData("jobId", id)
					.usingJobData("tempJob", Boolean.TRUE)
					.build();

			// create SimpleTrigger that will fire once, immediately		        
			SimpleTrigger tempTrigger = (SimpleTrigger) newTrigger()
					.withIdentity(triggerKey("tempTrigger-" + runId, "tempTriggerGroup"))
					.startNow()
					.build();

			Scheduler scheduler = SchedulerUtils.getScheduler();
			scheduler.scheduleJob(tempJob, tempTrigger);
			response.setSuccess(true);
		} catch (SchedulerException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/runLaterJob", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse runLaterJob(@RequestParam("runLaterJobId") Integer runLaterJobId,
			@RequestParam("runLaterDate") String runLaterDate,
			HttpServletRequest request) {

		logger.debug("Entering runLaterJob: runLaterJobId={}, runLaterDate='{}'",
				runLaterJobId, runLaterDate);

		AjaxResponse response = new AjaxResponse();

		try {
			String runId = runLaterJobId + "-" + ArtUtils.getUniqueId();

			JobDetail tempJob = newJob(ReportJob.class)
					.withIdentity(jobKey("tempJob-" + runId, "tempJobGroup"))
					.usingJobData("jobId", runLaterJobId)
					.usingJobData("tempJob", Boolean.TRUE)
					.build();

			ParameterProcessor parameterProcessor = new ParameterProcessor();
			Date runDate = parameterProcessor.convertParameterStringValueToDate(runLaterDate);

			// create SimpleTrigger that will fire once at the given date		        
			SimpleTrigger tempTrigger = (SimpleTrigger) newTrigger()
					.withIdentity(triggerKey("tempTrigger-" + runId, "tempTriggerGroup"))
					.startAt(runDate)
					.build();

			Scheduler scheduler = SchedulerUtils.getScheduler();
			scheduler.scheduleJob(tempJob, tempTrigger);
			response.setSuccess(true);
		} catch (SchedulerException | RuntimeException | ParseException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addJob", method = {RequestMethod.GET, RequestMethod.POST})
	public String addJob(Model model, HttpServletRequest request, HttpSession session) {
		logger.debug("Entering addJob");

		try {
			Job job = new Job();
			job.setActive(true);

			String reportIdString = request.getParameter("reportId");
			if (reportIdString != null) {
				Report report = reportService.getReport(Integer.parseInt(reportIdString));
				job.setReport(report);
				job.setName(report.getName());
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			job.setUser(sessionUser);
			job.setMailFrom(sessionUser.getEmail());

			model.addAttribute("job", job);

			ParameterProcessor parameterProcessor = new ParameterProcessor();
			ParameterProcessorResult paramProcessorResult = parameterProcessor.processHttpParameters(request);
			addParameters(model, paramProcessorResult);
		} catch (SQLException | RuntimeException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditJob("add", model);
	}

	@RequestMapping(value = "/saveJob", method = RequestMethod.POST)
	public String saveJob(@ModelAttribute("job") @Valid Job job,
			@RequestParam("action") String action, @RequestParam("nextPage") String nextPage,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session, HttpServletRequest request) {

		logger.debug("Entering saveJob: job={}, action='{}', nextPage='{}'", job, action, nextPage);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditJob(action, model);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			finalizeSchedule(job);

			if (StringUtils.equals(action, "add")) {
				jobService.addJob(job, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				jobService.updateJob(job, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			createQuartzJob(job);

			saveJobParameters(request, job.getJobId());

			redirectAttributes.addFlashAttribute("recordName", job.getName());
			redirectAttributes.addFlashAttribute("record", job);
			return "redirect:/" + nextPage;
		} catch (SQLException | RuntimeException | SchedulerException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditJob(action, model);
	}

	@RequestMapping(value = "/saveJobs", method = RequestMethod.POST)
	public String saveJobs(@ModelAttribute("multipleJobEdit") @Valid MultipleJobEdit multipleJobEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveJobs: multipleJobEdit={}", multipleJobEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditJobs();
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			jobService.updateJobs(multipleJobEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleJobEdit.getIds());
			return "redirect:/jobsConfig";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditJobs();
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @return the jsp file to display
	 */
	private String showEditJobs() {
		logger.debug("Entering showEditJobs");
		return "editJobs";
	}

	/**
	 * Saves job parameters
	 *
	 * @param request the http request that contains the job parameters
	 * @param jobId the job's id
	 * @throws NumberFormatException
	 * @throws SQLException
	 */
	private void saveJobParameters(HttpServletRequest request, int jobId)
			throws NumberFormatException, SQLException {

		logger.debug("Entering saveJobParameters: jobId={}", jobId);

		Map<String, String[]> passedValues = new HashMap<>();
		
		Map<String, String[]> requestParameters = request.getParameterMap();
		for (Entry<String, String[]> entry : requestParameters.entrySet()) {
			String htmlParamName = entry.getKey();
			logger.debug("htmlParamName='{}'", htmlParamName);

			if (htmlParamName.startsWith("p-")) {
				String[] paramValues = entry.getValue();
				passedValues.put(htmlParamName, paramValues);
			}
		}

		jobParameterService.deleteJobParameters(jobId);

		//add report parameters
		for (Entry<String, String[]> entry : passedValues.entrySet()) {
			String name = entry.getKey();
			String[] values = entry.getValue();
			for (String value : values) {
				JobParameter jobParam = new JobParameter();
				jobParam.setJobId(jobId);
				jobParam.setName(name);
				jobParam.setValue(value);
				jobParam.setParamTypeString("X");
				jobParameterService.addJobParameter(jobParam);
			}
		}

		//add report options
		String showSelectedParametersValue = request.getParameter("showSelectedParameters");
		if (showSelectedParametersValue != null) {
			JobParameter jobParam = new JobParameter();
			jobParam.setJobId(jobId);
			jobParam.setName("showSelectedParameters");
			jobParam.setValue("true");
			jobParam.setParamTypeString("X");
			jobParameterService.addJobParameter(jobParam);
		}
	}

	@RequestMapping(value = "/editJob", method = RequestMethod.GET)
	public String editJob(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editJob: id={}", id);

		try {
			Job job = jobService.getJob(id);
			model.addAttribute("job", job);

			ReportJob reportJob = new ReportJob();
			int reportId = job.getReport().getReportId();
			ParameterProcessorResult paramProcessorResult = reportJob.buildParameters(reportId, id);
			addParameters(model, paramProcessorResult);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditJob("edit", model);
	}

	/**
	 * Adds report parameters, report options and chart options to the model
	 *
	 * @param model the model
	 * @param paramProcessorResult the parameter processor result that contains
	 * report parameters, report options and chart options
	 */
	private void addParameters(Model model, ParameterProcessorResult paramProcessorResult) {
		List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();

		//create map in order to display parameters by position
		Map<Integer, ReportParameter> reportParams = new TreeMap<>();
		for (ReportParameter reportParam : reportParamsList) {
			reportParams.put(reportParam.getPosition(), reportParam);
		}

		model.addAttribute("reportParams", reportParams);

		//add report options for the showSelectedParameters option
		ReportOptions reportOptions = paramProcessorResult.getReportOptions();
		model.addAttribute("reportOptions", reportOptions);
	}

	@RequestMapping(value = "/editJobs", method = RequestMethod.GET)
	public String editJobs(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editJobs: ids={}", ids);

		MultipleJobEdit multipleJobEdit = new MultipleJobEdit();
		multipleJobEdit.setIds(ids);

		model.addAttribute("multipleJobEdit", multipleJobEdit);

		return "editJobs";
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action "add" or "edit"
	 * @param model the spring model
	 * @return the jsp file to display
	 */
	private String showEditJob(String action, Model model) {
		logger.debug("Entering showEditJob: action='{}'", action);

		model.addAttribute("action", action);

		model.addAttribute("jobTypes", JobType.list());

		try {
			model.addAttribute("dynamicRecipientReports", reportService.getDynamicRecipientReports());
			model.addAttribute("schedules", scheduleService.getAllSchedules());
			model.addAttribute("datasources", datasourceService.getAllDatasources());
			model.addAttribute("ftpServers", ftpServerService.getAllFtpServers());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "editJob";
	}

	/**
	 * Processes the job schedule details
	 *
	 * @param job the job to schedule
	 * @throws ParseException
	 */
	private void finalizeSchedule(Job job) throws ParseException {
		logger.debug("Entering finalizeSchedule: job={}", job);

		//create quartz job to be running this job
		//build cron expression for the schedule
		String minute;
		String hour;
		String day;
		String weekday;
		String month;
		String second = "0"; //seconds always 0
		String actualHour; //allow hour and minute to be left blank, in which case random values are used
		String actualMinute; //allow hour and minute to be left blank, in which case random values are used

		actualMinute = job.getScheduleMinute();
		actualMinute = StringUtils.deleteWhitespace(actualMinute); // cron fields shouldn't have any spaces in them
		minute = actualMinute;

		actualHour = job.getScheduleHour();
		actualHour = StringUtils.deleteWhitespace(actualHour);
		hour = actualHour;

		//enable definition of random start time
		if (StringUtils.contains(actualHour, "|")) {
			String startPart = StringUtils.substringBefore(actualHour, "|");
			String endPart = StringUtils.substringAfter(actualHour, "|");
			String startHour = StringUtils.substringBefore(startPart, ":");
			String startMinute = StringUtils.substringAfter(startPart, ":");
			String endHour = StringUtils.substringBefore(endPart, ":");
			String endMinute = StringUtils.substringAfter(endPart, ":");

			if (StringUtils.isBlank(startMinute)) {
				startMinute = "0";
			}
			if (StringUtils.isBlank(endMinute)) {
				endMinute = "0";
			}

			Date now = new Date();

			java.util.Calendar calStart = java.util.Calendar.getInstance();
			calStart.setTime(now);
			calStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
			calStart.set(Calendar.MINUTE, Integer.parseInt(startMinute));

			Calendar calEnd = Calendar.getInstance();
			calEnd.setTime(now);
			calEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
			calEnd.set(Calendar.MINUTE, Integer.parseInt(endMinute));

			long randomDate = ArtUtils.getRandomNumber(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
			Calendar calRandom = Calendar.getInstance();
			calRandom.setTimeInMillis(randomDate);

			hour = String.valueOf(calRandom.get(Calendar.HOUR_OF_DAY));
			minute = String.valueOf(calRandom.get(Calendar.MINUTE));
		}

		if (minute.length() == 0) {
			//no minute defined. use random value
			minute = String.valueOf(ArtUtils.getRandomNumber(0, 59));
		}

		if (hour.length() == 0) {
			//no hour defined. use random value between 3-6
			hour = String.valueOf(ArtUtils.getRandomNumber(3, 6));
		}

		month = StringUtils.deleteWhitespace(job.getScheduleMonth());
		if (month.length() == 0) {
			//no month defined. default to every month
			month = "*";
		}

		day = StringUtils.deleteWhitespace(job.getScheduleDay());
		weekday = StringUtils.deleteWhitespace(job.getScheduleWeekday());

		//set default day of the month if weekday is defined
		if (day.length() == 0 && weekday.length() >= 1 && !weekday.equals("?")) {
			//weekday defined but day of the month is not. default day to ?
			day = "?";
		}

		if (day.length() == 0) {
			//no day of month defined. default to *
			day = "*";
		}

		if (weekday.length() == 0) {
			//no day of week defined. default to undefined
			weekday = "?";
		}

		if (day.equals("?") && weekday.equals("?")) {
			//unsupported. only one can be ?
			day = "*";
			weekday = "?";
		}
		if (day.equals("*") && weekday.equals("*")) {
			//unsupported. only one can be defined
			day = "*";
			weekday = "?";
		}

		//build cron expression.
		//cron format is sec min hr dayofmonth month dayofweek (optionally year)
		String cronString = second + " " + minute + " " + hour + " " + day + " " + month + " " + weekday;

		logger.debug("cronString='{}'", cronString);

		String startDateString = job.getStartDateString();
		if (StringUtils.isBlank(startDateString)) {
			startDateString = "now";
		}
		ParameterProcessor parameterProcessor = new ParameterProcessor();
		Date startDate = parameterProcessor.convertParameterStringValueToDate(startDateString);
		job.setStartDate(startDate);

		String endDateString = job.getEndDateString();
		Date endDate;
		if (StringUtils.isBlank(endDateString)) {
			endDate = null;
		} else {
			endDate = parameterProcessor.convertParameterStringValueToDate(endDateString);
		}
		job.setEndDate(endDate);

		CronTrigger tempTrigger = newTrigger()
				.withSchedule(cronSchedule(cronString))
				.startAt(startDate)
				.endAt(endDate)
				.build();

		Date nextRunDate = tempTrigger.getFireTimeAfter(new Date());

		job.setNextRunDate(nextRunDate);

		job.setScheduleMinute(minute);
		job.setScheduleHour(hour);
		job.setScheduleDay(day);
		job.setScheduleMonth(month);
		job.setScheduleWeekday(weekday);

		job.setStartDate(startDate);
		job.setEndDate(endDate);
	}

	/**
	 * Creates a quartz job for the given art job
	 *
	 * @param job the art job
	 * @throws SchedulerException
	 */
	private void createQuartzJob(Job job) throws SchedulerException {
		Scheduler scheduler = SchedulerUtils.getScheduler();

		if (scheduler == null) {
			logger.warn("Scheduler not available");
			return;
		}

		int jobId = job.getJobId();

		String jobName = "job" + jobId;
		String triggerName = "trigger" + jobId;

		JobDetail quartzJob = newJob(ReportJob.class)
				.withIdentity(jobKey(jobName, ArtUtils.JOB_GROUP))
				.usingJobData("jobId", jobId)
				.build();

		//build cron expression.
		//cron format is sec min hr dayofmonth month dayofweek (optionally year)
		String second = "0";
		String cronString = second + " " + job.getScheduleMinute()
				+ " " + job.getScheduleHour() + " " + job.getScheduleDay()
				+ " " + job.getScheduleMonth() + " " + job.getScheduleWeekday();

		//create trigger that defines the schedule for the job
		CronTrigger trigger = newTrigger()
				.withIdentity(triggerKey(triggerName, ArtUtils.TRIGGER_GROUP))
				.withSchedule(cronSchedule(cronString))
				.startAt(job.getStartDate())
				.endAt(job.getEndDate())
				.build();

		//delete any existing jobs or triggers with the same id before adding them to the scheduler
		scheduler.deleteJob(jobKey(jobName, ArtUtils.JOB_GROUP));
		scheduler.unscheduleJob(triggerKey(triggerName, ArtUtils.TRIGGER_GROUP));

		//add job and trigger to scheduler
		scheduler.scheduleJob(quartzJob, trigger);
	}

}
