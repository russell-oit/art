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

import art.report.AvailableReport;
import art.servlets.ArtConfig;
import art.utils.AjaxResponse;
import art.utils.ArtUtils;
import art.utils.Encrypter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
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
 * Controller for the datasource configuration process
 *
 * @author Timothy Anyona
 */
@Controller
public class DatasourceController {

	private static final Logger logger = LoggerFactory.getLogger(DatasourceController.class);

	@Autowired
	private DatasourceService datasourceService;

	@RequestMapping(value = "/app/datasources", method = RequestMethod.GET)
	public String showDatasources(Model model) {
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
	DeleteDatasourceResponse deleteDatasource(@RequestParam("id") Integer id) {
		DeleteDatasourceResponse response = new DeleteDatasourceResponse();

		try {
			List<AvailableReport> linkedReports = datasourceService.getLinkedReports(id);
			if (linkedReports.isEmpty()) {
				//no linked reports. go ahead and delete datasource
				datasourceService.deleteDatasource(id);
				response.setSuccess(true);
				ArtConfig.refreshConnections();
			} else {
				response.setLinkedReports(linkedReports);
			}
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addDatasource", method = RequestMethod.GET)
	public String addDatasourceGet(Model model
	) {
		Datasource datasource = new Datasource();

		//set defaults
		datasource.setActive(true);
		datasource.setConnectionPoolTimeout(20);

		model.addAttribute("datasource", datasource);
		return displayDatasource("add", model);
	}

	@RequestMapping(value = "/app/addDatasource", method = RequestMethod.POST)
	public String addDatasourcePost(@ModelAttribute("datasource")
			@Valid Datasource datasource,
			BindingResult result, Model model, RedirectAttributes redirectAttributes
	) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return displayDatasource("add", model);
		}

		//encrypt and set password
		encryptAndSetPassword(datasource, datasource.getPassword());

		try {
			datasourceService.addDatasource(datasource);
			try {
				refreshConnections(datasource);
			} catch (Exception ex) {
				logger.error("Error", ex);
				redirectAttributes.addFlashAttribute("error", ex);
			}
			redirectAttributes.addFlashAttribute("message", "datasources.message.datasourceAdded");
			return "redirect:/app/datasources.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return displayDatasource("add", model);
	}

	@RequestMapping(value = "/app/editDatasource", method = RequestMethod.GET)
	public String editDatasourceGet(@RequestParam("id") Integer id, Model model
	) {

		try {
			model.addAttribute("datasource", datasourceService.getDatasource(id));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return displayDatasource("edit", model);
	}

	@RequestMapping(value = "/app/editDatasource", method = RequestMethod.POST)
	public String editDatasourcePost(@ModelAttribute("datasource")
			@Valid Datasource datasource,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return displayDatasource("edit", model);
		}

		//set password as appropriate
		boolean useCurrentPassword = false;
		String newPassword = datasource.getPassword();
		if (datasource.isUseBlankPassword()) {
			newPassword = "";
		} else {
			if (StringUtils.isEmpty(newPassword)) {
				//password field blank. use current password
				useCurrentPassword = true;
			}
		}

		if (useCurrentPassword) {
			try {
				//password field blank. use current password
				Datasource currentDatasource = datasourceService.getDatasource(datasource.getDatasourceId());
				datasource.setPassword(currentDatasource.getPassword());
			} catch (SQLException ex) {
				logger.error("Error", ex);
				model.addAttribute("error", ex);
				return displayDatasource("edit", model);
			}
		} else {
			//encrypt new password
			encryptAndSetPassword(datasource, newPassword);
		}

		try {
			datasourceService.updateDatasource(datasource);
			try {
				refreshConnections(datasource);
			} catch (Exception ex) {
				logger.error("Error", ex);
				redirectAttributes.addFlashAttribute("error", ex);
			}
			redirectAttributes.addFlashAttribute("message", "page.message.recordUpdated");
			return "redirect:/app/datasources.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return displayDatasource("edit", model);
	}

	@RequestMapping(value = "/app/testDatasource", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse testDatasource(@RequestParam("id") Integer id,
			@RequestParam("driver") String driver, @RequestParam("url") String url,
			@RequestParam("username") String username, @RequestParam("password") String password,
			@RequestParam("useBlankPassword") Boolean useBlankPassword) {

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
				if (currentDatasource != null) {
					password = decryptPassword(currentDatasource.getPassword());
				}
			}

			testConnection(driver, url, username, password);
			//if we are here, connection successful
			response.setSuccess(true);
		} catch (Exception ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	/**
	 * Prepare model data and return jsp file to display
	 *
	 * @param action
	 * @param model
	 * @param session
	 * @return
	 */
	private String displayDatasource(String action, Model model) {
		Map<String, String> databaseTypes = ArtUtils.getDatabaseTypes();
		databaseTypes.remove("demo");

		model.addAttribute("databaseTypes", databaseTypes);
		model.addAttribute("action", action);
		return "editDatasource";
	}

	/**
	 * Encrypt and set datasource password
	 *
	 * @param datasource
	 * @param password
	 */
	private void encryptAndSetPassword(Datasource datasource, String password) {
		if (!password.equals("")) {
			password = "o:" + Encrypter.encrypt(password);
		}

		datasource.setPassword(password);
	}

	/**
	 * Decrypt datasource password
	 *
	 * @param password
	 * @return
	 */
	private String decryptPassword(String password) {
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
	private void refreshConnections(Datasource datasource) throws Exception {
		String driver = datasource.getDriver();
		String url = datasource.getUrl();
		String username = datasource.getUsername();
		String password = decryptPassword(datasource.getPassword());

		if (datasource.isActive()) {
			testConnection(driver, url, username, password);
		}

		//Refresh the Art connection pools so that the new connection is ready to use (no manual refresh needed)
		ArtConfig.refreshConnections();
	}

	/**
	 * Test a datasource configuration
	 *
	 * @param driver
	 * @param url
	 * @param username
	 * @param password clear text password
	 * @throws Exception if connection failed, otherwise connection successful
	 */
	private void testConnection(String driver, String url, String username, String password) throws Exception {
		if (StringUtils.isNotBlank(driver)) {
			Class.forName(driver).newInstance();
			Connection testConn = DriverManager.getConnection(url, username, password);
			testConn.close();
		} else {
			//jndi datasource
			Connection testConn = ArtConfig.getJndiConnection(url);
			testConn.close();
		}
	}

}
