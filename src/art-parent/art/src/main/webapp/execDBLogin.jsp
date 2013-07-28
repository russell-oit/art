<%@ page import="java.io.*, java.sql.*, art.servlets.ArtDBCP" %>
<%@ page import="java.util.ResourceBundle" %>

<% request.setCharacterEncoding("UTF-8");%>

<%
	/**
	 * **************************************************************
	 * Database Authentication
	 *
	 * Validate username/password towards a database
	 *
	 */
	ResourceBundle messages = ResourceBundle.getBundle("art.i18n.ArtMessages", request.getLocale());

	String url = ArtDBCP.getArtSetting("jdbc_auth_url");
	String username = request.getParameter("dbusername");

	String nextPage = request.getParameter("nextPage");
	session.setAttribute("nextPage", nextPage);

	try {
		Connection conn = DriverManager.getConnection(url, username, request.getParameter("dbpassword"));
		// If no exception has been raised at this point 
		// the authentication is successful
		session.setAttribute("username", username);
		// redirect:
		//remove nextpage attribute to prevent endless redirection to login page for /admin pages
		session.removeAttribute("nextPage");
		response.sendRedirect(nextPage);

	} catch (Exception e) { // Exception
		String str = "#EDB " + username + " - " + request.getRemoteAddr() + "\n";
		System.out.println(str);
		e.printStackTrace(System.out);
		
		String message = "<br><small>Details: " + e + "</small>";
		request.setAttribute("message", messages.getString("invalidAccount") + message);
		request.getRequestDispatcher("/DBLogin.jsp").forward(request, response);
	}
%>


<%!
	/**
	 * *********************************************************************
	 *
	 * init, destroy and static functions
	 *
	 */
	public void jspInit() {

		// Load the JDBC Driver (it might be different from the other driver already
		// loaded for the Art repository or Target datasources
		try {
			Class.forName(ArtDBCP.getArtSetting("jdbc_auth_driver")).newInstance();
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
		}
	}

%>
