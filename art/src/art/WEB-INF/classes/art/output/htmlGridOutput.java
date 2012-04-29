/*
 * Grid html output mode
 */
package art.output;

import art.utils.ArtQueryParam;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Grid html output mode
 * 
 * @author Enrico Liboni
 */
public class htmlGridOutput implements ArtOutputInterface {

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
    public htmlGridOutput() {
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
        return "Browser (Grid)";
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
        out.println("  <script type='text/javascript' src='../js/gridtable.js'></script>");

        // print parameters if they are available
        if (displayParams != null && displayParams.size()>0) {
            out.println("<table border=\"0\" width=\"90%\"><tr><td>");
            out.println("<div id=\"param_div\" width=\"90%\" align=\"center\" class=\"qeparams\">");
            // decode the parameters handling multi ones
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
            out.println("</div>");
            out.println("</td></tr></table>");
        }

        out.println("<div style=\"border: 3px solid white\"><table class=\"sortable\" id=\"maintable\" cellpadding=\"2\" cellspacing=\"0\" width=\"80%\">");
        out.println(" <thead><tr>");
    }

    @Override
    public void addHeaderCell(String s) {
        out.println("  <th class=\"header\">" + s + "</th>");
    }

    @Override
    public void endHeader() {
        out.println(" </tr></thead>");

    }

    @Override
    public void beginLines() {
        out.println("<tbody>");
    }

    @Override
    public void addCellString(String s) {
        out.println("  <td>" + s + "</td>");
    }

    @Override
    public void addCellDouble(Double d) {
        String formattedValue = null;
        if (d != null) {
            formattedValue = nfPlain.format(d.doubleValue());
        }
        out.println("  <td align=\"right\">" + formattedValue + "</td>");
    }

    @Override
    public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
        out.println("  <td align=\"right\">" + i + "</td>");
    }

    @Override
    public void addCellDate(java.util.Date d) {                     
        out.println("  <td>" + d + "</td>");
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
            out.println(" <tr class=\"rows\" onclick=\"javascript:selectRow(this)\" ondblclick=\"javascript:selectRow2(this)\" onmouseover=\"javascript:highLight(this,'hiliterows')\" onmouseout=\"javascript:highLight(this,'rows')\">");
        } else if (numberOfLines < maxRows) {
            out.println(" </tr>");
            out.println(" <tr class=\"rows\" onclick=\"javascript:selectRow(this)\" ondblclick=\"javascript:selectRow2(this)\" onmouseover=\"javascript:highLight(this,'hiliterows')\" onmouseout=\"javascript:highLight(this,'rows')\">");
        } else {
            out.println("</tr></table></div>");
            return false;
        }

        return true;
    }

    @Override
    public void endLines() {
        out.println(" </tr></tbody></table></div>");
    }

    @Override
    public boolean isDefaultHtmlHeaderAndFooterEnabled() {
        return true;
    }
}
