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

import art.enums.ArtAuthenticationMethod;
import art.enums.DisplayNull;
import art.enums.LdapAuthenticationMethod;
import art.enums.LdapConnectionEncryptionMethod;
import art.enums.PdfPageSize;
import art.servlets.Config;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
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
		
		return "settings";
	}

	@RequestMapping(value = "settings", method = RequestMethod.POST)
	public String processSettings(@ModelAttribute("settings") @Valid Settings settings,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering processSettings: settings={}", settings);

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return "settings";
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
			Config.saveSettings(settings);

			session.setAttribute("administratorEmail", settings.getAdministratorEmail());
			session.setAttribute("casLogoutUrl", settings.getCasLogoutUrl());

			String dateDisplayPattern = settings.getDateFormat() + " " + settings.getTimeFormat();
			servletContext.setAttribute("dateDisplayPattern", dateDisplayPattern); //format of dates displayed in tables

			//use redirect after successful submission 
			redirectAttributes.addFlashAttribute("message", "settings.message.settingsSaved");
			return "redirect:/success";
		} catch (IOException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "settings";
	}
}
