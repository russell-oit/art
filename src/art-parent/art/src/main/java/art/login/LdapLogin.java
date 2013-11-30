package art.login;

import art.enums.LdapAuthenticationMethod;
import art.enums.LdapConnectionEncryptionMethod;
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
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
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
		if (StringUtils.isBlank(ArtConfig.getSettings().getLdapUrl())) {
			return authenticateUsingUnboundId(username, password);
		} else {
			return authenticateUsingJndi(username, password);
		}
	}

	/**
	 * Authenticate user using UnboundID library
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	private static LoginResult authenticateUsingUnboundId(String username, String password) {
		LoginResult result = new LoginResult();

		String ldapServer = ArtConfig.getSettings().getLdapServer();
		String baseDn = ArtConfig.getSettings().getLdapBaseDn();
		int ldapPort = ArtConfig.getSettings().getLdapPort();
		String bindDn = ArtConfig.getSettings().getLdapBindDn();
		String bindPassword = ArtConfig.getSettings().getLdapBindPassword();

		if (StringUtils.isBlank(ldapServer)) {
			result.setMessage("login.message.ldapAuthenticationNotConfigured");
			result.setDetails("ldap server not defined");
		} else if (StringUtils.isBlank(baseDn)) {
			result.setMessage("login.message.ldapAuthenticationNotConfigured");
			result.setDetails("ldap base dn not defined");
		} else {
			LDAPConnection ldapConnection = null;

			try {
				LdapConnectionEncryptionMethod encryptionMethod = ArtConfig.getSettings().getLdapConnectionEncryptionMethod();

				if (encryptionMethod == LdapConnectionEncryptionMethod.SSL) {
					//trustall manager isn't secure. review appropriate code to use
					//see http://stackoverflow.com/questions/11893608/using-unboundid-sdk-with-an-ssl-certificate-file-to-connect-to-ldap-server-in-an
					// also http://stackoverflow.com/questions/17656392/how-to-use-unboundid-sdk-to-connect-to-an-ldap-server-with-the-ssl-server-certif
					SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
					SSLSocketFactory socketFactory = sslUtil.createSSLSocketFactory();
					if (StringUtils.isBlank(bindDn)) {
						ldapConnection = new LDAPConnection(socketFactory, ldapServer, ldapPort);
					} else {
						ldapConnection = new LDAPConnection(socketFactory, ldapServer, ldapPort, bindDn, bindPassword);
					}
				} else {
					if (StringUtils.isBlank(bindDn)) {
						ldapConnection = new LDAPConnection(ldapServer, ldapPort);
					} else {
						ldapConnection = new LDAPConnection(ldapServer, ldapPort, bindDn, bindPassword);
					}

					if (encryptionMethod == LdapConnectionEncryptionMethod.StartTLS) {
						//trustall manager isn't secure even for starttls?
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

							result.setMessage("page.message.errorOccurred");
							result.setDetails("starttls negotiation failed");
						}
					}
				}

				if (ldapConnection != null) {
					try {
						//search for username under the ldap base dn
						Filter filter = Filter.createEqualityFilter(ArtConfig.getSettings().getLdapUserIdAttribute(), username);
						SearchRequest searchRequest = new SearchRequest(baseDn, SearchScope.SUB, filter);
						SearchResult searchResult = ldapConnection.search(searchRequest);

						List<SearchResultEntry> searchEntries = searchResult.getSearchEntries();
						if (searchEntries.isEmpty()) {
							//user not found
							result.setMessage("login.message.invalidUser");
							result.setDetails("invalid user");
						} else if (searchEntries.size() > 1) {
							// The search didn't match exactly one entry.
							result.setMessage("login.message.multipleUsersFound");
							result.setDetails("multiple entries found for user");
						} else {
							SearchResultEntry entry = searchEntries.get(0);
							String dn = entry.getDN();

							try {
								BindRequest bindRequest = null;
								LdapAuthenticationMethod authenticationMethod = ArtConfig.getSettings().getLdapAuthenticationMethod();

								if (authenticationMethod == LdapAuthenticationMethod.Simple) {
									bindRequest = new SimpleBindRequest(dn, password);
								} else if (authenticationMethod == LdapAuthenticationMethod.DigestMD5) {
									String ldapRealm = ArtConfig.getSettings().getLdapRealm();
									if (StringUtils.isBlank(ldapRealm)) {
										bindRequest = new DIGESTMD5BindRequest("dn:" + dn, password);
									} else {
										bindRequest = new DIGESTMD5BindRequest("dn:" + dn, null, password, ldapRealm, new Control[0]);
									}
								}
								ldapConnection.bind(bindRequest);

								// If we are here, then authentication was successful.
								result.setAuthenticated(true);
							} catch (LDAPException ex) {
								// The bind failed for some reason.
								logger.error("Error. username={}", username, ex);

								result.setMessage("login.message.invalidCredentials");
								result.setDetails(ex.getMessage());
								result.setError(ex.toString());

								if (!ex.getResultCode().isConnectionUsable()) {
									ldapConnection.close();
									ldapConnection = null;
								}
							}
						}
					} catch (LDAPSearchException ex) {
						logger.error("Error. username={}", username, ex);

						result.setMessage("page.message.errorOccurred");
						result.setDetails(ex.getMessage());
						result.setError(ex.toString());
					}
				}
			} catch (LDAPException ex) {
				logger.error("Error. username={}", username, ex);

				result.setMessage("page.message.errorOccurred");
				result.setDetails(ex.getMessage());
				result.setError(ex.toString());
			} catch (GeneralSecurityException ex) {
				logger.error("Error. username={}", username, ex);

				result.setMessage("page.message.errorOccurred");
				result.setDetails(ex.getMessage());
				result.setError(ex.toString());
			}

			if (ldapConnection != null) {
				ldapConnection.close();
			}
		}

		return result;
	}

	/**
	 * Authenticate user using jndi
	 *
	 * @param username
	 * @param password
	 * @return
	 */
	private static LoginResult authenticateUsingJndi(String username, String password) {
		//example code
		//http://stackoverflow.com/questions/12163947/ldap-how-to-authenticate-user-with-connection-details
		//http://www.adamretter.org.uk/blog/entries/LDAPTest.java

		LoginResult result = new LoginResult();

		Hashtable<String, String> env = new Hashtable<String, String>();

		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ArtConfig.getSettings().getLdapUrl());

		// To get rid of the PartialResultException when using Active Directory
		env.put(Context.REFERRAL, "follow");

		LdapAuthenticationMethod authenticationMethod = ArtConfig.getSettings().getLdapAuthenticationMethod();
		env.put(Context.SECURITY_AUTHENTICATION, authenticationMethod.getValue());

		if (authenticationMethod == LdapAuthenticationMethod.DigestMD5) {
			String ldapRealm = ArtConfig.getSettings().getLdapRealm();
			if (StringUtils.isNotBlank(ldapRealm)) {
				env.put("java.naming.security.sasl.realm", ldapRealm);
			}
		}

		String bindDn = ArtConfig.getSettings().getLdapBindDn();
		if (StringUtils.isNotBlank(bindDn)) {
			env.put(Context.SECURITY_PRINCIPAL, bindDn);
			env.put(Context.SECURITY_CREDENTIALS, ArtConfig.getSettings().getLdapBindPassword());
		}

		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);

			NamingEnumeration<javax.naming.directory.SearchResult> results = null;

			try {
				SearchControls controls = new SearchControls();
				controls.setSearchScope(SearchControls.SUBTREE_SCOPE); // Search Entire Subtree
				controls.setTimeLimit(5000); // Sets the time limit of these SearchControls in milliseconds

				String searchFilter = "(&(objectClass=person)("
						+ ArtConfig.getSettings().getLdapUserIdAttribute()
						+ "=" + username + "))";

				results = ctx.search(ArtConfig.getSettings().getLdapBaseDn(), searchFilter, controls);

				if (results.hasMoreElements()) {
					javax.naming.directory.SearchResult searchResult = (javax.naming.directory.SearchResult) results.next();

					//make sure there is not another item available, there should be only 1 match
					if (results.hasMoreElements()) {
						result.setMessage("login.message.multipleUsersFound");
						result.setDetails("multiple entries found for user");
					} else {
						String dn = searchResult.getNameInNamespace();

						// User Exists, Validate the Password
						env.put(Context.SECURITY_PRINCIPAL, dn);
						env.put(Context.SECURITY_CREDENTIALS, password);

						DirContext authContext = new InitialDirContext(env);

						//if we are here, authentication was successful
						result.setAuthenticated(true);

						try {
							authContext.close();
						} catch (NamingException ex) {
							logger.error("Error", ex);
						}
					}
				} else {
					//user not found
					result.setMessage("login.message.invalidUser");
					result.setDetails("invalid user");
				}

			} catch (AuthenticationException ex) { // Invalid Login
				logger.error("Error. username={}", ex);

				result.setMessage("login.message.invalidCredentials");
				result.setDetails(ex.toString());
			} finally {
				if (results != null) {
					try {
						results.close();
					} catch (Exception ex) {
						logger.error("Error", ex);
					}
				}
			}
		} catch (NamingException ex) {
			logger.error("Error. username={}", ex);

			result.setMessage("page.message.errorOccurred");
			result.setDetails(ex.toString());
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (Exception ex) {
					logger.error("Error", ex);
				}
			}
		}

		return result;
	}
}
