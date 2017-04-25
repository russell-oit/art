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

/**
 * This object is used by the PreparedQuery/XmlParser classes
 *  to maintain the text in an xml tag as
 *  well as its initial and end position in the SQL query string
 * 
 * @author Enrico Liboni
 */
public class XmlInfo {

    String text;
    int start, end;

    /**
     * 
     * @param t
     * @param s
     * @param e
     */
    public XmlInfo(String t, int s, int e) {
        text = t;
        start = s;
        end = e;

    }

    /**
     * 
     * @return start of tag
     */
    public int getStart() {
        return start;
    }

    /**
     * 
     * @return end of tag
     */
    public int getEnd() {
        return end;
    }

    /**
     * 
     * @return text of tag
     */
    public String getText() {
        return text;
    }
}
