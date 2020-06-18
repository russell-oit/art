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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
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
					startSerialPipeline(firstJobId, finalSerial, pipeline.getPipelineId());
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

		if (StringUtils.containsIgnoreCase(serial, "all")) {
			List<Integer> allJobIds = jobService.getAllJobIds();
			Collections.sort(allJobIds);
			return StringUtils.join(allJobIds, ",");
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
				String[] rangeStringArray = IntStream.rangeClosed(startInt, endInt).mapToObj(String::valueOf).toArray(String[]::new);
				Collections.addAll(serialList, rangeStringArray);
			} else {
				serialList.add(part);
			}
		}

		String finalSerial = StringUtils.join(serialList, ",");

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
