/*
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
// This is an attempt to create a decent streamable file
// that is loaded both by Ooo and MS Excel
// "decent" means that a string like "00123" is not
// considered as the number 123
package art.output;

import art.enums.ZipType;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtQueryParam;
import art.utils.ArtUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create slk output.
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class SlkOutput extends StandardOutput {

	private static final Logger logger = LoggerFactory.getLogger(SlkOutput.class);
	FileOutputStream fout;
	ZipOutputStream zout;
	byte[] buf;
	String tmpstr;
	StringBuffer exportFileStrBuf;
	NumberFormat nfPlain;
	PrintWriter htmlout;
	String fileUserName;
	int maxRows;
	int row_count;
	int column_count;
	int columns;
	int counter;
	Map<Integer, ArtQueryParam> displayParams;
	final int FLUSH_SIZE = 1024 * 4; // flush to disk each 4kb of columns ;) 
	String exportPath;
	ZipType zipType;

	/**
	 * Constructor
	 */
	public SlkOutput() {
		zipType = ZipType.None;

	}

	public SlkOutput(ZipType zipType) {
		this.zipType = zipType;

	}

	/**
	 * Initialise objects required to generate output
	 */
	@Override
	public void init() {
		exportFileStrBuf = new StringBuffer(8 * 1024);
		counter = 0;
		nfPlain = NumberFormat.getInstance();
		nfPlain.setMinimumFractionDigits(0);
		nfPlain.setGroupingUsed(false);
		nfPlain.setMaximumFractionDigits(99);

		try {
			fout = new FileOutputStream(fullOutputFilename);
			
			String filename=FilenameUtils.getBaseName(fullOutputFilename);

			if (zipType == ZipType.Zip) {
				ZipEntry ze = new ZipEntry(filename + ".slk");
				zout = new ZipOutputStream(fout);
				zout.putNextEntry(ze);
			}
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void beginHeader() {
		// insert slk header
		// This is the Ooo header:
		exportFileStrBuf.append("ID;PSCALC3\n");
		row_count = 1;
		column_count = 1;

		exportFileStrBuf.append("C;Y").append(row_count++).append(";X1;K\"")
				.append(reportName).append(" - ")
				.append(ArtUtils.isoDateTimeFormatter.format(new Date()))
				.append("\"\n"); // first row Y1

	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			String paramName = reportParam.getParameter().getName();
			addHeaderCell(paramName);
			row_count++;
		}

		for (ReportParameter reportParam : reportParamsList) {
			addCellString(reportParam.getDisplayValues());
		}

		row_count++;
	}

	@Override
	public void addHeaderCell(String s) {
		exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K\"" + s + "\"\n");
	}

	@Override
	public void endHeader() {
	}

	@Override
	public void beginRows() {
		column_count = 1;
	}

	@Override
	public void addCellString(String value) {
		if (value == null) {
			exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K\"" + value + "\"\n");
		} else {
			if (value.trim().length() > 250) {
				value = value.substring(0, 250) + "[...]";
			}
			exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K\""
					+ value.replace('\n', ' ').replace('\r', ' ').replace(';', '-').trim() + "\"\n");
		}
	}

	@Override
	public void addCellNumeric(Double value) {
		if (value == null) {
			exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++ + ";K\"" + value + "\"\n");
		} else {
			exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++
					+ ";K" + nfPlain.format(value.doubleValue()) + "\n");
		}
	}

	@Override
	public void addCellDate(Date value) {
		exportFileStrBuf.append("C;Y" + row_count + ";X" + column_count++
				+ ";K\"" + Config.getDateDisplayString(value) + "\"\n");
	}

	@Override
	public void newRow() {
		column_count = 1;
		row_count++;

		counter++;
		if ((counter * columns) > FLUSH_SIZE) {
			try {
				tmpstr = exportFileStrBuf.toString();
				buf = new byte[tmpstr.length()];
				buf = tmpstr.getBytes("UTF-8");
				fout.write(buf);
				fout.flush();
				exportFileStrBuf = new StringBuffer(32 * 1024);
			} catch (IOException e) {
				logger.error("Error. Data not completed. Please narrow your search", e);

				//htmlout not used for scheduled jobs
				if (htmlout != null) {
					htmlout.println("<span style=\"color:red\">Error: " + e
							+ ")! Data not completed. Please narrow your search!</span>");
				}
			}
		}

//		if (counter < maxRows) {
//			return true;
//		} else {
//			addCellString("Maximum number of rows exceeded! Query not completed.");
//			endLines(); // close files
//			return false;
//		}
	}

	@Override
	public void endRows() {
//		newRow();
//		addCellString("Total rows retrieved:");
//		addCellNumeric(Double.valueOf(counter));
		exportFileStrBuf.append("E");

		try {
			tmpstr = exportFileStrBuf.toString();
			buf = new byte[tmpstr.length()];
			buf = tmpstr.getBytes("UTF-8");
			exportFileStrBuf = null;

			if (zout == null) {
				fout.write(buf);
				fout.flush();
			} else {
				zout.write(buf);
				zout.flush();
				zout.close();
				zout = null;
			}
			fout.close();
			fout = null; // these nulls are because it seems to be a memory leak in some JVMs

		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

}
