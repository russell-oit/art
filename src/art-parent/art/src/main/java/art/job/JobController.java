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
package art.job;

import art.datasource.DatasourceService;
import art.destination.DestinationService;
import art.enums.JobType;
import art.enums.ReportType;
import art.holiday.HolidayService;
import art.jobdestination.JobDestinationService;
import art.jobholiday.JobHolidayService;
import art.jobparameter.JobParameter;
import art.jobparameter.JobParameterService;
import art.jobrunners.ReportJob;
import art.report.Report;
import art.report.ReportService;
import art.report.UploadHelper;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.RunReportHelper;
import art.schedule.ScheduleService;
import art.servlets.Config;
import art.smtpserver.SmtpServerService;
import art.user.User;
import art.general.AjaxResponse;
import art.startcondition.StartConditionService;
import art.user.UserService;
import art.utils.ArtHelper;
import art.utils.ArtUtils;
import art.utils.CronStringHelper;
import art.utils.ExpressionHelper;
import art.utils.SchedulerUtils;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
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
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
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
	private JobHolidayService jobHolidayService;

	@Autowired
	private HolidayService holidayService;

	@Autowired
	private DestinationService destinationService;

	@Autowired
	private JobDestinationService jobDestinationService;

	@Autowired
	private SmtpServerService smtpServerService;

	@Autowired
	private UserService userService;

	@Autowired
	private StartConditionService startConditionService;

	@RequestMapping(value = "/jobs", method = RequestMethod.GET)
	public String showJobs(Model model, HttpSession session) {
		logger.debug("Entering showJobs");

		List<Job> jobs = null;
		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			jobs = jobService.getUserJobs(sessionUser.getUserId());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		String action = "jobs";
		String nextPage = "jobs";

		return showJobsPage(action, model, nextPage, jobs);
	}

	@RequestMapping(value = "/jobsConfig", method = RequestMethod.GET)
	public String showJobsConfig(Model model) {
		logger.debug("Entering showJobsConfig");

		List<Job> jobs = null;
		try {
			jobs = jobService.getAllJobs();
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		String action = "config";
		String nextPage = "jobsConfig";

		return showJobsPage(action, model, nextPage, jobs);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action "jobs" or "config"
	 * @param model the spring model
	 * @param nextPage the page to display after editing a job
	 * @param jobs the jobs to display
	 * @return the jsp file to display
	 */
	private String showJobsPage(String action, Model model, String nextPage, List<Job> jobs) {
		logger.debug("Entering showJobsPage: action='{}', nextPage='{}'", action, nextPage);

		List<JobRunDetails> runningJobs = Config.getRunningJobs();

		for (JobRunDetails runningJob : runningJobs) {
			for (Job job : jobs) {
				if (runningJob.getJob().getJobId() == job.getJobId()) {
					job.setRunning(true);
					break;
				}
			}
		}

		model.addAttribute("action", action);
		model.addAttribute("nextPage", nextPage);
		model.addAttribute("jobs", jobs);
		model.addAttribute("serverDateString", ArtUtils.isoDateTimeMillisecondsFormatter.format(new Date()));
		model.addAttribute("serverTimeZoneDescription", Config.getServerTimeZoneDescription());

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
			String lastEndDateString = Config.getDateTimeDisplayString(job.getLastEndDate());
			job.setLastEndDateString(lastEndDateString);
			String nextRunDateString = Config.getDateTimeDisplayString(job.getNextRunDate());
			job.setNextRunDateString(nextRunDateString);

			List<JobRunDetails> runningJobs = Config.getRunningJobs();

			for (JobRunDetails runningJob : runningJobs) {
				if (runningJob.getJob().getJobId() == job.getJobId()) {
					job.setRunning(true);
					break;
				}
			}

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
	AjaxResponse runJob(@RequestParam("id") Integer id, HttpSession session) {
		logger.debug("Entering runJob: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			Date runDate = null;
			scheduleTempJob(id, runDate, sessionUser);
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
			HttpSession session) {

		logger.debug("Entering runLaterJob: runLaterJobId={}, runLaterDate='{}'",
				runLaterJobId, runLaterDate);

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			ExpressionHelper expressionHelper = new ExpressionHelper();
			Date runDate = expressionHelper.convertStringToDate(runLaterDate);

			scheduleTempJob(runLaterJobId, runDate, sessionUser);

			response.setSuccess(true);
		} catch (SchedulerException | RuntimeException | ParseException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	/**
	 * Schedules a run job or run later job
	 *
	 * @param jobId the job id
	 * @param runDate the run date, or null if to run immediately
	 * @param sessionUser the session user
	 * @throws SchedulerException
	 */
	private void scheduleTempJob(Integer jobId, Date runDate, User sessionUser)
			throws SchedulerException {

		logger.debug("Entering scheduleTempJob: jobId={}, runDate={},"
				+ " sessionUser={}", jobId, runDate, sessionUser);

		String runId = jobId + "-" + ArtUtils.getUniqueId();

		String username = sessionUser.getUsername();

		JobDetail tempJob = JobBuilder.newJob(ReportJob.class)
				.withIdentity("tempJob-" + runId, ArtUtils.TEMP_JOB_GROUP)
				.usingJobData("jobId", jobId)
				.usingJobData("username", username)
				.usingJobData("tempJob", true)
				.build();

		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
				.withIdentity("tempTrigger-" + runId, ArtUtils.TEMP_TRIGGER_GROUP);

		if (runDate == null) {
			//create SimpleTrigger that will fire once, immediately
			triggerBuilder.startNow();
		} else {
			//create SimpleTrigger that will fire once at the given date
			triggerBuilder.startAt(runDate);
		}

		Trigger tempTrigger = triggerBuilder.build();

		Scheduler scheduler = SchedulerUtils.getScheduler();
		scheduler.scheduleJob(tempJob, tempTrigger);
	}

	@RequestMapping(value = "/addJob", method = {RequestMethod.GET, RequestMethod.POST})
	public String addJob(Model model, HttpServletRequest request, HttpSession session,
			Locale locale) {

		logger.debug("Entering addJob");

		Job job = new Job();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			String reportIdString = request.getParameter("reportId");
			if (reportIdString != null) {
				int reportId = Integer.parseInt(reportIdString);
				if (!reportService.canUserRunReport(sessionUser, reportId)) {
					return "redirect:/accessDenied";
				}
				Report report = reportService.getReport(reportId);
				job.setReport(report);
				job.setName(report.getLocalizedName(locale));
			}

			job.setUser(sessionUser);
			job.setMailFrom(sessionUser.getEmail());

			model.addAttribute("job", job);

			ParameterProcessor parameterProcessor = new ParameterProcessor();
			ParameterProcessorResult paramProcessorResult = parameterProcessor.processHttpParameters(request, locale);
			Report report = job.getReport();
			addParameters(paramProcessorResult, report, request, session, locale);
		} catch (SQLException | RuntimeException | ParseException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditJob("add", model, job, locale);
	}

	@RequestMapping(value = "/saveJob", method = RequestMethod.POST)
	public String saveJob(@ModelAttribute("job") @Valid Job job,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("action") String action, @RequestParam("nextPage") String nextPage,
			@RequestParam(value = "emailTemplateFile", required = false) MultipartFile emailTemplateFile,
			HttpSession session, HttpServletRequest request, Locale locale) {

		logger.debug("Entering saveJob: job={}, action='{}', nextPage='{}'", job, action, nextPage);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditJob(action, model, job, locale);
		}

		try {
			//save email template file
			String saveFileMessage = saveEmailTemplateFile(emailTemplateFile, job, locale);
			logger.debug("saveFileMessage='{}'", saveFileMessage);
			if (saveFileMessage != null) {
				model.addAttribute("plainMessage", saveFileMessage);
				return showEditJob(action, model, job, locale);
			}

			setScheduleDates(job);

			User sessionUser = (User) session.getAttribute("sessionUser");

			if (StringUtils.equals(action, "add")) {
				jobService.addJob(job, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				jobService.updateJob(job, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			try {
				jobHolidayService.recreateJobHolidays(job);
				jobDestinationService.recreateJobDestinations(job);
				jobService.processSchedules(job, sessionUser);
				saveJobParameters(request, job.getJobId());
			} catch (SQLException | RuntimeException | SchedulerException | ParseException ex) {
				logger.error("Error", ex);
				redirectAttributes.addFlashAttribute("error", ex);
			}

			String recordName = job.getName() + " (" + job.getJobId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/" + nextPage;
		} catch (SQLException | RuntimeException | IOException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditJob(action, model, job, locale);
	}

	@RequestMapping(value = "/saveJobs", method = RequestMethod.POST)
	public String saveJobs(@ModelAttribute("multipleJobEdit") @Valid MultipleJobEdit multipleJobEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveJobs: multipleJobEdit={}", multipleJobEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditJobs(model);
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

		return showEditJobs(model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @return the jsp file to display
	 */
	private String showEditJobs(Model model) {
		logger.debug("Entering showEditJobs");

		try {
			model.addAttribute("users", userService.getActiveUsers());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "editJobs";
	}

	/**
	 * Saves job parameters
	 *
	 * @param request the http request that contains the job parameters
	 * @param jobId the job's id
	 * @throws SQLException
	 */
	private void saveJobParameters(HttpServletRequest request, int jobId)
			throws SQLException {

		logger.debug("Entering saveJobParameters: jobId={}", jobId);

		Map<String, String[]> passedValues = new HashMap<>();

		List<String> nonBooleanParams = new ArrayList<>();
		nonBooleanParams.add("chartWidth");
		nonBooleanParams.add("chartHeight");

		Map<String, String[]> requestParameters = request.getParameterMap();
		for (Entry<String, String[]> entry : requestParameters.entrySet()) {
			String htmlParamName = entry.getKey();
			logger.debug("htmlParamName='{}'", htmlParamName);

			if (StringUtils.startsWithIgnoreCase(htmlParamName, ArtUtils.PARAM_PREFIX)
					|| ArtUtils.containsIgnoreCase(nonBooleanParams, htmlParamName)) {
				String[] paramValues = entry.getValue();
				passedValues.put(htmlParamName, paramValues);
			}
		}

		jobParameterService.deleteJobParameters(jobId);

		JobParameter jobParam = new JobParameter();
		jobParam.setJobId(jobId);
		jobParam.setParamTypeString("X");

		//add report parameters
		for (Entry<String, String[]> entry : passedValues.entrySet()) {
			String name = entry.getKey();
			String[] values = entry.getValue();
			for (String value : values) {
				jobParam.setName(name);
				jobParam.setValue(value);
				jobParameterService.addJobParameter(jobParam);
			}
		}

		//add report options
		String showSelectedParametersValue = request.getParameter("showSelectedParameters");
		if (showSelectedParametersValue != null) {
			jobParam.setName("showSelectedParameters");
			jobParam.setValue("true");
			jobParameterService.addJobParameter(jobParam);
		}
		String swapAxesValue = request.getParameter("swapAxes");
		if (swapAxesValue != null) {
			jobParam.setName("swapAxes");
			jobParam.setValue("true");
			jobParameterService.addJobParameter(jobParam);
		}

		//add boolean chart options
		String showLegendValue = request.getParameter("showLegend");
		if (showLegendValue != null) {
			jobParam.setName("showLegend");
			jobParam.setValue("true");
			jobParameterService.addJobParameter(jobParam);
		}
		String showLabelsValue = request.getParameter("showLabels");
		if (showLabelsValue != null) {
			jobParam.setName("showLabels");
			jobParam.setValue("true");
			jobParameterService.addJobParameter(jobParam);
		}
		String showDataValue = request.getParameter("showData");
		if (showDataValue != null) {
			jobParam.setName("showData");
			jobParam.setValue("true");
			jobParameterService.addJobParameter(jobParam);
		}
		String showPointsValue = request.getParameter("showPoints");
		if (showPointsValue != null) {
			jobParam.setName("showPoints");
			jobParam.setValue("true");
			jobParameterService.addJobParameter(jobParam);
		}
	}

	@RequestMapping(value = "/editJob", method = RequestMethod.GET)
	public String editJob(@RequestParam("id") Integer id, Model model,
			HttpSession session, HttpServletRequest request, Locale locale) {

		logger.debug("Entering editJob: id={}", id);

		Job job = null;

		try {
			job = jobService.getJob(id);
			model.addAttribute("job", job);

			Map<String, String[]> finalValues = jobParameterService.getJobParameterValues(id);

			Report report = job.getReport();
			int reportId = report.getReportId();
			User sessionUser = (User) session.getAttribute("sessionUser");

			ParameterProcessor paramProcessor = new ParameterProcessor();
			paramProcessor.setValuesAsIs(true);
			ParameterProcessorResult paramProcessorResult = paramProcessor.process(finalValues, reportId, sessionUser, locale);

			addParameters(paramProcessorResult, report, request, session, locale);
		} catch (SQLException | RuntimeException | ParseException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditJob("edit", model, job, locale);
	}

	/**
	 * Adds report parameters, report options and chart options to the model
	 *
	 * @param paramProcessorResult the parameter processor result that contains
	 * the job's report report parameters, report options and chart options
	 * @report the job's report
	 * @param the http request
	 */
	private void addParameters(ParameterProcessorResult paramProcessorResult,
			Report report, HttpServletRequest request, HttpSession session,
			Locale locale) throws ParseException, SQLException, IOException {

		RunReportHelper runReportHelper = new RunReportHelper();
		runReportHelper.setSelectReportParameterAttributes(report, request, session, locale, paramProcessorResult);
	}

	@RequestMapping(value = "/editJobs", method = RequestMethod.GET)
	public String editJobs(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editJobs: ids={}", ids);

		MultipleJobEdit multipleJobEdit = new MultipleJobEdit();
		multipleJobEdit.setIds(ids);

		model.addAttribute("multipleJobEdit", multipleJobEdit);

		return showEditJobs(model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action "add" or "edit"
	 * @param model the spring model
	 * @param job the job that is being scheduled
	 * @param locale the current locale
	 * @return the jsp file to display
	 */
	private String showEditJob(String action, Model model, Job job, Locale locale) {
		logger.debug("Entering showEditJob: action='{}'", action);

		model.addAttribute("action", action);

		List<JobType> jobTypes = new ArrayList<>();

		if (job != null) { //may be null in case an error occurred while getting ready to display the page
			Report report = job.getReport();
			if (report != null) {
				int reportTypeId = report.getReportTypeId(); //use reportTypeId as reportType not filled
				ReportType reportType = ReportType.toEnum(reportTypeId);

				if (reportType.isDashboard()
						|| reportType == ReportType.JasperReportsTemplate
						|| reportType == ReportType.JxlsTemplate) {
					jobTypes.add(JobType.EmailAttachment);
					jobTypes.add(JobType.Publish);
					jobTypes.add(JobType.Print);
				} else if (reportType == ReportType.Update) {
					jobTypes.add(JobType.JustRun);
				} else if (reportType.isChart() || reportType.isXDocReport()
						|| reportType == ReportType.Group
						|| reportType == ReportType.JasperReportsArt
						|| reportType == ReportType.JxlsArt) {
					jobTypes.add(JobType.EmailAttachment);
					jobTypes.add(JobType.Publish);
					jobTypes.add(JobType.CondEmailAttachment);
					jobTypes.add(JobType.CondPublish);
					jobTypes.add(JobType.Print);
				} else if (reportType == ReportType.FixedWidth
						|| reportType == ReportType.CSV) {
					jobTypes.add(JobType.EmailAttachment);
					jobTypes.add(JobType.EmailInline);
					jobTypes.add(JobType.Publish);
					jobTypes.add(JobType.CondEmailAttachment);
					jobTypes.add(JobType.CondEmailInline);
					jobTypes.add(JobType.CondPublish);
					jobTypes.add(JobType.Print);
				} else {
					jobTypes.addAll(JobType.list());
				}
			}
		}

		model.addAttribute("jobTypes", jobTypes);

		Map<String, String> fileReportFormats = new LinkedHashMap<>();
		List<String> jobReportFormats = new ArrayList<>(Config.getReportFormats());
		jobReportFormats.remove("html");
		jobReportFormats.remove("htmlFancy");
		jobReportFormats.remove("htmlGrid");
		jobReportFormats.remove("htmlDataTable");
		jobReportFormats.remove("pivotTableJs");
		jobReportFormats.remove("c3");
		jobReportFormats.remove("plotly");

		final String REPORT_FORMAT_PREFIX = "reports.format.";
		for (String reportFormat : jobReportFormats) {
			String reportFormatDescription = messageSource.getMessage(REPORT_FORMAT_PREFIX + reportFormat, null, locale);
			fileReportFormats.put(reportFormat, reportFormatDescription);
		}
		model.addAttribute("fileReportFormats", fileReportFormats);

		try {
			model.addAttribute("dynamicRecipientReports", reportService.getDynamicRecipientReports());
			model.addAttribute("schedules", scheduleService.getAllSchedules());
			model.addAttribute("datasources", datasourceService.getAllDatasources());
			model.addAttribute("holidays", holidayService.getAllHolidays());
			model.addAttribute("destinations", destinationService.getAllDestinations());
			model.addAttribute("smtpServers", smtpServerService.getAllSmtpServers());
			model.addAttribute("startConditions", startConditionService.getAllStartConditions());

			if (job != null && !StringUtils.equals(action, "add")) {
				String cronString = CronStringHelper.getCronString(job);
				String mainScheduleDescription = CronStringHelper.getCronScheduleDescription(cronString, locale);
				model.addAttribute("mainScheduleDescription", mainScheduleDescription);
				Date nextRunDate = CronStringHelper.getNextRunDate(cronString);
				model.addAttribute("nextRunDate", nextRunDate);
			}
		} catch (SQLException | RuntimeException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("serverDateString", ArtUtils.isoDateTimeMillisecondsFormatter.format(new Date()));
		model.addAttribute("serverTimeZoneDescription", Config.getServerTimeZoneDescription());
		model.addAttribute("serverTimeZone", TimeZone.getDefault().getID());
		model.addAttribute("timeZones", Config.getTimeZones());

		ArtHelper artHelper = new ArtHelper();
		artHelper.setTinymceLanguage(locale, model);

		return "editJob";
	}

	/**
	 * Sets the job schedule start and end date
	 *
	 * @param job the art job object
	 * @throws ParseException
	 */
	private void setScheduleDates(Job job) throws ParseException {
		logger.debug("Entering setScheduleDates: job={}", job);

		String startDateString = job.getStartDateString();
		ExpressionHelper expressionHelper = new ExpressionHelper();
		Date startDate = expressionHelper.convertStringToDate(startDateString);

		String endDateString = job.getEndDateString();
		Date endDate;
		if (StringUtils.isBlank(endDateString)) {
			endDate = null;
		} else {
			endDate = expressionHelper.convertStringToDate(endDateString);
		}

		job.setStartDate(startDate);
		job.setEndDate(endDate);
	}

	/**
	 * Saves an email template file and updates the appropriate job property
	 * with the file name
	 *
	 * @param file the file to save
	 * @param job the job object to set
	 * @param locale the locale
	 * @return a problem description if there was a problem, otherwise null
	 * @throws IOException
	 */
	private String saveEmailTemplateFile(MultipartFile file, Job job,
			Locale locale) throws IOException {

		logger.debug("Entering saveEmailTemplateFile: job={}", job);

		logger.debug("file==null = {}", file == null);
		if (file == null) {
			return null;
		}

		logger.debug("file.isEmpty()={}", file.isEmpty());
		if (file.isEmpty()) {
			//can be empty if a file name is just typed
			//or if upload a 0 byte file
			//don't show message in case of file name being typed
			return null;
		}

		//set allowed upload file types
		List<String> validExtensions = new ArrayList<>();
		validExtensions.add("html");

		//save file
		String templatesPath = Config.getJobTemplatesPath();
		UploadHelper uploadHelper = new UploadHelper(messageSource, locale);
		String message = uploadHelper.saveFile(file, templatesPath, validExtensions, job.isOverwriteFiles());

		if (message != null) {
			return message;
		}

		String filename = file.getOriginalFilename();
		job.setEmailTemplate(filename);

		return null;
	}

	@GetMapping("/runningJobs")
	public String showRunningJobs(Model model) {
		logger.debug("Entering showRunningJobs");

		return "runningJobs";
	}

	@GetMapping("/getRunningJobs")
	@ResponseBody
	public AjaxResponse getRunningJobs() {
		logger.debug("Entering getRunningJobs");

		AjaxResponse ajaxResponse = new AjaxResponse();

		List<JobRunDetails> runningJobs = Config.getRunningJobs();

		ajaxResponse.setData(runningJobs);
		ajaxResponse.setSuccess(true);

		return ajaxResponse;
	}

}
