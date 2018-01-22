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

import art.enums.MigrationRecordType;
import art.servlets.Config;
import art.settings.Settings;
import art.settings.SettingsService;
import art.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletContext;
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
			if (importFile == null || importFile.isEmpty()) {
				throw new IllegalArgumentException("Import file not selected or empty");
			}

			User sessionUser = (User) session.getAttribute("sessionUser");

			switch (importRecords.getRecordType()) {
				case Settings:
					importSettings(importFile, sessionUser, session);
					break;
				default:
					break;
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
	 * @param file the import file
	 * @param sessionUser the session user
	 * @param session the http session
	 * @throws IOException
	 * @throws SQLException 
	 */
	private void importSettings(MultipartFile file, User sessionUser,
			HttpSession session) throws IOException, SQLException {
		
		logger.debug("Entering importSettings: sessionUser={}", sessionUser);

		String artTempPath = Config.getArtTempPath();
		String destinationFilename = artTempPath + file.getOriginalFilename();
		File destinationFile = new File(destinationFilename);
		file.transferTo(destinationFile);

		try {
			ObjectMapper mapper = new ObjectMapper();
			Settings settings = mapper.readValue(destinationFile, Settings.class);
			settingsService.importSettings(settings, sessionUser, session, servletContext);
			Config.loadSettings();
		} finally {
			destinationFile.delete();
		}
	}

}
