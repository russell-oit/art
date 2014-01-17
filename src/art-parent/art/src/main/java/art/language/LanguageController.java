/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.language;

import art.utils.LanguageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring controller for setting application language
 *
 * @author Timothy Anyona
 */
@Controller
public class LanguageController {

	final static Logger logger = LoggerFactory.getLogger(LanguageController.class);

	@RequestMapping(value = "/app/language", method = RequestMethod.GET)
	public String showLanguage(Model model) {
		model.addAttribute("languages", LanguageUtils.getLanguages());
		return "language";
	}

	@RequestMapping(value = "/app/language", method = RequestMethod.POST)
	public String showLanguage(RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("success", "true");
		return "redirect:/app/language.do";
	}

}
