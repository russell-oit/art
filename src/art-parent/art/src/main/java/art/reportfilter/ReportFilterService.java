/**
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
package art.reportfilter;

import art.dbutils.DbService;
import art.filter.Filter;
import art.filter.FilterService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
 * Class to provide methods related to report filters
 *
 * @author Timothy Anyona
 */
@Service
public class ReportFilterService {

	private static final Logger logger = LoggerFactory.getLogger(ReportFilterService.class);

	@Autowired
	private DbService dbService;

	@Autowired
	private FilterService filterService;

	private final String SQL_SELECT_ALL = "SELECT AQR.*, AR.RULE_ID, AR.RULE_NAME"
			+ " FROM ART_QUERY_RULES AQR"
			+ " INNER JOIN ART_RULES AR ON"
			+ " AQR.RULE_ID=AR.RULE_ID";

	/**
	 * Maps a resultset to an object
	 */
	private class ReportFilterMapper extends BasicRowProcessor {

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
			ReportFilter reportFilter = new ReportFilter();

			reportFilter.setReportFilterId(rs.getInt("QUERY_RULE_ID"));
			reportFilter.setReportId(rs.getInt("QUERY_ID"));
			reportFilter.setReportColumn(rs.getString("FIELD_NAME"));

			Filter filter = new Filter();
			filter.setFilterId(rs.getInt("RULE_ID"));
			filter.setName(rs.getString("RULE_NAME"));

			reportFilter.setFilter(filter);

			return type.cast(reportFilter);
		}
	}

	/**
	 * Get report filters for a given report
	 *
	 * @param reportId
	 * @return list of report filters for a given report, empty list otherwise
	 * @throws SQLException
	 */
	@Cacheable("filters")
	public List<ReportFilter> getReportFilters(int reportId) throws SQLException {
		logger.debug("Entering getReportFilters: reportId={}", reportId);

		String sql = SQL_SELECT_ALL + " WHERE AQR.QUERY_ID=?";
		ResultSetHandler<List<ReportFilter>> h = new BeanListHandler<>(ReportFilter.class, new ReportFilterMapper());
		return dbService.query(sql, h, reportId);
	}

	/**
	 * Get a report filter
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("filters")
	public ReportFilter getReportFilter(int id) throws SQLException {
		logger.debug("Entering getReportFilter: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE QUERY_RULE_ID=?";
		ResultSetHandler<ReportFilter> h = new BeanHandler<>(ReportFilter.class, new ReportFilterMapper());
		return dbService.query(sql, h, id);
	}

	/**
	 * Delete a report filter
	 *
	 * @param id
	 * @throws SQLException
	 */
	@CacheEvict(value = "filters", allEntries = true)
	public void deleteReportFilter(int id) throws SQLException {
		logger.debug("Entering deleteReportFilter: id={}", id);

		String sql;

		//finally delete report filter
		sql = "DELETE FROM ART_QUERY_RULES WHERE QUERY_RULE_ID=?";
		dbService.update(sql, id);
	}

	/**
	 * Add a new report filter to the database
	 *
	 * @param reportFilter
	 * @param reportId
	 * @return new record id
	 * @throws SQLException
	 */
	@CacheEvict(value = "filters", allEntries = true)
	public synchronized int addReportFilter(ReportFilter reportFilter, int reportId) throws SQLException {
		logger.debug("Entering addReportFilter: reportFilter={}", reportFilter);

		//generate new id
		String sql = "SELECT MAX(QUERY_RULE_ID) FROM ART_QUERY_RULES";
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

		sql = "INSERT INTO ART_QUERY_RULES"
				+ " (QUERY_RULE_ID, QUERY_ID, RULE_ID, RULE_NAME, FIELD_NAME)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 5) + ")";

		Object[] values = {
			newId,
			reportFilter.getReportId(),
			reportFilter.getFilter().getFilterId(),
			filterService.getFilterName(reportFilter.getFilter().getFilterId()), //remove once rule_name is removed
			reportFilter.getReportColumn()
		};

		dbService.update(sql, values);

		return newId;
	}

	/**
	 * Update an existing report filter
	 *
	 * @param reportFilter
	 * @throws SQLException
	 */
	@CacheEvict(value = "filters", allEntries = true)
	public void updateReportFilter(ReportFilter reportFilter) throws SQLException {
		logger.debug("Entering updateReportFilter: reportFilter={}", reportFilter);

		String sql = "UPDATE ART_QUERY_RULES SET QUERY_ID=?, RULE_ID=?,"
				+ " RULE_NAME=?, FIELD_NAME=?"
				+ " WHERE QUERY_RULE_ID=?";

		Object[] values = {
			reportFilter.getReportId(),
			reportFilter.getFilter().getFilterId(),
			filterService.getFilterName(reportFilter.getFilter().getFilterId()),
			reportFilter.getReportColumn(),
			reportFilter.getReportFilterId()
		};

		dbService.update(sql, values);
	}

}
