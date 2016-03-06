/*
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
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

import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Generate xml output
 *
 * @author Enrico Liboni
 */
public class XmlOutput extends StandardOutput {

    @Override
    public String getContentType() {
        return "application/xml"; // mime type (use "text/html" for html)
    }
	
	@Override
	public void init() {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		outputDtd();
		out.println("<table>");
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		out.println("<reportparams>");
		for (ReportParameter reportParam : reportParamsList) {
			String paramName = reportParam.getParameter().getName();
			String displayValues = reportParam.getDisplayValues();

			String outputString = "<name>" + StringEscapeUtils.escapeXml(paramName)
					+ "</name><value>" + StringEscapeUtils.escapeXml(displayValues) + "<value>";

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
		out.println("<col type=\"header\">" + StringEscapeUtils.escapeXml(value) + "</col>");
	}

	@Override
	public void endHeader() {
		out.println("</row>");
	}

	@Override
	public void addCellString(String value) {
		out.println("<col type=\"string\">" + StringEscapeUtils.escapeXml(value) + "</col>");
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue;

		if (value == null) {
			formattedValue = "0";
		} else {
			formattedValue = String.valueOf(value);
		}

		out.println("<col type=\"numeric\">" + formattedValue + "</col>");
	}

	@Override
	public void addCellDate(Date value) {
		out.println("<col type=\"date\">" + Config.getIsoDateDisplayString(value) + "</col>");
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
	public void endRows() {
		out.println("</row>");
		out.println("<totalrows>" + rowCount + "</totalrows>");
		out.println("</table>");
	}

//	@Override
//	public boolean isShowQueryHeaderAndFooter() {
//		return false; // if set to true, art will add a standard html header&footer around the output
//	}
}
