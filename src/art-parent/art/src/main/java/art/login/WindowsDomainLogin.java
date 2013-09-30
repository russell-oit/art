package art.login;

import art.servlets.ArtConfig;
import art.utils.ValidateNTLogin;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Timothy Anyona
 */
public class WindowsDomainLogin {

	final static Logger logger = LoggerFactory.getLogger(WindowsDomainLogin.class);

	public static boolean authenticate(String domain, String username, String password) {
		boolean authenticated = false;

		String domainController = ArtConfig.getArtSetting("mswin_auth_server");
		if (StringUtils.isBlank(domainController)) {
			logger.info("Windows Domain authentication server not set. Username={}", username);
		} else {
			ValidateNTLogin vnl = new ValidateNTLogin();
			vnl.setDomainController(domainController);
			if (vnl.isValidNTLogin(domain, username, password)) {
				authenticated = true;
			}
		}

		return authenticated;
	}
}
