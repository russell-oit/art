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
package art.output;

import art.reportoptions.CsvOutputArtOptions;
import art.servlets.Config;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Generates csv output. Also supports any delimiter, other than comma.
 *
 * @author Timothy Anyona
 */
public class CsvOutputArt extends StandardOutput {

	private FileOutputStream fout;
	private StringBuilder sb;
	private final int FLUSH_SIZE = 1024 * 4; // flush to disk each 4kb of columns ;)
	private final CsvOutputArtOptions options;

	public CsvOutputArt(CsvOutputArtOptions options) {
		Objects.requireNonNull(options, "options must not be null");

		this.options = options;
	}

	/**
	 * Resets global variables in readiness for output generation. Especially
	 * important for burst output where the same standard output object is
	 * reused for multiple output runs.
	 */
	private void resetVariables() {
		fout = null;
		sb = null;
	}

	@Override
	public void init() {
		resetVariables();

		sb = new StringBuilder(8 * 1024);

		try {
			if (isJob) {
				fout = new FileOutputStream(fullOutputFileName);
			}
		} catch (FileNotFoundException ex) {
			endOutput();
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addHeaderCell(String value) {
		appendValue(value);
	}

	@Override
	public void addCellString(String value) {
		appendValue(value);
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = plainNumberFormatter.format(value.doubleValue());
		}

		appendValue(formattedValue);
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = Config.getDateDisplayString(value);
		appendValue(formattedValue);
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		appendValue(formattedValue);
	}

	@Override
	public void newRow() {
		sb.append("\n");

		if ((rowCount * totalColumnCount) > FLUSH_SIZE) {
			try {
				String tmpstr = sb.toString();
				byte[] buf = tmpstr.getBytes("UTF-8");

				if (isJob) {
					fout.write(buf);
					fout.flush();
				} else {
					out.write(tmpstr);
				}

				sb = new StringBuilder(8 * 1024);
			} catch (IOException ex) {
				endOutput();
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public void endOutput() {
		try {
			String tmpstr = sb.toString();
			byte[] buf = tmpstr.getBytes("UTF-8");

			if (isJob) {
				if (fout != null) {
					fout.write(buf);
					fout.flush();
					fout.close();
					fout = null; // these nulls are because it seems to be a memory leak in some JVMs
				}
			} else {
				out.write(tmpstr);
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public boolean outputHeaderAndFooter() {
		return false;
	}

	/**
	 * Quotes the given value for output
	 *
	 * @param value the value to output
	 * @return the quoted value to output
	 */
	private String getQuotedValue(String value) {
		String quotedValue = value;
		String delimiter = options.getDelimiter();

		if (StringUtils.isNotBlank(delimiter)) {
			if (StringUtils.contains(value, delimiter)) {
				String quoteChar = options.getQuoteChar();
				quotedValue = quoteChar + value + quoteChar;
			}
		}

		return quotedValue;
	}

	/**
	 * Appends the given value to the output, quoting it if necessary
	 *
	 * @param value the value to append
	 */
	private void appendValue(String value) {
		String quotedValue = getQuotedValue(value);
		if (currentColumnIndex > 1) {
			sb.append(options.getDelimiter());
		}
		sb.append(quotedValue);
	}

}
