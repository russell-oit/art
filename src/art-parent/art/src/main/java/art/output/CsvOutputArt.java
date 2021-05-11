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

import art.enums.ZipType;
import art.reportoptions.CsvOutputArtOptions;
import art.servlets.Config;
import art.utils.FilenameHelper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Generates csv output
 *
 * @author Timothy Anyona
 */
public class CsvOutputArt extends StandardOutput {

	private FileOutputStream fout;
	private ZipOutputStream zout;
	private StringBuilder sb;
	private final int FLUSH_SIZE = 1024 * 4; // flush to disk each 4kb of columns ;)
	private final CsvOutputArtOptions options;
	private int currentColumnIndex = 1;
	private ZipType zipType = ZipType.None;
	private int localRowCount;

	public CsvOutputArt(CsvOutputArtOptions options, ZipType zipType) {
		Objects.requireNonNull(options, "options must not be null");

		this.options = options;
		this.zipType = zipType;
	}

	/**
	 * Resets global variables in readiness for output generation. Especially
	 * important for burst output where the same standard output object is
	 * reused for multiple output runs.
	 */
	private void resetVariables() {
		fout = null;
		sb = null;
		currentColumnIndex = 1;
		localRowCount=0;
	}

	@Override
	public void init() {
		resetVariables();

		sb = new StringBuilder(8 * 1024);

		try {
			fout = new FileOutputStream(fullOutputFileName);

			if (zipType == ZipType.Zip) {
				String filename = FilenameUtils.getBaseName(fullOutputFileName);
				FilenameHelper filenameHelper = new FilenameHelper();
				String zipEntryFilenameExtension = filenameHelper.getCsvExtension(report);
				String zipEntryFilename = filename + "." + zipEntryFilenameExtension;
				ZipEntry ze = new ZipEntry(zipEntryFilename);
				zout = new ZipOutputStream(fout);
				zout.putNextEntry(ze);
			}
		} catch (IOException ex) {
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
	public void addCellTime(Date value) {
		String formattedValue = Config.getTimeDisplayString(value);
		appendValue(formattedValue);
	}
	
	@Override
	public void addCellTime(Date timeValue, String formattedValue, long sortValue) {
		appendValue(formattedValue);
	}

	@Override
	public void newRow() {
		localRowCount++;
		
		sb.append("\n");

		currentColumnIndex = 1;// reset column index

		if ((localRowCount * totalColumnCount) > FLUSH_SIZE) {
			try {
				String tmpstr = sb.toString();
				byte[] buf = tmpstr.getBytes("UTF-8");

				if (zout == null) {
					fout.write(buf);
					fout.flush();
				} else {
					zout.write(buf);
					zout.flush();
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

			if (zout == null) {
				if (fout != null) {
					fout.write(buf);
					fout.flush();
				}
			} else {
				zout.write(buf);
				zout.flush();
				zout.close();
				zout = null;
			}

			if (fout != null) {
				fout.close();
			}

			fout = null; // these nulls are because it seems to be a memory leak in some JVMs
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
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
				String quoteChar = options.getQuote();
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

		currentColumnIndex++;
	}

}
