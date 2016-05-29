/*
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
package art.report;

import art.datasource.DatasourceService;
import art.enums.AccessLevel;
import art.enums.ReportType;
import art.parameter.Parameter;
import art.reportgroup.ReportGroupService;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportRunner;
import art.servlets.Config;
import art.user.User;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
import art.encryption.DesEncryptor;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

	@RequestMapping(value = "/app/reports", method = RequestMethod.GET)
	public String showReports(HttpSession session, HttpServletRequest request, Model model) {
		logger.debug("Entering showReports");

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			model.addAttribute("reports", reportService.getAvailableReports(sessionUser.getUserId()));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reports";
	}

	@RequestMapping(value = "/app/selectReportParameters", method = RequestMethod.GET)
	public String selectReportParameters(HttpSession session,
			@RequestParam("reportId") Integer reportId,
			HttpServletRequest request, Model model) {

		logger.debug("Entering selectReportParameters: reportId={}", reportId);

		try {
			Report report = reportService.getReport(reportId);
			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return "reportError";
			} else {
				model.addAttribute("report", report);

				//prepare report parameters
				ParameterProcessor paramProcessor = new ParameterProcessor();
				ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);

				Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
				List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();

				for (ReportParameter reportParam : reportParamsList) {
					Parameter param = reportParam.getParameter();
					if (param.isUseLov()) {
						//get all possible lov values.
						//don't run chained parameters. their values will be
						//loaded dynamically depending on parent and depends paremeter values
						if (!reportParam.isChained()) {
							ReportRunner lovReportRunner = null;
							try {
								lovReportRunner = new ReportRunner();
								int lovReportId = param.getLovReportId();
								Report lovReport = reportService.getReport(lovReportId);
								lovReportRunner.setReport(lovReport);
								lovReportRunner.setReportParamsMap(reportParamsMap);
								boolean applyFilters = false; //don't apply filters so as to get all values
								Map<Object, String> lovValues = lovReportRunner.getLovValuesAsObjects(applyFilters);
								reportParam.setLovValues(lovValues);
								Map<String, String> lovValuesAsString = reportParam.convertLovValuesFromObjectToString(lovValues);
								reportParam.setLovValuesAsString(lovValuesAsString);
							} finally {
								if (lovReportRunner != null) {
									lovReportRunner.close();
								}
							}
						}
					}
				}

				//create map in order to display parameters by position
				Map<Integer, ReportParameter> reportParams = new TreeMap<>();
				for (ReportParameter reportParam : reportParamsList) {
					reportParams.put(reportParam.getPosition(), reportParam);
				}

				model.addAttribute("reportParams", reportParams);

				ReportType reportType = report.getReportType();

				boolean enableReportFormats;
				switch (reportType) {
					case Dashboard:
					case Mondrian:
					case MondrianXmla:
					case SqlServerXmla:
					case Update:
					case Text:
					case TabularHtml:
					case JxlsArt:
					case JxlsTemplate:
						enableReportFormats = false;
						break;
					default:
						enableReportFormats = true;
						List<String> reportFormats = getAvailableReportFormats(report.getReportType());
						model.addAttribute("reportFormats", reportFormats);
				}
				model.addAttribute("enableReportFormats", enableReportFormats);

				User sessionUser = (User) session.getAttribute("sessionUser");
				int accessLevel = sessionUser.getAccessLevel().getValue();

				boolean enableSchedule;
				if (accessLevel >= AccessLevel.ScheduleUser.getValue()
						&& Config.getSettings().isSchedulingEnabled()) {

					switch (reportType) {
						case Dashboard:
						case Mondrian:
						case MondrianXmla:
						case SqlServerXmla:
						case Text:
							enableSchedule = false;
							break;
						default:
							enableSchedule = true;
					}
				} else {
					enableSchedule = false;
				}
				model.addAttribute("enableSchedule", enableSchedule);

				boolean enableShowSql;
				boolean enableShowSelectedParameters;

				switch (reportType) {
					case Dashboard:
					case Mondrian:
					case MondrianXmla:
					case SqlServerXmla:
					case JasperReportsTemplate:
					case JxlsTemplate:
					case Text:
						enableShowSql = false;
						enableShowSelectedParameters = false;
						break;
					default:
						if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
							enableShowSql = true;
						} else {
							enableShowSql = false;
						}

						if (!reportParamsList.isEmpty()) {
							enableShowSelectedParameters = true;
						} else {
							enableShowSelectedParameters = false;
						}
				}
				model.addAttribute("enableShowSql", enableShowSql);
				model.addAttribute("enableShowSelectedParameters", enableShowSelectedParameters);

				model.addAttribute("isChart", reportType.isChart());

				boolean enableRunInline;

				switch (reportType) {
					case Dashboard:
					case Mondrian:
					case MondrianXmla:
					case SqlServerXmla:
						enableRunInline = false;
						break;
					default:
						enableRunInline = true;
				}
				model.addAttribute("enableRunInline", enableRunInline);

				boolean enablePrint;

				switch (reportType) {
					case Dashboard:
					case Mondrian:
					case MondrianXmla:
					case SqlServerXmla:
						enablePrint = false;
						break;
					default:
						if (reportType.isChart()) {
							enablePrint = false;
						} else {
							enablePrint = true;
						}
				}
				model.addAttribute("enablePrint", enablePrint);
			}
		} catch (SQLException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "selectReportParameters";
	}

	/**
	 * Returns the available report formats for the given report type
	 * 
	 * @param reportType the report type
	 * @return the available report formats
	 */
	private List<String> getAvailableReportFormats(ReportType reportType) {
		logger.debug("Entering getAvailableReportFormats: reportType={}", reportType);

		List<String> formats = new ArrayList<>();

		if (reportType.isChart()) {
			formats.add("html");
			formats.add("pdf");
			formats.add("png");
		} else {
			switch (reportType) {
				case Tabular:
				case Crosstab:
					String formatsString = Config.getSettings().getReportFormats();
					String[] formatsArray = StringUtils.split(formatsString, ",");
					formats = Arrays.asList(formatsArray);
					break;
				case JasperReportsArt:
				case JasperReportsTemplate:
					formats.add("pdf");
					formats.add("xls");
					formats.add("xlsx");
					formats.add("html");
					break;
				case Group:
					formats.add("html");
					formats.add("xlsx");
					break;
				default:
					throw new IllegalArgumentException("Unexpected report type: " + reportType);
			}
		}

		return formats;
	}

	@RequestMapping(value = "/app/getReports", method = RequestMethod.GET)
	public @ResponseBody
	List<Report> getReports(HttpSession session, HttpServletRequest request) {
		//object will be automatically converted to json because of @ResponseBody and presence of jackson libraries
		//see http://www.mkyong.com/spring-mvc/spring-3-mvc-and-json-example/

		logger.debug("Entering getReports");

		User sessionUser = (User) session.getAttribute("sessionUser");

		List<Report> reports = null;
		try {
			reports = reportService.getAvailableReports(sessionUser.getUserId());
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}

		return reports;
	}

	@RequestMapping(value = "/app/reportsConfig", method = RequestMethod.GET)
	public String showReportsConfig(Model model) {
		logger.debug("Entering showReportsConfig");

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
			ActionResult deleteResult = reportService.deleteReport(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//report not deleted because of linked jobs
				response.setData(deleteResult.getData());
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/deleteReports", method = RequestMethod.POST)
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
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addReport", method = RequestMethod.GET)
	public String addReport(Model model, HttpSession session) {
		logger.debug("Entering addReport");

		model.addAttribute("report", new Report());
		return showEditReport("add", model, session);
	}

	@RequestMapping(value = "/app/editReport", method = RequestMethod.GET)
	public String editReport(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering editReport: id={}", id);

		try {
			model.addAttribute("report", reportService.getReport(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReport("edit", model, session);
	}

	@RequestMapping(value = "/app/editReports", method = RequestMethod.GET)
	public String editReports(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editReports: ids={}", ids);

		MultipleReportEdit multipleReportEdit = new MultipleReportEdit();
		multipleReportEdit.setIds(ids);

		model.addAttribute("multipleReportEdit", multipleReportEdit);
		return "editReports";
	}

	@RequestMapping(value = "/app/saveReport", method = RequestMethod.POST)
	public String saveReport(@ModelAttribute("report") @Valid Report report,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session, @RequestParam("action") String action,
			@RequestParam("templateFile") MultipartFile templateFile,
			@RequestParam("resourcesFile") MultipartFile resourcesFile) {

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
			redirectAttributes.addFlashAttribute("recordName", report.getName());
			return "redirect:/app/reportsConfig.do";
		} catch (SQLException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReport(action, model, session);
	}

	@RequestMapping(value = "/app/saveReports", method = RequestMethod.POST)
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
			return "redirect:/app/reportsConfig.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditReports();
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

	@RequestMapping(value = "/app/copyReport", method = RequestMethod.GET)
	public String copyReport(@RequestParam("id") Integer id, Model model,
			HttpSession session) {

		logger.debug("Entering copyReport: id={}", id);

		try {
			model.addAttribute("report", reportService.getReport(id));
		} catch (SQLException ex) {
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
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);

		return "editReport";
	}

	/**
	 * Saves a file
	 *
	 * @param file the file to save
	 * @return an i18n message string if there was a problem, otherwise null
	 * @throws IOException
	 */
	private String saveFile(MultipartFile file) throws IOException {
		return saveFile(file, null);
	}

	/**
	 * Saves a file and updates the report template property with the file name
	 *
	 * @param file the file to save
	 * @param report the report to set
	 * @return an i18n message string if there was a problem, otherwise null
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
		validExtensions.add("png");
		validExtensions.add("jpg");

		long maxUploadSize = Config.getSettings().getMaxFileUploadSizeMB(); //size in MB
		maxUploadSize = maxUploadSize * 1000L * 1000L; //size in bytes

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
		String destinationFilename = Config.getTemplatesPath() + filename;
		File destinationFile = new File(destinationFilename);
		file.transferTo(destinationFile);

		if (report != null) {
			report.setTemplate(filename);
		}

		return null;
	}

	/**
	 * Sets xmla password and chart options for the given report
	 *
	 * @param report the report to use
	 * @param action the action to take, "add" or "edit"
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
		logger.debug("report.getReportTypeId()={}", report.getReportTypeId());
		ReportType reportType = ReportType.toEnum(report.getReportTypeId());
		report.setReportType(reportType);
		if (reportType == ReportType.Text) {
			report.setReportSource(report.getReportSourceHtml());
		}

		//build chart options setting string
		logger.debug("(report.getChartOptions() != null) = {}", report.getChartOptions() != null);
		if (reportType.isChart() && report.getChartOptions() != null) {
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

		return null;
	}

	/**
	 * Sets the xmla password for the given report
	 *
	 * @param report the report to use
	 * @param action the action to take, "add" or "edit"
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
				newXmlaPassword = "o:" + DesEncryptor.encrypt(newXmlaPassword);
			}
			report.setXmlaPassword(newXmlaPassword);
		}

		return null;
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

		message = saveFile(templateFile, report); //update report template property
		if (message != null) {
			return message;
		}

		message = saveFile(resourcesFile);
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
