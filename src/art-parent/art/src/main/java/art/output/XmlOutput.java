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
import art.servlets.Config;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.owasp.encoder.Encode;

/**
 * Generates xml output
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class XmlOutput extends StandardOutput {

	@Override
	public String getContentType() {
		return "application/xml"; // mime type (use "text/html" for html)
	}

	@Override
	public boolean outputHeaderAndFooter() {
		return false;
	}

	@Override
	public void init() {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		outputDtd();
		out.println("<table>");
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (CollectionUtils.isEmpty(reportParamsList)) {
			return;
		}

		out.println("<reportparams>");
		for (ReportParameter reportParam : reportParamsList) {
			String paramLabel = reportParam.getParameter().getLabel();
			String displayValues = reportParam.getDisplayValues();

			String outputString = "<name>" + Encode.forXml(paramLabel)
					+ "</name><value>" + Encode.forXml(displayValues) + "<value>";

			out.println("<param>");
			out.println(outputString);
			out.println("</param>");
		}
		out.println("</reportparams>");
	}

	@Override
	public void beginHeader() {
		out.println("<row>");
	}

	private void outputDtd() {
		//out.println("<!DOCTYPE table SYSTEM \"art/etc/artxml.dtd\"");

		// simple DTD
		out.println("<!DOCTYPE table [");

		out.println("  <!ELEMENT table (reportparams,row*,totalrows)>");

		out.println("  <!ELEMENT reportparams (param*)>");
		out.println("  <!ELEMENT param   (name,value)>");
		out.println("  <!ELEMENT name   (#PCDATA)>");
		out.println("  <!ELEMENT value  (#PCDATA)>");

		out.println("  <!ELEMENT row   (col*)>");
		//out.println("  <!ELEMENT row   (col*,totalrows)>");
		out.println("  <!ELEMENT col   (#PCDATA)>");
		out.println("  <!ATTLIST col");
		out.println("       type CDATA #IMPLIED");
		out.println("  >");
		out.println("  <!ELEMENT totalrows (#PCDATA)>");
		out.println("]>");
	}

	@Override
	public void addHeaderCell(String value) {
		String escapedValue = Encode.forXml(value);
		out.println("<col type=\"header\">" + escapedValue + "</col>");
	}

	@Override
	public void endHeader() {
		out.println("</row>");
	}

	@Override
	public void addCellString(String value) {
		String escapedValue = Encode.forXml(value);
		out.println("<col type=\"string\">" + escapedValue + "</col>");
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "0";
		} else {
			formattedValue = String.valueOf(value);
		}
		
		String escapedFormattedValue = Encode.forXml(formattedValue);

		out.println("<col type=\"numeric\">" + escapedFormattedValue + "</col>");
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		String escapedFormattedValue = Encode.forXml(formattedValue);
		out.println("<col type=\"numeric\">" + escapedFormattedValue + "</col>");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = Config.getIsoDateDisplayString(value);
		String escapedFormattedValue = Encode.forXml(formattedValue);
		out.println("<col type=\"date\">" + escapedFormattedValue + "</col>");
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		String escapedFormattedValue = Encode.forXml(formattedValue);
		out.println("<col type=\"date\">" + escapedFormattedValue + "</col>");
	}

	@Override
	public void newRow() {
		if (rowCount > 1) {
			//close previous row
			out.println("</row>");
		}

		//open new row
		out.println("<row>");
	}

	@Override
	public void endRow() {
		out.println("</row>");
	}

	@Override
	public void beginTotalRow() {
		out.println("<row>");
	}

	@Override
	public void endOutput() {
		out.println("<totalrows>" + rowCount + "</totalrows>");
		out.println("</table>");
	}
}
