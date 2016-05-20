/*
 * Copyright 2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
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
	private String host;
	private int port;
	private boolean debug;
	private String[] cc;
	private String[] bcc;
	private boolean useStartTls;
	private boolean sendPartial = true;
	private String messageType;
	private boolean useAuthentication;
	private static final String[] EMPTY_STRING_ARRAY = {};

	/**
	 * @return the useAuthentication
	 */
	public boolean isUseAuthentication() {
		return useAuthentication;
	}

	/**
	 * @param useAuthentication the useAuthentication to set
	 */
	public void setUseAuthentication(boolean useAuthentication) {
		this.useAuthentication = useAuthentication;
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
	 * Get the smtp server ip or hostname
	 *
	 * @return the smtp server ip or hostname
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the debug
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug the debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
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
	 * @throws java.io.IOException
	 */
	public void send() throws MessagingException, IOException {
		logger.debug("Entering send");

		//Set session properties
		Properties props = new Properties();

		logger.debug("port={}", port);
		logger.debug("host='{}'", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.host", host);

		logger.debug("useStartTls={}", useStartTls);
		props.put("mail.smtp.starttls.enable", useStartTls);

		//If you're sending to multiple recipients, if one recipient address fails,
		//by default no email is sent to the other recipients
		//set the sendpartial property to true to have emails sent to the valid addresses,
		//even if invalid ones exist
		logger.debug("sendPartial={}", sendPartial);
		props.put("mail.smtp.sendpartial", sendPartial);

		logger.debug("debug={}", debug);
		props.put("mail.debug", debug);

		//get Session            
		Session session = Session.getInstance(props);

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
				logger.debug("to[{}]='{}'", i, to[i]);
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
				logger.debug("cc[{}]='{}'", i, cc[i]);
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
				logger.debug("bcc[{}]='{}'", i, bcc[i]);
				addressBcc[i] = new InternetAddress(bcc[i]);
			}
			msg.setRecipients(Message.RecipientType.BCC, addressBcc);
		}

		//set subject
		logger.debug("subject='{}'", subject);
		msg.setSubject(subject);

		//set message content
		final String DEFAULT_MESSAGE_TYPE = "text/html;charset=utf-8";
		if (messageType == null) {
			messageType = DEFAULT_MESSAGE_TYPE;
		}
		logger.debug("message='{}'", message);
		logger.debug("messageType='{}'", messageType);
		logger.debug("(attachments == null) = {}", attachments == null);
		if (attachments == null) {
			msg.setContent(message, messageType);
		} else {
			MimeMultipart mp = new MimeMultipart();

			MimeBodyPart textPart = new MimeBodyPart();
			textPart.setDisposition(Part.INLINE);
			textPart.setContent(message, messageType);

			mp.addBodyPart(textPart);

			logger.debug("attachments.size()={}", attachments.size());
			for (File file : attachments) {
				MimeBodyPart filePart = new MimeBodyPart();
				filePart.attachFile(file);
				mp.addBodyPart(filePart);
			}

			msg.setContent(mp);
		}

		//set sent date (sets date headers)
		msg.setSentDate(new Date());

		// Send the email
		logger.debug("useAuthentication={}", useAuthentication);
		if (useAuthentication) {
			logger.debug("username='{}'", username);
			Transport.send(msg, username, password);
		} else {
			Transport.send(msg);
		}
	}
}
