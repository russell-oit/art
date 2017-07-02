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
package art.artdatabase;

import art.cache.CacheHelper;
import art.dbutils.DatabaseUtils;
import art.enums.ConnectionPoolLibrary;
import art.servlets.Config;
import art.user.User;
import art.user.UserService;
import art.utils.ArtUtils;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.BooleanUtils;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for the art database configuration process
 *
 * @author Timothy Anyona
 */
@Controller
public class ArtDatabaseController {

	private static final Logger logger = LoggerFactory.getLogger(ArtDatabaseController.class);

	@Autowired
	private CacheHelper cacheHelper;

	@Autowired
	private UserService userService;

	@ModelAttribute("databaseTypes")
	public Map<String, String> addDatabaseTypes() {
		Map<String, String> databaseTypes = ArtUtils.getDatabaseTypes();
		//generic sun jdbc-odbc bridge driver not supported for the art database
		//for the jdbc-odbc bridge, you can only read column values ONCE
		//and in the ORDER they appear in the select. Adhering to this is brittle and cumbersome.
//		databaseTypes.remove("generic-odbc"); //sun jdbc-odbc bridge is removed in Java 8
		databaseTypes.remove("hbase-phoenix");
		databaseTypes.remove("msaccess-ucanaccess");
		databaseTypes.remove("sqlite-xerial");
		databaseTypes.remove("csv-csvjdbc");
		databaseTypes.remove("olap4j-mondrian");
		databaseTypes.remove("olap4j-xmla");
		databaseTypes.remove("couchbase");
		databaseTypes.remove("mongodb");
		databaseTypes.remove("drill");
		databaseTypes.remove("monetdb");
		databaseTypes.remove("vertica");
		databaseTypes.remove("cassandra-adejanovski");
		databaseTypes.remove("neo4j");

		return databaseTypes;
	}

	@ModelAttribute("connectionPoolLibraries")
	public List<ConnectionPoolLibrary> addConnectionPoolLibraries() {
		return ConnectionPoolLibrary.list();
	}

	@RequestMapping(value = "/artDatabase", method = RequestMethod.GET)
	public String showArtDatabaseConfiguration(Model model) {
		logger.debug("Entering showArtDatabaseConfiguration");

		ArtDatabase artDatabase = Config.getArtDbConfig();

		if (artDatabase == null) {
			//art database not configured. default to demo
			artDatabase = new ArtDatabase();
			artDatabase.setUrl("demo");

			//set default values
			Config.setArtDatabaseDefaults(artDatabase);
		}

		//use blank password should always start as false
		artDatabase.setUseBlankPassword(false);

		model.addAttribute("artDatabase", artDatabase);

		return "artDatabase";
	}

	@RequestMapping(value = "/artDatabase", method = RequestMethod.POST)
	public String processArtDatabaseConfiguration(
			@ModelAttribute("artDatabase") @Valid ArtDatabase artDatabase,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering processArtDatabaseConfiguration");

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return "artDatabase";
		}

		cacheHelper.clearAll();

		//set password field as appropriate
		String newPassword = artDatabase.getPassword();
		if (artDatabase.isUseBlankPassword()) {
			newPassword = "";
		} else {
			if (StringUtils.isEmpty(newPassword)) {
				//password field blank. use current password
				if (Config.isArtDatabaseConfigured()) {
					newPassword = Config.getArtDbConfig().getPassword();
				}
			}
		}
		artDatabase.setPassword(newPassword);

		//verify database settings
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			String hsqldbUrl = "jdbc:hsqldb:file:" + Config.getHsqldbPath() + "%s;shutdown=true;create=false;hsqldb.write_delay=false";
			String demoDbUrl = String.format(hsqldbUrl, "ArtRepositoryDB");

			boolean usingDemoDatabase = false;
			if (StringUtils.equalsIgnoreCase(artDatabase.getUrl(), "demo")) {
				usingDemoDatabase = true;

				artDatabase.setDriver("org.hsqldb.jdbcDriver");
				artDatabase.setUrl(demoDbUrl);
				artDatabase.setUsername("ART");
				artDatabase.setPassword("ART");
			}

			String driver = artDatabase.getDriver();
			String url = artDatabase.getUrl();
			String username = artDatabase.getUsername();
			String password = artDatabase.getPassword();

			if (artDatabase.isJndi()) {
				conn = ArtUtils.getJndiConnection(url);
			} else {
				//for jdbc 4 drivers, you don't have to specify driver or use class.forName()
				//https://stackoverflow.com/questions/5484227/jdbc-class-forname-vs-drivermanager-registerdriver
				if (StringUtils.isNotBlank(driver)) {
					Class.forName(driver).newInstance();
				}
				conn = DriverManager.getConnection(url, username, password);
			}

			/* if we are here, Connection to art database is successful.

			 If this is a connection to demo database just after the initial setup,
			 update "SampleDB" and "Art Repository" datasources in Art Repository 
			 to point to the correct files

			 */
			if (usingDemoDatabase) {
				String sql;
				sql = "UPDATE ART_DATABASES SET URL=? WHERE DATABASE_ID=?";
				ps = conn.prepareStatement(sql);

				String sampleDbUrl = String.format(hsqldbUrl, "SampleDB");
				ps.setString(1, sampleDbUrl);
				ps.setInt(2, 1);
				ps.addBatch();

				ps.setString(1, demoDbUrl);
				ps.setInt(2, 2);
				ps.addBatch();

				//don't use hsqldbUrl which contains ; within it. ; is used as a separator in the mondrian url
				//this means we can't effect the shutdown=true property and lock files will remain after the connections are closed
				String mondrianJdbcUrl = "jdbc:hsqldb:file:" + Config.getHsqldbPath() + "SampleDB";
				String mondrianUrl = "jdbc:mondrian:Jdbc=" + mondrianJdbcUrl + ";JdbcDrivers=org.hsqldb.jdbcDriver";
				ps.setString(1, mondrianUrl);
				ps.setInt(2, 3);
				ps.addBatch();

				ps.executeBatch();
			}

			Config.saveArtDatabaseConfiguration(artDatabase);

			Config.initializeArtDatabase();

			//refresh session user credentials as per new database
			boolean initialSetup = BooleanUtils.toBoolean((String) session.getAttribute("initialSetup"));
			if (!initialSetup) {
				User sessionUser = (User) session.getAttribute("sessionUser");
				String sessionUsername = sessionUser.getUsername();
				User updatedUser = userService.getUser(sessionUsername);
				if (updatedUser == null || !updatedUser.isActive()) {
					session.invalidate();
					return "redirect:/login";
				} else {
					session.setAttribute("sessionUser", updatedUser);
				}
			}

			//use redirect after successful submission so that a browser page refresh e.g. F5
			//doesn't resubmit the page (PRG pattern)
			redirectAttributes.addFlashAttribute("message", "artDatabase.message.configurationSaved");
			return "redirect:/success";
		} catch (NamingException | RuntimeException | SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException | IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		} finally {
			DatabaseUtils.close(ps, conn);
		}

		return "artDatabase";
	}
}
