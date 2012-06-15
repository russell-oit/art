<%@ page import="java.io.*, javax.naming.*, art.servlets.ArtDBCP, java.util.*" %>
<%  request.setCharacterEncoding("UTF-8"); %>
<%
  /****************************************************************
   * LDAP Authentication
   *
   */

java.util.ResourceBundle messages = java.util.ResourceBundle.getBundle("art.i18n.ArtMessages",request.getLocale());

String ldapServer   = ArtDBCP.getArtSetting("ldap_auth_server");
String ldapAuthType = ArtDBCP.getArtSetting("ldap_auth_method");

String username = request.getParameter("ldapusername");
// NOTE: username should be the uid e.g. for distinguished name like uid=jdoe,ou=users,ou=system, username should be jdoe
// for Active Directory, username should be the samAccountName e.g. jdoe
String password = request.getParameter("ldappassword");

String nextPage= request.getParameter("nextPage");
session.setAttribute("nextPage", nextPage);

String ldapUsersParentDn   = ArtDBCP.getArtSetting("ldap_users_parent_dn");
String ldapRealm = ArtDBCP.getArtSetting("ldap_realm");
String bindAuthType="simple"; //actual authentication method string used for ldap bind
String bindUsername; //actual username used for LDAP bind


if (username == null || password == null || password.equals("") ) {
   request.setAttribute("message",messages.getString("invalidAccount"));
} else {
			
	if("simple".equals(ldapAuthType)){
		bindUsername="uid=" + username + "," + ldapUsersParentDn;			
	} else {
		bindUsername=username;
	}
	
	if("simple".equals(ldapAuthType) || "ad-simple".equals(ldapAuthType)){
		bindAuthType="simple";
	}
	if("digestmd5".equals(ldapAuthType) || "ad-digestmd5".equals(ldapAuthType)){
		bindAuthType="DIGEST-MD5";
	}
	
	if(ldapRealm==null){
		ldapRealm="";
	}
	
				
	   Hashtable authEnv = new Hashtable(5);
	   authEnv.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");
	   authEnv.put(Context.PROVIDER_URL, ldapServer);
	   authEnv.put(Context.SECURITY_AUTHENTICATION, bindAuthType);        
	   authEnv.put(Context.SECURITY_PRINCIPAL, bindUsername);
	   authEnv.put(Context.SECURITY_CREDENTIALS, password);
	   
	   if(ldapRealm.length()>0 && ("digestmd5".equals(ldapAuthType) || "ad-digestmd5".equals(ldapAuthType))){
		authEnv.put("java.naming.security.sasl.realm", ldapRealm);
		}
		
		//for tracing
		//authEnv.put("com.sun.jndi.ldap.trace.ber", System.err);
		
	   try {
		   //DirContext authContext = new InitialDirContext(authEnv);
		   InitialContext authContext = new InitialContext(authEnv);
		   // Autentication Successful!
		   authContext.close();
		   
		   session.setAttribute("username", username);
		   // redirect:
		   response.sendRedirect(nextPage);
		   return;
	   } catch (AuthenticationException authEx) {
			authEx.printStackTrace();
		   String str = "#ELD " + username + " - " + request.getRemoteAddr() + "\n";
		   System.out.println(str);   
		   request.setAttribute("message",messages.getString("invalidAccount"));	
	   } catch (Exception e) {
			e.printStackTrace();
		   String message = "<small>Details: " + e + "</small>";
		   request.setAttribute("message",message);	   
	   }	 
}

//if we are here, authentication failed. go back to login page
request.getRequestDispatcher("/LDAPLogin.jsp").forward(request, response);
%>

 
<%!
/***********************************************************************
 *
 *  init, destroy and static functions
 *
 */
String log_file_name, base_log_path;


public void jspInit() {

    
}


%>
