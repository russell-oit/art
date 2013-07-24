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
package art.utils;

import art.servlets.ArtDBCP;
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
	 * getDefaulParams determines if the URL should contain the default
	 * parameters or not
	 *
	 * @param queryId
	 * @param encodeUrl
	 * @param getDefaultParams
	 * @return the URL to directly execute the object
	 * @throws ArtException
	 */
	public static String getExecuteUrl(int queryId, boolean encodeUrl, boolean getDefaultParams) throws ArtException {
		String url = "/user/ExecuteQuery?queryId=" + queryId;

		if (getDefaultParams) {
			//add parameters to url
			url = url + lookupParams(queryId, encodeUrl);
		}

		return url;
	}

	/**
	 * Build the portion of the URL with default parameter values If encodeUrl
	 * is true, the parameters values are encoded (used when printing the link)
	 */
	private static String lookupParams(int queryId, boolean encodeUrl) {
		StringBuilder sb = new StringBuilder(128);

		Connection conn = ArtDBCP.getConnection();
		PreparedStatement ps = null;

		try {

			String sqlQuery = "SELECT PARAM_LABEL, DEFAULT_VALUE, PARAM_TYPE "
					+ " FROM ART_QUERY_FIELDS "
					+ " WHERE QUERY_ID = ? ORDER BY FIELD_POSITION";

			ps = conn.prepareStatement(sqlQuery);
			ps.setInt(1, queryId);
			ResultSet rs = ps.executeQuery();

			String paramType, paramLabel, paramValue;
			while (rs.next()) {
				paramLabel = rs.getString("PARAM_LABEL");
				paramValue = rs.getString("DEFAULT_VALUE");
				if (encodeUrl && paramValue != null) {
					try {
						paramValue = URLEncoder.encode(paramValue, "UTF-8");
					} catch (Exception e) {
						logger.warn("Error while encoding. Parameter={}, Value={}", new Object[]{paramLabel, paramValue, e});
					}
				}

				paramType = rs.getString("PARAM_TYPE");
				if (StringUtils.equals(paramType, "I")) { // inline
					sb.append("&P_").append(paramLabel).append("=").append(paramValue);
				}
			}
			rs.close();
			ps.close();
		} catch (Exception e) {
			logger.error("Error", e);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("Error", e);
			}
		}

		return sb.toString();
	}
}
