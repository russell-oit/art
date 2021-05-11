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

import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Generates RSS 2.0 output. The query column names MUST follow the RSS 2.0
 * naming convention for &lt;item&gt; i.e. at least one of "title" or
 * "description" must exists. for other valid columns names (i.e. item sub-tags)
 * refer to the RSS 2.0 specs. (pubDate and guid should be there) <br>
 * select col1 "title", col2 "description" [, col3 "pubDate", col4 "guid", ...]
 * from ...
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class Rss20Output extends StandardOutput {
	//https://validator.w3.org/feed/

	private int columnIndex = 0; // current column
	private String[] columnNames;
	private int localRowCount;

	//rfc822 (2822) date
	//dates should not be localized. must use english locale.
	//https://validator.w3.org/feed/docs/error/InvalidRFC2822Date.html
	private final SimpleDateFormat Rfc822DateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

	@Override
	public String getContentType() {
		return "application/xml"; // mime type (use "text/html" for html)
	}

	@Override
	public boolean outputHeaderAndFooter() {
		return false;
	}

	/**
	 * Resets global variables in readiness for output generation. Especially
	 * important for burst output where the same standard output object is
	 * reused for multiple output runs.
	 */
	private void resetVariables() {
		columnIndex = 0;
		columnNames = null;
		localRowCount = 0;
	}

	@Override
	public void init() {
		resetVariables();

		columnNames = new String[totalColumnCount]; // stores columns names (i.e. xml tags for items)
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		// RSS 2.0 Header
		out.println("<rss version=\"2.0\">");
		// Channel definition
		out.println("<channel>");
		out.println("<title>" + reportName + "</title>");
		out.println("<link>" + Config.getSettings().getRssLink() + "</link>");
		out.println("<description>" + reportName + " ART Feed</description>");
		out.println("<pubDate>" + Rfc822DateFormat.format(new Date()) + "</pubDate> ");
		out.println("<generator>http://art.sourceforge.net</generator> ");
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (CollectionUtils.isEmpty(reportParamsList)) {
			return;
		}

		//needs to be tested, this is not RSS 2.0 compliant
		// hence, do not check the show params flag if not for debugging
		out.println("<art:reportparams>");
		for (ReportParameter reportParam : reportParamsList) {
			String paramName = reportParam.getParameter().getName();
			String displayValues = reportParam.getDisplayValues();

			String outputString = "<art:name>" + StringEscapeUtils.escapeXml10(paramName)
					+ "</art:name><art:value>" + StringEscapeUtils.escapeXml10(displayValues) + "<art:value>";

			out.println("<art:param>");
			out.println(outputString);
			out.println("</art:param>");
		}
		out.println("</art:reportparams>");
	}

	@Override
	public void addHeaderCell(String value) {
		columnNames[columnIndex] = value;
		columnIndex++;
	}

	@Override
	public void addCellString(String value) {
		out.println("<" + columnNames[columnIndex] + ">"
				+ StringEscapeUtils.escapeXml10(value)
				+ "</" + columnNames[columnIndex] + ">");
		columnIndex++;
	}

	@Override
	public void addCellNumeric(Double value) {
		out.println("<" + columnNames[columnIndex] + ">" + value + "</" + columnNames[columnIndex] + ">");
		columnIndex++;
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = Rfc822DateFormat.format(value);
		}

		out.println("<" + columnNames[columnIndex] + ">"
				+ formattedValue + "</" + columnNames[columnIndex] + ">");
		columnIndex++;
	}
	
	@Override
	public void addCellTime(Date value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = ArtUtils.isoTimeSecondsFormatter.format(value);
		}

		out.println("<" + columnNames[columnIndex] + ">"
				+ formattedValue + "</" + columnNames[columnIndex] + ">");
		columnIndex++;
	}

	@Override
	public void newRow() {
		localRowCount++;
		columnIndex = 0; // reset column index

		if (localRowCount > 1) {
			//close previous row
			out.println("</item>\n");
		}

		//open new row
		out.println("<item>");
	}

	@Override
	public void endRow() {
		out.println("</item>");
	}

	@Override
	public void endOutput() {
		out.println("</channel>");
		out.println("</rss>");
	}
}
