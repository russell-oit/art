/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.logger;

import art.enums.LoggerLevel;
import art.servlets.Config;
import art.utils.AjaxResponse;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for logger configuration
 *
 * @author Timothy
 */
@Controller
public class LoggerController {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LoggerController.class);

	@RequestMapping(value = "/app/loggers", method = RequestMethod.GET)
	public String showLoggers(Model model) {
		logger.debug("Entering showLoggers");

		if (Config.getCustomSettings().isShowErrors()) {
			//get only loggers configured in logback.xml
			//http://mailman.qos.ch/pipermail/logback-user/2008-November/000751.html
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			List<ch.qos.logback.classic.Logger> logList = new ArrayList<>();
			for (ch.qos.logback.classic.Logger log : lc.getLoggerList()) {
				if (log.getLevel() != null || hasAppenders(log)) {
					logList.add(log);
				}
			}
			model.addAttribute("loggers", logList);
		}

		return "loggers";
	}

	/**
	 * Returns <code>true</code> if the given logback logger has appenders
	 * 
	 * @param log the logback logger
	 * @return <code>true</code> if the given logback logger has appenders
	 */
	private boolean hasAppenders(ch.qos.logback.classic.Logger log) {
		Iterator<Appender<ILoggingEvent>> it = log.iteratorForAppenders();
		return it.hasNext();
	}

	@RequestMapping(value = "/app/disableLogger", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse disableLogger(@RequestParam("name") String name) {
		logger.debug("Entering disableLogger: name='{}'", name);

		AjaxResponse response = new AjaxResponse();

		ch.qos.logback.classic.Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name);
		log.setLevel(Level.OFF);

		response.setSuccess(true);

		return response;
	}

	@RequestMapping(value = "/app/addLogger", method = RequestMethod.GET)
	public String addLogger(Model model) {
		logger.debug("Entering addLogger");

		model.addAttribute("log", new art.logger.Logger());
		
		return showEditLogger("add", model);
	}

	@RequestMapping(value = "/app/saveLogger", method = RequestMethod.POST)
	public String saveLogger(@ModelAttribute("log") @Valid art.logger.Logger log,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveLogger: log={}, action='{}'", log, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditLogger(action, model);
		}

		//create or edit logger
		logger.debug("(log.getLevel()={}", log.getLevel());
		if (log.getLevel() != null) {
			logger.debug("log.getName()={}", log.getName());
			ch.qos.logback.classic.Logger newLog = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(log.getName()); //this creates logger if it didn't exist
			newLog.setLevel(Level.toLevel(log.getLevel().getValue()));
		}

		if (StringUtils.equals(action, "add")) {
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
		} else if (StringUtils.equals(action, "edit")) {
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
		}
		redirectAttributes.addFlashAttribute("recordName", log.getName());
		return "redirect:/app/loggers.do";
	}

	@RequestMapping(value = "/app/editLogger", method = RequestMethod.GET)
	public String editLogger(@RequestParam("name") String name, Model model) {
		logger.debug("Entering editLogger: name='{}'", name);

		art.logger.Logger log = new art.logger.Logger();
		log.setName(name);

		//get logger level
		ch.qos.logback.classic.Logger editLog = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(name);
		logger.debug("(editLog.getLevel() != null) = {}", editLog.getLevel() != null);
		if (editLog.getLevel() != null) {
			log.setLevel(LoggerLevel.toEnum(editLog.getLevel().toString()));
		}

		model.addAttribute("log", log);
		
		return showEditLogger("edit", model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to take
	 * @param model the model
	 * @return the jsp file to display
	 */
	private String showEditLogger(String action, Model model) {
		logger.debug("Entering showEditLogger: action='{}'", action);

		model.addAttribute("levels", LoggerLevel.list());
		model.addAttribute("action", action);
		
		return "editLogger";
	}
}
