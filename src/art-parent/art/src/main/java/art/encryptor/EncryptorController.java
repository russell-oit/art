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
package art.encryptor;

import art.encryption.AesEncryptor;
import art.enums.EncryptorType;
import art.report.UploadHelper;
import art.servlets.Config;
import art.user.User;
import art.utils.AjaxResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for encryptor configuration pages
 *
 * @author Timothy Anyona
 */
@Controller
public class EncryptorController {

	private static final Logger logger = LoggerFactory.getLogger(EncryptorController.class);

	@Autowired
	private EncryptorService encryptorService;

	@RequestMapping(value = "/encryptors", method = RequestMethod.GET)
	public String showEncryptors(Model model) {
		logger.debug("Entering showEncryptors");

		try {
			model.addAttribute("encryptors", encryptorService.getAllEncryptors());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "encryptors";
	}

	@RequestMapping(value = "/deleteEncryptor", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteEncryptor(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteEncryptor: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			encryptorService.deleteEncryptor(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteEncryptors", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteEncryptors(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteEncryptors: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			encryptorService.deleteEncryptors(ids);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addEncryptor", method = RequestMethod.GET)
	public String addEncryptor(Model model) {
		logger.debug("Entering addEncryptor");

		Encryptor encryptor = new Encryptor();

		model.addAttribute("encryptor", encryptor);

		return showEditEncryptor("add", model);
	}

	@RequestMapping(value = "/editEncryptor", method = RequestMethod.GET)
	public String editEncryptor(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editEncryptor: id={}", id);

		try {
			model.addAttribute("encryptor", encryptorService.getEncryptor(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditEncryptor("edit", model);
	}

	@RequestMapping(value = "/editEncryptors", method = RequestMethod.GET)
	public String editEncryptors(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editEncryptors: ids={}", ids);

		MultipleEncryptorEdit multipleEncryptorEdit = new MultipleEncryptorEdit();
		multipleEncryptorEdit.setIds(ids);

		model.addAttribute("multipleEncryptorEdit", multipleEncryptorEdit);

		return "editEncryptors";
	}

	@RequestMapping(value = "/saveEncryptor", method = RequestMethod.POST)
	public String saveEncryptor(@ModelAttribute("encryptor") @Valid Encryptor encryptor,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			@RequestParam(value = "publicKeyFile", required = false) MultipartFile publicKeyFile,
			@RequestParam(value = "signingKeyFile", required = false) MultipartFile signingKeyFile,
			HttpSession session) {

		logger.debug("Entering saveEncryptor: encryptor={}, action='{}'", encryptor, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditEncryptor(action, model);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPasswords(encryptor, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showEditEncryptor(action, model);
			}

			//save files
			String saveFilesMessage = saveFiles(encryptor, publicKeyFile, signingKeyFile);
			logger.debug("saveFilesMessage='{}'", saveFilesMessage);
			if (saveFilesMessage != null) {
				model.addAttribute("message", saveFilesMessage);
				return showEditEncryptor(action, model);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equals(action, "add")) {
				encryptorService.addEncryptor(encryptor, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				encryptorService.updateEncryptor(encryptor, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			String recordName = encryptor.getName() + " (" + encryptor.getEncryptorId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/encryptors";
		} catch (SQLException | RuntimeException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditEncryptor(action, model);
	}

	@RequestMapping(value = "/saveEncryptors", method = RequestMethod.POST)
	public String saveEncryptors(@ModelAttribute("multipleEncryptorEdit") @Valid MultipleEncryptorEdit multipleEncryptorEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveEncryptors: multipleEncryptorEdit={}", multipleEncryptorEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditEncryptors();
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			encryptorService.updateEncryptors(multipleEncryptorEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleEncryptorEdit.getIds());
			return "redirect:/encryptors";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditEncryptors();
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to use
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditEncryptor(String action, Model model) {
		logger.debug("Entering showEncryptor: action='{}'", action);

		model.addAttribute("encryptorTypes", EncryptorType.list());
		model.addAttribute("action", action);

		return "editEncryptor";
	}

	/**
	 * Prepares model data and returns jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showEditEncryptors() {
		logger.debug("Entering showEditEncryptors");

		return "editEncryptors";
	}

	/**
	 * Sets the password fields of the encryptor
	 *
	 * @param encryptor the encryptor object to set
	 * @param action "add or "edit"
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setPasswords(Encryptor encryptor, String action) throws SQLException {
		logger.debug("Entering setPasswords: encryptor={}, action='{}'", encryptor, action);

		//set the aes crypt password
		boolean useCurrentAesCryptPassword = false;
		String newAesCryptPassword = encryptor.getAesCryptPassword();

		if (StringUtils.isEmpty(newAesCryptPassword) && StringUtils.equals(action, "edit")) {
			//password field blank. use current password
			useCurrentAesCryptPassword = true;
		}

		if (useCurrentAesCryptPassword) {
			//password field blank. use current password
			Encryptor currentEncryptor = encryptorService.getEncryptor(encryptor.getEncryptorId());
			if (currentEncryptor == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				newAesCryptPassword = currentEncryptor.getAesCryptPassword();
			}
		} else {
			EncryptorType encryptorType = encryptor.getEncryptorType();
			if (encryptorType == EncryptorType.AESCrypt && StringUtils.isEmpty(newAesCryptPassword)) {
				return "encryptors.message.passwordMustNotBeEmpty";
			}
		}

		//encrypt new password
		String encryptedAesCryptPassword = AesEncryptor.encrypt(newAesCryptPassword);
		encryptor.setAesCryptPassword(encryptedAesCryptPassword);

		//set the signing key passphrase
		boolean useCurrentSigningKeyPassphrase = false;
		String newSigningKeyPassphrase = encryptor.getOpenPgpSigningKeyPassphrase();

		if (StringUtils.isEmpty(newSigningKeyPassphrase) && StringUtils.equals(action, "edit")) {
			//password field blank. use current password
			useCurrentSigningKeyPassphrase = true;
		}

		if (useCurrentSigningKeyPassphrase) {
			//password field blank. use current password
			Encryptor currentEncryptor = encryptorService.getEncryptor(encryptor.getEncryptorId());
			if (currentEncryptor == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				newSigningKeyPassphrase = currentEncryptor.getOpenPgpSigningKeyPassphrase();
			}
		}

		EncryptorType encryptorType = encryptor.getEncryptorType();
		if (encryptorType == EncryptorType.OpenPGP
				&& StringUtils.isNoneBlank(encryptor.getOpenPgpSigningKeyFile())
				&& StringUtils.isEmpty(newSigningKeyPassphrase)) {
			return "encryptors.message.passwordMustNotBeEmpty";
		}

		//encrypt new passphrase
		String encryptedSigningKeyPassphrase = AesEncryptor.encrypt(newSigningKeyPassphrase);
		encryptor.setOpenPgpSigningKeyPassphrase(encryptedSigningKeyPassphrase);

		return null;
	}

	/**
	 * Saves an openpgp public key file and updates the appropriate encryptor
	 * property with the file name
	 *
	 * @param file the file to save
	 * @param encryptor the encryptor object to set
	 * @return an i18n message string if there was a problem, otherwise null
	 * @throws IOException
	 */
	private String savePublicKeyFile(MultipartFile file, Encryptor encryptor)
			throws IOException {

		logger.debug("Entering savePublicKeyFile: encryptor={}", encryptor);

		logger.debug("file==null = {}", file == null);
		if (file == null) {
			return null;
		}

		logger.debug("file.isEmpty()={}", file.isEmpty());
		if (file.isEmpty()) {
			//can be empty if a file name is just typed
			//or if upload a 0 byte file
			//don't show message in case of file name being typed
			return null;
		}

		//set allowed upload file types
		List<String> validExtensions = new ArrayList<>();
		validExtensions.add("asc");
		validExtensions.add("gpg");

		//save file
		String templatesPath = Config.getTemplatesPath();
		UploadHelper uploadHelper = new UploadHelper();
		String message = uploadHelper.saveFile(file, templatesPath, validExtensions);

		if (message != null) {
			return message;
		}

		if (encryptor != null) {
			String filename = file.getOriginalFilename();
			encryptor.setOpenPgpPublicKeyFile(filename);
		}

		return null;
	}

	/**
	 * Saves an openpgp signing key file and updates the appropriate encryptor
	 * property with the file name
	 *
	 * @param file the file to save
	 * @param encryptor the encryptor object to set
	 * @return an i18n message string if there was a problem, otherwise null
	 * @throws IOException
	 */
	private String saveSigningKeyFile(MultipartFile file, Encryptor encryptor)
			throws IOException {

		logger.debug("Entering saveSigningKeyFile: encryptor={}", encryptor);

		logger.debug("file==null = {}", file == null);
		if (file == null) {
			return null;
		}

		logger.debug("file.isEmpty()={}", file.isEmpty());
		if (file.isEmpty()) {
			//can be empty if a file name is just typed
			//or if upload a 0 byte file
			//don't show message in case of file name being typed
			return null;
		}

		//set allowed upload file types
		List<String> validExtensions = new ArrayList<>();
		validExtensions.add("asc");
		validExtensions.add("gpg");
		validExtensions.add("key");

		//save file
		String templatesPath = Config.getTemplatesPath();
		UploadHelper uploadHelper = new UploadHelper();
		String message = uploadHelper.saveFile(file, templatesPath, validExtensions);

		if (message != null) {
			return message;
		}

		if (encryptor != null) {
			String filename = file.getOriginalFilename();
			encryptor.setOpenPgpSigningKeyFile(filename);
		}

		return null;
	}

	/**
	 * Saves openpgp key files and sets the appropriate encryptor properties
	 *
	 * @param encryptor the encryptor to use
	 * @param publicKeyFile the public key file
	 * @param signingKeyFile the signing key file
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws IOException
	 * @throws SQLException
	 */
	public String saveFiles(Encryptor encryptor, MultipartFile publicKeyFile,
			MultipartFile signingKeyFile) throws IOException, SQLException {

		logger.debug("Entering saveFiles: encryptor={}", encryptor);

		String message;

		message = savePublicKeyFile(publicKeyFile, encryptor);
		if (message != null) {
			return message;
		}

		message = saveSigningKeyFile(signingKeyFile, encryptor);
		if (message != null) {
			return message;
		}

		return null;
	}

}
