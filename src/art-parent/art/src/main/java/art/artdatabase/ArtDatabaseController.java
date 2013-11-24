package art.artdatabase;

import art.servlets.ArtConfig;
import art.utils.ArtUtils;
import art.utils.DbUtils;
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

	final static Logger logger = LoggerFactory.getLogger(ArtDatabaseController.class);

	@ModelAttribute("databaseTypes")
	public Map<String, String> addDatabaseTypes() {
		return ArtUtils.getDatabaseTypes();
	}

	@RequestMapping(value = "/app/artDatabase", method = RequestMethod.GET)
	public String showArtDatabaseConfiguration(HttpSession session, Model model) {

		ArtDatabaseForm artDatabaseForm = ArtConfig.getArtDatabaseConfiguration();

		if (artDatabaseForm == null) {
			//art database not configured. use default values
			artDatabaseForm=new ArtDatabaseForm();
		} 

		model.addAttribute("artDatabaseForm", artDatabaseForm);

		return "artDatabase";
	}

	@RequestMapping(value = "/app/artDatabase", method = RequestMethod.POST)
	public String processArtDatabaseConfiguration(
			@ModelAttribute("artDatabaseForm") @Valid ArtDatabaseForm artDatabaseForm,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {
		
		if (result.hasErrors()) {
			return "artDatabase";
		}

		//verify database settings
		Connection conn = null;
		PreparedStatement ps = null;

		try {

			String driver = artDatabaseForm.getDriver();
			String url = artDatabaseForm.getUrl();
			String username = artDatabaseForm.getUsername();
			String password = artDatabaseForm.getPassword();

			String demoDbUrl = "jdbc:hsqldb:file:" + ArtConfig.getHsqldbPath() + "ArtRepositoryDB;shutdown=true;create=false;hsqldb.write_delay=false";
			String sampleDbUrl = "jdbc:hsqldb:file:" + ArtConfig.getHsqldbPath() + "SampleDB;shutdown=true;create=false;hsqldb.write_delay=false";

			boolean usingDemoDatabase = false;
			if (StringUtils.equalsIgnoreCase(url, "demo")) {
				usingDemoDatabase = true;

				driver = "org.hsqldb.jdbcDriver";
				url = demoDbUrl;

				if (StringUtils.isBlank(username)) {
					//use default username and password
					username = "ART";
					password = "ART";
				}
			}
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
			ArtConfig.saveArtDatabaseConfiguration(artDatabaseForm);

			ArtConfig.refreshConnections();

			//use redirect after successful submission so that a browser page refresh e.g. F5
			//doesn't resubmit the page (PRG pattern)
			redirectAttributes.addFlashAttribute("success", "true");
			return "redirect:/app/artDatabase.do";
		} catch (Exception ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		} finally {
			DbUtils.close(ps, conn);
		}

		return "artDatabase";
	}
}
