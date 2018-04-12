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

import art.report.Report;
import art.runreport.GroovyDataDetails;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates group xlsx output
 *
 * @author Timothy Anyona
 */
public class GroupXlsxOutput {

	private static final Logger logger = LoggerFactory.getLogger(GroupXlsxOutput.class);

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
	private String fullOutputFileName;
	private String reportName;
	private Report report;
	private String dynamicOpenPassword;
	private String dynamicModifyPassword;

	/**
	 * @return the dynamicOpenPassword
	 */
	public String getDynamicOpenPassword() {
		return dynamicOpenPassword;
	}

	/**
	 * @param dynamicOpenPassword the dynamicOpenPassword to set
	 */
	public void setDynamicOpenPassword(String dynamicOpenPassword) {
		this.dynamicOpenPassword = dynamicOpenPassword;
	}

	/**
	 * @return the dynamicModifyPassword
	 */
	public String getDynamicModifyPassword() {
		return dynamicModifyPassword;
	}

	/**
	 * @param dynamicModifyPassword the dynamicModifyPassword to set
	 */
	public void setDynamicModifyPassword(String dynamicModifyPassword) {
		this.dynamicModifyPassword = dynamicModifyPassword;
	}

	/**
	 * Performs initialization in preparation for the output
	 */
	private void init() {
		try {
			fout = new FileOutputStream(fullOutputFileName);

			String sheetName = WorkbookUtil.createSafeSheetName(reportName);

			wb = new XSSFWorkbook();
			sheet = wb.createSheet(sheetName);

			XSSFFont headerFont = wb.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.BLUE.getIndex());
			short headerFontSize = 12;
			headerFont.setFontHeightInPoints(headerFontSize);

			headerStyle = wb.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setBorderBottom(BorderStyle.THIN);

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
		cell.setCellType(CellType.STRING);
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
		cell.setCellType(CellType.STRING);
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
		cell.setCellType(CellType.NUMERIC);
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
				RunReportHelper runReportHelper = new RunReportHelper();

				//set modify password
				String modifyPassword = runReportHelper.getEffectiveModifyPassword(report, dynamicModifyPassword);

				if (sheet != null && StringUtils.isNotEmpty(modifyPassword)) {
					sheet.protectSheet(modifyPassword);
				}

				if (wb != null) {
					wb.write(fout);
				}
				fout.close();

				//set open password
				String openPassword = runReportHelper.getEffectiveOpenPassword(report, dynamicOpenPassword);

				if (StringUtils.isNotEmpty(openPassword)) {
					PoiUtils.addOpenPassword(openPassword, fullOutputFileName);
				}
			}
		} catch (IOException | GeneralSecurityException | InvalidFormatException ex) {
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
		cell.setCellType(CellType.STRING);
		cell.setCellValue(new XSSFRichTextString(value));
		cell.setCellStyle(headerStyle);
	}

	/**
	 * Generates group output
	 *
	 * @param rs the resultset to use. Needs to be a scrollable.
	 * @param splitColumn the group column
	 * @param report the report object
	 * @param reportName the report name to use
	 * @param fullOutputFileName the output file name
	 * @return number of rows output
	 * @throws SQLException
	 */
	public int generateReport(ResultSet rs, int splitColumn, Report report,
			String reportName, String fullOutputFileName) throws SQLException {

		logger.debug("Entering generateReport: splitColumn={}, report={},"
				+ " reportName='{}', fullOutputFileName='{}'", splitColumn,
				report, reportName, fullOutputFileName);

		Objects.requireNonNull(rs, "rs must not be null");
		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(fullOutputFileName, "fullOutputFileName must not be null");

		this.report = report;
		this.reportName = reportName;
		this.fullOutputFileName = fullOutputFileName;

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
		int lastRow;

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

		endOutput();

		return counter + 1; // number of rows
	}

	/**
	 * Generates group output
	 *
	 * @param data the data to use
	 * @param splitColumn the group column
	 * @param report the report object
	 * @param reportName the report name to use
	 * @param fullOutputFileName the output file name
	 * @return number of rows output
	 * @throws SQLException
	 * @throws java.io.IOException
	 */
	public int generateReport(Object data, int splitColumn, Report report,
			String reportName, String fullOutputFileName) throws SQLException, IOException {

		logger.debug("Entering generateReport: splitColumn={}, report={},"
				+ " reportName='{}', fullOutputFileName='{}'", splitColumn,
				report, reportName, fullOutputFileName);

		Objects.requireNonNull(data, "data must not be null");
		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(fullOutputFileName, "fullOutputFileName must not be null");

		this.report = report;
		this.reportName = reportName;
		this.fullOutputFileName = fullOutputFileName;

		GroovyDataDetails dataDetails = RunReportHelper.getGroovyDataDetails(data);
		int rowCount = dataDetails.getRowCount();
		int colCount = dataDetails.getColCount();
		List<String> columnNames = dataDetails.getColumnNames();
		List<? extends Object> dataList = dataDetails.getDataList();

		int i;

		init();

		newLine();

		//output header columns for the result set columns
		for (i = 0; i < colCount; i++) {
			addHeaderCell(columnNames.get(i));
		}

		int maxRows = Config.getMaxRows("xlsx");

		int firstRow = 0;
		int lastRow;

		int counter = 0;
		StringBuilder cmpStr = null; // temporary string used to compare values
		StringBuilder tmpCmpStr; // temporary string used to compare values
		String summaryText = null;

		int rowIndex = -1;
		while ((rowIndex < rowCount) && (counter < maxRows)) {
			rowIndex++;

			newLine();

			beginLines();

			cmpStr = new StringBuilder();
			List<String> summaryValues = new ArrayList<>();

			Object dataRow = dataList.get(rowIndex);

			// Output Main Data (only one row, obviously)
			for (i = 0; i < splitColumn; i++) {
				String value = RunReportHelper.getStringRowValue(dataRow, i + 1, columnNames);

				addCell(value);
				cmpStr.append(value);

				summaryValues.add(value);
			}

			summaryText = StringUtils.join(summaryValues, "-");

			firstRow = rowIndex;

			// Output Sub Data (first line)
			for (; i < colCount; i++) {
				addCell(RunReportHelper.getStringRowValue(dataRow, i + 1, columnNames));
			}

			boolean currentMain = true;
			while (currentMain && counter < maxRows) {  // next line
				// Get Main Data in order to compare it
				rowIndex++;
				if (rowIndex < rowCount) {
					Object dataRow2 = dataList.get(rowIndex);
					counter++;
					tmpCmpStr = new StringBuilder();

					for (i = 0; i < splitColumn; i++) {
						tmpCmpStr.append(RunReportHelper.getStringRowValue(dataRow2, i + 1, columnNames));
					}

					if (tmpCmpStr.toString().equals(cmpStr.toString()) == true) {
						//row has same main data as previous row
						// Add data lines
						newLine();
						for (i = 0; i < colCount; i++) {
							addCell(RunReportHelper.getStringRowValue(dataRow2, i + 1, columnNames));
						}
					} else {
						//row has different main from previous row
						//create a grouping
						//http://www.mysamplecode.com/2011/10/apache-poi-excel-row-group-collapse.html
						lastRow = rowIndex;
						sheet.groupRow(firstRow - 1, lastRow - 1);
						sheet.setRowGroupCollapsed(firstRow - 1, true);

						newLine();
						addSummaryCell(summaryText);

						int summaryRowCount = (lastRow - firstRow) + 1;
						addSummaryCellNumeric(summaryRowCount);

						currentMain = false;
						rowIndex--;
					}
				} else {
					currentMain = false;
					// The outer and inner while will exit
				}
			}
		}

		if (cmpStr != null) {
			lastRow = rowIndex;
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
