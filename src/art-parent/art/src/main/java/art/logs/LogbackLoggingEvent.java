/*
 * ART. A Reporting Tool.
 * Copyright (C) 2019 Enrico Liboni <eliboni@users.sf.net>
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
package art.logs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import java.io.Serializable;
import java.util.Map;
import org.owasp.encoder.Encode;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

/**
 * Represents a logback logging event
 *
 * @author Timothy Anyona
 */
public class LogbackLoggingEvent implements Serializable {

	//https://github.com/qos-ch/logback/blob/master/logback-classic/src/main/java/ch/qos/logback/classic/spi/LoggingEventVO.java
	//https://github.com/qos-ch/logback/blob/master/logback-classic/src/main/java/ch/qos/logback/classic/spi/LoggingEventVO.java
	private static final long serialVersionUID = 1L;
	private String causedByText = "Caused by";
	private String commonFramesOmittedText = "common frames omitted";

	private String threadName;
	private String loggerName;
	private LoggerContextVO loggerContextVO;

	private transient Level level;
	private String message;

	// we gain significant space at serialization time by marking
	// formattedMessage as transient and constructing it lazily in
	// getFormattedMessage()
	private transient String formattedMessage;

	private transient Object[] argumentArray;

	private ThrowableProxyVO throwableProxy;
	private StackTraceElement[] callerDataArray;
	private Marker marker;
	private Map<String, String> mdcPropertyMap;
	private long timeStamp;

	public static LogbackLoggingEvent build(ILoggingEvent le) {
		LogbackLoggingEvent lle = new LogbackLoggingEvent();
		lle.loggerName = le.getLoggerName();
		lle.loggerContextVO = le.getLoggerContextVO();
		lle.threadName = le.getThreadName();
		lle.level = (le.getLevel());
		lle.message = (le.getMessage());
		lle.argumentArray = (le.getArgumentArray());
		lle.marker = le.getMarker();
		lle.mdcPropertyMap = le.getMDCPropertyMap();
		lle.timeStamp = le.getTimeStamp();
		lle.throwableProxy = ThrowableProxyVO.build(le.getThrowableProxy());
		// add caller data only if it is there already
		// fixes http://jira.qos.ch/browse/LBCLASSIC-145
		if (le.hasCallerData()) {
			lle.callerDataArray = le.getCallerData();
		}
		return lle;
	}

	public String getThreadName() {
		return threadName;
	}

	public LoggerContextVO getLoggerContextVO() {
		return loggerContextVO;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public Level getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}

	public String getFormattedMessage() {
		if (formattedMessage != null) {
			return formattedMessage;
		}

		if (argumentArray != null) {
			formattedMessage = MessageFormatter.arrayFormat(message, argumentArray).getMessage();
		} else {
			formattedMessage = message;
		}

		return formattedMessage;
	}

	public Object[] getArgumentArray() {
		return argumentArray;
	}

	public IThrowableProxy getThrowableProxy() {
		return throwableProxy;
	}

	public StackTraceElement[] getCallerData() {
		return callerDataArray;
	}

	public boolean hasCallerData() {
		return callerDataArray != null;
	}

	public Marker getMarker() {
		return marker;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public long getContextBirthTime() {
		return loggerContextVO.getBirthTime();
	}

	public LoggerContextVO getContextLoggerRemoteView() {
		return loggerContextVO;
	}

	public Map<String, String> getMDCPropertyMap() {
		return mdcPropertyMap;
	}

	public Map<String, String> getMdc() {
		return mdcPropertyMap;
	}

	/**
	 * @return the causedByText
	 */
	public String getCausedByText() {
		return causedByText;
	}

	/**
	 * @param causedByText the causedByText to set
	 */
	public void setCausedByText(String causedByText) {
		this.causedByText = causedByText;
	}

	/**
	 * @return the commonFramesOmittedText
	 */
	public String getCommonFramesOmittedText() {
		return commonFramesOmittedText;
	}

	/**
	 * @param commonFramesOmittedText the commonFramesOmittedText to set
	 */
	public void setCommonFramesOmittedText(String commonFramesOmittedText) {
		this.commonFramesOmittedText = commonFramesOmittedText;
	}

	/**
	 * Returns the exception details, formatted for html output
	 *
	 * @return exception details, formatted for html output
	 */
	public String getFormattedException() {
		//https://github.com/qos-ch/logback/blob/master/logback-classic/src/main/java/ch/qos/logback/classic/html/DefaultThrowableRenderer.java
		IThrowableProxy tp = getThrowableProxy();

		StringBuilder sb = new StringBuilder();

		while (tp != null) {
			render(sb, tp);
			tp = tp.getCause();
		}

		String formattedExceptionString = sb.toString();

		return formattedExceptionString;
	}

	private void render(StringBuilder sbuf, IThrowableProxy tp) {
		printFirstLine(sbuf, tp);

		final String TRACE_PREFIX = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;";

		int commonFrames = tp.getCommonFrames();
		StackTraceElementProxy[] stepArray = tp.getStackTraceElementProxyArray();

		for (int i = 0; i < stepArray.length - commonFrames; i++) {
			StackTraceElementProxy step = stepArray[i];
			sbuf.append(TRACE_PREFIX);
			sbuf.append(Encode.forHtmlContent(step.toString()));
			sbuf.append(System.lineSeparator());
		}

		if (commonFrames > 0) {
			sbuf.append(TRACE_PREFIX);
			sbuf.append("\t... ").append(commonFrames).append(" ").append(commonFramesOmittedText)
					.append(System.lineSeparator());
		}
	}

	private void printFirstLine(StringBuilder sb, IThrowableProxy tp) {
		int commonFrames = tp.getCommonFrames();
		if (commonFrames > 0) {
			sb.append("<br/>").append(causedByText).append(": ");
		}
		sb.append(tp.getClassName()).append(": ").append(Encode.forHtmlContent(tp.getMessage()));
		sb.append(System.lineSeparator());
	}

}
