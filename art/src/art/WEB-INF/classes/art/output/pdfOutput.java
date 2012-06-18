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
import com.lowagie.text.pdf.*;
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
    PdfPTable table;
    PdfPCell cell;
    PdfWriter pdfout;
    float pos;
    int counter;
    float evengray = 1.0f;
    float oddgray = 0.75f;
    float headergray = 0.5f;	
	FontSelector fsBody; //fonts to use for document body
	FontSelector fsHeading; //fonts to use for document heading/title

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

        filename = userName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ArtDBCP.getRandomString() + ".pdf";
        filename=ArtDBCP.cleanFileName(filename); //replace characters that would make an invalid filename

        try {
            // Create files
            fullFileName = s + filename;

            Rectangle pageSize;
            switch (Integer.parseInt(ArtDBCP.getArtSetting("page_size"))) {
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
			
			//use fontselector and fonts with specified encoding to enable display of more non-ascii characters
			fsBody=new FontSelector();	
			//default helvetica font
			Font font=new Font(Font.HELVETICA, 8, Font.NORMAL);
			fsBody.addFont(font);
			//helvetica with cp1252 (latin1) encoding - for western european languages
			BaseFont bf=BaseFont.createFont("Helvetica",BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			font=new Font(bf,8,Font.NORMAL);
			fsBody.addFont(font);
			//helvetica with cp1250 (latin2) encoding - for central and eastern european languages
			bf=BaseFont.createFont("Helvetica",BaseFont.CP1250, BaseFont.NOT_EMBEDDED);
			font=new Font(bf,8,Font.NORMAL);
			fsBody.addFont(font);
						
			//set fonts for document heading
			fsHeading=new FontSelector();
			font = new Font(Font.HELVETICA, 10, Font.BOLD);
			fsHeading.addFont(font);
			bf=BaseFont.createFont("Helvetica",BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			font=new Font(bf,10,Font.BOLD);
			fsBody.addFont(font);
			bf=BaseFont.createFont("Helvetica",BaseFont.CP1250, BaseFont.NOT_EMBEDDED);
			font=new Font(bf,10,Font.BOLD);
			fsBody.addFont(font);
															            			
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
        table.setTotalWidth(width - 72); //end result will have 72pt (1 inch) left and right margins
        table.setLockedWidth(true);
        table.setHeaderRows(1);

        try {
            Paragraph title = new Paragraph(fsHeading.process(queryName + " - " + y_m_d.replace('_', '-') + " " + h_m_s.replace('_', ':')));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Output parameter values list			
            if (displayParams != null && displayParams.size() > 0) {

                document.add(new Paragraph(fsBody.process("Parameters\n")));
                
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
				
				//show parameters in a numbered list
				List list = new List(true, 10);
				//set font to use for the list item numbers
				Font font=new Font(Font.HELVETICA, 8, Font.NORMAL);
				list.setListSymbol(new Chunk("1",font)); //item number will get font from chunk details. chunk string doesn't matter for numbered lists

				//add list items
                int size = params.length - 1;				
                for (int i = size; i >= 0; i--) {
					Phrase ph=fsBody.process(params[i]);
					ph.setLeading(12); //set spacing before the phrase
					ListItem listItem = new ListItem(ph); 					
                    list.add(listItem);
                }
				
                document.add(list);
            }
			
            document.add(new Paragraph(fsBody.process("\n")));

        } catch (DocumentException e) {
            logger.error("Error",e);
        }

    }

    @Override
    public void addHeaderCell(String s) {
        cell = new PdfPCell(new Paragraph(fsHeading.process(s)));
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
		cell = new PdfPCell(new Paragraph(fsBody.process(s)));
		
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(5f);
        cell.setGrayFill((oddline ? evengray : oddgray));
        table.addCell(cell);
    }

    @Override
    public void addCellDouble(Double d) {
        cell = new PdfPCell(new Paragraph(fsBody.process(d + "")));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPaddingLeft(5f);
        cell.setPaddingRight(5f);
        cell.setGrayFill((oddline ? evengray : oddgray));
        table.addCell(cell);
    }

    @Override
    public void addCellLong(Long l) {
        cell = new PdfPCell(new Paragraph(fsBody.process(l + "")));
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
        cell = new PdfPCell(new Paragraph(fsBody.process(i + "")));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPaddingLeft(10f);
        cell.setPaddingRight(10f);
        cell.setGrayFill((oddline ? evengray : oddgray));
        table.addCell(cell);
    }

    @Override
    public void addCellDate(java.util.Date d) {
        cell = new PdfPCell(new Paragraph(fsBody.process(d + "")));
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
            document.add(new Paragraph(fsBody.process("\n")));
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
