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
package art.datasource;

import art.dbutils.DbUtils;
import art.servlets.ArtConfig;
import art.utils.AjaxResponse;
import art.utils.ArtUtils;
import art.utils.Encrypter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
 * Controller for the datasource configuration process
 *
 * @author Timothy Anyona
 */
@Controller
public class DatasourceController {

	private static final Logger logger = LoggerFactory.getLogger(DatasourceController.class);

	@Autowired
	private DatasourceService datasourceService;

	@Autowired
	private MessageSource messageSource;

	@RequestMapping(value = "/app/datasources", method = RequestMethod.GET)
	public String showDatasources(Model model) {
		logger.debug("Entering showDatasources");

		try {
			model.addAttribute("datasources", datasourceService.getAllDatasources());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "datasources";
	}

	@RequestMapping(value = "/app/deleteDatasource", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteDatasource(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteDatasource: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			List<String> linkedReports = new ArrayList<>();
			int count = datasourceService.deleteDatasource(id, linkedReports);
			logger.debug("count={}", count);
			if (count == -1) {
				//datasource not deleted because of linked records
				response.setData(linkedReports);
			} else {
				response.setSuccess(true);
				ArtConfig.refreshConnections();
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addDatasource", method = RequestMethod.GET)
	public String addDatasource(Model model) {
		logger.debug("Entering addDatasource");

		Datasource datasource = new Datasource();

		//set defaults
		datasource.setActive(true);
		datasource.setConnectionPoolTimeout(20);

		model.addAttribute("datasource", datasource);
		return showDatasource("add", model);
	}

	@RequestMapping(value = "/app/saveDatasource", method = RequestMethod.POST)
	public String saveDatasource(@ModelAttribute("datasource")
			@Valid Datasource datasource, @RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		logger.debug("Entering saveDatasource: datasource={}, action='{}'", datasource, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showDatasource(action, model);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPassword(datasource, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showDatasource(action, model);
			}

			if (StringUtils.equals(action, "add")) {
				datasourceService.addDatasource(datasource);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				datasourceService.updateDatasource(datasource);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			try {
				updateConnection(datasource);
			} catch (Exception ex) {
				logger.error("Error", ex);
				redirectAttributes.addFlashAttribute("error", ex);
			}

			redirectAttributes.addFlashAttribute("recordName", datasource.getName());
			return "redirect:/app/datasources.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showDatasource(action, model);
	}

	@RequestMapping(value = "/app/editDatasource", method = RequestMethod.GET)
	public String editDatasource(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editDatasource: id={}", id);

		try {
			model.addAttribute("datasource", datasourceService.getDatasource(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showDatasource("edit", model);
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showDatasource(String action, Model model) {
		logger.debug("Entering showDatasource: action='{}'", action);

		Map<String, String> databaseTypes = ArtUtils.getDatabaseTypes();
		databaseTypes.remove("demo");

		model.addAttribute("databaseTypes", databaseTypes);
		model.addAttribute("action", action);
		return "editDatasource";
	}

	@RequestMapping(value = "/app/testDatasource", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse testDatasource(@RequestParam("id") Integer id,
			@RequestParam("jndi") Boolean jndi,
			@RequestParam("driver") String driver, @RequestParam("url") String url,
			@RequestParam("username") String username, @RequestParam("password") String password,
			@RequestParam("useBlankPassword") Boolean useBlankPassword,
			Locale locale) {

		logger.debug("Entering testDatasource: jndi={}, driver='{}', url='{}', username='{}', useBlankPassword={}",
				jndi, driver, url, username, useBlankPassword);

		AjaxResponse response = new AjaxResponse();

		try {
			//set password as appropriate
			boolean useCurrentPassword = false;
			if (useBlankPassword) {
				password = "";
			} else {
				if (StringUtils.isEmpty(password)) {
					//password field blank. use current password
					useCurrentPassword = true;
				}
			}

			if (useCurrentPassword) {
				//password field blank. use current password
				Datasource currentDatasource = datasourceService.getDatasource(id);
				logger.debug("currentDatasource={}", currentDatasource);
				if (currentDatasource == null) {
					response.setErrorMessage(messageSource.getMessage("page.message.cannotUseCurrentPassword", null, locale));
					return response;
				} else {
					password = decryptPassword(currentDatasource.getPassword());
				}
			}

			testConnection(jndi, driver, url, username, password);
			//if we are here, connection successful
			response.setSuccess(true);
		} catch (Exception ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	/**
	 * Decrypt datasource password
	 *
	 * @param password
	 * @return
	 */
	private String decryptPassword(String password) {
		logger.debug("Entering decryptPassword");

		if (password == null) {
			password = "";
		} else {
			if (password.startsWith("o:")) {
				//password is encrypted. decrypt
				password = Encrypter.decrypt(password.substring(2));
			}
		}

		return password;
	}

	/**
	 * Test datasource configuration and refresh art connections
	 *
	 * @param datasource
	 * @throws Exception
	 */
	private void updateConnection(Datasource datasource) throws Exception {
		logger.debug("Entering updateConnection: datasource={}", datasource);

		String driver = datasource.getDriver();
		String url = datasource.getUrl();
		String username = datasource.getUsername();
		String password = decryptPassword(datasource.getPassword());
		boolean jndi = datasource.isJndi();

		logger.debug("datasource.isActive()={}", datasource.isActive());
		if (datasource.isActive()) {
			testConnection(jndi, driver, url, username, password);
		}

		//Refresh the Art connection pools so that the new connection is ready to use (no manual refresh needed)
		ArtConfig.refreshConnections();
	}

	/**
	 * Test a datasource configuration
	 *
	 * @param jndi
	 * @param driver
	 * @param url
	 * @param username
	 * @param password clear text password
	 * @throws Exception if connection failed, otherwise connection successful
	 */
	private void testConnection(boolean jndi, String driver, String url, String username, String password) throws Exception {
		logger.debug("Entering testConnection: jndi={}, driver='{}', url='{}', username='{}'",
				jndi, driver, url, username);

		Connection conn = null;

		try {
			if (jndi) {
				conn = ArtConfig.getJndiConnection(url);
			} else {
				Class.forName(driver).newInstance();
				conn = DriverManager.getConnection(url, username, password);
			}
		} finally {
			DbUtils.close(conn);
		}
	}

	/**
	 * Set password
	 *
	 * @param datasource
	 * @param action
	 * @return i18n message to display in the user interface if there was a
	 * problem, null otherwise
	 * @throws SQLException
	 */
	private String setPassword(Datasource datasource, String action) throws SQLException {
		logger.debug("Entering setPassword: datasource={}, action='{}'", datasource, action);

		boolean useCurrentPassword = false;
		String newPassword = datasource.getPassword();

		if (datasource.isUseBlankPassword()) {
			newPassword = "";
		} else {
			if (StringUtils.isEmpty(newPassword) && StringUtils.equals(action, "edit")) {
				//password field blank. use current password
				useCurrentPassword = true;
			}
		}

		if (useCurrentPassword) {
			//password field blank. use current password
			Datasource currentDatasource = datasourceService.getDatasource(datasource.getDatasourceId());
			if (currentDatasource == null) {
				return "page.message.cannotUseCurrentPassword";
			} else {
				datasource.setPassword(currentDatasource.getPassword());
			}
		} else {
			//encrypt new password
			if (StringUtils.isNotEmpty(newPassword)) {
				newPassword = "o:" + Encrypter.encrypt(newPassword);
			}
			datasource.setPassword(newPassword);
		}

		return null;
	}

}
