package art.output;

import java.io.PrintWriter;

/**
 * Generate "Report on column x" (grouped) reports
 * 
 * @author Enrico Liboni
 */
public class htmlReportOutWriter {

    PrintWriter out;
    StringBuffer mainHeader; // temporary string used to store Main Header Values     
    StringBuffer subHeader;  // temporary string used to store Sub  Header Values

    /**
     * Constructor. Set the output object
     * 
     * @param o output object
     */
    public htmlReportOutWriter(PrintWriter o) {
        out = o;
        mainHeader = new StringBuffer();
        subHeader = new StringBuffer();
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
