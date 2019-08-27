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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import jcifs.CIFSContext;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbAuthException;
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
			//https://stackoverflow.com/questions/11251289/how-to-read-a-properties-file-in-java-from-outside-the-class-folder
			Properties properties = new Properties();
			String propertiesFilePath = Config.getClassesPath() + "jcifs.properties";
			File propertiesFile = new File(propertiesFilePath);
			if (propertiesFile.exists()) {
				try (FileInputStream input = new FileInputStream(propertiesFilePath)) {
					properties.load(input);
				}
			}

			//https://github.com/AgNO3/jcifs-ng/issues/139
			//https://github.com/AgNO3/jcifs-ng/issues/93
			//https://github.com/AgNO3/jcifs-ng/issues/67
			PropertyConfiguration config = new PropertyConfiguration(properties);
			BaseContext baseContext = new BaseContext(config);

			NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(domain, username, password);
			CIFSContext contextWithCredentials = baseContext.withCredentials(auth);

			contextWithCredentials.getTransportPool().logon(contextWithCredentials, contextWithCredentials.getNameServiceClient().getByName(domainController));

			//if we are here, authentication is successful
			result.setAuthenticated(true);
		} catch (SmbAuthException ex) {
			logger.error("Error. username='{}'", username, ex);

			result.setMessage("login.message.invalidCredentials");
			result.setDetails(ex.getMessage());
		} catch (IOException ex) {
			logger.error("Error. username='{}'", username, ex);

			result.setMessage("page.message.errorOccurred");
			result.setDetails(ex.getMessage());
			result.setError(ex.getMessage());
		}

		return result;
	}
}
