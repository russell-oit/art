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
import art.utils.ArtUtils;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.odftoolkit.odfdom.type.Color;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.style.Font;
import org.odftoolkit.simple.style.StyleTypeDefinitions;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

/**
 * Generate ods output
 *
 * @author Timothy Anyona
 */
public class OdsOutput extends StandardOutput {

	private SpreadsheetDocument document;
	private Table table;
	private Row row;
	private Cell cell;
	private int cellNumber;
	Font headerFont;
	Font bodyFont;
	Font totalFont;

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
		headerFont = null;
		bodyFont = null;
		totalFont = null;
	}

	@Override
	public void init() {
		try {
			resetVariables();

			document = SpreadsheetDocument.newSpreadsheetDocument();
//			table = document.getSheetByIndex(0);
//			table.setTableName(reportName);
			//using document.getSheetByIndex(0); and then table.setTableName(reportName);
			//results in info messages being logged about "No explicit text properties definition is found"
			//so append sheet and remove the first one created by default
			table = document.appendSheet(reportName);
			document.removeSheet(0);

			String fontFamilyName = "Arial";

			double headerFontSize = 12D;
			headerFont = new Font(fontFamilyName, StyleTypeDefinitions.FontStyle.BOLD, headerFontSize, Color.BLUE);

			double bodyFontSize = 10D;
			bodyFont = new Font(fontFamilyName, StyleTypeDefinitions.FontStyle.REGULAR, bodyFontSize, Color.BLACK);

			totalFont = new Font(fontFamilyName, StyleTypeDefinitions.FontStyle.BOLD, bodyFontSize, Color.BLACK);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addTitle() {
		row = table.getRowByIndex(0);
		addCellString(reportName);
		addCellString(ArtUtils.isoDateTimeSecondsFormatter.format(new Date()));
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			newRow();
			String paramLabel = reportParam.getParameter().getLabel();
			String paramDisplayValues = reportParam.getDisplayValues();
			addHeaderCell(paramLabel);
			addCellString(paramDisplayValues);
		}

		newRow();
	}

	@Override
	public void beginRows() {
		cellNumber = 0;
	}

	@Override
	public void beginHeader() {
		newRow();
	}

	@Override
	public void addHeaderCell(String value) {
		cell = row.getCellByIndex(cellNumber++);
		cell.setStringValue(value);
		cell.setFont(headerFont);
	}

	@Override
	public void addCellString(String value) {
		cell = row.getCellByIndex(cellNumber++);
		cell.setStringValue(value);
		cell.setFont(bodyFont);
	}

	@Override
	public void addCellNumeric(Double value) {
		cell = row.getCellByIndex(cellNumber++);
		cell.setDoubleValue(value);
		cell.setFont(bodyFont);
	}

	@Override
	public void addCellDate(Date value) {
		cell = row.getCellByIndex(cellNumber++);
		if (value != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(value);
			cell.setDateValue(calendar);
		}
		cell.setFont(bodyFont);
	}

	@Override
	public void newRow() {
		row = table.appendRow();
		cellNumber = 0;
	}

	@Override
	public void addCellTotal(Double value) {
		cell = row.getCellByIndex(cellNumber++);
		cell.setDoubleValue(value);
		cell.setFont(totalFont);
	}
	
	@Override
	public void addCellTotal(Double totalValue, String formattedValue, String sortValue) {
		addCellTotal(totalValue);
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

}
