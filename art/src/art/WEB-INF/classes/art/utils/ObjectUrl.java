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
package art.utils;

import art.servlets.ArtDBCP;

import java.sql.*;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to construct the default URL to execute an ART object
 *
 * @author Enrico Liboni
 */
public class ObjectUrl {

	final static Logger logger = LoggerFactory.getLogger(ObjectUrl.class);

	/**
	 *
	 */
	public ObjectUrl() {
	}

	/**
	 * Returns the URL to directly execute the object.
	 *
	 * If objects have parameters, the default values are used
	 *
	 * @param objectId
	 * @param encodeUrl
	 * @return the URL to directly execute the object
	 * @throws ArtException
	 */
	public static String getExecuteUrl(int objectId, boolean encodeUrl) throws ArtException {
		return getExecuteUrl(objectId, encodeUrl, true);
	}

	/**
	 * Returns the URL to directly execute the object.
	 *
	 * getDefaulParams stares if the URL should contain the default parameters
	 * or not
	 *
	 * @param objectId
	 * @param encodeUrl
	 * @param getDefaultParams
	 * @return the URL to directly execute the object
	 * @throws ArtException
	 */
	public static String getExecuteUrl(int objectId, boolean encodeUrl, boolean getDefaultParams) throws ArtException {
		Connection conn = null;
		String url = null;

		try {
			conn = ArtDBCP.getConnection();

			ArtQuery aq = new ArtQuery();
			if (aq.create(conn, objectId)) {
				int typeId = aq.getQueryType();

				url = lookupExecuteUrl(typeId, objectId);
				if (getDefaultParams) {
					if (!(typeId == 110 || typeId == 111)) {
						//add parameters to url
						url = url + lookupParams(objectId, conn, encodeUrl);
					}
				}
			} else {
				throw new ArtException("Not able to load header for object id: " + objectId);
			}

		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("Exception while initializing object url id: " + objectId + " Exception: " + e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
				throw new ArtException("Not able to close connection properly for object url id: " + objectId);
			}
		}

		return url;
	}

	/**
	 * Returns the URL to the parameters page of a query.
	 *
	 * If objects have parameters, the default values are used
	 *
	 * @param objectId
	 * @return the URL to the parameters page of a query
	 */
	public static String getParamsUrl(int objectId) {
		return "/user/showParams.jsp?queryId=" + objectId;
	}

	/**
	 * Build the portion of the URL with default parameter values If encodeUrl
	 * is true, the parameters values are encoded (used when printing the link)
	 */
	private static String lookupParams(int objectId, Connection conn, boolean encodeUrl) throws SQLException {
		StringBuilder sb = new StringBuilder(254);

		String sqlQuery = "SELECT PARAM_LABEL, DEFAULT_VALUE, PARAM_TYPE, "
				+ " CHAINED_PARAM_POSITION, PARAM_DATA_TYPE "
				+ " FROM ART_QUERY_FIELDS "
				+ " WHERE QUERY_ID = ? ORDER BY FIELD_POSITION";

		PreparedStatement ps = conn.prepareStatement(sqlQuery);
		ps.setInt(1, objectId);
		ResultSet rs = ps.executeQuery();
		String paramType, paramName, paramValue;
		while (rs.next()) {
			paramName = rs.getString("PARAM_LABEL");
			paramValue = rs.getString("DEFAULT_VALUE");
			if (encodeUrl && paramValue != null) {
				try {
					paramValue = URLEncoder.encode(paramValue, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					logger.warn("UTF-8 encoding not supported", e);
				}
			}

			paramType = rs.getString("PARAM_TYPE");
			if (StringUtils.equals(paramType, "I")) { // inline
				sb.append("&P_" + paramName + "=" + paramValue);
			}
		}
		rs.close();
		ps.close();

		return sb.toString();
	}

	/**
	 * Build the URL used to execute the query/object (without any parameter
	 * value) // change 1.8 to /user/<servlet> instead of /<servlet>
	 */
	static private String lookupExecuteUrl(int typeId, int objectId) {
		String type = "";
		String url;

		switch (typeId) {
			case 0:
			case 100:
			case 103:
				url = "/user/ExecuteQuery?queryId=" + objectId;
				break;
			case 101:
			case 102:
				type = "&viewMode=html&_isCrosstab=Y";
				url = "/user/ExecuteQuery?queryId=" + objectId;
				break;
			case 110:
				url = "/user/showPortlets.jsp?queryId=" + objectId;
				break;
			case 111:
				url = "/user/showText.jsp?queryId=" + objectId;
				break;
			case 112:
			case 113:
			case 114:
				url = "/user/showAnalysis.jsp?queryId=" + objectId;
				break;
			case 115:
			case 116:
				url = "/user/ExecuteQuery?queryId=" + objectId;
				break;
			case 117:
			case 118:
				url = "/user/ExecuteQuery?queryId=" + objectId;
				break;
			default:
				//graph or report on column			
				url = "/user/ExecuteQuery?queryId=" + objectId;
		}

		return url + type;
	}
}
