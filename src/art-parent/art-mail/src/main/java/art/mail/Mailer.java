/*
 * Copyright 2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, version 2.1 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.mail;

import java.io.File;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to send emails
 *
 */
public class Mailer {

	private static final Logger logger = LoggerFactory.getLogger(Mailer.class);
	private String[] tos;
	private String subject;
	private String message;
	private String from;
	private String username;
	private String password;
	private List<File> attachments;
	private String smtpServer;
	private int smtpPort;
	private boolean sessionDebug;
	private String[] ccs;
	private String[] bccs;
	private boolean useStartTls;
	private boolean sendPartial = true;
	private String messageType = "text/html;charset=utf-8";
	private boolean useSmtpAuthentication;

	/**
	 * @return the useSmtpAuthentication
	 */
	public boolean isUseSmtpAuthentication() {
		return useSmtpAuthentication;
	}

	/**
	 * @param useSmtpAuthentication the useSmtpAuthentication to set
	 */
	public void setUseSmtpAuthentication(boolean useSmtpAuthentication) {
		this.useSmtpAuthentication = useSmtpAuthentication;
	}

	/**
	 * @return the messageType
	 */
	public String getMessageType() {
		return messageType;
	}

	/**
	 * Set the message type e.g. text/plain, text/html, text/html;charset=utf-8
	 * 
	 * @param messageType the messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	/**
	 * @return the tos
	 */
	public String[] getTos() {
		if (tos == null) {
			return null;
		} else {
			return (String[]) tos.clone();
		}
	}

	/**
	 * @param tos the tos to set
	 */
	public void setTos(String[] tos) {
		if (tos == null) {
			this.tos = null;
		} else {
			this.tos = (String[]) tos.clone();
		}
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Username to use if server requires smtp authentication
	 *
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the attachments
	 */
	public List<File> getAttachments() {
		return attachments;
	}

	/**
	 * @param attachments the attachments to set
	 */
	public void setAttachments(List<File> attachments) {
		this.attachments = attachments;
	}

	/**
	 * Set single to address
	 *
	 * @param s
	 */
	public void setTo(String s) {
		tos = null;
		tos = new String[1];
		tos[0] = s;
	}
	
	/**
	 * Set the smtp server ip or hostname
	 * 
	 * @return 
	 */
	public String getSmtpServer() {
		return smtpServer;
	}

	/**
	 * @param smtpServer the smtpServer to set
	 */
	public void setSmtpServer(String smtpServer) {
		this.smtpServer = smtpServer;
	}

	/**
	 * @return the smtpPort
	 */
	public int getSmtpPort() {
		return smtpPort;
	}

	/**
	 * @param smtpPort the smtpPort to set
	 */
	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}

	/**
	 * @return the sessionDebug
	 */
	public boolean isSessionDebug() {
		return sessionDebug;
	}

	/**
	 * @param sessionDebug the sessionDebug to set
	 */
	public void setSessionDebug(boolean sessionDebug) {
		this.sessionDebug = sessionDebug;
	}

	/**
	 * Get the value of sendPartial
	 *
	 * @return the value of sendPartial
	 */
	public boolean isSendPartial() {
		return sendPartial;
	}

	/**
	 * Set the value of sendPartial
	 *
	 * @param sendPartial new value of sendPartial
	 */
	public void setSendPartial(boolean sendPartial) {
		this.sendPartial = sendPartial;
	}

	/**
	 * @return the ccs
	 */
	public String[] getCcs() {
		if (ccs == null) {
			return null;
		} else {
			return (String[]) ccs.clone();
		}
	}

	/**
	 * @param ccs the ccs to set
	 */
	public void setCcs(String[] ccs) {
		if (ccs == null) {
			this.ccs = null;
		} else {
			this.ccs = (String[]) ccs.clone();
		}
	}

	/**
	 * @return the bccs
	 */
	public String[] getBccs() {
		if (bccs == null) {
			return null;
		} else {
			return (String[]) bccs.clone();
		}
	}

	/**
	 * @param bccs the bccs to set
	 */
	public void setBccs(String[] bccs) {
		if (bccs == null) {
			this.bccs = null;
		} else {
			this.bccs = (String[]) bccs.clone();
		}
	}

	/**
	 * @return the useStartTls
	 */
	public boolean isUseStartTls() {
		return useStartTls;
	}

	/**
	 * @param useStartTls the useStartTls to set
	 */
	public void setUseStartTls(boolean useStartTls) {
		this.useStartTls = useStartTls;
	}

	/**
	 * Send the email
	 *
	 * @throws MessagingException
	 */
	public void send() throws MessagingException {
		logger.debug("Entering send");

		//Set smtp properties
		Properties props = new Properties();

		logger.debug("useStartTls={}", useStartTls);
		props.put("mail.smtp.starttls.enable", useStartTls);

		logger.debug("smtpPort={}", smtpPort);
		logger.debug("smtpHost='{}'", smtpServer);
		props.put("mail.smtp.port", smtpPort);
		props.put("mail.smtp.host", smtpServer);

		//If you're sending to multiple recipients, if one recipient address fails,
		//by default no email is sent to the other recipients
		//set the sendpartial property to true to have emails sent to the valid addresses,
		//even if invalid ones exist
		logger.debug("sendPartial={}", sendPartial);
		props.put("mail.smtp.sendpartial", sendPartial);

		logger.debug("username='{}'", username);

		//get Session            
		Session session;
		logger.debug("useSmtpAuthentication={}", useSmtpAuthentication);
		if (useSmtpAuthentication) {
			//smtp authentication enabled
			props.put("mail.smtp.auth", "true");
			Authenticator auth = new SMTPAuthenticator();
			session = Session.getInstance(props, auth);
		} else {
			session = Session.getDefaultInstance(props, null);
		}

		//enable session debug
		logger.debug("sessionDebug={}", sessionDebug);
		session.setDebug(sessionDebug);

		// Create a message
		Message msg = new MimeMessage(session);

		// Set the FROM: address
		logger.debug("from='{}'", from);
		msg.setFrom(new InternetAddress(from));

		// Set the TO: address(es)
		logger.debug("(tos != null) = {}", tos != null);
		if (tos != null) {
			logger.debug("tos.length={}", tos.length);
			InternetAddress[] addressTo = new InternetAddress[tos.length];
			for (int i = 0; i < tos.length; i++) {
				addressTo[i] = new InternetAddress(tos[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
		}

		// Set the CC: address(es)
		logger.debug("(ccs != null) = {}", ccs != null);
		if (ccs != null) {
			logger.debug("ccs.length={}", ccs.length);
			InternetAddress[] addressCc = new InternetAddress[ccs.length];
			for (int i = 0; i < ccs.length; i++) {
				addressCc[i] = new InternetAddress(ccs[i]);
			}
			msg.setRecipients(Message.RecipientType.CC, addressCc);
		}

		// Set the BCC: address(es)
		logger.debug("(bccs != null) = {}", bccs != null);
		if (bccs != null) {
			logger.debug("bccs.length={}", bccs.length);
			InternetAddress[] addressBcc = new InternetAddress[bccs.length];
			for (int i = 0; i < bccs.length; i++) {
				addressBcc[i] = new InternetAddress(bccs[i]);
			}
			msg.setRecipients(Message.RecipientType.BCC, addressBcc);
		}

		//set subject
		logger.debug("subject='{}'", subject);
		msg.setSubject(subject);

		//add attachments if available
		logger.debug("message='{}'", message);
		logger.debug("messageType='{}'", messageType);
		logger.debug("(attachments == null) = {}", attachments == null);
		if (attachments == null) {
			msg.setContent(message, messageType);
		} else {
			MimeMultipart mp = new MimeMultipart();

			MimeBodyPart text = new MimeBodyPart();
			text.setDisposition(Part.INLINE);
			text.setContent(message, messageType);

			mp.addBodyPart(text);

			logger.debug("attachments.size()={}", attachments.size());
			for (File file : attachments) {
				MimeBodyPart filePart = new MimeBodyPart();

				FileDataSource fds = new FileDataSource(file);
				DataHandler dh = new DataHandler(fds);
				filePart.setFileName(file.getName());
				filePart.setDisposition(Part.ATTACHMENT);
				filePart.setDescription("Attached file: " + file.getName());
				filePart.setDataHandler(dh);

				mp.addBodyPart(filePart);
			}

			msg.setContent(mp);
		}

		// Send the email
		Transport.send(msg);

		logger.debug("Leaving send");
	}

	private class SMTPAuthenticator extends Authenticator {

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(getUsername(), getPassword());
		}
	}
}
