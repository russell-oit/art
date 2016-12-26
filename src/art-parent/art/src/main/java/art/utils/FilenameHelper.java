/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
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
	 * Returns a file name to be used for the given report
	 *
	 * @param report the report
	 * @return a file name to be used for the given report
	 */
	public String getFileName(Report report) {
		return getFileName(report, null, null);
	}

	/**
	 * Returns a file name to be used for the given job
	 *
	 * @param job the job
	 * @return a file name to be used for the given job
	 */
	public String getFileName(Job job) {
		return getFileName(job.getReport(), job, null);
	}

	/**
	 * Returns a file name to be used for the given job
	 *
	 * @param job the job
	 * @param burstId the burst id for the job
	 * @return a file name to be used for the given job
	 */
	public String getFileName(Job job, String burstId) {
		return getFileName(job.getReport(), job, burstId);
	}

	/**
	 * Returns a file name to be used for the given report or job
	 *
	 * @param report the report, not null
	 * @param job the job, may be null
	 * @param burstId the burst id for the job, may be null
	 * @return a file name to be used for the given report or job
	 */
	private String getFileName(Report report, Job job, String burstId) {
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
		String random = RandomStringUtils.randomAlphanumeric(RANDOM_CHARACTER_COUNT);

		String fileName = namePart + "-" + timestamp + "-" + random
				+ "-" + reportId + "-" + jobId;

		fileName = ArtUtils.cleanFileName(fileName);

		return fileName;
	}
}
