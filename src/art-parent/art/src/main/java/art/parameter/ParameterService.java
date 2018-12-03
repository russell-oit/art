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
package art.parameter;

import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.report.Report;
import art.report.ReportService;
import art.report.ReportServiceHelper;
import art.user.User;
import art.general.ActionResult;
import art.utils.ArtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting parameters
 *
 * @author Timothy Anyona
 */
@Service
public class ParameterService {

	private static final Logger logger = LoggerFactory.getLogger(ParameterService.class);

	private final DbService dbService;
	private final ReportService reportService;

	@Autowired
	public ParameterService(DbService dbService, ReportService reportService) {
		this.dbService = dbService;
		this.reportService = reportService;
	}

	public ParameterService() {
		dbService = new DbService();
		reportService = new ReportService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_PARAMETERS AP";

	/**
	 * Maps a resultset to an object
	 */
	private class ParameterMapper extends BasicRowProcessor {

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
			Parameter parameter = new Parameter();

			parameter.setParameterId(rs.getInt("PARAMETER_ID"));
			parameter.setName(rs.getString("NAME"));
			parameter.setDescription(rs.getString("DESCRIPTION"));
			parameter.setParameterType(ParameterType.toEnum(rs.getString("PARAMETER_TYPE")));
			parameter.setLabel(rs.getString("PARAMETER_LABEL"));
			parameter.setHelpText(rs.getString("HELP_TEXT"));
			parameter.setDataType(ParameterDataType.toEnum(rs.getString("DATA_TYPE")));
			parameter.setDefaultValue(rs.getString("DEFAULT_VALUE"));
			parameter.setHidden(rs.getBoolean("HIDDEN"));
			parameter.setFixedValue(rs.getBoolean("FIXED_VALUE"));
			parameter.setShared(rs.getBoolean("SHARED"));
			parameter.setUseLov(rs.getBoolean("USE_LOV"));
			parameter.setUseRulesInLov(rs.getBoolean("USE_RULES_IN_LOV"));
			parameter.setDrilldownColumnIndex(rs.getInt("DRILLDOWN_COLUMN_INDEX"));
			parameter.setUseDirectSubstitution(rs.getBoolean("USE_DIRECT_SUBSTITUTION"));
			parameter.setOptions(rs.getString("PARAMETER_OPTIONS"));
			parameter.setDateFormat(rs.getString("PARAMETER_DATE_FORMAT"));
			parameter.setPlaceholderText(rs.getString("PLACEHOLDER_TEXT"));
			parameter.setUseDefaultValueInJobs(rs.getBoolean("USE_DEFAULT_VALUE_IN_JOBS"));
			parameter.setTemplate(rs.getString("TEMPLATE"));
			parameter.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			parameter.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			parameter.setCreatedBy(rs.getString("CREATED_BY"));
			parameter.setUpdatedBy(rs.getString("UPDATED_BY"));

			Report defaultValueReport = reportService.getReport(rs.getInt("DEFAULT_VALUE_REPORT_ID"));
			parameter.setDefaultValueReport(defaultValueReport);

			Report lovReport = reportService.getReport(rs.getInt("LOV_REPORT_ID"));
			parameter.setLovReport(lovReport);

			String options = parameter.getOptions();
			if (StringUtils.isBlank(options)) {
				ParameterOptions parameterOptions = new ParameterOptions();
				DateRangeOptions dateRangeOptions = new DateRangeOptions();
				parameterOptions.setDateRange(dateRangeOptions);
				parameter.setParameterOptions(parameterOptions);
			} else {
				ObjectMapper mapper = new ObjectMapper();
				try {
					ParameterOptions parameterOptions = mapper.readValue(options, ParameterOptions.class);
					DateRangeOptions dateRangeOptions = parameterOptions.getDateRange();
					if (dateRangeOptions == null) {
						dateRangeOptions = new DateRangeOptions();
						parameterOptions.setDateRange(dateRangeOptions);
					}
					parameter.setParameterOptions(parameterOptions);
				} catch (IOException ex) {
					logger.error("Error. {}", parameter, ex);
				}
			}

			return type.cast(parameter);
		}
	}

	/**
	 * Returns all parameters
	 *
	 * @return all parameters
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public List<Parameter> getAllParameters() throws SQLException {
		logger.debug("Entering getAllParameters");

		ResultSetHandler<List<Parameter>> h = new BeanListHandler<>(Parameter.class, new ParameterMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Returns parameters with given ids
	 *
	 * @param ids comma separated string of the parameter ids to retrieve
	 * @return parameters with given ids
	 * @throws SQLException
	 */
	public List<Parameter> getParameters(String ids) throws SQLException {
		logger.debug("Entering getParameters: ids='{}'", ids);

		Object[] idsArray = ArtUtils.idsToObjectArray(ids);

		String sql = SQL_SELECT_ALL
				+ " WHERE PARAMETER_ID IN(" + StringUtils.repeat("?", ",", idsArray.length) + ")";

		ResultSetHandler<List<Parameter>> h = new BeanListHandler<>(Parameter.class, new ParameterMapper());
		return dbService.query(sql, h, idsArray);
	}

	/**
	 * Returns shared parameters
	 *
	 * @return shared parameters
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public List<Parameter> getSharedParameters() throws SQLException {
		logger.debug("Entering getSharedParameters");

		String sql = SQL_SELECT_ALL + " WHERE SHARED=1";
		ResultSetHandler<List<Parameter>> h = new BeanListHandler<>(Parameter.class, new ParameterMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Returns fixed value parameters
	 *
	 * @return fixed value parameters
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public List<Parameter> getFixedValueParameters() throws SQLException {
		logger.debug("Entering getFixedValueParameters");

		String sql = SQL_SELECT_ALL + " WHERE FIXED_VALUE=1";
		ResultSetHandler<List<Parameter>> h = new BeanListHandler<>(Parameter.class, new ParameterMapper());
		return dbService.query(sql, h);
	}

	/**
	 * Returns a parameter
	 *
	 * @param id the parameter's id
	 * @return parameter if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public Parameter getParameter(int id) throws SQLException {
		logger.debug("Entering getParameter: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE PARAMETER_ID=?";
		ResultSetHandler<Parameter> h = new BeanHandler<>(Parameter.class, new ParameterMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns a parameter's name
	 *
	 * @param id the parameter's id
	 * @return the parameter's name
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public String getParameterName(int id) throws SQLException {
		logger.debug("Entering getParameterName: id={}", id);

		String sql = "SELECT NAME FROM ART_PARAMETERS WHERE PARAMETER_ID=?";
		ResultSetHandler<String> h = new ScalarHandler<>(1);
		return dbService.query(sql, h, id);
	}

	/**
	 * Returns parameters for a report
	 *
	 * @param reportId the report id
	 * @return parameters for the report
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public List<Parameter> getReportParameters(int reportId) throws SQLException {
		logger.debug("Entering getReportParameters: reportId={}", reportId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_REPORT_PARAMETERS ARP"
				+ " ON AP.PARAMETER_ID=ARP.PARAMETER_ID"
				+ " WHERE ARP.REPORT_ID=?";
		ResultSetHandler<List<Parameter>> h = new BeanListHandler<>(Parameter.class, new ParameterMapper());
		return dbService.query(sql, h, reportId);
	}

	/**
	 * Returns drilldown parameters for a report
	 *
	 * @param reportId the report id
	 * @return drilldown parameters for the report
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public List<Parameter> getDrilldownParameters(int reportId) throws SQLException {
		logger.debug("Entering getDrilldownParameters: reportId={}", reportId);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_REPORT_PARAMETERS ARP"
				+ " ON AP.PARAMETER_ID=ARP.PARAMETER_ID"
				+ " WHERE ARP.REPORT_ID=?"
				+ " AND AP.DRILLDOWN_COLUMN_INDEX > 0"
				+ " AND AP.PARAMETER_TYPE=?";
		ResultSetHandler<List<Parameter>> h = new BeanListHandler<>(Parameter.class, new ParameterMapper());
		return dbService.query(sql, h, reportId, ParameterType.SingleValue.getValue());
	}

	/**
	 * Deletes a parameter
	 *
	 * @param id the parameter id
	 * @return ActionResult. if not successful, data contains a list of linked
	 * reports which prevented the parameter from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public ActionResult deleteParameter(int id) throws SQLException {
		logger.debug("Entering deleteParameter: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedReports = getLinkedReports(id);
		if (!linkedReports.isEmpty()) {
			result.setData(linkedReports);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_USER_PARAM_DEFAULTS WHERE PARAMETER_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_PARAM_DEFAULTS WHERE PARAMETER_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_FIXED_PARAM_VAL WHERE PARAMETER_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_FIXED_PARAM_VAL WHERE PARAMETER_ID=?";
		dbService.update(sql, id);

		//finally delete parameter
		sql = "DELETE FROM ART_PARAMETERS WHERE PARAMETER_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);
		return result;
	}

	/**
	 * Deletes multiple parameters
	 *
	 * @param ids the ids of the parameters to delete
	 * @return ActionResult. if not successful, data contains the ids of the
	 * parameters which were not deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public ActionResult deleteParameters(Integer[] ids) throws SQLException {
		logger.debug("Entering deleteParameters: ids={}", (Object) ids);

		ActionResult result = new ActionResult();
		List<String> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteParameter(id);
			if (!deleteResult.isSuccess()) {
				@SuppressWarnings("unchecked")
				List<String> linkedReports = (List<String>) deleteResult.getData();
				String value = String.valueOf(id) + " - " + StringUtils.join(linkedReports, ", ");
				nonDeletedRecords.add(value);
			}
		}

		if (nonDeletedRecords.isEmpty()) {
			result.setSuccess(true);
		} else {
			result.setData(nonDeletedRecords);
		}
		return result;

	}

	/**
	 * Adds a new parameter to the database
	 *
	 * @param parameter the parameter to add
	 * @param actionUser the use who is performing the action
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public synchronized int addParameter(Parameter parameter, User actionUser) throws SQLException {
		logger.debug("Entering addParameter: parameter={}, actionUser={}", parameter, actionUser);

		//generate new id
		String sql = "SELECT MAX(PARAMETER_ID) FROM ART_PARAMETERS";
		int newId = dbService.getNewRecordId(sql);

		saveParameter(parameter, newId, actionUser);

		return newId;
	}

	/**
	 * Updates an existing parameter
	 *
	 * @param parameter the updated parameter
	 * @param actionUser the user performing the action
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public void updateParameter(Parameter parameter, User actionUser) throws SQLException {
		logger.debug("Entering updateParameter: parameter={}, actionUser={}", parameter, actionUser);

		Integer newRecordId = null;
		saveParameter(parameter, newRecordId, actionUser);
	}

	/**
	 * Imports parameter records
	 *
	 * @param parameters the list of parameters to import
	 * @param actionUser the user who is performing the import
	 * @param conn the connection to use
	 * @param local whether the import is to the local/current art instance
	 * @throws Exception
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public void importParameters(List<Parameter> parameters, User actionUser,
			Connection conn, boolean local) throws Exception {

		logger.debug("Entering importParameters: actionUser={}, local={}",
				actionUser, local);

		boolean originalAutoCommit = true;

		try {
			String sql = "SELECT MAX(PARAMETER_ID) FROM ART_PARAMETERS";
			int id = dbService.getMaxRecordId(conn, sql);

			originalAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);

			List<Report> reports = new ArrayList<>();
			for (Parameter parameter : parameters) {
				Report defaultValueReport = parameter.getDefaultValueReport();
				if (defaultValueReport != null) {
					reports.add(defaultValueReport);
				}
				Report lovReport = parameter.getLovReport();
				if (lovReport != null) {
					reports.add(lovReport);
				}
			}

			ReportServiceHelper reportServiceHelper = new ReportServiceHelper();
			boolean commitReports = false;
			reportServiceHelper.importReports(reports, actionUser, conn, local, commitReports);

			for (Parameter parameter : parameters) {
				id++;
				saveParameter(parameter, id, actionUser, conn);
			}
			conn.commit();
		} catch (Exception ex) {
			conn.rollback();
			throw ex;
		} finally {
			conn.setAutoCommit(originalAutoCommit);
		}
	}

	/**
	 * Saves a parameter
	 *
	 * @param parameter the parameter to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user who is performing the save
	 * @throws SQLException
	 */
	private void saveParameter(Parameter parameter, Integer newRecordId,
			User actionUser) throws SQLException {

		Connection conn = null;
		saveParameter(parameter, newRecordId, actionUser, conn);
	}

	/**
	 * Saves a parameter
	 *
	 * @param parameter the parameter to save
	 * @param newRecordId id of the new record or null if editing an existing
	 * record
	 * @param actionUser the user performing the action
	 * @param conn the connection to use. if null, the art database will be used
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public void saveParameter(Parameter parameter, Integer newRecordId,
			User actionUser, Connection conn) throws SQLException {

		logger.debug("Entering saveParameter: parameter={}, newRecordId={},"
				+ " actionUser={}", parameter, newRecordId, actionUser);

		//set values for possibly null property objects
		String parameterType;
		if (parameter.getParameterType() == null) {
			parameterType = null;
		} else {
			parameterType = parameter.getParameterType().getValue();
		}

		String dataType;
		if (parameter.getDataType() == null) {
			dataType = null;
		} else {
			dataType = parameter.getDataType().getValue();
		}

		Integer defaultValueReportId = null;
		if (parameter.getDefaultValueReport() != null) {
			defaultValueReportId = parameter.getDefaultValueReport().getReportId();
			if (defaultValueReportId == 0) {
				defaultValueReportId = null;
			}
		}

		Integer lovReportId = null;
		if (parameter.getLovReport() != null) {
			lovReportId = parameter.getLovReport().getReportId();
			if (lovReportId == 0) {
				lovReportId = null;
			}
		}

		int affectedRows;
		boolean newRecord = false;
		if (newRecordId != null) {
			newRecord = true;
		}

		if (newRecord) {
			String sql = "INSERT INTO ART_PARAMETERS"
					+ " (PARAMETER_ID, NAME, DESCRIPTION, PARAMETER_TYPE, PARAMETER_LABEL,"
					+ " HELP_TEXT, DATA_TYPE, DEFAULT_VALUE, DEFAULT_VALUE_REPORT_ID,"
					+ " HIDDEN, FIXED_VALUE, SHARED, USE_LOV, LOV_REPORT_ID, USE_RULES_IN_LOV,"
					+ " DRILLDOWN_COLUMN_INDEX, USE_DIRECT_SUBSTITUTION, PARAMETER_OPTIONS,"
					+ " PARAMETER_DATE_FORMAT, PLACEHOLDER_TEXT, USE_DEFAULT_VALUE_IN_JOBS,"
					+ " TEMPLATE,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 24) + ")";

			Object[] values = {
				newRecordId,
				parameter.getName(),
				parameter.getDescription(),
				parameterType,
				parameter.getLabel(),
				parameter.getHelpText(),
				dataType,
				parameter.getDefaultValue(),
				defaultValueReportId,
				BooleanUtils.toInteger(parameter.isHidden()),
				BooleanUtils.toInteger(parameter.isFixedValue()),
				BooleanUtils.toInteger(parameter.isShared()),
				BooleanUtils.toInteger(parameter.isUseLov()),
				lovReportId,
				BooleanUtils.toInteger(parameter.isUseRulesInLov()),
				parameter.getDrilldownColumnIndex(),
				BooleanUtils.toInteger(parameter.isUseDirectSubstitution()),
				parameter.getOptions(),
				parameter.getDateFormat(),
				parameter.getPlaceholderText(),
				BooleanUtils.toInteger(parameter.isUseDefaultValueInJobs()),
				parameter.getTemplate(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		} else {
			String sql = "UPDATE ART_PARAMETERS SET NAME=?, DESCRIPTION=?, PARAMETER_TYPE=?,"
					+ " PARAMETER_LABEL=?, HELP_TEXT=?, DATA_TYPE=?, DEFAULT_VALUE=?,"
					+ " DEFAULT_VALUE_REPORT_ID=?, HIDDEN=?, FIXED_VALUE=?,"
					+ " SHARED=?, USE_LOV=?, LOV_REPORT_ID=?,"
					+ " USE_RULES_IN_LOV=?, DRILLDOWN_COLUMN_INDEX=?, USE_DIRECT_SUBSTITUTION=?,"
					+ " PARAMETER_OPTIONS=?, PARAMETER_DATE_FORMAT=?, PLACEHOLDER_TEXT=?,"
					+ " USE_DEFAULT_VALUE_IN_JOBS=?, TEMPLATE=?,"
					+ " UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE PARAMETER_ID=?";

			Object[] values = {
				parameter.getName(),
				parameter.getDescription(),
				parameterType,
				parameter.getLabel(),
				parameter.getHelpText(),
				dataType,
				parameter.getDefaultValue(),
				defaultValueReportId,
				BooleanUtils.toInteger(parameter.isHidden()),
				BooleanUtils.toInteger(parameter.isFixedValue()),
				BooleanUtils.toInteger(parameter.isShared()),
				BooleanUtils.toInteger(parameter.isUseLov()),
				lovReportId,
				BooleanUtils.toInteger(parameter.isUseRulesInLov()),
				parameter.getDrilldownColumnIndex(),
				BooleanUtils.toInteger(parameter.isUseDirectSubstitution()),
				parameter.getOptions(),
				parameter.getDateFormat(),
				parameter.getPlaceholderText(),
				BooleanUtils.toInteger(parameter.isUseDefaultValueInJobs()),
				parameter.getTemplate(),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				parameter.getParameterId()
			};

			if (conn == null) {
				affectedRows = dbService.update(sql, values);
			} else {
				affectedRows = dbService.update(conn, sql, values);
			}
		}

		if (newRecordId != null) {
			parameter.setParameterId(newRecordId);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, parameter={}",
					affectedRows, newRecord, parameter);
		}
	}

	/**
	 * Returns reports that use a given parameter
	 *
	 * @param parameterId the parameter id
	 * @return linked report names
	 * @throws SQLException
	 */
	public List<String> getLinkedReports(int parameterId) throws SQLException {
		logger.debug("Entering getLinkedReports: parameterId={}", parameterId);

		String sql = "SELECT AQ.NAME"
				+ " FROM ART_QUERIES AQ"
				+ " INNER JOIN ART_REPORT_PARAMETERS ARP ON"
				+ " ARP.REPORT_ID=AQ.QUERY_ID"
				+ " WHERE ARP.PARAMETER_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>(1);
		return dbService.query(sql, h, parameterId);
	}

	/**
	 * Returns the parameter for a given report that is in a given position
	 *
	 * @param reportId the report id
	 * @param position the parameter position
	 * @return parameter if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public Parameter getParameter(int reportId, int position) throws SQLException {
		logger.debug("Entering getParameter: reportId={}, position={}", reportId, position);

		String sql = SQL_SELECT_ALL
				+ " INNER JOIN ART_REPORT_PARAMETERS ARP ON"
				+ " ARP.PARAMETER_ID=AP.PARAMETER_ID"
				+ " WHERE ARP.REPORT_ID=? AND ARP.PARAMETER_POSITION=?";

		ResultSetHandler<Parameter> h = new BeanHandler<>(Parameter.class, new ParameterMapper());
		return dbService.query(sql, h, reportId, position);
	}
}
