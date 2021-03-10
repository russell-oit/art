/*
 * ART. A Reporting Tool.
 * Copyright (C) 2021 Enrico Liboni <eliboni@users.sf.net>
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
package art.pipelinescheduledjob;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Provides methods for adding and deleting pipeline running job records
 *
 * @author Timothy Anyona
 */
@Service
public class PipelineScheduledJobService {

	private static final Logger logger = LoggerFactory.getLogger(PipelineScheduledJobService.class);

	private final DbService dbService;

	@Autowired
	public PipelineScheduledJobService(DbService dbService) {
		this.dbService = dbService;
	}

	public PipelineScheduledJobService() {
		dbService = new DbService();
	}

	/**
	 * Returns the ids of jobs scheduled as part of a pipeline
	 *
	 * @param pipelineId the pipeline id
	 * @return ids of scheduled pipeline jobs
	 * @throws SQLException
	 */
	public List<Integer> getPipelineScheduledJobIds(int pipelineId) throws SQLException {
		logger.debug("Entering getPipelineScheduledJobIds: pipelineId={}", pipelineId);

		String sql = "SELECT JOB_ID"
				+ " FROM ART_PIPELINE_SCHEDULED_JOBS"
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
	 * Adds a pipeline scheduled job record
	 *
	 * @param pipelineId the pipeline id
	 * @param jobId the job id
	 * @param quartzJobName the quartz job name
	 * @param runDate the run date for the job
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void addPipelineScheduledJob(int pipelineId, int jobId, String quartzJobName,
			Date runDate) throws SQLException {

		logger.debug("Entering addPipelineScheduledJob: pipelineId={}, jobId={},"
				+ " quartzJobName='{}', runDate={}",
				pipelineId, jobId, quartzJobName, runDate);

		String sql = "INSERT INTO ART_PIPELINE_SCHEDULED_JOBS"
				+ " (PIPELINE_ID, JOB_ID, QUARTZ_JOB_NAME, RUN_DATE)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 4) + ")";

		Object[] values = {
			pipelineId,
			jobId,
			quartzJobName,
			DatabaseUtils.toSqlTimestamp(runDate)
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
	public void removePipelineScheduledJob(int pipelineId, int jobId) throws SQLException {
		logger.debug("Entering removePipelineScheduledJob: pipelineId={}, jobId={}",
				pipelineId, jobId);

		String sql = "DELETE FROM ART_PIPELINE_SCHEDULED_JOBS"
				+ " WHERE PIPELINE_ID=? AND JOB_ID=?";

		dbService.update(sql, pipelineId, jobId);
	}

}
