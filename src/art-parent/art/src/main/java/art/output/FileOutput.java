/*
 * ART. A Reporting Tool.
 * Copyright (C) 2019 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.output;

import art.enums.ReportFormat;
import art.report.Report;
import art.runreport.GroovyDataDetails;
import art.runreport.RunReportHelper;
import art.utils.FilenameHelper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.owasp.encoder.Encode;

/**
 * Generates generic file output. Outputs the resultset as it is. Can be used
 * for queries which retrieve results as xml e.g. using the "for xml path"
 * statement in sql server.
 *
 * @author Timothy Anyona
 */
public class FileOutput {

	private ResultSet resultSet;
	private Object data;

	/**
	 * @return the resultSet
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}

	/**
	 * @param resultSet the resultSet to set
	 */
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Generates output for data in the given resultset
	 *
	 * @param writer the writer to output to. If html report format is required,
	 * a writer must be supplied
	 * @param report the report object for the report being run
	 * @param reportFormat the report format to use
	 * @param fullOutputFileName the output file name to use
	 * @return the number of rows in the output
	 * @throws java.lang.Exception
	 */
	public int generateOutput(PrintWriter writer, Report report,
			ReportFormat reportFormat, String fullOutputFileName) throws Exception {

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");

		int rowCount = 0;

		List<List<Object>> listData = null;
		int columnCount = 0;

		if (resultSet != null) {
			ResultSetMetaData rsmd = resultSet.getMetaData();
			columnCount = rsmd.getColumnCount();
		} else if (data != null) {
			GroovyDataDetails dataDetails = RunReportHelper.getGroovyDataDetails(data, report);
			listData = RunReportHelper.getListData(data);
			rowCount = dataDetails.getRowCount();
		}

		if (reportFormat.isHtml()) {
			writer.write("<pre>");
			if (resultSet != null) {
				while (resultSet.next()) {
					rowCount++;
					for (int i = 1; i <= columnCount; ++i) {
						String columnData = resultSet.getString(i);
						//https://stackoverflow.com/questions/49382257/display-raw-styled-xml-code-in-pre-tag
						//https://stackoverflow.com/questions/2820453/display-html-code-in-html
						String escapedData = Encode.forHtmlContent(columnData);
						writer.print(escapedData);
					}
					writer.println();
				}
			} else if (listData != null) {
				for (List<Object> row : listData) {
					for (Object columnData : row) {
						String columnString = String.valueOf(columnData);
						String escapedData = Encode.forHtmlContent(columnString);
						writer.print(escapedData);
					}
					writer.println();
				}
			}
			writer.write("</pre>");
		} else {
			try (FileOutputStream fout = new FileOutputStream(fullOutputFileName)) {
				if (reportFormat == ReportFormat.file) {
					if (resultSet != null) {
						rowCount = write(resultSet, columnCount, fout);
					} else if (listData != null) {
						write(listData, fout);
					}
				} else if (reportFormat == ReportFormat.fileZip) {
					String filename = FilenameUtils.getBaseName(fullOutputFileName);
					FilenameHelper filenameHelper = new FilenameHelper();
					String zipEntryFilenameExtension = filenameHelper.getFileReporFormatExtension(report);
					String zipEntryFilename = filename + "." + zipEntryFilenameExtension;
					ZipEntry ze = new ZipEntry(zipEntryFilename);
					try (ZipOutputStream zout = new ZipOutputStream(fout)) {
						zout.putNextEntry(ze);
						if (resultSet != null) {
							rowCount = write(resultSet, columnCount, zout);
						} else if (listData != null) {
							write(listData, zout);
						}
					}
				}
			}
		}

		return rowCount;
	}

	/**
	 * Writes the contents of a resultset to an output stream
	 *
	 * @param rs the resultset
	 * @param columnCount the number of columns in the resultset
	 * @param out the output stream
	 * @return the number of rows written
	 * @throws SQLException
	 * @throws IOException
	 */
	private int write(ResultSet rs, int columnCount, OutputStream out)
			throws SQLException, IOException {

		int rowCount = 0;
		while (rs.next()) {
			rowCount++;
			for (int i = 1; i <= columnCount; ++i) {
				String columnData = rs.getString(i);
				out.write(columnData.getBytes("UTF-8"));
			}
			out.write(System.lineSeparator().getBytes("UTF-8"));
		}

		return rowCount;
	}

	/**
	 * Writes the contents of a data list to an output stream
	 *
	 * @param listData the data list
	 * @param out the output stream
	 * @throws IOException
	 */
	private void write(List<List<Object>> listData, OutputStream out) throws IOException {
		for (List<Object> row : listData) {
			for (Object columnData : row) {
				String columnString = String.valueOf(columnData);
				out.write(columnString.getBytes("UTF-8"));
			}
			out.write(System.lineSeparator().getBytes("UTF-8"));
		}
	}

}
