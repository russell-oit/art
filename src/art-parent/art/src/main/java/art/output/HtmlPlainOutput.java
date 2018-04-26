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
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.owasp.encoder.Encode;

/**
 * Generates plain html output. Can be used for jobs because the output does not
 * depend on other files (css etc) and it is a standalone page
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class HtmlPlainOutput extends StandardOutput {

	private final boolean fileOutput;
	private int localRowCount;

	public HtmlPlainOutput(boolean fileOutput) {
		this.fileOutput = fileOutput;
	}

	@Override
	public void init() {
		if (fileOutput) {
			out.println("<!DOCTYPE html>");
			out.println("<html>");
			out.println("<head>");
			out.println("<meta charset='utf-8'>");
			out.println("</head>");
			out.println("<body>");
		}

		localRowCount = 0;

		//https://www.campaignmonitor.com/css/
		out.println("<style>"
				+ "table {border-collapse: collapse;}"
				+ "\n td {background-color: #FFFFFF; border: 1px solid #000000; font-size: 10pt; padding:0 2px;}"
				+ "\n th {background-color: #FFFFFF; border: 1px solid #000000; font-size: 10pt; padding:0 2px;}"
				+ "\n body {font-family: Verdana, Helvetica , Arial, SansSerif; color: #000000;}"
				+ "\n</style>");

	}

	@Override
	public void addTitle() {
//		if (!fileOutput) {
//			return;
//		}
//		
//		if (report.isOmitTitleRow()) {
//			return;
//		}
//
//		out.println("<div align='center'>");
//		out.println("<table border='0' width='100%' cellspacing='1'"
//				+ " cellpadding='1'>");
//		out.println("<tr><td>");
//
//		String escapedReportName = Encode.forHtmlContent(reportName);
//		String formattedRunDate = ArtUtils.isoDateTimeSecondsFormatter.format(new Date());
//		String escapedFormattedRunDate = Encode.forHtmlContent(formattedRunDate);
//
//		out.println("<b>" + escapedReportName + "</b> :: " + escapedFormattedRunDate);
//
//		out.println("</td></tr></table></div>");
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (CollectionUtils.isEmpty(reportParamsList)) {
			return;
		}

		if (!fileOutput) {
			return;
		}

		out.println("<div align='center'>");
		out.println("<table border='0' width='100%' cellspacing='1'"
				+ " cellpadding='1'>");
		out.println("<tr><td>");

		for (ReportParameter reportParam : reportParamsList) {
			String labelAndDisplayValues;
			try {
				labelAndDisplayValues = reportParam.getLocalizedLabelAndDisplayValues(locale);
			} catch (IOException ex) {
				labelAndDisplayValues = ex.toString();
			}
			String escapedLabelAndDisplayValues = Encode.forHtmlContent(labelAndDisplayValues);
			out.println(escapedLabelAndDisplayValues);
			out.println("<br>");
		}

		out.println("</td></tr></table></div>");
	}

	@Override
	public void beginHeader() {
		out.println("<div align='center'>");
		out.println("<table border='0' width='100%' cellspacing='1'"
				+ " cellpadding='1' class='heatmap'>");
		out.println("<thead><tr>");
	}

	@Override
	public void addHeaderCell(String value) {
		out.println("<th><b>" + value + "</b></th>");
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
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);

		double heatmapValue = getHeatmapValue(value);

		out.println("<td style='text-align: right'"
				+ "' data-value='" + heatmapValue + "'>"
				+ escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);

		double heatmapValue = getHeatmapValue(numericValue);

		out.println("<td style='text-align: right'"
				+ "' data-value='" + heatmapValue + "'>"
				+ escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = formatDateValue(value);
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);

		out.println("<td style='text-align: right'>" + escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);

		out.println("<td style='text-align: right'>" + escapedFormattedValue + "</td>");
	}

	@Override
	public void addCellImage(byte[] binaryData) {
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
	public void addCellTotal(Double value) {
		String formattedValue = formatNumericValue(value);
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);

		out.println("<td style='text-align: right'><b>" + escapedFormattedValue + "</b></td>");
	}

	@Override
	public void addCellTotal(Double totalValue, String formattedValue, String sortValue) {
		String escapedFormattedValue = Encode.forHtmlContent(formattedValue);

		out.println("<td style='text-align: right'><b>" + escapedFormattedValue + "</b></td>");
	}

	@Override
	public void endTotalRow() {
		out.println("</tr><tfoot>");
	}

	@Override
	public void endOutput() {
		out.println("</table></div>");

		if (fileOutput) {
			out.println("</body></html>");
		}
	}
}
