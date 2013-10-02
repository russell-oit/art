/**
 * Copyright 2001-2013 Enrico Liboni <eliboni@users.sourceforge.net>
 *
 * This file is part of ART.
 *
 * ART is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * ART is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ART.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * JQuery DataTables html output mode
 */
package art.output;

import art.servlets.ArtConfig;
import art.utils.ArtQueryParam;
import art.utils.ArtUtils;
import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JQuery DataTables html output mode
 *
 * @author Enrico Liboni
 */
public class htmlDataTableOutput implements ArtOutputInterface {

	final static Logger logger = LoggerFactory.getLogger(htmlDataTableOutput.class);
	PrintWriter out;
	int numberOfLines;
	int maxRows;
	NumberFormat nfPlain;
	boolean oddline = true;
	Map<Integer, ArtQueryParam> displayParams;
	String tableId; // random identifier
	DecimalFormat nfSort;
	private Locale locale; //locale to use for datatable. determines language used to display datatable strings

	/**
	 * Constructor
	 */
	public htmlDataTableOutput() {
		numberOfLines = 0;
		nfPlain = NumberFormat.getInstance();
		nfPlain.setMinimumFractionDigits(0);
		nfPlain.setGroupingUsed(true);
		nfPlain.setMaximumFractionDigits(99);
		tableId = "Tid" + Long.toHexString(Double.doubleToLongBits(Math.random()));
		
		nfSort=new DecimalFormat("#.#");
		nfSort.setMinimumIntegerDigits(20); //ensure all numbers are pre-padded with zeros so that sorting works correctly
		nfSort.setMaximumFractionDigits(99);
		
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public String getFileName() {
		return null; // this output mode does not generate external files
	}

	@Override
	public String getName() {
		return "Browser (DataTable)";
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
		/*
		 * Code for datatables
		 */

		//set language file to use for localization. language files to be put in the /js directory and to be named dataTables.xx_XX.txt	
		//language file content examples at http://datatables.net/plug-ins/i18n
		
		//by default don't set the language file option. (will default to english - in jquery.dataTables.min.js)
		String languageSetting =""; 
		
		String language="";
		if(locale!=null){
			language=locale.toString(); //e.g. en, en-us, it, fr etc
		}

		if (StringUtils.isNotBlank(language)) {
			String languageFileName = "dataTables." + language + ".txt";
			String sep = java.io.File.separator;
			String languageFilePath = ArtConfig.getAppPath() + sep + "js" + sep + languageFileName;
			File languageFile = new File(languageFilePath);
			if (languageFile.exists()) {
				languageSetting = ", \"oLanguage\": {\"sUrl\": \"../js/" + languageFileName + "\"}";
			} 
		}

		//set table options. see http://www.datatables.net/ref
		String props = "{aaSorting: []"
				+ ", \"sPaginationType\":\"full_numbers\""
				//+ ", \"bPaginate\": false"
				//+ ", \"sScrollY\": \"200px\""
				//+ ", \"bScrollCollapse\": true"
				//+ ", \"bProcessing\": true"
//				+ ", \"bJQueryUI\": true"
				+ languageSetting
				+ ", \"iDisplayLength\": 50" //default item in show entries e.g. -1
				+ ", \"aLengthMenu\": [[10, 25, 50, 100, -1], [10, 25, 50, 100, \"All\"]]" //show entries options
				+ "}";

		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/dataTables_demo_table.css\" /> ");
		out.println("<script type=\"text/javascript\" src=\"../js/jquery-1.6.2.min.js\"></script>");
		out.println("<script type=\"text/javascript\" src=\"../js/jquery.dataTables.min.js\"></script>");
		//enable use of jquery ui theme. needs theme css, jquery.js, jquery-ui.js
//		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/smoothness/jquery-ui-1.10.3.custom.css\" /> ");
//		out.println("<script type=\"text/javascript\" src=\"../js/jquery-ui-10.3.custom.min.js\"></script>");
		//
		out.println("<script type=\"text/javascript\" charset=\"utf-8\">");
		out.println("	var $jQuery = jQuery.noConflict();");
		out.println("	$jQuery(document).ready(function() {");
		out.println("		$jQuery('#" + tableId + "').dataTable(" + props + ");");
		out.println("	} );");
		out.println("</script>	");

		//display parameters
		ArtOutHandler.displayParameters(out, displayParams);

		//start results table
		out.println("<div style=\"border: 1px solid black; width:80%; margin 0 auto\"><table class=\"display\" id=\"" + tableId + "\">");
		out.println(" <thead><tr>");
	}

	@Override
	public void addHeaderCell(String s) {
		out.println("  <th>" + s + "</th>");
	}
	
	@Override
	public void addHeaderCellLeft(String s) {
		out.println("  <th style=\"text-align: left\">" + s + "</th>");
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
		String sortValue=null;
		if (d != null) {
			formattedValue = nfPlain.format(d.doubleValue());
			sortValue = nfSort.format(d.doubleValue());
		}
		//display value in invisible span so that sorting can work correctly when there are numbers with the thousand separator e.g. 1,000
		out.println("  <td align=\"right\"> <span style=\"display:none;\">" + sortValue + "</span>" + formattedValue + "</td>");
	}

	@Override
	public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
		String formattedValue = null;
		String sortValue=null;
		if (i != null) {
			formattedValue = nfPlain.format(i.longValue());
			sortValue = nfSort.format(i.longValue());
		}
		out.println("  <td align=\"right\"> <span style=\"display:none;\">" + sortValue + "</span>" + formattedValue + "</td>");
	}

	@Override
	public void addCellDate(java.util.Date d) {
		out.println("  <td style=\"text-align: left\"> <span style=\"display:none;\">" + ArtUtils.getDateSortString(d) + "</span>" + ArtConfig.getDateDisplayString(d) + "</td>");
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
			out.println(" </tr>");
			out.println(" <tr>");
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
	public boolean isShowQueryHeaderAndFooter() {
		return true;
	}
}
