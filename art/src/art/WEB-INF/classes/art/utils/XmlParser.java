/*
 * Copyright (C) 2001/2003  Enrico Liboni  - enrico@computer.org
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation;
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   (version 2) along with this program (see documentation directory); 
 *   otherwise, have a look at http://www.gnu.org or write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */
/**
 * Very simple and minimal implementation of a XML Parser (parse just ELEMENTS
 * and text within them) Used for dynamic SQL and Dashboards
 */
package art.utils;

import java.util.*;
import org.apache.commons.lang.StringUtils;

/**
 * Very simple and minimal implementation of a XML Parser (parse just ELEMENTS
 * and text within them) Used for dynamic SQL and Dashboards
 *
 * @author Enrico Liboni
 */
public class XmlParser {

	/**
	 * Returns the text between a given element in a xml string
	 *
	 * @param xml
	 * @param element
	 * @return the text between a given element in a xml string
	 * @throws ArtException
	 */
	public static String getXmlElementValue(String xml, String element) throws ArtException {

		String value=null; //return null if start element not found
		
		String startElement = "<" + element + ">";
		String endElement = "</" + element + ">";

		// Start Pos
		int start = StringUtils.indexOf(xml, startElement);

		if (start != -1) {
			// End Pos
			int end = xml.indexOf(endElement);

			// Validate end element
			if ((end == -1) || (end < start)) {
				throw new ArtException("End element not found: &lt;/" + element + "&gt;");
			}

			// Extract value
			value=xml.substring((start + startElement.length()), end);
		}

		return value; 
	}

	/**
	 * Returns a list (of strings) with the values between a given element in a
	 * xml string (the list stores all the element values)
	 *
	 * @param xml
	 * @param element
	 * @return list (of strings) with the values between a given element in a
	 * xml string
	 * @throws ArtException
	 */
	public static List<String> getXmlElementValues(String xml, String element) throws ArtException {

		String xmlString = xml;
		List<String> values = new ArrayList<String>();
		
		String startElement = "<" + element + ">";
		String endElement = "</" + element + ">";

		// Start Pos
		int start = StringUtils.indexOf(xmlString, startElement);

		while (start != -1) {
			int end = xmlString.indexOf(endElement, start);

			// validate end element
			if ((end == -1) || (end < start)) {
				throw new ArtException("End element not found: &lt;/" + element + "&gt;");
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
	 * @param xml
	 * @param element
	 * @param offset
	 * @return XmlInfo object that contains the text between a given element in
	 * an xml string
	 * @throws ArtException
	 */
	public static XmlInfo getXmlElementInfo(String xml, String element, int offset) throws ArtException {

		XmlInfo info=null;
		
		String startElement = "<" + element + ">";

		// Start Pos
		int start = StringUtils.indexOf(xml, startElement, offset);

		if (start != -1) {
			String endElement = "</" + element + ">";
			int end = xml.indexOf(endElement, start);

			// validate end element
			if ((end == -1) || (end < start)) {
				throw new ArtException("End element not found: &lt;/" + element + "&gt;");
			}

			info=new XmlInfo(xml.substring(start + startElement.length()), start, end);
		}

		return info;
	}
}
