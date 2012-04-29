/*
Generate xlsx output
Using code based on BigGridDemo (http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/xssf/usermodel/examples/BigGridDemo.java)
in order to enable generation of large xlsx files with limited memory
 */
package art.output;

import art.servlets.ArtDBCP;

import art.utils.ArtQueryParam;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.text.SimpleDateFormat;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
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
    Font fh;
    Font fb;
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
    String templateFileName;
    String xmlFileName;
    Map<String, CellStyle> styles;
    String sheetRef; //name of the zip entry holding sheet data, e.g. /xl/worksheets/sheet1.xml
    SpreadsheetWriter sw; //class that outputs temporary xlsx file
    Writer fw; //writer for temporary xml file
    File xmlFile; //file object for temporary xml file
    boolean errorOccurred = false; //flag that is set if an error occurs while creating the data file. so that processing can be stopped

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
        // Build filename		
        Date today = new Date();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd");
        y_m_d = dateFormatter.format(today);

        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH_mm_ss");
        h_m_s = timeFormatter.format(today);

        String baseName = userName + "-" + queryName + "-" + y_m_d + "-" + h_m_s;

        filename = baseName + ".xlsx";
        filename = ArtDBCP.cleanFileName(filename); //replace characters that would make an invalid filename
        fullFileName = s + filename;
        templateFileName = s + "template-" + filename;
        xmlFileName = s + "xml-" + baseName + ".xml";

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
            fh = wb.createFont();
            fh.setBoldweight(Font.BOLDWEIGHT_BOLD);
            fh.setColor(IndexedColors.BLUE.getIndex());
            fh.setFontHeightInPoints((short) 12);
            headerStyle.setFont(fh);
            headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
            styles.put("header", headerStyle);

            bodyStyle = wb.createCellStyle();
            fb = wb.createFont();
            fb.setColor(Font.COLOR_NORMAL);
            fb.setFontHeightInPoints((short) 10);
            bodyStyle.setFont(fb);
            styles.put("body", bodyStyle);

            dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
            dateStyle.setFont(fb);
            styles.put("date", dateStyle);

            //save the template			
            FileOutputStream os = new FileOutputStream(templateFileName);
            wb.write(os);
            os.close();

            //create xml file
            xmlFile = new File(xmlFileName);
            xmlFile.createNewFile();
            fw = new FileWriter(xmlFile);
            sw = new SpreadsheetWriter(fw);

        } catch (Exception e) {
            logger.error("Error", e);
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

        try {
            sw.beginSheet();

            currentRow = 0;

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
                sw.createCell(cellNumber++, d, styles.get("date").getIndex());
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
            substitute(templateFile, xmlFile, sheetRef.substring(1), out);
            out.close();

            //delete template and xml file			
            xmlFile.delete();
            templateFile.delete();

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
    public boolean isDefaultHtmlHeaderAndFooterEnabled() {
        return true;
    }

    /**
     * substitute temporary xlsx template file with data from temporary xml file containing final output
     *
     * @param zipfile the template file
     * @param tmpfile the XML file with the sheet data
     * @param entry the name of the sheet entry to substitute, e.g. xl/worksheets/sheet1.xml
     * @param out the stream to write the result to
     */
    private void substitute(File zipfile, File tmpfile, String entry, OutputStream out) throws IOException {
        ZipFile zip = new ZipFile(zipfile);

        ZipOutputStream zos = new ZipOutputStream(out);

        @SuppressWarnings("unchecked")
        Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zip.entries();
        while (en.hasMoreElements()) {
            ZipEntry ze = en.nextElement();
            if (!ze.getName().equals(entry)) {
                zos.putNextEntry(new ZipEntry(ze.getName()));
                InputStream is = zip.getInputStream(ze);
                copyStream(is, zos);
                is.close();
            }
        }
        zos.putNextEntry(new ZipEntry(entry));
        InputStream is = new FileInputStream(tmpfile);
        copyStream(is, zos);
        is.close();

        zos.close();
        zip.close();
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] chunk = new byte[1024];
        int count;
        while ((count = in.read(chunk)) >= 0) {
            out.write(chunk, 0, count);
        }
    }
}
