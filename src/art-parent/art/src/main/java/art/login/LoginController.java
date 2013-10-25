package art.login;

import art.servlets.ArtConfig;
import art.user.User;
import art.user.UserService;
import art.utils.ArtUtils;
import art.utils.UserEntity;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
@SessionAttributes({"languages", "domains", "selectedDomain"})
public class LoginController {

	final static Logger logger = LoggerFactory.getLogger(LoginController.class);
	@Autowired
	private UserService userService;

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String showLogin(HttpServletRequest request,
			@RequestParam(required = false) String authenticationMethod,
			Model model) {

		HttpSession session = request.getSession();

		if (!ArtConfig.isArtSettingsLoaded()) {
			//TODO change once refactoring is complete
			UserEntity ue = new UserEntity();
			ue.setAccessLevel(100);
			session.setAttribute("ue", ue);
			return "redirect:/admin/editSettings.jsp";
			//
		}

		String authenticationMethodSetting = ArtConfig.getAuthenticationMethod();

		if (authenticationMethod == null) {
			//authentication method not specified in url. use application setting
			authenticationMethod = authenticationMethodSetting;
		}

		AuthenticationMethod loginMethod = AuthenticationMethod.getEnum(authenticationMethod);

		//if auto login, no login page is displayed as user is authenticated by application server
		String username;
		String message;
		if (loginMethod == AuthenticationMethod.Auto) {
			//TODO also ensure app setting is auto? to avoid unintended, unauthorised access if machine not locked?
			//or add separate auto login allowed setting?
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
			String domains = ArtConfig.getArtSetting("mswin_domains");
			if (domains != null) {
				model.addAttribute("domains", domains);
			}
		}

		//store auth method in normal session attribute rather than spring session attribute
		//it will be used even after login
		session.setAttribute("authenticationMethod", loginMethod.getValue());

		//set available application languages
		//use a treemap so that languages are displayed in alphabetical order (of language codes)
		//don't include default (english)
		//see http://people.w3.org/rishida/names/languages.html for language names
		Map<String, String> languages = new TreeMap<String, String>();
		languages.put("es", "Español"); //spanish
		languages.put("fr", "Français"); //french
		languages.put("hu", "Magyar"); //hungarian
		languages.put("it", "Italiano"); //italian
		languages.put("pt_BR", "Português (Brasil)"); //brazilian portuguese
		languages.put("sw", "Kiswahili"); //swahili
		languages.put("zh_CN", "简体中文"); //simplified chinese
		languages.put("zh_TW", "繁體中文"); //traditional chinese

		model.addAttribute("languages", languages);

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
		if (windowsDomain != null) {
			//ModelMap.addAttribute() does not permit the attribute value to be null,
			//and will throw an IllegalArgumentException if it is
			model.addAttribute("selectedDomain", windowsDomain);
		}

		HttpSession session = request.getSession();
		String authenticationMethod = (String) session.getAttribute("authenticationMethod");
		AuthenticationMethod loginMethod = AuthenticationMethod.getEnum(authenticationMethod);

		String ip = request.getRemoteAddr();
		LoginHelper loginHelper = new LoginHelper();

		LoginResult result;

		if (loginMethod == AuthenticationMethod.Internal) {
			result = InternalLogin.authenticate(username, password);
		} else {
			//for external methods, try external and then internal authentication
			if (loginMethod == AuthenticationMethod.Database) {
				result = DbLogin.authenticate(username, password);
			} else if (loginMethod == AuthenticationMethod.Ldap) {
				result = LdapLogin.authenticate(username, password);
			} else if (loginMethod == AuthenticationMethod.WindowsDomain) {
				result = WindowsDomainLogin.authenticate(windowsDomain, username, password);
			} else {
				//enum has other possible values but they aren't relevant here
				//create default object
				result = new LoginResult();
			}

			if (!result.isAuthenticated()) {
				//external authentication failed
				//log external authentication failure
				loginHelper.log(loginMethod, result, username, ip);

				//try internal authentication
				loginMethod = AuthenticationMethod.Internal;
				result = InternalLogin.authenticate(username, password);
			}

		}

		//TODO test all authentication methods

		User user = null;
		if (result.isAuthenticated()) {
			//authentication successful. ensure user exists
			user = userService.getUser(username);
			if (user == null) {
				//user doesn't exist. update result status
				result = new LoginResult();
				result.setAuthenticated(false);
				result.setMessage("login.message.invalidUser");
				result.setDetails(ArtUtils.LOGIN_OK_INVALID_USER);
			} else if (!user.isActive()) {
				//user is disabled
				result = new LoginResult();
				result.setAuthenticated(false);
				result.setMessage("login.message.userDisabled");
				result.setDetails(ArtUtils.LOGIN_OK_USER_DISABLED);
			}
		}

		//log final login status
		loginHelper.log(loginMethod, result, username, ip);

		if (!result.isAuthenticated()) {
			//authentication failed or user doesn't exist or user is disabled
			//allow login if credentials match the repository user

			loginMethod = AuthenticationMethod.Repository;
			if (isValidRepositoryUser(username, password)) {
				user = new User();
				user.setAccessLevel(100); //repository user has super admin access

				result = new LoginResult();
				result.setAuthenticated(true);

				//log access using repository user
				loginHelper.logSuccess(loginMethod, username, ip);
			}
		}

		//finally, authentication process finished. display appropriate page
		if (!result.isAuthenticated()) {
			//login failure. always display invalid account message rather than actual result details
			//better for security if less details are displayed
			model.addAttribute("message", "login.message.invalidAccount");

			return "login";
		} else {
			//access granted 

			//TODO remove once refactoring is complete
			if (user != null) {
				UserEntity ue = new UserEntity(user.getUsername());
				ue.setAccessLevel(user.getAccessLevel());
				session.setAttribute("ue", ue);
				session.setAttribute("username", user.getUsername());
				if (user.getAccessLevel() >= 10) {
					session.setAttribute("AdminSession", "Y");
					session.setAttribute("AdminLevel", user.getAccessLevel());
					session.setAttribute("AdminUsername", user.getUsername());
				}
			}
			//

			session.setAttribute("sessionUser", user);
			session.setAttribute("authenticationMethod", loginMethod.getValue());

			//clear spring session attributes
			sessionStatus.setComplete();

			return "redirect:" + getNextPage(session);
		}
	}

	private String getNextPage(HttpSession session) {
		//TODO encode url. String nextPage = response.encodeRedirectURL((String) session.getAttribute("nextPage"));
		String nextPage = (String) session.getAttribute("nextPage");
		//remove nextpage attribute. 
		//it should only be set by the authorization filter, when the session expires
		session.removeAttribute("nextPage");

		if (nextPage == null) {
			nextPage = "/app/reports.do";
		}

		return nextPage;
	}

	private boolean isValidRepositoryUser(String username, String password) {
		boolean validRepositoryUser = false;

		if (StringUtils.equals(username, ArtConfig.getRepositoryUsername())
				&& StringUtils.equals(password, ArtConfig.getRepositoryPassword())
				&& StringUtils.isNotBlank(username)) {
			//repository user
			validRepositoryUser = true;
		}

		return validRepositoryUser;
	}
}
