/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.servlets;

import art.utils.ArtHelper;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter applied on accessing admin directory files
 *
 * @author Enrico Liboni
 */
public final class AuthFilterAdmin implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(AuthFilterAdmin.class);

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
	 * Check if this is a valid admin session
	 *
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			HttpServletRequest hrequest = (HttpServletRequest) request;
			HttpServletResponse hresponse = (HttpServletResponse) response;
			HttpSession session = hrequest.getSession();

			if (ArtConfig.isArtSettingsLoaded()) {
				// settings are defined
				if (session.getAttribute("AdminSession") != null) {
					// if the admin connection is not in the session 
					// get the connection and store it in the admin session 
					if (session.getAttribute("SessionConn") == null) {
						try {
							session.setAttribute("SessionConn", ArtConfig.getAdminConnection());
						} catch (Exception e) {
							logger.error("Error while getting the connection to the ART repository", e);
							PrintWriter out = response.getWriter();
							out.println("<html> Error while getting the connection to the ART repository<br><code>" + e + "</code></html>");
						}
					}
					chain.doFilter(request, response);
				} else {
					//check if we have come from a login page
					String username = hrequest.getParameter("username");
					String password = hrequest.getParameter("password");
					String sessionUser = (String) session.getAttribute("username");
					if (username != null || sessionUser != null) {
						//we have come from a login page. authenticate
						try {
							String msg = ArtHelper.authenticateSession(hrequest);
							if (msg == null) {
								//no error messages. authentication succeeded
								ArtHelper artHelper = new ArtHelper();
								boolean isArtRepositoryUser = artHelper.isValidRepositoryUser(username, password);
								if (isArtRepositoryUser) {
									hresponse.sendRedirect(hresponse.encodeRedirectURL(hrequest.getContextPath() + "/admin/adminConsole.jsp"));
									return; //not needed but retained in case code changes later giving execution path after redirect
								} else {
									// auth ok
									//ensure this is an admin user
									if (session.getAttribute("AdminSession") != null) {
										//this is an admin user
										chain.doFilter(request, response);
									} else {
										//this is not an admin user. go to start page
										hresponse.sendRedirect(hresponse.encodeRedirectURL(hrequest.getContextPath() + "/user/showGroups.jsp"));
										return; //not needed but retained in case code changes later giving execution path after redirect
									}
								}
							} else {
								//authentication failed. display error message
								//remember the page the user tried to access in order to forward after the authentication
//								String nextPage = hrequest.getRequestURI();
//								if (hrequest.getQueryString() != null) {
//									nextPage = nextPage + "?" + hrequest.getQueryString();
//								}
								String nextPage = StringUtils.substringAfter(hrequest.getRequestURI(), hrequest.getContextPath());
								session.setAttribute("nextPage", nextPage);

								//display appropriate login page
								forwardToLoginPage(hresponse, hrequest, msg);
							}
						} catch (Exception e) {
							logger.error("Error", e);
							forwardToLoginPage(hresponse, hrequest, e.getMessage());
						}
					} else {
						//we have not come from a login page. display appropriate login page
						//remember the page the user tried to access in order to forward after the authentication
//						String nextPage = hrequest.getRequestURI();
//						if (hrequest.getQueryString() != null) {
//							nextPage = nextPage + "?" + hrequest.getQueryString();
//						}
						String nextPage = StringUtils.substringAfter(hrequest.getRequestURI(), hrequest.getContextPath());
						session.setAttribute("nextPage", nextPage);

						java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("i18n.ArtMessages", hrequest.getLocale());
						forwardToLoginPage(hresponse, hrequest, messages.getString("sessionExpired"));
					}
				}

			} else {
				// settings are not defined - this is the 1st logon (see execLogin.jsp)
				session.setAttribute("AdminSession", "Y");
				session.setAttribute("AdminLevel", Integer.valueOf(100));
				session.setAttribute("AdminUsername", "art");
				chain.doFilter(request, response);
			}
		}
	}

	/**
	 * Forward to the appropriate login page.
	 *
	 * @param hresponse http response
	 * @param hrequest http request
	 * @param msg message to display
	 * @throws ServletException
	 * @throws IOException
	 */
	private void forwardToLoginPage(HttpServletResponse hresponse, HttpServletRequest hrequest, String msg) throws ServletException, IOException {
		hrequest.setAttribute("messageDetails", msg);
		hrequest.getRequestDispatcher("/login.do").forward(hrequest, hresponse);
	}
}
