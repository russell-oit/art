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

import art.servlets.Config;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.owasp.encoder.Encode;

/**
 * Generates group html reports
 * 
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class GroupHtmlOutput extends GroupOutput {
	
	private final StringBuilder mainHeader = new StringBuilder();
	private final StringBuilder subHeader = new StringBuilder();
	
	public void init() {
		//include required css and javascript files
		out.println("<link rel='stylesheet' type='text/css' href='" + getContextPath() + "/public/css/groupHtmlOutput.css'>");
	}


	/**
	 * Outputs the report header
	 * 
	 * @param width the header width as a percentage
	 */
    private void header(int width) {
        out.println("<div align='center'>");
        out.println("<table border='0' width='" + width + "%'>");
    }

	/**
	 * Outputs a value to the main header
	 * 
	 * @param value the value to output
	 */
    private void addCellToMainHeader(String value) {
		String escapedValue = Encode.forHtmlContent(value);
        mainHeader.append("<td>");
        mainHeader.append(escapedValue);
        mainHeader.append("</td>");
    }

	/**
	 * Outputs a value to the sub header
	 * 
	 * @param value the value to output
	 */
    private void addCellToSubHeader(String value) {
		String escapedValue = Encode.forHtmlContent(value);
        subHeader.append("<td>");
        subHeader.append(escapedValue);
        subHeader.append("</td>");
    }

	/**
	 * Outputs the main header
	 */
    private void printMainHeader() {
        beginLines();
        out.println(mainHeader.toString());
        endLines();
    }

	/**
	 * Outputs the sub header
	 */
    private void printSubHeader() {
        beginLines();
        out.println(subHeader.toString());
        endLines();
    }
	
	/**
	 * Outputs a group separator
	 */
	private void separator(){
		out.println("<br><hr style='width:90%; height:1px'><br>");
	}

	/**
	 * Outputs a data value
	 * 
	 * @param value the value to output
	 */
    private void addCellToLine(String value) {
		String escapedValue = Encode.forHtmlContent(value);
        out.println("<td class='data'>" + escapedValue + "</td>");
    }

	/**
	 * Outputs a data value
	 * 
	 * @param value the value to output
	 * @param numOfCells 
	 */
    private void addErrorCell(String value, int numOfCells) {
		String escapedValue = Encode.forHtmlContent(value);
        out.println("<td colspan='" + numOfCells + "' class='qeattr' align='left'>" + escapedValue + "</td>");
    }

	/**
	 * Performs initialization in readiness for data output
	 */
    private void beginLines() {
        out.println("<tr>");
    }

	/**
	 * Finalizes data output
	 */
    private void endLines() {
        out.println("</tr>");
    }

	/**
	 * Creates a new row
	 */
    private void newLine() {
        out.println("</tr><tr>");
    }

	/**
	 * Finalizes report
	 */
    private void footer() {
        out.println("</table></div>");
    }
	
	@Override
	public int generateGroupReport(ResultSet rs, int splitCol) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		
		int colCount = rsmd.getColumnCount();
		int i;
		int counter = 0;
		String tmpstr;
		StringBuffer cmpStr; // temporary string used to compare values
		StringBuffer tmpCmpStr; // temporary string used to compare values

		// Report, is intended to be something like that:
		/*
		 * ------------------------------------- | Attr1 | Attr2 | Attr3 | //
		 * Main header ------------------------------------- | Value1 | Value2 |
		 * Value3 | // Main Data -------------------------------------
		 *
		 * -----------------------------... | SubAttr1 | Subattr2 |... // Sub
		 * Header -----------------------------... | SubValue1.1 | SubValue1.2
		 * |... // Sub Data -----------------------------... | SubValue2.1 |
		 * SubValue2.2 |... -----------------------------...
		 * ................................ ................................
		 * ................................
		 *
		 * etc...
		 */
		
		init();
		
		// Build main header HTML
		for (i = 0; i < (splitCol); i++) {
			tmpstr = rsmd.getColumnLabel(i + 1);
			addCellToMainHeader(tmpstr);
		}
		// Now the header is completed

		// Build the Sub Header
		for (; i < colCount; i++) {
			tmpstr = rsmd.getColumnLabel(i + 1);
			addCellToSubHeader(tmpstr);
		}

		int maxRows = Config.getMaxRows("html");

		while (rs.next() && counter < maxRows) {
			// Separators
			separator();

			// Output Main Header and Main Data
			header(90);
			printMainHeader();
			beginLines();
			cmpStr = new StringBuffer();

			// Output Main Data (only one row, obviously)
			for (i = 0; i < splitCol; i++) {
				addCellToLine(rs.getString(i + 1));
				cmpStr.append(rs.getString(i + 1));
			}
			
			endLines();
			footer();

			// Output Sub Header and Sub Data
			header(80);
			printSubHeader();
			beginLines();

			// Output Sub Data (first line)
			for (; i < colCount; i++) {
				addCellToLine(rs.getString(i + 1));
			}

			boolean currentMain = true;
			while (currentMain && counter < maxRows) {  // next line
				// Get Main Data in order to compare it
				if (rs.next()) {
					counter++;
					tmpCmpStr = new StringBuffer();

					for (i = 0; i < splitCol; i++) {
						tmpCmpStr.append(rs.getString(i + 1));
					}

					if (tmpCmpStr.toString().equals(cmpStr.toString()) == true) { // same Main
						newLine();
						// Add data lines
						for (; i < colCount; i++) {
							addCellToLine(rs.getString(i + 1));
						}
					} else {
						endLines();
						footer();
						currentMain = false;
						rs.previous();
					}
				} else {
					currentMain = false;
					// The outer and inner while will exit
				}
			}
		}

		if (!(counter < maxRows)) {
			newLine();
			addErrorCell("<b>Too many rows (>" + maxRows
					+ "). Data not completed. Please narrow your search.</b>", colCount);
		}

		endLines();
		footer();

		return counter + 1; // number of rows
	}
}
