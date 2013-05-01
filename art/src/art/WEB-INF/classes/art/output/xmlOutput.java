/*
 * Generate a simple XML output
 */
package art.output;

import art.servlets.ArtDBCP;
import art.servlets.XmlDataProvider;
import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * Generate xml output
 * 
 * @author Enrico Liboni
 */
public class xmlOutput implements ArtOutputInterface {

    PrintWriter out;
    int numberOfLines = 0;       
    int maxRows;    
    Map<Integer, ArtQueryParam> displayParams;

    /**
     * Constructor
     */
    public xmlOutput() {
    }

    /**
     * {@inheritDoc}
     * @return <code>null</code>. this output mode does not generate external files
     */
    @Override
    public String getFileName() {
        return null; // this output mode does not generate external files
    }

    @Override
    public String getName() {
        return "XML"; // simple label
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
        //not used
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
        //not used
    }

    @Override
    public void setExportPath(String s) { // not needed as this output type does not generate a file
    }

    @Override
    public void setDisplayParameters(Map<Integer, ArtQueryParam> t) {
        displayParams = t;
    }

    @Override
    public void beginHeader() {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        //out.println("<!DOCTYPE table SYSTEM \"art/etc/artxml.dtd\"");

        // simple DTD
        out.println("<!DOCTYPE table [");

        out.println("  <!ELEMENT table (queryparam,row*,totalrows)>");

        out.println("  <!ELEMENT queryparams (param*)>");
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

        out.println("<table>");

        if (displayParams != null && displayParams.size()>0) {
            out.println("<queryparams>");
            // decode the parameters handling multi one
            for (Map.Entry<Integer, ArtQueryParam> entry : displayParams.entrySet()) {
                ArtQueryParam param=entry.getValue();
                String paramName=param.getName();
                Object pValue = param.getParamValue();
				String outputString;

                out.println("<param>");
                if (pValue instanceof String) {
					String paramValue = (String) pValue;
					outputString = "<name>" + XmlDataProvider.parseXml(paramName) + "</name><value>" + XmlDataProvider.parseXml(paramValue) + "<value> "; //default to displaying parameter value

					if (param.usesLov()) {
						//for lov parameters, show both parameter value and display string if any
						Map<String, String> lov = param.getLovValues();
						if (lov != null) {
							//get friendly/display string for this value
							String paramDisplayString = lov.get(paramValue);
							if (!StringUtils.equals(paramValue, paramDisplayString)) {
								//parameter value and display string differ. show both
								outputString = "<name>" + XmlDataProvider.parseXml(paramName) + "</name><value>" + XmlDataProvider.parseXml(paramDisplayString) + " (" + XmlDataProvider.parseXml(paramValue) + ")</value> ";
							}
						}
					}
					out.println(outputString);                       
                } else if (pValue instanceof String[]) { // multi
                    String[] paramValues = (String[]) pValue;
					outputString = "<name>" + XmlDataProvider.parseXml(paramName) + "</name><value>" + XmlDataProvider.parseXml(StringUtils.join(paramValues, ", ")) + "</value> "; //default to showing parameter values only

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
							outputString = "<name>" + XmlDataProvider.parseXml(paramName) + "</name><value>" + XmlDataProvider.parseXml(StringUtils.join(paramDisplayStrings, ", ")) + "</value> ";
						}
					}
					out.println(outputString);                            
                }
                out.println("</param>");
            }
            out.println("</queryparams>");
        }
        out.println("<row>");
    }

    @Override
    public void addHeaderCell(String s) {
        out.println("<col type=\"header\">" + XmlDataProvider.parseXml(s) + "</col>");
    }
	
	@Override
	public void addHeaderCellLeft(String s) {
		addHeaderCell(s);
	}

    @Override
    public void endHeader() {
        out.println("</row>");
    }

    @Override
    public void beginLines() {
    }

    @Override
    public void addCellString(String s) {
        out.println("<col type=\"string\">" + XmlDataProvider.parseXml(s) + "</col>");
    }

    @Override
    public void addCellDouble(Double d) {
        out.println("<col type=\"double\">" + d + "</col>");
    }

    @Override
    public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
        out.println("<col type=\"int\">" + i + "</col>");
    }

    @Override
    public void addCellDate(Date d) {
        out.println("<col type=\"date\">" + ArtDBCP.getDateDisplayString(d) + "</col>");
    }

    @Override
    public boolean newLine() {
        numberOfLines++;

        if (numberOfLines == 1) { // first row
            out.println(" <row>");
        } else if (numberOfLines < maxRows) {
            out.println("</row><row>");
        } else {
            out.println("</row><row>");
            out.println("<col type=\"error\">Too many rows</col>");
            out.println("</row></table>");
            return false; // ART will stop to feed the object if it returns false
        }

        return true;
    }

    @Override
    public void endLines() {
        out.println("</row>");
        out.println("<totalrows>" + numberOfLines + "</totalrows>");
        out.println("</table>");
    }

    @Override
    public boolean isShowQueryHeaderAndFooter() {
        return false; // if set to true, art will add a standard html header&footer around the output
    }
}