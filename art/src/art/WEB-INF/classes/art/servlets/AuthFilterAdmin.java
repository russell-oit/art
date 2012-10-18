/*
 * Copyright (C) 2001/2003  Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   (version 2) along with this program (see documentation directory); 
 *   otherwise, have a look at http://www.gnu.org or write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */
package art.servlets;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter applied on accessing admin directory files
 *
 * @author Enrico Liboni
 */
public final class AuthFilterAdmin implements Filter {

	final static Logger logger = LoggerFactory.getLogger(AuthFilterAdmin.class);
	private FilterConfig filterConfig = null;

	/**
	 *
	 */
	@Override
	public void destroy() {
		this.filterConfig = null;
	}

	/**
	 *
	 * @param filterConfig
	 * @throws ServletException
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
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
			FilterChain chain)
			throws IOException, ServletException {

		if (request instanceof HttpServletRequest) {
			HttpServletRequest hrequest = (HttpServletRequest) request;
			HttpSession session = hrequest.getSession();

			if (ArtDBCP.isArtSettingsLoaded()) { // properties are defined

				if (session.getAttribute("AdminSession") != null) {
					// if the admin connection is not in the session 
					// get the connection and store it in the admin session 
					if (session.getAttribute("SessionConn") == null) {
						try {
							session.setAttribute("SessionConn", ArtDBCP.getAdminConnection());
						} catch (Exception e) {
							logger.error("Error while getting the connection to the ART repository", e);
							PrintWriter out = response.getWriter();
							out.println("<html> Error while getting the connection to the ART repository<br><code>" + e + "</code></html>");
						}
					}
					chain.doFilter(request, response);
				} else {
					//display appropriate login page
					HttpServletResponse hresponse = (HttpServletResponse) response;
					java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages", hrequest.getLocale());
					hrequest.setAttribute("message", messages.getString("sessionExpired"));
					String toPage = ArtDBCP.getArtSetting("index_page_default");
					if (toPage == null || toPage.equals("default")) {
						toPage = "login";
					}
					hrequest.getRequestDispatcher("/" + toPage + ".jsp").forward(hrequest, hresponse);
				}

			} else {
				// properties are not defined - this is the 1st logon (see execLogin.jsp)
				session.setAttribute("AdminSession", "Y");
				session.setAttribute("AdminLevel", new Integer(100));
				session.setAttribute("AdminUsername", "art");
				chain.doFilter(request, response);
			}
		}
	}
}
