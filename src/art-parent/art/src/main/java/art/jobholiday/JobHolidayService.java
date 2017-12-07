/*
 * ART. A Reporting Tool.
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
package art.jobholiday;

import art.dbutils.DbService;
import art.holiday.Holiday;
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
 * Provides methods for retrieving, updating and deleting job-holiday
 * records
 *
 * @author Timothy Anyona
 */
@Service
public class JobHolidayService {

	private static final Logger logger = LoggerFactory.getLogger(JobHolidayService.class);

	private final DbService dbService;

	@Autowired
	public JobHolidayService(DbService dbService) {
		this.dbService = dbService;
	}

	public JobHolidayService() {
		dbService = new DbService();
	}

	/**
	 * Recreates job-holiday records for a given job
	 *
	 * @param job the job
	 * @throws SQLException
	 */
	@CacheEvict(value = {"jobs", "holidays"}, allEntries = true)
	public void recreateJobHolidays(Job job) throws SQLException {
		logger.debug("Entering recreateJobHolidays: job={}", job);

		int jobId = job.getJobId();
		
		deleteAllJobHolidaysForJob(jobId);
		addJobHolidays(jobId, job.getSharedHolidays());
	}

	/**
	 * Delete all job-holiday records for the given job
	 *
	 * @param jobId the job id
	 * @throws SQLException
	 */
	@CacheEvict(value = {"jobs", "holidays"}, allEntries = true)
	public void deleteAllJobHolidaysForJob(int jobId) throws SQLException {
		logger.debug("Entering deleteAllJobHolidaysForJob: jobId={}", jobId);

		String sql = "DELETE FROM ART_JOB_HOLIDAY_MAP WHERE JOB_ID=?";
		dbService.update(sql, jobId);
	}

	/**
	 * Adds job-holiday records for the given job
	 *
	 * @param jobId the job id
	 * @param holidays the holidays
	 * @throws SQLException
	 */
	@CacheEvict(value = {"jobs", "holidays"}, allEntries = true)
	public void addJobHolidays(int jobId, List<Holiday> holidays) throws SQLException {
		logger.debug("Entering addJobHolidays: jobId={}", jobId);

		if (CollectionUtils.isEmpty(holidays)) {
			return;
		}

		List<Integer> holidayIds = new ArrayList<>();
		for (Holiday holiday : holidays) {
			holidayIds.add(holiday.getHolidayId());
		}

		Integer[] jobs = {jobId};
		String action = "add";
		updateJobHolidays(action, jobs, holidayIds.toArray(new Integer[0]));
	}

	/**
	 * Adds or removes job-holiday records
	 *
	 * @param action "add" or "remove". anything else will be treated as remove
	 * @param jobs job ids
	 * @param holidays holiday ids
	 * @throws SQLException
	 */
	@CacheEvict(value = {"jobs", "holidays"}, allEntries = true)
	public void updateJobHolidays(String action, Integer[] jobs, Integer[] holidays) throws SQLException {
		logger.debug("Entering updateJobHolidays: action='{}'", action);

		logger.debug("(jobs == null) = {}", jobs == null);
		logger.debug("(holidays == null) = {}", holidays == null);
		if (jobs == null || holidays == null) {
			logger.warn("Update not performed. jobs or holidays is null.");
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
			sql = "INSERT INTO ART_JOB_HOLIDAY_MAP (JOB_ID, HOLIDAY_ID) VALUES (?, ?)";
		} else {
			sql = "DELETE FROM ART_JOB_HOLIDAY_MAP WHERE JOB_ID=? AND HOLIDAY_ID=?";
		}

		String sqlTest = "UPDATE ART_JOB_HOLIDAY_MAP SET JOB_ID=? WHERE JOB_ID=? AND HOLIDAY_ID=?";
		int affectedRows;
		boolean updateRecord;

		for (Integer jobId : jobs) {
			for (Integer holidayId : holidays) {
				updateRecord = true;
				if (add) {
					//test if record exists. to avoid integrity constraint error
					affectedRows = dbService.update(sqlTest, jobId, jobId, holidayId);
					if (affectedRows > 0) {
						//record exists. don't attempt a reinsert.
						updateRecord = false;
					}
				}
				if (updateRecord) {
					dbService.update(sql, jobId, holidayId);
				}
			}
		}
	}

}
