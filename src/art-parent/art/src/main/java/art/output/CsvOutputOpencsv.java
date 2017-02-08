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
package art.output;

import com.opencsv.CSVWriter;
import com.opencsv.ResultSetHelperService;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates csv output. Also supports any delimiter, other than comma.
 *
 * @author Timothy Anyona
 */
public class CsvOutputOpencsv {

	private static final Logger logger = LoggerFactory.getLogger(CsvOutputOpencsv.class);

	private CSVWriter csvWriter;
	private String dateFormat = "dd-MMM-yyyy";
	private String dateTimeFormat = "dd-MMM-yyy HH:mm:ss";
	private boolean includeHeaders = true;
	//https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html
	private char separator = CSVWriter.DEFAULT_SEPARATOR;
	private char quotechar = CSVWriter.DEFAULT_QUOTE_CHARACTER;

	/**
	 * @return the separator
	 */
	public char getSeparator() {
		return separator;
	}

	/**
	 * @param separator the separator to set
	 */
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	/**
	 * @return the quotechar
	 */
	public char getQuotechar() {
		return quotechar;
	}

	/**
	 * @param quotechar the quotechar to set
	 */
	public void setQuotechar(char quotechar) {
		this.quotechar = quotechar;
	}

	/**
	 * @return the dateFormat
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @return the dateTimeFormat
	 */
	public String getDateTimeFormat() {
		return dateTimeFormat;
	}

	/**
	 * @param dateTimeFormat the dateTimeFormat to set
	 */
	public void setDateTimeFormat(String dateTimeFormat) {
		this.dateTimeFormat = dateTimeFormat;
	}

	/**
	 * @return the includeHeaders
	 */
	public boolean isIncludeHeaders() {
		return includeHeaders;
	}

	/**
	 * @param includeHeaders the includeHeaders to set
	 */
	public void setIncludeHeaders(boolean includeHeaders) {
		this.includeHeaders = includeHeaders;
	}

	/**
	 * Writes the data in the given resultset to the given writer in csv format
	 *
	 * @param rs the resultset that contains the data to ouput
	 * @param outputWriter the writer to output to
	 * @throws SQLException
	 * @throws IOException
	 */
	public void generateOutput(ResultSet rs, Writer outputWriter) throws SQLException, IOException {
		logger.debug("Entering generateOutput");
		//http://opencsv.sourceforge.net/apidocs/com/opencsv/CSVWriter.html
		//https://stackoverflow.com/a/36974864/3274227
		//howtodoinjava.com/3rd-party/parse-read-write-csv-files-opencsv-tutorial/
		csvWriter = new CSVWriter(outputWriter, separator, quotechar);

		//https://stackoverflow.com/questions/33476281/change-the-default-date-format-while-exporting-the-resultset-data-using-opencsv
		ResultSetHelperService helperService = new ResultSetHelperService();
		helperService.setDateFormat(dateFormat);
		helperService.setDateTimeFormat(dateTimeFormat);

		csvWriter.setResultService(helperService);
		csvWriter.writeAll(rs, includeHeaders);
		csvWriter.flush();
		csvWriter.close(); //will close the underlying writer (outputWriter)
		//?don't close csvWriter - which will close the underlying writer (outputWriter)
		//we don't own outputWriter
	}

	/**
	 * Closes the csv writer used to generate the output
	 *
	 * @throws IOException
	 */
	public void closeCsvWriter() throws IOException {
		//https://stackoverflow.com/questions/27095363/opencsv-not-writing-to-a-file
		if (csvWriter != null) {
			csvWriter.close();
		}
	}
}
