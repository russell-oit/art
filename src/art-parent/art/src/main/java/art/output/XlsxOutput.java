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
import art.utils.ArtUtils;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
		sheet = null;
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

			sheet = wb.getSheetAt(0);
			sheet.setRandomAccessWindowSize(100);// keep 100 rows in memory, exceeding rows will be flushed to disk

			sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);

			PageOrientation pageOrientation = report.getPageOrientation();
			if (pageOrientation == PageOrientation.Landscape) {
				sheet.getPrintSetup().setLandscape(true);
			}

			styles = new HashMap<>();

			Font headerFont = wb.createFont();
			headerFont.setBold(true);
			headerFont.setColor(IndexedColors.BLUE.getIndex());
			short headerFontSize = 12;
			headerFont.setFontHeightInPoints(headerFontSize);

			headerStyle = wb.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setBorderBottom(BorderStyle.THIN);
			styles.put("header", headerStyle);

			Font bodyFont = wb.createFont();
			bodyFont.setColor(Font.COLOR_NORMAL);
			short bodyFontSize = 10;
			bodyFont.setFontHeightInPoints(bodyFontSize);

			bodyStyle = wb.createCellStyle();
			bodyStyle.setFont(bodyFont);
			styles.put("body", bodyStyle);

			dateStyle = wb.createCellStyle();
			if (StringUtils.isBlank(javaDateFormat)) {
				dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
			} else {
				DataFormat poiFormat = wb.createDataFormat();
				String excelDateFormat = DateFormatConverter.convert(locale, javaDateFormat);
				dateStyle.setDataFormat(poiFormat.getFormat(excelDateFormat));
			}
			dateStyle.setFont(bodyFont);
			styles.put("date", dateStyle);

			if (StringUtils.isNotBlank(numberFormat)) {
				numberStyle = wb.createCellStyle();
				DataFormat poiFormat = wb.createDataFormat();
				numberStyle.setDataFormat(poiFormat.getFormat(numberFormat));
				numberStyle.setFont(bodyFont);
				styles.put("number", numberStyle);
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
			styles.put("total", totalStyle);
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
		cell.setCellType(CellType.STRING);
		cell.setCellValue(new XSSFRichTextString(value));
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
		//https://poi.apache.org/spreadsheet/quick-guide.html#CreateDateCells
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

			addModifyPassword();

			if (wb != null) {
				try (FileOutputStream fout = new FileOutputStream(fullOutputFileName)) {
					wb.write(fout);
				}

				// dispose of temporary files backing this workbook on disk
				wb.dispose();
			}

			//delete template file
			File templateFile = new File(templateFileName);
			templateFile.delete();

			addOpenPassword();
		} catch (IOException | GeneralSecurityException | InvalidFormatException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Adds a password to prevent modification of the worksheet
	 */
	private void addModifyPassword() {
		//http://www.quicklyjava.com/create-password-protected-excel-using-apache-poi/
		//http://www.quicklyjava.com/create-password-protected-excel-sheets-using-apache-poi/
		//https://poi.apache.org/encryption.html
		//https://blogs.msdn.microsoft.com/david_leblanc/2010/04/16/dont-use-office-rc4-encryption-really-just-dont-do-it/
		//https://stackoverflow.com/questions/14701322/apache-poi-how-to-protect-sheet-with-options
		//https://stackoverflow.com/questions/17675685/apache-poi-excel-sheet-protection-and-data-validation
		//https://stackoverflow.com/questions/45097889/apache-poi-sheet-password-not-working
		if (sheet != null && StringUtils.isNotEmpty(report.getModifyPassword())) {
			sheet.protectSheet(report.getModifyPassword());
		}
	}

	/**
	 * Adds a password to prevent opening of the workbook
	 *
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @throws InvalidFormatException
	 */
	private void addOpenPassword() throws IOException, GeneralSecurityException, InvalidFormatException {
		if (StringUtils.isEmpty(report.getOpenPassword())) {
			return;
		}

		POIFSFileSystem fs = new POIFSFileSystem();
		EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
		// EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile, CipherAlgorithm.aes192, HashAlgorithm.sha384, -1, -1, null);

		Encryptor enc = info.getEncryptor();
		enc.confirmPassword(report.getOpenPassword());

		// Read in an existing OOXML file
		try (OPCPackage opc = OPCPackage.open(new File(fullOutputFileName), PackageAccess.READ_WRITE)) {
			OutputStream os = enc.getDataStream(fs);
			opc.save(os);
		}

		// Write out the encrypted version
		try (FileOutputStream fos = new FileOutputStream(fullOutputFileName)) {
			fs.writeFilesystem(fos);
		}
	}
}
