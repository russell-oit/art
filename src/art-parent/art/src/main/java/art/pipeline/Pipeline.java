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
package art.pipeline;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a pipeline
 *
 * @author Timothy Anyona
 */
public class Pipeline implements Serializable {

	private static final long serialVersionUID = 1L;

	private int pipelineId;
	private String name;
	private String description;
	private String serial;
	private boolean continueOnError;
	private Date creationDate;
	private Date updateDate;
	private String createdBy;
	private String updatedBy;
	private List<Integer> runningJobs;

	/**
	 * @return the runningJobs
	 */
	public List<Integer> getRunningJobs() {
		return runningJobs;
	}

	/**
	 * @param runningJobs the runningJobs to set
	 */
	public void setRunningJobs(List<Integer> runningJobs) {
		this.runningJobs = runningJobs;
	}

	/**
	 * @return the continueOnError
	 */
	public boolean isContinueOnError() {
		return continueOnError;
	}

	/**
	 * @param continueOnError the continueOnError to set
	 */
	public void setContinueOnError(boolean continueOnError) {
		this.continueOnError = continueOnError;
	}

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the serial
	 */
	public String getSerial() {
		return serial;
	}

	/**
	 * @param serial the serial to set
	 */
	public void setSerial(String serial) {
		this.serial = serial;
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

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + this.pipelineId;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Pipeline other = (Pipeline) obj;
		if (this.pipelineId != other.pipelineId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Pipeline{" + "name=" + name + '}';
	}

	/**
	 * Returns a comma separated string of running jobs for this pipeline
	 * 
	 * @return a comma separated string of running jobs for this pipeline
	 */
	public String getRunningJobsString() {
		return StringUtils.join(runningJobs, ",");
	}

}
