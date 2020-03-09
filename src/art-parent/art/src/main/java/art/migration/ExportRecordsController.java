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

import art.accessright.AccessRightService;
import art.accessright.UserGroupReportRight;
import art.accessright.UserGroupReportRightCsvExportMixIn;
import art.accessright.UserReportRight;
import art.accessright.UserReportRightCsvExportMixIn;
import art.cache.CacheHelper;
import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.datasource.DatasourceService;
import art.dbutils.DatabaseUtils;
import art.destination.Destination;
import art.destination.DestinationService;
import art.drilldown.Drilldown;
import art.drilldown.DrilldownCsvExportMixIn;
import art.drilldown.DrilldownService;
import art.encryptor.Encryptor;
import art.encryptor.EncryptorService;
import art.enums.EncryptorType;
import art.enums.MigrationFileFormat;
import art.enums.MigrationLocation;
import art.enums.MigrationRecordType;
import art.enums.ReportType;
import art.holiday.Holiday;
import art.holiday.HolidayService;
import art.parameter.Parameter;
import art.parameter.ParameterCsvExportMixIn;
import art.parameter.ParameterService;
import art.permission.Permission;
import art.report.Report;
import art.report.ReportCsvExportMixIn;
import art.report.ReportService;
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
import art.reportparameter.ReportParameterCsvExportMixIn;
import art.reportparameter.ReportParameterService;
import art.reportrule.ReportRule;
import art.reportrule.ReportRuleCsvExportMixIn;
import art.reportrule.ReportRuleService;
import art.role.Role;
import art.role.RoleService;
import art.rule.Rule;
import art.rule.RuleService;
import art.ruleValue.RuleValueService;
import art.ruleValue.UserGroupRuleValue;
import art.ruleValue.UserGroupRuleValueCsvExportMixIn;
import art.ruleValue.UserRuleValue;
import art.ruleValue.UserRuleValueCsvExportMixIn;
import art.schedule.Schedule;
import art.schedule.ScheduleService;
import art.servlets.Config;
import art.settings.Settings;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.collections4.CollectionUtils;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for exporting repository artifacts
 *
 * @author Timothy Anyona
 */
@Controller
public class ExportRecordsController {

	private static final Logger logger = LoggerFactory.getLogger(ExportRecordsController.class);

	@Autowired
	private SettingsService settingsService;

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
	private ReportService reportService;

	@Autowired
	private ReportParameterService reportParameterService;

	@Autowired
	private RuleValueService ruleValueService;

	@Autowired
	private ReportRuleService reportRuleService;

	@Autowired
	private AccessRightService accessRightService;

	@Autowired
	private DrilldownService drilldownService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private CacheHelper cacheHelper;

	@GetMapping("/exportRecords")
	public String showExportRecords(Model model, @RequestParam("type") String type,
			@RequestParam(value = "ids", required = false) String ids) {

		logger.debug("Entering showExportRecords: type='{}', ids='{}'", type, ids);

		ExportRecords exportRecords = new ExportRecords();
		exportRecords.setRecordType(MigrationRecordType.toEnum(type));
		exportRecords.setIds(ids);

		model.addAttribute("exportRecords", exportRecords);

		return showEditExportRecords(model);
	}

	@PostMapping("/exportRecords")
	public String processExportRecords(@Valid ExportRecords exportRecords,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering processExportRecords");

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditExportRecords(model);
		}

		try {
			MigrationRecordType recordType = exportRecords.getRecordType();
			String extension;
			MigrationFileFormat fileFormat = exportRecords.getFileFormat();
			switch (fileFormat) {
				case json:
					extension = ".json";
					break;
				case csv:
					extension = ".csv";
					break;
				default:
					throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
			}

			String baseExportFilename = "art-export-" + recordType;
			String exportFileName = baseExportFilename + extension;
			String recordsExportPath = Config.getRecordsExportPath();
			String exportFilePath = recordsExportPath + exportFileName;

			File file = new File(exportFilePath);

			User sessionUser = (User) session.getAttribute("sessionUser");

			Connection conn = null;
			if (exportRecords.getLocation() == MigrationLocation.Datasource) {
				Datasource datasource = exportRecords.getDatasource();
				conn = DbConnections.getConnection(datasource.getDatasourceId());
			}

			try {
				switch (recordType) {
					case Settings:
						exportSettings(exportRecords, file, sessionUser, conn);
						break;
					case Datasources:
						exportDatasources(exportRecords, file, sessionUser, conn);
						break;
					case Destinations:
						exportDestinations(exportRecords, file, sessionUser, conn);
						break;
					case Encryptors:
						exportFilePath = exportEncryptors(exportRecords, sessionUser, conn);
						break;
					case Holidays:
						exportHolidays(exportRecords, file, sessionUser, conn);
						break;
					case ReportGroups:
						exportReportGroups(exportRecords, file, sessionUser, conn);
						break;
					case SmtpServers:
						exportSmtpServers(exportRecords, file, sessionUser, conn);
						break;
					case UserGroups:
						exportFilePath = exportUserGroups(exportRecords, file, sessionUser, conn);
						break;
					case Schedules:
						exportFilePath = exportSchedules(exportRecords, file, sessionUser, conn);
						break;
					case Users:
						exportFilePath = exportUsers(exportRecords, file, sessionUser, conn);
						break;
					case Rules:
						exportRules(exportRecords, file, sessionUser, conn);
						break;
					case Parameters:
						exportFilePath = exportParameters(exportRecords, sessionUser, conn);
						break;
					case Reports:
						exportFilePath = exportReports(exportRecords, sessionUser, conn);
						break;
					case Roles:
						exportFilePath = exportRoles(exportRecords, file, sessionUser, conn);
						break;
					default:
						break;
				}
			} finally {
				DatabaseUtils.close(conn);
			}

			if (exportRecords.getLocation() == MigrationLocation.File) {
				exportFileName = FilenameUtils.getName(exportFilePath);
				redirectAttributes.addFlashAttribute("exportFileName", exportFileName);
			}

			redirectAttributes.addFlashAttribute("message", "page.message.recordsExported");
			return "redirect:/success";
		} catch (Exception ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditExportRecords(model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param model the model object
	 * @return the jsp file to display
	 */
	private String showEditExportRecords(Model model) {
		try {
			model.addAttribute("datasources", datasourceService.getActiveJdbcDatasources());
			model.addAttribute("locations", MigrationLocation.list());
			model.addAttribute("fileFormats", MigrationFileFormat.list());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "exportRecords";
	}

	/**
	 * Exports application settings
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @throws Exception
	 */
	private void exportSettings(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws Exception {

		logger.debug("Entering exportSettings");

		Settings settings = settingsService.getSettings();
		if (settings == null) {
			throw new RuntimeException("No settings to export");
		}
		settings.encryptPasswords();

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, settings);
						break;
					case csv:
						exportToCsv(file, settings, Settings.class);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				settingsService.importSettings(settings, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports datasource records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @throws Exception
	 */
	private void exportDatasources(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws Exception {

		logger.debug("Entering exportDatasources");

		String ids = exportRecords.getIds();
		List<Datasource> datasources = datasourceService.getDatasources(ids);
		for (Datasource datasource : datasources) {
			datasource.encryptPassword();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, datasources);
						break;
					case csv:
						exportToCsv(file, datasources, Datasource.class);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean overwrite = exportRecords.isOverwrite();
				datasourceService.importDatasources(datasources, sessionUser, conn, overwrite);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports destination records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @throws Exception
	 */
	private void exportDestinations(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws Exception {

		logger.debug("Entering exportDestinations");

		String ids = exportRecords.getIds();
		List<Destination> destinations = destinationService.getDestinations(ids);
		for (Destination destination : destinations) {
			destination.encryptPassword();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, destinations);
						break;
					case csv:
						exportToCsv(file, destinations, Destination.class);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean overwrite = exportRecords.isOverwrite();
				destinationService.importDestinations(destinations, sessionUser, conn, overwrite);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports encryptor records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @return the export file path for file export
	 * @throws Exception
	 */
	private String exportEncryptors(ExportRecords exportRecords,
			User sessionUser, Connection conn) throws Exception {

		logger.debug("Entering exportEncryptors");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<Encryptor> encryptors = encryptorService.getEncryptors(ids);
		for (Encryptor encryptor : encryptors) {
			encryptor.encryptPasswords();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				String zipFilePath = recordsExportPath + "art-export-Encryptors.zip";
				String encryptorsFilePath;
				File encryptorsFile;

				List<String> filesToZip = getEncryptorFiles(encryptors);

				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						encryptorsFilePath = recordsExportPath + ExportRecords.EMBEDDED_JSON_ENCRYPTORS_FILENAME;
						encryptorsFile = new File(encryptorsFilePath);
						exportToJson(encryptorsFile, encryptors);
						if (CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(encryptorsFilePath);
							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							encryptorsFile.delete();
						} else {
							exportFilePath = encryptorsFilePath;
						}
						break;
					case csv:
						encryptorsFilePath = recordsExportPath + ExportRecords.EMBEDDED_CSV_ENCRYPTORS_FILENAME;
						encryptorsFile = new File(encryptorsFilePath);
						exportToCsv(encryptorsFile, encryptors, Encryptor.class);
						if (CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(encryptorsFilePath);
							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							encryptorsFile.delete();
						} else {
							exportFilePath = encryptorsFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean overwrite = exportRecords.isOverwrite();
				encryptorService.importEncryptors(encryptors, sessionUser, conn, overwrite);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports holiday records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportHolidays(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws SQLException, IOException {

		logger.debug("Entering exportHolidays");

		String ids = exportRecords.getIds();
		List<Holiday> holidays = holidayService.getHolidays(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, holidays);
						break;
					case csv:
						exportToCsv(file, holidays, Holiday.class);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean overwrite = exportRecords.isOverwrite();
				holidayService.importHolidays(holidays, sessionUser, conn, overwrite);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports report group records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportReportGroups(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws SQLException, IOException {

		logger.debug("Entering exportReportGroups");

		String ids = exportRecords.getIds();
		List<ReportGroup> reportGroups = reportGroupService.getReportGroups(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, reportGroups);
						break;
					case csv:
						exportToCsv(file, reportGroups, ReportGroup.class);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean overwrite = exportRecords.isOverwrite();
				reportGroupService.importReportGroups(reportGroups, sessionUser, conn, overwrite);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports smtp server records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @throws Exception
	 */
	private void exportSmtpServers(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws Exception {

		logger.debug("Entering exportSmtpServers");

		String ids = exportRecords.getIds();
		List<SmtpServer> smtpServers = smtpServerService.getSmtpServers(ids);
		for (SmtpServer smtpServer : smtpServers) {
			smtpServer.encryptPasswords();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, smtpServers);
						break;
					case csv:
						exportToCsv(file, smtpServers, SmtpServer.class);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean overwrite = exportRecords.isOverwrite();
				smtpServerService.importSmtpServers(smtpServers, sessionUser, conn, overwrite);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports user group records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportUserGroups(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws SQLException, IOException {

		logger.debug("Entering exportUserGroups");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<UserGroup> userGroups = userGroupService.getUserGroups(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, userGroups);
						exportFilePath = recordsExportPath + "art-export-UserGroups.json";
						break;
					case csv:
						String userGroupsFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERGROUPS_FILENAME;
						File userGroupsFile = new File(userGroupsFilePath);
						exportToCsv(userGroupsFile, userGroups, UserGroup.class, UserGroupCsvExportMixIn.class);

						List<Role> allRoles = new ArrayList<>();
						for (UserGroup userGroup : userGroups) {
							List<Role> roles = userGroup.getRoles();
							for (Role role : roles) {
								role.setParentId(userGroup.getUserGroupId());
								allRoles.add(role);
							}
						}

						List<Permission> allPermissions = new ArrayList<>();
						for (UserGroup userGroup : userGroups) {
							List<Permission> permissions = userGroup.getPermissions();
							for (Permission permission : permissions) {
								permission.setParentId(userGroup.getUserGroupId());
								allPermissions.add(permission);
							}
						}

						if (CollectionUtils.isNotEmpty(allRoles)
								|| CollectionUtils.isNotEmpty(allPermissions)) {
							List<String> filesToZip = new ArrayList<>();
							filesToZip.add(userGroupsFilePath);

							String rolesFilePath = recordsExportPath + ExportRecords.EMBEDDED_ROLES_FILENAME;
							File rolesFile = new File(rolesFilePath);
							if (CollectionUtils.isNotEmpty(allRoles)) {
								exportToCsv(rolesFile, allRoles, Role.class);
								filesToZip.add(rolesFilePath);
							}

							String permissionsFilePath = recordsExportPath + ExportRecords.EMBEDDED_PERMISSIONS_FILENAME;
							File permissionsFile = new File(permissionsFilePath);
							if (CollectionUtils.isNotEmpty(allPermissions)) {
								exportToCsv(permissionsFile, allPermissions, Permission.class);
								filesToZip.add(permissionsFilePath);
							}

							exportFilePath = recordsExportPath + "art-export-UserGroups.zip";
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							userGroupsFile.delete();
							rolesFile.delete();
							permissionsFile.delete();
						} else {
							exportFilePath = userGroupsFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				userGroupService.importUserGroups(userGroups, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports schedule records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @param file the export file to use, for json output
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportSchedules(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws SQLException, IOException {

		logger.debug("Entering exportSchedules");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<Schedule> schedules = scheduleService.getSchedules(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, schedules);
						exportFilePath = recordsExportPath + "art-export-Schedules.json";
						break;
					case csv:
						String schedulesFilePath = recordsExportPath + ExportRecords.EMBEDDED_SCHEDULES_FILENAME;
						File schedulesFile = new File(schedulesFilePath);
						exportToCsv(schedulesFile, schedules, Schedule.class);
						List<Holiday> holidays = new ArrayList<>();
						for (Schedule schedule : schedules) {
							List<Holiday> sharedHolidays = schedule.getSharedHolidays();
							for (Holiday holiday : sharedHolidays) {
								holiday.setParentId(schedule.getScheduleId());
								holidays.add(holiday);
							}
						}
						if (CollectionUtils.isNotEmpty(holidays)) {
							String holidaysFilePath = recordsExportPath + ExportRecords.EMBEDDED_HOLIDAYS_FILENAME;
							File holidaysFile = new File(holidaysFilePath);
							exportToCsv(holidaysFile, holidays, Holiday.class);
							exportFilePath = recordsExportPath + "art-export-Schedules.zip";
							ArtUtils.zipFiles(exportFilePath, schedulesFilePath, holidaysFilePath);
							schedulesFile.delete();
							holidaysFile.delete();
						} else {
							exportFilePath = schedulesFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean overwrite = exportRecords.isOverwrite();
				scheduleService.importSchedules(schedules, sessionUser, conn, overwrite);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports user records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @param file the export file to use, for json output
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportUsers(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws SQLException, IOException {

		logger.debug("Entering exportUsers");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<User> users = userService.getUsers(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, users);
						exportFilePath = recordsExportPath + "art-export-Users.json";
						break;
					case csv:
						String usersFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERS_FILENAME;
						File usersFile = new File(usersFilePath);
						exportToCsv(usersFile, users, User.class, UserCsvExportMixIn.class);

						List<UserGroup> allUserGroups = new ArrayList<>();
						for (User user : users) {
							List<UserGroup> userGroups = user.getUserGroups();
							for (UserGroup userGroup : userGroups) {
								userGroup.setParentId(user.getUserId());
								allUserGroups.add(userGroup);
							}
						}

						List<Role> allRoles = new ArrayList<>();
						for (User user : users) {
							List<Role> roles = user.getRoles();
							for (Role role : roles) {
								role.setParentId(user.getUserId());
								allRoles.add(role);
							}
						}

						List<Permission> allPermissions = new ArrayList<>();
						for (User user : users) {
							List<Permission> permissions = user.getPermissions();
							for (Permission permission : permissions) {
								permission.setParentId(user.getUserId());
								allPermissions.add(permission);
							}
						}

						if (CollectionUtils.isNotEmpty(allUserGroups)
								|| CollectionUtils.isNotEmpty(allRoles)
								|| CollectionUtils.isNotEmpty(allPermissions)) {
							List<String> filesToZip = new ArrayList<>();
							filesToZip.add(usersFilePath);

							String userGroupsFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERGROUPS_FILENAME;
							File userGroupsFile = new File(userGroupsFilePath);
							if (CollectionUtils.isNotEmpty(allUserGroups)) {
								exportToCsv(userGroupsFile, allUserGroups, UserGroup.class, UserGroupCsvExportMixIn.class);
								filesToZip.add(userGroupsFilePath);
							}

							String rolesFilePath = recordsExportPath + ExportRecords.EMBEDDED_ROLES_FILENAME;
							File rolesFile = new File(rolesFilePath);
							if (CollectionUtils.isNotEmpty(allRoles)) {
								exportToCsv(rolesFile, allRoles, Role.class);
								filesToZip.add(rolesFilePath);
							}

							String permissionsFilePath = recordsExportPath + ExportRecords.EMBEDDED_PERMISSIONS_FILENAME;
							File permissionsFile = new File(permissionsFilePath);
							if (CollectionUtils.isNotEmpty(allPermissions)) {
								exportToCsv(permissionsFile, allPermissions, Permission.class);
								filesToZip.add(permissionsFilePath);
							}

							exportFilePath = recordsExportPath + "art-export-Users.zip";
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							usersFile.delete();
							userGroupsFile.delete();
							rolesFile.delete();
							permissionsFile.delete();
						} else {
							exportFilePath = usersFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				userService.importUsers(users, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports rule records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportRules(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws SQLException, IOException {

		logger.debug("Entering exportRules");

		String ids = exportRecords.getIds();
		List<Rule> rules = ruleService.getRules(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, rules);
						break;
					case csv:
						exportToCsv(file, rules, Rule.class);
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean overwrite = exportRecords.isOverwrite();
				ruleService.importRules(rules, sessionUser, conn, overwrite);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports parameter records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @return the export file path for file export
	 * @throws Exception
	 */
	private String exportParameters(ExportRecords exportRecords,
			User sessionUser, Connection conn) throws Exception {

		logger.debug("Entering exportParameters");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<Parameter> parameters = parameterService.getParameters(ids);

		for (Parameter parameter : parameters) {
			parameter.encryptAllPasswords();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				String zipFilePath = recordsExportPath + "art-export-Parameters.zip";
				String parametersFilePath;
				File parametersFile;

				List<String> filesToZip = getParameterTemplateFiles(parameters);

				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						parametersFilePath = recordsExportPath + ExportRecords.EMBEDDED_JSON_PARAMETERS_FILENAME;
						parametersFile = new File(parametersFilePath);
						exportToJson(parametersFile, parameters);
						if (CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(parametersFilePath);
							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							parametersFile.delete();
						} else {
							exportFilePath = parametersFilePath;
						}
						break;
					case csv:
						parametersFilePath = recordsExportPath + ExportRecords.EMBEDDED_CSV_PARAMETERS_FILENAME;
						parametersFile = new File(parametersFilePath);
						exportToCsv(parametersFile, parameters, Parameter.class, ParameterCsvExportMixIn.class);
						if (CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(parametersFilePath);
							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							parametersFile.delete();
						} else {
							exportFilePath = parametersFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean local = false;
				parameterService.importParameters(parameters, sessionUser, conn, local);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports report records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @return the export file path for file export
	 * @throws Exception
	 */
	private String exportReports(ExportRecords exportRecords,
			User sessionUser, Connection conn) throws Exception {

		logger.debug("Entering exportReports");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<Report> exportReports = reportService.getReports(ids);

		ReportServiceHelper reportServiceHelper = new ReportServiceHelper();
		List<Report> reports = reportServiceHelper.prepareReportsForExport(exportReports);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				String zipFilePath = recordsExportPath + "art-export-Reports.zip";
				String reportsFilePath;
				File reportsFile;

				List<String> filesToZip = getReportTemplateFiles(reports);

				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						reportsFilePath = recordsExportPath + ExportRecords.EMBEDDED_JSON_REPORTS_FILENAME;
						reportsFile = new File(reportsFilePath);
						exportToJson(reportsFile, reports);
						if (CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(reportsFilePath);
							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							reportsFile.delete();
						} else {
							exportFilePath = reportsFilePath;
						}
						break;
					case csv:
						reportsFilePath = recordsExportPath + ExportRecords.EMBEDDED_CSV_REPORTS_FILENAME;
						reportsFile = new File(reportsFilePath);
						exportToCsv(reportsFile, reports, Report.class, ReportCsvExportMixIn.class);

						List<ReportGroup> allReportGroups = new ArrayList<>();
						List<ReportParameter> allReportParams = new ArrayList<>();
						List<UserRuleValue> allUserRuleValues = new ArrayList<>();
						List<UserGroupRuleValue> allUserGroupRuleValues = new ArrayList<>();
						List<ReportRule> allReportRules = new ArrayList<>();
						List<UserReportRight> allUserReportRights = new ArrayList<>();
						List<UserGroupReportRight> allUserGroupReportRights = new ArrayList<>();
						List<Drilldown> allDrilldowns = new ArrayList<>();

						for (Report report : reports) {
							List<ReportGroup> reportGroups = report.getReportGroups();
							allReportGroups.addAll(reportGroups);

							List<ReportParameter> reportParams = report.getReportParams();
							allReportParams.addAll(reportParams);

							List<UserRuleValue> userRuleValues = report.getUserRuleValues();
							allUserRuleValues.addAll(userRuleValues);

							List<UserGroupRuleValue> userGroupRuleValues = report.getUserGroupRuleValues();
							allUserGroupRuleValues.addAll(userGroupRuleValues);

							List<ReportRule> reportRules = report.getReportRules();
							allReportRules.addAll(reportRules);

							List<UserReportRight> userReportRights = report.getUserReportRights();
							allUserReportRights.addAll(userReportRights);

							List<UserGroupReportRight> userGroupReportRights = report.getUserGroupReportRights();
							allUserGroupReportRights.addAll(userGroupReportRights);

							List<Drilldown> drilldowns = report.getDrilldowns();
							allDrilldowns.addAll(drilldowns);
						}

						if (CollectionUtils.isNotEmpty(allReportGroups)
								|| CollectionUtils.isNotEmpty(allReportParams)
								|| CollectionUtils.isNotEmpty(allUserRuleValues)
								|| CollectionUtils.isNotEmpty(allUserGroupRuleValues)
								|| CollectionUtils.isNotEmpty(allReportRules)
								|| CollectionUtils.isNotEmpty(allUserReportRights)
								|| CollectionUtils.isNotEmpty(allUserGroupReportRights)
								|| CollectionUtils.isNotEmpty(allDrilldowns)
								|| CollectionUtils.isNotEmpty(filesToZip)) {
							filesToZip.add(reportsFilePath);

							String reportGroupsFilePath = recordsExportPath + ExportRecords.EMBEDDED_REPORTGROUPS_FILENAME;
							File reportGroupsFile = new File(reportGroupsFilePath);

							String reportParamsFilePath = recordsExportPath + ExportRecords.EMBEDDED_REPORTPARAMETERS_FILENAME;
							File reportParamsFile = new File(reportParamsFilePath);

							String userRuleValuesFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERRULEVALUES_FILENAME;
							File userRuleValuesFile = new File(userRuleValuesFilePath);

							String userGroupRuleValuesFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERGROUPRULEVALUES_FILENAME;
							File userGroupRuleValuesFile = new File(userGroupRuleValuesFilePath);

							String reportRulesFilePath = recordsExportPath + ExportRecords.EMBEDDED_REPORTRULES_FILENAME;
							File reportRulesFile = new File(reportRulesFilePath);

							String userReportRightsFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERREPORTRIGHTS_FILENAME;
							File userReportRightsFile = new File(userReportRightsFilePath);

							String userGroupReportRightsFilePath = recordsExportPath + ExportRecords.EMBEDDED_USERGROUPREPORTRIGHTS_FILENAME;
							File userGroupReportRightsFile = new File(userGroupReportRightsFilePath);

							String drilldownsFilePath = recordsExportPath + ExportRecords.EMBEDDED_DRILLDOWNS_FILENAME;
							File drilldownsFile = new File(drilldownsFilePath);

							if (CollectionUtils.isNotEmpty(allReportGroups)) {
								exportToCsv(reportGroupsFile, allReportGroups, ReportGroup.class);
								filesToZip.add(reportGroupsFilePath);
							}

							if (CollectionUtils.isNotEmpty(allReportParams)) {
								exportToCsv(reportParamsFile, allReportParams, ReportParameter.class, ReportParameterCsvExportMixIn.class);
								filesToZip.add(reportParamsFilePath);
							}

							if (CollectionUtils.isNotEmpty(allUserRuleValues)) {
								exportToCsv(userRuleValuesFile, allUserRuleValues, UserRuleValue.class, UserRuleValueCsvExportMixIn.class);
								filesToZip.add(userRuleValuesFilePath);
							}

							if (CollectionUtils.isNotEmpty(allUserGroupRuleValues)) {
								exportToCsv(userGroupRuleValuesFile, allUserGroupRuleValues, UserGroupRuleValue.class, UserGroupRuleValueCsvExportMixIn.class);
								filesToZip.add(userGroupRuleValuesFilePath);
							}

							if (CollectionUtils.isNotEmpty(allReportRules)) {
								exportToCsv(reportRulesFile, allReportRules, ReportRule.class, ReportRuleCsvExportMixIn.class);
								filesToZip.add(reportRulesFilePath);
							}

							if (CollectionUtils.isNotEmpty(allUserReportRights)) {
								exportToCsv(userReportRightsFile, allUserReportRights, UserReportRight.class, UserReportRightCsvExportMixIn.class);
								filesToZip.add(userReportRightsFilePath);
							}

							if (CollectionUtils.isNotEmpty(allUserGroupReportRights)) {
								exportToCsv(userGroupReportRightsFile, allUserGroupReportRights, UserGroupReportRight.class, UserGroupReportRightCsvExportMixIn.class);
								filesToZip.add(userGroupReportRightsFilePath);
							}

							if (CollectionUtils.isNotEmpty(allDrilldowns)) {
								exportToCsv(drilldownsFile, allDrilldowns, Drilldown.class, DrilldownCsvExportMixIn.class);
								filesToZip.add(drilldownsFilePath);
							}

							exportFilePath = zipFilePath;
							ArtUtils.zipFiles(exportFilePath, filesToZip);
							reportsFile.delete();
							reportGroupsFile.delete();
							reportParamsFile.delete();
							userRuleValuesFile.delete();
							userGroupRuleValuesFile.delete();
							reportRulesFile.delete();
							userReportRightsFile.delete();
							userGroupReportRightsFile.delete();
							drilldownsFile.delete();
						} else {
							exportFilePath = reportsFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean local = false;
				reportServiceHelper.importReports(reports, sessionUser, conn, local);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		//clear objects that could have had their passwords encrypted, so as to have fresh start
		cacheHelper.clearReports();
		cacheHelper.clearDatasources();
		cacheHelper.clearEncryptors();
		cacheHelper.clearParameters();

		return exportFilePath;
	}

	/**
	 * Returns full paths of template files to include in the final zip package
	 *
	 * @param reports the reports being exported
	 * @return full paths of template files to include in the final zip package
	 * @throws IOException
	 */
	private List<String> getReportTemplateFiles(List<Report> reports) throws IOException {
		List<String> filesToZip = new ArrayList<>();

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
					addFileToZipList(template, mainTemplatePath, filesToZip);
				}

				String options = report.getOptions();
				if (StringUtils.isNotBlank(options)) {
					switch (reportType) {
						case JxlsArt:
						case JxlsTemplate:
							JxlsOptions jxlsOptions = ArtUtils.jsonToObject(options, JxlsOptions.class);
							String areaConfigFilename = jxlsOptions.getAreaConfigFile();
							addFileToZipList(areaConfigFilename, templatesPath, filesToZip);
							break;
						case PivotTableJsCsvServer:
						case DygraphsCsvServer:
						case DataTablesCsvServer:
							CsvServerOptions csvServerOptions = ArtUtils.jsonToObject(options, CsvServerOptions.class);
							String dataFileName = csvServerOptions.getDataFile();
							addFileToZipList(dataFileName, jsTemplatesPath, filesToZip);
							break;
						case C3:
							C3Options c3Options = ArtUtils.jsonToObject(options, C3Options.class);
							String cssFileName = c3Options.getCssFile();
							addFileToZipList(cssFileName, jsTemplatesPath, filesToZip);
							break;
						case Datamaps:
						case DatamapsFile:
							DatamapsOptions datamapsOptions = ArtUtils.jsonToObject(options, DatamapsOptions.class);

							String datamapsJsFileName = datamapsOptions.getDatamapsJsFile();
							addFileToZipList(datamapsJsFileName, jsTemplatesPath, filesToZip);

							dataFileName = datamapsOptions.getDataFile();
							addFileToZipList(dataFileName, jsTemplatesPath, filesToZip);

							String mapFileName = datamapsOptions.getMapFile();
							addFileToZipList(mapFileName, jsTemplatesPath, filesToZip);

							cssFileName = datamapsOptions.getCssFile();
							addFileToZipList(cssFileName, jsTemplatesPath, filesToZip);
							break;
						case Leaflet:
						case OpenLayers:
							WebMapOptions webMapOptions = ArtUtils.jsonToObject(options, WebMapOptions.class);

							cssFileName = webMapOptions.getCssFile();
							addFileToZipList(cssFileName, jsTemplatesPath, filesToZip);

							dataFileName = webMapOptions.getDataFile();
							addFileToZipList(dataFileName, jsTemplatesPath, filesToZip);

							List<String> jsFileNames = webMapOptions.getJsFiles();
							addFilesToZipList(jsFileNames, jsTemplatesPath, filesToZip);

							List<String> cssFileNames = webMapOptions.getCssFiles();
							addFilesToZipList(cssFileNames, jsTemplatesPath, filesToZip);
							break;
						case OrgChartDatabase:
						case OrgChartJson:
						case OrgChartList:
						case OrgChartAjax:
							OrgChartOptions orgChartOptions = ArtUtils.jsonToObject(options, OrgChartOptions.class);
							cssFileName = orgChartOptions.getCssFile();
							addFileToZipList(cssFileName, jsTemplatesPath, filesToZip);
							break;
						case FreeMarker:
						case Thymeleaf:
						case Velocity:
							TemplateResultOptions templateResultOptions = ArtUtils.jsonToObject(options, TemplateResultOptions.class);
							List<String> fileNames = templateResultOptions.getFiles();
							addFilesToZipList(fileNames, jsTemplatesPath, filesToZip);
							break;
						case JasperReportsArt:
						case JasperReportsTemplate:
							JasperReportsOptions jasperReportsOptions = ArtUtils.jsonToObject(options, JasperReportsOptions.class);

							List<String> subreportFileNames = jasperReportsOptions.getSubreports();
							addFilesToZipList(subreportFileNames, templatesPath, filesToZip);

							fileNames = jasperReportsOptions.getFiles();
							addFilesToZipList(fileNames, templatesPath, filesToZip);
							break;
						default:
							break;
					}
				}
			}
		}

		return filesToZip;
	}

	/**
	 * Returns full paths of template files to include in the final zip package
	 *
	 * @param parameters the parameters being exported
	 * @return full paths of template files to include in the final zip package
	 * @throws IOException
	 */
	private List<String> getParameterTemplateFiles(List<Parameter> parameters) throws IOException {
		List<String> filesToZip = new ArrayList<>();

		String jsTemplatesPath = Config.getJsTemplatesPath();
		for (Parameter parameter : parameters) {
			String template = parameter.getTemplate();
			addFileToZipList(template, jsTemplatesPath, filesToZip);
		}

		return filesToZip;
	}

	/**
	 * Returns full paths of encryptor files to include in the final zip package
	 *
	 * @param encryptors the encryptors being exported
	 * @return full paths of template files to include in the final zip package
	 * @throws IOException
	 */
	private List<String> getEncryptorFiles(List<Encryptor> encryptors) throws IOException {
		List<String> filesToZip = new ArrayList<>();

		String templatesPath = Config.getTemplatesPath();
		for (Encryptor encryptor : encryptors) {
			EncryptorType encryptorType = encryptor.getEncryptorType();
			if (encryptorType == null) {
				logger.warn("encryptorType is null. Encryptor={}", encryptor);
			} else {
				switch (encryptorType) {
					case OpenPGP:
						String publicKeyFileName = encryptor.getOpenPgpPublicKeyFile();
						addFileToZipList(publicKeyFileName, templatesPath, filesToZip);
						break;
					default:
						break;
				}
			}
		}

		return filesToZip;
	}

	/**
	 * Adds files to the files to zip list
	 *
	 * @param fileNames the file names
	 * @param templatesPath the path where the files are found
	 * @param filesToZip the list to add to
	 */
	private void addFilesToZipList(List<String> fileNames, String templatesPath,
			List<String> filesToZip) {

		if (CollectionUtils.isNotEmpty(fileNames)) {
			for (String fileName : fileNames) {
				addFileToZipList(fileName, templatesPath, filesToZip);
			}
		}

	}

	/**
	 * Adds a file to the files to zip list
	 *
	 * @param fileName the file name
	 * @param templatesPath the path where the file is
	 * @param filesToZip the list to add to
	 */
	private void addFileToZipList(String fileName, String templatesPath,
			List<String> filesToZip) {

		if (StringUtils.isNotBlank(fileName)) {
			String fullFileName = templatesPath + fileName;
			File file = new File(fullFileName);
			if (file.exists() && !filesToZip.contains(fullFileName)) {
				filesToZip.add(fullFileName);
			}
		}
	}

	/**
	 * Exports role records
	 *
	 * @param exportRecords the export records object
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @param file the export file to use, for json output
	 * @return the export file path for file export
	 * @throws SQLException
	 * @throws IOException
	 */
	private String exportRoles(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws SQLException, IOException {

		logger.debug("Entering exportRoles");

		String exportFilePath = null;

		String ids = exportRecords.getIds();
		List<Role> roles = roleService.getRoles(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				String recordsExportPath = Config.getRecordsExportPath();
				MigrationFileFormat fileFormat = exportRecords.getFileFormat();
				switch (fileFormat) {
					case json:
						exportToJson(file, roles);
						exportFilePath = recordsExportPath + "art-export-Roles.json";
						break;
					case csv:
						String rolesFilePath = recordsExportPath + ExportRecords.EMBEDDED_ROLES_FILENAME;
						File rolesFile = new File(rolesFilePath);
						exportToCsv(rolesFile, roles, Role.class);
						List<Permission> permissions = new ArrayList<>();
						for (Role role : roles) {
							List<Permission> rolePermissions = role.getPermissions();
							for (Permission permission : rolePermissions) {
								permission.setParentId(role.getRoleId());
								permissions.add(permission);
							}
						}
						if (CollectionUtils.isNotEmpty(permissions)) {
							String permissionsFilePath = recordsExportPath + ExportRecords.EMBEDDED_PERMISSIONS_FILENAME;
							File permissionsFile = new File(permissionsFilePath);
							exportToCsv(permissionsFile, permissions, Permission.class);
							exportFilePath = recordsExportPath + "art-export-Roles.zip";
							ArtUtils.zipFiles(exportFilePath, rolesFilePath, permissionsFilePath);
							rolesFile.delete();
							permissionsFile.delete();
						} else {
							exportFilePath = rolesFilePath;
						}
						break;
					default:
						throw new IllegalArgumentException("Unexpected file format: " + fileFormat);
				}
				break;
			case Datasource:
				boolean overwrite = exportRecords.isOverwrite();
				roleService.importRoles(roles, sessionUser, conn, overwrite);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}

		return exportFilePath;
	}

	/**
	 * Exports values to a file in json format
	 *
	 * @param file the file to export to
	 * @param value the object to export
	 * @throws java.io.IOException
	 */
	public void exportToJson(File file, Object value) throws IOException {
		ObjectMapper mapper = ArtUtils.getMigrationObjectMapper();
		mapper.writerWithDefaultPrettyPrinter().writeValue(file, value);
	}

	/**
	 * Exports values to a file in csv format
	 *
	 * @param file the file to export to
	 * @param value the object to export
	 * @param type the type of object
	 * @throws IOException
	 */
	private void exportToCsv(File file, Object value, Class<?> type) throws IOException {
		Class<?> mixIn = null;
		exportToCsv(file, value, type, mixIn);
	}

	/**
	 * Exports values to a file in csv format
	 *
	 * @param file the file to export to
	 * @param value the object to export
	 * @param type the type of object
	 * @param mixIn a mixin to apply
	 * @throws IOException
	 */
	private void exportToCsv(File file, Object value, Class<?> type, Class<?> mixIn)
			throws IOException {

		//https://gist.github.com/shsdev/11392809
		CsvMapper csvMapper = ArtUtils.getMigrationCsvMapper();
		if (mixIn != null) {
			csvMapper.addMixIn(type, mixIn);
		}
		CsvSchema schema = ExportRecords.getCsvSchema(csvMapper, type);
		csvMapper.writer(schema).writeValue(file, value);
	}

}
