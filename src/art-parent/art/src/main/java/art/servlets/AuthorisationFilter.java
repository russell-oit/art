package art.servlets;

import art.enums.AccessLevel;
import art.enums.ArtAuthenticationMethod;
import art.login.LoginHelper;
import art.user.User;
import art.user.UserService;
import art.utils.ArtUtils;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Filter to ensure user has access to the requested page
 *
 * @author Timothy Anyona
 */
public class AuthorisationFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(AuthorisationFilter.class);

	/**
	 *
	 */
	@Override
	public void destroy() {
	}

	/**
	 *
	 * @param filterConfig
	 * @throws ServletException
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	/**
	 * Ensure user has access to the requested page
	 *
	 * @param srequest
	 * @param sresponse
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doFilter(ServletRequest srequest, ServletResponse sresponse,
			FilterChain chain) throws IOException, ServletException {

		if (srequest instanceof HttpServletRequest && sresponse instanceof HttpServletResponse) {
			HttpServletRequest request = (HttpServletRequest) srequest;
			HttpServletResponse response = (HttpServletResponse) sresponse;

			HttpSession session = request.getSession();

			String message = null;

			User user = (User) session.getAttribute("sessionUser");
			if (user == null) {
				//custom authentication, public user session or session expired

				ArtAuthenticationMethod loginMethod = null;

				//test custom authentication
				String username = (String) session.getAttribute("username");

				if (username == null) {
					//not using custom authentication. test public user session
					if (request.getParameter("public_user") != null
							|| request.getParameter("_public_user") != null) {
						username = ArtUtils.PUBLIC_USER;
						loginMethod = ArtAuthenticationMethod.Public;
					}
				} else {
					loginMethod = ArtAuthenticationMethod.Custom;
				}

				if (username != null && loginMethod != null) {
					//either custom authentication or public user session
					//ensure user exists
					LoginHelper loginHelper = new LoginHelper();
					String ip = request.getRemoteAddr();

					UserService userService = new UserService();
					try {
						user = userService.getUser(username);
					} catch (SQLException ex) {
						logger.error("Error", ex);
					}
					if (user == null) {
						//user doesn't exist
						//always display invalidAccount message in login page. log actual cause
						message = "login.message.invalidCredentials";
						//log failure
						loginHelper.logFailure(loginMethod, username, ip, ArtUtils.ART_USER_INVALID);
					} else if (!user.isActive()) {
						//user disabled
						//always display invalidAccount message in login page. log actual cause
						message = "login.message.invalidCredentials";
						//log failure
						loginHelper.logFailure(loginMethod, username, ip, ArtUtils.ART_USER_DISABLED);
					} else {
						//valid access
						//ensure public user always has normal user access level
						if (loginMethod == ArtAuthenticationMethod.Public) {
							user.setAccessLevel(AccessLevel.NormalUser);
						}
						session.setAttribute("sessionUser", user);
						session.setAttribute("authenticationMethod", loginMethod.getValue());
						session.setAttribute("administratorEmail", ArtConfig.getSettings().getAdministratorEmail());
						//log success
						loginHelper.logSuccess(loginMethod, username, ip);
					}
				} else {
					//session expired or just unauthorized access attempt
					message = "login.message.sessionExpired";
				}
			}

			if (user == null) {
				//no valid user available

				//forward to login page. 
				//use forward instead of redirect so that an indication of the
				//page that was being accessed remains in the browser
				//remember the page the user tried to access in order to forward after the authentication
				//use relative path (without context path).
				//that's what redirect in login controller needs
				String nextPageAfterLogin = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
				if (request.getQueryString() != null) {
					nextPageAfterLogin = nextPageAfterLogin + "?" + request.getQueryString();
				}
				session.setAttribute("nextPageAfterLogin", nextPageAfterLogin);
				request.setAttribute("message", message);
				request.getRequestDispatcher("/login.do").forward(request, response);
				return;
			}

			//if we are here, user is authenticated
			//ensure they have access to the specific page. if not show access denied page
			String requestUri = request.getRequestURI();
			String path = request.getContextPath() + "/app/";
			String page = StringUtils.substringBetween(requestUri, path, ".do");

			if (canAccessPage(page, user, session)) {
				if (!ArtConfig.isArtDatabaseConfigured()) {
					//if art database not configured, only allow access to artDatabase.do
					if (!StringUtils.equals(page, "artDatabase")) {
						request.setAttribute("message", "page.message.artDatabaseNotConfigured");
						request.getRequestDispatcher("/app/accessDenied.do").forward(request, response);
						return;
					}
				}
				//authorisation is ok. display page

				//enable display of custom attributes in logs
				addMdcAttributes(request, user);
				try {
					chain.doFilter(srequest, sresponse);
				} finally {
					removeMdcAttributes();
				}
			} else {
				//show access denied page. 
				//use forward instead of redirect so that the intended url remains in the browser
				request.getRequestDispatcher("/app/accessDenied.do").forward(request, response);
			}
		}
	}

	/**
	 * Add mdc attributes
	 *
	 * @param request
	 * @param user
	 */
	private void addMdcAttributes(HttpServletRequest request, User user) {
		//http://logback.qos.ch/manual/mdc.html
		//http://logback.qos.ch/xref/ch/qos/logback/classic/helpers/MDCInsertingServletFilter.html
		MDC.put("user", user.getUsername());
		MDC.put("remoteAddr", request.getRemoteAddr());
		MDC.put("requestURI", request.getRequestURI());
		MDC.put("xForwardedFor", request.getHeader("X-Forwarded-For"));
	}

	/**
	 * Clear mdc attributes.
	 */
	private void removeMdcAttributes() {
		//everything added to mdc should be removed here
		MDC.remove("user");
		MDC.remove("remoteAddr");
		MDC.remove("requestURI");
		MDC.remove("xForwardedFor");
	}

	private boolean canAccessPage(String page, User user, HttpSession session) {
		if (user.getAccessLevel() == null) {
			return false;
		}

		boolean authorised = false;

		int accessLevel = user.getAccessLevel().getValue();

		//TODO use permissions instead of access level
		if (StringUtils.equals(page, "reports")) {
			//everyone can access
			//NOTE: "everyone" doesn't include when accessing as the art repository user
			if (accessLevel >= AccessLevel.NormalUser.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "jobs")) {
			//everyone
			if (accessLevel >= AccessLevel.NormalUser.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "logs")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "users") || StringUtils.endsWith(page, "User")) {
			//users.do, addUser.do, editUser.do, deleteUser.do
			//standard admins and above, and repository user
			if (accessLevel >= AccessLevel.StandardAdmin.getValue()
					|| accessLevel == AccessLevel.RepositoryUser.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "artDatabase")) {
			//super admins, and repository user
			if (accessLevel == AccessLevel.SuperAdmin.getValue()
					|| accessLevel == AccessLevel.RepositoryUser.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "settings")) {
			//super admins
			if (accessLevel == AccessLevel.SuperAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "serverInfo")) {
			//super admins
			if (accessLevel == AccessLevel.SuperAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "language")) {
			//all can access
			authorised = true;
		} else if (StringUtils.equals(page, "password")) {
			//everyone, plus user can change password, plus internal authentication
			String authenticationMethod = (String) session.getAttribute("authenticationMethod");
			if (user.isCanChangePassword() && StringUtils.equals(authenticationMethod, ArtAuthenticationMethod.Internal.getValue())) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "success")) {
			//all can access
			authorised = true;
		} else if (StringUtils.equals(page, "accessDenied")) {
			//all can access
			authorised = true;
		} else if (StringUtils.equals(page, "userGroups") || StringUtils.endsWith(page, "UserGroup")) {
			//standard admins and above
			if (accessLevel >= AccessLevel.StandardAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "datasources") || StringUtils.endsWith(page, "Datasource")
				|| StringUtils.equals(page, "testDatasource")) {
			//senior admins and above
			if (accessLevel >= AccessLevel.SeniorAdmin.getValue()) {
				authorised = true;
			}
		} else if (StringUtils.equals(page, "reportsConfig") || StringUtils.endsWith(page, "Report")) {
			//junior admins and above
			if (accessLevel >= AccessLevel.JuniorAdmin.getValue()) {
				authorised = true;
			}
		}

		return authorised;
	}
}
