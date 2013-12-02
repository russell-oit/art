package art.common;

import art.reportgroup.ReportGroup;
import art.user.User;
import art.user.UserService;
import java.util.Set;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for simple pages that don't have much logic
 *
 * @author Timothy Anyona
 */
@Controller
public class CommonController {

	final static Logger logger = LoggerFactory.getLogger(CommonController.class);

	@RequestMapping(value = "/app/accessDenied", method = RequestMethod.GET)
	public String showAccessDenied() {
		return "accessDenied";
	}

	@RequestMapping(value = "/app/reports", method = RequestMethod.GET)
	public String showReports(HttpSession session, Model model) {
		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			UserService userService = new UserService();
			model.addAttribute("reportGroups", userService.getAvailableReportGroups(sessionUser.getUsername()));
		} catch (Exception ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reports";
	}
}
