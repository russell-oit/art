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

import art.enums.PageOrientation;
import art.reportparameter.ReportParameter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFldChar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STFldCharType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;

/**
 * Generates docx output
 *
 * @author Timothy Anyona
 */
public class DocxOutput extends StandardOutput {

	private XWPFDocument document;
	private XWPFTable table;
	private XWPFTableRow row;
	private XWPFTableCell cell;
	private int cellNumber;

	/**
	 * Resets global variables in readiness for output generation. Especially
	 * important for burst output where the same standard output object is
	 * reused for multiple output runs.
	 */
	private void resetVariables() {
		document = null;
		table = null;
		row = null;
		cell = null;
		cellNumber = 0;
	}

	@Override
	public void init() {
		resetVariables();

		document = new XWPFDocument();
		setPageSize();
		try {
			createPageNumbers();
		} catch (IOException | XmlException ex) {
			endOutput();
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Sets the document page size. A4 portrait or landscape
	 */
	private void setPageSize() {
		//https://stackoverflow.com/questions/20188953/how-to-set-page-orientation-for-word-document
		//https://stackoverflow.com/questions/26483837/landscape-and-portrait-pages-in-the-same-word-document-using-apache-poi-xwpf-in

		CTDocument1 doc = document.getDocument();
		CTBody body = doc.getBody();

		if (!body.isSetSectPr()) {
			body.addNewSectPr();
		}
		CTSectPr section = body.getSectPr();

		if (!section.isSetPgSz()) {
			section.addNewPgSz();
		}
		CTPageSz pageSize = section.getPgSz();

		PageOrientation pageOrientation = report.getPageOrientation();
		switch (pageOrientation) {
			case Portrait:
				pageSize.setOrient(STPageOrientation.PORTRAIT);
				pageSize.setW(BigInteger.valueOf(595 * 20));
				pageSize.setH(BigInteger.valueOf(842 * 20));
				break;
			case Landscape:
				pageSize.setOrient(STPageOrientation.LANDSCAPE);
				pageSize.setW(BigInteger.valueOf(842 * 20));
				pageSize.setH(BigInteger.valueOf(595 * 20));
				break;
			default:
				endOutput();
				throw new IllegalArgumentException("Unexpected page orientation: " + pageOrientation);
		}
	}

	@Override
	public void addTitle() {
		XWPFParagraph paragraph = document.createParagraph();
		paragraph.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun run = paragraph.createRun();
		run.setText(reportName);
		run.setFontSize(14);
		run.setBold(true);
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (CollectionUtils.isEmpty(reportParamsList)) {
			return;
		}

		XWPFParagraph paragraph = document.createParagraph();
		for (ReportParameter reportParam : reportParamsList) {
			try {
				XWPFRun run = paragraph.createRun();
				String labelAndDisplayValues = reportParam.getLocalizedLabelAndDisplayValues(locale);
				run.setText(labelAndDisplayValues);
				run.addCarriageReturn();
			} catch (IOException ex) {
				endOutput();
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public void beginRows() {
		cellNumber = 0;
	}

	@Override
	public void beginHeader() {
		int rows = 1;
		int cols = totalColumnCount;
		table = document.createTable(rows, cols);
		//autofit table width to page width
		//https://stackoverflow.com/questions/15149422/how-to-auto-fit-table-and-aligning-the-table-to-center-according-to-word-docume
		//not perfect. table extends too much to the right, even if you set explicit page margins
		table.getCTTbl().addNewTblPr().addNewTblW().setW(BigInteger.valueOf(10000));
		row = table.getRow(0);
		row.setRepeatHeader(true);
	}

	@Override
	public void addHeaderCell(String value) {
		cell = row.getCell(cellNumber++);
		XWPFParagraph paragraph = cell.getParagraphs().get(0);
		paragraph.setAlignment(ParagraphAlignment.CENTER);

		XWPFRun run = paragraph.createRun();
		run.setBold(true);
		run.setText(value);
	}

	@Override
	public void addHeaderCellAlignLeft(String value) {
		cell = row.getCell(cellNumber++);
		XWPFParagraph paragraph = cell.getParagraphs().get(0);
		paragraph.setAlignment(ParagraphAlignment.LEFT);

		XWPFRun run = paragraph.createRun();
		run.setBold(true);
		run.setText(value);
	}

	@Override
	public void addCellString(String value) {
		outputCellText(value);
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue = formatNumericValue(value);
		outputCellText(formattedValue);
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		outputCellText(formattedValue);
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = formatDateValue(value);
		outputCellText(formattedValue);
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		outputCellText(formattedValue);
	}

	@Override
	public void newRow() {
		row = table.createRow();
		cellNumber = 0;
	}

	@Override
	public void addCellTotal(Double value) {
		String formattedValue = formatNumericValue(value);

		cell = row.getCell(cellNumber++);
		XWPFParagraph paragraph = cell.getParagraphs().get(0);

		XWPFRun run = paragraph.createRun();
		run.setBold(true);
		run.setText(formattedValue);
	}

	@Override
	public void addCellTotal(Double totalValue, String formattedValue, String sortValue) {
		cell = row.getCell(cellNumber++);
		XWPFParagraph paragraph = cell.getParagraphs().get(0);

		XWPFRun run = paragraph.createRun();
		run.setBold(true);
		run.setText(formattedValue);
	}

	@Override
	public void endOutput() {
		try {
			try (OutputStream fout = new FileOutputStream(fullOutputFileName)) {
				document.write(fout);
			}

			//set open password
			String openPassword = report.getOpenPassword();
			if (StringUtils.isNotEmpty(openPassword)) {
				PoiUtils.addOpenPassword(openPassword, fullOutputFileName);
			}
		} catch (IOException | GeneralSecurityException | InvalidFormatException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Outputs the given text to the current cell of the current row of the
	 * table that contains the results
	 *
	 * @param value the text to output
	 */
	private void outputCellText(String value) {
		cell = row.getCell(cellNumber++);
		XWPFParagraph paragraph = cell.getParagraphs().get(0);

		XWPFRun run = paragraph.createRun();
		run.setText(value);
	}

	/**
	 * Creates a page footer for the document with page numbers
	 *
	 * @throws IOException
	 * @throws XmlException
	 */
	private void createPageNumbers() throws IOException, XmlException {
		//https://stackoverflow.com/questions/23870311/apache-poi-ms-word-how-to-insert-page-number
		// create footer
		XWPFHeaderFooterPolicy headerFooterPolicy = document.getHeaderFooterPolicy();
		if (headerFooterPolicy == null) {
			CTBody body = document.getDocument().getBody();
			CTSectPr sectPr = body.getSectPr();
			if (sectPr == null) {
				sectPr = body.addNewSectPr();
			}
			headerFooterPolicy = new XWPFHeaderFooterPolicy(document, sectPr);
		}

		CTP ctpFooter = CTP.Factory.newInstance();

		XWPFParagraph[] parsFooter;

		// add style (s.th.)
		CTPPr ctppr = ctpFooter.addNewPPr();
		CTString pst = ctppr.addNewPStyle();
		pst.setVal("style21");
		CTJc ctjc = ctppr.addNewJc();
		ctjc.setVal(STJc.RIGHT);
		ctppr.addNewRPr();

		// add everything from the footerXXX.xml you need
		CTR ctr = ctpFooter.addNewR();
		ctr.addNewRPr();
		CTFldChar fch = ctr.addNewFldChar();
		fch.setFldCharType(STFldCharType.BEGIN);

		ctr = ctpFooter.addNewR();
		ctr.addNewInstrText().setStringValue(" PAGE ");

		ctpFooter.addNewR().addNewFldChar().setFldCharType(STFldCharType.SEPARATE);

		ctpFooter.addNewR().addNewT().setStringValue("1");

		ctpFooter.addNewR().addNewFldChar().setFldCharType(STFldCharType.END);

		XWPFParagraph footerParagraph = new XWPFParagraph(ctpFooter, document);

		parsFooter = new XWPFParagraph[1];

		parsFooter[0] = footerParagraph;

		headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT, parsFooter);
	}
}
