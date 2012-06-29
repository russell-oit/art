/*
 * Generate a RSS 2.0 output
 * the query column names MUST follow the RSS 2.0 naming convention for <item>
 * i.e. at least one of "title" or "description" must exists.
 * for other valid columns names (i.e. item sub-tags) refer to the RSS 2.0 specs.
 * (pubDate and guid should be there)
 * select col1 "title", col2 "description" [, col3 "pubDate", col4 "guid", ...] from ... 
 */
package art.output;

import art.servlets.ArtDBCP;
import art.servlets.XmlDataProvider;
import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 *  Generate RSS 2.0 output.
 * The query column names MUST follow the RSS 2.0 naming convention for &lt;item&gt;
 * i.e. at least one of "title" or "description" must exists.
 * for other valid columns names (i.e. item sub-tags) refer to the RSS 2.0 specs.
 * (pubDate and guid should be there) <br>
 * select col1 "title", col2 "description" [, col3 "pubDate", col4 "guid", ...] from ...
 * 
 * @author Enrico Liboni
 */
public class rss20Output implements ArtOutputInterface {

    PrintWriter out;
    int numberOfLines = 0;
    String queryName;    
    int maxRows;
    int columns;
    int columnIndex = 0; // current column
    String[] columnNames;
    Map<Integer, ArtQueryParam> displayParams;

    /**
     * Constructor
     */
    public rss20Output() {
    }

    @Override
    public String getFileName() {
        return null; // this output mode does not generate external files
    }

    @Override
    public String getName() {
        return "rss2.0"; // simple label
    }

    @Override
    public String getContentType() {
        return "application/xml"; // mime type (use "text/html" for html)
    }

    @Override
    public void setWriter(PrintWriter o) {
        out = o;
    }

    @Override
    public void setQueryName(String s) {
        queryName = s;
    }

    @Override
    public void setFileUserName(String s) {
        //not used
    }

    @Override
    public void setMaxRows(int i) {
        maxRows = i;
    }

    @Override
    public void setColumnsNumber(int i) {
        columns = i;
    }

    @Override
    public void setExportPath(String s) { // not needed as this output type does not generate a file
    }
   
    @Override
    public void setDisplayParameters(Map<Integer, ArtQueryParam> t) {
        displayParams = t;
    }
    /**
     * rfc822 (2822) standard date
     */
    public static SimpleDateFormat Rfc822DateFormat = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z");

    /**
     * 
     * @param date
     * @return rfc822 representation of date
     */
    public static String getDateAsRFC822String(Date date) {
        return Rfc822DateFormat.format(date);
    }

    @Override
    public void beginHeader() {
        columnNames = new String[columns]; // sotres columns names (i.e. xml tags for items)
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        //out.println("<!DOCTYPE table SYSTEM \"art/etc/artxml.dtd\"");

        // RSS 2.0 Header
        out.println("<rss version=\"2.0\">");
        // Channel definition

        out.println("<channel>");
        out.println("<title>" + queryName + "</title>");
        out.println("<link>" + ArtDBCP.getArtSetting("rss_link") + "</link>");
        out.println("<description>" + queryName + " ART Feed</description>");
        out.println("<pubDate>" + getDateAsRFC822String(new java.util.Date()) + "</pubDate> ");
        out.println("<generator>http://art.sourceforge.net</generator> ");

        // needs to be tested, this is not RSS 2.0 compliant
        // hence, do not check the show params flag if not for debugging
        if (displayParams != null && displayParams.size()>0) {
            out.println("<art:queryparams>");
            // decode the parameters handling multi one
            Iterator it = displayParams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                ArtQueryParam param=(ArtQueryParam)entry.getValue();
                String paramName=param.getName();
                Object pValue = param.getParamValue();
				String outputString;
                
                out.println("<art:param>");
                if (pValue instanceof String) {
					String paramValue = (String) pValue;
					outputString = "<art:name>" + XmlDataProvider.parseXml(paramName) + "</art:name><art:value>" + XmlDataProvider.parseXml(paramValue) + "</art:value> "; //default to displaying parameter value

					if (param.usesLov()) {
						//for lov parameters, show both parameter value and display string if any
						Map<String, String> lov = param.getLovValues();
						if (lov != null) {
							//get friendly/display string for this value
							String paramDisplayString = lov.get(paramValue);
							if (!StringUtils.equals(paramValue, paramDisplayString)) {
								//parameter value and display string differ. show both
								outputString = "<art:name>" + XmlDataProvider.parseXml(paramName) + "</art:name><art:value>" + XmlDataProvider.parseXml(paramDisplayString) + " (" + XmlDataProvider.parseXml(paramValue) + ")</art:value> ";
							}
						}
					}
					out.println(outputString);                    
                } else if (pValue instanceof String[]) { // multi
                    String[] paramValues = (String[]) pValue;
					outputString = "<art:name>" + XmlDataProvider.parseXml(paramName) + "</art:name><art:value>" + XmlDataProvider.parseXml(StringUtils.join(paramValues, ", ")) + "</art:value> "; //default to showing parameter values only

					if (param.usesLov()) {
						//for lov parameters, show both parameter value and display string if any
						Map<String, String> lov = param.getLovValues();
						if (lov != null) {
							//get friendly/display string for all the parameter values
							String[] paramDisplayStrings = new String[paramValues.length];
							for (int i = 0; i < paramValues.length; i++) {
								String value = paramValues[i];
								String display = lov.get(value);
								if (!StringUtils.equals(display, value)) {
									//parameter value and display string differ. show both
									paramDisplayStrings[i] = display + " (" + value + ")";
								} else {
									paramDisplayStrings[i] = value;
								}
							}
							outputString = "<art:name>" + XmlDataProvider.parseXml(paramName) + "</art:name><art:value>" + XmlDataProvider.parseXml(StringUtils.join(paramDisplayStrings, ", ")) + "</art:value> ";
						}
					}
					out.println(outputString);                    
                }
                out.println("</art:param>");
            }            
            out.println("</art:queryparams>");
        }

    }

    @Override
    public void addHeaderCell(String s) {
        columnNames[columnIndex] = s;
        columnIndex++;
    }

    @Override
    public void endHeader() {
        //columnIndex=0; // newLine does it
    }

    @Override
    public void beginLines() {
        out.println("<item>"); // see newLine
    }

    @Override
    public void addCellString(String s) {
        out.println("<" + columnNames[columnIndex] + ">" + art.servlets.XmlDataProvider.parseXml(s) + "</" + columnNames[columnIndex] + ">");
        columnIndex++;
    }

    @Override
    public void addCellDouble(Double d) {
        out.println("<" + columnNames[columnIndex] + ">" + d + "</" + columnNames[columnIndex] + ">");
        columnIndex++;
    }

    @Override
    public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
        out.println("<" + columnNames[columnIndex] + ">" + i + "</" + columnNames[columnIndex] + ">");
        columnIndex++;
    }

    @Override
    public void addCellDate(Date d) {
        out.println("<" + columnNames[columnIndex] + ">" + getDateAsRFC822String(d) + "</" + columnNames[columnIndex] + ">");
        columnIndex++;
    }

    @Override
    public boolean newLine() {
        numberOfLines++;
        columnIndex = 0; // reset column index

        if (numberOfLines == 1) { // first row
            //out.println(" <item>"); // do nothing beginLines already prints this
        } else if (numberOfLines < maxRows) {
            out.println("</item>\n<item>");
        } else {
            out.println("</item><item>");
            out.println("<title>Error: too many rows</title>");
            out.println("<description>the number of rows (" + numberOfLines + ") for this feed is above the threshold defined in ART. add parameters or increase the threshold</description>");
            endLines();

            return false; // ART will stop to feed the object if it returns false
        }

        return true;
    }

    @Override
    public void endLines() {
        out.println("</item>");
        out.println("</channel>");
        out.println("</rss>");
    }

    @Override
    public boolean isDefaultHtmlHeaderAndFooterEnabled() {
        return false; // if set to true, art will add standard html header&footer around the output
    }
}
