/*
 * ART. A Jobing Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.jobdestination;

import art.dbutils.DbService;
import art.destination.Destination;
import art.job.Job;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Provides methods for retrieving, updating and deleting job-destination
 * records
 *
 * @author Timothy Anyona
 */
@Service
public class JobDestinationService {

	private static final Logger logger = LoggerFactory.getLogger(JobDestinationService.class);

	private final DbService dbService;

	@Autowired
	public JobDestinationService(DbService dbService) {
		this.dbService = dbService;
	}

	public JobDestinationService() {
		dbService = new DbService();
	}

	/**
	 * Recreates job-destination records for a given job
	 *
	 * @param job the job
	 * @throws SQLException
	 */
	@CacheEvict(value = {"jobs", "destinations"}, allEntries = true)
	public void recreateJobDestinations(Job job) throws SQLException {
		logger.debug("Entering recreateJobDestinations: job={}", job);

		int jobId = job.getJobId();
		
		deleteAllJobDestinationsForJob(jobId);
		addJobDestinations(jobId, job.getDestinations());
	}

	/**
	 * Delete all job-destination records for the given job
	 *
	 * @param jobId the job id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"jobs", "destinations"}, allEntries = true)
	public void deleteAllJobDestinationsForJob(int jobId) throws SQLException {
		logger.debug("Entering deleteAllJobDestinationsForJob: jobId={}", jobId);

		String sql = "DELETE FROM ART_JOB_DESTINATION_MAP WHERE JOB_ID=?";
		dbService.update(sql, jobId);
	}

	/**
	 * Adds job-destination records for the given job
	 *
	 * @param jobId the job id
	 * @param destinations the destinations
	 * @throws SQLException
	 */
	@CacheEvict(value = {"jobs", "destinations"}, allEntries = true)
	public void addJobDestinations(int jobId, List<Destination> destinations) throws SQLException {
		logger.debug("Entering addJobDestinations: jobId={}", jobId);

		if (CollectionUtils.isEmpty(destinations)) {
			return;
		}

		List<Integer> destinationIds = new ArrayList<>();
		for (Destination destination : destinations) {
			destinationIds.add(destination.getDestinationId());
		}

		Integer[] jobs = {jobId};
		String action = "add";
		updateJobDestinations(action, jobs, destinationIds.toArray(new Integer[0]));
	}

	/**
	 * Adds or removes job-destination records
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param jobs job ids
	 * @param destinations destination ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"jobs", "destinations"}, allEntries = true)
	public void updateJobDestinations(String action, Integer[] jobs, Integer[] destinations) throws SQLException {
		logger.debug("Entering updateJobDestinations: action='{}'", action);

		logger.debug("(jobs == null) = {}", jobs == null);
		logger.debug("(destinations == null) = {}", destinations == null);
		if (jobs == null || destinations == null) {
			logger.warn("Update not performed. jobs or destinations is null.");
			return;
		}

		boolean add;
		if (StringUtils.equalsIgnoreCase(action, "add")) {
			add = true;
		} else {
			add = false;
		}

		String sql;

		if (add) {
			sql = "INSERT INTO ART_JOB_DESTINATION_MAP (JOB_ID, DESTINATION_ID) VALUES (?, ?)";
		} else {
			sql = "DELETE FROM ART_JOB_DESTINATION_MAP WHERE JOB_ID=? AND DESTINATION_ID=?";
		}

		String sqlTest = "UPDATE ART_JOB_DESTINATION_MAP SET JOB_ID=? WHERE JOB_ID=? AND DESTINATION_ID=?";
		int affectedRows;
		boolean updateRecord;

		for (Integer jobId : jobs) {
			for (Integer destinationId : destinations) {
				updateRecord = true;
				if (add) {
					//test if record exists. to avoid integrity constraint error
					affectedRows = dbService.update(sqlTest, jobId, jobId, destinationId);
					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRecord = false;
					}
				}
				if (updateRecord) {
					dbService.update(sql, jobId, destinationId);
				}
			}
		}
	}

}
