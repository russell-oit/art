/**
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.user;

import art.reportgroup.ReportGroup;
import art.reportgroup.ReportGroupService;
import art.usergroup.UserGroup;
import art.usergroup.UserGroupService;
import java.sql.SQLException;
import java.util.List;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Spring controller for user add and edit processes
 *
 * @author Timothy Anyona
 */
@Controller
public class SaveUserController {

	private static final Logger logger = LoggerFactory.getLogger(SaveUserController.class);

	@Autowired
	private UserService userService;
	
	@Autowired
	private UserGroupService userGroupService;
	
	@Autowired
	private ReportGroupService reportGroupService;

	@ModelAttribute("allUserGroups")
	public List<UserGroup> addAllUserGroups(Model model) {
		try {
			return userGroupService.getAllUserGroups();
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}
		
		return null;
	}
	
	@ModelAttribute("reportGroups")
	public List<ReportGroup> addReportGroups(Model model) {
		try {
			return reportGroupService.getAllReportGroups();
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}
		
		return null;
	}

	@RequestMapping(value = "/app/addUser", method = RequestMethod.GET)
	public String showAddUser(Model model) {
		User user=new User();
		
		//set defaults for new users
		user.setActive(true);
		user.setCanChangePassword(true);
		
		model.addAttribute("user", user);
		model.addAttribute("action", "add");
		return "editUser";
	}

	@RequestMapping(value = "/app/addUser", method = RequestMethod.POST)
	public String processAddUser(@ModelAttribute("user") @Valid User user,
			BindingResult result, Model model, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			model.addAttribute("action", "add");
			return "editUser";
		}

		try {
			userService.addUser(user);
			redirectAttributes.addFlashAttribute("message", "users.message.userAdded");
			return "redirect:/app/users.do";
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", "add");
		return "editUser";
	}

	@RequestMapping(value = "/app/editUser", method = RequestMethod.GET)
	public String showEditUser(@RequestParam("userId") Integer userId, Model model) {
		try {
			User user = userService.getUser(userId);
			model.addAttribute("user", user);
		} catch (SQLException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		model.addAttribute("action", "edit");
		return "editUser";
	}

}
