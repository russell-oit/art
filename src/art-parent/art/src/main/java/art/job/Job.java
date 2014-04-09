package art.job;

import art.enums.JobType;
import art.report.Report;
import art.user.User;
import java.io.Serializable;
import java.util.Date;

/**
 * Class to represent an art job
 *
 * @author Timothy Anyona
 */
public class Job implements Serializable {

	private static final long serialVersionUID = 1L;
	private int jobId;
	private String name;
	private JobType jobType;
	private Date lastEndDate;
	private Date lastStartDate;
	private String lastRunDetails;
	private Date nextRunDate;
	private String lastFileName;
	private String usesRules;
	private String allowSplitting;
	private String sharedLastFileName;
	private String sharedLastRunDetails;
	private Date sharedLastStartDate;
	private Date sharedLastEndDate;
	private String outputFormat;
	private String mailTo;
	private String mailMessage;
	private String mailSubject;
	private String cachedTableName;
	private String scheduleMinute;
	private String scheduleHour;
	private String scheduleDay;
	private String scheduleMonth;
	private String scheduleWeekday;
	private Date creationDate;
	private Date updateDate;
	private Report report;
	private User user;

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
	 * Get the value of allowSplitting
	 *
	 * @return the value of allowSplitting
	 */
	public String getAllowSplitting() {
		return allowSplitting;
	}

	/**
	 * Set the value of allowSplitting
	 *
	 * @param allowSplitting new value of allowSplitting
	 */
	public void setAllowSplitting(String allowSplitting) {
		this.allowSplitting = allowSplitting;
	}

	/**
	 * Get the value of usesRules
	 *
	 * @return the value of usesRules
	 */
	public String getUsesRules() {
		return usesRules;
	}

	/**
	 * Set the value of usesRules
	 *
	 * @param usesRules new value of usesRules
	 */
	public void setUsesRules(String usesRules) {
		this.usesRules = usesRules;
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
}
