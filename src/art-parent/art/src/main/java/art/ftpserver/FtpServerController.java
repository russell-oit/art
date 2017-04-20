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
package art.ftpserver;

import art.encryption.AesEncryptor;
import art.user.User;
import art.utils.AjaxResponse;
import java.sql.SQLException;
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
 * Controller for ftp server configuration pages
 *
 * @author Timothy Anyona
 */
@Controller
public class FtpServerController {

	private static final Logger logger = LoggerFactory.getLogger(FtpServerController.class);

	@Autowired
	private FtpServerService ftpServerService;

	@RequestMapping(value = "/ftpServers", method = RequestMethod.GET)
	public String showFtpServers(Model model) {
		logger.debug("Entering showFtpServers");

		try {
			model.addAttribute("ftpServers", ftpServerService.getAllFtpServers());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "ftpServers";
	}

	@RequestMapping(value = "/deleteFtpServer", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteFtpServer(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteFtpServer: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ftpServerService.deleteFtpServer(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteFtpServers", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteFtpServers(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteFtpServers: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ftpServerService.deleteFtpServers(ids);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addFtpServer", method = RequestMethod.GET)
	public String addFtpServer(Model model) {
		logger.debug("Entering addFtpServer");

		FtpServer ftpServer = new FtpServer();
		ftpServer.setActive(true);

		model.addAttribute("ftpServer", ftpServer);

		return showEditFtpServer("add", model);
	}

	@RequestMapping(value = "/editFtpServer", method = RequestMethod.GET)
	public String editFtpServer(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editFtpServer: id={}", id);

		try {
			model.addAttribute("ftpServer", ftpServerService.getFtpServer(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditFtpServer("edit", model);
	}

	@RequestMapping(value = "/editFtpServers", method = RequestMethod.GET)
	public String editFtpServers(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editFtpServers: ids={}", ids);

		MultipleFtpServerEdit multipleFtpServerEdit = new MultipleFtpServerEdit();
		multipleFtpServerEdit.setIds(ids);

		model.addAttribute("multipleFtpServerEdit", multipleFtpServerEdit);

		return "editFtpServers";
	}

	@RequestMapping(value = "/saveFtpServer", method = RequestMethod.POST)
	public String saveFtpServer(@ModelAttribute("ftpServer") @Valid FtpServer ftpServer,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveFtpServer: ftpServer={}, action='{}'", ftpServer, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditFtpServer(action, model);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPassword(ftpServer, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showEditFtpServer(action, model);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equals(action, "add")) {
				ftpServerService.addFtpServer(ftpServer, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				ftpServerService.updateFtpServer(ftpServer, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			
			String recordName = ftpServer.getName() + " (" + ftpServer.getFtpServerId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/ftpServers";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditFtpServer(action, model);
	}
	
	@RequestMapping(value = "/saveFtpServers", method = RequestMethod.POST)
	public String saveFtpServers(@ModelAttribute("multipleFtpServerEdit") @Valid MultipleFtpServerEdit multipleFtpServerEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveFtpServers: multipleFtpServerEdit={}", multipleFtpServerEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditFtpServers();
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			ftpServerService.updateFtpServers(multipleFtpServerEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleFtpServerEdit.getIds());
			return "redirect:/ftpServers";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditFtpServers();
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to use
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditFtpServer(String action, Model model) {
		logger.debug("Entering showFtpServer: action='{}'", action);

		model.addAttribute("action", action);

		return "editFtpServer";
	}
	
	/**
	 * Prepares model data and returns jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showEditFtpServers() {
		logger.debug("Entering showEditFtpServers");
		
		return "editFtpServers";
	}

	/**
	 * Sets the password field of the ftp server
	 *
	 * @param ftpServer the ftp server object to set
	 * @param action "add or "edit"
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setPassword(FtpServer ftpServer, String action) throws SQLException {
		logger.debug("Entering setPassword: ftpServer={}, action='{}'", ftpServer, action);

		boolean useCurrentPassword = false;
		String newPassword = ftpServer.getPassword();

		if (ftpServer.isUseBlankPassword()) {
			newPassword = "";
		} else {
			if (StringUtils.isEmpty(newPassword) && StringUtils.equals(action, "edit")) {
				//password field blank. use current password
				useCurrentPassword = true;
			}
		}

		if (useCurrentPassword) {
			//password field blank. use current password
			FtpServer currentFtpServer = ftpServerService.getFtpServer(ftpServer.getFtpServerId());
			if (currentFtpServer == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				newPassword = currentFtpServer.getPassword();
			}
		}

		//encrypt new password
		String encryptedPassword = AesEncryptor.encrypt(newPassword);
		ftpServer.setPassword(encryptedPassword);

		return null;
	}

}
