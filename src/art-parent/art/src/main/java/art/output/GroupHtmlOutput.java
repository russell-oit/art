/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.runreport.GroovyDataDetails;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates group html reports
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class GroupHtmlOutput {

	private static final Logger logger = LoggerFactory.getLogger(GroupHtmlOutput.class);

	private PrintWriter out;
	private String contextPath;
	private final StringBuilder mainHeader = new StringBuilder();
	private final StringBuilder subHeader = new StringBuilder();

	public void init() {
		//include required css and javascript files
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/css/groupHtmlOutput.css'>");
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
		mainHeader.append("<td>");
		mainHeader.append(value);
		mainHeader.append("</td>");
	}

	/**
	 * Outputs a value to the sub header
	 *
	 * @param value the value to output
	 */
	private void addCellToSubHeader(String value) {
		subHeader.append("<td>");
		subHeader.append(value);
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
	private void separator() {
		out.println("<br><hr style='width:90%; height:1px'><br>");
	}

	/**
	 * Outputs a data value
	 *
	 * @param value the value to output
	 */
	private void addCellToLine(String value) {
		out.println("<td class='groupData'>" + value + "</td>");
	}

	/**
	 * Outputs a data value
	 *
	 * @param value the value to output
	 * @param numOfCells
	 */
	private void addErrorCell(String value, int numOfCells) {
		out.println("<td colspan='" + numOfCells + "' class='groupError' align='left'>" + value + "</td>");
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

	/**
	 * Generates group output
	 *
	 * @param rs the resultset to use. Needs to be a scrollable. Must not be
	 * null.
	 * @param splitColumn the group column
	 * @param writer the writer to use. Must not be null.
	 * @param contextPath the application context path
	 * @return number of rows output
	 * @throws SQLException
	 */
	public int generateReport(ResultSet rs, int splitColumn, PrintWriter writer,
			String contextPath) throws SQLException {

		logger.debug("Entering generateReport: splitColumn={}, contextPath='{}'",
				splitColumn, contextPath);

		Objects.requireNonNull(rs, "rs must not be null");
		Objects.requireNonNull(writer, "writer must not be null");
		Objects.requireNonNull(contextPath, "contextPath must not be null");

		out = writer;
		this.contextPath = contextPath;

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

		ResultSetMetaData rsmd = rs.getMetaData();

		int colCount = rsmd.getColumnCount();
		int i;
		String tmpstr;

		// Build main header HTML
		for (i = 0; i < (splitColumn); i++) {
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

		int counter = 0;
		StringBuffer cmpStr; // temporary string used to compare values
		StringBuffer tmpCmpStr; // temporary string used to compare values

		while (rs.next() && counter < maxRows) {
			// Separators
			separator();

			// Output Main Header and Main Data
			header(90);
			printMainHeader();
			beginLines();
			cmpStr = new StringBuffer();

			// Output Main Data (only one row, obviously)
			for (i = 0; i < splitColumn; i++) {
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

					for (i = 0; i < splitColumn; i++) {
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

	/**
	 * Generates group output
	 *
	 * @param data the data to use
	 * @param splitColumn the group column
	 * @param writer the writer to use
	 * @param contextPath the application context path
	 * @return number of rows output
	 * @throws SQLException
	 * @throws java.io.IOException
	 */
	public int generateReport(Object data, int splitColumn, PrintWriter writer,
			String contextPath) throws SQLException, IOException {

		logger.debug("Entering generateReport: splitColumn={}, contextPath='{}'",
				splitColumn, contextPath);

		Objects.requireNonNull(data, "data must not be null");
		Objects.requireNonNull(writer, "writer must not be null");
		Objects.requireNonNull(contextPath, "contextPath must not be null");

		out = writer;
		this.contextPath = contextPath;

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
		GroovyDataDetails dataDetails = RunReportHelper.getGroovyDataDetails(data);
		int rowCount = dataDetails.getRowCount();
		int colCount = dataDetails.getColCount();
		List<String> columnNames = dataDetails.getColumnNames();
		List<? extends Object> dataList = dataDetails.getDataList();

		init();

		int i;
		String tmpstr;

		// Build main header HTML
		for (i = 0; i < (splitColumn); i++) {
			tmpstr = columnNames.get(i);
			addCellToMainHeader(tmpstr);
		}
		// Now the header is completed

		// Build the Sub Header
		for (; i < colCount; i++) {
			tmpstr = columnNames.get(i);
			addCellToSubHeader(tmpstr);
		}

		int maxRows = Config.getMaxRows("html");

		int counter = 0;
		StringBuffer cmpStr; // temporary string used to compare values
		StringBuffer tmpCmpStr; // temporary string used to compare values
		int currentRow = -1;
		while ((currentRow < rowCount) && (counter < maxRows)) {
			currentRow++;

			Object row = dataList.get(currentRow);
			// Separators
			separator();

			// Output Main Header and Main Data
			header(90);
			printMainHeader();
			beginLines();
			cmpStr = new StringBuffer();

			// Output Main Data (only one row, obviously)
			for (i = 0; i < splitColumn; i++) {
				String stringValue = RunReportHelper.getStringRowValue(row, i, columnNames);
				addCellToLine(stringValue);
				cmpStr.append(stringValue);
			}

			endLines();
			footer();

			// Output Sub Header and Sub Data
			header(80);
			printSubHeader();
			beginLines();

			// Output Sub Data (first line)
			for (; i < colCount; i++) {
				addCellToLine(RunReportHelper.getStringRowValue(row, i, columnNames));
			}

			boolean currentMain = true;
			while (currentMain && counter < maxRows) {  // next line
				// Get Main Data in order to compare it
				currentRow++;
				if (currentRow < rowCount) {
					Object row2 = dataList.get(currentRow);
					counter++;
					tmpCmpStr = new StringBuffer();

					for (i = 0; i < splitColumn; i++) {
						tmpCmpStr.append(RunReportHelper.getStringRowValue(row2, i, columnNames));
					}

					if (tmpCmpStr.toString().equals(cmpStr.toString()) == true) { // same Main
						newLine();
						// Add data lines
						for (; i < colCount; i++) {
							addCellToLine(RunReportHelper.getStringRowValue(row2, i, columnNames));
						}
					} else {
						endLines();
						footer();
						currentMain = false;
						currentRow--;
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
