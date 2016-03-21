/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.jobparameter;

import art.dbutils.DbService;
import art.enums.ParameterType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

/**
 *
 * @author Timothy Anyona
 */
public class JobParameterService {

	private static final Logger logger = LoggerFactory.getLogger(JobParameterService.class);

	private final DbService dbService;

	@Autowired
	public JobParameterService(DbService dbService) {
		this.dbService = dbService;
	}

	public JobParameterService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_JOBS_PARAMETERS";

	/**
	 * Class to map resultset to an object
	 */
	private class JobParameterMapper extends BasicRowProcessor {

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
			JobParameter filter = new JobParameter();

			filter.setJobId(rs.getInt("JOB_ID"));
			filter.setName(rs.getString("PARAM_NAME"));
			filter.setValue(rs.getString("PARAM_VALUE"));
			filter.setParameterType(ParameterType.toEnum(rs.getString("PARAM_TYPE")));

			return type.cast(filter);
		}
	}

	/**
	 * Get a filter
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	@Cacheable("filters")
	public List<JobParameter> getJobParameters(int id) throws SQLException {
		logger.debug("Entering getJobParameter: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE JOB_ID=?";
		ResultSetHandler<List<JobParameter>> h = new BeanListHandler<>(JobParameter.class, new JobParameterMapper());
		return dbService.query(sql, h, id);
	}
}
