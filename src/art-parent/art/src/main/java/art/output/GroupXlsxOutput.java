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

import art.servlets.Config;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Generates group xlsx output
 *
 * @author Timothy Anyona
 */
public class GroupXlsxOutput extends GroupOutput {

	private FileOutputStream fout;
	private XSSFWorkbook wb;
	private XSSFSheet sheet;
	private XSSFRow row;
	private XSSFCell cell;
	private XSSFCellStyle headerStyle;
	private XSSFCellStyle bodyStyle;
	private XSSFCellStyle dateStyle;
	private XSSFCellStyle summaryStyle;
	private int currentRow;
	private int cellNumber;

	/**
	 * Performs initialization in preparation for the output
	 */
	private void init() {
		try {
			fout = new FileOutputStream(fullOutputFilename);

			String sheetName = WorkbookUtil.createSafeSheetName(reportName);

			wb = new XSSFWorkbook();
			sheet = wb.createSheet(sheetName);

			XSSFFont headerFont = wb.createFont();
			headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
			headerFont.setColor(HSSFColor.BLUE.index);
			headerFont.setFontHeightInPoints((short) 12);

			headerStyle = wb.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);

			XSSFFont bodyFont = wb.createFont();
			bodyFont.setColor(XSSFFont.COLOR_NORMAL);
			bodyFont.setFontHeightInPoints((short) 10);

			bodyStyle = wb.createCellStyle();
			bodyStyle.setFont(bodyFont);

			dateStyle = wb.createCellStyle();
			dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
			dateStyle.setFont(bodyFont);

			XSSFFont summaryFont = wb.createFont();
			summaryFont.setColor(XSSFFont.COLOR_NORMAL);
			summaryFont.setFontHeightInPoints((short) 10);
			summaryFont.setBold(true);
			summaryStyle = wb.createCellStyle();
			summaryStyle.setFont(summaryFont);
		} catch (IOException ex) {
			endOutput();
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Outputs a value
	 *
	 * @param value the value to output
	 */
	private void addCell(String value) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(XSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new XSSFRichTextString(value));
		cell.setCellStyle(bodyStyle);
	}

	/**
	 * Outputs a summary value
	 *
	 * @param value the value to output
	 */
	private void addSummaryCell(String value) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(XSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new XSSFRichTextString(value));
		cell.setCellStyle(summaryStyle);
	}

	/**
	 * Outputs a numeric summary value
	 *
	 * @param value the value to output
	 */
	public void addSummaryCellNumeric(double value) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
		cell.setCellValue(value);
		cell.setCellStyle(summaryStyle);
	}

	/**
	 * Performs initialization before output starts
	 */
	private void beginLines() {
		cellNumber = 0;
	}

	/**
	 * Creates a new row
	 */
	private void newLine() {
		row = sheet.createRow(currentRow++);
		cellNumber = 0;
	}

	/**
	 * Finalizes output
	 */
	private void endOutput() {
		try {
			if (fout != null) {
				if (wb != null) {
					wb.write(fout);
				}
				fout.close();
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Outputs a header value
	 *
	 * @param value the value to output
	 */
	private void addHeaderCell(String value) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new XSSFRichTextString(value));
		cell.setCellStyle(headerStyle);
	}

	@Override
	public int generateGroupReport(ResultSet rs, int splitColumn) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();

		int i;

		init();

		newLine();

		//output header columns for the result set columns
		int colCount = rsmd.getColumnCount();
		for (i = 0; i < colCount; i++) {
			addHeaderCell(rsmd.getColumnLabel(i + 1));
		}

		int maxRows = Config.getMaxRows("xlsx");

		int firstRow = 0;
		int lastRow = 0;

		int counter = 0;
		StringBuilder cmpStr = null; // temporary string used to compare values
		StringBuilder tmpCmpStr; // temporary string used to compare values
		String summaryText = null;

		while (rs.next() && counter < maxRows) {
			newLine();

			beginLines();

			cmpStr = new StringBuilder();
			List<String> summaryValues = new ArrayList<>();

			// Output Main Data (only one row, obviously)
			for (i = 0; i < splitColumn; i++) {
				String value = rs.getString(i + 1);

				addCell(value);
				cmpStr.append(value);

				summaryValues.add(value);
			}

			summaryText = StringUtils.join(summaryValues, "-");

			firstRow = currentRow;

			// Output Sub Data (first line)
			for (; i < colCount; i++) {
				addCell(rs.getString(i + 1));
			}

			boolean currentMain = true;
			while (currentMain && counter < maxRows) {  // next line
				// Get Main Data in order to compare it
				if (rs.next()) {
					counter++;
					tmpCmpStr = new StringBuilder();

					for (i = 0; i < splitColumn; i++) {
						tmpCmpStr.append(rs.getString(i + 1));
					}

					if (tmpCmpStr.toString().equals(cmpStr.toString()) == true) {
						//row has same main data as previous row
						// Add data lines
						newLine();
						for (i = 0; i < colCount; i++) {
							addCell(rs.getString(i + 1));
						}
					} else {
						//row has different main from previous row
						//create a grouping
						//http://www.mysamplecode.com/2011/10/apache-poi-excel-row-group-collapse.html
						lastRow = currentRow;
						sheet.groupRow(firstRow - 1, lastRow - 1);
						sheet.setRowGroupCollapsed(firstRow - 1, true);

						newLine();
						addSummaryCell(summaryText);

						int summaryRowCount = (lastRow - firstRow) + 1;
						addSummaryCellNumeric(summaryRowCount);

						currentMain = false;
						rs.previous();
					}
				} else {
					currentMain = false;
					// The outer and inner while will exit
				}
			}
		}

		if (cmpStr != null) {
			lastRow = currentRow;
			sheet.groupRow(firstRow - 1, lastRow - 1);
			sheet.setRowGroupCollapsed(firstRow - 1, true);

			newLine();
			addSummaryCell(summaryText);

			int summaryRowCount = (lastRow - firstRow) + 1;
			addSummaryCellNumeric(summaryRowCount);
		}

		if (!(counter < maxRows)) {
			newLine();
			addCell("Too many rows (>" + maxRows
					+ "). Data not completed. Please narrow your search.");
		}

		for (i = 0; i < colCount; i++) {
			sheet.autoSizeColumn(i);
		}

		endOutput();

		return counter + 1; // number of rows
	}
}
