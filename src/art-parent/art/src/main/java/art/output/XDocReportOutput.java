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

import art.dbutils.DatabaseUtils;
import art.enums.ReportFormat;
import art.enums.ReportType;
import art.report.Report;
import art.reportoptions.TemplateResultOptions;
import art.reportparameter.ReportParameter;
import art.runreport.GroovyDataDetails;
import art.runreport.RunReportHelper;
import art.servlets.Config;
import art.utils.ArtUtils;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates output using the xdocreport library
 *
 * @author Timothy Anyona
 */
public class XDocReportOutput {

	private static final Logger logger = LoggerFactory.getLogger(XDocReportOutput.class);

	private Locale locale;
	private String dynamicOpenPassword;
	private String dynamicModifyPassword;
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
	 * @return the dynamicOpenPassword
	 */
	public String getDynamicOpenPassword() {
		return dynamicOpenPassword;
	}

	/**
	 * @param dynamicOpenPassword the dynamicOpenPassword to set
	 */
	public void setDynamicOpenPassword(String dynamicOpenPassword) {
		this.dynamicOpenPassword = dynamicOpenPassword;
	}

	/**
	 * @return the dynamicModifyPassword
	 */
	public String getDynamicModifyPassword() {
		return dynamicModifyPassword;
	}

	/**
	 * @param dynamicModifyPassword the dynamicModifyPassword to set
	 */
	public void setDynamicModifyPassword(String dynamicModifyPassword) {
		this.dynamicModifyPassword = dynamicModifyPassword;
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
	 * Generates report output
	 *
	 * @param report the report to use, not null
	 * @param reportParams the report parameters
	 * @param reportFormat the report format for the report
	 * @param outputFileName the full output file name to use for the generated
	 * report
	 * @throws java.sql.SQLException
	 * @throws fr.opensagres.xdocreport.core.XDocReportException
	 * @throws java.lang.Exception
	 */
	public void generateReport(Report report, List<ReportParameter> reportParams,
			ReportFormat reportFormat, String outputFileName) throws Exception {

		logger.debug("Entering generateReport");

		Objects.requireNonNull(report, "report must not be null");
		Objects.requireNonNull(outputFileName, "outputFileName must not be null");
		Objects.requireNonNull(reportFormat, "reportFormat must not be null");

		Connection conn = null;

		try {

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
				throw new RuntimeException("Template file not found: " + fullTemplateFileName);
			}

			//load doc
			ReportType reportType = report.getReportType();
			TemplateEngineKind templateEngineKind;

			if (reportType.isXDocReportFreeMarker()) {
				templateEngineKind = TemplateEngineKind.Freemarker;
			} else if (reportType.isXDocReportVelocity()) {
				templateEngineKind = TemplateEngineKind.Velocity;
			} else {
				throw new IllegalArgumentException("Unexpected report type: " + reportType);
			}

			InputStream in = new FileInputStream(fullTemplateFileName);
			IXDocReport xdocReport = XDocReportRegistry.getRegistry().loadReport(in, templateEngineKind);

			//set objects to be passed to template
			IContext context = xdocReport.createContext();

			//pass report parameters
			if (reportParams != null) {
				for (ReportParameter reportParam : reportParams) {
					String paramName = reportParam.getParameter().getName();
					context.put(paramName, reportParam);
				}
			}

			context.put("params", reportParams);
			context.put("locale", getLocale());

			if (reportType.isXDocReportVelocity()) {
				NumberTool numberTool = new NumberTool();
				context.put("numberTool", numberTool);

				DateTool dateTool = new DateTool();
				context.put("dateTool", dateTool);
			}

			TemplateResultOptions templateResultOptions;
			String reportOptions = report.getOptions();
			if (StringUtils.isBlank(reportOptions)) {
				templateResultOptions = new TemplateResultOptions();
			} else {
				templateResultOptions = ArtUtils.jsonToObject(reportOptions, TemplateResultOptions.class);
			}

			RunReportHelper runReportHelper = new RunReportHelper();

			//pass report data
			if (resultSet != null) {
				ArtJxlsJdbcHelper jdbcHelper = new ArtJxlsJdbcHelper(templateResultOptions);
				List<Map<String, Object>> rows = jdbcHelper.handle(resultSet);
				context.put("results", rows);

				//add metadata to indicate results fields are list fields
				List<String> columnNames = runReportHelper.getColumnNames(resultSet, templateResultOptions);
				FieldsMetadata metadata = new FieldsMetadata();
				for (String columnName : columnNames) {
					String metadataFieldName = "results." + columnName;
					metadata.addFieldAsList(metadataFieldName);
				}
				xdocReport.setFieldsMetadata(metadata);
			} else if (data != null) {
				context.put("results", data);
				GroovyDataDetails dataDetails = RunReportHelper.getGroovyDataDetails(data, report);
				List<String> columnNames = dataDetails.getColumnNames();
				FieldsMetadata metadata = new FieldsMetadata();
				for (String columnName : columnNames) {
					String metadataFieldName = "results." + columnName;
					metadata.addFieldAsList(metadataFieldName);
				}
				xdocReport.setFieldsMetadata(metadata);
			}

			conn = runReportHelper.getEffectiveReportConnection(report, reportParams);
			ArtJxlsJdbcHelper jdbcHelper = new ArtJxlsJdbcHelper(conn, templateResultOptions);
			context.put("jdbc", jdbcHelper);

			//create output
			try (OutputStream out = new FileOutputStream(new File(outputFileName))) {
				if ((reportType.isXDocReportDocx() && reportFormat == ReportFormat.docx)
						|| (reportType.isXDocReportOdt() && reportFormat == ReportFormat.odt)
						|| reportType.isXDocReportPptx()) {
					//no conversion
					xdocReport.process(context, out);
				} else {
					Options options = null;
					if (reportType.isXDocReportDocx()) {
						switch (reportFormat) {
							case html:
								options = Options.getTo(ConverterTypeTo.XHTML).via(ConverterTypeVia.XWPF);
								break;
							case pdf:
								options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.XWPF);
								break;
							default:
								throw new IllegalArgumentException("Unexpected report format: " + reportFormat);
						}
					} else if (reportType.isXDocReportOdt()) {
						switch (reportFormat) {
							case html:
								options = Options.getTo(ConverterTypeTo.XHTML).via(ConverterTypeVia.ODFDOM);
								break;
							case pdf:
								options = Options.getTo(ConverterTypeTo.PDF).via(ConverterTypeVia.ODFDOM);
								break;
							default:
								throw new IllegalArgumentException("Unexpected report format: " + reportFormat);
						}
					} else {
						throw new IllegalArgumentException("Unexpected report type/report format: " + reportType + "/" + reportFormat);
					}

					xdocReport.convert(context, options, out);
				}
			}

			if (reportFormat == ReportFormat.pdf) {
				PdfHelper pdfHelper = new PdfHelper();
				pdfHelper.addProtections(report, outputFileName, dynamicOpenPassword, dynamicModifyPassword);
			}

		} finally {
			DatabaseUtils.close(conn);
		}
	}
}
