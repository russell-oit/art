//smileybits 20100215. create zipped tsv output
//adapted from gzipped tsv output class  tsvGzOutput.java
package art.output;

import art.servlets.ArtDBCP;
import art.utils.ArtQueryParam;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate tsv zip output
 */
public class tsvZipOutput implements ArtOutputInterface {

	final static Logger logger = LoggerFactory.getLogger(tsvZipOutput.class);
	FileOutputStream fout;
	ZipOutputStream zout;
	byte[] buf;
	String tmpstr;
	StringBuffer exportFileStrBuf;
	String filename;
	NumberFormat nfPlain;
	String fullFileName;
	PrintWriter htmlout;
	String queryName;
	String fileUserName;
	int maxRows;
	int counter;
	int columns;
	String y_m_d;
	String h_m_s;
	Map<Integer, ArtQueryParam> displayParams;
	final int FLUSH_SIZE = 1024 * 4; // flush to disk each 4kb of columns ;) 
	String exportPath;

	/**
	 * Constructor
	 */
	public tsvZipOutput() {
		exportFileStrBuf = new StringBuffer(8 * 1024);
		counter = 0;
		nfPlain = NumberFormat.getInstance();
		nfPlain.setMinimumFractionDigits(0);
		nfPlain.setGroupingUsed(false);
		nfPlain.setMaximumFractionDigits(99);
	}

	@Override
	public String getName() {
		return "Spreadsheet (Zipped tsv - text)";
	}

	@Override
	public String getContentType() {
		return "text/html;charset=utf-8";
	}

	@Override
	public void setExportPath(String s) {
		exportPath = s;
	}

	@Override
	public String getFileName() {
		return fullFileName;
	}

	@Override
	public void setWriter(PrintWriter o) {
		htmlout = o;
	}

	@Override
	public void setQueryName(String s) {
		queryName = s;
	}

	@Override
	public void setFileUserName(String s) {
		fileUserName = s;
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
	public void setDisplayParameters(Map<Integer, ArtQueryParam> t) {
		displayParams = t;
	}

	@Override
	public void beginHeader() {
		//initialize objects required for output
		initializeOutput();

		if (displayParams != null && !displayParams.isEmpty()) {
			exportFileStrBuf.append("Params:\t");
			// decode the parameters handling multi one
			for (Map.Entry<Integer, ArtQueryParam> entry : displayParams.entrySet()) {
				ArtQueryParam param = entry.getValue();
				String paramName = param.getName();
				Object pValue = param.getParamValue();
				String outputString;

				if (pValue instanceof String) {
					String paramValue = (String) pValue;
					outputString = paramName + "=" + paramValue + " \t "; //default to displaying parameter value

					if (param.usesLov()) {
						//for lov parameters, show both parameter value and display string if any
						Map<String, String> lov = param.getLovValues();
						if (lov != null) {
							//get friendly/display string for this value
							String paramDisplayString = lov.get(paramValue);
							if (!StringUtils.equals(paramValue, paramDisplayString)) {
								//parameter value and display string differ. show both
								outputString = paramName + "=" + paramDisplayString + " (" + paramValue + ") \t ";
							}
						}
					}
					exportFileStrBuf.append(outputString);
				} else if (pValue instanceof String[]) { // multi
					String[] paramValues = (String[]) pValue;
					outputString = paramName + "=" + StringUtils.join(paramValues, ", ") + " \t "; //default to showing parameter values only

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
							outputString = paramName + "=" + StringUtils.join(paramDisplayStrings, ", ") + " \t ";
						}
					}
					exportFileStrBuf.append(outputString);
				}
			}
			exportFileStrBuf.append("\n");
		}
	}

	@Override
	public void addHeaderCell(String s) {
		exportFileStrBuf.append(s).append("\t");
	}

	@Override
	public void addHeaderCellLeft(String s) {
		addHeaderCell(s);
	}

	@Override
	public void endHeader() {
	}

	@Override
	public void beginLines() {
	}

	@Override
	public void addCellString(String s) {
		if (s != null) {
			exportFileStrBuf.append(s.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ')).append("\t");
		} else {
			exportFileStrBuf.append(s).append("\t");
		}
	}

	@Override
	public void addCellDouble(Double d) {
		String formattedValue = "";
		if (d != null) {
			formattedValue = nfPlain.format(d.doubleValue());
		}
		exportFileStrBuf.append(formattedValue).append("\t");
	}

	@Override
	public void addCellLong(Long i) {       // used for INTEGER, TINYINT, SMALLINT, BIGINT
		exportFileStrBuf.append(i).append("\t");
	}

	@Override
	public void addCellDate(java.util.Date d) {
		exportFileStrBuf.append(ArtDBCP.getDateDisplayString(d)).append("\t");
	}

	@Override
	public boolean newLine() {
		exportFileStrBuf.append("\n");
		counter++;
		if ((counter * columns) > FLUSH_SIZE) {
			try {
				tmpstr = exportFileStrBuf.toString();
				buf = new byte[tmpstr.length()];
				buf = tmpstr.getBytes("UTF-8");
				zout.write(buf);
				zout.flush();
				exportFileStrBuf = new StringBuffer(32 * 1024);
			} catch (IOException e) {
				logger.error("Error. Data not completed. Please narrow your search", e);

				//htmlout not used for scheduled jobs
				if (htmlout != null) {
					htmlout.println("<span style=\"color:red\">IO Exception: " + e
							+ ")! Data not completed. Please narrow your search!</span>");
				}
			}
		}

		if (counter < maxRows) {
			return true;
		} else {
			addCellString("Maximum number of rows exceeded! Query not completed.");
			endLines(); // close files
			return false;
		}
	}

	@Override
	public void endLines() {
		addCellString("\n Total rows retrieved:");
		addCellString("" + (counter));

		try {
			tmpstr = exportFileStrBuf.toString();
			buf = new byte[tmpstr.length()];
			buf = tmpstr.getBytes("UTF-8");
			zout.write(buf);
			zout.flush();
			zout.close();
			fout.close();

			//htmlout not used for scheduled jobs
			if (htmlout != null) {
				htmlout.println("<p><div align=\"Center\"><table border=\"0\" width=\"90%\">");
				htmlout.println("<tr><td colspan=2 class=\"data\" align=\"center\" >"
						+ "<a  type=\"application/octet-stream\" href=\"../export/" + filename + ".zip\"> "
						+ filename + ".zip</a>"
						+ "</td></tr>");
				htmlout.println("</table></div></p>");
			}
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

	@Override
	public boolean isShowQueryHeaderAndFooter() {
		return true;
	}

	/**
	 * Build filename for output file
	 */
	private void buildOutputFileName() {
		// Build filename						
		Date today = new Date();

		String dateFormat = "yyyy_MM_dd";
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		y_m_d = dateFormatter.format(today);

		String timeFormat = "HH_mm_ss";
		SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
		h_m_s = timeFormatter.format(today);

		filename = fileUserName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ArtDBCP.getRandomString();
		filename = ArtDBCP.cleanFileName(filename); //replace characters that would make an invalid filename
		fullFileName = exportPath + filename + ".zip";
	}

	/**
	 * Initialise objects required to generate output
	 */
	private void initializeOutput() {
		buildOutputFileName();

		try {
			ZipEntry ze = new ZipEntry(filename + ".tsv");
			fout = new FileOutputStream(fullFileName);
			zout = new ZipOutputStream(fout);
			zout.putNextEntry(ze);
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}
}
