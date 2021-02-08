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

import art.accessright.AccessRightService;
import art.datasource.DatasourceService;
import art.encryption.AesEncryptor;
import art.encryptor.EncryptorService;
import art.enums.PageOrientation;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.mail.Mailer;
import art.reportgroup.ReportGroupService;
import art.reportgroupmembership.ReportGroupMembershipService2;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import art.user.User;
import art.general.ActionResult;
import art.general.AjaxResponse;
import art.reportoptions.GridstackItemOptions;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportRunDetails;
import art.runreport.ReportRunner;
import art.savedparameter.SavedParameter;
import art.savedparameter.SavedParameterService;
import art.selfservice.SelfServiceHelper;
import art.user.UserService;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupService;
import art.utils.AjaxTableHelper;
import art.utils.ArtUtils;
import art.utils.FinalFilenameValidator;
import art.utils.MailService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rits.cloning.Cloner;
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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

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
	private ReportGroupMembershipService2 reportGroupMembershipService2;

	@Autowired
	private EncryptorService encryptorService;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private SavedParameterService savedParameterService;

	@Autowired
	private TemplateEngine defaultTemplateEngine;

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private AccessRightService accessRightService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private MailService mailService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String showHome(HttpSession session) {
		logger.debug("Entering showHome");

		User sessionUser = (User) session.getAttribute("sessionUser");

		String startReport = sessionUser.getEffectiveStartReport();
		if (StringUtils.isBlank(startReport)) {
			return "reports";
		} else {
			return "redirect:/runReport?reportId=" + startReport;
		}
	}

	@RequestMapping(value = "/reports", method = RequestMethod.GET)
	public String showReports() {
		logger.debug("Entering showReports");

		return "reports";
	}

	@RequestMapping(value = "/getAvailableReports", method = RequestMethod.GET)
	public @ResponseBody
	AjaxResponse getAvailableReports(HttpSession session, Locale locale,
			HttpServletRequest request) {
		//object will be automatically converted to json because of @ResponseBody and presence of jackson libraries
		//see http://www.mkyong.com/spring-mvc/spring-3-mvc-and-json-example/

		logger.debug("Entering getAvailableReports");

		AjaxResponse ajaxResponse = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			List<Report> reports = reportService.getDisplayReports(sessionUser.getUserId());

			AjaxTableHelper ajaxTableHelper = new AjaxTableHelper(messageSource, locale);

			List<BasicReport> finalReports = new ArrayList<>();

			for (Report report : reports) {
				BasicReport basicReport = new BasicReport(report);

				String name = report.getLocalizedName(locale);
				String encodedName = Encode.forHtmlContent(name);

				String finalLink;
				if (report.getReportType() == ReportType.Link) {
					String link = report.getLink();
					String encodedLink = Encode.forHtmlAttribute(link);
					String targetAttribute = "";
					if (report.isOpenInNewWindow()) {
						targetAttribute = "target='_blank'";
					}
					finalLink = "<a href='" + encodedLink + "' " + targetAttribute + ">" + encodedName + "</a>&nbsp;";
				} else {
					finalLink = "<a href='" + request.getContextPath()
							+ "/selectReportParameters?reportId="
							+ report.getReportId() + "'>" + encodedName + "</a>&nbsp;";
				}

				String label = ajaxTableHelper.processName("", report.getCreationDate(), report.getUpdateDate());
				basicReport.setName2(finalLink + label);

				String description = report.getLocalizedDescription(locale);
				if (StringUtils.isNotBlank(description)) {
					description = Encode.forHtml(description);
				}
				basicReport.setDescription2(description);

				finalReports.add(basicReport);
			}

			ajaxResponse.setData(finalReports);
			ajaxResponse.setSuccess(true);
		} catch (SQLException | IOException | RuntimeException ex) {
			logger.error("Error", ex);
			ajaxResponse.setErrorMessage(ex.toString());
		}

		return ajaxResponse;
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
				User sessionUser = (User) session.getAttribute("sessionUser");
				if (reportService.hasOwnerAccess(sessionUser, report.getReportId())) {
					List<User> users = userService.getActiveUsers();
					users.removeIf(user -> StringUtils.isBlank(user.getFullName()));
					List<UserGroup> userGroups = userGroupService.getAllUserGroups();

					request.setAttribute("users", users);
					request.setAttribute("userGroups", userGroups);
					request.setAttribute("enableShare", true);
				}

				RunReportHelper runReportHelper = new RunReportHelper();
				runReportHelper.setSelectReportParameterAttributes(report, request, session, locale);
			}
		} catch (SQLException | RuntimeException | ParseException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "selectReportParameters";
	}

	@RequestMapping(value = "/reportsConfig", method = RequestMethod.GET)
	public String showReportsConfig() {
		logger.debug("Entering showReportsConfig");

		return "reportsConfig";
	}

	@RequestMapping(value = "/reportConfig", method = RequestMethod.GET)
	public String showReportConfig(@RequestParam("reportId") Integer reportId, Model model,
			HttpSession session) {

		logger.debug("Entering showReportConfig: reportId={}", reportId);

		try {
			model.addAttribute("report", reportService.getReport(reportId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportConfig";
	}

	@GetMapping("/getConfigReports")
	public @ResponseBody
	AjaxResponse getConfigReports(Locale locale, HttpServletRequest request,
			HttpServletResponse httpResponse, HttpSession session) throws SQLException, IOException {

		logger.debug("Entering getConfigReports");

		AjaxResponse ajaxResponse = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			
			List<Report> reports = reportService.getAdminReportsBasic(sessionUser);

			WebContext ctx = new WebContext(request, httpResponse, servletContext, locale);
			AjaxTableHelper ajaxTableHelper = new AjaxTableHelper(messageSource, locale);

			List<BasicReport> finalReports = new ArrayList<>();

			for (Report report : reports) {
				ctx.setVariable("report", report);
				String templateName = "reportsConfigAction";
				String dtAction = defaultTemplateEngine.process(templateName, ctx);

				BasicReport basicReport = new BasicReport(report);
				basicReport.setDtAction(dtAction);

				String encodedName = ajaxTableHelper.processName(report.getName(), report.getCreationDate(), report.getUpdateDate());
				basicReport.setName2(encodedName);

				String description = report.getDescription();
				if (StringUtils.isNotBlank(description)) {
					description = Encode.forHtml(description);
				}
				basicReport.setDescription2(description);

				String activeStatus;
				if (report.isActive()) {
					activeStatus = ajaxTableHelper.getActiveSpan();
				} else {
					activeStatus = ajaxTableHelper.getDisabledSpan();
				}
				basicReport.setDtActiveStatus(activeStatus);

				finalReports.add(basicReport);
			}

			ajaxResponse.setData(finalReports);
			ajaxResponse.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			ajaxResponse.setErrorMessage(ex.toString());
		}

		return ajaxResponse;
	}

	@RequestMapping(value = {"/deleteReport", "/deleteGridstack", "/deleteSelfService"}, method = RequestMethod.POST)
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
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
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
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
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
		report.setContactPerson(sessionUser.getFullName());

		model.addAttribute("report", report);

		return showEditReport("add", model, session);
	}

	@RequestMapping(value = "/editReport", method = RequestMethod.GET)
	public String editReport(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering editReport: id={}", id);

		try {
			model.addAttribute("report", reportService.getReportWithOwnSource(id));
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

		return showEditReports(model, session);
	}

	@RequestMapping(value = "/saveReport", method = RequestMethod.POST)
	public String saveReport(@ModelAttribute("report") @Valid Report report,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session, @RequestParam("action") String action,
			@RequestParam(value = "templateFile", required = false) MultipartFile templateFile,
			Locale locale) {

		logger.debug("Entering saveReport: report={}, action='{}'", report, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditReport(action, model, session);
		}

		try {
			//set passwords as appropriate
			String setPasswordsMessage = setPasswords(report, action);
			logger.debug("setPasswordsMessage='{}'", setPasswordsMessage);
			if (setPasswordsMessage != null) {
				model.addAttribute("message", setPasswordsMessage);
				return showEditReport(action, model, session);
			}

			//save template file
			String saveFileMessage = saveFile(templateFile, report.getReportTypeId(), report.isOverwriteFiles(), locale, report);
			logger.debug("saveFileMessage='{}'", saveFileMessage);
			if (saveFileMessage != null) {
				model.addAttribute("plainMessage", saveFileMessage);
				return showEditReport(action, model, session);
			}

			//finalise report properties
			setProperties(report, action);

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

			try {
				reportGroupMembershipService2.recreateReportGroupMemberships(report);
			} catch (SQLException | RuntimeException ex) {
				logger.error("Error", ex);
				redirectAttributes.addFlashAttribute("error", ex);
			}
			return "redirect:/reportConfig?reportId=" + report.getReportId();
		} catch (Exception ex) {
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
			return showEditReports(model, session);
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			reportService.updateReports(multipleReportEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleReportEdit.getIds());

			if (!multipleReportEdit.isReportGroupsUnchanged()) {
				try {
					String[] ids = StringUtils.split(multipleReportEdit.getIds(), ",");
					for (String idString : ids) {
						int id = Integer.parseInt(idString);
						Report report = reportService.getReport(id);
						report.setReportGroups(multipleReportEdit.getReportGroups());
						reportGroupMembershipService2.recreateReportGroupMemberships(report);
					}
				} catch (SQLException | RuntimeException ex) {
					logger.error("Error", ex);
					redirectAttributes.addFlashAttribute("error", ex);
				}
			}

			return "redirect:/reportsConfig";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReports(model, session);
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

		if (!Config.getCustomSettings().isEnableEmailing()) {
			logger.info("Could not email report. Emailing disabled. User = {}", sessionUser);
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

		Mailer mailer = mailService.getMailer();

		mailer.setFrom(from);
		mailer.setSubject(subject);
		mailer.setMessage(mailMessage);
		mailer.setTo(tos);
		mailer.setCc(ccs);
		mailer.setBcc(bccs);

		List<File> attachments = new ArrayList<>();
		attachments.add(reportFile);
		mailer.setAttachments(attachments);

		//disable email for now? feature may be abused by users to send spam?
		try {
			mailer.send();
		} catch (MessagingException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			response.setSuccess(false);
			response.setErrorMessage(ex.getMessage());
		}

		response.setSuccess(true);

		return response;
	}

	@PostMapping("/saveParameterSelection")
	public @ResponseBody
	AjaxResponse saveParameterSelectionHandler(HttpServletRequest request,
			HttpSession session) {

		logger.debug("Entering saveParameterSelectionHandler");

		AjaxResponse response = new AjaxResponse();

		try {
			int reportId = Integer.parseInt(request.getParameter("reportId"));
			saveParameterSelection(session, request, reportId);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.getMessage());
		}

		return response;
	}

	/**
	 * Saves the parameter selection for the current user and the given report
	 *
	 * @param session the http session
	 * @param request the http request
	 * @throws NumberFormatException
	 * @throws SQLException
	 */
	private void saveParameterSelection(HttpSession session, HttpServletRequest request,
			int reportId) throws NumberFormatException, SQLException {

		User sessionUser = (User) session.getAttribute("sessionUser");
		int userId = sessionUser.getUserId();

		Map<String, String[]> passedValues = new HashMap<>();

		List<String> nonBooleanParams = new ArrayList<>();
		nonBooleanParams.add("chartWidth");
		nonBooleanParams.add("chartHeight");

		Map<String, String[]> requestParameters = request.getParameterMap();
		for (Map.Entry<String, String[]> entry : requestParameters.entrySet()) {
			String htmlParamName = entry.getKey();
			logger.debug("htmlParamName='{}'", htmlParamName);

			if (StringUtils.startsWithIgnoreCase(htmlParamName, ArtUtils.PARAM_PREFIX)
					|| ArtUtils.containsIgnoreCase(nonBooleanParams, htmlParamName)) {
				String[] paramValues = entry.getValue();
				passedValues.put(htmlParamName, paramValues);
			}
		}

		savedParameterService.deleteSavedParameters(userId, reportId);

		SavedParameter savedParam = new SavedParameter();
		savedParam.setUserId(userId);
		savedParam.setReportId(reportId);

		//add report parameters
		for (Map.Entry<String, String[]> entry : passedValues.entrySet()) {
			String name = entry.getKey();
			String[] values = entry.getValue();
			for (String value : values) {
				savedParam.setName(name);
				savedParam.setValue(value);
				savedParameterService.addSavedParameter(savedParam);
			}
		}

		//add report options
		String showSelectedParametersValue = request.getParameter("showSelectedParameters");
		if (showSelectedParametersValue != null) {
			savedParam.setName("showSelectedParameters");
			savedParam.setValue("true");
			savedParameterService.addSavedParameter(savedParam);
		}
		String swapAxesValue = request.getParameter("swapAxes");
		if (swapAxesValue != null) {
			savedParam.setName("swapAxes");
			savedParam.setValue("true");
			savedParameterService.addSavedParameter(savedParam);
		}
		String showSqlValue = request.getParameter("showSql");
		if (showSqlValue != null) {
			savedParam.setName("showSql");
			savedParam.setValue("true");
			savedParameterService.addSavedParameter(savedParam);
		}

		//add boolean chart options
		String showLegendValue = request.getParameter("showLegend");
		if (showLegendValue != null) {
			savedParam.setName("showLegend");
			savedParam.setValue("true");
			savedParameterService.addSavedParameter(savedParam);
		}
		String showLabelsValue = request.getParameter("showLabels");
		if (showLabelsValue != null) {
			savedParam.setName("showLabels");
			savedParam.setValue("true");
			savedParameterService.addSavedParameter(savedParam);
		}
		String showDataValue = request.getParameter("showData");
		if (showDataValue != null) {
			savedParam.setName("showData");
			savedParam.setValue("true");
			savedParameterService.addSavedParameter(savedParam);
		}
		String showPointsValue = request.getParameter("showPoints");
		if (showPointsValue != null) {
			savedParam.setName("showPoints");
			savedParam.setValue("true");
			savedParameterService.addSavedParameter(savedParam);
		}
	}

	@PostMapping("/clearSavedParameterSelection")
	public @ResponseBody
	AjaxResponse clearSavedParameterSelection(HttpSession session,
			@RequestParam("reportId") Integer reportId) {

		logger.debug("Entering clearSavedParameterSelection");

		AjaxResponse response = new AjaxResponse();

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			int userId = sessionUser.getUserId();

			savedParameterService.deleteSavedParameters(userId, reportId);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.getMessage());
		}

		return response;
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param model the model to use
	 * @param session the http session
	 * @return returns the jsp file to display
	 */
	private String showEditReports(Model model, HttpSession session) {
		logger.debug("Entering showEditReports");

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			model.addAttribute("reportGroups", reportGroupService.getAdminReportGroups(sessionUser));
			model.addAttribute("datasources", datasourceService.getAdminDatasources(sessionUser));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "editReports";
	}

	@RequestMapping(value = "/copyReport", method = RequestMethod.GET)
	public String copyReport(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering copyReport: id={}", id);

		try {
			model.addAttribute("report", reportService.getReportWithOwnSource(id));
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
			model.addAttribute("encryptors", encryptorService.getAllEncryptors());
			model.addAttribute("runId", ArtUtils.getUniqueId());
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
	 * Saves a file and updates the report template property with the file name
	 *
	 * @param file the file to save
	 * @param reportTypeId the reportTypeId for the report related to the file
	 * @param overwrite whether to overwrite existing files
	 * @param locale the locale
	 * @param report the report to update, or null
	 * @return a problem description if there was a problem, otherwise null
	 * @throws IOException
	 */
	private String saveFile(MultipartFile file, int reportTypeId, boolean overwrite,
			Locale locale, Report report) throws IOException {

		ReportType reportType = ReportType.toEnum(reportTypeId);
		String templatesPath;
		if (reportType.isUseJsTemplatesPath()) {
			templatesPath = Config.getJsTemplatesPath();
		} else if (reportType == ReportType.JPivotMondrian) {
			templatesPath = Config.getDefaultTemplatesPath();
		} else {
			templatesPath = Config.getTemplatesPath();
		}

		return saveFile(file, templatesPath, overwrite, locale, report);
	}

	/**
	 * Saves a file and updates the report template property with the file name
	 *
	 * @param file the file to save
	 * @param templatesPath the path where to save the file
	 * @param overwrite whether to overwrite existing files
	 * @param locale the locale
	 * @param report the report to update, or null
	 * @return a problem description if there was a problem, otherwise null
	 * @throws IOException
	 */
	private String saveFile(MultipartFile file, String templatesPath, boolean overwrite,
			Locale locale, Report report) throws IOException {

		logger.debug("Entering saveFile: report={}, templatesPath='{}', overwrite={}",
				report, templatesPath, overwrite);

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
		validExtensions.add("xml");
		validExtensions.add("jrxml");
		validExtensions.add("jasper");
		validExtensions.add("xls");
		validExtensions.add("xlsx");
		validExtensions.add("xlsm");
		validExtensions.add("png");
		validExtensions.add("jpg");
		validExtensions.add("jpeg");
		validExtensions.add("ftl");
		validExtensions.add("ftlh"); //http://freemarker.org/docs/pgui_config_outputformatsautoesc.html
		validExtensions.add("ftlx");
		validExtensions.add("vm");
		validExtensions.add("docx");
		validExtensions.add("odt");
		validExtensions.add("pptx");
		validExtensions.add("html"); //for thymeleaf reports
		validExtensions.add("csv"); //for pivottable.js csv server reports (.csv)
		validExtensions.add("txt"); //for pivottable.js csv server reports (.txt for other delimited files e.g. tab separated, pipe separated etc)
		validExtensions.add("css"); //for c3.js additional css
		validExtensions.add("js"); //for datamaps additional js
		validExtensions.add("json"); //for datamaps optional data file

		//save file
		UploadHelper uploadHelper = new UploadHelper(messageSource, locale);
		String message = uploadHelper.saveFile(file, templatesPath, validExtensions, overwrite);

		if (message != null) {
			return message;
		}

		if (report != null) {
			String filename = file.getOriginalFilename();
			report.setTemplate(filename);
		}

		return null;
	}

	/**
	 * Sets report source and chart options for the given report
	 *
	 * @param report the report to use
	 * @param action the action to take, "add" or "edit"
	 */
	private void setProperties(Report report, String action) {
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

		//https://stackoverflow.com/questions/11005286/check-if-null-boolean-is-true-results-in-exception
		logger.debug("report.getChartOptions().getShowLegend() = {}", report.getChartOptions().getShowLegend());
		if (BooleanUtils.isTrue(report.getChartOptions().getShowLegend())) {
			showLegend = "showLegend";
		}
		logger.debug("report.getChartOptions().getShowLabels() = {}", report.getChartOptions().getShowLabels());
		if (BooleanUtils.isTrue(report.getChartOptions().getShowLabels())) {
			showLabels = "showLabels";
		}
		logger.debug("report.getChartOptions().getShowPoints() = {}", report.getChartOptions().getShowPoints());
		if (BooleanUtils.isTrue(report.getChartOptions().getShowPoints())) {
			showPoints = "showPoints";
		}
		logger.debug("report.getChartOptions().getShowData() = {}", report.getChartOptions().getShowData());
		if (BooleanUtils.isTrue(report.getChartOptions().getShowData())) {
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

			String extension = FilenameUtils.getExtension(filename);

			if (FinalFilenameValidator.isValid(filename)) {
				try {
					boolean overwrite = BooleanUtils.toBoolean(request.getParameter("overwriteFiles"));
					String errorMessage;
					Report report = null;
					if (StringUtils.equalsAnyIgnoreCase(extension, "css", "js")) {
						String templatesPath = Config.getJsTemplatesPath();
						errorMessage = saveFile(file, templatesPath, overwrite, locale, report);
					} else {
						errorMessage = saveFile(file, reportTypeId, overwrite, locale, report);
					}
					if (errorMessage != null) {
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
				Object[] value = {
					filename
				};
				String errorMessage = messageSource.getMessage("reports.message.invalidFilename2", value, locale);
				fileDetails.setError(errorMessage);
			}

			fileList.add(fileDetails);
		}

		response.put("files", fileList);

		return response;
	}

	/**
	 * Sets the password fields, encrypting them in preparation for saving
	 *
	 * @param report the report
	 * @param action the action
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws Exception
	 */
	private String setPasswords(Report report, String action) throws Exception {
		logger.debug("Entering setPasswords: report={}, action='{}'", report, action);

		//set open password
		boolean useCurrentOpenPassword = false;
		String newOpenPassword = report.getOpenPassword();

		if (report.isUseNoneOpenPassword()) {
			newOpenPassword = null;
		} else if (StringUtils.isEmpty(newOpenPassword) && StringUtils.equalsAny(action, "edit", "copy")) {
			//password field blank. use current password
			useCurrentOpenPassword = true;
		}

		if (useCurrentOpenPassword) {
			//password field blank. use current password
			Report currentReport = reportService.getReport(report.getReportId());
			if (currentReport == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				newOpenPassword = currentReport.getOpenPassword();
			}
		}

		//encrypt new password
		if (StringUtils.equals(newOpenPassword, "")) {
			//if password set as empty string, there is no way to specify empty string as password for xlsx workbooks
			newOpenPassword = null;
		}
		String encryptedOpenPassword = AesEncryptor.encrypt(newOpenPassword);
		report.setOpenPassword(encryptedOpenPassword);

		//set modify password
		boolean useCurrentModifyPassword = false;
		String newModifyPassword = report.getModifyPassword();

		if (report.isUseNoneModifyPassword()) {
			newModifyPassword = null;
		} else if (StringUtils.isEmpty(newModifyPassword) && StringUtils.equalsAny(action, "edit", "copy")) {
			//password field blank. use current password
			useCurrentModifyPassword = true;
		}

		if (useCurrentModifyPassword) {
			//password field blank. use current password
			Report currentReport = reportService.getReport(report.getReportId());
			if (currentReport == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				newModifyPassword = currentReport.getModifyPassword();
			}
		}

		//encrypt new password
		if (StringUtils.equals(newModifyPassword, "")) {
			newModifyPassword = null;
		}
		String encryptedModifyPassword = AesEncryptor.encrypt(newModifyPassword);
		report.setModifyPassword(encryptedModifyPassword);

		return null;
	}

	@RequestMapping(value = "/parameterReports", method = RequestMethod.GET)
	public String showParameterReports(Model model,
			@RequestParam("parameterId") Integer parameterId) {

		logger.debug("Entering showParameterReports: parameterId={}", parameterId);

		try {
			model.addAttribute("report", reportService.getReportWithOwnSource(parameterId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "parameterReports";
	}

	@PostMapping("/savePivotTableJs")
	public @ResponseBody
	AjaxResponse savePivotTableJs(@RequestParam("reportId") Integer reportId,
			@RequestParam("config") String config, @RequestParam("name") String name,
			@RequestParam("description") String description,
			@RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite,
			@RequestParam(value = "saveAsPivotTable", defaultValue = "false") Boolean saveAsPivotTable,
			@RequestParam(value = "saveSelectedParameters", defaultValue = "false") Boolean saveSelectedParameters,
			HttpSession session, HttpServletRequest request, Locale locale) {

		logger.debug("Entering savePivotTableJs: reportId={}, config='{}',"
				+ " name='{}', description='{}', overwrite={}, saveAsPivotTable={},"
				+ " saveSelectedParameters={}",
				reportId, config, name, description, overwrite, saveAsPivotTable,
				saveSelectedParameters);

		//https://stackoverflow.com/questions/37359851/how-to-receive-html-check-box-value-in-spring-mvc-controller
		AjaxResponse response = new AjaxResponse();

		try {
			Report report = reportService.getReport(reportId);
			report.encryptPasswords();

			User sessionUser = (User) session.getAttribute("sessionUser");

			report.setPivotTableJsSavedOptions(config);
			if (StringUtils.isNotBlank(description)) {
				report.setDescription(description);
			}
			if (StringUtils.isNotBlank(name)) {
				report.setName(name);
			}

			boolean reportNameNotProvided = false;
			boolean reportNameExists = false;
			if (StringUtils.isBlank(name)) {
				if (!overwrite) {
					reportNameNotProvided = true;
				}
			} else {
				reportNameExists = reportService.reportExists(name);
			}

			if (reportNameNotProvided) {
				String message = messageSource.getMessage("reports.message.reportNameNotProvided", null, locale);
				response.setErrorMessage(message);
			} else if (reportNameExists) {
				String message = messageSource.getMessage("reports.message.reportNameExists", null, locale);
				response.setErrorMessage(message);
			} else {
				if (overwrite) {
					reportService.updateReport(report, sessionUser);
				} else {
					if (saveAsPivotTable) {
						report.setReportType(ReportType.PivotTableJs);
					}

					reportService.copyReport(report, report.getReportId(), sessionUser);
					reportService.grantAccess(report, sessionUser);

					//don't return whole report object. will include clear text passwords e.g. for the datasource which can be seen from the browser console
					response.setData(report.getReportId());
				}
				if (saveSelectedParameters) {
					saveParameterSelection(session, request, report.getReportId());
				}
				response.setSuccess(true);
			}
		} catch (Exception ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.getMessage());
		}

		return response;
	}

	@PostMapping("/deletePivotTableJs")
	public @ResponseBody
	AjaxResponse deletePivotTableJs(@RequestParam("id") Integer id) {
		logger.debug("Entering deletePivotTableJs: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = reportService.deleteReport(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//report not deleted because of linked jobs
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.getMessage());
		}

		return response;
	}

	@PostMapping("/saveGridstack")
	public @ResponseBody
	AjaxResponse saveGridstack(@RequestParam(value = "reportId", required = false) Integer reportId,
			@RequestParam("config") String config, @RequestParam("name") String name,
			@RequestParam("description") String description,
			@RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite,
			@RequestParam(value = "saveSelectedParameters", defaultValue = "false") Boolean saveSelectedParameters,
			@RequestParam(value = "selfServiceDashboard", defaultValue = "false") Boolean selfServiceDashboard,
			HttpSession session, HttpServletRequest request, Locale locale) {

		logger.debug("Entering saveGridstack: reportId={}, config='{}',"
				+ " name='{}', description='{}', overwrite={},"
				+ " saveSelectedParameters={}, selfServiceDashboard={}",
				reportId, config, name, description, overwrite,
				saveSelectedParameters, selfServiceDashboard);

		Integer viewReportId = null;
		return saveAdHoc(ReportType.GridstackDashboard, reportId, config, name, description, overwrite, saveSelectedParameters, selfServiceDashboard, session, request, locale, viewReportId);
	}

	@PostMapping("/saveSelfService")
	public @ResponseBody
	AjaxResponse saveSelfService(@RequestParam(value = "reportId", required = false) Integer reportId,
			@RequestParam("config") String config, @RequestParam("name") String name,
			@RequestParam("description") String description,
			@RequestParam(value = "overwrite", defaultValue = "false") Boolean overwrite,
			@RequestParam(value = "viewReportId", required = false) Integer viewReportId,
			HttpSession session, HttpServletRequest request, Locale locale) {

		logger.debug("Entering saveGridstack: reportId={}, config='{}',"
				+ " name='{}', description='{}', overwrite={}, viewReportId={}",
				reportId, config, name, description, overwrite, viewReportId);

		boolean saveSelectedParameters = false;
		boolean selfServiceDashboard = false;
		return saveAdHoc(ReportType.Tabular, reportId, config, name, description, overwrite, saveSelectedParameters, selfServiceDashboard, session, request, locale, viewReportId);
	}

	/**
	 * Saves an ad hoc or self service report or dashboard
	 *
	 * @param reportType the report type
	 * @param reportId the report id to edit or null if it's a new report
	 * @param config configuration for the report
	 * @param name the report name
	 * @param description the report description
	 * @param overwrite whether to overwrite the existing report
	 * @param saveSelectedParameters whether to save selected parameters
	 * @param selfServiceDashboard whether this is from the self service
	 * dashboards page
	 * @param session the http session
	 * @param request the http request
	 * @param locale the locale
	 * @param viewReportId the view report id when creating/saving self service
	 * reports
	 * @return the action result
	 */
	private AjaxResponse saveAdHoc(ReportType reportType, Integer reportId,
			String config, String name, String description, Boolean overwrite,
			Boolean saveSelectedParameters, Boolean selfServiceDashboard,
			HttpSession session, HttpServletRequest request, Locale locale,
			Integer viewReportId) {

		AjaxResponse response = new AjaxResponse();

		try {
			Report report;
			if (reportId == null) {
				if (reportType == ReportType.GridstackDashboard) {
					report = new Report();
					report.setReportType(reportType);
				} else {
					Report viewReport = reportService.getReport(viewReportId);
					Cloner cloner = new Cloner();
					report = cloner.deepClone(viewReport);
					report.setViewReportId(viewReportId);
					report.setReportType(ReportType.Tabular);
				}
			} else {
				report = reportService.getReport(reportId);
				report.encryptPasswords();
			}

			User sessionUser = (User) session.getAttribute("sessionUser");

			boolean reportNameNotProvided = false;
			boolean reportNameExists = false;
			if (StringUtils.isBlank(name)) {
				if (!overwrite) {
					reportNameNotProvided = true;
				}
			} else {
				reportNameExists = reportService.reportExists(name);
			}

			if (reportNameNotProvided) {
				String message = messageSource.getMessage("reports.message.reportNameNotProvided", null, locale);
				response.setErrorMessage(message);
			} else if (reportNameExists) {
				String message = messageSource.getMessage("reports.message.reportNameExists", null, locale);
				response.setErrorMessage(message);
			} else {
				if (reportType == ReportType.GridstackDashboard) {
					report.setGridstackSavedOptions(config);
					if (selfServiceDashboard) {
						//https://stackoverflow.com/questions/11664894/jackson-deserialize-using-generic-class
						//https://stackoverflow.com/questions/8263008/how-to-deserialize-json-file-starting-with-an-array-in-jackson
						ObjectMapper mapper = new ObjectMapper();
						List<GridstackItemOptions> itemOptions = mapper.readValue(config, new TypeReference<List<GridstackItemOptions>>() {
						});
						if (CollectionUtils.isEmpty(itemOptions)) {
							String message = messageSource.getMessage("reports.message.nothingToSave", null, locale);
							response.setErrorMessage(message);
							return response;
						} else {
							StringBuilder sb = new StringBuilder();
							sb.append("<DASHBOARD>");
							for (GridstackItemOptions itemOption : itemOptions) {
								sb.append("<ITEM>")
										.append("<TITLE>")
										.append(itemOption.getTitle())
										.append("</TITLE>")
										.append("<REPORTID>")
										.append(itemOption.getReportId())
										.append("</REPORTID>")
										.append("</ITEM>");
							}
							sb.append("</DASHBOARD>");
							report.setReportSource(sb.toString());
						}
					}
				} else {
					report.setSelfServiceOptions(config);
					SelfServiceHelper selfServiceHelper = new SelfServiceHelper();
					selfServiceHelper.applySelfServiceFields(report, sessionUser);
				}

				if (overwrite) {
					if (StringUtils.isNotEmpty(description)) {
						report.setDescription(description);
					}
					if (StringUtils.isNotBlank(name)) {
						report.setName(name);
						if (reportType == ReportType.GridstackDashboard) {
							report.setShortDescription(name);
						}
					}

					reportService.updateReport(report, sessionUser);
				} else {
					report.setDescription(description);
					report.setName(name);
					if (reportType == ReportType.GridstackDashboard) {
						report.setShortDescription(name);
					}

					if (reportId == null) {
						reportService.addReport(report, sessionUser);
					} else {
						reportService.copyReport(report, report.getReportId(), sessionUser);
					}

					reportService.grantAccess(report, sessionUser);

					//don't return whole report object. will include clear text passwords e.g. for the datasource which can be seen from the browser console
					response.setData(report.getReportId());
				}
				if (saveSelectedParameters) {
					saveParameterSelection(session, request, report.getReportId());
				}
				response.setSuccess(true);
			}
		} catch (Exception ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.getMessage());
		}

		return response;
	}

	@RequestMapping(value = "/getLovValues", method = RequestMethod.GET)
	public @ResponseBody
	List<Map<String, String>> getLovValues(@RequestParam("reportId") Integer reportId,
			@RequestParam(value = "defaultValues", required = false) List<String> defaultValues,
			HttpSession session, HttpServletRequest request, Locale locale) {

		logger.debug("Entering getLovValues: reportId={}", reportId);

		//https://appelsiini.net/projects/chained/
		//encapsulate values in a list (will be a json array) to ensure values
		//are displayed in the order given
		List<Map<String, String>> list = new ArrayList<>();
		Map<String, String> values = new HashMap<>();
		ReportRunner reportRunner = new ReportRunner();
		try {
			Report report = reportService.getReport(reportId);
			reportRunner.setReport(report);

			User sessionUser = (User) session.getAttribute("sessionUser");
			reportRunner.setUser(sessionUser);

			ParameterProcessor paramProcessor = new ParameterProcessor();
			ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request, locale);
			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			reportRunner.setReportParamsMap(reportParamsMap);

			values = reportRunner.getLovValues();
		} catch (SQLException | RuntimeException | ParseException | IOException ex) {
			logger.error("Error", ex);
		} finally {
			reportRunner.close();
		}

		for (Map.Entry<String, String> entry : values.entrySet()) {
			Map<String, String> value = new HashMap<>();
			String encodedValue = Encode.forHtmlContent(entry.getValue());
			value.put(entry.getKey(), encodedValue);
			list.add(value);
		}

		if (defaultValues != null) {
			for (String defaultValue : defaultValues) {
				Map<String, String> value = new HashMap<>();
				value.put("selected", defaultValue);
				list.add(value);
			}
		}

		return list;
	}

	@PostMapping("/shareReport")
	public @ResponseBody
	AjaxResponse shareReport(@RequestParam("shareReportId") Integer shareReportId,
			@RequestParam(value = "users[]", required = false) Integer[] users,
			@RequestParam(value = "userGroups[]", required = false) Integer[] userGroups,
			@RequestParam("action") String action) {

		logger.debug("Entering shareReport: shareReportId={}, action='{}'", shareReportId, action);

		AjaxResponse response = new AjaxResponse();

		try {
			Integer[] reports = {shareReportId};
			Integer[] reportGroups = null;
			Integer[] jobs = null;

			accessRightService.updateAccessRights(action, users, userGroups, reports, reportGroups, jobs);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@PostMapping("/cancelQuery")
	@ResponseBody
	public AjaxResponse cancelQuery(@RequestParam("runId") String runId) {
		logger.debug("Entering cancelQuery: runId='{}'", runId);

		//https://stackoverflow.com/questions/15067563/spring-controller-404-retuned-after-post-method-invoked
		//https://www.baeldung.com/spring-request-response-body
		AjaxResponse response = new AjaxResponse();

		try {
			boolean cancelled = Config.cancelQuery(runId);
			if (cancelled) {
				response.setSuccess(true);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@GetMapping("/runningQueries")
	public String showRunningQueries(Model model) {
		logger.debug("Entering showRunningQueries");

		return "runningQueries";
	}

	@GetMapping("/getRunningQueries")
	@ResponseBody
	public AjaxResponse getRunningQueries(Locale locale, HttpServletRequest request,
			HttpServletResponse httpResponse) {

		logger.debug("Entering getRunningQueries");

		AjaxResponse ajaxResponse = new AjaxResponse();

		WebContext ctx = new WebContext(request, httpResponse, servletContext, locale);

		String templateName = "runningQueriesAction";
		String dtAction = defaultTemplateEngine.process(templateName, ctx);

		List<ReportRunDetails> runningQueries = Config.getRunningQueries();
		for (ReportRunDetails query : runningQueries) {
			query.setDtAction(dtAction);
		}

		ajaxResponse.setData(runningQueries);
		ajaxResponse.setSuccess(true);

		return ajaxResponse;
	}

}
