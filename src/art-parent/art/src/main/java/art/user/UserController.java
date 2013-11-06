package art.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Spring controller for the user configuration process
 * 
 * @author Timothy Anyona
 */
@Controller
public class UserController {
	
	private final UserService userService;
	
	@Autowired
	public UserController(UserService userService){
		this.userService=userService;
	}
	
	@RequestMapping(value="/app/users", method= RequestMethod.GET)
	public String showUsers(Model model){
		model.addAttribute("users", userService.getAllUsers());
		return "users";
	}
	
}
