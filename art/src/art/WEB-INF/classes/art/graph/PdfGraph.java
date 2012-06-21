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

import art.output.pdfOutput;
import art.servlets.ArtDBCP;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.DefaultFontMapper.BaseFontParameters;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
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

	public static void createPdf(Object chartObject, String filename, String title) {
		createPdf(chartObject, filename, title, null);
	}

	/**
	 * Save chart to pdf
	 *
	 * @param chart chart object
	 * @param filename full file name to use to save the chart
	 * @param title chart title
	 */
	public static void createPdf(Object chartObject, String filename, String title, RowSetDynaClass rsdc) {

		ChartTheme currentChartTheme = null;
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
		Document document = new Document(pageSize, 72, 72, 36, 36); //document with 72pt (1 inch) margins for left, right, top, bottom
		//  float width = document.getPageSize().width();
		//  float height = document.getPageSize().height();
		//W= 595.0 H= 842.0

		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
			document.addTitle(title);
			document.addAuthor("Created by ART - http://art.sourceforge.net");
			SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
			HeaderFooter footer = new HeaderFooter(new Phrase("ART pdf output (" + df.format(new java.util.Date()) + ")", FontFactory.getFont(FontFactory.HELVETICA, 8)), false);
			footer.setAlignment(Element.ALIGN_CENTER);
			document.setFooter(footer);
			document.open();

			//create chart in pdf
			float chartWidth = 500f;
			float chartHeight = 400f;

			//enable use of custom fonts so as to display more non-ascii characters
			DefaultFontMapper mapper = new DefaultFontMapper();

//			mapper.insertDirectory("c:/windows/fonts");			
//			BaseFontParameters fp=mapper.getBaseFontParameters("Tahoma");
//			if(fp!=null){
//				System.out.println("font mapper found");
//				fp.encoding=BaseFont.CP1250;
//				fp.embedded=false;
//			}

			//			BaseFontParameters fontParameters=new BaseFontParameters("c:/windows/fonts/arialuni.ttf");			
//			fontParameters.encoding=BaseFont.CP1250;
			//			mapper.putName("Arial Unicode MS", fontParameters);



			JFreeChart chart = (JFreeChart) chartObject;
//			System.out.println(ch.getLegend().getItemFont().getName());
//			System.out.println(ch.getLegend().getItemFont().getSize());
//			ch.getLegend().setItemFont(new java.awt.Font("Tahoma", 0, 12));

//			//change chart them to allow use of different font in chart elements. to enable display of non-ascii characters
//			currentChartTheme = ChartFactory.getChartTheme();
//			//final StandardChartTheme chartTheme = (StandardChartTheme)org.jfree.chart.StandardChartTheme.createJFreeTheme();
//			StandardChartTheme chartTheme= (StandardChartTheme) StandardChartTheme.createLegacyTheme();
//			final java.awt.Font oldExtraLargeFont = chartTheme.getExtraLargeFont();
//            final java.awt.Font oldLargeFont = chartTheme.getLargeFont();
//            final java.awt.Font oldRegularFont = chartTheme.getRegularFont();
//            
//            final java.awt.Font extraLargeFont = new java.awt.Font("Tahoma", oldExtraLargeFont.getStyle(), oldExtraLargeFont.getSize());
//            final java.awt.Font largeFont = new java.awt.Font("Tahoma", oldLargeFont.getStyle(), oldLargeFont.getSize());
//            final java.awt.Font regularFont = new java.awt.Font("Tahoma", oldRegularFont.getStyle(), oldRegularFont.getSize());
//            
//            chartTheme.setExtraLargeFont(extraLargeFont);
//            chartTheme.setLargeFont(largeFont);
//            chartTheme.setRegularFont(regularFont);
//            			
//			chartTheme.apply(chart);

			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(chartWidth, chartHeight + 100);
			Graphics2D chartGraphics = tp.createGraphics(chartWidth, chartWidth, mapper);
			Rectangle2D chartRegion = new Rectangle2D.Float(0, 0, chartWidth, chartHeight);
			chart.draw(chartGraphics, chartRegion);
			chartGraphics.dispose();

			//place chart in pdf as image element instead of using addTemplate. so that positioning is as per document flow instead of absolute
			//cb.addTemplate(tp, 47, 175); 						
			Image chartImage = Image.getInstance(tp);
			chartImage.setAlignment(Image.ALIGN_CENTER);
			//chartImage.scaleToFit(chartWidth, chartHeight);
			document.add(chartImage);

			//display chart data below graph if so required
			if (rsdc != null) {
				java.util.List rows = rsdc.getRows();
				DynaProperty[] dynaProperties = null;
				String columnName;
				String columnValue;
				
				PdfPTable table = null;
				PdfPCell cell;
				float headergray = 0.5f;

				FontSelector fsBody = new FontSelector();
				FontSelector fsHeading = new FontSelector();
				pdfOutput pdfo = new pdfOutput();
				pdfo.setFontSelectors(fsBody, fsHeading);

				for (int i = 0; i < rows.size(); i++) {
					DynaBean row = (DynaBean) rows.get(i);
					if (i == 0) {
						//output column headings
						dynaProperties = row.getDynaClass().getDynaProperties();
						int columns = dynaProperties.length;
						table = new PdfPTable(columns);
						table.getDefaultCell().setBorder(0);						
						table.setHeaderRows(1);
						for (int j = 0; j < dynaProperties.length; j++) {
							columnName = dynaProperties[j].getName();
							cell = new PdfPCell(new Paragraph(fsHeading.process(columnName)));
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							cell.setPaddingLeft(5f);
							cell.setPaddingRight(5f);
							cell.setGrayFill(headergray);
							table.addCell(cell);
						}
					}

					//output data values
					for (int k = 0; k < dynaProperties.length; k++) {
						columnName = dynaProperties[k].getName();
						columnValue = String.valueOf(row.get(columnName));
						cell = new PdfPCell(new Paragraph(fsBody.process(columnValue)));
						cell.setPaddingLeft(5f);
						cell.setPaddingRight(5f);
						table.addCell(cell);
					}
				}

				//add graph data to document
				document.add(new Paragraph(fsBody.process("\n")));
				document.add(table);
			}
		} catch (Exception e) {
			logger.error("Error", e);
		}
		document.close();

//		//restore chart theme
//		if (currentChartTheme != null) {
//			ChartFactory.setChartTheme(currentChartTheme);
//		}
	}
}
