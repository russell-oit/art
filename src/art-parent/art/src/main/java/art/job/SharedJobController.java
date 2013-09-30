package art.job;

import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for shared jobs page
 * 
 * @author Timothy Anyona
 */
@Controller
public class SharedJobController {
	
	@RequestMapping(value="/app/sharedJobs", method=RequestMethod.GET)
	public String showSharedJobs(HttpSession session,Model model){
		String username=(String)session.getAttribute("username");
		model.addAttribute("jobs", JobDao.getSharedJobs(username));
		
		return "sharedJobs";
	}
	
}
