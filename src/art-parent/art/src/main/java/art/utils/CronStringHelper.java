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
package art.utils;

import art.job.Job;
import art.schedule.Schedule;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import net.redhogs.cronparser.CronExpressionDescriptor;
import net.redhogs.cronparser.Options;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides methods for getting cron strings for a job or schedule's main
 * schedule
 *
 * @author Timothy Anyona
 */
public class CronStringHelper {

	/**
	 * Returns the quartz cron string for a schedule's main schedule
	 *
	 * @param schedule the schedule, not null
	 * @return the cron string for the schedule's main schedule
	 */
	public static String getCronString(Schedule schedule) {
		Objects.requireNonNull(schedule, "schedule must not be null");

		return getCronString(schedule.getSecond(), schedule.getMinute(),
				schedule.getHour(), schedule.getDay(), schedule.getMonth(),
				schedule.getWeekday(), schedule.getYear());
	}

	/**
	 * Returns the quartz cron string for a job's main schedule
	 *
	 * @param job the job, not null
	 * @return the cron string for the job's main schedule
	 */
	public static String getCronString(Job job) {
		Objects.requireNonNull(job, "job must not be null");

		return getCronString(job.getScheduleSecond(), job.getScheduleMinute(),
				job.getScheduleHour(), job.getScheduleDay(), job.getScheduleMonth(),
				job.getScheduleWeekday(), job.getScheduleYear());
	}

	/**
	 * Returns a quartz cron string for the given schedule fields
	 *
	 * @param second the second
	 * @param minute the minute
	 * @param hour the hour
	 * @param day the day
	 * @param month the month
	 * @param weekday the weekday
	 * @param year the year
	 * @return
	 */
	private static String getCronString(String second, String minute, String hour,
			String day, String month, String weekday, String year) {

		minute = StringUtils.deleteWhitespace(minute);

		hour = StringUtils.deleteWhitespace(hour);
		String originalHour = hour;

		//enable definition of random start time
		if (StringUtils.contains(originalHour, "|")) {
			String startPart = StringUtils.substringBefore(originalHour, "|");
			String endPart = StringUtils.substringAfter(originalHour, "|");
			String startHour = StringUtils.substringBefore(startPart, ":");
			String startMinute = StringUtils.substringAfter(startPart, ":");
			String endHour = StringUtils.substringBefore(endPart, ":");
			String endMinute = StringUtils.substringAfter(endPart, ":");

			if (StringUtils.isBlank(startMinute)) {
				startMinute = "0";
			}
			if (StringUtils.isBlank(endMinute)) {
				endMinute = "0";
			}

			Date now = new Date();

			Calendar calStart = Calendar.getInstance();
			calStart.setTime(now);
			calStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
			calStart.set(Calendar.MINUTE, Integer.parseInt(startMinute));

			Calendar calEnd = Calendar.getInstance();
			calEnd.setTime(now);
			calEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
			calEnd.set(Calendar.MINUTE, Integer.parseInt(endMinute));

			long randomDate = ArtUtils.getRandomNumber(calStart.getTimeInMillis(), calEnd.getTimeInMillis());
			Calendar calRandom = Calendar.getInstance();
			calRandom.setTimeInMillis(randomDate);

			hour = String.valueOf(calRandom.get(Calendar.HOUR_OF_DAY));
			minute = String.valueOf(calRandom.get(Calendar.MINUTE));
		}

		if (StringUtils.isBlank(minute)) {
			//no minute defined. use random value
			minute = String.valueOf(ArtUtils.getRandomNumber(0, 59));
		}

		if (StringUtils.isBlank(hour)) {
			//no hour defined. use random value between 3-6
			hour = String.valueOf(ArtUtils.getRandomNumber(3, 6));
		}

		second = StringUtils.deleteWhitespace(second);
		if (StringUtils.isBlank(second)) {
			//no second defined. default to 0
			second = "0";
		}

		month = StringUtils.deleteWhitespace(month);
		if (StringUtils.isBlank(month)) {
			//no month defined. default to every month
			month = "*";
		}

		year = StringUtils.deleteWhitespace(year);
		if (StringUtils.isBlank(year)) {
			//no year defined. default to every year
			year = "*";
		}

		day = StringUtils.deleteWhitespace(day);
		weekday = StringUtils.deleteWhitespace(weekday);

		//set default day of the month if weekday is defined
		if (StringUtils.isBlank(day) && StringUtils.isNotBlank(weekday)
				&& !StringUtils.equals(weekday, "?")) {
			//weekday defined but day of the month is not. default day to ?
			day = "?";
		}

		if (StringUtils.isBlank(day)) {
			//no day of month defined. default to *
			day = "*";
		}

		if (StringUtils.isBlank(weekday)) {
			//no day of week defined. default to undefined
			weekday = "?";
		}

		if (StringUtils.equals(day, "?") && StringUtils.equals(weekday, "?")) {
			//unsupported. only one can be ?
			day = "*";
			weekday = "?";
		}
		if (StringUtils.equals(day, "*") && StringUtils.equals(weekday, "*")) {
			//unsupported. only one can be *
			day = "*";
			weekday = "?";
		}

		String cronString = second + " " + minute
				+ " " + hour + " " + day
				+ " " + month + " " + weekday
				+ " " + year;

		return cronString;
	}

	/**
	 * Returns a description for a schedule's main schedule
	 *
	 * @param schedule the schedule
	 * @param locale the locale to use
	 * @return a description for the schedule's main schedule
	 * @throws ParseException
	 */
	public static String getCronScheduleDescription(Schedule schedule, Locale locale) throws ParseException {
		String cronString = getCronString(schedule);
		String description = getCronScheduleDescription(cronString, locale);
		return description;
	}

	/**
	 * Returns a description for a job's main schedule
	 *
	 * @param job the job
	 * @param locale the locale to use
	 * @return a description for the job's main schedule
	 * @throws ParseException
	 */
	public static String getCronScheduleDescription(Job job, Locale locale) throws ParseException {
		String cronString = getCronString(job);
		String description = getCronScheduleDescription(cronString, locale);
		return description;
	}

	/**
	 * Returns a description for a quartz cron schedule
	 *
	 * @param cronString the cron string
	 * @param locale the locale to use
	 * @return a description for the quartz cron schedule
	 * @throws ParseException
	 */
	public static String getCronScheduleDescription(String cronString, Locale locale) throws ParseException {
		Options cronOptions = new Options();
		cronOptions.setZeroBasedDayOfWeek(false); //day of the week for quartz cron starts at 1 i.e. SUN
		cronOptions.setTwentyFourHourTime(true);

		String description = CronExpressionDescriptor.getDescription(cronString, cronOptions, locale);
		return description;
	}

}
