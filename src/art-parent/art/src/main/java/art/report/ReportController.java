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
package art.report;

import art.datasource.DatasourceService;
import art.enums.PageOrientation;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.jobrunners.ReportJob;
import art.mail.Mailer;
import art.reportgroup.ReportGroupService;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import art.user.User;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
import art.utils.ArtUtils;
import art.utils.FinalFilenameValidator;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for reports pages
 *
 * @author Timothy Anyona
 */
@Controller
public class ReportController {

	private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

	@Autowired
	private ReportService reportService;

	@Autowired
	private ReportGroupService reportGroupService;

	@Autowired
	private DatasourceService datasourceService;

	@Autowired
	private MessageSource messageSource;
	
	@RequestMapping(value = {"/", "/reports"}, method = RequestMethod.GET)
	public String showReports(HttpSession session, HttpServletRequest request, Model model) {
		logger.debug("Entering showReports");

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			model.addAttribute("reports", reportService.getDisplayReports(sessionUser.getUserId()));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reports";
	}
	
	@RequestMapping(value = "/selectReportParameters", method = RequestMethod.GET)
	public String selectReportParameters(HttpSession session,
			@RequestParam("reportId") Integer reportId,
			HttpServletRequest request, Model model, Locale locale) {

		logger.debug("Entering selectReportParameters: reportId={}", reportId);

		try {
			Report report = reportService.getReport(reportId);
			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return "reportError";
			} else {
				RunReportHelper runReportHelper = new RunReportHelper();
				runReportHelper.setSelectReportParameterAttributes(report, request, session, reportService, locale);
			}
		} catch (SQLException | RuntimeException | ParseException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "selectReportParameters";
	}

	@RequestMapping(value = "/getReports", method = RequestMethod.GET)
	public @ResponseBody
	List<Report> getReports(HttpSession session, HttpServletRequest request) {
		//object will be automatically converted to json because of @ResponseBody and presence of jackson libraries
		//see http://www.mkyong.com/spring-mvc/spring-3-mvc-and-json-example/

		logger.debug("Entering getReports");

		User sessionUser = (User) session.getAttribute("sessionUser");

		List<Report> reports = null;

		try {
			reports = reportService.getDisplayReports(sessionUser.getUserId());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
		}

		return reports;
	}

	@RequestMapping(value = "/reportsConfig", method = RequestMethod.GET)
	public String showReportsConfig(Model model) {
		logger.debug("Entering showReportsConfig");

		try {
			model.addAttribute("reports", reportService.getAllReports());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportsConfig";
	}

	@RequestMapping(value = "/deleteReport", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReport(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteReport: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = reportService.deleteReport(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//report not deleted because of linked jobs
				response.setData(deleteResult.getData());
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteReports", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReports(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteReports: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = reportService.deleteReports(ids);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				response.setData(deleteResult.getData());
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addReport", method = RequestMethod.GET)
	public String addReport(Model model, HttpSession session) {
		logger.debug("Entering addReport");

		User sessionUser = (User) session.getAttribute("sessionUser");

		Report report = new Report();
		report.setActive(true);
		report.setContactPerson(sessionUser.getFullName());

		model.addAttribute("report", report);

		return showEditReport("add", model, session);
	}

	@RequestMapping(value = "/editReport", method = RequestMethod.GET)
	public String editReport(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering editReport: id={}", id);

		try {
			model.addAttribute("report", reportService.getReport(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReport("edit", model, session);
	}

	@RequestMapping(value = "/editReports", method = RequestMethod.GET)
	public String editReports(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editReports: ids={}", ids);

		MultipleReportEdit multipleReportEdit = new MultipleReportEdit();
		multipleReportEdit.setIds(ids);

		model.addAttribute("multipleReportEdit", multipleReportEdit);

		return "editReports";
	}

	@RequestMapping(value = "/saveReport", method = RequestMethod.POST)
	public String saveReport(@ModelAttribute("report") @Valid Report report,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session, @RequestParam("action") String action,
			@RequestParam(value = "templateFile", required = false) MultipartFile templateFile,
			@RequestParam(value = "resourcesFile", required = false) MultipartFile resourcesFile) {

		logger.debug("Entering saveReport: report={}, action='{}'", report, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditReport(action, model, session);
		}

		try {
			//finalise report properties
			String prepareReportMessage = prepareReport(report, templateFile, resourcesFile, action);
			logger.debug("prepareReportMessage='{}'", prepareReportMessage);
			if (prepareReportMessage != null) {
				model.addAttribute("message", prepareReportMessage);
				return showEditReport(action, model, session);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");

			if (StringUtils.equals(action, "add")) {
				reportService.addReport(report, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "copy")) {
				reportService.copyReport(report, report.getReportId(), sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				reportService.updateReport(report, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = report.getName() + " (" + report.getReportId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);
			redirectAttributes.addFlashAttribute("record", report);
			return "redirect:/reportsConfig";
		} catch (SQLException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReport(action, model, session);
	}

	@RequestMapping(value = "/saveReports", method = RequestMethod.POST)
	public String saveReports(@ModelAttribute("multipleReportEdit") @Valid MultipleReportEdit multipleReportEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveReports: multipleReportEdit={}", multipleReportEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditReports();
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			reportService.updateReports(multipleReportEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleReportEdit.getIds());
			return "redirect:/reportsConfig";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReports();
	}

	@RequestMapping(value = "/emailReport", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse emailReport(@RequestParam("mailFrom") String mailFrom,
			@RequestParam("mailTo") String mailTo,
			@RequestParam("mailCc") String mailCc,
			@RequestParam("mailBcc") String mailBcc,
			@RequestParam("mailSubject") String mailSubject,
			@RequestParam("mailMessage") String mailMessage,
			HttpSession session, Locale locale) {

		logger.debug("Entering emailReport: mailFrom='{}',"
				+ " mailTo='{}', mailCc='{}', mailBcc='{}', mailSubject='{}'",
				mailFrom, mailTo, mailCc, mailBcc, mailSubject);

		AjaxResponse response = new AjaxResponse();
		String fileNotSentMessage = messageSource.getMessage("reports.message.fileNotSent", null, locale);
		response.setSuccess(false);
		response.setErrorMessage(fileNotSentMessage);

		String reportFileName = (String) session.getAttribute("reportFileName");
//		session.removeAttribute("reportFileName");
		User sessionUser = (User) session.getAttribute("sessionUser");

		String from = StringUtils.trim(mailFrom);
		String to = StringUtils.trim(mailTo);

		if (StringUtils.isBlank(reportFileName)) {
			logger.info("Could not email report. reportFileName is blank. User = {}", sessionUser);
			return response;
		}

		String fullReportFileName = Config.getReportsExportPath() + reportFileName;
		File reportFile = new File(fullReportFileName);
		if (!reportFile.exists()) {
			logger.info("Could not email report. Report file does not exist: '{}'. User = {}", reportFileName, sessionUser);
			return response;
		}

		if (StringUtils.length(from) < 5) {
			logger.info("Could not email report. Invalid mailFrom. User = {}", sessionUser);
			return response;
		}

		if (StringUtils.length(to) < 5) {
			logger.info("Could not email report. Invalid mailTo. User = {}", sessionUser);
			return response;
		}

		if (!Config.isEmailServerConfigured()) {
			logger.info("Could not email report. Email server not configured. User = {}", sessionUser);
			return response;
		}

		String subject = mailSubject;
		if (StringUtils.isBlank(subject)) {
			subject = "Report";
		}

		String[] tos;
		if (StringUtils.contains(to, ",")) {
			tos = StringUtils.split(to, ",");
		} else {
			tos = StringUtils.split(to, ";");
		}

		String[] ccs;
		if (StringUtils.contains(mailCc, ",")) {
			ccs = StringUtils.split(mailCc, ",");
		} else {
			ccs = StringUtils.split(mailCc, ";");
		}

		String[] bccs;
		if (StringUtils.contains(mailBcc, ",")) {
			bccs = StringUtils.split(mailBcc, ",");
		} else {
			bccs = StringUtils.split(mailBcc, ";");
		}

		ReportJob reportJob = new ReportJob();
		Mailer mailer = reportJob.getMailer();
		mailer.setFrom(from);
		mailer.setSubject(subject);
		mailer.setMessage(mailMessage);
		List<File> attachments = new ArrayList<>();
		attachments.add(reportFile);
		mailer.setAttachments(attachments);
		mailer.setTo(tos);
		mailer.setCc(ccs);
		mailer.setBcc(bccs);

		//disable email for now. feature may be abused by users to send spam?
		try {
			mailer.send();
		} catch (MessagingException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			response.setSuccess(false);
			response.setErrorMessage(ex.toString());
		}

		response.setSuccess(true);

		return response;
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @return returns the jsp file to display
	 */
	private String showEditReports() {
		logger.debug("Entering showEditReports");
		return "editReports";
	}

	@RequestMapping(value = "/copyReport", method = RequestMethod.GET)
	public String copyReport(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering copyReport: id={}", id);

		try {
			model.addAttribute("report", reportService.getReport(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReport("copy", model, session);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to take
	 * @param model the model to use
	 * @param session the http session
	 * @return the jsp file to display
	 */
	private String showEditReport(String action, Model model, HttpSession session) {
		logger.debug("Entering showEditReport: action='{}'", action);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			model.addAttribute("reportGroups", reportGroupService.getAdminReportGroups(sessionUser));
			model.addAttribute("reportTypes", ReportType.list());
			model.addAttribute("datasources", datasourceService.getAdminDatasources(sessionUser));
			model.addAttribute("reportFormats", ReportFormat.list());
			model.addAttribute("pageOrientations", PageOrientation.list());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);

		int maxFileSizeMB = Config.getSettings().getMaxFileUploadSizeMB();
		model.addAttribute("maxFileSizeMB", maxFileSizeMB);

		long maxFileSizeBytes = maxFileSizeMB * 1000L * 1000L;
		model.addAttribute("maxFileSizeBytes", maxFileSizeBytes);

		return "editReport";
	}

	/**
	 * Saves a file
	 *
	 * @param file the file to save
	 * @param reportTypeId the reportTypeId for the report related to the file
	 * @return an i18n message string if there was a problem, otherwise null
	 * @throws IOException
	 */
	private String saveFile(MultipartFile file, int reportTypeId) throws IOException {
		return saveFile(file, reportTypeId, null);
	}

	/**
	 * Saves a file and updates the report template property with the file name
	 *
	 * @param file the file to save
	 * @param reportTypeId the reportTypeId for the report related to the file
	 * @param report the report to set #param updateTemplateField determines
	 * whether the template field of the report should be updated with the name
	 * of the file given
	 * @return an i18n message string if there was a problem, otherwise null
	 * @throws IOException
	 */
	private String saveFile(MultipartFile file, int reportTypeId, Report report)
			throws IOException {

		logger.debug("Entering saveFile: report={}", report);

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

		//check file size
		long maxUploadSize = Config.getSettings().getMaxFileUploadSizeMB(); //size in MB
		maxUploadSize = maxUploadSize * 1000L * 1000L; //size in bytes

		long uploadSize = file.getSize();
		logger.debug("maxUploadSize={}, uploadSize={}", maxUploadSize, uploadSize);

		if (maxUploadSize >= 0 && uploadSize > maxUploadSize) { //-1 or any negative value means no size limit
			return "reports.message.fileBiggerThanMax";
		}

		//check upload file type
		List<String> validExtensions = new ArrayList<>();
		validExtensions.add("xml");
		validExtensions.add("jrxml");
		validExtensions.add("xls");
		validExtensions.add("xlsx");
		validExtensions.add("png");
		validExtensions.add("jpg");
		validExtensions.add("jpeg");
		validExtensions.add("ftl");
		validExtensions.add("ftlh"); //http://freemarker.org/docs/pgui_config_outputformatsautoesc.html
		validExtensions.add("ftlx");
		validExtensions.add("docx");
		validExtensions.add("odt");
		validExtensions.add("pptx");
		validExtensions.add("js"); //for react pivot templates
		validExtensions.add("html"); //for thymeleaf reports
		validExtensions.add("csv"); //for pivottable.js csv server reports (.csv)
		validExtensions.add("txt"); //for pivottable.js csv server reports (.txt for other delimited files e.g. tab separated, pipe separated etc)
		validExtensions.add("css"); //for c3.js additional css
		validExtensions.add("js"); //for datamaps additional js
		validExtensions.add("json"); //for datamaps optional data file

		String filename = file.getOriginalFilename();
		logger.debug("filename='{}'", filename);
		String extension = FilenameUtils.getExtension(filename);

		if (!ArtUtils.containsIgnoreCase(validExtensions, extension)) {
			return "reports.message.fileTypeNotAllowed";
		}

		if (!FinalFilenameValidator.isValid(filename)) {
			return "reports.message.invalidFilename";
		}

		//save file
		String templatesPath;
		ReportType reportType = ReportType.toEnum(reportTypeId);
		switch (reportType) {
			case ReactPivot:
			case PivotTableJs:
			case PivotTableJsCsvLocal:
			case PivotTableJsCsvServer: //can specify .js template and .csv data file
			case Dygraphs:
			case DygraphsCsvLocal:
			case DygraphsCsvServer:
			case DataTables:
			case DataTablesCsvLocal:
			case DataTablesCsvServer:
			case C3:
			case ChartJs:
			case Datamaps:
			case DatamapsFile:
			case Leaflet:
			case OpenLayers:
				templatesPath = Config.getJsTemplatesPath();
				break;
			case JPivotMondrian:
				templatesPath = Config.getDefaultTemplatesPath();
				break;
			default:
				templatesPath = Config.getTemplatesPath();
		}

		String destinationFilename = templatesPath + filename;
		File destinationFile = new File(destinationFilename);
		file.transferTo(destinationFile);

		if (report != null) {
			report.setTemplate(filename);
		}

		return null;
	}

	/**
	 * Sets report source and chart options for the given report
	 *
	 * @param report the report to use
	 * @param action the action to take, "add" or "edit"
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setProperties(Report report, String action) throws SQLException {
		logger.debug("Entering setProperties: report={}, action='{}'", report, action);

		//set report source for text reports
		logger.debug("report.getReportTypeId()={}", report.getReportTypeId());
		ReportType reportType = ReportType.toEnum(report.getReportTypeId());
		report.setReportType(reportType);
		if (reportType == ReportType.Text) {
			report.setReportSource(report.getReportSourceHtml());
		}

		//build chart options setting string
		logger.debug("(report.getChartOptions() != null) = {}", report.getChartOptions() != null);
		if (reportType.isChart() && report.getChartOptions() != null) {
			setChartOptionsSettingString(report);
		}

		return null;
	}

	/**
	 * Sets the chart options setting property of the given report
	 *
	 * @param report the report
	 */
	private void setChartOptionsSettingString(Report report) {
		logger.debug("Entering setChartOptionsSettingString: report={}", report);

		String size = report.getChartOptions().getWidth() + "x" + report.getChartOptions().getHeight();
		String yRange = report.getChartOptions().getyAxisMin() + ":" + report.getChartOptions().getyAxisMax();

		logger.debug("size='{}'", size);
		logger.debug("yRange='{}'", yRange);

		String showLegend = "";
		String showLabels = "";
		String showPoints = "";
		String showData = "";

		logger.debug("report.getChartOptions().isShowLegend() = {}", report.getChartOptions().isShowLegend());
		if (report.getChartOptions().isShowLegend()) {
			showLegend = "showLegend";
		}
		logger.debug("report.getChartOptions().isShowLabels() = {}", report.getChartOptions().isShowLabels());
		if (report.getChartOptions().isShowLabels()) {
			showLabels = "showLabels";
		}
		logger.debug("report.getChartOptions().isShowPoints() = {}", report.getChartOptions().isShowPoints());
		if (report.getChartOptions().isShowPoints()) {
			showPoints = "showPoints";
		}
		logger.debug("report.getChartOptions().isShowData() = {}", report.getChartOptions().isShowData());
		if (report.getChartOptions().isShowData()) {
			showData = "showData";
		}

		String rotateAt = "rotateAt:" + report.getChartOptions().getRotateAt();
		String removeAt = "removeAt:" + report.getChartOptions().getRemoveAt();

		logger.debug("rotateAt='{}'", rotateAt);
		logger.debug("removeAt='{}'", removeAt);

		Object[] options = {
			size,
			yRange,
			report.getChartOptions().getBackgroundColor(),
			showLegend,
			showLabels,
			showPoints,
			showData,
			rotateAt,
			removeAt
		};

		logger.debug("options='{}'", StringUtils.join(options, " "));
		report.setChartOptionsSetting(StringUtils.join(options, " "));
	}

	/**
	 * Finalises report properties
	 *
	 * @param report the report to use
	 * @param templateFile the template file
	 * @param resourcesFile the resources file
	 * @param action the action to take
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws IOException
	 * @throws SQLException
	 */
	private String prepareReport(Report report, MultipartFile templateFile,
			MultipartFile resourcesFile, String action) throws IOException, SQLException {

		logger.debug("Entering prepareReport: report={}, action='{}'", report, action);

		String message;

		int reportTypeId = report.getReportTypeId();
		message = saveFile(templateFile, reportTypeId, report); //pass report so that template field is updated
		if (message != null) {
			return message;
		}

		message = saveFile(resourcesFile, reportTypeId);
		if (message != null) {
			return message;
		}

		message = setProperties(report, action);
		if (message != null) {
			return message;
		}

		return null;
	}

	@PostMapping("/uploadResources")
	public @ResponseBody
	Map<String, List<FileUploadResponse>> uploadResources(MultipartHttpServletRequest request,
			Locale locale) {

		logger.debug("Entering uploadResources");

		//https://github.com/jdmr/fileUpload/blob/master/src/main/java/org/davidmendoza/fileUpload/web/ImageController.java
		//https://github.com/blueimp/jQuery-File-Upload/wiki/Setup#using-jquery-file-upload-ui-version-with-a-custom-server-side-upload-handler
		Map<String, List<FileUploadResponse>> response = new HashMap<>();
		List<FileUploadResponse> fileList = new ArrayList<>();

		String reportTypeIdString = request.getParameter("reportTypeId");
		int reportTypeId = NumberUtils.toInt(reportTypeIdString, Integer.MAX_VALUE);

		//http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/multipart/MultipartRequest.html
		Iterator<String> itr = request.getFileNames();
		while (itr.hasNext()) {
			String htmlParamName = itr.next();
			logger.debug("htmlParamName = '{}'", htmlParamName);

			MultipartFile file = request.getFile(htmlParamName);

			FileUploadResponse fileDetails = new FileUploadResponse();

			String filename = file.getOriginalFilename();
			fileDetails.setName(filename);
			fileDetails.setSize(file.getSize());

			logger.debug("filename = '{}'", filename);

			if (FinalFilenameValidator.isValid(filename)) {
				try {
					String message = saveFile(file, reportTypeId);
					if (message != null) {
						String errorMessage = messageSource.getMessage(message, null, locale);
						fileDetails.setError(errorMessage);
					}
				} catch (IOException ex) {
					logger.error("Error", ex);
					if (Config.getCustomSettings().isShowErrors()) {
						fileDetails.setError(ex.getMessage());
					} else {
						String errorMessage = messageSource.getMessage("page.message.errorOccurred", null, locale);
						fileDetails.setError(errorMessage);
					}
				}
			} else {
				String errorMessage = messageSource.getMessage("reports.message.invalidFilename", null, locale);
				fileDetails.setError(errorMessage);
			}

			fileList.add(fileDetails);
		}

		response.put("files", fileList);

		return response;
	}

}
