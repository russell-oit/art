/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.utils;

import art.job.Job;
import art.report.Report;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Provides methods for generating file names for use with report output
 *
 * @author Timothy Anyona
 */
public class FilenameHelper {

	/**
	 * Returns the base file name (file name before extension) to be used for
	 * the given report
	 *
	 * @param report the report
	 * @return the base file name to be used for the given report
	 */
	public String getBaseFilename(Report report) {
		return getBaseFilename(report, null, null);
	}

	/**
	 * Returns the base file name (file name before extension) to be used for
	 * the given job
	 *
	 * @param job the job
	 * @return the base file name to be used for the given job
	 */
	public String getBaseFilename(Job job) {
		return getBaseFilename(job.getReport(), job, null);
	}

	/**
	 * Returns the base file name (file name before extension) to be used for
	 * the given job
	 *
	 * @param job the job
	 * @param burstId the burst id for the job
	 * @return the base file name to be used for the given job
	 */
	public String getBaseFilename(Job job, String burstId) {
		return getBaseFilename(job.getReport(), job, burstId);
	}

	/**
	 * Returns the base file name (file name before extension) to be used for
	 * the given report or job
	 *
	 * @param report the report, not null
	 * @param job the job, may be null
	 * @param burstId the burst id for the job, may be null
	 * @return the base file name to be used for the given report or job
	 */
	private String getBaseFilename(Report report, Job job, String burstId) {
		Objects.requireNonNull(report, "report must not be null");

		int jobId;
		String namePart;

		if (job == null) {
			jobId = 0;
			namePart = report.getName();
		} else {
			jobId = job.getJobId();
			namePart = job.getName();
			if (StringUtils.isBlank(namePart)) {
				namePart = report.getName();
			}
		}

		if (burstId != null) {
			namePart = namePart + "-BurstId-" + burstId;
		}

		int reportId = report.getReportId();
		String timestamp = ArtUtils.fileNameDateFormatter.format(new Date());
		final int RANDOM_CHARACTER_COUNT = 5;
		String randomPart = RandomStringUtils.randomAlphanumeric(RANDOM_CHARACTER_COUNT);

		String filename = namePart + "-" + timestamp + "-" + randomPart
				+ "-" + reportId + "-" + jobId;

		String cleanFilename = ArtUtils.cleanBaseFilename(filename);

		return cleanFilename;
	}
}
