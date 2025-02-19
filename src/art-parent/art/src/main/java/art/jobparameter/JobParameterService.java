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
package art.jobparameter;

import art.dbutils.DbService;
import art.utils.ArtUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, adding, updating and deleting job parameters
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
	 * Maps a resultset to an object
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
			jobParam.setParamTypeString(rs.getString("PARAM_TYPE")); //might contain invalid type string

			return type.cast(jobParam);
		}
	}

	/**
	 * Returns job parameters for the given job
	 *
	 * @param jobId the job id
	 * @return job parameters for the given job
	 * @throws SQLException
	 */
	public List<JobParameter> getJobParameters(int jobId) throws SQLException {
		logger.debug("Entering getJobParameters: jobId={}", jobId);

		String sql = SQL_SELECT_ALL + " WHERE JOB_ID=?";
		ResultSetHandler<List<JobParameter>> h = new BeanListHandler<>(JobParameter.class, new JobParameterMapper());
		return dbService.query(sql, h, jobId);
	}

	/**
	 * Updates a job parameter
	 *
	 * @param jobParam the updated job parameter
	 * @throws SQLException
	 */
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

	/**
	 * Deletes job parameters for the given job
	 *
	 * @param jobId the job id
	 * @throws SQLException
	 */
	public void deleteJobParameters(int jobId) throws SQLException {
		logger.debug("Entering deleteJobParameters: jobId={}", jobId);

		String sql = "DELETE FROM ART_JOBS_PARAMETERS WHERE JOB_ID=?";

		dbService.update(sql, jobId);
	}

	/**
	 * Adds a job parameter
	 *
	 * @param jobParam the job parameter to add
	 * @throws SQLException
	 */
	public void addJobParameter(JobParameter jobParam) throws SQLException {
		logger.debug("Entering addJobParameter");

		String sql = "INSERT INTO ART_JOBS_PARAMETERS"
				+ " (JOB_ID, PARAM_TYPE, PARAM_NAME, PARAM_VALUE)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

		String parameterTypeString = "X";

		Object[] values = {
			jobParam.getJobId(),
			parameterTypeString,
			jobParam.getName(),
			jobParam.getValue()
		};

		dbService.update(sql, values);
	}

	/**
	 * Returns job parameter values for a given job
	 *
	 * @param jobId the job id
	 * @return job parameter values for a given job
	 * @throws SQLException
	 */
	public Map<String, String[]> getJobParameterValues(int jobId) throws SQLException {
		logger.debug("Entering getJobParameterValues: jobId={}", jobId);

		List<JobParameter> jobParams = getJobParameters(jobId);
		Map<String, List<String>> paramValues = new HashMap<>();

		//accomodate legacy job parameter names
		for (JobParameter jobParam : jobParams) {
			String name = jobParam.getName();
			String finalName = name;
			String paramTypeString = jobParam.getParamTypeString();

			switch (paramTypeString) {
				case "O":
					switch (name) {
						case "_showParams":
							finalName = "showSelectedParameters";
							break;
						case "_showGraphData":
							finalName = "showData";
							break;
						case "_showGraphLegend":
							finalName = "showLegend";
							break;
						case "_showGraphLabels":
							finalName = "showLabels";
							break;
						case "_showGraphDataPoints":
							finalName = "showPoints";
							break;
						default:
							finalName = name;
					}
					break;
				case "I":
				case "M":
					finalName = ArtUtils.PARAM_PREFIX + name;
					break;
				case "X":
					finalName = name;
					break;
				default:
					throw new IllegalArgumentException("Unexpected job parameter type: " + paramTypeString);
			}

			jobParam.setName(finalName);
			List<String> values = paramValues.get(finalName);
			if (values == null) {
				paramValues.put(finalName, new ArrayList<String>());
			}
		}

		for (JobParameter jobParam : jobParams) {
			String name = jobParam.getName();
			String value = jobParam.getValue();
			List<String> values = paramValues.get(name);
			values.add(value);
		}

		Map<String, String[]> finalValues = new HashMap<>();

		for (JobParameter jobParam : jobParams) {
			String name = jobParam.getName();
			List<String> values = paramValues.get(name);
			String[] valuesArray = values.toArray(new String[0]);
			finalValues.put(name, valuesArray);
		}

		return finalValues;
	}
}
