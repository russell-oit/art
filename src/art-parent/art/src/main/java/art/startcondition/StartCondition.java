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
package art.startcondition;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a start condition
 * 
 * @author Timothy Anyona
 */
public class StartCondition implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int startConditionId;
	private String name;
	private String description;
	private int retryDelayMins = 10;
	private int retryAttempts = 3;
	private String condition;
	private Date creationDate;
	private String createdBy;
	private Date updateDate;
	private String updatedBy;

	/**
	 * @return the startConditionId
	 */
	public int getStartConditionId() {
		return startConditionId;
	}

	/**
	 * @param startConditionId the startConditionId to set
	 */
	public void setStartConditionId(int startConditionId) {
		this.startConditionId = startConditionId;
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
	 * @return the retryDelayMins
	 */
	public int getRetryDelayMins() {
		return retryDelayMins;
	}

	/**
	 * @param retryDelayMins the retryDelayMins to set
	 */
	public void setRetryDelayMins(int retryDelayMins) {
		this.retryDelayMins = retryDelayMins;
	}

	/**
	 * @return the retryAttempts
	 */
	public int getRetryAttempts() {
		return retryAttempts;
	}

	/**
	 * @param retryAttempts the retryAttempts to set
	 */
	public void setRetryAttempts(int retryAttempts) {
		this.retryAttempts = retryAttempts;
	}

	/**
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * @param condition the condition to set
	 */
	public void setCondition(String condition) {
		this.condition = condition;
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
		int hash = 3;
		hash = 97 * hash + this.startConditionId;
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
		final StartCondition other = (StartCondition) obj;
		if (this.startConditionId != other.startConditionId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "StartCondition{" + "name=" + name + '}';
	}

}
