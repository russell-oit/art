/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;

// This is an attempt to create a decent streamable file
// that is loaded both by Ooo and MS Excel
// "decent" means that a string like "00123" is not
// considered as the number 123
/**
 * Generates slk output
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class SlkOutput extends StandardOutput {

	private FileOutputStream fout;
	private ZipOutputStream zout;
	private StringBuilder exportFileStrBuf;
	private NumberFormat nfPlain;
	private int localRowCount;
	private int columnCount;
	private int columns;
	private int counter;
	private final int FLUSH_SIZE = 1024 * 4; // flush to disk each 4kb of columns ;) 
	private final ZipType zipType;

	public SlkOutput() {
		zipType = ZipType.None;
	}

	public SlkOutput(ZipType zipType) {
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
		exportFileStrBuf = null;
		nfPlain = null;
		localRowCount = 0;
		columnCount = 0;
		columns = 0;
		counter = 0;
	}

	@Override
	public void init() {
		resetVariables();

		exportFileStrBuf = new StringBuilder(8 * 1024);
		// insert slk header
		// This is the Ooo header:
		exportFileStrBuf.append("ID;PSCALC3\n");

		nfPlain = NumberFormat.getInstance(locale);
		nfPlain.setMinimumFractionDigits(0);
		nfPlain.setGroupingUsed(false);
		nfPlain.setMaximumFractionDigits(99);

		try {
			fout = new FileOutputStream(fullOutputFileName);

			String filename = FilenameUtils.getBaseName(fullOutputFileName);

			if (zipType == ZipType.Zip) {
				ZipEntry ze = new ZipEntry(filename + ".slk");
				zout = new ZipOutputStream(fout);
				zout.putNextEntry(ze);
			}
		} catch (IOException ex) {
			endOutput();
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addTitle() {
		if (report.isOmitTitleRow()) {
			return;
		}
		
		newRow();

		exportFileStrBuf.append("C;Y").append(localRowCount++).append(";X1;K\"")
				.append(reportName).append(" - ")
				.append(ArtUtils.isoDateTimeSecondsFormatter.format(new Date()))
				.append("\"\n"); // first row Y1
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (CollectionUtils.isEmpty(reportParamsList)) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			try {
				newRow();
				String paramLabel = reportParam.getParameter().getLocalizedLabel(locale);
				String paramDisplayValues = reportParam.getDisplayValues();
				addHeaderCell(paramLabel);
				addCellString(paramDisplayValues);
			} catch (IOException ex) {
				endOutput();
				throw new RuntimeException(ex);
			}
		}

		newRow();
	}

	@Override
	public void beginHeader() {
		newRow();
	}

	@Override
	public void addHeaderCell(String s) {
		exportFileStrBuf.append("C;Y").append(localRowCount).append(";X")
				.append(columnCount++).append(";K\"").append(s).append("\"\n");
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
			exportFileStrBuf.append("C;Y").append(localRowCount).append(";X")
					.append(columnCount++).append(";K\"").append(value).append("\"\n");
		} else {
			if (value.trim().length() > 250) {
				value = value.substring(0, 250) + "[...]";
			}
			exportFileStrBuf.append("C;Y").append(localRowCount).append(";X")
					.append(columnCount++).append(";K\"")
					.append(value.replace('\n', ' ').replace('\r', ' ').replace(';', '-').trim()).append("\"\n");
		}
	}

	@Override
	public void addCellNumeric(Double value) {
		if (value == null) {
			exportFileStrBuf.append("C;Y").append(localRowCount).append(";X")
					.append(columnCount++).append(";K\"").append(value).append("\"\n");
		} else {
			exportFileStrBuf.append("C;Y").append(localRowCount).append(";X")
					.append(columnCount++).append(";K")
					.append(nfPlain.format(value.doubleValue())).append("\n");
		}
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		exportFileStrBuf.append("C;Y").append(localRowCount).append(";X")
				.append(columnCount++).append(";K\"").append(formattedValue).append("\"\n");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = Config.getDateDisplayString(value);

		exportFileStrBuf.append("C;Y").append(localRowCount).append(";X")
				.append(columnCount++).append(";K\"")
				.append(formattedValue).append("\"\n");
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		exportFileStrBuf.append("C;Y").append(localRowCount).append(";X")
				.append(columnCount++).append(";K\"")
				.append(formattedValue).append("\"\n");
	}

	@Override
	public void newRow() {
		columnCount = 1;
		localRowCount++;

		counter++;
		if ((counter * columns) > FLUSH_SIZE) {
			try {
				String tmpstr = exportFileStrBuf.toString();
				byte[] buf = tmpstr.getBytes("UTF-8");
				fout.write(buf);
				fout.flush();
				exportFileStrBuf = new StringBuilder(32 * 1024);
			} catch (IOException ex) {
				endOutput();
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public void endOutput() {
//		newRow();
//		addCellString("Total rows retrieved:");
//		addCellNumeric(Double.valueOf(counter));
		exportFileStrBuf.append("E");

		try {
			String tmpstr = exportFileStrBuf.toString();
//			byte[] buf = new byte[tmpstr.length()];
			byte[] buf = tmpstr.getBytes("UTF-8");
			exportFileStrBuf = null;

			if (zout == null) {
				if (fout != null) {
					fout.write(buf);
					fout.flush();
				}
			} else {
				zout.write(buf);
				zout.flush();
				zout.close();
				zout = null;
			}

			if (fout != null) {
				fout.close();
			}

			fout = null; // these nulls are because it seems to be a memory leak in some JVMs
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
