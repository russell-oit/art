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

import art.reportparameter.ReportParameter;
import art.servlets.Config;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Generate RSS 2.0 output. The query column names MUST follow the RSS 2.0
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

	int numberOfLines = 0;
	String queryName;
	int maxRows;
	int columns;
	int columnIndex = 0; // current column
	String[] columnNames;

	/**
	 * rfc822 (2822) standard date
	 */
	public final SimpleDateFormat Rfc822DateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z");

	/**
	 *
	 * @param date
	 * @return rfc822 representation of date
	 */
	public String getDateAsRFC822String(Date date) {
		return Rfc822DateFormat.format(date);
	}

	@Override
	public String getContentType() {
		return "application/xml"; // mime type (use "text/html" for html)
	}

	@Override
	public boolean outputHeaderandFooter() {
		return false;
	}

	@Override
	public void init() {
		columnNames = new String[resultSetColumnCount]; // sotres columns names (i.e. xml tags for items)
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

		// RSS 2.0 Header
		out.println("<rss version=\"2.0\">");
		// Channel definition
		out.println("<channel>");
		out.println("<title>" + reportName + "</title>");
		out.println("<link>" + Config.getSettings().getRssLink() + "</link>");
		out.println("<description>" + reportName + " ART Feed</description>");
		out.println("<pubDate>" + getDateAsRFC822String(new Date()) + "</pubDate> ");
		out.println("<generator>http://art.sourceforge.net</generator> ");
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		//TODO needs to be tested, this is not RSS 2.0 compliant
		// hence, do not check the show params flag if not for debugging
		out.println("<art:reportparams>");
		for (ReportParameter reportParam : reportParamsList) {
			String paramName = reportParam.getParameter().getName();
			String displayValues = reportParam.getDisplayValues();

			String outputString = "<art:name>" + StringEscapeUtils.escapeXml(paramName)
					+ "</art:name><art:value>" + StringEscapeUtils.escapeXml(displayValues) + "<art:value>";

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
	public void beginRows() {
		out.println("<item>");
	}

	@Override
	public void addCellString(String value) {
		out.println("<" + columnNames[columnIndex] + ">"
				+ StringEscapeUtils.escapeXml(value)
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
			formattedValue = getDateAsRFC822String(value);
		}

		out.println("<" + columnNames[columnIndex] + ">"
				+ formattedValue + "</" + columnNames[columnIndex] + ">");
		columnIndex++;
	}

	@Override
	public void newRow() {
		columnIndex = 0; // reset column index

		if (rowCount > 1) {
			//close previous row
			out.println("</item>\n");
		}

		//open new row
		out.println("<item>");
	}

	@Override
	public void endRows() {
		out.println("</item>");
		out.println("</channel>");
		out.println("</rss>");
	}

//    @Override
//    public boolean isShowQueryHeaderAndFooter() {
//        return false; // if set to true, art will add standard html header&footer around the output
//    }
}
