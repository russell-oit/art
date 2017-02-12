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

import com.univocity.parsers.common.processor.ObjectRowWriterProcessor;
import com.univocity.parsers.conversions.Conversions;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvRoutines;
import com.univocity.parsers.csv.CsvWriterSettings;
import java.io.Writer;
import java.sql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates csv output. Also supports any delimiter, other than comma.
 *
 * @author Timothy Anyona
 */
public class CsvOutputUnivocity {

	private static final Logger logger = LoggerFactory.getLogger(CsvOutputUnivocity.class);

	private String dateFormat = "dd-MMM-yyyy";
	private String dateTimeFormat = "dd-MMM-yyy HH:mm:ss";
	private boolean includeHeaders = true;
	private char delimiter = ',';
	private char quote = '"';
	private boolean quoteAllFields = false;

	/**
	 * @return the delimiter
	 */
	public char getDelimiter() {
		return delimiter;
	}

	/**
	 * @param delimiter the delimiter to set
	 */
	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * @return the quote
	 */
	public char getQuote() {
		return quote;
	}

	/**
	 * @param quote the quote to set
	 */
	public void setQuote(char quote) {
		this.quote = quote;
	}

	/**
	 * @return the quoteAllFields
	 */
	public boolean isQuoteAllFields() {
		return quoteAllFields;
	}

	/**
	 * @param quoteAllFields the quoteAllFields to set
	 */
	public void setQuoteAllFields(boolean quoteAllFields) {
		this.quoteAllFields = quoteAllFields;
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

	public void generateOutput(ResultSet rs, Writer outputWriter) {
		logger.debug("Entering generateOutput");
		//https://stackoverflow.com/questions/37556698/mysql-dump-character-escaping-and-csv-read
		//https://stackoverflow.com/a/36974864/3274227
		//https://github.com/uniVocity/univocity-parsers/issues/133#issuecomment-278208696
		ObjectRowWriterProcessor processor = new ObjectRowWriterProcessor();
		//assigns a "global" date format conversion for any timestamp that gets written.
		//you can also define field-specific conversions, which will override the default set here.
		processor.convertType(java.sql.Timestamp.class, Conversions.toDate(dateTimeFormat));
		processor.convertType(java.sql.Date.class, Conversions.toDate(dateFormat));

		//http://docs.univocity.com/parsers/2.0.0/com/univocity/parsers/csv/CsvWriterSettings.html
		//http://docs.univocity.com/parsers/2.0.0/com/univocity/parsers/common/CommonWriterSettings.html
		//http://docs.univocity.com/parsers/2.0.0/com/univocity/parsers/common/CommonSettings.html
		//https://stackoverflow.com/questions/36936943/jackson-serialize-csv-property-order
		CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
		csvWriterSettings.setRowWriterProcessor(processor);
		csvWriterSettings.setHeaderWritingEnabled(includeHeaders);
		csvWriterSettings.setQuoteAllFields(quoteAllFields);

		//http://docs.univocity.com/parsers/2.0.0/com/univocity/parsers/csv/CsvFormat.html
		CsvFormat csvFormat = csvWriterSettings.getFormat();
		csvFormat.setDelimiter(delimiter);
		csvFormat.setQuote(quote);

		CsvRoutines csvRoutines = new CsvRoutines(csvWriterSettings);
		csvRoutines.write(rs, outputWriter);
	}

}
