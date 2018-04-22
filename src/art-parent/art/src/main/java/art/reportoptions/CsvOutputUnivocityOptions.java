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
package art.reportoptions;

/**
 * Represents report options for the CSV report type
 * 
 * @author Timothy Anyona
 */
public class CsvOutputUnivocityOptions extends CommonUnivocityOptions {
	
	private static final long serialVersionUID = 1L;
	private char delimiter = ',';
	private char quote = '"';
	private boolean quoteAllFields;

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
	
}
