/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.migration;

import art.accessright.UserGroupReportRight;
import art.accessright.UserReportRight;
import art.artdatabase.ArtDatabase;
import art.cache.CacheHelper;
import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.datasource.DatasourceService;
import art.dbutils.DatabaseUtils;
import art.destination.Destination;
import art.destination.DestinationService;
import art.drilldown.Drilldown;
import art.encryptor.Encryptor;
import art.encryptor.EncryptorService;
import art.enums.MigrationFileFormat;
import art.enums.MigrationRecordType;
import art.enums.ReportType;
import art.holiday.Holiday;
import art.holiday.HolidayService;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.report.Report;
import art.report.ReportServiceHelper;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.reportoptions.C3Options;
import art.reportoptions.CsvServerOptions;
import art.reportoptions.DatamapsOptions;
import art.reportoptions.JxlsOptions;
import art.reportoptions.OrgChartOptions;
import art.reportoptions.WebMapOptions;
import art.reportparameter.ReportParameter;
import art.reportrule.ReportRule;
import art.rule.Rule;
import art.rule.RuleService;
import art.ruleValue.UserGroupRuleValue;
import art.ruleValue.UserRuleValue;
import art.schedule.Schedule;
import art.schedule.ScheduleService;
import art.servlets.Config;
import art.settings.Settings;
import art.settings.SettingsHelper;
import art.settings.SettingsService;
import art.smtpserver.SmtpServer;
import art.smtpserver.SmtpServerService;
import art.user.User;
import art.user.UserService;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupService;
import art.utils.ArtUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvRoutines;
import com.univocity.parsers.csv.CsvWriterSettings;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for importing repository artifacts
 *
 * @author Timothy Anyona
 */
@Controller
public class ImportRecordsController {

	private static final Logger logger = LoggerFactory.getLogger(ImportRecordsController.class);

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private DatasourceService datasourceService;

	@Autowired
	private DestinationService destinationService;

	@Autowired
	private EncryptorService encryptorService;

	@Autowired
	private HolidayService holidayService;

	@Autowired
	private ReportGroupService reportGroupService;

	@Autowired
	private SmtpServerService smtpServerService;

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private ScheduleService scheduleService;

	@Autowired
	private UserService userService;

	@Autowired
	private RuleService ruleService;

	@Autowired
	private ParameterService parameterService;

	@Autowired
	private CacheHelper cacheHelper;

	@GetMapping("/importRecords")
	public String showImportRecords(Model model, @RequestParam("type") String type) {
		logger.debug("Entering showImportRecords: type='{}'", type);

		ImportRecords importRecords = new ImportRecords();
		importRecords.setRecordType(MigrationRecordType.toEnum(type));

		model.addAttribute("importRecords", importRecords);

		return showEditImportRecords(model);
	}

	@PostMapping("/importRecords")
	public String processImportRecords(@Valid ImportRecords importRecords,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session, @RequestParam("importFile") MultipartFile importFile) {

		logger.debug("Entering processImportRecords");

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditImportRecords(model);
		}

		try {
			if (importFile.isEmpty()) {
				throw new IllegalArgumentException("Import file is empty");
			}

			User sessionUser = (User) session.getAttribute("sessionUser");

			String artTempPath = Config.getArtTempPath();
			String tempFilename = artTempPath + importFile.getOriginalFilename();
			File tempFile = new File(tempFilename);
			importFile.transferTo(tempFile);

			CsvWriterSettings writerSettings = new CsvWriterSettings();
			writerSettings.setHeaderWritingEnabled(true);

			CsvParserSettings parserSettings = new CsvParserSettings();
			parserSettings.setLineSeparatorDetectionEnabled(true);

			CsvRoutines csvRoutines = new CsvRoutines(parserSettings, writerSettings);

			Connection conn = DbConnections.getArtDbConnection();

			try {
				switch (importRecords.getRecordType()) {
					case Settings:
						importSettings(tempFile, sessionUser, conn, session);
						break;
					case Datasources:
						importDatasources(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case Destinations:
						importDestinations(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case Encryptors:
						importEncryptors(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case Holidays:
						importHolidays(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case ReportGroups:
						importReportGroups(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case SmtpServers:
						importSmtpServers(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case UserGroups:
						importUserGroups(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case Schedules:
						importSchedules(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case Users:
						importUsers(tempFile, sessionUser, conn, csvRoutines);
						break;
					case Rules:
						importRules(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case Parameters:
						importParameters(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case Reports:
						importReports(tempFile, sessionUser, conn, csvRoutines);
						break;
					default:
						break;
				}
			} finally {
				DatabaseUtils.close(conn);
				tempFile.delete();
			}

			redirectAttributes.addFlashAttribute("message", "page.message.recordsImported");
			return "redirect:/success";
		} catch (SQLException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditImportRecords(model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param model the model object
	 * @return the jsp file to display
	 */
	private String showEditImportRecords(Model model) {
		model.addAttribute("fileFormats", MigrationFileFormat.list());

		return "importRecords";
	}

	/**
	 * Imports application settings
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param session the http session
	 * @throws IOException
	 * @throws SQLException
	 */
	private void importSettings(File file, User sessionUser, Connection conn,
			HttpSession session) throws IOException, SQLException {

		logger.debug("Entering importSettings: sessionUser={}", sessionUser);

		ObjectMapper mapper = new ObjectMapper();
		Settings settings = mapper.readValue(file, Settings.class);
		if (settings.isClearTextPasswords()) {
			settings.encryptPasswords();
		}

		settingsService.importSettings(settings, sessionUser, conn);

		SettingsHelper settingsHelper = new SettingsHelper();
		settingsHelper.refreshSettings(settings, session, servletContext);
	}

	/**
	 * Imports datasource records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importDatasources(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importDatasources: sessionUser={}", sessionUser);

		List<Datasource> datasources;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = new ObjectMapper();
				datasources = mapper.readValue(file, new TypeReference<List<Datasource>>() {
				});
				break;
			case csv:
				datasources = csvRoutines.parseAll(Datasource.class, file);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		for (Datasource datasource : datasources) {
			if (datasource.isClearTextPassword()) {
				datasource.encryptPassword();
			}
		}

		datasourceService.importDatasources(datasources, sessionUser, conn);

		ArtDatabase artDbConfig = Config.getArtDbConfig();
		for (Datasource datasource : datasources) {
			if (datasource.isActive()) {
				datasource.decryptPassword();
				DbConnections.createConnectionPool(datasource, artDbConfig.getMaxPoolConnections(), artDbConfig.getConnectionPoolLibrary());
			}
		}
	}

	/**
	 * Imports destination records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importDestinations(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importDestinations: sessionUser={}", sessionUser);

		List<Destination> destinations;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = new ObjectMapper();
				destinations = mapper.readValue(file, new TypeReference<List<Destination>>() {
				});
				break;
			case csv:
				destinations = csvRoutines.parseAll(Destination.class, file);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		for (Destination destination : destinations) {
			if (destination.isClearTextPassword()) {
				destination.encryptPassword();
			}
		}

		destinationService.importDestinations(destinations, sessionUser, conn);
	}

	/**
	 * Imports encryptor records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importEncryptors(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importEncryptors: sessionUser={}", sessionUser);

		List<Encryptor> encryptors;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = new ObjectMapper();
				encryptors = mapper.readValue(file, new TypeReference<List<Encryptor>>() {
				});
				break;
			case csv:
				encryptors = csvRoutines.parseAll(Encryptor.class, file);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		for (Encryptor encryptor : encryptors) {
			if (encryptor.isClearTextPasswords()) {
				encryptor.encryptPasswords();
			}
		}

		encryptorService.importEncryptors(encryptors, sessionUser, conn);
	}

	/**
	 * Imports holiday records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importHolidays(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importHolidays: sessionUser={}", sessionUser);

		List<Holiday> holidays;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = new ObjectMapper();
				holidays = mapper.readValue(file, new TypeReference<List<Holiday>>() {
				});
				break;
			case csv:
				holidays = csvRoutines.parseAll(Holiday.class, file);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		holidayService.importHolidays(holidays, sessionUser, conn);
	}

	/**
	 * Imports report group records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importReportGroups(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importReportGroups: sessionUser={}", sessionUser);

		List<ReportGroup> reportGroups;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = new ObjectMapper();
				reportGroups = mapper.readValue(file, new TypeReference<List<ReportGroup>>() {
				});
				break;
			case csv:
				reportGroups = csvRoutines.parseAll(ReportGroup.class, file);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		reportGroupService.importReportGroups(reportGroups, sessionUser, conn);
	}

	/**
	 * Imports smtp server records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importSmtpServers(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importSmtpServers: sessionUser={}", sessionUser);

		List<SmtpServer> smtpServers;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = new ObjectMapper();
				smtpServers = mapper.readValue(file, new TypeReference<List<SmtpServer>>() {
				});
				break;
			case csv:
				smtpServers = csvRoutines.parseAll(SmtpServer.class, file);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		for (SmtpServer smtpServer : smtpServers) {
			if (smtpServer.isClearTextPassword()) {
				smtpServer.encryptPassword();
			}
		}

		smtpServerService.importSmtpServers(smtpServers, sessionUser, conn);
	}

	/**
	 * Imports user group records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importUserGroups(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importUserGroups: sessionUser={}", sessionUser);

		List<UserGroup> userGroups;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = new ObjectMapper();
				userGroups = mapper.readValue(file, new TypeReference<List<UserGroup>>() {
				});
				break;
			case csv:
				userGroups = csvRoutines.parseAll(UserGroup.class, file);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		userGroupService.importUserGroups(userGroups, sessionUser, conn);
	}

	/**
	 * Imports schedule records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importSchedules(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importSchedules: sessionUser={}", sessionUser);

		List<Schedule> schedules;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = new ObjectMapper();
				schedules = mapper.readValue(file, new TypeReference<List<Schedule>>() {
				});
				break;
			case csv:
				String extension = FilenameUtils.getExtension(file.getName());
				if (StringUtils.equalsIgnoreCase(extension, "csv")) {
					schedules = csvRoutines.parseAll(Schedule.class, file);
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					String artTempPath = Config.getArtTempPath();
					ArtUtils.unzipFile(file.getAbsolutePath(), artTempPath);
					String schedulesFileName = artTempPath + ExportRecords.EMBEDDED_SCHEDULES_FILENAME;
					File schedulesFile = new File(schedulesFileName);
					if (schedulesFile.exists()) {
						schedules = csvRoutines.parseAll(Schedule.class, schedulesFile);
					} else {
						throw new IllegalStateException("File not found: " + schedulesFileName);
					}

					String holidaysFileName = artTempPath + ExportRecords.EMBEDDED_HOLIDAYS_FILENAME;
					File holidaysFile = new File(holidaysFileName);
					if (holidaysFile.exists()) {
						List<Holiday> holidays = csvRoutines.parseAll(Holiday.class, holidaysFile);
						Map<Integer, Schedule> schedulesMap = new HashMap<>();
						for (Schedule schedule : schedules) {
							schedulesMap.put(schedule.getScheduleId(), schedule);
						}
						for (Holiday holiday : holidays) {
							int parentId = holiday.getParentId();
							Schedule schedule = schedulesMap.get(parentId);
							if (schedule == null) {
								throw new IllegalStateException("Schedule not found. Parent Id = " + parentId);
							} else {
								List<Holiday> sharedHolidays = schedule.getSharedHolidays();
								if (sharedHolidays == null) {
									sharedHolidays = new ArrayList<>();
								}
								sharedHolidays.add(holiday);
								schedule.setSharedHolidays(sharedHolidays);
							}
						}
					}
					schedulesFile.delete();
					holidaysFile.delete();
				} else {
					throw new IllegalArgumentException("Unexpected file extension: " + extension);
				}
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		scheduleService.importSchedules(schedules, sessionUser, conn);
	}

	/**
	 * Imports user records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @throws SQLException
	 */
	private void importUsers(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException, IOException {

		logger.debug("Entering importUsers: sessionUser={}", sessionUser);

		List<User> users;
		String extension = FilenameUtils.getExtension(file.getName());
		if (StringUtils.equalsIgnoreCase(extension, "csv")) {
			users = csvRoutines.parseAll(User.class, file);
		} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
			String artTempPath = Config.getArtTempPath();
			ArtUtils.unzipFile(file.getAbsolutePath(), artTempPath);
			String usersFileName = artTempPath + ExportRecords.EMBEDDED_USERS_FILENAME;
			File usersFile = new File(usersFileName);
			if (usersFile.exists()) {
				users = csvRoutines.parseAll(User.class, usersFile);
			} else {
				throw new IllegalStateException("File not found: " + usersFileName);
			}

			String userGroupsFileName = artTempPath + ExportRecords.EMBEDDED_USERGROUPS_FILENAME;
			File userGroupsFile = new File(userGroupsFileName);
			if (userGroupsFile.exists()) {
				List<UserGroup> allUserGroups = csvRoutines.parseAll(UserGroup.class, userGroupsFile);
				Map<Integer, User> usersMap = new HashMap<>();
				for (User user : users) {
					usersMap.put(user.getUserId(), user);
				}
				for (UserGroup userGroup : allUserGroups) {
					int parentId = userGroup.getParentId();
					User user = usersMap.get(parentId);
					if (user == null) {
						throw new IllegalStateException("User not found. Parent Id = " + parentId);
					} else {
						List<UserGroup> userGroups = user.getUserGroups();
						if (userGroups == null) {
							userGroups = new ArrayList<>();
						}
						userGroups.add(userGroup);
						user.setUserGroups(userGroups);
					}
				}
			}
			usersFile.delete();
			userGroupsFile.delete();
		} else {
			throw new IllegalArgumentException("Unexpected file extension: " + extension);
		}

		for (User user : users) {
			if (user.isClearTextPassword()) {
				user.encryptPassword();
			}
		}

		userService.importUsers(users, sessionUser, conn);
	}

	/**
	 * Imports rule records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importRules(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importRules: sessionUser={}", sessionUser);

		List<Rule> rules;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = new ObjectMapper();
				rules = mapper.readValue(file, new TypeReference<List<Rule>>() {
				});
				break;
			case csv:
				rules = csvRoutines.parseAll(Rule.class, file);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		ruleService.importRules(rules, sessionUser, conn);
	}

	/**
	 * Imports parameter records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importParameters(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importParameters: sessionUser={}", sessionUser);

		List<Parameter> parameters;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = new ObjectMapper();
				parameters = mapper.readValue(file, new TypeReference<List<Parameter>>() {
				});
				break;
			case csv:
				parameters = csvRoutines.parseAll(Parameter.class, file);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		for (Parameter parameter : parameters) {
			Report defaultValueReport = parameter.getDefaultValueReport();
			if (defaultValueReport != null) {
				defaultValueReport.encryptAllClearTextPasswords();
			}
			Report lovReport = parameter.getLovReport();
			if (lovReport != null) {
				lovReport.encryptAllClearTextPasswords();
			}
		}

		boolean local = true;
		parameterService.importParameters(parameters, sessionUser, conn, local);
	}

	/**
	 * Imports report records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @throws SQLException
	 */
	private void importReports(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException, IOException {

		logger.debug("Entering importReports: sessionUser={}", sessionUser);

		List<Report> reports;
		String extension = FilenameUtils.getExtension(file.getName());
		if (StringUtils.equalsIgnoreCase(extension, "csv")) {
			reports = csvRoutines.parseAll(Report.class, file);
		} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
			String artTempPath = Config.getArtTempPath();
			ArtUtils.unzipFile(file.getAbsolutePath(), artTempPath);
			String reportsFileName = artTempPath + ExportRecords.EMBEDDED_REPORTS_FILENAME;
			File reportsFile = new File(reportsFileName);
			if (reportsFile.exists()) {
				reports = csvRoutines.parseAll(Report.class, reportsFile);
			} else {
				throw new IllegalStateException("File not found: " + reportsFileName);
			}

			Map<Integer, Report> reportsMap = new HashMap<>();
			for (Report report : reports) {
				reportsMap.put(report.getReportId(), report);
			}

			String reportGroupsFileName = artTempPath + ExportRecords.EMBEDDED_REPORTGROUPS_FILENAME;
			File reportGroupsFile = new File(reportGroupsFileName);
			if (reportGroupsFile.exists()) {
				List<ReportGroup> allReportGroups = csvRoutines.parseAll(ReportGroup.class, reportGroupsFile);
				for (ReportGroup reportGroup : allReportGroups) {
					int parentId = reportGroup.getParentId();
					Report report = reportsMap.get(parentId);
					if (report == null) {
						throw new IllegalStateException("Report not found. Parent Id = " + parentId);
					} else {
						List<ReportGroup> reportGroups = report.getReportGroups();
						if (reportGroups == null) {
							reportGroups = new ArrayList<>();
						}
						reportGroups.add(reportGroup);
						report.setReportGroups(reportGroups);
					}
				}
			}

			String reportParamsFileName = artTempPath + ExportRecords.EMBEDDED_REPORTPARAMETERS_FILENAME;
			File reportParamsFile = new File(reportParamsFileName);
			if (reportParamsFile.exists()) {
				List<ReportParameter> allReportParams = csvRoutines.parseAll(ReportParameter.class, reportParamsFile);
				for (ReportParameter reportParam : allReportParams) {
					int parentId = reportParam.getParentId();
					Report report = reportsMap.get(parentId);
					if (report == null) {
						throw new IllegalStateException("Report not found. Parent Id = " + parentId);
					} else {
						List<ReportParameter> reportParams = report.getReportParams();
						if (reportParams == null) {
							reportParams = new ArrayList<>();
						}
						reportParams.add(reportParam);
						report.setReportParams(reportParams);
					}
				}
			}

			String userRuleValuesFileName = artTempPath + ExportRecords.EMBEDDED_USERRULEVALUES_FILENAME;
			File userRuleValuesFile = new File(userRuleValuesFileName);
			if (userRuleValuesFile.exists()) {
				List<UserRuleValue> allUserRuleValues = csvRoutines.parseAll(UserRuleValue.class, userRuleValuesFile);
				for (UserRuleValue userRuleValue : allUserRuleValues) {
					int parentId = userRuleValue.getParentId();
					Report report = reportsMap.get(parentId);
					if (report == null) {
						throw new IllegalStateException("Report not found. Parent Id = " + parentId);
					} else {
						List<UserRuleValue> userRuleValues = report.getUserRuleValues();
						if (userRuleValues == null) {
							userRuleValues = new ArrayList<>();
						}
						userRuleValues.add(userRuleValue);
						report.setUserRuleValues(userRuleValues);
					}
				}
			}

			String userGroupRuleValuesFileName = artTempPath + ExportRecords.EMBEDDED_USERGROUPRULEVALUES_FILENAME;
			File userGroupRuleValuesFile = new File(userGroupRuleValuesFileName);
			if (userGroupRuleValuesFile.exists()) {
				List<UserGroupRuleValue> allUserGroupRuleValues = csvRoutines.parseAll(UserGroupRuleValue.class, userGroupRuleValuesFile);
				for (UserGroupRuleValue userGroupRuleValue : allUserGroupRuleValues) {
					int parentId = userGroupRuleValue.getParentId();
					Report report = reportsMap.get(parentId);
					if (report == null) {
						throw new IllegalStateException("Report not found. Parent Id = " + parentId);
					} else {
						List<UserGroupRuleValue> userGroupRuleValues = report.getUserGroupRuleValues();
						if (userGroupRuleValues == null) {
							userGroupRuleValues = new ArrayList<>();
						}
						userGroupRuleValues.add(userGroupRuleValue);
						report.setUserGroupRuleValues(userGroupRuleValues);
					}
				}
			}

			String reportRulesFileName = artTempPath + ExportRecords.EMBEDDED_REPORTRULES_FILENAME;
			File reportRulesFile = new File(reportRulesFileName);
			if (reportRulesFile.exists()) {
				List<ReportRule> allReportRules = csvRoutines.parseAll(ReportRule.class, reportRulesFile);
				for (ReportRule reportRule : allReportRules) {
					int parentId = reportRule.getParentId();
					Report report = reportsMap.get(parentId);
					if (report == null) {
						throw new IllegalStateException("Report not found. Parent Id = " + parentId);
					} else {
						List<ReportRule> reportRules = report.getReportRules();
						if (reportRules == null) {
							reportRules = new ArrayList<>();
						}
						reportRules.add(reportRule);
						report.setReportRules(reportRules);
					}
				}
			}

			String userReportRightsFileName = artTempPath + ExportRecords.EMBEDDED_USERREPORTRIGHTS_FILENAME;
			File userReportRightsFile = new File(userReportRightsFileName);
			if (userReportRightsFile.exists()) {
				List<UserReportRight> allUserReportRights = csvRoutines.parseAll(UserReportRight.class, userReportRightsFile);
				for (UserReportRight userReportRight : allUserReportRights) {
					int parentId = userReportRight.getParentId();
					Report report = reportsMap.get(parentId);
					if (report == null) {
						throw new IllegalStateException("Report not found. Parent Id = " + parentId);
					} else {
						List<UserReportRight> userReportRights = report.getUserReportRights();
						if (userReportRights == null) {
							userReportRights = new ArrayList<>();
						}
						userReportRights.add(userReportRight);
						report.setUserReportRights(userReportRights);
					}
				}
			}

			String userGroupReportRightsFileName = artTempPath + ExportRecords.EMBEDDED_USERGROUPREPORTRIGHTS_FILENAME;
			File userGroupReportRightsFile = new File(userGroupReportRightsFileName);
			if (userGroupReportRightsFile.exists()) {
				List<UserGroupReportRight> allUserGroupReportRights = csvRoutines.parseAll(UserGroupReportRight.class, userGroupReportRightsFile);
				for (UserGroupReportRight userGroupReportRight : allUserGroupReportRights) {
					int parentId = userGroupReportRight.getParentId();
					Report report = reportsMap.get(parentId);
					if (report == null) {
						throw new IllegalStateException("Report not found. Parent Id = " + parentId);
					} else {
						List<UserGroupReportRight> userGroupReportRights = report.getUserGroupReportRights();
						if (userGroupReportRights == null) {
							userGroupReportRights = new ArrayList<>();
						}
						userGroupReportRights.add(userGroupReportRight);
						report.setUserGroupReportRights(userGroupReportRights);
					}
				}
			}

			String drilldownsFileName = artTempPath + ExportRecords.EMBEDDED_DRILLDOWNS_FILENAME;
			File drilldownsFile = new File(drilldownsFileName);
			String drilldownReportParamsFileName = artTempPath + ExportRecords.EMBEDDED_DRILLDOWNREPORTPARAMETERS_FILENAME;
			File drilldownReportParamsFile = new File(drilldownReportParamsFileName);
			if (drilldownsFile.exists()) {
				List<Drilldown> allDrilldowns = csvRoutines.parseAll(Drilldown.class, drilldownsFile);
				for (Drilldown drilldown : allDrilldowns) {
					int parentId = drilldown.getParentId();
					Report report = reportsMap.get(parentId);
					if (report == null) {
						throw new IllegalStateException("Report not found. Parent Id = " + parentId);
					} else {
						List<Drilldown> drilldowns = report.getDrilldowns();
						if (drilldowns == null) {
							drilldowns = new ArrayList<>();
						}
						drilldowns.add(drilldown);
						report.setDrilldowns(drilldowns);
					}
				}

				if (drilldownReportParamsFile.exists()) {
					Map<Integer, Report> drilldownReportsMap = new HashMap<>();
					for (Drilldown drilldown : allDrilldowns) {
						Report drilldownReport = drilldown.getDrilldownReport();
						drilldownReportsMap.put(drilldownReport.getReportId(), drilldownReport);
					}

					List<ReportParameter> allDrilldownReportParams = csvRoutines.parseAll(ReportParameter.class, drilldownReportParamsFile);
					for (ReportParameter drilldownReportParam : allDrilldownReportParams) {
						int parentId = drilldownReportParam.getParentId();
						Report drilldownReport = drilldownReportsMap.get(parentId);
						if (drilldownReport == null) {
							throw new IllegalStateException("Drilldown report not found. Parent Id = " + parentId);
						} else {
							List<ReportParameter> reportParams = drilldownReport.getReportParams();
							if (reportParams == null) {
								reportParams = new ArrayList<>();
							}
							reportParams.add(drilldownReportParam);
							drilldownReport.setReportParams(reportParams);
						}
					}
				}
			}

			for (Report report : reports) {
				ReportType reportType = report.getReportType();
				if (reportType == null) {
					logger.warn("reportType is null. Report={}", report);
				} else {
					String template = report.getTemplate();
					if (StringUtils.isNotBlank(template)) {
						String templateFilePath = artTempPath + template;
						File templateFile = new File(templateFilePath);
						if (templateFile.exists()) {
							String templatesPath;
							if (reportType.isUseJsTemplatesPath()) {
								templatesPath = Config.getJsTemplatesPath();
							} else if (reportType == ReportType.JPivotMondrian) {
								templatesPath = Config.getDefaultTemplatesPath();
							} else {
								templatesPath = Config.getTemplatesPath();
							}
							String destinationFilePath = templatesPath + template;
							File destinationFile = new File(destinationFilePath);
							FileUtils.copyFile(templateFile, destinationFile);
							templateFile.delete();
						}
					}

					String options = report.getOptions();
					if (StringUtils.isNotBlank(options)) {
						switch (reportType) {
							case JxlsArt:
							case JxlsTemplate:
								JxlsOptions jxlsOptions = ArtUtils.jsonToObject(options, JxlsOptions.class);
								String areaConfigFilename = jxlsOptions.getAreaConfigFile();
								if (StringUtils.isNotBlank(areaConfigFilename)) {
									String fullAreaConfigFilename = artTempPath + areaConfigFilename;
									File areaConfigFile = new File(fullAreaConfigFilename);
									if (areaConfigFile.exists()) {
										String templatesPath = Config.getTemplatesPath();
										String destinationFilePath = templatesPath + areaConfigFilename;
										File destinationFile = new File(destinationFilePath);
										FileUtils.copyFile(areaConfigFile, destinationFile);
										areaConfigFile.delete();
									}
								}
								break;
							case PivotTableJsCsvServer:
							case DygraphsCsvServer:
							case DataTablesCsvServer:
								CsvServerOptions csvServerOptions = ArtUtils.jsonToObject(options, CsvServerOptions.class);
								String dataFileName = csvServerOptions.getDataFile();
								if (StringUtils.isNotBlank(dataFileName)) {
									String fullDataFileName = artTempPath + dataFileName;
									File dataFile = new File(fullDataFileName);
									if (dataFile.exists()) {
										String jsTemplatesPath = Config.getJsTemplatesPath();
										String destinationFilePath = jsTemplatesPath + dataFileName;
										File destinationFile = new File(destinationFilePath);
										FileUtils.copyFile(dataFile, destinationFile);
										dataFile.delete();
									}
								}
								break;
							case C3:
								C3Options c3Options = ArtUtils.jsonToObject(options, C3Options.class);
								String cssFileName = c3Options.getCssFile();
								if (StringUtils.isNotBlank(cssFileName)) {
									String fullCssFileName = artTempPath + cssFileName;
									File cssFile = new File(fullCssFileName);
									if (cssFile.exists()) {
										String jsTemplatesPath = Config.getJsTemplatesPath();
										String destinationFilePath = jsTemplatesPath + cssFileName;
										File destinationFile = new File(destinationFilePath);
										FileUtils.copyFile(cssFile, destinationFile);
										cssFile.delete();
									}
								}
								break;
							case Datamaps:
							case DatamapsFile:
								DatamapsOptions datamapsOptions = ArtUtils.jsonToObject(options, DatamapsOptions.class);
								String jsTemplatesPath = Config.getJsTemplatesPath();

								String datamapsJsFileName = datamapsOptions.getDatamapsJsFile();
								if (StringUtils.isNotBlank(datamapsJsFileName)) {
									String fullDatamapsJsFileName = artTempPath + datamapsJsFileName;
									File datamapsJsFile = new File(fullDatamapsJsFileName);
									if (datamapsJsFile.exists()) {
										String destinationFilePath = jsTemplatesPath + datamapsJsFileName;
										File destinationFile = new File(destinationFilePath);
										FileUtils.copyFile(datamapsJsFile, destinationFile);
										datamapsJsFile.delete();
									}
								}

								dataFileName = datamapsOptions.getDataFile();
								if (StringUtils.isNotBlank(dataFileName)) {
									String fullDataFileName = artTempPath + dataFileName;
									File dataFile = new File(fullDataFileName);
									if (dataFile.exists()) {
										String destinationFilePath = jsTemplatesPath + dataFileName;
										File destinationFile = new File(destinationFilePath);
										FileUtils.copyFile(dataFile, destinationFile);
										dataFile.delete();
									}
								}

								String mapFileName = datamapsOptions.getMapFile();
								if (StringUtils.isNotBlank(mapFileName)) {
									String fullMapFileName = artTempPath + mapFileName;
									File mapFile = new File(fullMapFileName);
									if (mapFile.exists()) {
										String destinationFilePath = jsTemplatesPath + mapFileName;
										File destinationFile = new File(destinationFilePath);
										FileUtils.copyFile(mapFile, destinationFile);
										mapFile.delete();
									}
								}

								cssFileName = datamapsOptions.getCssFile();
								if (StringUtils.isNotBlank(cssFileName)) {
									String fullCssFileName = artTempPath + cssFileName;
									File cssFile = new File(fullCssFileName);
									if (cssFile.exists()) {
										String destinationFilePath = jsTemplatesPath + cssFileName;
										File destinationFile = new File(destinationFilePath);
										FileUtils.copyFile(cssFile, destinationFile);
										cssFile.delete();
									}
								}
								break;
							case Leaflet:
							case OpenLayers:
								WebMapOptions webMapOptions = ArtUtils.jsonToObject(options, WebMapOptions.class);
								jsTemplatesPath = Config.getJsTemplatesPath();

								cssFileName = webMapOptions.getCssFile();
								if (StringUtils.isNotBlank(cssFileName)) {
									String fullCssFileName = artTempPath + cssFileName;
									File cssFile = new File(fullCssFileName);
									if (cssFile.exists()) {
										String destinationFilePath = jsTemplatesPath + cssFileName;
										File destinationFile = new File(destinationFilePath);
										FileUtils.copyFile(cssFile, destinationFile);
										cssFile.delete();
									}
								}

								dataFileName = webMapOptions.getDataFile();
								if (StringUtils.isNotBlank(dataFileName)) {
									String fullDataFileName = artTempPath + dataFileName;
									File dataFile = new File(fullDataFileName);
									if (dataFile.exists()) {
										String destinationFilePath = jsTemplatesPath + dataFileName;
										File destinationFile = new File(destinationFilePath);
										FileUtils.copyFile(dataFile, destinationFile);
										dataFile.delete();
									}
								}

								List<String> jsFileNames = webMapOptions.getJsFiles();
								if (CollectionUtils.isNotEmpty(jsFileNames)) {
									for (String jsFileName : jsFileNames) {
										if (StringUtils.isNotBlank(jsFileName)) {
											String fullJsFileName = artTempPath + jsFileName;
											File jsFile = new File(fullJsFileName);
											if (jsFile.exists()) {
												String destinationFilePath = jsTemplatesPath + jsFileName;
												File destinationFile = new File(destinationFilePath);
												FileUtils.copyFile(jsFile, destinationFile);
												jsFile.delete();
											}
										}
									}
								}

								List<String> cssFileNames = webMapOptions.getCssFiles();
								if (CollectionUtils.isNotEmpty(cssFileNames)) {
									for (String listCssFileName : cssFileNames) {
										if (StringUtils.isNotBlank(listCssFileName)) {
											String fullListCssFileName = artTempPath + listCssFileName;
											File listCssFile = new File(fullListCssFileName);
											if (listCssFile.exists()) {
												String destinationFilePath = jsTemplatesPath + listCssFileName;
												File destinationFile = new File(destinationFilePath);
												FileUtils.copyFile(listCssFile, destinationFile);
												listCssFile.delete();
											}
										}
									}
								}
								break;
							case OrgChartDatabase:
							case OrgChartJson:
							case OrgChartList:
							case OrgChartAjax:
								OrgChartOptions orgChartOptions = ArtUtils.jsonToObject(options, OrgChartOptions.class);
								jsTemplatesPath = Config.getJsTemplatesPath();

								cssFileName = orgChartOptions.getCssFile();
								if (StringUtils.isNotBlank(cssFileName)) {
									String fullCssFileName = artTempPath + cssFileName;
									File cssFile = new File(fullCssFileName);
									if (cssFile.exists()) {
										String destinationFilePath = jsTemplatesPath + cssFileName;
										File destinationFile = new File(destinationFilePath);
										FileUtils.copyFile(cssFile, destinationFile);
										cssFile.delete();
									}
								}
								break;
							default:
								break;
						}
					}
				}
			}

			reportsFile.delete();
			reportGroupsFile.delete();
			reportParamsFile.delete();
			userRuleValuesFile.delete();
			userGroupRuleValuesFile.delete();
			reportRulesFile.delete();
			userReportRightsFile.delete();
			userGroupReportRightsFile.delete();
			drilldownsFile.delete();
			drilldownReportParamsFile.delete();
		} else {
			throw new IllegalArgumentException("Unexpected file extension: " + extension);
		}

		for (Report report : reports) {
			report.encryptAllClearTextPasswords();
		}

		ReportServiceHelper reportServiceHelper = new ReportServiceHelper();
		boolean local = true;
		reportServiceHelper.importReports(reports, sessionUser, conn, local);
		cacheHelper.clearReports();
		cacheHelper.clearReportGroups();
		cacheHelper.clearDatasources();
		cacheHelper.clearEncryptors();
		cacheHelper.clearParameters();
		cacheHelper.clearRules();
		cacheHelper.clearUsers();
		cacheHelper.clearUserGroups();
	}

}
