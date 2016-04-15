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
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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
			ReportParameter reportParam = new ReportParameter();

			reportParam.setReportParameterId(rs.getInt("REPORT_PARAMETER_ID"));
			reportParam.setPosition(rs.getInt("PARAMETER_POSITION"));
			reportParam.setChainedParents(rs.getString("CHAINED_PARENTS"));
			reportParam.setChainedDepends(rs.getString("CHAINED_DEPENDS"));

			reportParam.setReport(reportService.getReport(rs.getInt("REPORT_ID")));
			reportParam.setParameter(parameterService.getParameter(rs.getInt("PARAMETER_ID")));

			return type.cast(reportParam);
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

	/**
	 * Get a report parameter
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public ReportParameter getReportParameter(int id) throws SQLException {
		logger.debug("Entering getReportParameter: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE REPORT_PARAMETER_ID=?";
		ResultSetHandler<ReportParameter> h = new BeanHandler<>(ReportParameter.class, new ReportParameterMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Delete a report parameter
	 *
	 * @param id
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public void deleteReportParameter(int id) throws SQLException {
		logger.debug("Entering deleteReportParameter: id={}", id);

		String sql;

		sql = "DELETE FROM ART_REPORT_PARAMETERS WHERE REPORT_PARAMETER_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Delete a report parameter
	 *
	 * @param ids
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public void deleteReportParameters(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteReportParameters: ids={}", (Object) ids);

		String sql;

		sql = "DELETE FROM ART_REPORT_PARAMETERS"
				+ " WHERE REPORT_PARAMETER_ID IN(" + StringUtils.repeat("?", ",", ids.length) + ")";
		dbService.update(sql, (Object[]) ids);
	}

	/**
	 * Add a new report parameter to the database
	 *
	 * @param param
	 * @param actionUser
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public synchronized int addReportParameter(ReportParameter param, int reportId) throws SQLException {
		logger.debug("Entering addReportParameter: param={}, reportId={}", param, reportId);

		//generate new id
		String sql = "SELECT MAX(REPORT_PARAMETER_ID) FROM ART_REPORT_PARAMETERS";
		ResultSetHandler<Integer> h = new ScalarHandler<>();
		Integer maxId = dbService.query(sql, h);
		logger.debug("maxId={}", maxId);

		int newId;
		if (maxId == null || maxId < 0) {
			//no records in the table, or only hardcoded records
			newId = 1;
		} else {
			newId = maxId + 1;
		}
		logger.debug("newId={}", newId);

		//generate new position
		sql = "SELECT MAX(PARAMETER_POSITION)"
				+ " FROM ART_REPORT_PARAMETERS"
				+ " WHERE REPORT_ID=?";
		ResultSetHandler<Integer> h2 = new ScalarHandler<>();
		Integer maxPosition = dbService.query(sql, h2, reportId);
		logger.debug("maxPosition={}", maxPosition);

		int newPosition;
		if (maxPosition == null || maxPosition < 0) {
			//no records in the table, or only hardcoded records
			newPosition = 1;
		} else {
			newPosition = maxPosition + 1;
		}
		logger.debug("newPosition={}", newPosition);

		param.setReportParameterId(newId);
		param.setPosition(newPosition);
		param.getReport().setReportId(reportId);

		saveReportParameter(param, true);

		return newId;
	}

	/**
	 * Update an existing report parameter
	 *
	 * @param param
	 * @param actionUser
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public void updateReportParameter(ReportParameter param) throws SQLException {
		logger.debug("Entering updateReportParameter: param={}", param);

		saveReportParameter(param, false);
	}

	/**
	 * Save a report parameter
	 *
	 * @param reportParam
	 * @param newRecord
	 * @param actionUser
	 * @throws SQLException
	 */
	private void saveReportParameter(ReportParameter reportParam, boolean newRecord) throws SQLException {
		logger.debug("Entering saveReportParameter: reportParam={}, newRecord={}",
				reportParam, newRecord);

		int affectedRows;
		if (newRecord) {
			String sql = "INSERT INTO ART_REPORT_PARAMETERS"
					+ " (REPORT_PARAMETER_ID, REPORT_ID, PARAMETER_ID,"
					+ " PARAMETER_POSITION, CHAINED_PARENTS, CHAINED_DEPENDS)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 6) + ")";

			Object[] values = {
				reportParam.getReportParameterId(),
				reportParam.getReport().getReportId(),
				reportParam.getParameter().getParameterId(),
				reportParam.getPosition(),
				reportParam.getChainedParents(),
				reportParam.getChainedDepends()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_REPORT_PARAMETERS SET PARAMETER_ID=?,"
					+ " PARAMETER_POSITION=?, CHAINED_PARENTS=?, CHAINED_DEPENDS=?"
					+ " WHERE REPORT_PARAMETER_ID=?";

			Object[] values = {
				reportParam.getParameter().getParameterId(),
				reportParam.getPosition(),
				reportParam.getChainedParents(),
				reportParam.getChainedDepends(),
				reportParam.getReportParameterId()
			};

			affectedRows = dbService.update(sql, values);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, reportParam={}",
					affectedRows, newRecord, reportParam);
		}
	}

	/**
	 * Move a drilldown to a different position
	 *
	 * @param id
	 * @param fromPosition
	 * @param toPosition
	 * @param direction
	 * @param parentReportId
	 * @throws SQLException
	 */
	public void moveReportParameter(int id, int fromPosition, int toPosition, String direction,
			int parentReportId) throws SQLException {

		logger.debug("Entering moveReportParameter: id={}, fromPosition={},"
				+ " toPosition={}, direction='{}', parentReportId={}",
				id, fromPosition, toPosition, direction, parentReportId);

		String sql;

		//https://datatables.net/forums/discussion/comment/55311#Comment_55311
		if (StringUtils.equals(direction, "back")) {
			//toPosition < fromPosition
			int finalPosition = toPosition + 1;

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=0"
					+ " WHERE PARAMETER_POSITION=?"
					+ " AND REPORT_ID=?";
			dbService.update(sql, toPosition, parentReportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=?"
					+ " WHERE REPORT_PARAMETER_ID=?"
					+ " AND REPORT_ID=?";
			dbService.update(sql, toPosition, id, parentReportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=PARAMETER_POSITION + 1"
					+ " WHERE (?<=PARAMETER_POSITION"
					+ " AND PARAMETER_POSITION<=?)"
					+ " AND REPORT_PARAMETER_ID<>? AND PARAMETER_POSITION<>0"
					+ " AND REPORT_ID=?";
			dbService.update(sql, toPosition, fromPosition, id, parentReportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=?"
					+ " WHERE PARAMETER_POSITION=0"
					+ " AND REPORT_ID=?";
			dbService.update(sql, finalPosition, parentReportId);
		} else {
			//"forward". toPosition > fromPosition
			int finalPosition = toPosition - 1;

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=0"
					+ " WHERE PARAMETER_POSITION=?"
					+ " AND REPORT_ID=?";
			dbService.update(sql, toPosition, parentReportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=?"
					+ " WHERE REPORT_PARAMETER_ID=?"
					+ " AND REPORT_ID=?";
			dbService.update(sql, toPosition, id, parentReportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=PARAMETER_POSITION - 1"
					+ " WHERE (?<=PARAMETER_POSITION"
					+ " AND PARAMETER_POSITION<=?)"
					+ " AND REPORT_PARAMETER_ID<>? AND PARAMETER_POSITION<>0"
					+ " AND REPORT_ID=?";
			dbService.update(sql, fromPosition, toPosition, id, parentReportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=?"
					+ " WHERE PARAMETER_POSITION=0"
					+ " AND REPORT_ID=?";
			dbService.update(sql, finalPosition, parentReportId);
		}

		//http://www.codeproject.com/Articles/331986/Table-Row-Drag-and-Drop-in-ASP-NET-MVC-JQuery-Data
		//logic doesn't work here because position is part of primary key and updates fail because of duplicate primary keys
//		//move other drilldowns
//		if (StringUtils.equals(direction, "back")) {
//			//toPosition < fromPosition
//			sql = "UPDATE ART_REPORT_PARAMETERS"
//					+ " SET PARAMETER_POSITION=PARAMETER_POSITION + 1"
//					+ " WHERE ?<=PARAMETER_POSITION"
//					+ " AND PARAMETER_POSITION<=?"
//					+ " AND REPORT_ID=?";
//			dbService.update(sql, toPosition, fromPosition, parentReportId);
//		} else {
//			//"forward". toPosition > fromPosition
//			sql = "UPDATE ART_REPORT_PARAMETERS"
//					+ " SET PARAMETER_POSITION=PARAMETER_POSITION + 1"
//					+ " WHERE ?<=PARAMETER_POSITION"
//					+ " AND PARAMETER_POSITION<=?"
//					+ " AND REPORT_ID=?";
//			dbService.update(sql, fromPosition, toPosition, parentReportId);
//		}
//
//		//move this drilldown
//		sql = "UPDATE ART_REPORT_PARAMETERS"
//				+ " SET PARAMETER_POSITION=?"
//				+ " WHERE REPORT_PARAMETER_ID=?";
//		dbService.update(sql, toPosition);
	}

}
