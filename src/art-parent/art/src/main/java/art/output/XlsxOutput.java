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

import art.reportparameter.ReportParameter;
import art.utils.ArtUtils;
import java.io.*;
import java.util.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates xlsx output
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
	private CellStyle totalStyle;
	private CellStyle numberStyle;
	private int currentRow;
	private int cellNumber;
	private String templateFileName;
	private Map<String, CellStyle> styles;
	private Row row;
	private Cell cell;
	private final String javaDateFormat;
	private final String numberFormat;

	public XlsxOutput(String javaDateFormat, String numberFormat) {
		this.javaDateFormat = javaDateFormat;
		this.numberFormat = numberFormat;
	}

	/**
	 * Resets global variables in readiness for output generation. Especially
	 * important for burst output where the same standard output object is
	 * reused for multiple output runs.
	 */
	private void resetVariables() {
		wb = null;
		sh = null;
		row = null;
		cell = null;
		currentRow = 0;
		cellNumber = 0;
		headerStyle = null;
		bodyStyle = null;
		dateStyle = null;
		totalStyle = null;
		numberStyle = null;

		if (styles != null) {
			styles.clear();
			styles = null;
		}
	}

	@Override
	public void init() {
		try {
			resetVariables();

			// Create a template file. Setup sheets and workbook-level objects e.g. cell styles, number formats, etc.
			String sheetName = WorkbookUtil.createSafeSheetName(reportName);

			String fullPath = FilenameUtils.getFullPath(fullOutputFileName);
			String baseName = FilenameUtils.getBaseName(fullOutputFileName);
			templateFileName = fullPath + "template-" + baseName + ".xlsx";

			//save the template
			try (FileOutputStream fout = new FileOutputStream(templateFileName); XSSFWorkbook tmpwb = new XSSFWorkbook()) {
				tmpwb.createSheet(sheetName);
				tmpwb.write(fout);
			}

			XSSFWorkbook wb_template;
			try (FileInputStream inputStream = new FileInputStream(templateFileName)) {
				wb_template = new XSSFWorkbook(inputStream);
			}

			wb = new SXSSFWorkbook(wb_template);
			wb.setCompressTempFiles(true);

			sh = (SXSSFSheet) wb.getSheetAt(0);
			sh.setRandomAccessWindowSize(100);// keep 100 rows in memory, exceeding rows will be flushed to disk

			styles = new HashMap<>();

			Font headerFont = wb.createFont();
			headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			headerFont.setColor(IndexedColors.BLUE.getIndex());
			short headerFontSize = 12;
			headerFont.setFontHeightInPoints(headerFontSize);
			headerStyle = wb.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
			styles.put("header", headerStyle);

			Font bodyFont = wb.createFont();
			bodyFont.setColor(Font.COLOR_NORMAL);
			short bodyFontSize = 10;
			bodyFont.setFontHeightInPoints(bodyFontSize);
			bodyStyle = wb.createCellStyle();
			bodyStyle.setFont(bodyFont);
			styles.put("body", bodyStyle);

			dateStyle = wb.createCellStyle();
//			dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
			DataFormat poiFormat = wb.createDataFormat();
			String excelDateFormat = DateFormatConverter.convert(locale, javaDateFormat);
			dateStyle.setDataFormat(poiFormat.getFormat(excelDateFormat));
			dateStyle.setFont(bodyFont);
			styles.put("date", dateStyle);

			if (StringUtils.isNotBlank(numberFormat)) {
				numberStyle = wb.createCellStyle();
				DataFormat poiFormat2 = wb.createDataFormat();
				numberStyle.setDataFormat(poiFormat2.getFormat(numberFormat));
				numberStyle.setFont(bodyFont);
				styles.put("number", numberStyle);
			}

			Font totalFont = wb.createFont();
			totalFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
			totalFont.setColor(Font.COLOR_NORMAL);
			totalFont.setFontHeightInPoints(bodyFontSize);
			totalStyle = wb.createCellStyle();
			totalStyle.setFont(totalFont);
			styles.put("total", totalStyle);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addTitle() {
		newRow();
		addCellString(reportName);
		addCellString(ArtUtils.isoDateTimeSecondsFormatter.format(new Date()));
		newRow();
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			newRow();
			String paramLabel = reportParam.getParameter().getLabel();
			String paramDisplayValues = reportParam.getDisplayValues();
			addHeaderCell(paramLabel);
			addCellString(paramDisplayValues);
		}

		newRow();
	}

	@Override
	public void beginHeader() {
		newRow();
	}

	@Override
	public void addHeaderCell(String value) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(XSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new XSSFRichTextString(value));
		cell.setCellStyle(headerStyle);
	}

	@Override
	public void beginRows() {
		cellNumber = 0;
	}

	@Override
	public void addCellString(String value) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(XSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new XSSFRichTextString(value));
		cell.setCellStyle(bodyStyle);
	}

	@Override
	public void addCellNumeric(Double value) {
		cell = row.createCell(cellNumber++);

		if (value != null) {
			cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
			cell.setCellValue(value);
			if (numberStyle == null) {
				cell.setCellStyle(bodyStyle);
			} else {
				cell.setCellStyle(numberStyle);
			}
		}
	}

	@Override
	public void addCellDate(Date value) {
		//https://poi.apache.org/spreadsheet/quick-guide.html#CreateDateCells
		cell = row.createCell(cellNumber++);

		if (value != null) {
			cell.setCellValue(value);
			cell.setCellStyle(dateStyle);
		}
	}

	@Override
	public void newRow() {
		row = sh.createRow(currentRow++);
		cellNumber = 0;
	}

	@Override
	public void addCellTotal(Double value) {
		cell = row.createCell(cellNumber++);

		if (value != null) {
			cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
			cell.setCellValue(value);
			cell.setCellStyle(totalStyle);
		}
	}

	@Override
	public void endOutput() {
		//https://poi.apache.org/spreadsheet/quick-guide.html#Autofit
		for (int i = 0; i < totalColumnCount; i++) {
			sh.autoSizeColumn(i);
		}

		try {
			try (FileOutputStream fout = new FileOutputStream(fullOutputFileName)) {
				wb.write(fout);
			}

			// dispose of temporary files backing this workbook on disk
			wb.dispose();

			//delete template file
			File templateFile = new File(templateFileName);
			boolean deleted = templateFile.delete();
			if (!deleted) {
				logger.warn("Template file not deleted: {}", templateFileName);
			}
		} catch (IOException ex) {
			logger.error("Error", ex);
		}
	}
}
