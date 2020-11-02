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

import art.holiday.Holiday;
import art.job.Job;
import art.pipeline.Pipeline;
import art.schedule.Schedule;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Calendar;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import static org.quartz.TriggerKey.triggerKey;
import org.quartz.impl.calendar.CronCalendar;
import org.quartz.impl.triggers.AbstractTrigger;
import org.quartz.impl.triggers.CronTriggerImpl;

/**
 * Provides methods for assisting with quartz schedules
 *
 * @author Timothy Anyona
 */
public class QuartzScheduleHelper {

	/**
	 * Process holiday definitions
	 *
	 * @param job the art job
	 * @param timeZone the time zone to use
	 * @return the list of calendars representing configured holidays
	 * @throws ParseException
	 */
	private List<Calendar> processHolidays(Job job, TimeZone timeZone)
			throws ParseException {

		String holidays;
		Schedule schedule = job.getSchedule();
		if (schedule == null) {
			holidays = job.getHolidays();
		} else {
			holidays = schedule.getHolidays();
		}

		List<Holiday> sharedHolidays;
		if (schedule == null) {
			sharedHolidays = job.getSharedHolidays();
		} else {
			sharedHolidays = schedule.getSharedHolidays();
		}

		return processHolidays(holidays, sharedHolidays, timeZone);
	}

	/**
	 * Process holiday definitions
	 *
	 * @param pipeline the pipeline object
	 * @param timeZone the time zone to use
	 * @return the list of calendars representing configured holidays
	 * @throws ParseException
	 */
	private List<Calendar> processHolidays(Pipeline pipeline, TimeZone timeZone)
			throws ParseException {

		String holidays = null;
		Schedule schedule = pipeline.getSchedule();
		if (schedule != null) {
			holidays = schedule.getHolidays();
		}

		List<Holiday> sharedHolidays = null;
		if (schedule != null) {
			sharedHolidays = schedule.getSharedHolidays();
		}

		return processHolidays(holidays, sharedHolidays, timeZone);
	}

	/**
	 * Process holiday definitions
	 *
	 * @param holidays the string containing holiday definitions
	 * @param sharedHolidays the list of shared holidays
	 * @param timeZone the time zone to use
	 * @return the list of calendars representing configured holidays
	 * @throws ParseException
	 */
	private List<Calendar> processHolidays(String holidays,
			List<Holiday> sharedHolidays, TimeZone timeZone) throws ParseException {

		List<Calendar> calendars = new ArrayList<>();

		List<Calendar> mainCalendars = processHolidayString(holidays, timeZone);

		List<Calendar> nonLabelledCalendars = new ArrayList<>();
		for (Calendar calendar : mainCalendars) {
			if (StringUtils.isBlank(calendar.getDescription())) {
				nonLabelledCalendars.add(calendar);
			} else {
				calendars.add(calendar);
			}
		}

		if (CollectionUtils.isNotEmpty(sharedHolidays)) {
			for (Holiday holiday : sharedHolidays) {
				List<Calendar> sharedCalendars = processHolidayString(holiday.getDefinition(), timeZone);
				for (Calendar calendar : sharedCalendars) {
					if (StringUtils.isBlank(calendar.getDescription())) {
						nonLabelledCalendars.add(calendar);
					} else {
						calendars.add(calendar);
					}
				}
			}
		}

		if (CollectionUtils.isNotEmpty(nonLabelledCalendars)) {
			Calendar finalNonLabelledCalendar = concatenateCalendars(nonLabelledCalendars);
			calendars.add(finalNonLabelledCalendar);
		}

		return calendars;
	}

	/**
	 * Processes a string containing holiday definitions
	 *
	 * @param holidays the string containing holiday definitions
	 * @param timeZone the time zone to use
	 * @return a list of calendars representing the holiday definitions
	 * @throws ParseException
	 */
	private List<Calendar> processHolidayString(String holidays,
			TimeZone timeZone) throws ParseException {

		List<Calendar> calendars = new ArrayList<>();

		if (StringUtils.isNotBlank(holidays)) {
			if (StringUtils.startsWith(holidays, ExpressionHelper.GROOVY_START_STRING)) {
				ExpressionHelper expressionHelper = new ExpressionHelper();
				Object result = expressionHelper.runGroovyExpression(holidays);
				if (result instanceof List) {
					@SuppressWarnings("unchecked")
					List<Calendar> groovyCalendars = (List<Calendar>) result;
					List<Calendar> nonLabelledGroovyCalendars = new ArrayList<>();
					for (Calendar calendar : groovyCalendars) {
						if (StringUtils.isBlank(calendar.getDescription())) {
							nonLabelledGroovyCalendars.add(calendar);
						} else {
							calendars.add(calendar);
						}
					}
					if (CollectionUtils.isNotEmpty(nonLabelledGroovyCalendars)) {
						Calendar finalCalendar = concatenateCalendars(nonLabelledGroovyCalendars);
						calendars.add(finalCalendar);
					}
				} else {
					Calendar calendar = (Calendar) result;
					calendars.add(calendar);
				}
			} else {
				String values[] = holidays.split("\\r?\\n");
				List<Calendar> cronCalendars = new ArrayList<>();
				for (String value : values) {
					CronCalendar calendar = new CronCalendar(value);
					calendar.setTimeZone(timeZone);
					cronCalendars.add(calendar);
				}
				if (CollectionUtils.isNotEmpty(cronCalendars)) {
					Calendar finalCalendar = concatenateCalendars(cronCalendars);
					calendars.add(finalCalendar);
				}
			}
		}

		return calendars;
	}

	/**
	 * Concatenate calendars to get one calendar that includes all the dates in
	 * the given calendars
	 *
	 * @param calendars the list of calendars to concatenate
	 * @return a calendar that includes all the dates in the given calendars
	 */
	private Calendar concatenateCalendars(List<Calendar> calendars) {
		//https://stackoverflow.com/questions/5863435/quartz-net-multple-calendars
		if (CollectionUtils.isEmpty(calendars)) {
			return null;
		}

		//concatenate calendars. you can only specify one calendar for a trigger
		for (int i = 0; i < calendars.size(); i++) {
			if (i > 0) {
				Calendar currentCalendar = calendars.get(i);
				Calendar previousCalendar = calendars.get(i - 1);
				currentCalendar.setBaseCalendar(previousCalendar);
			}
		}
		Calendar finalCalendar = calendars.get(calendars.size() - 1);

		return finalCalendar;
	}

	/**
	 * Processes schedule definitions in the main fields and extra section
	 *
	 * @param job the art job
	 * @param timeZone the time zone to use for the triggers
	 * @param scheduler the scheduler to use
	 * @return details of the triggers to use for the job
	 * @throws ParseException
	 * @throws org.quartz.SchedulerException
	 */
	public TriggersResult processTriggers(Job job, TimeZone timeZone,
			Scheduler scheduler) throws ParseException, SchedulerException {

		List<Calendar> calendars = processHolidays(job, timeZone);

		int jobId = job.getJobId();
		String globalCalendarName = "jobCalendar" + jobId;

		CalendarsResult calendarsResult = processCalendars(calendars, globalCalendarName, scheduler);

		Set<Trigger> triggers = processTriggers(job, calendarsResult.getGlobalCalendar(), timeZone);

		TriggersResult triggersResult = new TriggersResult();
		triggersResult.setCalendarNames(calendarsResult.getCalendarNames());
		triggersResult.setTriggers(triggers);

		return triggersResult;
	}

	/**
	 * Processes schedule definitions in the main fields and extra section
	 *
	 * @param pipeline the pipeline object
	 * @param timeZone the time zone to use for the triggers
	 * @param scheduler the scheduler to use
	 * @return details of the triggers to use for the job
	 * @throws ParseException
	 * @throws org.quartz.SchedulerException
	 */
	public TriggersResult processTriggers(Pipeline pipeline, TimeZone timeZone,
			Scheduler scheduler) throws ParseException, SchedulerException {

		List<Calendar> calendars = processHolidays(pipeline, timeZone);

		int pipelineId = pipeline.getPipelineId();
		String globalCalendarName = "pipelineCalendar" + pipelineId;

		CalendarsResult calendarsResult = processCalendars(calendars, globalCalendarName, scheduler);

		Set<Trigger> triggers = processTriggers(pipeline, calendarsResult.getGlobalCalendar(), timeZone);

		TriggersResult triggersResult = new TriggersResult();
		triggersResult.setCalendarNames(calendarsResult.getCalendarNames());
		triggersResult.setTriggers(triggers);

		return triggersResult;
	}

	/**
	 * Processes calendars, adding them to the quartz scheduler
	 *
	 * @param calendars the quartz calendars
	 * @param globalCalendarName the global calendar name
	 * @param scheduler the quartz scheduler
	 * @return details of the calendar names
	 * @throws SchedulerException
	 */
	private CalendarsResult processCalendars(List<Calendar> calendars,
			String globalCalendarName, Scheduler scheduler) throws SchedulerException {

		CalendarsResult result = new CalendarsResult();

		Calendar globalCalendar = null;
		List<String> calendarNames = new ArrayList<>();
		for (Calendar calendar : calendars) {
			String calendarName = calendar.getDescription();
			if (StringUtils.isBlank(calendarName)) {
				globalCalendar = calendar;
				globalCalendar.setDescription(globalCalendarName);
				calendarName = globalCalendarName;
			}
			calendarNames.add(calendarName);

			boolean replace = true;
			boolean updateTriggers = true;
			scheduler.addCalendar(calendarName, calendar, replace, updateTriggers);
		}

		result.setGlobalCalendar(globalCalendar);
		result.setCalendarNames(calendarNames);

		return result;
	}

	/**
	 * Processes schedule definitions in the main fields and extra section
	 *
	 * @param job the art job
	 * @param globalCalendar the global calendar to apply to triggers
	 * @param timeZone the time zone to use for the triggers
	 * @return the list of triggers to use for the job
	 * @throws ParseException
	 */
	private Set<Trigger> processTriggers(Job job, Calendar globalCalendar,
			TimeZone timeZone) throws ParseException {

		int jobId = job.getJobId();
		String mainTriggerName = "jobTrigger" + jobId;

		String cronString;

		Schedule schedule = job.getSchedule();
		if (schedule == null) {
			cronString = CronStringHelper.getCronString(job);
		} else {
			cronString = CronStringHelper.getCronString(schedule);
		}

		//if start date is in the past, job will fire once immediately, for the missed fire time in the past
		Date now = new Date();
		if (job.getStartDate().before(now)) {
			job.setStartDate(now);
		}

		Date startDate = job.getStartDate();
		Date endDate = job.getEndDate();

		String extraSchedules;
		if (schedule == null) {
			extraSchedules = job.getExtraSchedules();
		} else {
			extraSchedules = schedule.getExtraSchedules();
		}

		return processTriggers(globalCalendar, timeZone, mainTriggerName, cronString, startDate, endDate, extraSchedules);
	}

	/**
	 * Processes schedule definitions for a pipeline
	 *
	 * @param pipeline the pipeline object
	 * @param globalCalendar the global calendar to apply to triggers
	 * @param timeZone the time zone to use for the triggers
	 * @return the list of triggers to use for the job
	 * @throws ParseException
	 */
	private Set<Trigger> processTriggers(Pipeline pipeline, Calendar globalCalendar,
			TimeZone timeZone) throws ParseException {

		int pipelineId = pipeline.getPipelineId();
		String mainTriggerName = "pipelineTrigger" + pipelineId;

		String cronString = null;

		Schedule schedule = pipeline.getSchedule();
		if (schedule != null) {
			cronString = CronStringHelper.getCronString(schedule);
		}

		Date startDate = new Date();
		Date endDate = null;

		String extraSchedules = null;
		if (schedule != null) {
			extraSchedules = schedule.getExtraSchedules();
		}

		return processTriggers(globalCalendar, timeZone, mainTriggerName, cronString, startDate, endDate, extraSchedules);
	}

	/**
	 * Processes schedule definitions in the main fields and extra section
	 *
	 * @param globalCalendar the global calendar to apply to triggers
	 * @param timeZone the time zone to use for the triggers
	 * @param mainTriggerName the main trigger name
	 * @param cronString the cron string for the main schedule
	 * @param startDate the start date
	 * @param endDate the end date
	 * @param extraSchedules the extra schedules
	 * @return the list of triggers to use for the job
	 * @throws ParseException
	 */
	private Set<Trigger> processTriggers(Calendar globalCalendar,
			TimeZone timeZone, String mainTriggerName, String cronString,
			Date startDate, Date endDate, String extraSchedules) throws ParseException {

		Set<Trigger> triggers = new HashSet<>();

		//create trigger that defines the main schedule for the job
		CronTriggerImpl mainTrigger = new CronTriggerImpl();
		mainTrigger.setKey(triggerKey(mainTriggerName, ArtUtils.TRIGGER_GROUP));
		mainTrigger.setCronExpression(cronString);
		mainTrigger.setStartTime(startDate);
		mainTrigger.setEndTime(endDate);
		mainTrigger.setTimeZone(timeZone);

		if (globalCalendar != null) {
			mainTrigger.setCalendarName(globalCalendar.getDescription());
		}

		triggers.add(mainTrigger);

		//create triggers for extra schedules
		if (StringUtils.isNotBlank(extraSchedules)) {
			if (StringUtils.startsWith(extraSchedules, ExpressionHelper.GROOVY_START_STRING)) {
				ExpressionHelper expressionHelper = new ExpressionHelper();
				Object result = expressionHelper.runGroovyExpression(extraSchedules);
				if (result instanceof List) {
					@SuppressWarnings("unchecked")
					List<AbstractTrigger<Trigger>> extraTriggers = (List<AbstractTrigger<Trigger>>) result;
					for (AbstractTrigger<Trigger> extraTrigger : extraTriggers) {
						finalizeTriggerProperties(extraTrigger, globalCalendar, startDate, endDate);
					}
					triggers.addAll(extraTriggers);
				} else {
					if (result instanceof AbstractTrigger) {
						@SuppressWarnings("unchecked")
						AbstractTrigger<Trigger> extraTrigger = (AbstractTrigger<Trigger>) result;
						finalizeTriggerProperties(extraTrigger, globalCalendar, startDate, endDate);
						triggers.add(extraTrigger);
					}
				}
			} else {
				String values[] = extraSchedules.split("\\r?\\n");
				int index = 1;
				for (String value : values) {
					index++;
					String extraTriggerName = mainTriggerName + "-" + index;

					CronTriggerImpl extraTrigger = new CronTriggerImpl();
					extraTrigger.setKey(triggerKey(extraTriggerName, ArtUtils.TRIGGER_GROUP));
					String finalCronString = CronStringHelper.processDynamicTime(value);
					extraTrigger.setCronExpression(finalCronString);
					extraTrigger.setStartTime(startDate);
					extraTrigger.setEndTime(endDate);
					extraTrigger.setTimeZone(timeZone);

					if (globalCalendar != null) {
						extraTrigger.setCalendarName(globalCalendar.getDescription());
					}

					triggers.add(extraTrigger);
				}
			}
		}

		return triggers;
	}

	/**
	 * Sets properties for a trigger where they are not explicitly defined e.g.
	 * calendar name, start date and end date
	 *
	 * @param trigger the trigger to set
	 * @param globalCalendar the global calendar in use
	 * @param startDate the start date
	 * @param endDate the end date
	 */
	private void finalizeTriggerProperties(AbstractTrigger<Trigger> trigger,
			Calendar globalCalendar, Date startDate, Date endDate) {

		if (StringUtils.isBlank(trigger.getCalendarName()) && globalCalendar != null) {
			trigger.setCalendarName(globalCalendar.getDescription());
		}
		if (trigger.getStartTime() == null) {
			trigger.setStartTime(startDate);
		}
		if (trigger.getEndTime() == null) {
			trigger.setEndTime(endDate);
		}
	}
}
