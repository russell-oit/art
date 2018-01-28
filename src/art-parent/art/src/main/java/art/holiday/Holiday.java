/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more definition.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.holiday;

import com.univocity.parsers.annotations.Format;
import com.univocity.parsers.annotations.Parsed;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents a schedule holiday
 * 
 * @author Timothy Anyona
 */
public class Holiday implements Serializable {
	
	private static final long serialVersionUID = 1L;
	@Parsed
	private int holidayId;
	@Parsed
	private String name;
	@Parsed
	private String description;
	@Parsed
	private String definition;
	@Format(formats = "yyyy-MM-dd HH:mm:ss.SSS")
	@Parsed
	private Date creationDate;
	@Parsed
	private String createdBy;
	@Format(formats = "yyyy-MM-dd HH:mm:ss.SSS")
	@Parsed
	private Date updateDate;
	@Parsed
	private String updatedBy;

	/**
	 * @return the holidayId
	 */
	public int getHolidayId() {
		return holidayId;
	}

	/**
	 * @param holidayId the holidayId to set
	 */
	public void setHolidayId(int holidayId) {
		this.holidayId = holidayId;
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
	 * @return the definition
	 */
	public String getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
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
		int hash = 7;
		hash = 53 * hash + this.holidayId;
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
		final Holiday other = (Holiday) obj;
		if (this.holidayId != other.holidayId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Holiday{" + "name=" + name + '}';
	}
	
}
