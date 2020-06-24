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
package art.jobrunners;

import art.cache.CacheHelper;
import art.job.Job;
import art.job.JobService;
import art.user.User;
import art.user.UserService;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Updates quartz schedules
 *
 * @author Timothy Anyona
 */
@Component
public class UpdateQuartzSchedulesJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(UpdateQuartzSchedulesJob.class);

	@Autowired
	private JobService jobService;

	@Autowired
	private UserService userService;

	@Autowired
	private CacheHelper cacheHelper;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//https://stackoverflow.com/questions/4258313/how-to-use-autowired-in-a-quartz-job
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		try {
			JobDataMap dataMap = context.getMergedJobDataMap();
			//must use containsKey() where key may not be there, otherwise getInt() will throw an exception
			int scheduleId = 0;
			if (dataMap.containsKey("scheduleId")) {
				scheduleId = dataMap.getInt("scheduleId");
			}
			int holidayId = 0;
			if (dataMap.containsKey("holidayId")) {
				holidayId = dataMap.getInt("holidayId");
			}
			int userId = dataMap.getInt("userId");

			if (scheduleId > 0) {
				List<Job> jobs = jobService.getJobsWithSchedule(scheduleId);
				if (CollectionUtils.isNotEmpty(jobs)) {
					User user = userService.getUser(userId);
					for (Job job : jobs) {
						jobService.processSchedules(job, user);
					}
				}
			}
			
			if (holidayId > 0) {
				List<Job> jobs = jobService.getHolidayJobs(holidayId);
				if (CollectionUtils.isNotEmpty(jobs)) {
					User user = userService.getUser(userId);
					for (Job job : jobs) {
						jobService.processSchedules(job, user);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Error", ex);
		}

		cacheHelper.clearJobs();
	}

}
