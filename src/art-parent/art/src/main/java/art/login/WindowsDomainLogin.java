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
			logger.info("Windows Domain authentication not configured. username={}", username);

			result.setMessage("login.message.windowsDomainAuthenticationNotConfigured");
			result.setDetails("windows domain authentication not configured");
		} else {
			try {
				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, username, password);
				UniAddress dc = UniAddress.getByName(domainController);
				//if we are here, domainController is an ip or a valid machine name
				//domainController can also be any machine that is a member of the domain,
				//doesn't have to be the domain controller
				SmbSession.logon(dc, auth);
				//if we are here, authentication is successful
				result.setAuthenticated(true);
			} catch (UnknownHostException ex) {
				//invalid domain controller machine name
				logger.error("Error. username={}", username, ex);

				result.setMessage("login.message.errorOccurred");
				result.setDetails(ex.getMessage());
				result.setError(ex.toString());
			} catch (SmbException ex) {
				//failed to connect to dc or logon failure
				logger.error("Error. username={}", username, ex);

				result.setMessage("login.message.invalidAccount");
				result.setDetails(ex.getMessage());
				result.setError(ex.toString());
			}
		}

		return result;
	}
}
