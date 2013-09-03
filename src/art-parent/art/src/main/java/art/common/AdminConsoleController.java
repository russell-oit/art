/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package art.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Timothy Anyona
 */
@Controller
public class AdminConsoleController {
	
	@RequestMapping(value="/admin/adminConsole", method=RequestMethod.GET)
	public ModelAndView displayAdminConsole(){
		return new ModelAndView("adminConsole");
	}
	
}
