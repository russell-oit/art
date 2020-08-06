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
package art.general;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for simple pages that don't have much logic
 *
 * @author Timothy Anyona
 */
@Controller
public class GeneralController {

	@RequestMapping(value = "/accessDenied", method = {RequestMethod.GET, RequestMethod.POST})
	public String showAccessDenied(HttpServletRequest request) {
		if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			//ajax
			return "accessDeniedInline";
		} else {
			return "accessDenied";
		}
	}

	@RequestMapping(value = "/success", method = RequestMethod.GET)
	public String showSuccess() {
		return "success";
	}

	@RequestMapping(value = "/reportError", method = RequestMethod.GET)
	public String showReportError() {
		return "reportError";
	}

}
