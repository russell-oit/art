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
import art.runreport.RunReportHelper;
import art.user.User;
import art.utils.XmlParser;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for displaying a dashboard report
 *
 * @author Timothy Anyona
 */
@Controller
public class DashboardController {

	private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

	@Autowired
	private ReportService reportService;

	@Autowired
	private MessageSource messageSource;

	@RequestMapping(value = "/app/showDashboard", method = {RequestMethod.GET, RequestMethod.POST})
	public String showDashboard(@RequestParam("reportId") Integer reportId,
			HttpServletRequest request, Model model, Locale locale,
			HttpSession session) {

		logger.debug("Entering showDashboard: reportId={}", reportId);

		try {
			Report report = reportService.getReport(reportId);

			String errorPage = "reportError";

			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return errorPage;
			}

			//check if user has permission to run report
			//admins can run all reports, even disabled ones. only check for non admin users
			User sessionUser = (User) session.getAttribute("sessionUser");

			if (!sessionUser.isAdminUser()) {
				if (!report.isActive()) {
					model.addAttribute("message", "reports.message.reportDisabled");
					return errorPage;
				}

				if (!reportService.canUserRunReport(sessionUser.getUserId(), reportId)) {
					model.addAttribute("message", "reports.message.noPermission");
					return errorPage;
				}
			}

			Dashboard dashboard = buildDashboard(report, request, locale);
			model.addAttribute("dashboard", dashboard);

			model.addAttribute("reportName", report.getName());
		} catch (SQLException | UnsupportedEncodingException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
		}
		
		boolean showInline = Boolean.valueOf(request.getParameter("showInline"));
		
		if(showInline){
			return "showDashboardInline";
		} else {
			return "showDashboard";
		}
	}

	/**
	 * Returns the dashboard object to be displayed, based on the given report
	 * object
	 *
	 * @param report the report to use
	 * @param request
	 * @param locale
	 * @return the dashboard object to be displayed
	 * @throws UnsupportedEncodingException
	 * @throws SQLException
	 * @throws ParseException
	 */
	private Dashboard buildDashboard(Report report, HttpServletRequest request,
			Locale locale) throws UnsupportedEncodingException, SQLException, ParseException {

		logger.debug("Entering buildDashboard");

		Dashboard dashboard = new Dashboard();

		ParameterProcessor paramProcessor = new ParameterProcessor();
		ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);

		Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();

		String shortDescription = report.getShortDescription();
		RunReportHelper runReportHelper = new RunReportHelper();
		shortDescription = runReportHelper.performDirectParameterSubstitution(shortDescription, reportParamsMap);

		dashboard.setTitle(shortDescription);
		dashboard.setDescription(report.getDescription());

		String dashboardXml = report.getReportSource();
		logger.debug("dashboardXml='{}'", dashboardXml);

		List<String> columnsXml = XmlParser.getXmlElementValues(dashboardXml, "COLUMN");

		List<List<Portlet>> dashboardColumns = new ArrayList<>();

		for (String columnXml : columnsXml) {
			logger.debug("columnXml='{}'", columnXml);

			List<Portlet> columnPortlets = new ArrayList<>();
			String columnSize = XmlParser.getXmlElementValue(columnXml, "SIZE");
			logger.debug("columnSize='{}'", columnSize);
			if (columnSize == null) {
				columnSize = "auto";
			}

			List<String> portletsXml = XmlParser.getXmlElementValues(columnXml, "PORTLET");
			for (String portletXml : portletsXml) {
				logger.debug("portletXml='{}'", portletXml);

				Portlet portlet = new Portlet();

				String source = RandomStringUtils.randomAlphanumeric(5);
				portlet.setSource(source);

				String refreshPeriod = getPortletRefreshPeriod(portletXml);
				portlet.setRefreshPeriod(refreshPeriod);

				String link = getPortletLink(portletXml, request);
				portlet.setBaseUrl(link);

				boolean executeOnLoad = getPortletExecuteOnLoad(portletXml);
				portlet.setExecuteOnLoad(executeOnLoad);

				String title = getPortletTitle(portletXml, request, executeOnLoad, refreshPeriod, locale);
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

	/**
	 * Returns the refresh period for a portlet
	 *
	 * @param portletXml the portlet's xml
	 * @return the refresh period for the portlet
	 */
	private String getPortletRefreshPeriod(String portletXml) {
		logger.debug("Entering getPortletRefreshPeriod");

		String value = XmlParser.getXmlElementValue(portletXml, "REFRESH"); //specified in seconds

		String refreshPeriodString = null;
		int minimumRefreshSeconds = 5;
		if (value != null) {
			if (NumberUtils.isNumber(value)) {
				if (Integer.parseInt(value) < minimumRefreshSeconds) {
					refreshPeriodString = String.valueOf(minimumRefreshSeconds);
				} else {
					refreshPeriodString = value;
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

	/**
	 * Return's the link to use for a portlet
	 *
	 * @param portletXml the portlet's xml
	 * @param request
	 * @return the link to use for the portlet
	 * @throws UnsupportedEncodingException
	 * @throws SQLException
	 * @throws ParseException
	 */
	private String getPortletLink(String portletXml, HttpServletRequest request) throws UnsupportedEncodingException, SQLException, ParseException {
		logger.debug("Entering getPortletLink");

		String link;

		// Get the portlet xml info
		link = XmlParser.getXmlElementValue(portletXml, "OBJECTID");

		//allow use of QUERYID tag
		if (link == null) {
			link = XmlParser.getXmlElementValue(portletXml, "QUERYID");
		}

		//allow use of REPORTID tag
		if (link == null) {
			link = XmlParser.getXmlElementValue(portletXml, "REPORTID");
		}

		if (link == null) {
			//no report defined. use url tag
			link = XmlParser.getXmlElementValue(portletXml, "URL");
		} else {
			// context path as suffix + build url + switch off html header&footer 
			int reportId = Integer.parseInt(link);

			link = request.getContextPath() + "/app/runReport.do?reportId=" + reportId
					+ "&isFragment=true";

			//add report parameters
			StringBuilder paramsSb = new StringBuilder(254);
			Enumeration<String> htmlParamNames = request.getParameterNames();
			while (htmlParamNames.hasMoreElements()) {
				String htmlParamName = htmlParamNames.nextElement();
				if (htmlParamName.startsWith("p-")) {
					String[] paramValues = request.getParameterValues(htmlParamName);
					for (String value : paramValues) {
						String encodedParamValue = URLEncoder.encode(value, "UTF-8");
						paramsSb.append("&").append(htmlParamName).append("=").append(encodedParamValue);
					}
				}
			}

			link = link + paramsSb.toString();
		}

		return link;
	}

	/**
	 * Returns the portlet's on load setting
	 *
	 * @param portletXml the portlet's xml
	 * @return the portlet's on load setting
	 */
	private boolean getPortletExecuteOnLoad(String portletXml) {
		logger.debug("Entering getPortletExecuteOnLoad");

		String value = XmlParser.getXmlElementValue(portletXml, "ONLOAD");

		boolean executeOnLoad = true;
		if (StringUtils.equalsIgnoreCase(value, "false")) {
			executeOnLoad = false;
		}

		return executeOnLoad;
	}

	/**
	 * Return's the portlet's title string
	 *
	 * @param portletXml the portlet's xml
	 * @param request
	 * @param executeOnLoad whether to execute the portlet on load
	 * @param refreshPeriod the portlet's refresh period setting
	 * @param locale
	 * @return the portlet's title string
	 */
	private String getPortletTitle(String portletXml, HttpServletRequest request,
			boolean executeOnLoad, String refreshPeriod, Locale locale) {

		logger.debug("Entering getPortletTitle");

		String title = XmlParser.getXmlElementValue(portletXml, "TITLE");

		String contextPath = request.getContextPath();
		if (!executeOnLoad) {
			title = title + "  <img src='" + contextPath + "/images/onLoadFalse.gif' title='"
					+ messageSource.getMessage("portlets.text.onLoadFalse", null, locale) + "'/>";
		}
		if (StringUtils.isNotEmpty(refreshPeriod)) {
			title = title + " <img src='" + contextPath + "/images/clock_mini.gif' title='"
					+ messageSource.getMessage("portlets.text.autoRefresh", null, locale)
					+ " " + refreshPeriod + " "
					+ messageSource.getMessage("portlets.text.seconds", null, locale)
					+ "'/> <small>" + refreshPeriod + "s</small>";
		}
		return title;
	}

	/**
	 * Returns the class name prefix to use for the portlet
	 *
	 * @param columnSize the portlet's column size setting
	 * @return the class name prefix to use for the portlet
	 */
	private String getPortletClassNamePrefix(String columnSize) {
		logger.debug("Entering getPortletClassNamePrefix: columnSize='{}'", columnSize);

		String prefix = "portlet" + StringUtils.upperCase(columnSize);
		return prefix;
	}
}
