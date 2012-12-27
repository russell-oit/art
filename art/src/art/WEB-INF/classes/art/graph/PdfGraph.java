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
import art.utils.ArtQueryParam;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.DefaultFontMapper.BaseFontParameters;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang.StringUtils;
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

	public static void createPdf(Object chartObject, String filename, String title) {
		createPdf(chartObject, filename, title, null, null);
	}

	public static void createPdf(Object chartObject, String filename, String title, RowSetDynaClass graphData) {
		createPdf(chartObject, filename, title, graphData, null);
	}

	/**
	 * Save chart to pdf
	 *
	 * @param chart chart object
	 * @param filename full file name to use to save the chart
	 * @param title chart title
	 */
	public static void createPdf(Object chartObject, String filename, String title,
			RowSetDynaClass graphData, Map<Integer, ArtQueryParam> displayParams) {

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
			writer.setStrictImageSequence(true); //ensure image order is maintained
			document.addTitle(title);
			document.addAuthor("Created by ART - http://art.sourceforge.net");
			SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy  HH:mm:ss");
			HeaderFooter footer = new HeaderFooter(new Phrase("ART pdf output (" + df.format(new java.util.Date()) + ")", FontFactory.getFont(FontFactory.HELVETICA, 8)), false);
			footer.setAlignment(Element.ALIGN_CENTER);
			document.setFooter(footer);
			document.open();

			//set fonts to be used, incase custom font is defined
			FontSelector fsBody = new FontSelector();
			FontSelector fsHeading = new FontSelector();
			pdfOutput pdfo = new pdfOutput();
			pdfo.setFontSelectors(fsBody, fsHeading);
			
			//output parameters if any
			pdfo.outputParameters(document, fsBody, displayParams);

			//create chart in pdf						
			JFreeChart chart = (JFreeChart) chartObject;
			DefaultFontMapper mapper = new DefaultFontMapper();

			//enable use of custom font so as to display non-ascii characters			
			if (ArtDBCP.isUseCustomPdfFont()) {
				//enable custom chart font to be used in pdf output
				BaseFontParameters fp;
				String pdfFontName = ArtDBCP.getArtSetting("pdf_font_name");
				String pdfFontDirectory = ArtDBCP.getArtSetting("pdf_font_directory");
				String pdfFontFile = ArtDBCP.getArtSetting("pdf_font_file");
				String pdfFontEncoding = ArtDBCP.getArtSetting("pdf_font_encoding");
				boolean pdfFontEmbedded = ArtDBCP.isPdfFontEmbedded();
				if (StringUtils.isNotBlank(pdfFontDirectory)) {
					mapper.insertDirectory(pdfFontDirectory);
					fp = mapper.getBaseFontParameters(pdfFontName);
					if (fp != null) {
						fp.encoding = pdfFontEncoding;
						fp.embedded = pdfFontEmbedded;
					}
				} else if (StringUtils.isNotBlank(pdfFontFile)) {
					fp = new BaseFontParameters(pdfFontFile);
					fp.encoding = pdfFontEncoding;
					fp.embedded = pdfFontEmbedded;
					mapper.putName(pdfFontName, fp);
				}
			}

			//add chart to document
			int chartWidth = 500;
			int chartHeight = 400;
			int chartHeightBuffer = 100; //to avoid chart being cut off at the top. not sure why. because of margin?

			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(chartWidth, chartHeight + chartHeightBuffer);
			Graphics2D chartGraphics = tp.createGraphics(chartWidth, chartWidth, mapper);
			Rectangle2D chartRegion = new Rectangle2D.Double(0, 0, chartWidth, chartHeight);
			chart.draw(chartGraphics, chartRegion);
			chartGraphics.dispose();

			//place chart in pdf as image element. so that positioning is as per document flow instead of using absolute positioning									
			Image chartImage = Image.getInstance(tp);
			chartImage.setAlignment(Image.ALIGN_CENTER);
			document.add(chartImage);

			//display chart data below graph if so required
			if (graphData != null) {
				Paragraph p=new Paragraph(fsBody.process("Data\n"));
				p.setAlignment(Element.ALIGN_CENTER);				
				document.add(p);
				
				java.util.List rows = graphData.getRows();
				DynaProperty[] dynaProperties = null;
				String columnName;
				String columnValue;
				int columns = 0; //column count

				PdfPTable table = null;
				PdfPCell cell;
				float headergray = 0.5f;

				for (int i = 0; i < rows.size(); i++) {
					DynaBean row = (DynaBean) rows.get(i);
					if (i == 0) {
						//output column headings
						dynaProperties = row.getDynaClass().getDynaProperties();
						columns = dynaProperties.length;
						table = new PdfPTable(columns);
						table.getDefaultCell().setBorder(0);
						table.setHeaderRows(1);
						for (int j = 0; j < columns; j++) {
							columnName = dynaProperties[j].getName();
							cell = new PdfPCell(new Paragraph(fsHeading.process(columnName + "")));
							cell.setHorizontalAlignment(Element.ALIGN_CENTER);
							cell.setPaddingLeft(5f);
							cell.setPaddingRight(5f);
							cell.setGrayFill(headergray);
							table.addCell(cell);
						}
					}

					//output data values
					for (int k = 0; k < columns; k++) {
						columnName = dynaProperties[k].getName();
						columnValue = String.valueOf(row.get(columnName));
						cell = new PdfPCell(new Paragraph(fsBody.process(columnValue + ""))); //add empty string to prevent NPE if value is null
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

	}
}
