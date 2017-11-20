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
package art.datasource;

import art.artdatabase.ArtDatabase;
import art.connectionpool.DbConnections;
import art.dbutils.DatabaseUtils;
import art.encryption.AesEncryptor;
import art.enums.DatasourceType;
import art.report.ReportService;
import art.servlets.Config;
import art.user.User;
import art.utils.ActionResult;
import art.utils.AjaxResponse;
import art.utils.ArtUtils;
import art.utils.MongoHelper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpSession;
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
	
	@Autowired
	private ReportService reportService;

	@RequestMapping(value = "/datasources", method = RequestMethod.GET)
	public String showDatasources(Model model) {
		logger.debug("Entering showDatasources");

		try {
			model.addAttribute("datasources", datasourceService.getAllDatasources());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "datasources";
	}

	@RequestMapping(value = "/deleteDatasource", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteDatasource(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteDatasource: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = datasourceService.deleteDatasource(id);

			logger.debug("deleteResult.isSuccess() = {}", deleteResult.isSuccess());
			if (deleteResult.isSuccess()) {
				response.setSuccess(true);
				DbConnections.removeConnectionPool(id);
			} else {
				//datasource not deleted because of linked reports
				List<String> cleanedData = deleteResult.cleanData();
				response.setData(cleanedData);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteDatasources", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteDatasources(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteDatasources: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			ActionResult deleteResult = datasourceService.deleteDatasources(ids);
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

	@RequestMapping(value = "/addDatasource", method = RequestMethod.GET)
	public String addDatasource(Model model) {
		logger.debug("Entering addDatasource");

		Datasource datasource = new Datasource();

		//set defaults
		datasource.setActive(true);
		datasource.setConnectionPoolTimeoutMins(20);
		datasource.setDatasourceType(DatasourceType.JDBC);

		model.addAttribute("datasource", datasource);

		return showEditDatasource("add", model);
	}

	@RequestMapping(value = "/editDatasource", method = RequestMethod.GET)
	public String editDatasource(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editDatasource: id={}", id);

		try {
			model.addAttribute("datasource", datasourceService.getDatasource(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDatasource("edit", model);
	}

	@RequestMapping(value = "/copyDatasource", method = RequestMethod.GET)
	public String copyDatasource(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering copyDatasource: id={}", id);

		try {
			model.addAttribute("datasource", datasourceService.getDatasource(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDatasource("copy", model);
	}

	@RequestMapping(value = "/editDatasources", method = RequestMethod.GET)
	public String editDatasources(@RequestParam("ids") String ids, Model model,
			HttpSession session) {

		logger.debug("Entering editDatasources: ids={}", ids);

		MultipleDatasourceEdit multipleDatasourceEdit = new MultipleDatasourceEdit();
		multipleDatasourceEdit.setIds(ids);

		model.addAttribute("multipleDatasourceEdit", multipleDatasourceEdit);

		return "editDatasources";
	}

	@RequestMapping(value = "/saveDatasource", method = RequestMethod.POST)
	public String saveDatasource(@ModelAttribute("datasource")
			@Valid Datasource datasource, @RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveDatasource: datasource={}, action='{}'", datasource, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditDatasource(action, model);
		}

		try {
			//set password as appropriate
			String setPasswordMessage = setPassword(datasource, action);
			logger.debug("setPasswordMessage='{}'", setPasswordMessage);
			if (setPasswordMessage != null) {
				model.addAttribute("message", setPasswordMessage);
				return showEditDatasource(action, model);
			}

			//ucanaccess will cause error if username is not empty
			if (StringUtils.startsWith(datasource.getUrl(), "jdbc:ucanaccess")) {
				datasource.setUsername("");
			}

			User sessionUser = (User) session.getAttribute("sessionUser");

			if (StringUtils.equals(action, "add") || StringUtils.equals(action, "copy")) {
				datasourceService.addDatasource(datasource, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				datasourceService.updateDatasource(datasource, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}

			try {
				updateConnectionPool(datasource);
			} catch (Exception ex) {
				logger.error("Error", ex);
				redirectAttributes.addFlashAttribute("error", ex);
			}

			String recordName = datasource.getName() + " (" + datasource.getDatasourceId() + ")";
			redirectAttributes.addFlashAttribute("recordName", recordName);
			return "redirect:/datasources";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDatasource(action, model);
	}

	@RequestMapping(value = "/saveDatasources", method = RequestMethod.POST)
	public String saveDatasources(@ModelAttribute("multipleDatasourceEdit") @Valid MultipleDatasourceEdit multipleDatasourceEdit,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveDatasources: multipleDatasourceEdit={}", multipleDatasourceEdit);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditDatasources();
		}

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			datasourceService.updateDatasources(multipleDatasourceEdit, sessionUser);
			redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordsUpdated");
			redirectAttributes.addFlashAttribute("recordName", multipleDatasourceEdit.getIds());
			return "redirect:/datasources";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditDatasources();
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showEditDatasource(String action, Model model) {
		logger.debug("Entering showEditDatasource: action='{}'", action);

		Map<String, String> databaseTypes = ArtUtils.getDatabaseTypes();
		databaseTypes.remove("demo");

		model.addAttribute("databaseTypes", databaseTypes);
		model.addAttribute("datasourceTypes", DatasourceType.list());
		model.addAttribute("action", action);

		return "editDatasource";
	}

	/**
	 * Prepares model data and returns jsp file to display
	 *
	 * @param action
	 * @param model
	 * @return
	 */
	private String showEditDatasources() {
		logger.debug("Entering showEditDatasources");
		return "editDatasources";
	}

	@RequestMapping(value = "/testDatasource", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse testDatasource(@RequestParam("id") Integer id,
			@RequestParam("jndi") Boolean jndi,
			@RequestParam("driver") String driver, @RequestParam("url") String url,
			@RequestParam("username") String username, @RequestParam("password") String password,
			@RequestParam("useBlankPassword") Boolean useBlankPassword,
			@RequestParam("action") String action,
			Locale locale) {

		logger.debug("Entering testDatasource: jndi={}, driver='{}', url='{}', username='{}',"
				+ " useBlankPassword={}, action='{}'", jndi, driver, url, username,
				useBlankPassword, action);

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

			if ((StringUtils.equalsIgnoreCase(action, "edit") || StringUtils.equalsIgnoreCase(action, "copy"))
					&& useCurrentPassword) {
				//password field blank. use current password
				Datasource currentDatasource = datasourceService.getDatasource(id);
				logger.debug("currentDatasource={}", currentDatasource);
				if (currentDatasource == null) {
					response.setErrorMessage(messageSource.getMessage("page.message.cannotUseCurrentPassword", null, locale));
					return response;
				} else {
					password = currentDatasource.getPassword();
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
	 * Tests datasource configuration and refreshes art connections
	 *
	 * @param datasource
	 * @throws Exception
	 */
	private void updateConnectionPool(Datasource datasource) throws Exception {
		logger.debug("Entering updateConnectionPool: datasource={}", datasource);

		String driver = datasource.getDriver();
		String url = datasource.getUrl();
		String username = datasource.getUsername();
		String clearTextPassword = AesEncryptor.decrypt(datasource.getPassword());
		datasource.setPassword(clearTextPassword);
		boolean jndi = datasource.isJndi();

		logger.debug("datasource.isActive()={}", datasource.isActive());
		if (datasource.isActive()) {
			testConnection(jndi, driver, url, username, clearTextPassword);

			DatasourceType datasourceType = datasource.getDatasourceType();
			logger.debug("datasourceType={}", datasourceType);
			switch (datasourceType) {
				case JDBC:
					ArtDatabase artDbConfig = Config.getArtDbConfig();
					DbConnections.createConnectionPool(datasource, artDbConfig.getMaxPoolConnections(), artDbConfig.getConnectionPoolLibrary());
					break;
				case MongoDB:
					DbConnections.createMongodbConnectionPool(datasource);
					break;
				default:
				//do nothing
			}
		}
	}

	/**
	 * Tests a datasource configuration
	 *
	 * @param jndi whether the datasource is a jndi datasource
	 * @param driver the jdbc driver for the datasource
	 * @param url the jdbc url for the datasource
	 * @param username the username for the datasource connection
	 * @param password the clear text password for the datasource connection
	 * @throws Exception if connection failed, otherwise connection successful
	 */
	private void testConnection(boolean jndi, String driver, String url,
			String username, String password) throws Exception {

		logger.debug("Entering testConnection: jndi={}, driver='{}', url='{}', username='{}'",
				jndi, driver, url, username);

		Connection conn = null;

		try {
			if (StringUtils.startsWith(url, "http")) {
				//do nothing
				//don't try to make a jdbc connection to http url. Probably xmla connection
			} else if (jndi) {
				conn = ArtUtils.getJndiConnection(url);
			} else {
				if (StringUtils.startsWith(url, "mongodb://")) {
					//https://docs.mongodb.com/manual/reference/connection-string/
					//https://mongodb.github.io/node-mongodb-native/driver-articles/mongoclient.html
					MongoClient mongoClient = null;
					try {
						MongoHelper mongoHelper = new MongoHelper();
						String finalUrl = mongoHelper.getUrlWithCredentials(url, username, password);
						MongoClientURI uri = new MongoClientURI(finalUrl);
						mongoClient = new MongoClient(uri);
					} finally {
						if (mongoClient != null) {
							mongoClient.close();
						}
					}
				} else {
					if (StringUtils.isNotBlank(driver)) {
						//use newInstance because of neo4j driver
						//https://github.com/neo4j-contrib/neo4j-jdbc/issues/104
						//Class.forName(driver);
						Class.forName(driver).newInstance();
					}
					//use ends with instead of equals to cater for net.sf.mondrianart.mondrian.olap4j.MondrianOlap4jDriver
					if (StringUtils.endsWith(driver, "mondrian.olap4j.MondrianOlap4jDriver")) {
						if (!StringUtils.endsWith(url, ";")) {
							url += ";";
						}
						if (StringUtils.isNotBlank(username)
								&& !StringUtils.contains(url, "JdbcUser")) {
							url += "JdbcUser=" + username + ";";
						}
						if (StringUtils.isNotBlank(password)
								&& !StringUtils.contains(url, "JdbcPassword")) {
							url += "JdbcPassword=" + password + ";";
						}
					}

					//ucanaccess will cause error if username is not empty
					if (StringUtils.startsWith(url, "jdbc:ucanaccess")) {
						username = "";
					}

					//conn = DriverManager.getConnection(url, username, password);
					//use getDriver() in order for correct reporting of No suitable driver error.
					//with some urls/drivers, the jvm tries to use the wrong driver
					//e.g. with neo4j driver if driver is not included in application lib/classpath or in jre\lib\ext
					Properties dbProperties = new Properties();
					dbProperties.put("user", username);
					dbProperties.put("password", password);
					Driver driverObject = DriverManager.getDriver(url); // get the right driver for the given url
					conn = driverObject.connect(url, dbProperties); // get the connection
				}
			}
		} finally {
			DatabaseUtils.close(conn);
		}
	}

	/**
	 * Sets the password field of the datasource
	 *
	 * @param datasource the datasource object to set
	 * @param action "add", "edit" or "copy"
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
			if (StringUtils.isEmpty(newPassword)
					&& (StringUtils.equals(action, "edit") || StringUtils.equals(action, "copy"))) {
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
				newPassword = currentDatasource.getPassword();
			}
		}

		//encrypt new password
		String encryptedPassword = AesEncryptor.encrypt(newPassword);
		datasource.setPassword(encryptedPassword);
		datasource.setPasswordAlgorithm("AES");

		return null;
	}
	
	@RequestMapping(value = "/reportsWithDatasource", method = RequestMethod.GET)
	public String showReportsWithDatasource(@RequestParam("datasourceId") Integer datasourceId, Model model) {
		logger.debug("Entering showReportsWithDatasource: datasourceId={}", datasourceId);

		try {
			model.addAttribute("datasource", datasourceService.getDatasource(datasourceId));
			model.addAttribute("reports", reportService.getReportsWithDatasource(datasourceId));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reportsWithDatasource";
	}
}
