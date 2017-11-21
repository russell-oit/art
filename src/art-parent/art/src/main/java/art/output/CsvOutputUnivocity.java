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

import art.enums.ReportFormat;
import art.report.Report;
import art.reportoptions.CsvOutputUnivocityOptions;
import art.utils.FilenameHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.univocity.parsers.common.processor.ObjectRowWriterProcessor;
import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvRoutines;
import com.univocity.parsers.csv.CsvWriterSettings;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates csv output using the univocity-parsers library. Also supports
 * generating the file with another delimiter, other than comma.
 *
 * @author Timothy Anyona
 */
public class CsvOutputUnivocity {
	//https://github.com/uniVocity/univocity-parsers/blob/master/src/test/java/com/univocity/parsers/examples/RoutineExamples.java
	//http://www.univocity.com/blogs/news
	//https://github.com/uniVocity/univocity-parsers/issues/28
	//https://github.com/uniVocity/csv-parsers-comparison
	//http://www.univocity.com/pages/univocity-tutorial

	private static final Logger logger = LoggerFactory.getLogger(CsvOutputUnivocity.class);

	/**
	 * Generates fixed width output for data in the given resultset
	 *
	 * @param rs the resultset that contains the data to output
	 * @param writer the writer to output to. If html report format is required,
	 * a writer must be supplied
	 * @param report the report object for the report being run
	 * @param reportFormat the report format to use
	 * @param fullOutputFileName the output file name to use
	 * @param locale the locale that determines date format output
	 * @throws java.io.IOException
	 */
	public void generateOutput(ResultSet rs, PrintWriter writer, Report report,
			ReportFormat reportFormat, String fullOutputFileName,
			Locale locale) throws IOException {

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");

		CsvOutputUnivocityOptions csvOptions;
		String options = report.getOptions();
		if (StringUtils.isBlank(options)) {
			csvOptions = new CsvOutputUnivocityOptions();
		} else {
			ObjectMapper mapper = new ObjectMapper();
			csvOptions = mapper.readValue(options, CsvOutputUnivocityOptions.class);
		}

		generateOutput(rs, writer, csvOptions, reportFormat, fullOutputFileName, report, locale);
	}

	/**
	 * Generates csv output for data in the given resultset
	 *
	 * @param rs the resultset that contains the data to output
	 * @param writer the writer to output to
	 * @param csvOptions the csv output options
	 * @param locale the locale that determines date format output
	 * @throws java.io.IOException
	 */
	public void generateOutput(ResultSet rs, Writer writer,
			CsvOutputUnivocityOptions csvOptions, Locale locale) throws IOException {

		logger.debug("Entering generateOutput");

		ReportFormat reportFormat = null;
		String fullOutputFileName = null;
		Report report = null;
		generateOutput(rs, writer, csvOptions, reportFormat, fullOutputFileName, report, locale);
	}

	/**
	 * Generates fixed width output for data in the given resultset
	 *
	 * @param rs the resultset that contains the data to output
	 * @param writer the writer to output to. If html report format is required,
	 * a writer must be supplied
	 * @param csvOptions the csv options
	 * @param reportFormat the report format to use
	 * @param fullOutputFileName the output file name to use
	 * @param report the report object for the report being run
	 * @param locale the locale that determines date format output
	 * @throws java.io.IOException
	 */
	private void generateOutput(ResultSet rs, Writer writer,
			CsvOutputUnivocityOptions csvOptions, ReportFormat reportFormat,
			String fullOutputFileName, Report report, Locale locale) throws IOException {

		Objects.requireNonNull(rs, "rs must not be null");
		Objects.requireNonNull(csvOptions, "csvOptions must not be null");

		//https://stackoverflow.com/questions/37556698/mysql-dump-character-escaping-and-csv-read
		//https://stackoverflow.com/a/36974864/3274227
		//https://github.com/uniVocity/univocity-parsers/issues/133#issuecomment-278208696
		//https://stackoverflow.com/questions/41099391/saving-a-dinamic-sql-query-to-csv
		ObjectRowWriterProcessor processor = new ObjectRowWriterProcessor();
		csvOptions.initializeProcessor(processor, locale);

		//http://docs.univocity.com/parsers/2.0.0/com/univocity/parsers/csv/CsvWriterSettings.html
		//http://docs.univocity.com/parsers/2.0.0/com/univocity/parsers/common/CommonWriterSettings.html
		//http://docs.univocity.com/parsers/2.0.0/com/univocity/parsers/common/CommonSettings.html
		//https://stackoverflow.com/questions/36936943/jackson-serialize-csv-property-order
		CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
		csvWriterSettings.setRowWriterProcessor(processor);
		csvWriterSettings.setHeaderWritingEnabled(csvOptions.isIncludeHeaders());
		csvWriterSettings.setQuoteAllFields(csvOptions.isQuoteAllFields());

		//http://docs.univocity.com/parsers/2.0.0/com/univocity/parsers/csv/CsvFormat.html
		CsvFormat csvFormat = csvWriterSettings.getFormat();
		csvFormat.setDelimiter(csvOptions.getDelimiter());
		csvFormat.setQuote(csvOptions.getQuote());

		CsvRoutines csvRoutines = new CsvRoutines(csvWriterSettings);
		csvRoutines.setKeepResourcesOpen(true);

		if (writer instanceof StringWriter) {
			csvRoutines.write(rs, writer);
		} else {
			if (reportFormat.isHtml()) {
				writer.write("<pre>");
				csvRoutines.write(rs, writer);
				writer.write("</pre>");
			} else {
				try (FileOutputStream fout = new FileOutputStream(fullOutputFileName)) {
					if (reportFormat == ReportFormat.csv) {
						csvRoutines.write(rs, fout);
					} else if (reportFormat == ReportFormat.csvZip) {
						String filename = FilenameUtils.getBaseName(fullOutputFileName);
						FilenameHelper filenameHelper = new FilenameHelper();
						String zipEntryFilenameExtension = filenameHelper.getCsvExtension(report);
						String zipEntryFilename = filename + "." + zipEntryFilenameExtension;
						ZipEntry ze = new ZipEntry(zipEntryFilename);
						try (ZipOutputStream zout = new ZipOutputStream(fout)) {
							zout.putNextEntry(ze);
							csvRoutines.write(rs, zout);
						}
					}
				}
			}
		}

	}

}
