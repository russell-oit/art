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

import art.servlets.Config;
import java.io.File;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;

/**
 * Generates DataTables html output
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class HtmlDataTableOutput extends StandardOutput {

	private String tableId;

	@Override
	public void init() {
		//include required css and javascript files
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/css/htmlDataTableOutput.css'>");
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/js/bootstrap-3.3.6/css/bootstrap.min.css'>");
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/js/dataTables/DataTables-1.10.13/css/dataTables.bootstrap.min.css'>");
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/js/dataTables/Buttons-1.2.4/css/buttons.dataTables.min.css'>");
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/js/dataTables/Buttons-1.2.4/css/buttons.bootstrap.min.css'>");
		//note that including script files will cause the browser to display the following warning e.g. on firefox's debug console (Ctrl + Shift + I) when report run inline (using ajax)
		//Synchronous XMLHttpRequest on the main thread is deprecated because of its detrimental effects to the end user's experience
		//https://stackoverflow.com/questions/24639335/javascript-console-log-causes-error-synchronous-xmlhttprequest-on-the-main-thr
		//https://github.com/jquery/jquery/issues/2060
		//however we have to include the script files for report run by ajax to work
		if (!ajax) {
			//including jquery.js while using $.load() or $.post() results in spinner not appearing on second run
			out.println("<script src='" + contextPath + "/js/jquery-1.12.4.min.js'></script>");
		}
		out.println("<script src='" + contextPath + "/js/dataTables/DataTables-1.10.13/js/jquery.dataTables.min.js'></script>");
		out.println("<script src='" + contextPath + "/js/dataTables/DataTables-1.10.13/js/dataTables.bootstrap.min.js'></script>");
		out.println("<script src='" + contextPath + "/js/dataTables/Buttons-1.2.4/js/dataTables.buttons.min.js'></script>");
		out.println("<script src='" + contextPath + "/js/dataTables/Buttons-1.2.4/js/buttons.bootstrap.min.js'></script>");
		out.println("<script src='" + contextPath + "/js/dataTables/JSZip-2.5.0/jszip.min.js'></script>");
		out.println("<script src='" + contextPath + "/js/dataTables/pdfmake-0.1.18/pdfmake.min.js'></script>");
		out.println("<script src='" + contextPath + "/js/dataTables/pdfmake-0.1.18/vfs_fonts.js'></script>");
		out.println("<script src='" + contextPath + "/js/dataTables/Buttons-1.2.4/js/buttons.html5.min.js'></script>");
		out.println("<script src='" + contextPath + "/js/dataTables/Buttons-1.2.4/js/buttons.print.min.js'></script>");
		out.println("<script src='" + contextPath + "/js/dataTables/Buttons-1.2.4/js/buttons.colVis.min.js'></script>");
		out.println("<script src='" + contextPath + "/js/art.js'></script>");

		//set language file to use for localization
		//language files to be put in the /js/datatables/i18n directory and to be named dataTables_xx.json according to the locale
		//language file content examples at http://datatables.net/plug-ins/i18n
		//https://datatables.net/reference/api/i18n()
		//https://datatables.net/reference/option/language
		//by default don't set the language file option. (will default to english - in jquery.dataTables.min.js)
		String languageSetting = "";

		String language = "";
		if (locale != null) {
			language = locale.toString(); //e.g. en, en_US, it, fr etc
		}

		if (StringUtils.isNotBlank(language)) {
			String languageFileName = "dataTables_" + language + ".json";

			String languageFilePath = Config.getAppPath() + File.separator
					+ "js" + File.separator
					+ "dataTables" + File.separator
					+ "i18n" + File.separator
					+ languageFileName;

			File languageFile = new File(languageFilePath);

			if (languageFile.exists()) {
				languageSetting = ", language: {url: '" + contextPath + "/js/dataTables/i18n/"
						+ languageFileName + "'}";
			}
		}

		String allText = "All";
		if (messageSource != null && locale != null) {
			allText = messageSource.getMessage("dataTables.text.showAllRows", null, locale);
			allText = Encode.forJavaScript(allText);
		}

		//http://www.datatables.net/reference
		String dataTableOptions
				= "{"
				+ "orderClasses: false"
				+ ", order: []"
				+ ", pagingType: 'full_numbers'"
				+ ", lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, '" + allText + "']]"
				+ ", pageLength: 50"
				+ ", dom: 'lBfrtip'"
				+ ", buttons: ["
				+ "{extend: 'colvis', postfixButtons: ['colvisRestore']},"
				+ "{extend: 'excel', exportOptions: {columns: ':visible'}},"
				+ "{extend: 'pdf', exportOptions: {columns: ':visible'}},"
				+ "{extend: 'print', exportOptions: {columns: ':visible'}}"
				+ "]"
				+ languageSetting
				+ ", initComplete: function() {if(!isMobile()){$('div.dataTables_filter input').focus();}}"
				+ "}";

		tableId = "Tid" + Long.toHexString(Double.doubleToLongBits(Math.random()));
		out.println("<script>");
		out.println("	$(document).ready(function() {");
		out.println("		$('#" + tableId + "').dataTable(" + dataTableOptions + ");");
		out.println("	});");
		out.println("</script>");
	}

	@Override
	public void beginHeader() {
		out.println("<div>");
		out.println("<table class='table table-bordered table-striped table-condensed heatmap' id='" + tableId + "'>");
		out.println("<thead><tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		out.println("<th>" + value + "</th>");
	}

	@Override
	public void addHeaderCellAlignLeft(String value) {
		out.println("<th style='text-align: left'>" + value + "</th>");
	}

	@Override
	public void addHeaderCellAlignLeft(String value, String sortValue) {
		String escapedSortValue = Encode.forHtmlAttribute(sortValue);
		out.println("<th style='text-align: left' data-order='" + escapedSortValue + "'>"
				+ value + "</th>");
	}

	@Override
	public void endHeader() {
		out.println("</tr></thead>");

	}

	@Override
	public void beginRows() {
		out.println("<tbody>");
	}

	@Override
	public void addCellString(String value) {
		out.println("<td style='text-align: left'>" + value + "</td>");
	}

	@Override
	public void addCellStringUnsafe(String value) {
		String escapedValue = Encode.forHtmlContent(value);
		out.println("<td style='text-align: left'>" + escapedValue + "</td>");
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue = formatNumericValue(value);
		String sortValue = getNumericSortValue(value);

		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		String escapedSortValue = Encode.forHtmlAttribute(sortValue);

		double heatmapValue = getHeatmapValue(value);

		out.println("<td style='text-align: right' data-order='" + escapedSortValue
				+ "' data-value='" + heatmapValue + "'>"
				+ escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		String escapedSortValue = Encode.forHtmlAttribute(sortValue);

		double heatmapValue = getHeatmapValue(numericValue);

		out.println("<td style='text-align: right' data-order='" + escapedSortValue
				+ "' data-value='" + heatmapValue + "'>"
				+ escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = formatDateValue(value);
		long sortValue = getDateSortValue(value);

		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);

		out.println("<td style='text-align: right' data-order='" + sortValue + "'>"
				+ escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		out.println("<td style='text-align: right' data-order='" + sortValue + "'>"
				+ escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellImage(byte[] binaryData) {
		//https://stackoverflow.com/questions/34111390/displaying-blob-image-from-mysql-database-into-dynamic-div-in-html
		if (binaryData == null) {
			out.println("<td></td>");
		} else {
			String stringData = Base64.encodeBase64String(binaryData);
			out.println("<td style='text-align: center'><img src='data:image/png;base64," + stringData + "'></td>");
		}
	}

	@Override
	public void newRow() {
		if (rowCount > 1) {
			//close previous row
			out.println("</tr>");
		}

		//open new row
		out.println("<tr>");
	}

	@Override
	public void endRow() {
		out.println("</tr>");
	}

	@Override
	public void endRows() {
		out.println("</tbody>");
	}

	@Override
	public void beginTotalRow() {
		out.println("<tfoot><tr>");
	}

	@Override
	public void endTotalRow() {
		out.println("</tr><tfoot>");
	}

	@Override
	public void endOutput() {
		out.println("</table></div>");
	}
}
