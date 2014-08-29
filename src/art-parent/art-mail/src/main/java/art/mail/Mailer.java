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
	private String[] to;
	private String subject;
	private String message;
	private String from;
	private String username;
	private String password;
	private List<File> attachments;
	private String smtpServer;
	private int smtpPort;
	private boolean sessionDebug;
	private String[] cc;
	private String[] bcc;
	private boolean useStartTls;
	private boolean sendPartial = true;
	private String messageType = "text/html;charset=utf-8";
	private boolean useSmtpAuthentication;
	private static final String[] EMPTY_STRING_ARRAY = {};

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
	 * @return the to
	 */
	public String[] getTo() {
		if (to == null) {
			return EMPTY_STRING_ARRAY;
		} else {
			return to.clone(); //clone mutable objects for enhanced security
		}
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(String[] to) {
		if (to == null) {
			this.to = null;
		} else {
			this.to = to.clone(); //clone mutable objects for enhanced security
		}
	}

	/**
	 * Set single to address
	 *
	 * @param s
	 */
	public void setTo(String s) {
		to = new String[]{s};
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
	 * @return the cc
	 */
	public String[] getCc() {
		if (cc == null) {
			return EMPTY_STRING_ARRAY;
		} else {
			return cc.clone(); //clone mutable objects for enhanced security
		}
	}

	/**
	 * @param cc the cc to set
	 */
	public void setCc(String[] cc) {
		if (cc == null) {
			this.cc = null;
		} else {
			this.cc = cc.clone();
		}
	}

	/**
	 * Set single cc address
	 *
	 * @param s
	 */
	public void setCc(String s) {
		cc = new String[]{s};
	}

	/**
	 * @return the bcc
	 */
	public String[] getBcc() {
		if (bcc == null) {
			return EMPTY_STRING_ARRAY;
		} else {
			return bcc.clone(); //clone mutable objects for enhanced security
		}
	}

	/**
	 * @param bcc the bcc to set
	 */
	public void setBcc(String[] bcc) {
		if (bcc == null) {
			this.bcc = null;
		} else {
			this.bcc = bcc.clone(); //clone mutable objects for enhanced security
		}
	}

	/**
	 * Set single bcc address
	 *
	 * @param s
	 */
	public void setBcc(String s) {
		bcc = new String[]{s};
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
		logger.debug("smtpServer='{}'", smtpServer);
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
		logger.debug("(to != null) = {}", to != null);
		if (to != null) {
			logger.debug("to.length={}", to.length);
			InternetAddress[] addressTo = new InternetAddress[to.length];
			for (int i = 0; i < to.length; i++) {
				addressTo[i] = new InternetAddress(to[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
		}

		// Set the CC: address(es)
		logger.debug("(cc != null) = {}", cc != null);
		if (cc != null) {
			logger.debug("cc.length={}", cc.length);
			InternetAddress[] addressCc = new InternetAddress[cc.length];
			for (int i = 0; i < cc.length; i++) {
				addressCc[i] = new InternetAddress(cc[i]);
			}
			msg.setRecipients(Message.RecipientType.CC, addressCc);
		}

		// Set the BCC: address(es)
		logger.debug("(bcc != null) = {}", bcc != null);
		if (bcc != null) {
			logger.debug("bcc.length={}", bcc.length);
			InternetAddress[] addressBcc = new InternetAddress[bcc.length];
			for (int i = 0; i < bcc.length; i++) {
				addressBcc[i] = new InternetAddress(bcc[i]);
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

				logger.debug("file.getName()='{}'", file.getName());
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
