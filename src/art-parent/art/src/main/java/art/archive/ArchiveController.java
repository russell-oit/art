/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.archive;

import art.user.User;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for displaying the archives page
 * 
 * @author Timothy Anyona
 */
@Controller
public class ArchiveController {
	
	private static final Logger logger = LoggerFactory.getLogger(ArchiveController.class);

	@Autowired
	private ArchiveService archiveService;

	@RequestMapping(value = "/archives", method = RequestMethod.GET)
	public String showArchives(HttpSession session, Model model) {
		logger.debug("Entering showArchives");
		
		User sessionUser = (User) session.getAttribute("sessionUser");
		
		try {
			List<Archive> archives = archiveService.getArchives(sessionUser.getUserId());
			model.addAttribute("archives", archives);
		} catch (SQLException | RuntimeException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}
		
		return "archives";
	}
}
