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

import art.login.method.LdapLogin;
import art.login.method.WindowsDomainLogin;
import art.login.method.InternalLogin;
import art.login.method.DbLogin;
import art.enums.ArtAuthenticationMethod;
import art.utils.ArtLogsHelper;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides methods for logging success or failure of login attempts
 *
 * @author Timothy Anyona
 */
public class LoginHelper {

	/**
	 * Logs login attempts
	 *
	 * @param loginMethod the login method
	 * @param result the login result
	 * @param username the username used
	 * @param ip the ip address from which login was done or attempted
	 */
	public void log(ArtAuthenticationMethod loginMethod, LoginResult result,
			String username, String ip) {

		log(loginMethod, result.isAuthenticated(), username, ip, result.getDetails());
	}

	/**
	 * Logs a successful login attempt
	 *
	 * @param loginMethod the login method used
	 * @param username the username used
	 * @param ip the ip address from which the login was done
	 */
	public void logSuccess(ArtAuthenticationMethod loginMethod,
			String username, String ip) {

		boolean success = true;
		String failureMessage = null;
		log(loginMethod, success, username, ip, failureMessage);
	}

	/**
	 * Logs a failed login attempt
	 *
	 * @param loginMethod the login method used
	 * @param username the username used
	 * @param ip the ip address from which the login was done
	 * @param failureMessage the message accompanying the failed login attempt
	 */
	public void logFailure(ArtAuthenticationMethod loginMethod,
			String username, String ip, String failureMessage) {

		boolean success = false;
		log(loginMethod, success, username, ip, failureMessage);
	}

	/**
	 * Logs login attempts
	 *
	 * @param loginMethod the login method
	 * @param success whether the login attempt was successful
	 * @param username the username used
	 * @param ip the ip address from which the login was done or attempted
	 * @param failureMessage the login failure message
	 */
	private void log(ArtAuthenticationMethod loginMethod, boolean success,
			String username, String ip, String failureMessage) {

		String event;
		String message;
		if (success) {
			event = "login";
			message = loginMethod.getValue();
		} else {
			event = "loginerr";
			//https://stackoverflow.com/questions/21936503/get-empty-string-when-null
			message = loginMethod.getValue() + ", " + StringUtils.defaultString(failureMessage);
		}

		ArtLogsHelper.log(username, event, ip, message);
	}

	/**
	 * Returns result of authentication using the given authentication method
	 * and credentials. Only authenticates against the following authentication
	 * methods: internal, database, ldap, windows domain.
	 *
	 * @param loginMethod the authentication method
	 * @param username the username
	 * @param password the password
	 * @param windowsDomain the windows domain. Only relevant for windows domain
	 * authentication
	 * @return authentication result
	 */
	public LoginResult authenticate(ArtAuthenticationMethod loginMethod,
			String username, String password, String windowsDomain) {

		LoginResult result;

		if (loginMethod == null) {
			result = new LoginResult();
		} else {
			switch (loginMethod) {
				case Internal:
					result = InternalLogin.authenticate(username, password);
					break;
				case Database:
					result = DbLogin.authenticate(username, password);
					break;
				case LDAP:
					result = LdapLogin.authenticate(username, password);
					break;
				case WindowsDomain:
					result = WindowsDomainLogin.authenticate(windowsDomain, username, password);
					break;
				default:
					//enum has other possible values but they aren't relevant here
					//create default object
					result = new LoginResult();
			}
		}

		return result;
	}
}
