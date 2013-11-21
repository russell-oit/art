package art.login;

import art.servlets.ArtConfig;
import com.unboundid.ldap.sdk.BindRequest;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.DIGESTMD5BindRequest;
import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import java.security.GeneralSecurityException;
import java.util.Hashtable;
import java.util.List;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to authenticate user using ldap
 *
 * @author Timothy Anyona
 */
public class LdapLogin {

	final static Logger logger = LoggerFactory.getLogger(LdapLogin.class);

	public static LoginResult authenticate(String username, String password) {
		LoginResult result = new LoginResult();

		// NOTE: username should be the uid e.g. for distinguished name like
		// uid=jdoe,ou=users,ou=system, username should be jdoe
		// for Active Directory, username should be the samAccountName e.g. jdoe

		String ldapServer = "192.168.56.110";
//		String ldapServer = ArtConfig.getArtSetting("ldap_auth_server");
		String ldapAuthType = ArtConfig.getArtSetting("ldap_auth_method");
		String ldapUsersParentDn = ArtConfig.getArtSetting("ldap_users_parent_dn");
		String ldapRealm = ArtConfig.getArtSetting("ldap_realm");
//		String baseDn = "dc=example,dc=com";
		String baseDn = "dc=mydomain,dc=test";
//		int ldapPort = 10389;
		int ldapPort = 389;
		String secureConnection = "no";
		String bindDn = "user@mydomain.test";
		String bindPassword = "abc.123";

		if (StringUtils.isBlank(ldapServer)) {
			result.setMessage("login.message.ldapAuthenticationNotConfigured");
			result.setDetails("ldap server not defined");
		} else if (StringUtils.isBlank(baseDn)) {
			result.setMessage("login.message.ldapAuthenticationNotConfigured");
			result.setDetails("ldap base dn not defined");
		} else {
			LDAPConnection ldapConnection = null;

			try {
				if (StringUtils.equalsIgnoreCase(secureConnection, "ssl")) {
					//TODO trustall manager isn't secure.
					//see http://stackoverflow.com/questions/11893608/using-unboundid-sdk-with-an-ssl-certificate-file-to-connect-to-ldap-server-in-an
					// also http://stackoverflow.com/questions/17656392/how-to-use-unboundid-sdk-to-connect-to-an-ldap-server-with-the-ssl-server-certif
					SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
					SSLSocketFactory socketFactory = sslUtil.createSSLSocketFactory();
					ldapConnection = new LDAPConnection(socketFactory, ldapServer, ldapPort);
				} else if (StringUtils.equalsIgnoreCase(secureConnection, "starttls")) {
					ldapConnection = new LDAPConnection(ldapServer, ldapPort);

					SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
					SSLContext sslContext = sslUtil.createSSLContext();
					ExtendedResult extendedResult = ldapConnection.processExtendedOperation(
							new StartTLSExtendedRequest(sslContext));

					if (extendedResult.getResultCode() == ResultCode.SUCCESS) {
						// The connection is now secure.
					} else {
						// The StartTLS negotiation failed for some reason.  
						// The connection can no longer be used.
						ldapConnection.close();
						ldapConnection = null;

						result.setMessage("login.message.errorOccurred");
						result.setDetails("starttls negotiation failed");
					}
				} else {
					ldapConnection = new LDAPConnection(ldapServer, ldapPort, bindDn, bindPassword);
				}

				if (ldapConnection != null) {

					try {
						//search for username under the ldap base dn
						Filter filter = Filter.createEqualityFilter("uid", username);
						SearchRequest searchRequest = new SearchRequest(baseDn, SearchScope.SUB, filter);
						SearchResult searchResult = ldapConnection.search(searchRequest);

						List<SearchResultEntry> searchEntries = searchResult.getSearchEntries();
						if (searchEntries.isEmpty()) {
							//uid not found
							result.setMessage("login.message.invalidUser");
							result.setDetails("invalid user");
						} else if (searchEntries.size() > 1) {
							// The search didn't match exactly one entry.
							result.setMessage("login.message.invalidUser");
							result.setDetails("multiple entries found for user");
						} else {
							SearchResultEntry entry = searchEntries.get(0);
							String dn = entry.getDN();

							try {
								BindRequest bindRequest = null;
								if (StringUtils.equalsIgnoreCase(ldapAuthType, "simple")) {
									bindRequest = new SimpleBindRequest(dn, password);
								} else if (StringUtils.equalsIgnoreCase(ldapAuthType, "digestmd5")) {
									if (StringUtils.isBlank(ldapRealm)) {
										bindRequest = new DIGESTMD5BindRequest("dn:" + dn, password);
									} else {
										bindRequest = new DIGESTMD5BindRequest("dn:" + dn, null, password, ldapRealm, new Control[0]);
									}

								}
								ldapConnection.bind(bindRequest);
								// If we get here, then the bind was successful.
								result.setAuthenticated(true);
							} catch (LDAPException le) {
								// The bind failed for some reason.
								logger.error("Error", le);

								result.setMessage("login.message.errorOccurred");
								result.setDetails(le.getMessage());
								result.setError(le.toString());

								if (!le.getResultCode().isConnectionUsable()) {
									ldapConnection.close();
									ldapConnection = null;
								}
							}
						}
					} catch (LDAPSearchException ex) {
						logger.error("Error", ex);
					}
				}
			} catch (LDAPException ex) {
				logger.error("Error", ex);

				result.setMessage("login.message.errorOccurred");
				result.setDetails(ex.getMessage());
				result.setError(ex.toString());
			} catch (GeneralSecurityException ex) {
				logger.error("Error", ex);

				result.setMessage("login.message.errorOccurred");
				result.setDetails(ex.getMessage());
				result.setError(ex.toString());
			}

			if (ldapConnection != null) {
				ldapConnection.close();
			}
		}

		return result;
	}

	public static LoginResult authenticateJndi(String username, String password) {
		LoginResult result = new LoginResult();

		// NOTE: username should be the uid e.g. for distinguished name like
		// uid=jdoe,ou=users,ou=system, username should be jdoe
		// for Active Directory, username should be the samAccountName e.g. jdoe

		String ldapServer = ArtConfig.getArtSetting("ldap_auth_server");
		String ldapAuthType = ArtConfig.getArtSetting("ldap_auth_method");
		String ldapUsersParentDn = ArtConfig.getArtSetting("ldap_users_parent_dn");
		String ldapRealm = ArtConfig.getArtSetting("ldap_realm");
		String bindAuthType = "simple"; //actual authentication method string used for ldap bind
		String bindUsername; //actual username used for LDAP bind

		if (StringUtils.isBlank(ldapServer)) {
			logger.info("LDAP authentication not configured. username={}", username);

			result.setMessage("login.message.ldapAuthenticationNotConfigured");
			result.setDetails("ldap authentication not configured");
		} else {

			if (StringUtils.equalsIgnoreCase(ldapAuthType, "simple")) {
				bindUsername = "uid=" + username + "," + ldapUsersParentDn;
			} else {
				bindUsername = username;
			}

			if (StringUtils.endsWithIgnoreCase(ldapAuthType, "simple")) {
				bindAuthType = "simple";
			} else if (StringUtils.endsWithIgnoreCase(ldapAuthType, "digestmd5")) {
				bindAuthType = "DIGEST-MD5";
			}

			Hashtable<String, String> authEnv = new Hashtable<String, String>(5);
			authEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			authEnv.put(Context.PROVIDER_URL, ldapServer);
			authEnv.put(Context.SECURITY_AUTHENTICATION, bindAuthType);
			authEnv.put(Context.SECURITY_PRINCIPAL, bindUsername);
			authEnv.put(Context.SECURITY_CREDENTIALS, password);

			if (StringUtils.isNotEmpty(ldapRealm) && StringUtils.endsWithIgnoreCase(ldapAuthType, "digestmd5")) {
				authEnv.put("java.naming.security.sasl.realm", ldapRealm);
			}

			//for tracing
			//authEnv.put("com.sun.jndi.ldap.trace.ber", System.err);

			DirContext authContext;
			try {
				authContext = new InitialDirContext(authEnv);
				//if we are here, authentication is successful
				result.setAuthenticated(true);

				try {
					authContext.close(); //close can also throw exception
				} catch (NamingException ex) {
					logger.error("Error. username={}", username, ex);
				}
			} catch (NamingException ex) {
				logger.error("Error. username={}", username, ex);

				result.setMessage("login.message.invalidCredentials");
				result.setDetails(ex.getMessage());
				result.setError(ex.toString());
			}
		}

		return result;
	}

	//from http://stackoverflow.com/questions/12163947/ldap-how-to-authenticate-user-with-connection-details
	
//	public static Boolean validateLogin(String userName, String userPassword) {
//		Hashtable<String, String> env = new Hashtable<String, String>();
//
//
//		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//		env.put(Context.PROVIDER_URL, "ldap://" + LDAP_SERVER + ":" + LDAP_SERVER_PORT + "/" + LDAP_BASE_DN);
//
//		// To get rid of the PartialResultException when using Active Directory
//		env.put(Context.REFERRAL, "follow");
//
//		// Needed for the Bind (User Authorized to Query the LDAP server) 
//		env.put(Context.SECURITY_AUTHENTICATION, "simple");
//		env.put(Context.SECURITY_PRINCIPAL, LDAP_BIND_DN);
//		env.put(Context.SECURITY_CREDENTIALS, LDAP_BIND_PASSWORD);
//
//		DirContext ctx;
//		try {
//			ctx = new InitialDirContext(env);
//		} catch (NamingException e) {
//			throw new RuntimeException(e);
//		}
//
//		NamingEnumeration<SearchResult> results = null;
//
//		try {
//			SearchControls controls = new SearchControls();
//			controls.setSearchScope(SearchControls.SUBTREE_SCOPE); // Search Entire Subtree
//			controls.setCountLimit(1);   //Sets the maximum number of entries to be returned as a result of the search
//			controls.setTimeLimit(5000); // Sets the time limit of these SearchControls in milliseconds
//
//			String searchString = "(&(objectCategory=user)(sAMAccountName=" + userName + "))";
//
//			results = ctx.search("", searchString, controls);
//
//			if (results.hasMore()) {
//
//				SearchResult result = (SearchResult) results.next();
//				Attributes attrs = result.getAttributes();
//				Attribute dnAttr = attrs.get("distinguishedName");
//				String dn = (String) dnAttr.get();
//
//				// User Exists, Validate the Password
//
//				env.put(Context.SECURITY_PRINCIPAL, dn);
//				env.put(Context.SECURITY_CREDENTIALS, userPassword);
//
//				new InitialDirContext(env); // Exception will be thrown on Invalid case
//				return true;
//			} else {
//				return false;
//			}
//
//		} catch (AuthenticationException e) { // Invalid Login
//
//			return false;
//		} catch (NameNotFoundException e) { // The base context was not found.
//
//			return false;
//		} catch (SizeLimitExceededException e) {
//			throw new RuntimeException("LDAP Query Limit Exceeded, adjust the query to bring back less records", e);
//		} catch (NamingException e) {
//			throw new RuntimeException(e);
//		} finally {
//
//			if (results != null) {
//				try {
//					results.close();
//				} catch (Exception e) { /* Do Nothing */ }
//			}
//
//			if (ctx != null) {
//				try {
//					ctx.close();
//				} catch (Exception e) { /* Do Nothing */ }
//			}
//		}
//	}
}
