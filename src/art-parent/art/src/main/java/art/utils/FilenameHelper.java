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
package art.utils;

import art.encryptor.Encryptor;
import art.enums.EncryptorType;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.job.Job;
import art.report.Report;
import art.reportoptions.CsvOutputArtOptions;
import art.reportoptions.FileOptions;
import art.reportoptions.GeneralReportOptions;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for generating file names for use with report output
 *
 * @author Timothy Anyona
 */
public class FilenameHelper {

	private static final Logger logger = LoggerFactory.getLogger(FilenameHelper.class);

	/**
	 * Returns the base file name (file name before extension) to be used for
	 * the given report
	 *
	 * @param report the report
	 * @param locale the locale being used
	 * @param reportParamsMap report parameters
	 * @return the base file name to be used for the given report
	 */
	private String getBaseFilename(Report report, Locale locale,
			Map<String, ReportParameter> reportParamsMap) {

		Job job = null;
		String burstId = null;
		return getBaseFilename(report, job, burstId, locale, reportParamsMap);
	}

	/**
	 * Returns the base file name (file name before extension) to be used for
	 * the given job
	 *
	 * @param job the job
	 * @param locale the locale being used
	 * @param reportParamsMap report parameters
	 * @return the base file name to be used for the given job
	 */
	private String getBaseFilename(Job job, Locale locale,
			Map<String, ReportParameter> reportParamsMap) {

		String burstId = null;
		return getBaseFilename(job.getReport(), job, burstId, locale, reportParamsMap);
	}

	/**
	 * Returns the base file name (file name before extension) to be used for
	 * the given job
	 *
	 * @param job the job
	 * @param burstId the burst id for the job
	 * @param locale the locale being used
	 * @param reportParamsMap report parameters
	 * @return the base file name to be used for the given job
	 */
	private String getBaseFilename(Job job, String burstId, Locale locale,
			Map<String, ReportParameter> reportParamsMap) {

		return getBaseFilename(job.getReport(), job, burstId, locale, reportParamsMap);
	}

	/**
	 * Returns the base file name (file name before extension) to be used for
	 * the given report or job
	 *
	 * @param report the report, not null
	 * @param job the job, may be null
	 * @param burstId the burst id for the job, may be null
	 * @param locale the locale being used
	 * @return the base file name to be used for the given report or job
	 */
	private String getBaseFilename(Report report, Job job, String burstId,
			Locale locale, Map<String, ReportParameter> reportParamsMap) {

		Objects.requireNonNull(report, "report must not be null");

		int jobId;
		String namePart;

		String reportName = report.getName();
		try {
			reportName = report.getLocalizedName(locale);
		} catch (IOException ex) {
			logger.error("Error", ex);
		}

		if (job == null) {
			jobId = 0;
			namePart = reportName;
		} else {
			jobId = job.getJobId();
			String jobName = job.getName();
			if (StringUtils.isBlank(jobName)) {
				namePart = reportName;
			} else {
				namePart = jobName;
			}
		}

		if (burstId != null) {
			namePart = namePart + "-BurstId-" + burstId;
		}

		int reportId = report.getReportId();
		String timestamp = ArtUtils.fileNameDateFormatter.format(new Date());
		final int RANDOM_CHARACTER_COUNT = 5;
		String randomPart = RandomStringUtils.randomAlphanumeric(RANDOM_CHARACTER_COUNT);

		String filename = namePart + "-" + timestamp + "-" + randomPart
				+ "-" + reportId + "-" + jobId;

		GeneralReportOptions generalReportOptions = report.getGeneralOptions();
		String fileNameOption = generalReportOptions.getFileName();
		if (StringUtils.isNotBlank(fileNameOption)) {
			String defaultFileNameIdentifier = "{default}";
			String startFileName = StringUtils.replaceIgnoreCase(fileNameOption, defaultFileNameIdentifier, filename);
			if (burstId != null) {
				String burstIdIdentifier = "{burstId}";
				startFileName = StringUtils.replaceIgnoreCase(startFileName, burstIdIdentifier, burstId);
			}
			ExpressionHelper expressionHelper = new ExpressionHelper();
			String customFileName = expressionHelper.processParameters(startFileName, reportParamsMap);
			if (StringUtils.isNotBlank(customFileName)) {
				filename = customFileName;
			}
		}

		String cleanFilename = ArtUtils.cleanBaseFilename(filename);

		return cleanFilename;
	}

	/**
	 * Returns the file name extension to be used for the given report
	 *
	 * @param report the report object
	 * @param reportFormat the report format
	 * @return the file name extension to be used for the given report
	 * @throws java.io.IOException
	 */
	private String getFilenameExtension(Report report, ReportFormat reportFormat)
			throws IOException {

		String extension;

		ReportType reportType = report.getReportType();

		if (reportType.isJxls()) {
			String jxlsFilename = report.getTemplate();
			extension = FilenameUtils.getExtension(jxlsFilename);
		} else if (reportFormat == ReportFormat.csv) {
			extension = getCsvExtension(report);
		} else if (reportFormat == ReportFormat.file) {
			extension = getFileReporFormatExtension(report);
		} else {
			extension = reportFormat.getFilenameExtension();
		}

		Encryptor encryptor = report.getEncryptor();
		if (encryptor != null && encryptor.isActive()) {
			EncryptorType encryptorType = encryptor.getEncryptorType();
			switch (encryptorType) {
				case AESCrypt:
					extension = extension + ".aes";
					break;
				case OpenPGP:
					extension = extension + ".gpg";
					break;
				default:
					break;
			}
		}

		return extension;
	}

	/**
	 * Returns the file name to be used for the given report
	 *
	 * @param report the report
	 * @param locale the locale being used
	 * @param reportFormat the report format
	 * @param reportParamsMap report parameters
	 * @return the file name to be used for the given report
	 * @throws java.io.IOException
	 */
	public String getFilename(Report report, Locale locale,
			ReportFormat reportFormat, Map<String, ReportParameter> reportParamsMap)
			throws IOException {

		String baseFilename = getBaseFilename(report, locale, reportParamsMap);
		String extension = getFilenameExtension(report, reportFormat);
		String filename = baseFilename + "." + extension;

		return filename;
	}

	/**
	 * Returns the full file name to be used for the given report
	 *
	 * @param report the report
	 * @param locale the locale being used
	 * @param reportFormat the report format
	 * @param reportParamsMap report parameters
	 * @return the full file name to be used for the given report
	 * @throws java.io.IOException
	 */
	public String getFullFilename(Report report, Locale locale,
			ReportFormat reportFormat, Map<String, ReportParameter> reportParamsMap)
			throws IOException {

		String filename = getFilename(report, locale, reportFormat, reportParamsMap);
		String exportPath = Config.getReportsExportPath();
		String fullFilename = exportPath + filename;

		return fullFilename;
	}

	/**
	 * Returns the file name to be used for the given job
	 *
	 * @param job the job
	 * @param locale the locale being used
	 * @param reportFormat the report format
	 * @param reportParamsMap report parameters
	 * @return the file name to be used for the given job
	 * @throws java.io.IOException
	 */
	public String getFilename(Job job, Locale locale, ReportFormat reportFormat,
			Map<String, ReportParameter> reportParamsMap) throws IOException {

		String baseFilename = getBaseFilename(job, locale, reportParamsMap);
		String extension = getFilenameExtension(job.getReport(), reportFormat);
		String filename = baseFilename + "." + extension;

		return filename;
	}

	/**
	 * Returns the file name to be used for the given job
	 *
	 * @param job the job
	 * @param burstId the burst id for the job
	 * @param locale the locale being used
	 * @param reportFormat the report format
	 * @param reportParamsMap report parameters
	 * @return the file name to be used for the given job
	 * @throws java.io.IOException
	 */
	public String getFilename(Job job, String burstId, Locale locale,
			ReportFormat reportFormat, Map<String, ReportParameter> reportParamsMap)
			throws IOException {

		String baseFilename = getBaseFilename(job, burstId, locale, reportParamsMap);
		String extension = getFilenameExtension(job.getReport(), reportFormat);
		String filename = baseFilename + "." + extension;

		return filename;
	}

	/**
	 * Returns the file name extension to use for a csv file. "csv" if delimiter
	 * is comma, "txt" otherwise
	 *
	 * @param report the report object
	 * @return the file name extension to use
	 * @throws java.io.IOException
	 */
	public String getCsvExtension(Report report) throws IOException {
		Objects.requireNonNull(report, "report must not be null");

		String extension;

		CsvOutputArtOptions csvOptions;
		String options = report.getOptions();
		if (StringUtils.isBlank(options)) {
			csvOptions = new CsvOutputArtOptions(); //has default values set
		} else {
			ObjectMapper mapper = new ObjectMapper();
			csvOptions = mapper.readValue(options, CsvOutputArtOptions.class);
		}

		String delimiter = csvOptions.getDelimiter();
		if (StringUtils.equals(delimiter, ",")) {
			extension = "csv";
		} else {
			extension = "txt";
		}

		return extension;
	}

	/**
	 * Returns the file name extension to use with file report format
	 *
	 * @param report the report object
	 * @return the file name extension to use
	 * @throws java.io.IOException
	 */
	public String getFileReporFormatExtension(Report report) throws IOException {
		Objects.requireNonNull(report, "report must not be null");

		String extension;

		FileOptions fileOptions;
		String options = report.getOptions();
		if (StringUtils.isBlank(options)) {
			fileOptions = new FileOptions();
		} else {
			ObjectMapper mapper = new ObjectMapper();
			fileOptions = mapper.readValue(options, FileOptions.class);
		}

		extension = fileOptions.getExtension();
		if (StringUtils.isBlank(extension)) {
			extension = "txt";
		}

		return extension;
	}

}
