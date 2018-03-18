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

import art.utils.ApiHelper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller to display error page for unhandled exceptions at the servlet
 * container level. That is exceptions that aren't caught by the spring
 * framework i.e that aren't generated within spring controllers.
 *
 * @author Timothy Anyona
 */
@Controller
public class ErrorController {
	//see http://blog.teamextension.com/ajax-friendly-simplemappingexceptionresolver-1057

	private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

	@ModelAttribute
	public void addStatusCodeAndUri(HttpServletRequest request, Model model) {
		//http://www.baeldung.com/spring-mvc-and-the-modelattribute-annotation
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
		String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
		if (errorMessage == null) {
			errorMessage = "";
		}

		model.addAttribute("statusCode", statusCode);
		model.addAttribute("requestUri", requestUri);
		model.addAttribute("errorMessage", errorMessage);

		boolean isApi = false;
		try {
			URI uri = new URI(requestUri);
			String path = uri.getPath();
			String contextPath = request.getContextPath();
			String pathMinusContext = StringUtils.substringAfter(path, contextPath);

			if (StringUtils.startsWith(pathMinusContext, "/api")) {
				isApi = true;
			}
		} catch (URISyntaxException ex) {
			logger.error("Error", ex);
		}
		model.addAttribute("isApi", isApi);
	}

	@RequestMapping(value = "/error")
	public String showError(HttpServletRequest request, Model model,
			HttpServletResponse response, @ModelAttribute("isApi") Boolean isApi) {

		//https://stackoverflow.com/questions/3553294/ideal-error-page-for-java-ee-app
		//http://www.tutorialspoint.com/jsp/jsp_exception_handling.htm
		Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
		model.addAttribute("exception", exception);
		logger.error("Error", exception);

		String errorDetails;

		if (exception != null) {
			if (exception instanceof ServletException) {
				// It's a ServletException: we should extract the root cause
				ServletException se = (ServletException) exception;
				Throwable rootCause = se.getRootCause();
				if (rootCause == null) {
					rootCause = se;
				}
				errorDetails = ExceptionUtils.getStackTrace(rootCause);
			} else {
				// It's not a ServletException, so we'll just show it
				errorDetails = ExceptionUtils.getStackTrace(exception);
			}
		} else {
			errorDetails = "No error information available";
		}
		model.addAttribute("errorDetails", errorDetails);

		if (isApi) {
			return null;
		} else if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			//don't return whole html page for ajax calls. Only error details
			return "error-inline";
		} else {
			return "error";
		}
	}

	@RequestMapping(value = "/error-404")
	public String showError404(HttpServletRequest request,
			HttpServletResponse response, @ModelAttribute("isApi") Boolean isApi) {

		if (isApi) {
			return null;
		} else if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			//don't return whole html page for ajax calls. Only error details
			return "error-404-inline";
		} else {
			return "error-404";
		}
	}

	@RequestMapping(value = "/error-400")
	public String showError400(HttpServletRequest request, Model model,
			HttpServletResponse response, @ModelAttribute("isApi") Boolean isApi,
			@ModelAttribute("errorMessage") String errorMessage) {

		if (isApi) {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage(errorMessage);

			try {
				ApiHelper.outputApiResponse(apiResponse, response);
			} catch (IOException ex) {
				logger.error("Error", ex);
			}
			return null;
		} else if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			//don't return whole html page for ajax calls. Only error details
			return "error-400-inline";
		} else {
			return "error-400";
		}
	}

	@RequestMapping(value = "/error-405")
	public String showError405(HttpServletRequest request, Model model,
			HttpServletResponse response, @ModelAttribute("isApi") Boolean isApi,
			@ModelAttribute("errorMessage") String errorMessage) {

		if (isApi) {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMessage(errorMessage);

			try {
				ApiHelper.outputApiResponse(apiResponse, response);
			} catch (IOException ex) {
				logger.error("Error", ex);
			}
			return null;
		} else if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			//don't return whole html page for ajax calls. Only error details
			return "error-405-inline";
		} else {
			return "error-405";
		}
	}

	@RequestMapping(value = "/error-500")
	public String showError500(HttpServletRequest request, Model model,
			HttpServletResponse response, @ModelAttribute("isApi") Boolean isApi) {

		if (isApi) {
			return null;
		} else if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			//don't return whole html page for ajax calls. Only error details
			return "error-500-inline";
		} else {
			return "error-500";
		}
	}
}
