/*
 * ART. A Reporting Tool.
 * Copyright (C) 2020 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.pipelinerunningjob;

import art.dbutils.DbService;
import art.role.Role;
import art.role.RoleService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Provides methods for adding and deleting pipeline running job records
 *
 * @author Timothy Anyona
 */
@Service
public class PipelineRunningJobService {

	private static final Logger logger = LoggerFactory.getLogger(PipelineRunningJobService.class);

	private final DbService dbService;

	@Autowired
	public PipelineRunningJobService(DbService dbService) {
		this.dbService = dbService;
	}

	public PipelineRunningJobService() {
		dbService = new DbService();
	}

	private final String SQL_SELECT_ALL = "SELECT * FROM ART_PIPELINE_RUNNING_JOBS";

	/**
	 * Maps a resultset to an object
	 */
	private class PipelineRunningJobMapper extends BasicRowProcessor {

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
			PipelineRunningJob job = new PipelineRunningJob();

			job.setPipelineId(rs.getInt("PIPELINE_ID"));
			job.setJobId(rs.getInt("JOB_ID"));
			job.setQuartzJobName(rs.getString("QUARTZ_JOB_NAME"));
			job.setParallel(rs.getBoolean("PARALLEL"));

			return type.cast(job);
		}
	}

	/**
	 * Returns parallel pipeline running jobs for a given pipeline
	 *
	 * @param pipelineId the pipeline id
	 * @return parallel pipeline running jobs
	 * @throws SQLException
	 */
	public List<PipelineRunningJob> getParallelPipelineRunningJobs(int pipelineId) throws SQLException {
		logger.debug("Entering getParallelPipelineRunningJobs: pipelineId={}", pipelineId);

		String sql = SQL_SELECT_ALL + " WHERE PARALLEL=1 AND PIPELINE_ID=?";
		ResultSetHandler<List<PipelineRunningJob>> h = new BeanListHandler<>(PipelineRunningJob.class, new PipelineRunningJobMapper());
		return dbService.query(sql, h, pipelineId);
	}

	/**
	 * Returns the ids of jobs currently running as part of a pipeline
	 *
	 * @param pipelineId the pipeline id
	 * @return ids of currently running pipeline jobs
	 * @throws SQLException
	 */
	public List<Integer> getPipelineRunningJobIds(int pipelineId) throws SQLException {
		logger.debug("Entering getPipelineRunningJobIds: pipelineId={}", pipelineId);

		String sql = "SELECT JOB_ID"
				+ " FROM ART_PIPELINE_RUNNING_JOBS"
				+ " WHERE PIPELINE_ID=?";

		ResultSetHandler<List<Number>> h = new ColumnListHandler<>("JOB_ID");
		List<Number> numberIds = dbService.query(sql, h, pipelineId);

		List<Integer> integerIds = new ArrayList<>();
		for (Number number : numberIds) {
			integerIds.add(number.intValue());
		}

		return integerIds;
	}

	/**
	 * Adds a pipeline running job record
	 *
	 * @param pipelineId the pipeline id
	 * @param jobId the job id
	 * @param quartzJobName the name of the quartz job
	 * @param serial the current serial setting
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void addPipelineRunningJob(int pipelineId, int jobId, String quartzJobName,
			String serial) throws SQLException {

		logger.debug("Entering addPipelineRunningJob: pipelineId={}, jobId={},"
				+ " quartzJobName='{}', serial='{}'",
				pipelineId, jobId, quartzJobName, serial);

		String sql = "INSERT INTO ART_PIPELINE_RUNNING_JOBS"
				+ " (PIPELINE_ID, JOB_ID, QUARTZ_JOB_NAME, PARALLEL)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

		boolean parallel = false;
		if (serial == null) {
			parallel = true;
		}

		Object[] values = {
			pipelineId,
			jobId,
			quartzJobName,
			BooleanUtils.toInteger(parallel)
		};

		dbService.update(sql, values);
	}

	/**
	 * Removes a pipeline running job record
	 *
	 * @param pipelineId the pipeline id
	 * @param jobId the job id
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void removePipelineRunningJob(int pipelineId, int jobId) throws SQLException {
		logger.debug("Entering removePipelineRunningJob: pipelineId={}, jobId={}",
				pipelineId, jobId);

		String sql = "DELETE FROM ART_PIPELINE_RUNNING_JOBS"
				+ " WHERE PIPELINE_ID=? AND JOB_ID=?";

		dbService.update(sql, pipelineId, jobId);
	}

}
