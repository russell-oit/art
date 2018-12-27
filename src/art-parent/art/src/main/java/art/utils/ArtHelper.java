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
package art.utils;

import art.dbutils.DatabaseUtils;
import art.dbutils.DbService;
import art.enums.ReportType;
import art.mail.Mailer;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.smtpserver.SmtpServer;
import art.user.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides helper methods to be used within the application
 *
 * @author Timothy Anyona
 */
public class ArtHelper {

	private static final Logger logger = LoggerFactory.getLogger(ArtHelper.class);

	/**
	 * Logs login attempts to the ART_LOGS table
	 *
	 * @param user the username
	 * @param type "login" if successful or "loginerr" if not
	 * @param ip the ip address from which login was done or attempted
	 * @param message the log message
	 */
	public static void log(String user, String type, String ip, String message) {
		Integer reportId = null;
		Integer totalTimeSeconds = null;
		Integer fetchTimeSeconds = null;
		log(user, type, ip, message, reportId, totalTimeSeconds, fetchTimeSeconds);
	}

	/**
	 * Logs an interactive report run in the ART_LOGS table
	 *
	 * @param sessionUser the session user
	 * @param ip the ip address where the report is being run from
	 * @param reportId the report id of the report being run
	 * @param reportFormat the report format
	 * @param reportParamsList the report parameters list
	 */
	public static void logInteractiveReportRun(User sessionUser, String ip, int reportId,
			String reportFormat, List<ReportParameter> reportParamsList) {

		Integer totalTimeSeconds = null;
		Integer fetchTimeSeconds = null;
		logInteractiveReportRun(sessionUser, ip, reportId, totalTimeSeconds, fetchTimeSeconds, reportFormat, reportParamsList);
	}

	/**
	 * Logs an interactive report run in the ART_LOGS table
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
	public static void logInteractiveReportRun(User sessionUser, String ip, int reportId,
			Integer totalTimeSeconds, Integer fetchTimeSeconds, String reportFormat,
			List<ReportParameter> reportParamsList) {

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
		log(username, "report", ip, message, reportId, totalTimeSeconds, fetchTimeSeconds);
	}

	/**
	 * Logs some action to the ART_LOGS table
	 *
	 * @param user the username of user who executed the report
	 * @param type the type of event
	 * @param ip the ip address from which report was run
	 * @param message the log message
	 * @param reportId the id of the report that was run
	 * @param totalTimeSeconds the total time to run the report and display the
	 * results (in seconds)
	 * @param fetchTimeSeconds the time to fetch the results from the database
	 * (in seconds)
	 */
	public static void log(String user, String type, String ip, String message,
			Integer reportId, Integer totalTimeSeconds, Integer fetchTimeSeconds) {

		try {
			String sql = "INSERT INTO ART_LOGS"
					+ " (LOG_DATE, USERNAME, LOG_TYPE, IP, QUERY_ID,"
					+ " TOTAL_TIME, FETCH_TIME, MESSAGE)"
					+ " VALUES(" + StringUtils.repeat("?", ",", 8) + ")";

			final int MAX_LOG_MESSAGE_LENGTH = 500;
			message = StringUtils.left(message, MAX_LOG_MESSAGE_LENGTH);

			Object[] values = {
				DatabaseUtils.getCurrentTimeAsSqlTimestamp(),
				user,
				type,
				ip,
				reportId,
				totalTimeSeconds,
				fetchTimeSeconds,
				message
			};

			DbService dbService = new DbService();
			dbService.update(sql, values);
		} catch (SQLException ex) {
			logger.error("Error", ex);
		}
	}

	/**
	 * Returns the default show legend option depending on the report type
	 *
	 * @param reportType the report type
	 * @return the default show legend option
	 */
	public boolean getDefaultShowLegendOption(ReportType reportType) {
		if (reportType == ReportType.HeatmapChart) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns the default show labels option depending on the report type
	 *
	 * @param reportType the report type
	 * @return the default show labels option
	 */
	public boolean getDefaultShowLabelsOption(ReportType reportType) {
		if (reportType == ReportType.Pie2DChart || reportType == ReportType.Pie3DChart) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns a mailer object that can be used to send emails, using
	 * configuration defined in application settings
	 *
	 * @return a mailer object that can be used to send emails
	 */
	public Mailer getMailer() {
		logger.debug("Entering getMailer");

		Mailer mailer = new Mailer();

		mailer.setHost(Config.getSettings().getSmtpServer());
		mailer.setPort(Config.getSettings().getSmtpPort());
		mailer.setUseStartTls(Config.getSettings().isSmtpUseStartTls());
		mailer.setUseAuthentication(Config.getSettings().isUseSmtpAuthentication());
		mailer.setUsername(Config.getSettings().getSmtpUsername());
		mailer.setPassword(Config.getSettings().getSmtpPassword());

		return mailer;
	}

	/**
	 * Returns a mailer object that can be used to send emails, using
	 * configuration defined in an smtp server object
	 *
	 * @param smtpServer the smtp server object
	 * @return a mailer object that can be used to send emails
	 */
	public Mailer getMailer(SmtpServer smtpServer) {
		logger.debug("Entering getMailer: smtpServer={}", smtpServer);

		Mailer mailer = new Mailer();

		mailer.setHost(smtpServer.getServer());
		mailer.setPort(smtpServer.getPort());
		mailer.setUseStartTls(smtpServer.isUseStartTls());
		mailer.setUseAuthentication(smtpServer.isUseSmtpAuthentication());
		mailer.setUsername(smtpServer.getUsername());
		mailer.setPassword(smtpServer.getPassword());

		return mailer;
	}
}
