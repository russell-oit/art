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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

/**
 * Generates DataTables html output
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class HtmlDataTableOutput extends StandardOutput {

	private String tableId;
	private int localRowCount; //use local variable for reportengine output

	@Override
	public void init() {
		localRowCount = 0;

		//set language file to use for localization
		//language files to be put in the /js/datatables/i18n directory and to be named dataTables_xx.json according to the locale
		//language file content examples at http://datatables.net/plug-ins/i18n
		//https://datatables.net/reference/api/i18n()
		//https://datatables.net/reference/option/language
		//if the language file option is not set, it will default to the english strings in jquery.dataTables.min.js
		final String DEFAULT_LANGUAGE = "en";
		String language;
		if (locale == null) {
			language = DEFAULT_LANGUAGE;
		} else {
			language = locale.toString(); //e.g. en, en_US, it, fr etc
			String languageFileName = "dataTables_" + language + ".json";

			String languageFilePath = Config.getAppPath() + File.separator
					+ "js" + File.separator
					+ "dataTables" + File.separator
					+ "i18n" + File.separator
					+ languageFileName;

			File languageFile = new File(languageFilePath);

			if (!languageFile.exists()) {
				language = DEFAULT_LANGUAGE;
			}
		}

		tableId = "table-" + RandomStringUtils.randomAlphanumeric(5);

		String options = report.getOptions();
		if (StringUtils.isBlank(options)) {
			options = "";
		}

		Context ctx = new Context(locale);
		ctx.setVariable("ajax", ajax);
		ctx.setVariable("pageHeaderLoaded", pageHeaderLoaded);
		ctx.setVariable("contextPath", contextPath);
		ctx.setVariable("tableId", tableId);
		ctx.setVariable("language", language);
		ctx.setVariable("options", options);

		SpringTemplateEngine templateEngine = Config.getDefaultThymeleafTemplateEngine();
		templateEngine.setMessageSource(messageSource);

		String templateName = "htmlDataTableOutputInit";
		String initString = templateEngine.process(templateName, ctx);
		out.println(initString);
	}

	@Override
	public void beginHeader() {
		out.println("<div>");
		out.println("<table class='table table-bordered table-striped table-condensed heatmap' id='" + tableId + "'>");
		out.println("<thead><tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		String cleanClassName;
		if (value == null) {
			cleanClassName = "";
		} else {
			//only allow english alphabets, numbers, underscore, dash
			cleanClassName = value.replaceAll("[^a-zA-Z0-9_\\-]+", "-");
		}
		String encodedClassName = Encode.forHtmlAttribute(cleanClassName);
		String finalClassName = "rcol-" + encodedClassName;
		
		out.println("<th class='" + finalClassName + "'>" + value + "</th>");
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
		localRowCount++;
		if (localRowCount > 1) {
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
