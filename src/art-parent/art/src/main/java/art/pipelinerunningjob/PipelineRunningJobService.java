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
import java.sql.SQLException;
import java.util.ArrayList;
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
	
	/**
	 * Returns the ids of jobs currently running as part of a pipeline
	 * 
	 * @param pipelineId the pipeline id
	 * @return ids of currently running pipeline jobs
	 * @throws SQLException 
	 */
	public List<Integer> getPipelineRunningJobs(int pipelineId) throws SQLException{
		logger.debug("Entering getPipelineRunningJobs");

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
	 * @throws SQLException
	 */
	@CacheEvict(value = "pipelines", allEntries = true)
	public void addPipelineRunningJob(int pipelineId, int jobId) throws SQLException {
		logger.debug("Entering addPipelineRunningJob: pipelineId={}, jobId={}",
				pipelineId, jobId);

		String sql = "INSERT INTO ART_PIPELINE_RUNNING_JOBS"
				+ " (PIPELINE_ID, JOB_ID)"
				+ " VALUES(" + StringUtils.repeat("?", ",", 2) + ")";

		Object[] values = {
			pipelineId,
			jobId
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
