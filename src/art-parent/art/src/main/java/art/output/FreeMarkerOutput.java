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
import art.reportoptions.TemplateResultOptions;
import art.reportparameter.ReportParameter;
import art.servlets.Config;
import art.utils.ArtUtils;
import art.utils.FilenameHelper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates output using the freemarker library
 *
 * @author Timothy Anyona
 */
public class FreeMarkerOutput {

	private static final Logger logger = LoggerFactory.getLogger(FreeMarkerOutput.class);

	private String contextPath;
	private Locale locale;
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
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the contextPath
	 */
	public String getContextPath() {
		return contextPath;
	}

	/**
	 * @param contextPath the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Generates output, updating the writer with the final output
	 *
	 * @param report the report to use, not null
	 * @param writer the writer to output to, not null
	 * @param reportParams the report parameters
	 * @param reportFormat the report format to use
	 * @param fullOutputFileName the output file name to use
	 * @throws java.sql.SQLException
	 * @throws java.io.IOException
	 * @throws freemarker.template.TemplateException
	 */
	public void generateOutput(Report report, Writer writer,
			List<ReportParameter> reportParams, ReportFormat reportFormat,
			String fullOutputFileName)
			throws SQLException, IOException, TemplateException {

		//set objects to be passed to freemarker
		Map<String, Object> variables = new HashMap<>();

		//pass report parameters
		if (reportParams != null) {
			for (ReportParameter reportParam : reportParams) {
				String paramName = reportParam.getParameter().getName();
				variables.put(paramName, reportParam);
			}

			variables.put("params", reportParams);
		}

		generateOutput(report, writer, variables, reportFormat, fullOutputFileName);
	}

	/**
	 * Generates output, updating the writer with the final output
	 *
	 * @param report the report to use, not null
	 * @param writer the writer to output to, not null
	 * @param variables the objects to be passed to the template
	 * @throws java.io.IOException
	 * @throws freemarker.template.TemplateException
	 * @throws java.sql.SQLException
	 */
	public void generateOutput(Report report, Writer writer,
			Map<String, Object> variables)
			throws IOException, TemplateException, SQLException {

		ReportFormat reportFormat = ReportFormat.html;
		String fullOutputFileName = null;
		generateOutput(report, writer, variables, reportFormat, fullOutputFileName);
	}

	/**
	 * Generates output, updating the writer with the final output
	 *
	 * @param report the report to use, not null
	 * @param writer the writer to output to, not null
	 * @param variables the objects to be passed to the template
	 * @param reportFormat the report format
	 * @param fullOutputFileName the output file name to use
	 * @throws java.io.IOException
	 * @throws freemarker.template.TemplateException
	 * @throws java.sql.SQLException
	 */
	public void generateOutput(Report report, Writer writer,
			Map<String, Object> variables, ReportFormat reportFormat,
			String fullOutputFileName)
			throws IOException, TemplateException, SQLException {

		logger.debug("Entering generateOutput: report={}, reportFormat={},"
				+ " fullOutputFileName='{}'", report, reportFormat, fullOutputFileName);

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");

		if (variables == null) {
			variables = new HashMap<>();
		}

		TemplateResultOptions templateResultOptions;
		String options = report.getOptions();
		if (StringUtils.isBlank(options)) {
			templateResultOptions = new TemplateResultOptions();
		} else {
			templateResultOptions = ArtUtils.jsonToObject(options, TemplateResultOptions.class);
		}

		//pass report data
		if (resultSet != null) {
			boolean useLowerCaseProperties = templateResultOptions.isUseLowerCaseProperties();
			boolean useColumnLabels = templateResultOptions.isUseColumnLabels();
			RowSetDynaClass rsdc = new RowSetDynaClass(resultSet, useLowerCaseProperties, useColumnLabels);
			variables.put("results", rsdc.getRows());
		} else if (data != null) {
			variables.put("results", data);
		}

		variables.put("contextPath", contextPath);
		String artBaseUrl = Config.getSettings().getArtBaseUrl();
		variables.put("artBaseUrl", artBaseUrl);
		variables.put("locale", locale);

		String templateFileName = report.getTemplate();
		String templatesPath = Config.getTemplatesPath();
		String fullTemplateFileName = templatesPath + templateFileName;

		logger.debug("templateFileName='{}'", templateFileName);

		//need to explicitly check if template file is empty string
		//otherwise file.exists() will return true because fullTemplateFileName will just have the directory name
		if (StringUtils.isBlank(templateFileName)) {
			throw new IllegalArgumentException("Template file not specified");
		}

		//check if template file exists
		File templateFile = new File(fullTemplateFileName);
		if (!templateFile.exists()) {
			throw new IllegalStateException("Template file not found: " + templateFileName);
		}

		Configuration cfg = Config.getFreemarkerConfig();
		Template template = cfg.getTemplate(templateFileName);

		//create output
		if (reportFormat == ReportFormat.html) {
			template.process(variables, writer);
			writer.flush();
		} else {
			String output;
			try (StringWriter stringWriter = new StringWriter()) {
				template.process(variables, stringWriter);
				stringWriter.flush();
				output = stringWriter.toString();
			}
			if (reportFormat == ReportFormat.htmlFancy) {
				writer.write("<pre>");
				String escapedOutput = Encode.forHtmlContent(output);
				writer.write(escapedOutput);
				writer.write("</pre>");
			} else {
				try (FileOutputStream fout = new FileOutputStream(fullOutputFileName)) {
					if (reportFormat == ReportFormat.file) {
						fout.write(output.getBytes("UTF-8"));
					} else if (reportFormat == ReportFormat.fileZip) {
						String filename = FilenameUtils.getBaseName(fullOutputFileName);
						FilenameHelper filenameHelper = new FilenameHelper();
						String zipEntryFilenameExtension = filenameHelper.getFileReporFormatExtension(report);
						String zipEntryFilename = filename + "." + zipEntryFilenameExtension;
						ZipEntry ze = new ZipEntry(zipEntryFilename);
						try (ZipOutputStream zout = new ZipOutputStream(fout)) {
							zout.putNextEntry(ze);
							zout.write(output.getBytes("UTF-8"));
						}
					}
				}
			}
		}
	}
}
