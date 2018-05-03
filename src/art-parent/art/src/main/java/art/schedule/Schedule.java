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
package art.schedule;

import art.holiday.Holiday;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.univocity.parsers.annotations.Parsed;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Represents a schedule
 *
 * @author Timothy Anyona
 */
public class Schedule implements Serializable {

	private static final long serialVersionUID = 1L;
	@Parsed
	private int scheduleId;
	@Parsed
	private String name;
	@Parsed
	private String description;
	@Parsed
	private String second;
	@Parsed
	private String minute;
	@Parsed
	private String hour;
	@Parsed
	private String day;
	@Parsed
	private String month;
	@Parsed
	private String weekday;
	@Parsed
	private String year;
	@Parsed
	private String timeZone;
	private Date creationDate;
	private Date updateDate;
	private String createdBy;
	private String updatedBy;
	@Parsed
	private String extraSchedules;
	@Parsed
	private String holidays;
	private List<Holiday> sharedHolidays;

	/**
	 * @return the timeZone
	 */
	public String getTimeZone() {
		return timeZone;
	}

	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	/**
	 * @return the year
	 */
	public String getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(String year) {
		this.year = year;
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
	 * @return the second
	 */
	public String getSecond() {
		return second;
	}

	/**
	 * @param second the second to set
	 */
	public void setSecond(String second) {
		this.second = second;
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
	 * Get the value of description
	 *
	 * @return the value of description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Set the value of description
	 *
	 * @param description new value of description
	 */
	public void setDescription(String description) {
		this.description = description;
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
	 * Get the value of scheduleId
	 *
	 * @return the value of scheduleId
	 */
	public int getScheduleId() {
		return scheduleId;
	}

	/**
	 * Set the value of scheduleId
	 *
	 * @param scheduleId new value of scheduleId
	 */
	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}


	/**
	 * @return the minute
	 */
	public String getMinute() {
		return minute;
	}

	/**
	 * @param minute the minute to set
	 */
	public void setMinute(String minute) {
		this.minute = minute;
	}

	/**
	 * @return the hour
	 */
	public String getHour() {
		return hour;
	}

	/**
	 * @param hour the hour to set
	 */
	public void setHour(String hour) {
		this.hour = hour;
	}

	/**
	 * @return the day
	 */
	public String getDay() {
		return day;
	}

	/**
	 * @param day the day to set
	 */
	public void setDay(String day) {
		this.day = day;
	}

	/**
	 * @return the month
	 */
	public String getMonth() {
		return month;
	}

	/**
	 * @param month the month to set
	 */
	public void setMonth(String month) {
		this.month = month;
	}

	/**
	 * @return the weekday
	 */
	public String getWeekday() {
		return weekday;
	}

	/**
	 * @param weekday the weekday to set
	 */
	public void setWeekday(String weekday) {
		this.weekday = weekday;
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

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 73 * hash + this.scheduleId;
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
		final Schedule other = (Schedule) obj;
		if (this.scheduleId != other.scheduleId) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Schedule{" + "name=" + name + '}';
	}
	
	/**
	 * Returns the cron string for the main schedule fields
	 * 
	 * @return the cron string for the main schedule fields
	 */
	@JsonIgnore
	public String getMainScheduleCronString(){
		String cronString = second + " " + minute
					+ " " + hour + " " + day
					+ " " + month + " " + weekday
					+ " " + year;
		
		return cronString;
	}
	
}
