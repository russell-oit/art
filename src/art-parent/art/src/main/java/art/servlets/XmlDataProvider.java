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
package art.servlets;

import art.utils.ArtQuery;
import art.utils.ArtQueryParam;
import art.utils.PreparedQuery;
import art.utils.UserEntity;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.ajaxtags.servlets.BaseAjaxServlet;
import net.sourceforge.ajaxtags.xml.AjaxXmlBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to return xml or html fragments to be used in ajaxtags components.
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class XmlDataProvider extends BaseAjaxServlet {

	private static final long serialVersionUID = 1L;
	final static Logger logger = LoggerFactory.getLogger(XmlDataProvider.class);

	/**
	 * Main method from which the other methods that return ajax data are
	 * called.
	 *
	 * @param request http request object
	 * @param response http response object
	 * @return ajax xml response or html response depending on the ajax tag used
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	public String getXmlContent(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String result = ""; //return ajax xml response or html response depending on the ajax tag used

		// make sure data is not cached
		response.setHeader("Cache-control", "no-cache");
		request.setCharacterEncoding("UTF-8");

		String action = request.getParameter("action");

		String username = (String) request.getSession().getAttribute("username");
		if (username == null) {
			username = "public_user";
		}

		if (StringUtils.equalsIgnoreCase(action,"lov")) {
			result = getXmlLov(request, response, username);
		} else if (StringUtils.equalsIgnoreCase(action,"queries")) {
			result = getXmlQueries(request, response);
		} else if (StringUtils.equalsIgnoreCase(action,"queriesadmin")) {
			result = getXmlAdminQueries(request, response, username);
		} else if (StringUtils.equalsIgnoreCase(action,"querydescr")) {
			result = getHtmlQueryDescription(request, response);
		} else if (StringUtils.equalsIgnoreCase(action,"schedule")) {
			result = getXmlSchedule(request, response);
		}

		return result;
	}

	/**
	 * Get ajax response for populating select box for chained parameters.
	 *
	 * @param request http request object
	 * @param response http response object
	 * @return ajax response for populating select box for chained parameters
	 * @throws IOException
	 */
	private String getXmlLov(HttpServletRequest request, HttpServletResponse response, String username) {

		AjaxXmlBuilder builder = new AjaxXmlBuilder();

		try {
			response.setContentType("text/xml;charset=utf-8");

			// Get parameters
			final String filterLabel = "filter";
			String[] filterValues = null; //in case master is a multi parameter
			String filterValue = request.getParameter(filterLabel);
			String isMultiValue = request.getParameter("isMulti");
			int queryId = Integer.parseInt(request.getParameter("queryId"));

			boolean isMulti = false;
			if (isMultiValue != null) {
				isMulti = true;
			}

			if (filterValue != null) { //filtervalue may be null on first display with a multi master when nothing is selected
				//determine if chained value is a multi parameter
				boolean isChainedValueMulti = false;
				if (StringUtils.startsWith(filterValue, "M_")) {
					isChainedValueMulti = true;
					List<String> values = new ArrayList<String>();
					String[] pairs = StringUtils.split(filterValue, "&");
					for (String pair : pairs) {
						values.add(StringUtils.substringAfter(pair, "="));
					}
					filterValues = values.toArray(new String[values.size()]);
				}

				//execute lov to get values to display
				PreparedQuery pq = new PreparedQuery();
				pq.setUsername(username);
				pq.setQueryId(queryId);

				Map<String, String> inlineParams = new HashMap<String, String>();
				Map<String, String[]> multiParams = new HashMap<String, String[]>();
				ArtQuery aq = new ArtQuery();
				Map<String, ArtQueryParam> htmlParams = aq.getHtmlParams(queryId);
				if (htmlParams.isEmpty()) {
					if (isChainedValueMulti) {
						//don't add ALL_ITEMS to multiParams map. Current logic works that way
						if (!StringUtils.equals(filterValues[0], "ALL_ITEMS")) {
							multiParams.put(filterLabel, filterValues);
						}
					} else {
						inlineParams.put(filterLabel, filterValue);
					}
				} else {
					ArtQueryParam param = htmlParams.get("P_filter");
					if(param==null){
						//filter may be a multi parameter
						param=htmlParams.get("M_filter");
					}
					if (param != null) {
						if (isChainedValueMulti) {
							//don't add ALL_ITEMS to multiParams map. Current logic works that way
							if (!StringUtils.equals(filterValues[0], "ALL_ITEMS")) {
								multiParams.put(filterLabel, filterValues);
							}
							param.setParamValue(filterValues);
						} else {
							inlineParams.put(filterLabel, filterValue);
							param.setParamValue(filterValue);
						}
					}
				}

				if (isChainedValueMulti) {
					pq.setMultiParams(multiParams);
				} else {
					pq.setInlineParams(inlineParams);
				}

				pq.setHtmlParams(htmlParams);

				Map<String, String> lov = pq.executeLovQuery(false); //don't apply rules

				if (isMulti) {
					//add All as the first item displayed
					ResourceBundle messages = ResourceBundle.getBundle("art.i18n.ArtMessages", request.getLocale());
					builder.addItem(messages.getString("allItems"), "ALL_ITEMS");
				}

				//add other items
				for (Map.Entry<String, String> entry : lov.entrySet()) {
					String value = entry.getKey();
					String viewColumnValue = entry.getValue();
					builder.addItem(parseXml(viewColumnValue), parseXml(value));
				}
				pq.close();
			}

		} catch (Exception e) {
			logger.error("Error", e);
		}

		return builder.toString();

	}

	/**
	 * Get the list of queries a user can view.
	 *
	 * @param request http request object
	 * @param response http response object
	 * @param conn connection to art repository
	 * @return ajax response for the list of queries a user can view
	 * @throws IOException
	 */
	private String getXmlQueries(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		AjaxXmlBuilder builder = new AjaxXmlBuilder();

		response.setContentType("text/xml;charset=utf-8");

		int groupId = -1;
		String group = request.getParameter("groupId");
		if (StringUtils.length(group) > 0) {
			groupId = Integer.parseInt(group);
		}

		if (groupId != -1) {
			UserEntity ue = (UserEntity) request.getSession().getAttribute("ue");
			Map<String, Integer> map = ue.getAvailableQueries(groupId);

			for (Map.Entry<String, Integer> entry : map.entrySet()) {
				builder.addItem(entry.getKey(), String.valueOf(entry.getValue()));
			}
		}

		return builder.toString();
	}

	/**
	 * Get the list of queries an admin can view.
	 *
	 * @param request http request object
	 * @param response http response object
	 * @param conn connection to art repository
	 * @return ajax response for the list of queries an admin can view
	 * @throws IOException
	 * @throws SQLException
	 */
	private String getXmlAdminQueries(HttpServletRequest request, HttpServletResponse response, String username) {

		AjaxXmlBuilder builder = new AjaxXmlBuilder();

		response.setContentType("text/xml;charset=utf-8");

		int accessLevel = ((Integer) request.getSession().getAttribute("AdminLevel")).intValue();

		int groupId = Integer.parseInt(request.getParameter("groupId"));
		if (groupId != -1) {

			String sql;
			if (accessLevel <= 30) {
				// get only queries with Group and datasource matching the "junior" admin priviledges
				sql = "SELECT AQ.QUERY_ID, AQ.NAME, AQ.ACTIVE_STATUS"
						+ " FROM ART_QUERIES AQ, "
						+ " ART_ADMIN_PRIVILEGES APG, ART_ADMIN_PRIVILEGES APD "
						+ " WHERE AQ.QUERY_GROUP_ID = APG.VALUE_ID "
						+ " AND AQ.QUERY_GROUP_ID = ?"
						+ " AND APG.PRIVILEGE = 'GRP' "
						+ " AND APG.USERNAME = ? "
						+ " AND AQ.DATABASE_ID = APD.VALUE_ID "
						+ " AND APD.PRIVILEGE = 'DB' "
						+ " AND APD.USERNAME = ? "
						+ " ORDER BY AQ.NAME";
			} else {
				sql = "SELECT AQ.QUERY_ID, AQ.NAME, AQ.ACTIVE_STATUS"
						+ " FROM ART_QUERIES AQ "
						+ " WHERE AQ.QUERY_GROUP_ID = ? "
						+ " ORDER BY AQ.NAME";
			}

			Connection conn = null;
			PreparedStatement ps = null;
			try {
				conn = ArtDBCP.getConnection();
				ps = conn.prepareStatement(sql);
				ps.setInt(1, groupId);

				if (accessLevel <= 30) {
					ps.setString(2, username);
					ps.setString(3, username);
				}

				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					// build html option list
					builder.addItem(parseXml(rs.getString(2)) + " (Id:" + rs.getInt(1) + " Status:" + rs.getString(3) + ")", String.valueOf(rs.getInt(1)));
				}
				rs.close();
			} catch (Exception e) {
				logger.error("Error", e);
			} finally {
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (Exception e) {
					logger.error("Error", e);
				}
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (Exception e) {
					logger.error("Error", e);
				}
			}
		}

		return builder.toString();
	}

	/**
	 * Get a html fragment with object summary information.
	 *
	 * @param request http request object
	 * @param response http response object
	 * @param conn connection to art repository
	 * @return a html fragment with object summary information
	 * @throws IOException
	 * @throws SQLException
	 */
	private String getHtmlQueryDescription(HttpServletRequest request, HttpServletResponse response) {

		StringBuilder builder = new StringBuilder(200);

		response.setContentType("text/html;charset=utf-8");

		int queryId = Integer.parseInt(request.getParameter("queryId"));

		Connection conn = null;
		PreparedStatement ps = null;

		try {
			ResourceBundle messages = ResourceBundle.getBundle("art.i18n.ArtMessages", request.getLocale());

			//use left outer join as dashboards, text queries etc don't have a datasource
			String sql = " SELECT aq.QUERY_ID , aq.NAME, aq.SHORT_DESCRIPTION, "
					+ " aq.DESCRIPTION, aq.QUERY_TYPE, aq.UPDATE_DATE, "
					+ " ad.NAME AS DATABASE_NAME "
					+ " FROM ART_QUERIES aq left outer join ART_DATABASES ad"
					+ " on aq.DATABASE_ID=ad.DATABASE_ID "
					+ " WHERE aq.QUERY_ID = ?";

			conn = ArtDBCP.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setInt(1, queryId);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				String type;
				int typeId = rs.getInt("QUERY_TYPE");
				switch (typeId) {
					case 0:
						type = messages.getString("tabularQuery");
						break;
					case 100:
						type = messages.getString("updateQuery");
						break;
					case 101:
						type = messages.getString("crosstabQuery");
						break;
					case 102:
						type = messages.getString("crosstabwebQuery");
						break;
					case 103:
						type = messages.getString("tabularwebQuery");
						break;
					case 110:
						type = messages.getString("dashboardQuery");
						break;
					case 111:
						type = messages.getString("textQuery");
						break;
					case 112:
					case 113:
					case 114:
						type = messages.getString("pivotTableQuery");
						break;
					case 115:
					case 116:
						type = messages.getString("jasperReportQuery");
						break;
					case 117:
					case 118:
						type = messages.getString("jxlsQuery");
						break;
					default:
						if (typeId > 0 && typeId < 99) {
							type = messages.getString("reportwebQuery");
						} else if (typeId < 0) {
							type = messages.getString("graphQuery");
						} else {
							type = "Unknown...";
						}
				}

				builder.append("<fieldset>");
				builder.append("<legend>").append(messages.getString("itemDescription")).append("</legend>");
				builder.append(messages.getString("objectId")).append(" <b>").append(rs.getInt("QUERY_ID")).append("</b> <br>");
				builder.append(messages.getString("objectName")).append(" <b>").append(rs.getString("NAME")).append("</b> ");
				String shortDescription = rs.getString("SHORT_DESCRIPTION");
				shortDescription = StringUtils.trim(shortDescription);
				if (StringUtils.length(shortDescription) > 0) {
					builder.append(":: <b>").append(shortDescription).append("</b>");
				}
				builder.append("<br>");
				builder.append(messages.getString("itemDescription")).append("<br>&nbsp;&nbsp; <b>").append(rs.getString("DESCRIPTION")).append("</b><br>");
				builder.append(messages.getString("objectType")).append(" <b>").append(type).append("</b><br>");
				builder.append(messages.getString("updateDate")).append(" <b>").append(rs.getString("UPDATE_DATE")).append("</b><br>");

				// dashboards, text querys, mondrian via xmla and ssas via xmla don't have a datasource
				if (typeId != 111 && typeId != 110 && typeId != 113 && typeId != 114) {
					builder.append(messages.getString("targetDatasource")).append(" <b>").append(rs.getString("DATABASE_NAME")).append("</b><br>");
				}

				//remove show params checkbox. let parameters always be shown
			/*
				 if (typeId == 110) { // add show params checkbox for dashboards
				 builder.append("<input type=\"checkbox\" id=\"editPortletsParameters\" name=\"editPortletsParameters\" value=\"true\" checked>" + messages.getString("editPortletsParameters"));
				 }
				 */

				builder.append("<input type=\"hidden\" name=\"typeId\" value=\"").append(typeId).append("\">");
				builder.append("</fieldset>");
			}
			rs.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}

		return builder.toString();
	}

	/**
	 * Get details for a saved job schedule. To use in the editJob.jsp page.
	 *
	 * @param request http request object
	 * @param response http response object
	 * @param conn connection to art repository
	 * @return details for a saved job schedule
	 * @throws IOException
	 * @throws SQLException
	 */
	private String getXmlSchedule(HttpServletRequest request, HttpServletResponse response) {

		AjaxXmlBuilder builder = new AjaxXmlBuilder();

		response.setContentType("text/xml;charset=utf-8");

		String scheduleName = request.getParameter("scheduleName");
		if (scheduleName != null) {

			Connection conn = null;
			PreparedStatement ps = null;
			try {
				String sql = "SELECT AJS.JOB_MINUTE, AJS.JOB_HOUR, AJS.JOB_DAY, AJS.JOB_WEEKDAY, AJS.JOB_MONTH "
						+ " FROM ART_JOB_SCHEDULES AJS "
						+ " WHERE AJS.SCHEDULE_NAME = ?";

				conn = ArtDBCP.getConnection();
				ps = conn.prepareStatement(sql);
				ps.setString(1, scheduleName);

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					String minute;
					String hour;
					String day;
					String month;
					String weekday;

					minute = rs.getString("JOB_MINUTE");
					builder.addItem("minute", minute);

					hour = rs.getString("JOB_HOUR");
					builder.addItem("hour", hour);

					day = rs.getString("JOB_DAY");
					builder.addItem("day", day);

					month = rs.getString("JOB_MONTH");
					builder.addItem("month", month);

					weekday = rs.getString("JOB_WEEKDAY");
					builder.addItem("weekday", weekday);
				}
				rs.close();
			} catch (Exception e) {
				logger.error("Error", e);
			} finally {
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (Exception e) {
					logger.error("Error", e);
				}
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (Exception e) {
					logger.error("Error", e);
				}
			}
		}

		return builder.toString();
	}

	//replace characters that have a special meaning in xml e.g. &, <, >
	/**
	 * Replace characters that have a special meaning in xml e.g. &, &lt;, &gt;
	 *
	 * @param s string to parse
	 * @return string with special xml characters removed
	 */
	public static String parseXml(String s) {
		String parsedString = null;

		if (s != null) {
			parsedString = StringEscapeUtils.escapeXml(s);
		}

		return parsedString;
	}
}
