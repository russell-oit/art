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
	private StringBuilder sb;
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
		sb = null;
	}

	@Override
	public void init() {
		resetVariables();

		sb = new StringBuilder(8 * 1024);

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
		} catch (IOException ex) {
			logger.error("Error", ex);
		}
	}

	@Override
	public void addSelectedParameters(List<ReportParameter> reportParamsList) {
		if (CollectionUtils.isEmpty(reportParamsList)) {
			return;
		}

		for (ReportParameter reportParam : reportParamsList) {
			sb.append(reportParam.getNameAndDisplayValues());
		}
	}

	@Override
	public void addHeaderCell(String value) {
		sb.append(value);
		sb.append("\t");
	}

	@Override
	public void addCellString(String value) {
		if (value == null) {
			sb.append(value);
			sb.append("\t");
		} else {
			sb.append(value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' '));
			sb.append("\t");

		}
	}

	@Override
	public void addCellNumeric(Double value) {
		String formattedValue;
		if (value == null) {
			formattedValue = "";
		} else {
			formattedValue = plainNumberFormatter.format(value.doubleValue());
		}

		sb.append(formattedValue).append("\t");
	}

	@Override
	public void addCellNumeric(Double numericValue, String formattedValue, String sortValue) {
		sb.append(formattedValue).append("\t");
	}

	@Override
	public void addCellDate(Date value) {
		String formattedValue = Config.getDateDisplayString(value);
		sb.append(formattedValue).append("\t");
	}

	@Override
	public void addCellDate(Date dateValue, String formattedValue, long sortValue) {
		sb.append(formattedValue).append("\t");
	}

	@Override
	public void newRow() {
		sb.append("\n");
		if ((rowCount * totalColumnCount) > FLUSH_SIZE) {
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
				logger.error("Error", ex);
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
