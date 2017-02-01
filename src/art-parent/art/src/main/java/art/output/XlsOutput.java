/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.enums.ZipType;
import art.reportparameter.ReportParameter;
import art.utils.ArtUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.ss.util.WorkbookUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates xls output
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class XlsOutput extends StandardOutput {

	private static final Logger logger = LoggerFactory.getLogger(XlsOutput.class);

	private FileOutputStream fout;
	private ZipOutputStream zout;
	private HSSFWorkbook wb;
	private HSSFSheet sheet;
	private HSSFRow row;
	private HSSFCell cell;
	private HSSFCellStyle headerStyle;
	private HSSFCellStyle bodyStyle;
	private HSSFCellStyle dateStyle;
	private HSSFCellStyle totalStyle;
	private HSSFCellStyle numberStyle;
	private int currentRow;
	private int cellNumber;
	private final ZipType zipType;
	private final String javaDateFormat;
	private final String numberFormat;

	public XlsOutput(String javaDateFormat, String numberFormat) {
		zipType = ZipType.None;
		this.javaDateFormat = javaDateFormat;
		this.numberFormat = numberFormat;
	}

	public XlsOutput(ZipType zipType, String javaDateFormat, String numberFormat) {
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

			String filename = FilenameUtils.getBaseName(fullOutputFileName);

			if (zipType == ZipType.Zip) {
				ZipEntry ze = new ZipEntry(filename + ".xls");
				zout = new ZipOutputStream(fout);
				zout.putNextEntry(ze);
			}

			String sheetName = WorkbookUtil.createSafeSheetName(reportName);
			wb = new HSSFWorkbook();
			sheet = wb.createSheet(sheetName);

			HSSFFont headerFont = wb.createFont();
			headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			headerFont.setColor(HSSFColor.BLUE.index);
			short headerFontSize = 12;
			headerFont.setFontHeightInPoints(headerFontSize);
			
			headerStyle = wb.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);

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

			if (StringUtils.isNotBlank(numberFormat)) {
				numberStyle = wb.createCellStyle();
				DataFormat poiFormat = wb.createDataFormat();
				numberStyle.setDataFormat(poiFormat.getFormat(numberFormat));
				numberStyle.setFont(bodyFont);
			}

			HSSFFont totalFont = wb.createFont();
			totalFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			totalFont.setColor(HSSFFont.COLOR_NORMAL);
			totalFont.setFontHeightInPoints(bodyFontSize);
			
			totalStyle = wb.createCellStyle();
			if (StringUtils.isNotBlank(numberFormat)) {
				DataFormat poiFormat = wb.createDataFormat();
				totalStyle.setDataFormat(poiFormat.getFormat(numberFormat));
			}
			totalStyle.setFont(totalFont);
		} catch (IOException ex) {
			logger.error("Error", ex);
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
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
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
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(value));
		cell.setCellStyle(bodyStyle);
	}

	@Override
	public void addCellNumeric(Double value) {
		cell = row.createCell(cellNumber++);

		if (value != null) {
			cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
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
	public void newRow() {
		row = sheet.createRow(currentRow++);
		cellNumber = 0;
	}

	@Override
	public void addCellTotal(Double value) {
		cell = row.createCell(cellNumber++);

		if (value != null) {
			cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
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
		for (int i = 0; i < totalColumnCount; i++) {
			sheet.autoSizeColumn(i);
		}

		try {
			if (zout == null) {
				wb.write(fout);
			} else {
				wb.write(zout);
				zout.close();
			}
			fout.close();
		} catch (IOException ex) {
			logger.error("Error", ex);
		}
	}
}
