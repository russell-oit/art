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
package art.login;

import art.enums.AccessLevel;
import art.general.ApiResponse;
import art.servlets.Config;
import art.user.User;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for obtaining authorization tokens to be used with REST calls
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/api/login")
public class LoginRestController {

	private static final Logger logger = LoggerFactory.getLogger(LoginRestController.class);

	@PostMapping
	public ResponseEntity<ApiResponse> login(@RequestParam("username") String username,
			@RequestParam("password") String password) {

		try {
			String jwtSecret = Config.getCustomSettings().getJwtSecret();
			if (StringUtils.isNotBlank(jwtSecret)) {
				LoginResult loginResult = InternalLogin.authenticate(username, password);
				User user = loginResult.getUser();
				if (loginResult.isAuthenticated()
						&& user.getAccessLevel().getValue() >= AccessLevel.StandardAdmin.getValue()) {
					String jwt = generateToken(username, jwtSecret);
					ApiResponse apiResponse = new ApiResponse();
					apiResponse.setMessage(jwt);
					return ResponseEntity.ok(apiResponse);
				}
			}

			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		} catch (UnsupportedEncodingException | RuntimeException ex) {
			logger.error("Error", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * Generates a jwt token
	 *
	 * @param username the username to set as the subject
	 * @param secret the secret to use in signing the jwt
	 * @return the jwt
	 * @throws UnsupportedEncodingException
	 */
	private String generateToken(String username, String secret) throws UnsupportedEncodingException {
		//https://stormpath.com/blog/beginners-guide-jwts-in-java
		//https://stormpath.com/blog/jwt-java-create-verify
		JwtBuilder builder = Jwts.builder()
				.setSubject(username)
				.signWith(SignatureAlgorithm.HS256, secret.getBytes("UTF-8"));

		int tokenExpiryMins = Config.getSettings().getJwtTokenExpiryMins();
		if (tokenExpiryMins > 0) {
			builder.setExpiration(DateUtils.addMinutes(new Date(), tokenExpiryMins));
		}
		String jwt = builder.compact();
		return jwt;
	}

}
