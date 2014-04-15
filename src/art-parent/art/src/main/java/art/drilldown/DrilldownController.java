/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.drilldown;

import art.report.ReportService;
import art.servlets.ArtConfig;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
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
 * Controller for drilldown configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class DrilldownController {

	private static final Logger logger = LoggerFactory.getLogger(DrilldownController.class);

	@Autowired
	private DrilldownService drilldownService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private MessageSource messageSource;

	@RequestMapping(value = "/app/drilldowns", method = RequestMethod.GET)
	public String showDrilldowns(Model model,
			@RequestParam("reportId") Integer reportId) {

		logger.debug("Entering showDrilldowns");

		try {
			model.addAttribute("parentReportId", reportId);
			model.addAttribute("parentReportName", reportService.getReportName(reportId));
			model.addAttribute("drilldowns", drilldownService.getDrilldowns(reportId));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "drilldowns";
	}

	@RequestMapping(value = "/app/deleteDrilldown", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteDrilldown(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteDrilldown: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			drilldownService.deleteDrilldown(id);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addDrilldown", method = RequestMethod.GET)
	public String addDrilldown(Model model, Locale locale,
			@RequestParam("parent") Integer parent) {

		logger.debug("Entering addDrilldown: parent={}", parent);

		model.addAttribute("drilldown", new Drilldown());
		return showDrilldown("add", model, locale, parent);
	}

	@RequestMapping(value = "/app/saveDrilldown", method = RequestMethod.POST)
	public String saveDrilldown(@ModelAttribute("drilldown") @Valid Drilldown drilldown,
			@RequestParam("action") String action,
			@RequestParam("parent") Integer parent,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			Locale locale) {

		logger.debug("Entering saveDrilldown: drilldown={}, action='{}', parent={}", drilldown, action, parent);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showDrilldown(action, model, locale, parent);
		}

		try {
			if (StringUtils.equals(action, "add")) {
				drilldownService.addDrilldown(drilldown, parent);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				drilldownService.updateDrilldown(drilldown);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", reportService.getReportName(drilldown.getDrilldownReport().getReportId()));
			return "redirect:/app/drilldowns.do?reportId=" + parent;
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showDrilldown(action, model, locale, parent);
	}

	@RequestMapping(value = "/app/editDrilldown", method = RequestMethod.GET)
	public String editDrilldown(@RequestParam("id") Integer id, Model model,
			Locale locale) {

		logger.debug("Entering editDrilldown: id={}", id);

		int parentReportId = 0;

		try {
			Drilldown drilldown = drilldownService.getDrilldown(id);
			model.addAttribute("drilldown", drilldown);
			if (drilldown != null) {
				parentReportId = drilldown.getParentReportId();
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showDrilldown("edit", model, locale, parentReportId);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showDrilldown(String action, Model model, Locale locale,
			Integer parent) {

		logger.debug("Entering showDrilldown: action='{}', parent={}", action, parent);

		try {
			model.addAttribute("parentReportName", reportService.getReportName(parent));
			model.addAttribute("drilldownReports", reportService.getCandidateDrilldownReports());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		Map<String, String> reportFormats = new LinkedHashMap<>();
		for (String reportFormat : ArtConfig.getReportFormats()) {
			reportFormats.put(reportFormat, messageSource.getMessage(reportFormat, null, locale));
		}
		reportFormats.put("graph", messageSource.getMessage("htmlGraph", null, locale));
		reportFormats.put("pdfGraph", messageSource.getMessage("pdfGraph", null, locale));
		reportFormats.put("pngGraph", messageSource.getMessage("pdfGraph", null, locale));

		model.addAttribute("reportFormats", reportFormats);

		model.addAttribute("parent", parent);
		model.addAttribute("action", action);
		return "editDrilldown";
	}

	@RequestMapping(value = "/app/moveDrilldown", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse moveDrilldown(Model model,
			@RequestParam("id") Integer id,
			@RequestParam("fromPosition") Integer fromPosition,
			@RequestParam("toPosition") Integer toPosition,
			@RequestParam("direction") String direction) {

		logger.debug("Entering moveDrilldown: id={}, fromPosition={}, toPosition={}, direction='{}'",
				id, fromPosition, toPosition, direction);

		AjaxResponse response = new AjaxResponse();

		try {
			Drilldown drilldown = drilldownService.getDrilldown(id);
			if (drilldown == null) {
				logger.warn("Drilldown not found: {}", id);
			} else {
				drilldownService.moveDrilldown(id, fromPosition, toPosition, direction, drilldown.getParentReportId());
				response.setSuccess(true);
				response.setData(drilldown.getDrilldownReport().getName());
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

}
