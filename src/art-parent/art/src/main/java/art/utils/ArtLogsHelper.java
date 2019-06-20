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
package art.utils;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.reportparameter.ReportParameter;
import art.user.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for logging events to the art_logs table
 *
 * @author Timothy Anyona
 */
public class ArtLogsHelper {

	private static final Logger logger = LoggerFactory.getLogger(ArtHelper.class);

	/**
	 * Logs a report run
	 *
	 * @param sessionUser the session user
	 * @param ip the ip address where the report is being run from
	 * @param reportId the report id of the report being run
	 * @param reportFormat the report format
	 * @param reportParamsList the report parameters list
	 */
	public static void logReportRun(User sessionUser, String ip, int reportId,
			String reportFormat, List<ReportParameter> reportParamsList) {

		Integer totalTimeSeconds = null;
		Integer fetchTimeSeconds = null;
		logReportRun(sessionUser, ip, reportId, totalTimeSeconds, fetchTimeSeconds, reportFormat, reportParamsList);
	}

	/**
	 * Logs a report run
	 *
	 * @param sessionUser the session user
	 * @param ip the ip address where the report is being run from
	 * @param reportId the report id of the report being run
	 * @param totalTimeSeconds the total time taken (in seconds) to fetch the
	 * report data and display the results
	 * @param fetchTimeSeconds the total time taken (in seconds) to fetch the
	 * report data
	 * @param reportFormat the report format
	 * @param reportParamsList the report parameters list
	 */
	public static void logReportRun(User sessionUser, String ip, int reportId,
			Integer totalTimeSeconds, Integer fetchTimeSeconds,
			String reportFormat, List<ReportParameter> reportParamsList) {

		List<String> parameterValuesList = new ArrayList<>();
		if (reportParamsList != null) {
			for (ReportParameter reportParam : reportParamsList) {
				String nameAndDisplayValues = reportParam.getNameAndDisplayValues();
				parameterValuesList.add(nameAndDisplayValues);
			}
		}

		String parameters = StringUtils.join(parameterValuesList, ",");
		if (StringUtils.isNotBlank(parameters)) {
			parameters = ", " + parameters;
		}

		String username = sessionUser.getUsername();
		String message = reportFormat + parameters;
		String event = "report";

		log(username, event, ip, message, reportId, totalTimeSeconds, fetchTimeSeconds);
	}

	/**
	 * Logs a job run
	 *
	 * @param user the username of the user who initiated the job
	 * @param jobId the job id
	 * @param message the result of the job run
	 * @param totalTimeSeconds
	 */
	public static void logJobRun(String user, int jobId, String message, Integer totalTimeSeconds) {
		String event = "job";
		String ip = null;
		Integer fetchTimeSeconds = null;
		log(user, event, ip, message, jobId, totalTimeSeconds, fetchTimeSeconds);
	}

	/**
	 * Logs an event
	 *
	 * @param user the username
	 * @param event type of event
	 * @param ip the ip address from which event was done
	 */
	public static void log(String user, String event, String ip) {
		String message = null;
		log(user, event, ip, message);
	}

	/**
	 * Logs an event
	 *
	 * @param user the username
	 * @param event type of event
	 * @param ip the ip address from which event was done
	 * @param message the log message
	 */
	public static void log(String user, String event, String ip, String message) {
		Integer reportId = null;
		Integer totalTimeSeconds = null;
		Integer fetchTimeSeconds = null;
		log(user, event, ip, message, reportId, totalTimeSeconds, fetchTimeSeconds);
	}

	/**
	 * Logs an event
	 *
	 * @param user the username of user who performed the event
	 * @param event the type of event
	 * @param ip the ip address from which the event was initiated
	 * @param message the log message
	 * @param itemId the id of the report or job that was run
	 * @param totalTimeSeconds the total time to run the report and display the
	 * results (in seconds), or total time of the job run
	 * @param fetchTimeSeconds the time to fetch the results from the database
	 * (in seconds)
	 */
	public static void log(String user, String event, String ip, String message,
			Integer itemId, Integer totalTimeSeconds, Integer fetchTimeSeconds) {

		try {
			String sql = "INSERT INTO ART_LOGS"
					+ " (LOG_DATE, USERNAME, LOG_TYPE, IP, ITEM_ID,"
					+ " TOTAL_TIME, FETCH_TIME, MESSAGE)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 8) + ")";

			final int MAX_MESSAGE_LENGTH = 500;
			message = StringUtils.left(message, MAX_MESSAGE_LENGTH);

			Object[] values = {
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				user,
				event,
				ip,
				itemId,
				totalTimeSeconds,
				fetchTimeSeconds,
				message};

			DbService dbService = new DbService();
			dbService.update(sql, values);
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}
	}

}
