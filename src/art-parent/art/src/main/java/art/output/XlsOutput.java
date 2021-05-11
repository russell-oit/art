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
import art.enums.ZipType;
import art.reportparameter.ReportParameter;
import art.utils.ArtUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.ss.util.WorkbookUtil;

/**
 * Generates xls output
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class XlsOutput extends StandardOutput {

	private FileOutputStream fout;
	private ZipOutputStream zout;
	private HSSFWorkbook wb;
	private HSSFSheet sheet;
	private HSSFRow row;
	private HSSFCell cell;
	private HSSFCellStyle headerStyle;
	private HSSFCellStyle bodyStyle;
	private HSSFCellStyle dateStyle;
	private HSSFCellStyle timeStyle;
	private HSSFCellStyle totalStyle;
	private HSSFCellStyle numberStyle;
	private int currentRow;
	private int cellNumber;
	private ZipType zipType = ZipType.None;
	private final String javaDateFormat;
	private final String numberFormat;

	public XlsOutput(String javaDateFormat, String numberFormat) {
		this(javaDateFormat, numberFormat, ZipType.None);
	}

	public XlsOutput(String javaDateFormat, String numberFormat, ZipType zipType) {
		this.zipType = zipType;
		this.javaDateFormat = javaDateFormat;
		this.numberFormat = numberFormat;
	}

	/**
	 * Resets global variables in readiness for output generation. Especially
	 * important for burst output where the same standard output object is
	 * reused for multiple output runs.
	 */
	private void resetVariables() {
		fout = null;
		zout = null;
		wb = null;
		sheet = null;
		row = null;
		cell = null;
		headerStyle = null;
		bodyStyle = null;
		dateStyle = null;
		timeStyle = null;
		totalStyle = null;
		numberStyle = null;
		currentRow = 0;
		cellNumber = 0;
	}

	@Override
	public void init() {
		try {
			resetVariables();

			fout = new FileOutputStream(fullOutputFileName);

			if (zipType == ZipType.Zip) {
				String filename = FilenameUtils.getBaseName(fullOutputFileName);
				ZipEntry ze = new ZipEntry(filename + ".xls");
				zout = new ZipOutputStream(fout);
				zout.putNextEntry(ze);
			}

			wb = new HSSFWorkbook();
			
			String sheetName = WorkbookUtil.createSafeSheetName(reportName);
			sheet = wb.createSheet(sheetName);

			sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);

			PageOrientation pageOrientation = report.getPageOrientation();
			if (pageOrientation == PageOrientation.Landscape) {
				//https://stackoverflow.com/questions/6743615/apache-poi-change-page-format-for-excel-worksheet
				sheet.getPrintSetup().setLandscape(true);
			}

			HSSFFont headerFont = wb.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.BLUE.getIndex());
			short headerFontSize = 12;
			headerFont.setFontHeightInPoints(headerFontSize);

			headerStyle = wb.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setBorderBottom(BorderStyle.THIN);

			HSSFFont bodyFont = wb.createFont();
			bodyFont.setColor(HSSFFont.COLOR_NORMAL);
			short bodyFontSize = 10;
			bodyFont.setFontHeightInPoints(bodyFontSize);

			bodyStyle = wb.createCellStyle();
			bodyStyle.setFont(bodyFont);

			dateStyle = wb.createCellStyle();
			if (StringUtils.isBlank(javaDateFormat)) {
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

			HSSFFont totalFont = wb.createFont();
			totalFont.setBold(true);
			totalFont.setColor(HSSFFont.COLOR_NORMAL);
			totalFont.setFontHeightInPoints(bodyFontSize);

			totalStyle = wb.createCellStyle();
			if (StringUtils.isNotBlank(numberFormat)) {
				DataFormat poiFormat = wb.createDataFormat();
				totalStyle.setDataFormat(poiFormat.getFormat(numberFormat));
			}
			totalStyle.setFont(totalFont);
		} catch (IOException ex) {
			endOutput();
			throw new RuntimeException(ex);
		}
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
		cell.setCellType(CellType.STRING);
		cell.setCellValue(new HSSFRichTextString(value));
		cell.setCellStyle(headerStyle);
	}

	@Override
	public void beginRows() {
		cellNumber = 0;
	}

	@Override
	public void addCellString(String value) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(CellType.STRING);
		cell.setCellValue(new HSSFRichTextString(value));
		cell.setCellStyle(bodyStyle);
	}

	@Override
	public void addCellNumeric(Double value) {
		cell = row.createCell(cellNumber++);

		if (value != null) {
			cell.setCellType(CellType.NUMERIC);
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
		cell = row.createCell(cellNumber++);

		if (value != null) {
			cell.setCellValue(value);
			cell.setCellStyle(dateStyle);
		}
	}
	
	@Override
	public void addCellTime(Date value) {
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
			HSSFPatriarch drawing = sheet.createDrawingPatriarch();
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
			cell.setCellType(CellType.NUMERIC);
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
			if (zout == null) {
				if (wb != null && fout != null) {
					wb.write(fout);
				}
			} else {
				if (wb != null) {
					wb.write(zout);
				}
				zout.close();
			}

			if (fout != null) {
				fout.close();
			}
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
