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
package art.usergrouprole;

import art.general.AjaxResponse;
import java.sql.SQLException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for user group role configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class UserGroupRoleController {

	private static final Logger logger = LoggerFactory.getLogger(UserGroupRoleController.class);

	@Autowired
	private UserGroupRoleService userGroupRoleService;

	@RequestMapping(value = "/deleteUserGroupRole", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteUserGroupRole(@RequestParam("id") String id) {
		logger.debug("Entering deleteUserGroupRole: id='{}'", id);

		AjaxResponse response = new AjaxResponse();

		//id format = <user group id>-<role id>
		String[] values = StringUtils.split(id, "-");

		try {
			userGroupRoleService.deleteUserGroupRole(NumberUtils.toInt(values[0]), NumberUtils.toInt(values[1]));
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

}
