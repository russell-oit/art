package art.servlets;

import art.login.AuthenticationMethod;
import art.user.User;
import art.user.UserService;
import art.utils.ArtUtils;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;

/**
 * Filter to ensure user has access to the requested page
 *
 * @author Timothy Anyona
 */
public class AuthorizationFilter implements Filter {

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

				AuthenticationMethod loginMethod = null;

				//test custom authentication
				String username = (String) session.getAttribute("username");

				if (username == null) {
					//not using custom authentication. test public user session
					if (request.getParameter("_public_user") != null) {
						username = ArtUtils.PUBLIC_USER; //hardcoded anonymous/guest user.
						loginMethod = AuthenticationMethod.Public;
					}
				} else {
					loginMethod = AuthenticationMethod.Custom;
				}

				if (username != null) {
					//either custom authentication or public user session
					//ensure user exists
					UserService userService = new UserService();
					user = userService.getUser(username);
					if (user == null) {
						message = "login.message.invalidAccount";
						//TODO log failure
					} else {
						//valid access
						//ensure public user always has 0 access level
						if (loginMethod == AuthenticationMethod.Public) {
							user.setAccessLevel(0);
						}
						session.setAttribute("sessionUser", user);
						session.setAttribute("authenticationMethod", loginMethod.getValue());
						//TODO log success
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
				String nextPage = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath());
				session.setAttribute("nextPage", nextPage);
				request.setAttribute("message", message);
				request.getRequestDispatcher("/login.do").forward(request, response);
				return;
			}

			//if we are here, user is authenticated
			//ensure they have access to the specific page. if not show access denied page
			if (canAccessPage(request, user)) {
				chain.doFilter(srequest, sresponse);
			} else {
				//show access denied page. 
				//use forward instead of redirect so that the intended url remains in the browser
				request.getRequestDispatcher("/app/accessDenied.do").forward(request, response);
			}
		}
	}

	private boolean canAccessPage(HttpServletRequest request, User user) {
		boolean authorized = false;

		int accessLevel = user.getAccessLevel();
		String contextPath = request.getContextPath();
		String requestUri = request.getRequestURI();
		String path = contextPath + "/app/";

		//TODO use permissions instead of access level
		if (StringUtils.startsWith(requestUri, path + "admin.do")) {
			//only admins can access
			if (accessLevel >= 10) {
				authorized = true;
			}
		} else if (StringUtils.startsWith(requestUri, path + "reports.do")) {
			//everyone can access
			//NOTE: "everyone" excludes the special codes when accessing as
			//the initial setup user (-1) and the art repository user (-2)
			if (accessLevel >= 0) {
				authorized = true;
			}
		} else if (StringUtils.startsWith(requestUri, path + "jobs.do")) {
			//everyone
			if (accessLevel >= 0) {
				authorized = true;
			}
		}

		return authorized;
	}
}
