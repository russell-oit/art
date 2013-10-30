package art.login;

import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for the logout process
 *
 * @author Timothy Anyona
 */
@Controller
public class LogoutController {

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		String authenticationMethod = (String) session.getAttribute("authenticationMethod");
		AuthenticationMethod loginMethod = AuthenticationMethod.getEnum(authenticationMethod);

		session.invalidate();

		if (loginMethod == AuthenticationMethod.Auto) {
			//display logout page for auto login.
			return "logout";
		} else {
			return "redirect:/login.do";
		}
	}
}
