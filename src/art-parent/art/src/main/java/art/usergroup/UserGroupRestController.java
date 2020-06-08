/*
 * ART. A Reporting Tool.
 * Copyright (C) 2020 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.usergroup;

import art.general.ActionResult;
import art.general.ApiResponse;
import art.user.User;
import art.usergrouprole.UserGroupRoleService;
import art.utils.ApiHelper;
import java.net.URI;
import java.sql.SQLException;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Controller to provide rest services related to user groups
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/api/user-groups")
public class UserGroupRestController {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupRestController.class);

	@Autowired
	private UserGroupService userGroupService;

	@Autowired
	private UserGroupRoleService userGroupRoleService;

	@GetMapping
	public ResponseEntity<?> getUserGroups() {
		logger.debug("Entering getUserGroups");

		try {
			List<UserGroup> userGroups = userGroupService.getAllUserGroups();
			return ApiHelper.getOkResponseEntity(userGroups);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getUserGroupById(@PathVariable("id") Integer id) {
		logger.debug("Entering getUserGroupById: id={}", id);

		try {
			UserGroup userGroup = userGroupService.getUserGroup(id);
			if (userGroup == null) {
				return ApiHelper.getNotFoundResponseEntity();
			} else {
				return ApiHelper.getOkResponseEntity(userGroup);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@GetMapping("/name/{name}")
	public ResponseEntity<?> getUserGroupByName(@PathVariable("name") String name) {
		logger.debug("Entering getUserGroupByName: name='{}'", name);

		try {
			UserGroup userGroup = userGroupService.getUserGroup(name);
			if (userGroup == null) {
				return ApiHelper.getNotFoundResponseEntity();
			} else {
				return ApiHelper.getOkResponseEntity(userGroup);
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteUserGroup(@PathVariable("id") Integer id) {
		logger.debug("Entering deleteUserGroup: id={}", id);

		try {
			ActionResult result = userGroupService.deleteUserGroup(id);
			if (result.isSuccess()) {
				return ApiHelper.getOkResponseEntity();
			} else {
				String message = "User Group not deleted because linked users exist";
				return ApiHelper.getLinkedRecordsExistResponseEntity(message, result.getData());
			}
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PostMapping
	public ResponseEntity<ApiResponse> addUserGroup(@RequestBody UserGroup userGroup,
			HttpSession session, UriComponentsBuilder b) {

		logger.debug("Entering addUserGroup");

		try {
			String name = userGroup.getName();
			if (StringUtils.isBlank(name)) {
				String message = "name field not provided or blank";
				return ApiHelper.getInvalidValueResponseEntity(message);
			}

			if (userGroupService.userGroupExists(name)) {
				String message = "A user group with the given name already exists";
				return ApiHelper.getRecordExistsResponseEntity(message);
			}

			User sessionUser = (User) session.getAttribute("sessionUser");
			int newId = userGroupService.addUserGroup(userGroup, sessionUser);
			userGroupRoleService.recreateUserGroupRoles(userGroup);

			UriComponents uriComponents = b.path("/api/user-groups/{id}").buildAndExpand(newId);
			URI uri = uriComponents.toUri();
			return ApiHelper.getCreatedResponseEntity(uri, userGroup);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateUserGroup(@PathVariable("id") Integer id,
			@RequestBody UserGroup userGroup, HttpSession session) {

		logger.debug("Entering updateUserGroup: id={}", id);

		try {
			userGroup.setUserGroupId(id);

			User sessionUser = (User) session.getAttribute("sessionUser");
			userGroupService.updateUserGroup(userGroup, sessionUser);
			userGroupRoleService.recreateUserGroupRoles(userGroup);

			return ApiHelper.getOkResponseEntity(userGroup);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			return ApiHelper.getErrorResponseEntity(ex);
		}
	}

}
