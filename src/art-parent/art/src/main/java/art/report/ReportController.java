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
import art.utils.Encrypter;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.io.FilenameUtils;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring controller for reports pages
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

		logger.debug("Entering showReports: reportGroupId={}", reportGroupId);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			List<AvailableReport> reports = reportService.getAvailableReports(sessionUser.getUsername());

			//allow to focus public_user in one report only. is this feature used? it's not documented
			if (reportGroupId != null) {
				List<AvailableReport> filteredReports = new ArrayList<>();
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
		logger.debug("Entering showReportsConfig");

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
		logger.debug("Entering deleteReport: id={}", id);

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
		logger.debug("Entering addReportGet");

		model.addAttribute("report", new Report());
		return showReport("add", model, session);
	}

	@RequestMapping(value = "/app/addReport", method = RequestMethod.POST)
	public String addReportPost(@ModelAttribute("report") @Valid Report report,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session,
			@RequestParam("templateFile") MultipartFile templateFile,
			@RequestParam("subreportFile") MultipartFile subreportFile) {

		logger.debug("Entering addReportPost: report={}", report);

		String action = "add";

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showReport(action, model, session);
		}

		try {
			//finalise report properties
			String prepareReportMessage = prepareReport(report, templateFile, subreportFile, action);
			logger.debug("prepareReportMessage='{}'", prepareReportMessage);
			if (prepareReportMessage != null) {
				model.addAttribute("message", prepareReportMessage);
				return showReport(action, model, session);
			}

			reportService.addReport(report);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			redirectAttributes.addFlashAttribute("recordName", report.getName());
			return "redirect:/app/reportsConfig.do";
		} catch (SQLException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport(action, model, session);
	}

	@RequestMapping(value = "/app/editReport", method = RequestMethod.GET)
	public String editReportGet(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering editReportGet: id={}", id);

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

		logger.debug("Entering editReportPost: report={}", report);

		String action = "edit";

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showReport(action, model, session);
		}

		try {
			//finalise report properties
			String prepareReportMessage = prepareReport(report, templateFile, subreportFile, action);
			logger.debug("prepareReportMessage='{}'", prepareReportMessage);
			if (prepareReportMessage != null) {
				model.addAttribute("message", prepareReportMessage);
				return showReport(action, model, session);
			}

			reportService.updateReport(report);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			redirectAttributes.addFlashAttribute("recordName", report.getName());
			return "redirect:/app/reportsConfig.do";
		} catch (SQLException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport(action, model, session);
	}

	@RequestMapping(value = "/app/copyReport", method = RequestMethod.GET)
	public String copyReportGet(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		try {
			model.addAttribute("report", reportService.getReport(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport("copy", model, session);
	}

	@RequestMapping(value = "/app/copyReport", method = RequestMethod.POST)
	public String copyReportPost(@ModelAttribute("report") @Valid Report report,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session,
			@RequestParam("templateFile") MultipartFile templateFile,
			@RequestParam("subreportFile") MultipartFile subreportFile) {

		logger.debug("Entering copyReportPost: report={}", report);

		String action = "copy";

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showReport(action, model, session);
		}

		try {
			//finalise report properties
			String prepareReportMessage = prepareReport(report, templateFile, subreportFile, action);
			logger.debug("prepareReportMessage='{}'", prepareReportMessage);
			if (prepareReportMessage != null) {
				model.addAttribute("message", prepareReportMessage);
				return showReport(action, model, session);
			}

			reportService.copyReport(report, report.getReportId());
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			redirectAttributes.addFlashAttribute("recordName", report.getName());
			return "redirect:/app/reportsConfig.do";
		} catch (SQLException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showReport(action, model, session);
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
		logger.debug("Entering showReport: action='{}'", action);

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

	/**
	 * Save file
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private String saveFile(MultipartFile file) throws IOException {
		return saveFile(file, null);
	}

	/**
	 * Save file and update report template property with the file name
	 *
	 * @param file
	 * @param report
	 * @return
	 * @throws IOException
	 */
	private String saveFile(MultipartFile file, Report report) throws IOException {
		logger.debug("Entering saveFile: report={}", report);

		logger.debug("file.isEmpty()={}", file.isEmpty());
		if (file.isEmpty()) {
			return null;
		}

		//check upload file type
		List<String> validExtensions = new ArrayList<>();
		validExtensions.add("xml");
		validExtensions.add("jrxml");
		validExtensions.add("xls");
		validExtensions.add("xlsx");

		long maxUploadSize = ArtConfig.getSettings().getMaxFileUploadSize(); //size in MB
		maxUploadSize = maxUploadSize * 1000 * 1000; //size in bytes

		//save template file
		long uploadSize = file.getSize();
		String filename = file.getOriginalFilename();
		logger.debug("filename='{}'", filename);
		String extension = FilenameUtils.getExtension(filename).toLowerCase();

		logger.debug("maxUploadSize={}, uploadSize={}", maxUploadSize, uploadSize);
		if (maxUploadSize >= 0 && uploadSize > maxUploadSize) { //0 effectively means no uploads allowed
			return "reports.message.fileBiggerThanMax";
		}

		if (!validExtensions.contains(extension)) {
			return "reports.message.invalidFileType";
		}

		//save file
		String destinationFilename = ArtConfig.getTemplatesPath() + filename;
		File destinationFile = new File(destinationFilename);
		file.transferTo(destinationFile);

		if (report != null) {
			report.setTemplate(filename);
		}

		return null;
	}

	/**
	 * Set xmla password and chart options setting properties
	 *
	 * @param report
	 * @param action
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setProperties(Report report, String action) throws SQLException {
		logger.debug("Entering setProperties: report={}, action='{}'", report, action);

		String setXmlaPasswordMessage = setXmlaPassword(report, action);
		logger.debug("setXmlaPasswordMessage='{}'", setXmlaPasswordMessage);
		if (setXmlaPasswordMessage != null) {
			return setXmlaPasswordMessage;
		}

		//set report source for text reports
		logger.debug("report.getReportType()={}", report.getReportType());
		ReportType reportType = ReportType.toEnum(report.getReportType());
		if (reportType == ReportType.Text || reportType == ReportType.TextPublic) {
			report.setReportSource(report.getReportSourceHtml());
		}

		//build chart options setting string
		logger.debug("(report.getChartOptions() != null) = {}", report.getChartOptions() != null);
		if (report.getReportType() < 0 && report.getChartOptions() != null) {
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
				report.getChartOptions().getBgColor(),
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

		return null;
	}

	/**
	 * Set xmla password
	 *
	 * @param report
	 * @param action
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setXmlaPassword(Report report, String action) throws SQLException {
		logger.debug("Entering setXmlapassword: report={}, action='{}'", report, action);

		boolean useCurrentXmlaPassword = false;
		String newXmlaPassword = report.getXmlaPassword();

		logger.debug("report.isUseBlankXmlaPassword()={}", report.isUseBlankXmlaPassword());
		if (report.isUseBlankXmlaPassword()) {
			newXmlaPassword = "";
		} else {
			logger.debug("StringUtils.isEmpty(newXmlaPassword)={}", StringUtils.isEmpty(newXmlaPassword));
			if (StringUtils.isEmpty(newXmlaPassword) && StringUtils.equals(action, "edit")) {
				//password field blank. use current password
				useCurrentXmlaPassword = true;
			}
		}

		logger.debug("useCurrentXmlaPassword={}", useCurrentXmlaPassword);
		if (useCurrentXmlaPassword) {
			//password field blank. use current password
			Report currentReport = reportService.getReport(report.getReportId());
			logger.debug("currentReport={}", currentReport);
			if (currentReport == null) {
				return "page.message.cannotUseCurrentXmlaPassword";
			} else {
				report.setXmlaPassword(currentReport.getXmlaPassword());
			}
		} else {
			logger.debug("StringUtils.isNotEmpty(newXmlaPassword)={}", StringUtils.isNotEmpty(newXmlaPassword));
			if (StringUtils.isNotEmpty(newXmlaPassword)) {
				newXmlaPassword = "o:" + Encrypter.encrypt(newXmlaPassword);
			}
			report.setXmlaPassword(newXmlaPassword);
		}

		return null;
	}

	/**
	 * Finalise report properties
	 *
	 * @param report
	 * @param templateFile
	 * @param subreportFile
	 * @param action
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws IOException
	 * @throws SQLException
	 */
	private String prepareReport(Report report, MultipartFile templateFile,
			MultipartFile subreportFile, String action) throws IOException, SQLException {

		logger.debug("Entering prepareReport: report={}, action='{}", report, action);

		String message;

		message = saveFile(templateFile, report); //update report template property
		if (message != null) {
			return message;
		}

		message = saveFile(subreportFile);
		if (message != null) {
			return message;
		}

		message = setProperties(report, action);
		if (message != null) {
			return message;
		}

		return null;
	}

}
