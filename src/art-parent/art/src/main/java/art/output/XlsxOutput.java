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

import art.enums.PageOrientation;
import art.reportparameter.ReportParameter;
import art.runreport.RunReportHelper;
import art.utils.ArtUtils;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

/**
 * Generates xlsx output
 *
 * @author Timothy Anyona
 */
public class XlsxOutput extends StandardOutput {

	private SXSSFWorkbook wb;
	private SXSSFSheet sheet;
	private CellStyle headerStyle;
	private CellStyle bodyStyle;
	private CellStyle dateStyle;
	private CellStyle timeStyle;
	private CellStyle totalStyle;
	private CellStyle numberStyle;
	private int currentRow;
	private int cellNumber;
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
		sheet = null;
		row = null;
		cell = null;
		currentRow = 0;
		cellNumber = 0;
		headerStyle = null;
		bodyStyle = null;
		dateStyle = null;
		timeStyle = null;
		totalStyle = null;
		numberStyle = null;
	}

	@Override
	public void init() {
		resetVariables();

		//https://poi.apache.org/spreadsheet/how-to.html#sxssf
		wb = new SXSSFWorkbook();
		wb.setCompressTempFiles(true);

		String sheetName = WorkbookUtil.createSafeSheetName(reportName);
		sheet = wb.createSheet(sheetName);
		sheet.setRandomAccessWindowSize(100);// keep 100 rows in memory, exceeding rows will be flushed to disk

		sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);

		PageOrientation pageOrientation = report.getPageOrientation();
		if (pageOrientation == PageOrientation.Landscape) {
			sheet.getPrintSetup().setLandscape(true);
		}

		Font headerFont = wb.createFont();
		headerFont.setBold(true);
		headerFont.setColor(IndexedColors.BLUE.getIndex());
		short headerFontSize = 12;
		headerFont.setFontHeightInPoints(headerFontSize);

		headerStyle = wb.createCellStyle();
		headerStyle.setFont(headerFont);
		headerStyle.setBorderBottom(BorderStyle.THIN);

		Font bodyFont = wb.createFont();
		bodyFont.setColor(Font.COLOR_NORMAL);
		short bodyFontSize = 10;
		bodyFont.setFontHeightInPoints(bodyFontSize);

		bodyStyle = wb.createCellStyle();
		bodyStyle.setFont(bodyFont);

		dateStyle = wb.createCellStyle();
		if (StringUtils.isBlank(javaDateFormat)) {
			//https://poi.apache.org/apidocs/dev/org/apache/poi/ss/usermodel/BuiltinFormats.html
			dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
		} else {
			DataFormat poiFormat = wb.createDataFormat();
			String excelDateFormat = DateFormatConverter.convert(locale, javaDateFormat);
			dateStyle.setDataFormat(poiFormat.getFormat(excelDateFormat));
		}
		dateStyle.setFont(bodyFont);
		
		timeStyle = wb.createCellStyle();
		if (StringUtils.isBlank(javaDateFormat)) {
			timeStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("h:mm:ss"));
		} else {
			DataFormat poiFormat = wb.createDataFormat();
			String excelDateFormat = DateFormatConverter.convert(locale, javaDateFormat);
			timeStyle.setDataFormat(poiFormat.getFormat(excelDateFormat));
		}
		timeStyle.setFont(bodyFont);

		if (StringUtils.isNotBlank(numberFormat)) {
			numberStyle = wb.createCellStyle();
			DataFormat poiFormat = wb.createDataFormat();
			numberStyle.setDataFormat(poiFormat.getFormat(numberFormat));
			numberStyle.setFont(bodyFont);
		}

		Font totalFont = wb.createFont();
		totalFont.setBold(true);
		totalFont.setColor(Font.COLOR_NORMAL);
		totalFont.setFontHeightInPoints(bodyFontSize);

		totalStyle = wb.createCellStyle();
		if (StringUtils.isNotBlank(numberFormat)) {
			DataFormat poiFormat = wb.createDataFormat();
			totalStyle.setDataFormat(poiFormat.getFormat(numberFormat));
		}
		totalStyle.setFont(totalFont);
	}

	@Override
	public void addTitle() {
		if (report.isOmitTitleRow()) {
			return;
		}

		newRow();
		addCellString(reportName);
		addCellString(ArtUtils.isoDateTimeSecondsFormatter.format(new Date()));
		newRow();
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (CollectionUtils.isEmpty(reportParamsList)) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			try {
				newRow();
				String paramLabel = reportParam.getParameter().getLocalizedLabel(locale);
				String paramDisplayValues = reportParam.getDisplayValues();
				addHeaderCell(paramLabel);
				addCellString(paramDisplayValues);
			} catch (IOException ex) {
				endOutput();
				throw new RuntimeException(ex);
			}
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
		cell.setCellValue(new XSSFRichTextString(value));
		cell.setCellStyle(bodyStyle);
	}

	@Override
	public void addCellNumeric(Double value) {
		cell = row.createCell(cellNumber++);

		if (value != null) {
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
	public void addCellTime(Date value) {
		//https://poi.apache.org/spreadsheet/quick-guide.html#CreateDateCells
		cell = row.createCell(cellNumber++);

		if (value != null) {
			cell.setCellValue(value);
			cell.setCellStyle(timeStyle);
		}
	}

	@Override
	public void addCellImage(byte[] binaryData) {
		cell = row.createCell(cellNumber++);

		if (binaryData != null) {
			//https://stackoverflow.com/questions/33712621/how-put-a-image-in-a-cell-of-excel-java
			//https://poi.apache.org/spreadsheet/quick-guide.html#Images
			int pictureIdx = wb.addPicture(binaryData, Workbook.PICTURE_TYPE_PNG);
			CreationHelper helper = wb.getCreationHelper();
			SXSSFDrawing drawing = sheet.createDrawingPatriarch();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(cellNumber - 1);
			anchor.setRow1(currentRow - 1);
			Picture pict = drawing.createPicture(anchor, pictureIdx);
			pict.resize();
		}
	}

	@Override
	public void newRow() {
		row = sheet.createRow(currentRow++);
		cellNumber = 0;
	}

	@Override
	public void addCellTotal(Double value) {
		cell = row.createCell(cellNumber++);

		if (value != null) {
			cell.setCellValue(value);
			cell.setCellStyle(totalStyle);
		}
	}

	@Override
	public void addCellTotal(Double totalValue, String formattedValue, String sortValue) {
		addCellTotal(totalValue);
	}

	@Override
	public void endOutput() {
		try {
			//set modify password
			RunReportHelper runReportHelper = new RunReportHelper();
			String modifyPassword = runReportHelper.getEffectiveModifyPassword(report, dynamicModifyPassword);

			if (StringUtils.isNotEmpty(modifyPassword)) {
				sheet.protectSheet(modifyPassword);
			}

			try (FileOutputStream fout = new FileOutputStream(fullOutputFileName)) {
				wb.write(fout);
			}

			// dispose of temporary files backing this workbook on disk
			wb.dispose();

			//set open password
			String openPassword = getEffectiveOpenPassword();

			if (StringUtils.isNotEmpty(openPassword)) {
				PoiUtils.addOpenPassword(openPassword, fullOutputFileName);
			}
		} catch (IOException | GeneralSecurityException | InvalidFormatException ex) {
			throw new RuntimeException(ex);
		}
	}

}
