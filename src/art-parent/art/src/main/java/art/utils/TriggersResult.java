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
import java.util.Set;
import org.quartz.Trigger;

/**
 * Represents results of processing of quartz trigger information
 * 
 * @author Timothy Anyona
 */
public class TriggersResult {
	
	private Set<Trigger> triggers;
	private List<String> calendarNames;

	/**
	 * @return the triggers
	 */
	public Set<Trigger> getTriggers() {
		return triggers;
	}

	/**
	 * @param triggers the triggers to set
	 */
	public void setTriggers(Set<Trigger> triggers) {
		this.triggers = triggers;
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
