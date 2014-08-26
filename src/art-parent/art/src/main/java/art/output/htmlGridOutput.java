/**
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
import java.util.Locale;
import java.util.Map;

/**
 * Grid html output mode
 *
 * @author Enrico Liboni
 */
public class htmlGridOutput implements ReportOutputInterface {

	PrintWriter out;
	int numberOfLines;
	int maxRows;
	NumberFormat nfPlain;
	Map<Integer, ArtQueryParam> displayParams;
	DecimalFormat nfSort;

	/**
	 * Constructor
	 */
	public htmlGridOutput() {
		numberOfLines = 0;
		nfPlain = NumberFormat.getInstance();
		nfPlain.setMinimumFractionDigits(0);
		nfPlain.setGroupingUsed(true);
		nfPlain.setMaximumFractionDigits(99);

		//specifically use english locale for sorting e.g.
		//in case default locale uses . as thousands separator
		nfSort = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		nfSort.applyPattern("#.#");
		//ensure all numbers are pre-padded with zeros so that sorting works correctly
		nfSort.setMinimumIntegerDigits(20);
		nfSort.setMaximumFractionDigits(99);
	}

	@Override
	public String getFileName() {
		return null; // this output mode does not generate external files
	}

	@Override
	public String getName() {
		return "Browser (Grid)";
	}

	@Override
	public String getContentType() {
		return "text/html;charset=utf-8";
	}

	@Override
	public void setWriter(PrintWriter o) {
		out = o;
	}

	@Override
	public void setQueryName(String s) {
		//not used
	}

	@Override
	public void setFileUserName(String s) {
		//not used. this output mode doesn't produce files
	}

	@Override
	public void setMaxRows(int i) {
		maxRows = i;
	}

	@Override
	public void setColumnsNumber(int i) {
		//not used
	}

	@Override
	public void setExportPath(String s) {
	}

	@Override
	public void setDisplayParameters(Map<Integer, ArtQueryParam> t) {
		displayParams = t;
	}

	@Override
	public void beginHeader() {
		
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/htmlGridOutput.css\">");

		//display parameters
//		ReportOutputHandler.displayParameters(out, displayParams);

		//start results table
		out.println("<div style=\"border: 3px solid white\"><table class=\"sortable\" name=\"maintable\" id=\"maintable\" cellpadding=\"2\" cellspacing=\"0\" width=\"80%\">");
		out.println(" <thead><tr>");
	}

	@Override
	public void addHeaderCell(String s) {
		out.println(" <th class=\"header\">" + s + "</th>");
	}

	@Override
	public void addHeaderCellLeft(String s) {
		out.println(" <th style=\"text-align: left\">" + s + "</th>");
	}

	@Override
	public void endHeader() {
		out.println(" </tr></thead>");

	}

	@Override
	public void beginLines() {
		out.println("<tbody>");
	}

	@Override
	public void addCellString(String s) {
		out.println("  <td style=\"text-align: left\">" + s + "</td>");
	}

	@Override
	public void addCellDouble(Double d) {
		String formattedValue = null;
		String sortValue = null;
		if (d != null) {
			formattedValue = nfPlain.format(d.doubleValue());
			sortValue = nfSort.format(d.doubleValue());
		}
		out.println("  <td align=\"right\" sorttable_customkey=\"" + sortValue + "\" >" + formattedValue + "</td>");
	}

	@Override
	public void addCellLong(Long i) {  // used for INTEGER, TINYINT, SMALLINT, BIGINT
		String formattedValue = null;
		String sortValue = null;
		if (i != null) {
			formattedValue = nfPlain.format(i.longValue());
			sortValue = nfSort.format(i.longValue());
		}
		out.println("  <td align=\"right\" sorttable_customkey=\"" + sortValue + "\" >" + formattedValue + "</td>");
	}

	@Override
	public void addCellDate(java.util.Date d) {
		String formattedValue;
		long sortValue;
		if (d == null) {
			sortValue = 0;
			formattedValue = "";
		} else {
			sortValue = d.getTime();
			formattedValue = ArtConfig.getDateDisplayString(d);
		}
		out.println("  <td style=\"text-align: left\" sorttable_customkey=\"" + sortValue + "\" >" + formattedValue + "</td>");
	}

	@Override
	public boolean newLine() {
		numberOfLines++;

		if (numberOfLines == 1) { // first row
			out.println(" <tr class=\"rows\" onclick=\"javascript:selectRow(this)\" ondblclick=\"javascript:selectRow2(this)\" onmouseover=\"javascript:highLight(this,'hiliterows')\" onmouseout=\"javascript:highLight(this,'rows')\">");
		} else if (numberOfLines < maxRows) {
			out.println(" </tr>");
			out.println(" <tr class=\"rows\" onclick=\"javascript:selectRow(this)\" ondblclick=\"javascript:selectRow2(this)\" onmouseover=\"javascript:highLight(this,'hiliterows')\" onmouseout=\"javascript:highLight(this,'rows')\">");
		} else {
			out.println("</tr></table></div>");
			return false;
		}

		return true;
	}

	@Override
	public void endLines() {
		out.println(" </tr></tbody></table></div>");
	}

	@Override
	public boolean isShowQueryHeaderAndFooter() {
		return true;
	}
}
