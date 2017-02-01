/*
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for simple pages that don't have much logic
 *
 * @author Timothy Anyona
 */
@Controller
public class CommonController {

	@RequestMapping(value = "/accessDenied", method = {RequestMethod.GET, RequestMethod.POST})
	public String showAccessDenied() {
		return "accessDenied";
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
