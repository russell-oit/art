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
import art.utils.ArtUtils;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.odftoolkit.odfdom.dom.element.style.StyleMasterPageElement;
import org.odftoolkit.odfdom.dom.style.props.OdfPageLayoutProperties;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStylePageLayout;
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
	private int currentRow;
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
		currentRow = 0;
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
			
			PageOrientation pageOrientation = report.getPageOrientation();
			if (pageOrientation == PageOrientation.Landscape) {
				setLandscapeOrientation();
			}

			String fontFamilyName = "Arial";

			double headerFontSize = 12D;
			headerFont = new Font(fontFamilyName, StyleTypeDefinitions.FontStyle.BOLD, headerFontSize, Color.BLUE);

			double bodyFontSize = 10D;
			bodyFont = new Font(fontFamilyName, StyleTypeDefinitions.FontStyle.REGULAR, bodyFontSize, Color.BLACK);

			totalFont = new Font(fontFamilyName, StyleTypeDefinitions.FontStyle.BOLD, bodyFontSize, Color.BLACK);
		} catch (Exception ex) {
			endOutput();
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Sets the document page size to A4 Landscape
	 */
	private void setLandscapeOrientation() {
		//https://dentrassi.de/2012/08/27/setting-the-page-size-and-orientation-with-odfdom-for-tables-aka-spreadsheets/
		//https://stackoverflow.com/questions/18108452/how-can-the-page-size-page-orientation-and-page-margins-of-an-ods-spreadsheet
		StyleMasterPageElement defaultPage = document.getOfficeMasterStyles().getMasterPage("Default");
		String pageLayoutName = defaultPage.getStylePageLayoutNameAttribute();
		OdfStylePageLayout pageLayout = defaultPage.getAutomaticStyles().getPageLayout(pageLayoutName);
		pageLayout.setProperty(OdfPageLayoutProperties.PrintOrientation, "landscape");
		pageLayout.setProperty(OdfPageLayoutProperties.PageHeight, "210.01mm");
		pageLayout.setProperty(OdfPageLayoutProperties.PageWidth, "297mm");
	}

	@Override
	public void addTitle() {
		if (report.isOmitTitleRow()) {
			return;
		}

		newRow();
		addCellString(reportName);
		addCellString(ArtUtils.isoDateTimeSecondsFormatter.format(new Date()));
		newRow();
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (CollectionUtils.isEmpty(reportParamsList)) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			try {
				newRow();
				String paramLabel = reportParam.getParameter().getLocalizedLabel(locale);
				String paramDisplayValues = reportParam.getDisplayValues();
				addHeaderCell(paramLabel);
				addCellString(paramDisplayValues);
			} catch (IOException ex) {
				endOutput();
				throw new RuntimeException(ex);
			}
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

		if (value != null) {
			cell.setDoubleValue(value);
			cell.setFont(bodyFont);
		}
	}

	@Override
	public void addCellDate(Date value) {
		cell = row.getCellByIndex(cellNumber++);

		if (value != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(value);
			cell.setDateValue(calendar);
			cell.setFont(bodyFont);
		}
	}

	@Override
	public void newRow() {
		//don't use table.appendRow(). a new table/sheet seems to have 2 rows already in it
		row = table.getRowByIndex(currentRow++);
		cellNumber = 0;
	}

	@Override
	public void addCellTotal(Double value) {
		cell = row.getCellByIndex(cellNumber++);

		if (value != null) {
			cell.setDoubleValue(value);
			cell.setFont(totalFont);
		}
	}

	@Override
	public void addCellTotal(Double totalValue, String formattedValue, String sortValue) {
		addCellTotal(totalValue);
	}

	@Override
	public void endOutput() {
		try {
			if (document != null) {
				//set open password
				String openPassword = report.getOpenPassword();
				if (StringUtils.isNotEmpty(openPassword)) {
					document.setPassword(openPassword);
				}
				
				//no way to set modify password. can only protect the worksheet, which can be unprotected without a password
				//table.setProtected(true);

				document.save(fullOutputFileName);
				document.close();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
