<%@ page import="java.io.*, art.utils.ValidateNTLogin, art.servlets.ArtConfig" %>
<%@ page import="java.util.ResourceBundle" %>

<% request.setCharacterEncoding("UTF-8");%>

<%
	/**
	 * **************************************************************
	 * WINDOWS DOMAIN CONTROLLER Authentication
	 *
	 */
	ResourceBundle messages = ResourceBundle.getBundle("i18n.ArtMessages", request.getLocale());

	String domain = request.getParameter("ntdomain");
	String username = request.getParameter("ntusername");
	String password = request.getParameter("ntpassword");

	String nextPage = request.getParameter("nextPage");
	session.setAttribute("nextPage", nextPage);

	if (username == null || password == null || domain == null) {
		request.setAttribute("message", messages.getString("invalidAccount"));
	} else if (errOnInit) {
		request.setAttribute("message", errOnInitMessage);
	} else {
		try {
			if (vnl.isValidNTLogin(domain, username, password)) {
				// Autentication Successful!
				session.setAttribute("username", username);
				// redirect:
				//remove nextpage attribute to prevent endless redirection to login page for /admin pages
				session.removeAttribute("nextPage");
				response.sendRedirect(nextPage);
				return; // this must stay here 
			} else {
				request.setAttribute("message", messages.getString("invalidAccount"));
				// log wrong login attempt
				String str = "#ENT " + username + " - " + request.getRemoteAddr() + "\n";
				System.out.println(str);
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			
			String message = "<small>Details: " + e + "</small>";
			request.setAttribute("message", message);
		}
	}
// nextPage is in the session and drives the behaviour of the xxLogin.jsp page
	request.getRequestDispatcher("/NTLogin.jsp").forward(request, response);
%>


<%!	/**
	 * *********************************************************************
	 *
	 * init, destroy and static functions
	 *
	 */
	boolean errOnInit = false;
	String errOnInitMessage;
	ValidateNTLogin vnl;

	public void jspInit() {

		String domainCtrlAddr = ArtConfig.getArtSetting("mswin_auth_server");
		try {
			vnl = new ValidateNTLogin();
			vnl.setDomainController(domainCtrlAddr);
		} catch (Exception e) {
			e.printStackTrace(System.out);
			
			errOnInit = true;
			errOnInitMessage = "Error while initializing connection to domain controller."
					+ " Check settings and reload the application: " + e;
		}
	}

%>
