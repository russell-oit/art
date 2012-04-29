/** 
 * This object is used by the PreparedQuery/XmlParser
 *  to maintain the text in an xml tag as
 *  well as its initial and end position in the SQL query
 *  string
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
