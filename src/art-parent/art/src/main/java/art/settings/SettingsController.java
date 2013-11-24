package art.settings;

import art.enums.PdfPageSize;
import art.servlets.ArtConfig;
import java.io.IOException;
import javax.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for settings configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class SettingsController {

	final static org.slf4j.Logger logger = LoggerFactory.getLogger(SettingsController.class);

	@ModelAttribute("pdfPageSizes")
	public PdfPageSize[] addPdfPageSizes() {
		return PdfPageSize.values();
	}

	@RequestMapping(value = "app/settings", method = RequestMethod.GET)
	public String showSettings(Model model) {
		model.addAttribute("settings", ArtConfig.getSettings());
		return "settings";
	}

	@RequestMapping(value = "app/settings", method = RequestMethod.POST)
	public String processSettings(@ModelAttribute("settings") @Valid Settings settings,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			return "settings";
		}

		try {
			ArtConfig.saveSettings(settings);
			
			//use redirect after successful submission 
			redirectAttributes.addFlashAttribute("success", "true");
			return "redirect:/app/settings.do";
		} catch (IOException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "settings";
	}
}
