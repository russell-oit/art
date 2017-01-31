/**
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.schedule;

import art.user.User;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for schedule configuration
 *
 * @author Timothy Anyona
 */
@Controller
public class ScheduleController {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

	@Autowired
	private ScheduleService scheduleService;

	@RequestMapping(value = "/schedules", method = RequestMethod.GET)
	public String showSchedules(Model model) {
		logger.debug("Entering showSchedules");

		try {
			model.addAttribute("schedules", scheduleService.getAllSchedules());
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "schedules";
	}

	@RequestMapping(value = "/deleteSchedule", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteSchedule(@RequestParam("id") Integer id) {
		logger.debug("Entering deleteSchedule: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			scheduleService.deleteSchedule(id);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/deleteSchedules", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse deleteSchedules(@RequestParam("ids[]") Integer[] ids) {
		logger.debug("Entering deleteSchedules: ids={}", (Object) ids);

		AjaxResponse response = new AjaxResponse();

		try {
			scheduleService.deleteSchedules(ids);
			response.setSuccess(true);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/getSchedule", method = RequestMethod.POST)
	public @ResponseBody
	AjaxResponse getSchedule(@RequestParam("id") Integer id) {
		logger.debug("Entering getSchedule: id={}", id);

		AjaxResponse response = new AjaxResponse();

		try {
			Schedule schedule = scheduleService.getSchedule(id);
			response.setSuccess(true);
			response.setData(schedule);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			response.setErrorMessage(ex.toString());
		}

		return response;
	}

	@RequestMapping(value = "/addSchedule", method = RequestMethod.GET)
	public String addSchedule(Model model) {
		logger.debug("Entering addSchedule");

		model.addAttribute("schedule", new Schedule());
		
		return showEditSchedule("add", model);
	}

	@RequestMapping(value = "/editSchedule", method = RequestMethod.GET)
	public String editSchedule(@RequestParam("id") Integer id, Model model) {
		logger.debug("Entering editSchedule: id={}", id);

		try {
			model.addAttribute("schedule", scheduleService.getSchedule(id));
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSchedule("edit", model);
	}

	@RequestMapping(value = "/saveSchedule", method = RequestMethod.POST)
	public String saveSchedule(@ModelAttribute("schedule") @Valid Schedule schedule,
			@RequestParam("action") String action,
			BindingResult result, Model model, RedirectAttributes redirectAttributes,
			HttpSession session) {

		logger.debug("Entering saveSchedule: schedule={}, action='{}'", schedule, action);

		logger.debug("result.hasErrors()={}", result.hasErrors());
		if (result.hasErrors()) {
			model.addAttribute("formErrors", "");
			return showEditSchedule(action, model);
		}

		//remove spaces in schedule fields. not legal but may commonly be put by users
		schedule.setMinute(StringUtils.remove(schedule.getMinute(), " "));
		schedule.setHour(StringUtils.remove(schedule.getHour(), " "));
		schedule.setDay(StringUtils.remove(schedule.getDay(), " "));
		schedule.setMonth(StringUtils.remove(schedule.getMonth(), " "));
		schedule.setWeekday(StringUtils.remove(schedule.getWeekday(), " "));

		try {
			User sessionUser = (User) session.getAttribute("sessionUser");
			if (StringUtils.equals(action, "add")) {
				scheduleService.addSchedule(schedule, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordAdded");
			} else if (StringUtils.equals(action, "edit")) {
				scheduleService.updateSchedule(schedule, sessionUser);
				redirectAttributes.addFlashAttribute("recordSavedMessage", "page.message.recordUpdated");
			}
			redirectAttributes.addFlashAttribute("recordName", schedule.getName());
			return "redirect:/schedules";
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return showEditSchedule(action, model);
	}

	/**
	 * Prepares model data and returns the jsp file to display
	 *
	 * @param action the action to use
	 * @param model the model to use
	 * @return the jsp file to display
	 */
	private String showEditSchedule(String action, Model model) {
		logger.debug("Entering showSchedule: action='{}'", action);

		model.addAttribute("action", action);
		
		return "editSchedule";
	}
}
