/*
 * Copyright (C)   Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the LGPL License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. *  
 */
package art.graph;

import art.servlets.ArtDBCP;
import com.lowagie.text.*;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save a chart to a pdf file
 * 
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class PdfGraph {
    
    final static Logger logger = LoggerFactory.getLogger(PdfGraph.class);
      
    /**
     * Save chart to pdf
     * 
     * @param chart chart object
     * @param filename full file name to use to save the chart
     * @param title chart title     
     */
    public static void createPdf(Object chart, String filename, String title) {

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
        Document document = new Document(pageSize, 72, 72, 72, 72);
        //  float width = document.getPageSize().width();
        //  float height = document.getPageSize().height();
        //W= 595.0 H= 842.0

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.addTitle(title);
            document.addAuthor("Created by ART - http://art.sourceforge.net");
            HeaderFooter footer = new HeaderFooter(new Phrase("ART pdf output (" + new java.util.Date().toString() + ")", FontFactory.getFont(FontFactory.HELVETICA, 8)), false);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.setFooter(footer);
            document.open();
            Paragraph paragraph = new Paragraph();
            paragraph.setAlignment(Element.ALIGN_CENTER);
                        
            paragraph.add(new Phrase(title, FontFactory.getFont(FontFactory.HELVETICA, 12)));
            document.add(paragraph);

            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(520, 420);
            Graphics2D g2 = tp.createGraphics(520, 420, new DefaultFontMapper());
            Rectangle2D r2D = new Rectangle2D.Double(20, 20, 500, 400);
            ((JFreeChart) chart).draw(g2, r2D);
            g2.dispose();
            cb.addTemplate(tp, 47, 175);                      
        } catch (Exception e) {
            logger.error("Error",e);
        }
        document.close();
    }
}
