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
import art.utils.ArtQueryParam;
import art.utils.ArtUtils;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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
	Workbook wb;
	Sheet sh;
	CellStyle headerStyle;
	CellStyle bodyStyle;
	CellStyle dateStyle;
	Font headerFont;
	Font bodyFont;
	int currentRow;
	String filename;
	String fullFileName;
	PrintWriter htmlout;
	String fileUserName;
	int maxRows;
	int columns;
	int cellNumber;
	Map<Integer, ArtQueryParam> displayParams;
	String templateFileName;
	String xmlFileName;
	Map<String, CellStyle> styles;
	String sheetRef; //name of the zip entry holding sheet data, e.g. /xl/worksheets/sheet1.xml
	SpreadsheetWriter sw; //class that outputs temporary xlsx file
	Writer fw; //writer for temporary xml file
	File xmlFile; //file object for temporary xml file
	boolean errorOccurred = false; //flag that is set if an error occurs while creating the data file. so that processing can be stopped
	String exportPath;

	/**
	 * Initialise objects required to generate output
	 */
	@Override
	public void init() {
		try {
			// Create a template file. Setup sheets and workbook-level objects e.g. cell styles, number formats, etc.						
			wb = new XSSFWorkbook();
			final int MAX_SHEET_NAME = 30; //excel max is 31
			String sheetName = reportName;
			if (sheetName.length() > MAX_SHEET_NAME) {
				sheetName = sheetName.substring(0, MAX_SHEET_NAME);
			}
			XSSFSheet xsh = (XSSFSheet) wb.createSheet(sheetName);
			sheetRef = xsh.getPackagePart().getPartName().getName();

			styles = new HashMap<String, CellStyle>();

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

			//save the template			
			FileOutputStream fout = new FileOutputStream(templateFileName);
			try {
				wb.write(fout);
			} finally {
				fout.close();
			}

			//create xml file
			xmlFile = new File(xmlFileName);
			boolean created = xmlFile.createNewFile();
			if (!created) {
				logger.warn("Couldn't create xmlFile. File already exists: {}", xmlFileName);
			}
			fw = new FileWriter(xmlFile);
			sw = new SpreadsheetWriter(fw);
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void beginHeader() {
		try {
			sw.beginSheet();

			currentRow = 0;

			newRow();
			addCellString(reportName + " - " + ArtUtils.isoDateTimeFormatter.format(new Date()));
		newRow();

		} catch (Exception e) {
			logger.error("Error", e);
			errorOccurred = true; //set flag so that no more rows are processed
		}
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			// rows with parameter names
			newRow();
			Parameter param = reportParam.getParameter();
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
		try {
			sw.createCell(cellNumber++, s, styles.get("header").getIndex());
		} catch (Exception e) {
			logger.error("Error", e);
			errorOccurred = true; //set flag so that no more rows are processed
		}
	}


	@Override
	public void beginRows() {
		cellNumber = 0;
	}

	@Override
	public void addCellString(String s) {
		try {
			if (s == null) {
				//output blank string
				sw.createCell(cellNumber++, "", styles.get("body").getIndex());
			} else {
				sw.createCell(cellNumber++, s, styles.get("body").getIndex());
			}
		} catch (Exception e) {
			logger.error("Error", e);
			errorOccurred = true; //set flag so that no more rows are processed
		}
	}

	@Override
	public void addCellNumeric(Double d) {
		try {
			if (d == null) {
				//output blank string
				sw.createCell(cellNumber++, "", styles.get("body").getIndex());
			} else {
				sw.createCell(cellNumber++, d, styles.get("body").getIndex());
			}
		} catch (Exception e) {
			logger.error("Error", e);
			errorOccurred = true; //set flag so that no more rows are processed
		}
	}

	@Override
	public void addCellDate(Date d) {
		try {
			if (d == null) {
				//output blank string
				sw.createCell(cellNumber++, "", styles.get("body").getIndex());
			} else {
				sw.createCell(cellNumber++, Config.getDateDisplayString(d), styles.get("date").getIndex());
			}
		} catch (Exception e) {
			logger.error("Error", e);
			errorOccurred = true; //set flag so that no more rows are processed
		}
	}

	@Override
	public void newRow() {
		boolean lineAdded = false;
		cellNumber = 0;

		try {
			if (errorOccurred) {
				//an error occurred. don't continue			
				if (htmlout != null) {
					htmlout.println("<span style=\"color:red\">An error occurred while running the query. Query not completed.</span>");
				}
				endRows(); // close files						
			} else {
				if (currentRow > 0) {
					sw.endRow(); //need to output end row marker before inserting a new row
				}

				sw.insertRow(currentRow++);
				if (currentRow <= maxRows + 2) { //+2 because of query title and column header rows					
					lineAdded = true;
				} else {
					//htmlout not used for scheduled jobs
					if (htmlout != null) {
						htmlout.println("<span style=\"color:red\">Too many rows (>"
								+ maxRows
								+ ")! Data not completed. Please narrow your search!</span>");
					}
					addCellString("Maximum number of rows exceeded! Query not completed.");
					endRows(); // close files				
				}
			}
		} catch (Exception e) {
			logger.error("Error", e);
			errorOccurred = true; //set flag so that no more rows are processed
		}

//		return lineAdded;
	}

	@Override
	public void endRows() {
		try {
			sw.endRow();
			sw.endSheet();
			fw.close();

			//Substitute the template with the generated data
			FileOutputStream out = new FileOutputStream(fullFileName);
			File templateFile = new File(templateFileName);
			try {
				substitute(templateFile, xmlFile, sheetRef.substring(1), out);
			} finally {
				out.close();
			}

			//delete template and xml file	
			boolean deleted;
			deleted = xmlFile.delete();
			if (!deleted) {
				logger.warn("xmlFile not deleted: {}", xmlFileName);
			}
			deleted = templateFile.delete();
			if (!deleted) {
				logger.warn("templateFile not deleted: {}", templateFileName);
			}

			//htmlout not used for scheduled jobs
			if (htmlout != null) {
				htmlout.println("<p><div align=\"center\"><table border=\"0\" width=\"90%\">");
				htmlout.println("<tr><td colspan=\"2\" class=\"data\" align=\"center\" >"
						+ "<a type=\"application/octet-stream\" href=\"../export/" + filename + "\" target=\"_blank\"> "
						+ filename + "</a>"
						+ "</td></tr>");
				htmlout.println("</table></div></p>");
			}

		} catch (Exception e) {
			logger.error("Error", e);
		}
	}

	/**
	 * substitute temporary xlsx template file with data from temporary xml file
	 * containing final output
	 *
	 * @param zipfile the template file
	 * @param tmpfile the XML file with the sheet data
	 * @param entry the name of the sheet entry to substitute, e.g.
	 * xl/worksheets/sheet1.xml
	 * @param out the stream to write the result to
	 */
	private void substitute(File zipfile, File tmpfile, String entry, OutputStream out) throws IOException {
		ZipFile zip = new ZipFile(zipfile);

		ZipOutputStream zos = new ZipOutputStream(out);

		try {
			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zip.entries();
			while (en.hasMoreElements()) {
				ZipEntry ze = en.nextElement();
				if (!ze.getName().equals(entry)) {
					zos.putNextEntry(new ZipEntry(ze.getName()));
					InputStream is = zip.getInputStream(ze);
					try {
						copyStream(is, zos);
					} finally {
						is.close();
					}
				}
			}
			zos.putNextEntry(new ZipEntry(entry));
			InputStream is = new FileInputStream(tmpfile);
			try {
				copyStream(is, zos);
			} finally {
				is.close();
			}

		} finally {
			zos.close();
			zip.close();
		}
	}

	private void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] chunk = new byte[1024];
		int count;
		while ((count = in.read(chunk)) >= 0) {
			out.write(chunk, 0, count);
		}
	}

}
