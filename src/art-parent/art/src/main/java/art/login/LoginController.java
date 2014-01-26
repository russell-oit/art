package art.login;

import art.enums.AccessLevel;
import art.enums.ArtAuthenticationMethod;
import art.servlets.ArtConfig;
import art.user.User;
import art.user.UserService;
import art.utils.ArtUtils;
import art.utils.DbUtils;
import art.language.LanguageUtils;
import art.utils.UserEntity;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@SessionAttributes({"domains", "selectedDomain", "selectedUsername"})
public class LoginController {

	final static Logger logger = LoggerFactory.getLogger(LoginController.class);
	@Autowired
	private UserService userService;

	@ModelAttribute("languages")
	public Map<String, String> addLanguages() {
		return LanguageUtils.getLanguages();
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String showLogin(HttpServletRequest request,
			@RequestParam(value = "authenticationMethod", required = false) String authenticationMethod,
			Model model,
			SessionStatus sessionStatus) {

		HttpSession session = request.getSession();

		if (!ArtConfig.isArtDatabaseConfigured()) {
			User user = new User();

			user.setUsername("initial setup");
			user.setAccessLevel(AccessLevel.RepositoryUser);

			session.setAttribute("sessionUser", user);
			session.setAttribute("initialSetup", "true");

			return "redirect:/app/artDatabase.do";
		}

		//set administrator email
		session.setAttribute("administratorEmail", ArtConfig.getSettings().getAdministratorEmail());

		//ensure art database connection is available
		Connection conn = ArtConfig.getConnection();
		if (conn == null) {
			model.addAttribute("message", "page.message.artDatabaseConnectionNotAvailable");
			return "headerlessError";
		} else {
			DbUtils.close(conn);
		}

		ArtAuthenticationMethod loginMethod;
		ArtAuthenticationMethod loginMethodAppSetting = ArtConfig.getSettings().getArtAuthenticationMethod();
		if (authenticationMethod == null) {
			//authentication method not specified in url. use application setting
			loginMethod = loginMethodAppSetting;
		} else {
			loginMethod = ArtAuthenticationMethod.getEnum(authenticationMethod);
		}

		//if auto login, no login page is displayed as user is authenticated by application server
		if (loginMethod == ArtAuthenticationMethod.Auto && loginMethodAppSetting == ArtAuthenticationMethod.Auto) {
			//ensure app setting is auto and not just url parameter. 
			//to avoid unintended, unknowing unauthorised access if machine not locked?

			String ip = request.getRemoteAddr();
			LoginHelper loginHelper = new LoginHelper();

			LoginResult result;

			//check if user is authenticated
			String username = request.getRemoteUser();

			if (StringUtils.isNotBlank(username)) {
				//user authenticated. ensure they are a valid ART user
				User user = null;
				try {
					user = userService.getUser(username);
				} catch (SQLException ex) {
					logger.error("Error", ex);
					model.addAttribute("error", ex);
				}

				if (user == null) {
					//user doesn't exist
					result = new LoginResult();
					result.setDetails(ArtUtils.ART_USER_INVALID);
				} else if (!user.isActive()) {
					//user is disabled
					result = new LoginResult();
					result.setDetails(ArtUtils.ART_USER_DISABLED);
				} else {
					//valid user
					//log access
					loginHelper.logSuccess(loginMethod, username, ip);

					//go to next page
					return getLoginSuccessNextPage(session, user, loginMethod, sessionStatus);
				}
			} else {
				//user not authenticated. should never get here as browser won't have authenticated?
				result = new LoginResult();
				result.setDetails("invalid user");
			}

			//if we are here auto login failed or invalid user or disabed user
			//log failure
			loginHelper.logFailure(loginMethod, username, ip, result.getDetails());

			//give message and change default to internal login
			model.addAttribute("invalidAutoLogin", "");
			model.addAttribute("autoLoginUser", username);
		}

		if (loginMethod == ArtAuthenticationMethod.WindowsDomain) {
			String domains = ArtConfig.getSettings().getAllowedWindowsDomains();
			//not allowed to add null attribute to model
			if (domains != null) {
				model.addAttribute("domains", domains);
			}
		} else if (loginMethod == ArtAuthenticationMethod.Auto) {
			//if we are here with auto login, authentication failed or not valid
			//change default to internal login
			loginMethod = ArtAuthenticationMethod.Internal;
		}

		//store auth method in normal session attribute rather than spring session attribute
		//it will be used even after login
		session.setAttribute("authenticationMethod", loginMethod.getValue());

		return "login";
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String processLogin(
			HttpServletRequest request,
			@RequestParam(value = "windowsDomain", required = false) String windowsDomain,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			Model model,
			SessionStatus sessionStatus) {

		//explicitly name requestparams to avoid error if code compiled without debug option
		//see http://www.java-allandsundry.com/2012/10/method-parameter-names-and-spring.html
		//windowsDomain request parameter may not be in the request parameters. 
		//only available with windows domain authentication
		if (windowsDomain != null) {
			//ModelMap.addAttribute() does not permit the attribute value to be null,
			//and will throw an IllegalArgumentException if it is
			model.addAttribute("selectedDomain", windowsDomain);
		}
		model.addAttribute("selectedUsername", username);

		if (StringUtils.isBlank(username)) {
			//likely just attempt to change the language
			//don't attempt to authenticate. just redisplay login page
			return "login";
		}

		HttpSession session = request.getSession();
		String authenticationMethod = (String) session.getAttribute("authenticationMethod");
		ArtAuthenticationMethod loginMethod = ArtAuthenticationMethod.getEnum(authenticationMethod);

		String ip = request.getRemoteAddr();
		LoginHelper loginHelper = new LoginHelper();

		LoginResult result;

		User user = null;
		try {
			user = userService.getUser(username);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		if (user == null) {
			//user doesn't exist
			result = new LoginResult();
			result.setAuthenticated(false);
			result.setMessage("login.message.artUserInvalid");
			result.setDetails(ArtUtils.ART_USER_INVALID);
		} else if (!user.isActive()) {
			//user is disabled
			result = new LoginResult();
			result.setAuthenticated(false);
			result.setMessage("login.message.artUserDisabled");
			result.setDetails(ArtUtils.ART_USER_DISABLED);
		} else {
			if (loginMethod == ArtAuthenticationMethod.Internal) {
				result = InternalLogin.authenticate(username, password);
			} else if (loginMethod == ArtAuthenticationMethod.Database) {
				result = DbLogin.authenticate(username, password);
			} else if (loginMethod == ArtAuthenticationMethod.LDAP) {
				result = LdapLogin.authenticate(username, password);
			} else if (loginMethod == ArtAuthenticationMethod.WindowsDomain) {
				result = WindowsDomainLogin.authenticate(windowsDomain, username, password);
			} else {
				//enum has other possible values but they aren't relevant here
				//create default object
				result = new LoginResult();
			}
		}

		//log result
		loginHelper.log(loginMethod, result, username, ip);

		if (!result.isAuthenticated() && user != null && loginMethod != ArtAuthenticationMethod.Internal) {
			//external authentication failed. try internal authentication
			result = InternalLogin.authenticate(username, password);
			if (result.isAuthenticated()) {
				//log access using internal authentication
				loginMethod = ArtAuthenticationMethod.Internal;
				loginHelper.logSuccess(loginMethod, username, ip);
			}
		}

		if (!result.isAuthenticated()) {
			//authentication failed or user doesn't exist or user is disabled
			//allow login if credentials match the repository user
			if (isValidRepositoryUser(username, password)) {
				loginMethod = ArtAuthenticationMethod.Repository;
				user = new User();
				user.setUsername("art db");
				user.setAccessLevel(AccessLevel.RepositoryUser);

				result = new LoginResult();
				result.setAuthenticated(true);

				//log access using repository user
				loginHelper.logSuccess(loginMethod, username, ip);
			}
		}

		//finally, authentication process finished. display appropriate page
		if (result.isAuthenticated() && user != null) {
			//access granted 

			return getLoginSuccessNextPage(session, user, loginMethod, sessionStatus);
		} else {
			//login failure. always display invalid account message rather than actual result details
			//better for security if less details are displayed
			model.addAttribute("invalidLogin", "");
			
			//add result to be available for display, although not displayed
			model.addAttribute("result", result); 

			return "login";

		}
	}

	private String getLoginSuccessNextPage(HttpSession session, User user,
			ArtAuthenticationMethod loginMethod, SessionStatus sessionStatus) {

		//set session attributes
		session.setAttribute("sessionUser", user);
		session.setAttribute("authenticationMethod", loginMethod.getValue());

		//clear spring session attributes
		sessionStatus.setComplete();

		//get next page
		String nextPageAfterLogin = (String) session.getAttribute("nextPageAfterLogin");
		//remove nextpage attribute. 
		//it should only be set by the authorization filter, when the session expires
		session.removeAttribute("nextPageAfterLogin");

		if (nextPageAfterLogin == null) {
			nextPageAfterLogin = "/app/reports.do";
		}

		return "redirect:" + nextPageAfterLogin;
	}

	/**
	 * Determine if given credentials match those of the art database user
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean isValidRepositoryUser(String username, String password) {
		boolean validRepositoryUser = false;

		String artDbUsername = ArtConfig.getArtDatabaseConfiguration().getUsername();
		String artDbPassword = ArtConfig.getArtDatabaseConfiguration().getPassword();
		if (StringUtils.equals(username, artDbUsername)
				&& StringUtils.equals(password, artDbPassword)
				&& StringUtils.isNotBlank(username)) {
			//repository user
			validRepositoryUser = true;
		}

		return validRepositoryUser;
	}
}
