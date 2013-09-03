<%@ page import="java.io.*, javax.naming.*, art.servlets.ArtConfig, java.util.*" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<% request.setCharacterEncoding("UTF-8");%>

<%
	/**
	 * **************************************************************
	 * LDAP Authentication
	 *
	 */
	ResourceBundle messages = ResourceBundle.getBundle("i18n.ArtMessages", request.getLocale());

	String ldapServer = ArtConfig.getArtSetting("ldap_auth_server");
	String ldapAuthType = ArtConfig.getArtSetting("ldap_auth_method");

	String username = request.getParameter("ldapusername");
// NOTE: username should be the uid e.g. for distinguished name like
// uid=jdoe,ou=users,ou=system, username should be jdoe
// for Active Directory, username should be the samAccountName e.g. jdoe
	
	String password = request.getParameter("ldappassword");

	String nextPage = request.getParameter("nextPage");
	session.setAttribute("nextPage", nextPage);

	String ldapUsersParentDn = ArtConfig.getArtSetting("ldap_users_parent_dn");
	String ldapRealm = ArtConfig.getArtSetting("ldap_realm");
	String bindAuthType = "simple"; //actual authentication method string used for ldap bind
	String bindUsername; //actual username used for LDAP bind


	if (username == null || StringUtils.isEmpty(password)) {
		request.setAttribute("message", messages.getString("invalidAccount"));
	} else {
		if (StringUtils.equals(ldapAuthType, "simple")) {
			bindUsername = "uid=" + username + "," + ldapUsersParentDn;
		} else {
			bindUsername = username;
		}

		if (StringUtils.endsWith(ldapAuthType, "simple")) {
			bindAuthType = "simple";
		} else if (StringUtils.endsWith(ldapAuthType, "digestmd5")) {
			bindAuthType = "DIGEST-MD5";
		}

		Hashtable<String,String> authEnv = new Hashtable<String,String>(5);
		authEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		authEnv.put(Context.PROVIDER_URL, ldapServer);
		authEnv.put(Context.SECURITY_AUTHENTICATION, bindAuthType);
		authEnv.put(Context.SECURITY_PRINCIPAL, bindUsername);
		authEnv.put(Context.SECURITY_CREDENTIALS, password);

		if (StringUtils.isNotEmpty(ldapRealm) && StringUtils.endsWith(ldapAuthType, "digestmd5")) {
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
			//remove nextpage attribute to prevent endless redirection to login page for /admin pages
			session.removeAttribute("nextPage");
			response.sendRedirect(nextPage);
			return;
		} catch (AuthenticationException authEx) {
			String str = "#ELD " + username + " - " + request.getRemoteAddr() + "\n";
			System.out.println(str);
			authEx.printStackTrace(System.out);
			
			request.setAttribute("message", messages.getString("invalidAccount"));
		} catch (Exception e) {
			e.printStackTrace(System.out);
			String message = "<small>Details: " + e + "</small>";
			request.setAttribute("message", message);
		}
	}

	//if we are here, authentication failed. go back to login page
	request.getRequestDispatcher("/LDAPLogin.jsp").forward(request, response);
%>


<%!	/**
	 * *********************************************************************
	 *
	 * init, destroy and static functions
	 *
	 */
	String log_file_name, base_log_path;

	public void jspInit() {
	}

%>
