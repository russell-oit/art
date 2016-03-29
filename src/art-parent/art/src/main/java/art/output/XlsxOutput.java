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

import art.parameter.Parameter;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtUtils;
import java.io.*;
import java.util.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate xlsx output. Using code based on BigGridDemo
 * (http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/xssf/usermodel/examples/BigGridDemo.java)
 * in order to enable generation of large xlsx files with limited memory
 *
 * @author Timothy Anyona
 */
public class XlsxOutput extends StandardOutput {

	private static final Logger logger = LoggerFactory.getLogger(XlsxOutput.class);
	private SXSSFWorkbook wb;
	private SXSSFSheet sh;
	private CellStyle headerStyle;
	private CellStyle bodyStyle;
	private CellStyle dateStyle;
	private Font headerFont;
	private Font bodyFont;
	private int currentRow;
	private int cellNumber;
	private String templateFileName;
	private Map<String, CellStyle> styles;
	private Row row;
	private Cell cell;

	/**
	 * Initialise objects required to generate output
	 */
	@Override
	public void init() {
		try {
			// Create a template file. Setup sheets and workbook-level objects e.g. cell styles, number formats, etc.
			final int MAX_SHEET_NAME = 30; //excel max is 31
			String sheetName = reportName;
			if (sheetName.length() > MAX_SHEET_NAME) {
				sheetName = sheetName.substring(0, MAX_SHEET_NAME);
			}

			String fullPath = FilenameUtils.getFullPath(fullOutputFilename);
			String baseName = FilenameUtils.getBaseName(fullOutputFilename);
			templateFileName = fullPath + "template-" + baseName + ".xlsx";

			//save the template
			try (FileOutputStream fout = new FileOutputStream(templateFileName)) {
				XSSFWorkbook tmpwb = new XSSFWorkbook();
				tmpwb.createSheet(sheetName);
				tmpwb.write(fout);
				tmpwb.close();
			}

			FileInputStream inputStream = new FileInputStream(templateFileName);
			XSSFWorkbook wb_template = new XSSFWorkbook(inputStream);
			inputStream.close();

			wb = new SXSSFWorkbook(wb_template);
			wb.setCompressTempFiles(true);

			sh = (SXSSFSheet) wb.getSheetAt(0);
			sh.setRandomAccessWindowSize(100);// keep 100 rows in memory, exceeding rows will be flushed to disk

			styles = new HashMap<>();

			headerStyle = wb.createCellStyle();
			headerFont = wb.createFont();
			headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.BLUE.getIndex());
			headerFont.setFontHeightInPoints((short) 12);
			headerStyle.setFont(headerFont);
			headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
			styles.put("header", headerStyle);

			bodyStyle = wb.createCellStyle();
			bodyFont = wb.createFont();
			bodyFont.setColor(Font.COLOR_NORMAL);
			bodyFont.setFontHeightInPoints((short) 10);
			bodyStyle.setFont(bodyFont);
			styles.put("body", bodyStyle);

			dateStyle = wb.createCellStyle();
			dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
			dateStyle.setFont(bodyFont);
			styles.put("date", dateStyle);

		} catch (IOException ex) {
			logger.error("Error", ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void beginHeader() {
		newRow();
		addCellString(reportName + " - " + ArtUtils.isoDateTimeFormatter.format(new Date()));
		newRow();
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			// rows with parameter names
			newRow();
			String paramDisplayValues = reportParam.getDisplayValues();
			addCellString(paramDisplayValues);
		}

		// rows with parameter values
		newRow();

		for (ReportParameter reportParam : reportParamsList) {
			// rows with parameter names
			newRow();
			Parameter param = reportParam.getParameter();
			String paramName = param.getName();
			addHeaderCell(paramName);
		}

		// prepare row for columns header
		newRow();
	}

	@Override
	public void addHeaderCell(String s) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(XSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new XSSFRichTextString(s));
		cell.setCellStyle(headerStyle);
	}

	@Override
	public void beginRows() {
		cellNumber = 0;
	}

	@Override
	public void addCellString(String s) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(XSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new XSSFRichTextString(s));
		cell.setCellStyle(bodyStyle);
	}

	@Override
	public void addCellNumeric(Double d) {
		cell = row.createCell(cellNumber++);

		if (d == null) {
			return;
		} else {
			cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
			cell.setCellValue(d);
			cell.setCellStyle(bodyStyle);
		}
	}

	@Override
	public void addCellDate(Date d) {
		cell = row.createCell(cellNumber++);

		if (d == null) {
			return;
		} else {
			cell.setCellValue(Config.getDateDisplayString(d));
			cell.setCellStyle(dateStyle);
		}
	}

	@Override
	public void newRow() {
		//open new row
		row = sh.createRow(currentRow++);
		cellNumber = 0;
	}

	@Override
	public void endRows() {
		try {
			FileOutputStream fout = new FileOutputStream(fullOutputFilename);
			wb.write(fout);
			fout.close();

			// dispose of temporary files backing this workbook on disk
			wb.dispose();

			//delete template file
			File templateFile = new File(templateFileName);
			boolean deleted = templateFile.delete();
			if (!deleted) {
				logger.warn("Template file not deleted: {}", templateFileName);
			}
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

}
