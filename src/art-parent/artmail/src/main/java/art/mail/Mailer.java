/*
 * Copyright (C)   Enrico Liboni  
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the LGPL License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. *  
 */
package art.mail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Mailer is a simple object created to send email with attachments.
 *
 * Usage example: <ul> <li> From command prompt:<br>
 * <code>java Mailer smtp.server
 * type from subject "message" to.addr1 to.addr2 ...</code> </li> <li> From
 * other objects:<br>
 * <code> Mailer m = new Mailer(); <br> m.setSmtpHost("smpt.server.com");
 * // optional <br> m.setUsername("smpt.username"); // optional <br>
 * m.setPassword("smpt.password"); <br> m.setType("text/html"); <i>// or
 * m.setType("text/plain"); </i><br> m.setFrom("sender@server.com"); <br>
 * m.setSubject("The Subject"); <br> m.setMessage("The message body"); <i>// can
 * be html </i><br> m.setTo("to@server.com"); <br> <i>// if t is an array with
 * e-mail addresses: m.setTos(t);<br> // attachments </i><br>
 * m.setAttachments(v); <br> <br> <i>// Send and handle the feedback </i> <br>
 * if (!m.send()) { <br> System.out.println("Error Sending message");<br> }<br>
 * </code> </li> </ul> Note: the activation.jar and mail.jar (javax.mail) have
 * to be in the classpath
 *
 */

public class Mailer {

	String[] tos; //recipient email addresses
	boolean isToSet = false; // false = not set, true = (or multiple or  single)
	boolean areThereAttachments = false;
	String subject = "";
	String message = "";
	String from = "";
	String type = "text/plain";
	String username, password; // used for smtpHost authentication
	List<File> attachments;
	String smtpHost = "";
	String smtpPort;
	String secureSmtp;
	String sendError = ""; //error message if sending fails
	boolean sessionDebug = false; //debug mode for session
	String[] cc;
	String[] bcc;

	/**
	 * Create a Mailer Object. <br>
	 *
	 * The default type is text/plain
	 */
	public Mailer() {
	}

	public void setCc(String[] value) {
		cc = value;
	}

	public void setBcc(String[] value) {
		bcc = value;
	}

	/**
	 * Get the error if any of an attempt to send an email
	 *
	 * @return the exception message
	 */
	public String getSendError() {
		return sendError;
	}

	/**
	 * Determine if the email session should be in debug mode
	 *
	 * @param value
	 */
	public void setSessionDebug(boolean value) {
		sessionDebug = value;
	}

	/**
	 *
	 * @param value
	 */
	public void setSmtpPort(String value) {
		smtpPort = value;
	}

	/**
	 *
	 * @param value
	 */
	public void setSecureSmtp(String value) {
		secureSmtp = value;
	}

	/**
	 * Set smtp server name
	 *
	 * @param s
	 */
	public void setSmtpHost(String s) {
		smtpHost = s;
	}

	/**
	 * Set message type (
	 * <code>text/plain</code> or
	 * <code>text/html</code>)
	 *
	 * @param s
	 */
	public void setType(String s) {
		type = s;
	}

	/**
	 * Set Subject
	 *
	 * @param s
	 */
	public void setSubject(String s) {
		subject = s;
	}

	/**
	 * Set Message body
	 *
	 * @param s
	 */
	public void setMessage(String s) {
		message = s;
	}

	/**
	 * Set From mail address <br>
	 *
	 * note: your smtp server may deny to send mails from an unknown address
	 *
	 * @param s
	 */
	public void setFrom(String s) {
		from = s;
	}

	/**
	 * Set To mail address<br>
	 *
	 * note: your smtp server may deny to send mails to an unknown address
	 *
	 * @param s
	 */
	public void setTo(String s) {
		if (isToSet == false) {
			tos = new String[1];
			tos[0] = s;
			isToSet = true;
		}
	}

	public void setToForce(String s) {
		tos=null;
		tos = new String[1];
		tos[0] = s;
	}

	/**
	 * Set the "to" list of email address.<br>
	 *
	 * t is a String[] of e-mail addresses
	 *
	 * @param t
	 */
	public void setTos(String[] t) {
		if (isToSet == false) {
			tos = t;
			isToSet = true;
		}
	}
	
	public void setTosForce(String[] t){
		tos=null;
		tos = t;
	}

	/**
	 * Set attachments<br> l is a List of valid File objects. <br> To create the
	 * list for a single file, just type:<br>
	 * <code> List v = new
	 * ArrayList();<br> v.add(new File("string path")); </code>
	 *
	 * @param l
	 */
	public void setAttachments(List<File> l) {
		areThereAttachments = true;

		attachments = null;
		attachments = l;
	}

	/**
	 * Set username for smtp authentication (optional) <br>
	 *
	 * note: your smtp server need to support this feature. Leave null if not
	 * used
	 *
	 * @param s
	 */
	public void setUsername(String s) {
		username = s;
	}

	/**
	 * Set password for smtp authentication (optional) <br>
	 *
	 * note: your smtp server need to support this feature. Leave null if not
	 * used
	 *
	 * @param s
	 */
	public void setPassword(String s) {
		password = s;
	}

	/**
	 * Pack and send the email. <br>
	 *
	 * Exception <i>MessagingException</i> and <i>IOException</i> are thrown,
	 * otherwise, in case of errors, returns false and write the exception on
	 * the standard error.
	 *
	 * @return
	 */
	public boolean send() {
		// here we should check params
		// ...
		try {
			sendError = "";
			sendEmail();
			return true;
		} catch (Exception e) {
			System.out.println("artmail: Error when sending email: " + e);
			sendError = e.toString();
			e.printStackTrace(System.out);
			return false;
		}
	}

	private void sendEmail() throws MessagingException, IOException {
		//Set smtp properties
		Properties props = new Properties();

		//enable secure smtp
		if (secureSmtp != null && secureSmtp.equals("starttls")) {
			props.put("mail.smtp.starttls.enable", "true");
		}
		//smtp port may not be 25
		if (smtpPort != null) {
			props.put("mail.smtp.port", smtpPort);
		}

		props.put("mail.smtp.host", smtpHost);

		// If you're sending to multiple recipients, if one recipient address fails, by default no email is sent to the other recipients
		// set the sendpartial property to true to have emails sent to the valid addresses, even if invalid ones exist
		props.put("mail.smtp.sendpartial", "true");


		//get the default Session            
		Session session;
		if (username != null && password != null) {
			//smtp authentication enabled
			props.put("mail.smtp.auth", "true");
			Authenticator auth = new SMTPAuthenticator();
			session = Session.getInstance(props, auth);
		} else {
			session = Session.getDefaultInstance(props, null);
		}

		//enable session debug
		session.setDebug(sessionDebug);

		// Create a message
		Message msg = new MimeMessage(session);

		// Set the FROM: address
		InternetAddress addressFrom = new InternetAddress(from);
		msg.setFrom(addressFrom);

		// Set the TO: address(es)
		if (tos != null) {
			InternetAddress[] addressTo = new InternetAddress[tos.length];
			for (int i = 0; i < tos.length; i++) {
				addressTo[i] = new InternetAddress(tos[i]);
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
		}

		// Set the CC: address(es)
		if (cc != null) {
			InternetAddress[] addressCc = new InternetAddress[cc.length];
			for (int i = 0; i < cc.length; i++) {
				addressCc[i] = new InternetAddress(cc[i]);
			}
			msg.setRecipients(Message.RecipientType.CC, addressCc);
		}

		// Set the BCC: address(es)
		if (bcc != null) {
			InternetAddress[] addressBcc = new InternetAddress[bcc.length];
			for (int i = 0; i < bcc.length; i++) {
				addressBcc[i] = new InternetAddress(bcc[i]);
			}
			msg.setRecipients(Message.RecipientType.BCC, addressBcc);
		}

		// Optional custom headers (?) 
		//msg.addHeader("MyHeaderName", "myHeaderValue"); //removed

		// Setting the Subject and Content Type
		msg.setSubject(subject);

		if (!areThereAttachments) {
			msg.setContent(message, type);
		} else {

			MimeMultipart mp = new MimeMultipart();

			MimeBodyPart text = new MimeBodyPart();
			text.setDisposition(Part.INLINE);
			text.setContent(message, type);
			mp.addBodyPart(text);

			for (int i = 0; i < attachments.size(); i++) {
				MimeBodyPart file_part = new MimeBodyPart();
				File file = (File) attachments.get(i);
				FileDataSource fds = new FileDataSource(file);
				DataHandler dh = new DataHandler(fds);
				file_part.setFileName(file.getName());
				file_part.setDisposition(Part.ATTACHMENT);
				file_part.setDescription("Attached file: " + file.getName());
				file_part.setDataHandler(dh);
				mp.addBodyPart(file_part);
			}

			msg.setContent(mp);
		}
		// Send the e-mail
		Transport.send(msg);
	}

	/**
	 * Static method to send mail from command line.<br>
	 *
	 * Usage:
	 * <code>java Mailer smtp.server type from subject "message" to.addr1
	 * to.addr2 ...</code><br> Note: mail.jar and activation.jar must be in the
	 * classpath
	 *
	 * @param s
	 */
	public static void main(String[] s) {

		// Create the Mailer Object
		Mailer m = new Mailer();
		// set parameters
		System.out.println("SMTP:    " + s[0]);
		m.setSmtpHost(s[0]);
		System.out.println("Type:    " + s[1]);
		m.setType(s[1]);
		System.out.println("From:    " + s[2]);
		m.setFrom(s[2]);
		System.out.println("Subject: " + s[3]);
		m.setSubject(s[3]);
		System.out.println("Message: " + s[4]);
		m.setMessage(s[4]);
		// set one or multiple TO address
		if (s.length == 6) {
			System.out.println("To:      " + s[5]);
			m.setTo(s[5]);
		} else {
			String[] t = new String[s.length - 5];
			for (int i = 0; i < s.length - 5; i++) {
				t[i] = s[i + 5];
				System.out.println("To:      " + t[i]);
			}
			m.setTos(t);
		}
		System.out.println("Sending...");
		// Send and handle the feedback
		if (m.send()) {
			System.out.println("Sent");
		} else {
			System.out.println("Error");
		}
	}

	private class SMTPAuthenticator extends javax.mail.Authenticator {

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password);
		}
	}
}
