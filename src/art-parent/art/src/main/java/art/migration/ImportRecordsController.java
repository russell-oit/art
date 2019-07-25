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
import art.enums.EncryptorType;
import art.enums.MigrationFileFormat;
import art.enums.MigrationRecordType;
import art.enums.ReportType;
import art.holiday.Holiday;
import art.holiday.HolidayService;
import art.parameter.Parameter;
import art.parameter.ParameterService;
import art.permission.Permission;
import art.report.Report;
import art.report.ReportServiceHelper;
import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.reportoptions.C3Options;
import art.reportoptions.CsvServerOptions;
import art.reportoptions.DatamapsOptions;
import art.reportoptions.JasperReportsOptions;
import art.reportoptions.JxlsOptions;
import art.reportoptions.OrgChartOptions;
import art.reportoptions.TemplateResultOptions;
import art.reportoptions.WebMapOptions;
import art.reportparameter.ReportParameter;
import art.reportrule.ReportRule;
import art.role.Role;
import art.role.RoleService;
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
import art.user.UserCsvExportMixIn;
import art.user.UserService;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupCsvExportMixIn;
import art.usergroup.UserGroupService;
import art.utils.ArtUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
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
import org.zeroturnaround.zip.ZipUtil;

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

	@Autowired
	private RoleService roleService;

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

			MigrationFileFormat fileFormat = importRecords.getFileFormat();
			MigrationRecordType recordType = importRecords.getRecordType();

			try {
				switch (recordType) {
					case Settings:
						importSettings(tempFile, sessionUser, conn, fileFormat, session);
						break;
					case Datasources:
						importDatasources(tempFile, sessionUser, conn, fileFormat);
						break;
					case Destinations:
						importDestinations(tempFile, sessionUser, conn, fileFormat);
						break;
					case Encryptors:
						importEncryptors(tempFile, sessionUser, conn, fileFormat);
						break;
					case Holidays:
						importHolidays(tempFile, sessionUser, conn, fileFormat);
						break;
					case ReportGroups:
						importReportGroups(tempFile, sessionUser, conn, fileFormat);
						break;
					case SmtpServers:
						importSmtpServers(tempFile, sessionUser, conn, fileFormat);
						break;
					case UserGroups:
						importUserGroups(tempFile, sessionUser, conn, fileFormat);
						break;
					case Schedules:
						importSchedules(tempFile, sessionUser, conn, fileFormat);
						break;
					case Users:
						importUsers(tempFile, sessionUser, conn, fileFormat);
						break;
					case Rules:
						importRules(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case Parameters:
						importParameters(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case Reports:
						importReports(tempFile, sessionUser, conn, csvRoutines, importRecords);
						break;
					case Roles:
						importRoles(tempFile, sessionUser, conn, csvRoutines, importRecords);
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
		} catch (Exception ex) {
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
	 * @param fileFormat the format of the file
	 * @param session the http session
	 * @throws Exception
	 */
	private void importSettings(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat, HttpSession session) throws Exception {

		logger.debug("Entering importSettings: sessionUser={}, fileFormat={}",
				sessionUser, fileFormat);

		Settings settings;
		switch (fileFormat) {
			case json:
				settings = importFromJson(file, Settings.class);
				break;
			case csv:
				settings = importValueFromCsv(file, Settings.class);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		if (settings.isClearTextPasswords()) {
			settings.encryptPasswords();
		}

		settingsService.importSettings(settings, sessionUser, conn);

		SettingsHelper settingsHelper = new SettingsHelper();
		settingsHelper.refreshSettings(session, servletContext);
	}

	/**
	 * Imports datasource records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param fileFormat the format of the file
	 * @throws Exception
	 */
	private void importDatasources(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat) throws Exception {

		logger.debug("Entering importDatasources: sessionUser={}, fileFormat={}",
				sessionUser, fileFormat);

		List<Datasource> datasources;
		switch (fileFormat) {
			case json:
				datasources = importFromJson(file, new TypeReference<List<Datasource>>() {
				});
				break;
			case csv:
				datasources = importValuesFromCsv(file, Datasource.class);
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
				DbConnections.createDatasourceConnectionPool(datasource, artDbConfig.getMaxPoolConnections(), artDbConfig.getConnectionPoolLibrary());
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
	 * @param fileFormat the format of the file
	 * @throws Exception
	 */
	private void importDestinations(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat) throws Exception {

		logger.debug("Entering importDestinations: sessionUser={}, fileFormat={}",
				sessionUser, fileFormat);

		List<Destination> destinations;
		switch (fileFormat) {
			case json:
				destinations = importFromJson(file, new TypeReference<List<Destination>>() {
				});
				break;
			case csv:
				destinations = importValuesFromCsv(file, Destination.class);
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
	 * @param fileFormat the format of the file
	 * @throws Exception
	 */
	private void importEncryptors(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat) throws Exception {

		logger.debug("Entering importEncryptors: sessionUser={}, fileFormat={}",
				sessionUser, fileFormat);

		List<Encryptor> encryptors;
		String extension = FilenameUtils.getExtension(file.getName());
		String artTempPath = Config.getArtTempPath();

		switch (fileFormat) {
			case json:
				if (StringUtils.equalsIgnoreCase(extension, "json")) {
					encryptors = importFromJson(file, new TypeReference<List<Encryptor>>() {
					});
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					String encryptorsFilePath = artTempPath + ExportRecords.EMBEDDED_JSON_ENCRYPTORS_FILENAME;
					File encryptorsFile = new File(encryptorsFilePath);
					boolean unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_JSON_ENCRYPTORS_FILENAME, encryptorsFile);
					if (unpacked) {
						encryptors = importFromJson(encryptorsFile, new TypeReference<List<Encryptor>>() {
						});
						encryptorsFile.delete();
						copyEncryptorFiles(encryptors, artTempPath, file);
					} else {
						throw new RuntimeException("File not found: " + encryptorsFilePath);
					}
				} else {
					throw new IllegalArgumentException("Unexpected file extension: " + extension);
				}
				break;
			case csv:
				if (StringUtils.equalsIgnoreCase(extension, "csv")) {
					encryptors = importValuesFromCsv(file, Encryptor.class);
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					String encryptorsFilePath = artTempPath + ExportRecords.EMBEDDED_CSV_ENCRYPTORS_FILENAME;
					File encryptorsFile = new File(encryptorsFilePath);
					boolean unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_CSV_ENCRYPTORS_FILENAME, encryptorsFile);
					if (unpacked) {
						encryptors = importValuesFromCsv(encryptorsFile, Encryptor.class);
						encryptorsFile.delete();
						copyEncryptorFiles(encryptors, artTempPath, file);
					} else {
						throw new RuntimeException("File not found: " + encryptorsFilePath);
					}
				} else {
					throw new IllegalArgumentException("Unexpected file extension: " + extension);
				}
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
	 * @param fileFormat the format of the file
	 * @throws SQLException
	 */
	private void importHolidays(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat) throws SQLException, IOException {

		logger.debug("Entering importHolidays: sessionUser={}, fileFormat={}",
				sessionUser, fileFormat);

		List<Holiday> holidays;
		switch (fileFormat) {
			case json:
				holidays = importFromJson(file, new TypeReference<List<Holiday>>() {
				});
				break;
			case csv:
				holidays = importValuesFromCsv(file, Holiday.class);
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
	 * @param fileFormat the format of the file
	 * @throws SQLException
	 */
	private void importReportGroups(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat) throws SQLException, IOException {

		logger.debug("Entering importReportGroups: sessionUser={}, fileFormat={}",
				sessionUser, fileFormat);

		List<ReportGroup> reportGroups;
		switch (fileFormat) {
			case json:
				reportGroups = importFromJson(file, new TypeReference<List<ReportGroup>>() {
				});
				break;
			case csv:
				reportGroups = importValuesFromCsv(file, ReportGroup.class);
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
	 * @param fileFormat the format of the file
	 * @throws Exception
	 */
	private void importSmtpServers(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat) throws Exception {

		logger.debug("Entering importSmtpServers: sessionUser={}, fileFormat={}",
				sessionUser, fileFormat);

		List<SmtpServer> smtpServers;
		switch (fileFormat) {
			case json:
				smtpServers = importFromJson(file, new TypeReference<List<SmtpServer>>() {
				});
				break;
			case csv:
				smtpServers = importValuesFromCsv(file, SmtpServer.class);
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		for (SmtpServer smtpServer : smtpServers) {
			if (smtpServer.isClearTextPassword()) {
				smtpServer.encryptPasswords();
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
	 * @param fileFormat the format of the file
	 * @throws SQLException
	 */
	private void importUserGroups(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat) throws SQLException, IOException {

		logger.debug("Entering importUserGroups: sessionUser={}, fileFormat={}",
				sessionUser, fileFormat);

		List<UserGroup> userGroups;
		switch (fileFormat) {
			case json:
				userGroups = importFromJson(file, new TypeReference<List<UserGroup>>() {
				});
				break;
			case csv:
				String extension = FilenameUtils.getExtension(file.getName());
				if (StringUtils.equalsIgnoreCase(extension, "csv")) {
					userGroups = importValuesFromCsv(file, UserGroup.class, UserGroupCsvExportMixIn.class);
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					String artTempPath = Config.getArtTempPath();
					boolean unpacked;
					String userGroupsFilePath = artTempPath + ExportRecords.EMBEDDED_USERGROUPS_FILENAME;
					File userGroupsFile = new File(userGroupsFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_USERGROUPS_FILENAME, userGroupsFile);
					if (unpacked) {
						userGroups = importValuesFromCsv(userGroupsFile, UserGroup.class, UserGroupCsvExportMixIn.class);
						userGroupsFile.delete();
					} else {
						throw new RuntimeException("File not found: " + userGroupsFilePath);
					}

					Map<Integer, UserGroup> userGroupsMap = new HashMap<>();
					for (UserGroup userGroup : userGroups) {
						userGroupsMap.put(userGroup.getUserGroupId(), userGroup);
					}

					String rolesFilePath = artTempPath + ExportRecords.EMBEDDED_ROLES_FILENAME;
					File rolesFile = new File(rolesFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_ROLES_FILENAME, rolesFile);
					if (unpacked) {
						List<Role> allRoles = importValuesFromCsv(rolesFile, Role.class);
						rolesFile.delete();
						for (Role role : allRoles) {
							int parentId = role.getParentId();
							UserGroup userGroup = userGroupsMap.get(parentId);
							if (userGroup == null) {
								throw new RuntimeException("User Group not found. Parent Id = " + parentId);
							} else {
								List<Role> roles = userGroup.getRoles();
								if (roles == null) {
									roles = new ArrayList<>();
								}
								roles.add(role);
								userGroup.setRoles(roles);
							}
						}
					}

					String permissionsFilePath = artTempPath + ExportRecords.EMBEDDED_PERMISSIONS_FILENAME;
					File permissionsFile = new File(permissionsFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_PERMISSIONS_FILENAME, permissionsFile);
					if (unpacked) {
						List<Permission> allPermissions = importValuesFromCsv(permissionsFile, Permission.class);
						permissionsFile.delete();
						for (Permission permission : allPermissions) {
							int parentId = permission.getParentId();
							UserGroup userGroup = userGroupsMap.get(parentId);
							if (userGroup == null) {
								throw new RuntimeException("User Group not found. Parent Id = " + parentId);
							} else {
								List<Permission> permissions = userGroup.getPermissions();
								if (permissions == null) {
									permissions = new ArrayList<>();
								}
								permissions.add(permission);
								userGroup.setPermissions(permissions);
							}
						}
					}
				} else {
					throw new IllegalArgumentException("Unexpected file extension: " + extension);
				}
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
	 * @param fileFormat the format of the file
	 * @throws SQLException
	 */
	private void importSchedules(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat) throws SQLException, IOException {

		logger.debug("Entering importSchedules: sessionUser={}, fileFormat",
				sessionUser, fileFormat);

		List<Schedule> schedules;
		switch (fileFormat) {
			case json:
				schedules = importFromJson(file, new TypeReference<List<Schedule>>() {
				});
				break;
			case csv:
				String extension = FilenameUtils.getExtension(file.getName());
				if (StringUtils.equalsIgnoreCase(extension, "csv")) {
					schedules = importValuesFromCsv(file, Schedule.class);
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					String artTempPath = Config.getArtTempPath();
					boolean unpacked;
					String schedulesFilePath = artTempPath + ExportRecords.EMBEDDED_SCHEDULES_FILENAME;
					File schedulesFile = new File(schedulesFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_SCHEDULES_FILENAME, schedulesFile);
					if (unpacked) {
						schedules = importValuesFromCsv(schedulesFile, Schedule.class);
						schedulesFile.delete();
					} else {
						throw new RuntimeException("File not found: " + schedulesFilePath);
					}

					String holidaysFilePath = artTempPath + ExportRecords.EMBEDDED_HOLIDAYS_FILENAME;
					File holidaysFile = new File(holidaysFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_HOLIDAYS_FILENAME, holidaysFile);
					if (unpacked) {
						List<Holiday> holidays = importValuesFromCsv(holidaysFile, Holiday.class);
						holidaysFile.delete();
						Map<Integer, Schedule> schedulesMap = new HashMap<>();
						for (Schedule schedule : schedules) {
							schedulesMap.put(schedule.getScheduleId(), schedule);
						}
						for (Holiday holiday : holidays) {
							int parentId = holiday.getParentId();
							Schedule schedule = schedulesMap.get(parentId);
							if (schedule == null) {
								throw new RuntimeException("Schedule not found. Parent Id = " + parentId);
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
	 * @param fileFormat the format of the file
	 * @throws SQLException
	 */
	private void importUsers(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat) throws SQLException, IOException {

		logger.debug("Entering importUsers: sessionUser={}, fileFormat={}",
				sessionUser, fileFormat);

		List<User> users;
		switch (fileFormat) {
			case json:
				users = importFromJson(file, new TypeReference<List<User>>() {
				});
				break;
			case csv:
				String extension = FilenameUtils.getExtension(file.getName());
				if (StringUtils.equalsIgnoreCase(extension, "csv")) {
					users = importValuesFromCsv(file, User.class, UserCsvExportMixIn.class);
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					String artTempPath = Config.getArtTempPath();
					boolean unpacked;
					String usersFilePath = artTempPath + ExportRecords.EMBEDDED_USERS_FILENAME;
					File usersFile = new File(usersFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_USERS_FILENAME, usersFile);
					if (unpacked) {
						users = importValuesFromCsv(usersFile, User.class, UserCsvExportMixIn.class);
						usersFile.delete();
					} else {
						throw new RuntimeException("File not found: " + usersFilePath);
					}

					Map<Integer, User> usersMap = new HashMap<>();
					for (User user : users) {
						usersMap.put(user.getUserId(), user);
					}

					String userGroupsFilePath = artTempPath + ExportRecords.EMBEDDED_USERGROUPS_FILENAME;
					File userGroupsFile = new File(userGroupsFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_USERGROUPS_FILENAME, userGroupsFile);
					if (unpacked) {
						List<UserGroup> allUserGroups = importValuesFromCsv(userGroupsFile, UserGroup.class, UserGroupCsvExportMixIn.class);
						userGroupsFile.delete();
						for (UserGroup userGroup : allUserGroups) {
							int parentId = userGroup.getParentId();
							User user = usersMap.get(parentId);
							if (user == null) {
								throw new RuntimeException("User not found. Parent Id = " + parentId);
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

					String rolesFilePath = artTempPath + ExportRecords.EMBEDDED_ROLES_FILENAME;
					File rolesFile = new File(rolesFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_ROLES_FILENAME, rolesFile);
					if (unpacked) {
						List<Role> allRoles = importValuesFromCsv(rolesFile, Role.class);
						rolesFile.delete();
						for (Role role : allRoles) {
							int parentId = role.getParentId();
							User user = usersMap.get(parentId);
							if (user == null) {
								throw new RuntimeException("User not found. Parent Id = " + parentId);
							} else {
								List<Role> roles = user.getRoles();
								if (roles == null) {
									roles = new ArrayList<>();
								}
								roles.add(role);
								user.setRoles(roles);
							}
						}
					}

					String permissionsFileName = artTempPath + ExportRecords.EMBEDDED_PERMISSIONS_FILENAME;
					File permissionsFile = new File(permissionsFileName);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_PERMISSIONS_FILENAME, permissionsFile);
					if (unpacked) {
						List<Permission> allPermissions = importValuesFromCsv(permissionsFile, Permission.class);
						permissionsFile.delete();
						for (Permission permission : allPermissions) {
							int parentId = permission.getParentId();
							User user = usersMap.get(parentId);
							if (user == null) {
								throw new RuntimeException("User not found. Parent Id = " + parentId);
							} else {
								List<Permission> permissions = user.getPermissions();
								if (permissions == null) {
									permissions = new ArrayList<>();
								}
								permissions.add(permission);
								user.setPermissions(permissions);
							}
						}
					}
				} else {
					throw new IllegalArgumentException("Unexpected file extension: " + extension);
				}
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
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
	 * @param fileFormat the format of the file
	 * @throws SQLException
	 */
	private void importRules(File file, User sessionUser, Connection conn,
			MigrationFileFormat fileFormat) throws SQLException, IOException {

		logger.debug("Entering importRules: sessionUser={}, fileFormat={}",
				sessionUser, fileFormat);

		List<Rule> rules;
		switch (fileFormat) {
			case json:
				rules = importFromJson(file, new TypeReference<List<Rule>>() {
				});
				break;
			case csv:
				rules = importValuesFromCsv(file, Rule.class);
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
	 * @throws Exception
	 */
	private void importParameters(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws Exception {

		logger.debug("Entering importParameters: sessionUser={}", sessionUser);

		List<Parameter> parameters;
		String extension = FilenameUtils.getExtension(file.getName());
		String artTempPath = Config.getArtTempPath();

		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				if (StringUtils.equalsIgnoreCase(extension, "json")) {
					ObjectMapper mapper = ArtUtils.getPropertyOnlyObjectMapper();
					parameters = mapper.readValue(file, new TypeReference<List<Parameter>>() {
					});
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					String parametersFilePath = artTempPath + ExportRecords.EMBEDDED_JSON_PARAMETERS_FILENAME;
					File parametersFile = new File(parametersFilePath);
					boolean unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_JSON_PARAMETERS_FILENAME, parametersFile);
					if (unpacked) {
						ObjectMapper mapper = ArtUtils.getPropertyOnlyObjectMapper();
						parameters = mapper.readValue(parametersFile, new TypeReference<List<Parameter>>() {
						});
						parametersFile.delete();
						copyParameterFiles(parameters, artTempPath, file);
					} else {
						throw new RuntimeException("File not found: " + parametersFilePath);
					}
				} else {
					throw new IllegalArgumentException("Unexpected file extension: " + extension);
				}
				break;
			case csv:
				if (StringUtils.equalsIgnoreCase(extension, "csv")) {
					parameters = csvRoutines.parseAll(Parameter.class, file);
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					String parametersFilePath = artTempPath + ExportRecords.EMBEDDED_CSV_PARAMETERS_FILENAME;
					File parametersFile = new File(parametersFilePath);
					boolean unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_CSV_PARAMETERS_FILENAME, parametersFile);
					if (unpacked) {
						parameters = csvRoutines.parseAll(Parameter.class, parametersFile);
						parametersFile.delete();
						copyParameterFiles(parameters, artTempPath, file);
					} else {
						throw new RuntimeException("File not found: " + parametersFilePath);
					}
				} else {
					throw new IllegalArgumentException("Unexpected file extension: " + extension);
				}
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
	 * @param importRecords the import records object
	 * @throws Exception
	 */
	private void importReports(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws Exception {

		logger.debug("Entering importReports: sessionUser={}", sessionUser);

		List<Report> reports;
		String extension = FilenameUtils.getExtension(file.getName());
		String artTempPath = Config.getArtTempPath();

		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				if (StringUtils.equalsIgnoreCase(extension, "json")) {
					ObjectMapper mapper = ArtUtils.getPropertyOnlyObjectMapper();
					reports = mapper.readValue(file, new TypeReference<List<Report>>() {
					});
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					String reportsFilePath = artTempPath + ExportRecords.EMBEDDED_JSON_REPORTS_FILENAME;
					File reportsFile = new File(reportsFilePath);
					boolean unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_JSON_REPORTS_FILENAME, reportsFile);
					if (unpacked) {
						ObjectMapper mapper = ArtUtils.getPropertyOnlyObjectMapper();
						reports = mapper.readValue(reportsFile, new TypeReference<List<Report>>() {
						});
						reportsFile.delete();
						copyReportFiles(reports, artTempPath, file);
					} else {
						throw new RuntimeException("File not found: " + reportsFilePath);
					}
				} else {
					throw new IllegalArgumentException("Unexpected file extension: " + extension);
				}
				break;
			case csv:
				if (StringUtils.equalsIgnoreCase(extension, "csv")) {
					reports = csvRoutines.parseAll(Report.class, file);
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					boolean unpacked;
					String reportsFilePath = artTempPath + ExportRecords.EMBEDDED_CSV_REPORTS_FILENAME;
					File reportsFile = new File(reportsFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_CSV_REPORTS_FILENAME, reportsFile);
					if (unpacked) {
						reports = csvRoutines.parseAll(Report.class, reportsFile);
						reportsFile.delete();
						copyReportFiles(reports, artTempPath, file);
					} else {
						throw new RuntimeException("File not found: " + reportsFilePath);
					}

					Map<Integer, Report> reportsMap = new HashMap<>();
					for (Report report : reports) {
						reportsMap.put(report.getReportId(), report);
					}

					String reportGroupsFilePath = artTempPath + ExportRecords.EMBEDDED_REPORTGROUPS_FILENAME;
					File reportGroupsFile = new File(reportGroupsFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_REPORTGROUPS_FILENAME, reportGroupsFile);
					if (unpacked) {
						List<ReportGroup> allReportGroups = csvRoutines.parseAll(ReportGroup.class, reportGroupsFile);
						reportGroupsFile.delete();
						for (ReportGroup reportGroup : allReportGroups) {
							int parentId = reportGroup.getParentId();
							Report report = reportsMap.get(parentId);
							if (report == null) {
								throw new RuntimeException("Report not found. Parent Id = " + parentId);
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

					String reportParamsFilePath = artTempPath + ExportRecords.EMBEDDED_REPORTPARAMETERS_FILENAME;
					File reportParamsFile = new File(reportParamsFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_REPORTPARAMETERS_FILENAME, reportParamsFile);
					if (unpacked) {
						List<ReportParameter> allReportParams = csvRoutines.parseAll(ReportParameter.class, reportParamsFile);
						reportParamsFile.delete();
						for (ReportParameter reportParam : allReportParams) {
							int parentId = reportParam.getParentId();
							Report report = reportsMap.get(parentId);
							if (report == null) {
								throw new RuntimeException("Report not found. Parent Id = " + parentId);
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

					String userRuleValuesFilePath = artTempPath + ExportRecords.EMBEDDED_USERRULEVALUES_FILENAME;
					File userRuleValuesFile = new File(userRuleValuesFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_USERRULEVALUES_FILENAME, userRuleValuesFile);
					if (unpacked) {
						List<UserRuleValue> allUserRuleValues = csvRoutines.parseAll(UserRuleValue.class, userRuleValuesFile);
						userRuleValuesFile.delete();
						for (UserRuleValue userRuleValue : allUserRuleValues) {
							int parentId = userRuleValue.getParentId();
							Report report = reportsMap.get(parentId);
							if (report == null) {
								throw new RuntimeException("Report not found. Parent Id = " + parentId);
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

					String userGroupRuleValuesFilePath = artTempPath + ExportRecords.EMBEDDED_USERGROUPRULEVALUES_FILENAME;
					File userGroupRuleValuesFile = new File(userGroupRuleValuesFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_USERGROUPRULEVALUES_FILENAME, userGroupRuleValuesFile);
					if (unpacked) {
						List<UserGroupRuleValue> allUserGroupRuleValues = csvRoutines.parseAll(UserGroupRuleValue.class, userGroupRuleValuesFile);
						userGroupRuleValuesFile.delete();
						for (UserGroupRuleValue userGroupRuleValue : allUserGroupRuleValues) {
							int parentId = userGroupRuleValue.getParentId();
							Report report = reportsMap.get(parentId);
							if (report == null) {
								throw new RuntimeException("Report not found. Parent Id = " + parentId);
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

					String reportRulesFilePath = artTempPath + ExportRecords.EMBEDDED_REPORTRULES_FILENAME;
					File reportRulesFile = new File(reportRulesFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_REPORTRULES_FILENAME, reportRulesFile);
					if (unpacked) {
						List<ReportRule> allReportRules = csvRoutines.parseAll(ReportRule.class, reportRulesFile);
						reportRulesFile.delete();
						for (ReportRule reportRule : allReportRules) {
							int parentId = reportRule.getParentId();
							Report report = reportsMap.get(parentId);
							if (report == null) {
								throw new RuntimeException("Report not found. Parent Id = " + parentId);
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

					String userReportRightsFilePath = artTempPath + ExportRecords.EMBEDDED_USERREPORTRIGHTS_FILENAME;
					File userReportRightsFile = new File(userReportRightsFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_USERREPORTRIGHTS_FILENAME, userReportRightsFile);
					if (unpacked) {
						List<UserReportRight> allUserReportRights = csvRoutines.parseAll(UserReportRight.class, userReportRightsFile);
						userReportRightsFile.delete();
						for (UserReportRight userReportRight : allUserReportRights) {
							int parentId = userReportRight.getParentId();
							Report report = reportsMap.get(parentId);
							if (report == null) {
								throw new RuntimeException("Report not found. Parent Id = " + parentId);
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

					String userGroupReportRightsFilePath = artTempPath + ExportRecords.EMBEDDED_USERGROUPREPORTRIGHTS_FILENAME;
					File userGroupReportRightsFile = new File(userGroupReportRightsFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_USERGROUPREPORTRIGHTS_FILENAME, userGroupReportRightsFile);
					if (unpacked) {
						List<UserGroupReportRight> allUserGroupReportRights = csvRoutines.parseAll(UserGroupReportRight.class, userGroupReportRightsFile);
						userGroupReportRightsFile.delete();
						for (UserGroupReportRight userGroupReportRight : allUserGroupReportRights) {
							int parentId = userGroupReportRight.getParentId();
							Report report = reportsMap.get(parentId);
							if (report == null) {
								throw new RuntimeException("Report not found. Parent Id = " + parentId);
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

					String drilldownsFilePath = artTempPath + ExportRecords.EMBEDDED_DRILLDOWNS_FILENAME;
					File drilldownsFile = new File(drilldownsFilePath);
					ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_DRILLDOWNS_FILENAME, drilldownsFile);

					String drilldownReportParamsFilePath = artTempPath + ExportRecords.EMBEDDED_DRILLDOWNREPORTPARAMETERS_FILENAME;
					File drilldownReportParamsFile = new File(drilldownReportParamsFilePath);
					ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_DRILLDOWNREPORTPARAMETERS_FILENAME, drilldownReportParamsFile);

					if (drilldownsFile.exists()) {
						List<Drilldown> allDrilldowns = csvRoutines.parseAll(Drilldown.class, drilldownsFile);
						drilldownsFile.delete();
						for (Drilldown drilldown : allDrilldowns) {
							int parentId = drilldown.getParentId();
							Report report = reportsMap.get(parentId);
							if (report == null) {
								throw new RuntimeException("Report not found. Parent Id = " + parentId);
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
							drilldownReportParamsFile.delete();
							for (ReportParameter drilldownReportParam : allDrilldownReportParams) {
								int parentId = drilldownReportParam.getParentId();
								Report drilldownReport = drilldownReportsMap.get(parentId);
								if (drilldownReport == null) {
									throw new RuntimeException("Drilldown report not found. Parent Id = " + parentId);
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
				} else {
					throw new IllegalArgumentException("Unexpected file extension: " + extension);
				}
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
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

	/**
	 * Copies template files included in reports export zip file to the
	 * appropriate template locations
	 *
	 * @param reports the reports being imported
	 * @param sourcePath the path where the files will be obtained from
	 * @param zipFile the zip file that contains template files to be unzipped
	 * @throws IOException
	 */
	private void copyReportFiles(List<Report> reports, String sourcePath,
			File zipFile) throws IOException {

		String jsTemplatesPath = Config.getJsTemplatesPath();
		String templatesPath = Config.getTemplatesPath();

		for (Report report : reports) {
			ReportType reportType = report.getReportType();
			if (reportType == null) {
				logger.warn("reportType is null. Report={}", report);
			} else {
				String template = report.getTemplate();
				if (StringUtils.isNotBlank(template)) {
					String mainTemplatePath;
					if (reportType.isUseJsTemplatesPath()) {
						mainTemplatePath = Config.getJsTemplatesPath();
					} else if (reportType == ReportType.JPivotMondrian) {
						mainTemplatePath = Config.getDefaultTemplatesPath();
					} else {
						mainTemplatePath = Config.getTemplatesPath();
					}
					copyFile(template, sourcePath, mainTemplatePath, zipFile, report);
				}

				String options = report.getOptions();
				if (StringUtils.isNotBlank(options)) {
					switch (reportType) {
						case JxlsArt:
						case JxlsTemplate:
							JxlsOptions jxlsOptions = ArtUtils.jsonToObject(options, JxlsOptions.class);
							String areaConfigFilename = jxlsOptions.getAreaConfigFile();
							copyFile(areaConfigFilename, sourcePath, templatesPath, zipFile, report);
							break;
						case PivotTableJsCsvServer:
						case DygraphsCsvServer:
						case DataTablesCsvServer:
							CsvServerOptions csvServerOptions = ArtUtils.jsonToObject(options, CsvServerOptions.class);
							String dataFileName = csvServerOptions.getDataFile();
							copyFile(dataFileName, sourcePath, jsTemplatesPath, zipFile, report);
							break;
						case C3:
							C3Options c3Options = ArtUtils.jsonToObject(options, C3Options.class);
							String cssFileName = c3Options.getCssFile();
							copyFile(cssFileName, sourcePath, jsTemplatesPath, zipFile, report);
							break;
						case Datamaps:
						case DatamapsFile:
							DatamapsOptions datamapsOptions = ArtUtils.jsonToObject(options, DatamapsOptions.class);

							String datamapsJsFileName = datamapsOptions.getDatamapsJsFile();
							copyFile(datamapsJsFileName, sourcePath, jsTemplatesPath, zipFile, report);

							dataFileName = datamapsOptions.getDataFile();
							copyFile(dataFileName, sourcePath, jsTemplatesPath, zipFile, report);

							String mapFileName = datamapsOptions.getMapFile();
							copyFile(mapFileName, sourcePath, jsTemplatesPath, zipFile, report);

							cssFileName = datamapsOptions.getCssFile();
							copyFile(cssFileName, sourcePath, jsTemplatesPath, zipFile, report);
							break;
						case Leaflet:
						case OpenLayers:
							WebMapOptions webMapOptions = ArtUtils.jsonToObject(options, WebMapOptions.class);

							cssFileName = webMapOptions.getCssFile();
							copyFile(cssFileName, sourcePath, jsTemplatesPath, zipFile, report);

							dataFileName = webMapOptions.getDataFile();
							copyFile(dataFileName, sourcePath, jsTemplatesPath, zipFile, report);

							List<String> jsFileNames = webMapOptions.getJsFiles();
							copyFiles(jsFileNames, sourcePath, jsTemplatesPath, zipFile, report);

							List<String> cssFileNames = webMapOptions.getCssFiles();
							copyFiles(cssFileNames, sourcePath, jsTemplatesPath, zipFile, report);
							break;
						case OrgChartDatabase:
						case OrgChartJson:
						case OrgChartList:
						case OrgChartAjax:
							OrgChartOptions orgChartOptions = ArtUtils.jsonToObject(options, OrgChartOptions.class);
							cssFileName = orgChartOptions.getCssFile();
							copyFile(cssFileName, sourcePath, jsTemplatesPath, zipFile, report);
							break;
						case FreeMarker:
						case Thymeleaf:
						case Velocity:
							TemplateResultOptions templateResultOptions = ArtUtils.jsonToObject(options, TemplateResultOptions.class);
							List<String> fileNames = templateResultOptions.getFiles();
							copyFiles(fileNames, sourcePath, jsTemplatesPath, zipFile, report);
							break;
						case JasperReportsArt:
						case JasperReportsTemplate:
							JasperReportsOptions jasperReportsOptions = ArtUtils.jsonToObject(options, JasperReportsOptions.class);

							List<String> subreportFileNames = jasperReportsOptions.getSubreports();
							copyFiles(subreportFileNames, sourcePath, templatesPath, zipFile, report);

							fileNames = jasperReportsOptions.getFiles();
							copyFiles(fileNames, sourcePath, templatesPath, zipFile, report);
							break;
						default:
							break;
					}
				}
			}
		}
	}

	/**
	 * Copies template files included in parameters export zip file to the
	 * appropriate template locations
	 *
	 * @param parameters the parameters being imported
	 * @param sourcePath the path where the files will be obtained from
	 * @param zipFile the zip file that contains template files to be unzipped
	 * @throws IOException
	 */
	private void copyParameterFiles(List<Parameter> parameters, String sourcePath,
			File zipFile) throws IOException {

		String jsTemplatesPath = Config.getJsTemplatesPath();
		for (Parameter parameter : parameters) {
			String template = parameter.getTemplate();
			copyFile(template, sourcePath, jsTemplatesPath, zipFile, parameter);
		}
	}

	/**
	 * Copies files included in encryptors export zip file to the appropriate
	 * locations
	 *
	 * @param encryptors the encryptors being imported
	 * @param sourcePath the path where the files will be obtained from
	 * @param zipFile the zip file that contains files to be unzipped
	 * @throws IOException
	 */
	private void copyEncryptorFiles(List<Encryptor> encryptors, String sourcePath,
			File zipFile) throws IOException {

		String templatesPath = Config.getTemplatesPath();
		for (Encryptor encryptor : encryptors) {
			EncryptorType encryptorType = encryptor.getEncryptorType();
			if (encryptorType == null) {
				logger.warn("encryptorType is null. Encryptor={}", encryptor);
			} else {
				switch (encryptorType) {
					case OpenPGP:
						String publicKeyFileName = encryptor.getOpenPgpPublicKeyFile();
						copyFile(publicKeyFileName, sourcePath, templatesPath, zipFile, encryptor);
						break;
					default:
						break;
				}
			}
		}
	}

	/**
	 * Copies files
	 *
	 * @param fileNames the file names
	 * @param sourcePath the path where the files are located
	 * @param templatesPath the path where the files should be copied to
	 * @param zipFile the zip file that contains the files
	 * @param report the report that contains the files
	 * @throws IOException
	 */
	private void copyFiles(List<String> fileNames, String sourcePath,
			String templatesPath, File zipFile, Report report) throws IOException {

		if (CollectionUtils.isNotEmpty(fileNames)) {
			for (String fileName : fileNames) {
				copyFile(fileName, sourcePath, templatesPath, zipFile, report);
			}
		}

	}

	/**
	 * Copies a file
	 *
	 * @param fileName the file name
	 * @param sourcePath the path where the file is located
	 * @param templatesPath the path where the file should be copied to
	 * @param zipFile the zip file that contains the file
	 * @param parentObject the object that contains the file e.g. Report,
	 * Parameter etc
	 * @throws IOException
	 */
	private void copyFile(String fileName, String sourcePath,
			String templatesPath, File zipFile, Object parentObject) throws IOException {

		if (StringUtils.isNotBlank(fileName)) {
			String filePath = sourcePath + fileName;
			File file = new File(filePath);
			boolean unpacked = ZipUtil.unpackEntry(zipFile, fileName, file);
			if (unpacked) {
				String destinationFilePath = templatesPath + fileName;
				File destinationFile = new File(destinationFilePath);
				if (destinationFile.exists()) {
					logger.warn("File not overwritten: '{}'. {}", fileName, parentObject);
				} else {
					FileUtils.copyFile(file, destinationFile);
				}
				file.delete();
			}
		}
	}

	/**
	 * Imports role records
	 *
	 * @param file the file that contains the records to import
	 * @param sessionUser the session user
	 * @param conn the connection to use
	 * @param csvRoutines the CsvRoutines object to use
	 * @param importRecords the import records object
	 * @throws SQLException
	 */
	private void importRoles(File file, User sessionUser, Connection conn,
			CsvRoutines csvRoutines, ImportRecords importRecords) throws SQLException, IOException {

		logger.debug("Entering importRoles: sessionUser={}", sessionUser);

		List<Role> roles;
		MigrationFileFormat fileFormat = importRecords.getFileFormat();
		switch (fileFormat) {
			case json:
				ObjectMapper mapper = ArtUtils.getPropertyOnlyObjectMapper();
				roles = mapper.readValue(file, new TypeReference<List<Role>>() {
				});
				break;
			case csv:
				String extension = FilenameUtils.getExtension(file.getName());
				if (StringUtils.equalsIgnoreCase(extension, "csv")) {
					roles = csvRoutines.parseAll(Role.class, file);
				} else if (StringUtils.equalsIgnoreCase(extension, "zip")) {
					String artTempPath = Config.getArtTempPath();
					boolean unpacked;
					String rolesFilePath = artTempPath + ExportRecords.EMBEDDED_ROLES_FILENAME;
					File rolesFile = new File(rolesFilePath);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_ROLES_FILENAME, rolesFile);
					if (unpacked) {
						roles = csvRoutines.parseAll(Role.class, rolesFile);
						rolesFile.delete();
					} else {
						throw new RuntimeException("File not found: " + rolesFilePath);
					}

					String permissionsFileName = artTempPath + ExportRecords.EMBEDDED_PERMISSIONS_FILENAME;
					File permissionsFile = new File(permissionsFileName);
					unpacked = ZipUtil.unpackEntry(file, ExportRecords.EMBEDDED_PERMISSIONS_FILENAME, permissionsFile);
					if (unpacked) {
						List<Permission> permissions = csvRoutines.parseAll(Permission.class, permissionsFile);
						permissionsFile.delete();
						Map<Integer, Role> rolesMap = new HashMap<>();
						for (Role role : roles) {
							rolesMap.put(role.getRoleId(), role);
						}
						for (Permission permission : permissions) {
							int parentId = permission.getParentId();
							Role role = rolesMap.get(parentId);
							if (role == null) {
								throw new RuntimeException("Role not found. Parent Id = " + parentId);
							} else {
								List<Permission> rolePermissions = role.getPermissions();
								if (rolePermissions == null) {
									rolePermissions = new ArrayList<>();
								}
								rolePermissions.add(permission);
								role.setPermissions(rolePermissions);
							}
						}
					}
				} else {
					throw new IllegalArgumentException("Unexpected file extension: " + extension);
				}
				break;
			default:
				throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
		}

		roleService.importRoles(roles, sessionUser, conn);
	}

	/**
	 * Imports values from a file in json format
	 *
	 * @param <T>
	 * @param file the file to import from
	 * @param type the type of object contained in the file
	 * @return a list of objects as contained in the file
	 * @throws IOException
	 */
	private <T> T importFromJson(File file, Class<T> type) throws IOException {
		ObjectMapper mapper = ArtUtils.getMigrationObjectMapper();
		return mapper.readValue(file, type);
	}

	/**
	 * Imports values from a file in json format
	 *
	 * @param <T>
	 * @param file the file to import from
	 * @param type the type of object contained in the file
	 * @return a list of objects as contained in the file
	 * @throws IOException
	 */
	private <T> T importFromJson(File file, TypeReference type) throws IOException {
		ObjectMapper mapper = ArtUtils.getMigrationObjectMapper();
		return mapper.readValue(file, type);
	}

	/**
	 * Imports values from a file in csv format
	 *
	 * @param <T>
	 * @param file the file to import from
	 * @param type the type of object contained in the file
	 * @return a list of objects as contained in the file
	 * @throws IOException
	 */
	private <T> List<T> importValuesFromCsv(File file, Class<T> type) throws IOException {
		Class<?> mixIn = null;
		return importValuesFromCsv(file, type, mixIn);
	}

	/**
	 * Imports values from a file in csv format
	 *
	 * @param <T>
	 * @param file the file to import from
	 * @param type the type of object contained in the file
	 * @param mixIn a mixin to apply
	 * @return a list of objects as contained in the file
	 * @throws IOException
	 */
	private <T> List<T> importValuesFromCsv(File file, Class<T> type, Class<?> mixIn) throws IOException {
		//https://github.com/FasterXML/jackson-dataformats-text/tree/master/csv
		//https://stackoverflow.com/questions/52239104/jackson-csv-parser-chokes-on-comma-separated-value-files-if-is-in-a-field-ev
		//https://czetsuya-tech.blogspot.com/2017/03/how-to-read-and-write-csv-using-jackson.html
		//https://itexpertsconsultant.wordpress.com/2016/08/03/how-to-readwrite-csv-file-to-map-in-java/
		CsvMapper csvMapper = ArtUtils.getMigrationCsvMapper();
		if (mixIn != null) {
			csvMapper.addMixIn(type, mixIn);
		}
		CsvSchema schema = ExportRecords.getCsvSchema(csvMapper, type);
		MappingIterator<T> it = csvMapper.readerFor(type).with(schema).readValues(file);
		return it.readAll();
	}

	/**
	 * Imports values from a file in csv format
	 *
	 * @param <T>
	 * @param file the file to import from
	 * @param type the type of object contained in the file
	 * @return an object as contained in the file
	 * @throws IOException
	 */
	private <T> T importValueFromCsv(File file, Class<T> type) throws IOException {
		CsvMapper csvMapper = ArtUtils.getMigrationCsvMapper();
		CsvSchema schema = ExportRecords.getCsvSchema(csvMapper, type);
		return csvMapper.readerFor(type).with(schema).readValue(file);
	}

}
