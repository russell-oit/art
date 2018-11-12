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
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
		databaseTypes.remove("odbc-sun");
		databaseTypes.remove("hbase-phoenix");
		databaseTypes.remove("msaccess-ucanaccess");
		databaseTypes.remove("msaccess-ucanaccess-password");
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
		databaseTypes.remove("exasol");
		databaseTypes.remove("redshift");
		databaseTypes.remove("teradata");
		databaseTypes.remove("snowflake1-us-west");
		databaseTypes.remove("snowflake2-other");
		databaseTypes.remove("presto");
		databaseTypes.remove("memsql");
		databaseTypes.remove("citus");
		databaseTypes.remove("aurora-mysql-mariadb");
		databaseTypes.remove("aurora-postgresql-postgresql");

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

			String username = artDatabase.getUsername();
			String password = artDatabase.getPassword();

			boolean usingDemoDatabase = false;
			if (StringUtils.equalsIgnoreCase(artDatabase.getUrl(), "demo")) {
				usingDemoDatabase = true;

				//org.hsqldb.jdbcDriver is for hsqldb 1.x, org.hsqldb.jdbc.JDBCDriver for hsqldb 2.x
				//http://www.hsqldb.org/doc/1.8/src/org/hsqldb/jdbcDriver.html
				//http://hsqldb.org/doc/src/org/hsqldb/jdbc/JDBCDriver.html
				//need to use class.forName() in a web app if you'll use DriverManager
				//https://stackoverflow.com/questions/1911253/the-infamous-java-sql-sqlexception-no-suitable-driver-found
				//https://tomcat.apache.org/tomcat-8.0-doc/jndi-datasource-examples-howto.html#DriverManager,_the_service_provider_mechanism_and_memory_leaks
				//https://stackoverflow.com/questions/5556664/how-to-fix-no-suitable-driver-found-for-jdbcmysql-localhost-dbname-error-w?noredirect=1&lq=1
				//https://docs.oracle.com/javase/8/docs/api/java/sql/DriverManager.html
				//https://github.com/brettwooldridge/HikariCP/issues/288
				//https://stackoverflow.com/questions/14478870/dynamically-load-the-jdbc-driver
				//https://stackoverflow.com/questions/50750789/java-drivermanager-does-not-load-mysql-driver
				//https://github.com/brettwooldridge/HikariCP/blob/dev/src/main/java/com/zaxxer/hikari/util/DriverDataSource.java
				//https://stackoverflow.com/questions/33703785/ucanaccess-driver-not-in-drivermanager-getdrivers-list-unless-class-forname
				//https://coderanch.com/t/619163/databases/suitable-driver-Tomcat
				//https://stackoverflow.com/questions/11377018/tomcat-error-java-sql-sqlexception-no-suitable-driver-found-for-jdbcsqlserver
				artDatabase.setDriver("org.hsqldb.jdbc.JDBCDriver");
				artDatabase.setUrl(demoDbUrl);

				if (StringUtils.isBlank(username)) {
					artDatabase.setUsername("ART");
					artDatabase.setPassword("ART");
				} else {
					artDatabase.setUsername(username);
					artDatabase.setPassword(password);
				}
			}

			String driver = artDatabase.getDriver();
			String url = artDatabase.getUrl();
			username = artDatabase.getUsername();
			password = artDatabase.getPassword();

			if (artDatabase.isJndi()) {
				conn = ArtUtils.getJndiConnection(url);
			} else {
				//for jdbc 4 drivers, you don't have to specify driver or use class.forName()
				//https://stackoverflow.com/questions/5484227/jdbc-class-forname-vs-drivermanager-registerdriver
				if (StringUtils.isNotBlank(driver)) {
					Class.forName(driver).newInstance();
				}
				//conn = DriverManager.getConnection(url, username, password);
				//use getDriver() in order for correct reporting of No suitable driver error.
				//with some urls/drivers, the jvm tries to use the wrong driver
				Properties dbProperties = new Properties();
				dbProperties.put("user", username);
				dbProperties.put("password", password);
				Driver driverObject = DriverManager.getDriver(url); // get the right driver for the given url
				conn = driverObject.connect(url, dbProperties); // get the connection
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
				String mondrianUrl = "jdbc:mondrian:Jdbc=" + mondrianJdbcUrl + ";JdbcDrivers=org.hsqldb.jdbc.JDBCDriver";
				ps.setString(1, mondrianUrl);
				ps.setInt(2, 3);
				ps.addBatch();
				
				String orgChartDbUrl = String.format(hsqldbUrl, "OrgChartDB");
				ps.setString(1, orgChartDbUrl);
				ps.setInt(2, 4);
				ps.addBatch();

				ps.executeBatch();
			}

			Config.saveArtDatabaseConfiguration(artDatabase);

			Config.initializeArtDatabase();
			
			cacheHelper.clearAll(session);

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
		} catch (Exception ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		} finally {
			DatabaseUtils.close(ps, conn);
		}

		return "artDatabase";
	}
}
