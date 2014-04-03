package art.job;

import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for jobs page and jobs configuration pages
 *
 * @author Timothy Anyona
 */
@Controller
public class JobController {
	private static final Logger logger = LoggerFactory.getLogger(JobController.class);

	@Autowired
	private JobService jobService;

	@RequestMapping(value = "/app/jobs", method = RequestMethod.GET)
	public String showJobs(HttpSession session, Model model) {
		logger.debug("Entering showJobs");
		
		String username = (String) session.getAttribute("username");
		model.addAttribute("jobs", jobService.getAllJobs(username));

		return "jobs";
	}
	
	@RequestMapping(value = "/app/jobsConfig", method = RequestMethod.GET)
	public String showJobsConfig(HttpSession session, Model model) {
		logger.debug("Entering showJobsConfig");
		
		model.addAttribute("action", "config");

		return "jobs";
	}
}
