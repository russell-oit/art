package art.artdatabase;

import art.utils.ArtUtils;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Spring controller for the art database configuration process
 *
 * @author Timothy Anyona
 */
@Controller
public class ArtDatabaseController {
	
	@ModelAttribute("databaseTypes")
	public Map<String,String> databaseTypes(){
		return ArtUtils.getDatabaseTypes();
	}

	@RequestMapping(value = "/app/artDatabase", method = RequestMethod.GET)
	public String showArtDatabaseConfiguration(HttpSession session, Model model) {
		boolean intialSetup=BooleanUtils.toBoolean((String) session.getAttribute("initialSetup"));
		model.addAttribute("artDatabase", new ArtDatabaseForm());
		return "artDatabase";
	}

	@RequestMapping(value = "/app/artDatabase", method = RequestMethod.POST)
	public String processArtDatabaseConfiguration(@ModelAttribute("artDatabase") ArtDatabaseForm artDatabase,
			Model model) {
		model.addAttribute("successMessage", "artDatabase.message.configurationSaved");
		return "artDatabase";
	}
}
