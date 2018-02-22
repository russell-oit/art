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
import art.enums.MigrationRecordType;
import art.holiday.Holiday;
import art.holiday.HolidayService;
import art.job.Job;
import art.job.JobService;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.report.Report;
import art.report.ReportServiceHelper;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
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
	private JobService jobService;

	@Autowired
	private CacheHelper cacheHelper;

	@GetMapping("/importRecords")
	public String showImportRecords(Model model, @RequestParam("type") String type) {
		logger.debug("Entering showImportRecords: type='{}'", type);

		ImportRecords importRecords = new ImportRecords();
		importRecords.setRecordType(MigrationRecordType.toEnum(type));

		model.addAttribute("importRecords", importRecords);

		return "importRecords";
	}

	@PostMapping("/importRecords")
	public String processImportRecords(@Valid ImportRecords importRecords,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session, @RequestParam("importFile") MultipartFile importFile) {

		logger.debug("Entering processImportRecords");

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return "importRecords";
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
						importDatasources(tempFile, sessionUser, conn, csvRoutines);
						break;
					case Destinations:
						importDestinations(tempFile, sessionUser, conn, csvRoutines);
						break;
					case Encryptors:
						importEncryptors(tempFile, sessionUser, conn, csvRoutines);
						break;
					case Holidays:
						importHolidays(tempFile, sessionUser, conn, csvRoutines);
						break;
					case ReportGroups:
						importReportGroups(tempFile, sessionUser, conn, csvRoutines);
						break;
					case SmtpServers:
						importSmtpServers(tempFile, sessionUser, conn, csvRoutines);
						break;
					case UserGroups:
						importUserGroups(tempFile, sessionUser, conn, csvRoutines);
						break;
					case Schedules:
						importSchedules(tempFile, sessionUser, conn, csvRoutines);
						break;
					case Users:
						importUsers(tempFile, sessionUser, conn, csvRoutines);
						break;
					case Rules:
						importRules(tempFile, sessionUser, conn, csvRoutines);
						break;
					case Parameters:
						importParameters(tempFile, sessionUser, conn, csvRoutines);
						break;
					case Jobs:
						importJobs(tempFile, sessionUser, conn, csvRoutines);
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
	 * @throws SQLException
	 */
	private void importDatasources(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException {

		logger.debug("Entering importDatasources: sessionUser={}", sessionUser);

		List<Datasource> datasources = csvRoutines.parseAll(Datasource.class, file);

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
	 * @throws SQLException
	 */
	private void importDestinations(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException {

		logger.debug("Entering importDestinations: sessionUser={}", sessionUser);

		List<Destination> destinations = csvRoutines.parseAll(Destination.class, file);

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
	 * @throws SQLException
	 */
	private void importEncryptors(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException {

		logger.debug("Entering importEncryptors: sessionUser={}", sessionUser);

		List<Encryptor> encryptors = csvRoutines.parseAll(Encryptor.class, file);

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
	 * @throws SQLException
	 */
	private void importHolidays(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException {

		logger.debug("Entering importHolidays: sessionUser={}", sessionUser);

		List<Holiday> holidays = csvRoutines.parseAll(Holiday.class, file);
		holidayService.importHolidays(holidays, sessionUser, conn);
	}

	/**
	 * Imports report group records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @throws SQLException
	 */
	private void importReportGroups(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException {

		logger.debug("Entering importReportGroups: sessionUser={}", sessionUser);

		List<ReportGroup> reportGroups = csvRoutines.parseAll(ReportGroup.class, file);
		reportGroupService.importReportGroups(reportGroups, sessionUser, conn);
	}

	/**
	 * Imports smtp server records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @throws SQLException
	 */
	private void importSmtpServers(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException {

		logger.debug("Entering importSmtpServers: sessionUser={}", sessionUser);

		List<SmtpServer> smtpServers = csvRoutines.parseAll(SmtpServer.class, file);

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
	 * @throws SQLException
	 */
	private void importUserGroups(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException {

		logger.debug("Entering importUserGroups: sessionUser={}", sessionUser);

		List<UserGroup> userGroups = csvRoutines.parseAll(UserGroup.class, file);
		userGroupService.importUserGroups(userGroups, sessionUser, conn);
	}

	/**
	 * Imports schedule records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @throws SQLException
	 */
	private void importSchedules(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException, IOException {

		logger.debug("Entering importSchedules: sessionUser={}", sessionUser);

		List<Schedule> schedules;
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
	 * @throws SQLException
	 */
	private void importRules(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException {

		logger.debug("Entering importRules: sessionUser={}", sessionUser);

		List<Rule> rules = csvRoutines.parseAll(Rule.class, file);
		ruleService.importRules(rules, sessionUser, conn);
	}

	/**
	 * Imports parameter records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @throws SQLException
	 */
	private void importParameters(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException {

		logger.debug("Entering importParameters: sessionUser={}", sessionUser);

		List<Parameter> parameters = csvRoutines.parseAll(Parameter.class, file);
		parameterService.importParameters(parameters, sessionUser, conn);
	}

	/**
	 * Imports job records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @throws SQLException
	 */
	private void importJobs(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines) throws SQLException {

		logger.debug("Entering importJobs: sessionUser={}", sessionUser);

		List<Job> jobs = csvRoutines.parseAll(Job.class, file);
		jobService.importJobs(jobs, sessionUser, conn);
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
			if (report.isClearTextPasswords()) {
				report.encryptPasswords();
			}
		}

		ReportServiceHelper reportServiceHelper = new ReportServiceHelper();
		reportServiceHelper.importReports(reports, sessionUser, conn);
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
