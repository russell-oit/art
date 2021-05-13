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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Generates tsv output
 *
 * @author Enrico Liboni
 * @author Timothy Anyona
 */
public class TsvOutput extends StandardOutput {

	private FileOutputStream fout;
	private ZipOutputStream zout;
	private GZIPOutputStream gzout;
	private StringBuilder sb;
	private final int FLUSH_SIZE = 1024 * 4; // flush to disk each 4kb of columns ;)
	private ZipType zipType = ZipType.None;
	private int localRowCount;
	private int currentColumnIndex;

	public TsvOutput() {

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
		sb = null;
		localRowCount = 0;
		currentColumnIndex = 0;
	}

	@Override
	public void init() {
		resetVariables();

		sb = new StringBuilder(8 * 1024);

		try {
			fout = new FileOutputStream(fullOutputFileName);

			if (zipType == ZipType.Zip) {
				String filename = FilenameUtils.getBaseName(fullOutputFileName);
				ZipEntry ze = new ZipEntry(filename + ".tsv");
				zout = new ZipOutputStream(fout);
				zout.putNextEntry(ze);
			} else if (zipType == ZipType.Gzip) {
				gzout = new GZIPOutputStream(fout);
			}
		} catch (IOException ex) {
			endOutput();
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (CollectionUtils.isEmpty(reportParamsList)) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			try {
				String labelAndDisplayValues = reportParam.getLocalizedLabelAndDisplayValues(locale);
				sb.append(labelAndDisplayValues);
			} catch (IOException ex) {
				endOutput();
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public void addHeaderCell(String value) {
		addColumnTab();
		sb.append(value);
	}

	@Override
	public void addCellString(String value) {
		addColumnTab();
		if (value == null) {
			sb.append(value);
		} else {
			sb.append(value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' '));
		}
	}

	@Override
	public void addCellNumeric(Double value) {
		addColumnTab();
		String formattedValue = formatNumericValuePlain(value);
		sb.append(formattedValue);
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		addColumnTab();
		sb.append(formattedValue);
	}

	@Override
	public void addCellDate(Date value) {
		addColumnTab();
		String formattedValue = formatDateValue(value);
		sb.append(formattedValue);
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		addColumnTab();
		sb.append(formattedValue);
	}

	@Override
	public void addCellDateTime(Date value) {
		addColumnTab();
		String formattedValue = formatDateTimeValue(value);
		sb.append(formattedValue);
	}

	@Override
	public void addCellDateTime(Date dateTimeValue, String formattedValue, long sortValue) {
		addColumnTab();
		sb.append(formattedValue);
	}

	@Override
	public void addCellTime(Date value) {
		addColumnTab();
		String formattedValue = Config.getTimeDisplayString(value);
		sb.append(formattedValue);
	}

	@Override
	public void addCellTime(Date timeValue, String formattedValue, long sortValue) {
		addColumnTab();
		sb.append(formattedValue);
	}

	@Override
	public void newRow() {
		localRowCount++;
		currentColumnIndex = 0;

		sb.append("\n");

		if ((localRowCount * totalColumnCount) > FLUSH_SIZE) {
			try {
				String tmpstr = sb.toString();
				byte[] buf = tmpstr.getBytes("UTF-8");

				if (zout == null) {
					fout.write(buf);
					fout.flush();
				} else {
					zout.write(buf);
					zout.flush();
				}

				sb = new StringBuilder(8 * 1024);
			} catch (IOException ex) {
				endOutput();
				throw new RuntimeException(ex);
			}
		}
	}

	@Override
	public void endOutput() {
//		addCellString("\n Total rows retrieved:");
//		addCellString("" + (counter));

		try {
			String tmpstr = sb.toString();
			byte[] buf = tmpstr.getBytes("UTF-8");

			switch (zipType) {
				case None:
					if (fout != null) {
						fout.write(buf);
						fout.flush();
					}
					break;
				case Zip:
					if (zout != null) {
						zout.write(buf);
						zout.flush();
						zout.close();
					}
					break;
				case Gzip:
					if (gzout != null) {
						gzout.write(buf);
						gzout.flush();
						gzout.close();
					}
					break;
				default:
					throw new IllegalArgumentException("Unexpected zip type: " + zipType);
			}

			if (fout != null) {
				fout.close();
			}

			fout = null; // these nulls are because it seems to be a memory leak in some JVMs
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Adds a tab to the current output and increments the column index counter
	 */
	private void addColumnTab() {
		currentColumnIndex++;
		if (currentColumnIndex > 1) {
			sb.append("\t");
		}
	}
}
