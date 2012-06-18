/*
 * Simple html output mode
 * Can be used on scheduling because the output does not
 * depend on other files (css etc) and it is a standalone page
 */
package art.output;

import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;

/**
 * Simple html output mode.
 * Can be used on scheduling because the output does not
 * depend on other files (css etc) and it is a standalone page
 * 
 * @author Enrico Liboni
 */
public class htmlPlainOutput implements ArtOutputInterface {

    PrintWriter out;
    int numberOfLines;
    String queryName;
    String userName;
    int maxRows;
    int columns;
    NumberFormat nfPlain;
    boolean oddline = true;
    Map<Integer, ArtQueryParam> displayParams;

    /**
     * Constructor
     */
    public htmlPlainOutput() {
        numberOfLines = 0;
        nfPlain = NumberFormat.getInstance();
        nfPlain.setMinimumFractionDigits(0);
        nfPlain.setGroupingUsed(true);
        nfPlain.setMaximumFractionDigits(99);
    }

    @Override
    public String getFileName() {
        return null; // this output mode does not generate external files
    }

    @Override
    public String getName() {
        return "Browser (Plain)";
    }

    @Override
    public String getContentType() {
        return "text/html;charset=utf-8";
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
    public void setUserName(String s) {
        userName = s;
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
    public void setExportPath(String s) {
    }
   
    @Override
    public void setDisplayParameters(Map<Integer, ArtQueryParam> t) {
        displayParams = t;
    }

    @Override
    public void beginHeader() {
		out.println("<html>");
		out.println("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head>");        
		out.println("<body><div align=\"center\">");

        if (displayParams != null && displayParams.size()>0) {
            out.println("<table border=\"0\" width=\"90%\"><tr><td>");
            out.println("<div align=\"center\"> <small>");
            // decode the parameters handling multi one
            Iterator it = displayParams.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();                
                ArtQueryParam param=(ArtQueryParam)entry.getValue();
                String paramName=param.getName();
                Object pValue = param.getParamValue();

                if (pValue instanceof String) {
                    out.println(paramName + ":" + pValue + " <br> ");
                } else if (pValue instanceof String[]) { // multi
                    StringBuilder pValuesSb = new StringBuilder(256);
                    String[] pValues = (String[]) pValue;
                    for (int i = 0; i < pValues.length; i++) {
                        pValuesSb.append(pValues[i]);
                        pValuesSb.append(", ");
                    }
                    out.println(paramName + ": (" + pValuesSb.toString() + " )<br> ");
                }
            }                
            out.println("</small></div>");
            out.println("</td></tr></table>");
        }

        out.println("<style>table { background-color: #000000; }\ntd { background-color: #FFFFFF; font-size: 10pt; }\nbody { font-family: Verdana, Helvetica , Arial, SansSerif; color: #000000; }</style>");
        out.println("<table style=\"\" border=\"0\" width=\"90%\" cellspacing=\"1\" cellpadding=\"1\"><tr>"); // cellspacing=\"1\" cellpadding=\"0\"
    }

    @Override
    public void addHeaderCell(String s) {
        out.println("<td><b>" + s + "</b></td>");
    }

    @Override
    public void endHeader() {
        out.println("</tr>");
    }

    @Override
    public void beginLines() {
    }

    @Override
    public void addCellString(String s) {
        out.println(" <td>" + s + "</td>");
    }

    @Override
    public void addCellDouble(Double d) {
        String formattedValue = null;
        if (d != null) {
            formattedValue = nfPlain.format(d.doubleValue());
        }
        out.println(" <td>" + formattedValue + "</td>");
    }

    @Override
    public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
        out.println(" <td>" + i + "</td>");
    }

    @Override
    public void addCellDate(java.util.Date d) {
        out.println(" <td>" + d + "</td>");
    }

    @Override
    public boolean newLine() {
        numberOfLines++;
        if (numberOfLines % 2 == 0) {
            oddline = true;
        } else {
            oddline = false;
        }

        if (numberOfLines == 1) { // first row
            out.println(" <tr>");
        } else if (numberOfLines < maxRows) {
            out.println("</tr><tr>");
        } else {
            out.println("</tr></table></div></html>");
            return false;
        }

        return true;
    }

    @Override
    public void endLines() {
        out.println("</tr></table></div></body></html>");
    }

    @Override
    public boolean isDefaultHtmlHeaderAndFooterEnabled() {
        return false;
    }
}
