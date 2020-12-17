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
import art.startcondition.StartCondition;
import art.startcondition.StartConditionHelper;
import art.utils.ArtUtils;
import art.utils.SchedulerUtils;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DateBuilder;
import static org.quartz.DateBuilder.futureDate;
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

	@Autowired
	private JobService jobService;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//https://stackoverflow.com/questions/4258313/how-to-use-autowired-in-a-quartz-job
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

		JobDataMap dataMap = context.getMergedJobDataMap();

		try {
			int pipelineId = dataMap.getInt("pipelineId");

			Pipeline pipeline = pipelineService.getPipeline(pipelineId);
			if (pipeline == null) {
				logger.warn("Pipeline not found. Pipeline Id {}", pipelineId);
			} else {
				boolean tempJob = dataMap.getBooleanValue("tempJob");
				boolean retryJob = dataMap.getBooleanValue("retryJob");

				Integer retryAttemptsLeft = null;
				if (retryJob) {
					retryAttemptsLeft = dataMap.getInt("retryAttemptsLeft");
				}

				boolean runPipeline = true;

				if (!tempJob) {
					StartCondition startCondition = pipeline.getStartCondition();
					if (startCondition != null) {
						StartConditionHelper startConditionHelper = new StartConditionHelper();
						boolean startConditionOk = startConditionHelper.evaluate(startCondition.getCondition());
						if (!startConditionOk) {
							runPipeline = false;

							int retryDelayMins = startCondition.getRetryDelayMins();
							int retryAttempts = startCondition.getRetryAttempts();
							if (retryAttemptsLeft == null) {
								retryAttemptsLeft = retryAttempts;
							}
							if (retryAttemptsLeft > 0 && retryDelayMins > 0) {
								retryAttemptsLeft--;
								String retryId = pipelineId + "-" + ArtUtils.getUniqueId();
								JobDetail quartzJob = JobBuilder.newJob(ReportJob.class)
										.withIdentity("retryJob-" + retryId, "retryJobGroup")
										.usingJobData("pipelineId", pipelineId)
										.usingJobData("retryJob", true)
										.usingJobData("retryAttemptsLeft", retryAttemptsLeft)
										.build();

								//http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/tutorial-lesson-05.html
								Trigger tempTrigger = TriggerBuilder.newTrigger()
										.withIdentity("retryTrigger-" + retryId, "retryTriggerGroup")
										.startAt(futureDate(retryDelayMins, DateBuilder.IntervalUnit.MINUTE))
										.build();

								Scheduler scheduler = SchedulerUtils.getScheduler();
								scheduler.scheduleJob(quartzJob, tempTrigger);
							}
						}
					}
				}

				if (runPipeline) {
					String serial = pipeline.getSerial();
					String finalSerial = processSerial(serial);
					String[] serialArray = StringUtils.split(finalSerial, ",");
					if (serialArray != null && serialArray.length > 0) {
						String firstJobIdString = serialArray[0].trim();
						int firstJobId = Integer.parseInt(firstJobIdString);
						startSerialPipeline(firstJobId, finalSerial, pipeline.getPipelineId());
					}
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
	 * @throws java.sql.SQLException
	 */
	private String processSerial(String serial) throws SQLException {
		if (StringUtils.isBlank(serial)) {
			return null;
		}

		String[] serialArray = StringUtils.split(serial, ",");
		//https://stackoverflow.com/questions/1128723/how-do-i-determine-whether-an-array-contains-a-particular-value-in-java
		if (Arrays.stream(serialArray).anyMatch(s -> StringUtils.equalsIgnoreCase(s.trim(), "all"))) {
			List<Integer> allJobIds = jobService.getAllJobIds();
			return StringUtils.join(allJobIds, ",");
		}

		List<String> finalList = new ArrayList<>();
		for (String part : serialArray) {
			part = StringUtils.trimToEmpty(part);
			if (StringUtils.isBlank(part)) {
				continue;
			}

			if (StringUtils.startsWith(part, "schedule:")) {
				String scheduleName = StringUtils.substringAfter(part, "schedule:").trim();
				List<Integer> ids = jobService.getJobIdsWithSchedule(scheduleName);
				//https://www.techiedelight.com/convert-list-integer-list-string-java/
				List<String> stringList = ids.stream().map(String::valueOf).collect(Collectors.toList());
				finalList.addAll(stringList);
			} else if (StringUtils.startsWith(part, "reportGroup:")) {
				String reportGroupNames = StringUtils.substringAfter(part, "reportGroup:");
				String[] reportGroupNamesArray = StringUtils.split(reportGroupNames, ";");
				String[] trimmedReportGroupNamesArray = Arrays.stream(reportGroupNamesArray).map(String::trim).toArray(String[]::new);
				List<Integer> ids = jobService.getJobIdsWithReportGroups(trimmedReportGroupNamesArray);
				List<String> stringList = ids.stream().map(String::valueOf).collect(Collectors.toList());
				finalList.addAll(stringList);
			} else if (StringUtils.contains(part, "-")) {
				String start = StringUtils.substringBefore(part, "-").trim();
				String end = StringUtils.substringAfter(part, "-").trim();
				int startInt = Integer.parseInt(start);
				int endInt = Integer.parseInt(end);
				//https://stackoverflow.com/questions/42990614/how-to-get-a-range-of-values-in-java
				//https://alvinalexander.com/source-code/how-to-populate-initialize-java-int-array-range/
				//https://stackoverflow.com/questions/3619850/converting-an-int-array-to-a-string-array
				List<String> rangeList = IntStream.rangeClosed(startInt, endInt).mapToObj(String::valueOf).collect(Collectors.toList());
				finalList.addAll(rangeList);
			} else if (StringUtils.endsWith(part, "+")) {
				String id = StringUtils.substringBefore(part, "+").trim();
				int idInt = Integer.parseInt(id);
				int lastId = jobService.getLastJobId();
				if (lastId <= idInt) {
					finalList.add(id);
				} else {
					List<String> rangeList = IntStream.rangeClosed(idInt, lastId).mapToObj(String::valueOf).collect(Collectors.toList());
					finalList.addAll(rangeList);
				}
			} else {
				finalList.add(part);
			}
		}

		String finalSerial = StringUtils.join(finalList, ",");

		return finalSerial;
	}

	/**
	 * Starts a serial pipeline
	 *
	 * @param jobId the job id
	 * @param serial the serial jobs to run
	 * @param pipelineId the pipeline id
	 * @throws SchedulerException
	 * @throws java.sql.SQLException
	 */
	private void startSerialPipeline(int jobId, String serial, int pipelineId)
			throws SchedulerException, SQLException {

		logger.debug("Entering startSerialPipeline: jobId={},"
				+ " serial='{}', pipelineId={}", jobId, serial, pipelineId);

		pipelineService.uncancelPipeline(pipelineId);
		jobService.scheduleSerialPipelineJob(jobId, serial, pipelineId);
	}
}
