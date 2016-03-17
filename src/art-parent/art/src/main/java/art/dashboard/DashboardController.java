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
package art.dashboard;

import art.report.Report;
import art.report.ReportService;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.utils.XmlParser;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Timothy Anyona
 */
@Controller
public class DashboardController {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DashboardController.class);

	@Autowired
	private ReportService reportService;

	@RequestMapping(value = "/app/showDashboard", method = {RequestMethod.GET, RequestMethod.POST})
	public String showDashboard(@RequestParam("reportId") Integer reportId,
			HttpServletRequest request, Model model) {

		try {
			Report report = reportService.getReport(reportId);

			//TODO add/consolidate report exists and user has access code with run report controller
			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return "showDashboard";
			}

			Dashboard dashboard = buildDashboard(report, request);
			model.addAttribute("dashboard", dashboard);

		} catch (SQLException | UnsupportedEncodingException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}

		return "showDashboard";
	}

	private Dashboard buildDashboard(Report report, HttpServletRequest request) throws UnsupportedEncodingException, SQLException, ParseException {
		Dashboard dashboard = new Dashboard();

		dashboard.setTitle(report.getShortDescription());
		dashboard.setDescription(report.getDescription());

		String dashboardXml = report.getReportSource();

		List<String> columnsXml = XmlParser.getXmlElementValues(dashboardXml, "COLUMN");

		List<List<Portlet>> dashboardColumns = new ArrayList<>();

		for (String columnXml : columnsXml) { // for each column
			List<Portlet> columnPortlets = new ArrayList<>();
			String columnSize = XmlParser.getXmlElementValue(columnXml, "SIZE");
			if (columnSize == null) {
				columnSize = "auto";
			}

			List<String> portletsXml = XmlParser.getXmlElementValues(columnXml, "PORTLET");
			for (String portletXml : portletsXml) {
				Portlet portlet = new Portlet();

				String refreshPeriod = getPortletRefreshPeriod(portletXml);
				portlet.setRefreshPeriod(refreshPeriod);

				String link = getPortletLink(portletXml, request);
				portlet.setBaseUrl(link);

				boolean executeOnLoad = getPortletExecuteOnLoad(portletXml);
				portlet.setExecuteOnLoad(executeOnLoad);

				String title = getPortletTitle(portletXml);
				portlet.setTitle(title);

				String classNamePrefix = getPortletClassNamePrefix(columnSize);
				portlet.setClassNamePrefix(classNamePrefix);

				columnPortlets.add(portlet);
			}

			dashboardColumns.add(columnPortlets);
		}

		dashboard.setColumns(dashboardColumns);

		return dashboard;
	}

	private String getPortletRefreshPeriod(String portletXml) {
		String value = XmlParser.getXmlElementValue(portletXml, "REFRESH");

		String refreshPeriodString = null;
		int minimumRefresh = 5;
		if (value != null) {
			if (NumberUtils.isNumber(value)) {
				if (Integer.parseInt(value) < minimumRefresh) {
					refreshPeriodString = String.valueOf(minimumRefresh);
				}
			} else {
				refreshPeriodString = null; //invalid number specified. default to no refresh
			}
		}

		if (refreshPeriodString == null) {
			refreshPeriodString = "";
		}

		return refreshPeriodString;
	}

	private String getPortletLink(String portletXml, HttpServletRequest request) throws UnsupportedEncodingException, SQLException, ParseException {
		String link;

		// Get the portlet xml info
		link = XmlParser.getXmlElementValue(portletXml, "OBJECTID");
		//allow use of QUERYID tag
		if (link == null) {
			link = XmlParser.getXmlElementValue(portletXml, "QUERYID");
		}

		if (link == null) {
			//no query defined. use url tag
			link = XmlParser.getXmlElementValue(portletXml, "URL");
		} else {
			// context path as suffix + build url + switch off html header&footer and add parameters
			StringBuilder paramsSb = new StringBuilder(254);
			ParameterProcessor paramProcessor = new ParameterProcessor();
			ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);
			List<ReportParameter> reportParamsList = paramProcessorResult.getReportParamsList();

			List<String> paramsList = new ArrayList<>();
			for (ReportParameter reportParam : reportParamsList) {
				String htmlName = reportParam.getHtmlElementName();
				String value = reportParam.getHtmlValue();
				String finalValue = URLEncoder.encode(value, "UTF-8"); //TODO also encode in drilldown link helper?
				String htmlParam = htmlName + "=" + finalValue;
				paramsList.add(htmlParam);
			}
			String params = StringUtils.join(paramsList, "&");

			int reportId = Integer.parseInt(request.getParameter("reportId"));
			link = request.getContextPath() + "/app/runReport.do?reportId=1" 
					+ "&isFragment=true" + params;
		}

		return link;
	}

	private boolean getPortletExecuteOnLoad(String portletXml) {
		String value = XmlParser.getXmlElementValue(portletXml, "ONLOAD");

		boolean executeOnLoad = true;
		if (StringUtils.equalsIgnoreCase(value, "false")) {
			executeOnLoad = false;
		}

		return executeOnLoad;
	}

	private String getPortletTitle(String portletXml) {
		String title = XmlParser.getXmlElementValue(portletXml, "TITLE");
		return title;
	}

	private String getPortletClassNamePrefix(String columnSize) {
		String prefix = "portlet" + StringUtils.upperCase(columnSize);
		return prefix;
	}
}
