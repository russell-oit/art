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
import java.util.Date;
import java.util.List;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.Fields;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Footer;
import org.odftoolkit.simple.text.Paragraph;

/**
 * Generates odt output
 *
 * @author Timothy Anyona
 */
public class OdtOutput extends StandardOutput {

	private TextDocument document;
	private Table table;
	private Row row;
	private Cell cell;
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
		try {
			resetVariables();
			document = TextDocument.newTextDocument();
			createPageNumbers();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addTitle() {
		Paragraph paragraph = document.addParagraph(reportName);
		paragraph.setHorizontalAlignment(StyleTypeDefinitions.HorizontalAlignmentType.CENTER);
		Font font = paragraph.getFont();
		font.setSize(14D);
		font.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
		paragraph.setFont(font);
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			document.addParagraph(reportParam.getNameAndDisplayValues());
		}
	}

	@Override
	public void beginRows() {
		cellNumber = 0;
	}

	@Override
	public void beginHeader() {
		document.addParagraph(""); //for paragraph/space/line between title and results table
		int rows = 1;
		int cols = totalColumnCount;
		table = document.addTable(rows, cols);
		row = table.getRowByIndex(0);
	}

	@Override
	public void addHeaderCell(String value) {
		cell = row.getCellByIndex(cellNumber++);
		Paragraph paragraph = cell.addParagraph(value);
		paragraph.setHorizontalAlignment(StyleTypeDefinitions.HorizontalAlignmentType.CENTER);
		Font font = paragraph.getFont();
		font.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
		paragraph.setFont(font);
	}

	@Override
	public void addHeaderCellAlignLeft(String value) {
		cell = row.getCellByIndex(cellNumber++);
		Paragraph paragraph = cell.addParagraph(value);
		paragraph.setHorizontalAlignment(StyleTypeDefinitions.HorizontalAlignmentType.LEFT);
		Font font = paragraph.getFont();
		font.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
		paragraph.setFont(font);
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
		row = table.appendRow();
		cellNumber = 0;
	}
	
	@Override
	public void addCellTotal(Double value){
		String formattedValue = formatNumericValue(value);
		
		cell = row.getCellByIndex(cellNumber++);
		Paragraph paragraph = cell.addParagraph(formattedValue);
		Font font = paragraph.getFont();
		font.setFontStyle(StyleTypeDefinitions.FontStyle.BOLD);
		paragraph.setFont(font);
		
	}

	@Override
	public void endOutput() {
		try {
			document.save(fullOutputFileName);
			document.close();
		} catch (Exception ex) {
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
		cell = row.getCellByIndex(cellNumber++);
		cell.setStringValue(value);
	}

	/**
	 * Creates page numbers in the document footer
	 */
	private void createPageNumbers() {
		Footer footer = document.getFooter();
		int rows = 1;
		int cols = 1;
		Table tbl = footer.addTable(rows, cols);
		Cell cl = tbl.getCellByPosition(0, 0);
		Paragraph paragraph = cl.addParagraph("");
		paragraph.setHorizontalAlignment(StyleTypeDefinitions.HorizontalAlignmentType.RIGHT);
		Fields.createCurrentPageNumberField(paragraph.getOdfElement());
	}

}
