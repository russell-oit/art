/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.savedparameter;

import art.dbutils.DbService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting saved
 * parameters
 *
 * @author Timothy Anyona
 */
@Service
public class SavedParameterService {

	private static final Logger logger = LoggerFactory.getLogger(SavedParameterService.class);

	private final DbService dbService;

	@Autowired
	public SavedParameterService(DbService dbService) {
		this.dbService = dbService;
	}

	public SavedParameterService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_SAVED_PARAMETERS";

	/**
	 * Maps a resultset to an object
	 */
	private class SavedParameterMapper extends BasicRowProcessor {

		@Override
		public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
			List<T> list = new ArrayList<>();
			while (rs.next()) {
				list.add(toBean(rs, type));
			}
			return list;
		}

		@Override
		public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
			SavedParameter savedParam = new SavedParameter();

			savedParam.setUserId(rs.getInt("USER_ID"));
			savedParam.setReportId(rs.getInt("REPORT_ID"));
			savedParam.setName(rs.getString("PARAM_NAME"));
			savedParam.setValue(rs.getString("PARAM_VALUE"));

			return type.cast(savedParam);
		}
	}

	/**
	 * Returns saved parameters for the given user and report
	 *
	 * @param userId the user id
	 * @param reportId the report id
	 * @return saved parameters for the given user and report
	 * @throws SQLException
	 */
	public List<SavedParameter> getSavedParameters(int userId, int reportId) throws SQLException {
		logger.debug("Entering getSavedParameters: userId={}, reportId={}", userId, reportId);

		String sql = SQL_SELECT_ALL + " WHERE USER_ID=? AND REPORT_ID=?";
		ResultSetHandler<List<SavedParameter>> h = new BeanListHandler<>(SavedParameter.class, new SavedParameterMapper());
		return dbService.query(sql, h, userId, reportId);
	}

	/**
	 * Deletes saved parameters for the given user and report
	 *
	 * @param userId the user id
	 * @param reportId the report id
	 * @throws SQLException
	 */
	public void deleteSavedParameters(int userId, int reportId) throws SQLException {
		logger.debug("Entering deleteSavedParameters: userId={}, reportId={}", userId, reportId);

		String sql = "DELETE FROM ART_SAVED_PARAMETERS WHERE USER_ID=? AND REPORT_ID=?";

		dbService.update(sql, userId, reportId);
	}

	/**
	 * Deletes a saved parameter
	 *
	 * @param userId the user id
	 * @param reportId the report id
	 * @param paramName the parameter name
	 * @param paramValue the parameter value
	 * @throws SQLException
	 */
	public void deleteSavedParameter(int userId, int reportId, String paramName,
			String paramValue) throws SQLException {

		logger.debug("Entering deleteSavedParameters: userId={}, reportId={},"
				+ " paramName='{}', paramValue='{}'", userId, reportId, paramName, paramValue);

		String sql = "DELETE FROM ART_SAVED_PARAMETERS WHERE USER_ID=? AND REPORT_ID=?"
				+ " AND PARAM_NAME=? AND PARAM_VALUE=?";

		dbService.update(sql, userId, reportId, paramName, paramValue);
	}

	/**
	 * Adds a saved parameter
	 *
	 * @param savedParam the saved parameter to add
	 * @throws SQLException
	 */
	public void addSavedParameter(SavedParameter savedParam) throws SQLException {
		logger.debug("Entering addSavedParameter");

		String sql = "INSERT INTO ART_SAVED_PARAMETERS"
				+ " (USER_ID, REPORT_ID, PARAM_NAME, PARAM_VALUE)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

		Object[] values = {
			savedParam.getUserId(),
			savedParam.getReportId(),
			savedParam.getName(),
			savedParam.getValue()
		};

		dbService.update(sql, values);
	}

	/**
	 * Returns saved parameter values for a given user and report
	 *
	 * @param userId the user id
	 * @param reportId the report id
	 * @return saved parameter values for a given user and report
	 * @throws SQLException
	 */
	public Map<String, String[]> getSavedParameterValues(int userId, int reportId) throws SQLException {
		logger.debug("Entering getSavedParameterValues: userId={}, reportId", userId, reportId);

		List<SavedParameter> savedParams = getSavedParameters(userId, reportId);
		Map<String, List<String>> paramValues = new HashMap<>();

		for (SavedParameter savedParam : savedParams) {
			String name = savedParam.getName();
			String value = savedParam.getValue();
			List<String> values = paramValues.get(name);
			if (values == null) {
				values = new ArrayList<>();
				paramValues.put(name, values);
			}
			values.add(value);
		}

		Map<String, String[]> finalValues = new HashMap<>();

		for (Entry<String, List<String>> entry : paramValues.entrySet()) {
			String name = entry.getKey();
			List<String> values = entry.getValue();
			String[] valuesArray = values.toArray(new String[0]);
			finalValues.put(name, valuesArray);
		}

		return finalValues;
	}

}
