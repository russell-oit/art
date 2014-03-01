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
package art.common;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to display error page for unhandled exceptions at the servlet
 * container level. That is exceptions that aren't caught by the spring
 * framework i.e that aren't generated within spring controllers
 *
 * @author Timothy Anyona
 */
@Controller
public class ErrorController {
	//see http://blog.teamextension.com/ajax-friendly-simplemappingexceptionresolver-1057

	private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

	@RequestMapping(value = "/error")
	public String showError(HttpServletRequest request) {
		Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");

		logger.error("Unexpected error", exception);

		if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			//don't return whole html page for ajax calls. Only error details
			return "error-ajax";
		} else {
			return "error";
		}
	}

}
