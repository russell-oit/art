/*
 * Copyright 2001-2016 Enrico Liboni <eliboni@users.sourceforge.net>
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

import art.servlets.Config;
import java.io.File;
import java.util.Date;
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
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/css/htmlDataTableOutput.css'>");
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/js/bootstrap-3.3.6/css/bootstrap.min.css'>");
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/js/dataTables-1.10.11/DataTables-1.10.11/css/dataTables.bootstrap.min.css'>");
		//note that including script files will cause the browser to display the following warning e.g. on firefox's debug console (Ctrl + Shift + I) when report run inline (using ajax)
		//Synchronous XMLHttpRequest on the main thread is deprecated because of its detrimental effects to the end user's experience
		//https://stackoverflow.com/questions/24639335/javascript-console-log-causes-error-synchronous-xmlhttprequest-on-the-main-thr
		//however we have to include the script files for report run by ajax to work
		out.println("<script type='text/javascript' src='" + contextPath + "/js/jquery-1.12.4.min.js'></script>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/dataTables-1.10.11/DataTables-1.10.11/js/jquery.dataTables.min.js'></script>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/dataTables-1.10.11/DataTables-1.10.11/js/dataTables.bootstrap.min.js'></script>");

		//set language file to use for localization. language files to be put in the /js directory and to be named dataTables.xx_XX.txt	
		//language file content examples at http://datatables.net/plug-ins/i18n
		//by default don't set the language file option. (will default to english - in jquery.dataTables.min.js)
		String languageSetting = "";

		String language = "";
		if (locale != null) {
			language = locale.toString(); //e.g. en, en_US, it, fr etc
		}

		if (StringUtils.isNotBlank(language)) {
			String languageFileName = "dataTables_" + language + ".txt";
			
			String languageFilePath = Config.getAppPath() + File.separator
					+ "js" + File.separator
					+ "dataTables-1.10.11" + File.separator
					+ "i18n" + File.separator
					+ languageFileName;
			
			File languageFile = new File(languageFilePath);
			
			if (languageFile.exists()) {
				languageSetting = ", language: {url: " + contextPath + "/js/dataTables-1.10.11/i18n/"
						+ languageFileName + "}";
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
				+ ", pagingType: 'full_numbers'"
				+ ", lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, '" + allText + "']]"
				+ ", pageLength: 50"
				+ languageSetting
				+ ", initComplete: function() {$('div.dataTables_filter input').focus();}"
				+ "}";

		tableId = "Tid" + Long.toHexString(Double.doubleToLongBits(Math.random()));
		out.println("<script type='text/javascript'>");
		out.println("	$(document).ready(function() {");
		out.println("		$('#" + tableId + "').dataTable(" + dataTableOptions + ");");
		out.println("	});");
		out.println("</script>");

	}

	@Override
	public void beginHeader() {
		out.println("<div>");
		out.println("<table class='table table-bordered table-striped table-condensed' id='" + tableId + "'>");
		out.println("<thead><tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		String escapedValue = Encode.forHtmlContent(value);
		out.println("<th>" + escapedValue + "</th>");
	}

	@Override
	public void addHeaderCellAlignLeft(String value) {
		String escapedValue = Encode.forHtmlContent(value);
		out.println("<th style='text-align: left'>" + escapedValue + "</th>");
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
		String escapedValue = Encode.forHtmlContent(value);
		out.println("<td style='text-align: left'>" + escapedValue + "</td>");
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue = formatNumericValue(value);
		String sortValue = getNumericSortValue(value);
		
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		String escapedSortValue = Encode.forHtmlAttribute(sortValue);

		out.println("<td style='text-align: right' data-order='" + escapedSortValue + "'>"
				+ escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);
		String escapedSortValue = Encode.forHtmlAttribute(sortValue);
		out.println("<td style='text-align: right' data-order='" + escapedSortValue + "'>"
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
