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
package art.login.method;

import art.login.LoginResult;
import art.servlets.Config;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.context.SingletonContext;
import jcifs.netbios.UniAddress;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
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

		SmbConfig config = SmbConfig.builder()
				.withTimeout(10, TimeUnit.SECONDS)
				.withSoTimeout(60, TimeUnit.SECONDS)
				.withMultiProtocolNegotiate(true)
				.build();

			try (SMBClient client = new SMBClient(config)) {
				try (com.hierynomus.smbj.connection.Connection connection = client.connect(domainController)) {
					if (username == null) {
						username = "";
					}

					if (password == null) {
						password = "";
					}

					AuthenticationContext ac;
					ac = new AuthenticationContext(username, password.toCharArray(), domain);

					com.hierynomus.smbj.session.Session session = connection.authenticate(ac);
					
					//if we are here, authentication is successful
			result.setAuthenticated(true);
			
					//session.logoff();

				} catch (SMBApiException ex) {
					logger.error("Error. username={}", username, ex);

					result.setMessage("login.message.invalidCredentials");
					result.setDetails(ex.getMessage());
					result.setError(ex.toString());
				} catch (IOException ex) {
					logger.error("Error. username={}", username, ex);

					result.setMessage("page.message.errorOccurred");
					result.setDetails(ex.getMessage());
					result.setError(ex.toString());
				}

			}


		return result;
	}
}
