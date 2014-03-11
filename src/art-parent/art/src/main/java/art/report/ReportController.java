/**
 * Copyright (C) 2013 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.report;

import art.datasource.DatasourceService;
import art.enums.AccessLevel;
import art.enums.ReportStatus;
import art.enums.ReportType;
import art.reportgroup.ReportGroupService;
import art.servlets.ArtConfig;
import art.user.User;
import art.utils.AjaxResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.io.FilenameUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring controller for reports page
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

	@RequestMapping(value = "/app/reports", method = RequestMethod.GET)
	public String showReports(HttpSession session,
			@RequestParam(value = "reportId", required = false) Integer reportGroupId,
			HttpServletRequest request, Model model) {

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			List<AvailableReport> reports = reportService.getAvailableReports(sessionUser.getUsername());

			//allow to focus public_user in one report only. is this feature used? it's not documented
			if (reportGroupId != null) {
				List<AvailableReport> filteredReports = new ArrayList<AvailableReport>();
				for (AvailableReport report : reports) {
					if (report.getReportGroupId() == reportGroupId) {
						filteredReports.add(report);
					}
				}
				model.addAttribute("reports", filteredReports);
			} else {
				model.addAttribute("reports", reports);
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reports";
	}

	/**
	 * Return available reports using ajax
	 *
	 * @param session
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/app/getReports", method = RequestMethod.GET)
	public @ResponseBody
	List<AvailableReport> getReports(HttpSession session, HttpServletRequest request) {
		//object will be automatically converted to json because of @ResponseBody and presence of jackson libraries
		//see http://www.mkyong.com/spring-mvc/spring-3-mvc-and-json-example/
		User sessionUser = (User) session.getAttribute("sessionUser");

		List<AvailableReport> reports = null;
		try {
			reports = reportService.getAvailableReports(sessionUser.getUsername());
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}

		return reports;
	}

	@RequestMapping(value = "/app/reportsConfig", method = RequestMethod.GET)
	public String showReportsConfig(Model model) {
		model.addAttribute("activeStatus", ReportStatus.Active.getValue());
		model.addAttribute("disabledStatus", ReportStatus.Disabled.getValue());

		try {
			model.addAttribute("reports", reportService.getAllReports());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportsConfig";
	}

	@RequestMapping(value = "/app/deleteReport", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteReport(@RequestParam("id") Integer id) {
		AjaxResponse response = new AjaxResponse();

		try {
			reportService.deleteReport(id);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addReport", method = RequestMethod.GET)
	public String addReportGet(Model model, HttpSession session) {
		model.addAttribute("report", new Report());
		return showReport("add", model, session);
	}

	@RequestMapping(value = "/app/addReport", method = RequestMethod.POST)
	public String addReportPost(@ModelAttribute("report") @Valid Report report,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session,
			@RequestParam("templateFile") MultipartFile templateFile,
			@RequestParam("subreportFile") MultipartFile subreportFile) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showReport("add", model, session);
		}

		try {
			String saveFileMessage;
			saveFileMessage = saveFile(templateFile, report);
			if (saveFileMessage != null) {
				model.addAttribute("message", saveFileMessage);
				return showReport("add", model, session);
			}
			saveFileMessage = saveFile(subreportFile, report);
			if (saveFileMessage != null) {
				model.addAttribute("message", saveFileMessage);
				return showReport("add", model, session);
			}

			reportService.addReport(report);
			redirectAttributes.addFlashAttribute("message", "page.message.recordAdded");
			return "redirect:/app/reportsConfig.do";
		} catch (SQLException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport("add", model, session);
	}

	@RequestMapping(value = "/app/editReport", method = RequestMethod.GET)
	public String editReportGet(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		try {
			model.addAttribute("report", reportService.getReport(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport("edit", model, session);
	}

	@RequestMapping(value = "/app/editReport", method = RequestMethod.POST)
	public String editReportPost(@ModelAttribute("report") @Valid Report report,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session,
			@RequestParam("templateFile") MultipartFile templateFile,
			@RequestParam("subreportFile") MultipartFile subreportFile) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showReport("edit", model, session);
		}

		try {
			String saveFileMessage;
			saveFileMessage = saveFile(templateFile, report);
			if (saveFileMessage != null) {
				model.addAttribute("message", saveFileMessage);
				return showReport("edit", model, session);
			}
			saveFileMessage = saveFile(subreportFile, report);
			if (saveFileMessage != null) {
				model.addAttribute("message", saveFileMessage);
				return showReport("edit", model, session);
			}

			reportService.updateReport(report);
			redirectAttributes.addFlashAttribute("message", "page.message.recordUpdated");
			return "redirect:/app/reportsConfig.do";
		} catch (SQLException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport("edit", model, session);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @param session
	 * @return
	 */
	private String showReport(String action, Model model, HttpSession session) {
		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			int userId = sessionUser.getUserId();
			AccessLevel accessLevel = sessionUser.getAccessLevel();

			model.addAttribute("reportGroups", reportGroupService.getAdminReportGroups(userId, accessLevel));
			model.addAttribute("reportStatuses", ReportStatus.list());
			model.addAttribute("reportTypes", ReportType.list());

			model.addAttribute("datasources", datasourceService.getAdminDatasources(userId, accessLevel));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);
		return "editReport";
	}

	private String saveFile(MultipartFile file, Report report) throws IOException {
		//check upload file type
		List<String> validExtensions = new ArrayList<>();
		validExtensions.add("xml");
		validExtensions.add("jrxml");
		validExtensions.add("xls");
		validExtensions.add("xlsx");

		long maxUploadSize = ArtConfig.getSettings().getMaxFileUploadSize(); //size in MB
		maxUploadSize = maxUploadSize * 1000 * 1000; //size in bytes

		//save template file
		if (!file.isEmpty()) {
			long uploadSize = file.getSize();
			String filename = file.getOriginalFilename();
			String extension = FilenameUtils.getExtension(filename).toLowerCase();

			if (maxUploadSize > 0 && uploadSize > maxUploadSize) {
				return "reports.message.fileBiggerThanMax";
			}

			if (!validExtensions.contains(extension)) {
				return "reports.message.invalidFileType";
			}

			//save file
			String destinationFilename = ArtConfig.getTemplatesPath() + filename;
			File destinationFile = new File(destinationFilename);
			file.transferTo(destinationFile);

			report.setTemplate(filename);
		}

		return null;
	}

}
