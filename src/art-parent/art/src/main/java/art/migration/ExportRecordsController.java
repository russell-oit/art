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

import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.datasource.DatasourceService;
import art.dbutils.DatabaseUtils;
import art.destination.Destination;
import art.destination.DestinationService;
import art.encryptor.Encryptor;
import art.encryptor.EncryptorService;
import art.enums.MigrationLocation;
import art.enums.MigrationRecordType;
import art.holiday.Holiday;
import art.holiday.HolidayService;
import art.servlets.Config;
import art.settings.Settings;
import art.settings.SettingsService;
import art.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.csv.CsvRoutines;
import com.univocity.parsers.csv.CsvWriterSettings;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
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
			String baseExportFilename = "art-export-" + recordType;
			String extension;
			switch (recordType) {
				case Settings:
					extension = ".json";
					break;
				default:
					extension = ".csv";
			}

			String exportFileName = baseExportFilename + extension;
			String recordsExportPath = Config.getRecordsExportPath();
			String exportFilePath = recordsExportPath + exportFileName;

			File file = new File(exportFilePath);

			User sessionUser = (User) session.getAttribute("sessionUser");

			CsvWriterSettings writerSettings = new CsvWriterSettings();
			writerSettings.setHeaderWritingEnabled(true);
			CsvRoutines csvRoutines = new CsvRoutines(writerSettings);

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
						exportDatasources(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					case Destinations:
						exportDestinations(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					case Encryptors:
						exportEncryptors(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					case Holidays:
						exportHolidays(exportRecords, file, sessionUser, csvRoutines, conn);
						break;
					default:
						break;
				}
			} finally {
				DatabaseUtils.close(conn);
			}

			if (exportRecords.getLocation() == MigrationLocation.File) {
				redirectAttributes.addFlashAttribute("exportFileName", exportFileName);
			}

			redirectAttributes.addFlashAttribute("message", "page.message.recordsExported");
			return "redirect:/success";
		} catch (SQLException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditExportRecords(model);
	}

	/**
	 * Exports application settings
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportSettings(ExportRecords exportRecords, File file,
			User sessionUser, Connection conn) throws SQLException, IOException {

		logger.debug("Entering exportSettings");

		Settings settings = settingsService.getSettings();
		settings.encryptPasswords();

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				ObjectMapper mapper = new ObjectMapper();
				mapper.writerWithDefaultPrettyPrinter().writeValue(file, settings);
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
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportDatasources(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportDatasources");

		String ids = exportRecords.getIds();
		List<Datasource> datasources = datasourceService.getDatasources(ids);
		for (Datasource datasource : datasources) {
			datasource.encryptPassword();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				csvRoutines.writeAll(datasources, Datasource.class, file);
				break;
			case Datasource:
				datasourceService.importDatasources(datasources, sessionUser, conn);
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
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportDestinations(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportDestinations");

		String ids = exportRecords.getIds();
		List<Destination> destinations = destinationService.getDestinations(ids);
		for (Destination destination : destinations) {
			destination.encryptPassword();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				csvRoutines.writeAll(destinations, Destination.class, file);
				break;
			case Datasource:
				destinationService.importDestinations(destinations, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports encryptor records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportEncryptors(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportEncryptors");

		String ids = exportRecords.getIds();
		List<Encryptor> encryptors = encryptorService.getEncryptors(ids);
		for (Encryptor encryptor : encryptors) {
			encryptor.encryptPasswords();
		}

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				csvRoutines.writeAll(encryptors, Encryptor.class, file);
				break;
			case Datasource:
				encryptorService.importEncryptors(encryptors, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
	}

	/**
	 * Exports holiday records
	 *
	 * @param exportRecords the export records object
	 * @param file the export file to use
	 * @param sessionUser the session user
	 * @param csvRoutines the CsvRoutines object to use for file export
	 * @param conn the connection to use for datasource export
	 * @throws SQLException
	 * @throws IOException
	 */
	private void exportHolidays(ExportRecords exportRecords, File file,
			User sessionUser, CsvRoutines csvRoutines, Connection conn)
			throws SQLException, IOException {

		logger.debug("Entering exportHolidays");

		String ids = exportRecords.getIds();
		List<Holiday> holidays = holidayService.getHolidays(ids);

		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				csvRoutines.writeAll(holidays, Holiday.class, file);
				break;
			case Datasource:
				holidayService.importHolidays(holidays, sessionUser, conn);
				break;
			default:
				throw new IllegalArgumentException("Unexpected location: " + location);
		}
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
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "exportRecords";
	}

}
