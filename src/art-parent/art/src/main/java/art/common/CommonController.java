package art.common;

import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupEqualsFilter;
import art.reportgroup.ReportGroupService;
import art.user.User;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;
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

	@RequestMapping(value = "/app/reports", method = RequestMethod.GET)
	public String showReports(HttpSession session,
			@RequestParam(value = "groupId", required = false) Integer groupId,
			HttpServletRequest request, Model model) {
		try {
			User sessionUser = (User) session.getAttribute("sessionUser");

			ReportGroupService reportGroupService = new ReportGroupService();

			List<ReportGroup> groups = reportGroupService.getAvailableReportGroups(sessionUser.getUsername());

			//allow to focus public_user in one group only. is this feature used? it's not documented
			if (groupId != null) {
				Predicate predicate = new ReportGroupEqualsFilter(groupId);
				Iterator<ReportGroup> filteredGroup = new FilterIterator(groups.iterator(), predicate);
				model.addAttribute("reportGroups", filteredGroup);
			} else {
				model.addAttribute("reportGroups", groups);
			}
		} catch (Exception ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "reports";
	}
}
