package art.user;

import art.utils.AjaxResponse;
import java.sql.SQLException;
import javax.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring controller for the user configuration process
 *
 * @author Timothy Anyona
 */
@Controller
public class UserController {

	final static org.slf4j.Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	private UserService userService;

	@RequestMapping(value = "/app/users", method = RequestMethod.GET)
	public String showUsers(Model model) {
		try {
			model.addAttribute("users", userService.getAllUsers());
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}
		
		return "users";
	}

	@RequestMapping(value = "/app/deleteUser", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	AjaxResponse deleteUser(@RequestParam("userId") Integer userId) {
		AjaxResponse response = new AjaxResponse();

		try {
			userService.deleteUser(userId);
			response.setSuccess(true);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/app/addUser", method = RequestMethod.GET)
	public String showAddUser(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("action", "add");
		return "editUser";
	}

	@RequestMapping(value = "/app/addUser", method = RequestMethod.POST)
	public String processAddUser(@RequestParam("action") String action,
			@ModelAttribute("user") @Valid User user,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {
		
		if(result.hasErrors()){
			model.addAttribute("formErrors","");
			model.addAttribute("action", action);
			return "editUser";
		}
		
		try{
			userService.addUser(user);
			redirectAttributes.addFlashAttribute("message", "users.message.userAdded");
			return "redirect:/app/users.do";
		} catch(SQLException ex){
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", action);
		return "editUser";
	}
	
	@RequestMapping(value = "/app/editUser", method = RequestMethod.GET)
	public String showEditUser(@RequestParam("userId") Integer userId, Model model) {
		try {
			User user = userService.getUser(userId);
			model.addAttribute("user", user);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", "edit");
		return "editUser";
	}

}
