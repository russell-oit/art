/*
 * Grid html output mode
 */
package art.output;

import art.utils.ArtQueryParam;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

/**
 * Grid html output mode
 *
 * @author Enrico Liboni
 */
public class htmlGridOutput implements ArtOutputInterface {

	PrintWriter out;
	int numberOfLines;
	int maxRows;
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
		//not used
	}

	@Override
	public void setFileUserName(String s) {
		//not used. this output mode doesn't produce files
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
		// print parameters if they are available
		if (displayParams != null && displayParams.size() > 0) {
			out.println("<table border=\"0\" width=\"90%\"><tr><td>");
			out.println("<div id=\"param_div\" width=\"90%\" align=\"center\" class=\"qeparams\">");
			// decode the parameters handling multi ones
			Iterator it = displayParams.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				ArtQueryParam param = (ArtQueryParam) entry.getValue();
				String paramName = param.getName();
				Object pValue = param.getParamValue();
				String outputString;

				if (pValue instanceof String) {
					String paramValue = (String) pValue;
					outputString = paramName + ": " + paramValue + " <br> "; //default to displaying parameter value

					if (param.usesLov()) {
						//for lov parameters, show both parameter value and display string if any
						Map<String, String> lov = param.getLovValues();
						if (lov != null) {
							//get friendly/display string for this value
							String paramDisplayString = lov.get(paramValue);
							if (!StringUtils.equals(paramValue, paramDisplayString)) {
								//parameter value and display string differ. show both
								outputString = paramName + ": " + paramDisplayString + " (" + paramValue + ") <br> ";
							}
						}
					}
					out.println(outputString);
				} else if (pValue instanceof String[]) { // multi
					String[] paramValues = (String[]) pValue;
					outputString = paramName + ": " + StringUtils.join(paramValues, ", ") + " <br> "; //default to showing parameter values only

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
							outputString = paramName + ": " + StringUtils.join(paramDisplayStrings, ", ") + " <br> ";
						}
					}
					out.println(outputString);
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
		String tmp="<a href='#' class='sortheader' "+ 
        "onclick='ts_resortTable(this, "+0+");return false;'>" + 
        s+"<span class='sortarrow'>&nbsp;&nbsp;&nbsp;</span></a>";
		
		out.println("  <th class=\"header\">" + tmp + "</th>");
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
		out.println("  <td style=\"text-align: left\">" + s + "</td>");
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
		out.println("  <td style=\"text-align: left\">" + d + "</td>");
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
