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

/**
 * Generates DataTables html output
 *
 * @author Enrico Liboni
 */
public class HtmlDataTableOutput extends StandardOutput {

	private String tableId;

	@Override
	public void init() {
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/css/htmlDataTableOutput.css'>");
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/js/bootstrap-3.3.6-dist/css/bootstrap.min.css'>");
		out.println("<link rel='stylesheet' type='text/css' href='" + contextPath + "/js/dataTables-1.10.11/DataTables-1.10.11/css/dataTables.bootstrap.min.css'>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/jquery-1.10.2.min.js'></script>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/dataTables-1.10.11/DataTables-1.10.11/js/jquery.dataTables.min.js'></script>");
		out.println("<script type='text/javascript' src='" + contextPath + "/js/dataTables-1.10.11/DataTables-1.10.11/js/dataTables.bootstrap.min.js'></script>");

		//set language file to use for localization. language files to be put in the /js directory and to be named dataTables.xx_XX.txt	
		//language file content examples at http://datatables.net/plug-ins/i18n
		//by default don't set the language file option. (will default to english - in jquery.dataTables.min.js)
		String languageSetting = "";

		String language = "";
		if (locale != null) {
			language = locale.toString(); //e.g. en, en-us, it, fr etc
		}

		if (StringUtils.isNotBlank(language)) {
			String languageFileName = "dataTables." + language + ".txt";
			String languageFilePath = Config.getAppPath() + File.separator
					+ "js" + File.separator + languageFileName;
			File languageFile = new File(languageFilePath);
			if (languageFile.exists()) {
				languageSetting = ", oLanguage: {\"sUrl\": " + contextPath + "/js/" + languageFileName + "\"}";
			}
		}

		//set table options. see http://www.datatables.net/ref
		String dataTableOptions = "{aaSorting: []"
				+ ", PaginationType:\"full_numbers\""
				+ languageSetting
				+ ", iDisplayLength: 50" //default item in show entries e.g. -1
				+ ", aLengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, \"All\"]]" //show entries options
				+ ", orderClasses: false"
				+ "}";

		tableId = "Tid" + Long.toHexString(Double.doubleToLongBits(Math.random()));
		out.println("<script type='text/javascript' charset='utf-8'>");
		out.println("	var $jQuery = jQuery.noConflict();");
		out.println("	$jQuery(document).ready(function() {");
		out.println("		$jQuery('#" + tableId + "').dataTable(" + dataTableOptions + ");");
		out.println("	} );");
		out.println("</script>");

	}

	@Override
	public void beginHeader() {
		out.println("<div style='border: 1px solid black; width: 95%; margin: 0 auto'>");
		out.println("<table class='table table-bordered table-striped table-condensed' id='" + tableId + "'>");
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
	public void addCellNumeric(Double value) {
		String formattedValue = formatNumbericValue(value);
		String sortValue;

		if (value == null) {
			sortValue = null;
		} else {
			sortValue = sortNumberFormatter.format(value);
		}

		out.println("<td style='text-align: right' data-order='" + sortValue + "'>"
				+ formattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = formatDateValue(value);
		long sortValue = getDateSortValue(value);

		out.println("<td style='text-align: right' data-order='" + sortValue + "'>"
				+ formattedValue + "</td>");
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
	public void endRow(){
		out.println("</tr>");
	}
	
	@Override
	public void endRows() {
		out.println("</tbody>");
	}
	
	@Override
	public void beginTotalRow(){
		out.println("<tfoot><tr>");
	}
	
	@Override
	public void endTotalRow(){
		out.println("</tr><tfoot>");
	}
	
	@Override
	public void endOutput() {
		out.println("</table></div>");
	}
}
