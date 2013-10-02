package art.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for simple pages that require have much logic
 * 
 * @author Timothy Anyona
 */
@Controller
public class CommonController {
	
	@RequestMapping(value="/app/accessDenied", method=RequestMethod.GET)
	public String showAccessDenied(){
		return "accessDenied";
	}
	
	@RequestMapping(value="/app/home", method=RequestMethod.GET)
	public String showHome(){
		return "home";
	}
	
}
