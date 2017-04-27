/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package art.chart;

import art.enums.PdfPageSize;
import art.output.PdfHelper;
import art.output.PdfOutput;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.DefaultFontMapper.BaseFontParameters;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Objects;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods to save a jfree chart to a pdf file
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class PdfChart {

	private static final Logger logger = LoggerFactory.getLogger(PdfChart.class);

	/**
	 * Saves the chart to a pdf file
	 *
	 * @param chart the chart object
	 * @param filename the full file path to use
	 * @param title the chart title, or null, or blank
	 * @param data the data to be displayed with the chart image, or null
	 * @param reportParamsList the report parameters to be displayed, or null or
	 * empty
	 */
	public static void generatePdf(JFreeChart chart, String filename, String title,
			RowSetDynaClass data, java.util.List<ReportParameter> reportParamsList) {

		logger.debug("Entering createPdf: filename='{}', title='{}'", filename, title);

		Objects.requireNonNull(chart, "chart must not be null");
		Objects.requireNonNull(filename, "filename must not be null");

		PdfHelper pdfHelper = new PdfHelper();

		Rectangle pageSize;
		PdfPageSize pageSizeSetting = Config.getSettings().getPdfPageSize();

		switch (pageSizeSetting) {
			case A4:
				pageSize = PageSize.A4;
				break;
			case A4Landscape:
				pageSize = PageSize.A4.rotate();
				break;
			case Letter:
				pageSize = PageSize.LETTER;
				break;
			case LetterLandscape:
				pageSize = PageSize.LETTER.rotate();
				break;
			default:
				throw new IllegalArgumentException("Unexpected pdf page size setting: " + pageSizeSetting);
		}

		final float LEFT_MARGIN = 72f;
		final float RIGHT_MARGIN = 72f;
		final float TOP_MARGIN = 36f;
		final float BOTTOM_MARGIN = 36f;
		Document document = new Document(pageSize, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);

		try {
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
			writer.setStrictImageSequence(true); //ensure image order is maintained
			if (title == null) {
				title = ""; //null title will cause an exception
			}
			document.addTitle(title);
			document.addAuthor(pdfHelper.PDF_AUTHOR_ART);
			document.open();

			//set fonts to be used, in case custom font is defined
			FontSelector fsBody = new FontSelector();
			FontSelector fsHeading = new FontSelector();
			pdfHelper.setFontSelectors(fsBody, fsHeading);

			//output parameters if any
			PdfOutput pdfOutput = new PdfOutput();
			pdfOutput.outputSelectedParameters(document, fsBody, reportParamsList);

			//create chart in pdf						
			DefaultFontMapper mapper = new DefaultFontMapper();

			//enable use of custom font so as to display non-ascii characters			
			if (Config.isUseCustomPdfFont()) {
				//enable custom chart font to be used in pdf output
				BaseFontParameters fp;
				String pdfFontName = Config.getSettings().getPdfFontName();
				String pdfFontDirectory = Config.getSettings().getPdfFontDirectory();
				String pdfFontFile = Config.getSettings().getPdfFontFile();
				String pdfFontEncoding = Config.getSettings().getPdfFontEncoding();
				boolean pdfFontEmbedded = Config.getSettings().isPdfFontEmbedded();
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

			//display chart data below chart if so required
			if (data != null) {
				Paragraph p = new Paragraph(fsBody.process("Data\n"));
				p.setAlignment(Element.ALIGN_CENTER);
				document.add(p);

				java.util.List<DynaBean> rows = data.getRows();
				DynaProperty[] dynaProperties = null;
				String columnName;
				String columnValue;
				int columns = 0; //column count

				PdfPTable table = null;
				PdfPCell cell;
				float headergray = 0.5f;

				for (int i = 0; i < rows.size(); i++) {
					DynaBean row = rows.get(i);
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

				//add chart data to document
				document.add(new Paragraph(fsBody.process("\n")));
				document.add(table);
			}
		} catch (FileNotFoundException | DocumentException ex) {
			logger.error("Error", ex);
		}

		document.close();
	}
}
