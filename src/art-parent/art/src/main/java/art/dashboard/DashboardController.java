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
package art.dashboard;

import art.enums.ReportFormat;
import art.enums.ReportType;
import art.report.Report;
import art.report.ReportService;
import art.reportoptions.GridstackItemOptions;
import art.reportoptions.GridstackOptions;
import art.reportparameter.ReportParameter;
import art.runreport.ParameterProcessor;
import art.runreport.ParameterProcessorResult;
import art.runreport.ReportOptions;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import art.user.User;
import art.utils.ArtLogsHelper;
import art.utils.ArtUtils;
import art.utils.FilenameHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
	private XPath xPath;
	private Element rootNode;

	@RequestMapping(value = "/showDashboard", method = {RequestMethod.GET, RequestMethod.POST})
	public String showDashboard(@RequestParam("reportId") Integer reportId,
			HttpServletRequest request, Model model, Locale locale,
			HttpSession session) {

		logger.debug("Entering showDashboard: reportId={}", reportId);

		Date startTime = new Date();
		Instant start = Instant.now();

		boolean showInline = BooleanUtils.toBoolean(request.getParameter("showInline"));

		//set appropriate error page to use
		String errorPage;
		if (showInline) {
			errorPage = "reportErrorInline";
		} else {
			errorPage = "reportError";
		}

		User sessionUser = (User) session.getAttribute("sessionUser");

		String reportFormatString = request.getParameter("reportFormat");
		ReportFormat reportFormat = ReportFormat.toEnum(reportFormatString, ReportFormat.html);

		List<ReportParameter> reportParamsList;
		ReportType reportType;

		String description = "";
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
		String startTimeString = df.format(startTime);

		Report report;

		try {
			Report suppliedReport = (Report) request.getAttribute("suppliedReport");
			if (suppliedReport == null) {
				report = reportService.getReport(reportId);
			} else {
				report = suppliedReport;
			}

			if (report == null) {
				model.addAttribute("message", "reports.message.reportNotFound");
				return errorPage;
			}

			//check if user has permission to run report
			//admins can run all reports, even disabled ones. only check for non admin users
			if (!sessionUser.hasConfigureReportsPermission()) {
				if (!report.isActive()) {
					model.addAttribute("message", "reports.message.reportDisabled");
					return errorPage;
				}

				if (!reportService.canUserRunReport(sessionUser, report.getReportId())) {
					model.addAttribute("message", "reports.message.noPermission");
					return errorPage;
				}
			}

			reportType = report.getReportType();
			model.addAttribute("reportType", reportType);

			ParameterProcessor paramProcessor = new ParameterProcessor();
			paramProcessor.setSuppliedReport(report);
			paramProcessor.setIsFragment(true);
			ParameterProcessorResult paramProcessorResult = paramProcessor.processHttpParameters(request, locale);
			Map<String, ReportParameter> reportParamsMap = paramProcessorResult.getReportParamsMap();
			reportParamsList = paramProcessorResult.getReportParamsList();

			//facilitate display of selected parameters in browser
			ReportOptions reportOptions = paramProcessorResult.getReportOptions();
			if (reportOptions.isShowSelectedParameters()) {
				request.setAttribute("reportParamEntries", reportParamsMap);
			}

			RunReportHelper runReportHelper = new RunReportHelper();

			if (reportFormat == ReportFormat.pdf) {
				FilenameHelper filenameHelper = new FilenameHelper();
				String fileName = filenameHelper.getFilename(report, locale, reportFormat, reportParamsMap);
				String exportPath = Config.getReportsExportPath();
				String outputFileName = exportPath + fileName;

				String dynamicOpenPassword = null;
				String dynamicModifyPassword = null;
				PdfDashboard.generatePdf(paramProcessorResult, report, sessionUser, locale, outputFileName, messageSource, dynamicOpenPassword, dynamicModifyPassword);

				request.setAttribute("reportFormat", "pdf");
				request.setAttribute("fileName", fileName);

				String shortDescription = report.getLocalizedShortDescription(locale);
				shortDescription = runReportHelper.performDirectParameterSubstitution(shortDescription, reportParamsMap);

				if (StringUtils.isNotBlank(shortDescription)) {
					description = " :: " + shortDescription;
				}
			} else {
				if (reportType == ReportType.Dashboard) {
					Dashboard dashboard = buildDashboard(report, request, locale, reportParamsMap);
					model.addAttribute("dashboard", dashboard);
				} else if (reportType == ReportType.GridstackDashboard) {
					GridstackDashboard dashboard = buildGridstackDashboard(report, request, locale, reportParamsMap);
					model.addAttribute("dashboard", dashboard);

					boolean exclusiveAccess = reportService.hasOwnerAccess(sessionUser, report.getReportId());
					request.setAttribute("exclusiveAccess", exclusiveAccess);
					request.setAttribute("report", report);

					if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
						request.setAttribute("ajax", true);
					}

					request.setAttribute("requestParameters", runReportHelper.getRequestParametersString(request));

					String options = report.getOptions();
					if (StringUtils.isNotBlank(options)) {
						GridstackOptions gridstackOptions = ArtUtils.jsonToObject(options, GridstackOptions.class);
						String cssFileName = gridstackOptions.getCssFile();
						if (StringUtils.isNotBlank(cssFileName)) {
							String jsTemplatesPath = Config.getJsTemplatesPath();
							String fullCssFileName = jsTemplatesPath + cssFileName;
							File cssFile = new File(fullCssFileName);
							if (!cssFile.exists()) {
								throw new RuntimeException("Css file not found: " + fullCssFileName);
							}
							request.setAttribute("cssFileName", cssFileName);
						}
					}
				}
			}

			boolean allowSelectParameters = BooleanUtils.toBoolean(request.getParameter("allowSelectParameters"));
			if (allowSelectParameters) {
				request.setAttribute("allowSelectParameters", allowSelectParameters);
				runReportHelper.setSelectReportParameterAttributes(report, request, session, locale);
			} else {
				runReportHelper.setRefreshPeriodAttribute(report, request);
			}

			String reportName = report.getLocalizedName(locale);
			model.addAttribute("reportName", reportName);
		} catch (Exception ex) {
			logger.error("Error", ex);
			model.addAttribute("error", ex);
			return errorPage;
		}

		Integer totalTimeSeconds = null;

		Instant end = Instant.now();
		Duration duration = Duration.between(start, end);

		if (reportFormat == ReportFormat.pdf) {
			totalTimeSeconds = (int) duration.getSeconds();
			double preciseTotalTimeSeconds = duration.toMillis() / (double) 1000;
			DecimalFormat df2 = (DecimalFormat) NumberFormat.getInstance(locale);
			df2.applyPattern("#,##0.0##");
			String formattedTotalTime = df2.format(preciseTotalTimeSeconds);
			request.setAttribute("timeTakenSeconds", formattedTotalTime);

			request.setAttribute("description", description);
			request.setAttribute("startTimeString", startTimeString);
		}

		Integer fetchTimeSeconds = null;
		ArtLogsHelper.logReportRun(sessionUser, request.getRemoteAddr(), report.getReportId(), totalTimeSeconds, fetchTimeSeconds, reportFormat.getValue(), reportParamsList);

		if (reportFormat == ReportFormat.pdf) {
			if (showInline) {
				return "showDashboardFileLink";
			} else {
				return "showDashboard";
			}
		} else if (reportType == ReportType.GridstackDashboard) {
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
	 * @param reportParamsMap the report parameters map
	 * @return the dashboard object to be displayed
	 * @throws UnsupportedEncodingException
	 * @throws Exception
	 */
	private Dashboard buildDashboard(Report report, HttpServletRequest request,
			Locale locale, Map<String, ReportParameter> reportParamsMap)
			throws Exception {

		logger.debug("Entering buildDashboard");

		String dashboardXml = report.getReportSource();
		logger.debug("dashboardXml='{}'", dashboardXml);

		if (StringUtils.isBlank(dashboardXml)) {
			throw new IllegalArgumentException("No dashboard content");
		}

		Dashboard dashboard = new Dashboard();

		String dashboardTitle = getDashboardTitle(report, reportParamsMap, locale);

		dashboard.setTitle(dashboardTitle);
		dashboard.setDescription(report.getLocalizedDescription(locale));

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(dashboardXml)));
		rootNode = document.getDocumentElement();

		xPath = XPathFactory.newInstance().newXPath();

		NodeList columnNodes = (NodeList) xPath.evaluate("COLUMN", rootNode, XPathConstants.NODESET);

		List<List<Portlet>> dashboardColumns = new ArrayList<>();
		Map<Integer, DashboardItem> itemsMap = new HashMap<>();

		int itemIndex = 0;
		int columnIndex = 0;
		for (int i = 0; i < columnNodes.getLength(); i++) {
			columnIndex++;

			Node columnNode = columnNodes.item(i);

			List<Portlet> columnPortlets = new ArrayList<>();

			String columnSize = xPath.evaluate("SIZE", columnNode);
			logger.debug("columnSize='{}'", columnSize);

			if (StringUtils.isBlank(columnSize)) {
				columnSize = "auto";
			}

			NodeList portletNodes = (NodeList) xPath.evaluate("PORTLET", columnNode, XPathConstants.NODESET);
			for (int j = 0; j < portletNodes.getLength(); j++) {
				itemIndex++;

				Node portletNode = portletNodes.item(j);

				Portlet portlet = new Portlet();

				portlet.setIndex(itemIndex);
				portlet.setColumnIndex(columnIndex);
				portlet.setRunId(ArtUtils.getUniqueId());

				setPortletProperties(portlet, portletNode, request, locale, columnSize);

				columnPortlets.add(portlet);

				itemsMap.put(portlet.getIndex(), portlet);
			}

			dashboardColumns.add(columnPortlets);
		}

		dashboard.setColumns(dashboardColumns);

		setDashboardTabs(itemsMap, dashboard);

		return dashboard;
	}

	/**
	 * Populates the dashboard tablist according to the defined xml. If no
	 * TABLIST is defined in the dashboard xml, the tablist will be null.
	 *
	 * @param itemsMap the map containing the dashboard's items
	 * @param dashboard the dashboard object to populate
	 * @throws IllegalArgumentException
	 */
	private void setDashboardTabs(Map<Integer, DashboardItem> itemsMap,
			AbstractDashboard dashboard) throws IllegalArgumentException, XPathExpressionException {

		logger.debug("Entering setDashboardTabs");

		Node tabListNode = (Node) xPath.evaluate("TABLIST", rootNode, XPathConstants.NODE);
		if (tabListNode != null) {
			DashboardTabList tabList = new DashboardTabList();
			List<DashboardTab> tabs = new ArrayList<>();

			NodeList tabNodes = (NodeList) xPath.evaluate("TAB", tabListNode, XPathConstants.NODESET);
			for (int i = 0; i < tabNodes.getLength(); i++) {
				Node tabNode = tabNodes.item(i);

				DashboardTab tab = new DashboardTab();

				String title = xPath.evaluate("TITLE", tabNode);
				tab.setTitle(title);

				List<DashboardItem> items = new ArrayList<>();
				NodeList itemNodes = (NodeList) xPath.evaluate("ITEM/text()", tabNode, XPathConstants.NODESET);
				//https://stackoverflow.com/questions/27604529/getting-the-text-content-of-an-xml-element-without-getting-the-text-content-of-i
				for (int j = 0; j < itemNodes.getLength(); j++) {
					Node itemNode = itemNodes.item(j);
					String itemIndexString = itemNode.getNodeValue();
					int tabItemIndex = Integer.parseInt(itemIndexString);
					DashboardItem item = itemsMap.get(tabItemIndex);
					if (item == null) {
						throw new IllegalArgumentException("Invalid item index: " + tabItemIndex);
					}
					items.add(item);
				}

				tab.setItems(items);

				tabs.add(tab);
			}

			tabList.setTabs(tabs);

			int defaultTab;
			String defaultTabString = xPath.evaluate("DEFAULTTAB", tabListNode);
			if (StringUtils.isBlank(defaultTabString)) {
				final int FIRST_TAB_INDEX = 1;
				defaultTab = FIRST_TAB_INDEX;
			} else {
				defaultTab = Integer.parseInt(defaultTabString);
				if (defaultTab < 0 || defaultTab > tabNodes.getLength()) {
					throw new IllegalArgumentException("Invalid default tab: " + defaultTab);
				}
			}
			tabList.setDefaultTab(defaultTab);

			dashboard.setTabList(tabList);
		}
	}

	/**
	 * Sets the properties of a dashboard portlet
	 *
	 * @param portlet the portlet object to set
	 * @param portletNode the portlet's node
	 * @param request the http request
	 * @param locale the locale being used
	 * @param columnSize the size setting of the column
	 * @throws UnsupportedEncodingException
	 * @throws ParseException
	 * @throws javax.xml.xpath.XPathExpressionException
	 */
	private void setPortletProperties(Portlet portlet, Node portletNode,
			HttpServletRequest request, Locale locale, String columnSize)
			throws UnsupportedEncodingException, ParseException,
			XPathExpressionException, JsonProcessingException {

		logger.debug("Entering setPortletProperties");

		int refreshPeriodSeconds = getPortletRefreshPeriod(portletNode);
		portlet.setRefreshPeriodSeconds(refreshPeriodSeconds);

		setPortletUrl(portlet, portletNode, request);

		boolean executeOnLoad = getPortletExecuteOnLoad(portletNode);
		portlet.setExecuteOnLoad(executeOnLoad);

		String title = getPortletTitle(portletNode, request, executeOnLoad, refreshPeriodSeconds, locale);
		portlet.setTitle(title);

		String classNamePrefix = getPortletClassNamePrefix(columnSize);
		portlet.setClassNamePrefix(classNamePrefix);
	}

	/**
	 * Returns the refresh period in seconds for a portlet, or -1 for no
	 * automatic refresh
	 *
	 * @param itemNode the portlet's node
	 * @return the refresh period in seconds for a portlet, or -1 for no
	 * automatic refresh
	 * @throws javax.xml.xpath.XPathExpressionException
	 */
	private int getPortletRefreshPeriod(Node itemNode) throws XPathExpressionException {
		logger.debug("Entering getPortletRefreshPeriod");

		String value = xPath.evaluate("REFRESH", itemNode); //specified in seconds

		int refreshPeriodSeconds;

		if (StringUtils.isBlank(value)) {
			refreshPeriodSeconds = PORTLET_NO_REFRESH_SETTING;
		} else {
			refreshPeriodSeconds = Integer.parseInt(value);
			final int MINIMUM_REFRESH_SECONDS = 5;
			if (refreshPeriodSeconds != PORTLET_NO_REFRESH_SETTING
					&& refreshPeriodSeconds < MINIMUM_REFRESH_SECONDS) {
				throw new IllegalArgumentException("Refresh setting less than minimum. Setting="
						+ refreshPeriodSeconds + ", Minimum=" + MINIMUM_REFRESH_SECONDS);
			}
		}

		return refreshPeriodSeconds;
	}

	/**
	 * Sets the url to use for a portlet's data. Also sets the baseUrl and
	 * parametersJson properties
	 *
	 * @param dashboardItem the portlet object
	 * @param itemNode the portlet's node
	 * @param request
	 * @throws UnsupportedEncodingException
	 * @throws javax.xml.xpath.XPathExpressionException
	 */
	private void setPortletUrl(DashboardItem dashboardItem,
			Node itemNode, HttpServletRequest request)
			throws UnsupportedEncodingException, XPathExpressionException,
			JsonProcessingException {

		//string returning form of xpath.evaluate() returns empty string if tag is not found
		//if require null, pass QName e.g. XPathConstants.NODE
		//https://stackoverflow.com/questions/17390684/jaxp-xpath-1-0-or-2-0-how-to-distinguish-empty-strings-from-non-existent-value
		//https://stackoverflow.com/questions/1985234/string-javax-xml-xpath-xpathexpression-evaluateobject-item-guarantees-it-never
		//allow use of OBJECTID tag (legacy)
		String reportIdString = xPath.evaluate("OBJECTID", itemNode);

		//allow use of QUERYID tag (legacy)
		if (StringUtils.isBlank(reportIdString)) {
			reportIdString = xPath.evaluate("QUERYID", itemNode);
		}

		//allow use of REPORTID tag (3.0+)
		if (StringUtils.isBlank(reportIdString)) {
			reportIdString = xPath.evaluate("REPORTID", itemNode);
		}

		String urlSetting = xPath.evaluate("URL", itemNode);

		setPortletUrl(dashboardItem, reportIdString, urlSetting, request);
	}

	/**
	 * Sets the url to use for a portlet's data. Also sets the baseUrl and
	 * parametersJson properties
	 *
	 * @param dashboardItem the portlet object
	 * @param itemNode the portlet's node
	 * @param request
	 * @throws UnsupportedEncodingException
	 * @throws javax.xml.xpath.XPathExpressionException
	 */
	private void setPortletUrl(DashboardItem dashboardItem,
			String reportIdString, String urlSetting, HttpServletRequest request)
			throws UnsupportedEncodingException, XPathExpressionException,
			JsonProcessingException {

		logger.debug("Entering setPortletUrl");

		String url = null;

		if (StringUtils.isBlank(reportIdString)) {
			//no report defined. use url tag
			url = urlSetting;
		} else {
			Map<String, String[]> requestParameters = request.getParameterMap();
			Map<String, String> singleValueParameters = new HashMap<>();
			Map<String, String[]> multipleValueParameters = new HashMap<>();

			//separate single value and multi value parameters
			//multi value parameters will have [] appended to the parameter name when called by jquery.load() using an object
			//https://sourceforge.net/p/art/discussion/352129/thread/84063367/
			for (Entry<String, String[]> entry : requestParameters.entrySet()) {
				String name = entry.getKey();
				if (StringUtils.startsWithIgnoreCase(name, ArtUtils.PARAM_PREFIX)) {
					String[] values = entry.getValue();
					if (values == null) {
						singleValueParameters.put(name, null);
					} else {
						if (values.length == 1) {
							singleValueParameters.put(name, values[0]);
						} else {
							multipleValueParameters.put(name, values);
						}
					}
				}
			}

			String parametersJson = ArtUtils.objectToJson(singleValueParameters);
			dashboardItem.setParametersJson(parametersJson);

			String baseUrl = request.getContextPath() + "/runReport?reportId="
					+ reportIdString + "&isFragment=true";

			//add multi value parameters to the url
			StringBuilder paramsSb = new StringBuilder();
			for (Entry<String, String[]> entry : multipleValueParameters.entrySet()) {
				String htmlParamName = entry.getKey();
				String[] paramValues = entry.getValue();
				String encodedParamName = URLEncoder.encode(htmlParamName, "UTF-8");
				for (String value : paramValues) {
					String encodedParamValue = URLEncoder.encode(value, "UTF-8");
					paramsSb.append("&").append(encodedParamName).append("=")
							.append(encodedParamValue);
				}
			}

			baseUrl = baseUrl + paramsSb.toString();
			dashboardItem.setBaseUrl(baseUrl);

			int reportId = Integer.parseInt(StringUtils.substringBefore(reportIdString, "&"));
			dashboardItem.setReportId(reportId);
		}

		dashboardItem.setUrl(url);
	}

	/**
	 * Returns the portlet's on-load setting
	 *
	 * @param itemNode the portlet's node
	 * @return the portlet's on-load setting
	 * @throws javax.xml.xpath.XPathExpressionException
	 */
	private boolean getPortletExecuteOnLoad(Node itemNode) throws XPathExpressionException {
		logger.debug("Entering getPortletExecuteOnLoad");

		String value = xPath.evaluate("ONLOAD", itemNode);

		boolean executeOnLoad;
		//can't use BooleanUtils.toBoolean() or Boolean.parseBoolean() because missing tag/empty string means true
		//these methods would evaluate empty string as false
		if (StringUtils.equalsIgnoreCase(value, "false")) {
			executeOnLoad = false;
		} else {
			executeOnLoad = true;
		}

		return executeOnLoad;
	}

	/**
	 * Return's the portlet's title string
	 *
	 * @param itemNode the portlet's node
	 * @param request
	 * @param executeOnLoad whether to execute the portlet on load
	 * @param refreshPeriodSeconds the portlet's refresh period setting
	 * @param locale
	 * @return the portlet's title string
	 * @throws javax.xml.xpath.XPathExpressionException
	 */
	private String getPortletTitle(Node itemNode, HttpServletRequest request,
			boolean executeOnLoad, int refreshPeriodSeconds, Locale locale)
			throws XPathExpressionException {

		String title = xPath.evaluate("TITLE", itemNode);
		return getPortletTitle(title, request, executeOnLoad, refreshPeriodSeconds, locale);

	}

	/**
	 * Return's the portlet's title string
	 *
	 * @param title the base title
	 * @param request
	 * @param executeOnLoad whether to execute the portlet on load
	 * @param refreshPeriodSeconds the portlet's refresh period setting
	 * @param locale
	 * @return the portlet's title string
	 * @throws javax.xml.xpath.XPathExpressionException
	 */
	private String getPortletTitle(String title, HttpServletRequest request,
			boolean executeOnLoad, int refreshPeriodSeconds, Locale locale)
			throws XPathExpressionException {

		logger.debug("Entering getPortletTitle");

		title = StringUtils.trimToEmpty(title);
		title = Encode.forHtmlContent(title);

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
	 * @param reportParamsMap the report parameters map
	 * @return the gridstack dashboard object to be used to display the
	 * gridstack dashboard
	 * @throws Exception
	 */
	private GridstackDashboard buildGridstackDashboard(Report report, HttpServletRequest request,
			Locale locale, Map<String, ReportParameter> reportParamsMap)
			throws Exception {

		logger.debug("Entering buildGridstackDashboard: Report={}", report);

		String dashboardXml = report.getReportSource();
		logger.debug("dashboardXml='{}'", dashboardXml);

		if (StringUtils.isBlank(dashboardXml)) {
			throw new IllegalArgumentException("No dashboard content");
		}

		GridstackDashboard dashboard = new GridstackDashboard();

		String dashboardTitle = getDashboardTitle(report, reportParamsMap, locale);

		dashboard.setTitle(dashboardTitle);
		dashboard.setDescription(report.getLocalizedDescription(locale));

		List<GridstackItem> items = new ArrayList<>();

		List<GridstackItemOptions> itemOptions;
		String savedOptions = report.getGridstackSavedOptions();
		if (StringUtils.isBlank(savedOptions)) {
			itemOptions = new ArrayList<>();
		} else {
			//https://stackoverflow.com/questions/11664894/jackson-deserialize-using-generic-class
			//https://stackoverflow.com/questions/8263008/how-to-deserialize-json-file-starting-with-an-array-in-jackson
			ObjectMapper mapper = new ObjectMapper();
			itemOptions = mapper.readValue(savedOptions, new TypeReference<List<GridstackItemOptions>>() {
			});
		}

		//https://stackoverflow.com/questions/773012/getting-xml-node-text-value-with-java-dom
		//https://stackoverflow.com/questions/4076910/how-to-retrieve-element-value-of-xml-using-java
		//http://www.w3schools.com/xml/xpath_intro.asp
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(dashboardXml)));
		rootNode = document.getDocumentElement();

		xPath = XPathFactory.newInstance().newXPath();

		setGridstackDashboardProperties(dashboard);

		Map<Integer, DashboardItem> itemsMap = new HashMap<>();

		NodeList itemNodes = (NodeList) xPath.evaluate("ITEM", rootNode, XPathConstants.NODESET);
		int itemIndex = 0;
		for (int i = 0; i < itemNodes.getLength(); i++) {
			itemIndex++;

			Node itemNode = itemNodes.item(i);

			GridstackItem item = new GridstackItem();
			item.setIndex(itemIndex);

			setGridstackItemProperties(item, itemNode, request, locale);

			for (GridstackItemOptions itemOption : itemOptions) {
				int index = itemOption.getIndex();
				if (index == itemIndex) {
					item.setxPosition(itemOption.getX());
					item.setyPosition(itemOption.getY());
					item.setWidth(itemOption.getWidth());
					item.setHeight(itemOption.getHeight());
					break;
				}
			}

			items.add(item);

			itemsMap.put(item.getIndex(), item);
		}

		setDashboardTabs(itemsMap, dashboard);

		dashboard.setItems(items);

		return dashboard;
	}

	/**
	 * Sets the properties of a gridstack item
	 *
	 * @param item the gridstack item
	 * @param itemNode the item's node
	 * @param request the http request
	 * @param locale the locale being used
	 * @throws UnsupportedEncodingException
	 * @throws javax.xml.xpath.XPathExpressionException
	 */
	private void setGridstackItemProperties(GridstackItem item, Node itemNode,
			HttpServletRequest request, Locale locale)
			throws UnsupportedEncodingException, XPathExpressionException,
			JsonProcessingException {

		logger.debug("Entering setGridstackItemProperties");

		int refreshPeriodSeconds = getPortletRefreshPeriod(itemNode);
		item.setRefreshPeriodSeconds(refreshPeriodSeconds);

		setPortletUrl(item, itemNode, request);

		boolean executeOnLoad = getPortletExecuteOnLoad(itemNode);
		item.setExecuteOnLoad(executeOnLoad);

		String title = getPortletTitle(itemNode, request, executeOnLoad, refreshPeriodSeconds, locale);
		item.setTitle(title);

		int xPosition;
		String xPositionString = xPath.evaluate("XPOSITION", itemNode);
		if (StringUtils.isBlank(xPositionString)) {
			final int DEFAULT_X_POSITION = 0;
			xPosition = DEFAULT_X_POSITION;
		} else {
			xPosition = Integer.parseInt(xPositionString);
		}
		item.setxPosition(xPosition);

		int yPosition;
		String yPositionString = xPath.evaluate("YPOSITION", itemNode);
		if (StringUtils.isBlank(yPositionString)) {
			final int DEFAULT_Y_POSITION = 0;
			yPosition = DEFAULT_Y_POSITION;
		} else {
			yPosition = Integer.parseInt(yPositionString);
		}
		item.setyPosition(yPosition);

		int width;
		String widthString = xPath.evaluate("WIDTH", itemNode);
		if (StringUtils.isBlank(widthString)) {
			final int DEFAULT_WIDTH = 2;
			width = DEFAULT_WIDTH;
		} else {
			width = Integer.parseInt(widthString);
		}
		item.setWidth(width);

		int height;
		String heightString = xPath.evaluate("HEIGHT", itemNode);
		if (StringUtils.isBlank(heightString)) {
			final int DEFAULT_HEIGHT = 2;
			height = DEFAULT_HEIGHT;
		} else {
			height = Integer.parseInt(heightString);
		}
		item.setHeight(height);

		String autoheightString = xPath.evaluate("AUTOHEIGHT", itemNode);
		boolean autoheight = BooleanUtils.toBoolean(autoheightString);
		item.setAutoheight(autoheight);

		String autowidthString = xPath.evaluate("AUTOWIDTH", itemNode);
		boolean autowidth = BooleanUtils.toBoolean(autowidthString);
		item.setAutowidth(autowidth);

		String noResizeString = xPath.evaluate("NORESIZE", itemNode);
		boolean noResize = BooleanUtils.toBoolean(noResizeString);
		item.setNoResize(noResize);

		String noMoveString = xPath.evaluate("NOMOVE", itemNode);
		boolean noMove = BooleanUtils.toBoolean(noMoveString);
		item.setNoMove(noMove);

		String autopositionString = xPath.evaluate("AUTOPOSITION", itemNode);
		boolean autoposition = BooleanUtils.toBoolean(autopositionString);
		item.setAutoposition(autoposition);

		String lockedString = xPath.evaluate("LOCKED", itemNode);
		boolean locked = BooleanUtils.toBoolean(lockedString);
		item.setLocked(locked);

		int minWidth;
		String minWidthString = xPath.evaluate("MINWIDTH", itemNode);
		if (StringUtils.isBlank(minWidthString)) {
			final int DEFAULT_MIN_WIDTH = 0; //also used in showGridstackDashboardInline.jsp
			minWidth = DEFAULT_MIN_WIDTH;
		} else {
			minWidth = Integer.parseInt(minWidthString);
		}
		item.setMinWidth(minWidth);

		int minHeight;
		String minHeightString = xPath.evaluate("MINHEIGHT", itemNode);
		if (StringUtils.isBlank(minHeightString)) {
			final int DEFAULT_MIN_HEIGHT = 0; //also used in showGridstackDashboardInline.jsp
			minHeight = DEFAULT_MIN_HEIGHT;
		} else {
			minHeight = Integer.parseInt(minHeightString);
		}
		item.setMinHeight(minHeight);

		int maxWidth;
		String maxWidthString = xPath.evaluate("MAXWIDTH", itemNode);
		if (StringUtils.isBlank(maxWidthString)) {
			final int DEFAULT_MAX_WIDTH = 0; //also used in showGridstackDashboardInline.jsp
			maxWidth = DEFAULT_MAX_WIDTH;
		} else {
			maxWidth = Integer.parseInt(maxWidthString);
		}
		item.setMaxWidth(maxWidth);

		int maxHeight;
		String maxHeightString = xPath.evaluate("MAXHEIGHT", itemNode);
		if (StringUtils.isBlank(maxHeightString)) {
			final int DEFAULT_MAX_HEIGHT = 0; //also used in showGridstackDashboardInline.jsp
			maxHeight = DEFAULT_MAX_HEIGHT;
		} else {
			maxHeight = Integer.parseInt(maxHeightString);
		}
		item.setMaxHeight(maxHeight);
	}

	/**
	 * Sets the properties of the overall gridstack grid
	 *
	 * @param dashboard dashboard object whose properties will be set
	 * @throws javax.xml.xpath.XPathExpressionException
	 */
	private void setGridstackDashboardProperties(GridstackDashboard dashboard)
			throws XPathExpressionException {

		logger.debug("Entering setGridstackDashboardProperties");

		String dashboardWidthString = xPath.evaluate("DASHBOARDWIDTH", rootNode);
		logger.debug("dashboardWidthString='{}'", dashboardWidthString);

		if (StringUtils.isNotBlank(dashboardWidthString)) {
			int dashboardWidth = Integer.parseInt(dashboardWidthString);
			dashboard.setWidth(dashboardWidth);
		}

		String floatEnabledString = xPath.evaluate("FLOAT", rootNode);
		logger.debug("floatEnabledString='{}'", floatEnabledString);
		//use boolean utils to allow use of "yes" and "on" in addition to "true" (case insensitive)
		//Boolean.parseBoolean() only allows "true" (case insensitive)
		boolean floatEnabled = BooleanUtils.toBoolean(floatEnabledString);
		dashboard.setFloatEnabled(floatEnabled);

		String animateString = xPath.evaluate("ANIMATE", rootNode);
		logger.debug("animateString='{}'", animateString);
		boolean animate = BooleanUtils.toBoolean(animateString);
		dashboard.setAnimate(animate);

		String disableDragString = xPath.evaluate("DISABLEDRAG", rootNode);
		logger.debug("disableDragString='{}'", disableDragString);
		boolean disableDrag = BooleanUtils.toBoolean(disableDragString);
		dashboard.setDisableDrag(disableDrag);

		String disableResizeString = xPath.evaluate("DISABLERESIZE", rootNode);
		logger.debug("disableResizeString='{}'", disableResizeString);
		boolean disableResize = BooleanUtils.toBoolean(disableResizeString);
		dashboard.setDisableResize(disableResize);

		String cellHeight = xPath.evaluate("CELLHEIGHT", rootNode);
		logger.debug("cellHeight='{}'", cellHeight);
		if (StringUtils.isNotBlank(cellHeight)) {
			dashboard.setCellHeight(cellHeight);
		}

		String verticalMargin = xPath.evaluate("VERTICALMARGIN", rootNode);
		logger.debug("verticalMargin='{}'", verticalMargin);
		if (StringUtils.isNotBlank(verticalMargin)) {
			dashboard.setVerticalMargin(verticalMargin);
		}

		String alwaysShowResizeHandleString = xPath.evaluate("ALWAYSSHOWRESIZEHANDLE", rootNode);
		logger.debug("alwaysShowResizeHandleString='{}'", alwaysShowResizeHandleString);
		boolean alwaysShowResizeHandle = BooleanUtils.toBoolean(alwaysShowResizeHandleString);
		dashboard.setAlwaysShowResizeHandle(alwaysShowResizeHandle);

		String dashboardHeightString = xPath.evaluate("DASHBOARDHEIGHT", rootNode);
		logger.debug("dashboardHeightString='{}'", dashboardHeightString);
		if (StringUtils.isNotBlank(dashboardHeightString)) {
			int dashboardHeight = Integer.parseInt(dashboardHeightString);
			dashboard.setHeight(dashboardHeight);
		}
	}

	/**
	 * Returns the string to be used as the dashboard title
	 *
	 * @param report the dashboard report
	 * @param reportParamsMap the report parameters map
	 * @param locale the locale being used
	 * @return the string to be used as the dashboard title
	 * @throws ParseException
	 * @throws SQLException
	 */
	private String getDashboardTitle(Report report, Map<String, ReportParameter> reportParamsMap, Locale locale)
			throws ParseException, SQLException, IOException {

		logger.debug("Entering getDashboardTitle: Report={}", report);

		String shortDescription = report.getLocalizedShortDescription(locale);
		logger.debug("shortDescription='{}'", shortDescription);

		RunReportHelper runReportHelper = new RunReportHelper();
		String dashboardTitle = runReportHelper.performDirectParameterSubstitution(shortDescription, reportParamsMap);
		logger.debug("dashboardTitle='{}'", dashboardTitle);

		return dashboardTitle;
	}

}
