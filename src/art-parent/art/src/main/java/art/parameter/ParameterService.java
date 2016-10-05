/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.parameter;

import art.dbutils.DbService;
import art.dbutils.DatabaseUtils;
import art.enums.ParameterDataType;
import art.enums.ParameterType;
import art.user.User;
import art.utils.ActionResult;
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

	@Autowired
	public ParameterService(DbService dbService) {
		this.dbService = dbService;
	}

	public ParameterService() {
		dbService = new DbService();
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
			parameter.setUseLov(rs.getBoolean("USE_LOV"));
			parameter.setLovReportId(rs.getInt("LOV_REPORT_ID"));
			parameter.setUseRulesInLov(rs.getBoolean("USE_RULES_IN_LOV"));
			parameter.setDrilldownColumnIndex(rs.getInt("DRILLDOWN_COLUMN_INDEX"));
			parameter.setUseDirectSubstitution(rs.getBoolean("USE_DIRECT_SUBSTITUTION"));
			parameter.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			parameter.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));
			parameter.setCreatedBy(rs.getString("CREATED_BY"));
			parameter.setUpdatedBy(rs.getString("UPDATED_BY"));

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
		List<Integer> nonDeletedRecords = new ArrayList<>();

		for (Integer id : ids) {
			ActionResult deleteResult = deleteParameter(id);
			if (!deleteResult.isSuccess()) {
				nonDeletedRecords.add(id);
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

		parameter.setParameterId(newId);

		saveParameter(parameter, true, actionUser);

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

		saveParameter(parameter, false, actionUser);
	}

	/**
	 * Saves a parameter
	 *
	 * @param parameter the parameter to save
	 * @param newRecord whether this is a new record
	 * @param actionUser the user performing the action
	 * @throws SQLException
	 */
	private void saveParameter(Parameter parameter, boolean newRecord, User actionUser) throws SQLException {
		logger.debug("Entering saveParameter: parameter={}, newRecord={}, actionUser={}",
				parameter, newRecord, actionUser);

		//set values for possibly null property objects
		String parameterType;
		if (parameter.getParameterType() == null) {
			logger.warn("Parameter type not defined. Defaulting to null");
			parameterType = null;
		} else {
			parameterType = parameter.getParameterType().getValue();
		}

		String dataType;
		if (parameter.getDataType() == null) {
			logger.warn("Data type not defined. Defaulting to null");
			dataType = null;
		} else {
			dataType = parameter.getDataType().getValue();
		}

		int affectedRows;
		if (newRecord) {
			String sql = "INSERT INTO ART_PARAMETERS"
					+ " (PARAMETER_ID, NAME, DESCRIPTION, PARAMETER_TYPE, PARAMETER_LABEL,"
					+ " HELP_TEXT, DATA_TYPE, DEFAULT_VALUE, HIDDEN, USE_LOV,"
					+ " LOV_REPORT_ID, USE_RULES_IN_LOV,"
					+ " DRILLDOWN_COLUMN_INDEX,"
					+ " USE_DIRECT_SUBSTITUTION, CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 16) + ")";

			Object[] values = {
				parameter.getParameterId(),
				parameter.getName(),
				parameter.getDescription(),
				parameterType,
				parameter.getLabel(),
				parameter.getHelpText(),
				dataType,
				parameter.getDefaultValue(),
				BooleanUtils.toInteger(parameter.isHidden()),
				BooleanUtils.toInteger(parameter.isUseLov()),
				parameter.getLovReportId(),
				BooleanUtils.toInteger(parameter.isUseRulesInLov()),
				parameter.getDrilldownColumnIndex(),
				BooleanUtils.toInteger(parameter.isUseDirectSubstitution()),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_PARAMETERS SET NAME=?, DESCRIPTION=?, PARAMETER_TYPE=?,"
					+ " PARAMETER_LABEL=?, HELP_TEXT=?, DATA_TYPE=?, DEFAULT_VALUE=?,"
					+ " HIDDEN=?, USE_LOV=?, LOV_REPORT_ID=?, USE_RULES_IN_LOV=?,"
					+ " DRILLDOWN_COLUMN_INDEX=?, USE_DIRECT_SUBSTITUTION=?,"
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
				BooleanUtils.toInteger(parameter.isHidden()),
				BooleanUtils.toInteger(parameter.isUseLov()),
				parameter.getLovReportId(),
				BooleanUtils.toInteger(parameter.isUseRulesInLov()),
				parameter.getDrilldownColumnIndex(),
				BooleanUtils.toInteger(parameter.isUseDirectSubstitution()),
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				parameter.getParameterId()
			};

			affectedRows = dbService.update(sql, values);
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
				+ " FROM ART_REPORT_PARAMETERS ARP"
				+ " INNER JOIN ART_QUERIES AQ ON"
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
				+ " INNER JOIN ART_REPORT_PARAMETERS ARP"
				+ " ARP.PARAMETER_ID=AP.PARAMETER_ID"
				+ " WHERE ARP.REPORT_ID=? AND ARP.PARAMETER_POSITION=?";

		ResultSetHandler<Parameter> h = new BeanHandler<>(Parameter.class, new ParameterMapper());
		return dbService.query(sql, h, reportId, position);
	}
}
