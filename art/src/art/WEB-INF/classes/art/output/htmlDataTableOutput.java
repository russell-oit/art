/*
 * JQuery DataTables html output mode
 */
package art.output;

import art.servlets.ArtDBCP;
import art.servlets.QueryExecute;
import art.utils.ArtQueryParam;
import java.io.File;
import java.io.PrintWriter;
import java.text.NumberFormat;
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
	private String language; //language to use for datatable e.g. en_US

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
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
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
		
		//default to english
		String languageSetting =""; // ", \"oLanguage\": {\"sUrl\": \"../js/dataTables.en.txt\"}";

		if (StringUtils.isNotBlank(language)) {
			String languageFileName = "dataTables." + language + ".txt";
			String sep = java.io.File.separator;
			String languageFilePath = ArtDBCP.getAppPath() + sep + "js" + sep + languageFileName;
			File languageFile = new File(languageFilePath);
			if (languageFile.exists()) {
				languageSetting = ", \"oLanguage\": {\"sUrl\": \"../js/" + languageFileName + "\"}";
			} 
		}

		//set table options. see http://www.datatables.net/ref
		String props = "{aaSorting: []"
				//+ ", \"sPaginationType\":\"full_numbers\""
				//+ ", \"bPaginate\": false"
				//+ ", \"sScrollY\": \"200px\""
				//+ ", \"bScrollCollapse\": true"
				+ languageSetting
				+ ", \"iDisplayLength\": 10" //default item in show entries
				+ ", \"aLengthMenu\": [[10, 25, 50, 100, -1], [10, 25, 50, 100, \"All\"]]" //show entries options
				+ "}";

		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/datatables.css\" /> ");
		out.println("<script type=\"text/javascript\" language=\"javascript\" src=\"../js/jquery.js\"></script>");
		out.println("<script type=\"text/javascript\" language=\"javascript\" src=\"../js/jquery.dataTables.min.js\"></script>");
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
		out.println("  <td> <span style=\"display:none;\">" + ArtDBCP.getDateSortString(d) + "</span>" + ArtDBCP.getDateDisplayString(d) + "</td>");
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
