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
package art.utils;

import java.util.*;
import org.apache.commons.lang3.StringUtils;

/**
 * Very simple and minimal implementation of a XML Parser (parse just ELEMENTS
 * and text within them) Used for dynamic SQL and Dashboards
 *
 * @author Enrico Liboni
 */
public class XmlParser {

	/**
	 * Returns the text between a given element in a xml string, null if the
	 * element is not found
	 *
	 * @param xml the xml string
	 * @param element the tag to find
	 * @return the text between a given element in a xml string, null if the
	 * element is not found
	 */
	public static String getXmlElementValue(String xml, String element) {

		String value = null; //return null if start element not found

		String startElement = "<" + element + ">";
		String endElement = "</" + element + ">";

		// Start Pos
		int start = StringUtils.indexOf(xml, startElement);

		if (start != -1) {
			// End Pos
			int end = xml.indexOf(endElement);

			// Validate end element
			if ((end == -1) || (end < start)) {
				throw new RuntimeException("End element not found: " + element);
			}

			// Extract value
			value = xml.substring((start + startElement.length()), end);
		}

		return value;
	}

	/**
	 * Returns a list (of strings) with the values between a given element in a
	 * xml string (the list stores all the element values)
	 *
	 * @param xml the xml string
	 * @param element the tag to find
	 * @return list (of strings) with the values between a given element in a
	 * xml string
	 */
	public static List<String> getXmlElementValues(String xml, String element) {

		String xmlString = xml;
		List<String> values = new ArrayList<>();

		String startElement = "<" + element + ">";
		String endElement = "</" + element + ">";

		// Start Pos
		int start = StringUtils.indexOf(xmlString, startElement);

		while (start != -1) {
			int end = xmlString.indexOf(endElement, start);

			// validate end element
			if ((end == -1) || (end < start)) {
				throw new RuntimeException("End element not found: " + element);
			}

			// extract the substring
			values.add(xmlString.substring((start + startElement.length()), end));
			xmlString = xmlString.substring(end);
			start = xmlString.indexOf(startElement);
		}

		return values;
	}

	/**
	 * Returns a XmlInfo object that contains the text between a given element
	 * in a xml string as well as the position of the text in the string
	 *
	 * @param xml the xml string
	 * @param element the tag to find
	 * @param offset the position of in the xml string to start at
	 * @return XmlInfo object that contains the text between a given element in
	 * an xml string or null if the element is not found
	 */
	public static XmlInfo getXmlElementInfo(String xml, String element, int offset) {

		XmlInfo info = null;

		String startElement = "<" + element + ">";

		// Start Pos
		int start = StringUtils.indexOf(xml, startElement, offset);

		if (start != -1) {
			String endElement = "</" + element + ">";
			int end = xml.indexOf(endElement, start);

			// validate end element
			if ((end == -1) || (end < start)) {
				throw new RuntimeException("End element not found: " + element);
			}

			info = new XmlInfo(xml.substring(start + startElement.length()), start, end);
		}

		return info;
	}
}
