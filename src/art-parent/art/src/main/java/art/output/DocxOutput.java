/*
 * Copyright (C) 2016 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART. If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.reportparameter.ReportParameter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFAbstractNum;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.slf4j.LoggerFactory;

/**
 * Generates docx output
 *
 * @author Timothy Anyona
 */
public class DocxOutput extends StandardOutput {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DocxOutput.class);

	XWPFDocument document;
	XWPFTable table;
	XWPFTableRow row;
	XWPFTableCell cell;
	private int cellNumber;

	@Override
	public void init() {
		document = new XWPFDocument();

//		//set page margins
//		//needs full ooxml-schemas.jar e.g. 1.1
//		//https://stackoverflow.com/questions/28617157/set-margins-with-apache-poi
//		CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
//		CTPageMar pageMar = sectPr.addNewPgMar();
//		long oneInchMargin = 1440L;
//		pageMar.setLeft(BigInteger.valueOf(oneInchMargin));
//		pageMar.setTop(BigInteger.valueOf(oneInchMargin));
//		pageMar.setRight(BigInteger.valueOf(oneInchMargin));
//		pageMar.setBottom(BigInteger.valueOf(oneInchMargin));
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
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		char symbol = '=';
		BigInteger styleNumId;
		try {
			styleNumId = addListStyle(document, symbol);
		} catch (XmlException ex) {
			logger.error("Error", ex);
			styleNumId = BigInteger.valueOf(0L);
		}
		XWPFParagraph paragraph = document.createParagraph();
		paragraph.setStyle("ListParagraph");
		paragraph.setNumID(styleNumId);
		for (ReportParameter reportParam : reportParamsList) {
			XWPFRun run = paragraph.createRun();
			run.setText(reportParam.getNameAndDisplayValues());
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
//		CTTbl table2 = table.getCTTbl();
//		CTTblPr pr = table2.getTblPr();
//		CTTblWidth tblW = pr.getTblW();
//		tblW.setW(BigInteger.valueOf(5000));
//		tblW.setType(STTblWidth.PCT);
//		pr.setTblW(tblW);
//		table2.setTblPr(pr);
		row = table.getRow(0);
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
		String formattedValue = formatNumbericValue(value);
		outputCellText(formattedValue);
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = formatDateValue(value);
		outputCellText(formattedValue);
	}

	@Override
	public void newRow() {
		row = table.createRow();
		cellNumber = 0;
	}

	@Override
	public void endRows() {
		try {
			try (OutputStream fout = new FileOutputStream(fullOutputFilename)) {
				document.write(fout);
			}
		} catch (IOException ex) {
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

	private BigInteger addListStyle(XWPFDocument doc, char symbol) throws XmlException {
		//https://stackoverflow.com/questions/35896624/apache-poi-round-bullet-list-in-word-document?rq=1
		long listStyleId = 0L;
		String styleMaybe = "<w:numbering xmlns:wpc=\"http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas\" xmlns:mc=\"http://schemas.openxmlformats.org/markup-compatibility/2006\" xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" xmlns:m=\"http://schemas.openxmlformats.org/officeDocument/2006/math\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:wp14=\"http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" xmlns:w10=\"urn:schemas-microsoft-com:office:word\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:w14=\"http://schemas.microsoft.com/office/word/2010/wordml\" xmlns:w15=\"http://schemas.microsoft.com/office/word/2012/wordml\" xmlns:wpg=\"http://schemas.microsoft.com/office/word/2010/wordprocessingGroup\" xmlns:wpi=\"http://schemas.microsoft.com/office/word/2010/wordprocessingInk\" xmlns:wne=\"http://schemas.microsoft.com/office/word/2006/wordml\" xmlns:wps=\"http://schemas.microsoft.com/office/word/2010/wordprocessingShape\" mc:Ignorable=\"w14 w15 wp14\">\n"
				+ "<w:abstractNum w:abstractNumId=\"" + listStyleId + "\">\n"
				+ "<w:nsid w:val=\"6871722E\"/>\n"
				+ "<w:multiLevelType w:val=\"hybridMultilevel\"/>\n"
				+ "<w:tmpl w:val=\"8FE6E4C8\"/>\n"
				+ "<w:lvl w:ilvl=\"0\" w:tplc=\"0410000D\">\n"
				+ "<w:start w:val=\"1\"/>\n"
				+ "<w:numFmt w:val=\"bullet\"/>\n"
				+ "<w:lvlText w:val=\"" + symbol + "\"/>\n"
				+ "<w:lvlJc w:val=\"left\"/>\n"
				+ "<w:pPr>\n"
				+ "<w:ind w:left=\"720\" w:hanging=\"360\"/>\n"
				+ "</w:pPr>\n"
				+ "<w:rPr>\n"
				+ "<w:rFonts w:ascii=\"Webdings\" w:hAnsi=\"Webdings\" w:hint=\"default\"/>\n"
				+ "</w:rPr>\n"
				+ "</w:lvl>\n"
				+ "</w:abstractNum>\n"
				+ "<w:num w:numId=\"1\">\n"
				+ "<w:abstractNumId w:val=\"0\"/>\n"
				+ "</w:num>\n"
				+ "</w:numbering>";

		XWPFNumbering numbering = doc.createNumbering();

		// genero il numbering style dall'XML
		CTAbstractNum abstractNum = CTAbstractNum.Factory.parse(styleMaybe);
		XWPFAbstractNum abs = new XWPFAbstractNum(abstractNum, numbering);
		// gli imposto un ID univoco
		BigInteger id = BigInteger.valueOf(listStyleId);

		// assegno l'id all'abs
		abs.getAbstractNum().setAbstractNumId(id);

		// ora aggiungo l'abs al CT del numbering, che mi dovrebbe ritornare lo stesso id
		id = numbering.addAbstractNum(abs);
		// ora lo aggiungo al numbering creato prima che mi restituirà ancora lo stesso id
		return doc.getNumbering().addNum(id);
	}

}
