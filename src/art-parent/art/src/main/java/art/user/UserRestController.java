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
import org.springframework.web.bind.annotation.ResponseStatus;
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
	public List<User> getUsers(@RequestParam(value = "active", required = false) Boolean active)
			throws SQLException {

		logger.debug("Entering getUsers: active={}", active);

		if (active != null) {
			return userService.getUsersByActiveStatus(active);
		} else {
			return userService.getAllUsers();
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<User> getUserById(@PathVariable("id") Integer id) throws SQLException {
		logger.debug("Entering getUserById: id={}", id);

		User user = userService.getUser(id);
		if (user == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(user);
		}
	}

	@GetMapping("/username/{username}")
	public ResponseEntity<User> getUserByUsername(@PathVariable("username") String username)
			throws SQLException {

		logger.debug("Entering getUserByUsername: username='{}'", username);

		User user = userService.getUser(username);
		if (user == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(user);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Object> deleteUser(@PathVariable("id") Integer id) throws SQLException {
		logger.debug("Entering deleteUser: id={}", id);

		ActionResult result = userService.deleteUser(id);
		if (result.isSuccess()) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(result.getData());
		}
	}

	@PostMapping
	public ResponseEntity<?> addUser(@RequestBody User user, HttpSession session,
			UriComponentsBuilder b) throws SQLException {

		logger.debug("Entering addUser");

		User sessionUser = (User) session.getAttribute("sessionUser");
		int newId = userService.addUser(user, sessionUser);

		UriComponents uriComponents = b.path("/api/users/{id}").buildAndExpand(newId);
		return ResponseEntity.created(uriComponents.toUri()).build();
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateUser(@PathVariable("id") Integer id,
			@RequestBody User user, HttpSession session) throws SQLException {

		logger.debug("Entering updateUser: id={}", id);

		User sessionUser = (User) session.getAttribute("sessionUser");
		user.setUserId(id);
		userService.updateUser(user, sessionUser);
	}
	
	@PostMapping("/{id}/disable")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void disableUser(@PathVariable("id") Integer id, HttpSession session) throws SQLException{
		logger.debug("Entering disableUser: id={}", id);
		
		User sessionUser = (User) session.getAttribute("sessionUser");
		userService.disableUser(id, sessionUser);
	}
	
	@PostMapping("/{id}/enable")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void enableUser(@PathVariable("id") Integer id, HttpSession session) throws SQLException{
		logger.debug("Entering disableUser: id={}", id);
		
		User sessionUser = (User) session.getAttribute("sessionUser");
		userService.enableUser(id, sessionUser);
	}

}
