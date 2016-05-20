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
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportRunner;
import art.user.User;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller for chained parameter calls
 * 
 * @author Timothy Anyona
 */
@Controller
public class ChainedParameterController {

	private static final Logger logger = LoggerFactory.getLogger(ChainedParameterController.class);

	@RequestMapping(value = "/app/getLovValues", method = RequestMethod.GET)
	public @ResponseBody
	Map<String, String> getLovValues(@RequestParam("reportId") Integer reportId,
			HttpSession session, HttpServletRequest request) {

		logger.debug("Entering getLovValues: reportId={}", reportId);

		Map<String, String> values = new HashMap<>();

		ReportRunner reportRunner = null;

		try {
			ReportService reportService = new ReportService();

			reportRunner = new ReportRunner();
			Report report = reportService.getReport(reportId);
			reportRunner.setReport(report);

			User sessionUser = (User) session.getAttribute("sessionUser");
			String username = sessionUser.getUsername();
			reportRunner.setUsername(username);

			ParameterProcessor paramProcessor = new ParameterProcessor();
			ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);
			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			reportRunner.setReportParamsMap(reportParamsMap);

			boolean useRules = false;
			values = reportRunner.getLovValues(useRules);
		} catch (SQLException | ParseException ex) {
			logger.error("Error", ex);
		} finally {
			if (reportRunner != null) {
				reportRunner.close();
			}
		}

		return values;
	}
}
