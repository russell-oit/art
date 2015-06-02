/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This class parses the dashboard XML definitions
 * 
*/
package art.utils;

import art.servlets.Config;
import java.sql.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class parses the dashboard XML definitions
 *
 * @author Enrico Liboni
 */
public class Dashboard {

	private static final Logger logger = LoggerFactory.getLogger(Dashboard.class);
	int columnsCount;  // number of columns in this dashboard
	List<List<String>> portlets; // portlets[0] contains a list of Xml strings representing the portlets in the first column, etc
	List<String> columnSize;
	String title;
	String description;

	/**
	 *
	 */
	public Dashboard() {
	}

	/**
	 * Load information and build data structure with the XML tags values
	 *
	 * @param dashboardId query id of the dashboard
	 * @throws ArtException
	 */
	public void load(int dashboardId) throws ArtException {

		/* Load dashboard info */
		Connection conn = null;
		try {
			String dashboardXml = null;
			conn = Config.getConnection();

			ArtQuery aq = new ArtQuery();
			if (aq.create(conn, dashboardId)) {
				title = aq.getShortDescription();
				description = aq.getDescription();
				dashboardXml = aq.getText();
			} else {
				throw new ArtException("Not able to load header or source code for dashboard id: " + dashboardId);
			}

			List<String> columns = XmlParser.getXmlElementValues(dashboardXml, "COLUMN");
			columnsCount = columns.size(); // number of cols

			portlets = new ArrayList<List<String>>(columnsCount);
			columnSize = new ArrayList<String>(columnsCount);

			for (String columnXml : columns) {
				String size = XmlParser.getXmlElementValue(columnXml, "SIZE");
				if (size == null) {
					size = "auto";
				}
				columnSize.add(size);
				portlets.add(XmlParser.getXmlElementValues(columnXml, "PORTLET"));
			}


		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("Exception while initializing dashboard id: " + dashboardId + " Exception: " + e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
				throw new ArtException("Not able to close connection properly for dashboard id: " + dashboardId);
			}
		}
	}

	/**
	 *
	 * @param dashboardId query id of the dashboard
	 * @return query ids of all art queries in the dashboard
	 * @throws ArtException
	 */
	public static List<Integer> getQueryIds(int dashboardId) throws ArtException {
		List<Integer> queryIds = new ArrayList<Integer>();

		Connection conn = null;
		try {
			String portletXml = null;
			conn = Config.getConnection();
			ArtQuery aq = new ArtQuery();
			if (aq.create(conn, dashboardId)) {
				portletXml = aq.getText();
			} else {
				throw new ArtException("Not able to load source for dashboard id: " + dashboardId);
			}
			// get list of query ids used by this dashboard
			List<String> queryIdStrings = XmlParser.getXmlElementValues(portletXml, "OBJECTID");
			//allow use of QUERYID tag
			queryIdStrings.addAll(XmlParser.getXmlElementValues(portletXml, "QUERYID"));

			for (String id : queryIdStrings) {
				queryIds.add(Integer.valueOf(id));
			}
		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("Exception while getting query array for dashboard id: " + dashboardId + " Exception: " + e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
				throw new ArtException("Not able to close connection properly for dashboard id: " + dashboardId);
			}
		}

		return queryIds;
	}

	/**
	 * Returns the link to embed in the portlet. It can be a absolute URL
	 * (specified within the <URL> tag) or the link to execute an ART query
	 *
	 * @param col
	 * @param row
	 * @param request
	 * @return the link to embed in the portlet
	 * @throws ArtException
	 */
	public String getPortletLink(int col, int row, HttpServletRequest request) throws ArtException {

		String link;

		// Get the portlet xml info
		String portletXml = portlets.get(col).get(row);
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
			boolean getDefaultParameters = true;
			@SuppressWarnings("rawtypes")
			Enumeration names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				if (name.startsWith("P_")) {
					paramsSb.append("&").append(name).append("=").append(request.getParameter(name));
					getDefaultParameters = false; // the URL has parameters, thus we'll not use the defaults
				} else if (name.startsWith("M_")) {
					String[] paramValues=request.getParameterValues(name);
					for(String value:paramValues){
						paramsSb.append("&").append(name).append("=").append(value);
					}
				}
			}
			link = QueryUrl.getExecuteUrl(Integer.parseInt(link), false, getDefaultParameters) + "&_isFragment=Y";
			link = request.getContextPath() + link + paramsSb.toString();
		}

		return link;
	}

	/**
	 *
	 * @param col
	 * @param row
	 * @return portlet refresh interval
	 * @throws ArtException
	 */
	public String getPortletRefresh(int col, int row) throws ArtException {
		// Get the portlet xml info
		String portletXml = portlets.get(col).get(row);
		String value = XmlParser.getXmlElementValue(portletXml, "REFRESH");

		int minimumRefresh = 5;
		if (value != null) {
			if (NumberUtils.isNumber(value)) {
				if (Integer.parseInt(value) < minimumRefresh) {
					value = String.valueOf(minimumRefresh);
				}
			} else {
				value = null; //invalid number specified. default to no refresh
			}
		}


		return value;
	}

	/**
	 *
	 * @param col
	 * @param row
	 * @return <code>true</code> if portlet to be executed on load
	 * @throws ArtException
	 */
	public boolean getPortletOnLoad(int col, int row) throws ArtException {
		// Get the portlet xml info
		String portletXml = portlets.get(col).get(row);
		String value = XmlParser.getXmlElementValue(portletXml, "ONLOAD");

		boolean executeOnLoad = true;
		if (StringUtils.equalsIgnoreCase(value, "false")) {
			executeOnLoad = false;
		}

		return executeOnLoad;
	}

	/**
	 *
	 * @param col
	 * @param row
	 * @return title for a particular portlet
	 * @throws ArtException
	 */
	public String getPortletTitle(int col, int row) throws ArtException {
		// Get the portlet xml info
		String portletXml = portlets.get(col).get(row);
		return XmlParser.getXmlElementValue(portletXml, "TITLE");
	}

	/**
	 * Returns the column size, i.e. the css class name used to render the
	 * portlet Values are: small, medium, large, auto
	 *
	 * @param col
	 * @return the css class name used to render the portlet
	 */
	public String getColumnSize(int col) {
		return columnSize.get(col).toUpperCase(Locale.ENGLISH); // common for all portlets of same column
	}

	/* Dashboard attributes */
	/**
	 * returns the number of columns in this dashboard
	 *
	 * @return the number of columns in this dashboard
	 */
	public int getColumnsCount() {
		return columnsCount;
	}

	/**
	 * returns the number of portlets in the dashboard's column
	 *
	 * @param col
	 * @return the number of portlets in the dashboard's column
	 */
	public int getPortletsCount(int col) {
		return portlets.get(col).size();
	}

	/**
	 *
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}
}
