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
package art.user;

import art.general.ActionResult;
import art.general.ApiResponse;
import art.utils.ApiHelper;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Controller to provide rest services related to users
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/api/users")
public class UserRestController {

	private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

	@Autowired
	private UserService userService;

	@GetMapping
	public ResponseEntity<?> getUsers(
			@RequestParam(value = "active", required = false) Boolean active) {

		logger.debug("Entering getUsers: active={}", active);

		try {
			List<User> users;
			if (active == null) {
				users = userService.getAllUsers();
			} else {
				users = userService.getUsersByActiveStatus(active);
			}

			List<User> cleanUsers = new ArrayList<>();
			for (User user : users) {
				User cleanUser = user.getCleanUser();
				cleanUsers.add(cleanUser);
			}
			return ApiHelper.getOkResponseEntity(cleanUsers);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getUserById(@PathVariable("id") Integer id) {
		logger.debug("Entering getUserById: id={}", id);

		try {
			User user = userService.getUser(id);
			if (user == null) {
				return ApiHelper.getNotFoundResponseEntity();
			} else {
				User cleanUser = user.getCleanUser();
				return ApiHelper.getOkResponseEntity(cleanUser);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@GetMapping("/username/{username}")
	public ResponseEntity<?> getUserByUsername(@PathVariable("username") String username) {
		logger.debug("Entering getUserByUsername: username='{}'", username);

		try {
			User user = userService.getUser(username);
			if (user == null) {
				return ApiHelper.getNotFoundResponseEntity();
			} else {
				User cleanUser = user.getCleanUser();
				return ApiHelper.getOkResponseEntity(cleanUser);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id) {
		logger.debug("Entering deleteUser: id={}", id);

		try {
			ActionResult result = userService.deleteUser(id);
			if (result.isSuccess()) {
				return ApiHelper.getOkResponseEntity();
			} else {
				String message = "User not deleted because linked jobs exist";
				return ApiHelper.getLinkedRecordsExistResponseEntity(message, result.getData());
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PostMapping
	public ResponseEntity<ApiResponse> addUser(@RequestBody User user,
			HttpSession session, UriComponentsBuilder b) {

		logger.debug("Entering addUser");

		try {
			String username = user.getUsername();
			if (StringUtils.isBlank(username)) {
				String message = "username field not provided or blank";
				return ApiHelper.getInvalidValueResponseEntity(message);
			}

			if (userService.userExists(username)) {
				String message = "A user with the given username already exists";
				return ApiHelper.getRecordExistsResponseEntity(message);
			}

			if (user.isClearTextPassword()) {
				user.encryptPassword();
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			int newId = userService.addUser(user, sessionUser);

			UriComponents uriComponents = b.path("/api/users/{id}").buildAndExpand(newId);
			URI uri = uriComponents.toUri();
			User cleanUser = user.getCleanUser();
			return ApiHelper.getCreatedResponseEntity(uri, cleanUser);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateUser(@PathVariable("id") Integer id,
			@RequestBody User user, HttpSession session) {

		logger.debug("Entering updateUser: id={}", id);

		try {
			user.setUserId(id);

			if (user.isClearTextPassword()) {
				user.encryptPassword();
			} else {
				User currentUser = userService.getUser(id);
				if (currentUser == null) {
					return ApiHelper.getNotFoundResponseEntity();
				} else {
					//use current password if nothing passed
					if (StringUtils.isEmpty(user.getPassword())) {
						user.setPassword(currentUser.getPassword());
						user.setPasswordAlgorithm(currentUser.getPasswordAlgorithm());
					}
				}
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			userService.updateUser(user, sessionUser);
			
			User cleanUser = user.getCleanUser();
			return ApiHelper.getOkResponseEntity(cleanUser);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PostMapping("/{id}/disable")
	public ResponseEntity<?> disableUser(@PathVariable("id") Integer id, HttpSession session) {
		logger.debug("Entering disableUser: id={}", id);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			userService.disableUser(id, sessionUser);
			return ApiHelper.getOkResponseEntity();
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PostMapping("/{id}/enable")
	public ResponseEntity<?> enableUser(@PathVariable("id") Integer id, HttpSession session) {
		logger.debug("Entering enableUser: id={}", id);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			userService.enableUser(id, sessionUser);
			return ApiHelper.getOkResponseEntity();
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

}
