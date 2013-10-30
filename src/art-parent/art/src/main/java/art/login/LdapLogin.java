package art.login;

import art.servlets.ArtConfig;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
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
}
