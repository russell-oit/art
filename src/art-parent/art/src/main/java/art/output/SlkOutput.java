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
package art.output;

import art.enums.ZipType;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This is an attempt to create a decent streamable file
// that is loaded both by Ooo and MS Excel
// "decent" means that a string like "00123" is not
// considered as the number 123
/**
 * Create slk output.
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class SlkOutput extends StandardOutput {

	private static final Logger logger = LoggerFactory.getLogger(SlkOutput.class);
	private FileOutputStream fout;
	private ZipOutputStream zout;
	private StringBuffer exportFileStrBuf;
	private NumberFormat nfPlain;
	private int localRowCount;
	private int columnCount;
	private int columns;
	private int counter;
	private final int FLUSH_SIZE = 1024 * 4; // flush to disk each 4kb of columns ;) 
	private ZipType zipType;

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

			String filename = FilenameUtils.getBaseName(fullOutputFilename);

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
		localRowCount = 1;
		columnCount = 1;

		exportFileStrBuf.append("C;Y").append(localRowCount++).append(";X1;K\"")
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
			localRowCount++;
		}

		for (ReportParameter reportParam : reportParamsList) {
			addCellString(reportParam.getDisplayValues());
		}

		localRowCount++;
	}

	@Override
	public void addHeaderCell(String s) {
		exportFileStrBuf.append("C;Y" + localRowCount + ";X" + columnCount++ + ";K\"" + s + "\"\n");
	}

	@Override
	public void endHeader() {
	}

	@Override
	public void beginRows() {
		columnCount = 1;
	}

	@Override
	public void addCellString(String value) {
		if (value == null) {
			exportFileStrBuf.append("C;Y" + localRowCount + ";X" + columnCount++ + ";K\"" + value + "\"\n");
		} else {
			if (value.trim().length() > 250) {
				value = value.substring(0, 250) + "[...]";
			}
			exportFileStrBuf.append("C;Y" + localRowCount + ";X" + columnCount++ + ";K\""
					+ value.replace('\n', ' ').replace('\r', ' ').replace(';', '-').trim() + "\"\n");
		}
	}

	@Override
	public void addCellNumeric(Double value) {
		if (value == null) {
			exportFileStrBuf.append("C;Y" + localRowCount + ";X" + columnCount++ + ";K\"" + value + "\"\n");
		} else {
			exportFileStrBuf.append("C;Y" + localRowCount + ";X" + columnCount++
					+ ";K" + nfPlain.format(value.doubleValue()) + "\n");
		}
	}

	@Override
	public void addCellDate(Date value) {
		exportFileStrBuf.append("C;Y" + localRowCount + ";X" + columnCount++
				+ ";K\"" + Config.getDateDisplayString(value) + "\"\n");
	}

	@Override
	public void newRow() {
		columnCount = 1;
		localRowCount++;

		counter++;
		if ((counter * columns) > FLUSH_SIZE) {
			try {
				String tmpstr = exportFileStrBuf.toString();
				byte[] buf = new byte[tmpstr.length()];
				buf = tmpstr.getBytes("UTF-8");
				fout.write(buf);
				fout.flush();
				exportFileStrBuf = new StringBuffer(32 * 1024);
			} catch (IOException e) {
				logger.error("Error. Data not completed. Please narrow your search", e);
			}
		}
	}

	@Override
	public void endRows() {
//		newRow();
//		addCellString("Total rows retrieved:");
//		addCellNumeric(Double.valueOf(counter));
		exportFileStrBuf.append("E");

		try {
			String tmpstr = exportFileStrBuf.toString();
			byte[] buf = new byte[tmpstr.length()];
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
