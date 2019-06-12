/*
 * ART. A Reporting Tool.
 * Copyright (C) 2019 Enrico Liboni <eliboni@users.sf.net>
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

import art.mail.Mailer;
import art.servlets.Config;
import art.smtpserver.SmtpServer;
import art.smtpserver.SmtpServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods for dealing with emailing
 *
 * @author Timothy Anyona
 */
@Service
public class MailService {

	private static final Logger logger = LoggerFactory.getLogger(MailService.class);

	@Autowired
	private SmtpServerService smtpServerService;

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
	 * @throws java.lang.Exception
	 */
	public Mailer getMailer(SmtpServer smtpServer) throws Exception {
		boolean updateDatabase = true;
		return getMailer(smtpServer, updateDatabase);
	}

	/**
	 * Returns a mailer object that can be used to send emails, using
	 * configuration defined in an smtp server object
	 *
	 * @param smtpServer the smtp server object
	 * @param updateDatabase whether to update the database with the potentially
	 * new oauth access token
	 * @return a mailer object that can be used to send emails
	 * @throws java.lang.Exception
	 */
	public Mailer getMailer(SmtpServer smtpServer, boolean updateDatabase)
			throws Exception {

		logger.debug("Entering getMailer: smtpServer={}, updateDatabase={}",
				smtpServer, updateDatabase);

		Mailer mailer = new Mailer();

		mailer.setHost(smtpServer.getServer());
		mailer.setPort(smtpServer.getPort());
		mailer.setUseStartTls(smtpServer.isUseStartTls());
		mailer.setUseAuthentication(smtpServer.isUseSmtpAuthentication());
		mailer.setUsername(smtpServer.getUsername());
		mailer.setUseOAuth2(smtpServer.isUseOAuth2());

		if (smtpServer.isUseOAuth2()) {
			smtpServerService.updateOAuthAccessToken(smtpServer, updateDatabase);
			mailer.setPassword(smtpServer.getOauthAccessToken());
		} else {
			mailer.setPassword(smtpServer.getPassword());
		}

		return mailer;
	}

}
