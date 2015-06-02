/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.servlets.Config;
import java.util.Date;

/**
 * Simple html output mode. Can be used on scheduling because the output does
 * not depend on other files (css etc) and it is a standalone page
 *
 * @author Enrico Liboni
 */
public class HtmlPlainOutput extends StandardOutput {

	private final String CLOSE_RESULTS_TABLE_HTML = "</tr></table></div></body></html>";

	private boolean displayInline; //whether display is inline in the showparams page. to avoid duplicate display of parameters
	private boolean standalone;

	public HtmlPlainOutput(boolean standalone) {
		this.standalone = standalone;
	}

	/**
	 * @return the displayInline
	 */
	public boolean isDisplayInline() {
		return displayInline;
	}

	/**
	 * @param displayInline the displayInline to set
	 */
	public void setDisplayInline(boolean displayInline) {
		this.displayInline = displayInline;
	}

	@Override
	public void beginHeader() {
		if (standalone) {
			out.println("<html>");
			out.println("<head>");
			out.println("<meta charset='utf-8'>");
			out.println("</head>");
			out.println("<body>");
		}

		//style should be in the head section. put in body for correct display in email inline jobs
		//TODO test
		//https://www.campaignmonitor.com/css/
		out.println("<style>"
				+ "table { border-collapse: collapse; }"
				+ "\n td { background-color: #FFFFFF; border: 1px solid #000000; font-size: 10pt; }"
				+ "\n body { font-family: Verdana, Helvetica , Arial, SansSerif; color: #000000; }"
				+ "</style>");

		//start results table
		out.println("<div align='center'>");
		out.println("<table border='0' width='90%' cellspacing='1'"
				+ " cellpadding='1'>");
		out.println("<tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		out.println("<td><b>" + value + "</b></td>");
	}

	@Override
	public void endHeader() {
		out.println("</tr>");
	}

	@Override
	public void addCellString(String value) {
		out.println("<td style='text-align: left'>" + value + "</td>");
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue;

		if (value == null) {
			formattedValue = null;
		} else {
			formattedValue = actualNumberFormatter.format(value);
		}

		out.println("<td style='text-align: right'>" + formattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = Config.getDateDisplayString(value);
		}

		out.println("<td style='text-align: left'>" + formattedValue + "</td>");
	}

	@Override
	public void newRow() {
		if (rowCount > 1) {
			//close previous row
			out.println("</tr>");
		}

		//open new row
		out.println("<tr>");
	}

	@Override
	public void endRows() {
		out.println("</tr></table></div>");

		if (standalone) {
			out.println("</body></html>");
		}
	}

}
