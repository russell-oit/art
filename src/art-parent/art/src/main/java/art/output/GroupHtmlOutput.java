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

import java.io.PrintWriter;

/**
 * Generates "Group: n columns" reports
 * 
 * @author Enrico Liboni
 */
public class GroupHtmlOutput extends GroupOutput {
	
	private final PrintWriter out;
	private final StringBuilder mainHeader = new StringBuilder();
	// temporary string used to store Main Header Values
	private final StringBuilder subHeader = new StringBuilder();


    /**
     * Sets the output writer
     * 
     * @param htmlWriter output writer
     */
    public GroupHtmlOutput(PrintWriter htmlWriter) {
        out = htmlWriter;
    }

    /**
     * Outputs report header. Report width is 80% of the page
     */
	@Override
    public void header() {
        out.println("<div align=\"center\">");
        out.println("<table border=\"0\" width=\"80%\">");
    }

	@Override
    public void header(int width) {
        out.println("<div align=\"center\">");
        out.println("<table border=\"0\" width=\"" + width + "%\">");
    }

	@Override
    public void addCellToMainHeader(String value) {
        mainHeader.append("<td>");
        mainHeader.append(value);
        mainHeader.append("</td>");
    }

	@Override
    public void addCellToSubHeader(String value) {
        subHeader.append("<td>");
        subHeader.append(value);
        subHeader.append("</td>");
    }

	@Override
    public void printMainHeader() {
        beginLines();
        out.println(mainHeader.toString());
        endLines();
    }

	@Override
    public void printSubHeader() {
        beginLines();
        out.println(subHeader.toString());
        endLines();
    }
	
	@Override
	public void separator(){
		out.println("<br><hr style=\"width:90%;height:1px\"><br>");
	}

	@Override
    public void addCellToLine(String value) {
        out.println("<td class=\"data\">" + value + "</td>");
    }

	@Override
    public void addCellToLine(String value, int numOfCells) {
        out.println("<td colspan=\"" + numOfCells + "\" class=\"data\">" + value + "</td>");
    }

	@Override
    public void addCellToLine(String value, String cssclass, int numOfCells) {
        out.println("<td colspan=\"" + numOfCells + "\" class=\"" + cssclass + "\">" + value + "</td>");
    }

	@Override
    public void addCellToLine(String value, String cssclass, String align, int numOfCells) {
        out.println("<td colspan=\"" + numOfCells + "\" class=\"" + cssclass
                + "\" align =\"" + align + "\">" + value + "</td>");
    }

	@Override
    public void beginLines() {
        out.println("<tr>");
    }

	@Override
    public void endLines() {
        out.println("</tr>");
    }

	@Override
    public void newLine() {
        out.println("</tr><tr>");
    }

	@Override
    public void footer() {
        out.println("</table></div>");
    }
}
