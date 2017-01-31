/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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

import art.enums.ReportFormat;
import art.report.Report;
import art.report.ReportService;
import art.utils.AjaxResponse;
import java.sql.SQLException;
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

	@RequestMapping(value = "/drilldowns", method = RequestMethod.GET)
	public String showDrilldowns(Model model, @RequestParam("reportId") Integer reportId) {

		logger.debug("Entering showDrilldowns: reportId={}", reportId);

		try {
			model.addAttribute("parentReportId", reportId);
			model.addAttribute("parentReportName", reportService.getReportName(reportId));
			model.addAttribute("drilldowns", drilldownService.getDrilldowns(reportId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "drilldowns";
	}

	@RequestMapping(value = "/deleteDrilldown", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteDrilldown(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteDrilldown: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			drilldownService.deleteDrilldown(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteDrilldowns", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteDrilldowns(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteDrilldowns: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			drilldownService.deleteDrilldowns(ids);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addDrilldown", method = RequestMethod.GET)
	public String addDrilldown(Model model,
			@RequestParam("parent") Integer parent) {

		logger.debug("Entering addDrilldown: parent={}", parent);

		model.addAttribute("drilldown", new Drilldown());

		return showEditDrilldown("add", model, parent);
	}

	@RequestMapping(value = "/editDrilldown", method = RequestMethod.GET)
	public String editDrilldown(@RequestParam("id") Integer id, Model model) {

		logger.debug("Entering editDrilldown: id={}", id);

		int parentReportId = 0;

		try {
			Drilldown drilldown = drilldownService.getDrilldown(id);
			model.addAttribute("drilldown", drilldown);
			if (drilldown != null) {
				parentReportId = drilldown.getParentReportId();
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDrilldown("edit", model, parentReportId);
	}

	@RequestMapping(value = "/saveDrilldown", method = RequestMethod.POST)
	public String saveDrilldown(@ModelAttribute("drilldown") @Valid Drilldown drilldown,
			@RequestParam("action") String action,
			@RequestParam("parent") Integer parent,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveDrilldown: drilldown={}, action='{}', parent={}",
				drilldown, action, parent);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditDrilldown(action, model, parent);
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
			return "redirect:/drilldowns?reportId=" + parent;
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDrilldown(action, model, parent);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action "add" or "edit"
	 * @param model the spring model to populate
	 * @param parent the report id of the parent report
	 * @return the jsp file to display
	 */
	private String showEditDrilldown(String action, Model model, Integer parent) {
		logger.debug("Entering showEditDrilldown: action='{}', parent={}", action, parent);

		try {
			model.addAttribute("parentReportName", reportService.getReportName(parent));
			List<Report> drilldownReports = reportService.getDrilldownReports();
			drilldownReports.addAll(reportService.getDashboardReports());
			model.addAttribute("drilldownReports", drilldownReports);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("reportFormats", ReportFormat.list());

		model.addAttribute("parent", parent);
		model.addAttribute("action", action);

		return "editDrilldown";
	}

	@RequestMapping(value = "/moveDrilldown", method = RequestMethod.POST)
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
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}
}
