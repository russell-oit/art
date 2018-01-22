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

import art.datasource.DatasourceService;
import art.enums.MigrationLocation;
import art.enums.MigrationRecordType;
import art.servlets.Config;
import art.settings.Settings;
import art.settings.SettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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

	@GetMapping("/exportRecords")
	public String showExportRecords(Model model, @RequestParam("type") String type) {
		logger.debug("Entering showExportRecords: type='{}'", type);
		
		ExportRecords exportRecords = new ExportRecords();
		exportRecords.setRecordType(MigrationRecordType.toEnum(type));

		model.addAttribute("exportRecords", exportRecords);

		return showEditExportRecords(model);
	}

	@PostMapping("/exportRecords")
	public String processExportRecords(@Valid ExportRecords exportRecords,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

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
					extension = ".zip";
			}

			String exportFileName = baseExportFilename + extension;
			String recordsExportPath = Config.getRecordsExportPath();
			String exportFilePath = recordsExportPath + exportFileName;

			switch (recordType) {
				case Settings:
					exportSettings(exportRecords, exportFilePath);
					break;
				default:
					break;
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
	 * @param exportFilePath the export file name to use
	 * @throws SQLException
	 * @throws IOException 
	 */
	private void exportSettings(ExportRecords exportRecords, String exportFilePath)
			throws SQLException, IOException {
		
		logger.debug("Entering exportSettings: exportFilepath='{}'", exportFilePath);

		Settings settings = settingsService.getSettings();
		MigrationLocation location = exportRecords.getLocation();
		switch (location) {
			case File:
				File exportFile = new File(exportFilePath);
				ObjectMapper mapper = new ObjectMapper();
				mapper.writerWithDefaultPrettyPrinter().writeValue(exportFile, settings);
				break;
			case Datasource:
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
