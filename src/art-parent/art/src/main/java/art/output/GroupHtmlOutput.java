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

import java.io.PrintWriter;

/**
 * Generate "Group: n columns" reports
 * 
 * @author Enrico Liboni
 */
public class GroupHtmlOutput extends GroupOutput {


    /**
     * Constructor. Set the output object
     * 
     * @param htmlWriter output object
     */
    public GroupHtmlOutput(PrintWriter htmlWriter) {
        out = htmlWriter;
    }

    /**
     * Output report header. Report width is 80% of the page
     */
    public void header() {
        out.println("<div align=\"center\">");
        out.println("<table border=\"0\" width=\"80%\">");
    }

    /**
     * Output report header with explicit report width
     * 
     * @param width report width as percentage of page
     */
    public void header(int width) {
        out.println("<div align=\"center\">");
        out.println("<table border=\"0\" width=\"" + width + "%\">");
    }

    /**
     * 
     * @param value
     */
    public void addCellToMainHeader(String value) {
        mainHeader.append("<td class=\"qeattr\">");
        mainHeader.append(value);
        mainHeader.append("</td>");
    }

    /**
     * 
     * @param value
     */
    public void addCellToSubHeader(String value) {
        subHeader.append("<td class=\"qesubattr\">");
        subHeader.append(value);
        subHeader.append("</td>");
    }

    /**
     * 
     */
    public void printMainHeader() {
        beginLines();
        out.println(mainHeader.toString());
        endLines();
    }

    /**
     * 
     */
    public void printSubHeader() {
        beginLines();
        out.println(subHeader.toString());
        endLines();
    }
	
	public void separator(){
		out.println("<br><hr style=\"width:90%;height:1px\"><br>");
	}

    /**
     * 
     * @param value
     */
    public void addCellToLine(String value) {
        out.println("<td class=\"data\">" + value + "</td>");
    }

    /**
     * 
     * @param value
     * @param numOfCells
     */
    public void addCellToLine(String value, int numOfCells) {
        out.println("<td colspan=\"" + numOfCells + "\" class=\"data\">" + value + "</td>");
    }

    /**
     * 
     * @param value
     * @param cssclass
     * @param numOfCells
     */
    public void addCellToLine(String value, String cssclass, int numOfCells) {
        out.println("<td colspan=\"" + numOfCells + "\" class=\"" + cssclass + "\">" + value + "</td>");
    }

    /**
     * 
     * @param value
     * @param cssclass
     * @param align
     * @param numOfCells
     */
    public void addCellToLine(String value, String cssclass, String align, int numOfCells) {
        out.println("<td colspan=\"" + numOfCells + "\" class=\"" + cssclass
                + "\" align =\"" + align + "\">" + value + "</td>");
    }

    /**
     * 
     */
    public void beginLines() {
        out.println("<tr>");
    }

    /**
     * 
     */
    public void endLines() {
        out.println("</tr>");
    }

    /**
     * 
     */
    public void newLine() {
        out.println("</tr><tr>");
    }

    /**
     * 
     */
    public void footer() {
        out.println("</table></div>");
    }
}
