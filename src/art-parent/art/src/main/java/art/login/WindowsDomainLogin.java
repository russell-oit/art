package art.login;

import art.servlets.ArtConfig;
import java.net.UnknownHostException;
import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;
import org.apache.commons.lang3.StringUtils;
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
			logger.info("Windows Domain authentication server not set. username={}", username);

			result.setMessage("login.message.windowsDomainAuthenticationNotConfigured");
			result.setDetails("windows domain authentication not configured");
		} else {
			try {
				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, username, password);
				UniAddress dc = UniAddress.getByName(domainController);
				//if we are here, domain controller is ok
				SmbSession.logon(dc, auth);
				//if we are here, authentication is successful
				result.setAuthenticated(true);
			} catch (UnknownHostException ex) {
				//problem with domain controller
				logger.error("Error. username={}", username, ex);

				result.setMessage("login.message.errorOccurred");
				result.setMessage(ex.getMessage());
				result.setError(ex.toString());
			} catch (SmbException ex) {
				logger.error("Error. username={}", username, ex);

				result.setMessage("login.message.invalidAccount");
				result.setMessage(ex.getMessage());
				result.setError(ex.toString());
			}
		}

		return result;
	}
}
