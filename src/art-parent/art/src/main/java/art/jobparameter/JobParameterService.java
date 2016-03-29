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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Timothy Anyona
 */
@Service
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
			JobParameter jobParam = new JobParameter();

			jobParam.setJobId(rs.getInt("JOB_ID"));
			jobParam.setName(rs.getString("PARAM_NAME"));
			jobParam.setValue(rs.getString("PARAM_VALUE"));
//			jobParam.setParameterType(ParameterType.toEnum(rs.getString("PARAM_TYPE")));
			jobParam.setParamTypeString(rs.getString("PARAM_TYPE"));

			return type.cast(jobParam);
		}
	}

	/**
	 * Get a filter
	 *
	 * @param id
	 * @return populated object if found, null otherwise
	 * @throws SQLException
	 */
	public List<JobParameter> getJobParameters(int id) throws SQLException {
		logger.debug("Entering getJobParameters: id={}", id);

		String sql = SQL_SELECT_ALL + " WHERE JOB_ID=?";
		ResultSetHandler<List<JobParameter>> h = new BeanListHandler<>(JobParameter.class, new JobParameterMapper());
		return dbService.query(sql, h, id);
	}

	public void updateJobParameter(JobParameter jobParam) throws SQLException {
		logger.debug("Entering updateJobParameter");
		
		String sql = "UPDATE ART_JOBS_PARAMETERS SET PARAM_VALUE=?"
				+ " WHERE JOB_ID=? AND PARAM_NAME=?";

		Object[] values = {
			jobParam.getValue(),
			jobParam.getJobId(),
			jobParam.getName()
		};

		dbService.update(sql, values);
	}
	
	public void deleteJobParameters(int jobId) throws SQLException{
		logger.debug("Entering deleteJobParameters: jobId={}", jobId);
		
		String sql = "DELETE FROM ART_JOBS_PARAMETERS"
				+ " WHERE JOB_ID=?";

		Object[] values = {
			jobId
		};

		dbService.update(sql, values);
	}
	
	public void addJobParameter(JobParameter jobParam) throws SQLException {
		logger.debug("Entering addJobParameter");
		
		String sql = "INSERT INTO ART_JOBS_PARAMETERS"
				+ " (JOB_ID, PARAM_TYPE, PARAM_NAME, PARAM_VALUE)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

		Object[] values = {
			jobParam.getJobId(),
			"X",
			jobParam.getName(),
			jobParam.getValue()
		};

		dbService.update(sql, values);
	}
}
