package art.artdatabase;

import art.servlets.ArtConfig;
import art.utils.ArtUtils;
import art.dbutils.DbUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring controller for the art database configuration process
 *
 * @author Timothy Anyona
 */
@Controller
public class ArtDatabaseController {

	private static final Logger logger = LoggerFactory.getLogger(ArtDatabaseController.class);

	@ModelAttribute("databaseTypes")
	public Map<String, String> addDatabaseTypes() {
		Map<String, String> databaseTypes = ArtUtils.getDatabaseTypes();
		//generic sun jdbc-odbc bridge driver not supported for the art database
		//for the jdbc-odbc bridge, you can only read column values ONCE
		//and in the ORDER they appear in the select. Adhering to this is brittle and cumbersome.
		databaseTypes.remove("odbc");
		return databaseTypes;
	}

	@RequestMapping(value = "/app/artDatabase", method = RequestMethod.GET)
	public String showArtDatabaseConfiguration(HttpSession session, Model model) {

		ArtDatabase artDatabase = ArtConfig.getArtDatabaseConfiguration();

		if (artDatabase == null) {
			//art database not configured. default to demo
			artDatabase = new ArtDatabase();
			artDatabase.setUrl("demo");

			//set default values
			ArtConfig.setArtDatabaseDefaults(artDatabase);
		}

		//use blank password should always start as false
		artDatabase.setUseBlankPassword(false);

		model.addAttribute("artDatabase", artDatabase);
		return "artDatabase";
	}

	@RequestMapping(value = "/app/artDatabase", method = RequestMethod.POST)
	public String processArtDatabaseConfiguration(HttpSession session,
			@ModelAttribute("artDatabase") @Valid ArtDatabase artDatabase,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

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
				if (ArtConfig.isArtDatabaseConfigured()) {
					newPassword = ArtConfig.getArtDatabaseConfiguration().getPassword();
				}
			}
		}
		artDatabase.setPassword(newPassword);

		//verify database settings
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			String demoDbUrl = "jdbc:hsqldb:file:" + ArtConfig.getHsqldbPath() + "ArtRepositoryDB;shutdown=true;create=false;hsqldb.write_delay=false";
			String sampleDbUrl = "jdbc:hsqldb:file:" + ArtConfig.getHsqldbPath() + "SampleDB;shutdown=true;create=false;hsqldb.write_delay=false";

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

			if (StringUtils.isNotBlank(driver)) {
				Class.forName(driver).newInstance();
				conn = DriverManager.getConnection(url, username, password);
			} else {
				//using jndi datasource
				conn = ArtConfig.getJndiConnection(url);
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

				ps.setString(1, demoDbUrl);
				ps.setInt(2, 2);
				ps.executeUpdate();

				ps.setString(1, sampleDbUrl);
				ps.setInt(2, 1);
				ps.executeUpdate();
			}

			//save settings
			ArtConfig.saveArtDatabaseConfiguration(artDatabase);

			ArtConfig.refreshConnections();

			//use redirect after successful submission so that a browser page refresh e.g. F5
			//doesn't resubmit the page (PRG pattern)
			redirectAttributes.addFlashAttribute("message", "artDatabase.message.configurationSaved");
			return "redirect:/app/success.do";
		} catch (Exception ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		} finally {
			DbUtils.close(ps, conn);
		}

		return "artDatabase";
	}
}
