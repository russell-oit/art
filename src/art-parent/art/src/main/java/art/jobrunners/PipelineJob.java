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
package art.jobrunners;

import art.job.JobService;
import art.pipeline.Pipeline;
import art.pipeline.PipelineService;
import art.user.User;
import art.utils.ArtUtils;
import art.utils.SchedulerUtils;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Runs a pipeline
 *
 * @author Timothy Anyona
 */
@Component
public class PipelineJob implements org.quartz.Job {

	private static final Logger logger = LoggerFactory.getLogger(PipelineJob.class);

	@Autowired
	private JobService jobService;

	@Autowired
	private PipelineService pipelineService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//https://stackoverflow.com/questions/4258313/how-to-use-autowired-in-a-quartz-job
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		JobDataMap dataMap = context.getMergedJobDataMap();

		int pipelineId = dataMap.getInt("pipelineId");

		try {
			Pipeline pipeline = pipelineService.getPipeline(pipelineId);
			if (pipeline == null) {
				logger.warn("Pipeline not found. Pipeline Id {}", pipelineId);
			} else {
				String serial = pipeline.getSerial();
				String[] serialArray = StringUtils.split(serial, ",");
				if (serialArray != null && serialArray.length > 0) {
					String firstJobIdString = serialArray[0].trim();
					int firstJobId = Integer.parseInt(firstJobIdString);
					scheduleTempJob(firstJobId, serial);
				}
			}
		} catch (Exception ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Schedules a run job or run later job
	 *
	 * @param jobId the job id
	 * @param serial the serial jobs to run
	 * @throws SchedulerException
	 */
	private void scheduleTempJob(Integer jobId, String serial)
			throws SchedulerException {

		logger.debug("Entering scheduleTempJob: jobId={},"
				+ " serial='{}'", jobId, serial);

		String runId = jobId + "-" + ArtUtils.getUniqueId();

		JobDetail tempJob = JobBuilder.newJob(ReportJob.class)
				.withIdentity("tempJob-" + runId, "tempJobGroup")
				.usingJobData("jobId", jobId)
				.usingJobData("serial", serial)
				.usingJobData("tempJob", Boolean.TRUE)
				.build();

		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
				.withIdentity("tempTrigger-" + runId, "tempTriggerGroup");

		//create SimpleTrigger that will fire once, immediately
		triggerBuilder.startNow();

		Trigger tempTrigger = triggerBuilder.build();

		Scheduler scheduler = SchedulerUtils.getScheduler();
		scheduler.scheduleJob(tempJob, tempTrigger);
	}
}
