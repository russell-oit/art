/*
 * Pdf output using itext. 
 * Version 1.0
 * Marios Timotheou

see http://itextdocs.lowagie.com/examples/com/lowagie/examples/objects/tables/pdfptable/FragmentTable.java
 */
package art.output;

import art.servlets.ArtDBCP;
import art.utils.ArtQueryParam;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate pdf output
 * 
 * @author Marios Timotheou
 */
public class pdfOutput implements ArtOutputInterface {

    final static Logger logger = LoggerFactory.getLogger(pdfOutput.class);
    
    FileOutputStream ou;
    String filename;
    String fullFileName;
    PrintWriter htmlout;
    String queryName;
    String userName;
    int maxRows;
    int columns;
    String y_m_d;
    String h_m_s;
    boolean oddline = true;
    Map<Integer, ArtQueryParam> displayParams;
    //for pdf
    Document document;
    Font font6;
    Font font8;
    Font font10b;
    PdfPTable table;
    PdfPCell cell;
    PdfWriter pdfout;
    float pos;
    int counter;
    float evengray = 1.0f;
    float oddgray = 0.75f;
    float headergray = 0.5f;

    /**
     * Constructor
     */
    public pdfOutput() {
    }

    @Override
    public String getName() {
        return "Pdf (pdf)";
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
        String timeFormat = "HH_mm_ss";

        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
        y_m_d = dateFormatter.format(today);

        dateFormatter.applyPattern(timeFormat);
        h_m_s = dateFormatter.format(today);

        filename = userName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ".pdf";
        filename=ArtDBCP.cleanFileName(filename); //replace characters that would make an invalid filename

        try {
            // Create files
            fullFileName = s + filename;

            com.lowagie.text.Rectangle pageSize;
            switch (Integer.parseInt(ArtDBCP.getArtProps("page_size"))) {
                case 1:
                    pageSize = PageSize.A4;
                    break;
                case 2:
                    pageSize = PageSize.A4.rotate();
                    break;
                case 3:
                    pageSize = PageSize.LETTER;
                    break;
                case 4:
                    pageSize = PageSize.LETTER.rotate();
                    break;
                default:
                    pageSize = PageSize.A4;
            }
            document = new Document(pageSize);

            PdfWriter.getInstance(document, new FileOutputStream(fullFileName));
            document.addTitle(queryName);
            document.addAuthor("ART - http://art.sourceforge.net");
									
			font6 = new Font(Font.HELVETICA, 6, Font.NORMAL);
			font8 = new Font(Font.HELVETICA, 8, Font.NORMAL);
			font10b = new Font(Font.HELVETICA, 10, Font.BOLD);
						            			
            HeaderFooter footer = new HeaderFooter(new Phrase(""), true);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.setFooter(footer);

            document.open();

        } catch (DocumentException de) {
           logger.error("Error",de);
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
        float width = document.getPageSize().getWidth();

        table = null;
        cell = null;

        table = new PdfPTable(columns);
        table.getDefaultCell().setBorder(0);
        table.setHorizontalAlignment(0);
        table.setTotalWidth(width - 72);
        table.setLockedWidth(true);
        table.setHeaderRows(1);

        try {
            Paragraph title = new Paragraph(queryName + " - " + y_m_d.replace('_', '-') + " " + h_m_s.replace('_', ':'), font10b);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Output parameter values list			
            if (displayParams != null && displayParams.size() > 0) {

                document.add(new Paragraph("Parameters\n", font8));

                com.lowagie.text.List list = new com.lowagie.text.List(true, 10);

                String[] params = new String[displayParams.size()];
                int index = 0;
                Iterator it = displayParams.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    ArtQueryParam aqp = (ArtQueryParam) entry.getValue();
                    String paramName = aqp.getName();
                    String param = paramName + " : ";
                    Object pValue = aqp.getParamValue();

                    if (pValue instanceof String) {
                        param += pValue;
                    } else if (pValue instanceof String[]) { // multi
                        // decode the parameters handling multi one
                        String[] pValues = (String[]) pValue;
                        int size = pValues.length - 1;
                        for (int i = 0; i < size; i++) {
                            param += pValues[i] + ",";
                        }
                        param += pValues[size];
                    }

                    params[index] = param;
                    index++;
                }

                int size = params.length - 1;
                for (int i = size; i >= 0; i--) {
                    ListItem listItem = new ListItem(params[i], font8);
                    list.add(listItem);
                }

                document.add(list);
            }

            document.add(new Paragraph("\n", font8));

        } catch (DocumentException e) {
            logger.error("Error",e);
        }

    }

    @Override
    public void addHeaderCell(String s) {
        cell = new PdfPCell(new Paragraph(s, font10b));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(5f);
        cell.setGrayFill(headergray);
        table.addCell(cell);
    }

    @Override
    public void endHeader() {
        //nope;
    }

    @Override
    public void beginLines() {
    }

    @Override
    public void addCellString(String s) {
        cell = new PdfPCell(new Paragraph(s, font8));
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(5f);
        cell.setGrayFill((oddline ? evengray : oddgray));
        table.addCell(cell);
    }

    @Override
    public void addCellDouble(Double d) {
        cell = new PdfPCell(new Paragraph(d + "", font8));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(5f);
        cell.setGrayFill((oddline ? evengray : oddgray));
        table.addCell(cell);
    }

    @Override
    public void addCellLong(Long l) {
        cell = new PdfPCell(new Paragraph(l + "", font8));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(5f);
        cell.setGrayFill((oddline ? evengray : oddgray));
        table.addCell(cell);
    }

    /**
     * 
     * @param i
     */
    public void addCellInt(int i) {
        cell = new PdfPCell(new Paragraph(i + "", font8));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPaddingLeft(10f);
        cell.setPaddingRight(10f);
        cell.setGrayFill((oddline ? evengray : oddgray));
        table.addCell(cell);
    }

    @Override
    public void addCellDate(java.util.Date d) {
        cell = new PdfPCell(new Paragraph(d + "", font8));
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(5f);
        cell.setGrayFill((oddline ? evengray : oddgray));
        table.addCell(cell);
    }

    @Override
    public boolean newLine() {
        counter++;

        if (counter % 2 == 0) {
            oddline = true;
        } else {
            oddline = false;
        }

        // split table in smaller pieces in order to save memory:
        // fragment size should come from ArtDBCP servlet, from  web.xml or properties		
        if (counter % 500 == 500 - 1) {
            try {
                document.add(table);
                table.deleteBodyRows();
                table.setSkipFirstHeader(true);
            } catch (DocumentException e) {
                logger.error("Error",e);
            }
        }

        // Check rows number		
        if (counter < maxRows) {
            return true;
        } else {
            //htmlout not needed for scheduled jobs
            if (htmlout != null) {
                htmlout.println("<span style=\"color:red\">Too many rows (>"
                        + maxRows
                        + ")! Data not completed. Please narrow your search!</span>");
            }
            addCellString("Maximum number of rows exceeded! Query not completed.");

            return false;
        }
    }

    @Override
    public void endLines() {
        // flush and close files
        try {
            document.add(new Paragraph("\n", font8));
            document.add(table);
            document.close();

            //htmlout not needed for scheduled jobs
            if (htmlout != null) {
                htmlout.println("<p><div align=\"Center\"><table border=\"0\" width=\"90%\">");
                htmlout.println("<tr><td colspan=2 class=\"data\" align=\"center\" >"
                        + "<a  type=\"application/octet-stream\" href=\"../export/" + filename + "\" target=\"_blank\"> "
                        + filename + "</a>"
                        + "</td></tr>");
                htmlout.println("</table></div></p>");
            }        
        } catch (Exception e) {
           logger.error("Error",e);
        }
    }

    @Override
    public boolean isDefaultHtmlHeaderAndFooterEnabled() {
        return true;
    }
}
