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

import art.enums.ApiStatus;
import art.general.ActionResult;
import art.general.ApiResponse;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
	public ResponseEntity<List<User>> getUsers(
			@RequestParam(value = "active", required = false) Boolean active) {

		logger.debug("Entering getUsers: active={}", active);

		try {
			List<User> users;
			if (active != null) {
				users = userService.getUsersByActiveStatus(active);
			} else {
				users = userService.getAllUsers();
			}
			return ResponseEntity.ok(users);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getUserById(@PathVariable("id") Integer id) {
		logger.debug("Entering getUserById: id={}", id);

		try {
			User user = userService.getUser(id);
			if (user == null) {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setArtStatus(ApiStatus.RECORD_NOT_FOUND);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
			} else {
				return ResponseEntity.ok(user);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/username/{username}")
	public ResponseEntity<?> getUserByUsername(@PathVariable("username") String username) {
		logger.debug("Entering getUserByUsername: username='{}'", username);

		try {
			User user = userService.getUser(username);
			if (user == null) {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setArtStatus(ApiStatus.RECORD_NOT_FOUND);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
			} else {
				return ResponseEntity.ok(user);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse> deleteUser(@PathVariable("id") Integer id) throws SQLException {
		logger.debug("Entering deleteUser: id={}", id);

		try {
			ActionResult result = userService.deleteUser(id);
			if (result.isSuccess()) {
				return ResponseEntity.noContent().build();
			} else {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setArtStatus(ApiStatus.LINKED_RECORDS_EXIST);
				apiResponse.setMessage("User not deleted because linked jobs exist");
				apiResponse.setData(result.getData());
				return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping
	public ResponseEntity<?> addUser(@RequestBody User user, HttpSession session,
			UriComponentsBuilder b) {

		logger.debug("Entering addUser");

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			int newId = userService.addUser(user, sessionUser);

			UriComponents uriComponents = b.path("/api/users/{id}").buildAndExpand(newId);
			return ResponseEntity.created(uriComponents.toUri()).build();
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<Void> updateUser(@PathVariable("id") Integer id,
			@RequestBody User user, HttpSession session) {

		logger.debug("Entering updateUser: id={}", id);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			user.setUserId(id);
			userService.updateUser(user, sessionUser);
			return ResponseEntity.noContent().build();
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/{id}/disable")
	public ResponseEntity<Void> disableUser(@PathVariable("id") Integer id, HttpSession session) {
		logger.debug("Entering disableUser: id={}", id);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			userService.disableUser(id, sessionUser);
			return ResponseEntity.noContent().build();
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/{id}/enable")
	public ResponseEntity<Void> enableUser(@PathVariable("id") Integer id, HttpSession session) {
		logger.debug("Entering disableUser: id={}", id);

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			userService.enableUser(id, sessionUser);
			return ResponseEntity.noContent().build();
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
