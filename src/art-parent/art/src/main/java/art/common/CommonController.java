package art.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for simple pages that don't have much logic
 *
 * @author Timothy Anyona
 */
@Controller
public class CommonController {

	@RequestMapping(value = "/accessDenied", method = {RequestMethod.GET, RequestMethod.POST})
	public String showAccessDenied() {
		return "accessDenied";
	}

	@RequestMapping(value = "/success", method = RequestMethod.GET)
	public String showSuccess() {
		return "success";
	}

	@RequestMapping(value = "/reportError", method = RequestMethod.GET)
	public String showReportError() {
		return "reportError";
	}
	
}
