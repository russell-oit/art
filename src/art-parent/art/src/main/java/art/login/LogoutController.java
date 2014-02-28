package art.login;

import art.enums.ArtAuthenticationMethod;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for the logout process
 *
 * @author Timothy Anyona
 */
@Controller
public class LogoutController {

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public String logout(HttpSession session) {
		String authenticationMethod = (String) session.getAttribute("authenticationMethod");
		ArtAuthenticationMethod loginMethod = ArtAuthenticationMethod.getEnum(authenticationMethod);

		session.invalidate();

		if (loginMethod == ArtAuthenticationMethod.Auto) {
			//display logout page for auto login.
			return "logout";
		} else {
			return "redirect:/login.do";
		}
	}
}
