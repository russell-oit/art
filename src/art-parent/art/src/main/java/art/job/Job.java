/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.job;

import art.destination.Destination;
import art.enums.JobType;
import art.ftpserver.FtpServer;
import art.holiday.Holiday;
import art.report.Report;
import art.schedule.Schedule;
import art.smtpserver.SmtpServer;
import art.user.User;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Represent a report job
 *
 * @author Timothy Anyona
 */
public class Job implements Serializable {

	private static final long serialVersionUID = 1L;
	private int jobId;
	private String name;
	private JobType jobType;
	protected Date lastEndDate;
	private Date lastStartDate;
	protected String lastRunDetails;
	private Date nextRunDate;
	protected String lastFileName;
	protected String sharedLastFileName;
	protected String sharedLastRunDetails;
	private Date sharedLastStartDate;
	protected Date sharedLastEndDate;
	private String outputFormat;
	private String mailTo;
	private String mailMessage;
	private String mailSubject;
	private String cachedTableName;
	private String scheduleSecond;
	private String scheduleMinute;
	private String scheduleHour;
	private String scheduleDay;
	private String scheduleMonth;
	private String scheduleWeekday;
	private String scheduleYear;
	private Date creationDate;
	private Date updateDate;
	private Report report;
	private User user;
	private String createdBy;
	private String updatedBy;
	private boolean active;
	private int recipientsReportId;
	private boolean allowSharing;
	private boolean allowSplitting;
	private boolean enableAudit;
	private String mailCc;
	private String mailBcc;
	private int runsToArchive;
	private String mailFrom;
	private Date startDate;
	private Date endDate;
	private String startDateString;
	private String endDateString;
	private String lastRunMessage;
	private String lastEndDateString;
	private String nextRunDateString;
	private int cachedDatasourceId;
	private String batchFile;
	private FtpServer ftpServer;
	private String fixedFileName;
	private String emailTemplate;
	private String extraSchedules;
	private String holidays;
	private String quartzCalendarNames;
	private Schedule schedule;
	private List<Holiday> sharedHolidays;
	private List<Destination> destinations;
	private String subDirectory;
	private SmtpServer smtpServer;
	private String options;
	private String errorNotificationTo;

	/**
	 * @return the errorNotificationTo
	 */
	public String getErrorNotificationTo() {
		return errorNotificationTo;
	}

	/**
	 * @param errorNotificationTo the errorNotificationTo to set
	 */
	public void setErrorNotificationTo(String errorNotificationTo) {
		this.errorNotificationTo = errorNotificationTo;
	}

	/**
	 * @return the options
	 */
	public String getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(String options) {
		this.options = options;
	}

	/**
	 * @return the smtpServer
	 */
	public SmtpServer getSmtpServer() {
		return smtpServer;
	}

	/**
	 * @param smtpServer the smtpServer to set
	 */
	public void setSmtpServer(SmtpServer smtpServer) {
		this.smtpServer = smtpServer;
	}

	/**
	 * @return the subDirectory
	 */
	public String getSubDirectory() {
		return subDirectory;
	}

	/**
	 * @param subDirectory the subDirectory to set
	 */
	public void setSubDirectory(String subDirectory) {
		this.subDirectory = subDirectory;
	}

	/**
	 * @return the destinations
	 */
	public List<Destination> getDestinations() {
		return destinations;
	}

	/**
	 * @param destinations the destinations to set
	 */
	public void setDestinations(List<Destination> destinations) {
		this.destinations = destinations;
	}

	/**
	 * @return the scheduleYear
	 */
	public String getScheduleYear() {
		return scheduleYear;
	}

	/**
	 * @param scheduleYear the scheduleYear to set
	 */
	public void setScheduleYear(String scheduleYear) {
		this.scheduleYear = scheduleYear;
	}

	/**
	 * @return the sharedHolidays
	 */
	public List<Holiday> getSharedHolidays() {
		return sharedHolidays;
	}

	/**
	 * @param sharedHolidays the sharedHolidays to set
	 */
	public void setSharedHolidays(List<Holiday> sharedHolidays) {
		this.sharedHolidays = sharedHolidays;
	}

	/**
	 * @return the schedule
	 */
	public Schedule getSchedule() {
		return schedule;
	}

	/**
	 * @param schedule the schedule to set
	 */
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	/**
	 * @return the scheduleSecond
	 */
	public String getScheduleSecond() {
		return scheduleSecond;
	}

	/**
	 * @param scheduleSecond the scheduleSecond to set
	 */
	public void setScheduleSecond(String scheduleSecond) {
		this.scheduleSecond = scheduleSecond;
	}

	/**
	 * @return the quartzCalendarNames
	 */
	public String getQuartzCalendarNames() {
		return quartzCalendarNames;
	}

	/**
	 * @param quartzCalendarNames the quartzCalendarNames to set
	 */
	public void setQuartzCalendarNames(String quartzCalendarNames) {
		this.quartzCalendarNames = quartzCalendarNames;
	}

	/**
	 * @return the holidays
	 */
	public String getHolidays() {
		return holidays;
	}

	/**
	 * @param holidays the holidays to set
	 */
	public void setHolidays(String holidays) {
		this.holidays = holidays;
	}

	/**
	 * @return the extraSchedules
	 */
	public String getExtraSchedules() {
		return extraSchedules;
	}

	/**
	 * @param extraSchedules the extraSchedules to set
	 */
	public void setExtraSchedules(String extraSchedules) {
		this.extraSchedules = extraSchedules;
	}

	/**
	 * @return the emailTemplate
	 */
	public String getEmailTemplate() {
		return emailTemplate;
	}

	/**
	 * @param emailTemplate the emailTemplate to set
	 */
	public void setEmailTemplate(String emailTemplate) {
		this.emailTemplate = emailTemplate;
	}

	/**
	 * @return the fixedFileName
	 */
	public String getFixedFileName() {
		return fixedFileName;
	}

	/**
	 * @param fixedFileName the fixedFileName to set
	 */
	public void setFixedFileName(String fixedFileName) {
		this.fixedFileName = fixedFileName;
	}

	/**
	 * @return the ftpServer
	 */
	public FtpServer getFtpServer() {
		return ftpServer;
	}

	/**
	 * @param ftpServer the ftpServer to set
	 */
	public void setFtpServer(FtpServer ftpServer) {
		this.ftpServer = ftpServer;
	}

	/**
	 * @return the batchFile
	 */
	public String getBatchFile() {
		return batchFile;
	}

	/**
	 * @param batchFile the batchFile to set
	 */
	public void setBatchFile(String batchFile) {
		this.batchFile = batchFile;
	}

	/**
	 * @return the cachedDatasourceId
	 */
	public int getCachedDatasourceId() {
		return cachedDatasourceId;
	}

	/**
	 * @param cachedDatasourceId the cachedDatasourceId to set
	 */
	public void setCachedDatasourceId(int cachedDatasourceId) {
		this.cachedDatasourceId = cachedDatasourceId;
	}

	/**
	 * @return the lastEndDateString
	 */
	public String getLastEndDateString() {
		return lastEndDateString;
	}

	/**
	 * @param lastEndDateString the lastEndDateString to set
	 */
	public void setLastEndDateString(String lastEndDateString) {
		this.lastEndDateString = lastEndDateString;
	}

	/**
	 * @return the nextRunDateString
	 */
	public String getNextRunDateString() {
		return nextRunDateString;
	}

	/**
	 * @param nextRunDateString the nextRunDateString to set
	 */
	public void setNextRunDateString(String nextRunDateString) {
		this.nextRunDateString = nextRunDateString;
	}

	/**
	 * @return the lastRunMessage
	 */
	public String getLastRunMessage() {
		return lastRunMessage;
	}

	/**
	 * @param lastRunMessage the lastRunMessage to set
	 */
	public void setLastRunMessage(String lastRunMessage) {
		this.lastRunMessage = lastRunMessage;
	}

	/**
	 * @return the startDateString
	 */
	public String getStartDateString() {
		return startDateString;
	}

	/**
	 * @param startDateString the startDateString to set
	 */
	public void setStartDateString(String startDateString) {
		this.startDateString = startDateString;
	}

	/**
	 * @return the endDateString
	 */
	public String getEndDateString() {
		return endDateString;
	}

	/**
	 * @param endDateString the endDateString to set
	 */
	public void setEndDateString(String endDateString) {
		this.endDateString = endDateString;
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
	 * @return the mailFrom
	 */
	public String getMailFrom() {
		return mailFrom;
	}

	/**
	 * @param mailFrom the mailFrom to set
	 */
	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	/**
	 * @return the runsToArchive
	 */
	public int getRunsToArchive() {
		return runsToArchive;
	}

	/**
	 * @param runsToArchive the runsToArchive to set
	 */
	public void setRunsToArchive(int runsToArchive) {
		this.runsToArchive = runsToArchive;
	}

	/**
	 * @return the mailCc
	 */
	public String getMailCc() {
		return mailCc;
	}

	/**
	 * @param mailCc the mailCc to set
	 */
	public void setMailCc(String mailCc) {
		this.mailCc = mailCc;
	}

	/**
	 * @return the mailBcc
	 */
	public String getMailBcc() {
		return mailBcc;
	}

	/**
	 * @param mailBcc the mailBcc to set
	 */
	public void setMailBcc(String mailBcc) {
		this.mailBcc = mailBcc;
	}

	/**
	 * @return the enableAudit
	 */
	public boolean isEnableAudit() {
		return enableAudit;
	}

	/**
	 * @param enableAudit the enableAudit to set
	 */
	public void setEnableAudit(boolean enableAudit) {
		this.enableAudit = enableAudit;
	}

	/**
	 * @return the allowSharing
	 */
	public boolean isAllowSharing() {
		return allowSharing;
	}

	/**
	 * @param allowSharing the allowSharing to set
	 */
	public void setAllowSharing(boolean allowSharing) {
		this.allowSharing = allowSharing;
	}

	/**
	 * @return the allowSplitting
	 */
	public boolean isAllowSplitting() {
		return allowSplitting;
	}

	/**
	 * @param allowSplitting the allowSplitting to set
	 */
	public void setAllowSplitting(boolean allowSplitting) {
		this.allowSplitting = allowSplitting;
	}

	/**
	 * @return the recipientsReportId
	 */
	public int getRecipientsReportId() {
		return recipientsReportId;
	}

	/**
	 * @param recipientsReportId the recipientsReportId to set
	 */
	public void setRecipientsReportId(int recipientsReportId) {
		this.recipientsReportId = recipientsReportId;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the updatedBy
	 */
	public String getUpdatedBy() {
		return updatedBy;
	}

	/**
	 * @param updatedBy the updatedBy to set
	 */
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	/**
	 * Get the value of user
	 *
	 * @return the value of user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Set the value of user
	 *
	 * @param user new value of user
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * Get the value of report
	 *
	 * @return the value of report
	 */
	public Report getReport() {
		return report;
	}

	/**
	 * Set the value of report
	 *
	 * @param report new value of report
	 */
	public void setReport(Report report) {
		this.report = report;
	}

	/**
	 * @return the creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @return the updateDate
	 */
	public Date getUpdateDate() {
		return updateDate;
	}

	/**
	 * @param updateDate the updateDate to set
	 */
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	/**
	 * Get the value of scheduleWeekday
	 *
	 * @return the value of scheduleWeekday
	 */
	public String getScheduleWeekday() {
		return scheduleWeekday;
	}

	/**
	 * Set the value of scheduleWeekday
	 *
	 * @param scheduleWeekday new value of scheduleWeekday
	 */
	public void setScheduleWeekday(String scheduleWeekday) {
		this.scheduleWeekday = scheduleWeekday;
	}

	/**
	 * Get the value of scheduleMonth
	 *
	 * @return the value of scheduleMonth
	 */
	public String getScheduleMonth() {
		return scheduleMonth;
	}

	/**
	 * Set the value of scheduleMonth
	 *
	 * @param scheduleMonth new value of scheduleMonth
	 */
	public void setScheduleMonth(String scheduleMonth) {
		this.scheduleMonth = scheduleMonth;
	}

	/**
	 * Get the value of scheduleDay
	 *
	 * @return the value of scheduleDay
	 */
	public String getScheduleDay() {
		return scheduleDay;
	}

	/**
	 * Set the value of scheduleDay
	 *
	 * @param scheduleDay new value of scheduleDay
	 */
	public void setScheduleDay(String scheduleDay) {
		this.scheduleDay = scheduleDay;
	}

	/**
	 * Get the value of scheduleHour
	 *
	 * @return the value of scheduleHour
	 */
	public String getScheduleHour() {
		return scheduleHour;
	}

	/**
	 * Set the value of scheduleHour
	 *
	 * @param scheduleHour new value of scheduleHour
	 */
	public void setScheduleHour(String scheduleHour) {
		this.scheduleHour = scheduleHour;
	}

	/**
	 * Get the value of scheduleMinute
	 *
	 * @return the value of scheduleMinute
	 */
	public String getScheduleMinute() {
		return scheduleMinute;
	}

	/**
	 * Set the value of scheduleMinute
	 *
	 * @param scheduleMinute new value of scheduleMinute
	 */
	public void setScheduleMinute(String scheduleMinute) {
		this.scheduleMinute = scheduleMinute;
	}

	/**
	 * Get the value of cachedTableName
	 *
	 * @return the value of cachedTableName
	 */
	public String getCachedTableName() {
		return cachedTableName;
	}

	/**
	 * Set the value of cachedTableName
	 *
	 * @param cachedTableName new value of cachedTableName
	 */
	public void setCachedTableName(String cachedTableName) {
		this.cachedTableName = cachedTableName;
	}

	/**
	 * Get the value of mailSubject
	 *
	 * @return the value of mailSubject
	 */
	public String getMailSubject() {
		return mailSubject;
	}

	/**
	 * Set the value of mailSubject
	 *
	 * @param mailSubject new value of mailSubject
	 */
	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
	}

	/**
	 * Get the value of mailMessage
	 *
	 * @return the value of mailMessage
	 */
	public String getMailMessage() {
		return mailMessage;
	}

	/**
	 * Set the value of mailMessage
	 *
	 * @param mailMessage new value of mailMessage
	 */
	public void setMailMessage(String mailMessage) {
		this.mailMessage = mailMessage;
	}

	/**
	 * Get the value of mailTo
	 *
	 * @return the value of mailTo
	 */
	public String getMailTo() {
		return mailTo;
	}

	/**
	 * Set the value of mailTo
	 *
	 * @param mailTo new value of mailTo
	 */
	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	/**
	 * Get the value of outputFormat
	 *
	 * @return the value of outputFormat
	 */
	public String getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Set the value of outputFormat
	 *
	 * @param outputFormat new value of outputFormat
	 */
	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	/**
	 * Get the value of sharedLastEndDate
	 *
	 * @return the value of sharedLastEndDate
	 */
	public Date getSharedLastEndDate() {
		return sharedLastEndDate;
	}

	/**
	 * Set the value of sharedLastEndDate
	 *
	 * @param sharedLastEndDate new value of sharedLastEndDate
	 */
	public void setSharedLastEndDate(Date sharedLastEndDate) {
		this.sharedLastEndDate = sharedLastEndDate;
	}

	/**
	 * Get the value of sharedLastRunDetails
	 *
	 * @return the value of sharedLastRunDetails
	 */
	public String getSharedLastRunDetails() {
		return sharedLastRunDetails;
	}

	/**
	 * Set the value of sharedLastRunDetails
	 *
	 * @param sharedLastRunDetails new value of sharedLastRunDetails
	 */
	public void setSharedLastRunDetails(String sharedLastRunDetails) {
		this.sharedLastRunDetails = sharedLastRunDetails;
	}

	/**
	 * Get the value of sharedLastStartDate
	 *
	 * @return the value of sharedLastStartDate
	 */
	public Date getSharedLastStartDate() {
		return sharedLastStartDate;
	}

	/**
	 * Set the value of sharedLastStartDate
	 *
	 * @param sharedLastStartDate new value of sharedLastStartDate
	 */
	public void setSharedLastStartDate(Date sharedLastStartDate) {
		this.sharedLastStartDate = sharedLastStartDate;
	}

	/**
	 * Get the value of sharedLastFileName
	 *
	 * @return the value of sharedLastFileName
	 */
	public String getSharedLastFileName() {
		return sharedLastFileName;
	}

	/**
	 * Set the value of sharedLastFileName
	 *
	 * @param sharedLastFileName new value of sharedLastFileName
	 */
	public void setSharedLastFileName(String sharedLastFileName) {
		this.sharedLastFileName = sharedLastFileName;
	}

	/**
	 * Get the value of lastFileName
	 *
	 * @return the value of lastFileName
	 */
	public String getLastFileName() {
		return lastFileName;
	}

	/**
	 * Set the value of lastFileName
	 *
	 * @param lastFileName new value of lastFileName
	 */
	public void setLastFileName(String lastFileName) {
		this.lastFileName = lastFileName;
	}

	/**
	 * Get the value of nextRunDate
	 *
	 * @return the value of nextRunDate
	 */
	public Date getNextRunDate() {
		return nextRunDate;
	}

	/**
	 * Set the value of nextRunDate
	 *
	 * @param nextRunDate new value of nextRunDate
	 */
	public void setNextRunDate(Date nextRunDate) {
		this.nextRunDate = nextRunDate;
	}

	/**
	 * Get the value of lastRunDetails
	 *
	 * @return the value of lastRunDetails
	 */
	public String getLastRunDetails() {
		return lastRunDetails;
	}

	/**
	 * Set the value of lastRunDetails
	 *
	 * @param lastRunDetails new value of lastRunDetails
	 */
	public void setLastRunDetails(String lastRunDetails) {
		this.lastRunDetails = lastRunDetails;
	}

	/**
	 * Get the value of lastStartDate
	 *
	 * @return the value of lastStartDate
	 */
	public Date getLastStartDate() {
		return lastStartDate;
	}

	/**
	 * Set the value of lastStartDate
	 *
	 * @param lastStartDate new value of lastStartDate
	 */
	public void setLastStartDate(Date lastStartDate) {
		this.lastStartDate = lastStartDate;
	}

	/**
	 * Get the value of lastEndDate
	 *
	 * @return the value of lastEndDate
	 */
	public Date getLastEndDate() {
		return lastEndDate;
	}

	/**
	 * Set the value of lastEndDate
	 *
	 * @param lastEndDate new value of lastEndDate
	 */
	public void setLastEndDate(Date lastEndDate) {
		this.lastEndDate = lastEndDate;
	}

	/**
	 * Get the value of jobType
	 *
	 * @return the value of jobType
	 */
	public JobType getJobType() {
		return jobType;
	}

	/**
	 * Set the value of jobType
	 *
	 * @param jobType new value of jobType
	 */
	public void setJobType(JobType jobType) {
		this.jobType = jobType;
	}

	/**
	 * Get the value of name
	 *
	 * @return the value of name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the value of name
	 *
	 * @param name new value of name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the value of jobId
	 *
	 * @return the value of jobId
	 */
	public int getJobId() {
		return jobId;
	}

	/**
	 * Set the value of jobId
	 *
	 * @param jobId new value of jobId
	 */
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 83 * hash + this.jobId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Job other = (Job) obj;
		if (this.jobId != other.jobId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Job{" + "name=" + name + '}';
	}

	public boolean isSplitJob() {
		if (report != null && report.isUsesRules() && allowSplitting) {
			return true;
		} else {
			return false;
		}
	}
}
