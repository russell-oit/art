/*
 * Simple html output mode
 * Can be used on scheduling because the output does not
 * depend on other files (css etc) and it is a standalone page
 */
package art.output;

import art.servlets.ArtDBCP;
import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.text.NumberFormat;
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
    int maxRows;    
    NumberFormat nfPlain;
    Map<Integer, ArtQueryParam> displayParams;
	private boolean displayInline=false; //whether display is inline in the showparams page. to avoid duplicate display of parameters
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

	/**
	 * @return the displayInline
	 */
	public boolean isDisplayInline() {
		return displayInline;
	}

	/**
	 * @param displayInline the displayInline to set
	 */
	public void setDisplayInline(boolean displayInline) {
		this.displayInline = displayInline;
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
        //not used
    }

    @Override
    public void setFileUserName(String s) {
        //not used. this output mode doesn't produce files by itself
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
    public void setExportPath(String s) {
    }
   
    @Override
    public void setDisplayParameters(Map<Integer, ArtQueryParam> t) {
        displayParams = t;
    }

    @Override
    public void beginHeader() {
		out.println("<html>");
		out.println("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		out.println("</head>");        
		out.println("<body>");
		
		//style should be in the head section. put in body for correct display in email inline jobs
		out.println("<style>table { border-collapse: collapse; }\n td { background-color: #FFFFFF; border: 1px solid #000000; font-size: 10pt; }\nbody { font-family: Verdana, Helvetica , Arial, SansSerif; color: #000000; }</style>");
		
		if(!displayInline){
			//display parameters
			ArtOutHandler.displayParameters(out, displayParams);
		}
		
		//start results table
        out.println("<div align=\"center\"><table class=\"htmlplain\" border=\"0\" width=\"90%\" cellspacing=\"1\" cellpadding=\"1\"><tr>"); 
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
		String formattedValue = null;
        if (i != null) {
            formattedValue = nfPlain.format(i.longValue());
        }
        out.println(" <td>" + formattedValue + "</td>");
    }

    @Override
    public void addCellDate(java.util.Date d) {
        out.println(" <td>" + ArtDBCP.getDateDisplayString(d) + "</td>");
    }

    @Override
    public boolean newLine() {
        numberOfLines++;

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
    public boolean isShowQueryHeaderAndFooter() {
        return false;
    }
}
