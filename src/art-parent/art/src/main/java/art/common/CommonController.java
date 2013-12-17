package art.common;

import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.user.User;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
	
	@RequestMapping(value = "/app/serverInfo", method = RequestMethod.GET)
	public String showServerInfo() {
		//info already in application context, set in ArtConfig init
		return "serverInfo";
	}

}
