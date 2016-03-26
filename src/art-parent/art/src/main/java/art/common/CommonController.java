package art.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for simple pages that don't have much logic
 *
 * @author Timothy Anyona
 */
@Controller
public class CommonController {

	@RequestMapping(value = "/app/accessDenied", method = RequestMethod.GET)
	public String showAccessDenied() {
		return "accessDenied";
	}

	@RequestMapping(value = "/app/success", method = RequestMethod.GET)
	public String showSuccess() {
		return "success";
	}
	
	@RequestMapping(value = "/app/reportError", method = RequestMethod.GET)
	public String showReportError() {
		return "reportError";
	}

}
