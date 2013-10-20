package art.job;

import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for jobs page
 *
 * @author Timothy Anyona
 */
@Controller
public class JobController {

	private final JobService jobService;

	@Autowired
	public JobController(JobService jobService) {
		this.jobService = jobService;
	}

	@RequestMapping(value = "/app/jobs", method = RequestMethod.GET)
	public String showSharedJobs(HttpSession session, Model model) {
		String username = (String) session.getAttribute("username");
		model.addAttribute("jobs", jobService.getAllJobs(username));

		return "jobs";
	}
}
