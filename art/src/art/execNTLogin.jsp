<%@ page import="java.io.*, art.utils.ValidateNTLogin, art.servlets.ArtDBCP;" %>
<%  request.setCharacterEncoding("UTF-8"); %>
<%
  /****************************************************************
   * WINDOWS DOMAIN CONTROLLER Authentication
   * 
   *    Download the jcifs-x.y.z.jar from jcifs.samba.org  and place
   *    it on the WEB-INF/lib directory (if not already present in
   *    your servlet engine - tested with jcifs-0.7.3.jar - jcifs is 
   *    not distributed along with ART since 1.2.1)
   *
   */

java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());

String domain   = request.getParameter("ntdomain");
String username = request.getParameter("ntusername");
String password = request.getParameter("ntpassword");

String nextPage= request.getParameter("nextPage");
session.setAttribute("nextPage", nextPage);

if (username == null || password == null || domain == null) {
       request.setAttribute("message",messages.getString("invalidAccount"));
} else if (errOnInit) {
       request.setAttribute("message",errOnInitMessage);
} else {
    try {
      if (vnl.isValidNTLogin(domain,username,password)) {
		// Autentication Successful!
		session.setAttribute("username", username);
		// redirect:
        response.sendRedirect(nextPage);
		return; // this must stay here 
      } else {
        request.setAttribute("message",messages.getString("invalidAccount"));
		// log wronglogin attempt
		String str = "#ENT " + username + " - " + request.getRemoteAddr() + "\n";
		System.out.println(str);
      }
   } catch(Exception e) {
        String message = "<small>Details: " + e + "</small>";
		request.setAttribute("message",message);
   }
}
// nextPage is in the session and drives the behaviour of the xxLogin.jsp page
request.getRequestDispatcher("/NTLogin.jsp").forward(request, response);
%>

 
<%!
/***********************************************************************
 *
 *  init, destroy and static functions
 *
 */

boolean errOnInit = false;
String  errOnInitMessage;
ValidateNTLogin vnl;

public void jspInit() {
      
    String domainCtrlAddr = ArtDBCP.getArtProps("mswin_auth_server");
    try {
       vnl = new ValidateNTLogin();
       vnl.setDomainController(domainCtrlAddr);
    } catch(Exception e) {
       errOnInit        =  true;
       errOnInitMessage = "Error while initializing connection to domain controller. Check settings and reload the application: " + e;
    }

}


%>
