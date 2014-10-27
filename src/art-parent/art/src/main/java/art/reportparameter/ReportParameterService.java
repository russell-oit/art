/*
 * Copyright (C) 2014 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.reportparameter;

import art.dbutils.DbService;
import art.parameter.ParameterService;
import art.report.ReportService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to report parameters
 *
 * @author Timothy Anyona
 */
@Service
public class ReportParameterService {

	private static final Logger logger = LoggerFactory.getLogger(ReportParameterService.class);

	private final DbService dbService;
	private final ReportService reportService;
	private final ParameterService parameterService;

	@Autowired
	public ReportParameterService(DbService dbService,
			ReportService reportService, ParameterService parameterService) {

		this.dbService = dbService;
		this.reportService = reportService;
		this.parameterService = parameterService;
	}

	public ReportParameterService() {
		dbService = new DbService();
		reportService = new ReportService();
		parameterService = new ParameterService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_REPORT_PARAMETERS ARP";

	/**
	 * Class to map resultset to an object
	 */
	private class ReportParameterMapper extends BasicRowProcessor {

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
			ReportParameter param = new ReportParameter();

			param.setReportParameterId(rs.getInt("REPORT_PARAMETER_ID"));
			param.setPosition(rs.getInt("PARAMETER_POSITION"));

			param.setReport(reportService.getReport(rs.getInt("REPORT_ID")));
			param.setParameter(parameterService.getParameter(rs.getInt("PARAMETER_ID")));

			return type.cast(param);
		}
	}

	/**
	 * Get the report parameter that is in a given position
	 *
	 * @param reportId
	 * @param position
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public ReportParameter getReportParameter(int reportId, int position) throws SQLException {
		logger.debug("Entering getReportParameter: reportId={}, position={}", reportId, position);

		String sql = SQL_SELECT_ALL
				+ " WHERE REPORT_ID=? AND PARAMETER_POSITION=?";

		ResultSetHandler<ReportParameter> h = new BeanHandler<>(ReportParameter.class, new ReportParameterMapper());
		return dbService.query(sql, h, reportId, position);
	}

	/**
	 * Get all report parameters for a given report
	 *
	 * @param reportId
	 * @return list of all report parameters for a given report, empty list
	 * otherwise
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public List<ReportParameter> getReportParameters(int reportId) throws SQLException {
		logger.debug("Entering getReportParameters: reportId={}", reportId);

		String sql = SQL_SELECT_ALL
				+ " WHERE REPORT_ID=?";

		ResultSetHandler<List<ReportParameter>> h = new BeanListHandler<>(ReportParameter.class, new ReportParameterMapper());
		return dbService.query(sql, h, reportId);
	}

	/**
	 * Get all report parameters for a given report
	 *
	 * @param reportId
	 * @return map of all report parameters for a given report. the key is the
	 * parameter name
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public Map<String, ReportParameter> getReportParametersMap(int reportId) throws SQLException {
		logger.debug("Entering getReportParametersMap: reportId={}", reportId);

		List<ReportParameter> reportParamsList = getReportParameters(reportId);

		//build map
		Map<String, ReportParameter> paramsMap = new HashMap<>();
		for (ReportParameter param : reportParamsList) {
			paramsMap.put(param.getParameter().getName(), param);
		}

		return paramsMap;
	}
//
//	/**
//	 * Get a report parameter
//	 *
//	 * @param id
//	 * @return populated object if found, null otherwise
//	 * @throws SQLException
//	 */
//	@Cacheable("parameters")
//	public ReportParameter getReportParameter(int id) throws SQLException {
//		logger.debug("Entering getReportParameter: id={}", id);
//
//		String sql = SQL_SELECT_ALL + " WHERE REPORT_PARAMETER_ID=?";
//		ResultSetHandler<ReportParameter> h = new BeanHandler<>(ReportParameter.class, new ReportParameterMapper());
//		return dbService.query(sql, h, id);
//	}
//
//	/**
//	 * Delete a report parameter
//	 *
//	 * @param id
//	 * @throws SQLException
//	 */
//	@CacheEvict(value = "parameters", allEntries = true)
//	public void deleteReportParameter(int id) throws SQLException {
//		logger.debug("Entering deleteReportParameter: id={}", id);
//
//		String sql;
//
//		//delete foreign key records
//		sql = "DELETE FROM ART_REPORT_PARAMETER_RULES WHERE REPORT_PARAMETER_ID=?";
//		dbService.update(sql, id);
//
//		sql = "DELETE FROM ART_USER_JOBS WHERE REPORT_PARAMETER_ID=?";
//		dbService.update(sql, id);
//
//		sql = "DELETE FROM ART_REPORT_PARAMETER_ASSIGNMENT WHERE REPORT_PARAMETER_ID=?";
//		dbService.update(sql, id);
//
//		sql = "DELETE FROM ART_REPORT_PARAMETER_QUERIES WHERE REPORT_PARAMETER_ID=?";
//		dbService.update(sql, id);
//
//		sql = "DELETE FROM ART_REPORT_PARAMETER_GROUPS WHERE REPORT_PARAMETER_ID=?";
//		dbService.update(sql, id);
//
//		sql = "DELETE FROM ART_REPORT_PARAMETER_JOBS WHERE REPORT_PARAMETER_ID=?";
//		dbService.update(sql, id);
//
//		//finally delete report parameter
//		sql = "DELETE FROM ART_REPORT_PARAMETERS WHERE REPORT_PARAMETER_ID=?";
//		dbService.update(sql, id);
//	}
//
//	/**
//	 * Add a new report parameter to the database
//	 *
//	 * @param param
//	 * @param actionUser
//	 * @return new record id
//	 * @throws SQLException
//	 */
//	@CacheEvict(value = "parameters", allEntries = true)
//	public synchronized int addReportParameter(ReportParameter param, User actionUser) throws SQLException {
//		logger.debug("Entering addReportParameter: param={}, actionUser={}", param, actionUser);
//
//		//generate new id
//		String sql = "SELECT MAX(REPORT_PARAMETER_ID) FROM ART_REPORT_PARAMETERS";
//		ResultSetHandler<Integer> h = new ScalarHandler<>();
//		Integer maxId = dbService.query(sql, h);
//		logger.debug("maxId={}", maxId);
//
//		int newId;
//		if (maxId == null || maxId < 0) {
//			//no records in the table, or only hardcoded records
//			newId = 1;
//		} else {
//			newId = maxId + 1;
//		}
//		logger.debug("newId={}", newId);
//
//		param.setReportParameterId(newId);
//
//		saveReportParameter(param, true, actionUser);
//
//		return newId;
//	}
//
//	/**
//	 * Update an existing report parameter
//	 *
//	 * @param param
//	 * @param actionUser
//	 * @throws SQLException
//	 */
//	@CacheEvict(value = "parameters", allEntries = true)
//	public void updateReportParameter(ReportParameter param, User actionUser) throws SQLException {
//		logger.debug("Entering updateReportParameter: param={}, actionUser={}", param, actionUser);
//
//		saveReportParameter(param, false, actionUser);
//	}
//
//	/**
//	 * Save a report parameter
//	 *
//	 * @param param
//	 * @param newRecord
//	 * @param actionUser
//	 * @throws SQLException
//	 */
//	private void saveReportParameter(ReportParameter param, boolean newRecord, User actionUser) throws SQLException {
//		logger.debug("Entering saveReportParameter: param={}, newRecord={}, actionUser={}",
//				param, newRecord, actionUser);
//
//		int affectedRows;
//		if (newRecord) {
//			String sql = "INSERT INTO ART_REPORT_PARAMETERS"
//					+ " (REPORT_PARAMETER_ID, NAME, DESCRIPTION, DEFAULT_QUERY_GROUP,"
//					+ " START_QUERY, CREATION_DATE, CREATED_BY)"
//					+ " VALUES(" + StringUtils.repeat("?", ",", 7) + ")";
//
//			Object[] values = {
//				param.getReportParameterId(),
//				param.getName(),
//				param.getDescription(),
//				param.getDefaultReportGroup(),
//				param.getStartReport(),
//				DbUtils.getCurrentTimeStamp(),
//				actionUser.getUsername()
//			};
//
//			affectedRows = dbService.update(sql, values);
//		} else {
//			String sql = "UPDATE ART_REPORT_PARAMETERS SET NAME=?, DESCRIPTION=?,"
//					+ " DEFAULT_QUERY_GROUP=?, START_QUERY=?, UPDATE_DATE=?, UPDATED_BY=?"
//					+ " WHERE REPORT_PARAMETER_ID=?";
//
//			Object[] values = {
//				param.getName(),
//				param.getDescription(),
//				param.getDefaultReportGroup(),
//				param.getStartReport(),
//				DbUtils.getCurrentTimeStamp(),
//				actionUser.getUsername(),
//				param.getReportParameterId()
//			};
//
//			affectedRows = dbService.update(sql, values);
//		}
//
//		logger.debug("affectedRows={}", affectedRows);
//
//		if (affectedRows != 1) {
//			logger.warn("Problem with save. affectedRows={}, newRecord={}, param={}",
//					affectedRows, newRecord, param);
//		}
//	}

}
