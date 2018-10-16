/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.runreport;

import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.user.User;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private ReportService reportService;

	@RequestMapping(value = "/getLovValues", method = RequestMethod.GET)
	public @ResponseBody
	List<Map<String, String>> getLovValues(@RequestParam("reportId") Integer reportId,
			HttpSession session, HttpServletRequest request, Locale locale) {

		logger.debug("Entering getLovValues: reportId={}", reportId);

		//encapsulate values in a list (will be a json array) to ensure values
		//are displayed in the order given
		List<Map<String, String>> list = new ArrayList<>();
		Map<String, String> values = new HashMap<>();
		ReportRunner reportRunner = new ReportRunner();
		try {
			Report report = reportService.getReport(reportId);
			reportRunner.setReport(report);

			User sessionUser = (User) session.getAttribute("sessionUser");
			reportRunner.setUser(sessionUser);

			ParameterProcessor paramProcessor = new ParameterProcessor();
			ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request, locale);
			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			reportRunner.setReportParamsMap(reportParamsMap);

			values = reportRunner.getLovValues();
		} catch (SQLException | RuntimeException | ParseException | IOException ex) {
			logger.error("Error", ex);
		} finally {
			reportRunner.close();
		}

		for (Entry<String, String> entry : values.entrySet()) {
			Map<String, String> value = new HashMap<>();
			String encodedKey = Encode.forHtmlAttribute(entry.getKey());
			String encodedValue = Encode.forHtmlContent(entry.getValue());
			value.put(encodedKey, encodedValue);
//			value.put(entry.getKey(), entry.getValue());
			list.add(value);
		}

		return list;
	}
}
