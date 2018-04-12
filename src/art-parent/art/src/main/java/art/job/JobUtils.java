/*
 * ART. A Reporting Tool.
 * Copyright (C) 2018 Enrico Liboni <eliboni@users.sf.net>
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
package art.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Calendar;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.spi.OperableTrigger;

/**
 * Provides utility methods for working with jobs
 *
 * @author Timothy Anyona
 */
public class JobUtils {

	/**
	 * Returns the next fire time of a group of triggers
	 * 
	 * @param triggers the triggers
	 * @param scheduler the quartz scheduler, not null
	 * @return the next fire time of a group of triggers
	 * @throws SchedulerException 
	 */
	public static Date getNextFireTime(List<Trigger> triggers, Scheduler scheduler)
			throws SchedulerException {
		
		if (CollectionUtils.isEmpty(triggers)) {
			return null;
		}

		Objects.requireNonNull(scheduler, "scheduler must not be null");

		Date nextRunDate = null;
		List<Date> nextRunDates = new ArrayList<>();
		Date now = new Date();
		for (Trigger trigger : triggers) {
			Date tempNextRunDate = null;
			String calendarName = trigger.getCalendarName();
			Calendar calendar = null;
			if (StringUtils.isNotBlank(calendarName)) {
				calendar = scheduler.getCalendar(calendarName);
			}
			if (calendar == null) {
				tempNextRunDate = trigger.getFireTimeAfter(now);
			} else {
				List<Date> nextDates = TriggerUtils.computeFireTimes((OperableTrigger) trigger, calendar, 1);
				if (CollectionUtils.isNotEmpty(nextDates)) {
					tempNextRunDate = nextDates.get(0);
				}
			}
			if (tempNextRunDate != null) {
				nextRunDates.add(tempNextRunDate);
			}
		}

		//https://stackoverflow.com/questions/39791318/how-to-get-the-earliest-date-of-a-list-in-java
		if (CollectionUtils.isNotEmpty(nextRunDates)) {
			nextRunDate = Collections.min(nextRunDates);
		}
		
		return nextRunDate;
	}

}
