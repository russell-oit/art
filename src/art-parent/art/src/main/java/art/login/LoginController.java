package art.login;

import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Spring controller for the login and logoff pages
 * 
 * @author Timothy Anyona
 */
@Controller
public class LoginController {
	
	@RequestMapping("/logOff")
	public String logOff(HttpSession session){
		session.invalidate();
		return "redirect:/login.do";
		
	}
	
	@RequestMapping(value="/login", method=RequestMethod.GET)
	public String showLogin(@RequestParam(value="loginMode", required=false) String loginMode, ModelMap model){
		
		return "login";
	}
	
}
