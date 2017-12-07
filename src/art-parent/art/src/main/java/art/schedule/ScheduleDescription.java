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
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.schedule;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents a cron schedule description
 *
 * @author Timothy Anyona
 */
public class ScheduleDescription implements Serializable {

	private static final long serialVersionUID = 1L;
	private String description;
	private Date nextRunDate;
	private String nextRunDateString;

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
	 * @return the nextRunDate
	 */
	public Date getNextRunDate() {
		return nextRunDate;
	}

	/**
	 * @param nextRunDate the nextRunDate to set
	 */
	public void setNextRunDate(Date nextRunDate) {
		this.nextRunDate = nextRunDate;
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
}
