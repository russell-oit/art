/*
 * Copyright 2001-2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
package art.output;

import java.util.Date;

/**
 * Generates Grid html output mode
 *
 * @author Enrico Liboni
 */
public class HtmlGridOutput extends StandardOutput {

	@Override
	public void init() {
		//include required css and javascript files
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/css/htmlGridOutput.css'>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/sorttable.js'></script>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/htmlGridOutput.js'></script>");
	}

	@Override
	public void beginHeader() {
		out.println("<div style='border: 3px solid white'>");
		out.println("<table class='sortable' name='maintable' id='maintable'"
				+ " cellpadding='2' cellspacing='0'"
				+ " style='margin: 0 auto; width: 95%'>");
		out.println("<thead><tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		out.println("<th>" + value + "</th>");
	}

	@Override
	public void addHeaderCellAlignLeft(String value) {
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
		String formattedValue = formatNumbericValue(value);
		String sortValue;

		if (value == null) {
			sortValue = null;
		} else {
			sortValue = sortNumberFormatter.format(value);
		}

		out.println("<td style='text-align: right' sorttable_customkey='"
				+ sortValue + "' >" + formattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = formatDateValue(value);
		long sortValue = getDateSortValue(value);

		out.println("<td style='text-align: left' sorttable_customkey='"
				+ sortValue + "'>" + formattedValue + "</td>");
	}

	@Override
	public void newRow() {
		if (rowCount > 1) {
			//close previous row
			out.println("</tr>");
		}

		//open new row
		out.println("<tr class='rows' onclick='javascript:selectRow(this)'"
				+ " ondblclick='javascript:selectRow2(this)'"
				+ " onmouseover='javascript:highLight(this,\"hiliterows\")'"
				+ " onmouseout='javascript:highLight(this,\"rows\")'"
				+ ">");
	}
	
	@Override
	public void endRow(){
		out.println("</tr>");
	}
	
	@Override
	public void endRows() {
		out.println("</tbody>");
	}
	
	@Override
	public void beginTotalRow(){
		out.println("<tfoot><tr>");
	}
	
	@Override
	public void endTotalRow(){
		out.println("</tr><tfoot>");
	}

	@Override
	public void endOutput() {
		out.println("</table></div>");
	}

}
