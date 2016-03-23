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
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author Timothy Anyona
 */
public class FilenameHelper {

	public String getFileName(Report report) {
		return getFileName(report, null);
	}

	public String getFileName(Job job) {
		return getFileName(job.getReport(), job);
	}

	private String getFileName(Report report, Job job) {
		int jobId;
		if (job == null) {
			jobId = 0;
		} else {
			jobId = job.getJobId();
		}

		int reportId = report.getReportId();
		String reportName = report.getName();
		String timestamp = ArtUtils.fileNameDateFormatter.format(new Date());
		final int RANDOM_CHARACTER_COUNT = 5;
		String random = RandomStringUtils.randomAlphanumeric(RANDOM_CHARACTER_COUNT);

		String fileName = reportName + "-" + timestamp + "-" + random
				+ "-" + reportId + "-" + jobId;

		fileName = ArtUtils.cleanFileName(fileName);

		return fileName;

	}

}
