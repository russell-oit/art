/*
 * Copyright (C) 2001/2003  Enrico Liboni 
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   (version 2) along with this program (see documentation directory); 
 *   otherwise, have a look at http://www.gnu.org or write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */
package art.output;

import art.servlets.ArtDBCP;

import art.utils.ArtQueryParam;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.SimpleDateFormat; //smileybits 20100212. format dates using simpledateformat class

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
    
    FileOutputStream ou;
    ZipOutputStream zipout;
    HSSFWorkbook wb;
    HSSFSheet sheet;
    HSSFRow r;
    HSSFCell c;
    HSSFCellStyle headerStyle;
    HSSFCellStyle bodyStyle;
    HSSFCellStyle dateStyle;
    HSSFFont fh;
    HSSFFont fb;
    int currentRow;
    String filename;
    String fullFileName;
    PrintWriter htmlout;
    String queryName;
    String userName;
    int maxRows;
    int columns;
    int cellNumber;
    String y_m_d;
    String h_m_s;
    Map<Integer, ArtQueryParam> displayParams;

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
        // Build filename		
        Date today = new Date();

        String dateFormat = "yyyy_MM_dd";
        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
        y_m_d = dateFormatter.format(today);

        String timeFormat = "HH_mm_ss";
        SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
        h_m_s = timeFormatter.format(today);

        filename = userName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ArtDBCP.getRandomString();
        filename = ArtDBCP.cleanFileName(filename); //replace characters that would make an invalid filename

        ZipEntry ze = new ZipEntry(filename + ".xls");

        try {
            fullFileName = s + filename + ".zip";
            ou = new FileOutputStream(fullFileName);
            zipout = new ZipOutputStream(ou);
            zipout.putNextEntry(ze);
            wb = new HSSFWorkbook();
            sheet = wb.createSheet();
            r = null;
            c = null;
            headerStyle = wb.createCellStyle();
            bodyStyle = wb.createCellStyle();
            fh = wb.createFont();
            fb = wb.createFont();

            currentRow = 0;

            fh.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            fh.setColor(org.apache.poi.hssf.util.HSSFColor.BLUE.index);
            fh.setFontHeightInPoints((short) 12);
            headerStyle.setFont(fh);
            headerStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);

            fb.setColor(HSSFFont.COLOR_NORMAL);
            fb.setFontHeightInPoints((short) 10);
            bodyStyle.setFont(fb);

            dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
            dateStyle.setFont(fb);

        } catch (IOException e) {
            logger.error("Error",e);
        }
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
    public void setUserName(String s) {
        userName = s;
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
        wb.setSheetName(0, queryName);

        newLine();
        addCellString(queryName + " - " + y_m_d.replace('_', '-') + " " + h_m_s.replace('_', ':'));

        // Output parameter values list
        if (displayParams != null && displayParams.size() > 0) {
            // rows with params names
            newLine();
            Iterator it = displayParams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ArtQueryParam param = (ArtQueryParam) entry.getValue();
                String paramName = param.getName();
                addHeaderCell(paramName);
            }
            // rows with params values
            newLine();
            it = displayParams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ArtQueryParam param = (ArtQueryParam) entry.getValue();
                Object pValue = param.getParamValue();

                if (pValue instanceof String) {
                    addCellString((String) pValue);
                } else if (pValue instanceof String[]) { // multi
                    // decode the parameters handling multi one
                    StringBuilder pValuesSb = new StringBuilder(256);
                    String[] pValues = (String[]) pValue;
                    for (int i = 0; i < pValues.length; i++) {
                        pValuesSb.append(pValues[i]);
                        pValuesSb.append(",");
                    }
                    addCellString(pValuesSb.toString());
                }
            }
        }
        // prepare row for columns header
        newLine();
    }

    @Override
    public void addHeaderCell(String s) {
        c = r.createCell(cellNumber++);
        c.setCellType(HSSFCell.CELL_TYPE_STRING);

        //upgraded from poi 2.5. setencoding deprecated and removed. POI now automatically handles Unicode without forcing the encoding
        //use undeprecated setcellvalue overload
        //c.setEncoding(HSSFCell.ENCODING_UTF_16); 
        //c.setCellValue(s);
        c.setCellValue(new HSSFRichTextString(s));

        c.setCellStyle(headerStyle);
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
        c = r.createCell(cellNumber++);
        c.setCellType(HSSFCell.CELL_TYPE_STRING);

        //upgraded from poi 2.5. setencoding deprecated and removed. POI now automatically handles Unicode without forcing the encoding
        //use undeprecated setcellvalue overload
        //c.setEncoding(HSSFCell.ENCODING_UTF_16); 
        //c.setCellValue(s);
        c.setCellValue(new HSSFRichTextString(s));

        c.setCellStyle(bodyStyle);
    }

    @Override
    public void addCellDouble(Double d) {
        c = r.createCell(cellNumber++);
        if (d != null) {
            c.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            c.setCellValue(d);
            c.setCellStyle(bodyStyle);
        }
    }

    @Override
    public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT		
        c = r.createCell(cellNumber++);
        if (i != null) {
            c.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            c.setCellValue(i);
            c.setCellStyle(bodyStyle);
        }
    }

    @Override
    public void addCellDate(Date d) {
        c = r.createCell(cellNumber++);
        if (d != null) {
            c.setCellValue(d);
            c.setCellStyle(dateStyle);
        }
    }

    @Override
    public boolean newLine() {
        r = sheet.createRow(currentRow++);
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
            ou.close();

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
            logger.error("Error",e);
        }
    }

    @Override
    public boolean isDefaultHtmlHeaderAndFooterEnabled() {
        return true;
    }
}
