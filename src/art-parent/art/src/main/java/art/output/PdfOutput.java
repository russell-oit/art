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
package art.output;

import art.reportparameter.ReportParameter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Generates pdf output. See
 * http://itextdocs.lowagie.com/examples/com/lowagie/examples/objects/tables/pdfptable/FragmentTable.java
 *
 * @author Marios Timotheou
 * @author Timothy Anyona
 */
public class PdfOutput extends StandardOutput {

	private Document document;
	private PdfPTable table;
	private PdfPCell cell;
	private final float headergray = 0.9f;
	private FontSelector fsBody; //fonts to use for document body
	private FontSelector fsHeading; //fonts to use for document title and column headings

	/**
	 * Resets global variables in readiness for output generation. Especially
	 * important for burst output where the same standard output object is
	 * reused for multiple output runs.
	 */
	private void resetVariables() {
		document = null;
		table = null;
		cell = null;
		fsBody = null;
		fsHeading = null;
	}

	@Override
	public void init() {
		try {
			resetVariables();

			PdfHelper pdfHelper = new PdfHelper();

			Rectangle pageSize = pdfHelper.getPageSize(report);

			//set document margins
			//document with 72pt (1 inch) margins for left, right, top, bottom
			final float LEFT_MARGIN = 72f;
			final float RIGHT_MARGIN = 72f;
			final float TOP_MARGIN = 72f;
			final float BOTTOM_MARGIN = 72f;
			document = new Document(pageSize, LEFT_MARGIN, RIGHT_MARGIN, TOP_MARGIN, BOTTOM_MARGIN);

			PdfWriter.getInstance(document, new FileOutputStream(fullOutputFileName));
			document.addTitle(reportName);
			document.addAuthor(pdfHelper.PDF_AUTHOR_ART);

			if (pdfPageNumbers) {
				pdfHelper.addPageNumbers(document);
			}

			document.open();

			table = new PdfPTable(totalColumnCount); //use total column count insead of resultset column count because of crosstab output
			table.getDefaultCell().setBorder(0);
			table.setWidthPercentage(100); //default is 80
			table.setHeaderRows(1);

			fsBody = new FontSelector();
			fsHeading = new FontSelector();
			pdfHelper.setFontSelectors(fsBody, fsHeading);
		} catch (DocumentException | IOException ex) {
			endOutput();
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
			endOutput();
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addSelectedParameters(java.util.List<ReportParameter> reportParamsList) {
		outputSelectedParameters(document, fsBody, reportParamsList);
	}

	/**
	 * Outputs parameters used for the report
	 *
	 * @param doc the itext document object
	 * @param fs the fontselector to be used
	 * @param reportParamsList the report parameters
	 */
	public void outputSelectedParameters(Document doc, FontSelector fs, java.util.List<ReportParameter> reportParamsList) {
		if (CollectionUtils.isEmpty(reportParamsList)) {
			return;
		}

		try {
			//show parameters in a numbered list
			com.lowagie.text.List list = new List(true, 10);
			//set font to use for the list item numbers
			Font font = new Font(Font.HELVETICA, 8, Font.NORMAL);
			list.setListSymbol(new Chunk("1", font)); //item number will get font from chunk details. chunk string doesn't matter for numbered lists

			//add list items
			for (ReportParameter reportParam : reportParamsList) {
				try {
					String labelAndDisplayValues = reportParam.getLocalizedLabelAndDisplayValues(locale);
					Phrase ph = fs.process(labelAndDisplayValues);
					ph.setLeading(12); //set spacing before the phrase
					ListItem listItem = new ListItem(ph);
					list.add(listItem);
				} catch (IOException ex) {
					endOutput();
					throw new RuntimeException(ex);
				}
			}

			doc.add(list);

			addNewline(doc, fs);
		} catch (DocumentException ex) {
			endOutput();
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Adds a new line
	 *
	 * @param doc the document to use
	 * @param fs the fontselector to use
	 * @throws DocumentException
	 */
	private void addNewline(Document doc, FontSelector fs) throws DocumentException {
		doc.add(new Paragraph(fs.process("\n")));
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
		String formattedValue = formatNumericValue(value);

		cell = new PdfPCell(new Paragraph(fsBody.process(formattedValue)));

		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
//		cell.setGrayFill((oddline ? evengray : oddgray));
		table.addCell(cell);
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		cell = new PdfPCell(new Paragraph(fsBody.process(formattedValue)));

		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
//		cell.setGrayFill((oddline ? evengray : oddgray));
		table.addCell(cell);
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = formatDateValue(value);

		cell = new PdfPCell(new Paragraph(fsBody.process(formattedValue)));
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);
//		cell.setGrayFill((oddline ? evengray : oddgray));
		table.addCell(cell);
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
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
				endOutput();
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public void addCellTotal(Double value) {
		String formattedValue = formatNumericValue(value);

		cell = new PdfPCell(new Paragraph(fsHeading.process(formattedValue)));

		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);

		table.addCell(cell);
	}

	@Override
	public void addCellTotal(Double totalValue, String formattedValue, String sortValue) {
		cell = new PdfPCell(new Paragraph(fsHeading.process(formattedValue)));

		cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cell.setPaddingLeft(5f);
		cell.setPaddingRight(5f);

		table.addCell(cell);
	}

	@Override
	public void endOutput() {
		// flush and close files
		try {
			if (document != null) {
				if (fsBody != null) {
					addNewline(document, fsBody);
				}
				if (table != null) {
					document.add(table);
				}
				document.close();
			}
		} catch (DocumentException ex) {
			throw new RuntimeException(ex);
		}
	}

}
