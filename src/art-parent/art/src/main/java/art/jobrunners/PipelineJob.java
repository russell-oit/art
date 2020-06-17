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

import art.pipeline.Pipeline;
import art.pipeline.PipelineService;
import art.utils.ArtUtils;
import art.utils.SchedulerUtils;
import edu.emory.mathcs.backport.java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
				String finalSerial = processSerial(serial);
				String[] serialArray = StringUtils.split(finalSerial, ",");
				if (serialArray != null && serialArray.length > 0) {
					String firstJobIdString = serialArray[0].trim();
					int firstJobId = Integer.parseInt(firstJobIdString);
					scheduleTempJob(firstJobId, finalSerial);
				}
			}
		} catch (Exception ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Returns a serial definition with ranges flattened
	 * 
	 * @param serial the initial serial definition
	 * @return serial definition with ranges flattened
	 */
	private String processSerial(String serial) {
		if (StringUtils.isBlank(serial)) {
			return null;
		}

		String[] serialArray = StringUtils.split(serial, ",");
		List<String> serialList = new ArrayList<>();
		for (String part : serialArray) {
			if (StringUtils.contains(part, "-")) {
				String start = StringUtils.substringBefore(part, "-").trim();
				String end = StringUtils.substringAfter(part, "-").trim();
				int startInt = Integer.parseInt(start);
				int endInt = Integer.parseInt(end);
				//https://stackoverflow.com/questions/42990614/how-to-get-a-range-of-values-in-java
				//https://alvinalexander.com/source-code/how-to-populate-initialize-java-int-array-range/
				//https://stackoverflow.com/questions/3619850/converting-an-int-array-to-a-string-array
				//int[] rangeArray = IntStream.rangeClosed(startInt, endInt).toArray();
				String[] rangeStringArray = IntStream.rangeClosed(startInt, endInt).mapToObj(String::valueOf).toArray(String[]::new);
				Collections.addAll(serialList, rangeStringArray);
				//serialList.addAll(Arrays.asList(rangeStringArray));
			} else {
				serialList.add(part);
			}
		}

		String finalSerial = StringUtils.join(serialList, ",");

		return finalSerial;
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
