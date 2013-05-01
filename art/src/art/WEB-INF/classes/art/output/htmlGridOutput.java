/*
 * Grid html output mode
 */
package art.output;

import art.servlets.ArtDBCP;
import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Map;

/**
 * Grid html output mode
 *
 * @author Enrico Liboni
 */
public class htmlGridOutput implements ArtOutputInterface {

	PrintWriter out;
	int numberOfLines;
	int maxRows;
	NumberFormat nfPlain;
	Map<Integer, ArtQueryParam> displayParams;

	/**
	 * Constructor
	 */
	public htmlGridOutput() {
		numberOfLines = 0;
		nfPlain = NumberFormat.getInstance();
		nfPlain.setMinimumFractionDigits(0);
		nfPlain.setGroupingUsed(true);
		nfPlain.setMaximumFractionDigits(99);
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
				
		//display parameters
        ArtOutHandler.displayParameters(out, displayParams);

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
		if (d != null) {
			formattedValue = nfPlain.format(d.doubleValue());
		}
		out.println("  <td align=\"right\">" + formattedValue + "</td>");
	}

	@Override
	public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
		String formattedValue = null;
		if (i != null) {
			formattedValue = nfPlain.format(i.longValue());
		}
		out.println("  <td align=\"right\">" + formattedValue + "</td>");
	}

	@Override
	public void addCellDate(java.util.Date d) {
		out.println("  <td style=\"text-align: left\" sorttable_customkey=\"" + ArtDBCP.getDateSortString(d) + "\" >" + ArtDBCP.getDateDisplayString(d) + "</td>");
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
