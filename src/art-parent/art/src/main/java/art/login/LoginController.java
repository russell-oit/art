package art.login;

import art.enums.LoginMethod;
import art.servlets.ArtConfig;
import art.utils.UserEntity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

/**
 * Spring controller for the login process
 *
 * @author Timothy Anyona
 */
@Controller
public class LoginController {

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String showLogin(HttpServletRequest request,
			@RequestParam(value = "loginMode", required = false) String loginMode,
			Model model) {

		if (!ArtConfig.isArtSettingsLoaded()) {
			return "redirect:/admin/adminConsole.jsp";
		}

		if (loginMode == null) {
			//authentication method not specified in url. use application setting
			loginMode = ArtConfig.getAuthenticationMethod();
		}

		LoginMethod loginMethod = LoginMethod.getEnum(loginMode);

		//if from logout controller, nextpage may be in an attribute. put it in the new session
		HttpSession session = request.getSession();
		String nextPage = (String) request.getAttribute("nextPage");
		if (nextPage != null) {
			session.setAttribute("nextPage", nextPage);
		}

		//if auto login, no login page is displayed as user is authenticated by application server
		String username;
		String message;
		if (loginMethod == LoginMethod.Auto) {
			username = request.getRemoteUser();
			message = (String) request.getAttribute("message");
			if (StringUtils.length(username) > 0 && message == null) {
				//user authenticated 

				//TODO encode url. String nextPage = response.encodeRedirectURL((String) session.getAttribute("nextPage"));
				nextPage = (String) session.getAttribute("nextPage");
				//remove nextpage attribute to prevent endless redirection to login page for /admin pages
				session.removeAttribute("nextPage");

				// redirect and art will verify if the user is setup as an art user
				if (nextPage == null) {
					nextPage = "user/showGroups.jsp";
				}
				return "redirect:" + nextPage;
			} else {
				//auto login failed. give message and change default to internal login
				model.addAttribute("username", username);
				model.addAttribute("autoLoginMessage", "login.message.invalidAutoLoginUser");
				loginMethod = LoginMethod.Internal;
			}
		} else if (loginMethod == LoginMethod.WindowsDomain) {
			model.addAttribute("windowsDomains", ArtConfig.getArtSetting("mswin_domains"));
		}

		session.setAttribute("loginMode", loginMethod.getValue());

		return "login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String processLogin(
			HttpServletRequest request,
			@RequestParam(required = false) String windowsDomain,
			@RequestParam String username,
			@RequestParam String password,
			Model model,
			SessionStatus sessionStatus) {

		//windowsDomain request parameter may not be in the request parameters. 
		//only available with windows domain authentication

		boolean authenticated = false;

		HttpSession session = request.getSession();
		String loginMode = (String) session.getAttribute("loginMode");
		LoginMethod loginMethod = LoginMethod.getEnum(loginMode);

		if (loginMethod == LoginMethod.Database) {
			authenticated = DbLogin.authenticate(username, password);
		} else if (loginMethod == LoginMethod.Ldap) {
			authenticated = LdapLogin.authenticate(username, password);
		} else if (loginMethod == LoginMethod.WindowsDomain) {
			authenticated = WindowsDomainLogin.authenticate(windowsDomain, username, password);
		}

		//auto login handled by GET

		if (!authenticated) {
			//if external authentication failed, try and use internal login
			authenticated = InternalLogin.authenticate(username, password);
		}

		if (authenticated) {
			String nextPage = (String) session.getAttribute("nextPage");
			session.removeAttribute("nextPage");

			UserEntity ue = new UserEntity(username);
			ue.setAccessLevel(80); //TODO change. get value from login classes?

			session.setAttribute("ue", ue);
			session.setAttribute("username", username);
			if (nextPage == null) {
				nextPage = "user/showGroups.jsp";
			}
			return "redirect:" + nextPage;
		} else {
			model.addAttribute("message", "invalid details");
			return "login";
		}
	}
}
