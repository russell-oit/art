/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.language;

import art.servlets.Config;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for setting application language
 *
 * @author Timothy Anyona
 */
@Controller
public class LanguageController {

	@RequestMapping(value = "/language", method = RequestMethod.GET)
	public String showLanguage(Model model) {
		model.addAttribute("languages", Config.getLanguages());
		return "language";
	}

	@RequestMapping(value = "/language", method = RequestMethod.POST)
	public String processLanguage(RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("message", "language.message.languageUpdated");
		return "redirect:/success";
	}
}
