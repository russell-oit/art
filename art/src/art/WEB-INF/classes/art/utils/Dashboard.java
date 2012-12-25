/*
 * Copyright (C) 2001/2003  Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   (version 2) along with this program (see documentation directory); 
 *   otherwise, have a look at http://www.gnu.org or write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */
/**
 * This class parses the dashboard XML definitions
 * 
*/
package art.utils;

import art.servlets.ArtDBCP;
import java.sql.*;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class parses the dashboard XML definitions
 *
 * @author Enrico Liboni
 */
public class Dashboard {

	final static Logger logger = LoggerFactory.getLogger(Dashboard.class);
	int columnsCount;  // number of columns in this dashboard
	List[] portlets; // portlets[0] contains a list of Xml strings representing the portlets in the first column, etc
	String[] columnSize;
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

		/* Load portlets info */
		Connection conn = null;
		try {
			String dashboardXml = null;
			conn = ArtDBCP.getConnection();

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

			portlets = new List[columnsCount];
			columnSize = new String[columnsCount];

			for (int i = 0; i < columnsCount; i++) { // for each column
				String columnXml = columns.get(i);
				String size = XmlParser.getXmlElementValue(columnXml, "SIZE");
				if (size == null) {
					size = "auto";
				}
				columnSize[i] = size;
				portlets[i] = XmlParser.getXmlElementValues(columnXml, "PORTLET");
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
			conn = ArtDBCP.getConnection();
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
		// Get the portlet xml info
		String portletXml = (String) portlets[col].get(row);
		String link = XmlParser.getXmlElementValue(portletXml, "OBJECTID");
		//allow use of QUERYID tag
		if(link==null){
			link = XmlParser.getXmlElementValue(portletXml, "QUERYID");
		}
		
		if (link == null) {
			return XmlParser.getXmlElementValue(portletXml, "URL");
		} else {
			// context path as suffix + build url + switch off html header&footer and add parameters
			StringBuilder linkSb = new StringBuilder();
			boolean getDefaultParameters = true;
			java.util.Enumeration names = request.getParameterNames();
			while (names.hasMoreElements()) {
				String name = (String) names.nextElement();
				if (name.startsWith("P_")) {
					linkSb.append("&" + name + "=" + request.getParameter(name));
					getDefaultParameters = false; // the URL has parameters, thus we'll not get the defaults
				}
			}
			link = QueryUrl.getExecuteUrl(Integer.parseInt(link), false, getDefaultParameters) + "&_isFragment=Y";
			return request.getContextPath() + link + linkSb.toString();
		}
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
		String portletXml = (String) portlets[col].get(row);
		String value = XmlParser.getXmlElementValue(portletXml, "REFRESH");

		int defaultRefresh = 5;
		if (value != null) {
			if (NumberUtils.isNumber(value)) {
				if (Integer.parseInt(value) < defaultRefresh) {
					value = String.valueOf(defaultRefresh);
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
		String portletXml = (String) portlets[col].get(row);
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
		String portletXml = (String) portlets[col].get(row);
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
		return columnSize[col].toUpperCase(); // common for all portlets of same column
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
		return portlets[col].size();
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
