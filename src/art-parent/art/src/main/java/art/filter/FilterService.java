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
package art.filter;

import art.dbutils.DbService;
import art.dbutils.DbUtils;
import art.enums.ParameterDataType;
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
 * Class to provide methods related to filters
 *
 * @author Timothy Anyona
 */
@Service
public class FilterService {

	private static final Logger logger = LoggerFactory.getLogger(FilterService.class);

	@Autowired
	private DbService dbService;

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_RULES";

	/**
	 * Class to map resultset to an object
	 */
	private class FilterMapper extends BasicRowProcessor {

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
			Filter filter = new Filter();

			filter.setFilterId(rs.getInt("RULE_ID"));
			filter.setName(rs.getString("RULE_NAME"));
			filter.setDescription(rs.getString("SHORT_DESCRIPTION"));
			filter.setDataType(ParameterDataType.toEnum(rs.getString("DATA_TYPE")));
			filter.setCreationDate(rs.getTimestamp("CREATION_DATE"));
			filter.setUpdateDate(rs.getTimestamp("UPDATE_DATE"));

			return type.cast(filter);
		}
	}

	/**
	 * Get all filters
	 *
	 * @return list of all filters, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("filters")
	public List<Filter> getAllFilters() throws SQLException {
		logger.debug("Entering getAllFilters");

		ResultSetHandler<List<Filter>> h = new BeanListHandler<>(Filter.class, new FilterMapper());
		return dbService.query(SQL_SELECT_ALL, h);
	}

	/**
	 * Get a filter
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("filters")
	public Filter getFilter(int id) throws SQLException {
		logger.debug("Entering getFilter: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE RULE_ID=?";
		ResultSetHandler<Filter> h = new BeanHandler<>(Filter.class, new FilterMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Get reports that use a given filter
	 *
	 * @param filterId
	 * @return list with linked report names, empty list otherwise
	 * @throws SQLException
	 */
	public List<String> getLinkedReports(int filterId) throws SQLException {
		logger.debug("Entering getLinkedReports: filterId={}", filterId);

		String sql = "SELECT AQ.NAME"
				+ " FROM ART_QUERY_RULES AQR"
				+ " INNER JOIN ART_QUERIES AQ ON"
				+ " AQR.QUERY_ID=AQ.QUERY_ID"
				+ " WHERE AQR.RULE_ID=?";

		ResultSetHandler<List<String>> h = new ColumnListHandler<>(1);
		return dbService.query(sql, h, filterId);
	}

	/**
	 * Delete a filter
	 *
	 * @param id
	 * @param linkedReports output parameter. list that will be populated with
	 * linked jobs if they exist
	 * @return -1 if the record was not deleted because there are some linked
	 * records in other tables, otherwise the count of the number of reports
	 * deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "filters", allEntries = true)
	public int deleteFilter(int id, List<String> linkedReports) throws SQLException {
		logger.debug("Entering deleteFilter: id={}", id);

		//don't delete if important linked records exist
		List<String> reports = getLinkedReports(id);
		if (!reports.isEmpty()) {
			if (linkedReports != null) {
				linkedReports.addAll(reports);
			}
			return -1;
		}

		String sql;

		sql = "DELETE FROM ART_USER_RULES WHERE RULE_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_RULES WHERE RULE_ID=?";
		dbService.update(sql, id);

		//finally delete filter
		sql = "DELETE FROM ART_RULES WHERE RULE_ID=?";
		return dbService.update(sql, id);
	}

	/**
	 * Add a new filter to the database
	 *
	 * @param filter
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "filters", allEntries = true)
	public synchronized int addFilter(Filter filter) throws SQLException {
		logger.debug("Entering addFilter: filter={}", filter);

		//generate new id
		String sql = "SELECT MAX(RULE_ID) FROM ART_RULES";
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

		sql = "INSERT INTO ART_RULES"
				+ " (RULE_ID, RULE_NAME, SHORT_DESCRIPTION, DATA_TYPE, CREATION_DATE)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 5) + ")";
		
		//set values for possibly null property objects
		Map<String, Object> defaults = getSaveDefaults(filter);

		Object[] values = {
			newId,
			filter.getName(),
			filter.getDescription(),
			defaults.get("dataType"),
			DbUtils.getCurrentTimeStamp()
		};

		dbService.update(sql, values);

		return newId;
	}

	/**
	 * Update an existing filter
	 *
	 * @param filter
	 * @throws SQLException
	 */
	@CacheEvict(value = "filters", allEntries = true)
	public void updateFilter(Filter filter) throws SQLException {
		logger.debug("Entering updateFilter: filter={}", filter);

		String sql = "UPDATE ART_RULES SET RULE_NAME=?, SHORT_DESCRIPTION=?,"
				+ " DATA_TYPE=?, UPDATE_DATE=?"
				+ " WHERE RULE_ID=?";
		
		//set values for possibly null property objects
		Map<String, Object> defaults = getSaveDefaults(filter);

		Object[] values = {
			filter.getName(),
			filter.getDescription(),
			defaults.get("dataType"),
			DbUtils.getCurrentTimeStamp(),
			filter.getFilterId()
		};

		dbService.update(sql, values);
	}

	/**
	 * Get values for possibly null property objects
	 *
	 * @param filter
	 * @return map with values to save. key = field name, value = field value
	 */
	private Map<String, Object> getSaveDefaults(Filter filter) {
		Map<String, Object> values = new HashMap<>();

		String dataType;
		if (filter.getDataType() == null) {
			logger.warn("Data type not defined. Defaulting to varchar");
			dataType = ParameterDataType.Varchar.getValue();
		} else {
			dataType = filter.getDataType().getValue();
		}
		values.put("dataType", dataType);

		return values;
	}

}
