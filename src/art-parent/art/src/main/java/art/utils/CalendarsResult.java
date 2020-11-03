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
package art.utils;

import java.util.List;
import org.quartz.Calendar;

/**
 * Represents results of processing of quartz calendars
 * 
 * @author Timothy Anyona
 */
public class CalendarsResult {
	
	private Calendar globalCalendar;
	private List<String> calendarNames;

	/**
	 * @return the globalCalendar
	 */
	public Calendar getGlobalCalendar() {
		return globalCalendar;
	}

	/**
	 * @param globalCalendar the globalCalendar to set
	 */
	public void setGlobalCalendar(Calendar globalCalendar) {
		this.globalCalendar = globalCalendar;
	}

	/**
	 * @return the calendarNames
	 */
	public List<String> getCalendarNames() {
		return calendarNames;
	}

	/**
	 * @param calendarNames the calendarNames to set
	 */
	public void setCalendarNames(List<String> calendarNames) {
		this.calendarNames = calendarNames;
	}
	
}
