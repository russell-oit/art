/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.settings;

import art.artdatabase.ArtDatabase;
import art.connectionpool.DbConnections;
import art.datasource.Datasource;
import art.datasource.DatasourceService;
import art.dbutils.DatabaseUtils;
import art.destination.Destination;
import art.destination.DestinationService;
import art.encryption.AesEncryptor;
import art.encryptor.Encryptor;
import art.encryptor.EncryptorService;
import art.enums.ArtAuthenticationMethod;
import art.enums.DisplayNull;
import art.enums.LdapAuthenticationMethod;
import art.enums.LdapConnectionEncryptionMethod;
import art.enums.LoggerLevel;
import art.enums.PdfPageSize;
import art.general.AjaxResponse;
import art.report.Report;
import art.report.ReportService;
import art.servlets.Config;
import art.smtpserver.SmtpServer;
import art.smtpserver.SmtpServerService;
import art.user.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for settings configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class SettingsController {

	private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private DatasourceService datasourceService;

	@Autowired
	private SettingsService settingsService;

	@Autowired
	private EncryptorService encryptorService;

	@Autowired
	private DestinationService destinationService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private SmtpServerService smtpServerService;

	@Autowired
	private MessageSource messageSource;

	@ModelAttribute("pdfPageSizes")
	public PdfPageSize[] addPdfPageSizes() {
		return PdfPageSize.values();
	}

	@ModelAttribute("ldapConnectionEncryptionMethods")
	public LdapConnectionEncryptionMethod[] addLdapConnectionEncryptionMethods() {
		return LdapConnectionEncryptionMethod.values();
	}

	@ModelAttribute("artAuthenticationMethods")
	public List<ArtAuthenticationMethod> addArtAuthenticationMethods() {
		List<ArtAuthenticationMethod> methods = ArtAuthenticationMethod.list();

		//remove irrelevant methods
		methods.remove(ArtAuthenticationMethod.Custom);
		methods.remove(ArtAuthenticationMethod.Public);
		methods.remove(ArtAuthenticationMethod.Repository);

		return methods;
	}

	@ModelAttribute("ldapAuthenticationMethods")
	public LdapAuthenticationMethod[] addLdapAuthenticationMethods() {
		return LdapAuthenticationMethod.values();
	}

	@ModelAttribute("displayNullOptions")
	public DisplayNull[] addDisplayNullOptions() {
		return DisplayNull.values();
	}

	@RequestMapping(value = "settings", method = RequestMethod.GET)
	public String showSettings(Model model) {
		logger.debug("Entering showSettings");

		Settings settings = Config.getSettings();

		model.addAttribute("settings", settings);

		return showEditSettings(model);
	}

	@RequestMapping(value = "settings", method = RequestMethod.POST)
	public String processSettings(@ModelAttribute("settings") @Valid Settings settings,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering processSettings: settings={}", settings);

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditSettings(model);
		}

		//set password field as appropriate
		String newSmtpPassword = settings.getSmtpPassword();
		if (settings.isUseBlankSmtpPassword()) {
			newSmtpPassword = "";
		} else {
			if (StringUtils.isEmpty(newSmtpPassword)) {
				//use current password
				newSmtpPassword = Config.getSettings().getSmtpPassword();
			}
		}
		settings.setSmtpPassword(newSmtpPassword);

		String newLdapBindPassword = settings.getLdapBindPassword();
		if (settings.isUseBlankLdapBindPassword()) {
			newLdapBindPassword = "";
		} else {
			if (StringUtils.isEmpty(newLdapBindPassword)) {
				//use current password
				newLdapBindPassword = Config.getSettings().getLdapBindPassword();
			}
		}
		settings.setLdapBindPassword(newLdapBindPassword);

		try {
			//encrypt password fields
			settings.encryptPasswords();

			User sessionUser = (User) session.getAttribute("sessionUser");
			settingsService.updateSettings(settings, sessionUser);

			SettingsHelper settingsHelper = new SettingsHelper();
			settingsHelper.refreshSettings(session, servletContext);

			//use redirect after successful submission 
			redirectAttributes.addFlashAttribute("message", "settings.message.settingsSaved");
			return "redirect:/success";
		} catch (Exception ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSettings(model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param model the spring model
	 * @return the jsp file to display
	 */
	private String showEditSettings(Model model) {
		List<LoggerLevel> errorNotificationLevels = LoggerLevel.list();
		errorNotificationLevels.remove(LoggerLevel.OFF);
		errorNotificationLevels.remove(LoggerLevel.DEBUG);
		errorNotificationLevels.remove(LoggerLevel.INFO);

		model.addAttribute("errorNotificationLevels", errorNotificationLevels);

		try {
			model.addAttribute("datasources", datasourceService.getAllDatasources());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "settings";
	}

	@PostMapping("/updateEncryptionKey")
	public @ResponseBody
	AjaxResponse updateEncryptionKey(HttpSession session, Locale locale) {
		logger.debug("Entering updateEncryptionKey");

		AjaxResponse response = new AjaxResponse();

		Connection conn = null;
		boolean originalAutoCommit = false;
		ArtDatabase artDbConfig = Config.getArtDbConfig();
		String originalArtDbPassword = artDbConfig.getPassword();

		try {
			CustomSettings fileCustomSettings = Config.getCustomSettingsFromFile();
			if (fileCustomSettings == null) {
				response.setErrorMessage("Custom settings not available");
				return response;
			}

			String newPassword = null;
			int newKeyLength = 0;
			EncryptionPassword newEncryptionPasswordConfig = fileCustomSettings.getEncryptionPassword();
			if (newEncryptionPasswordConfig == null) {
				newEncryptionPasswordConfig = new EncryptionPassword();
			} else {
				newPassword = newEncryptionPasswordConfig.getPassword();
				newKeyLength = newEncryptionPasswordConfig.getKeyLength();
			}

			String currentPassword = null;
			int currentKeyLength = 0;
			EncryptionPassword currentEncryptionPasswordConfig = Config.getCustomSettings().getEncryptionPassword();
			if (currentEncryptionPasswordConfig != null) {
				currentPassword = currentEncryptionPasswordConfig.getPassword();
				currentKeyLength = currentEncryptionPasswordConfig.getKeyLength();
			}

			boolean passwordChange = false;
			if (!StringUtils.equals(newPassword, currentPassword)
					&& (StringUtils.isNotEmpty(newPassword) || StringUtils.isNotEmpty(currentPassword))) {
				passwordChange = true;
			}

			if (StringUtils.isNotEmpty(newPassword) && (newKeyLength != currentKeyLength)) {
				passwordChange = true;
			}

			String newEncryptionKey = fileCustomSettings.getEncryptionKey();

			if (StringUtils.isEmpty(newEncryptionKey)) {
				newEncryptionKey = AesEncryptor.DEFAULT_KEY;
			}

			String currentEncryptionKey = AesEncryptor.getCurrentEncryptionKey();

			boolean encryptionKeyChange = false;
			if (StringUtils.isNotEmpty(newPassword)
					&& !StringUtils.equals(newEncryptionKey, currentEncryptionKey)) {
				encryptionKeyChange = true;
			}

			if (!passwordChange && !encryptionKeyChange) {
				String message = messageSource.getMessage("settings.message.noChange", null, locale);
				response.setErrorMessage(message);
				return response;
			}

			Config.saveArtDatabaseConfiguration(artDbConfig, newEncryptionKey, newEncryptionPasswordConfig);
			artDbConfig.setPassword(originalArtDbPassword);
			DbConnections.createArtDbConnectionPool(artDbConfig);

			User sessionUser = (User) session.getAttribute("sessionUser");

			conn = DbConnections.getArtDbConnection();
			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			List<Datasource> datasources = datasourceService.getAllDatasources();
			for (Datasource datasource : datasources) {
				if (!datasource.hasNullPassword()) {
					String originalPassword = datasource.getPassword();
					datasource.encryptPassword(newEncryptionKey, newEncryptionPasswordConfig);
					datasourceService.updateDatasource(datasource, sessionUser, conn);
					datasource.setPassword(originalPassword);
					if (datasource.isActive()) {
						DbConnections.createDatasourceConnectionPool(datasource, artDbConfig.getMaxPoolConnections(), artDbConfig.getConnectionPoolLibrary());
					}
				}
			}

			List<Destination> destinations = destinationService.getAllDestinations();
			for (Destination destination : destinations) {
				if (!destination.hasNullPassword()) {
					destination.encryptPassword(newEncryptionKey, newEncryptionPasswordConfig);
					destinationService.updateDestination(destination, sessionUser, conn);
				}
			}

			List<Encryptor> encryptors = encryptorService.getAllEncryptors();
			for (Encryptor encryptor : encryptors) {
				if (!encryptor.hasNullPasswords()) {
					encryptor.encryptPasswords(newEncryptionKey, newEncryptionPasswordConfig);
					encryptorService.updateEncryptor(encryptor, sessionUser, conn);
				}
			}

			List<Report> reports = reportService.getAllReports();
			for (Report report : reports) {
				if (!report.hasNullPasswords()) {
					report.encryptPasswords(newEncryptionKey, newEncryptionPasswordConfig);
					reportService.updateReport(report, sessionUser, conn);
				}
			}

			Settings settings = settingsService.getSettings();
			if (!settings.hasNullPasswords()) {
				settings.encryptPasswords(newEncryptionKey, newEncryptionPasswordConfig);
				settingsService.updateSettings(settings, sessionUser, conn);
			}

			List<SmtpServer> smtpServers = smtpServerService.getAllSmtpServers();
			for (SmtpServer smtpServer : smtpServers) {
				if (!smtpServer.hasNullPasswords()) {
					smtpServer.encryptPasswords(newEncryptionKey, newEncryptionPasswordConfig);
					smtpServerService.updateSmtpServer(smtpServer, sessionUser, conn);
				}
			}

			conn.commit();
			CustomSettings customSettings = Config.getCustomSettings();
			customSettings.setEncryptionKey(newEncryptionKey);
			customSettings.setEncryptionPassword(newEncryptionPasswordConfig);
			response.setSuccess(true);
		} catch (Exception ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());

			try {
				artDbConfig.setPassword(originalArtDbPassword);
				Config.saveArtDatabaseConfiguration(artDbConfig);
			} catch (Exception ex2) {
				logger.error("Error", ex2);
			}

			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex2) {
					logger.error("Error", ex2);
				}
			}
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(originalAutoCommit);
				} catch (SQLException ex) {
					logger.error("Error", ex);
				}
			}
			DatabaseUtils.close(conn);
		}

		return response;
	}

}
