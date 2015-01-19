/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * ART. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Grid html output mode
 */
package art.output;

import art.servlets.ArtConfig;
import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Grid html output mode
 *
 * @author Enrico Liboni
 */
public class HtmlGridOutput extends TabularOutput {

	private final String CLOSE_RESULTS_TABLE_HTML="</tr></tbody></table></div>";

	@Override
	public void beginHeader() {
		//include required css and javascript files
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/css/htmlGridOutput.css'>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/sorttable.js'></script>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/htmlGridOutput.js'></script>");

		//start results table
		out.println("<div style='border: 3px solid white'>");
		out.println("<table class='sortable' name='maintable' id='maintable'"
				+ " cellpadding='2' cellspacing='0' width='80%'>");
		out.println("<thead><tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		out.println("<th>" + value + "</th>");
	}

	@Override
	public void addHeaderCellLeftAligned(String value) {
		out.println("<th style='text-align: left'>" + value + "</th>");
	}

	@Override
	public void endHeader() {
		out.println("</tr></thead>");

	}

	@Override
	public void beginRows() {
		out.println("<tbody>");
	}

	@Override
	public void addCellString(String value) {
		out.println("<td style='text-align: left'>" + value + "</td>");
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue = null;
		String sortValue = null;
		if (value != null) {
			formattedValue = actualNumberFormatter.format(value);
			sortValue = sortNumberFormatter.format(value);
		}
		out.println("<td style='text-align: right' sorttable_customkey='"
				+ sortValue + "' >" + formattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = "";
		long sortValue = 0;
		if (value != null) {
			sortValue = value.getTime();
			formattedValue = ArtConfig.getDateDisplayString(value);
		}
		out.println("<td style='text-align: left' sorttable_customkey='"
				+ sortValue + "' >" + formattedValue + "</td>");
	}

	@Override
	public boolean newRow() {
		boolean canProceed;

		rowCount++;

		if (rowCount > maxRows) {
			canProceed = false;

			//close table
			out.println(CLOSE_RESULTS_TABLE_HTML);
		} else {
			canProceed = true;

			if (rowCount > 1) {
				//close previous row
				out.println("</tr>");
			}

			//open new row
			out.println("<tr class='rows' onclick='javascript:selectRow(this)'"
					+ " ondblclick='javascript:selectRow2(this)'"
					+ " onmouseover='javascript:highLight(this,'hiliterows')'"
					+ " onmouseout='javascript:highLight(this,'rows')'>");
		}

		return canProceed;
	}

	@Override
	public void endRows() {
		out.println(CLOSE_RESULTS_TABLE_HTML);
	}

}
