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
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

/**
 * Evaluator to use with an SMTPAppender to trigger an email when a log event of
 * a given level or above is encountered
 *
 * @author Timothy Anyona
 */
public class OnLevelEvaluator extends EventEvaluatorBase<ILoggingEvent> {
	//https://github.com/qos-ch/logback/blob/master/logback-classic/src/main/java/ch/qos/logback/classic/boolex/OnErrorEvaluator.java

	private final Level level;

	public OnLevelEvaluator(Level level) {
		//https://stackoverflow.com/questions/10508107/why-call-super-in-a-constructor
		this.level = level;
	}

	@Override
	public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException {
		return event.getLevel().levelInt >= level.levelInt;
	}

}
