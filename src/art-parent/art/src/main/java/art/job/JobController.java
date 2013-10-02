package art.job;

import javax.servlet.http.HttpSession;
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
	
	@RequestMapping(value="/app/jobs", method=RequestMethod.GET)
	public String showSharedJobs(HttpSession session,Model model){
		String username=(String)session.getAttribute("username");
		model.addAttribute("jobs", JobDao.getAllJobs(username));
		
		return "jobs";
	}
	
}
