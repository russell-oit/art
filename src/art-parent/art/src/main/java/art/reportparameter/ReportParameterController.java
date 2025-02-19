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
package art.reportparameter;

import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.report.Report;
import art.report.ReportService;
import art.general.AjaxResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
 * Controller for report parameter configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class ReportParameterController {

	private static final Logger logger = LoggerFactory.getLogger(ReportParameterController.class);

	@Autowired
	private ReportParameterService reportParameterService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private ParameterService parameterService;

	@RequestMapping(value = "/reportParameterConfig", method = RequestMethod.GET)
	public String showReportParameterConfig(Model model,
			@RequestParam("reportId") Integer reportId) {

		logger.debug("Entering showReportParameterConfig: reportId={}", reportId);

		try {
			model.addAttribute("reportId", reportId);
			String reportName = "";
			Report report = reportService.getReport(reportId);
			if (report != null) {
				reportName = report.getName();
			}
			model.addAttribute("reportName", reportName);
			model.addAttribute("reportParameters", reportParameterService.getReportParameters(reportId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportParameterConfig";
	}

	@RequestMapping(value = "/deleteReportParameter", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReportParameter(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteReportParameter: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ReportParameter reportParameter = reportParameterService.getReportParameter(id);
			Parameter parameter = reportParameter.getParameter();

			reportParameterService.deleteReportParameter(id);

			//also delete parameter if not shared
			if (!parameter.isShared()) {
				parameterService.deleteParameter(parameter.getParameterId());
			}

			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteReportParameters", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReportParameters(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteReportParameters: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			List<Parameter> parameters = new ArrayList<>();
			for (Integer id : ids) {
				ReportParameter reportParameter = reportParameterService.getReportParameter(id);
				parameters.add(reportParameter.getParameter());
			}

			reportParameterService.deleteReportParameters(ids);

			for (Parameter parameter : parameters) {
				if (!parameter.isShared()) {
					parameterService.deleteParameter(parameter.getParameterId());
				}
			}

			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addReportParameter", method = RequestMethod.GET)
	public String addReportParameter(Model model, @RequestParam("reportId") Integer reportId) {
		logger.debug("Entering addReportParameter: reportId={}", reportId);

		model.addAttribute("reportParameter", new ReportParameter());

		return showEditReportParameter("add", model, reportId);
	}

	@RequestMapping(value = "/editReportParameter", method = RequestMethod.GET)
	public String editReportParameter(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editReportParameter: id={}", id);

		int reportId = 0;

		try {
			ReportParameter reportParameter = reportParameterService.getReportParameter(id);
			if (reportParameter != null) {
				reportId = reportParameter.getReport().getReportId();
			}
			model.addAttribute("reportParameter", reportParameter);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReportParameter("edit", model, reportId);
	}

	@RequestMapping(value = "/saveReportParameter", method = RequestMethod.POST)
	public String saveReportParameter(@ModelAttribute("reportParameter") @Valid ReportParameter reportParameter,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam("action") String action,
			@RequestParam("reportId") Integer reportId) {

		logger.debug("Entering saveReportParameter: reportParameter={}, action='{}', "
				+ "reportId={}", reportParameter, action, reportId);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditReportParameter(action, model, reportId);
		}

		try {
			if (StringUtils.equals(action, "add")) {
				reportParameterService.addReportParameter(reportParameter, reportId);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				reportParameterService.updateReportParameter(reportParameter);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			int paramId = reportParameter.getParameter().getParameterId();
			String paramName = parameterService.getParameterName(paramId);

			String recordName = paramName + " (" + paramId + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);

			return "redirect:/reportParameterConfig?reportId=" + reportId;
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReportParameter(action, model, reportId);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to take
	 * @param model the model to use
	 * @param reportId the report id
	 * @return the jsp file to display
	 */
	private String showEditReportParameter(String action, Model model,
			Integer reportId) {

		logger.debug("Entering showEditReportParameter: action='{}', reportId={}", action, reportId);

		try {
			String reportName = "";
			Report report = reportService.getReport(reportId);
			if (report != null) {
				reportName = report.getName();
			}
			model.addAttribute("reportName", reportName);

			if (StringUtils.equals(action, "add")) {
				model.addAttribute("parameters", parameterService.getSharedParameters());
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("reportId", reportId);
		model.addAttribute("action", action);

		return "editReportParameter";
	}

	@RequestMapping(value = "/moveReportParameter", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse moveReportParameter(Model model,
			@RequestParam("id") Integer id,
			@RequestParam("fromPosition") Integer fromPosition,
			@RequestParam("toPosition") Integer toPosition,
			@RequestParam("direction") String direction) {

		logger.debug("Entering moveReportParameter: id={}, fromPosition={}, toPosition={}, direction='{}'",
				id, fromPosition, toPosition, direction);

		AjaxResponse response = new AjaxResponse();

		try {
			ReportParameter reportParameter = reportParameterService.getReportParameter(id);
			if (reportParameter == null) {
				logger.warn("Report parameter not found: {}", id);
			} else {
				reportParameterService.moveReportParameter(id, fromPosition, toPosition, direction, reportParameter.getReport().getReportId());
				response.setSuccess(true);
				response.setData(reportParameter.getParameter().getName());
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}
}
