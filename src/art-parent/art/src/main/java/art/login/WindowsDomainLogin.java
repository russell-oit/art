package art.login;

import art.servlets.ArtConfig;
import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbSession;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to authenticate user using windows domain
 * 
 * @author Timothy Anyona
 */
public class WindowsDomainLogin {

	final static Logger logger = LoggerFactory.getLogger(WindowsDomainLogin.class);

	public static LoginResult authenticate(String domain, String username, String password) {
		LoginResult result = new LoginResult();

		String domainController = ArtConfig.getArtSetting("mswin_auth_server");
		
		if (StringUtils.isBlank(domainController)) {
			logger.info("Windows Domain authentication server not set. Username={}", username);

			result.setMessage("login.message.windowsDomainAuthenticationNotConfigured");
			result.setMessageDetails("windows domain authentication not configured");
		} else {
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, username, password);
			try {
				UniAddress dc = UniAddress.getByName(domainController);
				//if we are here, domain controller is valid
				SmbSession.logon(dc, auth);
				//if we are here, authentication is successful
				result.setAuthenticated(true);
			} catch (Exception e) {
				logger.error("Error. Username={}", username, e);

				result.setMessage("login.message.invalidAccount");
				result.setMessage(e.getMessage());
				result.setError(e.toString());
			}

		}

		return result;
	}
}
