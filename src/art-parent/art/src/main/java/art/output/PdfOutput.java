/*
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

import art.enums.PdfPageSize;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate pdf output. See
 * http://itextdocs.lowagie.com/examples/com/lowagie/examples/objects/tables/pdfptable/FragmentTable.java
 *
 * @author Marios Timotheou
 * @author Timothy Anyona
 */
public class PdfOutput extends StandardOutput {

	private static final Logger logger = LoggerFactory.getLogger(PdfOutput.class);
	private Document document;
	private PdfPTable table;
	private PdfPCell cell;
	private float headergray = 0.9F;
	private FontSelector fsBody; //fonts to use for document body
	private FontSelector fsHeading; //fonts to use for document title and column headings
	public final String PDF_AUTHOR_ART = "ART - http://art.sourceforge.net";

	/**
	 * Initialise objects required to generate output
	 */
	@Override
	public void init() {
		logger.debug("Entering init");
		
		try {
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

			//set document margins
			//document with 72pt (1 inch) margins for left, right, top, bottom
			document = new Document(pageSize, 72, 72, 72, 72);

			PdfWriter.getInstance(document, new FileOutputStream(fullOutputFilename));
			document.addTitle(reportName);
			document.addAuthor(PDF_AUTHOR_ART);

			HeaderFooter footer = new HeaderFooter(new Phrase(""), true);
			footer.setAlignment(Element.ALIGN_CENTER);
			document.setFooter(footer);

			document.open();

			table = new PdfPTable(resultSetColumnCount);
			table.getDefaultCell().setBorder(0);
			table.setWidthPercentage(100F); //default is 80
			table.setHeaderRows(1);

			fsBody = new FontSelector();
			fsHeading = new FontSelector();
			setFontSelectors(fsBody, fsHeading);
		} catch (DocumentException | IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addTitle() throws RuntimeException {
		Paragraph title = new Paragraph(fsHeading.process(reportName));
		title.setAlignment(Element.ALIGN_CENTER);

		try {
			document.add(title);
		} catch (DocumentException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addSelectedParameters(java.util.List<ReportParameter> reportParamsList) {
		outputSelectedParameters(document, fsBody, reportParamsList);
	}

	public void outputSelectedParameters(Document doc, FontSelector fs, java.util.List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		try {
			//TODO don't use numbered list
			//show parameters in a numbered list
			com.lowagie.text.List list = new List(true, 10);
			//set font to use for the list item numbers
			Font font = new Font(Font.HELVETICA, 8, Font.NORMAL);
			list.setListSymbol(new Chunk("1", font)); //item number will get font from chunk details. chunk string doesn't matter for numbered lists

			//add list items
			for (ReportParameter reportParam : reportParamsList) {
				Phrase ph = fs.process(reportParam.getNameAndDisplayValues());
				ph.setLeading(12); //set spacing before the phrase
				ListItem listItem = new ListItem(ph);
				list.add(listItem);
			}

			doc.add(list);

			addNewline(doc, fs);
		} catch (DocumentException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void addNewline(Document doc, FontSelector fs) throws DocumentException {
		doc.add(new Paragraph(fs.process("\n")));

		//TODO test if font matters or if new line needed or can just have single space (empty space doesn't work?)
//		//or
//		document.add(Chunk.NEWLINE);
//		//or
//		document.add(new Paragraph(" "));
	}

	@Override
	public void addHeaderCell(String value) {
		String finalValue;

		if (value == null) {
			finalValue = ""; //fs.process() will throw NPE if value is null
		} else {
			finalValue = value;
		}

		cell = new PdfPCell(new Paragraph(fsHeading.process(finalValue)));

		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
		cell.setGrayFill(headergray);

		table.addCell(cell);
	}

	@Override
	public void addCellString(String value) {
		String finalValue;

		if (value == null) {
			finalValue = ""; //fs.process() will throw NPE if value is null
		} else {
			finalValue = value;
		}

		cell = new PdfPCell(new Paragraph(fsBody.process(finalValue)));

		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);

		table.addCell(cell);
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue;

		//TODO review abstract methods for numerics and dates
		//add method to take formatted string that can just
		//be displayed instead of every output class having formatting logic
		if (value == null) {
			formattedValue = "0";
		} else {
			formattedValue = actualNumberFormatter.format(value);
		}

		cell = new PdfPCell(new Paragraph(fsBody.process(formattedValue)));

		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
//		cell.setGrayFill((oddline ? evengray : oddgray));
		table.addCell(cell);
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = Config.getDateDisplayString(value);
		}

		cell = new PdfPCell(new Paragraph(fsBody.process(formattedValue)));
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
//		cell.setGrayFill((oddline ? evengray : oddgray));
		table.addCell(cell);
	}

	@Override
	public void newRow() {

		// split table in smaller pieces in order to save memory:
		// fragment size should come from Config servlet, web.xml or properties		
		if (rowCount % 500 == 500 - 1) {
			try {
				document.add(table);
				table.deleteBodyRows();
				table.setSkipFirstHeader(true);
			} catch (DocumentException ex) {
				throw new RuntimeException(ex);
			}
		}

	}

	@Override
	public void endRows() {
		// flush and close files
		try {
			addNewline(document, fsBody);
			document.add(table);
			document.close();
		} catch (DocumentException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Set font selector objects to be used for body text and header text Also
	 * used by pdfgraph class
	 *
	 * @param body
	 * @param header
	 */
	public void setFontSelectors(FontSelector body, FontSelector header) {
		//use fontselector and potentially custom fonts with specified encoding
		//to enable display of more non-ascii characters
		//first font added to selector wins

		//use custom font if defined			
		if (Config.isUseCustomPdfFont()) {
			String fontName = Config.getSettings().getPdfFontName();
			String encoding = Config.getSettings().getPdfFontEncoding();
			boolean embedded = Config.getSettings().isPdfFontEmbedded();

			Font bodyFont = FontFactory.getFont(fontName, encoding, embedded);
			bodyFont.setSize(8);
			bodyFont.setStyle(Font.NORMAL);
			body.addFont(bodyFont);

			Font headingFont = FontFactory.getFont(fontName, encoding, embedded);
			headingFont.setSize(10);
			headingFont.setStyle(Font.BOLD);
			header.addFont(headingFont);
		}

		//add default font after custom font			
		body.addFont(FontFactory.getFont(BaseFont.HELVETICA, 8, Font.NORMAL));
		header.addFont(FontFactory.getFont(BaseFont.HELVETICA, 10, Font.BOLD));
	}

}
