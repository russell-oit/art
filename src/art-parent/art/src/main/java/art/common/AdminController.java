package art.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Timothy Anyona
 */
@Controller
public class AdminController {
	
	@RequestMapping(value="/admin/admin", method=RequestMethod.GET)
	public String showAdmin(){
		return "admin";
	}
	
}
