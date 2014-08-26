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
package art.filter;

import art.dbutils.DbService;
import art.dbutils.DbUtils;
import art.enums.ParameterDataType;
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
			filter.setCreatedBy(rs.getString("CREATED_BY"));
			filter.setUpdatedBy(rs.getString("UPDATED_BY"));

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
	 * @return ActionResult. if delete was not successful, data contains a list
	 * of linked reports which prevented the filter from being deleted
	 * @throws SQLException
	 */
	@CacheEvict(value = "filters", allEntries = true)
	public ActionResult deleteFilter(int id) throws SQLException {
		logger.debug("Entering deleteFilter: id={}", id);

		ActionResult result = new ActionResult();

		//don't delete if important linked records exist
		List<String> linkedReports = getLinkedReports(id);
		if (!linkedReports.isEmpty()) {
			result.setData(linkedReports);
			return result;
		}

		String sql;

		sql = "DELETE FROM ART_USER_RULES WHERE RULE_ID=?";
		dbService.update(sql, id);

		sql = "DELETE FROM ART_USER_GROUP_RULES WHERE RULE_ID=?";
		dbService.update(sql, id);

		//finally delete filter
		sql = "DELETE FROM ART_RULES WHERE RULE_ID=?";
		dbService.update(sql, id);

		result.setSuccess(true);
		return result;
	}

	/**
	 * Add a new filter to the database
	 *
	 * @param filter
	 * @param actionUser
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "filters", allEntries = true)
	public synchronized int addFilter(Filter filter, User actionUser) throws SQLException {
		logger.debug("Entering addFilter: filter={}, actionUser={}", filter, actionUser);

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

		filter.setFilterId(newId);

		saveFilter(filter, true, actionUser);

		return newId;
	}

	/**
	 * Update an existing filter
	 *
	 * @param filter
	 * @param actionUser
	 * @throws SQLException
	 */
	@CacheEvict(value = "filters", allEntries = true)
	public void updateFilter(Filter filter, User actionUser) throws SQLException {
		logger.debug("Entering updateFilter: filter={}, actionUser={}", filter, actionUser);

		saveFilter(filter, false, actionUser);
	}

	/**
	 * Save a filter
	 *
	 * @param filter
	 * @param newRecord
	 * @param actionUser
	 * @throws SQLException
	 */
	private void saveFilter(Filter filter, boolean newRecord, User actionUser) throws SQLException {
		logger.debug("Entering saveFilter: filter={}, newRecord={},actionUser={}",
				filter, newRecord, actionUser);

		//set values for possibly null property objects
		String dataType;
		if (filter.getDataType() == null) {
			logger.warn("Data type not defined. Defaulting to varchar");
			dataType = ParameterDataType.Varchar.getValue();
		} else {
			dataType = filter.getDataType().getValue();
		}

		int affectedRows;
		if (newRecord) {
			String sql = "INSERT INTO ART_RULES"
					+ " (RULE_ID, RULE_NAME, SHORT_DESCRIPTION, DATA_TYPE,"
					+ " CREATION_DATE, CREATED_BY)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 6) + ")";

			Object[] values = {
				filter.getFilterId(),
				filter.getName(),
				filter.getDescription(),
				dataType,
				DbUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername()
			};

			affectedRows = dbService.update(sql, values);
		} else {
			String sql = "UPDATE ART_RULES SET RULE_NAME=?, SHORT_DESCRIPTION=?,"
					+ " DATA_TYPE=?, UPDATE_DATE=?, UPDATED_BY=?"
					+ " WHERE RULE_ID=?";

			Object[] values = {
				filter.getName(),
				filter.getDescription(),
				dataType,
				DbUtils.getCurrentTimeAsSqlTimestamp(),
				actionUser.getUsername(),
				filter.getFilterId()
			};

			affectedRows = dbService.update(sql, values);
		}

		logger.debug("affectedRows={}", affectedRows);

		if (affectedRows != 1) {
			logger.warn("Problem with save. affectedRows={}, newRecord={}, filter={}",
					affectedRows, newRecord, filter);
		}
	}

	/**
	 * Get the name of a given filter
	 *
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	@Cacheable("filters")
	public String getFilterName(int id) throws SQLException {
		logger.debug("Entering getFilterName: id={}", id);

		String sql = "SELECT RULE_NAME FROM ART_RULES WHERE RULE_ID=?";
		ResultSetHandler<String> h = new ScalarHandler<>(1);
		return dbService.query(sql, h, id);
	}

}
