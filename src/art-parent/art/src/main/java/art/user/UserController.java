package art.user;

import art.utils.AjaxResponse;
import java.sql.SQLException;
import java.util.Locale;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

	@Autowired
	private MessageSource messages;

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

	@RequestMapping(value = "/app/deleteUser", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteUser(@RequestParam("username") String username, Locale locale) {
		AjaxResponse response = new AjaxResponse();

		try {
			userService.deleteUser(username);
			response.setSuccess(true);
			response.setSuccessMessage(messages.getMessage("users.message.userDeleted", null, locale));
		} catch (SQLException ex) {
			logger.error("Error", ex);
			String message=messages.getMessage("page.message.errorOccurred", null, locale);
			response.setErrorDetails(message + "<p>" + ex.toString());
		}

		return response;
	}

}
