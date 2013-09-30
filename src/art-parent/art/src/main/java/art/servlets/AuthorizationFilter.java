package art.servlets;

import art.utils.UserEntity;
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

			UserEntity user = (UserEntity) session.getAttribute("ue");
			if (user == null) {
				//user not authenticated or session expired
				if (srequest.getParameter("_public_user") != null) {
					//allow public user access
					String username = "public_user";
					user = new UserEntity(username);
					user.setAccessLevel(0);
					session.setAttribute("ue", user);
					session.setAttribute("username", username);
				} else {
					//redirect to login page. 
					//give session expired message, although it may just be unauthorized access attempt

					//remember the page the user tried to access in order to forward after the authentication
					String nextPage = request.getRequestURI();
					if (request.getQueryString() != null) {
						nextPage = nextPage + "?" + request.getQueryString();
					}
					session.setAttribute("nextPage", nextPage);
					request.setAttribute("message", "login.message.sessionExpired");
					response.sendRedirect("/login.do");
					return;
				}
			}

			//if we are here, user is authenticated
			//ensure they have access to the specific page. if not redirect to home page
			//request.getPathInfo() doesn't work in filters
			//TODO or show accessdenied page?

			boolean canAccessPage = false;
			int accessLevel = user.getAccessLevel();
			String contextPath = request.getContextPath();
			String requestUri = request.getRequestURI();
			String path = contextPath + "/app/";

			//TODO use permissions instead of access level
			if (StringUtils.startsWith(requestUri, path + "admin.do")) {
				if (accessLevel >= 10) {
					canAccessPage = true;
				}
			}

			if (canAccessPage) {
				chain.doFilter(srequest, sresponse);
			} else {
				response.sendRedirect(response.encodeRedirectURL(contextPath + "/user/showGroups.jsp"));
			}
		}
	}
}
