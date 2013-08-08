/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package art.schedule;

import java.io.Serializable;

/**
 * Class to represent a job schedule. Data stored in the ART_JOB_SCHEDULES table
 *
 * @author Timothy Anyona
 */
public class Schedule implements Serializable {

	private static final long serialVersionUID = 1L;
	private String minute = "";
	private String hour = "";
	private String day = "";
	private String month = "";
	private String weekday = "";
	private String scheduleName = "";

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
	 * @return the scheduleName
	 */
	public String getScheduleName() {
		return scheduleName;
	}

	/**
	 * @param scheduleName the scheduleName to set
	 */
	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}
}
