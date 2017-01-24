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

import art.enums.ReportType;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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

	private final int PORTLET_NO_REFRESH_SETTING = -1; //also used in showDashboardInline.jsp and showGridstackDashboardInline.jsp

	@RequestMapping(value = "/app/showDashboard", method = {RequestMethod.GET, RequestMethod.POST})
	public String showDashboard(@RequestParam("reportId") Integer reportId,
			HttpServletRequest request, Model model, Locale locale,
			HttpSession session) {

		logger.debug("Entering showDashboard: reportId={}", reportId);

		ReportType reportType;

		String errorPage = "reportError";

		try {
			Report report = reportService.getReport(reportId);

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

			reportType = report.getReportType();
			model.addAttribute("reportType", reportType);

			if (reportType == ReportType.Dashboard) {
				Dashboard dashboard = buildDashboard(report, request, locale);
				model.addAttribute("dashboard", dashboard);
			} else if (reportType == ReportType.GridstackDashboard) {
				GridstackDashboard dashboard = buildGridstackDashboard(report, request, locale);
				model.addAttribute("dashboard", dashboard);
			}

			model.addAttribute("reportName", report.getName());
		} catch (SQLException | RuntimeException | UnsupportedEncodingException | ParseException ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
			return errorPage;
		}

		boolean showInline = Boolean.valueOf(request.getParameter("showInline"));

		if (reportType == ReportType.GridstackDashboard) {
			if (showInline) {
				return "showGridstackDashboardInline";
			} else {
				return "showDashboard";
			}
		} else {
			if (showInline) {
				return "showDashboardInline";
			} else {
				return "showDashboard";
			}
		}
	}

	/**
	 * Returns the dashboard object to be displayed, based on the given report
	 * object
	 *
	 * @param report the report to use
	 * @param request the http request
	 * @param locale the locale being used
	 * @return the dashboard object to be displayed
	 * @throws UnsupportedEncodingException
	 * @throws SQLException
	 * @throws ParseException
	 */
	private Dashboard buildDashboard(Report report, HttpServletRequest request,
			Locale locale) throws UnsupportedEncodingException, ParseException, SQLException {

		logger.debug("Entering buildDashboard");

		Dashboard dashboard = new Dashboard();

		String dashboardTitle = getDashboardTitle(request, report);

		dashboard.setTitle(dashboardTitle);
		dashboard.setDescription(report.getDescription());

		String dashboardXml = report.getReportSource();
		logger.debug("dashboardXml='{}'", dashboardXml);

		List<String> columnsXml = XmlParser.getXmlElementValues(dashboardXml, "COLUMN");

		List<List<Portlet>> dashboardColumns = new ArrayList<>();

		int columnIndex = 0;
		for (String columnXml : columnsXml) {
			columnIndex++;
			logger.debug("columnXml='{}'", columnXml);

			List<Portlet> columnPortlets = new ArrayList<>();
			String columnSize = XmlParser.getXmlElementValue(columnXml, "SIZE");
			logger.debug("columnSize='{}'", columnSize);
			if (columnSize == null) {
				columnSize = "auto";
			}

			List<String> portletsXml = XmlParser.getXmlElementValues(columnXml, "PORTLET");
			int portletIndex = 0;
			for (String portletXml : portletsXml) {
				portletIndex++;
				logger.debug("portletXml='{}'", portletXml);

				Portlet portlet = new Portlet();

				setPortletProperties(portlet, portletXml, request, locale, columnSize, columnIndex, portletIndex, report);

				columnPortlets.add(portlet);
			}

			dashboardColumns.add(columnPortlets);
		}

		dashboard.setColumns(dashboardColumns);

		return dashboard;
	}

	/**
	 * Sets the properties of a dashboard portlet
	 *
	 * @param portlet the portlet object to set
	 * @param portletXml the portlet's xml
	 * @param request the http request
	 * @param locale the locale being used
	 * @param columnSize the size setting of the column
	 * @param columnIndex the index of the column in which the portlet is
	 * contained
	 * @param portletIndex the index for the portlet within the column
	 * @param report the dashboard report
	 * @throws UnsupportedEncodingException
	 * @throws ParseException
	 * @throws SQLException
	 */
	private void setPortletProperties(Portlet portlet, String portletXml,
			HttpServletRequest request, Locale locale, String columnSize,
			int columnIndex, int portletIndex, Report report)
			throws UnsupportedEncodingException, ParseException, SQLException {

		logger.debug("Entering setPortletProperties");

		String id = getPortletId(columnIndex, portletIndex, report);
		portlet.setId(id);

		int refreshPeriodSeconds = getPortletRefreshPeriod(portletXml);
		portlet.setRefreshPeriodSeconds(refreshPeriodSeconds);

		String url = getPortletUrl(portletXml, request);
		portlet.setUrl(url);

		boolean executeOnLoad = getPortletExecuteOnLoad(portletXml);
		portlet.setExecuteOnLoad(executeOnLoad);

		String title = getPortletTitle(portletXml, request, executeOnLoad, refreshPeriodSeconds, locale);
		portlet.setTitle(title);

		String classNamePrefix = getPortletClassNamePrefix(columnSize);
		portlet.setClassNamePrefix(classNamePrefix);
	}

	/**
	 * Returns a unique id to identify a portlet
	 *
	 * @param columnIndex the index of the column in which the portlet is
	 * contained
	 * @param portletIndex the index for the portlet within the column
	 * @param report the dashboard report
	 * @return a unique id to identify a portlet
	 */
	private String getPortletId(int columnIndex, int portletIndex, Report report) {
		//use a fixed/determinable value instead of a random value to cater for some users needs
		//https://sourceforge.net/p/art/discussion/352129/thread/ee7c78d4/#592d
		String id = String.valueOf(columnIndex) + "_" + String.valueOf(portletIndex) + "_" + String.valueOf(report.getReportId());
		return id;
	}

	/**
	 * Returns the refresh period in seconds for a portlet, or -1 for no
	 * automatic refresh
	 *
	 * @param portletXml the portlet's xml
	 * @return the refresh period in seconds for a portlet, or -1 for no
	 * automatic refresh
	 */
	private int getPortletRefreshPeriod(String portletXml) {
		logger.debug("Entering getPortletRefreshPeriod");

		String value = XmlParser.getXmlElementValue(portletXml, "REFRESH"); //specified in seconds

		int refreshPeriodSeconds;

		final int MINIMUM_REFRESH_SECONDS = 5;

		if (StringUtils.isBlank(value)) {
			refreshPeriodSeconds = PORTLET_NO_REFRESH_SETTING;
		} else {
			refreshPeriodSeconds = Integer.parseInt(value);
			if (refreshPeriodSeconds < MINIMUM_REFRESH_SECONDS) {
				throw new IllegalArgumentException("Refresh setting less than minimum. Setting="
						+ refreshPeriodSeconds + ", Minimum=5");
			}
		}

		return refreshPeriodSeconds;
	}

	/**
	 * Returns the url to use for a portlet's data
	 *
	 * @param portletXml the portlet's xml
	 * @param request
	 * @return the url to use for the portlet's data
	 * @throws UnsupportedEncodingException
	 * @throws SQLException
	 * @throws ParseException
	 */
	private String getPortletUrl(String portletXml, HttpServletRequest request)
			throws UnsupportedEncodingException {

		logger.debug("Entering getPortletUrl");

		String url;

		// Get the portlet xml info
		url = XmlParser.getXmlElementValue(portletXml, "OBJECTID");

		//allow use of QUERYID tag
		if (url == null) {
			url = XmlParser.getXmlElementValue(portletXml, "QUERYID");
		}

		//allow use of REPORTID tag
		if (url == null) {
			url = XmlParser.getXmlElementValue(portletXml, "REPORTID");
		}

		if (url == null) {
			//no report defined. use url tag
			url = XmlParser.getXmlElementValue(portletXml, "URL");
		} else {
			// context path as suffix + build url + switch off html header&footer 
			int reportId = Integer.parseInt(url);

			url = request.getContextPath() + "/app/runReport.do?reportId=" + reportId
					+ "&isFragment=true";

			//add report parameters
			StringBuilder paramsSb = new StringBuilder(254);
			Map<String, String[]> requestParameters = request.getParameterMap();
			for (Entry<String, String[]> entry : requestParameters.entrySet()) {
				String htmlParamName = entry.getKey();
				if (htmlParamName.startsWith("p-")) {
					String[] paramValues = entry.getValue();
					for (String value : paramValues) {
						String encodedParamValue = URLEncoder.encode(value, "UTF-8");
						paramsSb.append("&").append(htmlParamName).append("=").append(encodedParamValue);
					}
				}
			}

			url = url + paramsSb.toString();
		}

		return url;
	}

	/**
	 * Returns the portlet's on-load setting
	 *
	 * @param portletXml the portlet's xml
	 * @return the portlet's on-load setting
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
	 * @param refreshPeriodSeconds the portlet's refresh period setting
	 * @param locale
	 * @return the portlet's title string
	 */
	private String getPortletTitle(String portletXml, HttpServletRequest request,
			boolean executeOnLoad, int refreshPeriodSeconds, Locale locale) {

		logger.debug("Entering getPortletTitle");

		String title = XmlParser.getXmlElementValue(portletXml, "TITLE");

		String contextPath = request.getContextPath();
		if (!executeOnLoad) {
			title = title + "  <img src='" + contextPath + "/images/onLoadFalse.gif' title='"
					+ messageSource.getMessage("portlets.text.onLoadFalse", null, locale) + "'/>";
		}

		if (refreshPeriodSeconds != PORTLET_NO_REFRESH_SETTING) {
			title = title + " <img src='" + contextPath + "/images/clock_mini.gif' title='"
					+ messageSource.getMessage("portlets.text.autoRefresh", null, locale)
					+ " " + refreshPeriodSeconds + " "
					+ messageSource.getMessage("portlets.text.seconds", null, locale)
					+ "'/> <small>" + refreshPeriodSeconds + "s</small>";
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

	/**
	 * Returns the gridstack dashboard object to be used to display the
	 * gridstack dashboard
	 *
	 * @param report the gridstack dashboard report
	 * @param request the http request
	 * @param locale the locale being used
	 * @return the gridstack dashboard object to be used to display the
	 * gridstack dashboard
	 * @throws SQLException
	 * @throws ParseException
	 * @throws UnsupportedEncodingException
	 */
	private GridstackDashboard buildGridstackDashboard(Report report, HttpServletRequest request,
			Locale locale) throws SQLException, ParseException, UnsupportedEncodingException {

		logger.debug("Entering buildGridstackDashboard: Report={}", report);

		String dashboardXml = report.getReportSource();
		logger.debug("dashboardXml='{}'", dashboardXml);

		GridstackDashboard dashboard = new GridstackDashboard();

		String dashboardTitle = getDashboardTitle(request, report);

		dashboard.setTitle(dashboardTitle);
		dashboard.setDescription(report.getDescription());

		setGridstackDashboardProperties(dashboardXml, dashboard);

		List<GridstackItem> items = new ArrayList<>();
		List<String> itemsXml = XmlParser.getXmlElementValues(dashboardXml, "ITEM");
		int itemIndex = 0;
		for (String itemXml : itemsXml) {
			itemIndex++;
			logger.debug("itemXml='{}'", itemXml);

			GridstackItem item = new GridstackItem();

			setGridstackItemProperties(item, itemXml, request, locale, itemIndex, report);

			items.add(item);
		}

		dashboard.setItems(items);

		return dashboard;
	}

	/**
	 * Sets the properties of a gridstack item
	 *
	 * @param item the gridstack item
	 * @param itemXml the item's xml
	 * @param request the http request
	 * @param locale the locale being used
	 * @param itemIndex the index of the item within the dashboard
	 * @param report the dashboard report
	 * @throws UnsupportedEncodingException
	 */
	private void setGridstackItemProperties(GridstackItem item, String itemXml,
			HttpServletRequest request, Locale locale, int itemIndex, Report report)
			throws UnsupportedEncodingException {

		logger.debug("Entering setGridstackItemProperties");

		String id = getGridstackItemId(itemIndex, report);
		item.setId(id);

		int refreshPeriodSeconds = getPortletRefreshPeriod(itemXml);
		item.setRefreshPeriodSeconds(refreshPeriodSeconds);

		String url = getPortletUrl(itemXml, request);
		item.setUrl(url);

		boolean executeOnLoad = getPortletExecuteOnLoad(itemXml);
		item.setExecuteOnLoad(executeOnLoad);

		String title = getPortletTitle(itemXml, request, executeOnLoad, refreshPeriodSeconds, locale);
		item.setTitle(title);

		int xPosition = getGridstackItemXPosition(itemXml);
		item.setxPosition(xPosition);

		int yPosition = getGridstackItemYPosition(itemXml);
		item.setyPosition(yPosition);

		int width = getGridstackItemWidth(itemXml);
		item.setWidth(width);

		int height = getGridstackItemHeight(itemXml);
		item.setHeight(height);

		boolean noResize = getGridstackItemNoResizeSetting(itemXml);
		item.setNoResize(noResize);

		boolean noMove = getGridstackItemNoMoveSetting(itemXml);
		item.setNoMove(noMove);

		boolean autoposition = getGridstackItemAutopositionSetting(itemXml);
		item.setAutoposition(autoposition);

		boolean locked = getGridstackItemLockedSetting(itemXml);
		item.setLocked(locked);

		int minWidth = getGridstackItemMinWidth(itemXml);
		item.setMinWidth(minWidth);

		int minHeight = getGridstackItemMinHeight(itemXml);
		item.setMinHeight(minHeight);

		int maxWidth = getGridstackItemMaxWidth(itemXml);
		item.setMaxWidth(maxWidth);

		int maxHeight = getGridstackItemMaxHeight(itemXml);
		item.setMaxHeight(maxHeight);
	}

	/**
	 * Returns a unique id to identify a gridstack item
	 *
	 * @param itemIndex the index of the item within the dashboard
	 * @param report the dashboard report
	 * @return a unique id to identify a gridstack item
	 */
	private String getGridstackItemId(int itemIndex, Report report) {
		//use a fixed/determinable value instead of a random value to cater for some users needs
		//https://sourceforge.net/p/art/discussion/352129/thread/ee7c78d4/#592d
		String id = String.valueOf(itemIndex) + "_" + String.valueOf(report.getReportId());
		return id;
	}

	/**
	 * Sets the properties of the overall gridstack grid
	 *
	 * @param dashboardXml complete dashboard xml
	 * @param dashboard dashboard object whose properties will be set
	 */
	private void setGridstackDashboardProperties(String dashboardXml, GridstackDashboard dashboard) {
		logger.debug("Entering setGridstackDashboardProperties");

		String dashboardWidthString = XmlParser.getXmlElementValue(dashboardXml, "DASHBOARDWIDTH");
		logger.debug("dashboardWidthString='{}'", dashboardWidthString);

		int dashboardWidth;
		if (StringUtils.isBlank(dashboardWidthString)) {
			final int DEFAULT_DASHBOARD_WIDTH = 12;
			dashboardWidth = DEFAULT_DASHBOARD_WIDTH;
		} else {
			dashboardWidth = Integer.parseInt(dashboardWidthString);
		}
		dashboard.setWidth(dashboardWidth);

		String floatEnabledString = XmlParser.getXmlElementValue(dashboardXml, "FLOAT");
		logger.debug("floatEnabledString='{}'", floatEnabledString);
		//use boolean utils to allow use of "yes" and "on" in addition to "true" (case insensitive)
		//Boolean.parseBoolean() only allows "true" (case insensitive)
		boolean floatEnabled = BooleanUtils.toBoolean(floatEnabledString);
		dashboard.setFloatEnabled(floatEnabled);

		String animateString = XmlParser.getXmlElementValue(dashboardXml, "ANIMATE");
		logger.debug("animateString='{}'", animateString);
		boolean animate = BooleanUtils.toBoolean(animateString);
		dashboard.setAnimate(animate);

		String disableDragString = XmlParser.getXmlElementValue(dashboardXml, "DISABLEDRAG");
		logger.debug("disableDragString='{}'", disableDragString);
		boolean disableDrag = BooleanUtils.toBoolean(disableDragString);
		dashboard.setDisableDrag(disableDrag);

		String disableResizeString = XmlParser.getXmlElementValue(dashboardXml, "DISABLERESIZE");
		logger.debug("disableResizeString='{}'", disableResizeString);
		boolean disableResize = BooleanUtils.toBoolean(disableResizeString);
		dashboard.setDisableResize(disableResize);

		String cellHeight = XmlParser.getXmlElementValue(dashboardXml, "CELLHEIGHT");
		logger.debug("cellHeight='{}'", cellHeight);
		if (StringUtils.isBlank(cellHeight)) {
			final String DEFAULT_CELL_HEIGHT = "60px";
			cellHeight = DEFAULT_CELL_HEIGHT;
		}
		dashboard.setCellHeight(cellHeight);

		String verticalMargin = XmlParser.getXmlElementValue(dashboardXml, "VERTICALMARGIN");
		logger.debug("verticalMargin='{}'", verticalMargin);
		if (StringUtils.isBlank(verticalMargin)) {
			final String DEFAULT_VERTICAL_MARGIN = "20px";
			verticalMargin = DEFAULT_VERTICAL_MARGIN;
		}
		dashboard.setVerticalMargin(verticalMargin);

		String alwaysShowResizeHandleString = XmlParser.getXmlElementValue(dashboardXml, "ALWAYSSHOWRESIZEHANDLE");
		logger.debug("alwaysShowResizeHandleString='{}'", alwaysShowResizeHandleString);
		boolean alwaysShowResizeHandle = BooleanUtils.toBoolean(alwaysShowResizeHandleString);
		dashboard.setAlwaysShowResizeHandle(alwaysShowResizeHandle);

		String dashboardHeightString = XmlParser.getXmlElementValue(dashboardXml, "DASHBOARDHEIGHT");
		logger.debug("dashboardHeightString='{}'", dashboardHeightString);
		int dashboardHeight;
		if (StringUtils.isBlank(dashboardHeightString)) {
			final int DEFAULT_DASHBOARD_HEIGHT = 0; //0 means no maximum
			dashboardHeight = DEFAULT_DASHBOARD_HEIGHT;
		} else {
			dashboardHeight = Integer.parseInt(dashboardHeightString);
		}
		dashboard.setHeight(dashboardHeight);
	}

	/**
	 * Returns the string to be used as the dashboard title
	 *
	 * @param request the http request
	 * @param report the dashboard report
	 * @return the string to be used as the dashboard title
	 * @throws ParseException
	 * @throws SQLException
	 */
	private String getDashboardTitle(HttpServletRequest request, Report report)
			throws ParseException, SQLException {

		logger.debug("Entering getDashboardTitle: Report={}", report);

		ParameterProcessor paramProcessor = new ParameterProcessor();
		ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request);
		Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();

		String shortDescription = report.getShortDescription();
		logger.debug("shortDescription='{}'", shortDescription);

		RunReportHelper runReportHelper = new RunReportHelper();
		String dashboardTitle = runReportHelper.performDirectParameterSubstitution(shortDescription, reportParamsMap);
		logger.debug("dashboardTitle='{}'", dashboardTitle);

		return dashboardTitle;
	}

	/**
	 * Returns the gridstack item's x position
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's x position
	 */
	private int getGridstackItemXPosition(String itemXml) {
		logger.debug("Entering getGridstackItemXPosition");

		String xPositionString = XmlParser.getXmlElementValue(itemXml, "XPOSITION");
		int xPosition = Integer.parseInt(xPositionString);

		return xPosition;
	}

	/**
	 * Returns the gridstack item's y position
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's y position
	 */
	private int getGridstackItemYPosition(String itemXml) {
		logger.debug("Entering getGridstackItemYPosition");

		String yPositionString = XmlParser.getXmlElementValue(itemXml, "YPOSITION");
		int yPosition = Integer.parseInt(yPositionString);

		return yPosition;
	}

	/**
	 * Returns the gridstack item's width
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's width
	 */
	private int getGridstackItemWidth(String itemXml) {
		logger.debug("Entering getGridstackItemWidth");

		String widthString = XmlParser.getXmlElementValue(itemXml, "WIDTH");
		int width = Integer.parseInt(widthString);

		return width;
	}

	/**
	 * Returns the gridstack item's height
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's height
	 */
	private int getGridstackItemHeight(String itemXml) {
		logger.debug("Entering getGridstackItemHeight");

		String heightString = XmlParser.getXmlElementValue(itemXml, "HEIGHT");
		int height = Integer.parseInt(heightString);

		return height;
	}

	/**
	 * Returns the gridstack item's no-resize setting
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's no-resize setting
	 */
	private boolean getGridstackItemNoResizeSetting(String itemXml) {
		logger.debug("Entering getGridstackItemNoResizeSetting");

		String noResizeString = XmlParser.getXmlElementValue(itemXml, "NORESIZE");
		boolean noResize = BooleanUtils.toBoolean(noResizeString);

		return noResize;
	}

	/**
	 * Returns the gridstack item's no-move setting
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's no-move setting
	 */
	private boolean getGridstackItemNoMoveSetting(String itemXml) {
		logger.debug("Entering getGridstackItemNoMoveSetting");

		String noMoveString = XmlParser.getXmlElementValue(itemXml, "NOMOVE");
		boolean noMove = BooleanUtils.toBoolean(noMoveString);

		return noMove;
	}

	/**
	 * Returns the gridstack item's autoposition setting
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's autoposition setting
	 */
	private boolean getGridstackItemAutopositionSetting(String itemXml) {
		logger.debug("Entering getGridstackItemAutopositionSetting");

		String autopositionString = XmlParser.getXmlElementValue(itemXml, "AUTOPOSITION");
		boolean autoposition = BooleanUtils.toBoolean(autopositionString);

		return autoposition;
	}

	/**
	 * Returns the gridstack item's locked setting
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's locked setting
	 */
	private boolean getGridstackItemLockedSetting(String itemXml) {
		logger.debug("Entering getGridstackItemLockedSetting");

		String lockedString = XmlParser.getXmlElementValue(itemXml, "LOCKED");
		boolean locked = BooleanUtils.toBoolean(lockedString);

		return locked;
	}

	/**
	 * Returns the gridstack item's minimum width
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's minimum width
	 */
	private int getGridstackItemMinWidth(String itemXml) {
		logger.debug("Entering getGridstackItemMinWidth");

		int minWidth;
		String minWidthString = XmlParser.getXmlElementValue(itemXml, "MINWIDTH");

		if (StringUtils.isBlank(minWidthString)) {
			final int DEFAULT_MIN_WIDTH = 0; //also used in showGridstackDashboardInline.jsp
			minWidth = DEFAULT_MIN_WIDTH;
		} else {
			minWidth = Integer.parseInt(minWidthString);
		}

		return minWidth;
	}

	/**
	 * Returns the gridstack item's minimum height
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's minimum height
	 */
	private int getGridstackItemMinHeight(String itemXml) {
		logger.debug("Entering getGridstackItemMinHeight");

		int minHeight;
		String minHeightString = XmlParser.getXmlElementValue(itemXml, "MINHEIGHT");

		if (StringUtils.isBlank(minHeightString)) {
			final int DEFAULT_MIN_HEIGHT = 0; //also used in showGridstackDashboardInline.jsp
			minHeight = DEFAULT_MIN_HEIGHT;
		} else {
			minHeight = Integer.parseInt(minHeightString);
		}

		return minHeight;
	}

	/**
	 * Returns the gridstack item's maximum width
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's maximum width
	 */
	private int getGridstackItemMaxWidth(String itemXml) {
		logger.debug("Entering getGridstackItemMaxWidth");

		int maxWidth;
		String maxWidthString = XmlParser.getXmlElementValue(itemXml, "MAXWIDTH");

		if (StringUtils.isBlank(maxWidthString)) {
			final int DEFAULT_MAX_WIDTH = 0; //also used in showGridstackDashboardInline.jsp
			maxWidth = DEFAULT_MAX_WIDTH;
		} else {
			maxWidth = Integer.parseInt(maxWidthString);
		}

		return maxWidth;
	}

	/**
	 * Returns the gridstack item's maximum height
	 *
	 * @param itemXml the item's xml
	 * @return the gridstack item's maximum height
	 */
	private int getGridstackItemMaxHeight(String itemXml) {
		logger.debug("Entering getGridstackItemMaxHeight");

		int maxHeight;
		String maxHeightString = XmlParser.getXmlElementValue(itemXml, "MAXHEIGHT");

		if (StringUtils.isBlank(maxHeightString)) {
			final int DEFAULT_MAX_HEIGHT = 0; //also used in showGridstackDashboardInline.jsp
			maxHeight = DEFAULT_MAX_HEIGHT;
		} else {
			maxHeight = Integer.parseInt(maxHeightString);
		}

		return maxHeight;
	}
}
