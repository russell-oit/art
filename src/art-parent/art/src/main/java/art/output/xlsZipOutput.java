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
package art.output;

import art.servlets.ArtDBCP;
import art.utils.ArtQueryParam;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate xls zip output
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class xlsZipOutput implements ArtOutputInterface {

	final static Logger logger = LoggerFactory.getLogger(xlsZipOutput.class);
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
	String exportPath;

	/**
	 * Constructor
	 */
	public xlsZipOutput() {
	}

	@Override
	public String getName() {
		return "Spreadsheet (compressed xls)";
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
		
		wb.setSheetName(0, queryName);

		newLine();
		addCellString(queryName + " - " + y_m_d.replace('_', '-') + " " + h_m_s.replace('_', ':'));

		// Output parameter values list
		if (displayParams != null && !displayParams.isEmpty()) {
			// rows with parameter names
			newLine();
			for (Map.Entry<Integer, ArtQueryParam> entry : displayParams.entrySet()) {
				ArtQueryParam param =  entry.getValue();
				String paramName = param.getName();
				addHeaderCell(paramName);
			}
			
			// rows with parameter values
			newLine();
			for (Map.Entry<Integer, ArtQueryParam> entry : displayParams.entrySet()) {
                ArtQueryParam param=entry.getValue();                
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
	}

	@Override
	public void addHeaderCell(String s) {
		cell = row.createCell(cellNumber++);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);

		//upgraded from poi 2.5. setencoding deprecated and removed. POI now automatically handles Unicode without forcing the encoding
		//use undeprecated setcellvalue overload
		//c.setEncoding(HSSFCell.ENCODING_UTF_16); 
		//c.setCellValue(s);
		cell.setCellValue(new HSSFRichTextString(s));

		cell.setCellStyle(headerStyle);
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
		cell = row.createCell(cellNumber++);
		cell.setCellType(HSSFCell.CELL_TYPE_STRING);

		//upgraded from poi 2.5. setencoding deprecated and removed. POI now automatically handles Unicode without forcing the encoding
		//use undeprecated setcellvalue overload
		//c.setEncoding(HSSFCell.ENCODING_UTF_16); 
		//c.setCellValue(s);
		cell.setCellValue(new HSSFRichTextString(s));

		cell.setCellStyle(bodyStyle);
	}

	@Override
	public void addCellDouble(Double d) {
		cell = row.createCell(cellNumber++);
		if (d != null) {
			cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			cell.setCellValue(d);
			cell.setCellStyle(bodyStyle);
		}
	}

	@Override
	public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT		
		cell = row.createCell(cellNumber++);
		if (i != null) {
			cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			cell.setCellValue(i);
			cell.setCellStyle(bodyStyle);
		}
	}

	@Override
	public void addCellDate(Date d) {
		cell = row.createCell(cellNumber++);
		if (d != null) {
			cell.setCellValue(ArtDBCP.getDateDisplayString(d));
			cell.setCellStyle(dateStyle);
		}
	}

	@Override
	public boolean newLine() {
		row = sheet.createRow(currentRow++);
		cellNumber = 0;

		if (currentRow <= maxRows + 2) { //+2 because of query title and column header rows
			return true;
		} else {
			//htmlout not used for scheduled jobs
			if (htmlout != null) {
				htmlout.println("<span style=\"color:red\">Too many rows (>"
						+ maxRows
						+ ")! Data not completed. Please narrow your search!</span>");
			}
			addCellString("Maximum number of rows exceeded! Query not completed.");
			endLines(); // close files
			return false;
		}
	}

	@Override
	public void endLines() {
		try {
			wb.write(zipout);
			zipout.close();
			fout.close();

			//htmlout not used for scheduled jobs
			if (htmlout != null) {
				htmlout.println("<p><div align=\"Center\"><table border=\"0\" width=\"90%\">");
				htmlout.println("<tr><td colspan=2 class=\"data\" align=\"center\" >"
						+ "<a  type=\"application/octet-stream\" href=\"../export/" + filename + ".zip\"> "
						+ filename + ".zip</a>"
						+ "</td></tr>");
				htmlout.println("</table></div></p>");
			}

		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

	@Override
	public boolean isShowQueryHeaderAndFooter() {
		return true;
	}

	/**
	 * Build filename for output file
	 */
	private void buildOutputFileName() {
		// Build filename		
		Date today = new Date();

		String dateFormat = "yyyy_MM_dd";
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		y_m_d = dateFormatter.format(today);

		String timeFormat = "HH_mm_ss";
		SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
		h_m_s = timeFormatter.format(today);

		filename = fileUserName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ArtDBCP.getRandomString();
		filename = ArtDBCP.cleanFileName(filename); //replace characters that would make an invalid filename

		fullFileName = exportPath + filename + ".zip";
	}

	/**
	 * Initialise objects required to generate output
	 */
	private void initializeOutput() {
		buildOutputFileName();

		try {
			ZipEntry ze = new ZipEntry(filename + ".xls");

			fout = new FileOutputStream(fullFileName);
			zipout = new ZipOutputStream(fout);
			zipout.putNextEntry(ze);
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
}
