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
package art.runreport;

import art.job.Job;
import art.report.Report;
import art.user.User;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents some details of a running report
 *
 * @author Timothy Anyona
 */
public class ReportRunDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private Report report;
	private Job job;
	private User user;
	private Date startTime;
	private String runId;
	private String dtAction;

	/**
	 * @return the dtAction
	 */
	public String getDtAction() {
		return dtAction;
	}

	/**
	 * @param dtAction the dtAction to set
	 */
	public void setDtAction(String dtAction) {
		this.dtAction = dtAction;
	}

	/**
	 * @return the report
	 */
	public Report getReport() {
		return report;
	}

	/**
	 * @param report the report to set
	 */
	public void setReport(Report report) {
		this.report = report;
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
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the runId
	 */
	public String getRunId() {
		return runId;
	}

	/**
	 * @param runId the runId to set
	 */
	public void setRunId(String runId) {
		this.runId = runId;
	}

	/**
	 * Returns the id to be used with datatable actions
	 *
	 * @return the report id
	 */
	public String getDtId() {
		return runId;
	}

	/**
	 * Returns the name to be used with datatable actions
	 *
	 * @return the report name
	 */
	public String getDtName() {
		String name = report.getName() + " - ";

		String jobName;
		if (job == null) {
			jobName = " ";
		} else {
			jobName = job.getName();
		}

		name += jobName + " - ";

		if (user != null) {
			name += user.getUsername();
		}

		return name;
	}

}
