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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to construct the default URL to execute an ART query
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class QueryUrl {

	final static Logger logger = LoggerFactory.getLogger(QueryUrl.class);

	/**
	 *
	 */
	public QueryUrl() {
	}

	/**
	 * Returns the URL to directly execute the query.
	 *
	 * If queries have parameters, the default values are used
	 *
	 * @param queryId
	 * @param encodeUrl
	 * @return the URL to directly execute the query
	 * @throws ArtException
	 */
	public static String getExecuteUrl(int queryId, boolean encodeUrl) throws ArtException {
		return getExecuteUrl(queryId, encodeUrl, true);
	}

	/**
	 * Returns the URL to directly execute the query.
	 *
	 * getDefaulParams determines if the URL should contain the default parameters
	 * or not
	 *
	 * @param queryId
	 * @param encodeUrl
	 * @param getDefaultParams
	 * @return the URL to directly execute the object
	 * @throws ArtException
	 */
	public static String getExecuteUrl(int queryId, boolean encodeUrl, boolean getDefaultParams) throws ArtException {
		Connection conn = null;
		
		String url = "/user/ExecuteQuery?queryId=" + queryId;

		try {
			conn = ArtDBCP.getConnection();
			
			if (getDefaultParams) {
				//add parameters to url
				url = url + lookupParams(queryId, conn, encodeUrl);
			}

		} catch (Exception e) {
			logger.error("Error", e);
			throw new ArtException("Error while initializing query url. Query id: " + queryId + " Exception: " + e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
				throw new ArtException("Not able to close connection properly for object url. Query id: " + queryId);
			}
		}

		return url;
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
}
