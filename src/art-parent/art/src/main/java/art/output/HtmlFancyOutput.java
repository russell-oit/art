/*
 * Copyright 2001-2016 Enrico Liboni <eliboni@users.sourceforge.net>
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

import java.util.Date;

/**
 * Fancy html output mode
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class HtmlFancyOutput extends StandardOutput {

	@Override
	public void init() {
		//include required css and javascript files
		//out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/css/htmlFancyOutput.css'>");
	}

	@Override
	public void beginHeader() {
		out.println("<div style='border: 3px solid white'>");
		out.println("<table class='table table-condensed table-bordered table-striped'>");
		out.println("<tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		out.println("<th>" + value + "</th>");
	}

	@Override
	public void addHeaderCellAlignLeft(String value) {
		out.println("<th class='text-left'>" + value + "</th>");
	}

	@Override
	public void endHeader() {
		out.println("</tr>");
	}

	@Override
	public void addCellString(String value) {
		String cssClass;
		if (evenRow) {
			cssClass = "text-left";
		} else {
			cssClass = "text-left";
		}

		out.println("<td class='" + cssClass + "'>" + value + "</td>");
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue = formatNumericValue(value);

		String cssClass;
		if (evenRow) {
			cssClass = "text-right";
		} else {
			cssClass = "text-right";
		}

		out.println("<td class='" + cssClass + "'>" + formattedValue + "</td>");
	}
	
	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		String cssClass;
		if (evenRow) {
			cssClass = "text-right";
		} else {
			cssClass = "text-right";
		}

		out.println("<td class='" + cssClass + "'>" + formattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = formatDateValue(value);

		String cssClass;
		if (evenRow) {
			cssClass = "text-left";
		} else {
			cssClass = "text-left";
		}

		out.println("<td class='" + cssClass + "'>" + formattedValue + "</td>");
	}
	
	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		String cssClass;
		
		if (evenRow) {
			cssClass = "text-left";
		} else {
			cssClass = "text-left";
		}

		out.println("<td class='" + cssClass + "'>" + formattedValue + "</td>");
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
