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
package art.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.apache.commons.lang3.StringUtils;

/**
 * Filters log events using a level and comma separated list of logger names
 *
 * @author Timothy Anyona
 */
public class LevelAndLoggerFilter extends Filter<ILoggingEvent> {
	//https://github.com/qos-ch/logback/blob/master/logback-classic/src/main/java/ch/qos/logback/classic/filter/ThresholdFilter.java

	private final Level level;
	private final String loggers;

	public LevelAndLoggerFilter(Level level, String loggers) {
		this.level = level;
		if (loggers == null) {
			loggers = "";
		}
		loggers = StringUtils.deleteWhitespace(loggers);
		this.loggers = loggers;
	}

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (!isStarted()) {
			return FilterReply.NEUTRAL;
		}

		boolean loggersOk;
		if (StringUtils.isBlank(loggers)) {
			loggersOk = true;
		} else {
			String[] loggersArray = StringUtils.split(loggers, ",");
			loggersOk = StringUtils.startsWithAny(event.getLoggerName(), loggersArray);
		}

		if (event.getLevel().isGreaterOrEqual(level) && loggersOk) {
			return FilterReply.NEUTRAL;
		} else {
			return FilterReply.DENY;
		}
	}
}
