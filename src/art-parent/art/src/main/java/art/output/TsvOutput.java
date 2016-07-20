/*
 * Copyright 2001-2016 Enrico Liboni <eliboni@users.sourceforge.net>
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates tsv output
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class TsvOutput extends StandardOutput {

	private static final Logger logger = LoggerFactory.getLogger(TsvOutput.class);

	private FileOutputStream fout;
	private ZipOutputStream zout;
	private GZIPOutputStream gzout;
	private StringBuilder exportFileStrBuf;
	private NumberFormat nfPlain;
	private int counter;
	private int columns;
	private final int FLUSH_SIZE = 1024 * 4; // flush to disk each 4kb of columns ;)
	private final ZipType zipType;

	public TsvOutput() {
		zipType = ZipType.None;
	}

	public TsvOutput(ZipType zipType) {
		this.zipType = zipType;
	}

	/**
	 * Resets global variables in readiness for output generation. Especially
	 * important for burst output where the same standard output object is
	 * reused for multiple output runs.
	 */
	private void resetVariables() {
		fout = null;
		zout = null;
		gzout = null;
		exportFileStrBuf = null;
		nfPlain = null;
		counter = 0;
		columns = 0;
	}

	@Override
	public void init() {
		resetVariables();
		
		exportFileStrBuf = new StringBuilder(8 * 1024);
		
		nfPlain = NumberFormat.getInstance(locale);
		nfPlain.setMinimumFractionDigits(0);
		nfPlain.setGroupingUsed(false);
		nfPlain.setMaximumFractionDigits(99);

		try {
			fout = new FileOutputStream(fullOutputFileName);

			String filename = FilenameUtils.getBaseName(fullOutputFileName);

			if (zipType == ZipType.Zip) {
				ZipEntry ze = new ZipEntry(filename + ".tsv");
				zout = new ZipOutputStream(fout);
				zout.putNextEntry(ze);
			} else if (zipType == ZipType.Gzip) {
				gzout = new GZIPOutputStream(fout);
			}
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (reportParamsList == null || reportParamsList.isEmpty()) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			exportFileStrBuf.append(reportParam.getNameAndDisplayValues());
		}
	}

	@Override
	public void addHeaderCell(String value) {
		exportFileStrBuf.append(value);
		exportFileStrBuf.append("\t");
	}

	@Override
	public void addCellString(String value) {
		if (value == null) {
			exportFileStrBuf.append(value);
			exportFileStrBuf.append("\t");
		} else {
			exportFileStrBuf.append(value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' '));
			exportFileStrBuf.append("\t");

		}
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue;
		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = nfPlain.format(value.doubleValue());
		}

		exportFileStrBuf.append(formattedValue).append("\t");
	}
	
	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		exportFileStrBuf.append(formattedValue).append("\t");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue=Config.getDateDisplayString(value);
		
		exportFileStrBuf.append(formattedValue).append("\t");
	}
	
	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		exportFileStrBuf.append(formattedValue).append("\t");
	}

	@Override
	public void newRow() {
		exportFileStrBuf.append("\n");
		counter++;
		if ((counter * columns) > FLUSH_SIZE) {
			try {
				String tmpstr = exportFileStrBuf.toString();
				byte[] buf = tmpstr.getBytes("UTF-8");

				if (zout == null) {
					fout.write(buf);
					fout.flush();
				} else {
					zout.write(buf);
					zout.flush();
				}

				exportFileStrBuf = new StringBuilder(32 * 1024);
			} catch (IOException e) {
				logger.error("Error. Data not completed. Please narrow your search", e);
			}
		}
	}

	@Override
	public void endOutput() {
//		addCellString("\n Total rows retrieved:");
//		addCellString("" + (counter));

		try {
			String tmpstr = exportFileStrBuf.toString();
			byte[] buf = tmpstr.getBytes("UTF-8");

			switch (zipType) {
				case None:
					fout.write(buf);
					fout.flush();
					break;
				case Zip:
					zout.write(buf);
					zout.flush();
					zout.close();
					break;
				case Gzip:
					gzout.write(buf);
					gzout.flush();
					gzout.close();
					break;
				default:
					throw new IllegalArgumentException("Unexpected zip type: " + zipType);
			}

			fout.close();
			fout = null; // these nulls are because it seems to be a memory leak in some JVMs
		} catch (IOException e) {
			logger.error("Error", e);
		}
	}
}
