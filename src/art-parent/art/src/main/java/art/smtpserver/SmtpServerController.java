/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
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
package art.smtpserver;

import art.encryption.AesEncryptor;
import art.job.JobService;
import art.user.User;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for smtp server configuration pages
 *
 * @author Timothy Anyona
 */
@Controller
public class SmtpServerController {

	private static final Logger logger = LoggerFactory.getLogger(SmtpServerController.class);

	@Autowired
	private SmtpServerService smtpServerService;
	
	@Autowired
	private JobService jobService;

	@RequestMapping(value = "/smtpServers", method = RequestMethod.GET)
	public String showSmtpServers(Model model) {
		logger.debug("Entering showSmtpServers");

		try {
			model.addAttribute("smtpServers", smtpServerService.getAllSmtpServers());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "smtpServers";
	}

	@RequestMapping(value = "/deleteSmtpServer", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteSmtpServer(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteSmtpServer: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = smtpServerService.deleteSmtpServer(id);
			
			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				//smtp server not deleted because of linked jobs
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteSmtpServers", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteSmtpServers(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteSmtpServers: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = smtpServerService.deleteSmtpServers(ids);
			
			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
			} else {
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addSmtpServer", method = RequestMethod.GET)
	public String addSmtpServer(Model model) {
		logger.debug("Entering addSmtpServer");

		SmtpServer smtpServer = new SmtpServer();

		model.addAttribute("smtpServer", smtpServer);

		return showEditSmtpServer("add", model);
	}

	@RequestMapping(value = "/editSmtpServer", method = RequestMethod.GET)
	public String editSmtpServer(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editSmtpServer: id={}", id);

		try {
			model.addAttribute("smtpServer", smtpServerService.getSmtpServer(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSmtpServer("edit", model);
	}
	
	@RequestMapping(value = "/copySmtpServer", method = RequestMethod.GET)
	public String copySmtpServer(@RequestParam("id") Integer id, Model model) {

		logger.debug("Entering copySmtpServer: id={}", id);

		try {
			model.addAttribute("smtpServer", smtpServerService.getSmtpServer(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSmtpServer("copy", model);
	}

	@RequestMapping(value = "/editSmtpServers", method = RequestMethod.GET)
	public String editSmtpServers(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editSmtpServers: ids={}", ids);

		MultipleSmtpServerEdit multipleSmtpServerEdit = new MultipleSmtpServerEdit();
		multipleSmtpServerEdit.setIds(ids);

		model.addAttribute("multipleSmtpServerEdit", multipleSmtpServerEdit);

		return "editSmtpServers";
	}

	@RequestMapping(value = "/saveSmtpServer", method = RequestMethod.POST)
	public String saveSmtpServer(@ModelAttribute("smtpServer") @Valid SmtpServer smtpServer,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveSmtpServer: smtpServer={}, action='{}'", smtpServer, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditSmtpServer(action, model);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPassword(smtpServer, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showEditSmtpServer(action, model);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equalsAny(action, "add", "copy")) {
				smtpServerService.addSmtpServer(smtpServer, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				smtpServerService.updateSmtpServer(smtpServer, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			
			String recordName = smtpServer.getName() + " (" + smtpServer.getSmtpServerId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/smtpServers";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSmtpServer(action, model);
	}
	
	@RequestMapping(value = "/saveSmtpServers", method = RequestMethod.POST)
	public String saveSmtpServers(@ModelAttribute("multipleSmtpServerEdit") @Valid MultipleSmtpServerEdit multipleSmtpServerEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveSmtpServers: multipleSmtpServerEdit={}", multipleSmtpServerEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditSmtpServers();
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			smtpServerService.updateSmtpServers(multipleSmtpServerEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleSmtpServerEdit.getIds());
			return "redirect:/smtpServers";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSmtpServers();
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to use
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditSmtpServer(String action, Model model) {
		logger.debug("Entering showSmtpServer: action='{}'", action);

		model.addAttribute("action", action);

		return "editSmtpServer";
	}
	
	/**
	 * Prepares model data and returns jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showEditSmtpServers() {
		logger.debug("Entering showEditSmtpServers");
		
		return "editSmtpServers";
	}

	/**
	 * Sets the password field of the smtp server
	 *
	 * @param smtpServer the smtp server object to set
	 * @param action "add or "edit"
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setPassword(SmtpServer smtpServer, String action) throws SQLException {
		logger.debug("Entering setPassword: smtpServer={}, action='{}'", smtpServer, action);

		boolean useCurrentPassword = false;
		String newPassword = smtpServer.getPassword();

		if (smtpServer.isUseBlankPassword()) {
			newPassword = "";
		} else {
			if (StringUtils.isEmpty(newPassword) && StringUtils.equalsAny(action, "edit", "copy")) {
				//password field blank. use current password
				useCurrentPassword = true;
			}
		}

		if (useCurrentPassword) {
			//password field blank. use current password
			SmtpServer currentSmtpServer = smtpServerService.getSmtpServer(smtpServer.getSmtpServerId());
			if (currentSmtpServer == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				newPassword = currentSmtpServer.getPassword();
			}
		}

		//encrypt new password
		String encryptedPassword = AesEncryptor.encrypt(newPassword);
		smtpServer.setPassword(encryptedPassword);

		return null;
	}
	
	@RequestMapping(value = "/jobsWithSmtpServer", method = RequestMethod.GET)
	public String showJobsWithSmtpServer(@RequestParam("smtpServerId") Integer smtpServerId, Model model) {
		logger.debug("Entering showJobsWithSmtpServer: smtpServerId={}", smtpServerId);

		try {
			model.addAttribute("jobs", jobService.getJobsWithSmtpServer(smtpServerId));
			model.addAttribute("smtpServer", smtpServerService.getSmtpServer(smtpServerId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "jobsWithSmtpServer";
	}

}
