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
package art.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PrintWriter that overrides the close method and does a flush instead of
 * close
 *
 * @author Timothy Anyona
 */
public class UncloseablePrintWriter extends PrintWriter {
	//https://stackoverflow.com/questions/8941298/system-out-closed-can-i-reopen-it

	private static final Logger logger = LoggerFactory.getLogger(UncloseablePrintWriter.class);

	public UncloseablePrintWriter(Writer out) {
		super(out);
	}

	@Override
	public void close() {
		try {
			out.flush();
		} catch (IOException ex) {
			logger.error("Error", ex);
		}
	}

}
