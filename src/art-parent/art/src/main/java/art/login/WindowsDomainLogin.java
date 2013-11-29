package art.login;

import art.servlets.ArtConfig;
import java.net.UnknownHostException;
import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to authenticate user using a windows domain
 *
 * @author Timothy Anyona
 */
public class WindowsDomainLogin {

	final static Logger logger = LoggerFactory.getLogger(WindowsDomainLogin.class);

	public static LoginResult authenticate(String domain, String username, String password) {
		LoginResult result = new LoginResult();

		String domainController = ArtConfig.getSettings().getWindowsDomainController();

		if (StringUtils.isBlank(domainController)) {
			logger.info("Windows Domain authentication not configured. username={}", username);

			result.setMessage("login.message.windowsDomainAuthenticationNotConfigured");
			result.setDetails("windows domain authentication not configured");
		} else {
			try {
				//See http://jcifs.samba.org/FAQ.html
				UniAddress dc = UniAddress.getByName(domainController);
				
				//if we are here, domainController is an ip address or a valid machine name
				//domainController can also be any machine that is a member of the domain,
				//doesn't have to be the domain controller
				NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, username, password);
				SmbSession.logon(dc, auth);
				
				//if we are here, authentication is successful
				result.setAuthenticated(true);
			} catch (UnknownHostException ex) {
				//if domainController provided was a hostname, name could not be resolved
				logger.error("Error. username={}", username, ex);

				result.setMessage("page.message.errorOccurred");
				result.setDetails(ex.getMessage());
				result.setError(ex.toString());
			} catch (SmbAuthException ex) {
				// AUTHENTICATION FAILURE
				logger.error("Error. username={}", username, ex);

				result.setMessage("login.message.invalidCredentials");
				result.setDetails(ex.getMessage());
				result.setError(ex.toString());
			} catch (SmbException ex) {
				// NETWORK PROBLEMS? failed to connect to dc
				logger.error("Error. username={}", username, ex);

				result.setMessage("page.message.errorOccurred");
				result.setDetails(ex.getMessage());
				result.setError(ex.toString());
			}
		}

		return result;
	}
}
