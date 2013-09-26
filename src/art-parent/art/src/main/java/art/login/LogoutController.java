package art.login;

import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for the logout process
 * 
 * @author Timothy Anyona
 */

@Controller
public class LogoutController {
	
	@RequestMapping("/logOut")
	public String logOut(HttpSession session,RedirectAttributes redirectAttributes){
		//save next page before invalidating the session
		redirectAttributes.addAttribute("nextPage", session.getAttribute("nextPage"));
		session.invalidate();
		return "redirect:/login.do";
	}
	
}
