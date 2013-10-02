package art.job;

import java.io.Serializable;
import java.util.Date;

/**
 * Class to represent an art job. Data stored in the ART_JOBS table
 *
 * @author Timothy Anyona
 */
public class Job implements Serializable {

	private static final long serialVersionUID = 1L;
	private int jobId;
	private String jobName;
	private int jobType;
	private Date lastEndDate;
	private Date lastStartDate;
	private String lastRunDetails;
	private Date nextRunDate;
	private String lastFileName;
	private String usesRules;
	private String allowSplitting;
	private String queryName;
	private String sharedLastFileName;
	private String sharedLastRunDetails;
	private Date sharedLastStartDate;
	private Date sharedLastEndDate;

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
	 * Get the value of queryName
	 *
	 * @return the value of queryName
	 */
	public String getQueryName() {
		return queryName;
	}

	/**
	 * Set the value of queryName
	 *
	 * @param queryName new value of queryName
	 */
	public void setQueryName(String queryName) {
		this.queryName = queryName;
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
	public int getJobType() {
		return jobType;
	}

	/**
	 * Set the value of jobType
	 *
	 * @param jobType new value of jobType
	 */
	public void setJobType(int jobType) {
		this.jobType = jobType;
	}

	/**
	 * Get the value of jobName
	 *
	 * @return the value of jobName
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * Set the value of jobName
	 *
	 * @param jobName new value of jobName
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
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
}
