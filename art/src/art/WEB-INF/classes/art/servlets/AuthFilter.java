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

import art.utils.ArtException;
import art.utils.Encrypter;
import art.utils.UserEntity;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Filter applied on accessing user directory files 
 * 
 * @author Enrico Liboni 
 */
public final class AuthFilter implements Filter {

    final static Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    private FilterConfig filterConfig = null;
    private boolean isArtSuperUser;

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
     * Check if this is a valid user session
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

        if (request instanceof HttpServletRequest) {
            HttpServletRequest hrequest = (HttpServletRequest) request;
            HttpServletResponse hresponse = (HttpServletResponse) response;
            HttpSession session = hrequest.getSession();
            if (session.getAttribute("ue") == null) {
                // Let's authenticate it
                if (!ArtDBCP.isArtSettingsLoaded()) {
                    // properties not defined: 1st Logon -> go to adminConsole.jsp (passing through the AuthFilterAdmin)
                    hresponse.sendRedirect(hresponse.encodeRedirectURL(hrequest.getContextPath() + "/admin/adminConsole.jsp"));
                    return; // !!!! this need to be here!!!
                }
                isArtSuperUser = false;
                try {
                    AuthenticateSession(hrequest);
                    if (isArtSuperUser) {
                        hresponse.sendRedirect(hresponse.encodeRedirectURL(hrequest.getContextPath() + "/admin/adminConsole.jsp"));
                        return;
                    } else {
                        // auth ok
                        chain.doFilter(request, response);
                    }
                } catch (ArtException ae) {
                    // auth failure
                    // cache the page the user tried to access in order to fwd after the authentication
                    String nextPage = hrequest.getRequestURI();
                    if (hrequest.getQueryString() != null) {
                        nextPage = nextPage + "?" + hrequest.getQueryString();
                    }
                    session.setAttribute("nextPage", nextPage);
                    forwardPage(hresponse, hrequest, ae.getMessage());
                } catch (Exception e) {
                    logger.error("Error", e);
                    forwardPage(hresponse, hrequest, e.getMessage());
                }
            } else {
                // Already Authenticated
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
    private void forwardPage(HttpServletResponse hresponse, HttpServletRequest hrequest, String msg) throws ServletException, IOException {
        hrequest.setAttribute("message", msg);
        String toPage = ArtDBCP.getArtSetting("index_page_default");
        if (toPage == null || toPage.equals("default")) {
            toPage = "login";
        }
        hrequest.getRequestDispatcher("/" + toPage + ".jsp").forward(hrequest, hresponse);
    }

    /**
     * Authenticate the session.
     * 
     * @param request
     * @throws ArtException if couldn't authenticate
     * @throws Exception 
     */
    private void AuthenticateSession(HttpServletRequest request) throws ArtException, Exception {

        /* Logic (an exception is generated if any action goes wrong)
        
        if username/password are provided
        -> check if they are for the ART superadmin
        -> validate the credentials
        else if username is in the session and the param 'external' is available
        -> this is an "external authenticated" session
        check if the username is an art user too
        end
        
        create a UserEntity UE and store it in the session
        store "username" attribute in the session as well
        if is an admin session initialize Admin attributes
         */

        HttpSession session = request.getSession();
        ResourceBundle messages = ResourceBundle.getBundle("art.i18n.ArtMessages", request.getLocale());

        int adminlevel = -1;
        boolean internalAuthentication = true;

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        /* *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** */
        if (username!=null && password != null) {  // INTERNAL AUTHENTICATION ( a username and password is available)
                        
            // check if the credentials match the ART Reposority username/password
            if (username.equals(ArtDBCP.getArtRepositoryUsername())
                    && password.equals(ArtDBCP.getArtRepositoryPassword()) && StringUtils.isNotBlank(username)) {
                // this is the powerful ART super admin !
                // no need to authenticate it
                adminlevel = 100;
                isArtSuperUser = true;
                ArtDBCP.log(username, "login", request.getRemoteAddr(), "internal-superadmin, level: " + adminlevel);
            } else { // begin normal internal authentication
				/*
                 * Let's verify if username and password are valid
                 */
                Connection c = null;
                try {
                    //default to using bcrypt instead of md5 for password hashing
                    //password = digestString(password, "MD5");

                    String SqlQuery = "SELECT ADMIN_LEVEL, PASSWORD, HASHING_ALGORITHM FROM ART_USERS "
                            + "WHERE USERNAME = ? AND (ACTIVE_STATUS = 'A' OR ACTIVE_STATUS IS NULL)";
                    c = ArtDBCP.getConnection();

                    // ART Repository Down !!!
                    if (c == null) {
                        throw new ArtException(messages.getString("invalidConnection"));
                    }

                    PreparedStatement ps = c.prepareStatement(SqlQuery);
                    ps.setString(1, username);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        //user exists. verify password
                        if (Encrypter.VerifyPassword(password, rs.getString("PASSWORD"), rs.getString("HASHING_ALGORITHM"))) {
                            // ----------------------------------------------------AUTHENTICATED!

                            adminlevel = rs.getInt("ADMIN_LEVEL");

                            session.setAttribute("username", username); // store username in the session

                            ArtDBCP.log(username, "login", request.getRemoteAddr(), "internal, level: " + adminlevel);
                        } else {
                            //wrong password
                            ArtDBCP.log(username, "loginerr", request.getRemoteAddr(), "internal, failed");
                            throw new ArtException(messages.getString("invalidAccount"));
                        }

                    } else {
                        //user doesn't exist
                        ArtDBCP.log(username, "loginerr", request.getRemoteAddr(), "internal, failed");
                        throw new ArtException(messages.getString("invalidAccount"));
                    }
                    rs.close(); // note: in case of invalid password this line is not reached, but ART DBCP will automatically close the open statements when the connection is returned to the pool in the finally below
                    ps.close();
                } catch (SQLException e) {
                    logger.error("Error", e);
                    throw e;
                } finally {
                    try {
                        if (c != null) {
                            c.close();
                        }
                    } catch (Exception e) {
                        logger.error("Error", e);
                    }
                }
            } // end internal authentication
			/* *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** *** */
        } else if (session.getAttribute("username") != null) { // EXTERNAL AUTHENTICATION ( a username in session is available)
			/* a username is already in the session, but the ue object is not
             * let's get other info and authenticate it
             */
            Connection c = null;
            try {
                username = (String) session.getAttribute("username");
                String SqlQuery = ("SELECT ADMIN_LEVEL FROM ART_USERS "
                        + " WHERE USERNAME = ? AND (ACTIVE_STATUS = 'A' OR ACTIVE_STATUS IS NULL) ");
                c = ArtDBCP.getConnection();
                // ART Repository Down !!!
                if (c == null) {
                    throw new ArtException(messages.getString("invalidConnection"));
                }

                PreparedStatement ps = c.prepareStatement(SqlQuery);
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    // ----------------------------------------------------AUTHENTICATED!

                    adminlevel = rs.getInt("ADMIN_LEVEL");
                    internalAuthentication = false;

                    ArtDBCP.log(username, "login", request.getRemoteAddr(), "external, level: " + adminlevel);
                } else {
                    ArtDBCP.log(username, "loginerr", request.getRemoteAddr(), "external, failed");
                    throw new ArtException(messages.getString("invalidUser"));
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                logger.error("Error", e);
                throw new Exception(e.getMessage());
            } finally {
                try {
                    if (c != null) {
                        c.close();
                    }
                } catch (Exception e) {
                    logger.error("Error", e);
                }
            }
        } else {
            // if the request is for a public_user session
            // create it...
            if (request.getParameter("_public_user") != null) {
                username = "public_user";

                adminlevel = 0;
                internalAuthentication = true;
            } else {
                // ... otherwise this is a session expired / unauthorized access attempt...
                throw new ArtException(messages.getString("sessionExpired"));
            }
        }

        // if no ArtExcpetion have been generated so far, the session is authenticated
        // Create the UserEntity object and store it in the session
        UserEntity ue = new UserEntity(username);

        //override some properties
        ue.setAdminLevel(adminlevel);
        ue.setInternalAuth(internalAuthentication);

        session.setAttribute("ue", ue);
        session.setAttribute("username", username);

        // Set admin session
        if (adminlevel > 5) {
            session.setAttribute("AdminSession", "Y");
            session.setAttribute("AdminLevel", new Integer(adminlevel));
            session.setAttribute("AdminUsername", username);
        }

    }
}
