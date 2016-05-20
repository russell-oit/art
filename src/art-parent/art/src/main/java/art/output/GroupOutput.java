/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.servlets.Config;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Generates a group report
 * 
 * @author Timothy Anyona
 */
public abstract class GroupOutput {

	/**
	 * Outputs report header
	 */
	public abstract void header();

	/**
	 * Outputs report header with explicit report width
	 *
	 * @param width report width as percentage of page
	 */
	public abstract void header(int width);

	/**
	 * Outputs a value to the main header
	 * 
	 * @param value the value to output
	 */
	public abstract void addCellToMainHeader(String value);

	/**
	 * Outputs a value to the sub header
	 * 
	 * @param value the value to output
	 */
	public abstract void addCellToSubHeader(String value);

	/**
	 * Outputs the main header
	 */
	public abstract void printMainHeader();

	/**
	 * Outputs the sub header
	 */
	public abstract void printSubHeader();

	/**
	 * Outputs the separator
	 */
	public abstract void separator();

	/**
	 * Outputs a value to a line
	 * 
	 * @param value the value to output
	 */
	public abstract void addCellToLine(String value);

	/**
	 * Outputs a value to a line
	 * 
	 * @param value the value to output
	 * @param numOfCells the number of cells to use
	 */
	public abstract void addCellToLine(String value, int numOfCells);

	/**
	 * Outputs a value to a line
	 * 
	 * @param value the value to output
	 * @param cssclass the css class to use
	 * @param numOfCells the number of cells to use
	 */
	public abstract void addCellToLine(String value, String cssclass, int numOfCells);

	/**
	 * Outputs a value to a line
	 * 
	 * @param value the value to output
	 * @param cssclass the css class to use
	 * @param align the align attribute to use
	 * @param numOfCells the number of cells to use
	 */
	public abstract void addCellToLine(String value, String cssclass, String align, int numOfCells);

	/**
	 * Begins a row
	 */
	public abstract void beginLines();

	/**
	 * Ends a row
	 */
	public abstract void endLines();

	/**
	 * Begins a new line
	 */
	public abstract void newLine();

	/**
	 * Outputs the footer
	 */
	public abstract void footer();
	
	/**
	 * Returns the maximum number of rows to output
	 * 
	 * @return the maximum number of rows to output
	 */
	public int getMaxRows(){
		return Config.getMaxRows("htmlreport");
	}
	
	/**
	 * Generates a group report
	 *
	 * @param rs the resultset to use. Needs to be a scrollable.
	 * @param splitCol the group column
	 * @return number of rows output
	 * @throws SQLException
	 */
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

		int maxRows = getMaxRows();

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
			addCellToLine("<blink>Too many rows (>" + maxRows
					+ "). Data not completed. Please narrow your search.</blink>", "qeattr", "left", colCount);
		}

		endLines();
		footer();

		return counter + 1; // number of rows
	}
}
