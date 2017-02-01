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
package art.archive;

import art.job.Job;
import art.user.User;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents a job archive
 * 
 * @author Timothy Anyona
 */
public class Archive implements Serializable {

	private static final long serialVersionUID = 1L;
	private String archiveId;
	private Job job;
	private User user;
	private String fileName;
	private Date startDate;
	private Date endDate;
	private String jobSharedStatus;

	/**
	 * @return the archiveId
	 */
	public String getArchiveId() {
		return archiveId;
	}

	/**
	 * @param archiveId the archiveId to set
	 */
	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}

	/**
	 * @return the job
	 */
	public Job getJob() {
		return job;
	}

	/**
	 * @param job the job to set
	 */
	public void setJob(Job job) {
		this.job = job;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the jobSharedStatus
	 */
	public String getJobSharedStatus() {
		return jobSharedStatus;
	}

	/**
	 * @param jobSharedStatus the jobSharedStatus to set
	 */
	public void setJobSharedStatus(String jobSharedStatus) {
		this.jobSharedStatus = jobSharedStatus;
	}
}
