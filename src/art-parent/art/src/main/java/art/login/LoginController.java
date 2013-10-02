package art.login;

import art.enums.AuthenticationMethod;
import art.servlets.ArtConfig;
import art.user.User;
import art.utils.UserEntity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
			@RequestParam(required = false) String authenticationMethod,
			Model model) {

		HttpSession session = request.getSession();

		if (!ArtConfig.isArtSettingsLoaded()) {
			UserEntity ue = new UserEntity();
			ue.setAccessLevel(100); //TODO change

			session.setAttribute("ue", ue);
			session.setAttribute("username", "");
			return "redirect:/admin/editSettings.jsp";
		}

		if (authenticationMethod == null) {
			//authentication method not specified in url. use application setting
			authenticationMethod = ArtConfig.getAuthenticationMethod();
		}

		AuthenticationMethod loginMethod = AuthenticationMethod.getEnum(authenticationMethod);

		//if auto login, no login page is displayed as user is authenticated by application server
		String username;
		String message;
		if (loginMethod == AuthenticationMethod.Auto) {
			username = request.getRemoteUser();
			message = (String) request.getAttribute("message");
			if (StringUtils.length(username) > 0 && message == null) {
				//user authenticated 
				return "redirect:" + getNextPage(session);
			} else {
				//auto login failed. give message and change default to internal login
				model.addAttribute("autoLoginUser", username);
				model.addAttribute("autoLoginMessage", "login.message.invalidAutoLoginUser");
				loginMethod = AuthenticationMethod.Internal;
			}
		} else if (loginMethod == AuthenticationMethod.WindowsDomain) {
			model.addAttribute("domains", ArtConfig.getArtSetting("mswin_domains"));
		}

		//TODO use loginmethod enum in session instead of value
		session.setAttribute("authenticationMethod", loginMethod.getValue());

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
		String authenticationMethod = (String) session.getAttribute("authenticationMethod");
		AuthenticationMethod loginMethod = AuthenticationMethod.getEnum(authenticationMethod);

		if (loginMethod == AuthenticationMethod.Database) {
			authenticated = DbLogin.authenticate(username, password);
		} else if (loginMethod == AuthenticationMethod.Ldap) {
			authenticated = LdapLogin.authenticate(username, password);
		} else if (loginMethod == AuthenticationMethod.WindowsDomain) {
			authenticated = WindowsDomainLogin.authenticate(windowsDomain, username, password);
		}

		//auto login handled by GET

		//TODO test all authentication methods
		//TODO log login attempt success or failure, to logger and to database

		if (!authenticated) {
			//if external authentication failed, try and use internal login
			authenticated = InternalLogin.authenticate(username, password);
			loginMethod = AuthenticationMethod.Internal;
		}

		if (authenticated) {
			if (loginMethod == AuthenticationMethod.Internal) {
				//if internal login, allow user to change password
				session.setAttribute("internalAuthentication", "true");
			}

			UserEntity ue = new UserEntity(username);
			ue.setAccessLevel(80); //TODO change. get value from login classes?
			session.setAttribute("ue", ue);

			User user = new User();
			user.setUsername(username);
			user.setAccessLevel(80);
			session.setAttribute("sessionUser", user);

			session.setAttribute("username", username);

			return "redirect:" + getNextPage(session);
		} else {
			model.addAttribute("message", "login.message.invalidAccount");
			return "login";
		}
	}

	private String getNextPage(HttpSession session) {
		//TODO encode url. String nextPage = response.encodeRedirectURL((String) session.getAttribute("nextPage"));
		String nextPage = (String) session.getAttribute("nextPage");
		//remove nextpage attribute. 
		//it should only be set by the authorization filter, when the session expires
		session.removeAttribute("nextPage");

		// redirect and art will verify if the user is setup as an art user
		if (nextPage == null) {
			nextPage = "/app/home.do";
		}

		return nextPage;
	}
}
