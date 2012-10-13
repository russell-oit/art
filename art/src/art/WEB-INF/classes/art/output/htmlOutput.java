/*
 * Fancy html output mode
 */
package art.output;

import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Map;

/**
 * Fancy html output mode
 *
 * @author Enrico Liboni
 */
public class htmlOutput implements ArtOutputInterface {

	PrintWriter out;
	int numberOfLines;
	int maxRows;
	NumberFormat nfPlain;
	boolean oddline = true;
	Map<Integer, ArtQueryParam> displayParams;

	/**
	 * Constructor
	 */
	public htmlOutput() {
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
	public String getContentType() {
		return "text/html;charset=utf-8";
	}

	@Override
	public String getName() {
		return "Browser (Fancy)";
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
		out.println("<div style=\"border: 3px solid white\"><table class=\"qe\" width=\"80%\"><tr>"); // cellspacing=\"1\" cellpadding=\"0\"
	}

	@Override
	public void addHeaderCell(String s) {
		out.println("<td class=\"qeattr\">" + s + "</td>");
	}

	@Override
	public void endHeader() {
		out.println("</tr>");
	}

	@Override
	public void beginLines() {
	}

	@Override
	public void addCellString(String s) {
		out.println("<td class=\"" + (oddline ? "qeoddLeft" : "qeevenLeft") + "\" >" + s + "</td>");
	}

	@Override
	public void addCellDouble(Double d) {
		String formattedValue = null;
		if (d != null) {
			formattedValue = nfPlain.format(d.doubleValue());
		}
		out.println("<td align=\"right\" class=\"" + (oddline ? "qeodd" : "qeeven") + "\" >" + formattedValue + "</td>");
	}

	@Override
	public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
		out.println("<td align=\"right\" class=\"" + (oddline ? "qeodd" : "qeeven") + "\" >" + i + "</td>");
	}

	@Override
	public void addCellDate(java.util.Date d) {
		out.println("<td class=\"" + (oddline ? "qeoddLeft" : "qeevenLeft") + "\" >" + d + "</td>");
	}

	@Override
	public boolean newLine() {
		numberOfLines++;
		if (numberOfLines % 2 == 0) {
			oddline = true;
		} else {
			oddline = false;
		}

		if (numberOfLines == 1) { // first row
			out.println(" <tr>");
		} else if (numberOfLines < maxRows) {
			out.println("</tr><tr>");
		} else {
			out.println("</tr></table></div>");
			return false;
		}

		return true;
	}

	@Override
	public void endLines() {
		out.println("</tr></table></div>");
	}

	@Override
	public boolean isDefaultHtmlHeaderAndFooterEnabled() {
		return true;
	}
}
