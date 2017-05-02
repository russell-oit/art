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
package art.reportparameter;

import art.dbutils.DbService;
import art.parameter.ParameterService;
import art.report.Report;
import art.report.ReportService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
 * Provide methods for retrieving, addding, updating and deleting report
 * parameters
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
	 * Maps a resultset to an object
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
	 * Returns the report parameter that is in a given position
	 *
	 * @param reportId the report id
	 * @param position the parameter position
	 * @return report parameter if found, null otherwise
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
	 * Returns all report parameters for a given report
	 *
	 * @param reportId the report id
	 * @return all report parameters for the given report
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
	 * Returns all report parameters for a dashboard report
	 *
	 * @param reportIds the report ids of the reports within the dashboard
	 * @return all report parameters for the dashboard report
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public List<ReportParameter> getDashboardReportParameters(List<Integer> reportIds) throws SQLException {
		logger.debug("Entering getDashboardReportParameters");

		if (reportIds == null || reportIds.isEmpty()) {
			return Collections.emptyList();
		}

		String sql = SQL_SELECT_ALL
				+ " WHERE REPORT_ID IN(" + StringUtils.repeat("?", ",", reportIds.size()) + ")";

		ResultSetHandler<List<ReportParameter>> h = new BeanListHandler<>(ReportParameter.class, new ReportParameterMapper());
		return dbService.query(sql, h, reportIds.toArray());
	}

	/**
	 * Returns all report parameters for a given report
	 *
	 * @param reportId the report id
	 * @return map of all report parameters for a given report. The key is the
	 * parameter name.
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
	 * Returns a report parameter
	 *
	 * @param id the report parameter id
	 * @return report parameter if found, null otherwise
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
	 * Deletes a report parameter
	 *
	 * @param id the report parameter id
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
	 * Deletes multiple report parameters
	 *
	 * @param ids the ids of the report parameters to delete
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
	 * Adds a new report parameter to the database
	 *
	 * @param reportParam the report parameter
	 * @param reportId the report id
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public synchronized int addReportParameter(ReportParameter reportParam, int reportId)
			throws SQLException {

		logger.debug("Entering addReportParameter: reportParam={}, reportId={}",
				reportParam, reportId);

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

		reportParam.setReportParameterId(newId);
		reportParam.setPosition(newPosition);
		
		Report report = new Report();
		report.setReportId(reportId);
		
		reportParam.setReport(report);
		
		saveReportParameter(reportParam, true);

		return newId;
	}

	/**
	 * Updates an existing report parameter
	 *
	 * @param param the updated report parameter
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public void updateReportParameter(ReportParameter param) throws SQLException {
		logger.debug("Entering updateReportParameter: param={}", param);

		saveReportParameter(param, false);
	}

	/**
	 * Saves a report parameter
	 *
	 * @param reportParam the report parameter
	 * @param newRecord whether this is a new record
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
	 * Moves a report parameter to a different position
	 *
	 * @param id the report parameter id
	 * @param fromPosition the position to move from
	 * @param toPosition the position to move to
	 * @param direction the direction. "forward" or "back"
	 * @param reportId the report id
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public void moveReportParameter(int id, int fromPosition, int toPosition, String direction,
			int reportId) throws SQLException {

		logger.debug("Entering moveReportParameter: id={}, fromPosition={},"
				+ " toPosition={}, direction='{}', parentReportId={}",
				id, fromPosition, toPosition, direction, reportId);

		String sql;

		//https://datatables.net/forums/discussion/comment/55311#Comment_55311
		if (StringUtils.equals(direction, "back")) {
			//toPosition < fromPosition
			int finalPosition = toPosition + 1;

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=0"
					+ " WHERE PARAMETER_POSITION=?"
					+ " AND REPORT_ID=?";
			dbService.update(sql, toPosition, reportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=?"
					+ " WHERE REPORT_PARAMETER_ID=?"
					+ " AND REPORT_ID=?";
			dbService.update(sql, toPosition, id, reportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=PARAMETER_POSITION + 1"
					+ " WHERE (?<=PARAMETER_POSITION"
					+ " AND PARAMETER_POSITION<=?)"
					+ " AND REPORT_PARAMETER_ID<>? AND PARAMETER_POSITION<>0"
					+ " AND REPORT_ID=?";
			dbService.update(sql, toPosition, fromPosition, id, reportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=?"
					+ " WHERE PARAMETER_POSITION=0"
					+ " AND REPORT_ID=?";
			dbService.update(sql, finalPosition, reportId);
		} else if (StringUtils.equals(direction, "forward")) {
			//toPosition > fromPosition
			int finalPosition = toPosition - 1;

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=0"
					+ " WHERE PARAMETER_POSITION=?"
					+ " AND REPORT_ID=?";
			dbService.update(sql, toPosition, reportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=?"
					+ " WHERE REPORT_PARAMETER_ID=?"
					+ " AND REPORT_ID=?";
			dbService.update(sql, toPosition, id, reportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=PARAMETER_POSITION - 1"
					+ " WHERE (?<=PARAMETER_POSITION"
					+ " AND PARAMETER_POSITION<=?)"
					+ " AND REPORT_PARAMETER_ID<>? AND PARAMETER_POSITION<>0"
					+ " AND REPORT_ID=?";
			dbService.update(sql, fromPosition, toPosition, id, reportId);

			sql = "UPDATE ART_REPORT_PARAMETERS"
					+ " SET PARAMETER_POSITION=?"
					+ " WHERE PARAMETER_POSITION=0"
					+ " AND REPORT_ID=?";
			dbService.update(sql, finalPosition, reportId);
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
