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
package art.parameter;

import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.reportparameter.ReportParameterService;
import art.user.User;
import art.general.ActionResult;
import art.general.AjaxResponse;
import art.report.UploadHelper;
import art.servlets.Config;
import art.utils.AjaxTableHelper;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

/**
 * Controller for parameter configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class ParameterController {

	private static final Logger logger = LoggerFactory.getLogger(ParameterController.class);

	@Autowired
	private ParameterService parameterService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private ReportParameterService reportParameterService;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private TemplateEngine defaultTemplateEngine;

	@RequestMapping(value = "/parameters", method = RequestMethod.GET)
	public String showParameters(Model model) {
		logger.debug("Entering showParameters");

		return showParametersPage("all", model);
	}

	@RequestMapping(value = "/unusedParameters", method = RequestMethod.GET)
	public String showUnusedParameters(Model model) {
		logger.debug("Entering showUnusedParameters");

		return showParametersPage("unused", model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action "all" or "unused"
	 * @param model the spring model
	 * @return the jsp file to display
	 */
	private String showParametersPage(String action, Model model) {
		logger.debug("Entering showParametersPage: action='{}'", action);

		model.addAttribute("action", action);

		return "parameters";
	}

	@GetMapping("/getParameters")
	public @ResponseBody
	AjaxResponse getParameters(Locale locale, HttpServletRequest request,
			HttpServletResponse httpResponse,
			@RequestParam(value = "action", required = false) String action) {

		logger.debug("Entering getParameters: action='{}'", action);

		AjaxResponse ajaxResponse = new AjaxResponse();

		try {
			List<Parameter> parameters;
			if (StringUtils.equals(action, "unused")) {
				parameters = parameterService.getAllUnusedParametersBasic();
			} else {
				parameters = parameterService.getAllParametersBasic();
			}

			WebContext ctx = new WebContext(request, httpResponse, servletContext, locale);
			AjaxTableHelper ajaxTableHelper = new AjaxTableHelper(messageSource, locale);

			List<Parameter> finalParameters = new ArrayList<>();

			for (Parameter parameter : parameters) {
				String enhancedName = ajaxTableHelper.processName(parameter.getName(), parameter.getCreationDate(), parameter.getUpdateDate());
				parameter.setName2(enhancedName);

				ctx.setVariable("parameter", parameter);
				String templateName = "parametersAction";
				String dtAction = defaultTemplateEngine.process(templateName, ctx);
				parameter.setDtAction(dtAction);

				finalParameters.add(parameter.getBasicParameter());
			}

			ajaxResponse.setData(finalParameters);
			ajaxResponse.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			ajaxResponse.setErrorMessage(ex.toString());
		}

		return ajaxResponse;
	}

	@RequestMapping(value = "/deleteParameter", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteParameter(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteParameter: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = parameterService.deleteParameter(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//parameter not deleted because of linked reports
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteParameters", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteParameters(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteParameters: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = parameterService.deleteParameters(ids);

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

	@RequestMapping(value = "/addParameter", method = RequestMethod.GET)
	public String addParameter(Model model,
			@RequestParam(value = "reportId", required = false) Integer reportId) {

		logger.debug("Entering addParameter: reportId={}", reportId);

		Parameter param = new Parameter();
		param.setParameterType(ParameterType.SingleValue);

		if (reportId == null) {
			param.setShared(true);
		}

		model.addAttribute("parameter", param);

		Integer returnReportId = null;
		Integer reportParameterId = null;
		return showEditParameter("add", model, reportId, returnReportId, reportParameterId);
	}

	@RequestMapping(value = "/editParameter", method = RequestMethod.GET)
	public String editParameter(@RequestParam("id") Integer id, Model model,
			@RequestParam(value = "returnReportId", required = false) Integer returnReportId) {

		logger.debug("Entering editParameter: id={}, returnReportId={}", id, returnReportId);

		try {
			model.addAttribute("parameter", parameterService.getParameter(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		Integer reportId = null;
		Integer reportParameterId = null;
		return showEditParameter("edit", model, reportId, returnReportId, reportParameterId);
	}

	@RequestMapping(value = "/copyParameter", method = RequestMethod.GET)
	public String copyParameter(@RequestParam("id") Integer id, Model model,
			@RequestParam(value = "reportId", required = false) Integer reportId,
			@RequestParam(value = "reportParameterId", required = false) Integer reportParameterId,
			HttpSession session) {

		logger.debug("Entering copyParameter: id={}, reportId={},"
				+ " reportParameterId={}", id, reportId, reportParameterId);

		try {
			model.addAttribute("parameter", parameterService.getParameter(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		Integer returnReportId = null;
		return showEditParameter("copy", model, reportId, returnReportId, reportParameterId);
	}

	@RequestMapping(value = "/saveParameter", method = RequestMethod.POST)
	public String saveParameter(@ModelAttribute("parameter") @Valid Parameter parameter,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("action") String action,
			@RequestParam(value = "reportId", required = false) Integer reportId,
			@RequestParam(value = "returnReportId", required = false) Integer returnReportId,
			@RequestParam(value = "reportParameterId", required = false) Integer reportParameterId,
			@RequestParam(value = "templateFile", required = false) MultipartFile templateFile,
			HttpSession session, Locale locale) {
		
		//https://github.com/spring-projects/spring-framework/issues/26088

		logger.debug("Entering saveParameter: parameter={}, action='{}',"
				+ " reportId={}, returnReportId={}, reportParameterId={}",
				parameter, action, reportId, returnReportId, reportParameterId);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditParameter(action, model, reportId, returnReportId, reportParameterId);
		}

		try {
			//save template file
			String saveFileMessage = saveTemplateFile(templateFile, parameter, locale);
			logger.debug("saveFileMessage='{}'", saveFileMessage);
			if (saveFileMessage != null) {
				model.addAttribute("plainMessage", saveFileMessage);
				return showEditParameter(action, model, reportId, returnReportId, reportParameterId);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equalsAny(action, "add", "copy")) {
				parameterService.addParameter(parameter, sessionUser);
				if (reportId != null && reportId != 0) {
					if (reportParameterId != null && reportParameterId != 0) {
						ReportParameter reportParameter = reportParameterService.getReportParameter(reportParameterId);
						reportParameter.setParameter(parameter);
						reportParameterService.updateReportParameter(reportParameter);
					} else {
						ReportParameter reportParameter = new ReportParameter();
						reportParameter.setParameter(parameter);
						reportParameterService.addReportParameter(reportParameter, reportId);
					}
				}
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				parameterService.updateParameter(parameter, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = parameter.getName() + " (" + parameter.getParameterId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);

			Integer reportParameterConfigReportId = null;
			if (reportId != null && reportId != 0) {
				reportParameterConfigReportId = reportId;
			} else if (returnReportId != null && returnReportId != 0) {
				reportParameterConfigReportId = returnReportId;
			}

			if (reportParameterConfigReportId == null) {
				return "redirect:/parameters";
			} else {
				return "redirect:/reportParameterConfig?reportId=" + reportParameterConfigReportId;
			}
		} catch (SQLException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditParameter(action, model, reportId, returnReportId, reportParameterId);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to be performed
	 * @param model the model to use
	 * @param reportId the report id of the report to add this parameter to,
	 * null if not applicable
	 * @param returnReportId the report id to display in the
	 * reportParameterConfig page after saving the report, null if not
	 * applicable
	 * @param reportParameterId the id of the report parameter to replace with
	 * this parameter
	 * @return the jsp file to display
	 */
	private String showEditParameter(String action, Model model, Integer reportId,
			Integer returnReportId, Integer reportParameterId) {

		logger.debug("Entering showEditParameter: action='{}', reportId={},"
				+ " returnReportId={}, reportParameterId={}",
				action, reportId, returnReportId, reportParameterId);

		try {
			model.addAttribute("lovReports", reportService.getLovReports());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("parameterTypes", ParameterType.list());
		//form:options tag by default renders all enum values if no valid items are explicity passed/used
		//https://jira.spring.io/browse/SPR-3389
		//https://stackoverflow.com/questions/15073830/spring-formoptions-tag-with-enum
		model.addAttribute("dataTypes", ParameterDataType.list());

		model.addAttribute("action", action);
		model.addAttribute("reportId", reportId);
		model.addAttribute("returnReportId", returnReportId);
		model.addAttribute("reportParameterId", reportParameterId);

		return "editParameter";
	}

	@RequestMapping(value = "/reportsForParameter", method = RequestMethod.GET)
	public String showReportsForParameter(@RequestParam("parameterId") Integer parameterId, Model model) {
		logger.debug("Entering showReportsForParameter: parameterId={}", parameterId);

		try {
			model.addAttribute("reports", reportService.getReportsForParameter(parameterId));
			model.addAttribute("parameter", parameterService.getParameter(parameterId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportsForParameter";
	}

	/**
	 * Saves a template file and updates the appropriate parameter property with
	 * the file name
	 *
	 * @param file the file to save
	 * @param parameter the parameter object to set
	 * @param locale the locale
	 * @return a problem description if there was a problem, otherwise null
	 * @throws IOException
	 */
	private String saveTemplateFile(MultipartFile file, Parameter parameter,
			Locale locale) throws IOException {

		logger.debug("Entering saveTemplateFile: parameter={}", parameter);

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
		validExtensions.add("js");

		//save file
		String templatesPath = Config.getJsTemplatesPath();
		UploadHelper uploadHelper = new UploadHelper(messageSource, locale);
		String message = uploadHelper.saveFile(file, templatesPath, validExtensions, parameter.isOverwriteFiles());

		if (message != null) {
			return message;
		}

		String filename = file.getOriginalFilename();
		parameter.setTemplate(filename);

		return null;
	}
}
