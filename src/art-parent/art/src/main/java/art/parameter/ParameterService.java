/**
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
package art.parameter;

import art.dbutils.DbService;
import art.dbutils.DbUtils;
import art.enums.ParameterDataType;
import art.enums.ParameterType;
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
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Class to provide methods related to parameters
 *
 * @author Timothy Anyona
 */
@Service
public class ParameterService {

	private static final Logger logger = LoggerFactory.getLogger(ParameterService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_PARAMETERS";

	/**
	 * Class to map resultset to an object
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
			parameter.setUseFiltersInLov(rs.getBoolean("USE_FILTERS_IN_LOV"));
			parameter.setChainedPosition(rs.getInt("CHAINED_POSITION"));
			parameter.setChainedValuePosition(rs.getInt("CHAINED_VALUE_POSITION"));
			parameter.setDrilldownColumnIndex(rs.getInt("DRILLDOWN_COLUMN_INDEX"));
			parameter.setUseDirectSubstitution(rs.getBoolean("USE_DIRECT_SUBSTITUTION"));
			parameter.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			parameter.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));

			return type.cast(parameter);
		}
	}

	/**
	 * Get all parameters
	 *
	 * @return list of all parameters, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("parameters")
	public List<Parameter> getAllParameters() throws SQLException {
		logger.debug("Entering getAllParameters");

		ResultSetHandler<List<Parameter>> h = new BeanListHandler<>(Parameter.class, new ParameterMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Get a parameter
	 *
	 * @param id
	 * @return populated object if found, null otherwise
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
	 * Delete a parameter
	 *
	 * @param id
	 * @param linkedReports output parameter. list that will be populated with
	 * linked jobs if they exist
	 * @return -1 if the record was not deleted because there are some linked
	 * records in other tables, otherwise the count of the number of reports
	 * deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public int deleteParameter(int id, List<String> linkedReports) throws SQLException {
		logger.debug("Entering deleteParameter: id={}", id);

		//don't delete if important linked records exist
		List<String> reports = getLinkedReports(id);
		if (!reports.isEmpty()) {
			if (linkedReports != null) {
				linkedReports.addAll(reports);
			}
			return -1;
		}

		String sql;

		//finally delete parameter
		sql = "DELETE FROM ART_PARAMETERS WHERE PARAMETER_ID=?";
		return dbService.update(sql, id);
	}

	/**
	 * Add a new parameter to the database
	 *
	 * @param parameter
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public synchronized int addParameter(Parameter parameter) throws SQLException {
		logger.debug("Entering addParameter: parameter={}", parameter);

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

		sql = "INSERT INTO ART_PARAMETERS"
				+ " (PARAMETER_ID, NAME, DESCRIPTION, PARAMETER_TYPE, PARAMETER_LABEL,"
				+ " HELP_TEXT, DATA_TYPE, DEFAULT_VALUE, HIDDEN, USE_LOV,"
				+ " LOV_REPORT_ID, USE_FILTERS_IN_LOV, CHAINED_POSITION,"
				+ " CHAINED_VALUE_POSITION, DRILLDOWN_COLUMN_INDEX,"
				+ " USE_DIRECT_SUBSTITUTION, CREATION_DATE)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 16) + ")";

		//set values for possibly null property objects
		Map<String, Object> defaults = getSaveDefaults(parameter);

		Object[] values = {
			newId,
			parameter.getName(),
			parameter.getDescription(),
			defaults.get("parameterType"),
			parameter.getLabel(),
			parameter.getHelpText(),
			defaults.get("dataType"),
			parameter.getDefaultValue(),
			parameter.isHidden(),
			parameter.isUseLov(),
			parameter.getLovReportId(),
			parameter.isUseFiltersInLov(),
			parameter.getChainedPosition(),
			parameter.getChainedValuePosition(),
			parameter.getDrilldownColumnIndex(),
			parameter.isUseDirectSubstitution(),
			DbUtils.getCurrentTimeStamp()
		};

		dbService.update(sql, values);

		return newId;
	}

	/**
	 * Update an existing parameter
	 *
	 * @param parameter
	 * @throws SQLException
	 */
	@CacheEvict(value = "parameters", allEntries = true)
	public void updateParameter(Parameter parameter) throws SQLException {
		logger.debug("Entering updateParameter: parameter={}", parameter);

		String sql = "UPDATE ART_PARAMETERS SET NAME=?, DESCRIPTION=?, PARAMETER_TYPE=?,"
				+ " PARAMETER_LABEL=?, HELP_TEXT=?, DATA_TYPE=?, DEFAULT_VALUE=?,"
				+ " HIDDEN=?, USE_LOV=?, LOV_REPORT_ID=?, USE_FILTERS_IN_LOV=?,"
				+ " CHAINED_POSITION=?, CHAINED_VALUE_POSITION=?,"
				+ " DRILLDOWN_COLUMN_INDEX=?, USE_DIRECT_SUBSTITUTION=?,"
				+ " UPDATE_DATE=?"
				+ " WHERE PARAMETER_ID=?";

		//set values for possibly null property objects
		Map<String, Object> defaults = getSaveDefaults(parameter);

		Object[] values = {
			parameter.getName(),
			parameter.getDescription(),
			defaults.get("parameterType"),
			parameter.getLabel(),
			parameter.getHelpText(),
			defaults.get("dataType"),
			parameter.getDefaultValue(),
			parameter.isHidden(),
			parameter.isUseLov(),
			parameter.getLovReportId(),
			parameter.isUseFiltersInLov(),
			parameter.getChainedPosition(),
			parameter.getChainedValuePosition(),
			parameter.getDrilldownColumnIndex(),
			parameter.isUseDirectSubstitution(),
			DbUtils.getCurrentTimeStamp(),
			parameter.getParameterId()
		};

		dbService.update(sql, values);
	}

	/**
	 * Get values for possibly null property objects
	 *
	 * @param parameter
	 * @return map with values to save. key = field name, value = field value
	 */
	private Map<String, Object> getSaveDefaults(Parameter parameter) {
		Map<String, Object> values = new HashMap<>();

		String parameterType;
		if (parameter.getParameterType() == null) {
			logger.warn("Parameter type not defined. Defaulting to null");
			parameterType = null;
		} else {
			parameterType = parameter.getParameterType().getValue();
		}
		values.put("parameterType", parameterType);

		String dataType;
		if (parameter.getDataType() == null) {
			logger.warn("Data type not defined. Defaulting to null");
			dataType = null;
		} else {
			dataType = parameter.getDataType().getValue();
		}
		values.put("dataType", dataType);

		return values;
	}

	/**
	 * Get reports that use a given parameter
	 *
	 * @param parameterId
	 * @return list with linked report names, empty list otherwise
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

}
