/*
 * ART. A Reporting Tool.
 * Copyright (C) 2021 Enrico Liboni <eliboni@users.sf.net>
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
package art.pipelinerunningjob;

import java.io.Serializable;

/**
 * Represents a pipeline running job
 * 
 * @author Timothy Anyona
 */
public class PipelineRunningJob implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int pipelineId;
	private int jobId;
	private String quartzJobName;
	private boolean parallel;

	/**
	 * @return the pipelineId
	 */
	public int getPipelineId() {
		return pipelineId;
	}

	/**
	 * @param pipelineId the pipelineId to set
	 */
	public void setPipelineId(int pipelineId) {
		this.pipelineId = pipelineId;
	}

	/**
	 * @return the jobId
	 */
	public int getJobId() {
		return jobId;
	}

	/**
	 * @param jobId the jobId to set
	 */
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	/**
	 * @return the quartzJobName
	 */
	public String getQuartzJobName() {
		return quartzJobName;
	}

	/**
	 * @param quartzJobName the quartzJobName to set
	 */
	public void setQuartzJobName(String quartzJobName) {
		this.quartzJobName = quartzJobName;
	}

	/**
	 * @return the parallel
	 */
	public boolean isParallel() {
		return parallel;
	}

	/**
	 * @param parallel the parallel to set
	 */
	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}
	
}
