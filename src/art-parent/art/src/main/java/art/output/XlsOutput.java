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

import art.enums.ZipType;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtQueryParam;
import art.utils.ArtUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate xls output
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class XlsOutput extends StandardOutput {

	private static final Logger logger = LoggerFactory.getLogger(XlsOutput.class);

	FileOutputStream fout;
	ZipOutputStream zipout;
	HSSFWorkbook wb;
	HSSFSheet sheet;
	HSSFRow row;
	HSSFCell cell;
	HSSFCellStyle headerStyle;
	HSSFCellStyle bodyStyle;
	HSSFCellStyle dateStyle;
	HSSFFont headerFont;
	HSSFFont bodyFont;
	int currentRow;
	PrintWriter htmlout;
	String fileUserName;
	int maxRows;
	int columns;
	int cellNumber;
	Map<Integer, ArtQueryParam> displayParams;
	String exportPath;
	ZipType zipType;
	
	public XlsOutput(){
		zipType=ZipType.None;
	}

	public XlsOutput(ZipType zipType) {
		this.zipType = zipType;
	}

	/**
	 * Initialise objects required to generate output
	 */
	public void init() {

		try {
			fout = new FileOutputStream(fullOutputFilename);
			
			String filename=FilenameUtils.getBaseName(fullOutputFilename);

			if (zipType == ZipType.Zip) {
				ZipEntry ze = new ZipEntry(filename + ".xls");
				zipout = new ZipOutputStream(fout);
				zipout.putNextEntry(ze);
			}

			wb = new HSSFWorkbook();
			sheet = wb.createSheet();
			row = null;
			cell = null;
			headerStyle = wb.createCellStyle();
			bodyStyle = wb.createCellStyle();
			headerFont = wb.createFont();
			bodyFont = wb.createFont();

			currentRow = 0;

			headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			headerFont.setColor(org.apache.poi.hssf.util.HSSFColor.BLUE.index);
			headerFont.setFontHeightInPoints((short) 12);
			headerStyle.setFont(headerFont);
			headerStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);

			bodyFont.setColor(HSSFFont.COLOR_NORMAL);
			bodyFont.setFontHeightInPoints((short) 10);
			bodyStyle.setFont(bodyFont);

			dateStyle = wb.createCellStyle();
			dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
			dateStyle.setFont(bodyFont);

		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void beginHeader() {
		final int MAX_SHEET_NAME = 30; //excel max is 31
		String sheetName = reportName;
		if (sheetName.length() > MAX_SHEET_NAME) {
			sheetName = sheetName.substring(0, MAX_SHEET_NAME);
		}

		wb.setSheetName(0, sheetName);

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
			addCellString(reportParam.getNameAndDisplayValues());
		}
	}

	@Override
	public void endHeader() {
		// prepare row for columns header
		newRow();
	}

	@Override
	public void addHeaderCell(String s) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(s));
		cell.setCellStyle(headerStyle);
	}

	@Override
	public void beginRows() {
		cellNumber = 0;
	}

	@Override
	public void addCellString(String s) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		cell.setCellValue(new HSSFRichTextString(s));
		cell.setCellStyle(bodyStyle);
	}

	@Override
	public void addCellNumeric(Double value) {
		cell = row.createCell(cellNumber++);

		if (value == null) {
			return;
		} else {
			cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			cell.setCellValue(value);
			cell.setCellStyle(bodyStyle);
		}
	}

	@Override
	public void addCellDate(Date value) {
		cell = row.createCell(cellNumber++);

		if (value == null) {
			return;
		} else {
			cell.setCellValue(Config.getDateDisplayString(value));
			cell.setCellStyle(dateStyle);
		}
	}

	@Override
	public void newRow() {
		//open new row
		row = sheet.createRow(currentRow++);
		cellNumber = 0;
	}

	@Override
	public void endRows() {
		try {
			if (zipout == null) {
				wb.write(fout);
			} else {
				wb.write(zipout);
				zipout.close();
			}
			fout.close();
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}
}
