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
package art.common;

import art.report.Report;
import art.report.ReportService;
import art.runreport.ReportRunner;
import art.utils.AjaxResponse;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Timothy Anyona
 */
@Controller
public class ChainedParameterController {

	@RequestMapping(value = "/app/getLovValues", method = RequestMethod.GET)
	public @ResponseBody
	Map<Object, String> getLovValues(@RequestParam("reportId") Integer reportId) {
		Map<Object, String> values = null;
		try {
			ReportService reportService = new ReportService();
			ReportRunner reportRunner = new ReportRunner();
			Report report = reportService.getReport(reportId);
			reportRunner.setReport(report);
			values = reportRunner.getLovValues();
		} catch (SQLException ex) {
			Logger.getLogger(ChainedParameterController.class.getName()).log(Level.SEVERE, null, ex);
		}

		return values;

	}
}
