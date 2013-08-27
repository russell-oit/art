/**
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
/*
 Generate xlsx output
 Using code based on BigGridDemo (http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/xssf/usermodel/examples/BigGridDemo.java)
 in order to enable generation of large xlsx files with limited memory
 */
package art.output;

import art.servlets.ArtConfig;
import art.utils.ArtQueryParam;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate xlsx output
 *
 * @author Timothy Anyona
 */
public class xlsxOutput implements ArtOutputInterface {

	final static Logger logger = LoggerFactory.getLogger(xlsxOutput.class);
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
	String queryName;
	String fileUserName;
	int maxRows;
	int columns;
	int cellNumber;
	String y_m_d;
	String h_m_s;
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
	 * Constructor
	 */
	public xlsxOutput() {
	}

	@Override
	public String getName() {
		return "Spreadsheet (xlsx)";
	}

	@Override
	public String getContentType() {
		return "text/html;charset=utf-8";
	}

	@Override
	public void setExportPath(String s) {
		exportPath = s;
	}

	@Override
	public String getFileName() {
		return fullFileName;
	}

	@Override
	public void setWriter(PrintWriter o) {
		htmlout = o;
	}

	@Override
	public void setQueryName(String s) {
		queryName = s;
	}

	@Override
	public void setFileUserName(String s) {
		fileUserName = s;
	}

	@Override
	public void setMaxRows(int i) {
		maxRows = i;
	}

	@Override
	public void setColumnsNumber(int i) {
		columns = i;
	}

	@Override
	public void setDisplayParameters(Map<Integer, ArtQueryParam> t) {
		displayParams = t;
	}

	@Override
	public void beginHeader() {
		//initialize objects required for output
		initializeOutput();

		try {
			sw.beginSheet();

			currentRow = 0;

			newLine();
			addCellString(queryName + " - " + y_m_d.replace('_', '-') + " " + h_m_s.replace('_', ':'));

			// Output parameter values list
			if (displayParams != null && !displayParams.isEmpty()) {
				// rows with parameter names
				newLine();
				for (Map.Entry<Integer, ArtQueryParam> entry : displayParams.entrySet()) {
					ArtQueryParam param = entry.getValue();
					String paramName = param.getName();
					addHeaderCell(paramName);
				}

				// rows with parameter values
				newLine();
				for (Map.Entry<Integer, ArtQueryParam> entry : displayParams.entrySet()) {
					ArtQueryParam param = entry.getValue();
					Object pValue = param.getParamValue();
					String outputString;

					if (pValue instanceof String) {
						String paramValue = (String) pValue;
						outputString = paramValue; //default to displaying parameter value

						if (param.usesLov()) {
							//for lov parameters, show both parameter value and display string if any
							Map<String, String> lov = param.getLovValues();
							if (lov != null) {
								//get friendly/display string for this value
								String paramDisplayString = lov.get(paramValue);
								if (!StringUtils.equals(paramValue, paramDisplayString)) {
									//parameter value and display string differ. show both
									outputString = paramDisplayString + " (" + paramValue + ")";
								}
							}
						}
						addCellString(outputString);
					} else if (pValue instanceof String[]) { // multi
						String[] paramValues = (String[]) pValue;
						outputString = StringUtils.join(paramValues, ", "); //default to showing parameter values only

						if (param.usesLov()) {
							//for lov parameters, show both parameter value and display string if any
							Map<String, String> lov = param.getLovValues();
							if (lov != null) {
								//get friendly/display string for all the parameter values
								String[] paramDisplayStrings = new String[paramValues.length];
								for (int i = 0; i < paramValues.length; i++) {
									String value = paramValues[i];
									String display = lov.get(value);
									if (!StringUtils.equals(display, value)) {
										//parameter value and display string differ. show both
										paramDisplayStrings[i] = display + " (" + value + ")";
									} else {
										paramDisplayStrings[i] = value;
									}
								}
								outputString = StringUtils.join(paramDisplayStrings, ", ");
							}
						}
						addCellString(outputString);
					}
				}
			}
			// prepare row for columns header
			newLine();
		} catch (Exception e) {
			logger.error("Error", e);
			errorOccurred = true; //set flag so that no more rows are processed
		}
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
	public void addHeaderCellLeft(String s) {
		addHeaderCell(s);
	}

	@Override
	public void endHeader() {
		//nope;
	}

	@Override
	public void beginLines() {
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
	public void addCellDouble(Double d) {
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
	public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
		try {
			if (i == null) {
				//output blank string
				sw.createCell(cellNumber++, "", styles.get("body").getIndex());
			} else {
				sw.createCell(cellNumber++, i, styles.get("body").getIndex());
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
				sw.createCell(cellNumber++, ArtConfig.getDateDisplayString(d), styles.get("date").getIndex());
			}
		} catch (Exception e) {
			logger.error("Error", e);
			errorOccurred = true; //set flag so that no more rows are processed
		}
	}

	@Override
	public boolean newLine() {
		boolean lineAdded = false;
		cellNumber = 0;

		try {
			if (errorOccurred) {
				//an error occurred. don't continue			
				if (htmlout != null) {
					htmlout.println("<span style=\"color:red\">An error occurred while running the query. Query not completed.</span>");
				}
				endLines(); // close files						
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
					endLines(); // close files				
				}
			}
		} catch (Exception e) {
			logger.error("Error", e);
			errorOccurred = true; //set flag so that no more rows are processed
		}

		return lineAdded;
	}

	@Override
	public void endLines() {
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

	@Override
	public boolean isShowQueryHeaderAndFooter() {
		return true;
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

	/**
	 * Build filename for output file
	 */
	private void buildOutputFileName() {
		// Build filename		
		Date today = new Date();

		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd");
		y_m_d = dateFormatter.format(today);

		SimpleDateFormat timeFormatter = new SimpleDateFormat("HH_mm_ss");
		h_m_s = timeFormatter.format(today);

		String baseName = fileUserName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ArtConfig.getRandomString();
		baseName = ArtConfig.cleanFileName(baseName);

		filename = baseName + ".xlsx";
		fullFileName = exportPath + filename;
		templateFileName = exportPath + "template-" + filename;
		xmlFileName = exportPath + "xml-" + baseName + ".xml";
	}

	/**
	 * Initialise objects required to generate output
	 */
	private void initializeOutput() {
		buildOutputFileName();

		try {
			// Create a template file. Setup sheets and workbook-level objects e.g. cell styles, number formats, etc.						
			wb = new XSSFWorkbook();
			final int MAX_SHEET_NAME = 30; //excel max is 31
			String sheetName = queryName;
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
}
