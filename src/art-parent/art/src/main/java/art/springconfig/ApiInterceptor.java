/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.springconfig;

import art.enums.ApiStatus;
import art.general.ApiResponse;
import art.login.method.InternalLogin;
import art.login.LoginResult;
import art.servlets.Config;
import art.user.User;
import art.user.UserService;
import art.utils.ApiHelper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.net.URI;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Authorizes access to api endpoints
 *
 * @author Timothy Anyona
 */
public class ApiInterceptor extends HandlerInterceptorAdapter {

	private static final Logger logger = LoggerFactory.getLogger(ApiInterceptor.class);

	@Autowired
	UserService userService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
			Object handler) throws Exception {

		try {
			//https://stackoverflow.com/questions/4050087/how-to-obtain-the-last-path-segment-of-an-uri
			//https://docs.oracle.com/javase/7/docs/api/java/net/URI.html
			//https://stackoverflow.com/questions/4931323/whats-the-difference-between-getrequesturi-and-getpathinfo-methods-in-httpservl
			String requestUri = request.getRequestURI();
			URI uri = new URI(requestUri);
			String path = uri.getPath();
			String page = path.substring(path.lastIndexOf('/') + 1);
			page = StringUtils.substringBefore(page, ";"); //;jsessionid may be included at the end of the url. may also be in caps? i.e. ;JSESSIONID ?

			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setHttpStatus(HttpStatus.UNAUTHORIZED.value());
			apiResponse.setArtStatus(ApiStatus.UNAUTHORIZED);
			apiResponse.setMessage("Unauthorized");

			if (StringUtils.equals(page, "login")) {
				return true;
			} else {
				String header = request.getHeader("Authorization");
				if (header == null) {
					apiResponse.setArtStatus(ApiStatus.AUTHORIZATION_REQUIRED);
					apiResponse.setMessage("Authorization required");
				} else {
					final String BASIC_AUTH_PREFIX = "Basic ";
					final String BEARER_AUTH_PREFIX = "Bearer ";
					if (StringUtils.startsWith(header, BASIC_AUTH_PREFIX)) {
						String base64Credentials = StringUtils.substringAfter(header, BASIC_AUTH_PREFIX);
						byte[] decodedBytes = Base64.decodeBase64(base64Credentials);
						String clearCredentials = new String(decodedBytes, "UTF-8");
						String[] credentialParts = StringUtils.split(clearCredentials, ":");
						String username = credentialParts[0];
						String password = credentialParts[1];

						LoginResult loginResult = InternalLogin.authenticate(username, password);
						User user = loginResult.getUser();
						if (loginResult.isAuthenticated() && user.hasPermission("use_api")) {
							HttpSession session = request.getSession();
							session.setAttribute("sessionUser", user);
							return true;
						}
					} else if (StringUtils.startsWith(header, BEARER_AUTH_PREFIX)) {
						String jwt = StringUtils.substringAfter(header, BEARER_AUTH_PREFIX);
						String jwtSecret = Config.getCustomSettings().getJwtSecret();
						if (StringUtils.isNotBlank(jwtSecret)) {
							Jws<Claims> claims = Jwts.parser()
									.setSigningKey(jwtSecret.getBytes("UTF-8"))
									.parseClaimsJws(jwt);
							Date expiry = claims.getBody().getExpiration();
							Date now = new Date();
							if (expiry == null || now.before(expiry)) {
								String username = claims.getBody().getSubject();
								User user = userService.getUser(username);
								if (user != null && user.isActive()
										&& user.hasPermission("use_api")) {
									HttpSession session = request.getSession();
									session.setAttribute("sessionUser", user);
									return true;
								}
							}
						}
					}
				}
			}

			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			ApiHelper.outputApiResponse(apiResponse, response);
			return false;
		} catch (Exception ex) {
			logger.error("Error", ex);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			apiResponse.setArtStatus(ApiStatus.ERROR);
			ApiHelper.outputApiResponse(apiResponse, response);
			return false;
		}
	}

}
