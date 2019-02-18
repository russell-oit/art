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

import art.enums.ReportType;
import art.mail.Mailer;
import art.servlets.Config;
import art.smtpserver.SmtpServer;
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
