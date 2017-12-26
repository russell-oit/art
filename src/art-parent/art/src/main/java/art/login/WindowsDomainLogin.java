/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.login;

import art.servlets.Config;
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
 * Authenticates a user using a windows domain
 *
 * @author Timothy Anyona
 */
public class WindowsDomainLogin {

	private static final Logger logger = LoggerFactory.getLogger(WindowsDomainLogin.class);

	/**
	 * Authenticates a user using windows domain credentials
	 *
	 * @param domain the domain to use
	 * @param username the username to use
	 * @param password the password to use
	 * @return the result of the authentication process
	 */
	public static LoginResult authenticate(String domain, String username, String password) {
		logger.debug("Entering authenticate: domain='{}', username='{}'", domain, username);

		LoginResult result = new LoginResult();

		String domainController = Config.getSettings().getWindowsDomainController();

		logger.debug("domainController='{}'", domainController);

		if (StringUtils.isBlank(domainController)) {
			logger.info("Windows Domain authentication not configured. username='{}'", username);

			result.setMessage("login.message.windowsDomainAuthenticationNotConfigured");
			result.setDetails("windows domain authentication not configured");
			return result;
		}

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

		return result;
	}
}
